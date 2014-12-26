/*  XQEngine Copyright (C) 2002-2005 Fatdog Software Inc. [howardk@fatdog.com]
 *	--------------------------------------------------------------------------
 *  This file is part of XQEngine and is distributed under the terms of the
 *  GNU General Public License. See additional info at the end of this file. */
 
package com.fatdog.xmlEngine;

import com.fatdog.xmlEngine.words.*;
import com.fatdog.xmlEngine.exceptions.CategorizedInvalidQueryException;
import com.fatdog.xmlEngine.exceptions.InvalidQueryException;

import java.io.PrintWriter;
import java.util.Vector;

	/**
	 * A collection of items corresponding to one document's contribution to a <code>ResultList</code>.
	 * The items are "proper" nodes from the index if if <code>m_id</code> >= 0; 
	 * constructed nodes and atomics if <code>m_id</code> = QUERY_DOCUMENT.
	 * 
	 * @author Howard Katz, howardk@fatdog.com
	 * @version 0.69
	 */

public class DocItems extends WordBreaker implements IWordHandler
{
    int             m_id;
    
    IndexManager	m_indexer;
    IntList        	m_list;

    DocItems   		m_nextDoc;
    DocItems   		m_priorDoc;
    
    int             m_numValidItems;
    int             m_numTotalItems;    
    
    final static int LEAF   = 0;
    final static int PARENT = 1;
    
    // id of a DocItems object that holds constructed nodes and atomics
    public final static int QUERY_DOCUMENT	= -1; 
    
    final static int 		DEFAULT_LIST_ALLOCS = 8;  
    
    // TODO: review use of this flag. see also NodeTree
    final static boolean 	LINEFEED_AFTER_INDIVIDUAL_ITEMS = false;
    
    // NOTE overlap of following four in NodeTree
    
	public final static int     ELEM       	 	= 0;
	public final static int     ATTR        	= 1;
	public final static int     TEXT        	= 2;
    
	public final static int		DOC_NODE    	= -1;    
	public final static int     INT         	= -2;	// value == value of in
	public final static int     STRING      	= -3;	// literal text; value points into RList hashtable
														// could also result from concatenated text via string(ELEM)
	public final static int		BOOLEAN			= -4;	// 0 == false; -1 == true

	public final static int		ATTR_TEXT		= -5;	// look up attr in NodeTree but emit text only
	public final static int		TEXT_AS_STRING	= -6;	// started life as TEXT, but string() function changed its type

	public final static int		NULL_ORDERSPEC	= -7;
	
	public final static int		DOUBLE			= -8;	// new 11may05
	public final static int		DECIMAL			= -9;	// new 17may05
	public final static int		FLOAT			= -10;
	
	public final static int		UNTYPED_ATOMIC	= -99;
	
	public final static int		TRUE			= -1;
	public final static int		FALSE			= 0;
	
	public final static int		VOIDED_NODE = NodeTree. VOIDED_NODE;
	
	public final static int		BOOLEAN_FALSE	= 0;
	public final static int		BOOLEAN_TRUE	= -1;
  
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	
	// ctors with NodeTree will eventually replace all
	// ctors with IndexManager ????
	
	// NOTA: 	quite the cascading sequence of ctors.
	// 			do we really need all these?
	
    public DocItems( NodeTree itsTree )
    //---------------------------------
    {
    	this( itsTree, DEFAULT_LIST_ALLOCS );
    }
    
	public DocItems( NodeTree itsTree, int initialAllocs )
	//----------------------------------------------------
	{
    	this( itsTree, new IntList( 2, initialAllocs ));
	}
	
	public DocItems( NodeTree itsTree, IntList itsList )
	//--------------------------------------------------
	{
    	this( itsTree.getIndexManager(), itsList );
	}
    
    public DocItems( IndexManager indexer )
    //-------------------------------------
    {
        this( indexer, DEFAULT_LIST_ALLOCS );
    }
    
	public DocItems( IndexManager indexer, int[] valueType, int docId )
	//-----------------------------------------------------------------
	{
		this( indexer, DEFAULT_LIST_ALLOCS );   
    	
		m_list.addRef_2( valueType[0], valueType[ 1 ] );
    	
		m_numTotalItems = m_numValidItems = 1;
		
		setId( docId );
	}
	
    public DocItems( IndexManager indexer, int[] valueType )
    //------------------------------------------------------
    {
    	this( indexer, valueType, QUERY_DOCUMENT );
    }
    
    public DocItems( IndexManager indexer, int initialAllocs )
    //--------------------------------------------------------
    {
        this( indexer, new IntList( 2, initialAllocs ));
    }
    
    public DocItems( IndexManager indexer, IntList list )
    //---------------------------------------------------
    {
        m_indexer   = indexer;
        m_list      = list;
        
        m_numTotalItems = list.count();     // by definition, all nodes
        m_numValidItems = m_numTotalItems;  // in a new list are valid
    }    
    
    public void setList( IntList list )
    //---------------------------------
    {
    	m_list = list;
    	
		setNumTotalItems( list.count() );
		setNumValidItems( list.count() );
    }
    
    public void setList( IntList list, int numInvalid )
    //-------------------------------------------------
    {
		m_list = list;
    	
		setNumTotalItems( list.count() );
		setNumValidItems( list.count() - numInvalid );
    }
    
    public int evaluateAsInteger( int itemIx )
    //----------------------------------------
    {
    	int[] itemInfo = m_list.getRef_Both( itemIx );
    	
    	if ( itemInfo[ 1 ] != INT )
    		throw new IllegalArgumentException(
				"\nDocItems.evaluateAsInteger(): Program error -- entry is not INT");
    		
    	return itemInfo[ 0 ];
    }
    
    public int evaluateAsString( int itemIx )
    //---------------------------------------
    {
    	int[] itemInfo = m_list.getRef_Both( itemIx );
    	
    	if ( itemInfo[1] != STRING )
    		throw new IllegalArgumentException( 
				"\nDocItems.evaluateAsString(): Program error -- entry is not STRING");
				
		return itemInfo[ 0 ];
    }
    
    public void setId( int id )     { m_id = id; }
    //-------------------------
    public int getId()              { return m_id; }
    //----------------
    
