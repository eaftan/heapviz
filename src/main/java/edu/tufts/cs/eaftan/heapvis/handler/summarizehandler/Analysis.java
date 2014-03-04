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

/**
 * Performs summarization over the object graph
 */

package edu.tufts.cs.eaftan.heapvis.handler.summarizehandler;

import java.util.*;

import edu.tufts.cs.eaftan.util.DuplicateEdgeException;
import edu.tufts.cs.eaftan.util.Graph;
import edu.tufts.cs.eaftan.util.Edge;

public class Analysis {
  
  /**
   * TODO: Is it possible to require more than one pass of either 
   * backbone collapse or parent-child?  i.e. can a parent-child pass
   * cause more opportunities for backbone collapse, or vice versa?
   * 
   * Yes for parent-child.  No (I think) for backbone.
   */
  
  /**
   * Merges the backbones of recursive data structures, for example, the 
   * LinkedList$Node objects in a LinkedList.  We pairwise collapse two 
   * nodes of the same type that are linked by an edge.  We continue this
   * until there are no more such pairs of nodes in the graph.  
   * 
   * TODO: Can this rule collapse things we don't want to be collapsed?
   * LinkedList of LinkedLists example
   * Tree example
   * 
   * @param g The graph whose recursive backbones we want to collapse
   */
  public static boolean mergeBackbones(Graph<Vertex, String> g) {
    
    boolean anychange = false;
    
    boolean changed = true;
    while (changed) {
      changed = false;
      Set<Vertex> vertices = g.getVertices();
      for (Vertex v : vertices) {
        List<Edge<Vertex, String>> outEdges = g.getOutgoingEdges(v);
        
        // children
        for (Edge<Vertex, String> e : outEdges) {
          if (!e.from.equals(e.to)) {   // skip self edges
            if (e.to.repType.equals(v.repType)) {
              // same type
              ArrayList<Vertex> thisList = new ArrayList<Vertex>();
              thisList.add(v);
              thisList.add(e.to);
              mergeVertices(g, thisList);
              anychange = true;
              changed = true;
              break;
            }
          }
        }
        if (changed) break; 
      }
    }
    
    return anychange;
  
  }
  
  /**
   * Goes over the whole graph, computes predecessor sets for each vertex,
   * then merges vertices with the same predecessor sets and the same
   * type.
   * 
   * @param g The graph to simplify
   * @return True if the graph has changed
   */
  public static boolean mergeSamePredecessorsAndType(Graph<Vertex, String> g) {
    
    boolean changed = false;

    /* Generate map of predecessor sets to lists of vertices.  We want to find
     * all vertices with the same predecessors.
     */
    HashMap<Set<Vertex>, ArrayList<Vertex>> data = new HashMap<Set<Vertex>, ArrayList<Vertex>>();
    for (Vertex v : g.getVertices()) {
      Set<Vertex> ps = g.getPredecessors(v);
      ArrayList<Vertex> list = data.get(ps);
      if (list == null) {
        list = new ArrayList<Vertex>();
        data.put(ps, list);
      } 
      list.add(v);
    }
    
    /* Now for each list of vertices with the same predecessors, find those
     * with the same type and merge them.
     */
    for (Set<Vertex> ps : data.keySet()) {
      ArrayList<Vertex> list = data.get(ps);
      HashMap<String, ArrayList<Vertex>> byType = new HashMap<String, ArrayList<Vertex>>();
      for (Vertex v : list) {
        ArrayList<Vertex> list2 = byType.get(v.repType);
        if (list2 == null) {
          list2 = new ArrayList<Vertex>();
          byType.put(v.repType, list2);
        }
        list2.add(v);
      }
      
      for (String type : byType.keySet()) {
        ArrayList<Vertex> toMerge = byType.get(type);
        if (toMerge.size() > 1) {
          mergeVertices(g, toMerge);
          changed = true;
        }
      }
    }
    
    return changed;
  
  }
  
  public static void summarizeGraph(Graph<Vertex, String> g) {
    
    mergeBackbones(g);
    
    boolean changed = true;
    while (changed) {
      changed = false;
      changed = mergeSamePredecessorsAndType(g);
    }
    
  }
  
  /**
   * Given a graph and a list of vertices, merge the vertices
   * 
   * TODO: how to summarize edge labels?  do we even need them?
   * 
   * @param g The input graph
   * @param vertices A list of vertices of the same type to merge
   */
  public static void mergeVertices(Graph<Vertex, String> g, List<Vertex> vertices) {
    
    // create new summary vertex
    Vertex newVertex = Vertex.merge(vertices);

    // find all predecessors and successors of the new vertex
    // TODO: what to do with self edges?  right now we're deleting them
    HashSet<Vertex> preds = new HashSet<Vertex>();
    HashSet<Vertex> succs = new HashSet<Vertex>();
    for (Vertex v : vertices) {
      List<Edge<Vertex, String>> edges = g.getEdges(v);
      for (Edge<Vertex, String> e : edges) {
        if (e.from.equals(v) && !vertices.contains(e.to)) {
          // outgoing
          succs.add(e.to);
        } else if (e.to.equals(v) && !vertices.contains(e.from)) {
          // incoming
          preds.add(e.from);
        } 
      }
      g.removeVertex(v);
    }

    // add new vertex and edges to graph
    g.addVertex(newVertex);
    for (Vertex pred : preds) {
      try {
        g.addEdge(pred, newVertex, null);
      } catch (DuplicateEdgeException e) {
        System.err.println("Tried to add a duplicate edge from " + pred + " to " + newVertex);
      }
    }
    for (Vertex succ : succs) {
      try {
        g.addEdge(newVertex, succ, null);
      } catch (DuplicateEdgeException e) {
        System.err.println("Tried to add a duplicate edge from " + newVertex + " to " + succ);
      }
    }

  }
  
}
