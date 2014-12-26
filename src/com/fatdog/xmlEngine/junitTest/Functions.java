/*  XQEngine Copyright (C) 2002-2005 Fatdog Software Inc. [howardk@fatdog.com]
 *	--------------------------------------------------------------------------
 *  This file is part of XQEngine and is distributed under the terms of the
 *  GNU General Public License. See additional info at the end of this file. */

package com.fatdog.xmlEngine.junitTest;

import com.fatdog.xmlEngine.*;
import com.fatdog.xmlEngine.exceptions.InvalidQueryException;
import com.fatdog.xmlEngine.exceptions.CategorizedInvalidQueryException;
import javax.xml.parsers.*;
import junit.framework.*;
import junit.extensions.TestSetup;
import org.xml.sax.XMLReader;

	/**
	 * Tests various built-in functions.
	 * 
	 * <P><B>NOTA</B>: This test case tests indexing a web-based document and requires internet connectivity.
	 * See {@link #suite()} if you don't want that.
	 * 
	 * @author Howard Katz, howardk@fatdog.com
	 * @version 0.69
	 */

public class Functions extends TestCase
{
	/**
	 * Testing the Effective Boolean Value of various arguments
	 */
	
	public void test_boolean_1() throws InvalidQueryException
	//--------------------------
	{
		ResultList hits = m_engine.setQuery( "boolean( () )" );
		assertEquals( hits.booleanValue(), false );
	}
	
	public void test_boolean_2() throws InvalidQueryException
	//--------------------------
	{
		ResultList hits = m_engine.setQuery( "boolean( false() )" );
		assertEquals( hits.booleanValue(), false );
	}
	
	public void test_boolean_3() throws InvalidQueryException
	//--------------------------
	{
		ResultList hits = m_engine.setQuery( "boolean( doc( 'nosuchdocumentanywhere' ) )" );
		assertEquals( hits.booleanValue(), false );
	}
	
	public void test_boolean_4() throws InvalidQueryException
	//--------------------------
	{
		ResultList hits = m_engine.setQuery( "boolean( '' )" );
		assertEquals( hits.booleanValue(), false );
	}
	
	public void test_boolean_5() throws InvalidQueryException
	//--------------------------
	{
		ResultList hits = m_engine.setQuery( "boolean( 0 )" );
		assertEquals( hits.booleanValue(), false );
	}
	
	public void test_boolean_6() throws InvalidQueryException
	//--------------------------
	{
		ResultList hits = m_engine.setQuery( "boolean( (1) )" );
		assertEquals( hits.booleanValue(), true );
	}
	
	public void test_boolean_7() throws InvalidQueryException
	//--------------------------
	{
		ResultList hits = m_engine.setQuery( "boolean( true() )" );
		assertEquals( hits.booleanValue(), true );
	}
	
	public void test_boolean_8() throws InvalidQueryException
	//--------------------------
	{
		ResultList hits = m_engine.setQuery( "boolean( 'a' )" );
		assertEquals( hits.booleanValue(), true );
	}
	
	public void test_boolean_9() throws InvalidQueryException
	//--------------------------
	{
		ResultList hits = m_engine.setQuery( "boolean( 123 )" );
		assertEquals( hits.booleanValue(), true );
	}
	
	
	/**
	 * A slash (/) separates "TCP" and "IP" into two words for word breaking.
	 */
	
