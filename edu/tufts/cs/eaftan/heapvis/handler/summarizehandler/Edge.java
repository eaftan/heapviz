package edu.tufts.cs.eaftan.heapvis.handler.summarizehandler;

public class Edge {
  
  public long fromObjId;
  public long toObjId;
  public String name;
  public boolean isStatic; 
  
  public Edge(long fromObjId, long toObjId, String name, boolean isStatic) {
    this.fromObjId = fromObjId;
    this.toObjId = toObjId;
    this.name = name;
    this.isStatic = isStatic;
  }

}
