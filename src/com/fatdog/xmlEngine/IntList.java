/*  XQEngine Copyright (C) 2002-2005 Fatdog Software Inc. [howardk@fatdog.com]
 *	--------------------------------------------------------------------------
 *  This file is part of XQEngine and is distributed under the terms of the
 *  GNU General Public License. See additional info at the end of this file. */
 
package com.fatdog.xmlEngine;

	/**
	 * A dynamic array structure for storing collections of integers.
	 * 
	 * @author Howard Katz, howardk@fatdog.com
	 * @version 0.69
	 */

public class IntList
{
    private final static int DEFAULT_ALLOCS 	= 8;  // 8 slots each m_numInts wide
    private final static int DEFAULT_NUMINTS	= 2;
    
    private boolean		m_aggressiveAllocator = true;

    private int			m_numInts;
    private int			m_newEntryAllocs;

    private int			m_count;    // num logical entries, not array slots
    private int[]		m_xrefs;    // for historical reasons
    
    private IntList		m_nextList;	// experimental, added 11dec03
    									// to use as linked list for WordListHashTable
    
    private static final int COPY_THRESHOLD = 9999;
    
    public IntList()				{ this( DEFAULT_NUMINTS, DEFAULT_ALLOCS ); }
	//--------------
    
    public IntList( int numInts )   { this( numInts, DEFAULT_ALLOCS ); }
    //---------------------------

    public IntList( int numInts, int newEntryAllocs )
    //-----------------------------------------------
    {
        m_numInts = numInts;
        m_newEntryAllocs = newEntryAllocs;

        m_xrefs = new int[ m_numInts * newEntryAllocs ];
    }
    
    public IntList cloneList( )
    //-------------------------
    {
    	IntList newList = new IntList( m_numInts, m_count );
    	
    	int[] newRawList = newList.getRawList();
    	
    	System.arraycopy( m_xrefs, 0, newRawList, 0, m_count * m_numInts );
    	newList.setCount( m_count );
    	
    	return newList;
    }
   
    public void setNextList( IntList nextList )	{ m_nextList = nextList; }
	//-------------------------------------------
    public IntList nextList()						{ return m_nextList; }
    //------------------------
    
    public int getLast_1()
    //--------------------
    {
        if ( m_count == 0 )
            return -1;
        
        return m_xrefs[ ( m_count - 1 ) * m_numInts ]; 
    }

    public int[] getRawList()   { return m_xrefs; }
    //-----------------------
    public void setRawList( int[] rawList )	{ m_xrefs = rawList; }
    //------------------------------------------------------------
    
    // we want to reuse list w/out reallocating from scratch

    public void zeroCount() { m_count = 0; }
    //---------------------

    public void setAggressiveAllocator( boolean value ) { m_aggressiveAllocator = value; }
    //-------------------------------------------------

    // 11jan03 NOTA: the following assumes 1 integer/entry @@@@@
    
    public void checkBlockAllocation( int newBlockLen )
    //-------------------------------------------------
    {
        int newAlloc = ( m_count * m_numInts ) + newBlockLen;
        
        if ( newAlloc > m_xrefs.length )
        {
            newAlloc = m_aggressiveAllocator ? 
            
                    2 * m_xrefs.length      + newBlockLen : 
                    5 * m_xrefs.length / 4  + newBlockLen;
                    
            int[] tempXrefs = new int[ newAlloc ];
            System.arraycopy( m_xrefs, 0, tempXrefs, 0, m_count * m_numInts );

            m_xrefs = tempXrefs; tempXrefs = null;
        }                    
    }
    
    public void appendBlock( char[] block )
    //-------------------------------------
    {
        int blocklen = block.length;        
        
        checkBlockAllocation( blocklen );
        
        if ( blocklen < COPY_THRESHOLD )
        {
            for( int i = 0; i < blocklen; i++ )
                m_xrefs[ m_count ++ ] = block[ i ];
        }
        else
        {
            System.arraycopy( block, 0, m_xrefs, m_count, blocklen );
            m_count += blocklen; 
        }
    }
    
    public void appendBlock( int count, int[] block )
    //-----------------------------------------------
    {
        int blocklen = count * m_numInts;        
        
        checkBlockAllocation( blocklen );
        
        if ( blocklen < COPY_THRESHOLD )
        {
        	int next = m_count * m_numInts;
        	
            for( int i = 0; i < blocklen; i++ )
                m_xrefs[ next ++ ] = block[ i ];
                
            m_count += count;
        }
        else
        {
            System.arraycopy( block, 0, m_xrefs, m_count, blocklen );
            m_count += blocklen; 
        }
    }
    
    public void checkAllocation( )
    //----------------------------
    {
        if ( m_count * m_numInts >= m_xrefs.length )
        {
            int newAlloc = m_aggressiveAllocator ? 2 * m_xrefs.length : 5 * m_xrefs.length / 4;

            int[] tempXrefs = new int[ newAlloc ];
            System.arraycopy( m_xrefs, 0, tempXrefs, 0, m_count * m_numInts );

            m_xrefs = tempXrefs;
            tempXrefs = null;
        }
    }
    
