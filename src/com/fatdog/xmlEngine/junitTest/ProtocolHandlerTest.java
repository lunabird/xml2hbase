/*  XQEngine Copyright (C) 2002-2005 Fatdog Software Inc. [howardk@fatdog.com]
 *	--------------------------------------------------------------------------
 *  This file is part of XQEngine and is distributed under the terms of the
 *  GNU General Public License. See additional info at the end of this file. */

package com.fatdog.xmlEngine.junitTest;

import com.fatdog.xmlEngine.*;
import com.fatdog.xmlEngine.exceptions.*;
import java.io.*;
import javax.xml.parsers.*;
import junit.framework.*;
import junit.extensions.TestSetup;

import org.xml.sax.XMLReader;

	/**
	 * A Junit test case that tests XQEngine custom-protocol handling.
	 * 
	 * This test instantiates, registers, and then indirectly requests XML content from 
	 * a custom protocol handler, {@link XYZ_ProtocolHandler}.
	 * 
	 * @author Howard Katz, howardk@fatdog.com
	 * @version 0.61
	 */

public class ProtocolHandlerTest extends TestCase
{
	/**
	 * Index two "XYZ::"-scheme based documents and query their content. 
	 * 
	 * @throws InvalidQueryException
	 */
	
	public void test_goodHandler_1() throws InvalidQueryException
	//------------------------------------------------------------
	{
		int docId;
		
		try {
			docId = m_engine.setDocument( "XYZ::someAddr_1" );
			docId = m_engine.setDocument( "XYZ::someAddr_2" );
		}
		catch( CantParseDocumentException cpde ) { }
		catch( MissingOrInvalidSaxParserException e ) { }	
		catch( FileNotFoundException fnfe ) {  }	
					
		ResultList hits = m_engine.setQuery( "/*" );      
		assertEquals( "//* --", 2, hits.getNumValidItems() );
		
		int[] expectedNodes = { 0, 0, 1, 0 };
		assertEqualNodeSequences( "root nodes on two XYZ:: addresses in setDocument() --",
												expectedNodes, hits.getDocNotatedNodeList() );
	}
	
	/**
	 * Ask the protocol handler indirectly to return content from one document on a query.
	 * 
	 * @throws InvalidQueryException
	 */
	
	public void test_goodHandler_2() throws InvalidQueryException
	//------------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "doc( 'XYZ::someAddr_1' )/xyz" );      
		assertEquals( "doc( 'XYZ::someAddress_1' )/xyz --", 1, hits.getNumValidItems() );
		
		int[] expectedNodes = { 0, 0 };
		assertEqualNodeSequences( "nodeId of doc( ... )/xyz --", expectedNodes, hits.getDocNotatedNodeList() );
	}
	
	/**
	 * Ask the protocol handler to return content from a second document on a query.
	 * 
	 * @throws InvalidQueryException
	 */
	
	public void test_goodHandler_3() throws InvalidQueryException
	//-----------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "doc( 'XYZ::someAddr_4' )/xyz" );      
		assertEquals( "doc( 'XYZ::someAddress_4' )/xyz --", 1, hits.getNumValidItems() );
		
		int[] expectedNodes = { 2, 0 };
		assertEqualNodeSequences( "nodeId of doc( ... )/xyz --", expectedNodes, hits.getDocNotatedNodeList() );
	}
	
	/**
	 * Ask the protocol handler to return content from a document that's never been indexed.
	 * 
	 * Failure expected.
	 */
	
	public void test_badHandler_1()
	//-----------------------------
	{
		try {
			ResultList hits = m_engine.setQuery( "doc( 'XYZ::someAddr_3' )/xyz" );      
			fail( "\nExpected InvalidQueryException because handler cannot provide document" );
		}
		catch( InvalidQueryException iqe ) {}
	}
	
	/**
	 * Attempt to index a document the protocol handler doesn't recognize.
	 * 
	 * Failure expected.
	 */
	
	public void test_badHandler_2()
	//-----------------------------
	{
		try {
			int docId = m_engine.setDocument( "XYZ::someAddr_3" );
			fail( "\nXYZ handler recognized invalid address" );
		}
		catch( CantParseDocumentException cpde ) { }
		catch( MissingOrInvalidSaxParserException e ) { }	
		catch( FileNotFoundException fnfe ) {  }	
	}
	
	// query engine doesn't recognize protocol in doc() call -- return null results 
	
	public void test_badHandler_3() throws InvalidQueryException
	//-----------------------------
	{
		ResultList hits = m_engine.setQuery( "doc( 'ABC::a bad protocol' )/xyz" ); 		
		assertEquals( 0, hits.getNumTotalItems() );	
	}
	
	
	void assertEqualNodeSequences( String nodeMsg, int[] correctResponse, int[] actualResponse )
	//------------------------------------------------------------------------------------------
	{
		assertEquals( "Number of nodes in resultSet", correctResponse.length/2, actualResponse.length/2 );
		for( int i = 0; i < actualResponse.length; i++ )
			assertEquals( nodeMsg, correctResponse[i], actualResponse[i] );
	} 
	
	static void installSunXMLReader() throws Exception
	//--------------------------------
	{
		SAXParserFactory spf = SAXParserFactory.newInstance();
		try
		{
			SAXParser parser = spf.newSAXParser();
			XMLReader reader = parser.getXMLReader();

			m_engine.setXMLReader( reader );
	        m_engine.setUseLexicalPrefixes( true );
		}
		catch( Exception e )    { throw e; }
	}  
	
	private static XQEngine m_engine;
	
	public static Test suite()
	//------------------------
	{				
		TestSuite suite = new TestSuite( ProtocolHandlerTest.class );
		
		TestSetup setup = new TestSetup( suite ) {
			protected void setUp() throws Exception 
			{
				System.out.println( "\nJUnit test suite: ProtocolHandlerTest" );
				
				m_engine = new XQEngine();

				m_engine.setMinIndexableWordLength( 0 );				
				m_engine.setDebugOutputToConsole( false );

				installSunXMLReader();   
                
				XYZ_ProtocolHandler xyzTestHandler = new XYZ_ProtocolHandler();
				
				// set up XYZ test handler with some dummy data
				// for various setDocument() and doc() calls
				
				xyzTestHandler.setContent( "XYZ::someAddr_1", "<xyz>some xyz content_1</xyz>" );   
				xyzTestHandler.setContent( "XYZ::someAddr_2", "<xyz>some xyz content_2</xyz>" );	
				xyzTestHandler.setContent( "XYZ::someAddr_4", "<xyz>some xyz content_4</xyz>" );	
				
				// register the protocol handler and its "scheme" with the engine
				// the scheme can be any combination of letters and/or special punctuation
				
				m_engine.registerProtocolHandler( "XYZ::", xyzTestHandler );
			
			}
		};
		
		return setup;
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