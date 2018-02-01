package com.jewsbury.ryley.cpsc231.project.compiler;

@SuppressWarnings("serial")
public class CompilerException extends RuntimeException
{
	public CompilerException(String message)
	{
		super(message);
	}
	
	public CompilerException(String message, Throwable throwable)
	{
		super(message, throwable);
	}
}
