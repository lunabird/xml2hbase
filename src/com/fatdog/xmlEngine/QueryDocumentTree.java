/*  XQEngine Copyright (C) 2002-2005 Fatdog Software Inc. [howardk@fatdog.com]
 *	--------------------------------------------------------------------------
 *  This file is part of XQEngine and is distributed under the terms of the
 *  GNU General Public License. See additional info at the end of this file. */
 
package com.fatdog.xmlEngine;

import com.fatdog.xmlEngine.words.WordManager;
import java.util.Stack;

	/**
	 * A {@link NodeTree} optimized to hold constructed nodes.
	 * 
	 * @author Howard Katz, howardk@fatdog.com
	 * @version 0.6e
	 */

public class QueryDocumentTree extends NodeTree 
{
	public final static int RESERVED			= 3;
	public final static int	ENCLOSED_RESULTS 	= 4;
	public final static int ELEMENT_CTOR		= 5;
	
	Stack			m_tagStack;
	int				m_nodeIx;
	
	TreeWalker		m_treeWalker;

	WordManager     m_elementWM;
	WordManager     m_attributeWM;
	
	ResultList[]	m_enclosedResults;
	
	IntList			m_docId = null;
	
	boolean			m_isWordIndexing;
	
	
	public QueryDocumentTree( TreeWalker walker, int numInitialEntries, int allocationPolicy )
	//----------------------------------------------------------------------------------------
	{
		super( walker.getIndexer(), numInitialEntries, allocationPolicy );
		
		m_treeWalker = walker;	
		
		setId( DocItems.QUERY_DOCUMENT );
		
		m_nodeIx        = -1;
		
		m_tagStack = new Stack();
		m_tagStack.push( new Long( (long) -1 << 4*8 | NodeTree.NO_PRIOR_SIBS ) );
		
		m_elementWM     = walker.getIndexer().getElementWM();
		m_attributeWM   = walker.getIndexer().getAttributeWM();
		
		m_enclosedResults = new ResultList[ 0 ];
		
		m_isWordIndexing = walker.getIndexer().getEngine().isWordIndexing();
		
		if ( m_isWordIndexing )
		{
			m_docId = new IntList(1);
			m_docId.addRef_1( -1 );
		}
	}
	
	// 16dec03
	// NOTA: at the moment, we don't construct namespaced elements

	public int startElement( String localName, String qName, boolean isEnclosed )
	//---------------------------------------------------------------------------
	{
		// int namespaceIx = getNamespaceIndex( qName, namespaceURI );
		
		int elementId = ++ m_nodeIx;
	        
		long both = (( Long) m_tagStack.pop() ).longValue();
	        
		int parent      = (int) ( both >>> 4*8 );
		int priorSib    = (int) ( both & 0xFFFFFFFF );     
	        
		both = (long) parent << 4*8 | elementId;
		m_tagStack.push( new Long( both ));
			
		int[] nameKeys = m_elementWM.addEntry( localName, qName );
			
		if ( isEnclosed )
			addElementNode( -1, nameKeys, parent, priorSib );
		else
			super.addElementNode( -1, nameKeys, parent, priorSib );
	        
		both = (long) elementId << 4*8 | NodeTree.NO_PRIOR_SIBS;
		m_tagStack.push( new Long( both ) );
			
		return elementId;
	}
	
	// NOTA: 16dec03 Namespacing code needed here
	
	public void addElementNode( int namespaceIx, int[] keys, int parent, int priorSibling )
	//-------------------------------------------------------------------------------------
	{
		checkAllocation();
    
		int ix = m_ix;
        
		m_tree[ ix ] = ELEMENT_CTOR;
            
		if ( priorSibling == NO_PRIOR_SIBS )
			m_tree[ ix ] |= 0x80000000;   // mark as first in sib chain
		else 
			m_tree[ priorSibling * INTS_PER_ENTRY ] |= ( m_numNodes << 8 );

		++ ix;
        
		m_tree[ ix ++ ]    = keys[ 0 ]; 
		m_tree[ ix ++ ]    = keys[ 1 ];
		m_tree[ ix ++ ]    = parent;

		m_ix = ix;
        
		++ m_numNodes;        
	}
	
	public int endElement()
	//---------------------
	{ 		        
		long both = ((Long) m_tagStack.pop() ).longValue();

		return (int) ( both >>> 4*8 );
	}		
	
	// TODO: implement attribute normalization rules as defined in XQuery 1.0 spec 3.7.1.1 (aug2003)

	public void addAttribute( String qName, String value )
	//----------------------------------------------------
	{		
		long both = (( Long) m_tagStack.peek() ).longValue();
		int parentElement = (int) ( both >>> 4*8 );
		
		++ m_nodeIx;
     
		int[] keys = m_attributeWM.addEntry( null, qName );
		
		addAttributeNode( -1, keys, value, parentElement );
	}
	
