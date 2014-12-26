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
	 * Tests a variety of XQuery expressions that don't require file access.
	 * 
	 * @author Howard Katz, howardk@fatdog.com
	 * @version 0.69
	 */
	
public class Expressions extends TestCase
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
	
	public void test_ctor_0() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "<r>{<a/>}</r>" );
		String xml = hits.emitXml();
		assertEquals( "<r><a/></r>", xml );
	}
	
	public void test_flwor_1() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "for $b in /bib return $b/book" );	
		assertEquals( "for $b in /bib return $b/book --", 4, hits.getNumValidItems() );        
		
		int[] correctResponse = { 0, 2, 0, 20, 0, 38, 0, 68 };      
		assertEqualNodeSequences( "nodeId of let sequence --", correctResponse, hits.getDocNotatedNodeList() );   
	}
	
	public void test_flwor_2() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "let $i := /bib, $j := $i//editor return $j" );
		assertEquals( "let $i := /bib, $j := $i/editor return $j --", 1, hits.getNumValidItems() );        
		
		int[] correctResponse = { 0, 74 };      
		assertEqualNodeSequences( "nodeId of let sequence --", correctResponse, hits.getDocNotatedNodeList() );   
	}
	
	public void test_flwor_3() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "let $i := /bib, $i := $i//editor return $i" );	
		assertEquals( "let $i := /bib, $i := $i/editor return $i --", 1, hits.getNumValidItems() );        
		
		int[] correctResponse = { 0, 74 };      
		assertEqualNodeSequences( "nodeId of let sequence --", correctResponse, hits.getDocNotatedNodeList() );   
	}
	
	public void test_flwor_4() throws InvalidQueryException
	//-------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "for $i in //book let $j := $i/author return $j" );		
		assertEquals( "for $i in //book let $j := $i/author return $j --", 5, hits.getNumValidItems() );
	}
	
	public void test_flwor_5() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "for $i in //book return $i/author//text()" );		
		assertEquals( "for $i in //book return $i/author//text() --", 10, hits.getNumValidItems() );
	}
	
	public void test_flwor_6() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "for $i in //book let $j := $i/author return $j//text()" );		
		assertEquals( "for $i in //book let $j := $i/author return $j//text() --", 10, hits.getNumValidItems() );
	}

	public void test_flwor_7() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "for $i in //book let $j := $i/author return <author>    {$j//text()}       </author>" );
		
		assertEquals( "for $i in //book let $j := $i/author return <author> {$j//text()} </author> --", 4, hits.getNumValidItems() );
		assertEquals( 
			"\n<author>StevensW.</author><author>StevensW.</author><author>AbiteboulSergeBunemanPeterSuciuDan</author><author></author> --",
				"<author>StevensW.</author><author>StevensW.</author><author>AbiteboulSergeBunemanPeterSuciuDan</author><author></author>", hits.emitXml() );
	}

	public void test_flwor_8()
	//------------------------
	{
		try {
			ResultList hits = m_engine.setQuery( "for $i in //book order by $i/author return $i" );
			
			fail( "orderSpec must not return multiple values --" );
		}
		catch( CategorizedInvalidQueryException ciqe ) 
		{
			// ok to be here
		}
		catch( InvalidQueryException iqe )
		{
			// we never get here, but need to declare to keep Java happy
		}
	}
	
	public void test_flwor_9() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "for $i in //book order by $i/author[1] return $i/author" );
		
		int[] correctResponse = { 0, 44, 0, 50, 0, 56, 0, 8, 0, 26 };      
		assertEqualNodeSequences( "nodeId of let sequence --", correctResponse, hits.getDocNotatedNodeList() );   
	}
	
	// null orderspec for editor-book falls at end of orderSpecList
	
	public void test_flwor_10() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "for $i in //book order by $i/author[1]/first return $i//first/text()" );
		
		assertEquals( "\nDarcySergePeterDanW.W. --", 6, hits.getNumValidItems() );
		assertEquals( "\nDarcySergePeterDanW.W. --", "DarcySergePeterDanW.W.", hits.emitXml() );
	}
	
	// 3 null orderspecs for non-editor books fall at start of orderSpecList
	
	public void test_flwor_11() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "for $i in //book/author order by $i/last return $i/last/text()" );
						
		assertEquals( "\nAbiteboulBunemanStevensStevensSuciu --", 5, hits.getNumValidItems());
		assertEquals( "\nAbiteboulBunemanStevensStevensSuciu --", "AbiteboulBunemanStevensStevensSuciu", hits.emitXml() );
	}
	
	// general comparisions -- integers
	public void test_flwor_12() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "for $b in //book where $b/@year = 2000 return $b/title/text()" );
			
		assertEquals( "\nData on the Web --", 1, hits.getNumValidItems());
		assertEquals( "\nData on the Web --", "Data on the Web", hits.emitXml() );
	}
	
	public void test_flwor_13() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "for $b in //book where $b/@year <= 1999 order by $b/title return $b/title/text()" );			
		assertEquals( "\nData on the Web --", 3, hits.getNumValidItems());
	}
	
	public void test_flwor_14() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "for $b in //book where $b/@year <= 1999 order by $b/title return $b/@year" );
			
		assertEquals( "\nData on the Web --", 3, hits.getNumValidItems());
		assertEquals( "\n@year=\"1992\" @year=\"1994\" @year=\"1999\" --", "@year=\"1992\" @year=\"1994\" @year=\"1999\" ", hits.emitXml() );
	}
	
	public void test_flwor_15() throws InvalidQueryException
	//------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "let $a := (1,2) for $b in $a return $a" );
			
		assertEquals( "", 4, hits.getNumValidItems());
	}

	public void test_generalComp_1() throws InvalidQueryException
	//----------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "'1'='1'" );      		
		assertEquals( "\n'1'!='1' --", true, hits.booleanValue() );
	}

	public void test_generalComp_2() throws InvalidQueryException
	//----------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "'1'='2'" );      		
		assertEquals( "\n'1'!='1' --", false, hits.booleanValue() );
	}
	
	public void test_generalComp_3() throws InvalidQueryException
	//----------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "'1'!='1'" );     		
		assertEquals( "\n'1'!='1' --", false, hits.booleanValue() );
	}

	public void test_generalComp_4() throws InvalidQueryException
	//----------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "'1'!='2'" );      		
		assertEquals( "\n'1'!='2' --", true, hits.booleanValue() );
	}
	
	public void test_generalComp_5() throws InvalidQueryException
	//----------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "'22' < '3'" );      		
		assertEquals( "\n'22' < '3' --", true, hits.booleanValue() );
	}

	public void test_generalComp_6() throws InvalidQueryException
	//----------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//editor/last/text() = 'Gerbarg'" );      		
		assertEquals( "\n//editor/last/text() = 'Gerbarg' --", true, hits.booleanValue() );
	}

	public void test_generalComp_7() throws InvalidQueryException
	//----------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1 <= 1" );      		
		assertEquals( "\n1 <= 1 --", true, hits.booleanValue() );
	}
	
	public void test_generalComp_8() throws InvalidQueryException
	//----------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1 <= 0" );      		
		assertEquals( "\n1 <= 0 --", false, hits.booleanValue() );
	}
	
	public void test_generalComp_9() throws InvalidQueryException
	//----------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1 > 0" );      
		assertEquals( "\n1 > 0 --", true, hits.booleanValue() );
	}

	public void test_generalComp_12() throws InvalidQueryException
	//----------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//editor/last/text() = 'Gerbarg'" );      
		assertEquals( "//editor/last/text() = 'Gerbarg' --", true, hits.booleanValue() );
	}
	
	public void test_generalComp_13() throws InvalidQueryException
	//----------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//author[2] = 'BunemanPeter'" );      
		assertEquals( "//author[2] = 'BunemanPeter' --", true, hits.booleanValue() );
	}

	public void test_ifthenelse_1() throws InvalidQueryException
	//----------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery(
			"if ( 1 = 1 ) then fn:true() else fn:false()" );
		
		boolean boolResult = hits.booleanValue();
		
		assertEquals( boolResult, true );	
	}
	
	public void test_intLiteral_1() throws InvalidQueryException
	//----------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "42" );      

		int[] actual 	= hits.valueType( 0 );
		int[] expected	= { 42, DocItems. INT };
		
		assertEqualIntSequences( "42 --", expected, actual );
	}
	
	public void test_orderComparison_1()
	//----------------------------------
	{
		try
		{
			ResultList hits = m_engine.setQuery( "1 << 2" ); 
			fail( "Can't do order compare on atomics -- should have been caught");
		}
		catch( CategorizedInvalidQueryException ciqe ) 
		{ // ok to be here 
		}
		catch( Exception e )
		{ 
			System.out.println( "Wrong exception: should be Categorized!" );
		}
	}
	
	public void test_orderComparison_2() throws InvalidQueryException
	//---------------------------------------------------------------
	{
		try
		{
			ResultList hits = m_engine.setQuery( "/bib << //book" ); 
			fail( "Can't do order compare on >1 nodes -- should have been caught");
		}
		catch( CategorizedInvalidQueryException ciqe ) 
		{ // ok to be here 
		}
		catch( Exception e )
		{ 
			System.out.println( "Wrong exception: should be Categorized!" );
		}
	}

	public void test_orderComparison_3() throws InvalidQueryException
	//---------------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "/bib << //editor" ); 
		assertEquals( true, hits.booleanValue() );
	}
	
	// an interesting way of testing for node equality (identity)
	
	public void test_orderComparison_4() throws InvalidQueryException
	//---------------------------------------------------------------
	{
		String q = "let $n := <a/> return not( $n << $n ) and not( $n >> $n )";
		
		ResultList hits = m_engine.setQuery( q ); 
		assertEquals( true, hits.booleanValue() );
	}
	
	public void test_sequence_1() throws InvalidQueryException
	//--------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "(1,2,3)" );      
		
		assertEquals( "(1,2,3) --", 3, hits.getNumValidItems() );
		assertEquals( "(1,2,3) --", "123", hits.emitXml() );
	}
	
	public void test_sequence_2() throws InvalidQueryException
	//--------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "(//book, 5)" );      
		
		ResultList sub_1 = hits.subscript(2);
		
		int[] actual 	= sub_1.valueType( 0 );
		int[] expected	= { 20, 20 };
		
		assertEqualIntSequences( "(//book, 5)[2] --", expected, actual );
	}
 
	public void test_sequence_3() throws InvalidQueryException
	//--------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "(//book, 5)" );      
		
		ResultList sub_1 = hits.subscript(5);
		
		int[] actual 	= sub_1.valueType( 0 );
		int[] expected	= { 5, DocItems. INT };
		
		assertEqualIntSequences( "(//book, 5).subscript(5) --", expected, actual );
	}
	
	public void test_sequence_4() throws InvalidQueryException
	//--------------------------------------------------------
	{
		ResultList hits 	= m_engine.setQuery( "(//book, 5)" );      
		ResultList sub_1	= null;
		
		try {
			sub_1 = hits.subscript(0);
			fail( "Exceptions.test_sequence_4(): Illegal Subscript <= 0 --" );
		}
		catch( java.lang.IllegalArgumentException e )
		{
			// ok to be here
		}
	}
	
	public void test_sequence_5() throws InvalidQueryException
	//--------------------------------------------------------
	{
		ResultList hits 	= m_engine.setQuery( "(//book, 5)[1]" );      
		
		assertEquals( "(//book,5)[1] --", 1, hits.getNumValidItems() );
		
		int[] correctResponse = { 0, 2 };      
		assertEqualNodeSequences( "(//book,5][1] --", correctResponse, hits.getDocNotatedNodeList() );   
	}
	
	// NOTA: it's ok to have a subscript greater than length
	
	public void test_sequence_6() throws InvalidQueryException
	//--------------------------------------------------------
	{
		ResultList hits 	= m_engine.setQuery( "(//book, 5)" );      
		ResultList sub1	= hits.subscript(6);
		
		assertEquals( "(//book,5)[6] --", 0, sub1.getNumValidItems() );
	}

	public void test_some_1() throws InvalidQueryException
	//----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "some $i in (1,2) satisfies $i = 1" );
		assertEquals( "some $i in ('1','2') satisfies $i = '1' --", true, hits.booleanValue() );	
	}

	public void test_some_2() throws InvalidQueryException
	//----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "some $i in ('1','2') satisfies $i = '1'" );
		assertEquals( "some $i in ('1','2') satisfies $i = '1' --", true, hits.booleanValue() );	
	}
	
	public void test_some_3() throws InvalidQueryException
	//----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "some $i in ('1','2') satisfies $i = '2'" );
		assertEquals( "some $i in ('1','2') satisfies $i = '2' --", true, hits.booleanValue() );	
	}
	
	public void test_some_4() throws InvalidQueryException
	//----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "some $i in ('1','2') satisfies $i = '3'" );
		assertEquals( "some $i in ('1','2') satisfies $i = '3' --", false, hits.booleanValue() );		
	}

	public void test_stringLiteral() throws InvalidQueryException
   //--------------------------------------------------------
   	{
	   ResultList hits = m_engine.setQuery( "\"string literal!\"" );      
	
	   String expected = "string literal!";
	   String actual 	= hits.toString( true );
		
	   assertEquals( "\"string literal!\" -- ", expected, actual );
   	}
	
	/* Value comparisons NYI @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	 
	public void test_valueComp_1() throws InvalidQueryException
	//----------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "'1'eq'1'" );      
		
		assertEquals( "'1'eq'1' --", 1, hits.getNumValidNodes() );
		
		int[] actualValueType 	= hits.valueType( 0 );
		int[] expectedValueType	= { 0, DocItems. STRING };
	
		assertEqualIntSequences( "'1'eq'1' --", expectedValueType, actualValueType );
	}

	public void test_valueComp_2() throws InvalidQueryException
	//----------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "'1'ne'1'" );      
		
		assertEquals( "'1'ne'1' --", 0, hits.getNumValidNodes() );
	}
	
	public void test_valueComp_3() throws InvalidQueryException
	//----------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "'1'eq'2'" );      
		
		assertEquals( "'1'eq'2' --", 0, hits.getNumValidNodes() );
	}
	
	public void test_valueComp_4() throws InvalidQueryException
	//----------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "'1'ne'2'" );      
		
		assertEquals( "'1'ne'2' --", 1, hits.getNumValidNodes() );
		
		int[] actualValueType 	= hits.valueType( 0 );
		int[] expectedValueType	= { 0, DocItems. STRING };
	
		assertEqualIntSequences( "'1'ne'2' --", expectedValueType, actualValueType );
	}
	
	public void test_valueComp_5() throws InvalidQueryException
	//----------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//editor/first eq 'Darcy'" );      
		
		assertEquals( "//editor/first eq 'Darcy' --", 1, hits.getNumValidNodes() );
		
		int[] actualValueType 	= hits.valueType( 0 );
		int[] expectedValueType	= { 0, DocItems. STRING };
	
		assertEqualIntSequences( "//editor/first eq 'Darcy' --", expectedValueType, actualValueType );
	}
	
	// string equality testing is case-sensitive
	
	public void test_valueComp_6() throws InvalidQueryException
	//----------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//editor/first ne 'darcy'" );      
		
		assertEquals( "//editor/first ne 'darcy' --", 1, hits.getNumValidNodes() );
		
		int[] actualValueType 	= hits.valueType( 0 );
		int[] expectedValueType	= { 0, DocItems. STRING };
	
		assertEqualIntSequences( "//editor/first ne 'darcy' --", expectedValueType, actualValueType );
	}
	
	public void test_valueComp_7()
	//----------------------------------------------------------
	{
		try
		{
			ResultList hits = m_engine.setQuery( "//book/first eq 'anyContentAtAll'" );  
			fail( "\nMultiple value in ValueComparison should throw!" );    
		}
		catch( InvalidQueryException iqe )
		{
			// should except to here due to mulitple values
		}
	}
*/
	static void installSunXMLReader() throws Exception
	//------------------------------------------------
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
	
	static XQEngine m_engine;
	
	public static Test suite()
	//------------------------
	{				
		TestSuite suite = new TestSuite( Expressions.class );
		
		TestSetup setup = new TestSetup( suite ) {
			protected void setUp() throws Exception 
			{
				System.out.println( "\nJUnit test suite: Expressions" );
				
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