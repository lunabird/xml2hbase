/*  XQEngine Copyright (C) 2002-2005 Fatdog Software Inc. [howardk@fatdog.com]
 *	--------------------------------------------------------------------------
 *  This file is part of XQEngine and is distributed under the terms of the
 *  GNU General Public License. See additional info at the end of this file. */

package com.fatdog.xmlEngine;

import java.io.FileNotFoundException;
import java.util.Hashtable;
import java.util.Vector;

import com.fatdog.xmlEngine.exceptions.*;
import com.fatdog.xmlEngine.javacc.*;
import com.fatdog.xmlEngine.words.*;

	/**
	 * Provides the low-level implementations for XQuery's built-in functions.
	 * 
	 * @author Howard Katz, howardk@fatdog.com
	 * @version 0.61
	 */

public class FandO extends WordBreaker implements IWordHandler, XQueryParserTreeConstants		
{
    TreeWalker      m_treeWalker;
    IndexManager    m_indexer;
    
    static final int    STRING  = 1;
    static final int    INT     = 2;
    static final int    ITEM    = 3;
    static final int    NODE    = 4;
    
    static final int    NOT_EMPTY_OK    = 0;    
    static final int    EMPTY_OK        = 1;
    static final int    ONE_ONLY        = 2;
    static final int    STAR            = 3;
    
    static final boolean ENDS_IN        = true;
                            
   // static final int    RELPATH     = 0;
   // static final int    QNAME       = 1;
   // static final int    STRING_LIT  = 2;
    
    Hashtable m_functions = new Hashtable();
    
    String[] functions = {	"count",            //0
                       		"doc",              //1
                           	"contains-word",    //2
                            "cw",               //3
                            "empty",			//4
                            "exists",			//5
                            "string",			//6
                            "true",				//7
                            "fn:true",			//8
                            "false",			//9
                            "fn:false",			//10
                            "not",				//11
                            "doc-name",			//12
                            "document-name",	//13
                            "root",				//14
                            "name",				//15
                            "chr",				//16
                            "boolean",			//17
							"following-sibling" //18
                        };
                                
    boolean[] mustHaveArg = {	true,	//0
   								true,	//1
   								true,	//2
   								true,	//3
   								true,	//4
   								true,	//5
   								false,	//6
   								false,	//7
   								false,	//8
   								false,	//9
   								false,	//10
   								true,	//11
   								true, 	//12
   								true,	//13
   								false,	//14
   								false,	//15
   								false,	//16
   								false,	//17
								true	//18
   							};
   								 	
   	// node is present for functions that require static context (eg string())
   	
   	/**
   	 * The public {@link FandO} interface for built-in functions.
   	 */
                                             
    public ResultList dispatchNamedFunction( SimpleNode node, String name, SimpleNode args ) throws InvalidQueryException
    //--------------------------------------------------------------------------------------
    {
        if ( !m_functions.containsKey( name ) )
        	if ( m_treeWalker.isNamedFunction( node, "last" ))
        	
				throw new InvalidQueryException( 
					"\nFandO.dispatchNamedFunction(): 'last()' can currently only be used in conjunction with 'position()'" );        	
        	else
        	
            	throw new InvalidQueryException( 
                	"\nFandO.dispatchNamedFunction(): no such function '" + name + "()'" );
                 
		int funcId = ((Integer) m_functions.get( name )).intValue();  
		
		if ( args == null && mustHaveArg[ funcId ] )
			throw new InvalidQueryException( 
			 "\nFandO.dispatchNamedFunction(): The function " + name + "() takes an argument" );
		
		SimpleNode lhs = null, rhs = null;
		
		SimpleNode context = null;
		
		if ( mustHaveArg[ funcId ])
		{		
	        lhs =  (SimpleNode) args.jjtGetChild(0);
	        rhs =  (SimpleNode) args.jjtGetChild(1);                 
		}
		else
			context = (SimpleNode) node.jjtGetParent();
		
        switch( funcId )
        {
            case 0 :	return count( m_treeWalker.eval( args ) );
            case 1 : 	return doc_dispatcher( args.getText() );
            
            case 2 : 	return contains_word( lhs, rhs );
            case 3 : 	return contains_word( lhs, rhs );
            
            case 4 : 	return empty( m_treeWalker.eval(args));
            case 5 : 	return exists( m_treeWalker.eval(args));
            
            case 6 : 	return string( context, args );
            
            case 7 : 
            case 8 : 
            case 9 :
            case 10: 	return booleanCtor( name );
            
            case 11:	return not( m_treeWalker.eval(args) );
            
            case 12:	return docName( args );	// doc-name
            case 13:	return docName( args );	// document-name
            
            case 14:	return root( context, args );
            
            case 15:	return name( context, args );
            
            case 16:	return chr( args.getText() );
            
            case 17:	return boolean_( m_treeWalker.eval( args ));
            
            case 18:	return followingSibling( m_treeWalker.eval( args ) );
            
            default : 
         
				throw new IllegalArgumentException(
					"\nFandO.dispatchNamedFunction() program error: no function corresponding to funcId " + funcId );
        }    
    }

