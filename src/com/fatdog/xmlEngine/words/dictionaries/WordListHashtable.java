/*  XQEngine Copyright (C) 2002-2005 Fatdog Software Inc. [howardk@fatdog.com]
 *	--------------------------------------------------------------------------
 *  This file is part of XQEngine and is distributed under the terms of the
 *  GNU General Public License. See additional info at the end of this file. */
 
package com.fatdog.xmlEngine.words.dictionaries;

import com.fatdog.xmlEngine.IntList;
import com.fatdog.xmlEngine.words.dictionaries.WordList;

	/**
	 * There be words here.
	 * 
	 * <P>This class is derived originally from Hashtable. At present
	 * it's not a one-to-one replacement for it, but it's close. 
	 * 
	 * <P>12dec03: I've mutated IntList slightly by adding an IntList m_nextList
	 * field. This allows several ...XRef()-named methods here to use a linked-
	 * list structure to hold blocks of words having identical hashes and
	 * unique id's, with each word block holding the sequence of docId's
	 * the word occurs in. 
	 * 
	 * <P>NOTA: The way rehashing seems to work (looking at Sun code) is if 
	 * m_count exceeds the threshold, where m_count is the total number of 
	 * unique entries, not the number of buckets.
	 * Do we really want to rehash when this count is exceeded ????
	 * 
	 * @author Howard Katz, howardk@fatdog.com
	 * @version 0.69
	 */

public
class WordListHashtable 
{
	private IntList	m_list;    // one per table slot rather than chained HashtableEntry's

    private IntList[]	m_table;    // for new condensed form

    private WordList	m_wordList;	// words get stored here, not in the table itself

    private  int   	m_count;
    private int		threshold;
    private float	loadFactor;
    
    private final static int KEY		= 0;
	private final static int HASH	= 1;
	private final static int INDEX	= 2;
	private final static int DOCID	= 3;

    private static final long serialVersionUID = 1421746759512286392L;

    public WordListHashtable(int initialCapacity, float loadFactor)
    //-------------------------------------------------------------
    {
    	if ((initialCapacity <= 0) || (loadFactor <= 0.0)) {
    	    throw new IllegalArgumentException();
    	}
    	this.loadFactor = loadFactor;
    	
    	if ( initialCapacity % 2 == 0 )
    		++ initialCapacity;
    	threshold = (int)(initialCapacity * loadFactor);

    	m_table     = new IntList[ initialCapacity ];
    	m_wordList  = new WordList( initialCapacity, 6 * initialCapacity );
    }

    public WordListHashtable(int initialCapacity)   { this(initialCapacity, 0.75f); }
    //------------------------------------------

    public WordListHashtable()  { this(101, 0.75f); }
    //------------------------

    public void put( String word, int[] hashId )
    //-----------------------------------------
    {
        int index = hashId[ 1 ];
        
        if ( word == null )
            throw new NullPointerException( 
            	"\nWordListHashtable.put(): word argument was null" );
            
        if ( m_count >= threshold )
        {
        	// System.out.println( "rehashing at threshold = "  + threshold );
        	rehash();
        	
        	// table length has changed; stored index has to be updated appropriately
			hashId[ 1 ]   = (hashId[ 0 ] & 0x7FFFFFFF) % m_table.length;
        	put( word, hashId );
        	return;
        }

	    if ( m_table[ index ] == null )
	        m_table[ index ] = new IntList( 2, 2 );  // 2 int/entry, start w/ 2 entries
            
            							// key		hash
        m_table[ index ].addRef_2( hashId[ 0 ], hashId[ 2 ] );
		++ m_count;
        
	    m_wordList.append( word );
    }
    
    public String wordFromKey( int key )
    //----------------------------------
    {
        return m_wordList.wordFromKey( key );
    }

