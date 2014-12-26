/*  XQEngine Copyright (C) 2002-2005 Fatdog Software Inc. [howardk@fatdog.com]
 *	--------------------------------------------------------------------------
 *  This file is part of XQEngine and is distributed under the terms of the
 *  GNU General Public License. See additional info at the end of this file. */
 
package com.fatdog.xmlEngine;

import com.fatdog.xmlEngine.Sizes;
import com.fatdog.xmlEngine.words.*;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.io.DataOutputStream;
import java.io.IOException;

/*          base +        0            1           2         3
                         int
     type              3     1        int         int       int
     ----           ------- ----    --------    --------   ------
    ELEMENTS :      nextSib ELEM    prefixKey   localKey   parent
    ATTRIBUTES :    attrIx  ATTR    prefixKey   localKey   parent
    TEXT :                  TEXT    fromChar    toChar     parent
    RESULT_LIST          RESULTS    listIx
  
    attrIx :        	int            int
                      --------       -------
                      fromChar       toChar        */           

	/**
	 * A dynamic array structure for storing a collection of nodes from a single indexed document.
	 * 
	 * @author Howard Katz, howardk@fatdog.com
	 * @version 0.69
	 */
	
public class NodeTree
{
	final static boolean DEBUGGING	= false;
	
	public static int NS_SHIFT;	// we're comparing against namespace indexes, not prefix keys (==8)
								// (see XQEngine.getUseLexicalPrefixes())
	
	// added 3jan05 to fix bug w/ previous NS_SHIFT
	// 5jan05 removed again and replace SHIFT amount as 16. That is, entry[ 1 ] for the prefix is
	//		namespace index	 |   prefixKey
	//			0		1		2		3
	// (short) entry[ 1 ] >> 16 	brings the namespace index into view
	// (short) entry[ 1 ] >> 0		exposes just the prefix key
	public static int NS_MASK;
	
	// these are kept in the tree
	// note that DOC_NODE overlaps same usage in DocItems (should I remove the latter??)
	
	public final static int		DOC_NODE	= -1;
    public final static int     ELEM        = 0;
    public final static int     ATTR        = 1;
    public final static int     TEXT        = 2;
    
    // QueryDocItems has EVALED_RESULTS = 3;
    
    public final static int     ALL_NODES   = 9;	// any number not in above list
    
    // offsets into m_tree
    public final static int     ATEXT_IX_TYPE       = 0;
    public final static int     TYPE                = 0;
    public final static int     PREFIX_KEY          = 1;
    public final static int     LOCAL_KEY           = 2;
    public final static int     E_TEXT_FROM_CHAR    = 1;
    public final static int     E_TEXT_TO_CHAR      = 2;
    public final static int     PARENT              = 3;
    
// TODO: review use of this flag. see also DocItems
// removed 10feb04 in favour of 'prettyPrint' flag passed through emitXml() interfaces

//	final static boolean LINEFEED_AFTER_INDIVIDUAL_NODES = false;
    
    final static int    NO_PRIOR_SIBS       = 0;    
    final static int    NO_FURTHER_SIBS     = 0;
    
    public final static int     VOIDED_NODE                 = -999999;

    public final static int     INTS_PER_ENTRY             = 4;
    final static int            INTS_PER_ATTR_TEXT_ENTRY   = 2;
    
    int             m_id;   // outside identifier for this document/nodeTree
    
    int             m_numInitialEntries;
    int             m_numInitialAttrTextEntries;    

    IndexManager    m_indexManager;

    int             m_allocationPolicy;

    int[]           m_tree;
    int[]           m_attrTextTree;         // added separate tree 28dec02
    										// (not really a tree; how about a better name ???)
    int             m_numNodes;         
    int             m_numNodesAttrText;    
    
    int             m_ix;                   // number of *slots* in m_tree array
    int             m_ixAttrText;           // (ie m_numNodes * INTS_PER_ENTRY)

    StringBuffer    m_elemTextBuffer;
    StringBuffer    m_attrTextBuffer;

    int             m_textStart             = 0;
    int             m_attrTextStart			= 0;
    
    Hashtable       m_wordXrefs;	// experimental; not currently used
    
	WordManager m_elementWM;	//
	WordManager m_attributeWM;
    
    
    public NodeTree( IndexManager indexManager, int numInitialEntries, int allocationPolicy )
    //---------------------------------------------------------------------------------------
    {
        m_indexManager = indexManager;

        // make sure it's an even multiple
        
        m_numInitialEntries = ( (int)( numInitialEntries / INTS_PER_ENTRY )) * INTS_PER_ENTRY * INTS_PER_ENTRY;
        if ( m_numInitialEntries < INTS_PER_ENTRY )
            m_numInitialEntries = INTS_PER_ENTRY;       
           
        m_numInitialAttrTextEntries = m_numInitialEntries / 2;  // FOR NOW AT ANY RATE  
        m_tree              = new int[ m_numInitialEntries ];       
        m_attrTextTree      = new int[ m_numInitialAttrTextEntries ];  
            
        m_allocationPolicy  = allocationPolicy;
        m_numNodes          = 0;
        m_numNodesAttrText  = 0;
        
        m_elemTextBuffer    = new StringBuffer();
        m_attrTextBuffer    = new StringBuffer();
        
        m_wordXrefs         = new Hashtable();
        
        if ( getIndexManager().getEngine().isDistributedQNameDictionaries() )
        {
			// numbers in ctor are just initial hints for (1) prefixes (2) localParts, and (3) words
			m_elementWM     = new WordManager( m_indexManager, 3, 51, 0 );
			m_elementWM.registerWordHandler( m_elementWM, null );
	        
			m_attributeWM   = new WordManager( m_indexManager, 3, 29, 0 ); // new WordManager( 5, 51, 1001 );
			m_attributeWM.registerWordHandler( m_attributeWM, null );        
        }
        
        NS_SHIFT = indexManager.getEngine().getUseLexicalPrefixes() ? 0 : 16;
        
        // NEW 3jan05
        NS_MASK = indexManager.getEngine().getUseLexicalPrefixes() ? 0x0000FFFF : 0xFFFF0000;
    }

	public IndexManager getIndexManager()	{ return m_indexManager; }
	//-----------------------------------
	
    public void setId( int id )     { m_id = id; }
    //-------------------------
    public int getId()              { return m_id; }
    //----------------
    public int getType( int i ) 
    //-------------------------
	{ 
		if ( i == DOC_NODE )
		{
			return DOC_NODE;
		}
		
		return m_tree[ INTS_PER_ENTRY * i ] & 0x0000000F; 
	}
    public int type( int i )   		{ return m_tree[ i ] & 0x0000000F; }
    //---------------
    int getSibling( int node )      { return ( m_tree[ INTS_PER_ENTRY * node ] & 0x7FFFFFFF ) >> 8; }
    //------------------------
    int sibling( int base )         { return ( m_tree[ base ] & 0x7FFFFFFF ) >> 8; }
    //---------------------

	// 16dec03
	// getKeys() now gets the namespaceIx + localPart key for NodeTree searching for QName (ie namespaceURI) matching
	// getNameKeys() is used specifically to return the lexical prefix key to regenerate a QName for serialization.
	
