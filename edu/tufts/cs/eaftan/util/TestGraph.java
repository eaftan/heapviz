package edu.tufts.cs.eaftan.util;

import java.util.List;
import java.util.Map;

public class TestGraph {

  public static void main(String[] args) {

    Graph<Character, Object> g = new Graph<Character, Object>();

    g.addVertex('a');
    g.addVertex('b');
    g.addVertex('c');
    g.addVertex('d');
    g.addVertex('e');
    g.addVertex('f');

    try {
      g.addEdge('a', 'b', null);
      g.addEdge('a', 'c', null);
  
      g.addEdge('b', 'd', null);
      
      g.addEdge('c', 'e', null);
      g.addEdge('c', 'f', null);
      
      g.addEdge('d', 'e', null);
  
      g.addEdge('e', 'd', null);
      g.addEdge('e', 'f', null);
  
      g.addEdge('f', 'e', null);
    } catch (DuplicateEdgeException e) {
      System.err.println("Tried to add a duplicate edge");
    }

    g.setRoot('a');

    /*
    for (Character c : g.getVertices()) {
      System.out.print(c);
      System.out.println(" " + g.getSuccessors(c));
    }
    */

    List<Character> postordering = g.computePostordering('a');
    System.out.println(postordering);

    Map<Character,Character> dominators = g.computeDominators('a');
    System.out.println(dominators);


    g = new Graph<Character, Object>();

    g.addVertex('A');
    g.addVertex('B');
    g.addVertex('C');
    g.addVertex('D');
    g.addVertex('E');
    g.addVertex('F');
    g.addVertex('G');
    g.addVertex('H');
    g.addVertex('I');

    try {
      g.addEdge('F', 'B', null);
      g.addEdge('F', 'G', null);

      g.addEdge('B', 'A', null);
      g.addEdge('B', 'D', null);

      g.addEdge('D', 'C', null);
      g.addEdge('D', 'E', null);

      g.addEdge('G', 'I', null);

      g.addEdge('I', 'H', null);
    } catch (DuplicateEdgeException e) {
      System.err.println("Tried to add a duplicate edge");
    }

    g.setRoot('F');
    
    postordering = g.computePostordering('F');
    System.out.println(postordering);

    dominators = g.computeDominators('F');
    System.out.println(dominators);

  }

}


