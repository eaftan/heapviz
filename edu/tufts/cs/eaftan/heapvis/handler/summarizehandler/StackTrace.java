package edu.tufts.cs.eaftan.heapvis.handler.summarizehandler;

import java.util.HashMap;

public class StackTrace {
  
  public int stackTraceSerialNum;
  public int threadSerialNum;
  public long[] stackFrameIds;
  
  /**
   * Converts a stack trace to a text format.  These look similar 
   * to standard JVM stack traces.  For example:
   * 
   * BTree.<init>(BTree.java:7)
   * BTree.makeTreeX(BTree.java:28)
   * BH.main(BH.java:34)
   */
  public String toString(HashMap<Long, StackFrame> stackFrameMap,
      HashMap<Integer, Class> classSerialNumMap) {
    
    StringBuilder sb = new StringBuilder();
    for (long stackFrameId : stackFrameIds) {
      StackFrame frame = stackFrameMap.get(stackFrameId);
      assert(frame != null);
      Class cls = classSerialNumMap.get(frame.classSerialNum);
      assert(cls != null);
      sb.append(cls.className);
      sb.append('.');
      sb.append(frame.methodName);
      sb.append('(');
      sb.append(frame.sourceFileName);
      sb.append(':');
      sb.append(frame.lineNum);
      sb.append(")\\n");
    }
    
    return sb.toString();
  }
}
