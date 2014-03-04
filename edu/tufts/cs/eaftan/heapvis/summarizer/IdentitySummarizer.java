package edu.tufts.cs.eaftan.heapvis.summarizer;

import edu.tufts.cs.eaftan.heapvis.handler.summarizehandler.Vertex;
import edu.tufts.cs.eaftan.util.Graph;
/**
 * 
 * @author nricci01
 * This summarizer makes no changes to the graph;
 * Useful if you don't want to do any summary at all.
 */
public class IdentitySummarizer implements Summarizer {

	@Override
	public Graph<Vertex, String> summarize(Graph<Vertex, String> g) {
		
		return g;
	}

}
