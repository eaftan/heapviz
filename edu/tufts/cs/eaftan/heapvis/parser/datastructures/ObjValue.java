package edu.tufts.cs.eaftan.heapvis.parser.datastructures;

public class ObjValue extends AbstractValue implements Value {
	
	private final long val;
	
	public ObjValue(long l){
		val = l;
	}
	
	@Override
	public long getValueObj(){
		return val;
	}
	
	@Override
	public Type getType(){
		return Type.OBJ;
	}

}
