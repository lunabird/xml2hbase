/*  XQEngine Copyright (C) 2002-2005 Fatdog Software Inc. [howardk@fatdog.com]
 *	--------------------------------------------------------------------------
 *  This file is part of XQEngine and is distributed under the terms of the
 *  GNU General Public License. See additional info at the end of this file. */

package com.fatdog.xmlEngine.junitTest;

import com.fatdog.xmlEngine.IProtocolHandler;
import java.util.Hashtable;

	/**
	 * A test protocol handler that's used by the JUnit test suite {@link ProtocolHandlerTest}.
	 * 
	 * <P>The handler recognizes custom addresses beginning with the scheme, "XYZ::". When
	 * {@link com.fatdog.xmlEngine.XQEngine#setDocument(String)} or the function doc(String)
	 * encounter an address prefixed by this scheme, the query engine will ask the protocol handler
	 * to provide the XML content corresponding to that address.
	 * 
	 * @author Howard Katz, howardk@fatdog.com
	 * @version 0.61
	 */
	
public class XYZ_ProtocolHandler implements IProtocolHandler
{
    Hashtable m_contentHash = new Hashtable();
    
    // a way of setting various content keyed to a particular url for test purposes
    
    public void setContent( String addr, String content )
    //--------------------------------------------------
    {
    	m_contentHash.put( addr, content );
    }
    
    // our single IProtocolHandler callback. the handler needs to return 
    // apt content on request by the query engine via this method
    
    public String content( String addr )
    //----------------------------------
    {
    	return (String) m_contentHash.get( addr );
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