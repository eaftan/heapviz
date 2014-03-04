package edu.tufts.cs.eaftan.heapvis.parser.datastructures;

public class DoubleValue extends AbstractValue implements Value {
	private final double val;
	
	public DoubleValue(Double d){
		val = d;
	}
	
	@Override
	public double getValueDouble(){
		return val;
	}
	
	@Override
	public Type getType(){
		return Type.DOUBLE;
	}
	
}