    public int[] getKeys( int node )
    //------------------------------
    {
        int[] key = { -1, -1 };
        
        int entry = INTS_PER_ENTRY * node;
        
        key[ 0 ] = (byte) m_tree[ entry + 1 ]; // & 0xFF;	// uriIx stored in low byte
        key[ 1 ] = m_tree[ entry + 2 ];
        
        return key;
    }    
    
	// 16dec03 NEW NEW NEW
	
	public int[] getNameKeys_base( int base )
	//---------------------------------------
	{
		int[] key = { -1, -1 };
	/*
		key[ 0 ] = (byte) ( m_tree[ base ++ ] >> 8 ); // & 0xFF;
		if ( key[ 0 ] == 255 )
			key[ 0 ] = -1;
	*/
		// new 4jan05 -- but wrong again. we -do- need to shift, not mask
	/*
		key[ 0 ] = m_tree[ ++ base ] & 0x0000FFFF;	
		if ( key[ 0 ] == 0xFFFF )
			key[ 0 ] = -1;
    */
		// we're only getting dictionary keys for the prefix, not
		// the namespace key that's also concatenated in this int
		// (because we're emitting the prefix string, not the namespace url

		key[ 0 ] = (short) ( m_tree[ ++ base ] & 0x0000FFFF );	
		key[ 1 ] = m_tree[ ++ base ];
        
		return key;
	}    
	
    // 16dec03 NEW NEW NEW
    
	public int[] getNameKeys( int node )
	//----------------------------------
	{
		int[] key = { -1, -1 };
        
		int entry = INTS_PER_ENTRY * node;
        
//		key[ 0 ] = (byte) ( m_tree[ entry + 1 ] >> 8 ); // & 0xFF;
		
		// new 4jan05
		key[ 0 ] = m_tree[ ++ entry ] & 0x0000FFFF;	
		if ( key[ 0 ] == 0xFFFF )
			key[ 0 ] = -1;
		
//		if ( key[ 0 ] == 255 )
//			key[ 0 ] = -1;
			
		key[ 1 ] = m_tree[ ++ entry ];
        
		return key;
	}    
	
	public int getNamespaceIndex( int node )
	//--------------------------------------
	{
		byte namespaceIx = (byte) (m_tree[ INTS_PER_ENTRY * node + PREFIX_KEY ] ); // & 0xFF);
		return namespaceIx; // m_tree[ INTS_PER_ENTRY * node + PREFIX_KEY ] & 0xFF;
	}
	
    public short getPrefixKey( int node )
    //---------------------------------
    {
        return (short) ( m_tree[ INTS_PER_ENTRY * node + PREFIX_KEY ] & 0x0000FFFF );
    }
    
    public int getLocalKey( int node )
    //--------------------------------
    {
        return m_tree[ INTS_PER_ENTRY * node + LOCAL_KEY ];
    }
        
    public int getParent( int i )   		{ return m_tree[ INTS_PER_ENTRY * i + PARENT ]; }
    //--------------------------- 
    public int getParent_BASE( int base )   { return m_tree[ base + PARENT ]; }
    //-----------------------------------
    
    public boolean isFirstSibling_BASE( int base )	{ return m_tree[ base ] < 0; }
    //--------------------------------------------

    public boolean isLastSibling( int node ) { return ( m_tree[ INTS_PER_ENTRY * node ] & 0x7FFFFFFF ) >> 8 == NO_FURTHER_SIBS; }
    //--------------------------------------
    
    boolean hasNewlineAtStart( int node )  	{ return ( m_tree[ INTS_PER_ENTRY * node ] & 0x00000020 ) == 0x0020; }
    //-----------------------------------
    boolean hasNewlineAtEnd( int node )		{ return ( m_tree[ INTS_PER_ENTRY * node ] & 0x00000010 ) == 0x0010; }
    //---------------------------------
    
    int getAttrTextNodeIx( int node )   	{ return m_tree[ INTS_PER_ENTRY * node ] >>> 8; }
    //----------------------------------
	int attrTextNodeIx( int base )     		{ return m_tree[ base ] >>> 8; }
	//----------------------------
    
    public String getAttributeName( int nodeID )    { return m_indexManager.getAttributeWM().getQName( getNameKeys( nodeID ) ); }
    //------------------------------------------
    public String getElementName( int nodeID )      { return m_indexManager.getElementWM().getQName( getNameKeys( nodeID ) ); }
    //----------------------------------------
   
    public int[] getRawTree()   { return m_tree; }
    //-----------------------
    
    void checkAllocation()
    //--------------------
    {
        int len = m_tree.length;

        if ( m_ix >= len )
        {
            int[] newTree = new int[ m_allocationPolicy == com.fatdog.xmlEngine.XQEngine.AGGRESSIVE ?

	                            2 * len :
	                            len + m_numInitialEntries ];

            System.arraycopy( m_tree, 0, newTree, 0, len );
            
            m_tree = newTree; newTree = null;
        }
    }
    
    void checkAttrTextAllocation()
    //----------------------------
    {
        int len = m_attrTextTree.length;

        if ( m_ixAttrText >= len )
        {
            int[] newTree = new int[ m_allocationPolicy == com.fatdog.xmlEngine.XQEngine.AGGRESSIVE ?

                                    2 * len :
                                    len + m_numInitialAttrTextEntries ];

            System.arraycopy( m_attrTextTree, 0, newTree, 0, len );
            
            m_attrTextTree = newTree; newTree = null;
        }
    }    

    public void addElementNode( int namespaceIx, int[] key, int parent, int priorSibling )
    //------------------------------------------------------------------------------------
    {
        checkAllocation();
    
        int ix = m_ix;
        
        m_tree[ ix ] = ELEM;
            
        if ( priorSibling == NO_PRIOR_SIBS )
            m_tree[ ix ] |= 0x80000000;   // mark as first in sib chain
        else 
            m_tree[ priorSibling * INTS_PER_ENTRY ] |= ( m_numNodes << 8 );

        ++ ix;
        
//        m_tree[ ix ++ ]    = nameKeys[ 0 ] << 8 | namespaceIx;   
        
//        int shiftedNamespace = namespaceIx << 16;
//        int prefix = 0x0000FFFF & key[ 0 ];
        
        int concat = ( namespaceIx << 16 ) | ( 0x0000FFFF & key[ 0 ] );
        m_tree[ ix ++ ]    = concat;
        
        m_tree[ ix ++ ]    = key[ 1 ];
        m_tree[ ix ++ ]    = parent;

        m_ix = ix;
        
        ++ m_numNodes;        
    }

	public void addAttributeNode( int namespaceIx, int[] key, String text, int parent )
	//----------------------------------------------------------------------------------
	{
		checkAllocation();
		
		int ix = m_ix;

		m_tree[ ix ++ ]        = ATTR | ( m_ixAttrText << 8 );
		
		//m_tree[ m_ix + 1 ]    = key[ 0 ] << 8 | namespaceIx;
		
        int concat = ( namespaceIx << 16 ) | ( 0x0000FFFF & key[ 0 ] );
        m_tree[ ix ++ ]  	= concat;
		
		m_tree[ ix ++ ]	= key[ 1 ];
		m_tree[ ix ++ ] 	= parent;

		++ m_numNodes;
		
	//	ix += INTS_PER_ENTRY;
		
		m_ix = ix;

		checkAttrTextAllocation();
		
		int end = m_attrTextStart + text.length();	// we're including an extra prepended space
													// (for now; we might change this later)

		m_attrTextTree[ m_ixAttrText ]      = m_attrTextStart + 1;
		m_attrTextTree[ m_ixAttrText + 1 ]  = end;

		m_attrTextStart = ++ end;

		m_attrTextBuffer.append( ' ' );			
		m_attrTextBuffer.append( text );

		m_ixAttrText += INTS_PER_ATTR_TEXT_ENTRY;
	}
    
