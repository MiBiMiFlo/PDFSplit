package de.code2be.pdfsplit.ui.swing.actions;

import java.awt.event.ActionEvent;

import de.code2be.pdfsplit.ui.swing.PDFSplitFrame;

public class CloseAll extends BasicAction
{

    /**
     * 
     */
    private static final long serialVersionUID = 6390046974432516447L;

    public static final String ACTION_NAME = "CLOSE_ALL";

    private final PDFSplitFrame mFrame;

    public CloseAll(PDFSplitFrame aFrame)
    {
        mFrame = aFrame;
        setCommandKey(ACTION_NAME);
    }


    @Override
    public void actionPerformed(ActionEvent aE)
    {
        mFrame.closeAllTabs();
    }

}
