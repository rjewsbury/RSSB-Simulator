package com.jewsbury.ryley.cpsc231.project.simulator.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Memory
{
	public static final String FLASH_FILE_EXTENSION = ".flash";
	public static final int MEMORY_SIZE = (int)Math.pow(2, 15);
	
	private int[] myMemory;
	private ArrayList<Integer> myChangedAddresses;
	
	public Memory()
	{
		//until I fix the scripts to allow for negative addresses,
		//we can not use negative values for address space
		myMemory = new int[MEMORY_SIZE];
		myChangedAddresses = new ArrayList<Integer>();
	}
	
	public int get(int address)
	{
		return myMemory[address];
	}
	
	public String getWord(int address)
	{
		return String.format("%04x", get(address));
	}
	
	public void set(int address, int val)
	{
		myMemory[address] = val;
		myChangedAddresses.add(address);
	}
	
	public ArrayList<Integer> getChanges()
	{
		return myChangedAddresses;
	}
	
	public void clearChanges()
	{
		myChangedAddresses = new ArrayList<Integer>();
	}
	
	public void loadFromFile(File sourceFile) throws FileNotFoundException
	{
		int address;
		
		if(!sourceFile.getName().endsWith(FLASH_FILE_EXTENSION))
			throw new IllegalArgumentException("The source file was not a "+FLASH_FILE_EXTENSION+" file");
		
		Scanner sourceReader = new Scanner(sourceFile);
		sourceReader.useDelimiter("[\\s<>:]+");
		
		while(sourceReader.hasNext())
		{
			address = Integer.parseInt(sourceReader.next(), 16);
			set(address, Integer.parseInt(sourceReader.next(), 16));
		}
		
		sourceReader.close();
	}
}
