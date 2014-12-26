/*  XQEngine Copyright (C) 2002-2005 Fatdog Software Inc. [howardk@fatdog.com]
 *	--------------------------------------------------------------------------
 *  This file is part of XQEngine and is distributed under the terms of the
 *  GNU General Public License. See additional info at the end of this file. */

package com.fatdog.xmlEngine.junitTest;

import com.fatdog.xmlEngine.*;
import com.fatdog.xmlEngine.exceptions.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import javax.xml.parsers.*;
import junit.framework.*;
import junit.extensions.TestSetup;
import org.xml.sax.XMLReader;

	/**
	 * Tests a variety of "official" XQuery Uses Cases.
	 * 
	 * <P>See the working draft, <a href="http://www.w3.org/TR/xquery-use-cases/">XML Query Use Cases</a>.
	 * 
	 * @author Howard Katz, howardk@fatdog.com
	 * @version 0.61
	 */

public class XQueryUseCases extends TestCase
{
	public void test_XMP_1() throws InvalidQueryException
	//----------------------
	{
		String content = getFileContents( "com/fatdog/xmlEngine/junitTest/XMP_1.txt" );
		ResultList hits = m_engine.setQuery( content );
		
		String actual = hits.emitXml();	
		String expect = "<bib><book year=\"1994\"><title>TCP/IP Illustrated</title></book><book year=\"1992\"><title>Advanced Programming in the Unix environment</title></book></bib>";

		assertEquals( expect, actual );
	}
	
	public void test_XMP_2() throws InvalidQueryException
	//----------------------
	{
		String content = getFileContents( "com/fatdog/xmlEngine/junitTest/XMP_2.txt" );
		ResultList hits = m_engine.setQuery( content );
		
		String expect = "<results><result><title>TCP/IP Illustrated</title>\r\n            <author><last>Stevens</last><first>W.</first></author></result><result><title>Advanced Programming in the Unix environment</title>\r\n            <author><last>Stevens</last><first>W.</first></author></result><result><title>Data on the Web</title>\r\n            <author><last>Abiteboul</last><first>Serge</first></author></result><result><title>Data on the Web</title>\r\n            <author><last>Buneman</last><first>Peter</first></author></result><result><title>Data on the Web</title>\r\n            <author><last>Suciu</last><first>Dan</first></author></result></results>";
		String actual = hits.emitXml();
		
		assertEquals( expect, actual );
	}
	
	public void test_XMP_3() throws InvalidQueryException
	//----------------------
	{
		String content = getFileContents( "com/fatdog/xmlEngine/junitTest/XMP_3.txt" );
		ResultList hits = m_engine.setQuery( content );
		
		String expect = "<results><result><title>TCP/IP Illustrated</title>\r\n            <author><last>Stevens</last><first>W.</first></author></result><result><title>Advanced Programming in the Unix environment</title>\r\n            <author><last>Stevens</last><first>W.</first></author></result><result><title>Data on the Web</title>\r\n            <author><last>Abiteboul</last><first>Serge</first></author><author><last>Buneman</last><first>Peter</first></author><author><last>Suciu</last><first>Dan</first></author></result><result><title>The Economics of Technology and Content for Digital TV</title>\r\n            </result></results>";
		String actual = hits.emitXml();
		
		assertEquals( expect, actual );
	}
	
	public void test_XMP_5() throws InvalidQueryException
	//----------------------
	{
		String content = getFileContents( "com/fatdog/xmlEngine/junitTest/XMP_5.txt" );
		ResultList hits = m_engine.setQuery( content );

		String expect = "<books-with-prices><book-with-prices><title>TCP/IP Illustrated</title>\r\n            <price-bstore2>65.95</price-bstore2>\r\n            <price-bstore1> 65.95</price-bstore1></book-with-prices><book-with-prices><title>Advanced Programming in the Unix environment</title>\r\n            <price-bstore2>65.95</price-bstore2>\r\n            <price-bstore1>65.95</price-bstore1></book-with-prices><book-with-prices><title>Data on the Web</title>\r\n            <price-bstore2>34.95</price-bstore2>\r\n            <price-bstore1>39.95</price-bstore1></book-with-prices></books-with-prices>";
		String actual = hits.emitXml();
		
		assertEquals( expect, actual );
	}
	
	public void test_XMP_7() throws InvalidQueryException
	//------------------------------------------------------------------
	{
		String content = getFileContents( "com/fatdog/xmlEngine/junitTest/XMP_7.txt" );
		ResultList hits = m_engine.setQuery( content );
		
		String expect = "<bib><book year=\"1992\">\r\n            <title>Advanced Programming in the Unix environment</title></book><book year=\"1994\">\r\n            <title>TCP/IP Illustrated</title></book></bib>";
		String actual = hits.emitXml();
		
		assertEquals( expect, actual );
	}
	
	public void test_XMP_11() throws InvalidQueryException
	//----------------------
	{
		String content = getFileContents( "com/fatdog/xmlEngine/junitTest/XMP_11.txt" );
		ResultList hits = m_engine.setQuery( content.toString() );
		
		String actual = hits.emitXml();
		String expect = "<bib><book><title>TCP/IP Illustrated</title>\r\n                <author><last>Stevens</last><first>W.</first></author></book><book><title>Advanced Programming in the Unix environment</title>\r\n                <author><last>Stevens</last><first>W.</first></author></book><book><title>Data on the Web</title>\r\n                <author><last>Abiteboul</last><first>Serge</first></author><author><last>Buneman</last><first>Peter</first></author><author><last>Suciu</last><first>Dan</first></author></book>\r\n<reference><title>The Economics of Technology and Content for Digital TV</title>\r\n            <affiliation>CITI</affiliation></reference></bib>";

		assertEquals( expect, actual );
	}

