package de.code2be.pdfsplit.ui.swing.actions;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;

import de.code2be.pdfsplit.ui.swing.PDFDocumentPanel;
import de.code2be.pdfsplit.ui.swing.PDFSplitFrame;

public class RenameAction extends BasicAction
{

    private static final long serialVersionUID = 1343590381079832914L;

    private final PDFSplitFrame mFrame;

    public RenameAction(PDFSplitFrame aFrame)
    {
        mFrame = aFrame;
        setCommandKey("RENAME");
    }


    @Override
    public void actionPerformed(ActionEvent aE)
    {
        PDFDocumentPanel panel = mFrame.getSelectedDocumentPanel();

        File f = panel.getFile();

        JFileChooser chooser = new JFileChooser(f.getParentFile());
        chooser.setSelectedFile(f);
        int res = chooser.showSaveDialog(mFrame);

        if (res == JFileChooser.APPROVE_OPTION)
        {
            f = chooser.getSelectedFile();
            panel.setFile(f);
        }

    }
}
