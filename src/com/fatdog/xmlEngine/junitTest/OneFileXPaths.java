/*  XQEngine Copyright (C) 2002-2005 Fatdog Software Inc. [howardk@fatdog.com]
 *	--------------------------------------------------------------------------
 *  This file is part of XQEngine and is distributed under the terms of the
 *  GNU General Public License. See additional info at the end of this file. */

package com.fatdog.xmlEngine.junitTest;

import com.fatdog.xmlEngine.*;
import com.fatdog.xmlEngine.exceptions.*;
import com.fatdog.xmlEngine.javacc.*;
import java.io.FileNotFoundException;
import javax.xml.parsers.*;
import junit.framework.*;
import junit.extensions.TestSetup;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.XMLReader;
	
	/**
	 * Tests a variety of XQuery expressions that select nodes from a single-document collection.
	 * 
	 * <P>The single file referenced, "bib.xml", ships with the XQuery Uses Cases.
	 * 
	 * @author Howard Katz, howardk@fatdog.com
	 * @version 0.61
	 */
	
public class OneFileXPaths extends TestCase
{  
	public void test_attributes_1() throws InvalidQueryException
	//----------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//book/@year" );           
		assertEquals( "//book/@year --", 4, hits.getNumValidItems() );
        
		int[] correctResponse = { 0, 3, 0, 21, 0, 39, 0, 69 };
		assertEqualNodeSequences( "nodeId of //book/@year --", correctResponse, hits.getDocNotatedNodeList() );
	}
    
	public void test_attributes_2() throws InvalidQueryException
	//----------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//@year" );       
		assertEquals( "//@year --", 4, hits.getNumValidItems() );
        
		int[] correctResponse = { 0, 3, 0, 21, 0, 39, 0, 69 };      
		assertEqualNodeSequences( "nodeId of //@year --", correctResponse, hits.getDocNotatedNodeList() );        
	}    
	
	public void test_attributes_3() throws InvalidQueryException
	//----------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "/bib/book/@year" );       
		assertEquals( "/bib/book/@year --", 4, hits.getNumValidItems() );
        
		int[] correctResponse = { 0, 3, 0, 21, 0, 39, 0, 69 };      
		assertEqualNodeSequences( "nodeId of /bib/book@year --", correctResponse, hits.getDocNotatedNodeList() );        
	}    
    
	public void test_child_1() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "/bib/book" );        
		assertEquals( "/bib/book --", 4, hits.getNumValidItems() );
        
		int[] correctResponse = { 0, 2, 0, 20, 0, 38, 0, 68 };      
		assertEqualNodeSequences( "nodeId of /bib/book --", correctResponse, hits.getDocNotatedNodeList() );        
	}    
    
	public void test_child_2() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "/(bib)/book" );      
		assertEquals( "/(bib)/book --", 4, hits.getNumValidItems() );
        
		int[] correctResponse = { 0, 2, 0, 20, 0, 38, 0, 68 };    
		assertEqualNodeSequences( "nodeId of /(bib)/book --", correctResponse, hits.getDocNotatedNodeList() );        
	}        
    
	public void test_child_3() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "/bib/book/editor" );       
		assertEquals( "/bib/book/editor --", 1, hits.getNumValidItems() );
        
		int[] correctResponse = { 0, 74 };       
		assertEqualNodeSequences( "nodeId of /bib/book/editor --", correctResponse, hits.getDocNotatedNodeList() );        
	}    
	
	public void test_descendants_1() throws InvalidQueryException
	//-----------------------------------------------------------
	{
	  ResultList hits = m_engine.setQuery( "/bib//book" );         	
	  assertEquals( "/bib//book --", 4, hits.getNumValidItems() );          
	}
    
	public void test_descendants_2() throws InvalidQueryException
	//------------------------------
	{
	  ResultList hits = m_engine.setQuery( "/bib//editor" );        
	  assertEquals( "/bib//editor --", 1, hits.getNumValidItems() );          
	}   
    
	public void test_descendants_3() throws InvalidQueryException
	//------------------------------
	{
	  ResultList hits = m_engine.setQuery( "/bib//editor/last" );           
	  assertEquals( "bib//editor/last --", 1, hits.getNumValidItems() );          
	}     
	    
	public void test_descendants_4() throws InvalidQueryException
	//------------------------------
	{
	  ResultList hits = m_engine.setQuery( "//editor[1]//text()[1]" );           
	  assertEquals( "//editor[1]//text()[1] --", 4, hits.getNumValidItems() );        
	  
	  int[] correctResponse = { 0, 75, 0, 77, 0, 79, 0, 82 };      
	  assertEqualNodeSequences( "nodeId of //editor[1]//text()[1] --", correctResponse, hits.getDocNotatedNodeList() );      
	} 
	
	public void test_filter_00() throws InvalidQueryException
	//-------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//book[author/first]" );          
		assertEquals( "//book[author/first] --", 3, hits.getNumValidItems() );        
	}	
	public void test_filter_0() throws InvalidQueryException
	//------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//book[author]" );          
		assertEquals( "//book[author] --", 3, hits.getNumValidItems() );        
	}
	
	public void test_filter_1() throws InvalidQueryException
	//------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//book[editor]" );          
		assertEquals( "//book[editor] --", 1, hits.getNumValidItems() );        
		
		int[] correctResponse = { 0, 68 };      
		assertEqualNodeSequences( "nodeId of //book[editor] --", correctResponse, hits.getDocNotatedNodeList() );      
	}
	
	public void test_filter_2() throws InvalidQueryException
	//------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//book[editor/affiliation]" );          
		assertEquals( "//book[editor/affiliation] --", 1, hits.getNumValidItems() );        
		
		int[] correctResponse = { 0, 68 };      
		assertEqualNodeSequences( "nodeId of //book[editor/affiliation] --", correctResponse, hits.getDocNotatedNodeList() );      
	}
	
	public void test_filter_3() throws InvalidQueryException
	//------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//book[editor]/affiliation" );          
		assertEquals( "//book[editor]/affiliation --", 0, hits.getNumValidItems() );        
	}
	
	public void test_filter_4() throws InvalidQueryException
	//------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//book[editor]//affiliation" );          
		assertEquals( "//book[editor]//affiliation --", 1, hits.getNumValidItems() );        
		
		int[] correctResponse = { 0, 81 };      
		assertEqualNodeSequences( "nodeId of //book[editor]//affiliation --", correctResponse, hits.getDocNotatedNodeList() );      
	}
	
	public void test_filter_5() throws InvalidQueryException
	//------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//book[@year]" );          
		assertEquals( "//book[@year] --", 4, hits.getNumValidItems() );        
		
		int[] correctResponse = { 0, 2, 0, 20, 0, 38, 0, 68 };      
		assertEqualNodeSequences( "nodeId of //book[@year] --", correctResponse, hits.getDocNotatedNodeList() );   
	}
	
	public void test_filter_6() throws InvalidQueryException
	//------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//editor[text()]" );          
		assertEquals( "//editor[text()] --", 1, hits.getNumValidItems() );        
	}
	
	public void test_filter_7() throws InvalidQueryException
	//------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "/*[text()]" );          
		assertEquals( "/*[text()] --", 1, hits.getNumValidItems() );        
	}
	
	// out of 36 elements in total, 5 <author>s
	public void test_filter_8() throws InvalidQueryException
	//------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//*[text()]" );          
		assertEquals( "//*[text()] --", 31, hits.getNumValidItems() );        
	}

	public void test_getNumDocs()
	//---------------------------
	{
		int numDocs = m_engine.getNumDocuments();
		assertEquals( "OneFileXPath numDocuments --", 1, numDocs );
	}
	
	public void test_getDocNames()
	//----------------------------
	{
		String docName = m_engine.getDocumentName( 0 );
		assertEquals( "OneFileXPaths docName -- ", "bib.xml", docName );
	}
	
	public void test_getDocNameOutOfRange()
	//-------------------------------------
	{
		try {
			m_engine.getDocumentName( -2 );
			fail( "TwoFileXPaths, No document ID -2 --" );
			
			m_engine.getDocumentName( 1 );
			fail( "TwoFileXPaths, No document ID 1 --" );
		}
		catch( java.lang.IllegalArgumentException e )
		{
			// ok to be here
		}
	}
	
	public void test_getNodeCounts()
	//------------------------------
	{
		int[] expected = { 36, 4, 50 };
		
		int[] counts = m_engine.getNodeTypeCounts();
		for( int i = 0; i < 3; i++ )
			assertEquals( "OneFileXPath nodeType counts -- ", expected[ i ], counts[ i ] );
	}
	
 	boolean[] getNodeTypesAtLeaf( String query ) throws InvalidQueryException
	//------------------------------------------
	{
		XQueryParser parser = new XQueryParser( new java.io.StringReader( query ));
        
		try 
		{
			SimpleNode root = parser.getXQueryAST();
	
			return m_engine.getIndexManager().getCurrTreeWalker().getNodeTypesAtLeaf( root );
		}
		catch( ParseException ex ) {
			throw new InvalidQueryException( ex.getMessage() );
		}
		catch( TokenMgrError error ) {
			throw new InvalidQueryException( error.getMessage() );
		}
	}
	
	// some static type checking tests that are actually file-independent
	// maybe we should move these to a MiscTesting file ??????????
	
	public void test_misc_nodeTypesAtLeaf_1() throws InvalidQueryException
	//---------------------------------------
	{
		boolean[] types = getNodeTypesAtLeaf( "/*" );
		
		boolean[] expected = { true, false, false };
		
		for( int i = 0; i < types.length; i++ )
			assertEquals( "Leaf Type " + i + " for '/*' -- ", expected[ i ], types[ i ] );
	}
	
	public void test_misc_nodeTypesAtLeaf_2() throws InvalidQueryException
	//---------------------------------------
	{
		boolean[] types = getNodeTypesAtLeaf( "//@*" );
		
		boolean[] expected = { false, false, true };
		
		for( int i = 0; i < types.length; i++ )
			assertEquals( "Leaf Type " + i + " for '//@*' -- ", expected[ i ], types[ i ] );
	}
	  
	public void test_misc_nodeTypesAtLeaf_3() throws InvalidQueryException 
	//---------------------------------------
	{
		boolean[] types = getNodeTypesAtLeaf( "//text()" );
		
		boolean[] expected = { false, true, false };
		
		for( int i = 0; i < types.length; i++ )
			assertEquals( "Leaf Type " + i + " for '//text()' -- ", expected[ i ], types[ i ] );
	}  

	public void test_misc_nodeTypesAtLeaf_4() throws InvalidQueryException 
	//---------------------------------------
	{
		boolean[] types = getNodeTypesAtLeaf( "//node()" );
		
		boolean[] expected = { true, true, true };
		
		for( int i = 0; i < types.length; i++ )
			assertEquals( "Leaf Type " + i + " for '//node()' -- ", expected[ i ], types[ i ] );
	}  

	public void test_node_1() throws InvalidQueryException
	//----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "/node()" );      
		assertEquals( "/node() --", 1, hits.getNumValidItems() );
	}      
    
	public void test_node_2() throws InvalidQueryException
	//----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//node()" );      
		assertEquals( "//node() --", 90, hits.getNumValidItems() );
	}  
	
	public void test_node_3() throws InvalidQueryException
	//----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//editor/node()" );       
		assertEquals( "//editor/node()/.. --", 6, hits.getNumValidItems() );
	} 
	
	public void test_node_4() throws InvalidQueryException
	//----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//editor//node()" );       
		assertEquals( "//editor//node()/.. --", 9, hits.getNumValidItems() );
	} 
	
	// NOTA: does not include parent(0) == document node. IS THIS CORRECT ??
    
	public void test_parent_1() throws InvalidQueryException
	//------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//node()/.." );       
		assertEquals( "//node()/.. --", 36, hits.getNumValidItems() );
	}
	
	public void test_parent_2() throws InvalidQueryException
	//------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//book/title/.." );       
		assertEquals( "//book/title/.. --", 4, hits.getNumValidItems() );
	}
	
	public void test_parent_3() throws InvalidQueryException
   	//------------------------------------------------------
   	{
	   	ResultList hits = m_engine.setQuery( "//book/title/../editor" );       
 		assertEquals( "//book/title/../editor --", 1, hits.getNumValidItems() );
 		
		int[] correctResponse = { 0, 74 };
		assertEqualNodeSequences( "nodeId of //book/title/../editor --", correctResponse, hits.getDocNotatedNodeList() );
   	}
   	
	public void test_parent_4() throws InvalidQueryException
	//------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//book/editor/../title" );       
		assertEquals( "//book/editor/../title --", 1, hits.getNumValidItems() );
		
		int[] correctResponse = { 0, 71 };
		assertEqualNodeSequences( "nodeId of //book/title/../editor --", correctResponse, hits.getDocNotatedNodeList() );
	}
	
	/*
	public void test_Parent_2() throws InvalidQueryException
	{
		ResultList hits = m_engine.setQuery( "//text()/.." );       
		assertEquals( "//text()/.. --", 31, hits.getNumValidNodes() );
	}  
	*/ 
