package com.jewsbury.ryley.cpsc231.project.simulator;

import java.awt.Font;

import javax.swing.SwingUtilities;

import com.jewsbury.ryley.cpsc231.project.simulator.view.SimulatorFrame;

/**
 * Creates a new instance of the simulator GUI
 * Starts the GUI on the AWT dispatch thread
 * 
 * @author Ryley
 */
public class SimulatorMain
{
	public static final Font GLOBAL_FONT = new Font(Font.MONOSPACED,Font.PLAIN,12);
	
	public static void main(String[] args)
	{
		//UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
				new SimulatorFrame();
			}
		});
	}
}
