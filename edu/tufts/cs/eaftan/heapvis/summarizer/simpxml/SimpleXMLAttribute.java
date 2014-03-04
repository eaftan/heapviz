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
