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

package edu.tufts.cs.eaftan.heapvis.summarizer.gmodel;

import java.util.*;

//A simple class to intern type names for abstraction function, these names have no real semantics.
public final class RCCType
{
	private static HashMap<String, RCCType> interntypes;
	
	private String name;
	private boolean isRec;
	
	private RCCType(String s)
	{
		this.name = s;
		this.isRec = false;
	}
	
	public static void resetTypeInfo()
	{interntypes = new HashMap<String, RCCType>();}
	
	public static RCCType internRCCName(String tn)
	{
		if(!interntypes.containsKey(tn))
			interntypes.put(tn, new RCCType(tn));
		return interntypes.get(tn);
	}
	
	public static void resetAllTypeRecInfo()
	{
		for(RCCType tt : interntypes.values())
			tt.isRec = false;
	}
	
	public void markTypeAsRec()
	{this.isRec = true;}
	
	public boolean isTypeRec()
	{return this.isRec;}

	@Override
	public String toString()
	{return this.name + (this.isRec ? "*" : "");}
	
	public String getTypeStrForDot()
	{
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < this.name.length(); ++i)
		{
			if(this.name.charAt(i) == '<' || this.name.charAt(i) == '>')
				sb.append('\\');
			
			sb.append(this.name.charAt(i));
		}
		
		return sb.toString();
	}
}