package edu.tufts.cs.eaftan.heapvis.summarizer;

import java.util.List;
import java.util.HashSet;
import edu.tufts.cs.eaftan.heapvis.handler.summarizehandler.Vertex;
import edu.tufts.cs.eaftan.util.DuplicateEdgeException;
import edu.tufts.cs.eaftan.util.Edge;
import edu.tufts.cs.eaftan.util.Graph;

/*Some static utility functions that may be useful for many summarizers
 */
public class Utils {
	
	
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