    public int addElementTextNode( char[] buff, int start, int numChars, int parent, int priorSibling )
    //-------------------------------------------------------------------------------------------------
    {
        checkAllocation();
                  
        m_tree[ m_ix ] = TEXT;
        
        if ( priorSibling == NO_PRIOR_SIBS )
            m_tree[ m_ix ] |= 0x80000000;   // mark as first in chain
        else 
            m_tree[ priorSibling * INTS_PER_ENTRY ] |= ( (long) m_numNodes << 8 );
            
        int nodeId = m_numNodes ++;
        
		boolean prependSpace = false;       
		if ( m_textStart > 0 )
		{
			// did the last bufferfull end in a character or letter
        	
			if ( Character.isLetterOrDigit( m_elemTextBuffer.charAt( m_textStart - 1 ) ) )
				if ( Character.isLetterOrDigit( buff[ start ] ) )
				{
					prependSpace = true;		
					++ m_textStart;
				}			
		}
        
        int end = m_textStart + numChars - 1;       
               
        m_tree[ m_ix + 1 ]    = m_textStart;
        m_tree[ m_ix + 2 ]    = end;
        m_tree[ m_ix + 3 ]    = parent;       
        
        m_textStart = ++ end;
        
        //String debugStr = new String( buff, start, numChars );

		if ( prependSpace )
		{
			m_elemTextBuffer.append( ' ' );
		}
		
        m_elemTextBuffer.append( buff, start, numChars );

        m_ix += INTS_PER_ENTRY;
        
        return nodeId;
    }

	public void singleNewline( int node, boolean atStart )
	//----------------------------------------------------
	{
		m_tree[ INTS_PER_ENTRY * node ] |= atStart ? 0x20 : 0x10;
        
		m_elemTextBuffer.append( '\n' );
        
		++ m_textStart;
        
		//System.out.print( "Single CR before " );
		//System.out.println( ( atStart ? "start Tag <" : "end Tag </" ) + node + ">" );
	}
	
	public int getLastAddedNode()	{ return m_numNodes - 1; }
	//---------------------------
	
    public int getNodeCount()		{ return m_numNodes; }
    //-----------------------

    public int[] countNodeTypes()
    //---------------------------
    {
        int[] count = new int[ 3 ];

        for( int i = 0; i < INTS_PER_ENTRY * m_numNodes; i += INTS_PER_ENTRY )
        {
            switch( type( i ) )
            {
                case ELEM : count[ ELEM ] ++; break;
                case ATTR : count[ ATTR ] ++; break;
                case TEXT : count[ TEXT ] ++; break;
            }
        }

        return count;
    }

    public StringBuffer getElemTextBuffer()     { return m_elemTextBuffer; }
    //-------------------------------------
    public StringBuffer getAttrTextBuffer()     { return m_attrTextBuffer; }
    
    //-------------------------------------
    public void setElemTextBuffer( StringBuffer textBuffer )    { m_elemTextBuffer = textBuffer; }
    //------------------------------------------------------
    
    public int sizeTextBuffer( int bufferType )
    //-----------------------------------------
    {
        if ( bufferType != ELEM && bufferType != ATTR )
            throw new IllegalArgumentException( 
            	"NodeTree.sizeTextBuffer(): unknown text buffer type: " + bufferType );
                    
        return ( bufferType == ELEM ) ?

                m_elemTextBuffer.length() :
                m_attrTextBuffer.length();   
    }

    public void printNode( int level, int base, int type, StringBuffer sb )
    //---------------------------------------------------------------------
    {
        int SPACE_PER_LEVEL = 4;
        
        for( int i = 0; i < SPACE_PER_LEVEL * level; i++ )
            sb.append( " " );
            
        printNode( base, type, sb );
    }
    
    public void printNode( int base, int type, StringBuffer sb )
    //----------------------------------------------------------
    {
        WordManager wm  = null;
        
        int prefixKey   = -99999;
        int localKey    = -99999;
        int[] nameKeys	= null;

        int parent = m_tree[ base + PARENT ];                
        
        if ( type == ELEM || type == QueryDocumentTree. ELEMENT_CTOR || type == ATTR )
        {
            wm = type == ATTR ?	m_indexManager.getAttributeWM() :
            					m_indexManager.getElementWM();
                          
            nameKeys = getNameKeys_base( base );
        }
        
        switch( type )
        {
            case ELEM : 
            case QueryDocumentTree. ELEMENT_CTOR	:
            
            			    sb.append( " <" ); 
                            sb.append( wm.getQName( nameKeys ));  sb.append( ">" );
                            sb.append( " [parent=" ); sb.append( parent ); sb.append( "] " );
                            
                            int nextSib = sibling( base );
                            if ( nextSib != NO_FURTHER_SIBS )
                            {
                                sb.append( "[nextSib=" ); sb.append( nextSib ); sb.append( "]" );
                            }
                            
                            if ( type == QueryDocumentTree. ELEMENT_CTOR )
                            {
                            	sb.append( " [ELEMENT_CTOR]");
                            }
                            
                            sb.append( "\n" );
                            break;
                            
            case ATTR :     String attrValue = attributeText( base );
            
            				sb.append( " @" ); 
                            sb.append( wm.getQName( nameKeys ));
                            
                            sb.append( "=");
                            
                            // NOTA: at present all attribute values in the text buffer
                            // are preceded by a space to enable proper word-breaking on a search
							// attributeText() and getAttributeText() have already compensated
                                                  
                            sb.append( "\"" + attrValue + "\"" );                           
                            sb.append( " [parent=" ); sb.append( parent ); sb.append( "]\n" );                 
                            
                            break;

            case TEXT :     int fromChar    = m_tree[ base + 1 ];
                            int toChar      = m_tree[ base + 2 ];

                            sb.append( " TEXT" );
                            sb.append( " [parent=" + parent + "]" );  
                            sb.append( " [len=" + ( toChar - fromChar + 1 ) ); sb.append( "]" );                        
                            sb.append( " [fromOff=" + fromChar + "] [toOff=" + toChar + "]" );                             
                            
                            nextSib = sibling( base );
                            if ( nextSib != NO_FURTHER_SIBS )
                            {
                                sb.append( " [nextSib=" ); sb.append( nextSib ); sb.append( "]" );
                            }
                        /*
                            int MAXLEN = 12;
                            String text = getElementText( base / 4 );
                            int len  = text.length() > MAXLEN ? MAXLEN : text.length();
                            text = text.substring( 0, len ).replace( '\n', '^' );;
                            System.out.print( "\"" + text + "\"" );    
                        */                           
                            sb.append( '\n' );
                            break; 
                            
			case QueryDocumentTree.RESERVED :
			
							sb.append( " RESERVED" );
							sb.append( '\n');
							break;
						
			case QueryDocumentTree.ENCLOSED_RESULTS :	
							
							ResultList results = ((QueryDocumentTree) this).getEnclosedResults( base / INTS_PER_ENTRY );
										
							sb.append( " ENCLOSED_RESULTS" );
							sb.append( " [parent=" + parent + "]" );
							sb.append( " [numItems=" + results.getNumValidItems() + "]" );
							sb.append( '\n');	
							break;
                                                      
            default :
				
				
					throw new IllegalArgumentException( "\nNodeTree.printNode(): unknown nodetype " + type );
        }          
    }

