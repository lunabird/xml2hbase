 /*  XQEngine Copyright (C) 2002-2005 Fatdog Software Inc. [howardk@fatdog.com]
 *	--------------------------------------------------------------------------
 *  This file is part of XQEngine and is distributed under the terms of the
 *  GNU General Public License. See additional info at the end of this file. */
 
package com.fatdog.xmlEngine;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

import cn.edu.xidian.repace.xml2hbase.MappingTableManager;
import cn.edu.xidian.repace.xml2hbase.filter.CusFilter;
import cn.edu.xidian.repace.xml2hbase.filter.FloatFilter;
import cn.edu.xidian.repace.xml2hbase.filter.IntFilter;
import cn.edu.xidian.repace.xml2hbase.filter.TestFilter;
import cn.edu.xidian.repace.xml2hbase.hbase.HbaseReader;
import cn.edu.xidian.repace.xml2hbase.hbase.HbaseRecreateMappingTable;

import com.fatdog.xmlEngine.words.WordManager;
import com.fatdog.xmlEngine.javacc.SimpleNode;
import com.fatdog.xmlEngine.exceptions.InvalidQueryException;
	
	/**
	 * A collection class representing the results of a query.
	 * A linked list holds subordinate {@link DocItems} objects.
	 * 
	 * @author Howard Katz, howardk@fatdog.com
	 * @version 0.69
	 */

public class ResultList
{
	String xpath="";
	public List<String> columns;//看样子要更改为Map<int,List<String>>!!不，可以添加一个List<int> file
	public List<String> file;//file.add(数组)
	//Map<int,String> c2f;
	//ArrayList<String> ans;
	int[] valueTypeReal=new int[2];
	public int type=0;
	int validNum;
	String var="";
	
	//public Map<String,List<String>> resultMap;
	
    IndexManager	m_indexer;
    public TreeWalker		m_treeWalker;
    
    SimpleNode		m_ast;

    int   		m_numValidItems;
    int        	m_numTotalItems;
    
    DocItems   	m_headDoc           = null; // for appending
    DocItems   	m_tailDoc           = null; // ditto
    DocItems   	m_currNextDoc       = null; // for iterating
    
    int      	m_numDocuments;
    
    BitSet		m_bitset;			// not fully supported at present
    
    int 		m_currDocId;		// set by valueType() to set the document from which a valueType was pulled
    								// then referenced by Variables.evalVariableValue() to set the proper document
    								// on pulling a single node out of a for group. NOTA: ARCHITECTURE WILL LIKELY CHANGE.
    								
 	boolean 	m_hasUsedContext;
 				// filter() checks this result to see whether its predicate (rhs) has
 				// already used the lhs available from the TreeWalker.m_filterLhsStack
 				
 	public boolean	hasUsedContext()					{  return m_hasUsedContext; }
 	public void		setHasUsedContext( boolean usedIt )	{ m_hasUsedContext = usedIt; }
 	//-------------------------------------------------
    
    public ResultList( TreeWalker walker )
    //------------------------------------
    {
    	xpath="";
    	columns=new ArrayList<String>();
    	file=new ArrayList<String>();
    	validNum=0;
    	
    	//resultMap=new HashMap<String,List<String>>();
    	
    	//m_indexer 		= walker.getIndexer();
    	m_treeWalker	= walker;
    	  	
    	//m_indexer.setResultList( this ); 	// so DocumentItems can have access ??????
    	
    	//m_bitset = new BitSet( m_numDocuments );
    }
    
	public ResultList( TreeWalker walker, int[] singleValueType, int docId )
	//----------------------------------------------------------------------
	{
		this( walker );
 		
 		//DocItems doc = new DocItems( walker.getIndexer(), singleValueType, docId );
 		
		//appendDocument( doc );	
	}
    
    public ResultList( TreeWalker walker, int[] singleValueType )
    //-----------------------------------------------------------
    {
 		this( walker, singleValueType, DocItems.QUERY_DOCUMENT );
    }

	public void setAST( SimpleNode ast )	{ m_ast = ast; }
	//----------------------------------
	public SimpleNode getAST()				{ return m_ast; }
	//-------------------------

	public IndexManager getIndexer()	{ return m_indexer; }
    //------------------------------
    public TreeWalker getTreeWalker()	{ return m_treeWalker; }
    //-------------------------------
    
    public DocItems headDocument()		{ return m_headDoc; }
    //----------------------------
    public DocItems tailDocument()		{ return m_tailDoc; }

	void setCurrDocId( int docId )		{ m_currDocId = docId; }	// see above note
	//----------------------------
	int getCurrDocId()					{ return m_currDocId; }
	//----------------
	
	public int getItemValue( int[] item )	{ return item[ 0 ]; }
	//-----------------------------------
	public int getItemType( int[] item )	{ return item[ 1 ]; }
	//----------------------------------
	
	public int evaluateAsInteger()
	//----------------------------
	{
		int item;
    	
    	// NOTA: 	it's conceivable we might have multiple documents
    	// 			but only this one with a single valid item
    	
		if ( m_numDocuments == 1 )
		{
			DocItems doc = nextDocument();
			try
			{
				item = doc.evaluateAsInteger( 0 );
			}
			catch( IllegalArgumentException ex )
			{
				throw new IllegalArgumentException( 
					"\nResultList.evaluateAsInteger(): ResultList does not contain a single valid integer");
			}
			
			return item;
		}
    	
		throw new IllegalArgumentException( 
			"\nResultList.evaluateAsInteger(): ResultList does not contain a single valid integer");
	}
    
    public int getNumDocuments()    				{ return m_numDocuments; }
    //--------------------------
    public int getNumValidItems()                   { return m_numValidItems; }
    //---------------------------
    public void setNumValidItems( int numValid )	{ m_numValidItems = numValid; }
    //------------------------------------------
    public int getNumTotalItems()                   { return m_numTotalItems; }
    //---------------------------
    
    // NOTA: we only do the following in ResultList.namedChildOfParent currently,
    // where we're swapping out an old DocumentItems object and swapping in a new one.
    // we have to adjust the total count accordingly
    public void updateTotalItemCount( int update )  { m_numTotalItems += update; }
    //--------------------------------------------
    public void updateValidItemCount( int update )  { m_numValidItems += update; }
    //--------------------------------------------
    
	public DocItems nextDocument()
	//---------------------------------
	{
		if ( m_currNextDoc == null )
		{
			resetDocumentIterator(); // we're at end           
			return null;
		}
    
		DocItems next = m_currNextDoc;
		m_currNextDoc = next.getNextDocument();
            
		return next;
	}
	
	// 11sept03
	// I'm temporarily (?) replacing this with a ctor'ed version, since
	// I'm having some odd bugs that might be related to static usage
	/*
	public static ResultList newValue( TreeWalker walker, int[] valueType )
	//---------------------------------------------------------------------
	{		
		ResultList results = new ResultList( walker );
		results.appendDocument( new DocItems( walker.getIndexer(), valueType ));
		
		return results;		
	}
	*/
	
	// NOTA: subscripts are 1-based in accordance with XPath conventions
	
	// NOTA: presently subscript value <= 0 is an exception, but
	// values > ResultList.numValidNodes() is not. IS THAT WHAT WE WANT ?
	
	public ResultList subscript( int n )
	//----------------------------------
	{
		if ( n <= 0 )
			throw new IllegalArgumentException( "\nResultList.subscript(): can't have a subscript <= 0");
			
		// not enuf items in the RList 
		if ( n > getNumValidItems() )
		{
			return new ResultList( m_treeWalker );
		}
		
		DocItems nextDoc;
		int nextNumValid, totalTraversed = 0;
		
		while ( ( nextDoc = nextDocument() ) != null ) 
		{
			nextNumValid = nextDoc.getNumValidItems();
			if ( totalTraversed + nextNumValid >= n )
			{
				int[] valueType = nextDoc.subscript( n - totalTraversed );
				
				resetDocumentIterator();
				
				return new ResultList( m_treeWalker, valueType, nextDoc.getId() );
			} 
			
			totalTraversed += nextNumValid;
		}   
		
		return new ResultList( m_treeWalker );
	}
	
