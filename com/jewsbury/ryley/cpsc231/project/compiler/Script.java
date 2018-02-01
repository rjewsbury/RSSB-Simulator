package com.jewsbury.ryley.cpsc231.project.compiler;

import java.util.ArrayList;
import java.util.Stack;

/**
 * All The available scripts.
 * Complete list:
 * 		NOP
 * Movement:
 * 		INIT	A
 * 		MOV		A, B
 * 		MOVN	A, B
 * 		NEG		A
 * 		SWAP	A, B
 * 		LOAD	A, [B]
 * 		LOAD	A, [B,C]
 * 		STR		A, [B]
 * 		STR		A, [B,C]
 * 		PUSH	A
 * 		POP		A
 * Arithmetic:
 * 		ADD		A, B, C
 * 		ADD		A, B
 * 		SUB		A, B, C
 * 		SUB		A, B
 * 		SUBP	A, B		//remove?
 * 		SUBN	A, B		//remove?
 * Control:
 * 		IFLT	A, B
 * 		IFGT	A, B
 * 		ELSE
 * 		END
 * 		B		label
 * 		BL		label
 * 		BX		A
 * 		BXL		A
 * 		HALT
 * 
 * Changelog:
 * 	- Fixed an off-by-one bug with the constants in BL and BXL
 *  - Fixed a bug where BXL LR would update the link register before using it
 * 
 * @author Ryley Jewsbury
 */
public class Script
{
	//special registers - just in case I decide to change the names later
	public static final String	IP	= "IP",
								ACC	= "ACC",
								ZERO= "ZERO",
								TEMP= "TEMP",
								SP	= "SP",
								LR	= "LR";
	
	private ArrayList<String>	myLines;
	
	//label variables
	private int loadCount;
	private int storeCount;
	private int branchCount;
	private int ifCount;
	private Stack<Integer> lastIf;
	
	public Script()
	{
		myLines		= new ArrayList<String>();
		
		loadCount	= 0;
		storeCount	= 0;
		branchCount	= 0;
		ifCount		= 0;
		lastIf		= new Stack<>();
	}
	
	/* --------------------- COMPILER SCRIPTS ---------------------------- */
	//not sure if I should put them in here
	public ArrayList<String> getLines()
	{
		return myLines;
	}
	
	public void label(String label)
	{
		myLines.add(label+":");
	}
	
	public void section(String section)
	{
		myLines.add(".section "+section);
	}
	
	public void origin(String section)
	{
		myLines.add(".origin "+section);
	}
	
	public void word(String word)
	{
		myLines.add("\trssb "+word);
	}
	
	/* --------------------- DATA MOVEMENT SCRIPTS ----------------------- */
	
	/**
	 * INIT A
	 * Sets A and ACC to 0
	 */
	public void init(String dest)
	{
		myLines.add("\trssb "+ACC);
		myLines.add("\trssb "+dest);
		myLines.add("\trssb "+dest);
		myLines.add("\trssb "+dest+"\t\t;"+dest+" = 0");
	}
	
