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

import edu.tufts.eaftan.heapviz.analzyer.summarizehandler.Vertex;
import edu.tufts.eaftan.heapviz.util.Graph;
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
