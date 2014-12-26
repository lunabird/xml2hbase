/*  XQEngine Copyright (C) 2002-2005 Fatdog Software Inc. [howardk@fatdog.com]
 *	--------------------------------------------------------------------------
 *  This file is part of XQEngine and is distributed under the terms of the
 *  GNU General Public License. See additional info at the end of this file. */

package com.fatdog.xmlEngine.words.dictionaries;

	/**
	 * A class used to store and retrieve words.
	 * 
	 * <P>This class is a thin wrapper for the actual dictionary implementation,
	 * which at present is a {@link WordListHashtable}. All heavy lifting
	 * is delegated to that class.
	 * 
	 * @author Howard Katz, howardk@fatdog.com
	 * @version 0.69
	 */

import com.fatdog.xmlEngine.IntList;

public class Dictionary
{
    protected int m_numWords = 0;

    WordListHashtable   m_table;

	// numSubLists not used this WordListHashtable implementation
	
    public Dictionary( int numSublists, int newEntryAllocs ) // 1st arg unused
    //------------------------------------------------------
    {
        m_table = new WordListHashtable( newEntryAllocs );      
    }

    public int addWordEntry( char[] charWord, int start, int end )
    //----------------------------------------------------------------------------
    {
        String word = new String( charWord, start, end - start );
        
        return addWordEntry( word );
    }
	
    public int addWordEntry( String word )
    //----------------------------------------------------
    {
        int count 		= m_numWords;         
        int[] hashId 	= new int[ 3 ];

    /*  hashId array filled in with: (1) hash (2) index, and (3) wordKey/id
        if word is present, both hash and Id are filled in; 
        if the word isn't present, id == -1
    */
        if ( m_table.containsKey( word, hashId ) )
            return hashId[ 2 ];

        hashId[ 2 ] = m_numWords++;	// becomes the word's key/id
        
        m_table.put( word, hashId );
        
        return count ;      
    }  

	public int addWordEntryWithXRef( char[] charWord, int start, int end, IntList args )
	//----------------------------------------------------------------------------
	{
		return addWordEntryWithXRef( new String( charWord, start, end - start ), args );
	}
	
	/*
	 	if the word is new, put it and this docId into table/return
	 	else // the word is old
	 		if this docId != the docId from the word
	 			add the docId
	 */
	public int addWordEntryWithXRef( String word, IntList args )
	//----------------------------------------------------------
	{
		int count 		= m_numWords;         
		int[] wordArgs	= new int[ 4 ];
		
		int thisDocId	= args.getRef_1( args.count() - 1 );
		
		// wordArgs gets returned as 
		//
		//	  0		1		2		3
		// [ key ][ hash ][ index ][ lastDoc ]

		// word is new? add it plus the docId 'xref' (for lack of a better term)
		
		if ( ! m_table.containsWordXRefs( word, wordArgs ) )
		{
			wordArgs[ 0 ] = m_numWords ++;
			wordArgs[ 3 ] = thisDocId;
			
			m_table.putWordXRef( word, wordArgs );
			
			return count;
		}

		// word is old. if we've already seen one in the curr doc, we're done
		
		if ( wordArgs[ 3 ] == thisDocId )
			return wordArgs[ 0 ];
			
		// add a new doc xref  at the end of the entry for this word
		
		m_table.putXRef( wordArgs );
		
		return count ;  
	}
	
    public String wordFromKey( int key )
    //----------------------------------
    {
        return m_table.wordFromKey( key );
    }

    // only return hashKey if the word's in the dictionary

    public int keyFromWord( String word )
    //-----------------------------------
    {
    	
        return m_table.containsKey( word ) ?

                m_table.getKey( word ) :
                -1;
    }

    public int keyFromWord( char[] charWord )
    //---------------------------------------
    {
        String word = new String( charWord );

        return m_table.containsKey( word ) ?
        
                m_table.getKey( word ) :
                -1;
    }

    public int getNumWords()        { return m_numWords; }
    //----------------------
    
    public void debugSummary()      { m_table.debugSummary(); }
    //------------------------
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