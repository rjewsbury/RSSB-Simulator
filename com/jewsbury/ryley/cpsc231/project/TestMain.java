package com.jewsbury.ryley.cpsc231.project;

import com.jewsbury.ryley.cpsc231.project.assembler.AssemblerMain;
import com.jewsbury.ryley.cpsc231.project.compiler.CompilerMain;
import com.jewsbury.ryley.cpsc231.project.simulator.InDevMain;

/**
 * A quick entry point to test the compiler, assembler,
 * and Simulator/Memory code at the same time.
 * 
 * To run the actual program, use simulator.SimulatorMain
 *
 * @author Ryley Jewsbury
 */
public class TestMain
{
	public static void main(String[] args)
	{
		//compile the file
		CompilerMain.main(new String[]{"fibb.src"});
		//assemble the file
		AssemblerMain.main(new String[]{"fibb.rssb"});
		//simulate the file
		InDevMain.main(new String[]{"fibb.flash"});
	}
}
