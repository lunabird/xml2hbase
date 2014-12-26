/*  XQEngine Copyright (C) 2002-2005 Fatdog Software Inc. [howardk@fatdog.com]
 *	--------------------------------------------------------------------------
 *  This file is part of XQEngine and is distributed under the terms of the
 *  GNU General Public License. See additional info at the end of this file. */

package com.fatdog.xmlEngine.words;

import com.fatdog.xmlEngine.IntList;

	/**
	 * A class used to word-parse its way through a collection of text.
	 * <P>Clients needing word-parsing capability should subclass themselves
	 * from this class and register themselves as an {@link IWordHandler}.
	 * 
	 * @author Howard Katz, howardk@fatdog.com
	 * @version 0.69
	 */

public class WordBreaker
{
	protected char[]		m_word;
    protected IWordHandler  m_wordHandler;
    protected boolean		m_ignoreCase	= true;
  
    public void registerWordHandler( IWordHandler handler )
    //-----------------------------------------------------
    {
    	registerWordHandler( handler, null );
    }
    
    public void registerWordHandler( IWordHandler handler, String word )
    //------------------------------------------------------------------
    {
        m_wordHandler 	= handler;
        
        if ( word != null )
        	m_word = word.toCharArray();
    }
    
    public void setSearchWord( char[] word )
    //--------------------------------------
    {
		m_word = word;
		
		if ( m_ignoreCase )
			toLowerCase( m_word, 0, m_word.length );
    }
    
    public char[] 	getSearchWord()						{ return m_word; }
    //-----------------------------
    public void		setIgnoreCase( boolean ignoreCase )	{ m_ignoreCase = ignoreCase; }
    //-------------------------------------------------
    public boolean	getIgnoreCase()						{ return m_ignoreCase; }
    //-----------------------------
    
	public void toLowerCase( char[] word )
	//------------------------------------
	{
		for( int i = 0; i < word.length; i++ )
		{
			word[ i ] = Character.toLowerCase( word[ i ] );
		}    
	}
    
	public void toLowerCase( char[] word, int start, int len )
	//--------------------------------------------------------
	{	
		for( int i = start; i < start + len; i++ )
		
			word[ i ] = Character.toLowerCase( word[ i ] );
			
	}
    
    public void characters( char[] cbuf, int start, int len, int parent, IntList ints )
    //---------------------------------------------------------------------------------
    {
        int     wordStart   = 0;
        int     end         = start - 1;
        boolean inWord      = false;
        char    ch          = '!';
        
        if ( m_ignoreCase )
        	toLowerCase( cbuf, start, len );

        int i;
        for( i = 0; i < len; i++ )
        {
            ch = cbuf[ start + i ];

            if ( Character.isLetterOrDigit( ch ) )
            {
                if ( !inWord )
                {
                    inWord = true;
                    wordStart = start + i;
                }
            }
            else if ( inWord )  // we were in a word, now terminating it
            {
                
                // special case: don't break on apostrophes
                if ( ch == 0x27 || ch == 0x02bc )  continue;

                inWord = false;
                end = start + i;
                
                String word = new String( cbuf, wordStart, end - wordStart );
                
                m_wordHandler.newWord( cbuf, wordStart, end, parent, ints );
            }
        }

        if ( inWord || Character.isLetterOrDigit( ch ) )
        {
			String word = new String( cbuf, wordStart, start + i - wordStart );
			
            m_wordHandler.newWord( cbuf, wordStart, start + i, parent, ints );
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