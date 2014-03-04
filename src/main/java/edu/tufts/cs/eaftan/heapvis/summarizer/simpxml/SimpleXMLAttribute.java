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

package edu.tufts.cs.eaftan.heapvis.summarizer.simpxml;

public final class SimpleXMLAttribute {
	private String attrName;
	private String attrVal;

	public SimpleXMLAttribute(String aname, String aval)
	{
		this.attrName = aname;
		this.attrVal = aval;
	}

	public String aName()
	{return this.attrName;}

	public String getAData()
	{return this.attrVal;}

	public int getADataAsInt()
	{return Integer.parseInt(this.getAData());}

	public double getADataAsFloat()
	{return Double.parseDouble(this.getAData());}

	public boolean getADataAsBool()
	{return this.getAData().equals("true");}

	public void toXMLStr(StringBuilder sb)
	{sb.append(this.attrName + "=\"" + this.attrVal + "\"");}
}
