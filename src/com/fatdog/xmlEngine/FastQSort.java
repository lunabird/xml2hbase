/*  XQEngine Copyright (C) 2002-2005 Fatdog Software Inc. [howardk@fatdog.com]
 *	--------------------------------------------------------------------------
 *  This file is part of XQEngine and is distributed under the terms of the
 *  GNU General Public License. See additional info at the end of this file. */
 
package com.fatdog.xmlEngine;
	
	/**
	 * A QuickSort algorithm w/ a long and varied history.
	 * 
	 * @author Howard Katz, howardk@fatdog.com
	 * @version 0.61
	 */

/*  katz 29jan03

    This adaption for XQEngine v0.55+ sorts a single int[] array
    which is derived from an IntList( 2 ), ie, each entry has two
    components. we generally sort on int[ ix * 2 + 1 ] as opposed
    to int[ ix * 2 ]. [MAKE THIS SWITCH SELECTABLE ???]

    when we do sort on [ 2 * ix + 1 ], we also swap [ 2 * ix ]
    as appropriate.
*/

/*
 * @(#)QSortAlgorithm.java	1.3   29 Feb 1996 James Gosling
 *
 * Copyright (c) 1994-1996 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software
 * and its documentation for NON-COMMERCIAL or COMMERCIAL purposes and
 * without fee is hereby granted.
 * Please refer to the file http://www.javasoft.com/copy_trademarks.html
 * for further important copyright and trademark information and to
 * http://www.javasoft.com/licensing.html for further important
 * licensing information for the Java (tm) Technology.
 *
 * SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 *
 * THIS SOFTWARE IS NOT DESIGNED OR INTENDED FOR USE OR RESALE AS ON-LINE
 * CONTROL EQUIPMENT IN HAZARDOUS ENVIRONMENTS REQUIRING FAIL-SAFE
 * PERFORMANCE, SUCH AS IN THE OPERATION OF NUCLEAR FACILITIES, AIRCRAFT
 * NAVIGATION OR COMMUNICATION SYSTEMS, AIR TRAFFIC CONTROL, DIRECT LIFE
 * SUPPORT MACHINES, OR WEAPONS SYSTEMS, IN WHICH THE FAILURE OF THE
 * SOFTWARE COULD LEAD DIRECTLY TO DEATH, PERSONAL INJURY, OR SEVERE
 * PHYSICAL OR ENVIRONMENTAL DAMAGE ("HIGH RISK ACTIVITIES").  SUN
 * SPECIFICALLY DISCLAIMS ANY EXPRESS OR IMPLIED WARRANTY OF FITNESS FOR
 * HIGH RISK ACTIVITIES.
 */

/*
 * A quick sort demonstration algorithm.
 * SortAlgorithm.java
 *
 * @author James Gosling
 * @author Kevin A. Smith
 * @version 	@(#)QSortAlgorithm.java	1.3, 29 Feb 1996
 * extended with TriMedian and InsertionSort by Denis Ahrens
 * with all the tips from Robert Sedgewick (Algorithms in C++).
 * It uses TriMedian and InsertionSort for lists shorts than 4.
 * <fuhrmann@cs.tu-berlin.de>
 */

public class FastQSort
{
	/** This is a generic version of C.A.R Hoare's Quick Sort
	* algorithm.  This will handle arrays that are already
	* sorted, and arrays with duplicate keys.<BR>
	*
	* If you think of a one dimensional array as going from
	* the lowest index on the left to the highest index on the right
	* then the parameters to this function are lowest index or
	* left and highest index or right.  The first time you call
	* this function it will be with the parameters 0, a.length - 1.
	*
	* @param a	    an integer array
	* @param lo0   left boundary of array partition
	* @param hi0	right boundary of array partition
	*/

    private static void QuickSort(int a[], int l, int r)// throws Exception
    //--------------------------------------------------
    {
    	int M = 4;
    	int i;
    	int j;
    	int v;

    	if ((r-l)>M)
    	{
    	    // 26july00: following [.][1] -> [.][0]
    		i = (r+l)/2;
    		if (a[ 2 ]>a[ 2*i ]) swap(a,2,2*i);	// Tri-Median Methode!
    		if (a[ 2 ]>a[ 2*r ]) swap(a,2,2*r);
    		if (a[ 2*i ]>a[ 2*r ]) swap(a,2*i,2*r);

    		j = r-1;
    		swap(a,2*i,2*j);
    		i = l;

    	    // 26july00: following [.][1] -> [.][0]
    		v = a[2*j];
    		for(;;)
    		{
    	    // 26july00: following [.][1] -> [.][0]
    			while(a[ (++i)*2 ]<v);
    			while(a[ (--j)*2 ]>v);
    			if (j<i) break;
    			swap (a,2*i,2*j);
    		}

    		swap(a,2*i,2*(r-1));
    		QuickSort(a,l,j);
    		QuickSort(a,i+1,r);
    	}
    }

	private static void swap(int a[], int i, int j )
	//----------------------------------------------
	{
		int T;

		T = a[ i ];
		a[ i ] = a[ j ];
		a[ j ] = T;
		
		T = a[ i + 1 ];
		a[ i + 1 ] = a[ j + 1 ];
		a[ j + 1 ] = T;		
	}

    // we sort on a[ i ]
	private static void InsertionSort( int[] a, int lo0, int hi0)
	//-----------------------------------------------------------
	{
		int i, j, v, w;

		int temp = 0;

		for ( i = lo0 + 1; i <= hi0; i++ )
		{
		    v = a[ 2*i ];
		    w = a[ 2*i + 1 ];
			j = i;

			while (( j > lo0 ) && a[ 2*(j-1) ] > v )
			{
			    a[ 2*j ]    = a[ 2*(j - 1) ];
			    a[ 2*j + 1 ]= a[ 2*(j - 1) + 1 ];
				j--;
			}

            a[ 2*j ]        = v;
            a[ 2*j + 1 ]    = w;
	 	}
	}


	public static void sort(int a[], int count )
	//------------------------------------------
	{
		QuickSort( a, 0, count - 1 );
		InsertionSort( a, 0, count -1 );
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