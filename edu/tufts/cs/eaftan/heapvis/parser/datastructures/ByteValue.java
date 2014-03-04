package edu.tufts.cs.eaftan.heapvis.parser.datastructures;

public class ByteValue extends AbstractValue implements Value {
	
	private final byte val;
	
	public ByteValue(byte b){
		val = b;
	}
	
	@Override
	public byte getValueByte(){
		return val;
	}

}