    public int getKey( String word )
    //------------------------------
    {
	    int hash    = word.hashCode();
	    int index   = (hash & 0x7FFFFFFF) % m_table.length;

	    IntList list = m_table[ index ];
	    
	    if ( list == null )
	        return -1;
	        
	    for( int i = 0; i < list.count(); i ++ )
	    {
	        int oldHash = list.getRef_1( i );
            int wordIx  = list.getRef_2( i ); // get 2nd of 2 args
            
	        if ( hash == oldHash && m_wordList.wordAtLocationEquals( word, wordIx ) )
                return wordIx;
	    }	        
	    
	    return -1;
    }
    
    public boolean containsKey( String word )
    //---------------------------------------
    {
	    int hash    = word.hashCode();
	    int index   = (hash & 0x7FFFFFFF) % m_table.length;

	    IntList list = m_table[ index ];

        if ( list == null )
            return false;

	    for( int i = 0; i < list.count(); i ++ )
	    {
	        int oldHash = list.getRef_1( i );
            int wordIx  = list.getRef_2( i ); // get 2nd of 2 args
            
	        if ( hash == oldHash && m_wordList.wordAtLocationEquals( word, wordIx ) )
                return true;
	    }

        return false;
    }
    
    // we're adding a new word plus the docId xref for this document
    
    public void putWordXRef( String word, int[] wordArgs )
    //------------------------------------------------------
    {
    	int index 			= wordArgs[ INDEX ];
    	IntList listHead 	= m_table[ index ];
        
		if ( word == null )
			throw new NullPointerException( 
				"\nWordListHashtable.put(): word argument was null" );
            
		if ( m_count >= threshold )
		{
			// System.out.println( "rehashing at threshold = "  + threshold );
			rehash();
        	
			// table length has changed; cached index has to be updated appropriately
			
			wordArgs[ INDEX ] = ( wordArgs[ HASH ] & 0x7FFFFFFF) % m_table.length;
			
			putWordXRef( word, wordArgs );
			return;
		}
			
		IntList entry = new IntList( 1, 3 ); // 3 slots in a 1-int-per-slot intlist
		
		if ( listHead == null )
			listHead = entry;
		else
		{
			entry.setNextList( listHead) ;
			listHead = entry;
		}
            			
        entry.addRef_1( wordArgs[ KEY ] );
        entry.addRef_1( wordArgs[ HASH ] );
        entry.addRef_1( wordArgs[ DOCID ] );
        
		++ m_count;
		m_wordList.append( word );
    }
    
    /**
     * We know the word's already in the dictionary -- we need to find its
     * linked list entry (looking thru the list for a matching id)
     * and add the currDocId to the end of the entry
     * 
     * @param wordArgs [ key ][ hash ][ index ] [ currDocId ]
     */
    public void putXRef( int[] wordArgs )
    //-------------------------------------
    {
		int index 		= wordArgs[ INDEX ];
		int searchKey	= wordArgs[ KEY ];
		IntList entry 	= m_table[ index ];
		
		while ( entry != null )
		{			
			if ( entry.getRef_1( KEY ) == searchKey )
			{
				entry.addRef_1( wordArgs[ DOCID ] );
				break;
			}
			
			entry = entry.nextList();
		}
    }
    
    /**
     * We check whether the word's already been entered in the dictionary.
     * whether it has been or not we return its computed hash and table index,
     * to save a subsequent call to this object from having to recompute them.
     * 
     * <P>We also return its key if it exists, else -1. We also return
     * its lastDocId entry, since we want to make sure the current doc
     * has one and only one entry in the slot for this word
     * 
     * 							0		1		2		3
	 * wordArgs entries are: [ key ][ hash ][ index ][ lastDoc ]
	 * 
     * @param word the word being checked for existence in the dictionary
     * @param wordArgs a 4-int array with contents as above
     * @return boolean -- true if the word already exists
     */
	public boolean containsWordXRefs( String word, int[] wordArgs )
	//---------------------------------------------------------------
	{
		int hash    = word.hashCode();
		int index   = (hash & 0x7FFFFFFF) % m_table.length;
		
		wordArgs[ 0 ] = -1; // until proven otherwise
		wordArgs[ 1 ] = hash; wordArgs[ 2 ] = index;
		
		IntList list = m_table[ index ];

		if ( list == null ) return false;
			
		while ( list != null )
		{
			int key 		= list.getRef_1( 0 );
			int oldHash	= list.getRef_1( 1 );
			
			if ( hash == oldHash && m_wordList.wordAtLocationEquals( word, key ) )
			{
				wordArgs[ 0 ] = key;
				wordArgs[ 3 ] = list.getRef_1( list.count() - 1 );
				return true;
			}
			
			list = list.nextList();
		}
		
		return false;
	}
    
