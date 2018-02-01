package com.jewsbury.ryley.cpsc231.project.simulator.view;

import java.awt.*;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.text.DefaultCaret;

import com.jewsbury.ryley.cpsc231.project.simulator.SimulatorMain;
import com.jewsbury.ryley.cpsc231.project.simulator.model.Memory;

/**
 * Displays the contents of memory
 *
 * @author Ryley Jewsbury
 */
public class MemoryPanel extends JPanel
{
	//display information
	public static final int PANEL_WIDTH = 600;
	public static final int ADDRESSES_PER_LINE = 12;
	
	private JTextArea myRegisterText;
	private JTextArea myMemoryText;
	private Memory myMemory;
	private ArrayList<String> myLines;
	
	public MemoryPanel(Memory memory)
	{
		myMemory = memory;
		myLines = new ArrayList<String>();
		
		//displays components in a vertical line
		BoxLayout layout = new BoxLayout(this,BoxLayout.Y_AXIS);
		setLayout(layout);
		
		myRegisterText = new JTextArea();
		myRegisterText.setFont(SimulatorMain.GLOBAL_FONT);
		myRegisterText.setEditable(false);
		
		myMemoryText = new JTextArea();
		myMemoryText.setFont(SimulatorMain.GLOBAL_FONT);
		myMemoryText.setEditable(false);
		((DefaultCaret)myMemoryText.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		
		displayRegisters();
		initLines();
		
		JScrollPane scrollPane = new JScrollPane(myMemoryText);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		//add the components
		//add(Box.createVerticalStrut(10));
		add(myRegisterText);
		//add(Box.createVerticalStrut(10));
		add(scrollPane);
		
		//sets the preferred width
		setPreferredSize(new Dimension(PANEL_WIDTH,Integer.MAX_VALUE));
	}
	
	/**
	 * Shortcut method to make startup faster
	 */
	public void initLines()
	{
		String line = "";
		for(int i=0;i<ADDRESSES_PER_LINE;i++)
			line += "0000 ";
		for(int i=0;i<ADDRESSES_PER_LINE;i++)
			line += ".";
		
		for(int i=0; i<Memory.MEMORY_SIZE;i+=ADDRESSES_PER_LINE)
			myLines.add(String.format("<%04x>: "+line, i));
		
		if(Memory.MEMORY_SIZE%ADDRESSES_PER_LINE != 0)
		{
			
			line = String.format("<%04x>: ", Memory.MEMORY_SIZE-Memory.MEMORY_SIZE%ADDRESSES_PER_LINE);
			for(int i=0;i<ADDRESSES_PER_LINE;i++)
				if(i<Memory.MEMORY_SIZE%ADDRESSES_PER_LINE)
					line += "0000 ";
				else
					line += "     ";
			for(int i=0;i<Memory.MEMORY_SIZE%ADDRESSES_PER_LINE;i++)
				line += ".";
			myLines.set(myLines.size()-1, line);
		}
		
		//System.out.println("Memory Initialized");
		
		myMemoryText.setText("");
		for(String output: myLines)
			myMemoryText.append(output+System.lineSeparator());
		
		//System.out.println("Drawn");
	}
	
	public void displayMemory()
	{
		displayRegisters();
		displayChanges();
	}
	
	/**
	 * Only builds the lines that have changed since last time to improve performance
	 */
	public void displayChanges()
	{
		ArrayList<Integer> changes = myMemory.getChanges();
		ArrayList<Integer> linesChanged = new ArrayList<>();
		String line = "";
		String charMemory = "";
		char memorySymbol;
		int lineNum;
		int maxLine = 1;
		
		//long start = System.currentTimeMillis();
		
		for(int address: changes)
		{
			lineNum = (address/ADDRESSES_PER_LINE);
			if(!linesChanged.contains(lineNum))
			{
				maxLine = Math.max(maxLine, lineNum);
				linesChanged.add(lineNum);
				
				line = String.format("<%04x>: ", lineNum*ADDRESSES_PER_LINE);
				
				//add addresses until we fill the line or run out
				for(int i=0; i<ADDRESSES_PER_LINE && lineNum*ADDRESSES_PER_LINE+i<Memory.MEMORY_SIZE;i++)
				{
					line += (myMemory.getWord(lineNum*ADDRESSES_PER_LINE+i)+" ");
					memorySymbol = (char)myMemory.get(lineNum*ADDRESSES_PER_LINE+i);
					//if the character is a control character, don't print it properly
					charMemory += memorySymbol<32? '.': memorySymbol;
				}
				
				line += charMemory;
				charMemory = "";
				myLines.set(lineNum, line);
			}
		}
		myMemory.clearChanges();
		//System.out.println("String Built");
		
		//unchanged text goes here
		line = myMemoryText.getText().split("\\n",maxLine+2)[maxLine+1];
		
		myMemoryText.setText("");
		for(int i=0; i<= maxLine;i++)
			myMemoryText.append(myLines.get(i)+System.lineSeparator());
		//add the unchanged lines
		myMemoryText.append(line);
		
		//System.out.println(System.currentTimeMillis()-start);
		//System.out.println("Drawn");
	}
	
	public void displayRegisters()
	{
		String content = "";
		final String[] NAMES =
			{"  IP:   ","    ACC:  ","    ZERO: ","    TMP:  ","    SP:   ","    LR:   ",
			"\r\n  R0:   ","    R1:   ","    R2:   ","    R3:   ","    R4:   ","    R5:   ",
			"\r\n  R6:   ","    R7:   ","    R8:   ","    R9:   ","    R10:  ","    R11:  "};
		
		
		for(int i=0;i<NAMES.length;i++)
			content += (NAMES[i]+myMemory.getWord(i));
		
		//System.out.println("Register Built");
		myRegisterText.setText(content);
		//System.out.println("Register Drawn");
	}
}
