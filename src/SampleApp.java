/*  XQEngine Copyright (C) 2002-2005 Fatdog Software Inc. [howardk@fatdog.com]
 * 
 *  This file is part of XQEngine and is distributed under the terms of the
 *  GNU General Public License. See additional information at the end of this file. */
 
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.xml.parsers.*;

import com.fatdog.xmlEngine.ResultList;
import com.fatdog.xmlEngine.XQEngine;
import com.fatdog.xmlEngine.exceptions.*;

import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
	
	/**
	 * <BSampleApp</B>, a XQEngine client application that posts a simple two-panel Swing dialog.
	 * Queries entered in the top panel return {@link com.fatdog.xmlEngine.ResultList} objects 
	 * in the bottom panel, as well as writing either serialized XML or debug information to the console.
	 * 
	 * @author Howard Katz, howardk@fatdog.com
	 * @version 0.69
	 */

public class SampleApp implements ActionListener, DocumentListener
{
	boolean PRETTYPRINT = true;
	
	// following for running generically on anybody's machine (no absolute addresses)
	// (data files found in the XQEngine.jar file)

	String BIB_FILE 		= "bib.xml";		// XMP use cases & misc junit tests
	String REVIEWS			= "reviews.xml";	// XMP use cases
	String BOOK				= "book.xml";		// TREE use cases
	String REPORT			= "report1.xml";	// SEQ use cases
	
    static  boolean m_go = false;

	XQEngine   	m_engine;

	String    	m_title = "XQuery Sample App";
	JFrame    	m_frame;
	JTextArea  	m_textArea;   
	JTextArea  	m_swingDisplayArea;   
	
	char		m_priorChar;	// for changes in the Document
	int			m_priorOffset;	// on keystroke entry
  
 
	public static void main( String[] args ) throws Exception
	//-------------------------------------------------------
	{
//		mouseClickToGo( "Click to start", 10, 10, 180, 100 ); 
//		while ( m_go == false ) {}         
        
		new SampleApp().run();
	}  
  
	ResultList query( String query ) throws InvalidQueryException
	//------------------------------
	{	
		long startTime = System.currentTimeMillis();
		
		ResultList results = m_engine.setQuery( query );
        
		long elapsed = System.currentTimeMillis() - startTime;      
		double newDuration = ( (int)( 100.0 * elapsed/1000.0 ) )/ 100.0;        
    		
   	// NOTA: 	This is *debug* stuff going (at present anyway) to the console
   	//			Standard serialized XML goes (via actionPerformed) to the Swing dialog
       	
		System.out.println( "\nquery = " + query );                
		results.getAST().dump("    " ); 
		System.out.println();
		
		if ( results.getNumValidItems() ==  0 )
		{
			System.out.println( "No hits!" );
		}
		else
		{					
			System.out.println( results.toString() );
				
			//String outFile = "C:/Documents and Settings/Howard/My Documents/XMP_11_OUT.txt";		
			//outputResultsToFile(  outFile, results, true );	
		}

		return results;
	}
	
	// here's where we instantiate the query engine, configure it,
	// and then call on it to index Shakespeare, bib.xml, or whatever
	
    public void run() throws Exception
    //---------------
    {
        m_swingDisplayArea = display();       
        
		mouseClickWindow( "Click to quit", 10, 600, 160, 100 );  
        
        m_engine = new XQEngine();

        m_engine.setMinIndexableWordLength( 0 );
        m_engine.setShowFileIndexing( true );
        
        m_engine.setUseLexicalPrefixes( false ); // no namespace decls required 
        
        // m_engine.setDebugOutputToConsole( false ); // no longer using in XQEngine
        
        // if ( testHTML )
        //     m_engine.setXMLReader( new org.ccil.cowan.tagsoup.Parser() );            

       	installSunXMLReader();           
        //installXercesXMLReader();
          
		long startTime = System.currentTimeMillis();
		
        try 
        {   
        	// following 4 are built into the XQEngine.jar file
    
			m_engine.setDocument( BIB_FILE );	
			m_engine.setDocument( BOOK );	
			m_engine.setDocument( REPORT );
			m_engine.setDocument( REVIEWS );

			 
			// the final int parameter says how many plays to index.
			
			String dirName = "C:/@@XQEngine/testfiles/shakespeare/";   
			indexShakespeare( m_engine, dirName, 0 );		
        }
        catch( FileNotFoundException e )
        {
            System.err.println( "\nSampleApp.run(): " + e );
            return;
        }
        catch( CantParseDocumentException e ) {
            System.out.println( "\n\nSampleApp.run(): CantParseDocumentException: " + e.getMessage() );
            return;
        }
        catch( MissingOrInvalidSaxParserException e ) {
            System.out.println( "\nSampleApp.run() exception: " + e.getMessage() );
            return;
        }
        long elapsed = System.currentTimeMillis() - startTime;
     
        m_engine.printSessionStats( elapsed, null );
    }
	
