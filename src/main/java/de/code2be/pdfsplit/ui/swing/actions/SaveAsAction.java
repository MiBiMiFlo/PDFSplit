package de.code2be.pdfsplit.ui.swing.actions;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;

import de.code2be.pdfsplit.ui.swing.PDFDocumentPanel;
import de.code2be.pdfsplit.ui.swing.PDFSplitFrame;

public class SaveAsAction extends BasicAction
{

    private static final long serialVersionUID = 7976895178921372492L;

    private final PDFSplitFrame mFrame;

    public SaveAsAction(PDFSplitFrame aFrame)
    {
        mFrame = aFrame;
        setCommandKey("SAVE_AS");
    }


    @Override
    public void actionPerformed(ActionEvent aE)
    {
        PDFDocumentPanel pnl = mFrame.getSelectedDocumentPanel();
        if (pnl != null)
        {
            File curFile = pnl.getFile();
            JFileChooser chooser = new JFileChooser(curFile.getParentFile());
            chooser.setSelectedFile(curFile);
            int res = chooser.showSaveDialog(mFrame);
            if (res == JFileChooser.APPROVE_OPTION)
            {
                File newFile = chooser.getSelectedFile();
                if (newFile != null)
                {
                    pnl.saveAs(newFile);
                }

            }

        }
    }
}
