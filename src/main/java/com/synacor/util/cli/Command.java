package com.synacor.util.cli;

public abstract class Command
{
	public String name;
	public String[] args;

	public Command()
	{
	}

	public Command(String name)
	{
		this.name = name;
	}

	public void help()
	{
		System.out.println("HELP!");
	}

	public abstract void execute(String[] args);
}