    public FandO( TreeWalker walker, boolean debug )
    //----------------------------------------------
    {
        m_treeWalker    = walker;
        m_indexer   	= walker.getIndexer();

        initializeBuiltIns( debug );
    }
    
    /*  12june03
        for now ignore signatures. one name, one function, no overloading
    */    
    void initializeBuiltIns( boolean debug )
    //--------------------------------------
    {
        int numFunctions = functions.length;
        
        if ( debug )
        {
            System.out.println( "FandO.initializeBuiltIns(): " + numFunctions + " functions" );
            for( int i = 0; i < numFunctions; i ++ )
                System.out.println( functions[ i ] + "()" );
        }
        
        for( int i = 0; i < numFunctions; i ++ )
            m_functions.put( functions[ i ], new Integer( i ) );
    }
    
    // doc() has two signatures -- dispatch to the proper one
    
    boolean isIntegerArg( String arg )
    //--------------------------------
	{
		if ( arg == null || arg.length() == 0 )
			return false;
			
		boolean isIntegerArg = true;
		for( int i = 0; i < arg.length(); i++ )
			if ( arg.charAt( i ) < '0' || arg.charAt(i) > '9')
				isIntegerArg = false;
    			
    	return isIntegerArg;
	}
	
	ResultList name( SimpleNode context, SimpleNode arg ) throws InvalidQueryException
	//---------------------------------------------------
	{
		if ( arg == null )
		{		
			throw new InvalidQueryException( 
					"\nname() function: argument must be supplied -- ie, context cannot currently be inferred" );
		}
		
		ResultList results = m_treeWalker.eval( arg );
		
		if ( results.getNumValidItems() == 0 )
		
			return new ResultList( m_treeWalker ).newString( "" );
			
		else if ( results.getNumValidItems() > 1 )
		
			throw new InvalidQueryException( 
					"\nname() function: argument must be the empty sequence or a single node" );
					
		ResultList item = results.subscript(1);
		int[] valueType = item.valueType( 0 );
		
		String name = "";
		
		if ( item.isElementNode( valueType ))
		
			name = item.headDocument().getTree().getElementName( valueType[ 0 ] );
			
		else if ( item.isAttributeNode( valueType ))
		
			name = item.headDocument().getTree().getAttributeName( valueType[ 0 ] );
		
		// return "" string for document nodes and ()
		
		return new ResultList( m_treeWalker ).newString( name );	
	}
	
	ResultList docName( SimpleNode arg ) throws InvalidQueryException
	//----------------------------------
	{
		ResultList results;
		
		if ( isIntegerArg( arg.getText() ))
		{
			int docId = Integer.parseInt( arg.getText() );
						
			return new ResultList( m_treeWalker ).newString( m_indexer.getDocumentName( docId ));
		}

		results = m_treeWalker.eval( arg );
		
		if ( results.getNumValidItems() == 0 )		// return empty sequence

			return results;
				
		else if ( results.getNumValidItems() > 1 )	// an error
				
			throw new InvalidQueryException( 
						"\nFandO.document-name(): function can only be applied to a single node" );					
		else 
		{
					
			ResultList item = results.subscript(1);
			int[] valueType = item.valueType( 0 );

			if ( valueType[ 1 ] != DocItems.DOC_NODE )	
			{	
				throw new InvalidQueryException( 
						"\nFandO.document-name(): argument must be the empty sequence or a single document node" );
			}
			
			int docId = item.headDocument().getId();
			
			return new ResultList( m_treeWalker ).newString( m_indexer.getDocumentName( docId ));
		}
	}
	

