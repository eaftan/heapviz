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
 * This class implements a graph based on an incidence list representation.
 *
 *
 * To do:
 *   2) change list of edges from HashSet to ArrayList
 *   3) if necessary for performance, I can wrap vertices to store
 *      info about them (postorder numbering, etc.).  right now we store
 *      this info in a hashmap.
 *   4) Check for duplication insertions/deletions.  Right now I'm assuming
 *      everything is called correctly.
 *   5) more thorough repOK -- need to check that edges and vertices match
 *      up
 */

package edu.tufts.cs.eaftan.util;

import java.util.*;

public class Graph<V, E> {

  /* invariants: 
   *   1) root must be in incidentEdges map
   */
  // TODO: these might need to become sets or linked lists for faster 
  // removal
  private HashMap<V, ArrayList<Edge<V, E>>> incidentEdges;
  //private V root;
  private int avgDegree;
  private int expectedVertices;
  
  
  //In an effort to make this as fast as possible,
  //this may not work correctly with mutable types for V,E
  public Graph<V,E> deepishCopy(){
	  Graph<V,E> newGraph = new Graph<V,E>();
	  
	  for(V v: incidentEdges.keySet()){
		  newGraph.incidentEdges.put(v, new ArrayList<Edge<V,E>>());
		  for(Edge<V,E> e:   incidentEdges.get(v)){
			  newGraph.incidentEdges.get(v).add(e);
		  }
	  }
	  
	  //newGraph.root = root;
	  newGraph.avgDegree = avgDegree;
	  newGraph.expectedVertices = expectedVertices;
	  
	  
	  return newGraph;
  }

  /**
   * Creates an empty graph
   */
  public Graph() {
    incidentEdges = new HashMap<V, ArrayList<Edge<V, E>>>();
    avgDegree = 10;
    expectedVertices = 100;
    assert(repOK());
  }

  /**
   * Creates an empty graph of a specified size
   *
   * @param numVertices The (approximate) number of vertices in the graph
   */
  public Graph(int numVertices, int avgDegree) {
    incidentEdges = new HashMap<V, ArrayList<Edge<V, E>>>(numVertices);
    this.avgDegree = avgDegree;
    this.expectedVertices = numVertices;
    assert(repOK());
  }

  /**
   * Set the root of the graph
   */
  public void setRoot(V root) {
    assert(repOK());
    assert(incidentEdges.containsKey(root));
    //this.root = root;
  }

  /**
   * Add a vertex to the graph
   * 
   * @param v The vertex to add
   * @return True if the vertex was added, false if not (already in the graph)
   */
  public boolean addVertex(V v) {
    assert(repOK());

    if (incidentEdges.containsKey(v))
      return false;

    incidentEdges.put(v, new ArrayList<Edge<V, E>>(avgDegree));
    return true;
  }
  
  /**
   * Remove a vertex from the graph, including all edges
   * that refer to it
   *  
   * @param v The vertex to remove
   * @return True if the vertex was removed, false if not
   */
  public boolean removeVertex(V v) {
    
    if (!incidentEdges.containsKey(v))
      return false;

    // remove edges that reference this vertex from other vertices
    for (Edge<V, E> e : incidentEdges.get(v)) {
      // if self edge, can skip this
      if (e.from == e.to) {
        continue;
      }
      if (e.from.equals(v)) {
        incidentEdges.get(e.to).remove(e);
      } else if (e.to.equals(v)) {
        incidentEdges.get(e.from).remove(e);
      } else {
        System.err.println("Error: edge not found");
        System.exit(1);
      }
    }
    
    // remove vertex itself
    incidentEdges.remove(v);
    
    assert(repOK());
    return true;
  }


  /**
   * Add a pointer edge to the graph.  If an edge from vertex "from" to vertex "to" already
   * exists with the same label, throw an exception.
   */
  public void addEdge(V from, V to, E data) throws DuplicateEdgeException {
    assert(repOK());
    
    if (!incidentEdges.containsKey(from) || !incidentEdges.containsKey(to)) {
      System.err.println("Error: adding an edge to vertices that don't exist!");
      System.exit(1);
    }
    
    Edge<V, E> newEdge = new Edge<V, E>(from, to, data, true, false);
    
    boolean duplicate = false;
    for (Edge<V, E> e : incidentEdges.get(from)) {
      if (e.equalsToFromData(newEdge)) {
        duplicate = true;
        throw new DuplicateEdgeException();
      }
    }
    if (!duplicate)
      incidentEdges.get(from).add(newEdge);
    
    duplicate = false;
    for (Edge<V, E> e : incidentEdges.get(to)) {
      if (e.equalsToFromData(newEdge)) {
        duplicate = true;
        throw new DuplicateEdgeException();
      }
    }
    if (!duplicate)
      incidentEdges.get(to).add(newEdge);
   
    assert(repOK());
  }
  