	public void test_contains_word_1() throws InvalidQueryException
	//-------------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "contains-word( //title, 'TCP/IP' )" );
		assertEquals( "contains-word(title, 'tcp/ip' ) --", 1, hits.getNumValidItems() );
	}
	
	/**
	 * By default, word case is irrelevant.
	 */
	
	public void test_contains_word_2() throws InvalidQueryException
	//-------------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "contains-word( //title, 'tCp' )" );
		assertEquals( "contains-word(title, 'tcp' ) --", 1, hits.getNumValidItems() );
	}
	
	/**
	 * Case is significant if you want it to be.
	 */
	
	public void test_contains_word_3() throws InvalidQueryException
	//-------------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "contains-word( //title, 'tcp', true() )" );
		assertEquals( "contains-word(title, 'tcp', true() ) --", 0, hits.getNumValidItems() );
	}
	
	public void test_contains_word_4() throws InvalidQueryException
	//-------------------------------------------------------------
	{
		String query = "contains-word( //first,\"w\" )";
		
		ResultList hits = m_engine.setQuery( query );
		assertEquals( "contains-word( //first, \"w\" ) --", 2, hits.getNumValidItems() );
	}
	
	/**
	 * Internal whitespace and/or punctuation acts to delimit words.
	 */
	 
	public void test_contains_word_5() throws InvalidQueryException
	//-------------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "contains-word( //title, 'tcp/ip' )" );
		assertEquals( "contains-word(title, 'tcp', 'ip' ) --", 1, hits.getNumValidItems() );
	}
	
	/**
	 * Any in-word separators will do.
	 */
	
	public void test_contains_word_6() throws InvalidQueryException
	//-------------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "contains-word( //title, 'tcp    ip' )" );
		assertEquals( "contains-word(title, 'tcp', 'ip' ) --", 1, hits.getNumValidItems() );
	}
	
	/**
	 * Any number of explicit word arguments is fine as well.
	 */
	
	public void test_contains_word_7() throws InvalidQueryException
	//-------------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "contains-word( //title, 'tcp', 'ip' )" );
		assertEquals( "contains-word(title, 'tcp', 'ip' ) --", 1, hits.getNumValidItems() );
	}
	
	/**
	 * If *any* words are missing, the search fails.
	 */
	
	public void test_contains_word_8() throws InvalidQueryException
	//-------------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "contains-word( //title, 'tcp', 'ip', 'antidisestablishmentarianism' )" );
		assertEquals( "contains-word(title, 'tcp', 'ip' ) --", 0, hits.getNumValidItems() );	
	}
	
	public void test_countDocuments() throws InvalidQueryException
	//------------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "/*" );      
		assertEquals( "//* --", 1, hits.getNumValidItems() );
	}
	
	public void test_count_1() throws InvalidQueryException
	//-----------------------------------------------------
	{		
		ResultList hits = m_engine.setQuery( "count( //node() )" );      
		assertEquals( "count( //node() ) --", 90, hits.evaluateAsInteger() );
	}      

	public void test_count_2() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "count( 1,'two',3 )" );      	
		assertEquals( "count( 1,'two',3  ) --", 3, hits.evaluateAsInteger() );
	}   
	
	public void test_count_3() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "count( () )" );      
		assertEquals( "count( () ) -- ", 0, hits.evaluateAsInteger() );
	}   	
	
	public void test_count_4() throws InvalidQueryException
	//-----------------------------------------------------
	{		
		ResultList hits = m_engine.setQuery( "count( //first)" );      
		assertEquals( "count( //first ) --", 6, hits.evaluateAsInteger() );
	}   
	
	public void test_doc_0() throws InvalidQueryException
	//----------------------------------------------------
	{
		try
		{
			ResultList hits = m_engine.setQuery( "doc( '' )" );
			fail( "Shouldn't be here!" );
		}
		catch ( CategorizedInvalidQueryException ciqe )
		{
			// expect to be here
		}
	}
	
	// 23feb04: Note this is new behaviour for this test case. Previously expected failure   
	
	public void test_doc_1() throws InvalidQueryException
	//---------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "doc( 'NoSuchDocumentAnywhereInTheKnownUniverseAsFarAsICanTell.xml' )" );
		assertEquals( 0, hits.getNumTotalItems() );
	}
	
	public void test_doc_2() throws InvalidQueryException
	//----------------------
	{
		ResultList hits = m_engine.setQuery( "doc('http://www.fatdog.com/bib.xml' )" );
		assertEquals( 1, hits.getNumValidItems() );
	}   
	
	public void test_doc_3() throws InvalidQueryException
	//----------------------
	{
		ResultList hits = m_engine.setQuery( "doc('http://www.fatdog.com/bib.xml' )//*" );
		assertEquals( 36, hits.getNumValidItems() );
	}   
	
	public void test_doc_4() throws InvalidQueryException
	//----------------------
	{
		ResultList hits = m_engine.setQuery( "doc('http://www.fatdog.com/bib.xml' )//@*" );
		assertEquals( 4, hits.getNumValidItems() );
	}   
	
	public void test_empty_1() throws InvalidQueryException
	//------------------------
	{
		ResultList hits = m_engine.setQuery( "empty(//edit)" );
		assertEquals( true, hits.booleanValue() );
	}
	
	public void test_empty_2() throws InvalidQueryException
	//------------------------
	{
		ResultList hits = m_engine.setQuery( "empty(//editor)" );
		assertEquals( false, hits.booleanValue() );	
	}
	
	public void test_exists_1() throws InvalidQueryException
	//-------------------------
	{
		ResultList hits = m_engine.setQuery( "exists( () )" );
		
		int[] boolResult = hits.valueType( 0 );
		
		assertEquals( "'exists() -- value not False --'", 0, boolResult[0] );
		assertEquals( "'exists() -- incorrect NodeTree.BOOLEAN --'", DocItems. BOOLEAN, boolResult[1] );		
	}
	
	public void test_exists_2() throws InvalidQueryException
	//-------------------------
	{
		ResultList hits = m_engine.setQuery( "exists(1,2)" );
		
		int[] boolResult = hits.valueType( 0 );
		
		assertEquals( "'exists(1,2) -- value not True --'", -1, boolResult[0] );
		assertEquals( "'exists(1,2) -- incorrect NodeTree.BOOLEAN --'", DocItems. BOOLEAN, boolResult[1] );		
	}	
	
	public void test_followingSibling_0()
	//---------------------------------
	{
		try
		{
			ResultList hits = m_engine.setQuery( "following-sibling( )" );
			fail( "following-sibling() should be demanding an argument" );
		}
		catch( InvalidQueryException ieq ) { /* should be here */ }
	}
	
	// empty sequence arg
	
	public void test_followingSibling_1() throws InvalidQueryException
	//---------------------------------
	{
		ResultList hits = m_engine.setQuery( "following-sibling( () )" );
		assertEquals( 0, hits.getNumValidItems() );
	}
	
	// evals to empty sequence arg
	
	public void test_followingSibling_2() throws InvalidQueryException
	//---------------------------------
	{
		ResultList hits = m_engine.setQuery( "following-sibling( doc( 'adfjadfk' ) )" );
		assertEquals( 0, hits.getNumValidItems() );
	}
	
	// doc nodes don't have following-sibs 
	
	public void test_followingSibling_3() throws InvalidQueryException
	//---------------------------------
	{
		ResultList hits = m_engine.setQuery( "following-sibling( root( //editor ) )" );
		assertEquals( 0, hits.getNumValidItems() );
	}
	
	// NOTA for now, atomics are ignored. DO WE WANT TO THROW INSTEAD ????
	
	public void test_followingSibling_4() throws InvalidQueryException
	//---------------------------------
	{
		ResultList hits = m_engine.setQuery( "following-sibling( ( 1, 2 ) )" );
		assertEquals( 0, hits.getNumValidItems() );
	}
	
	public void test_followingSibling_5() throws InvalidQueryException
	//---------------------------------
	{
		ResultList hits = m_engine.setQuery( "following-sibling( //editor )" );
		int[] expected = { 0,84, 0,85, 0,87, 0,88 };
		
		assertEqualIntSequences( "", expected, hits.getDocNotatedNodeList() );      
	}
	
	public void test_followingSibling_6() throws InvalidQueryException
	//---------------------------------
	{
		ResultList hits = m_engine.setQuery( "following-sibling( //editor/last )[1]" );
		
		assertEquals( "<first>Darcy</first>", hits.emitXml() );
	}
	
	public void test_followingSibling_7() throws InvalidQueryException
	//---------------------------------
	{
		ResultList hits = m_engine.setQuery( "following-sibling( //editor/last )[3]/text()" );
		
		int[] expected = { 82, 81 };
		assertEqualIntSequences( "", expected, hits.valueType( 0 ) );
	}
	
	// test checking for mandatory argument in FAndO.dispatchNamedFunction()
	public void test_functionMustHavaArg()
	//------------------------------------
	{
		try
		{
			ResultList hits = m_engine.setQuery( "exists()" );      
			fail( "Expected 'InvalidQueryException (FAndO: function exists() takes an argument)");
		}
		catch (InvalidQueryException ex ) {}
	}	
	
	/*
	 * empty sequence -> 0-length string
	 */
	
	public void test_name_1() throws InvalidQueryException
	//-----------------------
	{
		ResultList hits = m_engine.setQuery( "name( () )" );
		
		int[] item = hits.valueType( 0 );
		String strResult = hits.getStringResult( item[ 0 ] );
		
		assertEquals( 0, strResult.length() );
	}
	
	/*
	 * single node -> name of node
	 */
	public void test_name_2() throws InvalidQueryException
	//-----------------------
	{
		ResultList hits = m_engine.setQuery( "name( //editor )" );
		
		int[] item = hits.valueType( 0 );
		String str = hits.getStringResult( item[ 0 ] );
		
		assertEquals( str, "editor" );
	}
	
	/*
	 * single attribute node
	 */
	public void test_name_3() throws InvalidQueryException
	//-----------------------
	{
		ResultList hits = m_engine.setQuery( "name( //@year[ . = '1992' ] )" );
		
		int[] item = hits.valueType( 0 );
		String str = hits.getStringResult( item[ 0 ] );
		
		assertEquals( "year", str );
	}
	
	public void test_name_4()
	//-----------------------
	{
		try
		{
			ResultList hits = m_engine.setQuery( "name( //book )" );
			fail( "Expect failure: argument must be the empty sequence or a single node" );
		}
		catch( InvalidQueryException ciqe )
		{
			// expect to be here
		}
	}
	
	public void test_name_5() throws InvalidQueryException
	//-----------------------
	{
		ResultList hits = m_engine.setQuery( "name( root( //editor ) )" );
		
		int[] item = hits.valueType( 0 );
		String str = hits.getStringResult( item[ 0 ] );
		
		assertEquals( str, "" );
	}
	

	public void test_string_1()
	//-------------------------
	{		
		try
		{
			ResultList hits = m_engine.setQuery( "string()" );       
			fail( "Expected 'FAndO.string(): No context present when string() function is at query root'" );
		}
		catch (InvalidQueryException ex ) {}
	}
	
	public void test_string_2() throws InvalidQueryException	
	//-------------------------
	{		
		ResultList hits = m_engine.setQuery( "string(//@year)" );
		
		String expected = "1994199220001999";
		String actual 	= hits.toString( true );
		
		assertEquals( "string(//@year) -- ", expected, actual );
	}
	
	public void test_string_3() throws InvalidQueryException	
	//-------------------------
	{		
		ResultList hits = m_engine.setQuery( "string(//first)" );
		
		String expected = "W.W.SergePeterDanDarcy";
		String actual 	= hits.toString( true );
		
		assertEquals( "string(//first) -- ", expected, actual );
	}
	
	public void test_string_4() throws InvalidQueryException	
	//-------------------------
	{		
		ResultList hits = m_engine.setQuery( "string(//first/text())" );
		
		String expected = "W.W.SergePeterDanDarcy";
		String actual 	= hits.toString( true );
		
		assertEquals( "string(//first/text() ) -- ", expected, actual );
	}
	
	public void test_string_5() throws InvalidQueryException	
	//-------------------------
	{		
		ResultList hits = m_engine.setQuery( "string(//editor)" );
		
		String expected = "\n\t\tGerbargDarcy\n\t\tCITI\n\t\t";
		String actual 	= hits.toString( true );
		
		assertEquals( "string(//editor) -- ", expected, actual );
	}
	
	public void test_string_6() throws InvalidQueryException	
	//-------------------------
	{		
		ResultList hits = m_engine.setQuery( "string(1)" );
		
		String expected = "1";
		String actual 	= hits.toString( true );
		
		assertEquals( "string(1) -- ", expected, actual );
	}
	
	public void test_string_7() throws InvalidQueryException	
	//-------------------------
	{		
		ResultList hits = m_engine.setQuery( "string( (1,2) )" );
		
		String expected = "12";
		String actual 	= hits.toString( true );
		
		assertEquals( "string( (1,2) ) -- ", expected, actual );
	}
	
	public void test_trueFalse_1() throws InvalidQueryException
	//----------------------------
	{
		ResultList hits = m_engine.setQuery( "true()" );
		assertEquals( "true", hits.emitXml() );
		
		hits = m_engine.setQuery( "fn:true()" );
		assertEquals( "true", hits.emitXml() );
	}	
	
	public void test_trueFalse_2() throws InvalidQueryException
	//----------------------------
	{
		ResultList hits = m_engine.setQuery( "false()" );
		assertEquals( "false", hits.emitXml() );
		
		hits = m_engine.setQuery( "fn:false()" );
		assertEquals( "false", hits.emitXml() );
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
	
	void assertEqualIntSequences( String msg, int[] expectedResponse, int[] actualResponse )
	//-------------------------------------------------------------------------------------
	{
		assertEquals( "Number of entries in resultList -- ", expectedResponse.length/2, actualResponse.length/2 );
		for( int i = 0; i < actualResponse.length; i++ )
			assertEquals( msg, expectedResponse[i], actualResponse[i] );
	} 
	
	private static XQEngine m_engine;
	
	public static Test suite()
	//------------------------
	{		
		TestSuite suite = new TestSuite( Functions.class );
		
		TestSetup setup = new TestSetup( suite ) {
			protected void setUp() throws Exception 
			{
				System.out.println( "\nJUnit test suite: Functions" );
				
				m_engine = new XQEngine();
				
				m_engine.setMinIndexableWordLength( 0 );				
				m_engine.setDebugOutputToConsole( false );

				installSunXMLReader();   
                
				try {	
					// illustrates use of 
					// (1) 'ad hoc indexing' feature
					// (2) remote indexing
					
					String query = "doc( 'http://www.fatdog.com/bib.xml' )";
					m_engine.setQuery( query );      
				}
				catch( InvalidQueryException e ) { throw e; }			
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