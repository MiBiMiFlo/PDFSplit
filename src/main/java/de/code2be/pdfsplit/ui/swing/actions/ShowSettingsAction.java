package de.code2be.pdfsplit.ui.swing.actions;

import java.awt.event.ActionEvent;
import java.lang.System.Logger.Level;

import javax.swing.JOptionPane;

import de.code2be.pdfsplit.Config;
import de.code2be.pdfsplit.ui.swing.PDFSplitFrame;
import de.code2be.pdfsplit.ui.swing.PDFSplitSettingsPanel;

public class ShowSettingsAction extends BasicAction
{

    private static final long serialVersionUID = -5786934215465751672L;

    public static final String ACTION_NAME = "SETTINGS";

    private final PDFSplitFrame mFrame;

    public ShowSettingsAction(PDFSplitFrame aFrame)
    {
        mFrame = aFrame;
        setCommandKey(ACTION_NAME);
    }


    @Override
    public void actionPerformed(ActionEvent aE)
    {
        PDFSplitSettingsPanel settings = new PDFSplitSettingsPanel();
        Config cfg = mFrame.getConfig();
        settings.initialize(cfg);

        int res = JOptionPane.showConfirmDialog(mFrame, settings, "Settings",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION)
        {
            settings.saveTo(cfg);
            mFrame.setConfig(cfg);
            try
            {
                Config.saveConfig(cfg);
            }
            catch (Exception ex)
            {
                LOGGER.log(Level.ERROR, ex.getMessage(), ex);
                mFrame.showError(ex.getMessage(),
                        "ERROR - Can not write config", ex);
            }

        }
    }

}
