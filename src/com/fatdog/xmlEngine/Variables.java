/*  XQEngine Copyright (C) 2002-2005 Fatdog Software Inc. [howardk@fatdog.com]
 *	--------------------------------------------------------------------------
 *  This file is part of XQEngine and is distributed under the terms of the
 *  GNU General Public License. See additional info at the end of this file. */
 
package com.fatdog.xmlEngine;

import com.fatdog.xmlEngine.exceptions.*;

	/**
	 * Maintains the values of XQuery variables.
	 * 
	 * @author Howard Katz, howardk@fatdog.com
	 * @version 0.69
	 */

public class Variables 
{
	IndexManager 	m_indexer;
	TreeWalker		m_treeWalker;

	int				m_currVarId	= -1;
	
	String[]		m_name 			= new String[0];
	int[]			m_forLetIndex	= new int[0];
	ResultList[]	m_value			= new ResultList[0];
	
	public final static int	LET_INDEX = -1;
	public final static int FOR_INDEX = 0;
	
	public Variables( TreeWalker walker )
	//-----------------------------------
	{
		//m_indexer 		= walker.getIndexer();
		m_treeWalker	= walker;
	}

	// register a new variable and its value
	
	public int newVariable( String name, ResultList value, int forLetIndex )
	//----------------------------------------------------------------------
	{
		++ m_currVarId;	// one greater than current index
		
		String[] 		tempName 		= new String[ m_currVarId + 1 ]; 
		int[]			tempForLetIndex	= new int[ m_currVarId + 1 ];
		ResultList[]	tempValue		= new ResultList[ m_currVarId + 1 ];
		
		System.arraycopy( m_name, 0, tempName, 0, m_currVarId );
		System.arraycopy( m_forLetIndex, 0, tempForLetIndex, 0, m_currVarId );
		System.arraycopy( m_value, 0, tempValue, 0, m_currVarId );
		
		tempName[ m_currVarId ] 		= name;
		tempForLetIndex[ m_currVarId ] 	= forLetIndex;
		tempValue[ m_currVarId ] 		= value;
		
		m_name			= tempName; 		tempName = null;
		m_forLetIndex	= tempForLetIndex;	tempForLetIndex = null;
		m_value			= tempValue; 		tempValue = null;
		
		return m_currVarId;
	}

	// given a variable name, return its value
	// NOTA: Pretty brain-dead!! Doesn't account for variables of the
	// same name nested within for loops
	
	public ResultList evalVariableValue( String variableName ) throws InvalidQueryException
	//--------------------------------------------------------
	{
		for ( int i = m_name.length - 1; i >= 0; --i )
		
			if ( variableName.equals( m_name[ i ]))
			{
				ResultList varResults = m_value[ i ];			
				
				if ( m_forLetIndex[ i ] == LET_INDEX ||true)
				{
					// BUG-FIX 27jan04
					ResultList clone = m_value[ i ].cloneResultList();
					clone.var=variableName;
					//clone.columns=m_value[ i ].columns;
					return clone;
					//return m_value[ i ];
				}
				//else
				//{
					// BUG-FIX 14jan04
//					int[] valueType = varResults.valueType( m_forLetIndex[ i ] );
				//	ResultList tempResults = varResults.subscript( m_forLetIndex[ i ] + 1 );
				//	int[] valueType = tempResults.valueType( 0 );

				//	return new ResultList( m_treeWalker, valueType, tempResults.getCurrDocId() );
				//}					
			}
			
		throw new CategorizedInvalidQueryException( 

					"XP0016", 
					"Variable '$" + variableName + "' not in scope" );
	}
	
	public int findNamedForVariable( String varName )
	//-----------------------------------------------
	{
		for ( int i = m_name.length - 1; i >= 0; --i )
		{
			if ( varName.equals( m_name[ i ]) && m_forLetIndex[ i ] >= 0 )

				return i;
		}
		
		return -1;
	}
	
	public ResultList getVariableValue( int varId )
	//---------------------------------------------
	{
		if ( varId >= 0 && varId <= m_currVarId )
		{
			return m_value[ varId ];
		}
		
		throw new IllegalArgumentException( "\nVariables.getVariableValue(): No variable found with id = " + varId );
	}
	
	public String getVariableName( int varId )
	//---------------------------------------------
	{
		if ( varId >= 0 && varId <= m_currVarId )
		{
			return m_name[ varId ];
		}
		
		throw new IllegalArgumentException( "\nVariables.getVariableName(): No variable found with id = " + varId );
	}
	
	public void advanceForIndex( int variableId )
	//-------------------------------------------
	{
		m_forLetIndex[ variableId ]++;
		
		if ( m_forLetIndex[ variableId ] > m_value[ variableId ].getNumValidItems() )
			m_forLetIndex[ variableId ] = 0;	// do we need to do this ?
	}
	
	public void unbindFollowing( int variableIx )
	//-------------------------------------------
	{
		int newLen = variableIx + 1;

		String[]    	tempName    = new String[ newLen ];
		ResultList[] 	tempValue   = new ResultList[ newLen ];
		int[]       	tempIx      = new int[ newLen ];

		if ( variableIx >= 0 )
		{
			System.arraycopy( m_name, 0, tempName, 0, newLen );
			System.arraycopy( m_value, 0, tempValue, 0, newLen  );
			System.arraycopy( m_forLetIndex, 0, tempIx, 0, newLen );
		}

		m_name 			= tempName;
		m_value 		= tempValue;
		m_forLetIndex 	= tempIx;

		m_currVarId = variableIx;
	}
}
