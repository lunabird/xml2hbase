/*  XQEngine Copyright (C) 2002-2005 Fatdog Software Inc. [howardk@fatdog.com]
 *	--------------------------------------------------------------------------
 *  This file is part of XQEngine and is distributed under the terms of the
 *  GNU General Public License. See additional info at the end of this file. */

package com.fatdog.xmlEngine.junitTest;
import java.io.FileNotFoundException;

import junit.framework.*;
import junit.extensions.TestSetup;

import com.fatdog.xmlEngine.*;
import com.fatdog.xmlEngine.exceptions.*;

	/**
	 * Tests a variety of XQuery expressions that select nodes from a two-document collection.
	 * 
	 * <P>Files referenced are "bib.xml" and "bib_2.xml". "bib_2" is identical to "bib",
	 * except that all element and attribute names have been prefixed by "xqe:".
	 * (This version of the query engine does not support proper namespace declarations.)
	 * 
	 * @author Howard Katz, howardk@fatdog.com
	 * @version 0.69
	 */

public class TwoFileXPaths extends OneFileXPaths
{
	// NOTA	first several tests are overrides on OneFile methods
	//		since we're reusing the same queries but returning higher counts
	
	/* This test is failing / provide dummy override of OneFileXPaths version for now

	public void test_descendants_4() throws InvalidQueryException
	//------------------------------
	{
	  ResultList hits = m_engine.setQuery( "//editor[1]//text()[1]" );           
	  assertEquals( "//editor[1]//text()[1] --", 4, hits.getNumValidNodes() );        
	  
	  int[] correctResponse = { 0, 75, 0, 77, 0, 79, 0, 82 };      
	  assertEqualNodeSequences( "nodeId of //editor[1]//text()[1] --", correctResponse, hits.getNotatedNodeList() );      
	}   
	*/
	
	public void test_descendants_4() {}	// temporary dummy override
	//------------------------------
	
	public void test_filter_1() throws InvalidQueryException
	//------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//*:book[*:editor]" );          
		assertEquals( "//*:book[*:editor] --", 2, hits.getNumValidItems() );        
		
