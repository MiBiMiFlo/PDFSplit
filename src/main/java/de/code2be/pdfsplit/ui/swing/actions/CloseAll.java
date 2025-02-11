package de.code2be.pdfsplit.ui.swing.actions;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

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
        int unsaved = mFrame.getUnsavedCount();
        if (unsaved > 0)
        {
            String msg = getMessageForSubKey("confirm.closeUnsaved.message",
                    unsaved);
            String title = getMessageForSubKey("confirm.closeUnsaved.title");
            int res = JOptionPane.showConfirmDialog(mFrame, msg, title,
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (res != JOptionPane.YES_OPTION)
            {
                // do not close
                return;
            }
        }
        mFrame.closeAllTabs();
    }

}