/*
	public void test_Parent3() throws InvalidQueryException
	{
		ResultList hits = m_engine.setQuery( "//editor/text()/.." );       
		assertEquals( "//editor/text()/.. --", 1, hits.getNumValidNodes() );
	} 
	*/
	/*
	public void test_Parent4() throws InvalidQueryException
	{
		ResultList hits = m_engine.setQuery( "//editor//text()/.." );       
		assertEquals( "//editor//text()/.. --", 4, hits.getNumValidNodes() );
	} 
*/
/*
	public void test_Parent5() throws InvalidQueryException
	{
		ResultList hits = m_engine.setQuery( "//editor//node()/.." );       
		assertEquals( "//editor//node()/.. --", 1, hits.getNumValidNodes() );
	} 
	*/
	/*
	public void test_Parent6() throws InvalidQueryException
	{
		ResultList hits = m_engine.setQuery( "//editor//node()/.." );       
		assertEquals( "//editor//node()/.. --", 4, hits.getNumValidNodes() );
	} 
	*/
	
	public void test_protocolHandler_1()
	//----------------------------------
	{
		try {
			m_engine.setDocument( "junkProtocol:dkdkdkdkdk" );
			fail( "Junk protocol: should have thrown" );
		}
		catch( com.fatdog.xmlEngine.exceptions.CantParseDocumentException cpde ) 			{}
		catch( java.io.FileNotFoundException fnfe )											{}
		catch( com.fatdog.xmlEngine.exceptions.MissingOrInvalidSaxParserException mispe )	{}
	}
	
	public void test_root_1() throws InvalidQueryException
	//----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "/bib" ); 
		assertEquals( "/bib --", 1, hits.getNumValidItems() );
	}
    
	public void test_root_2() throws InvalidQueryException
	//----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "bib" ); 
		assertEquals( "bib --", 0, hits.getNumValidItems() );
	}
    
	public void test_root_3() throws InvalidQueryException
	//----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//book" );        
		assertEquals( "//book --", 4, hits.getNumValidItems() );
	}
    
	public void test_root_4() throws InvalidQueryException
	//----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "/*" ); 
		assertEquals( "/* --", 1, hits.getNumValidItems() );
	}
    
	public void test_root_5() throws InvalidQueryException
	//----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "/gobbledeegook" ); 
		assertEquals( "/gobbledeegook --", 0, hits.getNumValidItems() );
	}

	public void test_root_6() throws InvalidQueryException
	//----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "gobbledeegook" ); 
		assertEquals( "gobbledeegook --", 0, hits.getNumValidItems() );
	}

	public void test_subscripts_1() throws InvalidQueryException
	//----------------------------------------------------------
	{
	  ResultList hits = m_engine.setQuery( "/*[1]" );        
	  assertEquals( "/*[1] --", 1, hits.getNumValidItems() );   
	}
	
	public void test_subscripts_2() throws InvalidQueryException
	//----------------------------------------------------------
	{
	  ResultList hits = m_engine.setQuery( "/bib[1]" );        
	  assertEquals( "/bib[1] --", 1, hits.getNumValidItems() );          
	}
    
	public void test_subscripts_3() throws InvalidQueryException
	//----------------------------------------------------------
	{
	  ResultList hits = m_engine.setQuery( "//book[1]" );        
	  assertEquals( "//book[1] --", 1, hits.getNumValidItems() );          
	}
	
	public void test_subscripts_4() throws InvalidQueryException
	//----------------------------------------------------------
	{
	  ResultList hits = m_engine.setQuery( "/bib/book/author[1]" );        
	  assertEquals( "/bib/book/author[1] --", 3, hits.getNumValidItems() );          
	}
	
	public void test_subscripts_5() throws InvalidQueryException
	//----------------------------------------------------------
	{
	  ResultList hits = m_engine.setQuery( "/bib/book/author[2]" );        
	  assertEquals( "/bib/book/author[2] --", 1, hits.getNumValidItems() );          
	}
	
	public void test_subscripts_6() throws InvalidQueryException
	//----------------------------------------------------------
	{
	  ResultList hits = m_engine.setQuery( "//author[2]" );        
	  assertEquals( "/author[2] --", 1, hits.getNumValidItems() );          
	}
	
	public void test_subscripts_7() throws InvalidQueryException
	//----------------------------------------------------------
	{
	  ResultList hits = m_engine.setQuery( "/bib/book/author[2]/last" );        
	  assertEquals( "/bib/book/author[2]/last --", 1, hits.getNumValidItems() );          
	}
	
	public void test_subscripts_8() throws InvalidQueryException
	//----------------------------------------------------------
	{
	  ResultList hits = m_engine.setQuery( "//author[2]/last" );        
	  assertEquals( "//author[2]/last --", 1, hits.getNumValidItems() );          
	}
	
	public void test_subscripts_9() throws InvalidQueryException
	//----------------------------------------------------------
	{
	  ResultList hits = m_engine.setQuery( "//author[2]/last/text()" );        
	  assertEquals( "//author[2]/last/text() --", 1, hits.getNumValidItems() );          
	}
	
	public void test_subscripts_10() throws InvalidQueryException
	//----------------------------------------------------------
	{
	  ResultList hits = m_engine.setQuery( "//author[2]/node()" );        
	  assertEquals( "//author[2]/node() --", 2, hits.getNumValidItems() );          
	}	
	
	public void test_subscripts_11() throws InvalidQueryException
	//----------------------------------------------------------
	{
	  ResultList hits = m_engine.setQuery( "//author[2]//node()" );        
	  assertEquals( "//author[2]//node() --", 4, hits.getNumValidItems() );          
	}
	
	public void test_subscripts_12() throws InvalidQueryException
	//----------------------------------------------------------
	{
	  ResultList hits = m_engine.setQuery( "//author[2]/text()" );        
	  assertEquals( "//author[2]/text() --", 0, hits.getNumValidItems() );          
	}
	
	public void test_subscripts_13() throws InvalidQueryException
	//----------------------------------------------------------
	{
	  ResultList hits = m_engine.setQuery( "//author[2]//text()" );        
	  assertEquals( "//author[2]//text() --", 2, hits.getNumValidItems() );          
	}

	public void test_subscripts_14() throws InvalidQueryException
	//----------------------------------------------------------
	{
	  ResultList hits = m_engine.setQuery( "//*:book[1]" );        
	  assertEquals( "//*:book[1] --", 1, hits.getNumValidItems() );          
	}
	
	public void test_subscripts_15() throws InvalidQueryException
	//----------------------------------------------------------
	{
	  ResultList hits = m_engine.setQuery( "//*:book[0]" );        
	  assertEquals( "//*:book[1] --", 0, hits.getNumValidItems() );          
	}

