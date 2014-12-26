/*  XQEngine Copyright (C) 2002-2005 Fatdog Software Inc. [howardk@fatdog.com]
 *	--------------------------------------------------------------------------
 *  This file is part of XQEngine and is distributed under the terms of the
 *  GNU General Public License. See additional info at the end of this file. */

package com.fatdog.xmlEngine;

import com.fatdog.xmlEngine.ResultList;
import com.fatdog.xmlEngine.TreeWalker;
import com.fatdog.xmlEngine.words.*;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

	/**
	 * The main class responsible for managing internal index data structures.
	 * 
	 * @author Howard Katz, howardk@fatdog.com
	 * @version 0.69
	 */
	
public class IndexManager
{
    public static final int DOC_NOT_INDEXED	= -1;
	final static String		INDEX_FILENAME 	= "trees.ndx";
    
    XQEngine    m_engine;
    WordManager m_elementWM;
    WordManager m_attributeWM;
    
    ResultList	m_results;

	TreeWalker	m_treeWalker;

    NodeTree[]  m_trees;
    String[]    m_names;

    int         m_numTrees;

    boolean     m_aggressiveAllocator = true;
    
	long		m_nextSerializationOffset = 0;
	Vector	m_serializationTreeOffsets	= new Vector();
	File		m_indexFile = null;

    public IndexManager( XQEngine engine )
    //------------------------------------
    {
        m_engine = engine;

		// if we're distributing QName dictionaries to the NodeTrees, they'll instaniate per tree
		// else we'll do it globally here
		
		if ( ! m_engine.isDistributedQNameDictionaries() )
		{
			// ctor args are initial allocations for 
			// (1) prefixes (2) localParts, and (3) words
			
			int wordAlloc = m_engine.isWordIndexing() ?
								1001 :
								0;
										
			m_elementWM     = new WordManager( this, 11, 101, wordAlloc );
	        m_elementWM.registerWordHandler( m_elementWM, null );
	        
	        m_attributeWM   = new WordManager( this, 3, 29, wordAlloc );
	        m_attributeWM.registerWordHandler( m_attributeWM, null ); 
		}
		
        m_trees      = new NodeTree[ 1 ];
        m_names      = new String[ 1 ];

        m_numTrees  = 0;
    }
    
    // TODO: There may be multiple TreeWalkers per IndexManager; remove this dependency

	public void	setTreeWalker( TreeWalker walker )	{ m_treeWalker = walker; }
	//--------------------------------------------
	public TreeWalker getCurrTreeWalker()			{ return m_treeWalker; }
	//-----------------------------------

    public XQEngine     getEngine()                 { return m_engine; }
    //-----------------------------
    public WordManager  getElementWM()              { return m_elementWM; }
    public WordManager  getAttributeWM()            { return m_attributeWM; }
    //----------------------------------
    public void  setElementWM( WordManager wm )     { m_elementWM = wm; }
    public void  setAttributeWM( WordManager wm )   { m_attributeWM = wm; }
    //-------------------------------------------

	// called back from ResultList ctor to subordinate DocumentItems
	// objects can access ResultList data structures
	
	public void setResultList( ResultList results )	{ m_results = results; }
	//---------------------------------------------
	public ResultList getResultList()				{ return m_results; }
	//-------------------------------

	// name could go in the tree; right now trying it out keeping it external

    public int addTree( NodeTree tree, String name )
    //----------------------------------------------
    {
        checkTreeAllocation();
        
        int numTrees = m_numTrees;
        
        tree.setId( numTrees ); 
        
        m_trees[ numTrees ] = tree;
        m_names[ numTrees ] = name;
        
        //m_trees[ numTrees ].setId( m_numTrees );        

        ++ m_numTrees;
        
        return numTrees;
    }

    void checkTreeAllocation()
    //------------------------
    {
        int len = m_trees.length;

        if ( m_numTrees >= len )
        {
            NodeTree[] newTrees = new NodeTree[ m_aggressiveAllocator ? 2 * len :
                                                                        5 * len / 4 ];

            String[] newNames = new String[ m_aggressiveAllocator ? 2 * len :
                                                                        5 * len / 4 ];
            System.arraycopy( m_trees, 0, newTrees, 0, len );
            System.arraycopy( m_names, 0, newNames, 0, len );

            m_trees = newTrees;
            m_names = newNames;

            newTrees = null;
            newNames = null;
        }
    }

