/*  XQEngine Copyright (C) 2002-2005 Fatdog Software Inc. [howardk@fatdog.com]
 *	--------------------------------------------------------------------------
 *  This file is part of XQEngine and is distributed under the terms of the
 *  GNU General Public License. See additional info at the end of this file. */

package com.fatdog.xmlEngine.words.dictionaries;

	/**
	 * A dictionary used to store <code>prefix</code> or <code>localpart</code> QName components.
	 * 
	 * <P>At present this could be just a straight {@link Dictionary}. In past
	 * (and possibly in future) a <code>TextDictionary</code> subclass can provide
	 * slightly different behaviour for "real" word content, such as ignoring
	 * case on lookup (a desirable option for full-text).
	 * 
	 * <P><B>NOTA</B>: Underlying dictionary implementation is still evolving.
	 * Eg, at present separate dictionaries are dedicated to prefixes and
	 * localparts, for both elements and attributes. I will review whether these
	 * continue to require separate structures or whether they can and/or should be consolidated.
	 * 
	 * <P>As well, the underlying {@link WordListHashtable} implementation seems reasonably
	 * efficient as a storage device, but does not allow alphabetic storage and retrieval.
	 * I may swap this out.
	 * 
	 * @author Howard Katz, howardk@fatdog.com
	 * @version 0.69
	 */

import com.fatdog.xmlEngine.IntList;

public class TextDictionary extends Dictionary
{
    public TextDictionary( int newEntryAllocs )
    //----------------------------------------
    {
        super( -9999, newEntryAllocs );
    }
    
	public int addWordEntryWithXRef( char[] word, int start, int end, IntList args )
	//----------------------------------------------------
	{
		for( int i = start; i < end; i++ )
		{
			char ch = word[ i ];
			word[ i ] = Character.toLowerCase( ch );
		}
		return super.addWordEntryWithXRef( word, start, end, args );
	}
	
	public int addWordEntry( char[] word, int start, int end )
	//----------------------------------------------------
	{
		for( int i = start; i < end; i++ )
		{
			char ch = word[ i ];
			word[ i ] = Character.toLowerCase( ch );
		}
		return super.addWordEntry( word, start, end );
	}
	
	public int keyFromWord( char[] charWord )
	//---------------------------------------
	{
		for( int i = 0; i < charWord.length; i++ )
			charWord[ i ] = Character.toLowerCase( charWord[ i ]);
			
		String word = new String( charWord );
		return m_table.containsKey( word ) ?
        
				m_table.getKey( word ) :
				-1;
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