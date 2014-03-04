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

public final class GNode
{
	private static int nodeIDCtr = 0;
	
	private int nodeID;
	private int sourceID;
	
	//Internal connectivity information stuff.
	//What fields may have self edges, and which in edges may be connected.
	private HashSet<RCCRefName> intOffsets;
	private InternalConnRelation ici;

	//The nullity of each field in this object.
	private HashMap<RCCRefName, Integer> nullityInfo;
	
	//Other properties
	//The types, number, possible container sizes of objects represented by this node. 
	private HashSet<RCCType> types; 
	private int count;
	private int containerSize;
	
	//The internal shape of the region.
	private int tshape;

	//connectivity
	private LinkedList<GEdge> inEdges;
	private LinkedList<GEdge> outEdges;

	///////////////////////////////////////////////
	//Traversal support	
	public static final int colorWhite = 0;
	public static final int colorBlack = 1;
	
	public static int timeCtr = 0;
	
	private int color;
	private int timestamp;
	private boolean pending;
	
	//Set of of types that are reachable from this node (does not include the types that only appear in this node).
	private HashSet<RCCType> recReachSet;
	
	
	//////////////////////////////////////////////////////////////////////////////////
	private GNode()
	{
		this.nodeID = GNode.nodeIDCtr++;
		this.sourceID = SimpleDom.sourceClear;
		
		this.intOffsets = new HashSet<RCCRefName>();
		this.ici = new InternalConnRelation();
		this.nullityInfo = new HashMap<RCCRefName, Integer>();
		this.types = new HashSet<RCCType>();
		this.count = 1;
		this.containerSize = -1;
		this.tshape = SimpleDom.singleton_S;

		this.inEdges = new LinkedList<GEdge>();
		this.outEdges = new LinkedList<GEdge>();
	}
	
	/**
	 * Construct a node of the given type with the corresponding fields, For Non-Collection Objects since it does not understand cardinality.
	 * Initially all fields are null, we incrementally add edges later.
	 * @param tt Type of the object represented by the node.
	 * @param fields Fields that are associated with the object.
	 */
	public GNode(RCCType tt, Vector<RCCRefName> fields)
	{
		this();
		this.types.add(tt);
		this.containerSize = SimpleDom.notContainer;
		
		
		for(RCCRefName rn : fields)
			this.nullityInfo.put(rn, SimpleDom.mustNull);
	}
	
	/**
	 * Construct a node to represent a collection object (Array, Vector, List, Set, Map).
	 * Initially assumed to contain all null values, instantiate contents later.
	 * @param tt Type of the collection.
	 * @param summaryfield The name of the summary field representing all objects in the container.
	 * @param cSize The number of entries in the collection.
	 */
	public GNode(RCCType tt, RCCRefName summaryfield, int cSize)
	{
		this();
		this.types.add(tt);
		this.containerSize = cSize;
	
		if(summaryfield != null)
			this.nullityInfo.put(summaryfield, SimpleDom.mustNull);
	}
	
	public void clear()
	{
		this.nodeID = -1;
		this.sourceID = SimpleDom.sourceClear;
		
		this.intOffsets.clear();
		this.ici.clear();
		this.nullityInfo.clear();
		this.types.clear();
		this.count = 1;
		this.containerSize = -1;
		this.tshape = SimpleDom.singleton_S;

		this.inEdges.clear();
		this.outEdges.clear();
	}
	
	//Is the node dead.
	public boolean isDead()
	{return this.nodeID == -1;}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("(");
		boolean first;

		sb.append(this.nodeID + ": {");
		
		first = true;
		for(RCCType ot : this.types)
		{
			if(first)
				first = false;
			else
				sb.append(", ");
			sb.append(ot.toString());
		}

		sb.append("}, " + SimpleDom.linearityToString(this.count) + ", " + SimpleDom.getShapeTypeStr(this.tshape));
		
		if(this.containerSize != -1)
			sb.append("|" + SimpleDom.getContainerStr(this.containerSize) + "|)");
		else
			sb.append(")");
		
