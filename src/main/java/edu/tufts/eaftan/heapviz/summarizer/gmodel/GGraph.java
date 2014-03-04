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

public final class GGraph
{
	private HashSet<GNode> nodes;
	private HashSet<GEdge> edges;
	private LinkedList<RefTarget> refs;
	
	public GGraph(HashSet<GNode> tn, HashSet<GEdge> te, LinkedList<RefTarget> tvt)
	{
		this.nodes = tn;
		this.edges = te;
		this.refs = tvt;
	}
	
	private static void doDFSVisit(GNode n)
	{
		if(n.getColor() != GNode.colorWhite)
			return;

		n.setColor(GNode.colorBlack);
		
		for(GEdge ee : n.getOutEdges())
			doDFSVisit(ee.getEnd());

		n.setTimeStamp(GNode.timeCtr++);
	}
	
	private void doDFS()
	{
		for(GNode n : this.nodes)
			doDFSVisit(n);
	}

	private static void doDFSRevVisitBuildSCC(GNode n, Vector<GNode> pscc)
	{
		if(n.getColor() != GNode.colorWhite)
			return;

		n.setColor(GNode.colorBlack);
		pscc.add(n);
		
		for(GEdge ee : n.getOutEdges())
			doDFSVisit(ee.getEnd());

		n.setTimeStamp(GNode.timeCtr++);
	}
	
	private void computeSCCs(Vector<Vector<GNode>> sccs)
	{
		for(GNode nn : this.nodes)
			nn.initTraversalInfo();
		
		Vector<GNode> topov = new Vector<GNode>();
		this.topoSort(topov);
		
		for(GNode nn : this.nodes)
			nn.initTraversalInfo();
		
		Vector<GNode> nscc = new Vector<GNode>();
		for(int i = topov.size() - 1; i >= 0; --i)
		{
			doDFSRevVisitBuildSCC(topov.get(i), nscc);
			if(nscc.size() >= 2)
			{
				sccs.add(nscc);
				nscc = new Vector<GNode>();
			}
			else
				nscc.clear();
		}
	}
	
	//sort the graph psudo-topologically put the results into sorted
	private void topoSort(Vector<GNode> sorted)
	{
		this.doDFS();
		
		sorted.addAll(this.nodes);
		TopoComparator c = new TopoComparator();
		Collections.sort(sorted, c);
	}
	
	private void setSourceID(int ts) 
	{
		for(GNode n : this.nodes)
			n.setSource(ts);
		
		for(GEdge e : this.edges)
			e.setSource(ts);
	}
	
	private void clearSourceID() 
	{
		for(GNode n : this.nodes)
			n.clearSource();
		
		for(GEdge e : this.edges)
			e.clearSource();
	}
	
	/**
	 * A generic node merge, clears one of the nodes, if there are no edges between it is tn2 that is cleared.
	 * Call this whenever you need to merge nodes.
	 * @param tn1
	 * @param tn2
	 */
	private void mergeNodes(GNode tn1, GNode tn2)
	{
		HashSet<GEdge> ebtn1n2 = new HashSet<GEdge>();
		for(GEdge oe : tn1.getOutEdges())
		{
			if(oe.getEnd() == tn2)
				ebtn1n2.add(oe);
		}

		HashSet<GEdge> ebtn2n1 = new HashSet<GEdge>();
		for(GEdge oe : tn2.getOutEdges())
		{
			if(oe.getEnd() == tn1)
				ebtn2n1.add(oe);
		}

		HashSet<GEdge> allebt = new HashSet<GEdge>();
		allebt.addAll(ebtn1n2);
		allebt.addAll(ebtn2n1);
		
		if(ebtn1n2.size() == 0 && ebtn2n1.size() == 0) //case 1 no ebt edges
		{
			tn1.joinNodeInto(tn2);
			this.nodes.remove(tn2);
			tn2.clear();
		}
		else if(ebtn1n2.size() != 0 && ebtn2n1.size() == 0)
		{
			tn1.mergeConnNodeInto(tn2, ebtn1n2, false);
			this.nodes.remove(tn2);
			tn2.clear();
		}
		else if(ebtn1n2.size() == 0 && ebtn2n1.size() != 0)
		{
			tn2.mergeConnNodeInto(tn1, ebtn2n1, false);
			this.nodes.remove(tn1);
			tn1.clear();
		}
		else
		{
			tn1.mergeConnNodeInto(tn2, allebt, true);
			this.nodes.remove(tn2);
			tn2.clear();
		}

		//clear all the ebt edges
		for(GEdge ee : allebt)
		{
			this.edges.remove(ee);
			ee.clear();
		}
	}

