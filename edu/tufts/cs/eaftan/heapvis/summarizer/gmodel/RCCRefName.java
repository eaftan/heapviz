package edu.tufts.cs.eaftan.heapvis.summarizer.gmodel;

import java.util.*;


//A simple class to intern field/variables names for abstraction function, these names have no real semantics.
public final class RCCRefName
{
	private static HashMap<String, RCCRefName> internrefnames;
	
	private String name;
	
	private RCCRefName(String s)
	{this.name = s;}
	
	public static void resetNameInfo()
	{internrefnames = new HashMap<String, RCCRefName>();}
	
	public static RCCRefName internRCCName(String tn)
	{
		if(!internrefnames.containsKey(tn))
			internrefnames.put(tn, new RCCRefName(tn));
		return internrefnames.get(tn);
	}
	
	@Override
	public String toString()
	{return this.name;}
}