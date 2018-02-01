package com.jewsbury.ryley.cpsc231.project.simulator.control;

import java.io.File;
import javax.swing.filechooser.FileFilter;

public class RSSBFileFilter extends FileFilter
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
				accepts = splitName[splitName.length - 1].equals("rssb");
		}

		return accepts;
	}

	@Override
	public String getDescription()
	{
		// TODO Auto-generated method stub
		return "RSSB Source Code File (.rssb)";
	}
}
