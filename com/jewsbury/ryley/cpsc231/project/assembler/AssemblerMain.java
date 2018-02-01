package com.jewsbury.ryley.cpsc231.project.assembler;

import java.io.IOException;

public class AssemblerMain
{
	public static void main(String[] args)
	{	
		if(args.length == 1)
		{
			Assembler assembler = new Assembler();
			
			try {
				assembler.assemble(args[0]);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else
			System.err.println("The assembler requires a .rssb filename passed as an argument.");
	}
}