  /**
   * Add an ownership edge to the graph.  If an edge with the same "from" 
   * and "to" vertices and data exists, replace it.  Otherwise add a new
   * ownership edge with "pointer" set to false.
   */
  public void addOwnershipEdge(V from, V to, E data) {
    assert(repOK());
    
    if (!incidentEdges.containsKey(from) || !incidentEdges.containsKey(to)) {
      System.err.println("Error: adding an edge to vertices that don't exist!");
      System.exit(1);
    }
    
    boolean found = false;
    for (Edge<V, E> e : incidentEdges.get(from)) {
      if (e.from.equals(from) && e.to.equals(to)) {
        if ((e.data == null && data == null) ||
            e.data.equals(data)) {
          e.ownership = true;
          found = true;
          break;
        }
      }
    }
    if (!found)
      incidentEdges.get(from).add(new Edge<V, E>(from, to, data, false, true));
    
    found = false;
    for (Edge<V, E> e : incidentEdges.get(to)) {
      if (e.from.equals(from) && e.to.equals(to)) {
        if ((e.data == null && data == null) ||
            e.data.equals(data)) {  
          e.ownership = true;
          found = true;
          break;
        }
      }
    }
    if (!found)
      incidentEdges.get(to).add(new Edge<V, E>(from, to, data, false, true));
     
    assert(repOK());
  }
  
  /**
   * Remove an edge from the adjacency list of this vertex
   * 
   * @param vertex The vertex from which to remove this edge
   * @param edge The edge to remove
   */
  public void removeEdge(V vertex, E edge) {
    if (!incidentEdges.get(vertex).remove(edge)) {
      System.err.println("Error: tried to remove edge from vertex, cannot find");
      System.exit(1);
    }
  }
  
  /**
   * Get the successors of a given vertex
   */
  public Set<V> getSuccessors(V v) {
    assert(repOK());

    HashSet<V> successors = new HashSet<V>(avgDegree/2);
    if(incidentEdges == null){
    	System.err.println("incidentEdges is null, this should not happen!");    	
    }
    else if(incidentEdges.get(v) == null){
    	System.err.println("incidentEdges.get(v) == null, " + v.toString());    	
    }
    
    for (Edge<V, E> e : incidentEdges.get(v)) {
      if (e.from.equals(v)) {
        successors.add(e.to);
      }
    }
        
    return successors;
  }

  /**
   * Get the predecessors of a given vertex
   */
  public Set<V> getPredecessors(V v) {
    assert(repOK());

    HashSet<V> predecessors = new HashSet<V>(avgDegree/2);
    for (Edge<V, E> e : incidentEdges.get(v)) {
      if (e.to.equals(v)) {
        predecessors.add(e.from);
      }
    }
        
    return predecessors;
  }
  
  /**
   * Get all edges incident to a given vertex
   * 
   * @param v The vertex whose edges we want
   * @return A list of Edges incident to v
   */
  public List<Edge<V, E>> getEdges(V v) {
    return incidentEdges.get(v);
  }

  /**
   * Get the outgoing edges of a given vertex
   */
  public List<Edge<V, E>> getOutgoingEdges(V v) {
    assert(repOK());

    ArrayList<Edge<V, E>> edges = new ArrayList<Edge<V, E>>(avgDegree/2);
    for (Edge<V, E> e : incidentEdges.get(v)) {
      if (e.from.equals(v)) {
        edges.add(e);
      }
    }
        
    return edges;
  }

  /**
   * Get the incoming edges of a given vertex
   */
  public List<Edge<V, E>> getIncomingEdges(V v) {
    assert(repOK());

    ArrayList<Edge<V, E>> edges = new ArrayList<Edge<V, E>>(avgDegree/2);
    for (Edge<V, E> e : incidentEdges.get(v)) {
      if (e.to.equals(v)) {
        edges.add(e);
      }
    }
        
    return edges;
  }


  /**
   * Get all vertices in the graph
   * 
   * @return A Set containing all vertices in the graph
   */
  public Set<V> getVertices() {
    assert(repOK());
    return incidentEdges.keySet();
  }
  
  /**
   * Get all edges in the graph
   * 
   * @return A Set containing all edges in the graph
   */
  public Set<Edge<V, E>> getEdges() {
    assert(repOK());
    
    HashSet<Edge<V, E>> edges = new HashSet<Edge<V, E>>(expectedVertices);
    for (V vertex : incidentEdges.keySet()) {
      edges.addAll(incidentEdges.get(vertex));
    }
    return edges;
  }
  
  /**
   * Get the number of vertices in the graph
   * 
   * @return The number of vertices in the graph
   */
  public int getNumVertices() {
    return incidentEdges.size();
  }
  