    public void trim()
    //----------------
    {
        int[] newTree = new int[ m_ix ];
        System.arraycopy( m_tree, 0, newTree, 0, m_ix );
        m_tree = newTree; newTree = null;
        
        int[] newAttrTree   = new int[ m_ixAttrText ];
        System.arraycopy( m_attrTextTree, 0, newAttrTree, 0, m_ixAttrText );
        m_attrTextTree = newAttrTree; newAttrTree = null;        

        m_elemTextBuffer.setLength( m_elemTextBuffer.length() );
        m_attrTextBuffer.setLength( m_attrTextBuffer.length() );
    }

	public String attributeText( int base )
	//-------------------------------------
	{
		if ( type( base ) != NodeTree. ATTR )
			throw new java.lang.IllegalArgumentException(
				"\nNodeTree.getAttributeText(): invalid attribute node ID: " + ( base * INTS_PER_ENTRY ) );

		int attrIx = attrTextNodeIx( base );

		int len = m_attrTextTree[ attrIx + 1 ] - m_attrTextTree[ attrIx ] + 1;
		char[] chars = new char[ len ];

		if ( len > 0 ) // attr value can be empty
			m_attrTextBuffer.getChars( m_attrTextTree[ attrIx ], m_attrTextTree[ attrIx + 1 ] + 1, chars, 0 );

		return new String( chars );	
	}
	
    public String getAttributeText( int attrNode )
    //--------------------------------------------
    {
        if ( getType( attrNode ) != NodeTree. ATTR )
            throw new java.lang.IllegalArgumentException(
                "\nNodeTree.getAttributeText(): invalid attribute node ID: " + attrNode );

        int attrIx = getAttrTextNodeIx( attrNode );

        int len = m_attrTextTree[ attrIx + 1 ] - m_attrTextTree[ attrIx ] + 1;
        char[] chars = new char[ len ];

        if ( len > 0 ) // attr value can be empty
            m_attrTextBuffer.getChars( m_attrTextTree[ attrIx ], m_attrTextTree[ attrIx + 1 ] + 1, chars, 0 );

        return new String( chars );
    }
    
    public int numAttributesOnElement( int elementNodeIx )
    //----------------------------------------------------
    {
        int attribCount = 0;        
        int nextNode    = elementNodeIx + 1;
        
        while( nextNode < getNodeCount() && getType( nextNode ) == ATTR )
        {
            ++ attribCount;
            ++ nextNode;
        }
        
        return attribCount;
    }
 
    public boolean elementHasContent( int elementNodeIx )
    //---------------------------------------------------
    {
        // skip to first node past attributes
        
        int possibleChild = elementNodeIx + numAttributesOnElement( elementNodeIx ) + 1;
        
        boolean hasContent = ( possibleChild < getNodeCount() 
                                    && getParent( possibleChild ) == elementNodeIx );
            
        // extra double-check for singleCR contents possibility    
        
        if ( ! hasContent )
            hasContent = hasNewlineAtEnd( elementNodeIx );
            
        return hasContent;
    }
    
    public String getElementText( int node )
    //-------------------------------------
    {
        int entry = INTS_PER_ENTRY * node;
        
        if ( type( entry ) != TEXT )
            throw new java.lang.IllegalArgumentException( "\ngetElementText() argument (" + node + ") is not type TEXT" );
            
        char[] text = new char[ m_tree[ entry + 2 ] - m_tree[ entry + 1 ] + 1 ];
        
        m_elemTextBuffer.getChars( m_tree[ entry + 1 ], m_tree[ entry + 2 ] + 1, text, 0 );
        
        return new String( text );
    }    
   
    public int namedAncestor( int[] ancestKey, int nodeType, IntList descendList )
    //----------------------------------------------------------------------------
    {
        int[]   tree            = m_tree;
        int     numInvalidNodes = 0;        
                
    nextNode :                
        for( int i = 0; i < descendList.count(); i ++ )
        {
            int child = descendList.getRef_2( i );
            if ( child == VOIDED_NODE )
                continue;

            int parent;
            while ( ( parent = tree[ INTS_PER_ENTRY * child + PARENT ] ) != -1 )
            {
                int parentBase = INTS_PER_ENTRY * parent;
            
              if ( ancestKey[ 0 ] == WordManager.WILDCARD || (short)( tree[ parentBase + 1 ] >> NS_SHIFT ) == ancestKey[ 0 ] )
			//	if ( ancestKey[ 0 ] == WordManager.WILDCARD || ( tree[ parentBase + 1 ] & NS_MASK ) == ancestKey[ 0 ] )
                    if ( ancestKey[ 1 ] == WordManager. WILDCARD || tree[ parentBase + 2 ] == ancestKey[ 1 ] )
                    {
                        descendList.setRef_2( i, parent ); 
                        continue nextNode;
                    }
                    
                    child = parent;
            }
            
            descendList.setRef_2( i, VOIDED_NODE );
            ++ numInvalidNodes;
        }
        
        return numInvalidNodes;
    }

    int firstElementChild( int parent )
    //---------------------------------
    {      
        int currNode = parent;
        while( ++ currNode < m_numNodes && getType( currNode ) != ELEM ) 
        ;

        if ( currNode > m_numNodes - 1 || getParent( currNode ) != parent )
            return -1;

        return currNode;
    }
    
    void appendNamedAttributes( int parent, int[] key, IntList list )
    //-----------------=---------------------------------------------
    {
    	int attribute = parent + 1;
    	
		while ( attribute < m_numNodes && getType( attribute ) == NodeTree. ATTR )
		{
			int[] attrKey = getKeys( attribute );
                
		  if ( key[ 0 ] == WordManager.WILDCARD || key[ 0 ] == (short)( attrKey[ 0 ] >> NS_SHIFT ) )
		//	if ( key[ 0 ] == WordManager.WILDCARD || key[ 0 ] == ( attrKey[ 0 ] & NS_MASK ) );
				if ( key[ 1 ] == WordManager.WILDCARD || key[ 1 ] == attrKey[ 1 ] )
				{         
					list.addRef_2( attribute, parent );
				} 
                
			++ attribute;
		}
    }
    
    // TODO: don't forget to check isPredicate argument
    