    public void checkAllocation( int numArgs )
    //----------------------------------------
    {
        if ( numArgs * m_count * m_numInts >= m_xrefs.length )
        {
            int newAlloc = m_aggressiveAllocator ? 2 * m_xrefs.length : 5 * m_xrefs.length / 4;

            int[] tempXrefs = new int[ newAlloc ];
            System.arraycopy( m_xrefs, 0, tempXrefs, 0, m_count * m_numInts );

            m_xrefs = tempXrefs;
            tempXrefs = null;
        }
    }
    
    public void setRef_1( int index, int param )
    //------------------------------------------
    {
        int off         = index * m_numInts;
        m_xrefs[ off ]  = param;
    }
    
    public void setRef_2( int index, int param_2 )
    //--------------------------------------------
    {
        int off = index * m_numInts;
        
        m_xrefs[ off + 1 ]  = param_2;
    }
    
    public void setRef_Both( int index, int param_1, int param_2 )
    //-----------------------------------------------------------
    {
        int off = index * m_numInts;
        
        m_xrefs[ off ]      = param_1;
        m_xrefs[ off + 1 ]  = param_2;
    }
    
    public void setRef_Both( int index, int[] valueType )
    //---------------------------------------------------
    {
		int off = index * m_numInts;
        
		m_xrefs[ off ]      = valueType[0];
		m_xrefs[ off + 1 ]  = valueType[1];
    }
    
	public int[] getRef_Both( int index )
	//-----------------------------------
	{
		int[] refs = new int[ m_numInts ];
		
		int off = index * m_numInts;
        
        for( int i = 0; i < m_numInts; i++ )
        	refs[ i ] = m_xrefs[ off++ ];
        	
        return refs;
	}    
    
    public void addRef_1( int param_1 )
    //---------------------------------
    {
        checkAllocation( 1 );

        m_xrefs[ m_count ] = param_1;

        ++m_count;
    }

	public void addRef_2( int[] params )
	//------------------------------------------
	{
		checkAllocation();
		
		int off = m_count * m_numInts;

		m_xrefs[ off ]      = params[ 0 ];
		m_xrefs[ off + 1 ]  = params[ 1 ];

		++m_count;
	}
    public void addRef_2( int param_1, int param_2 )
    //----------------------------------------------
    {
        checkAllocation();

        int off = m_count * m_numInts;

        m_xrefs[ off ]      = param_1;
        m_xrefs[ off + 1 ]  = param_2;

        ++m_count;
    }
    
    public void addRef_Both( int[] valueType )
    //----------------------------------------
    {
    	checkAllocation();
    	
		int off = m_count * m_numInts;

		m_xrefs[ off ]      = valueType[0];
		m_xrefs[ off + 1 ]  = valueType[1];

		++m_count;	
    }
    
    public int getRef_1( int ix ) {  return m_xrefs[ ix * m_numInts ]; }
    //---------------------------
    public int getRef_2( int ix ) {  return m_xrefs[ ix * m_numInts + 1 ]; }
    //---------------------------
    
    public void copyRef_1To2( int ix )
    //--------------------------------
    {
        int off = ix * m_numInts;
        m_xrefs[ off + 1 ] = m_xrefs[ off ];
    }
    
    public int size()               	{ return m_count; }
    //---------------
    public int getSize()            	{ return m_count; }
    //------------------
    public int count()              	{ return m_count; }
    //----------------
    public void setCount( int count )	{ m_count = count; }	// called by cloneList()
    
    public void trim()
    //----------------
    {
        int newlen = m_count * m_numInts;
        
        int[] list = new int[ newlen ];
        System.arraycopy( m_xrefs, 0, list, 0, newlen );
        m_xrefs = list; list = null;    
    }
    
    public int markDuplicatesInvalid() // NOTA: minor optimization assumes m_numInts = 2
    //--------------------------------
    {
        int numNewInvalids = 0;
        int[] list = m_xrefs;
        
        int priorVal = m_xrefs[ 0 ];
        
        for( int i = 2; i < 2 * m_count; i += 2 )
        {
        	if ( list[ i ] != NodeTree.VOIDED_NODE )
        	{
	            if ( list[ i ] == priorVal )
	            {
	                list[ i + 1 ] = NodeTree.VOIDED_NODE ;
	                ++ numNewInvalids;
	            }
	            else
	                priorVal = list[ i ];
        	}
        	else
        		priorVal = list[ i ];
        }
        
        return numNewInvalids;
    }
    
    // hkatz 31jan03 assumes sort field is LEAF not ROOT on 2-entry list
    // NOTA: also assumes non-atomics only
   
    public boolean isSorted()
    //-----------------------
    {
        int[] list = m_xrefs;
        for( int i = 2; i < 2 * m_count; i += 2 )
        {
            if ( list[ i + 1 ] < list[ i - 1 ] )
          //	if ( list[ i ] < list[ i - 2 ])
                return false;
        }
        
        return true;
    }  
    
    public void sort()
    //----------------
    {
    	FastQSort.sort( getRawList(), m_count ); 
    }
    
	public String toString()
	//----------------------
	{
		StringBuffer sb = new StringBuffer();

		sb.append( "Item count = " + m_count );
		sb.append( "\n" );

		int[] list = m_xrefs;
        
		for( int i = 0, base = 0; i < m_count; i++, base += m_numInts )
		{
			sb.append( i + " " );
			for( int j = 0; j < m_numInts; j++ )
				{
					sb.append( "[" + m_xrefs[ base + j ] + "]" );
				}
			sb.append( "\n" );
		}
		
		return sb.toString();
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