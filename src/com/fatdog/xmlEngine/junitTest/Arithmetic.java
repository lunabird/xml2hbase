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

public class Arithmetic extends TestCase
{  
	// accceptable diffs for double subtractions
	// (w/ my jvm1.4, diffs are actually good down to 1.0e-16, but this is good enuf)
	// (other jvm's might not be so accurate ??? -- maybe )
	
	public final static double kEPSILON = 1.0e-12;
	
	// int + int
	public void test_add_1() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1 + 2" );
		String xml = hits.emitXml();
		assertEquals( "3", xml );
	}
	
	// int + decimal
	public void test_add_2() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1 + 1.2" );
		String xml = hits.emitXml();
		assertEquals( "2.2", xml );
	}
	
	// int + double
	public void test_add_3() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1 + 1.2e0" );
		String xml = hits.emitXml();
		assertEquals( "2.2", xml );
	}
	
	// 4 - int + float comes here
	
	// decimal + int
	public void test_add_5() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1.2 + 1" );
		String xml = hits.emitXml();
		assertEquals( "2.2", xml );
	}
	
	// decimal + decimal
	public void test_add_6() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1.1 + 1.2" );
		String xml = hits.emitXml();
		assertEquals( "2.3", xml );
	}
	
	// decimal + double
	public void test_add_7() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1.2 + 1.0e0" );
		String xml = hits.emitXml();
		assertEquals( "2.2", xml );
	}
	
	// 8 decimal + float comes here
	
	// double + int
	public void test_add_9() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1.2e0 + 1" );
		String xml = hits.emitXml();
		assertEquals( "2.2", xml );
	}
	
	// double + decimal
	public void test_add_10() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1.2e0 + 1.0" );
		String xml = hits.emitXml();
		assertEquals( "2.2", xml );
	}
	
	// double + double
	public void test_add_11() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1.2e0 + 1.0e0" );
		String xml = hits.emitXml();
		assertEquals( "2.2", xml );
	}
	
	// 12 -- double + float comes here
	
	public void test_add_13() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1 + 0" );
		String xml = hits.emitXml();
		assertEquals( "1", xml );
	}
	
	public void test_add_14() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1.0 + 0" );
		String xml = hits.emitXml();
		assertEquals( "1.0", xml );
	}
	
	public void test_add_15() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1.0e0 + 0" );
		String xml = hits.emitXml();
		assertEquals( "1.0", xml );
	}
	
	//===========================================================================//
	
	// int - int
	public void test_subtract_1() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1 - 2" );
		String xml = hits.emitXml();
		assertEquals( "-1", xml );
	}
	
	// int - decimal
	public void test_subtract_2() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1 - 1.2" );
		String xml = hits.emitXml();
		assertEquals( "-0.2", xml );
	}
	
	// int - double
	public void test_subtract_3() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "2 - 1.2e0" );
		String xml = hits.emitXml();
		assertEquals( "0.8", xml );
	}
	
	// 4 - int - float comes here
	
	// decimal - int
	public void test_subtract_5() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1.2 - 1" );
		String xml = hits.emitXml();
		assertEquals( "0.2", xml );
	}
	
	// decimal - decimal
	public void test_subtract_6() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1.2 - 1.1" );
		String xml = hits.emitXml();
		assertEquals( "0.1", xml );
	}
	
	// decimal - double
	public void test_subtract_7() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1.2 - 1.0e0" );
		
		double diff = hits.expectDouble();		
		assertEquals( true, ( 0.2 - diff ) < kEPSILON );
	}
	
	// 8 decimal + float comes here
	
	// double - int
	public void test_subtract_9() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1.2e0 - 1" );
		
		double diff = hits.expectDouble();		
		assertEquals( true, ( 0.2 - diff ) < kEPSILON );
	}
	
	// double - decimal
	public void test_subtract_10() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1.2e0 - 1.0" );
		
		double diff = hits.expectDouble();		
		assertEquals( true, ( 0.2 - diff ) < kEPSILON );
	}
	
	// double - double
	public void test_subtract_11() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1.2e0 - 1.0e0" );
		
		double diff = hits.expectDouble();		
		assertEquals( true, ( 0.2 - diff ) < kEPSILON );
	}
	
	// 12 -- double - float comes here
	
	public void test_subtract_13() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1 - 0" );
		String xml = hits.emitXml();
		assertEquals( "1", xml );
	}
	
	public void test_subtract_14() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1.0 - 0" );
		String xml = hits.emitXml();
		assertEquals( "1.0", xml );
	}
	
	public void test_subtract_15() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1.0e0 - 0" );
		String xml = hits.emitXml();
		assertEquals( "1.0", xml );
	}