	public DocItems namedChildOfParent( 
						int[] key, int childType, DocItems parents, boolean isPredicate )
	//-----------------------------------------------------------------------------------
	{
		IntList parentList	= parents.getIntList();
		IntList newList 	= new IntList( 2, 8 ); 
               
        if ( childType != ATTR && childType != ELEM && childType != TEXT && childType != ALL_NODES )
			throw new IllegalArgumentException( 
				"\nNodeTree.namedChildOfParent(): unknown child type " + childType );
			
        if ( childType == ATTR )

			for( int parentIx = 0; parentIx < parentList.count(); parentIx ++ )
			{
				if ( parentList.getRef_2( parentIx ) == VOIDED_NODE )
					continue;

				int parent = parentList.getRef_1( parentIx );  
				
				appendNamedAttributes( parent, key, newList );
			 }
		else
				
			for( int parentIx = 0; parentIx < parentList.count(); parentIx ++ )
			{
				int parent	= parentList.getRef_1( parentIx );  			
				int type 	= parentList.getRef_2( parentIx );
				
				if ( type == VOIDED_NODE )
					continue;						
				else if ( type == DocItems. DOC_NODE )
				{
					// if the root element in this tree matches the key we're searching on
					
					if ( key[ 0 ] == WordManager.WILDCARD || (short) ( m_tree[ 1 ] >> NS_SHIFT ) == key[ 0 ] )
				//	if ( key[ 0 ] == WordManager.WILDCARD || ( m_tree[ 1 ] & NS_MASK ) == key[ 0 ] )
						if ( key[ 1 ] == WordManager.WILDCARD || m_tree[ 2 ] == key[ 1 ] )
							newList.addRef_2( 0, ELEM );	// docId
							
					continue;
				}

				int firstChild = firstChild( parent );
				if ( firstChild == -1 )
					continue;
					 
				buildSiblingList( childType, parent, firstChild, newList, key );
			}                   
				
		return new DocItems( m_indexManager, newList );
	}
	
    public DocItems newNamedParentOfNamedChild( int[] parentKeys, int[] childKeys, 
                                                    int nodeType, boolean isPredicate  )
    //----------------------------------------------------------------------------------
    {
        DocItems   doc  = new DocItems( this );
        
        IntList         list = doc.getIntList();
        
        int[] tree = m_tree;
                
        // root element doesn't have named parent!        
        for( int child = 1; child < m_numNodes; child ++ )
        {
            int childBase = INTS_PER_ENTRY * child
            ;
            if ( type( childBase ) == nodeType )
            {
            	++childBase;
            	
            	//int treePrefix_1 = tree[ childBase ] & 0xFF;
            	//int treePrefix_2 = (byte) tree[ childBase ];
            	
            	// SHIFT_AMOUNT == 0 if we're comparing namespaceURI indexes and
            	// SHIFT_AMOUNT == 8 if we comparing prefix keys directly
            	
              if ( childKeys[ 0 ] != WordManager.WILDCARD && (short)( tree[ childBase ] >> NS_SHIFT ) != childKeys[ 0 ] )
		//		if ( childKeys[ 0 ] != WordManager.WILDCARD && ( tree[ childBase ] & NS_MASK ) != childKeys[ 0 ] )
                    continue;               
                ++childBase;   
                if ( childKeys[ 1 ] != WordManager.WILDCARD && tree[ childBase ] != childKeys[ 1 ] )
                    continue;                
                    
                int parent = tree[ ++ childBase ];
                int parentBase = INTS_PER_ENTRY * parent;
                
                ++parentBase;
              if ( parentKeys[ 0 ] != WordManager.WILDCARD && (short) ( tree[ parentBase ] >> NS_SHIFT ) != parentKeys[ 0 ] )
    		//	if ( parentKeys[ 0 ] != WordManager.WILDCARD && ( tree[ parentBase ] & NS_MASK ) != parentKeys[ 0 ] )
                    continue;
                ++parentBase;
                if ( parentKeys[ 1 ] != WordManager.WILDCARD && tree[ parentBase ] != parentKeys[ 1 ] )
                    continue;                    

                if ( ! isPredicate )
                    list.addRef_2( child, parent );
                    
                else if ( list.getLast_1() != parent )
                    list.addRef_2( parent, parent );
            }
        }
        
        int count = list.count();
        
        if ( count == 0 )
            return null;
            
        doc.setNumTotalItems( count );
        doc.setNumValidItems( count );
        
        doc.setId( getId() );
        doc.trim();        
        
        return doc;
    }
    
    public DocItems newNamedNodesAtRoot( int[] keys )
    //----------------------------------------------------
    {
        DocItems doc = new DocItems( this );        
        
        IntList list = doc.getIntList();
        
        if ( keys[ 0 ] != WordManager.WILDCARD && (short) ( m_tree[ 1 ] >> NS_SHIFT ) != keys[ 0 ] )
            return null;
            
        if ( keys[ 1 ] != WordManager.WILDCARD && keys[ 1 ] != getLocalKey( 0 ) )
            return null;
            
        list.addRef_2( 0, 0 );    
            
        doc.setNumTotalItems( 1 );
        doc.setNumValidItems( 1 );
        
        doc.setId( getId() );
        doc.trim();
        
        return doc;
    }
    
    public DocItems newLeafNodeList( int nodeType, int[] key )
    //--------------------------------------------------------
    {
        //DocItems doc = new DocItems( m_indexManager );        
        DocItems doc = new DocItems( this );
        
        IntList list = doc.getIntList();
        
        if ( nodeType == ALL_NODES )
        {
            for( int node = 0; node < m_numNodes; node ++ )
            {
                list.addRef_2( node, node );
            }
        }
        else if ( nodeType == TEXT )
        {
            for( int node = 0, base = 0; node < m_numNodes; node ++, base += INTS_PER_ENTRY )
            { 
                if ( type( base ) == TEXT )
                {
                    list.addRef_2( node, node );     
                }
            }  
        }
        else // ELEM or ATTR
        {
            int[] tree = m_tree;
            for( int node = 0, base = 0; node < m_numNodes; node ++, base += INTS_PER_ENTRY )
            { 
                if ( type( base ) == nodeType )
                {
               // 	byte shifted 			= (byte) ( tree[ base + 1 ] >> NS_SHIFT );
               // 	byte shifted_Prefix		= (byte) ( tree[ base + 1 ] >> 8 );
				//	byte shifted_Namespace	= (byte) ( tree[ base + 1 ] >> 0 );
					
				//	boolean debugPrefixKey = key[ 0 ] == WordManager.WILDCARD;
					short debugKey =  (short) ( tree[ base + 1 ] >> NS_SHIFT );
					
				//	if ( key[ 0 ] != WordManager.WILDCARD && ( debugKey ) != key[ 0 ] )
                    if ( key[ 0 ] != WordManager.WILDCARD && (short) ( tree[ base + 1 ] >> NS_SHIFT ) != key[ 0 ] )
                        continue;
                    if ( key[ 1 ] != WordManager.WILDCARD && tree[ base + 2 ] != key[ 1 ] )
                        continue;                            

                    list.addRef_2( node, node );     
                }
            }
        }
        
        int count = list.count();
        
        if ( count == 0 )
            return null;
            
        doc.setNumTotalItems( count );
        doc.setNumValidItems( count );
        
        doc.setId( getId() );
        doc.trim();        
        
        return doc;
    }    
    
