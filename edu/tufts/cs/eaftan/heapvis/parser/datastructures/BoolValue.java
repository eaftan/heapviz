package edu.tufts.cs.eaftan.heapvis.parser.datastructures;

public class BoolValue extends AbstractValue implements Value {
	
	private final boolean val;
	
	public BoolValue(boolean b){
		val = b;
	}
	
	@Override 
	public boolean getValueBool(){
		return val;
	}
	
	@Override
	public Type getType(){
				return Type.BOOL;
	}

}
