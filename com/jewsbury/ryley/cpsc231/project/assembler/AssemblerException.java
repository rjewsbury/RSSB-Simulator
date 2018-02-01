package com.jewsbury.ryley.cpsc231.project.assembler;

@SuppressWarnings("serial")
public class AssemblerException extends RuntimeException
{
	public AssemblerException(String message)
	{
		super(message);
	}
	
	public AssemblerException(String message, Throwable throwable)
	{
		super(message, throwable);
	}
}
