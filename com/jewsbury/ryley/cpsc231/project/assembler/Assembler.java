package com.jewsbury.ryley.cpsc231.project.assembler;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;

public class Assembler
{
	public static final String RSSB_FILE_EXTENSION = ".rssb";
	public static final String COMMENT_CHARACTER = ";";
	public static final String LABEL_CHARACTER = ":";
	public static final String CONSTANT_CHARACTER = "=";
	
	//matches a pos/neg hex/dec value
	private static final String CONSTANT_REGEX = "[+-]?(0x[0-9a-fA-F]+|\\d+)";
	private static final int NUM_GENERAL_REGISTERS = 12;
	
	private int myTextOrigin;
	private int myDataOrigin;
	private int myStackOrigin;
	
	private Section mySection;
	
	private ArrayList<String> mySystemData;
	private ArrayList<String> myText;
	private ArrayList<String> myStack;
	private ArrayList<String> myData;
	
	private ArrayList<Label> myLabels;
	
	//used for displaying the RSSB in the simulator properly
	private ArrayList<String> myDisplayInfo;
	
	public Assembler()
	{
		//nothing should happen when the object's created,
		//because we need to initialize everything at assemble time
	}
	
	public void assemble(String sourcePath) throws IOException
	{
		File sourceFile = new File(sourcePath);
		assemble(sourceFile,false);
	}
	
	public void assemble(File sourceFile) throws IOException
	{
		assemble(sourceFile,false);
	}
	
	public void assemble(File sourceFile,boolean withDisplayInfo) throws IOException
	{
		if(!sourceFile.getPath().endsWith(RSSB_FILE_EXTENSION))
			throw new IllegalArgumentException("The source file was not a "+RSSB_FILE_EXTENSION+" file");
		
		ArrayList<String> myOutput;
		String outputPath = sourceFile.getPath().split("\\.")[0]+".flash";
		File outputFile = new File(outputPath);
		
		mySystemData	= new ArrayList<String>();
		myText			= new ArrayList<String>();
		myStack			= new ArrayList<String>();
		myData			= new ArrayList<String>();
		myLabels		= new ArrayList<Label>();
		
		myDisplayInfo	= new ArrayList<String>();
		
		myTextOrigin = 0;
		myDataOrigin = 0;
		myStackOrigin = 0;
		
		addSystemLabels();
		
		//System.out.println(getWordFormat(myTextOrigin));
		//System.out.println(getWordFormat(myStackOrigin));
		//System.out.println(getWordFormat(myDataOrigin));
		
		readSource(sourceFile);
		
		if(withDisplayInfo)
			buildDisplayInfo();
		
		setupSystemData();
		
		convertLines(myText);
		convertLines(myStack);
		convertLines(myData);
		
		myOutput = buildOutput();
		
		Files.write(outputFile.toPath(), myOutput, Charset.forName("UTF-8"));
		
		System.out.println("File assembled.");
	}
	
	public ArrayList<String> getDisplayInfo()
	{
		return myDisplayInfo;
	}
	
	private void buildDisplayInfo()
	{
		for(int i=0; i<myText.size();i++)
		{
			for(Label l:myLabels)
				if(l.getAddress()==i+myTextOrigin)
					myDisplayInfo.add("     "+l.getLabel()+":");
			myDisplayInfo.add("       "+getWordFormat(i+myTextOrigin)+"> rssb "+myText.get(i));
		}
	}
	
	private ArrayList<String> buildOutput()
	{
		if(myTextOrigin == 0)
			throw new AssemblerException("No text origin specified");
		if(myStackOrigin == 0)
			throw new AssemblerException("No stack origin specified");
		if(myDataOrigin == 0)
			throw new AssemblerException("No data origin specified");
		
		ArrayList<String> output = new ArrayList<>();
		
		for(int i=0; i<mySystemData.size();i++)
			output.add("<"+getWordFormat(i)+">: "+mySystemData.get(i));
		output.add("");
		
		if(!myText.isEmpty())
		{
			for(int i=0; i<myText.size();i++)
				output.add("<"+getWordFormat(myTextOrigin+i)+">: "+myText.get(i));
			output.add("");
		}
		
		if(!myStack.isEmpty())
		{
			for(int i=0; i<myStack.size();i++)
				output.add("<"+getWordFormat(myStackOrigin+i)+">: "+myStack.get(i));
			output.add("");
		}
		
		if(!myData.isEmpty())
		{
			for(int i=0; i<myData.size();i++)
				output.add("<"+getWordFormat(myDataOrigin+i)+">: "+myData.get(i));
			output.add("");
		}
		
		return output;
	}
	
