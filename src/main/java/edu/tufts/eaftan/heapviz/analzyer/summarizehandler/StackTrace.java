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