    ResultList doc_dispatcher( String arg ) throws NumberFormatException, InvalidQueryException
    //-------------------------------------
    {
    	if ( isIntegerArg(arg) )
    	{
   			return doc( Integer.parseInt( arg ));
    	}

		return doc( arg );
    }
    
    
	/**
	 * Low-level routine corresponding to boolean constructor functions fn:true/fn:false.
	 * 
	 * @param name "true", "fn:true", "false", or "fn:false"
	 */
	
	
	ResultList booleanCtor( String name )
	//-----------------------------------
	{
		boolean isTrue = name.equals("true") || name.equals("fn:true");
    	
		int[] valueType = { isTrue ? -1 : 0, DocItems. BOOLEAN };

		return new ResultList( m_treeWalker, valueType );
	}
	
    
	/**
	 * Convert the item sequence to a String. 
	 * 
	 * <P>Low-level routine corresponding to F&O fn:string() accessor.
	 * 
	 * @param context The query tree JJTFunctionCall node, for convenience
	 * @param arg The query tree JJTSimpleNode expression to be string'ified. If a JJTSequnce, evaluate the <code>RHS</code>
	 * operand as a boolean <code>addInterSpace</code>, indicating internal items should be separated by spaces
	 * @return The results of converting the <code>arg</code> expression to a string
	 * @throws InvalidQueryException
	 */
    
	ResultList string( SimpleNode context, SimpleNode arg ) throws InvalidQueryException
	//-----------------------------------------------------
	{
		if ( arg == null )
		{
			if ( context.id() == JJTSTARTNODE )
	    	
				throw new InvalidQueryException(
					"\nFAndO.string(): No context present when string() function is at query root");
	    			
			throw new InvalidQueryException( 
				"\nFAndO.string(): Cannot currently evaluate string() function in 'context' mode");
		}
    	
		boolean addInterSpace = false;
    	
		if ( arg.id() == JJTSEQ )
		{
			ResultList doSpace = m_treeWalker.eval( (SimpleNode ) arg.jjtGetChild(1 ) );
			addInterSpace = doSpace.booleanValue();
    		
			arg = (SimpleNode) arg.jjtGetChild(0);
		}

		ResultList results = m_treeWalker.eval( arg );
		
		if ( results.getNumValidItems() == 0 )
		
			return results.newString( "" );
		
		return results.string_value( addInterSpace );
	}
    
	/**
	 * <P>Low-level routine corresponding to F&O fn:exists().
	 * 
	 * Return a {@link ResultList} containing a single boolean result
	 * of value <code>true()</code> if the sequence contains any items.
	 */
	
	ResultList exists( ResultList itemSequence )
	//------------------------------------------
	{
		int count = itemSequence.getNumValidItems();
    	
		int[] valueType = { (count == 0 )? 0 : -1, DocItems. BOOLEAN };

		return new ResultList( m_treeWalker, valueType );
	}
    
	/**
	 * <P>Low-level routine corresponding to F&O fn:empty().
	 * 
	 * Return a {@link ResultList} containing a single boolean result
	 * of value <code>true()</code> if the sequence contains no items.
	 */
    
	ResultList empty( ResultList itemSequence )
	//-----------------------------------------
	{
		int count = itemSequence.getNumValidItems();
    	
		int[] valueType = { (count == 0 )? -1 : 0, DocItems. BOOLEAN };

		return new ResultList( m_treeWalker, valueType );
	}
    
    /**
     * Return the document node of a previously indexed document by its docId.
     * 
     * @param docId
     * @return A {@link ResultList} object containing the document node.
     * @throws InvalidQueryException if the argument doesn't correspond to a valid document.
     */
    
    ResultList doc( int docId ) throws InvalidQueryException
    //-------------------------
    {
    	if ( m_indexer.isValidDocId( docId ) )
    	{
			return new ResultList( m_treeWalker ).newDocumentNode( docId );
    	}
    	
    	throw new InvalidQueryException( "\ndoc() function called with invalid integer argument" );
    }
    
