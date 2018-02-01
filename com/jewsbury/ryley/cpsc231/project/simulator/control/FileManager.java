package com.jewsbury.ryley.cpsc231.project.simulator.control;

import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class FileManager
{
	//remembers the last location for saving, loading, and importing
	private JFileChooser rssbChooser, sourceChooser;
	//the Course filter used to find only .unbc files
	private RSSBFileFilter rssbFilter;
	//the course filter used to find only .csv files 
	private SourceFileFilter sourceFilter;
	//the most recent save location
	private File saveLocation;
	
	/**
	 * Creates a new default file manager
	 */
	public FileManager()
	{
		rssbFilter = new RSSBFileFilter();
		sourceFilter = new SourceFileFilter();
		
		rssbChooser = new JFileChooser();
		//loads from .unbc files
		rssbChooser.addChoosableFileFilter(rssbFilter);
		//prevents non .unbc files from being used
		rssbChooser.setAcceptAllFileFilterUsed(false);
		
		sourceChooser = new JFileChooser();
		//loads from csv's
		sourceChooser.addChoosableFileFilter(sourceFilter);
		//prevents non csv files from being used
		sourceChooser.setAcceptAllFileFilterUsed(false);
		
		saveLocation = null;
	}
	
	/**
	 * Loads a .rssb file
	 * @param loader the reader for the file
	 */
	public void LoadRSSBFile(Loadable loader)
	{
		int rssbReturn = rssbChooser.showOpenDialog(null);
		if (rssbReturn == JFileChooser.APPROVE_OPTION &&
				rssbChooser.getSelectedFile().exists() &&
				rssbFilter.accept(rssbChooser.getSelectedFile()))
		{
			saveLocation = rssbChooser.getSelectedFile();
			//return a loadable file
			
			//load the file. (hopefully ignoring the error like this is finel
			try{
				loader.loadFromFile(saveLocation);
			}catch(FileNotFoundException e)
			{
				//we already verified that the file exists, so we *shouldn't* ever get here
				JOptionPane.showMessageDialog(null, "You tried to load a file that doesn't exist!\nand it somehow got past my checks!\nWow! That's really impressive!");
			}
		}
		else if(rssbReturn == JFileChooser.APPROVE_OPTION)
		{
			JOptionPane.showMessageDialog(null, "That file can not be loaded!"+
								"\nPlease check that the following file was the one you meant to load:\n\n"+
								rssbChooser.getSelectedFile().getName()+
								"\n\nPlease ensure the file exists and is a .RSSB file");
		}
	}
	
	/**
	 * loads a csv into a loadable
	 * @param loader the csv reader
	 */
	public void LoadSourceFile(Loadable loader)
	{
		int loadReturn = sourceChooser.showOpenDialog(null);
		if (loadReturn == JFileChooser.APPROVE_OPTION && loader.canLoad(sourceChooser.getSelectedFile()))
		{
			//return a loadable file
			try{
				loader.loadFromFile(sourceChooser.getSelectedFile());
			}catch(FileNotFoundException e)
			{
				//we already verified that the file exists, so we *shouldn't* ever get here
				JOptionPane.showMessageDialog(null, "You tried to load a file that doesn't exist!\nand it somehow got past my checks!\nWow! That's really impressive!");
			}
		}
		else if(loadReturn == JFileChooser.APPROVE_OPTION)
		{
			JOptionPane.showMessageDialog(null, "That file can not be loaded!"+
								"\nPlease check that the following file was the one you meant to load:\n\n"+
								sourceChooser.getSelectedFile().getName()+
								"\n\nPlease ensure the file exists and is a .SRC file");
		}
	}
}