	public int[] valueType( int n )
	//-----------------------------
	{
		int[] valueType;
		
		if ( n < 0 )
			throw new IllegalArgumentException( "\nResultList.valueType(): can't have an argument < 0");

		if ( n >= getNumTotalItems() )
			throw new IllegalArgumentException( "\nResultList.valueType(): can't have an argument >= numTotalNodes");
			
		DocItems doc;
		int nextStart, startItem = 0;
		
		while ( ( doc = nextDocument() ) != null ) 
		{
//			nextStart = startItem + doc.getNumTotalItems();
			nextStart = startItem + doc.getNumValidItems();
						
			if ( n < nextStart )
			{
				valueType = doc.getIntList().getRef_Both( n - startItem );
				
				setCurrDocId( doc.getId() ); // so the outside client can set the proper doc on the new RList
				
				resetDocumentIterator(); 			
				
				return valueType;
			} 
			
			startItem = nextStart;
		}
		
		return null;  
	}

	public int[] valueType_0( int n )
	//-----------------------------
	{
		int[] valueType;
		
		if ( n < 0 )
			throw new IllegalArgumentException( "\nResultList.valueType(): can't have an argument < 0");

		if ( n >= getNumTotalItems() )
			throw new IllegalArgumentException( "\nResultList.valueType(): can't have an argument >= numTotalNodes");
			
		DocItems doc;
		int nextNumTotal, startItem = 0;
		
		while ( ( doc = nextDocument() ) != null ) 
		{
			nextNumTotal = doc.getNumTotalItems();
						
			if ( startItem + nextNumTotal >= n )
			{
				valueType = doc.getIntList().getRef_Both( n - startItem );
				
				setCurrDocId( doc.getId() ); // so the outside client can set the proper doc on the new RList
				
				resetDocumentIterator(); 			
				
				return valueType;
			} 
			
			startItem += nextNumTotal;
		}
		
		return null;  
	}
    public ResultList newInteger( String integer ) throws InvalidQueryException
    //--------------------------------------------
    {
        try 
        { 
            return newInteger( Integer.parseInt( integer ) );
        }
        catch( NumberFormatException nfe )
        {
            throw new InvalidQueryException( 
                "\nResultList.newInteger(): Not a valid integer: " + integer );
        }        
    }
     
    public ResultList newInteger( int integer )
    //-----------------------------------------
    {
    	int[] valueType = { integer, DocItems. INT }; 
    	this.valueTypeReal=valueType;
    	//appendDocument( new DocItems( m_indexer, valueType));
    	
    	return this;
    }
    
    public ResultList newDouble( String doubleStr )
    //---------------------------------------------
    {
    	int doubleId = m_treeWalker.newDoubleToList( doubleStr );
    	
    	int[] item = { doubleId, DocItems. DOUBLE };
    	
    	//appendDocument( new DocItems( m_indexer, item ));
    	this.valueTypeReal=item;
    	return this;
    }
    
    public ResultList newDouble( double val )
    //---------------------------------------
    {
    	int[] item = { m_treeWalker.newDoubleToList( val ), DocItems. DOUBLE };
    	
    	//appendDocument( new DocItems( m_indexer, item ));
    	this.valueTypeReal=item;
    	return this;
    }
    
    public ResultList newFloat( float val )
    //-------------------------------------
    {
    	int[] item = { m_treeWalker.newFloatToList( val ), DocItems. FLOAT };
    	
    	//appendDocument( new DocItems( m_indexer, item ));
    	this.valueTypeReal=item;
    	return this;
    }
    
    public ResultList newDecimal( BigDecimal decVal )
    //-----------------------------------------------
    {
    	int[] item = { m_treeWalker.newDecimalToList( decVal ), DocItems. DECIMAL };
    	
    	//appendDocument( new DocItems( m_indexer, item ));
    	this.valueTypeReal=item;
    	return this;
    }
    
    public ResultList newString( String string )
    //------------------------------------------
    {
    	int[] valueType = { m_treeWalker.newStringToList( string ), DocItems. STRING };
    	this.valueTypeReal=valueType;
    
    	//appendDocument( new DocItems( m_indexer, valueType ));
    	
    	return this;
    }

    public ResultList newBoolean( boolean boolValue )
    //-----------------------------------------------
    {
		int[] valueType = { boolValue ? DocItems.BOOLEAN_TRUE : DocItems.BOOLEAN_FALSE , DocItems.BOOLEAN };
		
		appendDocument( new DocItems( m_indexer, valueType ));
		
		return this;
    }
    
    public ResultList newDocumentNode( int docId )
    //--------------------------------------------
    {
        DocItems doc = new DocItems( m_indexer );
		
        doc.setId( docId ); 
        doc.addRef_2( DocItems. DOC_NODE, DocItems. DOC_NODE );

        appendDocument( doc );
        
        return this;
    }     
	
	public void append( ResultList rhs )
	//----------------------------------
	{
		DocItems rhsDoc;
		while ( ( rhsDoc = rhs.nextDocument()) != null )
		{
			appendDocument( rhsDoc );
		}
	}
    
    public ResultList appendSequence( ResultList rhs )
    //------------------------------------------------
    {
        DocItems nextDoc;
        while (( nextDoc = rhs.nextDocument() ) != null ) 
        {
            appendDocument( nextDoc );
        }
        
        return this;
    }
    
    // TODO: doublecheck all cases of removeDocument for proper total & validNode count updates
    public void removeDocument( DocItems items )
    //------------------------------------------
    {
    	-- m_numDocuments;
    	
    	// removing the first one?
       	   	
    	if ( m_headDoc == items )
    	{
    		// first and last both (ie the only one)?
    		
    		if ( m_tailDoc == items )
    		{
				m_headDoc = null;
				m_tailDoc = null;
				
				m_currNextDoc = null;
				
				return;
    		}
    		
    		m_headDoc = items.getNextDocument();
    		m_headDoc.setPriorDocument( null );
    		
    		return;
    	}
    	
    	// removing the last one?
    	
    	if ( m_tailDoc == items )
    	{
    		m_tailDoc = items.getPriorDocument();
    		m_tailDoc.setNextDocument(( null ));
    		
    		m_currNextDoc = items.getNextDocument(); // ???? is this right?
    		
    		return;
    	}
    	
    	DocItems priorDoc 	= items.getPriorDocument();
    	DocItems nextDoc 	= items.getNextDocument();
    	
    	priorDoc.setNextDocument( nextDoc );
    	nextDoc.setPriorDocument( priorDoc );
    	
    }
    
    public void appendDocument( DocItems items )
    //------------------------------------------
    {        
    	if ( items == null )
            return;
            
        int docId = items.getId();
            
        if ( docId >= 0 )
        	m_bitset.set( docId );
        	            
		items.setNextDocument( null );
            
        if ( m_headDoc == null )
        {
            m_headDoc = items;
            m_tailDoc = items;
            
            m_currNextDoc = m_headDoc;
        }
        else
        {
        	//	NOTA:	a number of routines call appendDocument(). need to keep
        	//			an eye on possible need for sorting when amalgamating.
        	
        	// 	this amalgamates -1 docs (probably the major use) as well as nodal docs
        	
			if ( items.getId() == m_tailDoc.getId() )
        	{
        		m_tailDoc.append( items );

				m_numValidItems += items.getNumValidItems();
				m_numTotalItems += items.getNumTotalItems();   
				
				return;     		
        	}
        	
            items.setPriorDocument( m_tailDoc );
            
            m_tailDoc.setNextDocument( items ); 
            m_tailDoc = items;
        }
        
        ++ m_numDocuments;
        
        m_numValidItems += items.getNumValidItems();
        m_numTotalItems += items.getNumTotalItems();           
    } 
    
    /**
    *   amalgamate docId and nodeid info for use by ResultList client
    *   (right now, JUnit tester uses to compare result sets
    */
    public int[] getDocNotatedNodeList()
    //----------------------------------
    {
        int[] nodeList = new int[ 2 * m_numValidItems ];
        int startIx = 0;
        
        DocItems doc;
        while (( doc = nextDocument() ) != null )
        {
            int[] docNodeList = doc.getNotatedNodeList();
            int len = docNodeList.length;
            
            System.arraycopy( docNodeList, 0, nodeList, startIx, len );
            startIx += len;
        }
        
        return nodeList;
    }
    
    public ResultList newNamedParentOfNamedChild( String parentName, String childName, 
                                                boolean isAttribute, boolean isPredicate ) throws InvalidQueryException
    //------------------------------------------------------------------------------------
    {
    	String is=isAttribute ?
    	        
                "/@" :
                "/";
    	xpath=xpath+"/"+parentName+is+childName;
                        
        return this;                
    }        
    