// ============================================================================== //

//	 int * int
	public void test_multiply_1() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "2 * 3" );
		
		String xml = hits.emitXml();
		assertEquals( "6", xml );
	}
	
	// int * decimal
	public void test_multiply_2() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1 * 1.2" );
		
		String xml = hits.emitXml();
		assertEquals( "1.2", xml );
	}
	
	// int * double
	public void test_multiply_3() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "2 * 1.2e0" );
		
		String xml = hits.emitXml();
		assertEquals( "2.4", xml );
	}
	
	// 4 - int * float comes here
	
	// decimal * int
	public void test_multiply_5() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1.2 * 2" );
		String xml = hits.emitXml();
		assertEquals( "2.4", xml );
	}
	
	// decimal * decimal
	public void test_multiply_6() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1.2 * 1.2" );
		
		String xml = hits.emitXml();
		assertEquals( "1.44", xml );
	}
	
	// decimal * double
	public void test_multiply_7() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1.2 * 1.2e0" );
		
		String xml = hits.emitXml();
		assertEquals( "1.44", xml );
	}
	
	// 8 decimal + float comes here
	
	// double * int
	public void test_multiply_9() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1.2e0 * 2" );
		
		String xml = hits.emitXml();
		assertEquals( "2.4", xml );
	}
	
	// double * decimal
	public void test_multiply_10() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1.2e0 * 1.2" );
		
		String xml = hits.emitXml();
		assertEquals( "1.44", xml );
	}
	
	// double * double
	public void test_multiply_11() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1.2e0 * 1.2e0" );
		
		String xml = hits.emitXml();
		assertEquals( "1.44", xml );
	}

	// 12 - double * float comes here
	
	public void test_multiply_13() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1 * 0" );
		String xml = hits.emitXml();
		assertEquals( "0", xml );
	}
	
	public void test_multiply_14() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1.0 * 0" );
		String xml = hits.emitXml();
		assertEquals( "0.0", xml );
	}
	
	public void test_multiply_15() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1.0e0 * 0" );
		String xml = hits.emitXml();
		assertEquals( "0.0", xml );
	}
	
	//===========================================================================//
	
//	 int div int
	public void test_divide_1() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "2 div 3" );
		
		double quotient = hits.expectDouble();	
		
		double diff = quotient - ( (double)2 / (double)3 );
		assertEquals( true, diff < kEPSILON );
	}
	
	// int div decimal
	public void test_divide_2() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "3 div 1.5" );
		
		String xml = hits.emitXml();
		assertEquals( "2", xml );
	}
	
	// int div double
	public void test_divide_3() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "5 div 4e0" );
		
		String xml = hits.emitXml();
		assertEquals( "1.25", xml );
	}
	
	// 4 - int div float comes here

	// decimal div int
	public void test_divide_5() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "8.8e0 div 4" );

		String xml = hits.emitXml();
		assertEquals( "2.2", xml );
	}
	
	// decimal div decimal
	// NOTA 22mar05 Saxon83, MarkLogic, Galax return 1 (as decimal)
	public void test_divide_6() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1.2 div 1.2" );
		
		String xml = hits.emitXml();
		assertEquals( "1.0", xml );
	}
	
	// decimal div double
	// NOTA ditto results as above for Saxon, MarkLogic, Galax
	public void test_divide_7() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1.2 div 1.2e0" );
		
		String xml = hits.emitXml();
		assertEquals( "1.0", xml );
	}
	
	// 8 decimal div float comes here
	
	// double div int
	public void test_divide_9() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1.2e0 div 2" );
		
		String xml = hits.emitXml();
		assertEquals( "0.6", xml );
	}
	
	// double div decimal
	public void test_divide_10() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1.2e0 div 1.2" );
		
		String xml = hits.emitXml();
		assertEquals( "1.0", xml );
	}
	
	// double div double
	public void test_divide_11() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1.2e0 div 1.2e0" );
		
		String xml = hits.emitXml();
		assertEquals( "1.0", xml );
	}

	// 12 - double div float comes here
	
	public void test_divide_13() throws InvalidQueryException
	//-----------------------------------------------------
	{
		try
		{
			ResultList hits = m_engine.setQuery( "1 div 0" );
			
			fail( "division by zero should have raised an error" );
		}
		catch( CategorizedInvalidQueryException ciqe )
		{
			// happily, it did
		}
	}
	
	public void test_divide_14() throws InvalidQueryException
	//-----------------------------------------------------
	{
		try
		{
			ResultList hits = m_engine.setQuery( "1.0 div 0" );
			
			fail( "division by zero should have raised an error" );
		}
		catch( CategorizedInvalidQueryException ciqe )
		{
			// happily, it did
		}
	}
	
	public void test_divide_15() throws InvalidQueryException
	//-----------------------------------------------------
	{
		try
		{
			ResultList hits = m_engine.setQuery( "1.0e0 div 0" );
			fail( "division by zero should have raised an error" );
		}
		catch( CategorizedInvalidQueryException ciqe )
		{
			// happily, it did
		}
	}
	
	//===========================================================================//
	
