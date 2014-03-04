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

/*
public final class InternalConnRelation
{
	public InternalConnRelation()
	{;}
	
	public void clear()
	{;}
	
	@Override
	public String toString()
	{return "{}";}
	
	public boolean areEdgesDisjoint(int e1, int e2)
	{return false;}
	
	public String stringifyRelatedConnInto(int te)
	{return "(all)";}

	public void joinConnInfoInto(InternalConnRelation ocr, LinkedList<GEdge> tev, LinkedList<GEdge> oev)
	{;}

	public void mergeConnInfoIntoOther(InternalConnRelation ocr)
	{;}

	public void joinEdgeConnDataInto(int eRemain, int eRemove)
	{;}

	public boolean areAllEdgesDisjointInSet(HashSet<GEdge> ebt)
	{
		return ebt.size() == 1;
	}
}
*/

public final class InternalConnRelation
{
	private HashMap<Integer, TreeSet<Integer>> disj;
	
	public InternalConnRelation()
	{this.disj = new HashMap<Integer, TreeSet<Integer>>();}
	
	public void clear()
	{;}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("{??}");
		return sb.toString();
	}
	
	public boolean areEdgesDisjoint(int e1, int e2)
	{
		if(this.disj.containsKey(e1))
			return this.disj.get(e1).contains(e2);
		else
			return false;
	}
	
	public String stringifyRelatedConnInto(int te)
	{	
		if(! this.disj.containsKey(te))
			return "";
		
		StringBuilder sb = new StringBuilder();
		TreeSet<Integer> tt =  this.disj.get(te);
		
		boolean first = true;
		for(Integer cp : tt)
		{
			if(! first)
				sb.append(", ");
			else
				first = false;

			sb.append(cp);
		}
		
		return sb.toString();
	}

	public void joinConnInfoInto(InternalConnRelation ocr, LinkedList<GEdge> tev, LinkedList<GEdge> oev)
	{
		this.disj.putAll(ocr.disj);
		
		for(GEdge ee1 : tev)
		{
			if(! this.disj.containsKey(ee1.getEdgeID()))
				this.disj.put(ee1.getEdgeID(), new TreeSet<Integer>());
			
			TreeSet<Integer> tts = this.disj.get(ee1.getEdgeID());
			for(GEdge ee2 : oev)
				tts.add(ee2.getEdgeID());
		}
			
		for(GEdge ee2 : oev)
		{
			if(! this.disj.containsKey(ee2.getEdgeID()))
				this.disj.put(ee2.getEdgeID(), new TreeSet<Integer>());
			
			TreeSet<Integer> ots = this.disj.get(ee2.getEdgeID());
			for(GEdge ee1 : tev)
				ots.add(ee1.getEdgeID());
		}
	}

	public void mergeConnInfoIntoOther(InternalConnRelation ocr)
	{this.disj.clear();}

	public void joinEdgeConnDataInto(int eRemain, int eRemove)
	{
		this.disj.remove(eRemove);
		
		for(Map.Entry<Integer, TreeSet<Integer>> ttve : this.disj.entrySet())
		{
			if(ttve.getKey() == eRemain)
				ttve.getValue().remove(eRemove);
			else
			{
				if((! ttve.getValue().contains(eRemain)) || (! ttve.getValue().contains(eRemove)))
					ttve.getValue().remove(eRemain);
				ttve.getValue().remove(eRemove);
			}
		}
	}
	
	public boolean areAllEdgesDisjointInSet(HashSet<GEdge> ebt)
	{
		Iterator<GEdge> ebti = ebt.iterator();
		while(ebti.hasNext())
		{
			int eidi = ebti.next().getEdgeID();
			Iterator<GEdge> ebtj = ebt.iterator();
			while(ebtj.hasNext())
			{
				int eidj = ebtj.next().getEdgeID();
				if(eidi != eidj && (! this.areEdgesDisjoint(eidi, eidj)))
					return false;
			}
		}
		
		return true;
	}
}