	/**
	 * MOV A, B
	 * if A = B, replace with NOP
	 */
	public void mov(String dest, String source)
	{
		init(dest);
		myLines.add("\trssb "+source);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+dest+"\t\t;"+dest+" = "+source);
		myLines.add("\trssb "+ACC);
		//myLines.add("\trssb "+ACC);
	}
	
	/**
	 * MOVN A, B
	 * A cannot equal B
	 */
	public void movN(String dest, String source)
	{
		init(dest);
		myLines.add("\trssb "+source);
		myLines.add("\trssb "+dest);
		myLines.add("\trssb "+dest+"\t\t;"+dest+" = -"+source);
		//myLines.add("\trssb "+ACC);
	}
	
	/**
	 * NEG A
	 */
	public void neg(String dest)
	{
		myLines.add("\t\t;NEG "+dest);
		movN(TEMP, dest);
		mov(dest, TEMP);
	}
	
	/**
	 * SWAP A, B
	 * if A = B, replace with NOP
	 */
	public void swap(String op1, String op2)
	{
		myLines.add("\t\t;SWAP "+op1+", "+op2);
		mov(TEMP, op1);
		mov(op1, op2);
		mov(op2, TEMP);
	}
	
	/**
	 * LOAD A, [B]
	 * Loads the data from the address stored in B into A
	 */
	public void loadIndirect(String dest, String source)
	{
		myLines.add("\t\t;LOAD "+dest+", ["+source+"]");
		
		//move source into _load_
		myLines.add("\trssb "+ACC);
		myLines.add("\trssb _load"+loadCount+"_");
		myLines.add("\trssb _load"+loadCount+"_");
		myLines.add("\trssb "+source);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb _load"+loadCount+"_");
		//init dest
		myLines.add("\trssb "+ACC);
		myLines.add("\trssb "+dest);
		myLines.add("\trssb "+dest);
		myLines.add("\trssb "+dest);
		//move data into dest
		myLines.add("_load"+loadCount+"_:");
		myLines.add("\trssb 0");
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+dest);
		myLines.add("\trssb "+ACC);
		//myLines.add("\trssb "+ACC);
		
		loadCount++;
	}
	
	/**
	 * LOAD A, [B,C]
	 * Loads the data from the address stored in B+C into A
	 * assumes that B is positive and B+C is positive
	 */
	public void loadOffset(String dest, String base, String offset)
	{
		myLines.add("\t\t;LOAD "+dest+", ["+base+", "+offset+"]");
		
		//move base into _load_
		myLines.add("\trssb "+ACC);
		myLines.add("\trssb _load"+loadCount+"_");
		myLines.add("\trssb _load"+loadCount+"_");
		myLines.add("\trssb "+base);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb _load"+loadCount+"_");
		//add offset
		myLines.add("\trssb "+ACC);
		myLines.add("\trssb "+offset);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb _load"+loadCount+"_");
		//init dest
		myLines.add("\trssb "+ACC);
		myLines.add("\trssb "+dest);
		myLines.add("\trssb "+dest);
		myLines.add("\trssb "+dest);
		//move data into dest
		myLines.add("_load"+loadCount+"_:");
		myLines.add("\trssb 0");
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+dest);
		myLines.add("\trssb "+ACC);
		//myLines.add("\trssb "+ACC);
		
		loadCount++;
	}
	
	/**
	 * STR A, [B]
	 * Stores the data from A into the address stored in B
	 */
	public void storeIndirect(String source, String dest)
	{
		myLines.add("\t\t;STR "+source+", ["+dest+"]");
		
		//move base into _store_
		myLines.add("\trssb "+ACC);
		myLines.add("\trssb _storeA"+storeCount+"_");
		myLines.add("\trssb _storeA"+storeCount+"_");
		myLines.add("\trssb _storeB"+storeCount+"_");
		myLines.add("\trssb _storeB"+storeCount+"_");
		myLines.add("\trssb _storeC"+storeCount+"_");
		myLines.add("\trssb _storeC"+storeCount+"_");
		myLines.add("\trssb _storeD"+storeCount+"_");
		myLines.add("\trssb _storeD"+storeCount+"_");
		myLines.add("\trssb "+dest);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb _storeA"+storeCount+"_");
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb _storeB"+storeCount+"_");
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb _storeC"+storeCount+"_");
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb _storeD"+storeCount+"_");
		//init address
		myLines.add("\trssb "+ACC);
		myLines.add("_storeA"+storeCount+"_:");
		myLines.add("\trssb 0");
		myLines.add("_storeB"+storeCount+"_:");
		myLines.add("\trssb 0");
		myLines.add("_storeC"+storeCount+"_:");
		myLines.add("\trssb 0");
		//move data into dest
		myLines.add("\trssb "+source);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+ZERO);
		myLines.add("_storeD"+storeCount+"_:");
		myLines.add("\trssb 0");
		myLines.add("\trssb "+ACC);
		//myLines.add("\trssb "+ACC);
		
		storeCount++;
	}
	
	/**
	 * STR A, [B,C]
	 * Stores the data from A into the address stored in B+C
	 */
	public void storeOffset(String source, String base, String offset)
	{
		myLines.add("\t\t;STR "+source+", ["+base+", "+offset+"]");
		
		//move base into _store_
		myLines.add("\trssb "+ACC);
		myLines.add("\trssb _storeA"+storeCount+"_");
		myLines.add("\trssb _storeA"+storeCount+"_");
		myLines.add("\trssb _storeB"+storeCount+"_");
		myLines.add("\trssb _storeB"+storeCount+"_");
		myLines.add("\trssb _storeC"+storeCount+"_");
		myLines.add("\trssb _storeC"+storeCount+"_");
		myLines.add("\trssb _storeD"+storeCount+"_");
		myLines.add("\trssb _storeD"+storeCount+"_");
		myLines.add("\trssb "+base);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb _storeA"+storeCount+"_");
		//add offset
		myLines.add("\trssb "+ACC);
		myLines.add("\trssb "+offset);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb _storeA"+storeCount+"_");
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb _storeB"+storeCount+"_");
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb _storeC"+storeCount+"_");
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb _storeD"+storeCount+"_");
		//init address
		myLines.add("\trssb "+ACC);
		myLines.add("_storeA"+storeCount+"_:");
		myLines.add("\trssb 0");
		myLines.add("_storeB"+storeCount+"_:");
		myLines.add("\trssb 0");
		myLines.add("_storeC"+storeCount+"_:");
		myLines.add("\trssb 0");
		//move data into dest
		myLines.add("\trssb "+source);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+ZERO);
		myLines.add("_storeD"+storeCount+"_:");
		myLines.add("\trssb 0");
		myLines.add("\trssb "+ACC);
		//myLines.add("\trssb "+ACC);
		
		storeCount++;
	}
	
	/**
	 * PUSH A
	 * Stores the data from A on the stack
	 */
	public void push(String source)
	{
		storeIndirect(source,SP);
		myLines.add("\trssb "+ACC);
		myLines.add("\trssb =-1");
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+SP+"\t\t;SP++");
	}
	
	/**
	 * POP A
	 * Stores the data from the top of the stack in A
	 */
	public void pop(String dest)
	{
		myLines.add("\trssb "+ACC);
		myLines.add("\trssb =1");
		myLines.add("\trssb "+SP+"\t\t;SP--");
		loadIndirect(dest, SP);
	}
	
	/* --------------------- ARITHMETIC SCRIPTS -------------------------- */
	
	/**
	 * ADD A, B, C
	 * Stores the result B+C in A
	 */
	public void add3(String dest, String op1, String op2)
	{
		mov(dest, op1);
		add2(dest, op2);
	}
	
	/**
	 * ADD A, B
	 * Stores the result A+B in A
	 */
	public void add2(String dest, String op)
	{
		myLines.add("\trssb "+ACC);
		myLines.add("\trssb "+op);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+dest+"\t\t;"+dest+" = "+dest+" + "+op);
		myLines.add("\trssb "+ACC);
		//myLines.add("\trssb "+ACC);
	}
	
	/**
	 * SUB A, B, C
	 * Stores the result B-C in A
	 */
	public void sub3(String dest, String op1, String op2)
	{
		movN(dest, op2);
		add2(dest, op1);
	}
	
	/**
	 * SUB A, B
	 * Stores the result A-B in A
	 * you would think this script would be really short, but the skipping
	 * ruins it, so some shorter scripts are provided by SUBP and SUBN
	 */
	public void sub2(String dest, String op)
	{
		mov(TEMP, dest);
		sub3(dest, TEMP, op);
	}
	
	/**
	 * SUBP A, B
	 * Stores the result A-B in A
	 * requires that B is positive or zero
	 * 		if B is negative, NOP
	 */
	public void subP(String dest, String op)
	{
		myLines.add("\trssb "+ACC);
		myLines.add("\trssb "+op+"\t\t;must be non-negative");
		myLines.add("\trssb "+dest+"\t\t;"+dest+" = "+dest+" - "+op);
		myLines.add("\trssb "+ACC);
		//myLines.add("\trssb "+ACC);
	}
	
	/**
	 * SUBN A, B
	 * Stores the result A-B in A
	 * requires that B is negative
	 * 		if B is positive or zero, NOP
	 */
	public void subN(String dest, String op)
	{
		myLines.add("\trssb "+ACC);
		myLines.add("\trssb "+op+"\t\t;must be negative");
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+dest+"\t\t;"+dest+" = "+dest+" - "+op);
		myLines.add("\trssb "+ACC);
		//myLines.add("\trssb "+ACC);
	}
	
	/* --------------------- FLOW CONTROL SCRIPTS ------------------------ */
	
	/**
	 * IFLT A, B
	 * continues execution until ELSE if A < B
	 * jumps to ELSE otherwise
	 */
	public void ifLess(String op1, String op2)
	{
		myLines.add("\t\t;IFLT "+op1+", "+op2);
		
		movN(TEMP, op2);
		myLines.add("\trssb "+ACC);
		myLines.add("\trssb "+op1);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+TEMP);
		myLines.add("\trssb "+ACC);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb _else"+ifCount+"_");
		myLines.add("\trssb "+ACC);
		myLines.add("\trssb "+IP);
		myLines.add("_if"+ifCount+"_:");
		
		lastIf.push(ifCount);
		ifCount++;
	}
	
	/**
	 * IFGT A, B
	 * continues execution until ELSE if A > B
	 * jumps to ELSE otherwise
	 */
	public void ifGreater(String op1, String op2)
	{
		myLines.add("\t\t;IFGT "+op1+", "+op2);
		
		movN(TEMP, op2);
		myLines.add("\trssb "+ACC);
		myLines.add("\trssb "+op1);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+TEMP);
		myLines.add("\trssb "+ZERO);	//only line added
		myLines.add("\trssb "+ACC);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb _else"+ifCount+"_");
		myLines.add("\trssb "+ACC);
		myLines.add("\trssb "+IP);
		myLines.add("_if"+ifCount+"_:");
		
		lastIf.push(ifCount);
		ifCount++;
	}
	
	/**
	 * ELSE
	 * separates conditional blocks
	 */
	public void ifElse()
	{
		int ifNum = lastIf.peek();
		
		branch("_end"+ifNum+"_");
		myLines.add("_else"+ifNum+"_:");
		myLines.add("\trssb (_if"+ifNum+"_-_else"+ifNum+"_-1)");
		myLines.add("\t\t;ELSE");
	}
	
	/**
	 * END
	 * ends a conditional block
	 */
	public void ifEnd()
	{
		myLines.add("\t\t;END");
		myLines.add("_end"+lastIf.pop()+"_:");
	}
	
	/**
	 * B label
	 * Jumps a number of steps. distance to labels must be
	 * pre-computed
	 */
	public void branch(String label)
	{
		myLines.add("\t\t;B "+label);
		myLines.add("\trssb "+ACC);
		myLines.add("\trssb _branch"+branchCount+"_");
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+IP);
		myLines.add("_branch"+branchCount+"_:");
		myLines.add("\trssb ("+label+"-_branch"+branchCount+"_)");
		
		branchCount++;
	}
	
	/**
	 * BL label
	 * Jumps a number of steps. distance to labels must be
	 * pre-computed
	 * Updates the Link Register
	 */
	public void branchLink(String label)
	{
		myLines.add("\t\t;BL "+label);
		
		myLines.add("\trssb "+ACC);
		myLines.add("\trssb "+LR);
		myLines.add("\trssb "+LR);
		myLines.add("\trssb "+IP);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+LR);
		myLines.add("\trssb "+ACC);
		myLines.add("\trssb =-14");
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+LR);
		branch(label);
		
		//Possible Optimization? Untested
		//Mov LR _linkN_
		//B label
		//_linkN_:
	}
	
	/**
	 * BX A
	 * Jumps to the address stored in A
	 */
	public void branchDirect(String op)
	{
		myLines.add("\t\t;BX "+op);
		
		mov(TEMP,op);
		myLines.add("\trssb "+ACC);
		myLines.add("\trssb "+IP);
		myLines.add("\trssb "+TEMP);
		myLines.add("\trssb "+ACC);
		myLines.add("\trssb "+ACC);
		myLines.add("\trssb =9");
		myLines.add("\trssb "+TEMP);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+IP);
	}
	
	/**
	 * BXL A
	 * Jumps to the address stored in A
	 * Updates the Link Register
	 */
	public void branchDirectLink(String op)
	{
		myLines.add("\t\t;BXL "+op);
		
		mov(TEMP, op);
		
		myLines.add("\trssb "+ACC);
		myLines.add("\trssb "+LR);
		myLines.add("\trssb "+LR);
		myLines.add("\trssb "+IP);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+LR);
		myLines.add("\trssb "+ACC);
		myLines.add("\trssb =-18");
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+LR);
		
		myLines.add("\trssb "+ACC);
		myLines.add("\trssb "+IP);
		myLines.add("\trssb "+TEMP);
		myLines.add("\trssb "+ACC);
		myLines.add("\trssb "+ACC);
		myLines.add("\trssb =9");
		myLines.add("\trssb "+TEMP);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+ZERO);
		myLines.add("\trssb "+IP);
	}
	
	/**
	 * HALT
	 * Stops execution
	 */
	public void halt()
	{
		myLines.add("\t\t;HALT");
		myLines.add("\trssb "+ACC);
		myLines.add("\trssb "+IP);
		myLines.add("\trssb "+IP);
	}
}
