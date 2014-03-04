package edu.tufts.cs.eaftan.heapvis.parser.datastructures;

public class ShortValue extends AbstractValue implements Value {
	private final short val;
	
	public ShortValue(short s){
		val = s;
	}

	@Override
	public short getValueShort(){
		return val;
	}
	
	@Override
	public Type getType(){
		return Type.SHORT;
	}
}
