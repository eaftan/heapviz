package edu.tufts.cs.eaftan.heapvis.summarizer;

import edu.tufts.cs.eaftan.heapvis.handler.summarizehandler.Vertex;
import edu.tufts.cs.eaftan.util.Graph;

public interface Summarizer {
  
	public Graph<Vertex,String> summarize(Graph<Vertex, String> g);
	
}