	public NodeTree getTree()
	//-----------------------
	{
		return ( m_id == QUERY_DOCUMENT ) ?	
		
						m_indexer.getCurrTreeWalker().getQueryDocumentTree() :
						m_indexer.getTree( m_id );
	}
    
    public IntList getIntList()     { return m_list; }
    //-------------------------
    
    public void addRef_2( int arg1, int arg2 )
    //----------------------------------------
    { 
        ++ m_numTotalItems;
        ++ m_numValidItems;
        
        m_list.addRef_2( arg1, arg2 ); 
    }    


    // amalgamate docId info w/ m_list info for use by ResultList clients
    // (currently JUnit tests are the major client)
    
    public int[] getNotatedNodeList()
    //-------------------------------
    {
        int[] nodeList = new int[ 2 * m_numValidItems ]; // one for docid, one for nodeid
        
        int j = 0;
        int[] rawNode = m_list.getRawList();
        for( int i = 0; i < 2 * m_numTotalItems; i++, i++ )
        {
            if ( rawNode[ i + 1 ] != NodeTree.VOIDED_NODE )
            {
                nodeList[ j++ ] = m_id;
                nodeList[ j++ ] = rawNode[ i ];
            }
        }
        
        return nodeList;
    }
    
    public int getNumValidItems()   { return m_numValidItems; }
    //---------------------------
    public int getNumTotalItems()   { return m_numTotalItems; }
    //---------------------------
    
    public void setNumValidItems( int numValidNodes )   { m_numValidItems = numValidNodes; }
    //-----------------------------------------------
    public void setNumTotalItems( int numTotalNodes )   { m_numTotalItems = numTotalNodes; }
    //----------------------------------------------    
    public void updateValidItemCount( int update )      { m_numValidItems += update; }
    //--------------------------------------------  
    
    public void setNextDocument( DocItems document ) { m_nextDoc = document; }
    //---------------------------------------------------
    public void setPriorDocument( DocItems document ) { m_priorDoc = document; }
    //---------------------------------------------------
    
    public DocItems getNextDocument()     { return m_nextDoc; }
    //------------------------------------
    public DocItems getPriorDocument()    { return m_priorDoc; }
    //-------------------------------------
    
    public void trim()  { m_list.trim(); }
    //----------------
   
   	// the DocItems contains lists of siblings laid end to end, marked [sibIx][parentIx]
   	// each time parentIx changes, move to its n'th sibling, invalidating all else
   	
   	void validateNthSiblings( int n )
    //-------------------------------
    {
        IntList list = m_list;
        
        int sibCount    = 1;
        int numInvalid  = 0;
        int prior       = -99999;
        
        for( int i = 0; i < list.count(); i++ )
        {
            int parent = list.getRef_2( i );
            if ( parent != prior )
            {
                sibCount = 1;
                prior = parent;
            }
            
            if ( sibCount != n )
            {
                list.setRef_2( i, NodeTree. VOIDED_NODE );
                ++ numInvalid;
            }
            else
                list.copyRef_1To2( i ); 
            
            ++ sibCount;
        }
        
        updateValidItemCount( -numInvalid );
    }  
    
    // create a new DocItems object containing
    // the n'th item in this one, if any.
    
    public DocItems groupPositional( int n )
    //---------------------------------------
    {
    	int[] valueType = subscript( n );
    	
    	if ( valueType == null )
    	{
    		return new DocItems( m_indexer );
    	}
    	
    	return new DocItems( m_indexer, valueType, m_id );
    }
    

    // [ position() compareOp int ], where int = -1 == last()
    
    // we've previous prepared this DocItems as a list of sibling
    // sequences in order of parent, which ordering we use
    
	public int complexPositional( String compareOp, int subscript )
	//-------------------------------------------------------------
	{
		int newInvalid = 0;
		int op = -1;
		
		int comparee = subscript;

		if 		( compareOp.equals( "=" ))	op = 0; 
		else if	( compareOp.equals( "!=" ))	op = 1;
		else if	( compareOp.equals( "<" ))	op = 2;
		else if	( compareOp.equals( "<=" ))	op = 3;
		else if	( compareOp.equals( ">" ))	op = 4;
		else if	( compareOp.equals( ">=" ))	op = 5;	
        
		int[] tree      = getTree().getRawTree();
		IntList list    = m_list;
        
        int sibCount = 0;
        int priorParent = -999;
        
		for( int i = 0; i < m_numTotalItems; i++ )
		{
			int parent = list.getRef_2( i );
			if ( parent == NodeTree.VOIDED_NODE )			
				continue;
				
			if ( parent != priorParent )
			{
				sibCount = 1;
				priorParent = parent;
				
				// if we're comparing against last(), we need to find
				// its 'subscript' count for this run of siblings
				 
				if ( comparee == -1 )
				{
					subscript = 1;
					int j = i;
					while( ++ j < list.count() )
					{ 
						int nextParent = list.getRef_2( j );
						if ( nextParent == NodeTree.VOIDED_NODE ) 
							continue;
						
						if ( nextParent == parent )
							++ subscript; 
						else
							break;
					}
				}
			}
			
			if ( compareInts( op, sibCount, subscript ))
			{
				++ sibCount;
				continue;
			}
			
			list.setRef_2( i, NodeTree.VOIDED_NODE ); 
			
			++ sibCount;
			++ newInvalid;
		}
		
		return newInvalid;
	}
	 
	// much simpler logic than the preceding method
	
	public int complexBlockPositional( String compareOp, int subscript )
	//------------------------------------------------------------------
	{
		int newInvalid 	= 0;
		int count 		= 0;
		int op;

		if 		( compareOp.equals( "=" ))	op = 0; 
		else if	( compareOp.equals( "!=" ))	op = 1;
		else if	( compareOp.equals( "<" ))	op = 2;
		else if	( compareOp.equals( "<=" ))	op = 3;
		else if	( compareOp.equals( ">" ))	op = 4;
		else if	( compareOp.equals( ">=" ))	op = 5;	
		else
		
			throw new IllegalArgumentException( 
					"\nDocItems.complexBlockPositionals(): Illegal operation: " + compareOp );
        
		IntList list    = m_list;	
		
		if ( subscript == - 1) subscript = list.count();  // last()
	  
		for( int i = 0; i < m_numTotalItems; i++ )
		{
			if ( list.getRef_2( i ) == NodeTree.VOIDED_NODE )
			{
				continue;
			}
			
			++ count;
			if ( compareInts( op, count, subscript ))
			{
				continue;
			}
			
			list.setRef_2( i, NodeTree.VOIDED_NODE ); 
			
			++ count;
			++ newInvalid;
		}
		
		return newInvalid;
	}
	