	public int[] getNodeTypeCounts()
	//------------------------------
	{
		int[] totalNodeTypeCounts = new int[ 3 ];
        
		for ( int i = 0; i < getNumTrees(); i++ )
		{
			NodeTree tree = getTree( i );

			int[] nodeCounts = tree.countNodeTypes();

			totalNodeTypeCounts[ 0 ] += nodeCounts[ 0 ];    // elems
			totalNodeTypeCounts[ 1 ] += nodeCounts[ 1 ];    // attrs
			totalNodeTypeCounts[ 2 ] += nodeCounts[ 2 ];    // text
		}        
        
		return totalNodeTypeCounts;
	}
	
	public boolean isValidDocId( int i )	{ return ( i >= 0 && i < getNumTrees() ); }
	//----------------------------------

    public String getDocumentName( int i )
    //------------------------------------
	{
		if ( isValidDocId( i ) )
		{
			return m_names[ i ];
		}
		else if ( i == DocItems. QUERY_DOCUMENT )
		{
			return "QUERY_DOCUMENT";
		}
		
		throw new java.lang.IllegalArgumentException( 
			"\nIndexManager.getDocumentName(): That document ID is not valid (it's out of range): " + i );
	}
    
    public int getNumDocuments()				{ return m_trees.length; }
    //--------------------------
    
    public NodeTree getTree( int id )           { return m_trees[ id ]; }
    //-------------------------------
    public NodeTree[] getTrees()                { return m_trees; }
    //--------------------------
    public int getNumTrees()                    { return m_numTrees; }
    //----------------------

    public int getNumWordsIndexed()     { return m_elementWM.getNumWordsIndexed(); }
    //-----------------------------

    public int getNumWords()            { return m_elementWM.getNumWords(); }
    //----------------------
    
    public int currDocID()   { return m_numTrees - 1; }
    //--------------------
    
    public String getAttributeText( int docID, int attrTextNodeID )
    //-------------------------------------------------------------
    {
        return m_trees[ docID ].getAttributeText( attrTextNodeID );
    }
    
    public int getDocId( String url )
    //-------------------------------
    {
        for ( int i = 0; i < m_numTrees; i++ )
        {
            if ( m_names[ i ].equals( url ) )
                return i;
        }
        
        return DOC_NOT_INDEXED;
    }
    
	File createSerializationIndexFile()
	//---------------------------------
	{
		File file = null;
		
		DataOutputStream dos = null;
		
		String serializationDir	= m_engine.getSerializationDirectory();
		
		try 
		{
			File dir = new File( serializationDir );
			dir.mkdir();
			
			file = new File( dir, INDEX_FILENAME );
			
			if ( file.exists() )
			{
				file.delete();
			}
				
			file.createNewFile();
		}
		catch( IOException ioe )
		{
			throw new RuntimeException( 
				"\nIndexManager.serializationInit(): Couldn't create new index file " + "\"" + INDEX_FILENAME + "\"" );
		}
			
		return file;
	}
    
    public void endDocument( NodeTree tree )
    //--------------------------------------
    {   	
    	if ( m_engine.doSerializeIndex() )
    	{
    		if ( m_numTrees == 1 )
    		{
    			m_indexFile = createSerializationIndexFile();
    		}
    		
    		// offset into 'trees.ndx' stream for this tree
			m_serializationTreeOffsets.addElement( new Long( m_nextSerializationOffset ));
			
			try  // and the starting offset for the next one
			{
				DataOutputStream dos = new DataOutputStream( new BufferedOutputStream( new FileOutputStream( m_indexFile, true )) );
				m_nextSerializationOffset += tree.serialize( dos );
				
				dos.close();
			}
			catch( java.io.IOException ioe )
			{ 
				throw new RuntimeException( "\nIndexManager.endDocument(): IOException on tree serialization" ); 
			}
    	}
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