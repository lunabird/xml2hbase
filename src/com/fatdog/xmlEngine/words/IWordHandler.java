/*  XQEngine Copyright (C) 2002-2005 Fatdog Software Inc. [howardk@fatdog.com]
 *	--------------------------------------------------------------------------
 *  This file is part of XQEngine and is distributed under the terms of the
 *  GNU General Public License. See additional info at the end of this file. */
 
package com.fatdog.xmlEngine.words;

import com.fatdog.xmlEngine.IntList;

	/**
	 * Provides a callback for clients who need a word-break capability.
	 * 
	 * <P>To use this class, register the object implementing its <code>newWord()</code> callback with
	 * a {@link WordBreaker} or an object derived from it. When <code>WordBreaker.characters()</code>
	 * is invoked, <code>newWord()</code> is called every time a new word is parsed.
	 * 
	 * <P>You can initialize and pass in an {@link com.fatdog.xmlEngine.IntList} to be populated
	 * by <code>newWord()</code> with whatever information is useful to you.
	 * 
	 * @author Howard Katz, howardk@fatdog.com
	 * @version 0.69
	 * 
	 * @see WordBreaker
	 */

public interface IWordHandler
{
	
    /**
     * This method will be called whenever {@link WordBreaker#characters(char[], int, int, int, IntList)} encounters
     * a new word.
	 * 
	 * @param word A char[] array holding the word, possibly larger than the word itself
	 * @param wordStart Starting offset of the word in the array
	 * @param wordEnd Offset of the first character past the end of the word
	 * @param parent Document id index of the node in which the word is found
	 * @param ints An optional {@link com.fatdog.xmlEngine.IntList} you can pass indirectly to the routine
	 * via <code>WordBreaker.characters()</code>
	 * @see com.fatdog.xmlEngine.FandO#contains_word(SimpleNode,SimpleNode)
	 * @see com.fatdog.xmlEngine.IndexManager
	 * 
	 */
    
	public void newWord( char[] word, int wordStart, int wordEnd, int parent, IntList ints );
    //--------------------------------------------------------------------------------------
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