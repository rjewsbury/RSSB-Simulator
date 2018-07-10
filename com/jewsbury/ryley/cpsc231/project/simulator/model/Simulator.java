package com.jewsbury.ryley.cpsc231.project.simulator.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Simulator
{
	public static final int MAX = (int)Math.pow(2, 16);
	public static final int MAX_POSITIVE = (int)Math.pow(2, 15)-1;
	
	Memory myMemory;
	private int MAR, MDR, R;
	private boolean N;
	
	//this keeps track of break points for the simulator
	private ArrayList<Integer> myBreaks;
	
	public Simulator(Memory memory)
	{
		myMemory = memory;
		MAR = MDR = R = 0;
		N = false;
		
		myBreaks = new ArrayList<>();
	}
	
	public void setBreaks(ArrayList<Integer> breaks)
	{
		myBreaks = breaks;
	}

	public void run()
	{
		//do at least one step
		step();
		
		//in real hardware this would busyloop while(true)
		//stops when the program has reached a halted state or a user defined break
		//if there's a non-terminating program with no breaks, the program never responds. TODO: fix?
		while(!(myMemory.get(0) == 2 && myMemory.get(1) == 1) && !myBreaks.contains(myMemory.get(0)))
		{
			//display();
			step();
		}
		//display();
	}
	
	public void step()
	{
		//Cin, MARin, READ
		MAR = 1;
		MDR = myMemory.get(MAR);
		//MDRout, Rin
		R = MDR;
		//MARin, READ
		MAR = 0;
		MDR = myMemory.get(MAR);
		//MDRout, MARin, READ
		MAR = MDR;
			//System.out.printf("%04x%n",MAR);
		MDR = myMemory.get(MAR);
		//MDRout, MARin, READ
		MAR = MDR;
			//System.out.printf("\t%04x%n",MAR);
		MDR = myMemory.get(MAR);
		//MDRout, Comp, Cin, Nin, MDRin, WRITE
		MDR = (MDR - R + MAX)%MAX;
		N = MDR > MAX_POSITIVE;
		myMemory.set(MAR, MDR);
		//Cin, MARin, WRITE
		MAR = 1;
		myMemory.set(MAR, MDR);
		//Cin, MDRin
		MDR = 1;
		//Cin, MDRout, MARin
		MAR = 1+MDR;
		//MDRin, WRITE
		MDR = 0;
		myMemory.set(MAR, MDR);
		//MARin, READ
		MAR = 0;
		MDR = myMemory.get(MAR);
		//MDRout, Cin, MDRin, WRITE, NNEND
		MDR = 1+MDR;
		myMemory.set(MAR, MDR);
		if(!N) return;
		//MDRout, Cin, MDRin, WRITE
		MDR = 1+MDR;
		myMemory.set(MAR, MDR);
	}
	
	/**
	 * Temporary debug method so I can see what's going on
	 * (manually change the stack and data addresses depending on the program)
	 */
	@Deprecated
	public void display()
	{
		final int STACK = 16384;
		final int DATA  = 20480;
		
		System.out.println("System:[IP:"+myMemory.getWord(0)+
				",ACC:"+myMemory.getWord(1)+",ZERO:"+myMemory.getWord(2)+
				",TMP:"+myMemory.getWord(3)+",SP:"+myMemory.getWord(4)+
				",LR:"+myMemory.getWord(5)+",R0:"+myMemory.getWord(6)+
				",R1:"+myMemory.getWord(7)+",R2:"+myMemory.getWord(8)+
				",R3:"+myMemory.getWord(9)+",R4:"+myMemory.getWord(10)+
				"] Stack: ["+myMemory.getWord(STACK)+
				","+myMemory.getWord(STACK+1)+","+myMemory.getWord(STACK+2)+"]");
		System.out.print("\tData: ["+myMemory.getWord(DATA));
		for(int i=1;i<20;i++)
			System.out.print(","+myMemory.getWord(DATA+i));
		System.out.print("]");
		System.out.println();
	}
	
	public void loadFromFile(File sourceFile) throws IOException
	{
		myMemory.loadFromFile(sourceFile);
	}
}
