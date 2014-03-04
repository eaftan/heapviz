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

/**
 * Instances of this class represent a node in the object graph.
 * They should include all information we need to visualize the 
 * graph.
 */

package edu.tufts.eaftan.heapviz.analzyer.summarizehandler;

import java.util.*;

import edu.tufts.eaftan.heapviz.util.XMLSanitizer;

public class Vertex {
  
  /** 
   * Static variables
   */
  
  /**
   * A set of identifiers currently in use
   */
  private static HashSet<Long> idsInUse = new HashSet<Long>(100000);
  
  private static Random rand = new Random();
  
  
  /**
   * Instance variables
   */
  
  /**
   * A unique identifier for this vertex
   */
  public long id;
  
  /**
   * A list of concrete object identifiers that this vertex represents 
   */
  //public ArrayList<Long> ids;
  private long[] ids;
  
  /**
   * The representative type of this vertex
   */
  public String repType;
  
  /**
   * A list of concrete object types that this vertex represents 
   */
  public ArrayList<String> types;
  
  /**
   * A list of (fieldname, value) pairs for this concrete object.
   * Not used when this vertex represents more than one concrete
   * object.
   */
  private HashMap<String, String> fields;
  
  /**
   * The total size of the objects summarized by this node
   */
  public long size;
  
  /**
   * The allocation context for this vertex.  Null if the vertex 
   * represents multiple concrete nodes with different allocation
   * contexts.
   */
  public String allocContext;
  
  /**
   * Construct a vertex representing a single concrete object
   * 
   * @param id A unique identifier for the object
   * @param type The type of the object
   * @param size The size of the object in bytes
   * @param allocContext A string representing the allocation context
   */
  public Vertex(long id, String type, long size, String allocContext) {
    if (!idsInUse.add(id)) {
      System.err.println("Cannot create node, id in use");
      System.exit(1);
    }
    
    this.id = id;
    this.ids = new long[1];
    this.ids[0] = id;
    this.repType = type;
    this.types = new ArrayList<String>();
    this.types.add(type);
    this.size = size;
    this.allocContext = allocContext;
  }
  
  /**
   * Construct a vertex representing more than one concrete object

   * @param id A unique identifier for the vertex
   * @param ids A list of concrete object ids that this vertex represents
   * @param repType The representative type of the vertex
   * @param types A list of concrete object types that this vertex represents
   * @param num The number of concrete objects that are represented by this vertex
   * @param size The total size in bytes of the concrete objects this vertex represents
   * @param allocContext The allocation context for the concrete objects this vertex represents
   */
  public Vertex(long id, long[] ids, String repType, 
      ArrayList<String> types, long size, String allocContext) {
    if (!idsInUse.add(id)) {
      System.err.println("Cannot create node, id in use");
      System.exit(1);
    }
    
    this.id = id;
    
    this.ids = ids;
    this.repType = repType;
    this.types = types;
    this.size = size;
    this.allocContext = allocContext;
  }
  
  /**
   * Add a field-value pair to the vertex
   * 
   * @param name The name of the field
   * @param value The value of the field
   */
  public void addField(String name, String value) {
    if (ids.length > 1) {
      System.err.println("Cannot add fields to summarized vertex");
      System.exit(1);
    }
      
    if (fields == null) {
      fields = new HashMap<String, String>();
    }
    fields.put(name, value);
  }
  
  /**
   * Given a list of vertices, merge them into a single representative 
   * vertex. If the representative types are different, give the new vertex 
   * the same representative type as the first vertex in the list. 
   * 
   * @param vertices A list of vertices to merge
   * @return A new vertex that represents all the vertices in the list
   */
  public static Vertex merge(List<Vertex> vertices) {
    
	
	int totalIds = 0;
	
	for(Vertex v: vertices){
		totalIds += v.ids.length;		
	}
	

    //ArrayList<Long> ids = new ArrayList<Long>();
	long[] ids = new long[totalIds];
	
	
    ArrayList<String> types = new ArrayList<String>();
    String repType = null;
    String allocContext = null;
    
    long totalSize = 0;
    int idIndex = 0;
    boolean first = true;
    for (Vertex v : vertices) {
      totalSize += v.size;
      if (first) {    	
        //ids.addAll(v.ids);
    	for(long id: v.ids){
    		ids[idIndex] = id;
    		idIndex++;
    	}
    	  
        types.addAll(v.types);
        repType = v.repType;
        allocContext = v.allocContext;
        first = false;
      } 
      else {
        for (Long id : v.ids) {
          if (!contains(id, ids)) {
            ids[idIndex] = id;
            idIndex++;
          }
        }
        for (String type : v.types) {
          if (!types.contains(type)) {
            types.add(type);
          }
        }
        //In some summary algorithms, allocContext may be null to begin with,
        //thus the two checks.
        if (v.allocContext != null && !v.allocContext.equals(allocContext)) {
          allocContext = null;
        }
      }
    }
    //ids may have been made longer than necessary, so we copy out the necessary chunk here.
    return new Vertex(getFreshId(), Arrays.copyOf(ids, idIndex), repType, types, totalSize, allocContext);
  }
  
  /**
   * Produce a GraphML representation of this node
   * 
   * @return A String with a GraphML representation of this node
   */
  public String toGraphML() {
    StringBuilder sb = new StringBuilder();
    
    sb.append("<node id=\"");
    sb.append(this.id);
    sb.append("\">\n");

    sb.append("  <data key=\"type\">");
    sb.append(repType);
    sb.append("</data>\n");
    
    sb.append("  <data key=\"count\">");
    sb.append(ids.length);
    sb.append("</data>\n");
    
    sb.append("  <data key=\"size\">");
    sb.append(size);
    sb.append("</data>\n");

    sb.append("  <data key=\"types\">");
    boolean first = true;
    for (String t : types) {
      if (!first) {
        sb.append(":");
      }
      sb.append(t);
      first = false;
    }
    sb.append("</data>\n");

    
    /* Fields
     * 
     * 1) need to escape certain characters ('<', '>', etc.)
     * 2) having invalid unicode characters show up in the output
     */
    if (fields != null && !fields.isEmpty()) {
      sb.append("  <data key=\"members\">");
      for (String fieldName : fields.keySet()) {
        String value = fields.get(fieldName);
        int length = value.length();
        sb.append(fieldName + ":" + length + ":" + 
            XMLSanitizer.escape(XMLSanitizer.sanitize(value)));
      }
      sb.append("</data>\n");
    }
    
    /* Alloc site */
    if (allocContext != null) {
      sb.append("  <data key=\"allocContext\">");
      sb.append(XMLSanitizer.escape(XMLSanitizer.sanitize(allocContext)));
      sb.append("</data>\n");
      
    }
        
    sb.append("</node>\n");

    return sb.toString();
  }
  
  
  /**
   * Static methods
   */
  
  @Override
  public int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result = PRIME * result + (int) (id ^ (id >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final Vertex other = (Vertex) obj;
    if (id != other.id)
      return false;
    return true;
  }

  /**
   * Get a fresh (unused) identifier
   * 
   * @return A fresh (unused) identifier
   */
  private static long getFreshId() {
    long id;
    
    while (true) {
      id = rand.nextLong();
      if (!idsInUse.contains(id))
        return id;
    }
    
  }
  
  public static void clearIdsInUse() {
    idsInUse.clear();
  }
  
  @Override
  public String toString() {
    return id + " (" + repType + ")";
    
  }

  private  static boolean contains(long target, long[] arr){
	  for(int i = 0; i < arr.length; i++){
		  if(arr[i] == target){
			  return true;
		  }
	  }
	  
	  return false;
  }
  
}
