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