  	public void test_SEQ_1() throws InvalidQueryException
  	//---------------------------------------------------
 	{
	  String content = getFileContents( "com/fatdog/xmlEngine/junitTest/SEQ_1.txt" );
									
	  ResultList hits = m_engine.setQuery( content );
	  
	  String actual = hits.emitXml();	
	  String expect = "<instrument>electrocautery</instrument>";

	  assertEquals( expect, actual );
  	}

  	public void test_SEQ_2() throws InvalidQueryException
  	//---------------------------------------------------
  	{
	  String content = getFileContents( "com/fatdog/xmlEngine/junitTest/SEQ_2.txt" );
	  ResultList hits = m_engine.setQuery( content );
		
	  String actual = hits.emitXml();	
	  String expect = "<instrument>using electrocautery.</instrument><instrument>electrocautery</instrument>";

	  assertEquals( expect, actual );
  	}
  
	public void test_SEQ_4() throws InvalidQueryException
	//---------------------------------------------------
	{
	  String content = getFileContents( "com/fatdog/xmlEngine/junitTest/SEQ_4.txt" );
	  ResultList hits = m_engine.setQuery( content );

	  assertEquals( 0, hits.getNumValidItems() );
	}
	
	public void test_TREE_2() throws InvalidQueryException
	//----------------------------------------------------
	{
		String content = getFileContents( "com/fatdog/xmlEngine/junitTest/TREE_2.txt" );		
		ResultList hits = m_engine.setQuery( content );
		
		String actual = hits.emitXml();
		String expect = "<figlist><figure height=\"400\" width=\"400\">\r\n            <title>Traditional client/server architecture</title></figure><figure height=\"200\" width=\"500\">\r\n            <title>Graph representations of structures</title></figure><figure height=\"250\" width=\"400\">\r\n            <title>Examples of Relations</title></figure></figlist>";
		
		assertEquals( expect, actual );
	}
	
	public void test_TREE_3() throws InvalidQueryException
	//----------------------------------------------------
	{
		String content = getFileContents( "com/fatdog/xmlEngine/junitTest/TREE_3.txt" );
		ResultList hits = m_engine.setQuery( content );
		
		String actual = hits.emitXml();
		String expect = "<section_count>7</section_count><figure_count>3</figure_count>";
		
		assertEquals( expect, actual );
	}
	
	public void test_TREE_4() throws InvalidQueryException
	//----------------------------------------------------
	{
		String content = getFileContents( "com/fatdog/xmlEngine/junitTest/TREE_4.txt" );
		ResultList hits = m_engine.setQuery( content );
		
		String actual = hits.emitXml();
		String expect = "<top_section_count>2</top_section_count>";
		
		assertEquals( expect, actual );
	}
	
	public void test_TREE_5() throws InvalidQueryException
	//----------------------------------------------------
	{
		String content = getFileContents( "com/fatdog/xmlEngine/junitTest/TREE_5.txt" );
		ResultList hits = m_engine.setQuery( content );
		
		String actual = hits.emitXml();
		String expect = "<section_list><section title=\"Introduction\" figcount=\"0\"/><section title=\"Audience\" figcount=\"0\"/><section title=\"Web Data and the Two Cultures\" figcount=\"1\"/><section title=\"A Syntax For Data\" figcount=\"1\"/><section title=\"Base Types\" figcount=\"0\"/><section title=\"Representing Relational Databases\" figcount=\"1\"/><section title=\"Representing Object Databases\" figcount=\"0\"/></section_list>";
		
		assertEquals( expect, actual );	
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
	
	
	String getFileContents( String fileName ) throws InvalidQueryException
	//---------------------------------------
	{		
		char[] content;
		
		URL url = getClass().getClassLoader().getResource( fileName );

		try {
		
			URLConnection connection = url.openConnection();
			
			int len = connection.getContentLength();
			
			content = new char[ len ];
			
			BufferedReader r = new BufferedReader( new InputStreamReader( connection.getInputStream() ));
			
			r.read( content, 0, len );	
        
			connection.getInputStream().close();
	        
		}
		catch( IOException ioe )
		{
			throw new InvalidQueryException(
					"\njunitTest.XQueryUseCases: couldn't read query file" );
		}
		
		return new String( content );	
		
		/* if it were a regular file (not in a jar), we'd do this:
		
		File file = new File( fileName );
		
		char[] content = new char[ (int)file.length() ];
		
		try {						
			FileReader fr = new FileReader( fileName );		
								
			fr.read( content );
			fr.close();														
		} 
		catch (IOException e )
		{
			System.out.println( "Oops, I/O error on file read!" );
		}

		return new String( content );
		*/
	}
	
	private static XQEngine m_engine;
	
	public static Test suite()
	//------------------------
	{		
		TestSuite suite = new TestSuite( XQueryUseCases.class );
		
		TestSetup setup = new TestSetup( suite ) {
			protected void setUp() throws Exception 
			{
				System.out.println( "\nJUnit test suite: XQueryUseCases" );
				
				m_engine = new XQEngine();
				
				m_engine.setMinIndexableWordLength( 0 );				
				m_engine.setDebugOutputToConsole( false );

				installSunXMLReader();   
                
				try {	
					String testFile_1 = "bib.xml";		// XMP
					String testFile_2 = "reviews.xml";	// XMP
					String testFile_3 = "report1.xml";	// SEQ
					String testFile_4 = "book.xml";		// TREE
					
					m_engine.setDocument( testFile_1 );
					m_engine.setDocument( testFile_2 );
					m_engine.setDocument( testFile_3 );
					m_engine.setDocument( testFile_4 );
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