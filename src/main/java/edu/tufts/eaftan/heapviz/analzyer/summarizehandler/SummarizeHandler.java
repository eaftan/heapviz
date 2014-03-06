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

/****************************************************************************
 * This file does the analysis for heap summarization.  Basically
 * we need to store all the info from the trace as we process
 * the trace, then at the end convert everything into graph format
 * and output it.
 ****************************************************************************/

package edu.tufts.eaftan.heapviz.analzyer.summarizehandler;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;

import com.google.common.base.Preconditions;

import edu.tufts.eaftan.heapviz.summarizer.*;
import edu.tufts.eaftan.heapviz.util.DuplicateEdgeException;
import edu.tufts.eaftan.heapviz.util.Graph;
import edu.tufts.eaftan.hprofparser.parser.datastructures.*;
import edu.tufts.eaftan.hprofparser.handler.NullRecordHandler;

public class SummarizeHandler extends NullRecordHandler {

  private static final String BLACKLIST = "/sun_blacklist.txt";

  /* Instance variables */

  /**
   * Should we compute the summary?
   */
  private boolean doSummary;

  /**
   * Should we output pointer edges?
   */
  private boolean printPtrEdges;

  /**
   * Should we output dominance edges?
   */
  private boolean printDomEdges;

  /**
   * Maps object IDs to strings
   */
  private HashMap<Long, String> stringMap = new HashMap<Long, String>();

  /**
   * Maps object IDs to class info objects
   */
  private HashMap<Long, Class> classIdMap = new HashMap<Long, Class>();

  /**
   * Maps class serial numbers to class info objects
   */
  private HashMap<Integer, Class> classSerialNumMap = new HashMap<Integer, Class>();

  /**
   * Maps object IDs to instance info objects
   */
  private HashMap<Long, Instance> instanceMap = new HashMap<Long, Instance>();

  /**
   * Maps stack frame IDs to stack frame info objects
   */
  private HashMap<Long, StackFrame> stackFrameMap = new HashMap<Long, StackFrame>();

  /**
   * Maps stack trace serial numbers to stack trace info objects
   */
  private HashMap<Integer, StackTrace> stackTraceMap = new HashMap<Integer, StackTrace>();

  /**
   * LinkedList of root references
   */
  private LinkedList<Root> roots = new LinkedList<Root>();

  /**
   * Blacklist of Java stack references not to print
   */
  private HashSet<String> javaStackBlacklist = new HashSet<String>();

  /**
   * Blacklist of Java static references not to print
   *
   * This is a HashMap that maps a class name to a HashSet of names of fields
   * not to print.
   */
  private HashMap<String, HashSet<String>> staticsBlacklist = new HashMap<String, HashSet<String>>();

  /**
   * List of rendered heap graphs to display
   */
  private LinkedList<String> heapImages = new LinkedList<String>();


  /**
   * What kind of summary to do? Set in constructor
   */
  private Summarizer summarizer;


  /* Constructors */

  /*
  public SummarizeHandler2() {
    loadBlacklist(BLACKLIST);
  }
   */

  public SummarizeHandler(boolean doSummary, boolean printDomEdges,
      boolean printPtrEdges, Summarizer sum) {
    loadBlacklist(BLACKLIST);
    this.doSummary = doSummary;
    this.printDomEdges = printDomEdges;
    this.printPtrEdges = printPtrEdges;

    if(sum == null){
      this.summarizer = new Softvis2010Summarizer();
    }
    else{
      this.summarizer = sum;

    }
  }


  /* Handlers for top-level records */

  /**
   * This method is called when a String In UTF-8 record is parsed.
   * It stores the string in a hashmap indexed by object ID.
   */
  public void stringInUTF8(long id, String data) {
    stringMap.put(id, data);
  }

  /**
   * This method is called when a Load Class record is parsed.
   * It stores the information in 2 hashmaps (one indexed by object
   * ID, the other by class serial number) for later lookup (i.e.
   * when we need the name of a class).
   */
  public void loadClass(int classSerialNum, long classObjId,
      int stackTraceSerialNum, long classNameStringId) {
    Class cls = new Class();
    cls.classSerialNum = classSerialNum;
    cls.classObjId = classObjId;
    cls.className = stringMap.get(classNameStringId);
    classIdMap.put(classObjId, cls);
    classSerialNumMap.put(classSerialNum, cls);
  }

