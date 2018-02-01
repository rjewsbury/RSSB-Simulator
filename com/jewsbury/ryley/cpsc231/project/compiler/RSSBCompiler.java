package com.jewsbury.ryley.cpsc231.project.compiler;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;

public class RSSBCompiler
{
	public static final String SOURCE_FILE_EXTENSION = ".src";
	public static final String COMMENT_CHARACTER = ";";
	public static final String LABEL_CHARACTER = ":";
	public static final String RESERVED_LABEL_CHARACTER = "_";
	public static final String CONSTANT_CHARACTER = "=";
	public static final String ASSEMBLER_CHARACTER = ".";
	
	//matches either [ ] , surrounded by arbitrary whitespace or just whitespace
	private static final String FILLER_REGEX = "[\\s,\\[\\]]+"; //"\\s*[\\[\\],]\\s*|\\s+"; //this didnt allow "R0, [R1]"
	//matches a pos/neg hex/dec zero value
	private static final String ZERO_REGEX = "=[+-]?(0x)?0+";
	//matches a pos/neg hex/dec value
	private static final String CONSTANT_REGEX = "[+-]?(0x[0-9a-fA-F]+|\\d+)";
	
	//used to actually write the rssb code
	private Script myScript;
	//makes sure we dont use a label twice
	private ArrayList<String> myLabels;
	
	public RSSBCompiler()
	{
		
	}
	
	public void compile(String sourcePath) throws IOException
	{
		File sourceFile = new File(sourcePath);
		compile(sourceFile);
	}
	
	/**
	 * Compiles the given source file into a RSSB file
	 * @throws IOException if either the source file could not be read
	 * 						or the output file could not be written
	 */
	public void compile(File sourceFile) throws IOException
	{
		if(!sourceFile.getName().endsWith(SOURCE_FILE_EXTENSION))
			throw new IllegalArgumentException("The source file was not a "+SOURCE_FILE_EXTENSION+" file");
		
		int lineNum = 0;
		Scanner sourceReader;
		String outputPath = sourceFile.getPath().split("\\.")[0]+".rssb";
		
		File outputFile = new File(outputPath);
		
		if(!sourceFile.canRead())
			throw new IOException("The source file could not be read.");
		
		sourceReader	= new Scanner(sourceFile);
		myScript		= new Script();
		myLabels		= new ArrayList<String>();
		
		System.out.println("Compiling...");
		
		while(sourceReader.hasNextLine())
		{
			lineNum++;
			try{
				interpretLine(sourceReader.nextLine());
			}catch(RuntimeException e)
			{
				System.out.println("Compiler error on line ( "+lineNum+" ):");
				e.printStackTrace();
				
				sourceReader.close();
				return;
			}
		}
		
		Files.write(outputFile.toPath(), myScript.getLines(), Charset.forName("UTF-8"));
		
		System.out.println("File compiled.");
		sourceReader.close();
	}
	
	/**
	 * Interprets a line consisting of up to...
	 * 		-a single label
	 * 		-a single operation
	 * 		-(0-3) operands
	 * 		-arbitrary length comments
	 * in that order.
	 * 
	 * Converts each operation into a script of RSSB instructions
	 * 
	 * @param line the line to be interpreted
	 */
	private void interpretLine(String line)
	{
		line = line.split(COMMENT_CHARACTER)[0];			//strip comments
		String[] tokens = line.split(LABEL_CHARACTER,2);	//separate the label (includes the empty string after ":" if it was the only thing on the line)
		
		if(tokens.length>1)		//if there was a label
		{
			interpretLabel(tokens[0].trim());
			line = tokens[1];	//strip the label
		}
		
		//trims leading whitespace to avoid an empty first token
		tokens = line.trim().split(FILLER_REGEX);
		
		interpretScript(tokens);
	}
	
	/**
	 * Interprets a label. ensures that a label  has not been used before,
	 * and does not equal a reserved compiler label.
	 * 
	 * @param label the label to be interpreted.
	 */
	private void interpretLabel(String label)
	{
		//throw a compile time error if the label contains whitespace or starts with a reserved character
		if(label.split("\\s+").length > 1)
			throw new CompilerException("Illegal label name: \""+label+"\"");
		if(label.startsWith(RESERVED_LABEL_CHARACTER))
			throw new CompilerException("Reserved label name: \""+label+"\"");
		if(myLabels.contains(label))
			throw new CompilerException("Identical label name: \""+label+"\"");
		
		myLabels.add(label);
		myScript.label(label);
	}
	