    void buildSiblingList( int sibType, int parent, int sib, IntList list, int[] keys )
    //---------------------------------------------------------------------------------
    {
        if ( sibType == ALL_NODES )
        
            while( true )
            {
                list.addRef_2( sib, parent );
                
                sib = getSibling( sib );          
                if ( sib == NO_FURTHER_SIBS )
                    return;
            }
            
        else if ( sibType == TEXT )

            while( sib != NO_FURTHER_SIBS )
            {
                int base = INTS_PER_ENTRY * sib;              
                if ( type( base ) == TEXT )
                {
                    list.addRef_2( sib, parent );
                }
                
                sib = sibling( base );           
            }     
            
        else  // elements and attributes
        
            while( true )
            {
                int base = INTS_PER_ENTRY * sib;
                
                if ( type( base ) == sibType )            
					if ( keys[ 0 ] == WordManager.WILDCARD || keys[ 0 ] == (short)( m_tree[ base + PREFIX_KEY ] >> NS_SHIFT ) )
				//	if ( keys[ 0 ] == WordManager.WILDCARD || ( m_tree[ base + PREFIX_KEY ] & NS_MASK ) == keys[ 0 ] )
						if ( keys[ 1 ] == WordManager.WILDCARD || keys[ 1 ] == m_tree[ base + LOCAL_KEY ] )
						{
							list.addRef_2( sib, parent );
						}				
                
                sib = sibling( base );      
                if ( sib == NO_FURTHER_SIBS )
                    return;             
            }
    }
    
    // 2jun03   NOTA: this is inefficient -- it builds a list of   		@@@@@@@
    //          *all* sibling chains in the tree. To be optimized ...   @@@@@@@
    
    public DocItems newSiblingList( int siblingType, int[] keys )
    //-----------------------------------------------------------
    {
        DocItems doc = new DocItems( this );        
        IntList list = doc.getIntList();
        
        int base = 0;             
        for( int node = 0; node < m_numNodes; node ++, base += 4 )
        {
            if ( isFirstSibling_BASE( base ))
            {
                buildSiblingList( siblingType, m_tree[ base + PARENT ], node, list, keys );  
            }
        }

        int count = doc.getIntList().count();
        
        if ( count == 0 )
            return null;
        
        doc.setNumTotalItems( count );
        doc.setNumValidItems( count );
        
        doc.setId( getId() );
        doc.trim();        
        
        return doc;
    }  
    
    
    // NOTA: addRef_2( parent, parent ) for node 0 results in VOIDED_NODE
    // which means '/bib/..' -> no hits and *not* the document node
    // Is this what we want ??
    // TO-DO: check if '/bib/..' => VOIDED_NODE in NodeTree.newParentOf() is desirable
    
    // 3july03:	I've now changed this to only return parents of nodes > 0, not the root
    
    public DocItems newParentOf( int type, int[] key )
    //------------------------------------------------
    {
        DocItems doc = new DocItems( this );        
        IntList list = doc.getIntList();
        
        int[] tree = m_tree;
        
        if ( type == ALL_NODES )
        {
            for( int node = 1, base = INTS_PER_ENTRY; base < INTS_PER_ENTRY * m_numNodes; node++, base += INTS_PER_ENTRY )
            {
                int parent = tree[ base + PARENT ];              
                list.addRef_2( parent, parent );
            }
        }    
        else if ( type == TEXT )
        {
            int base = INTS_PER_ENTRY;
            for( int node = 1; node < m_numNodes; node ++, base += INTS_PER_ENTRY )      
                if ( type( base ) == TEXT )
                {
                    int parent = tree[ base + PARENT ];                   
                   	list.addRef_2( parent, parent );

                }
        }
        else if ( type == ATTR || type == ELEM )
        {
            
            for( int node = 1, base = INTS_PER_ENTRY; node < m_numNodes; node ++, base += INTS_PER_ENTRY )
            { 
                if ( type( base ) == type )
                {
                    if ( key[ 0 ] != WordManager.WILDCARD && (short)( tree[ base + 1 ] >> NS_SHIFT ) != key[ 0 ] )
    		//		if ( key[ 0 ] != WordManager.WILDCARD && ( tree[ base + 1 ] & NS_MASK ) != key[ 0 ] )
                        continue;
                    if ( key[ 1 ] != WordManager.WILDCARD && tree[ base + 2 ] != key[ 1 ] )
                        continue;     
                        
                    int parent = tree[ base + PARENT ];                 
                    list.addRef_2( parent, parent );
				}
            }            
        }
        else
            throw new IllegalArgumentException( "\nNodeTree.newParentOf(): unknown node type " + type );
            
        if ( list.count() == 0 )
            return null;       
                     
		list.trim();
		
		//  NOTA: looks like sort() is sorting on roots not leaves. IS THIS WHAT WE WANT ??
        
		if ( !doc.isSorted() )
			doc.sort();
        
        int numTotal = list.count();
        
        doc.setNumTotalItems( numTotal );
        doc.setNumValidItems( numTotal - list.markDuplicatesInvalid() );
        
        doc.setId( getId() );
        
        return doc;       
    }  
    
    public String toString()
    //----------------------
    {
        StringBuffer sb = new StringBuffer();
        sb.append( "\n" );
        
        for ( int i = 0, base = 0; base < 4 * m_numNodes; base += 4, i++ )
        {
            sb.append( "[" + i + "]" );
            
            int level = 0;
            int parent = getParent_BASE( base );
            while ( parent != -1 )
            {
                ++ level;
                int nextBase = parent * INTS_PER_ENTRY;
                parent = getParent_BASE( nextBase );
            }
            
            printNode( level, base, type( base ), sb );
        }

        return sb.toString();
    } 
    
    int firstChild( int parent )
    //--------------------------
    {
        int currNode = parent;
        
        while( ++ currNode < m_numNodes && getType( currNode ) == ATTR ) {}

        return ( currNode < m_numNodes && getParent( currNode ) == parent ) ?
        
            currNode : 
            -1;
    }
    
    public DocItems evalAncestor( DocItems descDoc, DocItems ancestorDoc, IntList newDescList )
    //-----------------------------------------------------------------------------------------
    {
    	return evalAncestor( descDoc, ancestorDoc, newDescList, false );
    }
    
  	public DocItems evalAncestor( DocItems descDoc, DocItems ancestorDoc, 
  													IntList newDescList, boolean allowSelfMatches  )
	//---------------------------------------------------------------------------------=------------
	{     
		IntList	descList	= descDoc.getIntList();
		IntList	ancList		= ancestorDoc.getIntList();
		
		int[] tree 			= m_tree;
		
		nextDesc :
		
		for( int descIx = 0; descIx < descList.count(); descIx ++ )
		{
			int[] descLeafRoot = descList.getRef_Both( descIx );	
			if ( descLeafRoot[ 1 ] == VOIDED_NODE )
				continue;
				
			int desc = descLeafRoot[ 0 ];
			
			for( int ancIx = 0; ancIx < ancList.count(); ancIx ++ )
			{				
				int[] ancLeafRoot = ancList.getRef_Both( ancIx );
				if ( ancLeafRoot[ 1 ] == VOIDED_NODE )
					continue;
					
				int anc = ancLeafRoot[ 0 ];

				if ( desc < anc )
					continue nextDesc;
					
				if ( allowSelfMatches && desc == anc && getType( desc ) == ATTR )	
				{
					newDescList.addRef_2( descLeafRoot[ 0 ], ancLeafRoot[ 0 ] );
					continue nextDesc;	
				}
				
				int parent = desc;		
				
				while ( ( parent = tree[ INTS_PER_ENTRY * parent + PARENT ] ) >= anc )
 				{
 					if ( parent == anc )
 					{
 						if ( parent > DocItems. DOC_NODE )
 							newDescList.addRef_2( descLeafRoot[ 0 ], ancLeafRoot[ 0 ] );
 						else
							newDescList.addRef_2( descLeafRoot[ 0 ], 0 );
 						continue nextDesc;
 					}
 				}
			}
		}
		
		descDoc.setList( newDescList );	// replace the existing list
		
		return descDoc;
	}
	