	private void addSystemLabels()
	{
		myLabels.add(new Label("IP", 0));
		myLabels.add(new Label("ACC", 1));
		myLabels.add(new Label("ZERO", 2));
		myLabels.add(new Label("TEMP", 3));
		myLabels.add(new Label("SP", 4));
		myLabels.add(new Label("LR", 5));
		for(int i=0;i<NUM_GENERAL_REGISTERS;i++)
			myLabels.add(new Label("R"+i, i+6));
	}
	
	private void setupSystemData()
	{
		mySystemData.add(getWordFormat(myTextOrigin));	//IP
		mySystemData.add(getWordFormat(0));				//ACC
		mySystemData.add(getWordFormat(0));				//ZERO
		mySystemData.add(getWordFormat(0));				//TEMP
		mySystemData.add(getWordFormat(myStackOrigin+myStack.size()));	//SP
		mySystemData.add(getWordFormat(2));				//LR
		for(int i=0;i<NUM_GENERAL_REGISTERS;i++)
			mySystemData.add(getWordFormat(0));				//Ri
	}
	
	private String getWordFormat(int address)
	{
		//we CANNOT use negative values for address space because
		//BRANCHING will be off by 1 if the destination is a negative address,
		//HOWEVER, we have to allow values to be written in word format
		if(address < -Math.pow(2, 15) || address >= Math.pow(2, 16))
			throw new AssemblerException("Address out of bounds");
		
		if(address < 0)
			address += Math.pow(2, 16);
		
		return String.format("%04x", address);
	}
	
	private void readSource(File sourceFile) throws IOException
	{
		if(!sourceFile.canRead())
			throw new IOException("The source file could not be read.");
		
		int lineNum = 0;
		Scanner sourceReader = new Scanner(sourceFile);
		
		System.out.println("Reading RSSB...");
		while(sourceReader.hasNextLine())
		{
			lineNum++;
			try{
				interpretLine(sourceReader.nextLine());
			}catch(RuntimeException e){
				System.err.println("Assembler error on line ( "+lineNum+" ):");
				sourceReader.close();
				throw e;
			}
		}
		
		sourceReader.close();
	}
	
	private void interpretLine(String line)
	{
		//strip comments
		line = line.split(COMMENT_CHARACTER)[0];
		//separate the label (includes the empty string after ":" if it was the only thing on the line)
		String[] tokens = line.split(LABEL_CHARACTER,2);
		
		if(tokens.length>1)		//if there was a label
		{
			
			interpretLabel(tokens[0].trim());
			line = tokens[1];	//strip the label
		}
		
		//trims leading whitespace to avoid an empty first token
		tokens = line.trim().split("\\s+",2);
		
		if(tokens[0].equals(".section"))
		{
			switch(tokens[1])
			{
				case ".text":
					mySection = Section.TEXT;
					break;
				case ".stack":
					mySection = Section.STACK;
					break;
				case ".data":
					mySection = Section.DATA;
					break;
				default:
					throw new AssemblerException("Illegal section.");
			}
		}
		else if(tokens[0].equals(".origin"))
		{
			int origin;
			if(tokens[1].startsWith("0x"))
				origin = Integer.parseInt(tokens[1].substring(2),16);
			else
				origin = Integer.parseInt(tokens[1]);
			
			switch(mySection)
			{
				case TEXT:
					myTextOrigin = origin;
					break;
				case STACK:
					myStackOrigin = origin;
					break;
				case DATA:
					myDataOrigin = origin;
					break;
				default:
					throw new AssemblerException("Illegal section.");
			}
		}
		else if(tokens[0].equalsIgnoreCase("rssb"))
		{
			line = tokens[1];
			switch(mySection)
			{
				case TEXT:
					myText.add(line);
					break;
				case STACK:
					myStack.add(line);
					break;
				case DATA:
					myData.add(line);
					break;
				default:
					throw new AssemblerException("No section defined");
			}
		}
		else if(!tokens[0].equals(""))
			throw new AssemblerException("Illegal command.");
	}
	
	private void interpretLabel(String labelString)
	{
		int address;
		switch(mySection)
		{
			case TEXT:
				address = myTextOrigin + myText.size();
				break;
			case STACK:
				address = myStackOrigin + myStack.size();
				break;
			case DATA:
				address = myDataOrigin + myData.size();
				break;
			default:
				throw new AssemblerException("No section defined");
		}
		
		Label label = new Label(labelString, address);
		//throw a compile time error if the label contains whitespace or starts with a reserved character
		if(labelString.split("\\s+").length > 1)
			throw new AssemblerException("Illegal label name: \""+label+"\"");
		if(myLabels.contains(label))
			throw new AssemblerException("Identical label name: \""+label+"\"");
		
		myLabels.add(label);
	}
	