	boolean compareInts( int op, int int1, int int2 )
	//-----------------------------------------------
	{
		switch( op )
		{
			case 0:	return int1 == int2;	
			case 1: return int1 != int2;
			case 2: return int1 <  int2;
			case 3:	return int1 <= int2;
			case 4:	return int1 >  int2;
			case 5: return int1 >= int2;
		}
		
		return false;
	}
    
    public boolean isSorted()   { return m_list.isSorted(); }
    //-----------------------
    public void sort()			{ getIntList().sort(); }
    //----------------

    // bib/book/editor ->            /
    //                        bib         /
    //                              book    editor
	
    public int namedParentOfEvaledChild( int[] parentKey )
    //----------------------------------------------------
    {
        int newInvalid = 0;
        
        int[] tree      = getTree().getRawTree();
        IntList list    = m_list;
        
        for( int i = 0; i < m_numTotalItems; i++ )
        {
            int node = list.getRef_2( i );
            if ( node == NodeTree.VOIDED_NODE )
                continue;

            int nodeBase = node * NodeTree.INTS_PER_ENTRY;
            int parentNode = tree[ nodeBase + NodeTree.PARENT ];
            
            if ( parentNode == -1 )
            {
				list.setRef_2( i, NodeTree.VOIDED_NODE ); 
				++ newInvalid;
				continue;
            }
            
            int parentBase = parentNode * NodeTree.INTS_PER_ENTRY;
           
            // 8dec04: Bugfix added (byte) cast and NS_SHIFT
            // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
            
       //     int treeNsKey 	= tree[ parentBase + 1 ];
       //     int shift 		= NodeTree.NS_SHIFT;
        //    int shiftedKey	= (byte) ( tree[ parentBase ] >> NodeTree.NS_SHIFT );
            
            if ( parentKey[ 0 ] != WordManager.WILDCARD && parentKey[ 0 ] != (short)( tree[ parentBase + 1 ] >> NodeTree.NS_SHIFT ) )
         //   if ( parentKey[ 0 ] != WordManager.WILDCARD && parentKey[ 0 ] != ( tree[ parentBase + 1 ] & NodeTree.NS_MASK ) )
            //if ( parentKey[ 0 ] != WordManager.WILDCARD && parentKey[ 0 ] != tree[ ++ parentBase ] )
            {
                list.setRef_2( i, NodeTree.VOIDED_NODE ); 
                ++ newInvalid;
                continue;
            }
            
            if ( parentKey[ 1 ] != WordManager.WILDCARD && parentKey[ 1 ] != tree[ parentBase + 2 ] )
            {
                list.setRef_2( i, NodeTree.VOIDED_NODE );
                ++ newInvalid;
                continue;
            } 
            
            list.setRef_2( i, parentNode );
        }
        
        return newInvalid;
    }
    
    public int nodesAtRoot()
    //----------------------
    {
        int newInvalid  = 0;
        IntList list    = m_list;
        
        for( int i = 0; i < m_numTotalItems; i++ )
        {
        	int rootEndValue = list.getRef_2( i );
        	
        	// already invalid
            if ( rootEndValue == NodeTree.VOIDED_NODE )
                continue;
            else if ( rootEndValue == 0 )
            	continue;
                
            list.setRef_2( i, NodeTree.VOIDED_NODE );
            ++ newInvalid;
        }
        
        updateValidItemCount( - newInvalid );
        
        return newInvalid;
    }
    
    public void parentOf()
    //---------------------
    {
        IntList list = m_list;
        
        NodeTree tree = getTree();
        
        for( int i = 0; i < list.count(); i++ )
        {
            if ( list.getRef_2( i ) != NodeTree. VOIDED_NODE )
            {
                int child = list.getRef_1( i );
                int parent = tree.getParent( child );
                
                list.setRef_1( i, parent );
            }
        }
    }
    
    public int ancestor( IntList ancList )
    //-----------------------------------
    {
    	int	numNewInvalid = 0;
    	
    	IntList		descList	= m_list;
    	NodeTree	tree 		= getTree();
    	
    	for( int descIx = 0; descIx < descList.count(); descIx ++ )
    	{
    		int desc = descList.getRef_2( descIx );
    		if ( desc == NodeTree. VOIDED_NODE )
    			continue;
    			
			int nextDesc = desc;
		
    		// if we can reach any of these anc ancestors from desc,
    		// the desc node stays; else we rule it VOIDED
    		
    		boolean match = false;
    		int[] 	ancs;
    		int		anc = -1;
    		
    		for( int ancIx = 0; ancIx < ancList.count(); ancIx++ )
    		{
    			ancs = ancList.getRef_Both( ancIx );
    			//anc = ancList.getRef_2( ancIx );
    			if ( ancs[1] == NodeTree. VOIDED_NODE )
    				continue;
    			
    			anc = ancs[0];
    			if ( anc >= desc )	// parent pointers upward from desc get smaller
    			{					// so if anc is already too large, it'll only
    				break;			// get larger. ie we're done w/ this descendant
    			}   				
    				
				int parentOfDesc;
				while ( ( parentOfDesc = tree.getParent( nextDesc ) ) >= anc )
				{
					if ( parentOfDesc == anc )
					{
						match = true;
						break;
					} 
						
					nextDesc = parentOfDesc;
				}			
    		}
    		
    		if ( match )
				descList.setRef_2( descIx, anc );
				//descList.setRef_2( desc, anc );
	    	else
    		{
				descList.setRef_2( descIx, NodeTree. VOIDED_NODE );
				//descList.setRef_2( desc, NodeTree. VOIDED_NODE );
				++ numNewInvalid;	
    		}
    	}
    	
    	return numNewInvalid;
    }
    