	public DocItems evalParent( DocItems childDoc, DocItems parentDoc, boolean isPredicate )
	//--------------------------------------------------------------------------------------
	{	
		IntList parents 	= parentDoc.getIntList();
		IntList children	= childDoc.getIntList();
		
		IntList	newChildren	= new IntList( 2 );
		
		int priorAdd = -1;
		
		if ( !parents.isSorted() )	parents.sort();
		if ( !children.isSorted())	children.sort();
		
		for ( int childIx = 0; childIx < children.count(); childIx ++ )
		{
			int child = children.getRef_2( childIx );
			if ( child == VOIDED_NODE )
				continue;
				
			int childsParent = getParent( child );
			
		nextChild :
		
			for( int parentIx = 0; parentIx < parents.count(); parentIx ++ )
			{
				if ( parents.getRef_2( parentIx ) == VOIDED_NODE )
					continue;
					
				int parent = parents.getRef_1( parentIx );
				
				if ( child <= parent )
					continue;
					
				if ( childsParent == parent )
				{
					if ( isPredicate )
					{
						if ( parent != priorAdd )
						{                   	
							newChildren.addRef_2( parent, parent );
							priorAdd = parent;
							
							break nextChild;
						}
					}
   					else    
					{
						if ( parent > DocItems. DOC_NODE )
							newChildren.addRef_2( children.getRef_1( childIx ), parent );
						else
							newChildren.addRef_2( children.getRef_1( childIx ), 0 );
					}
				}
				
				//continue nextChild;				
			}
		}
		
		int newInvalid = 0;
		
		if ( isPredicate )
			if ( !newChildren.isSorted() )
			{
				
				newChildren.sort();
				newInvalid = newChildren.markDuplicatesInvalid();
			}
		
		childDoc.setList( newChildren, newInvalid );
		
		return childDoc;
	}
	
	public DocItems evalParent_( DocItems childDoc, DocItems parentDoc, boolean isPredicate )
	//--------------------------------------------------------------------------------------
	{        
 		IntList		parentList		= parentDoc.getIntList();
		IntList		childList		= childDoc.getIntList();       
		IntList		newChildList	= new IntList( 2 );
        
		// prevents duplicate adds to list in the case of predicates 
		int priorNewParentAdd = -1;
		
		for( int parentIx = 0; parentIx < parentList.count(); parentIx ++ )
		{
			if ( parentList.getRef_2( parentIx ) == VOIDED_NODE )
				continue;
                
			int parent = parentList.getRef_1( parentIx );
                                                            
			  // first cycle thru all attributes looking for a match in childList
			  // CORRECTION: given the current architecture, for the moment there's
			  // no possibility of finding a match in attributes. Cycle past them
            
			int sibling = parent;
			while ( ++ sibling < m_numNodes && getType( sibling ) == ATTR ) { }
            
			if ( sibling >= m_numNodes || getParent( sibling ) != parent )
				continue;
                
	  // cycle through element and text children looking for a match against childList
            
	  nextParent :        
    		
			for( int childIx = 0; childIx < childList.count(); childIx ++ )
			{               
				int child = childList.getRef_2( childIx );
				if ( child == VOIDED_NODE )
					continue;
                    
		 nextChild :            
				while ( true )
				{
					if ( child < sibling )	
					  
						break nextChild;

					else if ( child == sibling )
					{
						if ( isPredicate )
						{
							if ( parent != priorNewParentAdd )
							{                   	
								newChildList.addRef_2( parent, parent );
								priorNewParentAdd = parent;
							}
						}
                        
						else    
                        {
                        	if ( parent > DocItems. DOC_NODE )
								newChildList.addRef_2( childList.getRef_1( childIx ), parent );
							else
								newChildList.addRef_2( childList.getRef_1( childIx ), 0 );
                        }
						
						break nextChild;
					}
                    
					sibling = getSibling( sibling );
                    
					if ( sibling == NO_FURTHER_SIBS )
						break nextParent;
				}
			}
		}      
        
		childDoc.setList( newChildList );     
        
		  // 30sept03 have just added a check on a predicate add that we're different
		  // from the prior add. This should obviate need to do an explicit dup check
        
		  //if ( isPredicate )
			  //childDoc.updateValidNodeCount( - newChildList.markDuplicatesInvalid() );
        
		return childDoc;
	}
    
    // a temporary hack allowing us to hoist pure attributes in enclosed results in
    // constructed elements into the element tag preceding them. To be replaced in the next rev
    
    boolean hoistAttributeNodesInEnclosedContent( int attrNode )
    //----------------------------------------------------------
    {
		if ( attrNode < m_numNodes )
			if ( getType( attrNode ) == QueryDocumentTree. ENCLOSED_RESULTS )
				if ( ( (QueryDocumentTree)this).getEnclosedResults( attrNode ).isAttributesOnly() )
					return true;
    	
    	return false;
    }
    
     // NOTA:	This is toplevel, called from a DocItems object.
     //			attributes and text nodes should be emitted in a 
     //			different fashion from elements

    public void emitXml( int node, PrintWriter w, boolean prettyPrint )
    //-----------------------------------------------------------------
    {
    	switch( getType( node ))
    	{
    		case DocItems. DOC_NODE :	
    		
    						emitElementNode( node + 1, w, false, prettyPrint );
    						break;
    		
    		case ELEM :		
			case QueryDocumentTree. ELEMENT_CTOR :
			
							boolean hoistAttributes = hoistAttributeNodesInEnclosedContent( node + 1);				
			
    						emitElementNode( node, w, hoistAttributes, prettyPrint );
    						break;
    		
    		case ATTR :		emitAttributeNode( node, w, true );
    						break;
    		
    		case TEXT :		emitTextNode( node, w, true );
    						break;
    						
    		default :
    		
					throw new IllegalArgumentException( 
						"\nNodeTree.emitXml(): unknown toplevel nodetype: " + getType( node ));
    	}  	
    	
		if ( prettyPrint )
			w.println();

    	w.flush();
    }
    
