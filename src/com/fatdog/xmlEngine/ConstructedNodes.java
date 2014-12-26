/*  XQEngine Copyright (C) 2002-2005 Fatdog Software Inc. [howardk@fatdog.com]
 *	--------------------------------------------------------------------------
 *  This file is part of XQEngine and is distributed under the terms of the
 *  GNU General Public License. See additional info at the end of this file. */
 
package com.fatdog.xmlEngine;

import com.fatdog.xmlEngine.words.WordManager;
import java.util.Stack;

	/**
	 * An ancillary {@link NodeTree} subclass that helps in assembling constructed nodes.
	 * 
	 * @author Howard Katz, howardk@fatdog.com
	 * @version 0.61
	 */

public class ConstructedNodes extends NodeTree 
{
	public final static int	EVALED_RESULTS = 3;	// goes w/ NodeTree. ELEM, ATTR, and TEXT
	
	Stack			m_tagStack;
	int				m_nodeIx;
	
	TreeWalker		m_treeWalker;

	WordManager     m_elementWM;
	WordManager     m_attributeWM;
	
	ResultList[]	m_evaledResults;	// NOTA: initially we don't do deep copies of eval'ed node
										// results. This may become a switch-settable option
	
	public ConstructedNodes( TreeWalker walker, int numInitialEntries, int allocationPolicy )
	//---------------------------------------------------------------------------------------
	{
		super( walker.getIndexer(), numInitialEntries, allocationPolicy );
		
		m_treeWalker = walker;	
		
		setId( DocItems.QUERY_DOCUMENT );
		
		m_nodeIx        = -1;
		
		m_tagStack = new Stack();
		m_tagStack.push( new Long( (long) -1 << 4*8 | NodeTree.NO_PRIOR_SIBS ) );
		
		m_elementWM     = walker.getIndexer().getElementWM();
		m_attributeWM   = walker.getIndexer().getAttributeWM();
		
		m_evaledResults = new ResultList[ 0 ];
	}
	
	public void moveNodesToQueryDocTree( QueryDocumentTree queryDocTree )
	//-------------------------------------------------------------------
	{
		if ( m_numNodes > 0 )
		{
			int[] keys = getKeys( m_numNodes - 1 );	// last node
			
			//queryDocTree.startElement( keys );
		}		
	}

	// needs namespacing code
	
	public int startElement( String localName, String qName )
	//-------------------------------------------------------
	{
		int elementId = ++ m_nodeIx;
        
		long both = (( Long) m_tagStack.pop() ).longValue();
        
		int parent      = (int) ( both >>> 4*8 );
		int priorSib    = (int) ( both & 0xFFFFFFFF );     
        
		both = (long) parent << 4*8 | elementId;
		m_tagStack.push( new Long( both ));
		
		int[] keys = m_elementWM.addEntry( localName, qName );
		
		addElementNode( -1, keys, parent, priorSib );
        
		both = (long) elementId << 4*8 | NodeTree.NO_PRIOR_SIBS;
		m_tagStack.push( new Long( both ) );
		
		return elementId;
	}

	public int endElement()
	//---------------------
	{ 		        
		long both = ((Long) m_tagStack.pop() ).longValue();

		return (int) ( both >>> 4*8 );
	}		
	
	// TODO: implement attribute normalizaiton rules as defined in XQuery 1.0 spec 3.7.1.1 (aug2003)
	
	// NOTA: needs namespacing 16dec03

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
               
		m_elementWM.characters( text.toCharArray(), 0, text.length(), m_nodeIx, null );
		
		return nodeId;
	}
	
	// TODO: check evaled results content according to rules in XQuery 1.0 doc (aug03) 3.7.1.3
	// ALSO: not worrying about sibling nodes at this time

	void addEvaledResults( ResultList results )
	//-----------------------------------------
	{
		long both = ((Long) m_tagStack.pop() ).longValue();
        
		int parent      = (int) ( both >>> 4*8 );
		int priorSib    = (int) ( both & 0xFFFFFFFF );
        
		m_tagStack.push( new Long( (long) parent << 4*8 | ++ m_nodeIx ));
		
		int oldLen = m_evaledResults.length;
		ResultList[] tempRList = new ResultList[ oldLen + 1];
		System.arraycopy( m_evaledResults, 0, tempRList, 0, oldLen );
		
		m_evaledResults = tempRList;
		m_evaledResults[ oldLen ] = results;
		tempRList = null;
		
		checkAllocation();
		
		m_tree[ m_ix ++ ] = EVALED_RESULTS;
		
		m_tree[ m_ix ++ ] = oldLen;
		m_ix ++; 	// this slot in tree not used
		
		//long both = ((Long) m_tagStack.peek() ).longValue();       
		
		m_tree[ m_ix ++ ] = parent;
		
		++ m_numNodes;
	}
	
	public ResultList getEvaledResults( int node )
	//--------------------------------------------
	{
		int numSlot = node * INTS_PER_ENTRY;
		
		return m_evaledResults[ m_tree[ numSlot + 1 ]];
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
}
