package edu.tufts.cs.eaftan.heapvis.parser.datastructures;

public class FloatValue extends AbstractValue implements Value {
	
	private final float val;
	
	public FloatValue(float f){
		val = f;
	}
	
	@Override
	public float getValueFloat(){
		return val;
	}
	
	@Override
	public Type getType(){
		return Type.FLOAT;
	}
}