	private void convertLines(ArrayList<String> lines)
	{
		String line;
		int address;
		int constant;
		for(int i=0; i<lines.size(); i++)
		{
			line = lines.get(i);
			address = -1;
			constant = -1;
			
			if(line.startsWith("("))
			{
				address = interpretEquation(line);
			}
			else
			{
				if(line.matches(CONSTANT_CHARACTER+CONSTANT_REGEX))
				{
					if(line.contains("0x"))
					{
						line = line.replace("0x", "");
						constant = Integer.parseInt(line.substring(1), 16);
					}
					else
						constant = Integer.parseInt(line.substring(1));
					line = "="+getWordFormat(constant);
				}
				
				address = getLabelAddress(line);
				
				if(address == -1)
				{
					if(line.matches(CONSTANT_CHARACTER+"[0-9a-fA-F]+"))
					{
						//if the number was a constant, and we didn't already have that constant,
						myLabels.add(new Label(line, mySystemData.size()));
						address = mySystemData.size();
						mySystemData.add(getWordFormat(constant));
					}
					else if(line.startsWith(CONSTANT_CHARACTER))
					{
						//if the constant is not a number, check if it's a label
						constant = getLabelAddress(line.substring(1));
						if(constant != -1)
						{
							myLabels.add(new Label(line, mySystemData.size()));
							address = mySystemData.size();
							mySystemData.add(getWordFormat(constant));
						}
						else
							throw new AssemblerException("Illegal argument");
					}
					else if(line.matches(CONSTANT_REGEX))
					{
						if(line.contains("0x"))
						{
							line = line.replace("0x", "");
							address = Integer.parseInt(line, 16);
						}
						else
							address = Integer.parseInt(line);
						
					}
					else
						throw new AssemblerException("Illegal argument");
				}
			}
			
			lines.set(i, getWordFormat(address));
		}
	}
	
	private int interpretEquation(String line)
	{
		//System.out.println(line);
		
		int address = 0;
		int sign = 0;
		int temp = 0;
		String token;
		//removes whitespace and the opening bracket
		line = line.replaceAll("\\s", "").substring(1);
		if(line.charAt(0)!='-' || line.charAt(0)!='+')
			line = "+"+line;	//adds a + just to make things consistent
		while(line.charAt(0)!=')')
		{
			if(line.charAt(0)=='+')
				sign = 1;
			else
				sign = -1;
			line = line.substring(1);
			token = line.split("[)+-]")[0];
			
			if(token.matches(CONSTANT_REGEX))
			{
				if(token.contains("0x"))
				{
					temp = Integer.parseInt(token.replace("0x", ""), 16);
				}
				else
					temp = Integer.parseInt(token);
			}
			else
			{
				//the token is not a constant
				temp = getLabelAddress(token);
				if(temp == -1)
					throw new AssemblerException("Illegal label: "+token);
			}
			address += sign*temp;
			line = line.substring(token.length());
		}
		
		return address;
	}
	
	private int getLabelAddress(String name)
	{
		for(Label label: myLabels)
		{
			if(label.getLabel().equalsIgnoreCase(name))
				return label.getAddress();
		}
		
		return -1;
	}
	
	/**
	 * Originally, I wanted to keep the format of the assembler close to that of
	 * Atmel Studio, however I only really need the origin of each section so I just
	 * added that as a command.
	 * 
	 * I'm keeping this in case I ever decide to go back to a linker format
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private void readLinker(String linkerPath) throws IOException
	{
		File linkerFile = new File(linkerPath);
		if(!linkerFile.canRead())
			throw new IOException("The linker file could not be read.");
		
		Scanner linker = new Scanner(linkerFile);
		String line;
		String token;
		
		System.out.println("Reading Linker...");
		
		while(linker.hasNextLine())
		{
			line = linker.nextLine().trim();
			int radix;
			int value;
			
			if(line.startsWith(".text:"))
				mySection = Section.TEXT;
			if(line.startsWith(".stack:"))
				mySection = Section.STACK;
			if(line.startsWith(".data:"))
				mySection = Section.DATA;
			
			if(line.contains("ORIGIN"))
			{
				//start when we see ORIGIN
				token = line.split("ORIGIN\\s*=\\s*")[1];
				//end when we see an ending character, or nothing
				token = token.split("[,;]")[0];
				
				if(token.startsWith("0x"))
				{
					radix = 16;
					token = token.substring(2);
				}
				else if(token.startsWith("#"))
				{
					radix = 10;
					token = token.substring(1);
				}
				else
					radix = 10;
				
				value = Integer.parseUnsignedInt(token, radix);
				
				switch(mySection)
				{
					case TEXT:
						myTextOrigin = value;
						break;
					case DATA:
						myDataOrigin = value;
						break;
					case STACK:
						myStackOrigin = value;
						break;
					default:
						throw new AssemblerException("No section defined");
				}
			}
		}
		
		linker.close();
	}
}
