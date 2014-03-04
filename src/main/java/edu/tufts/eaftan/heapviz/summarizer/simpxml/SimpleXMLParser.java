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

package edu.tufts.eaftan.heapviz.summarizer.simpxml;

import java.util.*;

public final class SimpleXMLParser
{
	private String s;
	private int pos;

	public SimpleXMLParser(String ss)
	{
		this.s = ss;
		this.pos = 0;
	}

	public SimpleXMLNode parseXMLStr()
	{
		SimpleXMLNode rn = null;
		this.advanceWhiteSpace();
		LinkedList<SimpleXMLNode> ps = new LinkedList<SimpleXMLNode>();
		ps.addLast(this.readOpenTag());

		while(ps.size() > 0)
		{
			if(this.s.charAt(this.pos) == '<' && this.s.charAt(this.pos + 1) == '/')
			{
				SimpleXMLNode nn = ps.getLast();
				ps.removeLast();
				this.readCloseTag(nn.eName());

				if(ps.size() == 0)
					rn = nn;
				else
					ps.getLast().getChildren().add(nn);
			}
			else if(this.s.charAt(this.pos) == '<')
			{
				SimpleXMLNode nn = this.readOpenTag();
				ps.addLast(nn);
			}
			else
			{
				SimpleXMLNode nn = ps.getLast();
				nn.setCData(this.readCData());
			}
		}

		return rn;
	}

	@Override
	public String toString()
	{
		int window = 10;

		int ploc = Math.max(0, this.pos - window);
		int psize = this.pos - ploc;

		int asize = Math.min(window, this.s.length() - this.pos);

		return this.s.substring(ploc, ploc + psize) + "^" + this.s.substring(this.pos, this.pos + asize);
	}

	private void advanceWhiteSpace()
	{
		while(this.pos < this.s.length() && Character.isWhitespace(this.s.charAt(this.pos)))
			++this.pos;
	}

	private SimpleXMLAttribute parseXMLAttribute()
	{
		this.advanceWhiteSpace();
		int eqloc = this.s.indexOf('=', this.pos);
		int endloc = this.s.indexOf('\"', eqloc + 2);

		String atrn = this.s.substring(this.pos, eqloc);
		String atv = this.s.substring(eqloc + 2, endloc);

		this.pos = endloc + 1;
		return new SimpleXMLAttribute(atrn, atv);
	}

	private SimpleXMLNode readOpenTag()
	{
		if(this.s.charAt(this.pos) != '<')
		{
			System.out.println("Not at the start of an open tag");
			System.exit(1);
		}
		
		this.pos++; //advance off the <

		int a = this.s.indexOf(' ', this.pos);
		int b = this.s.indexOf('>', this.pos);
		int c = Math.min(a, b);
		String ename = this.s.substring(this.pos, c);
		this.pos = c;

		Vector<SimpleXMLAttribute> attrl = new Vector<SimpleXMLAttribute>();
		while(this.s.charAt(this.pos) != '>')
			attrl.add(this.parseXMLAttribute());

		this.pos++;
		this.advanceWhiteSpace();

		if(this.s.charAt(this.pos) == '<')
			return new SimpleXMLNode(ename, new Vector<SimpleXMLNode>(), attrl);
		else
			return new SimpleXMLNode(ename, "", attrl);
	}

	private void readCloseTag(String eName)
	{
		if(! ((this.s.charAt(this.pos) == '<') && (this.s.charAt(this.pos + 1) == '/')))
		{
			System.out.println("not at close tag yet");
			System.exit(1);
		}
		
		int cl = this.s.indexOf('>', this.pos);
		String cet = this.s.substring(this.pos + 2, cl);

		if(! eName.equals(cet))
		{
			System.out.println("Open and Close tag mismatch");
			System.exit(1);
		}
		
		this.pos = cl + 1;
		this.advanceWhiteSpace();
	}

	private String readCData()
	{
		int cl = this.s.indexOf('<', this.pos);
		String cds = SimpleXMLNode.deXMLifyString(this.s.substring(this.pos, cl));

		this.pos = cl;
		return cds;
	}
}
