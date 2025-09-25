package com.rolreminder;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
        name = "Ring of Life Reminder",
        description = "Shows an image overlay when you don’t have a Ring of Life equipped.",
        tags = {"safety","ring","life"}
)
public class RingOfLifeReminderPlugin extends Plugin
{
    private static final int RING_SLOT_INDEX = net.runelite.api.EquipmentInventorySlot.RING.getSlotIdx();

    @Inject private Client client;
    @Inject private RingOfLifeReminderConfig config;
    @Inject private ClientThread clientThread;

    @Inject private OverlayManager overlayManager;
    @Inject private RingOfLifeReminderOverlay overlay;

    private boolean missingRing = false;

    boolean isMissingRing()
    {
        return missingRing;
    }

    @Provides
    RingOfLifeReminderConfig provideConfig(ConfigManager cm)
    {
        return cm.getConfig(RingOfLifeReminderConfig.class);
    }

    @Override
    protected void startUp()
    {
        overlayManager.add(overlay);
        clientThread.invoke(() -> {
            if (client.getGameState() == GameState.LOGGED_IN)
            {
                updateMissingRing();
            }
            else
            {
                missingRing = false;
            }
        });
    }

    @Override
    protected void shutDown()
    {
        overlayManager.remove(overlay);
        missingRing = false;
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged e)
    {
        if (e.getGameState() == GameState.LOGGED_IN)
        {
            updateMissingRing();
        }
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged e)
    {
        if (e.getContainerId() == InventoryID.EQUIPMENT.getId())
        {
            updateMissingRing();
        }
    }

    private void updateMissingRing()
    {
        missingRing = !hasRingOfLifeEquipped();
    }

    private boolean hasRingOfLifeEquipped()
    {
        ItemContainer equip = client.getItemContainer(InventoryID.EQUIPMENT);
        if (equip == null) return false;

        Item[] items = equip.getItems();
        if (items == null || RING_SLOT_INDEX >= items.length) return false;

        Item ring = items[RING_SLOT_INDEX];
        if (ring == null) return false;

        return ring.getId() == ItemID.RING_OF_LIFE;
    }
}