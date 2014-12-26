/*  XQEngine Copyright (C) 2002-2005 Fatdog Software Inc. [howardk@fatdog.com]
 *	--------------------------------------------------------------------------
 *  This file is part of XQEngine and is distributed under the terms of the
 *  GNU General Public License. See additional info at the end of this file. */

package com.fatdog.xmlEngine;

	/**
	 * Provides a callback for implementers of custom protocols.
	 * <P>Clients wanting to address XML resources using a custom-protocol addressing 
	 * scheme need to implement this interface's {@link #content(String)} method.
	 * 
	 * @author Howard Katz, howardk@fatdog.com
	 * @version 0.69
	 */
	
public interface IProtocolHandler
{
	/**
	 * Return the XML contents of the resource addressed by this custom protocol.
	 * 
	 * <P>This callback method will be called when <B>XQEngine</B> determines that an address
	 * you've passed to {@link XQEngine#setDocument(String)} (or indirectly through 
	 * the function fn:doc(String) is the address of a custom-protocol that you've previously
	 * registered with the query engine.
	 * 
	 * @param url The address of the resource
	 * @return The serialized XML content of the resource at the address
	 * @see XQEngine#registerProtocolHandler(String, IProtocolHandler)
	 */
	
    public String content( String url );
    //----------------------------------
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