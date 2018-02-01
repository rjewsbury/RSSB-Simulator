package com.jewsbury.ryley.cpsc231.project.simulator;

import java.io.File;
import java.io.IOException;

import com.jewsbury.ryley.cpsc231.project.simulator.model.Memory;
import com.jewsbury.ryley.cpsc231.project.simulator.model.Simulator;

/**
 * Displays some info from memory at every step of execution.
 * only used during development for testing
 *
 * @author Ryley Jewsbury
 */
public class InDevMain
{	
	public static void main(String[] args)
	{
		if(args.length == 1)
		{
			Simulator simulator = new Simulator(new Memory());
			try{
				File file = new File(args[0]);
				simulator.loadFromFile(file);
			}catch (IOException e){
				e.printStackTrace();
				return;
			}
			simulator.run();
		}
		else
			System.err.println("The simulator requires a .flash file passed as an argument.");
	}
}