    /* for each item in this resultList:
    
    	1) 	if it's an atomic, ignore it
    	2) 	if it's a node, ask its nodeTree to convert it to a string
    	3) 	word-parse the string once for each word in the words vector
    	4) 	if the wordStarts list return a zero count for any word
    		(ie, the word's not present), invalidate the node  
    */
    
	public int contains_word( Vector words, Vector flags, TreeWalker treeWalker )
	//---------------------------------------------------------------------------
	{
		int numInvalid	= 0;
		NodeTree tree 	= getTree();
		
		boolean caseSensitive = ((Boolean)flags.get( 0 )).booleanValue();
    	
    	registerWordHandler( this );
		setIgnoreCase( false );	// we'll do our own lowercasing (if ignoreCase) for efficiency reasons
		
		IntList wordStarts = new IntList();	
		
		IntList items = getIntList();
		for( int node = 0; node < items.count(); node ++ )
		{
			int[] item = items.getRef_Both( node );
			
			boolean allWords = true;
			
			if ( item[ 1 ] < DOC_NODE )
			{
				allWords = false;
			}
			else
			{
				char[] nodeContents = tree.asString( item[ 0 ], true ).toString().toCharArray();
				
				// do it ourselves
				if ( !caseSensitive )
					toLowerCase( nodeContents );

				for( int wordIx = 0; wordIx < words.size(); wordIx ++ )
				{
					char[] word = (char[]) words.get( wordIx );
							
					if ( !caseSensitive )
						toLowerCase( word );// ditto do it ourselves
											// (would be even more efficient if we did once at start of routine)
					setSearchWord( word );				
			
					characters( nodeContents, 0, nodeContents.length, -1, wordStarts );		
				
					if ( wordStarts.count() == 0 )
					{
						allWords = false; 					
						break;
					}
					
					wordStarts.zeroCount();
				}
			}
			
			if ( !allWords )
			{
				items.setRef_2( node, VOIDED_NODE );
				++ numInvalid;
			}
		}
		
		updateValidItemCount( -numInvalid );
		return numInvalid;
	}
   
    // supplied under contract to IWordHandler
    
    public void newWord( char[] cbuf, int wordStart, int wordEnd, int parent, IntList matches )
    //-----------------------------------------------------------------------------------------
    {
        char[] word = getSearchWord(); // we're searching for this word
           
	//	String DEBUG_WORD = new String( cbuf, wordStart, wordEnd - wordStart );
		
        if ( word.length != wordEnd - wordStart )
            return;
        	
        int i;
        for( i = 0; i < wordEnd - wordStart; i ++ )
        {
            if ( cbuf[ wordStart + i ] != word[ i ] )
                return;
        }
            
        matches.addRef_2( wordStart, wordStart );
    }
    
    public void emitXml( PrintWriter w, boolean prettyPrint )
    //-------------------------------------------------------
    {
    	if ( getNumValidItems() == 0 )
    		return;
    		
    	IntList nodes 	= getIntList();
    	NodeTree tree	= getTree();
    	
    	for( int i = 0; i < nodes.count(); i++ )
    	{
    		int type = nodes.getRef_2( i );    		
    		if ( type == NodeTree.VOIDED_NODE )
    			continue;
    			
    		int value = nodes.getRef_1( i );
    		
    		if ( type >= NodeTree. ELEM )
    		{   	
    			tree.emitXml( value, w, prettyPrint );	
    		}
    		else
    			switch( type )
    			{
    				case DOC_NODE : 						
		    						tree.emitXml( 0, w, prettyPrint );
									break;
							
    				case INT :					
									w.print( value );
						    		break;
    				
    				case STRING :   				
		    						String string = m_indexer.getCurrTreeWalker().getStringResult( value );
		    						 
									w.print( string );
		    						break;
    						
					case BOOLEAN :					
									w.print( (value == 0)? "false" : "true"); 
									break;
						
					case ATTR_TEXT :						
									string = tree.getAttributeText( value );
												 
									w.print( string );
									break;
				
					case TEXT_AS_STRING : // was TEXT, but string() function changed it to this
						
									string = tree.getElementText( value );								
														 
									w.print( string );
									break;
									
					case DOUBLE :	w.print( m_indexer.getCurrTreeWalker().getDouble( value ).doubleValue() );
									break;
									
					case DECIMAL :	w.print( m_indexer.getCurrTreeWalker().getDecimal( value ));
									break;
									
    				default :
			    					throw new IllegalArgumentException( 
			    						"\nDocumentItems:emitXml(): unknown item type " + type );
    			}
    			
    			if ( LINEFEED_AFTER_INDIVIDUAL_ITEMS )
    			{
    				w.println();
    			}
    	}
    	
    	w.flush();
    }
    
	public StringBuffer asString( int[] item )	{ return asString( item, false ); }
	//----------------------------------------
	
    public StringBuffer asString( int[] item, boolean addInterTextSpace )
    //-------------------------------------------------------------------
    {
    	StringBuffer sb = new StringBuffer();
    	
		if ( item[ 1 ] >= DOC_NODE )
		{
			sb.append( getTree().asString( item[ 0 ], addInterTextSpace ) );
		}
		
		else switch ( item[ 1 ] )
		{
			case VOIDED_NODE :		break;
				
			case INT :				sb.append( item[ 0 ] );
									break;

			case STRING :			sb.append( m_indexer.getCurrTreeWalker().getStringResult( item[ 0 ] ) );
									break;
	
			case BOOLEAN :			sb.append( ( item[ 0 ] == 0)? "false" : "true"); 
			
			case NULL_ORDERSPEC :	sb.append( "" );
									break;

			case DOC_NODE :			// fall thru for now		

			default :
				
				throw new IllegalArgumentException( "\nDocItems.asString(): can't convert value of type : " + item[ 1 ] );
		}
		
		return sb;
    }
    
    public StringBuffer asString( boolean appendSpace )
    //-------------------------------------------------
    {
    	StringBuffer sb = new StringBuffer();
    	
    	NodeTree tree 	= getTree();
    	IntList	list	= m_list;
    	
    	boolean isFirst = true; // to insert spaces between all but first item if appendSpace
    	
		for( int i = 0; i < m_numTotalItems; i++ )
		{
			if ( appendSpace )
				if( !isFirst )
				{
					sb.append( ' ' );
				}
				else
					isFirst = false;
				
			int[] valueType = list.getRef_Both( i );
			
			sb.append( asString( valueType ));
		}

    	return sb;
    }
    