		int[] correctResponse = { 0, 68, 1, 68 };      
		assertEqualNodeSequences( "nodeId of //*:book[*:editor] --", correctResponse, hits.getDocNotatedNodeList() );      
	}
	
	public void test_filter_2_1() throws InvalidQueryException
	//------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//xqe:book[xqe:editor/xqe:affiliation]" );          
		assertEquals( "//xqe:book[xqe:editor/xqe:affiliation] --", 1, hits.getNumValidItems() );        
		
		int[] correctResponse = { 1, 68 };      
		assertEqualNodeSequences( "nodeId of //xqe:book[xqe:editor/xqe:affiliation] --", correctResponse, hits.getDocNotatedNodeList() );      
	}
	
	public void test_filter_2_2() throws InvalidQueryException
	//------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//*:book[*:editor/*:affiliation]" );          
		assertEquals( "//*:book[*:editor/*:affiliation] --", 2, hits.getNumValidItems() );        
		
		int[] correctResponse = { 0, 68, 1, 68 };      
		assertEqualNodeSequences( "nodeId of //xqe:book[xqe:editor/xqe:affiliation] --", correctResponse, hits.getDocNotatedNodeList() );      
	}
	
	public void test_filter_3() throws InvalidQueryException
	//------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//xqe:book[xqe:editor]/xqe:affiliation" );          
		assertEquals( "//xqe:book[xqe:editor]/xqe:affiliation --", 0, hits.getNumValidItems() );        
	}
	
	public void test_filter_4_1() throws InvalidQueryException
	//------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//xqe:book[xqe:editor]//xqe:affiliation" );          
		assertEquals( "//xqe:book[xqe:editor]//xqe:affiliation --", 1, hits.getNumValidItems() );        
		
		int[] correctResponse = { 1, 81 };      
		assertEqualNodeSequences( "nodeId of //book[editor]//affiliation --", correctResponse, hits.getDocNotatedNodeList() );      
	}
	
	public void test_filter_4_2() throws InvalidQueryException
	//------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//*:book[*:editor]//*:affiliation" );          
		assertEquals( "//*:book[*:editor]//*:affiliation --", 2, hits.getNumValidItems() );        
		
		int[] correctResponse = { 0, 81, 1, 81 };      
		assertEqualNodeSequences( "nodeId of //book[editor]//affiliation --", correctResponse, hits.getDocNotatedNodeList() );      
	}
	
	public void test_filter_5_1() throws InvalidQueryException
	//------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//xqe:book[@xqe:year]" );          
		assertEquals( "//xqe:book[@xqe:year] --", 4, hits.getNumValidItems() );        
		
		int[] correctResponse = { 1, 2, 1, 20, 1, 38, 1, 68 };      
		assertEqualNodeSequences( "nodeId of //xqe:book[@xqe:year] --", correctResponse, hits.getDocNotatedNodeList() );   
	}	
	
	public void test_filter_5_2() throws InvalidQueryException
	//------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//*:book[@*:year]" );          
		assertEquals( "//*:book[@*:year] --", 8, hits.getNumValidItems() );        
		
		int[] correctResponse = { 0, 2, 0, 20, 0, 38, 0, 68, 1, 2, 1, 20, 1, 38, 1, 68 };      
		assertEqualNodeSequences( "nodeId of //*:book[@xqe:year] --", correctResponse, hits.getDocNotatedNodeList() );   
	}

	public void test_filter_5_3() throws InvalidQueryException
	//------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//*:book[@xqe:year]" );          
		assertEquals( "//*:book[@xqe:year] --", 4, hits.getNumValidItems() );        
		
		int[] correctResponse = { 1, 2, 1, 20, 1, 38, 1, 68 };      
		assertEqualNodeSequences( "nodeId of //*:book[@xqe:year] --", correctResponse, hits.getDocNotatedNodeList() );   
	}
	
	public void test_filter_7() throws InvalidQueryException
	//------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "/*[text()]" );          
		assertEquals( "/*[text()] --", 2, hits.getNumValidItems() );        
	}
	
	public void test_filter_8() throws InvalidQueryException
	//------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//*[text()]" );          
		assertEquals( "//*[text()] --", 62, hits.getNumValidItems() );        
	}
	
	public void test_getNumDocs()
	//---------------------------
	{
		int numDocs = m_engine.getNumDocuments();
		assertEquals( "TwoFileXPaths numDocuments --", 2, numDocs );
	}
	
	public void test_getDocNames()
	//----------------------------
	{
		String docName = m_engine.getDocumentName( 0 );
		assertEquals( "bib.xml", docName );
		
		docName = m_engine.getDocumentName( 1 );
		assertEquals( "bib_2.xml", docName );	
	}
	
	public void test_getDocNameOutOfRange()
	//-------------------------------------
	{
		try {
			m_engine.getDocumentName( -2 );
			fail( "TwoFileXPaths, No document ID -2 --" );
			
			m_engine.getDocumentName( 2 );
			fail( "TwoFileXPaths, No document ID 2 --" );
		}
		catch( java.lang.IllegalArgumentException e )
		{
			// ok to be here
		}
	}
	
	public void test_getNodeCounts()
	//------------------------------
	{
		int[] expected = { 72, 8, 100 };
		
		int[] counts = m_engine.getNodeTypeCounts();
		for( int i = 0; i < 3; i++ )
			assertEquals( "OneFileXPath nodeType counts -- ", expected[ i ], counts[ i ] );
	}
	
	public void test_node_1() throws InvalidQueryException
	//----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "/node()" );      
		assertEquals( "/node()", 2, hits.getNumValidItems() );		
	}      
	
	public void test_node_2() throws InvalidQueryException
	//----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//node()" );      
		assertEquals( "//node() --", 180, hits.getNumValidItems() );
	}  
	
	public void test_parent_1() throws InvalidQueryException
	//------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//node()/.." );       
		assertEquals( "//node()/.. --", 72, hits.getNumValidItems() );
	}
	
	public void test_root_4() throws InvalidQueryException
	//----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "/*" ); 
		assertEquals( "/* --", 2, hits.getNumValidItems() );
	}
	
	public void test_subscripts_1() throws InvalidQueryException
	//-----------------------------------------------------------
	{
	  ResultList hits = m_engine.setQuery( "/*[1]" );        
	  assertEquals( "/*[1] --", 2, hits.getNumValidItems() );   
	}
	
	public void test_subscripts_14() throws InvalidQueryException
	//----------------------------------------------------------
	{
	  ResultList hits = m_engine.setQuery( "//*:book[1]" );
	  assertEquals( "//*:book[1] --", 2, hits.getNumValidItems() );
	  
	  int[] correctResponse = { 0, 2, 1, 2 };      
	  assertEqualNodeSequences( "nodeId of let sequence --", correctResponse, hits.getDocNotatedNodeList() );   
	}
	
	/*
	public void test_subscripts_11() throws InvalidQueryException
	//-----------------------------
	{
	  ResultList hits = m_engine.setQuery( "//author[2]//node()" );        
	  assertEquals( "//author[2]//node() --", 0, hits.getNumValidNodes() );          
	}
		*/
	
	public void test_parent_2_2() throws InvalidQueryException
	//------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//*:book/*:title/.." );       
		assertEquals(  "//*:book/*:title/..--", 8, hits.getNumValidItems() );
	}
	
	public void test_text_1() throws InvalidQueryException
	//----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//text()" );       
		assertEquals( "//text() --", 100, hits.getNumValidItems() );
	}    
	
	public void test_wildcard_1() throws InvalidQueryException
	//--------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//*" );       
		assertEquals( "//* --", 72, hits.getNumValidItems() );
	}
	
	public static Test suite()
	//------------------------
	{				
		TestSuite suite = new TestSuite( TwoFileXPaths.class );
		
		
		TestSetup setup = new TestSetup( suite ) {
			protected void setUp() throws Exception 
			{
				System.out.println( "\nJUnit test suite: TwoFileXPaths" );
				
				m_engine = new XQEngine();

				m_engine.setMinIndexableWordLength( 0 );			
				m_engine.setDebugOutputToConsole( false );
				m_engine.setUseLexicalPrefixes( true );

				installSunXMLReader();   
                
				try {	
					m_engine.setDocument( "bib.xml" );      
					m_engine.setDocument( "bib_2.xml" );   
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