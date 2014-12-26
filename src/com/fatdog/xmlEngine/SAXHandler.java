/*  XQEngine Copyright (C) 2002-2005 Fatdog Software Inc. [howardk@fatdog.com]
 *	--------------------------------------------------------------------------
 *  This file is part of XQEngine and is distributed under the terms of the
 *  GNU General Public License. See additional info at the end of this file. */
 
package com.fatdog.xmlEngine;

import java.io.*;
import java.util.*;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ContentHandler;
import com.fatdog.xmlEngine.words.*;

	/**
	 * A SAX2 <code>ContentHandler</code> for parsing XML documents.
	 * 
	 * @author Howard Katz, howardk@fatdog.com
	 * @version 0.69
	 */

public class SAXHandler implements ContentHandler
{
    IndexManager    m_indexer;

    WordManager     m_elementWM;
    WordManager     m_attributeWM;

    NodeTree        m_nodeTree;

    int             m_nodeIx;

    Stack           m_tagStack;
    Vector			m_vNamespaces;

    String			m_fileName;
    long			m_fileSize;

    StringBuffer    m_elemTextBuffer;

    char[]          m_buff  = new char[ 0 ];
    int             m_numChars;
    
    int             m_numSingleNewlines;
    
    boolean			m_isWordIndexing;
    IntList			m_thisDocIdList;
        

    public SAXHandler( IndexManager indexer )
    //---------------------------------------
    {
        m_elementWM     = indexer.getElementWM();
        m_attributeWM   = indexer.getAttributeWM();
        
        m_indexer       = indexer;
        
		m_isWordIndexing = indexer.getEngine().isWordIndexing();
		if ( m_isWordIndexing )
		{
			m_thisDocIdList = new IntList(1);
		}
    }

    public void startDocument()
    //-------------------------
    {
        m_nodeIx        = -1;

        m_tagStack = new Stack();
        m_tagStack.push( new Long( (long) -1 << 4*8 | NodeTree.NO_PRIOR_SIBS ) );
                
        // -1 for both nodeIx && lastSibling

        // an extremely simple heuristic to help manage memory allocation. Note
        // we'll overallocate if we have a large file w/ few elements and attributes.

        int initialEntries = (int) m_fileSize / 100;
                                                                // ?????????????
        m_nodeTree = new NodeTree( m_indexer, initialEntries, com.fatdog.xmlEngine.XQEngine.AGGRESSIVE );
        
        int docId = m_indexer.addTree( m_nodeTree, m_fileName ); 
        
        m_elemTextBuffer = m_nodeTree.getElemTextBuffer();          
        m_elementWM.startDocument( m_nodeTree ); 		// only for debugging
        
        m_indexer.getEngine().setDocumentId( docId );   // make the id available to set(Explicit)Document
        												// (can't it get it from IndexManager ???)
        												
        if ( m_isWordIndexing )	// we'll pass this on to characters() -> WM.word() -> dictionary xref
        {
        	m_thisDocIdList.addRef_1( docId );
        }
        
		m_vNamespaces = new Vector();
    }

    public void endDocument() throws SAXException
    //-----------------------
    {
        m_tagStack  = null;

        m_nodeTree.setElemTextBuffer( m_elemTextBuffer );
        m_nodeTree.trim();
        
        m_indexer.endDocument( m_nodeTree );
        
		m_nodeTree = null;
        
        m_elementWM.stopDocument();  
    }
    
    void addTextNode()
    //----------------
    {
        long both = ((Long)m_tagStack.pop() ).longValue();
        
        int parent      = (int) ( both >>> 4*8 );
        int priorSib    = (int) ( both & 0xFFFFFFFF );    
        
        m_tagStack.push( new Long( (long) parent << 4*8 | ++ m_nodeIx ));
        
        m_nodeTree.addElementTextNode( m_buff, 0, m_numChars, parent, priorSib );
               
        // let the elementWM do the hard lifing (tho it doesn't do much at present)
		if ( m_isWordIndexing )
		{
        	m_elementWM.characters( m_buff, 0, m_numChars, m_nodeIx, m_thisDocIdList );
		}
        
		m_numChars = 0;
		m_numSingleNewlines = 0;
    }
    
    final static int URI_STRING = 1;
    
    public int namespaceToId( String nsUri )
    //--------------------------------------
    {
    	for( int i = 0; i < m_vNamespaces.size(); i++ )
    	{
    		Object[] entry = (Object[]) m_vNamespaces.elementAt( i );
    		
    		String uri = (String) entry[ URI_STRING ];
    		if ( nsUri.equals( uri ) )
    		{
    			return i;
    		}
    		
    	}
    	
    	return -1;
    }
    
    protected int getNamespaceIndex( String qName, String namespaceURI )
    //------------------------------------------------------------------
    {
    	int namespaceIx = -1;
    	
		if ( ! namespaceURI.equals( "" ) )
		{
			namespaceIx = namespaceToId( namespaceURI );
    		
			if ( namespaceIx == -1 ) // s'new, add it
			{
				Object[] nsParams = new Object [ 2 ];
    			
				String prefix = null;
    		
				int colonIx = qName.indexOf( ':' );
				if ( colonIx != -1 )
				{
					prefix = qName.substring( 0, colonIx );
				}
    			
				nsParams[ 0 ] = new Integer( namespaceURI.hashCode() );
				nsParams[ 1 ] = namespaceURI;
    			
				m_vNamespaces.addElement( nsParams );
    			
				namespaceIx = m_vNamespaces.size() - 1;
			}
		}
		
    	return namespaceIx;
    }
    