		return sb.toString();
	}
	
	/**
	 * Add the given edge to the field and set the nullity value as needed.
	 * @param ee
	 */
	public void addOutEdge(GEdge ee)
	{
		this.nullityInfo.put(ee.getEdgeRef(), SimpleDom.mustNonNull);
		this.outEdges.add(ee);
	}
	
	/**
	 * Assert that the container does/does not contain null/non-null values as given by the flag.
	 * @param name Special field name for container contents.
	 * @param doesContainerHaveNullValue See constants defined SimpleDom for ways to instantiate this. Must always do this for containers.
	 */
	public void setContainerLocToMayNonNull(RCCRefName name, int doesContainerHaveNullValue)
	{this.nullityInfo.put(name, doesContainerHaveNullValue);}
	
	/**
	 * Add the given in edge to this node.
	 * @param ine
	 */
	public void addInEdgeRelAll(GEdge ine)
	{this.inEdges.add(ine);}
	
	/**
	 * Do a structural removal of oute from this (e.g. only ok if nullity is updated elsewhere like in joinEdges).
	 * @param oute
	 */
	public void removeOutEdge(GEdge oute)
	{this.outEdges.remove(oute);}
	
	public int getNodeID()
	{return this.nodeID;}
	
	public int getSource()
	{return this.sourceID;}
	
	public void setSource(int newid) 
	{this.sourceID = newid;}
	
	public void clearSource() 
	{this.sourceID = SimpleDom.sourceClear;}
	
	/**
	 * Return true if all the types represented by this node are NON-RECURSIVE.
	 */
	public boolean allNonRecTypes()
	{
		for(RCCType tt : this.types)
		{
			if(tt.isTypeRec())
				return false;
		}
		
		return true;
	}
	
	/**
	 * Determine if the two nodes are similar with respect to recursive properties and are merge candidates.
	 * @return True if the are similar in this respect.
	 */
	public static boolean areRecSimilar(GNode n1, GNode n2)
	{return n1.allNonRecTypes() == n2.allNonRecTypes();}
	
	/**
	 * Determine if two in edges have the same set of var referring to them.
	 * @return True if the nodes are similar in this respect.
	 */
	private static boolean areVarRefSimilar(GNode n1, GNode n2)
	{
		HashSet<RCCRefName> inves1 = new HashSet<RCCRefName>();
		for(GEdge ie1 : n1.inEdges)
		{
			if(ie1.isVarEdge())
				inves1.add(ie1.getEdgeRef());
		}
		
		HashSet<RCCRefName> inves2 = new HashSet<RCCRefName>();
		for(GEdge ie2 : n2.inEdges)
		{
			if(ie2.isVarEdge())
				inves2.add(ie2.getEdgeRef());
		}
		
		return inves1.equals(inves2);
	}
	
	/**
	 * Test if the two nodes are similar.
	 * @return True if they are similar according to our heuristics.
	 */
	public static boolean nodeDataInfoEdgeMergeMatch(GNode n1, GNode n2)
	{return areVarRefSimilar(n1, n2) && areRecSimilar(n1, n2);}

	public String stringifyIntoGViz(HashMap<RCCRefName, String> nullityStrMap)
	{
		StringBuilder sb = new StringBuilder();
		boolean first;

		sb.append(this.nodeID + " | ");

		first = true;
		for(RCCType ot : this.types)
		{
			if(first)
				first = false;
			else
				sb.append(" ");
			sb.append(ot.getTypeStrForDot());
		}

		sb.append(" | " + SimpleDom.linearityToString(this.count));
		
		sb.append(" | " + SimpleDom.getShapeTypeStr(this.tshape));

		sb.append(this.containerSize != -1 ? (" | card-" + SimpleDom.getContainerStr(this.containerSize)) : "");
		
		for(RCCRefName fd : this.nullityInfo.keySet())
		{
			int vv = this.nullityInfo.get(fd).intValue();
			String dv = (vv == SimpleDom.mustNonNull) ? "solid" : "dotted";
			nullityStrMap.put(fd, dv);
		}
		
		return sb.toString();
	}
  
  /**
   * Create a GraphML string from this node
   * 
   * @param nullityStrMap  ??? an output parameter
   * @return a string in GraphML format that represents this node
   */
  public String stringifyIntoGraphML(HashMap<RCCRefName, String> nullityStrMap)
  {
    StringBuilder sb = new StringBuilder();
    
    sb.append("<node id=\"");
    sb.append(this.nodeID);
    sb.append("\">\n");

    // types
    // TODO: more than one data element with same key? i.e. multiple types
    sb.append("  <data key=\"types\">");
    boolean first = true;
    for (RCCType ot : this.types)
    {
      if (first) {
        first = false;
      } else {
        sb.append(" ");
      }
      sb.append(ot.getTypeStrForDot());
    }
    sb.append("</data>\n");
    
    // linearity
    sb.append("  <data key=\"linearity\">");
    sb.append(SimpleDom.linearityToString(this.count));
    sb.append("</data>\n");
    
    // shape type
    sb.append("  <data key=\"shapeType\">");
    sb.append(SimpleDom.getShapeTypeStr(this.tshape));
    sb.append("</data>\n");
    
    // cardinality of containers
    if (this.containerSize != -1) {
      sb.append("  <data key=\"cardinality\">card-");
      sb.append(SimpleDom.getContainerStr(this.containerSize));
      sb.append("</data>\n");
    }
   
    // compute nullityStrMap 
    // TODO: what is this?
    for(RCCRefName fd : this.nullityInfo.keySet())
    {
      int vv = this.nullityInfo.get(fd).intValue();
      String dv = (vv == SimpleDom.mustNonNull) ? "solid" : "dotted";
      nullityStrMap.put(fd, dv);
    }
    
    sb.append("</node>\n");
       
    return sb.toString();
  }
  
	
	public String stringifyConnInfoInto(int te)
	{return this.ici.stringifyRelatedConnInto(te);}
	
	/**
	 * Take 2 nodes with no edges between them and merge the domain info into this, nd is dead after this and can be freed/invalidated.
	 * @param nd The node to merge into this, is destroyed by this operation.
	 */
	public void joinNodeInto(GNode nd)
	{
		this.intOffsets.addAll(nd.intOffsets);
		this.types.addAll(nd.types);
		
		this.ici.joinConnInfoInto(nd.ici, this.inEdges, nd.inEdges);

		boolean mbss = SimpleDom.mayBeSameSource(this.sourceID, nd.sourceID);
		this.count = SimpleDom.joinLinearityValues(this.count, nd.count, mbss);
		
		this.containerSize = SimpleDom.joinContainerSizes(this.containerSize, nd.containerSize);

		this.sourceID = SimpleDom.joinSourceID(this.sourceID, nd.sourceID);
		this.tshape = SimpleDom.joinShapes(this.tshape, nd.tshape);
		
		for(RCCRefName fd : nd.nullityInfo.keySet())
		{
			int ndnf = nd.nullityInfo.get(fd).intValue();
		
			if(this.nullityInfo.containsKey(fd))
			{
				int tnif = this.nullityInfo.get(fd).intValue();
				this.nullityInfo.put(fd, SimpleDom.joinNullityValues(tnif, ndnf));
			}
			else
				this.nullityInfo.put(fd, ndnf);
		}
		
		//remap edge endpoints/startpoints
		ListIterator<GEdge> iei = nd.inEdges.listIterator();
		while(iei.hasNext())
		{
			GEdge ee = iei.next();
			ee.changeEnd(this);
			this.inEdges.add(ee);
		}
		
		ListIterator<GEdge> oei = nd.outEdges.listIterator();
		while(oei.hasNext())
		{
			GEdge ee = oei.next();
			ee.changeStart(this);
			this.outEdges.add(ee);
		}
		
		if(this.recReachSet != null)
		{
			this.recReachSet.addAll(nd.recReachSet);
			this.timestamp = Math.max(this.timestamp, nd.timestamp);
		}
	}

	/**
	 * Take 2 nodes with some edges between them and merge the domain info into this, nd is dead after this and can be freed/invalidated.
	 * @param nd The node to merge into this, is destroyed by this operation.
	 * @param ebt The edges between this and nd (may be empty).
	 * @param forceCycle True if we just want to assume the shape is a cycle.
	 */
	public void mergeConnNodeInto(GNode nd, HashSet<GEdge> ebt, boolean forceCycle)
	{
		//clear ebt edges
		ListIterator<GEdge> nrebtiei = nd.inEdges.listIterator();
		while(nrebtiei.hasNext())
		{
			if(ebt.contains(nrebtiei.next()))
				nrebtiei.remove();
		}
		
		ListIterator<GEdge> nebtoei = nd.outEdges.listIterator();
		while(nebtoei.hasNext())
		{
			if(ebt.contains(nebtoei.next()))
				nebtoei.remove();
		}
		
		ListIterator<GEdge> rebtiei = this.inEdges.listIterator();
		while(rebtiei.hasNext())
		{
			if(ebt.contains(rebtiei.next()))
				rebtiei.remove();
		}
		
		ListIterator<GEdge> rebtoei = this.outEdges.listIterator();
		while(rebtoei.hasNext())
		{
			if(ebt.contains(rebtoei.next()))
				rebtoei.remove();
		}
		
		boolean allptrDisjoint = nd.ici.areAllEdgesDisjointInSet(ebt);
		int ebtSum = 0;
		
		HashSet<RCCRefName> ebtOffsets = new HashSet<RCCRefName>();
		for(GEdge ebte : ebt)
		{
			allptrDisjoint &= (! ebte.doesEdgeInterfere());
			ebtSum += ebte.getCut();
			ebtOffsets.add(ebte.getEdgeRef());
		}
		
		this.ici.mergeConnInfoIntoOther(nd.ici);

		this.count = SimpleDom.addLinearityValues(this.count, nd.count);
		this.containerSize = SimpleDom.joinContainerSizes(this.containerSize, nd.containerSize);

		this.sourceID = SimpleDom.joinSourceID(this.sourceID, nd.sourceID);
		
		if(forceCycle)
			this.tshape = SimpleDom.joinShapes(SimpleDom.cycle_S, SimpleDom.joinShapes(this.tshape, nd.tshape));
		else
			this.tshape = SimpleDom.joinConnShapes(this.tshape, nd.tshape, allptrDisjoint, ebtSum, this.intOffsets, nd.intOffsets, ebtOffsets);
		
		this.intOffsets.addAll(nd.intOffsets);
		this.intOffsets.addAll(ebtOffsets);
		this.types.addAll(nd.types);
		
		for(RCCRefName fd : nd.nullityInfo.keySet())
		{
			int ndnf = nd.nullityInfo.get(fd).intValue();
		
			if(this.nullityInfo.containsKey(fd))
			{
				int tnif = this.nullityInfo.get(fd).intValue();
				this.nullityInfo.put(fd, SimpleDom.joinNullityValues(tnif, ndnf));
			}
			else
				this.nullityInfo.put(fd, ndnf);
		}
		
		//remap edge endpoints/startpoints
		ListIterator<GEdge> iei = nd.inEdges.listIterator();
		while(iei.hasNext())
		{
			GEdge ee = iei.next();
			ee.changeEnd(this);
			this.inEdges.add(ee);
		}
		
		ListIterator<GEdge> oei = nd.outEdges.listIterator();
		while(oei.hasNext())
		{
			GEdge ee = oei.next();
			ee.changeStart(this);
			this.outEdges.add(ee);
		}

		if(this.recReachSet != null)
		{
			this.recReachSet.addAll(nd.recReachSet);
			this.timestamp = Math.max(this.timestamp, nd.timestamp);
		}
	}
	
	/**
	 * Join the edges and remove eRemove from the inedge list.
	 */
	public void joinEdgeConnDataInto(GEdge eRemain, GEdge eRemove)
	{
		this.ici.joinEdgeConnDataInto(eRemain.getEdgeID(), eRemove.getEdgeID());
		this.inEdges.remove(eRemove);
	}
		
	/**
	 * Iteratively propagate the reachable type information. Return true if the set of reachable types has changed.
	 * @param oNode
	 * @return True if the set of reachable types has changed.
	 */
	public boolean propigateRecReachInfo(GNode oNode)
	{
		boolean change = (! this.recReachSet.containsAll(oNode.recReachSet)) || (! this.recReachSet.containsAll(oNode.types));
		
		if(change)
		{
			this.recReachSet.addAll(oNode.recReachSet);
			this.recReachSet.addAll(oNode.types);
		}

		return change;
	}
	
	public boolean areEdgesDisjoint(GEdge e1, GEdge e2)
	{return this.ici.areEdgesDisjoint(e1.getEdgeID(), e2.getEdgeID());}
	
	public HashSet<RCCType> getNodeTypes()
	{return this.types;}
  
  /**
   * intOffsets holds the names of fields that may have self edges.  If we are 
   * building a graph and notice a self edge, we do not create the edge but
   * we add it to the intOffsets field via this method.
   * 
   * @param field The name of the field with a self edge
   */
  public void addToIntOffsets(RCCRefName field) {
    intOffsets.add(field);
  }

	public HashSet<RCCRefName> getIntOffsets()
	{return this.intOffsets;}
	
	public LinkedList<GEdge> getInEdges() 
	{return this.inEdges;}
	
	public LinkedList<GEdge> getOutEdges()
	{return this.outEdges;}
		
	public void initTraversalInfo()
	{
		this.color = GNode.colorWhite;
		this.timestamp = -1;
		this.pending = false;
	}
	
	public void initTypeInfo()
	{this.recReachSet = new HashSet<RCCType>();}
	
	public void clearTypeInfo()
	{this.recReachSet = null;}
	
	public int getColor()
	{return this.color;}
	
	public void setColor(int cl)
	{this.color = cl;}
	
	public int getTimeStamp()
	{return this.timestamp;}
	
	public void setTimeStamp(int ts)
	{this.timestamp = ts;}
	
	public boolean isPending()
	{return this.pending;}
	
	public void setPending()
	{this.pending = true;}
	
	public void clearPending()
	{this.pending = false;}
	
	public HashSet<RCCType> getRecReachTypeSet()
	{return this.recReachSet;}
  
  public void setTShape(int shape) {
    tshape = shape;
  }
}