  /**
   * Get the root node of the graph
   */
  //public V getRoot() {
    //return root;
  //}


  /** 
   * Compute a postordering over the vertices in the graph
   */
  public List<V> computePostordering(V root) {
    assert(repOK());
    assert(root != null);
    
    if (root == null) {
      System.err.println("No root defined for graph");
      System.exit(1);
    }

    int i = 0;
    ArrayList<V> postordering = new ArrayList<V>(getNumVertices());
    HashSet<V> visited = new HashSet<V>(getNumVertices());
    Stack<StackElem> worklist = new Stack<StackElem>();
    
    worklist.push(new StackElem(State.SCAN, root));
    while (!worklist.empty()) {
      StackElem curr = worklist.pop();
      if (curr.state == State.SCAN) {
        if (!visited.contains(curr.node)) {
          visited.add(curr.node);
          worklist.push(new StackElem(State.POSTORDER, curr.node));

          for (V s : getSuccessors(curr.node)) {
            worklist.push(new StackElem(State.SCAN, s));
          }
        }

      } else if (curr.state == State.POSTORDER) {
        postordering.add(curr.node);
        i++;
      }
    }

    assert(postordering.size() == getNumVertices());

    return postordering;
  }

  /**
   * Compute the immediate dominator for each vertex
   */
  public Map<V, V> computeDominators(V root) {

    // compute postordering
    // we need both maps from postorder number to vertex and
    // from vertex to postorder number
    List<V> postorderToVertex = computePostordering(root);
    HashMap<V,Integer> vertexToPostorder = new HashMap<V, Integer>(getNumVertices());
    for (int i=0; i<postorderToVertex.size(); i++)
      vertexToPostorder.put(postorderToVertex.get(i), i);

    // for all nodes, b 
    //   doms[b] <- undefined
    int[] doms = new int[getNumVertices()];
    for (int i=0; i<doms.length; i++)
      doms[i] = -1;

    // doms[start_node] <- start_node
    doms[doms.length-1] = doms.length-1;

    boolean changed = true;
    while (changed) {
      changed = false;

      // for all nodes, b, in reverse postorder (except start_node)
      for (int i=doms.length-2; i>=0; i--) {

        int new_idom = -1;
        V firstPred = null;
        V currVertex = postorderToVertex.get(i);
        Set<V> preds = getPredecessors(currVertex);

        // new_idom <- first (processed) predecessor of b
        for (V predVertex : preds) {
          int predPostorder = vertexToPostorder.get(predVertex);
          if (predPostorder > i) {
            firstPred = predVertex;
            new_idom = predPostorder;
            break;
          }
        }
        assert(new_idom >= 0);

        // for all other predecessors, p, of b
        for (V predVertex : preds) {
          if (!firstPred.equals(predVertex)) {
            int predPostorder = vertexToPostorder.get(predVertex);
            if (doms[predPostorder] >= 0) {
              new_idom = intersect(predPostorder, new_idom, doms);
            }

          }
        }
        
        if (doms[i] != new_idom) {
          doms[i] = new_idom;
          changed = true;
        }
      }

    }

    HashMap<V, V> dominators = new HashMap<V, V>(getNumVertices());
    for (int i=0; i<doms.length; i++)
      dominators.put(postorderToVertex.get(i), postorderToVertex.get(doms[i]));

    return dominators;

  }

  private int intersect(int b1, int b2, int[] doms) {
    int finger1 = b1;
    int finger2 = b2;

    while (finger1 != finger2) {
      while (finger1 < finger2)
        finger1 = doms[finger1];

      while (finger2 < finger1)
        finger2 = doms[finger2];
    }

    return finger1;
  }





  /**
   * Check data structure invariants
   */
  private boolean repOK() {
    // root must be in incidentEdges set
    //boolean rootValid = root == null ? true : incidentEdges.containsKey(root);

    // all edges in graph must be mapped correctly
    boolean edgesValid = true;
    for (V v : incidentEdges.keySet()) {
      for (Edge e : incidentEdges.get(v)) {
        if (!incidentEdges.containsKey(e.from) ||
            !incidentEdges.containsKey(e.to) ||
            !(e.to.equals(v) || e.from.equals(v))) {
          edgesValid = false;
          break;
        }
      }
    }

    return edgesValid;
    //return (rootValid && edgesValid);
  }


  /**
   * Inner classes
   */
  private class StackElem {
    private State state;
    private V node;

    public StackElem(State state, V node) {
      this.state = state;
      this.node = node;
    }

    public String toString() {
      return new String(state.toString() + ", " + node.toString());
    }
  }

  private enum State {
    SCAN, POSTORDER
  }

}
