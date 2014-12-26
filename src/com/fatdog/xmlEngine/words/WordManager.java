/*  XQEngine Copyright (C) 2002-2005 Fatdog Software Inc. [howardk@fatdog.com]
 *	--------------------------------------------------------------------------
 *  This file is part of XQEngine and is distributed under the terms of the
 *  GNU General Public License. See additional info at the end of this file. */
 
package com.fatdog.xmlEngine.words;

import com.fatdog.xmlEngine.IndexManager;
import com.fatdog.xmlEngine.IntList;
import com.fatdog.xmlEngine.NodeTree;
import com.fatdog.xmlEngine.exceptions.CategorizedInvalidQueryException;
import com.fatdog.xmlEngine.words.dictionaries.*;

	/**
	 * A class used to manage words.
	 * 
	 * <P>In <code>XQEngine</code>, <i>everything</i> is a word:
	 * the individual words contained in both element and attribute content,
	 * as well as both components of the <code>QNames</code> which represent
	 * element and attribute names. 
	 * 
	 * @author Howard Katz, howardk@fatdog.com
	 * @version 0.69
	 */

public class WordManager extends WordBreaker implements IWordHandler
{
    public final static int NO_ENTRY    = -1;
    //public final static int NO_ENTRY    = 65535;
    public final static int WILDCARD    = -2;
    public final static int NOT_FOUND	= -3;
    
    IndexManager	m_indexer;
    NodeTree        m_nodeTree;
    IWordHandler    m_wordHandler;
    
    TagDictionary   m_prefixesDict;
    TagDictionary   m_localPartsDict;

    TextDictionary  m_wordDict;
    int  m_numWords = 0;

    private int prefixKeyFromPrefix( String prefix ) throws CategorizedInvalidQueryException
	//----------------------------------------------
	{
    	int prefixKey;
    	
    	if ( prefix == null )
    		return NO_ENTRY;
    	else if ( prefix.equals( "*") )
    		return WILDCARD;
    	
    	if ( m_indexer.getEngine().getUseLexicalPrefixes() )
    	{
    		prefixKey = m_prefixesDict.keyFromWord( prefix.toCharArray() );
    		
    		return prefixKey == NO_ENTRY ? NOT_FOUND : prefixKey;
    	}
    	
		String uri = m_indexer.getCurrTreeWalker().currNamespaceBinding( prefix );
		
		if ( uri == null )
		{
			throw new CategorizedInvalidQueryException(			
					"XP0008", "WordManager.keysFromQName(): " +
					"Namespace has not been declared for the prefix: '" + prefix + "'" );
  		}
		
		return m_indexer.getEngine().getSaxHandler().namespaceToId( uri );
	}
    
    /**
     * Returns either a prefix key + localPart key pair, or a namespace index
     * plus localPart key pair, depending on the value of the boolean argument
     * to m_engine.setUseLexicalPrefixes(true/false).
     * 
     * @param qName
     * @return int[]
     * @throws CategorizedInvalidQueryException
     */

    public int[] keysFromQName( String qName ) throws CategorizedInvalidQueryException
    //----------------------------------------
    { 
    	return keysFromQName( parseQName( qName ) ); 
    }
    
    private int[] keysFromQName( String[] part ) throws CategorizedInvalidQueryException
    //------------------------------------------
	{
    	int[] key = { -1, -1 };
    	
    	key[ 0 ] = prefixKeyFromPrefix( part[ 0 ] );
    	
    	String localPart = part[ 1 ];
    	
    	if ( localPart == null )
    		key[ 1 ] = NO_ENTRY;
    	else if ( localPart.equals( "*" ))
    		key[ 1 ] = WILDCARD;
    	else
    		key[ 1 ] = m_localPartsDict.keyFromWord( localPart.toCharArray() );
    	
    	return key;
	}
    
    //public WordManager()    { this( 101, 101, 101 ); }
    //------------------ 

    public WordManager( IndexManager indexer, int prefixes, int locals, int words )
    //-----------------------------------------------------------------------------
    {
    	m_indexer = indexer;
    	// args are numAllocs per type
    	
        m_prefixesDict      = new TagDictionary( prefixes );
        m_localPartsDict    = new TagDictionary( locals );
        
        if ( words > 0 )
        	m_wordDict    	= new TextDictionary( words );
    }
    
    public void startDocument( NodeTree tree )
    //----------------------------------------
    {
        m_nodeTree = tree;
    }  
    
    public void stopDocument() {}
    //------------------------

    public int[] addEntry( String localName, String qName )
    //-----------------------------------------------------
    {
        int prefixKey   = -1;
        int localKey    = -1;
        int colonPos	= -1;

		if ( ( colonPos = qName.indexOf( ':' )) != -1 )
		{
			prefixKey = m_prefixesDict.addWordEntry( qName.substring( 0, colonPos ) );
		}
		
        localKey = m_localPartsDict.addWordEntry( qName.substring( colonPos + 1 ) );

		int[] keys = { prefixKey, localKey };
        return keys;
    }

    //public Dictionary   getPrefixesDictionary()     { return m_prefixesDict; }
    //public Dictionary   getLocalPartsDictionary()   { return m_localPartsDict; }

    public int   getNumPrefixes()     { return m_prefixesDict.getNumWords(); }
    public int   getNumLocalParts()   { return m_localPartsDict.getNumWords(); }
    
    String[] parseQName( String qName )
    //---------------------------------
    {
        String[] parts = new String[ 2 ];
        
        int colonPos = qName.indexOf( ':' );
        if ( colonPos != -1 )
            parts[ 0 ] = qName.substring( 0, colonPos ); // else null
        
        parts[ 1 ] = qName.substring( ++ colonPos, qName.length() );    
        
        return parts;
    }    

     public String getQName( int[] keys )    { return getQName( keys[ 0 ], keys[ 1 ] ); }
    //----------------------------------

    public String getQName( int prefixKey, int localKey )
    //---------------------------------------------------
    {
        String prefix;
        StringBuffer sb = new StringBuffer();

        if ( prefixKey != - 1 )
        {
            sb.append( m_prefixesDict.wordFromKey( prefixKey ) );
            sb.append( ':' );
        }

        sb.append( m_localPartsDict.wordFromKey( localKey ) );

        return sb.toString();
    }

	public int getNumWordInstances()			{ return m_numWords; }
	//-----------------------------
	
	// 10dec03: need to sort out the diff between word instances and headwords
	// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	
    public int getNumWordsIndexed()     { return m_numWords; }
    //-----------------------------
    
    public int getNumWords()            { return m_numWords; }
    									// { return m_wordDict.getNumWords(); }
    //----------------------
    
    //public void debuglocalPartsSummary() 	{ m_localPartsDict.debugSummary(); }
    //----------------------------------
    //public void debugWordsDictSummary()  	{ m_wordDict.debugSummary(); }
    //---------------------------------
    
    // implementing the WordHandler interface to parse element and attribute content.
    // right now we don't do very much except count.
    
    public void newWord( char[] cbuf, int wordStart, int wordEnd, int parent, IntList args )
    //------------------------------------------------------------------------------------
    {
     //   String DEBUG_WORD = new String( cbuf, wordStart, wordEnd - wordStart );
     //   System.out.println( "<" + DEBUG_WORD + "> [node=" + parent + "]" );
        
        int key = m_wordDict.addWordEntryWithXRef( cbuf, wordStart, wordEnd, args );
        
        ++m_numWords;
    }
}

/*
 *  XQEngine is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License5
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