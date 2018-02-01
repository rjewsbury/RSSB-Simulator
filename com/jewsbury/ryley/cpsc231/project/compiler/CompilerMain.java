package com.jewsbury.ryley.cpsc231.project.compiler;

import java.io.IOException;

/**
 * Takes a command line .src file and compiles it into a .rssb file
 *
 * @author Ryley Jewsbury
 */
public class CompilerMain
{
	public static void main(String[] args)
	{	
		if(args.length == 1)
		{
			RSSBCompiler compiler = new RSSBCompiler();
			
			try {
				compiler.compile(args[0]);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else
			System.err.println("The compiler requires a .src filename passed as an argument.");
	}
}
