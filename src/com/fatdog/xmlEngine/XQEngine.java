/*  XQEngine Copyright (C) 2002-2005 Fatdog Software Inc. [howardk@fatdog.com]
 *	--------------------------------------------------------------------------
 *  This file is part of XQEngine and is distributed under the terms of the
 *  GNU General Public License. See additional info at the end of this file. */
 
package com.fatdog.xmlEngine;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;

import com.fatdog.xmlEngine.exceptions.*;
import com.fatdog.xmlEngine.javacc.XQueryParser;
import com.fatdog.xmlEngine.javacc.SimpleNode;
import com.fatdog.xmlEngine.javacc.ParseException;
import com.fatdog.xmlEngine.javacc.TokenMgrError;
import com.fatdog.xmlEngine.words.*;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

	/**
	 * The main driver for the query engine.
	 * 
	 * @author Howard Katz, howardk@fatdog.com
	 * @version 0.69
	 */
	
public class XQEngine
{
	boolean	m_doSerializeIndex = false;
	
	final static String	INDEX_DIR = "xqengine_index";
	//-----------------------------------------------
	
    public final static String  DEFAULT_EXPLICIT_DOCNAME = "@ExplicitDocument@";    
    
    int   m_explicitDocNum = -1;

    // allocation policy
    public static   int   AGGRESSIVE  = 0;
    public static   int   MILD        = 1;
    public static   int   MEEK        = 2;

	boolean			m_debugOutputToConsole 	= false;
	boolean			m_showFileIndexing		= false;
	
    int             m_docCount      = 0;
    long            m_totalFileLen  = 0;
    
    int             m_minIndexableLength;

    XMLReader       m_reader;
    SAXHandler      m_saxHandler;
    IndexManager    m_indexer;
    
    int             m_currDocId;  // the document just indexed
    
    Hashtable       m_protocolHandlers 	= new Hashtable();
    
    
	boolean m_isDistributedQNameDictionaries = false;
	
	public void setIsDistributedQNameDictionaries( boolean isDistributed )
	//--------------------------------------------------------------------
	{
		m_isDistributedQNameDictionaries = isDistributed;
	}
	public boolean isDistributedQNameDictionaries()	{ return m_isDistributedQNameDictionaries; }
	//---------------------------------------------
	
		
	boolean m_isWordIndexing = false ;
	
	/**
	 * 
	 * NYI. To be used when explicit word indexing is required.
	 * 
	 * @param wordIndexing
	 */
	public void setIsWordIndexing( boolean wordIndexing ) { m_isWordIndexing = wordIndexing; }
	//----------------------------------------------------

	public boolean isWordIndexing() 	{ return m_isWordIndexing; }
	//-----------------------------
	
	boolean m_useLexicalPrefixes = false;	
	
	/**
	 * Indicate whether lexical (non-standard) namespace searching is in effect.
	 * 
	 * <P>Lexical prefix/namespace searching allows specification of location-path
	 * queries such as //biblio:book -- the QName prefix will be searched
	 * for exactly as is. A namespace declaration is not required (and is an error
	 * if present).
	 * 
	 * <P>If lexical prefix searching is false, a namespace declaration
	 * for the prefix is required.
	 * 
	 * @param useLexicalPrefixes a boolean flag indicating whether non-standard
	 * lexical prefix searching is allowed.
	 */
	public void setUseLexicalPrefixes( boolean useLexicalPrefixes ) { m_useLexicalPrefixes = useLexicalPrefixes; }
	//-------------------------------------------------------------

	public boolean getUseLexicalPrefixes() { return m_useLexicalPrefixes; }
	//------------------------------------
   
	/**
	 * Set a flag indicating whether debug output is desired.
	 * 
	 * <P>This sets a flag in the engine that calling routines can interrogate 
	 * via {@link #isDebugOutputToConsole}. This flag is only informational and 
	 * doesn't actually force debug output (which results from printing
	 * the result of {@link com.fatdog.xmlEngine.ResultList#toString} to the console).
	 * 
	 * @param debugOutput boolean toogle for indicating desired output type
	 */
	
	public void setDebugOutputToConsole( boolean debugOutput )	{ m_debugOutputToConsole = debugOutput; }
	//--------------------------------------------------------
	
	/**
	 * Queries whether the debug-output-to-console flag has been set.
	 */
	
	public boolean isDebugOutputToConsole()						{ return m_debugOutputToConsole; }
	//-------------------------------------
	