/*
	// 			assumes single "bib.xml" file   
	// NOTA:    Following two routine names do not seem to be well-named.
	//          WHAT DO THEY DO ??????????????????????????????????????
    
	public void test_elementCounts()
	//------------------------------
	{
		//assertEquals( "Unique element names", 42, m_engine.getNumUniqueElementNames() );
		assertEquals( "Total element names", 54, m_engine.getNumTotalElementNames() );
	}
    
	// NOTA: The following count of TEXT nodes does *NOT* include single-line CR's!!
    
	public void test_countNodeTypes()
	//-------------------------------
	{
		int[] nodeTypes = m_engine.getNodeTypeCounts();
		assertEquals( "Number of element nodes", 36, nodeTypes[ 0 ] );
		assertEquals( "Number of attribute nodes", 4, nodeTypes[ 1 ] );
		assertEquals( "Number of text nodes", 50, nodeTypes[ 2 ] );        
	}
   */
 
	public void test_text_1() throws InvalidQueryException
	//----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//text()" );       
		assertEquals( "//text() --", 50, hits.getNumValidItems() );
	}    
    
	public void test_text_2() throws InvalidQueryException
	//----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "text()" );       
		assertEquals( "text() --", 0, hits.getNumValidItems() );
	} 
	
	public void test_text_3() throws InvalidQueryException
	//----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//editor/text()" );       
		assertEquals( "//editor/text() --", 3, hits.getNumValidItems() );
	} 
	
	public void test_text_4() throws InvalidQueryException
	//----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//editor//text()" );       
		assertEquals( "//editor//text() --", 6, hits.getNumValidItems() );
	} 
	
	public void test_wildcard_1() throws InvalidQueryException
	//--------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//*" );       
	//	assertEquals( "//* --", 36, hits.getNumValidNodes() );
	}
	
	public void test_wildcard_2() throws InvalidQueryException
	//--------------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "//editor/*" );       
		assertEquals( "//editor/* --", 3, hits.getNumValidItems() );
	}
    
	void assertEqualNodeSequences( String nodeMsg, int[] correctResponse, int[] actualResponse )
	//------------------------------------------------------------------------------------------
	{
		assertEquals( "Number of nodes in resultSet", correctResponse.length/2, actualResponse.length/2 );
		for( int i = 0; i < actualResponse.length; i++ )
			assertEquals( nodeMsg, correctResponse[i], actualResponse[i] );
	} 

	private static void installXercesXMLReader() throws Exception
	//-----------------------------------------------------------
	{
		String parserName = "org.apache.xerces.parsers.SAXParser";
		XMLReader parser = XMLReaderFactory.createXMLReader( parserName );

		m_engine.setXMLReader( parser );
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
	        m_engine.setUseLexicalPrefixes( true );
		}
		catch( Exception e )    { throw e; }
	}  
	
	static XQEngine m_engine;

	public static Test suite()
	//------------------------
	{				
		TestSuite suite = new TestSuite( OneFileXPaths.class );
		
		TestSetup setup = new TestSetup( suite ) {
			protected void setUp() throws Exception 
			{
				System.out.println( "\nJUnit test suite: OneFileXPaths" );
				
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