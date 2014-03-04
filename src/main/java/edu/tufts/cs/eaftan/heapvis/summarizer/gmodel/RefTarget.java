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

package edu.tufts.cs.eaftan.heapvis.summarizer.gmodel;

import java.util.*;

/**
 * A RefTarget represents a root or static reference
 *
 */
public final class RefTarget
{
	private boolean isStaticRef;
	private boolean mayNull;
	private LinkedList<GEdge> targets;
	private RCCRefName name;
	
	/**
	 * Build a root variable or static variable reference.
	 * @param v The name of the var.
	 * @param tl Edge representing the reference target (null if the reference is null). 
	 * @param isStatic True if the reference is a static variable, false if a local var.
	 */
	public RefTarget(RCCRefName v, GEdge tl, boolean isStatic)
	{
		this.isStaticRef = isStatic;
		this.mayNull = (tl == null);
		
		this.targets = new LinkedList<GEdge>();
		if(tl !=null)
			this.targets.add(tl);
		
		this.name = v;
	}
	
	public boolean mustBeNullRef()
	{return this.targets.size() == 0;}
	
	public boolean mayBeNullRef()
	{return this.mayNull;}
	
	public LinkedList<GEdge> getTargets()
	{return this.targets;}
	
	/**
	 * After an edge join need to remove the merged into edge, use this.
	 * @param e
	 */
	public void removeTargetEdge(GEdge e)
	{this.targets.remove(e);}
	
	public boolean areTargetsForSameStorageLoc(RefTarget ort)
	{return (this.name == ort.name);}
	
	public boolean isTargetForStorageLoc(RCCRefName v)
	{return (this.name == v);}
	
	public boolean isStaticRef()
	{return this.isStaticRef;}
	
	public void mergeOtherRefInto(RefTarget rt)
	{
		assert this.areTargetsForSameStorageLoc(rt) : "refs don't match";
		
		this.mayNull |= rt.mayNull;
		this.targets.addAll(rt.targets);
		
		rt.targets.clear();
	}
	
	public void nameRefToDotString(StringBuilder nodeStr, StringBuilder edgeStr, HashSet<GNode> toPrint, LinkedList<GNode> pending)
	{
		String vname;
		if(this.isStaticRef())
		{
			vname = this.name.toString();
			nodeStr.append("\"" + vname + "\"" + " [shape=invhouse, label=\"");
			nodeStr.append(vname);
		}
		else
		{
			vname = this.name.toString();
			nodeStr.append("\"" + vname + "\"" + " [shape=ellipse, label=\"");
			nodeStr.append(vname);
		}
		

		nodeStr.append("\"];\n");

		for(GEdge te : this.targets)
		{
			GNode nn = te.getEnd();
			String conns = nn.stringifyConnInfoInto(te.getEdgeID());
			
			String nullStr = (this.mayNull) ? "dotted" : "solid";
			te.stringifyIntoGViz(edgeStr, conns, nullStr, "\"" + vname + "\"");

			if(! toPrint.contains(nn))
			{
				toPrint.add(nn);
				pending.add(nn);
			}
		}
	}
  
  /**
   * Create a GraphML string from this RefTarget
   * 
   * @return a string in GraphML format that represents this node
   */
  public String stringifyIntoGraphML(HashSet<GNode> toPrint, LinkedList<GNode> pending) {

    StringBuilder sb = new StringBuilder();
    
    // node
    sb.append("<node id=\"");
    sb.append(this.name.toString());
    sb.append("\">\n");
    sb.append("  <data key=\"roottype\">");
    if (this.isStaticRef()) {
      sb.append("static");
    } else {
      sb.append("stack");
    }
    sb.append("</data>\n</node>\n");

    // edges
    for (GEdge te : this.targets) {
      GNode nn = te.getEnd();
      String conns = nn.stringifyConnInfoInto(te.getEdgeID());
      
      String nullStr = (this.mayNull) ? "dotted" : "solid";
      te.stringifyIntoGraphML(conns, nullStr, "\"" + this.name.toString() + "\"");

      if(! toPrint.contains(nn))
      {
        toPrint.add(nn);
        pending.add(nn);
      }
    }
    
    return sb.toString();
    
  }

}



