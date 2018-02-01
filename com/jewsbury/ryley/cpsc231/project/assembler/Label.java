package com.jewsbury.ryley.cpsc231.project.assembler;

public class Label
{
	private String myLabel;
	private int myAddress;
	
	public Label(String label, int address)
	{
		myLabel = label;
		myAddress = address;
	}
	
	public int getAddress()
	{
		return myAddress;
	}
	
	public String getLabel()
	{
		return myLabel;
	}
	
	public boolean equals(Object o)
	{
		if(o instanceof Label)
			return equals((Label)o);
		else
			return false;
	}
	
	public boolean equals(Label l)
	{
		return myLabel.equalsIgnoreCase(l.myLabel);
	}
}
