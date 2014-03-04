package edu.tufts.cs.eaftan.heapvis.parser.datastructures;

public abstract class AbstractValue implements Value {
	private boolean initialized;
	
	@Override
	public Type getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getValueBool() {
		typeError();
		return false;
	}

	@Override
	public double getValueDouble() {
		typeError();
		return 0;
	}

	@Override
	public float getValueFloat() {
		typeError();
		return 0;
	}

	@Override
	public int getValueInt() {
		typeError();
		return 0;
	}

	@Override
	public long getValueObj() {
		typeError();
		return 0;
	}

	@Override
	public short getValueShort() {
		typeError();
		return 0;
	}
	
	public long getValueLong(){
		typeError();
		return 0;
	}
	
	void typeError(){
	      System.err.println("Error: wrong type in Value");
	      System.exit(1);
	}
	
	public char getValueChar(){
		System.err.println("Error: wrong type in Value");
		System.exit(1);		
		return 0;
	}
	
	public byte getValueByte(){
		System.err.println("Error: wrong type in Value");
		System.exit(1);		
		return 0;
	}
	
	void initilizedOrBust(){
	    if (!initialized) {
	        System.err.println("Error: attempt to read from uninitialized value field");
	        System.exit(1);
	      }
	}	
	
	
	
	
}