    /*  first we check to see whether the doc is already indexed. If so, we construct and return a doc node.
        if it's not already indexed, we attempt to do so, calling setDocument() on it as appropriate. That
        either returns its docId, and we construct and return a doc node as above, or we return a
        FNF error wrapped as an InvalidQueryException
        
        NOTA-1:     we might want to review the exceptions being thrown on a failed setDocument() call
        NOTA-2:     This calls setDocument() only, so we only call non-explicit or scheme-driven content
                    Since there's no one naming pattern for explicit documents, we need to pass in an
                    <explicitDocumentFlag> to the indexer on indexing, so we can then query isDocumentExplicit() 
                    below, in order to then call setExplicitDocument() 
    */
    
	/**
	 * Return the document node of a previously indexed document by its address.
	 * 
	 * <P>The routine first checks whether the document has been previously indexed.
	 * If it hasn't been, it attempts to index it before returning its document node.
	 * 
	 * @param address fileName of the document
	 * @return A {@link ResultList} object containing the document node.
	 * @throws InvalidQueryException if the argument doesn't correspond to a valid document.
	 * @see XQEngine#setDocument(String) for information on allowable address types.
	 */
	
    ResultList doc( String address ) throws InvalidQueryException
    //-------------------------------
    {
        int docId;
        
        // CASE I:  this document has not been indexed. Attempt to index it, get
        //          its new docID, and construct and return it as a documentNode. 
        //			If indexing is unsuccessful, throw
                
        if ( ( docId = m_indexer.getDocId( address ) ) == IndexManager. DOC_NOT_INDEXED )
        {
        	
        	if ( address.length() == 0 )
        	{
        		throw new CategorizedInvalidQueryException(
        		
        			"FODC0002", "Invalid argument to doc() function" );
        	}
        	
            try
            {
                docId = m_indexer.getEngine().setDocument( address, true );
                
                return new ResultList( m_treeWalker ).newDocumentNode( docId );
            }
            catch( java.lang.NullPointerException npe )
            {
            	return new ResultList( m_treeWalker ); // null results
            }
            catch ( FileNotFoundException fnfEx )               
            { 
				return new ResultList( m_treeWalker ); // null results
            }
            catch ( CantParseDocumentException cpdEx )          
            {
                throw new InvalidQueryException( "\ndoc(" + address + ") called, document not parseable" );
            }
            catch ( MissingOrInvalidSaxParserException mipEx ) 
            {
                // can't happen on a query (said optimistically!)
            }
        }
        
        // CASE II: the document has been indexed. Retrieve its docId and ditto        
        
        return new ResultList( m_treeWalker ).newDocumentNode( docId );
    }
    
    // fn:count($srcval as item*) as xs:integer 
    
    /**
     * Count the number of items in the sequence.
     * 
     * <P>The number of items is returned as a single int encapsulated in a <code>ResultList</code>.
     */
    
	ResultList count( ResultList sequence ) throws InvalidQueryException
    //-------------------------------------
    {
    	int[] valueType = { sequence.getNumValidItems(), DocItems.INT };

    	return new ResultList( m_treeWalker, valueType );
    }  
    
    // user has supplied various word 'quanta' and possible boolean flags (one only at this time)
    
    void addParams( SimpleNode p, Vector vWords, Vector vFlags ) throws InvalidQueryException
    //----------------------------------------------------------
    {
		SimpleNode lhs =  (SimpleNode) p.jjtGetChild(0);
		SimpleNode rhs =  (SimpleNode) p.jjtGetChild(1); 
		
		if ( lhs.id() != JJTSTRINGLIT )
		{
			throw new InvalidQueryException(	
					
				"\nFandO.contains_word_addParams(): Only allowable parameters are one or more words, "
													+ "followed by a single boolean flag for 'ignoreCase'" );
		}
		
		parseForMultipleWords( lhs.getText(), vWords );
		
		if ( rhs.id() == JJTSEQ )
		
			addParams( rhs, vWords, vFlags );
			
		else if ( rhs.id() == JJTSTRINGLIT )
		
			parseForMultipleWords( rhs.getText(), vWords );	
			
		else if ( rhs != null )
		{	
			vFlags.addElement( new Boolean( m_treeWalker.eval( (SimpleNode) rhs ).booleanValue()) );
		}
    }
    
