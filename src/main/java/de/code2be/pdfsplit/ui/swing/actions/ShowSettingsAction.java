package de.code2be.pdfsplit.ui.swing.actions;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import de.code2be.pdfsplit.ui.swing.PDFSplitFrame;

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
        JOptionPane.showMessageDialog(mFrame, "Not Implemented!");

    }

}