	/**
	 * If we merge 2 edges we need to structurally remove one from the graph. This handles that (and the casewise issues that come up).
	 * @param e
	 */
	private void removeEdgeFromStartLoc(GEdge e)
	{
		if(e.isPtrEdge())
			e.getStart().removeOutEdge(e);
		else
		{
			for(RefTarget rt : this.refs)
			{
				if(rt.isTargetForStorageLoc(e.getEdgeRef()))
				{
					rt.removeTargetEdge(e);
					return;
				}
			}
		}
	}
	
	/**
	 * Join 2 edges, merge eRemove into eRemain, clear eRemove.
	 * @param eRemain The edge to merge into.
	 * @param eRemove The edge to remove (if the merge is done).
	 */
	private void joinEdges(GEdge eRemain, GEdge eRemove)
	{
		if(eRemain.getEnd() != eRemove.getEnd())
			this.mergeNodes(eRemain.getEnd(), eRemove.getEnd());
			
		//must test before modification
		boolean aredj = eRemain.getEnd().areEdgesDisjoint(eRemain, eRemove);
		
		eRemain.getEnd().joinEdgeConnDataInto(eRemain, eRemove);
		
		eRemain.joinEdgeIntoThis(eRemove, aredj);
		
		this.removeEdgeFromStartLoc(eRemove);
		this.edges.remove(eRemove);
		eRemove.clear();
	}

	/**
	 * Given a vector of edges, sort them based on topological order of the end nodes then join (as needed) them in order
	 * @param toNorm the edges to merge as needed.
	 */
	private void doAndReturnUniqueTargetNormSameRefGroup(Vector<GEdge> toNorm)
	{
		//sort in topo order of endpoint to maximize chance of well ordered compression
		Collections.sort(toNorm, new EdgeEndTopoComparator());
		
		for(int i = 0; i < toNorm.size(); ++i)
		{
			GEdge e1 = toNorm.get(i);
			if(! e1.isDead())
			{
				for(int j = i + 1; j < toNorm.size(); ++j)
				{
					GEdge e2 = toNorm.get(j);
					if(e2.isDead())
						continue;

					if(GNode.nodeDataInfoEdgeMergeMatch(e1.getEnd(), e2.getEnd()))
						this.joinEdges(e1, e2);
				}
			}
		}
	}

	/**
	 * Do all the required edge/node merging for a reference.
	 * @param rt
	 */
	private void mergeAllAmbigNameRefTargets(RefTarget rt)
	{
		Vector<GEdge> tee = new Vector<GEdge>(rt.getTargets());
		this.doAndReturnUniqueTargetNormSameRefGroup(tee);
	}

	/**
	 * Do all the required edge/node merging for all of the references in a node.
	 * @param nn
	 */
	private void mergeAmbigNodeFieldTargets(GNode nn)
	{
		HashMap<RCCRefName, Vector<GEdge>> mergeMap = new HashMap<RCCRefName, Vector<GEdge>>();
	
		for(GEdge oe : nn.getOutEdges())
		{
			RCCRefName f = oe.getEdgeRef();
			if(! mergeMap.containsKey(f))
				mergeMap.put(f, new Vector<GEdge>());
			
			mergeMap.get(f).add(oe);
		}
		
		for(Vector<GEdge> mrg : mergeMap.values())
			this.doAndReturnUniqueTargetNormSameRefGroup(mrg);
	}