  /**
   * This method is called when a Stack Frame record is parsed.
   * It creates a new stack frame info object and puts it into
   * a hashmap for later lookup (i.e. when we have stack traces
   * for allocation sites).
   */
  public void stackFrame(long stackFrameId, long methodNameStringId,
      long methodSigStringId, long sourceFileNameStringId,
      int classSerialNum, int location) {
    StackFrame frame = new StackFrame();
    frame.stackFrameId = stackFrameId;
    frame.methodName = stringMap.get(methodNameStringId);
    frame.methodSig = stringMap.get(methodSigStringId);
    frame.sourceFileName = stringMap.get(sourceFileNameStringId);
    frame.classSerialNum = classSerialNum;
    frame.lineNum = location;
    stackFrameMap.put(stackFrameId, frame);
  }

  /**
   * This method is called when a Stack Trace record is parsed.
   * It creates a new stack trace info object and puts it into
   * a hashmap for later lookup (i.e. in an instance dump,
   * when we want the allocation site).
   */
  public void stackTrace(int stackTraceSerialNum, int threadSerialNum,
      int numFrames, long[] stackFrameIds) {
    StackTrace stack = new StackTrace();
    stack.stackTraceSerialNum = stackTraceSerialNum;
    stack.threadSerialNum = threadSerialNum;
    stack.stackFrameIds = stackFrameIds;
    stackTraceMap.put(stackTraceSerialNum, stack);
  }


  /* Handlers for heap dump records */

  public void rootUnknown(long objId) {
    //recordRoot(objId, "rootUnknown");
  }

  public void rootJNIGlobal(long objId, long JNIGlobalRefId) {
    //recordRoot(objId, "rootJNIGlobal");
  }

  public void rootJNILocal(long objId, int threadSerialNum, int frameNum) {
    //recordRoot(objId, "rootJNILocal");
  }

  public void rootJavaFrame(long objId, int threadSerialNum, int frameNum) {
    RootJavaFrame newRoot = new RootJavaFrame();
    newRoot.targetId = objId;
    newRoot.threadSerialNum = threadSerialNum;
    newRoot.frameNum = frameNum;
    roots.add(newRoot);
  }

  public void rootNativeStack(long objId, int threadSerialNum) {
    //recordRoot(objId, "rootNativeStack");
  }

  public void rootStickyClass(long objId) {
    //recordRoot(objId, "rootStickyClass");
  }

  public void rootThreadBlock(long objId, int threadSerialNum) {
    //recordRoot(objId, "rootThreadBlock");
  }

  public void rootMonitorUsed(long objId) {
    //recordRoot(objId, "rootMonitorUsed");
  }

  public void rootThreadObj(long objId, int threadSerialNum,
      int stackTraceSerialNum) {
    //recordRoot(objId, "rootThreadObj");
  }

  /**
   * This method is called when a Class Dump subrecord of the Heap Dump
   * record is parsed.  We should already have seen this class as part
   * of a Load Class record, so we look it up in the hashmap and
   * fill in the rest of its information.
   *
   * We also need to record static edges here.
   */
  public void classDump(long classObjId, int stackTraceSerialNum,
      long superClassObjId, long classLoaderObjId, long signersObjId,
      long protectionDomainObjId, long reserved1, long reserved2,
      int instanceSize, Constant[] constants, Static[] statics,
      InstanceField[] instanceFields) {

    // update class info in objIdMap
    Class cls = classIdMap.get(classObjId);
    if (cls != null) {
      cls.superClassObjId = superClassObjId;
      cls.instanceSize = instanceSize;
      cls.instanceFields = instanceFields;
      cls.stackTraceSerialNum = stackTraceSerialNum;
    } else {
      System.err.println("Error: class " + classObjId + " not found");
    }

    // add statics to root list
    if (statics != null) {
      for (Static s: statics) {
        if (s.value.type == Type.OBJ) {
          @SuppressWarnings("unchecked")
          Value<Long> value = (Value<Long>) s.value;
          if (value.value != 0) {
            RootStatic newRoot = new RootStatic();
            newRoot.targetId = value.value;
            newRoot.classObjId = classObjId;
            newRoot.fieldNameStringId = s.staticFieldNameStringId;
            roots.add(newRoot);
          }
        }
      }
    }

  }

  public void instanceDump(long objId, int stackTraceSerialNum,
      long classObjId, Value<?>[] instanceFieldValues) {

    if (instanceMap.get(objId) == null) {
      ObjectInstance newInstance = new ObjectInstance();
      newInstance.objId = objId;
      newInstance.stackTraceSerialNum = stackTraceSerialNum;
      newInstance.classObjId = classObjId;
      newInstance.instanceFieldValues = instanceFieldValues;
      instanceMap.put(objId, newInstance);
    }

  }

