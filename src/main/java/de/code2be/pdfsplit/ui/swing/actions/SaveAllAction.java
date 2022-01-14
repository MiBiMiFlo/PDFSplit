package de.code2be.pdfsplit.ui.swing.actions;

import java.awt.event.ActionEvent;

import de.code2be.pdfsplit.ui.swing.PDFDocumentPanel;
import de.code2be.pdfsplit.ui.swing.PDFSplitFrame;

public class SaveAllAction extends BasicAction
{

    private static final long serialVersionUID = 2739602670097652296L;

    private final PDFSplitFrame mFrame;

    public SaveAllAction(PDFSplitFrame aFrame)
    {
        mFrame = aFrame;
        setCommandKey("SAVE_ALL");
    }


    @Override
    public void actionPerformed(ActionEvent aE)
    {
        for (PDFDocumentPanel pnl : mFrame.getDocumentPanels())
        {
            pnl.save();
        }
    }

}