//	 int idiv int
	public void test_idivide_1() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "2 idiv 3" );
		
		String xml = hits.emitXml();
		assertEquals( "0", xml );
	}
	
	public void test_idivide_1_1() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "3 idiv 2" );
		
		String xml = hits.emitXml();
		assertEquals( "1", xml );
	}
	
	// int idiv decimal
	public void test_idivide_2() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "3 idiv 1.5" );
		
		String xml = hits.emitXml();
		assertEquals( "2", xml );
	}
	
	// int idiv double
	public void test_idivide_3() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "5 idiv 4e0" );
		
		String xml = hits.emitXml();
		assertEquals( "1", xml );
	}
	
	// 4 - int div float comes here

	// decimal idiv int
	public void test_idivide_5() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "8.8e0 idiv 4" );

		String xml = hits.emitXml();
		assertEquals( "2", xml );
	}
	
	// decimal idiv decimal
	// NOTA 22mar05 Saxon83, MarkLogic, Galax return 1 (as decimal)
	public void test_idivide_6() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "-1.2 idiv 1.2" );
		
		String xml = hits.emitXml();
		assertEquals( "-1", xml );
	}
	
	// decimal idiv double
	// NOTA ditto results as above for Saxon, MarkLogic, Galax
	public void test_idivide_7() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1.2 idiv -1.2e0" );
		
		String xml = hits.emitXml();
		assertEquals( "-1", xml );
	}
	
	// 8 decimal div float comes here
	
	// double idiv int
	public void test_idivide_9() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1.2e0 idiv 2" );
		
		String xml = hits.emitXml();
		assertEquals( "0", xml );
	}
	
	// double idiv decimal
	public void test_idivide_10() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1.2e0 idiv 1.2" );
		
		String xml = hits.emitXml();
		assertEquals( "1", xml );
	}
	
	// double idiv double
	public void test_idivide_11() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "-1.2e0 idiv 1.2e0" );
		
		String xml = hits.emitXml();
		assertEquals( "-1", xml );
	}

	// 12 - double idiv float comes here
	
	public void test_idivide_13() throws InvalidQueryException
	//-----------------------------------------------------
	{
		try
		{
			ResultList hits = m_engine.setQuery( "1 idiv 0" );
			
			fail( "division by zero should have raised an error" );
		}
		catch( CategorizedInvalidQueryException ciqe )
		{
			// happily, it did
		}
	}
	
	public void test_idivide_14() throws InvalidQueryException
	//-----------------------------------------------------
	{
		try
		{
			ResultList hits = m_engine.setQuery( "1.0 idiv 0" );
			
			fail( "division by zero should have raised an error" );
		}
		catch( CategorizedInvalidQueryException ciqe )
		{
			// happily, it did
		}
	}
	
	public void test_idivide_15() throws InvalidQueryException
	//-----------------------------------------------------
	{
		try
		{
			ResultList hits = m_engine.setQuery( "1.0e0 idiv 0" );
			fail( "division by zero should have raised an error" );
		}
		catch( CategorizedInvalidQueryException ciqe )
		{
			// happily, it did
		}
	}

	//===========================================================================//
	