	/**
	 * Do all the required edge/node merging for all the nodes in the graph.
	 * @param nn All nodes in the graph in topological order.
	 */
	private void doMergeAllAmbigFieldTargets(Vector<GNode> topop)
	{
		Iterator<GNode> pp = topop.iterator();
		while(pp.hasNext())
		{
			GNode nn = pp.next();
			if(! nn.isDead())
				this.mergeAmbigNodeFieldTargets(nn);
		}
	}

	/**
	 * Starting with node n, repeatedly merge recursive children into n.
	 * @param n
	 */
	private void doRecTypesNormSingle(GNode n)
	{
		boolean chtt;
		do
		{
			chtt = false;
			for(GEdge oe : n.getOutEdges())
			{
				GNode np = oe.getEnd();
				if(np.isDead())
					continue;

				boolean recReachTrue = (! Collections.disjoint(n.getNodeTypes(), np.getRecReachTypeSet()));
				boolean recAdjTrue = (! Collections.disjoint(n.getNodeTypes(), np.getNodeTypes()));
				
				if(recReachTrue | recAdjTrue)
				{
					chtt = true;
					this.mergeNodes(n, np);
					break;
				}
			}
		} while(chtt & (! n.isDead()));
	}

	/**
	 * Compress all the recursive structures in this graph. In current definition each structure is reduced to a 
	 * single node.
	 */
	private void doRecTypesNormAll()
	{
		RCCType.resetAllTypeRecInfo();
		
		GNode.timeCtr = 0;
		//init all the containers etc for computing rec-reachable types and such
		for(GNode nn : this.nodes)
		{
			nn.initTraversalInfo();
			nn.initTypeInfo();
			nn.setPending();
		}
		
		//get topo information
		Vector<GNode> pTopoSort = new Vector<GNode>();
		this.topoSort(pTopoSort);

		boolean changed;
		LinkedList<GNode> ntp = new LinkedList<GNode>(pTopoSort); //large finish at start so we pop from end and insert at front

		//comptue all rec reachable types
		while(ntp.size() != 0)
		{
			GNode nodev = ntp.getLast();
			ntp.removeLast();
			nodev.clearPending();

			changed = false;
			for(GEdge oe : nodev.getOutEdges())
				changed |= nodev.propigateRecReachInfo(oe.getEnd());
			
			if(changed)
			{
				for(GEdge ine : nodev.getInEdges())
				{
					if(ine.isPtrEdge())
					{
						GNode startn = ine.getStart();
						if(! startn.isPending())
						{
							startn.setPending();
							ntp.addFirst(startn);
						}
					}
				}
			}
		}

		//Since we are computing rec types online we need to look at each node and check the recursiveness of all the types.
		for(GNode nn : this.nodes)
		{
			for(RCCType tt : nn.getNodeTypes())
			{
				if(nn.getRecReachTypeSet().contains(tt))
					tt.markTypeAsRec();
			}
		}
		
		//now rec-norm everyone
		for(int i = 0; i < pTopoSort.size(); ++i)
			this.doRecTypesNormSingle(pTopoSort.get(i));
	}
	
	/**
	 * Compute all SCC's in this heap model and replace them with single nodes.
	 */
	private void mergeAllSccComponents()
	{
		Vector<Vector<GNode>> sccs = new Vector<Vector<GNode>>();
		this.computeSCCs(sccs);
		
		for(Vector<GNode> scc : sccs)
		{
			GNode trr = scc.firstElement();
			for(int i = 1; i < scc.size(); ++i)
			{
				GNode onn = scc.get(i);
				HashSet<GEdge> ebt = new HashSet<GEdge>();
				
				for(GEdge oe : trr.getOutEdges())
				{
					if(oe.getEnd() == onn)
						ebt.add(oe);
				}

				for(GEdge oe : onn.getOutEdges())
				{
					if(oe.getEnd() == trr)
						ebt.add(oe);
				}
				
				trr.mergeConnNodeInto(onn, ebt, true);
			}
		}
	}
	