    public String toString() { return doString( false ); }
    //----------------------
    
	public String doString( boolean detailOnly )
	//------------------------------------------
	{
		StringBuffer sb = new StringBuffer();

		if ( !detailOnly )
		{
			sb.append( "\nDocument id   = " + m_id + " [\"" + m_indexer.getDocumentName( m_id ) + "\"]" ); 
			
			sb.append( "\nNumTotalItems = " + m_numTotalItems );
			sb.append( "\nNumValidItems = " + m_numValidItems );
			sb.append( "\n" );
		}

		IntList list = m_list;
	 
	 	NodeTree tree = getTree();
	
	 	for( int i = 0; i < m_numTotalItems; i++ )
	 	{
		 	if ( list.getRef_2( i ) != NodeTree.VOIDED_NODE )
		 	{
			 	int item = list.getRef_1( i );
			 	int type = list.getRef_2( i );
			 
			 	int base = 0;
	                      
			 	if ( type >= NodeTree.ELEM )
			 	{
			 		// NOTA: type is misnomer here
			 		
				 	sb.append( "[" + item + "]" );
				 	sb.append( "[" + type + "]" );
	            
				 	//NodeTree tree = m_indexer.getTree( m_id );
	            
				 	base = NodeTree.INTS_PER_ENTRY * item;
				 	type = tree.type( base );
	            
				 	tree.printNode( base, type, sb );
			 	}              
			 	else 
				 	switch( type )
				 	{
						case DOC_NODE : 
	                
	                		// NOTA: we can check LINEFEED_AFTER_INDIVIDUAL_ITEMS as in emitXml() above
	                
								 tree.printNode( 0, NodeTree.ELEM, sb );
								 break;
	                                
						case INT :             
	                
								 sb.append( item );
								 break;
	                
						case STRING :
	                
	                			// NOTA: investigate changing getResultList() from indexer
	                			//		to retrieving it from the DocItems itself
	                			
								 String str = m_indexer.getCurrTreeWalker().getStringResult( item );	 
								                    			
								 sb.append( str );
								 break;
								 
						case BOOLEAN :
					
								sb.append( (item == 0)? "False" : "True");
								break;
								
						case ATTR_TEXT :
					
								sb.append( tree.getAttributeText( item ) );									
								break;
								
						case TEXT_AS_STRING :	// was TEXT, but string() function changed it to this
					
								sb.append( tree.getElementText( item ));
								break;
								
						case NULL_ORDERSPEC :
					
								sb.append( "NULL_ORDERSPEC" );
								break;
					
								
						case DOUBLE :
							
								sb.append( m_indexer.getCurrTreeWalker().getDouble( item ) ).toString();
								break;
								
						case DECIMAL :
							
								sb.append( m_indexer.getCurrTreeWalker().getDecimal( item )).toString();
								break;
								
						default :
	
						 		throw new java.lang.IllegalArgumentException( 
									 "\nDocumentItems.toString(): unknown result type :" + type );
				 	}
		 	}    
	 	}

		return sb.toString();
	 }  
	 
	 public int[] subscript( int n )
	 //-----------------------------
	 {
	 	if ( n < 1 || n > m_list.count() )
	 	{
	 		return null;
	 	}
	 	
	 	IntList list = m_list;
	 	
	 	int count = 0;
	 	for( int i = 0; i < list.count(); i++ )
	 	{
	 		int[] valueType = list.getRef_Both( i );
	 		if ( valueType[ 1 ] != NodeTree. VOIDED_NODE )
	 		{
	 			++ count;
	 			if (count == n )
	 			{
	 				return valueType;
	 			}
	 		}
	 	}
	 	
	 	return null;
	 }
	 
	public void string_value( TreeWalker itsWalker, int[] item ) throws InvalidQueryException
	//----------------------=-----------------------------------
	{
		if ( isNode( item ))
		{
			String str = getTree().string( item );
			
			if ( str != null )
			{
				int strId = itsWalker.newStringToList( str );
				
				item[ 0 ] = strId;
				item[ 1 ] = STRING;
			}
		}
		
		else
		{
			
		}
	}
		
	 public void string_value( TreeWalker itsWalker )
	 //----------------------------------------------
	 {
	 	string_value( itsWalker, false );
	 }
	 
	 // fn:string( eval(relPath) ) has been called. We evaluate the relPath to derive
	 // a ResultList, then call string() on that, which converts nodes in the DocItems
	 // in place to a form suitable for emitting text. The only real work is done for element nodes.

	 
	 public void string_value( TreeWalker itsWalker, boolean addInterTextSpaces )
	 //--------------------------------------------------------------------------
	 {
	 	IntList list 	= m_list;
	 	String str		= null;
	 	int strId;
	 	
	 	for( int i = 0; i < list.count(); i++ )
	 	{
	 		int[] valueType = list.getRef_Both(i);
	 		
	 		if ( valueType[1] >= 0 )	// it's a node
	 		{
	 			// NodeTree converts types in place so that a later emit()
	 			// does the right thing. in the special case of ELEM,
	 			// we concatenate subordinate text, add it to the ResultList's
	 			// string hashtable, and return its type as a STRING

	 			str = getTree().string( valueType, addInterTextSpaces );
	 			
	 			if ( str != null ) // it was an ELEM, now a STRING
	 			{
	 				strId = itsWalker.newStringToList( str );
	 				
	 				valueType[0] = strId;
	 			}
	 			
	 			list.setRef_Both( i, valueType ); // put it back
	 		}
	 			
	 		else 
	 		{	 			
	 			switch( valueType[ 1 ])
		 		{
		 			case VOIDED_NODE :		break;	// TODO: provide a NodeTree.string() function for the doc node
		 			
		 			case DOC_NODE :
		 						
						throw new IllegalArgumentException( 
							"\nDocItems.string_value(): currently cannot take the string-value of a Document Node" );
		 			
		 			case INT :		 			
		 									str = Integer.toString( valueType[0]);
		 									strId = itsWalker.newStringToList( str );
		 									
		 									valueType[ 0 ] = strId;
		 									valueType[ 1 ] = STRING;
		 									
		 									list.setRef_Both( i, valueType );
		 									break;
		 									
		 			case STRING :			break; // a no-op
		 			
		 			case BOOLEAN :			str = valueType[ 0 ] == 0 ? "false" : "true" ;
		 									break;
		 			
		 			case ATTR_TEXT :
		 			case TEXT_AS_STRING	:	break;
		 			
		 			default :
		 			
		 				throw new IllegalArgumentException( "\nDocItems.string_value(): can't convert type : " + valueType[1] ); 			
		 		}
	 		}
	 	}
	 }
	
