package edu.tufts.cs.eaftan.heapvis.summarizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.tufts.cs.eaftan.heapvis.handler.summarizehandler.Vertex;
import edu.tufts.cs.eaftan.util.Graph;

public class TypeGraphSummarizer implements Summarizer {

	@Override
	public Graph<Vertex, String> summarize(Graph<Vertex, String> g) {
		HashMap<List<String>, List<Vertex>> m = new HashMap<List<String>, List<Vertex>>();


		Graph<Vertex,String> copy = g.deepishCopy();

		for(Vertex v : g.getVertices()){

			if( m.get(v.types) == null){
				m.put(v.types, new ArrayList<Vertex>());
			}

			m.get(v.types).add(v);
		}

		for(List<Vertex> vs: m.values()){
			Utils.mergeVertices(copy, vs);
		}

		return copy;
	}

}