    public ResultList newLeafNodeLists( int nodeType, String qName ) throws InvalidQueryException
    //--------------------------------------------------------------
    {
        int[] keys = null;
        
        if ( nodeType == NodeTree.ELEM || nodeType == NodeTree.ATTR )
        {
            WordManager manager = ( nodeType == NodeTree.ELEM ) ?

					m_indexer.getElementWM() :
					m_indexer.getAttributeWM();
                
            keys = manager.keysFromQName( qName );
        }
        
        NodeTree[] trees = m_indexer.getTrees();
        for ( int i = 0; i < m_indexer.getNumTrees(); i++ )
        {
            DocItems docItems = trees[ i ].newLeafNodeList( nodeType, keys );
            if ( docItems != null )
            {
                appendDocument( docItems );
            }
        }
        
        return this;
    }
    
    public ResultList newSiblingLists( int siblingType, String qName ) throws InvalidQueryException
    //----------------------------------------------------------------
    {
		int[] keys = null;
        
		if ( qName != null )
		{
			keys = ( siblingType == NodeTree.ELEM )  ?
            
				m_indexer.getElementWM().keysFromQName( qName ) :
				m_indexer.getAttributeWM().keysFromQName( qName );
		}
        
		NodeTree[] trees = m_indexer.getTrees();
		for ( int i = 0; i < m_indexer.getNumTrees(); i++ )
		{            
			DocItems docItems = trees[ i ].newSiblingList( siblingType, keys );
			if ( docItems != null )
			{                    
				int numValid = docItems.getNumValidItems();
				if ( numValid > 0 )
				{
					appendDocument( docItems );    
				}
			}
		}
        
		return this;     	
    }
    
    public ResultList newSiblingLists( int siblingType, String qName, int subscript ) throws InvalidQueryException
    //-------------------------------------------------------------------------------
    {
        int[] keys = null;
        
        if ( qName != null )
        {
            keys = ( siblingType == NodeTree.ELEM )  ?
            
                m_indexer.getElementWM().keysFromQName( qName ) :
                m_indexer.getAttributeWM().keysFromQName( qName );
        }
        
        NodeTree[] trees = m_indexer.getTrees();
        for ( int i = 0; i < m_indexer.getNumTrees(); i++ )
        {            
            DocItems docItems = trees[ i ].newSiblingList( siblingType, keys );
            if ( docItems != null )
            {                    
                docItems.validateNthSiblings( subscript );
                
                int numValid = docItems.getNumValidItems();
                if ( numValid > 0 )
                {
                    appendDocument( docItems );    
                }
            }
        }
        
        return this;        
    }
    
    // the resultlist consists of lists of siblings; DocItems.complexPositional()
    // does the hard work of doing positional() compares of sibs against subscript
    
    public ResultList complexPositional( String comparisonOp, int subscript )
    //-----------------------------------------------------------------------
    {
		DocItems docItems;

		while (( docItems = nextDocument() ) != null ) 
		{
			int priorNumValid = docItems .getNumValidItems();
			
			int numInvalidated = docItems.complexPositional( comparisonOp, subscript );

			if ( numInvalidated == priorNumValid )
			{
				updateTotalItemCount( - docItems.getNumTotalItems());
				updateValidItemCount( - priorNumValid );
				
				removeDocument( docItems );			
			}
			else if ( numInvalidated > 0 )
			{
				updateValidItemCount( - numInvalidated );
			}
		}
			
    	return this;
    }
    
    // much simpler than the above method. we just check each item in each DocItems
    // against the subscript using the compareOp. Ie we ignore sibling position altogether
    
	public ResultList complexBlockPositional( String comparisonOp, int subscript )
	//----------------------------------------------------------------------------
	{
		DocItems docItems;

		while (( docItems = nextDocument() ) != null ) 
		{
			int priorNumValid = docItems .getNumValidItems();
			
			int numInvalidated = docItems.complexBlockPositional( comparisonOp, subscript );

			if ( numInvalidated == priorNumValid )
			{
				updateTotalItemCount( - docItems.getNumTotalItems());
				updateValidItemCount( - priorNumValid );
				
				removeDocument( docItems );			
			}
			else if ( numInvalidated > 0 )
			{
				updateValidItemCount( - numInvalidated );
			}
		}
			
		return this;
	}
    
    // NOTA:    Do we need to do something different for QueryDocumentItems
    //          if we're allowing multiple fragments ???
    
    public ResultList newNamedNodesAtRoot( String elementName ) throws InvalidQueryException
    //---------------------------------------------------------
    {	
    	xpath="/"+elementName;
    	System.out.println("Now in newNamedNodesAtRoot "+elementName);
    	
        return this;
    }
    
    public ResultList parentOf()
    //-----------------------------
    {
        DocItems docItems;
        while (( docItems = nextDocument() ) != null ) 
        {
            docItems.parentOf();
        }
        
        return this;
    }
    
    // we start w/ an empty ResultList and generate new nodes directly from the NodeTree
    
    public ResultList newParentOf( int type, String childName ) throws InvalidQueryException
    //---------------------------------------------------------
    {
        int[] keys = null;
        
        if ( childName != null )
        {
            keys = ( type == NodeTree.ELEM )  ?
            
                m_indexer.getElementWM().keysFromQName( childName ) :
                m_indexer.getAttributeWM().keysFromQName( childName );
        }
        
        NodeTree[] trees = m_indexer.getTrees();
        for ( int i = 0; i < m_indexer.getNumTrees(); i++ )
        {            
            DocItems docItems = trees[ i ].newParentOf( type, keys );
            if ( docItems != null )
            {
                appendDocument( docItems );
            }
        }
        
        return this;
    }
 
	public DocItems getDocumentWithId( int id )
	//-----------------------------------------
	{
		DocItems doc;
		
		resetDocumentIterator();
		
		while( ( doc = nextDocument() ) != null && doc.getId() < id );
	
		if ( doc != null && doc.getId() == id )
			return doc;
			
		return null;
	}
	
	public ResultList evalAncestor( ResultList ancestorResults )
	//----------------------------------------------------------
	{
		return evalAncestor( ancestorResults, false );
	}
	
	// The ResultList object initially holds descendants. 
	
	// see TO-DO note on textNodesContainingWord for use of allowSelfMatches arg
	
	public ResultList evalAncestor( ResultList ancestorResults, boolean allowSelfMatches )
	//------------------------------------------------------------------------------------
	{
		// NOTA: we strictly speaking don't have to zero subordinate DocItems if we
		// zero the master valid count, but we should just in case users (or other routines?)
		// mistakenly don't check the master RList valid count
		
		// (could we also return a new empty RList? we do that for an out-of-bounds subscript())
		
		if ( getNumValidItems() == 0 || ancestorResults.getNumValidItems() == 0 )
		{	
			setNumValidItems( 0 );
			
			DocItems doc;
			while (( doc = nextDocument() ) != null ) 
				doc.setNumValidItems( 0 );
			
			return this;
		}
			
		m_numTotalItems = 0;	// zero both, because descDocs returned
		m_numValidItems = 0;	// below are repopulated from from scratch
		
		DocItems descDoc;
		while (( descDoc = nextDocument() ) != null ) 
		{
			// NOTA: This will get progressively more inefficient as we work our way
			// further and further along a long linked list of DocumentItems
			
			DocItems ancestorDoc = ancestorResults.getDocumentWithId( descDoc.getId() );
			/*
			System.out.println("RList.evalAncestor(): descDoc.getId() = " + descDoc.getId() );
			if ( ancestorDoc == null )
					System.out.println("RList.evalAncestor(): NULL ancestor");
			else	System.out.println("RList.evalAncestor(): ancestorDoc.getId() = " + ancestorDoc.getId() );
			*/
			
			// NOTA: removeDocument doesn't update master total and validNode counts, but doesn't
			// have to in this case because count has been initially zeroed anyway
			
			if ( ancestorDoc == null )
			{
				removeDocument( descDoc );
				continue;
			}
            
			NodeTree tree = descDoc.getTree();
			descDoc = tree.evalAncestor( descDoc, ancestorDoc, new IntList( 2 ), allowSelfMatches );
			
			if ( descDoc.getNumValidItems() == 0 )
			{
				removeDocument( descDoc );
				continue;
			}

			m_numTotalItems += descDoc.getNumTotalItems();
			m_numValidItems += descDoc.getNumValidItems();
		}

		return this;
	}
	