	// called by ResultList.appendDocument(). we're amalgamating two DocItems together
	
	public void append( DocItems doc )
	//--------------------------------
	{
		IntList list = m_list;
		
		m_list.appendBlock( doc.getNumTotalItems(), doc.getIntList().getRawList() );
		
		m_numTotalItems += doc.getNumTotalItems();
		m_numValidItems += doc.getNumValidItems();	
	}
	
	public int[] getFirstValidItem()
	//------------------------------
	{
		IntList list = m_list;
		
		for( int i = 0; i < m_numTotalItems; i++ )
		{
			int[] valueType = list.getRef_Both( i );
			
			if ( valueType[ 1 ] != VOIDED_NODE )
			
				return valueType;	
		}
		
		return null;
	}
	
	public DocItems cloneDoc()
	//------------------------
	{
		DocItems cloneDoc = new DocItems( m_indexer );
		cloneDoc.setId( getId() );
		
		IntList cloneList = m_list.cloneList();
		
		cloneDoc.setList( cloneList );
		
		return cloneDoc;
	}
	
	// called by TreeWalker.elementContent() -> ResultList.removeAttributes
	
	public DocItems removeAttributes()
	//--------------------------------
	{
		DocItems attributes = new DocItems( m_indexer );
		attributes.setId( getId() );
		
		IntList	list	= m_list;
		NodeTree tree	= getTree();
		
		for( int i = 0; i < list.count(); i++ )
		{
			int[] valueType = list.getRef_Both( i );
			
			int leafNode = valueType[ 1 ];
			
			if ( leafNode >= NodeTree. ELEM )
			{
				if ( tree.getType( valueType[ 0 ] ) == NodeTree. ATTR )
				{
					list.setRef_2( i, NodeTree.VOIDED_NODE );
					
					attributes.addRef_2( valueType[ 0 ], NodeTree. ATTR );
				}
			}
		}
		
		updateValidItemCount( -attributes.getNumValidItems());
		
		return attributes;
	}
	
	public void addToNodeTree( QueryDocumentTree dest )
	//-------------------------------------------------
	{
		IntList	list		= m_list;
		NodeTree sourceTree	= getTree();
		
		for( int i = 0; i < list.count(); i++ )
		{
			int[] valueType = list.getRef_Both( i );
			
			int leafNode = valueType[ 0 ];
			
			if ( leafNode >= NodeTree. ELEM )
			{
				if ( sourceTree.getType( leafNode ) == NodeTree. ATTR )
				{
					String name 	= sourceTree.getAttributeName( leafNode );
					String value	= sourceTree.getAttributeText( leafNode );	
					
					dest.addAttribute( name, value );							
				}
			}
		}
	}
	
	public boolean containsAtomics()
	//------------------------------
	{
		IntList list 	= m_list;
		
		int[] rawList = m_list.getRawList();

		for( int i = 1; i < getNumTotalItems(); i += 2 )
		{
			if( rawList[ i ] < DocItems. DOC_NODE )
			
				return true;
		}
		
		return false;
	}
	
	public boolean containsNonAtomics()
	//---------------------------------
	{
		IntList list 	= m_list;
		
		int[] rawList = m_list.getRawList();

		for( int i = 1; i < getNumTotalItems(); i += 2 )
		{
			if( rawList[ i ] >= DocItems. DOC_NODE )
			
				return true;
		}
		
		return false;
	}
	
	public void sort( IntList orderSpecList, IntList returnCounts )
	//-------------------------------------------------------------
	{			
		/* NOTA: this gets us going initially but needs redoing:
		
			1) better not to instantiate all the strings at once
			2) the sort routine below is well-named!	
		*/
		
		String[] specString = new String[ orderSpecList.count() ];
		
		for( int i = 0; i < specString.length; i++ )
		{
			specString[ i ] = asString( orderSpecList.getRef_Both( i ) ).toString();
		}
		
		int[] sortedSpecs = dumbSort( specString );

		IntList list 	= m_list;
		IntList newList = new IntList( 2 );
		
		int[] runningCount = new int[ getNumValidItems() ];
		
		runningCount[ 0 ] = 0;
		for( int i = 0; i < sortedSpecs.length - 1; i++ )
		{
			runningCount[ i + 1 ] = runningCount[ i ] + returnCounts.getRef_1( i );
		}
		
		for( int i = 0; i < sortedSpecs.length; i++ )
		{
			// we can get 0 items returned per return clause (eg calling for an 
			// author on the 'editor' book), but need to track these to maintain
			// a 1-to-1 ratio with the evaluated order spec

			//int count = returnCounts.getRef_1( sortedSpec[ i ])
			
			int sortOrder = sortedSpecs[ i ];
			
			int count = returnCounts.getRef_1( sortOrder );
			if ( count > 0 )
			{		
				int startIx = runningCount[ sortOrder ];
				for( int j = startIx; j < startIx + count; j++ )
				{
					int[] valueType = list.getRef_Both( j );
					if ( valueType[ 1 ] != VOIDED_NODE )
					{
						newList.addRef_Both( valueType );
					}
				}
			}
		}
		
		setList( newList );
	}
	
	int[] dumbSort( String[] str )
	//----------------------------
	{
		int[] text = new int[ str.length ];
		
		for( int i = 0; i < text.length; i++ )
			text[ i ] = i;
			
		int temp;
			
		for (int i = 0; i < str.length; i++) 
		{
			for ( int j = i + 1; j < str.length; j++) 
			{
				if ( str[ text[i] ].compareTo( str[ text[j] ] ) > 0 ) 
				{
					temp = text[i];
					text[i] = text[j];
					text[j] = temp;
				}
			}
		}
		
		return text;
	}
	
