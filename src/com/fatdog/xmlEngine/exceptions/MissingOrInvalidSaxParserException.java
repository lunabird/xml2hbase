/*  XQEngine Copyright (C) 2002-2005 Fatdog Software Inc. [howardk@fatdog.com]
 *	--------------------------------------------------------------------------
 *  This file is part of XQEngine and is distributed under the terms of the
 *  GNU General Public License. See additional info at the end of this file. */
 
package com.fatdog.xmlEngine.exceptions;

	/**
	 * The user hasn't set a valid SAX parser using 
	 * {@link com.fatdog.xmlEngine.XQEngine#setXMLReader(XMLReader)}.
	 * 
	 * @author Howard Katz, howardk@fatdog.com
	 * @version 0.69
	 */

public class MissingOrInvalidSaxParserException extends Exception
{
    public MissingOrInvalidSaxParserException( String msg )
    //-----------------------------------------------------
    {
        super( msg );
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