    int emitElementNode( int node, PrintWriter w, boolean hoistFollowingAttributes, boolean prettyPrint )
    //---------------------------------------------------------------------------------------------------
    {
    	int myself 			= node;
    	String elementName 	= getElementName( node );
    	int elementNode		= node;
    	
		w.print( '<' + elementName );
		
		if ( DEBUGGING )
			w.print( ":" + node );
		
		while( ++ node < m_numNodes && getType( node ) == ATTR )
		{
			emitAttributeNode( node, w, false );			
		}
		
		// QUESTION: Why are we precalculating this boolean, as opposed to checking
		// its value in place right here ???????????????????????????????????????????
		
		if ( hoistFollowingAttributes )
		{
			( (QueryDocumentTree)this).getEnclosedResults( node ).emitAttributes( w );
		}
    	
    	if ( node >= m_numNodes || getParent( node ) != myself )
    	{
			w.print( "/>" );
			return node;
    	}
		else
    		w.print( ">" );

    	while( node < m_numNodes && getParent( node ) == myself )
    	{
    		int nodeType = getType( node );
    		switch( nodeType )
    		{
    			case ELEM :	
    			
							boolean hoistAttributes = hoistAttributeNodesInEnclosedContent( node + 1 );
							
    						node = emitElementNode( node, w, hoistAttributes, prettyPrint ); 
    						break;
				case TEXT : 
							node = emitTextNode( node, w, false ); 
							break;
							
				case QueryDocumentTree. ELEMENT_CTOR :	// skip if encountered in element content
			    										// (only emitted indirectly via ENCLOSED_RESULTS below)
							int parent = node;
			    						
							while( ++ node < m_numNodes && getParent(node) == parent ) { }
							
							break;	

				case QueryDocumentTree. RESERVED :	
				case QueryDocumentTree. ENCLOSED_RESULTS :
				
							if ( ! (this instanceof QueryDocumentTree) )
				
								throw new IllegalArgumentException( 
									"\nNodeTree.emitElementNode(): unknown nodetype " + getType( node ));
									
							if ( nodeType == QueryDocumentTree. RESERVED )
							
								w.println( "Reserved" );
							else
							
								((QueryDocumentTree) this).getEnclosedResults( node ).emitXml( w, prettyPrint );
							
							++ node;
							break;
    			
    			default :
    			
    				throw new IllegalArgumentException( 
    						"\nNodeTree.emitElementNode(): unknown nodetype " + getType( node ));
    		}	
    	}
    	
		w.print( "</" + elementName );
		if ( DEBUGGING )
			w.print( ":" + elementNode );
		w.print( ">" );
		
		return node;
    }
  
	void emitAttributeNode( int node, PrintWriter w, boolean standalone )
	//-------------------------------------------------------------------
	{   
		String name = getAttributeName( node );
		
		if ( standalone )
		{
			w.print( "@" + name + "=");
		}
		else
		{
			w.print( ' ' + name + "=" );
		}
		
		String attrValue = getAttributeText( node );
		w.print( "\"" + attrValue + "\"" );
		
		if ( standalone )
			w.print( ' ' );
		
		if ( DEBUGGING )
			w.print( ":" + node );
	}
    
	int emitTextNode( int node, PrintWriter w, boolean standalone )
	//-------------------------------------------------------------
	{   			
		String text = getElementText( node );
		w.print( text );
		
		if ( DEBUGGING )
			w.print( ":" + node );
		
		return ++ node;
	}   
	
	// needs some work!
	
	public DocItems subscript( int subscript )	{ return null; }
	//----------------------------------------
	
	public StringBuffer asString( int node )	{ return asString( node, false ); }
	//--------------------------------------

	public StringBuffer asString( int node, boolean addInterTextSpaces )
	//------------------------------------------------------------------
	{
		int item = getType( node );
		
		switch( item )
		{
			case ELEM :
			
					return concatenateSubordinateTextNodes( node, addInterTextSpaces );
			
			case ATTR :
			
					return new StringBuffer( getAttributeText( node ));
					
			case TEXT :
			
					return new StringBuffer( getElementText( node ));
					
			case QueryDocumentTree.ENCLOSED_RESULTS :	
							
							ResultList results = ((QueryDocumentTree) this).getEnclosedResults( node );
							return new StringBuffer( results.asString( true ));
				
			default :
			
					throw new java.lang.IllegalArgumentException( 
					
							"\nNodeTree.asString(): Unknown node type for string value" );
		}
	}
	
	public String string( int[] valueType )
	//-------------------------------------
	{
		return string( valueType, false );
	}
	
	// addInterTextSpaces: an experiment to facilitate full-text searches on subordinate
	// element content, where text and element nodes might not have whitespace separators
	
	public String string( int[] valueType, boolean addInterTextSpaces )
	//-----------------------------------------------------------------
	{
		int nodeId = valueType[0];
		
		int type = getType( nodeId );
		
		if ( type == TEXT )	
			
			valueType[1] = DocItems.TEXT_AS_STRING;
			
		else if ( type == ATTR )
		
			valueType[1] = DocItems. ATTR_TEXT;
			
		else if ( type == ELEM)
		{
			StringBuffer concatenatedText = concatenateSubordinateTextNodes( valueType[0], addInterTextSpaces );
			
			valueType[1] = DocItems. STRING;
			
			return concatenatedText.toString();
		}
		
		return null;
	}
	
	StringBuffer concatenateSubordinateTextNodes( int parent )
	//--------------------------------------------------------
	{
		return concatenateSubordinateTextNodes( parent, false );
	}
	
	StringBuffer concatenateSubordinateTextNodes( int parent, boolean addSpace )
	//--------------------------------------------------------------------------
	{
		StringBuffer sb = new StringBuffer();		
		
		int firstSib = firstChild( parent );
		
		if ( firstSib == -1 )
		{
			return sb;	// no subordinate TEXT or ELEM nodes
		}
		
		IntList sibList = new IntList( 2 );
    
		buildSiblingList( ALL_NODES, parent, firstSib, sibList, null );		
		
		int sib = firstSib;
		
		while( true )
		{
			int base = INTS_PER_ENTRY * sib;
			
			if ( type( base ) == TEXT )
			{
				sb.append( getElementText( sib ));
				
				if ( addSpace )
					sb.append( ' ' );
			}
			
			else if ( type( base ) == ELEM )
			{
				sb.append( concatenateSubordinateTextNodes( sib, addSpace ));
			}
                
			sib = sibling( base );
			   
			if ( sib == NO_FURTHER_SIBS )
				return sb;
		}
	}
	
	public long serialize( DataOutputStream out ) throws IOException
	//-------------------------------------------
	{
		long numWritten = 0;
        
		out.writeInt( m_id );
		out.writeInt( INTS_PER_ENTRY );
		
		numWritten += 2 * Sizes.SINT;
		
		int[] tree = m_tree;
		
		for ( int i = 0; i < m_numNodes * INTS_PER_ENTRY; )
		{
			out.writeInt( tree[ i++ ] );
			out.writeInt( tree[ i++ ] );
			out.writeInt( tree[ i++ ] );
			out.writeInt( tree[ i++ ] );
		}
		
		numWritten += INTS_PER_ENTRY * m_numNodes * Sizes.SINT;
		
		return numWritten;
	}
	
	/**
	 * Given a node list in 'doc', return all following siblings in doc order.
	 * 
	 * @param doc Source nodes to be 'followed'
	 * @return A new node sequence of 'followers'
	 */
	
	DocItems followingSibling( DocItems doc )
	//---------------------------------------
	{
		IntList items = doc.getIntList();
		
		DocItems siblingDoc = new DocItems( this );	
		siblingDoc.setId( doc.getId() );
		
		IntList siblingItems = new IntList( 2 );
		
		for( int i = 0; i < items.count(); i++ )
		{
			int[] item = items.getRef_Both( i );
			if ( item[ 1 ] != VOIDED_NODE )
				if ( doc.isNode( item ) && !doc.isDocumentNode( item ))
				{
					int nextSibling = getSibling( item[ 0 ] );
					while ( nextSibling != NO_FURTHER_SIBS )
					{
						siblingItems.addRef_2( nextSibling, nextSibling );						
						nextSibling = getSibling( nextSibling );
					}
				}
		}
		
		siblingDoc.setList( siblingItems );
		
		return siblingDoc;
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