    // if true,  return hash + key (== word posit)
    // if false, return hash + -1
    
    public boolean containsKey( String word, int[] hashId )
    //-----------------------------------------------------
    {
	    int hash    = word.hashCode();
	    int index   = (hash & 0x7FFFFFFF) % m_table.length;
	    
	    hashId[ 0 ] = hash;
	    hashId[ 1 ] = index;
	    
	    hashId[ 2 ] = -1;   // unless proven otherwise

	    IntList list = m_table[ index ];

        if ( list == null )
            return false;

	    for( int i = 0; i < list.count(); i ++ )
	    {
	        int oldHash = list.getRef_1( i );
            int wordIx  = list.getRef_2( i ); // get 2nd of 2 args
            
	        if ( hash == oldHash && m_wordList.wordAtLocationEquals( word, wordIx ) )
	        {
	            hashId[ 2 ] = wordIx;
                return true;
            }
	    }

        return false;
    }    

    public int size()           { return m_count; }
    //---------------
    public boolean isEmpty()    { return m_count == 0; }
    //----------------------

	protected void rehash()
	//-----------------------
	{
		IntList[] oldTable = m_table;
		int oldCapacity = m_table.length;
		int newCapacity = 2 * oldCapacity + 1;
		threshold = (int)(newCapacity * loadFactor);
		IntList[] newTable = new IntList[ newCapacity ];
		
		for (int index = oldCapacity ; index -- > 0 ;) 
		{
			if ( oldTable[ index ] == null )
				continue;
				
			IntList list = oldTable[ index ];
			for ( int j = 0; j < list.count(); j++ )
			{
				int[] hashPlusKey = list.getRef_Both( j );
				
				int newIndex = ( hashPlusKey[ 0 ] & 0x7FFFFFFF ) % newCapacity;
				
				if ( newTable[ newIndex ] == null )
					newTable[ newIndex ] = new IntList( 2, 2 );  // 2 int/entry, start w/ 2 entries
            
				newTable[ newIndex ].addRef_2( hashPlusKey );
			}
		}
		
		m_table = newTable;
		oldTable = null;
	}

    public void debugSummary()
    //------------------------
    {
        int slotsUsed = 0, totalChainLength = 0, totalWordLength = 0;

        int tableLen = m_table.length;

    	for ( int j = tableLen ; j-- > 0 ;)
    	    if ( m_table[ j ] != null )
    	    {
    	        ++ slotsUsed;

                IntList list = m_table[ j ];
        	    for( int k = 0; k < list.count(); k ++ )
        	    {
        	        ++ totalChainLength;
        	    }
    	    }

        // NOTA: totalChainLength == number of entries in the table

    	System.out.println( "Hashtable summary" );
    	System.out.println( "-----------------" );
    	System.out.println( "Table length       = " + tableLen );
    	System.out.println( "Slots used         = " + slotsUsed );
    	System.out.println( "Total chain length = " + totalChainLength );

    	double ratio1 = slotsUsed / (double)tableLen;
    	double ratio2 = totalChainLength / (double) slotsUsed;
    	double ratio3 = totalWordLength / (double) totalChainLength;

    	ratio1 = (int)( 100 * ratio1 ) / 100.0;
    	ratio2 = (int)( 100 * ratio2 ) / 100.0;
    	ratio3 = (int)( 100 * ratio3 ) / 100.0;

    	System.out.println( "Load factor        = " + ratio1 );
    	System.out.println( "Entries/chain      = " + ratio2 );
    	System.out.println( "Average word len   = " + ratio3 + "\n" );
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