	/**
	 * The normalize method to call from userland.
	 */
	public void normalize()
	{
		RCCType.resetAllTypeRecInfo();
		//as long as we are mergeing nodes or edges keep going 
		int thisDeadCT = this.nodes.size() + this.edges.size();
		int lastDeadCT;
		do
		{
			//Remove all cycles
			this.mergeAllSccComponents();
			
			//Now RecCompress
	 		this.doRecTypesNormAll();

	 		Vector<GNode> pTopoSort = new Vector<GNode>();
			this.topoSort(pTopoSort);
			
	 		//Merge static first since they are nice anchors in most programs
	 		for(RefTarget rt : this.refs)
			{
				if(rt.isStaticRef())
					this.mergeAllAmbigNameRefTargets(rt);
			}
	 		
	 		//now do all local vars
	 		for(RefTarget rt : this.refs)
			{
				if(! rt.isStaticRef())
					this.mergeAllAmbigNameRefTargets(rt);
			}

	 		//merge all the ambig ptr fields
			this.doMergeAllAmbigFieldTargets(pTopoSort);
			
	 		lastDeadCT = thisDeadCT;
			thisDeadCT = this.nodes.size() + this.edges.size();
		}while(thisDeadCT != lastDeadCT);		
	}
	
	/**
	 * Get the ref in this that has the same name as ort. 
	 * @param ort
	 */
	private RefTarget getRefTargetSameAsOther(RefTarget ort)
	{
		for(RefTarget rt : this.refs)
		{
			if(rt.areTargetsForSameStorageLoc(ort))
				return rt;
		}
		
		assert false : "not found? huh?";
		return null;
	}
	
	/**
	 * Merge g2 into this. Destroy both graphs. Call this from userland.
	 * @param g2
	 */
	public void mergeGraphInto(GGraph g2)
	{
		this.setSourceID(SimpleDom.sourceA);
		
		g2.setSourceID(SimpleDom.sourceB);
		
		for(RefTarget rt : this.refs)
		{
			RefTarget ort = g2.getRefTargetSameAsOther(rt);
			rt.mergeOtherRefInto(ort);
		}
		
		this.nodes.addAll(g2.nodes);
		this.edges.addAll(g2.edges);
		
		g2 = null;
		this.normalize();	
		this.clearSourceID();
	}
	
	/**
	 * Process a heap for display.
	 */
	public void processForDisplay()
	{this.normalize();}
	
	/**
	 * Compute the dot string for this graph.
	 * @return
	 */
	public String computeDotDisplayString()
	{
		StringBuilder ds = new StringBuilder();
		StringBuilder edgeStr = new StringBuilder();
		StringBuilder nodeStr = new StringBuilder();

		ds.append("digraph G {\n");

		HashSet<GNode> toPrint = new HashSet<GNode>();
		LinkedList<GNode> pending = new LinkedList<GNode>();

		//put the static vars at the bottom, others where-ever
		ds.append("{ rank = sink; ");
		for(RefTarget rt : this.refs)
		{
			if(rt.isStaticRef())
				rt.nameRefToDotString(ds, edgeStr, toPrint, pending);
			else
				rt.nameRefToDotString(nodeStr, edgeStr, toPrint, pending);
		}
		ds.append("}\n\n");

		while(pending.size() != 0)
		{
			HashMap<RCCRefName, String> nullityStrMap = new HashMap<RCCRefName, String>();
			GNode ni = pending.getFirst();
			pending.removeFirst();

			String nStr = ni.stringifyIntoGViz(nullityStrMap);

			nodeStr.append("n_");
			nodeStr.append(ni.getNodeID());
			nodeStr.append(" [shape=record, label=\"");
			nodeStr.append(nStr);
			nodeStr.append("\"];\n");
			
			//add the info for the internal edges
			for(RCCRefName ifd : ni.getIntOffsets())
			{
				edgeStr.append("n_" + ni.getNodeID() + " -> " + "n_" + ni.getNodeID());

				edgeStr.append(" [label=\"[");

				edgeStr.append(ifd.toString());
				
				edgeStr.append("]\"];\n");
			}

			//add the info for the out edge
			for(GEdge ee : ni.getOutEdges())
			{
				GNode nn = ee.getEnd();
				String conns = nn.stringifyConnInfoInto(ee.getEdgeID());
				
				ee.stringifyIntoGViz(edgeStr, conns, nullityStrMap.get(ee.getEdgeRef()), null);

				if(! toPrint.contains(nn))
				{
					toPrint.add(nn);
					pending.add(nn);
				}
			}
		}

		ds.append(nodeStr);
		ds.append(edgeStr);
		ds.append("}\n");
		
		return ds.toString();
	}
  
