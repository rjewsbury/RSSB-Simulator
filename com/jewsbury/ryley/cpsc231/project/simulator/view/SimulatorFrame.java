package com.jewsbury.ryley.cpsc231.project.simulator.view;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.*;

import com.jewsbury.ryley.cpsc231.project.assembler.*;
import com.jewsbury.ryley.cpsc231.project.compiler.*;
import com.jewsbury.ryley.cpsc231.project.simulator.control.*;
import com.jewsbury.ryley.cpsc231.project.simulator.model.*;

/**
 * The main container for the schedule GUI
 * 
 * handles file i/o
 * allows the SchedulePanel, ControlPanel, and FileManager to communicate
 * 
 * @author Ryley
 * @version 2.0
 */
public class SimulatorFrame extends JFrame implements Loadable
{
	public static final int SPACING = 5;
	
	//variables to possible change the way file i/o is implemented at some point
	//declared final to be used in anonymous menu classes
	private final Loadable loader = this;
	
	//handles all file i/o
	private FileManager myFileManager;
	
	//keeps tracks of panels because this is an interconnected mess
	private CodePanel myCodePanel;
	private MemoryPanel myMemoryPanel;
	
	//keeps track of the simulator to add break points
	private Simulator mySimulator;
	//I need this to highlight the correct line in the code panel
	private Memory myMemory;
	
	//This class is the middle man between the user, the code panel, and the memory panel
	//it's extremely messy and I hope nobody has to read this, or any of this project really.
	public SimulatorFrame()
	{
		myFileManager = new FileManager();
		
		//setting up the frame
		setTitle("URISC Simulator");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1100, 700);
		//setMinimumSize(new Dimension(316,380));
		//resizing things is terrible to deal with
		setResizable(false);
		
		myMemory = new Memory();
		mySimulator = new Simulator(myMemory);
		
		//setting up the general panels
		myCodePanel = new CodePanel();
		myMemoryPanel = new MemoryPanel(myMemory);
		
		add(myCodePanel,BorderLayout.CENTER);
		add(myMemoryPanel,BorderLayout.WEST);
		
		//setting up the menu
		setJMenuBar(createMenuBar());
		
		setVisible(true);
	}
	
	/**
	 * Creates a JMenuBar that allows for file i/o
	 * 
	 * @return a JMenuBar that loads CSVs, and read/writes .unbc files
	 */
	private JMenuBar createMenuBar()
	{
		JMenuBar menu = new JMenuBar();
		JMenu fileMenu = new JMenu("File            ");
		JMenuItem tempItem;
		JButton tempButton;
		
		//loads rssb files
		tempItem = new JMenuItem("Load RSSB...");
		tempItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				myFileManager.LoadRSSBFile(loader);
			}});
		fileMenu.add(tempItem);
		
		//loads flash files
		tempItem = new JMenuItem("Load SOURCE...");
		tempItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				myFileManager.LoadSourceFile(loader);
			}});
		fileMenu.add(tempItem);
		menu.add(fileMenu);
		menu.add(Box.createHorizontalGlue());
		
		//add/removes break points
		tempButton = new JButton("Toggle Break");
		tempButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				myCodePanel.toggleBreak();
				mySimulator.setBreaks(myCodePanel.getBreaks());
			}});
		menu.add(tempButton);
		menu.add(Box.createHorizontalStrut(10));
		
		//steps forward
		tempButton = new JButton("Step");
		tempButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				//System.out.println("Clicked");
				mySimulator.step();
				//System.out.println("Stepped");
				myMemoryPanel.displayMemory();
				myCodePanel.setSelectedLine(myMemory.get(0));
			}});
		menu.add(tempButton);
		
		//run the program
		tempButton = new JButton("Run");
		tempButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				mySimulator.run();
				myMemoryPanel.displayMemory();
				myCodePanel.setSelectedLine(myMemory.get(0));
			}});
		menu.add(tempButton);
		
		return menu;
	}
	
	/**
	 * Checks to see if a file is readable
	 */
	@Override
	public boolean canLoad(File file)
	{
		RSSBFileFilter rssbFilter = new RSSBFileFilter();
		SourceFileFilter sourceFilter = new SourceFileFilter();
		return file.exists() && file.isFile() && (rssbFilter.accept(file) || sourceFilter.accept(file));
	}
	
	/**
	 * loads information from a source file
	 */
	@Override
	public void loadFromFile(File file) throws FileNotFoundException
	{
		RSSBFileFilter rssbFilter = new RSSBFileFilter();
		SourceFileFilter sourceFilter = new SourceFileFilter();
		
		if(rssbFilter.accept(file))
			loadRSSB(file);
		else if(sourceFilter.accept(file))
			loadSource(file);
	}
	
	private void loadRSSB(File file)
	{
		Assembler assembler = new Assembler();
		try{
			assembler.assemble(file,true);
		}catch (IOException e){
			JOptionPane.showMessageDialog(null,
					"The file could not be read:\r\n"+e.getMessage(),
					"Assembler IO Error", JOptionPane.ERROR_MESSAGE);
			return;
		}catch (AssemblerException e){
			JOptionPane.showMessageDialog(null,
					"The file could not be assembled:\r\n"+e.getMessage(),
					"Assemble Time Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		File flashFile = new File(file.getPath().split("\\.")[0]+".flash");
		try{
			mySimulator.loadFromFile(flashFile);
		}catch (IOException e){
			JOptionPane.showMessageDialog(null,
					"The machine code file was not found:\r\n"+e.getMessage(),
					"Flash Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		mySimulator.setBreaks(new ArrayList<>());
		myCodePanel.readRSSB(assembler.getDisplayInfo());
		myMemoryPanel.displayMemory();
	}
	
	private void loadSource(File file)
	{
		RSSBCompiler compiler = new RSSBCompiler();
		try{
			compiler.compile(file);
		}catch (IOException e){
			JOptionPane.showMessageDialog(null,
					"The file could not be read:\r\n"+e.getMessage(),
					"Compiler IO Error", JOptionPane.ERROR_MESSAGE);
			return;
		}catch (CompilerException e){
			JOptionPane.showMessageDialog(null,
					"The file could not be compiled:\r\n"+e.getMessage(),
					"Compile Time Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		File rssbFile = new File(file.getPath().split("\\.")[0]+".rssb");
		loadRSSB(rssbFile);
	}
}
