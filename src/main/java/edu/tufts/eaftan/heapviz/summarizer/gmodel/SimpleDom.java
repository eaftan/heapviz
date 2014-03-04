/*
 * Copyright 2014 Edward Aftandilian. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.tufts.eaftan.heapviz.summarizer.gmodel;

import java.util.*;

public final class SimpleDom
{
	//////////////////////////////////////////////////
	//Sources for Equality Compare on count/cut
	public static final int exactlyOneValue = 1;
	public static final int manyValue = -1;
	
	public static int joinLinearityValues(int l1, int l2, boolean mayBeSameSource)
	{
		if(! mayBeSameSource)
			return (l1 == exactlyOneValue && l2 == exactlyOneValue) ? exactlyOneValue : manyValue;
		else
			return addLinearityValues(l1, l2);
	}
	
	public static int addLinearityValues(int l1, int l2)
	{
		if(l1 == manyValue || l2 == manyValue)
			return manyValue;
		else
			return l1 + l2;
	}
	
	public static boolean areLinearityValuesEquiv(int c1, int c2)
	{
		return (c1 == c2);
	}
	
	public static String linearityToString(int lv)
	{
		if(lv == manyValue)
			return "#";
		else
			return Integer.toString(lv);
	}
	
	//////////////////////////////////////////////////
	//Sources for Join
	public static final int sourceClear = 0;
	public static final int sourceA = 1;
	public static final int sourceB = 2; 
	public static final int sourceUnknown = 3;

	//should return true if both are source clear (thus merge ops in canonical are correct)
	public static boolean mayBeSameSource(int id1, int id2)
	{return (id1 == SimpleDom.sourceUnknown) || (id2 == SimpleDom.sourceUnknown) || (id1 == id2);}

	public static int joinSourceID(int id1, int id2)
	{return (id1 == id2) ? id1 : SimpleDom.sourceUnknown;}
	
	//////////////////////////////////////////////////
	//Nullity Values
	public static final int mustNull = 0;
	public static final int mustNonNull = 1;
	public static final int unknownNull = 2; 
	
	public static boolean isMustNull(int nv)
	{return nv == SimpleDom.mustNull;}
	
	public static boolean isMustNonNull(int nv)
	{return nv == SimpleDom.mustNonNull;}
	
	public static int joinNullityValues(int nv1, int nv2)
	{return (nv1 == nv2) ? nv1 : SimpleDom.unknownNull;}
	
	//////////////////////////////////////////////////
	//Shape Values
	public static final int singleton_S = 0;
	public static final int list_S = 1;
	public static final int tree_S = 2;
	public static final int dag_S = 3;
	public static final int cycle_S = 4;
	public static final int uninterp_S = 5;

	public static int joinShapes(int s1, int s2)
	{return Math.max(s1, s2);}

	//assume edges only one way from sa to sb
	public static int joinConnShapes(int sa, int sb, boolean allptrDisjoint, int ebtSum, HashSet<RCCRefName> internalOffsetsA, HashSet<RCCRefName> internalOffsetsB, HashSet<RCCRefName> ebtOffsets)
	{
		RCCRefName ebtOff = ebtOffsets.iterator().next();
		boolean upperListOk = (sa == SimpleDom.singleton_S) | (internalOffsetsA.size() == 1 && internalOffsetsA.iterator().next() == ebtOff);
		boolean lowerListOk = (sb == SimpleDom.singleton_S) | (internalOffsetsB.size() == 1 && internalOffsetsB.iterator().next() == ebtOff);
		boolean listOk = (ebtSum == 1) & upperListOk & lowerListOk;

		int sr;
		if(! allptrDisjoint)
			sr = SimpleDom.joinShapes(SimpleDom.dag_S, SimpleDom.joinShapes(sa, sb));
		else if((sa == SimpleDom.singleton_S) & listOk)
			sr = SimpleDom.joinShapes(SimpleDom.list_S, sb);
		else if(sa == SimpleDom.singleton_S)
			sr = SimpleDom.joinShapes(SimpleDom.tree_S, sb);
		else if((sa == SimpleDom.list_S) & listOk)
			sr = SimpleDom.joinShapes(SimpleDom.list_S, sb);
		else if(sa == SimpleDom.list_S)
			sr = SimpleDom.joinShapes(SimpleDom.tree_S, sb);
		else
			sr = SimpleDom.joinShapes(sa, sb);

		return sr;
	}

	public static String getShapeTypeStr(int s)
	{
		switch(s)
		{
		case SimpleDom.singleton_S:
			return "S";
		case SimpleDom.list_S:
			return "L";
		case SimpleDom.tree_S:
			return "T";
		case SimpleDom.dag_S:
			return "D";
		case SimpleDom.cycle_S:
			return "C";
		default:
			return "U";
		}
	}
	
	//////////////////////////////////////////////////
	//Container Sizes
	public static final int notContainer = -1;
	public static final int emptyContainer = 0;
	public static final int alwaysNonemptyContainer = -2;
	public static final int maybeEmptyContainer = -3;

	public static int joinContainerSizes(int cs1, int cs2)
	{
		if(cs1 == notContainer)
			return cs2;
		else if(cs2 == notContainer)
			return cs1;
		else
		{
			if(cs1 == cs2)
				return cs1;
			else if(cs1 == alwaysNonemptyContainer && cs2 == alwaysNonemptyContainer)
				return alwaysNonemptyContainer;
			else
				return maybeEmptyContainer;
		}
	}

	public static String getContainerStr(int s)
	{
		if(s >= 0)
			return Integer.toString(s);
		else if(s == -2)
			return "+0+";
		else if(s == -3)
			return "=0+";
		else
			return "";
	}
}
