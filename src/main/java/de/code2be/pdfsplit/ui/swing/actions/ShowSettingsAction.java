package de.code2be.pdfsplit.ui.swing.actions;

import java.awt.event.ActionEvent;
import java.util.Properties;

import javax.swing.JOptionPane;

import de.code2be.pdfsplit.ui.swing.PDFSplitFrame;
import de.code2be.pdfsplit.ui.swing.PDFSplitSettingsPanel;

public class ShowSettingsAction extends BasicAction
{

    private static final long serialVersionUID = -5786934215465751672L;

    private final PDFSplitFrame mFrame;

    public ShowSettingsAction(PDFSplitFrame aFrame)
    {
        mFrame = aFrame;
        setCommandKey("SETTINGS");
    }


    @Override
    public void actionPerformed(ActionEvent aE)
    {
        PDFSplitSettingsPanel settings = new PDFSplitSettingsPanel();
        Properties cfg = mFrame.getConfig();
        settings.initialize(cfg);

        int res = JOptionPane.showConfirmDialog(mFrame, settings, "Settings",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION)
        {
            settings.saveTo(cfg);
            mFrame.setConfig(cfg);
            mFrame.saveConfig();
        }
    }

}