	int attemptIntegerCast( int[] valueType ) throws InvalidQueryException
	//--------------------------------------------------------------------
	{
		String str = valueType[ 1 ] >= DOC_NODE ? 
		
						getTree().asString( valueType[ 0 ]).toString() :
						
						this.asString( valueType ).toString();
		
		int value;
		
		try 
		{ 
			value = Integer.parseInt( str );
		}
		catch( NumberFormatException nfe )
		{
			throw new CategorizedInvalidQueryException( 
	
					"XP0021", 
					"DocItems.integerCompare(): can't cast the item to an integer value" );
		}
		
		return value;
	}
    
	boolean attemptCompareInts( int op, int[] valueType1, int[] valueType2 ) throws InvalidQueryException
	//----------------------------------------------------------------------
	{
		int value_1 = valueType1[ 1 ] == INT ? valueType1[ 0 ] : attemptIntegerCast( valueType1 );
		int value_2 = valueType2[ 1 ] == INT ? valueType2[ 0 ] : attemptIntegerCast( valueType2 );

		switch( op )
		{
			case 0:	return value_1 == value_2;
			case 1: return value_1 != value_2;
			case 2: return value_1 <  value_2;
			case 3: return value_1 <= value_2;
			case 4: return value_1 >  value_2;
			case 5: return value_1 >= value_2;
		}
		
		return false;
	}
	
	boolean compareStrings( int op, String str_1, String str_2) throws InvalidQueryException
	//---------------------------------------------------------
	{		
		int compareValue = str_1.compareTo( str_2 );
		
		switch( op )
		{
			case 0:	return compareValue == 0;
			case 1: return compareValue != 0;
			case 2: return compareValue <  0;
			case 3: return compareValue <= 0;
			case 4: return compareValue >  0;
			case 5: return compareValue >= 0;
		}
		
		return false;
	}
		
	String getString( int[] valueType, NodeTree itsTree )
	//---------------------------------------------------
	{
		return valueType[ 1 ] >= DocItems.DOC_NODE ?	
				
					itsTree.asString( valueType[ 0 ]).toString() :
					this.asString( valueType ).toString();
	}
	
	public boolean generalCompareOnLhs( String compareOp, DocItems doc_2 ) throws InvalidQueryException
	//--------------------------------------------------------------------
	{
		int op;
		
		if 		( compareOp.equals( "=" ))	op = 0; 
		else if	( compareOp.equals( "!=" ))	op = 1;
		else if	( compareOp.equals( "<" ))	op = 2;
		else if	( compareOp.equals( "<=" ))	op = 3;
		else if	( compareOp.equals( ">" ))	op = 4;
		else if	( compareOp.equals( ">=" ))	op = 5;	
		else
		
			throw new IllegalArgumentException( 
					"\nDocItems.generalCompareOnLhs(): Illegal operation: " + compareOp );
					
		int newInvalid = 0;
		
		NodeTree tree_1	= getTree();
		NodeTree tree_2	= doc_2.getTree();
			
		IntList list_1	= m_list;
		IntList list_2 	= doc_2.getIntList();
		
		for( int i = 0; i < list_1.count(); i++ )
		{			
			int[] valueType_1 = list_1.getRef_Both( i );
			if ( valueType_1[ 1] == VOIDED_NODE )
			
				continue;
				
			for( int j = 0; j < list_2.count(); j ++ )
			{	
				int[] valueType_2 = list_2.getRef_Both( j );
				if ( valueType_2[ 1] == VOIDED_NODE )
				
					continue;
					
				boolean comparison;
				
				if ( valueType_1[ 1 ] == INT || valueType_2[ 1 ] == INT )
				{
					if ( attemptCompareInts( op, valueType_1, valueType_2 ) )
					
						continue;
				}
				else 
				{
					String str1 = getString( valueType_1, tree_1 );
					String str2 = getString( valueType_2, tree_2 );
					
					if ( compareStrings( op, str1, str2 ) )
					{
						continue;
					}
				}
				
				list_1.setRef_2( i, VOIDED_NODE );
				++ newInvalid;
			}		
		}
		
		updateValidItemCount( - newInvalid );

		return getNumValidItems() > 0 ? true : false ;
	}
	
	// in normal XQuery semantics, we return true if *any* pair return true
	// not the case for generalCompareOnLhs above, which returns *all* lhs items for which the comparison holds
	
	public boolean generalCompare( String compareOp, DocItems doc_2 ) throws InvalidQueryException
	//---------------------------------------------------------------
	{
		int op;
		
		if 		( compareOp.equals( "=" ))	op = 0; 
		else if	( compareOp.equals( "!=" ))	op = 1;
		else if	( compareOp.equals( "<" ))	op = 2;
		else if	( compareOp.equals( "<=" ))	op = 3;
		else if	( compareOp.equals( ">" ))	op = 4;
		else if	( compareOp.equals( ">=" ))	op = 5;	
		else
		
			throw new IllegalArgumentException( 
					"\nDocItems.generalCompare: Illegal operation: " + compareOp );
					
		NodeTree tree_1	= getTree();
		NodeTree tree_2	= doc_2.getTree();
			
		IntList list_1	= m_list;
		IntList list_2 	= doc_2.getIntList();
		
		for( int i = 0; i < list_1.count(); i++ )
		{			
			int[] valueType_1 = list_1.getRef_Both( i );
			if ( valueType_1[ 1] == VOIDED_NODE )
				continue;

			for( int j = 0; j < list_2.count(); j ++ )
			{	
				int[] valueType_2 = list_2.getRef_Both( j );
				if ( valueType_2[ 1 ] == VOIDED_NODE )
					continue;
					
				boolean comparison;
				
				if ( valueType_1[ 1 ] == INT || valueType_2[ 1 ] == INT )
				{
					if ( attemptCompareInts( op, valueType_1, valueType_2 ) )
					
						return true;
				}
				else 
				{
					String str1 = getString( valueType_1, tree_1 );
					String str2 = getString( valueType_2, tree_2 );
					
					if ( compareStrings( op, str1, str2 ) )
					
						return true;
				}
			}		
		}
		
		return false;
	}
	
	/*
	public boolean isAtomic( int[] valueType )
	//----------------------------------------
	{
		int type = valueType[ 1 ];
		
		return ( type == INT || type == STRING || type == BOOLEAN );
	}
	*/
	
