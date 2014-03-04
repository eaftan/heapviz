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

package edu.tufts.cs.eaftan.heapvis;
import java.io.*;

import edu.tufts.cs.eaftan.heapvis.handler.*;
import edu.tufts.cs.eaftan.heapvis.handler.summarizehandler.SummarizeHandler2;
import edu.tufts.cs.eaftan.heapvis.parser.HprofParser;
import java.util.Map;
import java.util.HashMap;
import edu.tufts.cs.eaftan.heapvis.summarizer.*;


public class Parse {

  public static void main(String[] args) {
 
    String inputfile;
    boolean doSummary = true, printDomEdges = true, printPtrEdges = true;
    int i;
    
    
    /*
    for (i=0; i<args.length-1; i++) {
      if (args[i].equals("-nosummary")) {
        doSummary = false;
      } else if (args[i].startsWith("-printEdges=", 0)) {
        if (args[i].substring(12).equals("pointer")) {
          printDomEdges = false;
        } else if (args[i].substring(12).equals("ownership")) {
          printPtrEdges = false;
        } else if (args[i].substring(12).equals("both")) {
        } else {
          System.err.println("Unrecognized option: " + args[i]);
          System.err.println("Usage: java Parser [-nosummary] [-printEdges={pointer,ownership,both}] inputfile");
          System.exit(1);
        }
      } else {
        System.err.println("Unrecognized option: " + args[i]);
        System.err.println("Usage: java Parser [-nosummary] [-printEdges={pointer,ownership,both}] inputfile");
        System.exit(1);
      }
    }
    if (i != args.length - 1) {
      System.err.println("No input file provided");
      System.err.println("Usage: java Parser [-nosummary] [-printEdges={pointer,ownership,both}] inputfile");
      System.exit(1);
      
    }*/
    inputfile = args[args.length-1];

    // RecordHandler handler = new RecordHandler();
    //RecordHandler handler = new PrintHandler();
    // RecordHandler handler = new StaticPrintHandler();
    Summarizer summarizer = null;
    
    
    Map<String,String> argmap = parseCommandLineArgs(args);
    
    
    
    System.out.println(argmap.get("-s"));
    if(argmap.get("-s") != null){
	    if(argmap.get("-s").equals("SoftVis2010")){
	    	System.out.println("Using SoftVis2010 summarizer.");
	    	summarizer = new Softvis2010Summarizer();
	    	
	    }
	    else if (argmap.get("-s").equals("AllocSite")){
	    	System.out.println("Using AllocSite summarzier.");
	    	summarizer = new AllocSiteSummarizer();
	    }
	    else if (argmap.get("-s").equals("TypeGraph")){
	    	System.out.println("Using TypeGraph summarizer.");
	    	summarizer = new TypeGraphSummarizer();    	
	    } else if (argmap.get("-s").equals("Identity")){
	    	System.out.println("Using Identity summarizer");
	    	summarizer = new IdentitySummarizer();
	    }
    }
    else{
    	System.out.println("No summarizer specified; defaulting to SoftVis2010.");
    	summarizer = new Softvis2010Summarizer();
    }
    
    RecordHandler handler = new SummarizeHandler2(doSummary, printDomEdges, printPtrEdges, summarizer);
    HprofParser parser = new HprofParser(handler);

    try {
      FileInputStream fs = new FileInputStream(inputfile);
      DataInputStream in = new DataInputStream(new BufferedInputStream(fs));

      parser.parse(in);

      in.close();
    } catch (IOException e) {
      System.out.println("Error: " + e);
    }

  }
  
  private static HashMap<String,String> parseCommandLineArgs(String[] args){
	    HashMap<String,String> argMap = new HashMap<String,String>();
	                
	    for(int i =0; i < args.length; i++){
	      if(args[i].equals("-s")){
	        argMap.put("-s", args[++i]);
	      }
	      else{
	        //TODO: Throw some kinda exception...
	      }         
	    }
	    return argMap;
	  }

  
  
  

}
