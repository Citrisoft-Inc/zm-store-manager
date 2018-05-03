package com.synacor.util.cli;

import java.util.Arrays;
import java.util.HashMap;

// Holds a set of commands
public class CommandSet
	extends Command
{
	public HashMap<String,Command> commands;

	public CommandSet(String name)
	{
		//super(name);
		this.name = name;
		commands = new HashMap<>();
	}

	public CommandSet add(Command command)
	{
		commands.put(command.name, command);

		return this;
	}

	public void execute(String[] args)
	{
		String commandName = null;
		String[] commandArgs = {};


		if (args.length > 0) commandName = args[0];

		if (args.length > 1) commandArgs = Arrays.copyOfRange(args, 1, args.length);

		Command command = commands.get(commandName);

		if (command != null)
		{
			command.execute(commandArgs);
		}
		else
		{
			help();
		}
	}
}