  public void objArrayDump(long objId, int stackTraceSerialNum,
      long elemClassObjId, long[] elems) {

    if (instanceMap.get(objId) == null) {
      ObjectArray newArray = new ObjectArray();
      newArray.objId = objId;
      newArray.stackTraceSerialNum = stackTraceSerialNum;
      newArray.elemClassObjId = elemClassObjId;
      newArray.elems = elems;
      instanceMap.put(objId, newArray);
    }

  }

  public void primArrayDump(long objId, int stackTraceSerialNum,
      byte elemType, Value<?>[] elems) {

    if (instanceMap.get(objId) == null) {
      PrimArray newArray = new PrimArray();
      newArray.objId = objId;
      newArray.stackTraceSerialNum = stackTraceSerialNum;
      newArray.elemType = Type.hprofTypeToEnum(elemType);
      newArray.elems = elems;
      instanceMap.put(objId, newArray);
    }

  }

  /**
   * Called at the end of a heap dump record.  At this point we want to
   * process the heap dump, build a graph, and output the graph in XML
   * format.
   */
  public void heapDumpEnd() {

    Graph<Vertex, String> g = new Graph<Vertex, String>(instanceMap.size(), 10);

    HashMap<Long, Vertex> objIdToVertex = new HashMap<Long, Vertex>(instanceMap.size());

    // worklist of object ids of instances to process
    Stack<Long> worklist = new Stack<Long>();

    // set of visited object ids
    HashSet<Long> visited = new HashSet<Long>(instanceMap.size());

    // create a fake root node that will point to the real roots
    Vertex root = new Vertex(0, "Fake root", 0, null);
    g.addVertex(root);
    g.setRoot(root);

    // start at roots, push references onto worklist
    for (Root r: roots) {
      if (keepRoot(r)) {

        // find vertex that is target of the edge
        Vertex target;
        try {
          target = findOrCreateVertex(objIdToVertex, r.targetId);

          // create edge
          /* TODO: we may want to distinguish between stack roots
           * and static roots in edges
           */
          String label = null;
          if (r instanceof RootJavaFrame) {
            RootJavaFrame rjf = (RootJavaFrame)r;
            label = "rootJavaFrame-" + rjf.threadSerialNum + "-" + rjf.frameNum;
          } else if (r instanceof RootStatic) {
            RootStatic rs = (RootStatic)r;
            /* TODO: is this really how we want to represent this?  Shouldn't it really
             * be a node with the class name and edges with the static field names? */
            label = classIdMap.get(rs.classObjId).className + "." + stringMap.get(rs.fieldNameStringId);
          }
          g.addVertex(target);
          try {
            g.addEdge(root, target, label);
          } catch (DuplicateEdgeException e) {
            System.err.println("Tried to add a duplicate edge from " + root + " to " + target);
          }

          worklist.push(r.targetId);
        } catch (InvalidVertexException e) {
          // TODO(eaftan): Log object id
        }
      }
    }
    visited.add(0L);

    // iterate over worklist, scanning each object and pushing its children
    while (!worklist.isEmpty()) {
      long objId = worklist.pop();
      if (!visited.contains(objId)) {
        Instance obj = instanceMap.get(objId);

        // TODO(eaftan): This is failing, figure out why.
        //Preconditions.checkState(obj != null, "obj with id %s not found", objId);

        if (obj instanceof ObjectInstance) {
          ObjectInstance objInstance  = (ObjectInstance) obj;
          Class cls = classIdMap.get(objInstance.classObjId);
          assert(cls != null);

          if (objInstance.instanceFieldValues != null) {
            // superclass of Object is 0
            int i = 0;
            long nextClass = objInstance.classObjId;
            while (nextClass != 0) {
              Class ci = classIdMap.get(nextClass);
              nextClass = ci.superClassObjId;
              if (ci.instanceFields != null) {
                for (InstanceField field: ci.instanceFields) {
                  if (field.type == Type.OBJ) {
                    String fieldName = stringMap.get(field.fieldNameStringId);
                    Value<?> value = objInstance.instanceFieldValues[i];
                    if (value.type == Type.OBJ) {
                      @SuppressWarnings("unchecked")
                      Value<Long> longValue = (Value<Long>) value;
                      if (longValue.value != 0) {   // reference is non-null
                        try {
                          Vertex from = findOrCreateVertex(objIdToVertex, objId);
                          Vertex to = findOrCreateVertex(objIdToVertex, longValue.value);

                          if (from == null || to == null) {
                            System.err.println("Cannot find endpoints of edge!");
                            System.exit(1);
                          }
                          g.addVertex(to);
                          try {
                            g.addEdge(from, to, fieldName);
                          } catch (DuplicateEdgeException e) {
                            System.err.println("Tried to add a duplicate edge from " + from + " to " + to);
                          }
                          worklist.push(longValue.value);
                        } catch (InvalidVertexException e) {
                          // TODO(eaftan): Log invalid object id
                        }
                      }
                    }
                  }
                  i++;
                }
              }
            }
            if (i != objInstance.instanceFieldValues.length) {
              System.err.println("Error in object instance");
              System.exit(1);
            }
          }

        } else if (obj instanceof ObjectArray) {

          ObjectArray arrayInstance = (ObjectArray)obj;
          String arrayType = classIdMap.get(arrayInstance.elemClassObjId).className;

          if (arrayInstance.elems != null) {
            for (int i=0; i<arrayInstance.elems.length; i++) {
              long ref = arrayInstance.elems[i];
              if (ref != 0) {
                try {
                  Vertex from = findOrCreateVertex(objIdToVertex, objId);
                  Vertex to = findOrCreateVertex(objIdToVertex, ref);
                  g.addVertex(to);
                  // use array index as edge label
                  try {
                    g.addEdge(from, to, Long.toString(i));
                  } catch (DuplicateEdgeException e) {
                    System.err.println("Tried to add a duplicate edge from " + from + " to " + to);
                  }
                } catch (InvalidVertexException e) {
                  // TODO(eaftan): log object id
                }
                worklist.push(ref);
              }
            }
          }
        }

        visited.add(objId);
      }
    }

    // we're done building the graph


    if (doSummary){
      g = this.summarizer.summarize(g);
      //Summarizer s = new Softvis2010Summarizer();
      //Summarizer s = new AllocSiteSummarizer();
      //g = s.summarize(g);
    }

    // compute dominators
    //Map<Vertex, Vertex> dominators = g.computeDominators(root);
    //for (Vertex to : dominators.keySet()) {
    //  Vertex from = dominators.get(to);
    //  if (!from.equals(to))
    //   g.addOwnershipEdge(from, to, null);
    //}

    // output to GraphML
    int id = heapImages.size();
    String gmlpath = Render.graphToGraphML(g, Integer.toString(id),
        printDomEdges, printPtrEdges);
    heapImages.add(gmlpath);

    // clear data structures
    instanceMap.clear();
    roots.clear();
    Vertex.clearIdsInUse();

  }