	void outputResultsToFile( String fileName, ResultList results, boolean prettyPrint )
	//----------------------------------------------------------------------------------
	{
		try
		{						
			FileWriter fw = new FileWriter( fileName );		
								
			results.emitXml( new PrintWriter( fw ), prettyPrint );
			
			fw.flush();
			fw.close();														
		} 
		catch (IOException e )
		{
			System.out.println( "Oops, I/O error on file write" );
		}
	}

    public void doReligion( XQEngine engine, String dir ) throws	FileNotFoundException,
                                                                    CantParseDocumentException,         
                                                                    MissingOrInvalidSaxParserException
	//---------------------------------------------------
    {
        engine.setDocument( dir + "bom.xml" );
        engine.setDocument( dir + "nt.xml" );
        engine.setDocument( dir + "ot.xml" );
        engine.setDocument( dir + "quran.xml" );        
    }
    
    public void indexShakespeare( XQEngine xmlEngine, String dir, int n )  	throws FileNotFoundException,
                                                                        	CantParseDocumentException,
                                                                        	MissingOrInvalidSaxParserException
    //-------------------------------------------------------------------
    {
        int count = 0;
  // 1     
        if ( ++count > n ) return; xmlEngine.setDocument( dir + "a_and_c.xml" );
        if ( ++count > n ) return; xmlEngine.setDocument( dir + "all_well.xml" );
        if ( ++count > n ) return; xmlEngine.setDocument( dir + "as_you.xml" );
        if ( ++count > n ) return; xmlEngine.setDocument( dir + "com_err.xml" );
        if ( ++count > n ) return; xmlEngine.setDocument( dir + "coriolan.xml" );
        if ( ++count > n ) return; xmlEngine.setDocument( dir + "cymbelin.xml" );
        if ( ++count > n ) return; xmlEngine.setDocument( dir + "dream.xml" );
        if ( ++count > n ) return; xmlEngine.setDocument( dir + "hamlet.xml" );
        if ( ++count > n ) return; xmlEngine.setDocument( dir + "hen_iv_1.xml" );
        if ( ++count > n ) return; xmlEngine.setDocument( dir + "hen_iv_2.xml" );
  // 11
        if ( ++count > n ) return; xmlEngine.setDocument( dir + "hen_v.xml" );
        if ( ++count > n ) return; xmlEngine.setDocument( dir + "hen_vi_1.xml" );
        if ( ++count > n ) return; xmlEngine.setDocument( dir + "hen_vi_2.xml" );
        if ( ++count > n ) return; xmlEngine.setDocument( dir + "hen_vi_3.xml" );
        if ( ++count > n ) return; xmlEngine.setDocument( dir + "hen_viii.xml" ); 
        if ( ++count > n ) return; xmlEngine.setDocument( dir + "j_caesar.xml" );
        if ( ++count > n ) return; xmlEngine.setDocument( dir + "john.xml" );
        if ( ++count > n ) return; xmlEngine.setDocument( dir + "lear.xml" );
        if ( ++count > n ) return; xmlEngine.setDocument( dir + "lll.xml" );
        if ( ++count > n ) return; xmlEngine.setDocument( dir + "m_for_m.xml" );
  // 21
        if ( ++count > n ) return; xmlEngine.setDocument( dir + "m_wives.xml" );
        if ( ++count > n ) return; xmlEngine.setDocument( dir + "macbeth.xml" );
        if ( ++count > n ) return; xmlEngine.setDocument( dir + "merchant.xml" );
        if ( ++count > n ) return; xmlEngine.setDocument( dir + "much_ado.xml" );
        if ( ++count > n ) return; xmlEngine.setDocument( dir + "othello.xml" );
        if ( ++count > n ) return; xmlEngine.setDocument( dir + "pericles.xml" );
        if ( ++count > n ) return; xmlEngine.setDocument( dir + "r_and_j.xml" );
        if ( ++count > n ) return; xmlEngine.setDocument( dir + "rich_ii.xml" );
        if ( ++count > n ) return; xmlEngine.setDocument( dir + "rich_iii.xml" );
        if ( ++count > n ) return; xmlEngine.setDocument( dir + "t_night.xml" );
  // 31
        if ( ++count > n ) return; xmlEngine.setDocument( dir + "taming.xml" );
        if ( ++count > n ) return; xmlEngine.setDocument( dir + "tempest.xml" );
        if ( ++count > n ) return; xmlEngine.setDocument( dir + "timon.xml" );
        if ( ++count > n ) return; xmlEngine.setDocument( dir + "titus.xml" );
        if ( ++count > n ) return; xmlEngine.setDocument( dir + "troilus.xml" );
        if ( ++count > n ) return; xmlEngine.setDocument( dir + "two_gent.xml" ); 
        if ( ++count > n ) return; xmlEngine.setDocument( dir + "win_tale.xml" );
    }
    
    private void installXercesXMLReader() throws Exception
    //-----------------------------------
    {
        String parserName = "org.apache.xerces.parsers.SAXParser";
        XMLReader parser = XMLReaderFactory.createXMLReader( parserName );

        m_engine.setXMLReader( parser );
    }

