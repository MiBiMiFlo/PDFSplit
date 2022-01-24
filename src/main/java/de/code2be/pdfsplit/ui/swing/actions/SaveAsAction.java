package de.code2be.pdfsplit.ui.swing.actions;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;

import de.code2be.pdfsplit.ui.swing.FileExtensionFilter;
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


    protected void performSaveAs(PDFDocumentPanel aPanel, File aFile)
    {
        mFrame.setStatusText(
                getMessageForSubKey("msgWillSave", aFile.getAbsolutePath()));
        aPanel.saveAs(aFile);
        mFrame.setStatusText(
                getMessageForSubKey("msgSaved", aFile.getAbsolutePath()));
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

        File curFile = panel.getFile();
        JFileChooser chooser = new JFileChooser(curFile.getParentFile());
        chooser.addChoosableFileFilter(new FileExtensionFilter(".pdf",
                getMessageForSubKey("pdfFilter")));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        chooser.setSelectedFile(curFile);
        int res = chooser.showSaveDialog(mFrame);
        if (res == JFileChooser.APPROVE_OPTION)
        {
            File newFile = chooser.getSelectedFile();
            if (newFile != null)
            {
                Thread t = new Thread(() -> performSaveAs(panel, newFile));
                t.start();
                panel.saveAs(newFile);
            }

        }
    }
}