	// m_id == QUERY_DOCUMENT only
	
	public int amalgamateAdjacentAtomics( TreeWalker treeWalker )
	//-----------------------------------------------------------
	{
		IntList list = m_list;
		
		int newInvalids 		= 0;
		int consecutiveAtomics 	= 0;
		
		boolean priorIsAtomic = false;
		StringBuffer sb = new StringBuffer();
		
		int count = list.count();
		
		for( int i = 0; i < count; i++ )
		{
			int[] valueType = list.getRef_Both( i );
	
			if ( isAtomic( valueType ))	
			{
				if ( !priorIsAtomic )
				{
					priorIsAtomic = true;
				}
				
				if ( consecutiveAtomics > 0 )
					sb.append( ' ' );
				
				sb.append( asString(valueType) );
				list.setRef_2( i, VOIDED_NODE );

				++ newInvalids;
				++ consecutiveAtomics;
			}
			// terminating a sequence of atomics
			else if ( priorIsAtomic ) // && ! isAtomic( valueType ) )
			{
				priorIsAtomic = false;
				
				int stringId = treeWalker.newStringToList( sb.toString() );
				list.setRef_Both( i - 1, stringId, STRING );
				
				-- newInvalids;	
				
				sb.setLength(0);
				consecutiveAtomics = 0;
			}		
		}
		
		if ( priorIsAtomic )
		{
			int stringId = treeWalker.newStringToList( sb.toString() );
			list.setRef_Both( count - 1, stringId, STRING );
			
			-- newInvalids;
		}

		updateValidItemCount( - newInvalids);
		
		return newInvalids;
	}
	
	public static boolean isType( int[] item, int type ) { return item[ 1 ] == type; }
	//--------------------------------------------------
	
	public static boolean isUntypedAtomic( int[] item )	{ return item[ 1 ] == UNTYPED_ATOMIC; }
	//-------------------------------------------------
	public static boolean isAtomic( int[] item )	{ return item[ 1 ] < DOC_NODE; }
	//------------------------------------------
	public static boolean isNode( int[] item ) 		{ return item[ 1 ] >= DOC_NODE; }
	//----------------------------------------
	public static boolean isInteger( int[] item )	{ return item[ 1 ] == INT; }
	//-------------------------------------------
	public static boolean isString( int[] item )	{ return item[ 1 ] == STRING; }
	//------------------------------------------
	public static boolean isBoolean( int[] item )	{ return item[ 1 ] == BOOLEAN; }
	//-------------------------------------------
	public static boolean isDouble( int[] item )	{ return item[ 1 ] == DOUBLE; }
	//------------------------------------------
	public static boolean isFloat( int[] item )		{ return item[ 1 ] == FLOAT; }
	//------------------------------------------
	public static boolean isDecimal( int[] item )	{ return item[ 1 ] == DECIMAL; }
	//------------------------------------------
	
	public static boolean isDocumentNode( int[] item ) { return item[ 0 ] == DOC_NODE; }
	//------------------------------------------------
	
	public boolean isElementNode( int[] item )	
	//-----------------------------------------
	{ 
		return isNode( item ) ? getTree().getType( item[ 0 ] ) == ELEM :
								false;
	}	
	
	public boolean isAttributeNode( int[] item )
	//------------------------------------------
	{ 
		return isNode( item ) ?	getTree().getType( item[ 0 ] ) == ATTR :
								false;
	}
	
	public boolean isTextNode( int[] item )
	//-------------------------------------
	{ 
		return isNode( item ) ?	getTree().getType( item[ 0 ] ) == TEXT :
								false;
	}
	
	public int[] getSingleValue()	{ return m_list.getRef_Both( 0 ); }
	//---------------------------
	
 	void markDuplicatesInvalid()
	//--------------------------
	{
		IntList list = m_list;
		
		int newInvalid = list.markDuplicatesInvalid();
		
		updateValidItemCount( -newInvalid );
	}
	
    public int castAsInteger( int[] item )
    //------------------------------------
    {	
    	switch( item[ 1 ] )
    	{
    		case INT : 		
    		
    				return item[ 0 ];
    		
    		case STRING :
    		
					String str = m_indexer.getCurrTreeWalker().getStringResult( item[ 0 ] );
					
					return Integer.parseInt( str );
    		
    		case BOOLEAN :
    		
    				throw new NumberFormatException();
    	}
    	
    	return -1; // never get here
    }
    
    // 11may05: called by TreeWalker arithmetic functions where operand
    // have been atomized as a string (actually untypedAtomic)
    // if its untypedAtomic, it's actually been saved as a string
    
    public static int[] attemptCastAsDouble( TreeWalker walker, int[] item ) throws CategorizedInvalidQueryException
	//----------------------------------------------------------------------
	{
    	double doubleVal;
    	
    	String untypedVal = walker.getStringResult( item[ 0 ]);
    	
    	try { doubleVal = new Double( untypedVal ).doubleValue(); }
    	
    	catch( NumberFormatException nfe )
		{
    		throw new CategorizedInvalidQueryException(
    				"XP0001",
					"Can't cast UntypedAtomic value to double " );
		}

        	int doubleId = walker.newDoubleToList( doubleVal );
        	
        	int[] newItem = { doubleId, DocItems. DOUBLE };
        	return newItem;
	}
    
    public static double castAsDouble( int[] item ) throws CategorizedInvalidQueryException
    //---------------------------------------------
    {
    	if ( ! isString( item ) )
    	{
    		throw new IllegalArgumentException( 
    				"DocItems.castAsDouble(): function expected string argument" );		
    	}
    	
    	double doubleVal = 0;
    	
    	try
		{
    		doubleVal = new Double( item[ 0 ] ).doubleValue();
		}
    	catch( NumberFormatException nfe )
		{
    		throw new CategorizedInvalidQueryException(
    				"err:FORG0001",
					"DocItems.castAsDouble(): invalid value for cast/constructor" );
		}
    	
    	return doubleVal;
    }
    
    public static boolean canCastToDouble( int[] item )
    //-------------------------------------------------
    {
    	try { double doubleVal = new Double( item[ 0 ] ).doubleValue(); }
    	
    	catch( NumberFormatException nfe )
		{
    		return false;
		}
    	
    	return true;
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