//	 int mod int
	public void test_mod_1() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "2 mod 3" );	// 2
		
		String xml = hits.emitXml();
		assertEquals( "2", xml );
	}
	
	public void test_mod_1_1() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "3 mod 2" );	// 1
		
		String xml = hits.emitXml();
		assertEquals( "1", xml );
	}
	
	// int mod decimal
	public void test_mod_2() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "3 mod 1.5" );		// 0
		
		String xml = hits.emitXml();
		assertEquals( "0", xml );
	}
	
	// int mod double
	
	// NOTA: Saxon83 returns a non-decimal-pt 1
	public void test_mod_3() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "5 mod 4e0" );
		
		String xml = hits.emitXml();
		assertEquals( "1.0", xml );
	}
	
	// 4 - int div float comes here

	// decimal mod int
	public void test_mod_5() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "8.8e0 mod 4" );	// .80000 ...

		double answer = hits.expectDouble();		
		assertEquals( true, ( answer - 0.8 ) < kEPSILON );
	}
	
	// decimal mod decimal
	public void test_mod_6() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "-1.2 mod 1.2" );	// 0
		
		String xml = hits.emitXml();
		assertEquals( "0", xml );
	}
	
	// decimal mod double
	
	// NOTA: Saxon83 returns a non-decimal-pt 1
	public void test_mod_7() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1.2 mod -1.2e0" );	// 0
		
		double answer = hits.expectDouble();
		assertEquals( true, ( answer ) < kEPSILON );
	}
	
	// 8 decimal mod float comes here
	
	// double mod int
	public void test_mod_9() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1.2e0 mod 2" );	// 1.2
		
		String xml = hits.emitXml();
		assertEquals( "1.2", xml );
	}
	
	// double mod decimal
	
	// NOTA: Saxon83 returns a non-decimal-pt 1
	public void test_mod_10() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "1.2e0 mod 1.2" );	// 0
		
		double answer = hits.expectDouble();
		assertEquals( true, ( answer ) < kEPSILON );
	}
	
	// double mod double
	public void test_mod_11() throws InvalidQueryException
	//-----------------------------------------------------
	{
		ResultList hits = m_engine.setQuery( "-1.2e0 mod 1.2e0" );	// 0

		double answer = hits.expectDouble();
		assertEquals( true, ( answer ) < kEPSILON );
	}

	// 12 - double mod float comes here
	
	public void test_mod_13() throws InvalidQueryException
	//-----------------------------------------------------
	{
		try
		{
			ResultList hits = m_engine.setQuery( "1 mod 0" );
			
			fail( "division by zero should have raised an error" );
		}
		catch( CategorizedInvalidQueryException ciqe )
		{
			// happily, it did
		}
	}
	
	public void test_mod_14() throws InvalidQueryException
	//-----------------------------------------------------
	{
		try
		{
			ResultList hits = m_engine.setQuery( "1.0 mod 0" );
			
			fail( "division by zero should have raised an error" );
		}
		catch( CategorizedInvalidQueryException ciqe )
		{
			// happily, it did
		}
	}
	
	public void test_mod_15() throws InvalidQueryException
	//-----------------------------------------------------
	{
		try
		{
			ResultList hits = m_engine.setQuery( "1.0e0 mod 0" );
			fail( "division by zero should have raised an error" );
		}
		catch( CategorizedInvalidQueryException ciqe )
		{
			// happily, it did
		}
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
		TestSuite suite = new TestSuite( Arithmetic.class );
		
		TestSetup setup = new TestSetup( suite ) {
			protected void setUp() throws Exception 
			{
				System.out.println( "\nJUnit test suite: Arithmetic" );
				
				m_engine = new XQEngine();
				
				installSunXMLReader();
		/*
				m_engine.setMinIndexableWordLength( 0 );			
				m_engine.setDebugOutputToConsole( false )
                
				try {
					String testFile = "bib.xml";	
					m_engine.setDocument( testFile );      
				}
				catch( FileNotFoundException e ) { throw e; }
				catch( CantParseDocumentException e ) { throw e; }
				catch( MissingOrInvalidSaxParserException e ) { throw e; }	
		*/			
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