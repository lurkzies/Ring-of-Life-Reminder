package com.rolreminder;

import javax.inject.Inject;
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.util.ImageUtil;

public class RingOfLifeReminderOverlay extends Overlay
{
    private static final float OPACITY = 0.85f; // fixed
    private static final int OFFSET_X = 10;     // fixed
    private static final int OFFSET_Y = 10;     // fixed

    // Real percentage bounds
    private static final int MIN_SCALE_PCT = 1;
    private static final int MAX_SCALE_PCT = 100;

    private final RingOfLifeReminderPlugin plugin;
    private final RingOfLifeReminderConfig config;
    private final Client client;

    private BufferedImage icon;
    private boolean loggedMissingOnce = false;

    @Inject
    public RingOfLifeReminderOverlay(RingOfLifeReminderPlugin plugin,
                                     RingOfLifeReminderConfig config,
                                     Client client)
    {
        this.plugin = plugin;
        this.config = config;
        this.client = client;

        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(OverlayPriority.MED);
        setPosition(OverlayPosition.DYNAMIC);

        // PNG expected at jar root: /rol_warning.png
        this.icon = tryLoad("/rol_warning.png");
        if (this.icon == null)
        {
            // Fallback in case it's packaged under the classpath
            this.icon = tryLoad("/com/rolreminder/rol_warning.png");
        }
    }

    private static BufferedImage tryLoad(String resourcePath)
    {
        try
        {
            return ImageUtil.loadImageResource(RingOfLifeReminderOverlay.class, resourcePath);
        }
        catch (IllegalArgumentException ex)
        {
            return null;
        }
    }

    @Override
    public Dimension render(Graphics2D g)
    {
        if (!plugin.isMissingRing() || icon == null)
        {
            if (icon == null && !loggedMissingOnce)
            {
                System.err.println("[RingOfLifeReminder] Missing overlay image. Expected /rol_warning.png at jar root.");
                loggedMissingOnce = true;
            }
            return null;
        }

        int scalePct = config.overlayScalePercent();
        // Clamp in case an old profile stored an out-of-range value
        if (scalePct < MIN_SCALE_PCT || scalePct > MAX_SCALE_PCT)
        {
            scalePct = Math.max(MIN_SCALE_PCT, Math.min(MAX_SCALE_PCT, scalePct));
        }
        double scale = scalePct / 100.0;

        int imgW = (int) Math.round(icon.getWidth() * scale);
        int imgH = (int) Math.round(icon.getHeight() * scale);

        int x = OFFSET_X;
        int y = OFFSET_Y;

        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, OPACITY));
        g.drawImage(icon, x, y, imgW, imgH, null);
        g.setComposite(old);

        return new Dimension(imgW, imgH);
    }
}