	/**
	 * Interprets a operation followed by 0-3 operands
	 * @param tokens
	 */
	private void interpretScript(String[] tokens)
	{
		if(tokens[0].startsWith(ASSEMBLER_CHARACTER))
		{
			if(tokens[0].equals(ASSEMBLER_CHARACTER+"section"))
				myScript.section(tokens[1]);
			else if(tokens[0].equals(ASSEMBLER_CHARACTER+"origin"))
				myScript.origin(tokens[1]);
			else if(tokens[0].equals(ASSEMBLER_CHARACTER+"word"))
				myScript.word(tokens[1]);
			else
				throw new CompilerException("Illegal compiler command.");
		}
		else
		{
			for(int i=0;i<tokens.length;i++)
			{
				if(tokens[i].matches(CONSTANT_REGEX))
					//if we're moving a constant, mark it as a constant
					tokens[i] = CONSTANT_CHARACTER+tokens[i];
				
				//System.out.print("\""+tokens[i]+"\"");
			}
			//System.out.println();
			
			switch(tokens[0].toUpperCase())
			{
				case "NOP":
					break;
				case "INIT":
					myScript.init(tokens[1]);
					break;
				case "MOV":
					if(tokens[1].equalsIgnoreCase(tokens[2])){/*NOP*/}
					//if we're moving 0, use init
					else if(tokens[2].matches(ZERO_REGEX))
						myScript.init(tokens[1]);
					else
						myScript.mov(tokens[1], tokens[2]);
					break;
				case "MOVN":
					if(tokens[1].equalsIgnoreCase(tokens[2]))
						myScript.neg(tokens[1]);
					//if we're moving 0, use init
					else if(tokens[2].matches(ZERO_REGEX))
						myScript.init(tokens[1]);
					//if we're moving a constant, mark it as a constant
					else
						myScript.movN(tokens[1], tokens[2]);
					break;
				case "NEG":
					myScript.neg(tokens[1]);
					break;
				case "SWAP":
					myScript.swap(tokens[1], tokens[2]);
					break;
				case "LOAD":
					if(tokens.length == 4)
						if(tokens[3].matches(ZERO_REGEX))
							myScript.loadIndirect(tokens[1], tokens[2]);
						else
							myScript.loadOffset(tokens[1], tokens[2], tokens[3]);
					else if(tokens.length == 3)
						myScript.loadIndirect(tokens[1], tokens[2]);
					else
						throw new CompilerException("Illegal arguments for LOAD");
					break;
				case "STR":
					if(tokens.length == 4)
						if(tokens[3].matches(ZERO_REGEX))
							myScript.storeIndirect(tokens[1], tokens[2]);
						else
							myScript.storeOffset(tokens[1], tokens[2], tokens[3]);
					else if(tokens.length == 3)
						myScript.storeIndirect(tokens[1], tokens[2]);
					else
						throw new CompilerException("Illegal arguments for STR");
					break;
				case "PUSH":
					myScript.push(tokens[1]);
					break;
				case "POP":
					myScript.pop(tokens[1]);
					break;
				case "ADD":
					if(tokens.length == 4)
					{
						if(tokens[2].matches(ZERO_REGEX))
							if(tokens[3].matches(ZERO_REGEX))
								myScript.init(tokens[1]);
							else
								myScript.mov(tokens[1], tokens[3]);
						else
							if(tokens[3].matches(ZERO_REGEX))
								myScript.mov(tokens[1], tokens[2]);
							else
								myScript.add3(tokens[1], tokens[2], tokens[3]);
					}
					else if(tokens.length == 3)
						if(tokens[2].matches(ZERO_REGEX)){/*NOP*/}
						else
							myScript.add2(tokens[1], tokens[2]);
					else
						throw new CompilerException("Illegal arguments for ADD");
					break;
				case "SUB":
					if(tokens.length == 4)
					{
						if(tokens[2].matches(ZERO_REGEX))
							if(tokens[3].matches(ZERO_REGEX))
								myScript.init(tokens[1]);
							else
								myScript.movN(tokens[1], tokens[3]);
						else
							if(tokens[3].matches(ZERO_REGEX))
								myScript.mov(tokens[1], tokens[2]);
							else
								myScript.sub3(tokens[1], tokens[2], tokens[3]);
					}
					else if(tokens.length == 3)
						if(tokens[2].matches(ZERO_REGEX)){/*NOP*/}
						else
							myScript.sub2(tokens[1], tokens[2]);
					else
						throw new CompilerException("Illegal arguments for SUB");
					break;
				case "SUBP":
					myScript.subP(tokens[1], tokens[2]);
					break;
				case "SUBN":
					myScript.subN(tokens[1], tokens[2]);
					break;
				case "IFLT":
					myScript.ifLess(tokens[1], tokens[2]);
					break;
				case "IFGT":
					myScript.ifGreater(tokens[1], tokens[2]);
					break;
				case "ELSE":
					myScript.ifElse();
					break;
				case "END":
					myScript.ifEnd();
					break;
				case "B":
					myScript.branch(tokens[1]);
					break;
				case "BL":
					myScript.branchLink(tokens[1]);
					break;
				case "BX":
					myScript.branchDirect(tokens[1]);
					break;
				case "BXL":
					myScript.branchDirectLink(tokens[1]);
					break;
				case "HALT":
					myScript.halt();
					break;
				default:
			}
		}
	}
}