    // contains-word( path, "word" ) 
    // contains-word( path, "word", ignoreCaseFlag )
    // contains-word( path, "word", "word", caseIsSignificantFlag )
    // caseIsSignificantFlag default is false()
    
    // NOTA: any flag result that doesn't eval to true() is false 
    // (including true: it's evaled as a QName, which returns a null result => false)
    
/*

contains-word( //author, "w" )
contains-word( //author, "w", "stevens" )
contains-word( //author, "w", false() )

*/
	
	// user has provided a list of userWords; if these have internal
	// punctuation, we need to parse them into individual words
	
	void parseForMultipleWords( String userWord, Vector vWords )
	//----------------------------------------------------------
	{			
		IntList wordParams = new IntList();
		
		char[] userChars = userWord.toCharArray();
		
		characters( userChars, 0, userChars.length, -1, wordParams );
		
		if ( wordParams.count() == 1 )
		{
			vWords.addElement( userChars );
		}
		else
		{
			for( int i = 0; i < wordParams.count(); i++ )
			{
				int[] startLen = wordParams.getRef_Both( i );			
				String word = new String( userChars, startLen[ 0 ], startLen[ 1 ] );
				
				vWords.addElement( word.toCharArray() );
			}
		}
	}
	
	/**
	 * Return a sequence of nodes containing the word(s) of interest.
	 * 
	 * <P>NOTA: This is the low-level version of the contains-word() function, 
	 * whose signature is :
	 * <P>
	 * <OL>
	 * <LI>a valid XQuery location path,
	 * <LI>one or more words, separated by commas, and optionally
	 * <LI>a boolean argument. If this evaluates to <code>true()</code>, the query
	 * will be case-sensitive (the default is <code>false()</code>).
	 * </OL>
	 * 
	 * <P>The location path argument will be evaluated to a node sequence; each node
	 * in the sequence will be inspected to see whether it contains the <code>AND</code>ed
	 * product of all word arguments, and returned as part of the resulting {@link ResultList} if so.
	 * 
	 * <P>The word arguments may contain embedded whitespace and/or punctuation,
	 * which the query engine will view as additional word separators. This provides three ways of
	 * specifying word arguments: as individual words, as concatenated groups of words separated by
	 * internal whitespace and/or punctuation, or as a combination of the two.
	 * 
	 * @param path Must evaluate to a legal XQuery location path
	 * @param wordsPlusFlag One or more words (given here as a single SimpleNode root for the query subtree, 
	 * plus an optional caseIsSignificant flag).
	 * @return A ResultList object containing a node sequence satisfying the query.
	 * @throws InvalidQueryException If the argument list doesn't contain one or more words.
	 */
	
	public ResultList contains_word( SimpleNode path, SimpleNode wordsPlusFlag ) throws InvalidQueryException
	//--------------------------------------------------------------------------
	{
		Vector vWords 	= new Vector();
		Vector vFlags	= new Vector();
		
		registerWordHandler( this );	
		setIgnoreCase( false );	// we'll do our own lowercasing (if ignoreCase) for efficiency reasons
		
		IntList wordStarts = new IntList();	
		
		if ( wordsPlusFlag == null )
		
				throw new InvalidQueryException(	
					
					"\nFandO.contains_word(): word must be specified as the second parameter" );
	
		if ( wordsPlusFlag.id() == JJTSTRINGLIT )
		{
			parseForMultipleWords( wordsPlusFlag.getText(), vWords );
		}
		else
		{
			addParams( wordsPlusFlag, vWords, vFlags );
		}
			
		if ( vFlags.size() == 0 )
		{
			vFlags.addElement( new Boolean( false )); // DEFAULT = not case-sensitive
		}
			
		return m_treeWalker.eval( path ).contains_word( vWords, vFlags );	
	}
    
    /**
     * Supply lists of word starts and lengths for the function contains-word().
     * <P>{@link FandO} is a <code>WordBreaker</code>; <code>WordBreaker.characters()</code>
     * calls this routine.
     */
    