    private void installSunXMLReader() throws Exception
    //--------------------------------
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

    public void results( String xmlOutput ) { System.out.println( xmlOutput ); }
    //-------------------------------------

	void doQuery( String query )
	//--------------------------
	{
		ResultList results;
        
		try 
		{
			results = query( query );
			
			if ( results.getNumValidItems() == 0 )
			{
				m_swingDisplayArea.append( "\nNo hits!" );
				return;
			}				
		}
		catch( InvalidQueryException iqe ) {
			System.err.println( "\n" + iqe );
            
			m_swingDisplayArea.append( "InvalidQueryException\n" );
			return;
		}
		catch( Exception e ) {
			System.out.println( e );
            
			m_swingDisplayArea.append( "Exception\n" );
			return;
		}

		m_swingDisplayArea.append( results.emitXml( PRETTYPRINT ) + "\n");
	}
	
    public void actionPerformed( ActionEvent event )
    //----------------------------------------------
    {
        String query = event.getActionCommand();
        
        doQuery( query );
    }

    public JTextArea display( /*String query*/ )
    //------------------------------------------
    {
        m_frame = new JFrame( m_title );

        // top, left, width, height, last two ignored
        m_frame.setBounds( 10, 5, 10, 10 );

        m_frame.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e) {System.exit(0);}
            //--------------------------------------
        });

        Container contentFrame = m_frame.getContentPane();
        contentFrame.setLayout( new BorderLayout() );
        
        JTextArea queryArea = new JTextArea( "Query goes here ...", 14, 60 );
        JTextArea resultsArea = new JTextArea();

		JPanel textPanels = makeTextPanels( queryArea, resultsArea );
        contentFrame.add( "Center", makeTextPanels( queryArea, resultsArea ) );

        m_frame.pack();
        m_frame.setVisible(true);

        return resultsArea;
    }
	
    public JPanel makeTextPanels( JTextArea queryArea, JTextArea resultsArea )
    //------------------------------------------------------------------------
    {
        JPanel panel = new JPanel();
        panel.setLayout( new BorderLayout() );   
       	panel.add( "North", new JScrollPane( queryArea ));

       	Font font = new Font( "courier", Font.PLAIN, 12 );

		resultsArea.setFont( font );
		
		queryArea.setFont( font );
		queryArea.selectAll();
		queryArea.setFont( font );
		
		queryArea.getDocument().addDocumentListener( this );
		queryArea.requestFocus();
		
		resultsArea.setEditable( true );
    	
        panel.setPreferredSize( new Dimension( 500, 600 ));
        
    	panel.add( "Center", new JScrollPane( resultsArea ) );

		resultsArea.setLineWrap( true );
		resultsArea.setAutoscrolls(true);

    	return panel;
    }
   
	public void removeUpdate( DocumentEvent docEvent )  {}
	//------------------------------------------------
	public void changedUpdate( DocumentEvent docEvent ) {}
	//-------------------------------------------------
	public void insertUpdate( DocumentEvent docEvent )
	//------------------------------------------------
	{
		Document doc = docEvent.getDocument();
		String newText = null;
		
		int offset = docEvent.getOffset();
		if ( offset == m_priorOffset )
			return;
			
		m_priorOffset = offset;
		
		char currChar = '\0';
		
		try {
			
			currChar = doc.getText( offset, 1 ).charAt( 0 );
			
			if ( offset > 0 && currChar == '\n' && m_priorChar == '\n')
			{
				int len = doc.getLength();
				String query = doc.getText( 0, len - 1 );

				doQuery( query );
			}
		}
		catch( BadLocationException ble ) 
		{ 
			System.out.println( "BadLocationException: too bad!" ); 
		}
		
		int changeLength = docEvent.getLength();
				
		m_priorChar = currChar;
	}
	
    public static void mouseClickToGo( String title, int x, int y, int w, int h )
    //---------------------------------------------------------------------------
    {
        final java.awt.Frame frame = new java.awt.Frame();
        frame.setBounds( x, y, w, h );
        frame.addMouseListener (
            new java.awt.event.MouseAdapter() {
                public void mousePressed( java.awt.event.MouseEvent event ) {
                    System.out.println( "Mouse pressed -- starting up!" );
                    m_go = true;
                    frame.dispose();
                    return;
                }
            }
        );
        
        frame.setTitle( title );
        frame.show(); 
    }     
    
    public static void mouseClickWindow( String title, int x, int y, int w, int h )
    //-----------------------------------------------------------------------------
    {
        java.awt.Frame frame = new java.awt.Frame();
        frame.setBounds( x, y, w, h );
        frame.addMouseListener (
            new java.awt.event.MouseAdapter() {
                public void mousePressed( java.awt.event.MouseEvent event ) {
                    System.out.println( "Mouse Pressed!" );
                    System.exit( 0 );
                }
            }
        );
        
        frame.setTitle( title );
        frame.show(); 
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
 *   along with XQEngine; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */