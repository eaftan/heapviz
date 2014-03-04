package edu.tufts.cs.eaftan.heapvis.summarizer;

import edu.tufts.cs.eaftan.heapvis.handler.summarizehandler.Vertex;
import edu.tufts.cs.eaftan.util.Graph;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class DominatorSetSummarizer implements Summarizer {
	private Set<Vertex> roots; 
	
	public DominatorSetSummarizer(Set<Vertex> roots){
		this.roots = roots;
		
	}
	
	@Override
	public Graph<Vertex, String> summarize(Graph<Vertex, String> g) {
		ArrayList<Map<Vertex,Vertex>> dominatorSets;
		Softvis2010Summarizer s = new Softvis2010Summarizer();
		HashMap<Set<Vertex>,Set<Vertex>> m;//Map from dominator set to nodes that have that dominator set.
		
		dominatorSets = new ArrayList<Map<Vertex,Vertex>>();
		
		for(Vertex r: roots){
			dominatorSets.add( g.computeDominators(r) );
		}
		
		//iterate over graph
		//if nodes have same type, same dominator set, merge them
		
		
		
		
		// TODO Auto-generated method stub
		return null;
	}

}