  /* Private methods */
  private void loadBlacklist(String blacklistFileName) {
    // TODO(eaftan): try-with-resources to close BufferedReader properly
    try {
      InputStream blackListStream = getClass().getResourceAsStream(blacklistFileName);
      //File blackList = new File(ClassLoader.getSystemResource(blacklistFileName).toURI());
      BufferedReader in = new BufferedReader(new InputStreamReader(blackListStream));
      String line;
      while ((line = in.readLine()) != null) {
        line = line.trim();

        // skip blank lines and comment lines
        if (line.length() > 0 && line.charAt(0) != '#') {
          String[] parts = line.split(",");
          if (!(parts.length >= 2)) {
            System.err.println("Error in blacklist file: incorrect format");
            System.err.println(line);
          } else {

            if (parts[0].trim().equals("Java_stack")) {

              // Java stack references
              javaStackBlacklist.add(parts[1].trim());

            } else if (parts[0].trim().equals("Static")) {

              // Static references
              HashSet<String> bannedFields = staticsBlacklist.get(parts[1].trim());
              if (bannedFields == null) {
                bannedFields = new HashSet<String>();
              }
              bannedFields.add(parts[2].trim());
              staticsBlacklist.put(parts[1].trim(), bannedFields);

            }
          }
        }
      }
      in.close();
    } catch (FileNotFoundException e) {
      System.err.println("File not found: " + e);
    } catch (IOException e) {
      System.err.println("IOException: " + e);
      System.exit(1);
    }

  }

