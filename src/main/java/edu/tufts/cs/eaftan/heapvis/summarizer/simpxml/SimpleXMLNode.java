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

import java.util.*;

public final class SimpleXMLNode
{
	private static int indt = 0;

	private static void addindent(StringBuilder sb)
	{
		for(int i = 0; i < SimpleXMLNode.indt; ++i)
			sb.append("  ");
	}

	private String elementName;
	private boolean isData;
	private String cdata;
	private Vector<SimpleXMLNode> nnl;
	private Vector<SimpleXMLAttribute> attrl;

	public SimpleXMLNode(String en, Vector<SimpleXMLNode> nl, Vector<SimpleXMLAttribute> al)
	{
		this.elementName = en;
		this.isData = false;
		this.cdata = null;
		this.nnl = new Vector<SimpleXMLNode>(nl);
		this.attrl = new Vector<SimpleXMLAttribute>(al);
	}

	public SimpleXMLNode(String en, SimpleXMLNode xmln)
	{
		this.elementName = en;
		this.isData = false;
		this.cdata = null;
		this.nnl = new Vector<SimpleXMLNode>();
		this.nnl.add(xmln);
		this.attrl = new Vector<SimpleXMLAttribute>();
	}

	public SimpleXMLNode(String en, String cdn, Vector<SimpleXMLAttribute> al)
	{
		this.elementName = en;
		this.isData = true;
		this.cdata = cdn;
		this.nnl = null;
		this.attrl = new Vector<SimpleXMLAttribute>(al);
	}

	@Override
	public String toString()
	{return this.elementName;}

	public String eName() 
	{return this.elementName;}

	public boolean isDataNode() 
	{return this.isData;}

	public void setCData(String cd)
	{
		if(! this.isDataNode())
		{
			System.out.println("cannot set CData in non-data node");
			System.exit(1);
		}
		
		this.cdata = cd; 
	}

	public String getCData()
	{
		if(! this.isDataNode())
		{
			System.out.println("cannot read CData from non-data node");
			System.exit(1);
		}
		
		return this.cdata;
	}

	public int getCDataAsInt()
	{return Integer.parseInt(this.getCData());}

	public double getCDataAsFloat()
	{return Double.parseDouble(this.getCData());}

	public boolean getCDataAsBool()
	{return this.getCData().equals("true");}

	public Vector<SimpleXMLNode> getChildren()
	{
		if(this.isDataNode())
		{
			System.out.println("cannot read children from data node");
			System.exit(1);
		}
		
		return this.nnl;
	}

	public boolean hasChildWName(String nn)
	{
		if(this.isDataNode())
		{
			System.out.println("cannot read children from data node");
			System.exit(1);
		}
		
		for(int i = 0; i < this.nnl.size(); ++i)
		{
			if(this.nnl.get(i).elementName.equals(nn))
				return true;
		}
		
		return false;
	}

	public SimpleXMLNode getChildWName(String nn)
	{
		if(this.isDataNode())
		{
			System.out.println("cannot read children from data node");
			System.exit(1);
		}
		
		for(int i = 0; i < this.nnl.size(); ++i)
		{
			if(this.nnl.get(i).elementName.equals(nn))
				return this.nnl.get(i);
		}
		
		return null;
	}

	public boolean hasAttributeWName(String an)
	{
		for(int i = 0; i < this.attrl.size(); ++i)
		{
			if(this.attrl.get(i).aName().equals(an))
				return true;
		}
		
		return false;
	}

	public SimpleXMLAttribute getAttributeWName(String an)
	{
		for(int i = 0; i < this.attrl.size(); ++i)
		{
			if(this.attrl.get(i).aName().equals(an))
				return this.attrl.get(i);
		}
		
		return null;
	}

	public void toXMLStr(StringBuilder sb)
	{
		SimpleXMLNode.addindent(sb);
		sb.append("<" + this.elementName);

		Iterator<SimpleXMLAttribute> atri = this.attrl.iterator();
		while(atri.hasNext())
		{
			sb.append(" ");
			atri.next().toXMLStr(sb);
		}

		sb.append(">");

		if(this.isData)
			sb.append(SimpleXMLNode.xmlifyString(this.cdata));
		else if(this.nnl.size() != 0)
		{
			SimpleXMLNode.indt++;
			sb.append("\n");
			Iterator<SimpleXMLNode> nnli = this.nnl.iterator();
			while(nnli.hasNext())
				nnli.next().toXMLStr(sb);
			SimpleXMLNode.indt--;
		}
		else
		{ ;}

		if((! this.isData) && (this.nnl.size() > 0))
			SimpleXMLNode.addindent(sb);
		sb.append("</" + this.elementName + ">\n");
	}

	public static SimpleXMLNode parseXMLString(String s)
	{
		return new SimpleXMLParser(s).parseXMLStr();
	}

	public static String xmlifyString(String s)
	{
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < s.length(); ++i)
		{
			if(s.charAt(i) == '&')
				sb.append("&amp;");
			else if(s.charAt(i) == '<')
				sb.append("&lt;");
			else if(s.charAt(i) == '>')
				sb.append("&gt;");
			else if(s.charAt(i) == '\'')
				sb.append("&apos;");
			else if(s.charAt(i) == '\"')
				sb.append("&quot;");
			else
				sb.append(s.charAt(i));
		}

		return sb.toString();
	}

	public static boolean stringMatchFrom(String ms, String os, int loc)
	{
		if(ms.length() < os.length() + loc)
			return false;
		else
		{
			for(int i = 0; i < os.length(); ++i)
			{
				if(os.charAt(i) != ms.charAt(loc + i))
					return false;
			}

			return true;
		}
	}

	public static String deXMLifyString(String s)
	{
		StringBuilder sb = new StringBuilder();
		int i = 0;
		while(i < s.length())
		{
			if(stringMatchFrom(s, "&amp;", i))
			{
				sb.append("&");
				i += 5;
			}
			else if(stringMatchFrom(s, "&lt;", i))
			{
				sb.append("<");
				i += 4;
			}
			else if(stringMatchFrom(s, "&gt;", i))
			{
				sb.append(">");
				i += 4;
			}
			else if(stringMatchFrom(s, "&apos;", i))
			{
				sb.append("\'");
				i += 6;
			}
			else if(stringMatchFrom(s, "&quot;", i))
			{
				sb.append("\"");
				i += 6;
			}
			else
			{
				sb.append(s.charAt(i));
				++i;
			}
		}

		return sb.toString();
	}
}
