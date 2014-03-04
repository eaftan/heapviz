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

package edu.tufts.cs.eaftan.heapvis.parser.datastructures;

public enum Type {

  OBJ("Object"), BOOL("boolean"), CHAR("char"), FLOAT("float"), DOUBLE("double"), 
  BYTE("byte"), SHORT("short"), INT("int"), LONG("long");
  
  private final String name;
  
  private Type(String name) {
    this.name = name;
  }

  public static Type hprofTypeToEnum(byte type) {
    switch (type) {
      case 2:
        return OBJ;
      case 4:
        return BOOL;
      case 5:
        return CHAR;
      case 6:
        return FLOAT;
      case 7:
        return DOUBLE;
      case 8:
        return BYTE;
      case 9:
        return SHORT;
      case 10:
        return INT;
      case 11:
        return LONG;
      default:
        System.err.println("Error: unsupported type " + type);
        System.exit(1);
        break;
    }

    return null;
  }
  
  /** 
   * Given a type, return the number of bytes used to represent it.
   * Note that some of these are implementation dependent, specifically
   * booleans and objects.  What do do about these?
   * 
   * @param t The type
   * @return The number of bytes used to represent the type
   */
  public static int typeToSizeBytes(Type t) {
    
    switch (t) {
      case OBJ:
        return 4; // TODO: this should depend on the JVM that produced the output
      case BOOL:
        return 1; // TODO: this is implementation dependent
      case CHAR:
        return 2;
      case FLOAT:
        return 4;
      case DOUBLE:
        return 8;
      case BYTE:
        return 1;
      case SHORT:
        return 2;
      case INT:
        return 4;
      case LONG:
        return 8;
      default:
        System.err.println("Undefined type");
        System.exit(1);
        return -1;
    }

  }
    
  public String toString() {
    return name;
  }

}


