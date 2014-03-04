/**
 * Abstract base class for instances in a heap dump.  Possible subclasses are 
 * object instances, object arrays, and primitive arrays.
 */

package edu.tufts.cs.eaftan.heapvis.handler.summarizehandler;

public abstract class Instance {
  
  public long objId;
  public int stackTraceSerialNum;
  
}
