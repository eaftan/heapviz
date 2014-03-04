package edu.tufts.cs.eaftan.heapvis.parser.datastructures;

public class LongValue extends AbstractValue implements Value {
	private final long val;
	
	public LongValue(long l){
		val = l;
	}
	
	@Override
	public long getValueLong(){
		return val;
	}
	
	@Override
	public Type getType(){
		return Type.LONG;
	}
	

}
