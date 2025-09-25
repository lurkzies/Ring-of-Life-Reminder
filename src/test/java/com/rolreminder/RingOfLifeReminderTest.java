package com.rolreminder;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class RingOfLifeReminderTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(RingOfLifeReminderPlugin.class);
		RuneLite.main(args);
	}
}