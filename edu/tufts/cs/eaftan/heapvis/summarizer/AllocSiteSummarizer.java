package edu.tufts.cs.eaftan.heapvis.summarizer;

import java.util.ArrayList;
import java.util.HashMap;

import edu.tufts.cs.eaftan.heapvis.handler.summarizehandler.Vertex;
import edu.tufts.cs.eaftan.util.Graph;

/**
 * A simple summarizer that merges nodes that have the same allocation context;
 */
public class AllocSiteSummarizer implements Summarizer {

  @Override
  public Graph<Vertex, String> summarize(Graph<Vertex, String> g) {

    /* maps allocation site string to a list of instances */
    HashMap<String, ArrayList<Vertex>> allocSiteMap =
        new HashMap<String, ArrayList<Vertex>>();

    for (Vertex v : g.getVertices()) {
      if (v.allocContext != null) {
        ArrayList<Vertex> nodes = allocSiteMap.get(v.allocContext);
        if (nodes == null) {
          nodes = new ArrayList<Vertex>();
          allocSiteMap.put(v.allocContext, nodes);
        }
        nodes.add(v);
      }
    }

    for (String s : allocSiteMap.keySet()) {
      ArrayList<Vertex> vs = allocSiteMap.get(s);
      if (vs.size() > 1) {
    	  Utils.mergeVertices(g, vs);
      }
    }

    return g;
  }

  //Moved to Utils
  /*
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

  }*/

}
