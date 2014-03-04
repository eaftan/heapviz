package edu.tufts.cs.eaftan.heapvis.parser.datastructures;

public class CharValue extends AbstractValue implements Value {
	private final char val;
	
	public  CharValue(char c){
		val = c;
	}
	
	@Override
	public char getValueChar(){
		return val;
	}
	

}