  /**
   * Compute the GraphML string for this graph.
   * 
   * @return A string representing this graph in GraphML format
   */
  public String computeGraphMLDisplayString() {
    StringBuilder sb = new StringBuilder();    
    HashSet<GNode> toPrint = new HashSet<GNode>();
    LinkedList<GNode> pending = new LinkedList<GNode>();

    // header
    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    sb.append("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"\n");
    sb.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
    sb.append("xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns\n");
    sb.append("http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">\n\n");
 
    // data schema
    sb.append("<key id=\"types\" for=\"node\" attr.name=\"types\" attr.type=\"string\"/>\n");
    sb.append("<key id=\"linearity\" for=\"node\" attr.name=\"linearity\" attr.type=\"int\"/>\n");
    sb.append("<key id=\"shapeType\" for=\"node\" attr.name=\"shapeType\" attr.type=\"string\"/>\n");
    sb.append("<key id=\"cardinality\" for=\"node\" attr.name=\"cardinality\" attr.type=\"string\"/>\n");
    sb.append("<key id=\"roottype\" for=\"node\" attr.name=\"roottype\" attr.type=\"string\"/>\n");
    sb.append("<key id=\"label\" for=\"edge\" attr.name=\"label\" attr.type=\"string\"/>\n");
    sb.append("<key id=\"nullable\" for=\"edge\" attr.name=\"nullable\" attr.type=\"boolean\"/>\n\n");
    
    // start graph
    sb.append("<graph edgedefault=\"directed\">\n\n");

    // roots
    for (RefTarget rt : this.refs) {
      // generate "dummy nodes" for each root
      // TODO: rework
      sb.append(rt.stringifyIntoGraphML(toPrint, pending));
    }

    // traverse graph
    while (pending.size() != 0) {
      HashMap<RCCRefName, String> nullityStrMap = new HashMap<RCCRefName, String>();
      GNode ni = pending.getFirst();
      pending.removeFirst();

      sb.append(ni.stringifyIntoGraphML(nullityStrMap));
      
      // self edges
      for (RCCRefName ifd : ni.getIntOffsets()) {
        sb.append("<edge source=\"");
        sb.append(ni.getNodeID());
        sb.append("\" target=\"");
        sb.append(ni.getNodeID());
        sb.append("\">\n");
        sb.append("  <data id=\"label\">");
        sb.append("[");
        sb.append(ifd.toString());
        sb.append("]</data>\n");
        sb.append("</edge>");
      }

      // out edges
      for (GEdge ee : ni.getOutEdges()) {
        GNode nn = ee.getEnd();
        String conns = nn.stringifyConnInfoInto(ee.getEdgeID());
        
        sb.append(ee.stringifyIntoGraphML(conns, nullityStrMap.get(ee.getEdgeRef()), null));

        if (!toPrint.contains(nn)) {
          toPrint.add(nn);
          pending.add(nn);
        }
      }
    }

    sb.append("</graph>\n</graphml>");
    return sb.toString();
  }
  
}

final class TopoComparator implements Comparator<GNode>
{
	public int compare(GNode n1, GNode n2)
	{return n2.getTimeStamp() - n1.getTimeStamp();}
	
	public boolean equals(Object o)
	{return o instanceof TopoComparator;}
}

final class EdgeEndTopoComparator implements Comparator<GEdge>
{
	public int compare(GEdge e1, GEdge e2)
	{
		assert e1.getEnd().getTimeStamp() != -1 && e2.getEnd().getTimeStamp() != -1 : "not topo thinged";
		
		return e2.getEnd().getTimeStamp() - e1.getEnd().getTimeStamp();
	}
	
	public boolean equals(Object o)
	{return o instanceof EdgeEndTopoComparator;}
}



