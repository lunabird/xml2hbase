/*  XQEngine Copyright (C) 2002-2005 Fatdog Software Inc. [howardk@fatdog.com]
 *	--------------------------------------------------------------------------
 *  This file is part of XQEngine and is distributed under the terms of the
 *  GNU General Public License. See additional info at the end of this file. */
 
package com.fatdog.xmlEngine.words.dictionaries;

	/**
	 * A dynamic array structure used to hold lists of words.
	 * 
	 * @author Howard Katz, howardk@fatdog.com
	 * @version 0.69
	 */

public class WordList
{
    static final int m_allocationPolicy = com.fatdog.xmlEngine.XQEngine.AGGRESSIVE;

    char[]      m_words;
    int[]       m_offsets;

    int         m_wordCount;
    int         m_currCharsLength;

    private int m_newEntryAllocs;
    private int m_newWordByteAllocs;


    public WordList() { this( 0, 0 ); }
    //---------------

    public WordList( int newEntryAllocs, int newWordByteAllocs )
    //----------------------------------------------------------
    {
        m_newEntryAllocs    = newEntryAllocs;
        m_newWordByteAllocs = newWordByteAllocs;

        m_offsets   = new int[ m_newEntryAllocs > 0 ? m_newEntryAllocs : 4 ];
        m_words     = new char[ m_newWordByteAllocs > 0 ? m_newWordByteAllocs : 24 ];
    }

    public void append( String word )
    //-------------------------------
    {
        append( word.toCharArray(), 0, word.length() );
    }
    
    public String wordFromKey( int key )
    //----------------------------------
    {
        int offset = m_offsets[ key ];
        int len = ( key == m_wordCount - 1 ) ?
        
                        m_currCharsLength - offset :
                        m_offsets[ key + 1 ] - offset;
                        
        return new String( m_words, offset, len );         
    }
    
    public boolean wordAtLocationEquals( String word, int location )
    //--------------------------------------------------------------
    {
        int offset = m_offsets[ location ];
        int len = ( location == m_wordCount - 1 ) ?
        
                        m_currCharsLength - offset :
                        m_offsets[ location + 1 ] - offset;
                        
        if ( word.length() != len )
            return false;
            
        for( int i = 0; i < len; i ++ )
            if ( word.charAt( i ) != m_words[ offset ++ ] )
                return false;
                        
        return true;
    }
    
    public void append( char[] word, int off, int end )
    //-------------------------------------------------
    {
        int len = end - off;

        checkAllocation( len );

        System.arraycopy( word, off, m_words, m_currCharsLength, len );

        m_offsets[ m_wordCount ] = m_currCharsLength;

        m_currCharsLength += len;
        m_wordCount ++;
    }

    public void checkAllocation( int wordLength )
    //-------------------------------------------
    {
        int newAlloc;

        if ( m_wordCount >= m_offsets.length )
        {
            if      ( m_allocationPolicy == com.fatdog.xmlEngine.XQEngine.AGGRESSIVE )   newAlloc = 2 * m_offsets.length;
            else if ( m_allocationPolicy == com.fatdog.xmlEngine.XQEngine.MILD )         newAlloc = 3 * m_offsets.length / 2;
            else                                                    newAlloc = 5 * m_offsets.length / 4;

            int[] newOffsets = new int[ newAlloc ];

            System.arraycopy( m_offsets, 0, newOffsets, 0, m_wordCount );
            m_offsets   = newOffsets;
            newOffsets  = null;
        }

        int neededLen = m_currCharsLength + wordLength;
        if ( neededLen > m_words.length )
        {
            // NOTA: 3/2 won't work unless initial alloc is at least 2, etc
            // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

            if      ( m_allocationPolicy == com.fatdog.xmlEngine.XQEngine.AGGRESSIVE )   newAlloc = 2 * m_words.length;
            else if ( m_allocationPolicy == com.fatdog.xmlEngine.XQEngine.MILD )         newAlloc = 3 * m_words.length / 2;
            else
                    newAlloc = m_words.length + ( 6 * m_newWordByteAllocs );

            if ( neededLen > newAlloc )
                newAlloc = neededLen;

            char[] newWords = new char[ newAlloc ];

            System.arraycopy( m_words, 0, newWords, 0, m_words.length );
            m_words = newWords;

            newWords = null;
        }
    }

/*
    long saveIndex( DataOutputStream out ) throws IOException
	//------------------------------------
	{
	    out.writeInt( m_newEntryAllocs );
	    out.writeInt( m_newWordByteAllocs );

	    out.writeInt( m_wordCount );
	    out.writeInt( m_currCharsLength );

	    out.writeInt( m_firstIx );

	    for( int i = 0; i < m_wordCount; i++ )
            out.writeInt( m_offsets[ i ] );

	    for( int i = 0; i < m_wordCount; i++ )
            out.writeInt( m_offsetIx[ i ] );

        for( int i = 0; i < m_currCharsLength; i++ )
            out.writeChar( m_words[ i ] );

	    return Sizes.SINT + Sizes.SINT
	            + 2 * m_wordCount * Sizes.SINT
	            + m_currCharsLength * Sizes.SCHAR;
	}

    public static StringList loadIndex( DataInputStream in ) throws IOException
    //------------------------------------------------------
    {
        StringList list = null;

        int entryAllocs = in.readInt();
        int wordByteAllocs = in.readInt();

        list = new StringList( entryAllocs, wordByteAllocs );

        list.m_wordCount        = in.readInt();
        list.m_currCharsLength  = in.readInt();
        list.m_firstIx          = in.readInt();

        int[] offsets   = new int[ list.m_wordCount ];
        int[] offsetIx  = new int[ list.m_wordCount ];
        char[] words    = new char[ list.m_currCharsLength ];

    	for( int i = 0; i < list.m_wordCount; i++ )
            offsets[ i ] = in.readInt( );

	    for( int i = 0; i < list.m_wordCount; i++ )
             offsetIx[ i ] = in.readInt();

        for( int i = 0; i < list.m_currCharsLength; i++ )
            words[ i ] = in.readChar( );

        list.m_offsets  = offsets;      offsets = null;
        list.m_offsetIx = offsetIx;     offsetIx = null;
        list.m_words    = words;        words = null;

        return list;
    }
*/    
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