	int addTextNode( String text )
	//-----------------------------
	{
		long both = ((Long) m_tagStack.pop() ).longValue();
        
		int parent      = (int) ( both >>> 4*8 );
		int priorSib    = (int) ( both & 0xFFFFFFFF );
        
		m_tagStack.push( new Long( (long) parent << 4*8 | ++ m_nodeIx ));
        
		int nodeId = addElementTextNode( text.toCharArray(), 0, text.length(), parent, priorSib );
               
        // 	BUG-FIX 5feb04
        //	add m_isWordIndexing instance variable and only call characters() if true
        //	NOTA: make sure m_elementWM is instantiated first if we're going to be doing that
        //	(see NodeTree ctor for a template)
        
        if ( m_isWordIndexing )
        {
			m_elementWM.characters( text.toCharArray(), 0, text.length(), m_nodeIx, m_docId );
        }
		
		return nodeId;
	}
    
	public int reserveNodeForEnclosedResults( )
	//-----------------------------------------
	{
		long both 	= ((Long) m_tagStack.peek() ).longValue();      
		int parent 	= (int) ( both >>> 4*8 );
		
		checkAllocation();
    	
		int ix = m_ix;
    	
		m_tree[ ix ++ ] = RESERVED;
		
		m_tree[ ix ++ ] = 0;		
		m_tree[ ix ++ ] = 0;
		m_tree[ ix ++ ] = parent;
    	
		m_ix = ix;
    	
		int reservedSpot = m_numNodes ++;
    	
		++m_nodeIx;
		
		return reservedSpot;
	}
	
	void updateReservedResults( int nodeIx, ResultList results )
	//----------------------------------------------------------
	{
		int oldLen = m_enclosedResults.length;
		
		ResultList[] tempRList = new ResultList[ oldLen + 1];
		System.arraycopy( m_enclosedResults, 0, tempRList, 0, oldLen );
		
		m_enclosedResults = tempRList;
		m_enclosedResults[ oldLen ] = results;
		tempRList = null;
		
		int ix = nodeIx * INTS_PER_ENTRY;
		
		m_tree[ ix ++ ] = ENCLOSED_RESULTS;
		
		m_tree[ ix ++ ] = oldLen;
		m_tree[ ix ++ ] = 0;	// for now, otherwise parent
	}

	public ResultList getEnclosedResults( int node )
	//---------------------------------------------
	{
		return m_enclosedResults[ m_tree[ ( INTS_PER_ENTRY * node ) + 1 ]];
	}

	public boolean isTopLevelNode( int nodeId )
	//-----------------------------------------
	{
		return getParent( nodeId ) == -1;
	}
	
	public ResultList newConstructedTextNode( int nodeId )
	//----------------------------------------------------
	{
		int[] value = { nodeId, NodeTree. TEXT }; 
		
		return new ResultList( m_treeWalker, value );
	}
	
	public ResultList newConstructedElement( int nodeId )
	//---------------------------------------------------
	{
		int[] value = { nodeId, NodeTree. ELEM }; // { nodeId, DocItems. CONSTRUCTED_ELEM };
		
		return new ResultList( m_treeWalker, value );
	}
	
	// we're done constructing element nodes -- time to move them to their final destination
	// TODO: move toplevel attribute and other non-element nodes as well
	
	public void CopyNodesToQueryDocTree( QueryDocumentTree destinationTree, int fromNode )
	//------------------------------------------------------------------------------------
	{
		for ( int i = fromNode; i < getNodeCount(); i++ )
		{
			if ( getType( i ) == ELEM )
			
				i = copyElementToQueryDocTree( destinationTree, i );
				
			else if ( getType( i ) == ELEMENT_CTOR )
			
				;
				
			else if ( getType( i ) == TEXT )
			
				;
				
			else if ( getType( i ) == ENCLOSED_RESULTS )
			
				;
		}
	}
	
	int copyElementToQueryDocTree( QueryDocumentTree destinationTree, int node )
	//--------------------------------------------------------------------------
	{
		int parent = node;
		
		while ( ++ node < m_numNodes && getParent( node ) >= parent )
		{
			switch( getType( node ))
			{
				case ELEM :				
								node = copyElementToQueryDocTree( destinationTree, node );
								break;
										
				case ATTR :				
										
								break;
										
				case TEXT :				
								break;
										
				case ENCLOSED_RESULTS :	
				
								ResultList results = getEnclosedResults( node );
								results.copyResultItemsToDestination( this, destinationTree );
								break;
										
				case ELEMENT_CTOR :	
				
								int nextParent = node;
								while( ++ node < m_numNodes && getParent( node ) >= nextParent) 
								{ --node; }
								continue;
										
				default :
				
					throw new IllegalArgumentException( 
							"\nQueryDocumentTree.copyElementToQueryDocTree(): unknown illegal node type" );						
			}
		}
		
		return node;
	}
}