    public void startElement(	String namespaceURI, String localName,
                                String qName, Attributes attrs ) throws SAXException
    //----------------------------------------------------------
    {
    //	get the namespace index, adding the namespace if it's new
    
    	int namespaceIx = getNamespaceIndex( qName, namespaceURI );
    	
    	// namespaceIx at this point is either -1 (no namespace on this element)
    	// or an integer (the ix of the namespace that's in effect)
    	
        if ( m_numChars > 1 || ( m_numChars == 1 && m_buff[ 0 ] != '\n' ))
            
            addTextNode();
            
        int element = ++ m_nodeIx;
        
        long both = (( Long) m_tagStack.pop() ).longValue();
        
        // we're only popping here to get at the parental-level sibling info, and immediately 
        // push again with *this* as sibling. we might consider using a separate siblingStack 
        // to track this relationship, as it might be easier to understand. It's at the same 
        // level as our parent.
        
        int parent      = (int) ( both >>> 4*8 );
        int priorSib    = (int) ( both & 0xFFFFFFFF );     
        
        both = (long) parent << 4*8 | element;
        m_tagStack.push( new Long( both ));     // push the parent/sibling stack

        int[] nameKeys = m_elementWM.addEntry( localName, qName );
        
        m_nodeTree.addElementNode( namespaceIx, nameKeys, parent, priorSib );
        
        both = (long) element << 4*8 | NodeTree.NO_PRIOR_SIBS;
        m_tagStack.push( new Long( both ) );    // push a new one for *this* stack frame
        
        for( short i = 0; i < attrs.getLength(); i++ )
        {
            ++ m_nodeIx;
            
            String attrQName = attrs.getQName( i );
			nameKeys = m_attributeWM.addEntry( attrs.getLocalName( i ), attrQName );
            
            String words = attrs.getValue( i );
            
            if ( m_isWordIndexing )
				m_attributeWM.characters( words.toCharArray(), 0, words.length(), m_nodeIx, m_thisDocIdList );    
			
			int attrNamespaceIx = getNamespaceIndex( attrQName, attrs.getURI( i ) );
			
            m_nodeTree.addAttributeNode( attrNamespaceIx, nameKeys, words, element );
        }
        
        if ( m_numChars == 1 && m_buff[ 0 ] == '\n' )
        {
            m_nodeTree.singleNewline( element, true );
            m_numChars = 0;
            
            m_numSingleNewlines = 1;
        }
    }

    public void endElement( String namespaceURI,
                            String localName, String qName ) throws SAXException
    //------------------------------------------------------
    {
        if ( m_numChars > 1 || ( m_numChars == 1 && m_buff[ 0 ] != '\n' ))
            
            addTextNode();
            
        long both   = ((Long) m_tagStack.pop() ).longValue();
        int parent  = (int)( both >>> 4*8 );
            
        if ( m_numChars == 1 && m_buff[ 0 ] == '\n' )
        {
            m_nodeTree.singleNewline( parent, false );
            m_numChars = 0;
            
            m_numSingleNewlines = 1;    
        }
    }

    public void ignorableWhitespace( char[] cbuf, int start, int len ) throws SAXException
    //----------------------------------------------------------------
    {
        characters( cbuf, start, len );
    }

    // accumulate everythng until the next startElement() or endElement()

    public void characters( char[] cbuf, int start, int len ) throws SAXException
    //-------------------------------------------------------
    {
        int currlen = m_buff.length;

        if ( len != 0 )
        {
            int needed = m_numChars + len;
            if ( needed >= currlen )
            {
                int newlen = 2 * currlen;
                if ( needed > newlen )
                    newlen = needed;

                char[] newBuff = new char[ newlen ];
                System.arraycopy( m_buff, 0, newBuff, 0, currlen );
                m_buff = newBuff; newBuff = null;
            }

             System.arraycopy( cbuf, start, m_buff, m_numChars, len );
             m_numChars += len;
        }
    }

    public void skippedEntity( String name ) throws SAXException {}
    //--------------------------------------
    public void startPrefixMapping( String prefix, String URI ) throws SAXException {}
    //---------------------------------------------------------
    public void endPrefixMapping( String prefix ) throws SAXException {}
    //-------------------------------------------
    public void processingInstruction( String target, String data ) {}
    //-------------------------------------------------------------
    public void setDocumentLocator( org.xml.sax.Locator locator ) {}
    //----------------------------------------------------------

    // XQEngine.parse() passes in both just before it parses
    
    public void setFileName( String fileName )  { m_fileName = fileName; }
    //----------------------------------------
    public void setFileSize( long size )        { m_fileSize = size; }
    //----------------------------------
    
    public InputSource resolveEntity(   java.lang.String publicId, 
                                        java.lang.String systemId ) throws SAXException
    //-------------------------------------------------------------
    {
        System.out.println( "resolveEntity(): systemId = " + systemId );
        return null;
    }
    
    void debugDumpNodeTreeToFile( String file )
    //-----------------------------------------
    {
        System.out.println( "dumping current NodeTree to file: " + file );
        
        try 
        {
            PrintWriter pw = new PrintWriter( new FileWriter( file ) );
            pw.write( m_nodeTree.toString() ); pw.close();
        }
        catch( IOException ioe ) 
        { 
            System.out.println( "couldn't write \"" + file + "\" to disk" ); }
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