package com.jewsbury.ryley.cpsc231.project.simulator.control;

import java.io.File;
import javax.swing.filechooser.FileFilter;

public class SourceFileFilter extends FileFilter
{
	public boolean accept(File f)
	{
		boolean accepts = false;
		String[] splitName;
		if (f.isDirectory())
		{
			accepts = true;
		}
		else
		{
			splitName = f.getName().split("\\.");
			if (splitName.length > 1)
				accepts = splitName[splitName.length - 1].equals("src");
		}

		return accepts;
	}

	@Override
	public String getDescription()
	{
		// TODO Auto-generated method stub
		return "Assembly Source File (.src)";
	}
}