	/**
	 * Queries whether file-indexing progress is to be written to the console.
	 */
	
	public boolean isShowFileIndexing()							{ return m_showFileIndexing; }
	//---------------------------------
	
	/**
	 * Query status of file-indexing flag.
	 * 
	 * @see #isShowFileIndexing
	 */
	
	public void setShowFileIndexing( boolean showFileIndexing )	{ m_showFileIndexing = showFileIndexing; }
	//--------------------------------------------------------- 

	/**
	 * Return the {@link com.fatdog.xmlEngine.IndexManager} for this session.
	 */
	
    public IndexManager getIndexManager()						{ return m_indexer; }
	//-----------------------------------
	
    /**
	 * Set the minimum length of word to index.
	 * 
	 * <P>Default is 1.
	 * 
	 * @param length Only index words of this length or greater
	 */
	
    public void setMinIndexableWordLength( int length ) 		{ m_minIndexableLength = length; }
    //-------------------------------------------------

	/**
	 * Indicate the SAX2 {@link XMLReader} you want to use for parsing XML documents.
	 * 
	 * @param reader The XMLReader
	 * @return The {@link com.fatdog.xmlEngine.IndexManager} for the query engine
	 */
	
    public IndexManager setXMLReader( XMLReader reader )
    //--------------------------------------------------
    {
        m_indexer  		= new IndexManager( this );
        m_saxHandler    = new SAXHandler( m_indexer );
        m_reader        = reader;
        
        m_reader.setContentHandler( m_saxHandler );
        
		try {
		  	m_reader.setFeature("http://xml.org/sax/features/namespace-prefixes", false );
			m_reader.setFeature("http://xml.org/sax/features/namespaces", true);
		} catch (SAXException e) 
		{
		  System.err.println("setXMLReader(): namespace prefixes might not work!"); 
		}

		if ( isDebugOutputToConsole() )
        System.out.println( "XQEngine.setXMLReader(): XMLReader installed successfully\n" );
        
        return m_indexer;
    }
    
    /**
     * 
     * Get the SAXHandler currently in use.
     * 
     * @return current SAXHandler
     */
    public SAXHandler getSaxHandler()	{ return m_saxHandler; }
    //-------------------------------
    
	/**
	 * Register an {@link com.fatdog.xmlEngine.IProtocolHandler} object to receive, via its {@link com.fatdog.xmlEngine.IProtocolHandler#content} method, 
	 * the address argument passed in to either the {@link #setDocument(String)} method or the {@link #setExplicitDocument(String)} function. 
	 * 
	 * The query engine will have determined that the address is prefixed by the scheme prefix 
	 * being registered by this function. The engine uses the scheme to distinguish 
	 * custom-protocol-based addresses from normal filepath-based addresses, 
	 * so you should ensure the engine can use your scheme to disambiguate between the two. 
	 * 
	 * @param scheme An arbitrary string prefix of your own devising
	 * @param yourHandler An {@link com.fatdog.xmlEngine.IProtocolHandler} object
	 * @exception IllegalArgumentException If scheme or yourHandler are null, or the scheme is already registered
	 */
	public void registerProtocolHandler( String scheme, IProtocolHandler yourHandler )
	//--------------------------------------------------------------------------------
	{		
		if ( scheme == null || scheme == "" )
			throw new IllegalArgumentException(
				"\nregisterProtocolHandler(): you haven't registered a scheme" );
		
		if ( yourHandler == null )
			throw new IllegalArgumentException(
				"\nregisterProtocolHandler(): you haven't registered a protocol handler" );		
				
				
		if ( scheme.startsWith( "http" ) || scheme.startsWith( "file" ) )
			throw new IllegalArgumentException( 
				"\nregisterProtocolHandler(): http:// and file:// schemes are registered automatically" );		
        
		if ( m_protocolHandlers.containsKey( scheme ) )
			throw new IllegalArgumentException( 
				"\nregisterProtocolHandler(): Scheme " + scheme + " is already registered" );
        
		m_protocolHandlers.put( scheme, yourHandler );
	}
	
	// we've already determined that the prefix part of address has a protocol handler
	
	int getContentFromProtocolHandler( String address, IProtocolHandler handler ) throws
				SAXException, CantParseDocumentException, MissingOrInvalidSaxParserException 
	//----------------------------------------------------------------------------------
	{
		String content = handler.content( address );
		if ( content == null )
			throw new CantParseDocumentException( 
					"\nContent for protocol address \"" + address + "\" was empty" );
					
		return parseExplicit( content, address );
	}

    synchronized int setExplicitDocument( String explicitContent, String documentName ) throws 
    									CantParseDocumentException, MissingOrInvalidSaxParserException
    //------------------------------------------------------------------------------------------------
    {
        ++ m_explicitDocNum; 
        
        return parseExplicit( explicitContent, documentName );
    }   
    
	/**
	 * Add explicit (literal angle-bracket) XML to the index.
	 * 
	 * <P>Can be called repeatedly to add different explicit documents to the index. Unlike {@link #setDocument}, 
	 * this method takes literal, angle-bracket XML content and not a filename as a argument. 
	 * 
	 * @param explicitContent The literal "angle bracket" XML content you want indexed
	 * @return The integer document ID that identifies the document in the index
	 * 
	 * @exception CantParseDocumentException If the SAX parser can't parse the document
	 * @exception MissingOrInvalidSaxParserException If the SAX parser is missing or invalid
	 */
    
    public synchronized int setExplicitDocument( String explicitContent ) throws 
    							CantParseDocumentException, MissingOrInvalidSaxParserException
    //-----------------------------------------------------------------------------------------
    {
        ++ m_explicitDocNum; 

        return parseExplicit( explicitContent, 
        					generateExplicitDocumentName( explicitContent, m_explicitDocNum ) );
    }
    
    /*
     * 	You can override this method if you want to generate your own explicit
     *  filename, using the two arguments or not to help in that process. You 
     *  should make sure that the generated name is unique
     */
     
	protected String generateExplicitDocumentName( String explicitContent, int docId )
	//--------------------------------------------------------------------------------
	{
		return new String( DEFAULT_EXPLICIT_DOCNAME + "_<" + docId + ">" );
	}
	
	public synchronized int setDocument( String name ) throws 
					FileNotFoundException, CantParseDocumentException, MissingOrInvalidSaxParserException
  //-----------------------------------------------------------------------------------------------------    
	{
		return setDocument( name, false );
	}
	
	/**
	 * Index this document. 
	 * 
	 * <P>The document can be either :
	 * <ul>
	 * <li> an http://-based url
	 * <li> a file:/-based url
	 * <li> a local (relative) filepath-based name
	 * <li> an absolute filepath-based name
	 * <li> a custom-protocol address prefixed by a registered 'scheme'
	 * </ul>
	 * 
	 * @param name The name of the document to be indexed
	 * @return The unique integer docID assigned this document by the query engine
	 * @throws FileNotFoundException If the file couldn't be located
	 * @throws CantParseDocumentException If the SAX parser complained
	 * @throws MissingOrInvalidSaxParserException No SAX parser had been registered
	 */
	
    public synchronized int setDocument( String name, boolean inQuery ) throws 
    				FileNotFoundException, CantParseDocumentException, MissingOrInvalidSaxParserException
  //-----------------------------------------------------------------------------------------------------    
    {
        if ( m_reader == null )
            throw new MissingOrInvalidSaxParserException( "\nXMLReader not installed" );

        try {
            return parse( name );
        }
        catch( NullPointerException npe )
        {
        	if ( inQuery ) 
        		throw npe;
        	else
        		throw new FileNotFoundException( "XQEngine.setDocument(): File not found" );
        }
        catch( SAXParseException e ) 
        {
            throw new CantParseDocumentException( e.getMessage() );
        }
        catch( SAXException e )
        {
            if ( e.getException() instanceof FileNotFoundException )
                throw (FileNotFoundException)(e.getException());

            throw new CantParseDocumentException( e.getMessage() );
        }
        catch( java.lang.OutOfMemoryError oom )
        {
            System.out.println ("setDocument(): OOM exception on file : \"" + name + "\"" );
            throw oom;
        }
    }  

    int parse( String fileName ) throws SAXException, CantParseDocumentException, MissingOrInvalidSaxParserException
    //----------------------------
    {
		IProtocolHandler handler = null;		

    	if ( ( handler = hasRegisteredProtocolHandler( fileName ) ) != null )
    	{
			return getContentFromProtocolHandler( fileName, handler );
    	}

        File file = new File( fileName );
        
        boolean isUri = fileName.startsWith( "http://" ) || fileName.startsWith( "file:/" );

        String systemID = isUri ?

		                fileName :
		                fileToURLString( file );

		InputSource source;
        long fileLength = 0;
        
        try
        {
			if ( isUri || file.isAbsolute() )
			{
				source = new InputSource( systemID );
				
				fileLength = isUri ?
				
						getFileLengthFromUrl( fileName ) :
						file.length();
			}
			else
			{
				// this lets us run the sample app out of the jar for local relative files
        	
				URL url = getClass().getClassLoader().getResource( fileName );
				source = new InputSource( url.toString() );
				
				fileLength = getFileLengthFromUrl( url );
			}
		
	/*	
			long fileLength = isUri ?
			
						getFileLengthFromUrl( fileName ) :
					//	getFileLengthFromUrl( url )
				//		file.length();		
			
   */				
        
			m_saxHandler.setFileName( fileName );
			m_saxHandler.setFileSize( file.length() );
                                        
            m_reader.parse( source );            
 			
			m_totalFileLen += fileLength;
			
			++ m_docCount;
           
			if ( isShowFileIndexing() )   
			{
				if ( m_docCount == 1 )
					System.out.println();
					
				System.out.print( m_docCount + " : " + fileName );                                       
				System.out.println( " [doclen=" + fileLength + "]");  
			}			
            
            return m_currDocId;
        }
        catch( SAXException e) { throw e; }
        catch( IOException e )
        {
            throw new SAXException( e );
        }
        catch( NullPointerException npe )
        {
        	
        	throw new SAXException( new java.io.FileNotFoundException( fileName ) );
        }
    }
  
	int parseExplicit( String content, String docName ) 
							throws CantParseDocumentException, MissingOrInvalidSaxParserException
	//-------------------------------------------------------------------------------------------
	{
		if ( m_reader == null )
			throw new MissingOrInvalidSaxParserException( "\nXMLReader not installed" );
        
		if ( content == null )
			throw new CantParseDocumentException( "\nDocument " + docName + " contains null content" );
            
		InputSource source = new InputSource( new StringReader( content ));
        
		++ m_docCount;
        
		int docLen = content.length();
        
		m_saxHandler.setFileName( docName );
		m_saxHandler.setFileSize( docLen );   
        
		if ( isDebugOutputToConsole() )  
		{
			System.out.print( m_docCount + " : " + docName );   
			System.out.println( " [doclen=" + docLen + "]");
		}
     
		m_totalFileLen += docLen;        
        
		try
		{
			m_reader.parse( source );          
			return m_currDocId;
		}
		catch( Exception e ) 
		{ throw new CantParseDocumentException( e.getMessage() ); }
	}  
	 
    long getFileLengthFromUrl( String filename ) throws IOException
    //------------------------------------------
    {
        long len = 0;

        URL url = new URL( filename );
        
        URLConnection conn = url.openConnection();
        len = conn.getContentLength();
        
        conn.getInputStream().close();

        return len;
    }
    
    long getFileLengthFromUrl( URL url ) throws IOException
    //----------------------------------
    {
		long length = 0;
        
		URLConnection conn = url.openConnection();
		length = conn.getContentLength();
        
		conn.getInputStream().close();
		
		return length;
    }

    public static double elapsedTime( long elapsedMillis )
    //----------------------------------------------------
    {
        double duration = elapsedMillis / 1000.0;
        return  ( (int)( 100.0 * duration ) )/ 100.0;
    }
    
	/**
	 * Get a breakdown of node types in the index.
	 * 
	 * @return A 3-entry integer array containing the
	 * node-count breakdowns for the index. In order, counts represent :
	 * <ol>
	 * <li>element nodes
	 * <li>attribute nodes
	 * <li>text nodes
	 * </ol>
	 */
	
    public int[] getNodeTypeCounts()	{ return m_indexer.getNodeTypeCounts(); }
    //------------------------------
    
	/**
	 * Return the aggregate file length of all files in the index.
	 * 
	 * @return The aggregate file length of all files indexed to date
	 */
	
    public long getTotalFileLength()	{ return m_totalFileLen; }
    //------------------------------
    
	/**
	 * Print a summary of indexing information.
	 * 
	 * @param elapsedMillis Elapsed time in milliseconds since the indexing session began.
	 * @param dest {@link PrintWriter} where output should be directed.
	 */
	
	public void printSessionStats( long elapsedMillis, PrintWriter dest )    
	//-------------------------------------------------------------------
	{  
		printSessionStats( elapsedMillis, dest, false ); 
	}
	
	/**
	 * Print a summary of indexing information.
	 * 
	 * @param elapsedMillis Elapsed time in milliseconds since the indexing session began.
	 * @param destination {@link PrintWriter} where output should be directed.
	 * @param doTextBlocks Show space allocated in NodeTrees for concatenated element and attribute text
	 */
	
    public void printSessionStats( long elapsedMillis, PrintWriter destination, boolean doTextBlocks )
    //------------------------------------------------------------------------------------------------
    {
        PrintWriter pw = ( destination == null ) ?  new PrintWriter( System.out ) :
                                                    destination;

        WordManager elementWM = m_indexer.getElementWM();
        WordManager attributeWM = m_indexer.getAttributeWM();

        pw.println( "\nSession Totals" );
        pw.println(   "--------------" );
        
        pw.println( "Aggregate filelength = " + getTotalFileLength() );
		pw.println( "numDocuments         = " + m_indexer.getNumTrees() );
//		pw.println( "------------" );     
        pw.println();

        pw.println( "unique element prefixes   : " + elementWM.getNumPrefixes() );
        pw.println( "unique element localParts : " + elementWM.getNumLocalParts() );

        pw.println();

        pw.println( "unique attr prefixes      : " + attributeWM.getNumPrefixes() );
        pw.println( "unique attr localParts    : " + attributeWM.getNumLocalParts() );

        pw.println();
   
        int[] nodeCounts = getNodeTypeCounts();
        int totalNodes = nodeCounts[ 0 ] + nodeCounts[ 1 ] + nodeCounts[ 2 ];

        pw.println( "element nodes   : " + nodeCounts[ 0 ] );
        pw.println( "attribute nodes : " + nodeCounts[ 1 ] );
        pw.println( "text nodes      : " + nodeCounts[ 2 ] );
        pw.println( "---------------" );
        pw.println( "total nodes     : " + totalNodes );      

		if ( doTextBlocks )
	        printTextBlockSizes( pw );
/*
        pw.println( "\ntext block sizes: " );
        int totalTextSize = 0;

        for ( int i = 0; i < m_indexManager.getNumTrees(); i++ )
        {
            NodeTree tree = m_indexManager.getTree( i );

            int textSize = tree.sizeTextBuffer( NodeTree.E_TEXT );
            totalTextSize += textSize;

            pw.println( "   textSize " + i + " : " + textSize );
        }

        pw.println( "---------------" );
        pw.println( "total size    : " + totalTextSize );
*/
        pw.println( "\nElapsed time =  " + elapsedTime( elapsedMillis ) + " secs\n" );
        
        // need to clarify what's a "word" before I post this information
        
        //pw.println( "Num header words in Dictionary = " + m_indexer.getNumWords() );                
        //pw.println( "Num total words indexed        = " + m_indexer.getNumWordsIndexed() );
        
        pw.println();

        pw.flush();
    }

    void printTextBlockSizes( PrintWriter pw )
    //----------------------------------------
    {
        pw.println( "Text Blocks: " );
        pw.println( "--------------" );
        
        pw.println( "   doc     attr     elem" );
        pw.println( "   ---     ----     -----" );

        int totalElemTextSize   = 0;
        int totalAttrTextSize   = 0;

        for ( int i = 0; i < m_indexer.getNumTrees(); i++ )
        {
            NodeTree tree = m_indexer.getTree( i );

            int elemTextSize    = tree.sizeTextBuffer( NodeTree.ELEM );
            int attrTextSize    = tree.sizeTextBuffer( NodeTree.ATTR );

            totalElemTextSize   += elemTextSize;
            totalAttrTextSize   += attrTextSize;

            String attrPad = "       ".substring( 0, 8 - Integer.toString( attrTextSize ).length() );          
            String elemPad = "       ".substring( 0, 8 - Integer.toString( elemTextSize ).length() );
            
            pw.println( "   [" + i + "] "  + attrPad + attrTextSize + "  " + elemPad + elemTextSize );
        }

        pw.println( "   ----------------------" );
        
        String attrPad = "       ".substring( 0, 8 - Integer.toString( totalAttrTextSize ).length() );          
        String elemPad = "       ".substring( 0, 8 - Integer.toString( totalElemTextSize ).length() ); 
        
        pw.println( " Totals" + attrPad + totalAttrTextSize + "  " + elemPad + totalElemTextSize );
    }

	/**
	 * Utility routine borrowed (thanks) from James Clark.
	 * 
	 * @param file The java File object representation of the file.
	 * @return The name translated to a 'fttp:/ ...' URL in String form
	 */
	
    static public String fileToURLString( File file )
    //-----------------------------------------------
    {
        String path = file.getAbsolutePath();
        String fSep = System.getProperty("file.separator");
        if (fSep != null && fSep.length() == 1)
            path = path.replace(fSep.charAt(0), '/');
        if (path.length() > 0 && path.charAt(0) != '/')
            path = '/' + path;
        try { return new URL("file", null, path).toString(); }
        catch (java.net.MalformedURLException e) {
            throw new Error("unexpected MalformedURLException");
        }
    }   
    
	/**
	 * An alternative form of {@link #setQuery(String)}.
	 * 
	 * @param file Name of the file containing the XQuery query
	 * @return a {@link com.fatdog.xmlEngine.ResultList} object representing the results
	 * @throws InvalidQueryException If the query was invalid
	 */
    
    public ResultList setQueryFromFile( String file ) throws InvalidQueryException
    //-----------------------------------------------
    {
		ResultList results;
        
		try {
			
			XQueryParser parser = new XQueryParser( new FileReader( file ));
			
			SimpleNode root = parser.getXQueryAST();                    
            
			//TreeWalker walker = new TreeWalker( m_indexer );
			TreeWalker walker = new TreeWalker(  );
            
			m_indexer.setTreeWalker( walker );

			return walker.walk( root );
		}
		catch( FileNotFoundException fnf ) {
			throw new InvalidQueryException( "setQueryAgainstFile(): No such file : " + file );
		}		
		catch( ParseException ex ) {
			throw new InvalidQueryException( ex.getMessage() );
		}
		catch( TokenMgrError error ) {
			throw new InvalidQueryException( error.getMessage() );
		}
    }
   
	/**
	 * Pass an XQuery query to the engine, get a {@link com.fatdog.xmlEngine.ResultList} back.
	 * 
	 * @param query A valid XQuery query in String format
	 * @return a {@link ResultList} object
	 * @throws InvalidQueryException
	 */
	
    public ResultList setQuery( String query ) throws InvalidQueryException
    //----------------------------------------
    {
        ResultList results;
        XQueryParser parser = new XQueryParser( new StringReader( query ));
        
        try {
            SimpleNode root = parser.getXQueryAST();                      
       
            //TreeWalker walker = new TreeWalker( m_indexer );
            TreeWalker walker = new TreeWalker();
            
            m_indexer.setTreeWalker( walker );

            return walker.walk( root );
        }
        catch( ParseException ex ) {
            throw new InvalidQueryException( ex.getMessage() );
        }
        catch( TokenMgrError error ) {
            throw new InvalidQueryException( error.getMessage() );
        }
    }    
    
    /**
     * Get the number of documents in the index.
     */
    
    public int getNumDocuments()	{ return m_indexer.getNumDocuments(); }
    //--------------------------
    
    /**
     * Get the name of the document corresponding to its docId.
     * 
     * @param docId The integer ID assigned to the document when it was indexed.
     * @see #setDocument
     * @see #setExplicitDocument
     */
    
    public String getDocumentName( int docId )
    //----------------------------------------
    {
    	return m_indexer.getDocumentName( docId );
    }
    
    // 28may 03 NOTA:   the following routines only return element-related counts.
    //                  they're *not* well named! @@@@

    public int getNumUniqueElementNames()   { return m_indexer.getNumWords(); }
    //-----------------------------------
    public int getNumTotalElementNames()    { return m_indexer.getNumWordsIndexed(); }
    //----------------------------------
    
    protected IProtocolHandler hasRegisteredProtocolHandler( String addressString )
    //-----------------------------------------------------------------------------
    {
    	if ( addressString == null || addressString.startsWith( "http://" ) || addressString.startsWith( "file:/" ))
        {
            return null;         
    	}
            
		for ( java.util.Enumeration e = m_protocolHandlers.keys(); e.hasMoreElements();  ) 
		{
			String scheme = (String) e.nextElement();
			
			if( addressString.startsWith( scheme ) )
			{
				return (IProtocolHandler) m_protocolHandlers.get( scheme );
			}
		}

        return null;    
    }

    void setDocumentId( int docId ) // called back from SaxHandler for each doc indexed
    //-----------------------------
    {
        m_currDocId = docId;
    }
    
    public boolean doSerializeIndex()			{ return m_doSerializeIndex; }
    //-------------------------------
    
    public String getSerializationDirectory()	{ return INDEX_DIR; }
    //---------------------------------------
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