	public void newWord( char[] word, int wordStart, int wordEnd, int parent, IntList ints )
	//--------------------------------------------------------------------------------------
	{
		ints.addRef_2( wordStart, wordEnd - wordStart );
	}
    
 	void evalFunctionReturnType( SimpleNode functionOp, boolean[] types )
	//-------------------------------------------------------------------
	{                
		SimpleNode lhs =  (SimpleNode) functionOp.jjtGetChild(0);
		SimpleNode rhs =  (SimpleNode) functionOp.jjtGetChild(1);   
		
		throw new IllegalArgumentException( "\nFandO.evalFunctionReturnType(): Not Yet Implemented" );
	}
	
	/**
	 * Return the Effective Boolean Value of the argument
	 */
	ResultList boolean_( ResultList args )
	//-----------------------------------
	{
		return new ResultList( m_treeWalker ).newBoolean( args.effectiveBooleanValue() );
	}
	
	/**
	 * Return a boolean result (encapsulated as a {@link ResultList} indicating the negation of its argument
	 */
	
	ResultList not( ResultList args )
	//-------------------------------
	{
		return new ResultList( m_treeWalker ).newBoolean( ! args.effectiveBooleanValue() );
	}
	
	/**
	 * 
	 * Return the root node of node.
	 * 
	 * <P>Two signatures:
	 * <dl>
	 * 		<dt>root() as node()</dt>
	 * 		<dd>use context node. if former absent or not a node: "err:FOTY0011, context item is not a node"</dd>
	 *
	 * 		<dt>root( $arg as node()? ) as node()?</dt>
	 * 		<dd>if $arg is (), return (). 
	 * 			<BR/>if $arg is a document node, return $arg
	 * 			<BR/>else return root of the tree</dd>
	 * </dl>
	 *
	 */
	
	ResultList root( SimpleNode context, SimpleNode arg ) throws InvalidQueryException
	//---------------------------------------------------
	{
		ResultList results = null;
		
		// non-contextual form of the function
		
		if ( arg != null )
		{
			results = m_treeWalker.eval( arg );
			
			if ( results.getNumValidItems() == 0 )
			
				// return an empty sequence
				
				return results;
				
			else if ( results.getNumValidItems() > 1 )
			
				// can take root of only one item
				
				throw new InvalidQueryException( 
						"\nroot() function can only be applied to a single node" );
					
			else
			{
				int[] item = results.valueType( 0 );
				
				int itemType = item[ 1 ];
				
				// has to be a node
				
				if ( itemType < DocItems.DOC_NODE )
				{
					throw new InvalidQueryException( 
							"\nroot() function can only be applied to node items" );
				}
				
				if ( itemType == DocItems.DOC_NODE )
				
					return results;
					
				else 
				{
					item[ 0 ] = DocItems.DOC_NODE;
					item[ 1 ] = DocItems.DOC_NODE;
					
					// NOTA: we can do this because count (1) stays the same
					
					results.headDocument().getIntList().setRawList( item );
				}
			}
		}
		
		else // arg is null; context is set by the location path
		{
			if ( context.id() != JJTRELPATH )
			
				throw new InvalidQueryException( 
						"\nFandO.root(): context expected to be set by a RelPath operator" );	
			
			throw new InvalidQueryException( 
					"\nFandO.root(): argument must be supplied -- ie, context cannot currently be inferred" );
		}
		
		return results;
	}
	
	ResultList chr( String id ) throws InvalidQueryException
	//-------------------------
	{
		if ( id == null )
		
			return new ResultList( m_treeWalker ).newString( "" );
			
		if ( !isIntegerArg( id ))
			
			throw new InvalidQueryException( 
					"\nFandO.chr(): argument must be an integer" );
					
		char[] ch = new char[ 1 ];
		
		ch[ 0 ] = (char) Integer.parseInt( id );
		
		return new ResultList( m_treeWalker ).newString( new String( ch ) );
	}
	
	ResultList followingSibling( ResultList nodes )
	//---------------------------------------------
	{
		return nodes.followingSibling();
	}
}

/*
 *  XQEngine is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  XQEngine is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with XQEngine; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */