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

package edu.tufts.eaftan.heapviz.summarizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.tufts.eaftan.heapviz.analzyer.summarizehandler.Vertex;
import edu.tufts.eaftan.heapviz.util.Graph;

public class TypeGraphSummarizer implements Summarizer {

	@Override
	public Graph<Vertex, String> summarize(Graph<Vertex, String> g) {
		HashMap<List<String>, List<Vertex>> m = new HashMap<List<String>, List<Vertex>>();


		Graph<Vertex,String> copy = g.deepishCopy();

		for(Vertex v : g.getVertices()){

		  // TODO(eaftan): Remove check for v != null.  Why are there null vertices in the graph?
		  if (v != null) {
		    if (m.get(v.types) == null){
		      m.put(v.types, new ArrayList<Vertex>());
		    }

		    m.get(v.types).add(v);
		  }
		}

		for(List<Vertex> vs: m.values()){
			Utils.mergeVertices(copy, vs);
		}

		return copy;
	}

}