    public ResultList namedAncestor( String ancestor, int nodeType ) throws InvalidQueryException
    //--------------------------------------------------------------
    {
		if ( getNumValidItems() == 0 )
			return this;
			
        int[] keys = ( nodeType == NodeTree. ELEM )  ?

                    m_indexer.getElementWM().keysFromQName( ancestor )   :
                    m_indexer.getAttributeWM().keysFromQName( ancestor );
        
        DocItems descendItems;
        while (( descendItems = nextDocument() ) != null )
        {
            NodeTree tree = descendItems.getTree();
            
            int numNewInvalid = tree.namedAncestor( keys, nodeType, descendItems.getIntList() );
            
            descendItems.updateValidItemCount( - numNewInvalid );
            updateValidItemCount( - numNewInvalid );
            
            if ( descendItems.getNumValidItems() == 0 )
            {
            	updateTotalItemCount( - descendItems.getNumTotalItems() );
            	removeDocument( descendItems );
            }
        }

        return this;
    }

	// m_docItems list holds children. we're following parent pointers
	
	public ResultList namedParentOfEvaledChild( String namedParent ) throws InvalidQueryException
	//--------------------------------------------------------------
	{   	
		
        this.xpath="/"+namedParent+xpath;
		return this;
	}
	
	public String asString()	{ return asString( false ); }
	//----------------------
    
    public String asString( boolean appendSpace )
    //-------------------------------------------
    {
   		StringBuffer sb = new StringBuffer();
   		
		DocItems doc;
		while (( doc = nextDocument( ) ) != null )
		{
			sb.append( doc.asString( appendSpace ));
		}
        
		return sb.toString();
    }
    
    public String toString() { return toString( false ); }
    //---------------------- 
    
    public String toString( boolean detailOnly )
    //------------------------------------------
    {
        StringBuffer sb = new StringBuffer();
        
        if ( !detailOnly )
        {
   			sb.append(  "ResultList.toString():\n" );
			
	        sb.append( "NumDocuments   = " + m_numDocuments + "\n" ); 
	        sb.append( "NumTotalItems  = " + m_numTotalItems + "\n" ); 
	        sb.append( "NumValidItems  = " + m_numValidItems + "\n" );    
        }
	        
        DocItems doc;
        while (( doc = nextDocument() ) != null )
        {
            sb.append( doc.doString( detailOnly ) );
        }
        
        return sb.toString();
    }
    
    // TO-DO: 
    // [1/2] consider if DocumentItems should directly notify ResultList on new invalid node count
    // possibly by calling m_indexer.getResultList().updateValidNodeCount()
    // [2/2] In fact, maybe update via appendDocument should be replaced w/ this mechanism as well
    
    public ResultList nodesAtRoot()
    //-----------------------------
    {
       
        return this;
    }
    
    public void resetDocumentIterator()     { m_currNextDoc = m_headDoc; }
    //---------------------------------
  
    // TODO: DON'T forget to check use of isPredicate in subordinate routine
    
    public ResultList namedChildOfParent( String child, int type, boolean isPredicate ) throws InvalidQueryException
    //-------------------------------------------------------------------------------
    {
    	String is="/";
    	if(type==NodeTree.ATTR){
    		is="/@";
    	}
        this.xpath=xpath+is+child;
        
        return this;
    }    
    
     // NOTA:    I haven't looked at resetting m_tailDoc appropriately !!!!
    //          @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    
    public void replaceDocument( DocItems oldDoc, DocItems newDoc )
    //-------------------------------------------------------------
    {
        DocItems priorDoc = oldDoc.getPriorDocument();   
        
        // unhook currDoc and replace it with resultDoc
        if ( priorDoc != null )
            priorDoc.setNextDocument( newDoc );
            
        if ( m_headDoc == oldDoc )
            m_headDoc = newDoc;
            
      	if ( m_tailDoc == oldDoc )
      		m_tailDoc = newDoc;
        
        newDoc.setPriorDocument( priorDoc );
        newDoc.setNextDocument( oldDoc.getNextDocument() );
        
        DocItems nextDoc = oldDoc.getNextDocument();
        if ( nextDoc != null )
            nextDoc.setPriorDocument( newDoc );      
            
    }
    
    // version 2 30sept03: replaces original version below
    
    public ResultList evalParent( ResultList parentResults, ResultList childResults, boolean isPredicate )
    //----------------------------------------------------------------------------------------------------
    {
    	//System.out.println("进入evalParent");
    	
		this.xpath=xpath+parentResults.xpath+childResults.xpath;
		//this.columns=parentResults.columns; //modded by kanchuanqi 2013.3.19
    	
    	return this;
    }
    /*
     	30sept03: replaced with version above where RList starts out empty w/ initial
     	arguments childResults and parentResults. This makes it easier to manage synching
     	the two DocItems lists together and only appending a childDocItems list
     	to the new RList when the results of a tree.evalParent() returns valid nodes
     */
    
    /*
    public ResultList evalParent( ResultList parentResults, boolean isPredicate )
    //--------------------------------------------------------------------------
    {
		m_numTotalItems = 0;	// zero both, because childDocs returned
		m_numValidItems = 0;	// below are repopulated from from scratch
		
        DocItems childDoc;
        while (( childDoc = nextDocument() ) != null ) 
        {
            DocItems parentDoc = parentResults.nextDocument();
            if ( parentDoc == null )
            {
                // we have one or more child docs remaining but no parents               
                // for now, just ignore the rest and the fact that we
                // need to update master total & valid counts
                
                childDoc.setNextDocument( null );
                return this;
            }
            
            NodeTree tree = childDoc.getTree();
                        
            childDoc = tree.evalParent( childDoc, parentDoc, isPredicate );

			m_numTotalItems += childDoc.getNumTotalItems();
			m_numValidItems += childDoc.getNumValidItems();
        }

        return this;
    }
    */
    
    public String debugDump()
    //-----------------------
    {
        StringBuffer sb = new StringBuffer();
        
        sb.append( "NumDocs       = " + m_numDocuments + "\n" );
        sb.append( "NumValidItems = " + getNumValidItems() + "\n");

        int docNum = 0;
        DocItems doc;
        while ( ( doc = nextDocument() ) != null )
        {
        	++ docNum;
        	
            sb.append( "    Doc  " + docNum + " [id=" + doc.getId() + ']' );
            sb.append( "  NumValidItems = " + doc.getNumValidItems() + "\n" );   
        }
             
        return sb.toString();
    }
    
    public ResultList contains_word( Vector words, Vector flags ) throws InvalidQueryException
	//-----------------------------------------------------------
	{
		if ( getNumValidItems() == 0 ) return this;

		int numNewInvalid = 0;
		
		DocItems currDoc;
		while (( currDoc = nextDocument() ) != null ) 
		{
			int currInvalid = currDoc.getNumValidItems();
			
		 	numNewInvalid = currDoc.contains_word( words, flags, m_treeWalker );
			
			if ( numNewInvalid == currInvalid )
			{
				updateTotalItemCount( - currDoc.getNumTotalItems() );
				removeDocument( currDoc );
			}
			
			updateValidItemCount( - numNewInvalid );
		}
		
		return this;
	}
	
	public String emitXml()
	//---------------------
	{
		return emitXml( false );
	}
	
	public String emitXml( boolean prettyPrint )
	//------------------------------------------
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter( sw );
		
		emitXml( pw, prettyPrint );
		
