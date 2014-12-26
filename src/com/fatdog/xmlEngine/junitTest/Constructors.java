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
	 * JUnit test suite for attribute and element constructors.
	 * 
	 * @author Howard Katz, howardk@fatdog.com
	 * @version 0.61
	 */

public class Constructors extends TestCase
{
	void assertEqualIntSequences( String msg, int[] expectedResponse, int[] actualResponse )
	//-------------------------------------------------------------------------------------
	{
		assertEquals( "Number of entries in resultList -- ", expectedResponse.length/2, actualResponse.length/2 );
		for( int i = 0; i < actualResponse.length; i++ )
			assertEquals( msg, expectedResponse[i], actualResponse[i] );
	} 
	
	// NOTA: boundary whitespace default is 'declare xmlspace strip'
	
	public void test_boundaryWhitespace_0() throws InvalidQueryException
	//------------------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "<a>  </a>" );
		assertEquals( "\n<a>  </a> --", "<a>  </a>", hits.emitXml() );
	}
	
	public void test_boundaryWhitespace_1() throws InvalidQueryException
	//------------------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "<a>   {42}</a>" );
		String actual = hits.emitXml();
		assertEquals( "\n<a>42</a> --", "<a>42</a>", actual );
	}
	
	public void test_boundaryWhitespace_2() throws InvalidQueryException
	//------------------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "<a>   {42}   </a>" );
		assertEquals( "\n<a>42</a> --", "<a>42</a>", hits.emitXml() );
	}
	
	public void test_boundaryWhitespace_3() throws InvalidQueryException
	//------------------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "<a> {//editor/last/text()} </a>" );
		assertEquals( "\n<a>Gerbarg</a> --", "<a>Gerbarg</a>", hits.emitXml() );
	}
	
	public void test_boundaryWhitespace_4() throws InvalidQueryException
	//------------------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "declare xmlspace preserve; <a>  {42}  </a>" );
		assertEquals( "\ndeclare xmlspace preserve; <a>  42  </a> --", "<a>  42  </a>", hits.emitXml() );
	}
	
	public void test_boundaryWhitespace_5() throws InvalidQueryException
	//------------------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "declare xmlspace preserve; <a>  {42}  </a>" );
		assertEquals( "\ndeclare xmlspace preserve; <a>  42  </a> --", "<a>  42  </a>", hits.emitXml() );
	}
	
	public void test_boundaryWhitespace_6() throws InvalidQueryException
	//------------------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "declare xmlspace preserve; <a>  {//editor/last/text()}  </a>" );
		assertEquals( "\ndeclare xmlspace preserve; <a>  {//editor/last/text()}  </a> --", "<a>  Gerbarg  </a>", hits.emitXml() );
	}
	
	public void test_boundaryWhitespace_7() throws InvalidQueryException
	//------------------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "<a>  <b>  {12}  </b>  </a>" );
		
		String expected = "<a><b>12</b></a>";
		String actual 	= hits.emitXml();
		
		assertEquals(  "<a>  <b>  {12}  </b>  </a> --", expected, actual );
	}
	
	public void test_boundaryWhitespace_8() throws InvalidQueryException
	//------------------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "declare xmlspace preserve; <a>  <b>  {12}  </b>  </a>" );
		
		String expected = "<a>  <b>  12  </b>  </a>";
		String actual 	= hits.emitXml();
				
		assertEquals( "\ndeclare xmlspace preserve; <a>  <b>  {12}  </b>  </a> --", expected, actual );
	}
		
	public void test_ctor_1() throws InvalidQueryException
	//----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "<a/>" );
		assertEquals( "\n<a/> --", "<a/>", hits.emitXml() );
	}
	
	public void test_ctor_2() throws InvalidQueryException
	//----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "<r>{<a>{ '123' }</a>}</r>" );
		String xml = hits.emitXml();
		assertEquals(  "<r><a>123</a></r>", xml );
	}
		
	public void test_ctor_3() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "<r>{<a>{ 123, <b/> }</a>}</r>" );
		assertEquals( "<r><a>123<b/></a></r>", hits.emitXml() );
	}
	
	// same as above w/ 4 instances of terminal whitespace
	public void test_ctor_4() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "<r>     {<a>     { 123, <b/> }    </a>}     </r>" );
		
		String expected = "<r><a>123<b/></a></r>";
		String actual 	= hits.emitXml();
		
		assertEquals( expected, hits.emitXml() );
	}

	public void test_ctor_5() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "<r>{'123'}{'456'}</r>" );
		assertEquals( "<r>123456</r>", hits.emitXml() );
	}
	
	// emitXml() emits a canonical <a/> form for an empty <a></a> input
	
	public void test_ctor_6() throws InvalidQueryException
	//----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "<a></a>" );
		assertEquals( "\n<a></a> --", "<a/>", hits.emitXml() );
	}

	public void test_ctor_7() throws InvalidQueryException
	//----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "<a>{1,2}</a>" );
		assertEquals( "\n<a>{1,2}</a> --", "<a>1 2</a>", hits.emitXml() );
	}
	
	public void test_ctor_8() throws InvalidQueryException
	//----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "<a>{true()}</a>" );
		assertEquals( "\n<a>{true()}</a> --", "<a>true</a>", hits.emitXml() );
	}
	
	public void test_ctor_9() throws InvalidQueryException
	//----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "<a>{false()}</a>" );
		assertEquals( "\n<a>{false()}</a> --", "<a>false</a>", hits.emitXml() );
	}
	
	// no internal space separator
	public void test_ctor_10() throws InvalidQueryException
	//----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "<a>{12,//editor/last}</a>" );
		assertEquals( "<a>{12,//editor/last}</a> --", "<a>12<last>Gerbarg</last></a>", hits.emitXml() );
	}
	
	// internal space separator for enclosed expr
	public void test_ctor_11() throws InvalidQueryException
	//----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "<a>{12,13,//editor/last}</a>" );
		assertEquals( "<a>{12,13//editor/last}</a> --", "<a>12 13<last>Gerbarg</last></a>", hits.emitXml() );
	}
	
	public void test_ctor_12() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "<a>{for $i in //book return <b/>}</a>" );
		assertEquals( "<a><b/><b/><b/><b/></a>", hits.emitXml() );
	}
	
	public void test_ctor_13() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "<a>{for $i in //book return <TIT>{$i/title/text()}</TIT> }</a>" );

		String expected = "<a><TIT>TCP/IP Illustrated</TIT><TIT>Advanced Programming in the Unix environment</TIT><TIT>Data on the Web</TIT><TIT>The Economics of Technology and Content for Digital TV</TIT></a>";
		String actual	= hits.emitXml();
		
		assertEquals( expected, actual );
	}
	
	public void test_ctor_14() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "<a>{for $i in //book return <ED>{$i/editor//text()}</ED> }</a>" );

		String expected = "<a><ED></ED><ED></ED><ED></ED><ED>\n\t\tGerbargDarcy\n\t\tCITI\n\t\t</ED></a>";
		String actual	= hits.emitXml();
		
		assertEquals( expected, actual );
	}
	
	
	public void test_ctor_15() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "<attributesOnly>{ doc( \"bib.xml\" )//@* }</attributesOnly>" );

		String expected = "<attributesOnly year=\"1994\" year=\"1992\" year=\"2000\" year=\"1999\"></attributesOnly>";
		String actual	= hits.emitXml();
		
		assertEquals( expected, actual );
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
		TestSuite suite = new TestSuite( Constructors.class );
		
		TestSetup setup = new TestSetup( suite ) {
			protected void setUp() throws Exception 
			{
				System.out.println( "\nJUnit test suite: Constructors" );
				
				m_engine = new XQEngine();
				
				m_engine.setMinIndexableWordLength( 0 );				
				m_engine.setDebugOutputToConsole( false );

				installSunXMLReader();   
                
				try {	
					String testFile = "bib.xml";	
					m_engine.setDocument( testFile );      
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