package de.code2be.pdfsplit.ui.swing.actions;

import java.awt.event.ActionEvent;

import de.code2be.pdfsplit.ui.swing.PDFDocumentPanel;
import de.code2be.pdfsplit.ui.swing.PDFSplitFrame;

public class SaveAction extends BasicAction
{

    private static final long serialVersionUID = 7791149187731440237L;

    private final PDFSplitFrame mFrame;

    public SaveAction(PDFSplitFrame aFrame)
    {
        mFrame = aFrame;
        setCommandKey("SAVE");
    }


    @Override
    public void actionPerformed(ActionEvent aE)
    {
        PDFDocumentPanel panel = mFrame.getSelectedDocumentPanel();

        if (panel == null)
        {
            mFrame.setStatusText(getMessageForSubKey("errorNoPanel"));
            return;
        }
        panel.save();
    }
}
