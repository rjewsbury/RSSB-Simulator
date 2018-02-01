package com.jewsbury.ryley.cpsc231.project.simulator.view;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.*;

import com.jewsbury.ryley.cpsc231.project.simulator.SimulatorMain;

public class CodePanel extends JPanel
{
	//display information
		public static final int PANEL_WIDTH = 600;
		public static final int ADDRESSES_PER_LINE = 15;
		
		private JList<String> myCodeList;
		private DefaultListModel<String> myListModel;
		
		private ArrayList<Integer> myBreaks;
		
		public CodePanel()
		{
			myBreaks = new ArrayList<>();
			
			//displays components in a vertical line
			BoxLayout layout = new BoxLayout(this,BoxLayout.Y_AXIS);
			setLayout(layout);
			
			myListModel = new DefaultListModel<String>();
			
			myCodeList = new JList<>(myListModel);
			myCodeList.setFont(SimulatorMain.GLOBAL_FONT);
			
			JScrollPane scrollPane = new JScrollPane(myCodeList);
			scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			
			//add the components
			add(scrollPane);
			
			//sets the preferred width
			setPreferredSize(new Dimension(PANEL_WIDTH,Integer.MAX_VALUE));
		}
		
		/**
		 * Parses a specially formatted arraylist with code from the assembler
		 */
		public void readRSSB(ArrayList<String> rssbCode)
		{
			myBreaks = new ArrayList<>();
			myListModel.clear();
			
			for(int i=0;i<rssbCode.size();i++)
			{
				myListModel.addElement(rssbCode.get(i));
			}
		}
		
		public void toggleBreak()
		{
			int index = myCodeList.getSelectedIndex();
			if(index == -1)
				return;
			
			String line = myListModel.remove(index);
			
			if(line.split(">").length>1)
			{
				if(line.startsWith("STOP"))
				{
					line = "     "+line.substring(5);
					myListModel.add(index, line);
					myBreaks.remove((Object)Integer.parseUnsignedInt(line.trim().substring(0, 4),16));
				}
				else
				{
					myListModel.add(index, "STOP "+line.substring(5));
					myBreaks.add(Integer.parseUnsignedInt(line.trim().substring(0, 4),16));
				}
			}
			else
				myListModel.add(index, line);
			
			myCodeList.setSelectedIndex(index);
		}
		
		public ArrayList<Integer> getBreaks()
		{
			//System.out.println(myBreaks);
			return myBreaks;
		}
		
		public void setSelectedLine(int address)
		{
			String line;
			int index;
			for(int i=0;i<myListModel.size();i++)
			{
				line = myListModel.getElementAt(i);
				index = line.indexOf('>');
				if(index >= 4 && address == Integer.parseUnsignedInt(line.substring(index-4, index),16))
				{
					myCodeList.setSelectedIndex(i);
					myCodeList.ensureIndexIsVisible(i);
					return;
				}
			}
			myCodeList.setSelectedIndex(-1);
		}
}
