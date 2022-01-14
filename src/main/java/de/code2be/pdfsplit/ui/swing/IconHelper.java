package de.code2be.pdfsplit.ui.swing;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

public class IconHelper
{

    private static final Logger LOGGER = Logger
            .getLogger(IconHelper.class.getName());

    public static ImageIcon getIconForName(String aName)
    {
        try
        {
            URL iconURL = IconHelper.class.getResource(aName);

            if (iconURL == null)
            {
                LOGGER.log(Level.SEVERE, "No icon found for name {0}", aName);
                return null;
            }

            return new ImageIcon(iconURL, aName);
        }
        catch (Exception ex)
        {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
        return null;
    }
}
