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

package edu.tufts.eaftan.heapviz.analzyer.summarizehandler;

import java.io.*;
import java.util.*;

import edu.tufts.eaftan.heapviz.util.Edge;
import edu.tufts.eaftan.heapviz.util.Graph;

public class Render {
  
  public static String dispDir = null;
  public static String dispGml = null;
  
  /**
   * initializer
   */
  static {
    String fsep = System.getProperty("file.separator");
    dispDir = System.getProperty("user.dir") + fsep + "images" + fsep;  // working directory + "/images/"
    
    // if dispDir does not exist, create it
    File dispDirFile = new File(dispDir);
    if (!dispDirFile.exists()) 
      dispDirFile.mkdir();
        
    dispGml = dispDir + "vis_heap$.xml";
  }
  
  
  public static String graphToGraphML(Graph<Vertex, String> g, String extraLabel,
      boolean printDomEdges, boolean printPtrEdges) {
    
    try {
      if (extraLabel == null)
        extraLabel = "";
      File visgml = new File(dispGml.replace("$", extraLabel));
      BufferedWriter out = new BufferedWriter(new FileWriter(visgml.getCanonicalPath()));
      computeGraphMLString(out, g, printDomEdges, printPtrEdges);
      out.close();
      return visgml.getCanonicalPath();
      
    } catch (IOException e) {
      System.out.println("Unable to generate GraphML file: " + e);
      System.exit(1);
    }
    
    return null;
    
  }
  
  /**
   * Compute the GraphML string for this graph.
   * 
   * @return A string representing this graph in GraphML format
   */
  public static void computeGraphMLString(Writer out, Graph<Vertex, String> g,
      boolean printDomEdges, boolean printPtrEdges) {    

    try {
      // header
      out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      out.write("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"\n");
      out.write("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
      out.write("xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns\n");
      out.write("http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">\n\n");
   
      // data schema
      out.write("<key id=\"type\" for=\"node\" attr.name=\"type\" attr.type=\"string\"/>\n");
      out.write("<key id=\"members\" for=\"node\" attr.name=\"members\" attr.type=\"string\"/>\n");
      out.write("<key id=\"count\" for=\"node\" attr.name=\"count\" attr.type=\"int\"/>\n");
      out.write("<key id=\"size\" for=\"node\" attr.name=\"size\" attr.type=\"int\"/>\n");
      out.write("<key id=\"types\" for=\"node\" attr.name=\"types\" attr.type=\"string\"/>\n");
      out.write("<key id=\"allocContext\" for=\"node\" attr.name=\"allocContext\" attr.type=\"string\"/>\n");
      out.write("<key id=\"collapsed\" for=\"node\" attr.name=\"collapsed\" attr.type=\"boolean\">\n");
      out.write("  <default>false</default>\n");
      out.write("</key>\n");
  
      out.write("<key id=\"label\" for=\"edge\" attr.name=\"label\" attr.type=\"string\"/>\n");
      out.write("<key id=\"ownership\" for=\"edge\" attr.name=\"ownership\" attr.type=\"boolean\">\n");
      out.write("  <default>false</default>\n");
      out.write("</key>\n");
      out.write("<key id=\"pointer\" for=\"edge\" attr.name=\"pointer\" attr.type=\"boolean\">\n");
      out.write("  <default>true</default>\n");
      out.write("</key>\n\n");
      
      
      // start graph
      out.write("<graph edgedefault=\"directed\">\n\n");
  
      // vertices
      Set<Vertex> vertices = g.getVertices();
      for (Vertex v : vertices) {
        out.write(v.toGraphML());
      }
      
      // edges
      Set<Edge<Vertex, String>> edges = g.getEdges();
      for (Edge<Vertex, String> e : edges) {
        if ((e.ownership && printDomEdges) || (e.pointer && printPtrEdges)) {
          out.write("<edge source=\"");
          out.write(Long.toString(e.from.id));
          out.write("\" target=\"");
          out.write(Long.toString(e.to.id));
          out.write("\">\n");
          
          out.write("  <data key=\"ownership\">");
          out.write(String.valueOf(e.ownership));
          out.write("</data>\n");
          
          out.write("  <data key=\"pointer\">");
          out.write(String.valueOf(e.pointer));
          out.write("</data>\n");
          out.write("</edge>\n");
        }  
      }
      
      out.write("</graph>\n</graphml>");
    } catch (IOException e) {
      System.err.println(e);
      System.exit(1);
    }
  }

}
