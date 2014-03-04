package edu.tufts.cs.eaftan.heapvis.parser.datastructures;

public class IntValue extends AbstractValue implements Value {
	private final int val;
	
	public IntValue(int i){
		val = i;
	}
	
	@Override
	public int getValueInt(){
		return val;
	}
	
	@Override
	public Type getType(){
		return Type.INT;		
	}
	
}
