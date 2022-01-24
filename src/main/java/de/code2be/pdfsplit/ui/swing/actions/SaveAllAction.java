package de.code2be.pdfsplit.ui.swing.actions;

import java.awt.event.ActionEvent;
import java.util.List;

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


    protected void performSaveAll()
    {
        int i = 0;
        List<PDFDocumentPanel> panels = mFrame.getDocumentPanels();
        int count = panels.size();
        for (PDFDocumentPanel pnl : mFrame.getDocumentPanels())
        {
            i++;
            mFrame.setStatusText(getMessageForSubKey("msgWillSave", i, count,
                    pnl.getName()));
            pnl.save();
        }

        mFrame.setStatusText(getMessageForSubKey("msgSaved"));

    }


    @Override
    public void actionPerformed(ActionEvent aE)
    {
        Thread t = new Thread(() -> performSaveAll());
        t.start();
    }

}
