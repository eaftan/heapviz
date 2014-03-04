package edu.tufts.cs.eaftan.heapvis.parser.datastructures;

public interface Value {
	  public long getValueObj();
	  public boolean getValueBool();
	  public float getValueFloat();
	  public double getValueDouble();
	  public short getValueShort();
	  public int getValueInt();
	  public String toString();
	  public Type getType();
}
