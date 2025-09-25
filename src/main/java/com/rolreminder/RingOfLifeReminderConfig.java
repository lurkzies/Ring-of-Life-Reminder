package com.rolreminder;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("rolreminder")
public interface RingOfLifeReminderConfig extends Config
{
    @Range(min = 1, max = 100)
    @ConfigItem(
            keyName = "overlayScalePercent",
            name = "Scale (%)",
            description = "Set the size of the overlay image."
    )
    default int overlayScalePercent() { return 50; }
}