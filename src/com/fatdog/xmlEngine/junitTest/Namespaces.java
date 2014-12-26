/*  XQEngine Copyright (C) 2002-2005 Fatdog Software Inc. [howardk@fatdog.com]
 *	--------------------------------------------------------------------------
 *  This file is part of XQEngine and is distributed under the terms of the
 *  GNU General Public License. See additional info at the end of this file. */

package com.fatdog.xmlEngine.junitTest;

import com.fatdog.xmlEngine.*;
import com.fatdog.xmlEngine.exceptions.*;
import java.io.FileNotFoundException;
import javax.xml.parsers.*;
import junit.framework.*;
import junit.extensions.TestSetup;
import org.xml.sax.XMLReader;

	/**
	 * Tests a variety of xpaths using namespace prefixes (setUseLexicalPrefixes(false))
	 * 
	 * @author Howard Katz, howardk@fatdog.com
	 * @version 0.69
	 */
	
public class Namespaces extends TestCase
{  
	void assertEqualIntSequences( String msg, int[] correctResponse, int[] actualResponse )
	//-------------------------------------------------------------------------------------
	{
		assertEquals( "Number of entries in resultList -- ", correctResponse.length/2, actualResponse.length/2 );
		for( int i = 0; i < actualResponse.length; i++ )
			assertEquals( msg, correctResponse[i], actualResponse[i] );
	} 
	
	void assertEqualNodeSequences( String nodeMsg, int[] correctResponse, int[] actualResponse )
	//------------------------------------------------------------------------------------------
	{
		assertEquals( "Number of nodes in resultSet", correctResponse.length/2, actualResponse.length/2 );
		for( int i = 0; i < actualResponse.length; i++ )
			assertEquals( nodeMsg, correctResponse[i], actualResponse[i] );
	} 
	
	public void test_ns_1() throws InvalidQueryException
	//--------------------------------------------------
	{
		// NOTA no namespace decl -- not required
 		ResultList hits = m_engine.setQuery( "//channel" );	
		assertEquals( 1, hits.getNumValidItems() );
		
		int[] correct = { 0, 2 };	
		assertEqualNodeSequences( "", correct, hits.getDocNotatedNodeList() );
	}
	
	public void test_ns_2() throws InvalidQueryException
	//--------------------------------------------------
	{
		// NOTA no namespace decl -- not required
 		ResultList hits = m_engine.setQuery( "/*:RDF/channel" );	
		assertEquals( 1, hits.getNumValidItems() );
		
		int[] correct = { 0, 2 };	
		assertEqualNodeSequences( "", correct, hits.getDocNotatedNodeList() );
	}
	
	public void test_ns_3() throws InvalidQueryException
	//--------------------------------------------------
	{
		// namespace decl required
		
		try
		{
			ResultList hits = m_engine.setQuery( "rdf:RDF/channel" );	
			fail( "Namespaces: No namespace decl for rdf:" );
		}
		catch( com.fatdog.xmlEngine.exceptions.CategorizedInvalidQueryException e )
		{
			// we want and expect to be here
		}
	}
	
	public void test_ns_4() throws InvalidQueryException
	//--------------------------------------------------
	{
		String nsDecl = "declare namespace rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#';\n";
		
 		ResultList hits = m_engine.setQuery( nsDecl + "rdf:RDF/channel" );	
		assertEquals( 1, hits.getNumValidItems() );
		
		int[] correct = { 0, 2 };	
		assertEqualNodeSequences( "", correct, hits.getDocNotatedNodeList() );
	}
	
	public void test_ns_5() throws InvalidQueryException
	//--------------------------------------------------
	{
 		ResultList hits = m_engine.setQuery( "//*" );	
		assertEquals( 11, hits.getNumValidItems() );
	}

	public void test_ns_6() throws InvalidQueryException
	//--------------------------------------------------
	{
		String nsDecl = "declare namespace default = 'http://purl.org/rss/1.0/';\n";
		
 		ResultList hits = m_engine.setQuery( nsDecl + "//default:*" );	
		assertEquals( 4, hits.getNumValidItems() );
		
		int[] correct = { 0, 5, 0, 7, 0, 10, 0, 13 };	
		assertEqualNodeSequences( "", correct, hits.getDocNotatedNodeList() );
	}

	public void test_ns_7() throws InvalidQueryException
	//--------------------------------------------------
	{
		String dc = "declare namespace dc = 'http://purl.org/dc/elements/1.1/';\n";
		
 		ResultList hits = m_engine.setQuery( dc + "//dc:*" );	
		
		int[] correct = { 0, 16 };	
		assertEqualNodeSequences( "", correct, hits.getDocNotatedNodeList() );
	}
	
	public void test_ns_8() throws InvalidQueryException
	//--------------------------------------------------
	{
		String dc = "declare namespace dc = 'http://purl.org/dc/elements/1.1/';\n";
		
 		ResultList hits = m_engine.setQuery( dc + "//dc:*" );	
		assertEquals( 1, hits.getNumValidItems() );
	}
	
	public void test_ns_9() throws InvalidQueryException
	//--------------------------------------------------
	{
		String dc = "declare namespace dc = 'http://purl.org/dc/elements/1.1/';\n";
		
 		ResultList hits = m_engine.setQuery( dc + "//@*" );	
		assertEquals( 4, hits.getNumValidItems() );
			
		int[] correct = { 0, 3, 0, 17, 0, 25, 0, 28 };	
		assertEqualNodeSequences( "", correct, hits.getDocNotatedNodeList() );
	}
	
	public void test_ns_10() throws InvalidQueryException
	//---------------------------------------------------
	{
		String dc = "declare namespace dc = 'http://purl.org/dc/elements/1.1/';\n";
		
 		ResultList hits = m_engine.setQuery( dc + "//@dc:*" );	
		assertEquals( 1, hits.getNumValidItems() );
		
		int[] correct = { 0, 17 };	
		assertEqualNodeSequences( "", correct, hits.getDocNotatedNodeList() );
	}
	
	static void installSunXMLReader() throws Exception
	//------------------------------------------------
	{
		SAXParserFactory spf = SAXParserFactory.newInstance();
		try
		{
			SAXParser parser = spf.newSAXParser();
			XMLReader reader = parser.getXMLReader();

			m_engine.setXMLReader( reader );
		}
		catch( Exception e )    { throw e; }
	}  
	
	static XQEngine m_engine;
	
	public static Test suite()
	//------------------------
	{				
		TestSuite suite = new TestSuite( Namespaces.class );
		
		TestSetup setup = new TestSetup( suite ) {
			protected void setUp() throws Exception 
			{
				System.out.println( "\nJUnit test suite: Namespaces" );
				
				m_engine = new XQEngine();

				m_engine.setMinIndexableWordLength( 0 );			
				m_engine.setDebugOutputToConsole( false );
				
				m_engine.setUseLexicalPrefixes( false );

				installSunXMLReader();   
                
				try 
				{
					m_engine.setDocument( "home_1.rss" );      
				}
				catch( FileNotFoundException e ) { throw e; }
				catch( CantParseDocumentException e ) { throw e; }
				catch( MissingOrInvalidSaxParserException e ) { throw e; }				
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