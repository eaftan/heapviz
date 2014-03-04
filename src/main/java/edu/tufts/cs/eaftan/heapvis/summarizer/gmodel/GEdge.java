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

public final class GEdge
{	
	private static int edgeIDCtr = 0;
	
	private int edgeID;
	private boolean isVarName;
	private boolean isStatic;
	private RCCRefName name;
	
	private GNode start;
	private GNode end;
	
	private int cut;
	private boolean isInterfere;
	
	private int sourceID;
	
	public GEdge(RCCRefName fd, boolean isStat, GNode startNode, GNode endNode)
	{
		this.edgeID = GEdge.edgeIDCtr++;
		
		this.isVarName = false;
		this.isStatic = isStat;
		this.name = fd;
	
		this.start = startNode;
		this.end = endNode;
		
		this.cut = 1;
		this.isInterfere = false;
		
		this.sourceID = SimpleDom.sourceClear;
	}
	
	public GEdge(RCCRefName vn, GNode endNode)
	{
		this.edgeID = GEdge.edgeIDCtr++;
		
		this.isVarName = true;
		this.isStatic = false;
		this.name = vn;
		
		this.start = null;
		this.end = endNode;
		
		this.cut = 1;
		this.isInterfere = false;
		
		this.sourceID = SimpleDom.sourceClear;
	}

	public void clear()
	{
		this.edgeID = -1;
		
		this.isVarName = false;
		this.isStatic = false;
		
		this.start = null;
		this.end = null;
		
		this.cut = 1;
		this.isInterfere = false;
		
		this.sourceID = SimpleDom.sourceClear;
	}
	
	public boolean isDead()
	{return this.edgeID == -1;}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("(");
		
		if(this.isPtrEdge())
			sb.append(this.start.getNodeID());
		else
			sb.append(this.name.toString());

		sb.append(" -> " + this.end.getNodeID());

		sb.append(" [" + this.edgeID);

		if(this.isPtrEdge())
			sb.append(", " + this.name.toString());

		sb.append(", " + SimpleDom.linearityToString(this.cut) + ", " + (this.isInterfere ? "ip" : "np") + "]");
		
		return sb.toString();
	}
	
	public boolean isVarEdge() 
	{return this.isVarName;}
	
	public boolean isPtrEdge()
	{return (! this.isVarName) && (! this.isStatic);}
	
	public boolean isStaticEdge()
	{return (! this.isVarName) && (this.isStatic);}
	
	public int getEdgeID()
	{return this.edgeID;}
	
	public int getSource()
	{return this.sourceID;}
	
	public void setSource(int newid) 
	{this.sourceID = newid;}
	
	public void clearSource() 
	{this.sourceID = SimpleDom.sourceClear;}
	
	public GNode getStart()
	{
		assert this.isPtrEdge() : "source only valid for ptr edges";
		return this.start;
	}

	public GNode getEnd()
	{return this.end;}

	public boolean edgeStartsAt(GNode n) 
	{
		assert this.isPtrEdge() : "source only valid for ptr edges";
		return this.start == n;
	}

	public boolean edgeEndsAt(GNode n) 
	{return this.end == n;}

	public void changeStart(GNode ns) 
	{
		assert this.isPtrEdge() : "don't do this";
		this.start = ns;
	}
	
	public void changeEnd(GNode ns)
	{this.end = ns;}

	
	public RCCRefName getEdgeRef()
	{return this.name;}
		
	public int getCut()
	{return this.cut;}
	
	public boolean doesEdgeInterfere()
	{return this.isInterfere;}
	
	public void stringifyIntoGViz(StringBuilder edgeStr, String connStr, String nullStr, String useVName)
	{
		if(useVName == null)
		{
			edgeStr.append("n_");
			edgeStr.append(this.start.getNodeID());
		}
		else
			edgeStr.append(useVName);

		edgeStr.append(" -> n_" + this.end.getNodeID());

		edgeStr.append(" [style=" + nullStr + ", label=\"{" + this.edgeID);

		if(this.isPtrEdge())
			edgeStr.append(", " + this.name.toString());

		edgeStr.append(", " + SimpleDom.linearityToString(this.cut));
		
		if(this.isInterfere)
			edgeStr.append(", ip");
		else
			edgeStr.append(", np");
		
		if(connStr.length() != 0)
			edgeStr.append(", (" + connStr + ")");	

		edgeStr.append("}\"];\n");
	}
  
  /**
   * Return a string that represents this edge in GraphML format
   * 
   * TODO: handle the case that this is a root
   * 
   * @param connStr ???
   * @param nullStr This edge may have null values or not.  If it is nullable then this 
   * value is "dotted", if not it is "solid" 
   * @param useVName If supplied, used as the id of the source node
   * @return A string that represents this edge in GraphML format 
   */
  public String stringifyIntoGraphML(String connStr, String nullStr, String useVName) {
    StringBuilder sb = new StringBuilder();
    
    sb.append("<edge source=\"");
     
    // source id
    if (useVName == null) {
      sb.append(this.start.getNodeID());
    } else {
      sb.append(useVName);
    }
    
    sb.append("\" target=\"");
    
    // target id
    sb.append(this.end.getNodeID());
    sb.append("\">\n");
    
    // nullable?
    sb.append("  <data key=\"nullable\">");
    if (nullStr.equals("dotted")) {
      sb.append("true");
    } else {
      sb.append("false");
    }
    sb.append("</data>\n");
    
    // label
    sb.append("  <data key=\"label\">{");
    sb.append(this.edgeID);
    if (this.isPtrEdge()) {
      sb.append(", " + this.name.toString());
    }
    sb.append(", " + SimpleDom.linearityToString(this.cut));
    if (this.isInterfere) {
      sb.append(", ip");
    } else {
      sb.append(", np");
    }
    if(connStr.length() != 0)
      sb.append(", (" + connStr + ")");  

    sb.append("}</data>\n</edge>\n");
    
    return sb.toString();
  }

	void joinEdgeIntoThis(GEdge oe, boolean areEdgeIndep)
	{
		assert (this.name == oe.name) : "edges not of same type";
		assert this.start == oe.start && this.end == oe.end : "endpoints must be the same";

		//join source at end, so we can use it in the body		
		if(SimpleDom.mayBeSameSource(this.sourceID, oe.sourceID))
		{
			this.cut = SimpleDom.addLinearityValues(this.cut, oe.cut);
			this.isInterfere = (! areEdgeIndep) || this.isInterfere || oe.isInterfere;
		}
		else
		{
			this.cut = SimpleDom.joinLinearityValues(this.cut, oe.cut, false);
			this.isInterfere = this.isInterfere || oe.isInterfere;
		}

		this.sourceID = SimpleDom.joinSourceID(this.sourceID, oe.sourceID);
	}
}