  /**
   * Given an object Instance, produces a corresponding Vertex.  Does not
   * do anything with edges.
   *
   * TODO: Processing the fields blows up the memory usage.  Ideas?
   */
  private Vertex instanceToVertex(Instance obj) {

    // same for all types
    StackTrace stack = stackTraceMap.get(obj.stackTraceSerialNum);
    assert (stack != null);
    String allocContext = stack.toString(stackFrameMap, classSerialNumMap);

    if (obj instanceof ObjectArray) {
      ObjectArray arrayInstance = (ObjectArray)obj;
      String arrayType = classIdMap.get(arrayInstance.elemClassObjId).className;
      long size = 0;
      if  (arrayInstance.elems != null)
        size = arrayInstance.elems.length * Type.OBJ.sizeInBytes();
      return new Vertex(arrayInstance.objId, arrayType, size, allocContext);
    } else if (obj instanceof ObjectInstance) {
      ObjectInstance objInstance  = (ObjectInstance)obj;
      Class cls = classIdMap.get(objInstance.classObjId);
      if (cls == null) {
        System.err.println("Cannot find class");
        System.exit(1);
      }
      Vertex v = new Vertex(objInstance.objId, cls.className, cls.instanceSize, allocContext);

      if (objInstance.instanceFieldValues != null) {
        // superclass of Object is 0
        int i = 0;
        long nextClass = objInstance.classObjId;
        while (nextClass != 0) {
          Class ci = classIdMap.get(nextClass);
          nextClass = ci.superClassObjId;
          if (ci.instanceFields != null) {
            for (InstanceField field: ci.instanceFields) {
              if (field.type != Type.OBJ) {
                String fieldName = stringMap.get(field.fieldNameStringId);
                String value = objInstance.instanceFieldValues[i].toString();
                v.addField(fieldName, value);
              }
              i++;
            }
          }
        }
        if (i != objInstance.instanceFieldValues.length) {
          System.err.println("Error in object instance");
          System.exit(1);
        }
      }

      return v;
    } else if (obj instanceof PrimArray) {
      PrimArray arrayInstance = (PrimArray)obj;
      String arrayType = arrayInstance.elemType.toString() + "[]";
      long size = 0;
      if (arrayInstance.elems != null)
        size = arrayInstance.elems.length * arrayInstance.elemType.sizeInBytes();
      Vertex v = new Vertex(arrayInstance.objId, arrayType, size, allocContext);

      if (arrayInstance.elems != null) {
        for (int i=0; i<arrayInstance.elems.length; i++) {
          v.addField(Integer.toString(i), arrayInstance.elems[i].toString());
        }
      }

      return v;
    }

    System.err.println("Error: cannot process instance");
    return null;
  }

  /**
   * Given a Class object, produces a corresponding Vertex
   * TODO: is this how we want to represent class objects?
   *
   * @param obj The Class object to convert to a Vertex object
   * @return The new Vertex object
   */
  private Vertex classToVertex(Class obj) {

    long size = 0;      // TODO: what is the size of a class object?

    StackTrace stack = stackTraceMap.get(obj.stackTraceSerialNum);
    assert (stack != null);
    String allocContext = stack.toString(stackFrameMap, classSerialNumMap);

    Vertex v = new Vertex(obj.classObjId, "java.lang.Class - " + obj.className,
        size, allocContext);
    return v;

  }

  /**
   * Given an object id and a mapping of previously-created vertices, return
   * either a new Vertex representing the specified object or an already
   * existing vertex that was in the map.
   */
  private Vertex findOrCreateVertex(Map<Long, Vertex> objIdToVertex, long objId)
      throws InvalidVertexException {
    Vertex target = objIdToVertex.get(objId);
    if (target == null) {
      if (instanceMap.containsKey(objId)) {
        Instance i = instanceMap.get(objId);
        target = instanceToVertex(i);
        objIdToVertex.put(objId, target);
      } else if (classIdMap.containsKey(objId)) {
        Class c = classIdMap.get(objId);
        target = classToVertex(c);
        objIdToVertex.put(objId, target);
      } else {
        throw new InvalidVertexException("No object or class exists with id " + objId);
      }
    }

    return target;
  }

  /**
   * Encapsulates the logic regarding whether to keep a given root, i.e. is
   * it on our blacklist?
   *
   * @param r The root to either keep or discard
   * @return true if we should keep this root, false if we should discard it
   */
  private boolean keepRoot(Root r) {

    // discard root if we don't have its instance
    Instance instance = instanceMap.get(r.targetId);
    if (instance == null) {
      return false;
    }

    // otherwise check blacklist
    if (r instanceof RootJavaFrame) {

      Class cls = null;
      if (instance instanceof ObjectInstance) {
        cls = classIdMap.get(((ObjectInstance)instance).classObjId);
        return !javaStackBlacklist.contains(cls.className);
      } else if (instance instanceof ObjectArray) {
        cls = classIdMap.get(((ObjectArray)instance).elemClassObjId);
        return !javaStackBlacklist.contains(cls.className);
      } else {		// PrimArray
        return true;
      }


    } else if (r instanceof RootStatic) {

      RootStatic rs = (RootStatic)r;
      HashSet<String> fieldNames = staticsBlacklist.get(classIdMap.get(rs.classObjId).className);
      if (fieldNames == null) {
        return true;
      } else {
        return !fieldNames.contains(stringMap.get(rs.fieldNameStringId));
      }

    }

    return false;

  }

}