		return sw.toString();
	}
	
    public void emitXml( PrintWriter pw, boolean prettyPrint )
    //--------------------------------------------------------
    {  		
		if ( getNumValidItems() == 0 )
			return;
			
		resetDocumentIterator();
    		
		DocItems doc;
		while (( doc = nextDocument() ) != null ) 
		{
			doc.emitXml( pw, prettyPrint );	    
		}
    }
    
    public ResultList cloneResultList( )
    //----------------------------------
    {
    	ResultList clone = new ResultList( m_treeWalker );
    	clone.xpath=this.xpath;
    	clone.columns=this.columns;
    	clone.file=this.file;
    	//clone.resultMap=this.resultMap;
    	
    	return clone;
    }
    
	public ResultList string_value( )
	//-------------------------------
	{
		return string_value( false );
	}
	
    public ResultList string_value( boolean addInterTextSpaces )
    //----------------------------------------------------------
    {
		DocItems doc;
		while (( doc = nextDocument() ) != null ) 
		{
			doc.string_value( m_treeWalker, addInterTextSpaces );	    
		}
		
    	return this;
    }
    
	/**
	 * 
	 * Given a string ID component from a String item, retrieve the string itself.
	 * 
	 * @param item an integer ID representing a string.
	 * @return the string it stands for.
	 */
	
    public String getStringResult( int item )
    //---------------------------------------
    {
    	return m_treeWalker.getStringResult( item );
    }
    
    int[] getFirstValidItem()
    //-----------------------
    {
		DocItems doc;
		while (( doc = nextDocument() ) != null ) 
		{			
			if ( doc.getNumValidItems() > 0 )
				return doc.getFirstValidItem();
		}
		
    	return null; 
    }
    
    public boolean effectiveBooleanValue()
    //------------------------------------
    {
    	if ( m_numValidItems == 0 )
    		return false;
    		
    	if ( m_numValidItems == 1 )
    	{
    		// TODO: Isn't this the same as subscript()
    		
    		int[] valueType = getFirstValidItem();
    		
    		if ( isBooleanValue( valueType ) && booleanValue( valueType ) == false )
    		
    			return false;
    			
    		if ( valueType[ 1 ] == DocItems. STRING )
    		{
    			String value = getStringResult( valueType[ 0 ] );
    			
    			if ( value.equals( "" ) || value.equals( "0 " ) )
    			
					return false;
    		}
    			
    		if ( valueType[ 1 ] == DocItems. INT && valueType[ 0 ] == 0 )
    		
    			return false;
    	}
    	
    	return true;
    }
    
	public ResultList removeAttributes()
	//----------------------------------
	{
		ResultList results = new ResultList( m_treeWalker );
		
		DocItems doc;
		while (( doc = nextDocument() ) != null ) 
		{			
			if ( doc.m_id >= 0 && doc.getNumValidItems() > 0 )
			{
				DocItems attributesOnly = doc.removeAttributes();
				
				int numAttributes = attributesOnly.getNumValidItems();
				
				if ( numAttributes > 0 )
				{
					results.appendDocument( attributesOnly );
					
					updateValidItemCount( - numAttributes );
				}
			}
		}
		
		return results;
	}
	
	public void appendResults( ResultList addend )
	//--------------------------------------------
	{
		DocItems addDoc;
		while (( addDoc = addend.nextDocument() ) != null )
		{
			appendDocument( addDoc );
		}
	}
	
	public boolean containsAtomics()
	//------------------------------
	{
		DocItems doc;
		while (( doc = nextDocument()) != null )
		{
			if ( doc.getId() == DocItems.QUERY_DOCUMENT && doc.containsAtomics() )
				return true;
		}
		
		return false;
	}
	
	public boolean containsNonAtomics()
	//---------------------------------
	{
		DocItems doc;
		while (( doc = nextDocument()) != null )
		{
			if ( doc.getId() != DocItems.QUERY_DOCUMENT && doc.containsNonAtomics() )
				return true;
		}
		
		return false;
	}

	public void sort( IntList orderSpecList, IntList returnCounts ) throws InvalidQueryException
	//-------------------------------------------------------------
   	{
	 	DocItems doc;
	   	while (( doc = nextDocument()) != null )
	   	{
		   	if ( orderSpecList.count() >= 2 )
		   	{
			   	doc.sort( orderSpecList, returnCounts );
		   	}
	   	}
	}			
	
	// this version of general compare has different semantics from the normal generalCompare, which
	// returns a boolean. this version keeps the lhs item if the comparison is true, else is voids it
	
	public ResultList generalCompareOnLhs( String op, ResultList lhs, ResultList rhs ) throws InvalidQueryException
	//--------------------------------------------------------------------------------
	{
		DocItems lhsDoc;
		while (( lhsDoc = lhs.nextDocument()) != null )
		{
			DocItems rhsDoc = rhs.headDocument();
					
			if ( rhs.getNumDocuments() != 1 || rhs.headDocument().m_id != DocItems.QUERY_DOCUMENT )			
			{
				rhsDoc = rhs.getDocumentWithId( lhsDoc.getId() );
			}
				
			if ( rhsDoc != null )
			{
				if ( lhsDoc.generalCompareOnLhs( op, rhsDoc ) )
				{
					appendDocument( lhsDoc );
				}
			}
		}
		
		return this;
	}
	
	
	public Result queryOneWithFilter(String dataBase,String rowKey, String xPath, Map<String, List<String>> mt, String keyword,String op)
			throws IOException
	{
		CompareFilter.CompareOp comop=CompareFilter.CompareOp.EQUAL;
		if(op.equals("=")){
			comop=CompareFilter.CompareOp.EQUAL;
		}
		else if(op.equals(">")){
			comop=CompareFilter.CompareOp.GREATER;
		}
		else if(op.equals(">=")){
			comop=CompareFilter.CompareOp.GREATER_OR_EQUAL;
		}
		else if(op.equals("<")){
			comop=CompareFilter.CompareOp.LESS;
		}
		else if(op.equals("<=")){
			comop=CompareFilter.CompareOp.LESS_OR_EQUAL;
		}
		else if(op.equals("!=")){
			comop=CompareFilter.CompareOp.NOT_EQUAL;
		}
		
		System.out.println("比较：xpath="+xPath+op+"\t"+keyword);
		Result rs = null;
		List<String> columns = mt.get(xPath);
		ArrayList<String> ans = new ArrayList<String>();
		if(columns != null){
			int i;
			List<Filter> filters = new ArrayList<Filter>();
			for(i = 0; i < columns.size(); ++i){
				Filter filter = new SingleColumnValueFilter(
							  Bytes.toBytes("xmark"),
							  Bytes.toBytes(columns.get(i)),
							  comop,
							  new BinaryComparator(Bytes.toBytes(keyword))
							  );
				filters.add(filter);
			}
			FilterList filterlist = new FilterList(FilterList.Operator.MUST_PASS_ONE, filters);
			rs = HbaseReader.getRowWithEqualFilter(dataBase, rowKey,  filterlist);
		}
		return rs;
	}
	
	public String getParentCol(String childCol,int n){
		if(n==0){
			return childCol;
		}
		int offset;
		int offset2=childCol.length();
		for(int i=0;i<n;i++){
			offset = childCol.lastIndexOf('.', offset2 - 1);
			offset2 = childCol.lastIndexOf('.', offset - 1);
		}
		
		return childCol.substring(0, offset2);
	}
	
	
	// the RList we're in is empty, operands are in lhs and rhs
	
	public ResultList generalCompare( String op, ResultList lhs, ResultList rhs ) throws InvalidQueryException
	//---------------------------------------------------------------------------
	{
		//ResultList results = new ResultList(this.m_treeWalker);
		//调用过滤器，得到结果集。
		//
		CompareFilter.CompareOp cop;
		if(op.equals("=")){
			cop=CompareFilter.CompareOp.EQUAL;
		}
		else if(op.equals(">")){
			cop=CompareFilter.CompareOp.GREATER;
		}
		else if(op.equals(">=")){
			cop=CompareFilter.CompareOp.GREATER_OR_EQUAL;
		}
		else if(op.equals("<")){
			cop=CompareFilter.CompareOp.LESS;
		}
		else if(op.equals("<=")){
			cop=CompareFilter.CompareOp.LESS_OR_EQUAL;
		}
		else if(op.equals("!=")){
			cop=CompareFilter.CompareOp.NOT_EQUAL;
		}
		else{
			throw new InvalidQueryException("Not Support "+op);
		}
		
		System.out.println("进行比较"+lhs.xpath);
		int deep=0;
		String oldTmp="";
		String newTmp="";
		int parent=-1;
		for(parent=0;parent<lhs.m_treeWalker.m_vars.m_name.length;parent++){
			 if(lhs.xpath.startsWith(lhs.m_treeWalker.m_vars.m_value[parent].xpath)&&lhs.m_treeWalker.m_vars.m_forLetIndex[parent]>=0){
				 //int size=survives.columns.size();
				 String[] tmp=lhs.m_treeWalker.m_vars.m_value[parent].xpath.split("/");
				 deep=tmp.length;
				 //deep=deep*4-3;
			 }
		}
		String tmp1[]=lhs.xpath.split("/");
		int childDeep=tmp1.length;
		childDeep-=deep;
		
		
		/*
		if(lhs.var.length()>0){
			parent=lhs.m_treeWalker.m_vars.findNamedForVariable(lhs.var);
			String[] tmp=lhs.m_treeWalker.m_vars.m_value[parent].xpath.split("/");
			deep=tmp.length;
			deep=deep*4-3;
		}
		*/
		
		
		//ResultList parentList=lhs.m_treeWalker.m_vars.m_value[parent];
		
		ResultList parentList=new ResultList(this.m_treeWalker);
		parentList.var=lhs.var;
		List<String> columns1 = this.m_treeWalker.pctable.get(lhs.xpath);
		
		if(rhs.valueTypeReal[1]==DocItems. STRING){
			String keyword=(String)lhs.m_treeWalker.m_stringsHash.get(rhs.valueTypeReal[0]);		
			System.out.println("keyword: "+keyword);		
			
			List<String> columnsTmp=new ArrayList<String>();
	        if(columns1 != null){
				int i;
				List<Filter> filters = new ArrayList<Filter>();
				for(i = 0; i < columns1.size(); ++i){
					//New a SingleColumnValueFilter to filter on a qualifier
					SingleColumnValueFilter filter = new SingleColumnValueFilter(
								  Bytes.toBytes("xmark"),
								  Bytes.toBytes(columns1.get(i)),
								  cop,
								  Bytes.toBytes(keyword)
								  );
					//If the column is missing in a row, filter will skip
					filter.setFilterIfMissing(true);
					//Add the filter into filterList
					filters.add(filter);
				}
				//New a filterList and set the operator with MUST_PASS_ONE
				FilterList filterlist = new FilterList(FilterList.Operator.MUST_PASS_ONE, filters);
				//Get the scan result of HBase table
				ResultScanner rscanner;
				try {
					rscanner = HbaseReader.getRowsWithFilterList(this.m_treeWalker.tableName, filterlist,m_treeWalker.startRow,m_treeWalker.stopRow);
					if(op.equals("=")){
						for(Result rs : rscanner){
						//System.out.println("The row of the result is :" + Bytes.toString(rs.getRow()));
						for(i = 0; i < columns1.size(); ++i){
							//columnsTmp=new ArrayList<String>();
							//Find the right column and get the xpath2 column value
							//System.out.println(Bytes.toString(rs.getValue(Bytes.toBytes("xmark"), Bytes.toBytes(columns.get(i)))));
							if(Bytes.toString(rs.getValue(Bytes.toBytes("xmark"), Bytes.toBytes(columns1.get(i)))).equals(keyword)){
								//System.out.println("The xpath column is : " + columns1.get(i));
								//String parentCol = getParentCol(columns.get(i));
								parentList.columns.add(this.getParentCol(columns1.get(i),childDeep));
								parentList.file.add(Bytes.toString(rs.getRow()));
								//columnsTmp.add(this.getParentCol(columns1.get(i),childDeep));
							}
						}
						//parentList.resultMap.put(Bytes.toString(rs.getRow()), columnsTmp);
						//columnsTmp=new ArrayList<String>();
						//columnsTmp.clear();
					}
					}
					else if(op.equals(">")){
						for(Result rs : rscanner){
							//System.out.println("The row of the result is :" + Bytes.toString(rs.getRow()));
							for(i = 0; i < columns1.size(); ++i){
								if(Bytes.toString(rs.getValue(Bytes.toBytes("xmark"), Bytes.toBytes(columns1.get(i)))).compareTo(keyword)>0){
									parentList.columns.add(this.getParentCol(columns1.get(i),childDeep));
									parentList.file.add(Bytes.toString(rs.getRow()));
								}
							}
						}
					}
					else if(op.equals("<")){
						for(Result rs : rscanner){
							//System.out.println("The row of the result is :" + Bytes.toString(rs.getRow()));
							for(i = 0; i < columns1.size(); ++i){
								if(Bytes.toString(rs.getValue(Bytes.toBytes("xmark"), Bytes.toBytes(columns1.get(i)))).compareTo(keyword)<0){
									parentList.columns.add(this.getParentCol(columns1.get(i),childDeep));
									parentList.file.add(Bytes.toString(rs.getRow()));
								}
							}
						}
					}
					else if(op.equals("!=")){
						for(Result rs : rscanner){
							//System.out.println("The row of the result is :" + Bytes.toString(rs.getRow()));
							for(i = 0; i < columns1.size(); ++i){
								if(!Bytes.toString(rs.getValue(Bytes.toBytes("xmark"), Bytes.toBytes(columns1.get(i)))).equals(keyword)){
									parentList.columns.add(this.getParentCol(columns1.get(i),childDeep));
									parentList.file.add(Bytes.toString(rs.getRow()));
								}
							}
						}
					}
					
					rscanner.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//Iterator on the ResultSet
				//Result rst=new Result("ddd");
				//rst.
				
	        }
		}
		else if(rhs.valueTypeReal[1]==DocItems. INT){
			int keyword=rhs.valueTypeReal[0];
			System.out.println("INT keyword: "+keyword);
			if(columns1 != null){
				int i;
				List<Filter> filters = new ArrayList<Filter>();
				for(i = 0; i < columns1.size(); ++i){
					System.out.println(columns1.get(i));
					CusFilter filter = new CusFilter(
								  Bytes.toBytes("xmark"),
								  Bytes.toBytes(columns1.get(i)),
								  cop,
								  Bytes.toBytes(keyword+"")
								  );
					//filter.setFilterIfMissing(true);
					filters.add(filter);
				}
				FilterList filterlist = new FilterList(FilterList.Operator.MUST_PASS_ONE, filters);
				ResultScanner rscanner;
				try {
					rscanner = HbaseReader.getRowsWithFilterList(this.m_treeWalker.tableName, filterlist,m_treeWalker.startRow,m_treeWalker.stopRow);
					for(Result rs : rscanner){
						for(i = 0; i < columns1.size(); ++i){
							if(rs.getValue(Bytes.toBytes("xmark"), Bytes.toBytes(columns1.get(i)))!=null){
								parentList.columns.add(this.getParentCol(columns1.get(i),childDeep));
								parentList.file.add(Bytes.toString(rs.getRow()));
							}
						}
				    }
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
		}
		else if(rhs.valueTypeReal[1]==DocItems.DECIMAL){
			BigDecimal keyword=lhs.m_treeWalker.getDecimal(rhs.valueTypeReal[0]);
			float key=keyword.floatValue();
			System.out.println("Float keyword: "+key);
			if(columns1 != null){
				int i;
				List<Filter> filters = new ArrayList<Filter>();
				for(i = 0; i < columns1.size(); ++i){
					System.out.println(columns1.get(i));
					FloatFilter filter = new FloatFilter(
								  Bytes.toBytes("xmark"),
								  Bytes.toBytes(columns1.get(i)),
								  cop,
								  Bytes.toBytes(key+"")
								  );
					//filter.setFilterIfMissing(true);
					filters.add(filter);
				}
				//PageFilter pf=new PageFilter(1);
				//filters.add(pf);
				FilterList filterlist = new FilterList(FilterList.Operator.MUST_PASS_ONE, filters);
				ResultScanner rscanner;
				try {
					rscanner = HbaseReader.getRowsWithFilterList(this.m_treeWalker.tableName, filterlist,m_treeWalker.startRow,m_treeWalker.stopRow);
					for(Result rs : rscanner){
						for(i = 0; i < columns1.size(); ++i){
							if(rs.getValue(Bytes.toBytes("xmark"), Bytes.toBytes(columns1.get(i)))!=null){
								parentList.columns.add(this.getParentCol(columns1.get(i),childDeep));
								parentList.file.add(Bytes.toString(rs.getRow()));
							}
						}
				    }
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
		}
		else{
			System.out.println("未支持类型 "+rhs.valueTypeReal[1]);
		}
		
		
		//System.out.println("The parentList is "+parentList.resultMap.values());
		return parentList;
	}
	
	// TODO: review both the following functions. They expect booleans and throw otherwise
	//		 is this what we want in all cases?
	
	public boolean booleanValue( int[] valueType )
	//--------------------------------------------
	{
		if ( valueType[ 1 ] != DocItems. BOOLEAN )
		
			throw new IllegalArgumentException( "\nResultList.booleanValue([]): value is not a boolean" );
		
		return valueType[0] == DocItems. BOOLEAN_TRUE ? true : false;	
	}
	
	public boolean booleanValue()
	//---------------------------
	{
		if ( getNumValidItems() == 0 )
		
			return false;
			
		if ( getNumValidItems() > 1 )	
		
			throw new IllegalArgumentException( "\nResultList.booleanValue(): ResultList has more than 1 item" );
			
		int[] valueType = valueType(0);
		
		if ( valueType[ 1 ] != DocItems. BOOLEAN )
		
			throw new IllegalArgumentException( "\nResultList.booleanValue(): value is not a boolean" );
			
		return valueType[0] == DocItems. BOOLEAN_TRUE ? true : false;
	}
	
	public boolean isBooleanValue( int[] valueType ) { return valueType[ 1 ] == DocItems. BOOLEAN; }
	//----------------------------------------------

	// we've just evaluated enclosed content. make sure adjacent atomics are merged into a single
	// string with space separators. (we'll produce a new text node in the next rev.)
	
	public void amalgamateAdjacentAtomics()
	//-------------------------------------
	{
		DocItems doc;
		while (( doc = nextDocument()) != null )
		{
			if ( doc.getId() != DocItems.QUERY_DOCUMENT )
				continue;
				
			int newInvalid = doc.amalgamateAdjacentAtomics( m_treeWalker );
			{
				if ( newInvalid > 0 )
				{
					updateValidItemCount( -newInvalid );
				}
			}
		}
	}

	public void copyResultItemsToDestination( QueryDocumentTree fromTree, QueryDocumentTree toTree )
	//----------------------------------------------------------------------------------------------
	{
		// TBD next rev	
	}
	
	// used in current rev (0.61) as a temporary hack to hoist attributes in
	// evaluated content into their owning element parents for emission as Xml
	
	public boolean isAttributesOnly()
	//-------------------------------
	{
		DocItems doc;
		while (( doc = nextDocument()) != null )		
		{
			NodeTree tree = doc.getTree();
			
			IntList list = doc.getIntList();
			for( int i = 0; i < list.count(); i++ )
			{
				if ( tree.getType( list.getRef_1(i) ) != NodeTree.ATTR )
					return false;
			}
		}
			
		return true;	
	}
	
	// emit each attribute to w, voiding it at the same time so that the
	// subsequent QueryDocTree.getEnclosedResult().emitXml() doesn't emit it again
	
	public void emitAttributes( PrintWriter w )
	//-----------------------------------------
	{
		DocItems doc;
		while (( doc = nextDocument()) != null )		
		{
			NodeTree tree = doc.getTree();
			
			IntList list = doc.getIntList();
			for( int i = 0; i < list.count(); i++ )
			{
				int node = list.getRef_1(i);
				
				if ( tree.getType( node ) == NodeTree.ATTR )
				{
					updateValidItemCount( -1 );
					doc.updateValidItemCount( -1 );
					
					list.setRef_2( i, DocItems.VOIDED_NODE );
					
					w.print( ' ' + tree.getAttributeName( node ) 
								+ '=' + '\"' + tree.getAttributeText( node ) + '\"' );
				}				
			}
		}
	}
	
	public boolean isType( int[] item, int type )
	//-------------------------------------------
	{ 
		return DocItems.isType( item, type ); 
	}
	
	public boolean isNode( int[] item )			{ return DocItems.isNode( item ); }
	//---------------------------------
	public boolean isAtomic( int[] item )		{ return DocItems.isAtomic( item ); }
	//-----------------------------------	
	public boolean isInteger( int[] item )		{ return DocItems.isInteger( item ); }
	//------------------------------------
	public boolean isString( int[] item )		{ return DocItems.isString( item ); }
	//-----------------------------------
	public boolean isBoolean( int[] item )		{ return DocItems.isBoolean( item ); }
	//------------------------------------
	public boolean isDouble( int[] item )		{ return DocItems.isDouble( item ); }
	//-----------------------------------
	public boolean isDecimal( int[] item )		{ return DocItems.isDecimal( item ); }
	//------------------------------------
	
	// these test depend on the DocItems, which depends on its corresponding NodeTree
	// THESE ARE NOT SAFE IN OTHER WORDS UNLESS THERE IS ONLY A SINGLE DocItems IN THE ResultList
	// ???????????????????  MAYBE THESE SHOULD BE REMOVED FROM ResultList ????????????????????
	
	public boolean isElementNode( int[] item )		{ return headDocument().isElementNode( item ); }
	//----------------------------------------
	public boolean isAttributeNode( int[] item )	{ return headDocument().isAttributeNode( item ); }
	//------------------------------------------
	public boolean isTextNode( int[] item ) 		{ return headDocument().isTextNode( item ); }
	//-------------------------------------
	
	// translated from dawgQL 9dec04
	public boolean getSingleBooleanValue() throws InvalidQueryException
	//------------------------------------
	{
		if ( m_numTotalItems != 1 )
		{
			throw new InvalidQueryException(
				"\nResultList:getSingleInt(): expected a single boolean result" );
		}
			
		int[] value = m_headDoc.getSingleValue();
		if ( value[ 1 ] != DocItems. BOOLEAN )
				
			throw new InvalidQueryException(
				"\nResultList:getSingleInt(): expected a single boolean result" );
			
		return ( value[ 0 ] != DocItems. BOOLEAN_FALSE ); 
	}

	public boolean isSingleBoolean()
	//------------------------------
	{
		if ( m_numTotalItems != 1 )
			return false;
		
		int[] value = m_headDoc.getSingleValue();
		
		return ( value[ 1 ] == DocItems. BOOLEAN );
	}
	
	/**
	 * Provides the same functionality as the following-sibling:: axis.
	 * 
	 * @return A ResultList containing all siblings which follow the originals in document order
	 */
	
	public ResultList followingSibling()
	//----------------------------------
	{
		ResultList siblingResults = new ResultList( m_treeWalker );
		
		DocItems doc;
		while (( doc = nextDocument()) != null )		
		{
			if ( doc.getNumValidItems() == 0 )
				continue;
				
			NodeTree tree = doc.getTree();
			
			DocItems siblingDoc = tree.followingSibling( doc );
			
			if ( !siblingDoc.isSorted() )
				siblingDoc.sort();
				
			siblingDoc.markDuplicatesInvalid();
			
			if ( siblingDoc.getNumValidItems() > 0 )
			{				
				siblingResults.appendDocument( siblingDoc );
			}
		}
		
		return siblingResults;
	}

	// atomic types are returned as is; nodes are stringified
	// we don't have typed values yet, so we can skip that step 
	
	ResultList atomize() throws InvalidQueryException
	//------------------
	{
		DocItems doc;	
		
		while ( ( doc = nextDocument() ) != null ) 
		{
			IntList items = doc.getIntList();
			
			for( int i = 0; i < items.count(); i++ )
			{
				int[] item = items.getRef_Both( i );
				
				if ( doc.isNode( item ) ) // else we return it as is
				{
					doc.string_value( m_treeWalker, item );	// adds string reference and converts in place
					
					item[ 1 ] = DocItems.UNTYPED_ATOMIC;
					
					items.setRef_Both( i, item );
				}
			}
		}
		
		return this;
	}
	
	// right now (17mar05) we're only calling this from TreeWalker arithmetic
	// operations on encountering UntypedAtomic after atomization
	
	public ResultList attemptCastAsDouble( TreeWalker walker, int[] item )
	//--------------------------------------------------------------------
	{
		double doubleVal = 0.0;
		
		if ( DocItems.isString( item ) || DocItems.isUntypedAtomic( item ) )
		{
			try
			{
				Double dbl = new Double( item[ 0 ] );
				
				return new ResultList( walker ).newDouble( dbl.doubleValue() );
			}
			catch( NumberFormatException nfe )
			{
				return new ResultList( walker ); // null RList => couldn't cast
			}
		}
		
		return new ResultList( walker );
	}
	
	public int castAsInteger( int[] item )
	//------------------------------------
	{
		return headDocument().castAsInteger( item );
	}
	
	public int expectInteger( ) throws InvalidQueryException
	//-------------------------
	{	
		if ( getNumValidItems() != 1 )
		
			throw new InvalidQueryException( "\nExpected a single integer only" );
			
		int[] item = subscript( 1 ).valueType( 0 );
		
		if ( ! isInteger( item ) )
		
			throw new InvalidQueryException( "\nExpected item to be an integer" );
			
		return item[ 0 ];
	}
	
	public final static double kEPSILON = 1.0e-12;
	
	public double expectDouble( ) throws InvalidQueryException
	//---------------------------
	{
		if ( getNumValidItems() != 1 )
			
			throw new InvalidQueryException( "\nExpected a single double only" );
				
		int[] item = subscript( 1 ).valueType( 0 );
		
		if ( ! isDouble( item ))
			
			throw new InvalidQueryException( "\nExpected item to be an double" );
		
		Double doubleObj = m_treeWalker.getDouble( item[ 0 ]);
		
		double doubleVal = doubleObj.doubleValue();
		
		if ( doubleVal - 0 < kEPSILON )
			return 0;
		
		return doubleVal;
		
	}
	public double expectDecimal( ResultList singleItem ) throws InvalidQueryException
	//--------------------------------------------------
	{
		if ( getNumValidItems() != 1 )
			
			throw new InvalidQueryException( "\nExpected a single decimal only" );
				
		int[] item = subscript( 1 ).valueType( 0 );
		
		if ( ! isDecimal( item ))
			
			throw new InvalidQueryException( "\nExpected item to be an decimal" );
		
		BigDecimal bigDec = m_treeWalker.getDecimal( item[ 0 ] );
		
		return bigDec.doubleValue();
	}
	
	public void getValues()throws IOException{
		int i=0;
		this.type=1;
		//long timeTestStart=System.currentTimeMillis();
        List<String> columns = this.m_treeWalker.pctable.get(this.xpath);
        this.columns=columns;
       /* if(columns!=null){
        	ResultScanner rs = HbaseReader.getRowKeys(this.m_treeWalker.tableName, this.m_treeWalker.startRow, this.m_treeWalker.stopRow);
        	for(Result r : rs){
        		String row=Bytes.toString(r.getRow());
        		for(i=0;i<columns.size();i++){
        			this.columns.add(row);
        			this.file.add(row);
        		}
        	}
        }*/
       /*
        if(columns != null){
        	ResultScanner rs = HbaseReader.getSpecifiedQualifiers(this.m_treeWalker.tableName, "xmark", columns,this.m_treeWalker.startRow,this.m_treeWalker.stopRow);
        	for(Result r : rs){
        		byte[][] ans = new byte[r.size()][];
        		r.getFamilyMap(Bytes.toBytes("xmark")).values().toArray(ans);
        		
        		for(byte[] a : ans){
        			//String row=new String(r.getRow());
        			System.out.println(Bytes.toString(a));
        		  
        			//ansList.add("<increase>" + Bytes.toString(a) + "</increase>");
        		}
        	}
        }
        
		*/
			
       //ee
	
	}
	
	public void getReal(){
		//System.out.println("进入getReal() "+this.xpath+" "+this.var+this.columns);
		if(this.type==1){
			return;
		}
		List<String> columnsTmp = this.m_treeWalker.pctable.get(this.xpath);
		List<String> resultTmp=new ArrayList<String>();
		Map<String,List<String>> result=new HashMap<String,List<String>>();
		/*
		Set set = this.resultMap.entrySet();
		Iterator i = set.iterator();
		while(i.hasNext()) {
			Map.Entry me = (Map.Entry)i.next();
			List<String> columnsTmpTmp=(List<String>) me.getValue();
			String key=(String) me.getKey();
			for(int j=0;j<columnsTmpTmp.size();j++){
				String column=columnsTmpTmp.get(j);
				for(int k=0;k<columnsTmp.size();k++){
					String tmp=columnsTmp.get(k);
					if(tmp.startsWith(column)){
						resultTmp.add(tmp);
					}
				}
			}
			result.put(key, resultTmp);
			resultTmp=new ArrayList<String>();
		}
		//this.resultMap=result;
		
		*/
		
		
		//下面的对应clomuns,file
		
		//List<String> columnsTmp = this.m_treeWalker.pctable.get(this.xpath);
		List<String> columnsResult=new ArrayList<String>();
		List<String> fileResult =new ArrayList<String>();
		for(int i=0;i<this.columns.size();i++){
			String column=this.columns.get(i);
			for(int j=0;j<columnsTmp.size();j++){
				String tmp=columnsTmp.get(j);
				if(tmp.startsWith(column)){
					columnsResult.add(tmp);
					//System.out.println(tmp);
					fileResult.add(this.file.get(i));
				}
			}
		}
		this.columns=columnsResult;
		this.file=fileResult;
		
		
		//下面的为最原始
		/*List<String> columnsResult=new ArrayList<String>();
    	String child1="";
    	String oldFile="";
    	String newFile="";
    	int validNumTmp=0;
    	List<String> columnsTmp =new ArrayList<String>();
    	List<Integer> fileTmp=new ArrayList<Integer>();
    	for(int i=0;i<file.size();i++){		
    		String parent=this.columns.get(i);
    		newFile=this.m_treeWalker.fileList[this.file.get(i)];
    		if(!newFile.equals(oldFile)){
    		Map<String, List<String>> mt = this.m_treeWalker.mtm.get(newFile);
    		columnsTmp = mt.get(xpath);
    		}
    		for(int j=0;j<columnsTmp.size();j++){
    			child1=columnsTmp.get(j);
    			if(child1.startsWith(parent)){
    				columnsResult.add(child1);
    				fileTmp.add(this.file.get(i));
    				validNumTmp++;
    			}
    		}
    	}
    	this.columns=columnsTmp;
    	this.file=fileTmp;
    	this.validNum=validNumTmp;
    	*/
	}
	
	public ResultList intersection(ResultList lhr,ResultList rhr){
		System.out.println("进入交集操作");
		ResultList result=new ResultList(this.m_treeWalker);
		//现在还未判断是否为同一变量。
		result.var=lhr.var;
		Hashtable<String, Integer> tmp=new Hashtable<String,Integer>();
		
		
		/*
		Set set = lhr.resultMap.entrySet();
		Iterator i = set.iterator();
		List<String> resultTmp=new ArrayList<String>();
		while(i.hasNext()) {
			Map.Entry me = (Map.Entry)i.next();
			List<String> columnsTmp=(List<String>) me.getValue();
			String key=(String) me.getKey();
			for(int h=0;h<columnsTmp.size();h++){
				tmp.put(columnsTmp.get(h), 1);
			}
			List<String> columnsTmp2=rhr.resultMap.get(me.getKey());
			for(int h=0;h<columnsTmp2.size();h++){
				if(tmp.containsKey(columnsTmp2.get(h))){
					resultTmp.add(columnsTmp2.get(h));
				}
			}
			result.resultMap.put(key, resultTmp);
			tmp.clear();
			columnsTmp=new ArrayList<String>();
		}
		*/
		int i=0;
		for(i=0;i<lhr.columns.size();i++){
			tmp.put(lhr.columns.get(i)+lhr.file.get(i), 1);
		}
		for(i=0;i<rhr.columns.size();i++){
			if(!tmp.containsKey(rhr.columns.get(i)+rhr.file.get(i))){
				continue;
			}
			else{
				result.columns.add(rhr.columns.get(i));
				result.file.add(rhr.file.get(i));
			}
		}
		
		return result;
	}
	
	public ResultList union(ResultList lhr,ResultList rhr){
		System.out.println("进入并集操作");
		ResultList result=new ResultList(this.m_treeWalker);
		//现在还未判断是否为同一变量。
		result.var=lhr.var;
		Hashtable<String, Integer> tmp=new Hashtable<String,Integer>();
		
		/*
		Set set = lhr.resultMap.entrySet();
		Iterator i = set.iterator();
		List<String> resultTmp=new ArrayList<String>();
		while(i.hasNext()) {
			Map.Entry me = (Map.Entry)i.next();
			List<String> columnsTmp=(List<String>) me.getValue();
			String key=(String) me.getKey();
			for(int h=0;h<columnsTmp.size();h++){
				String s=columnsTmp.get(h);
				//System.out.println("The big sb is "+s);
				resultTmp.add(s);
				tmp.put(s, 1);
			}
			List<String> columnsTmp2=rhr.resultMap.get(key);
			if(columnsTmp2!=null){
				for(int h=0;h<columnsTmp2.size();h++){
					if(!tmp.containsKey(columnsTmp2.get(h))){
						resultTmp.add(columnsTmp2.get(h));
					}
				}
				rhr.resultMap.remove(key);
			}
			result.resultMap.put(key, resultTmp);
			tmp.clear();
			columnsTmp=new ArrayList<String>();
			
		}
		
		set = rhr.resultMap.entrySet();
		i = set.iterator();
		while(i.hasNext()) {
			Map.Entry me = (Map.Entry)i.next();
			System.out.println((String)me.getKey()+"\t"+(List<String>)me.getValue());
			result.resultMap.put((String)me.getKey(), (List<String>)me.getValue());
		}
		*/
		
		int i=0;
		for(i=0;i<lhr.columns.size();i++){
			result.columns.add(lhr.columns.get(i));
			result.file.add(lhr.file.get(i));
			tmp.put(lhr.columns.get(i)+lhr.file.get(i), 1);
		}
		for(i=0;i<rhr.columns.size();i++){
			if(!tmp.containsKey(rhr.columns.get(i)+rhr.file.get(i))){
				result.columns.add(rhr.columns.get(i));
				result.file.add(rhr.file.get(i));
			}
			else{
				continue;
			}
		}
		
		
		return result;
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