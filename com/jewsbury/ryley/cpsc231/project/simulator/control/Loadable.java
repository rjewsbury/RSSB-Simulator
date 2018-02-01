package com.jewsbury.ryley.cpsc231.project.simulator.control;

import java.io.File;
import java.io.FileNotFoundException;

public interface Loadable
{
	boolean canLoad(File file);
	void loadFromFile(File file) throws FileNotFoundException;
}
