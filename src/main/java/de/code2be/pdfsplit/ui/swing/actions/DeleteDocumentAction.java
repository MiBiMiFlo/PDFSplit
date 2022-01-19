package de.code2be.pdfsplit.ui.swing.actions;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import de.code2be.pdfsplit.ui.swing.PDFDocumentPanel;
import de.code2be.pdfsplit.ui.swing.PDFSplitFrame;

/**
 * An action class that is used to delete a document from the list of open
 * documents.
 * 
 * @author Michael Weiss
 *
 */
public class DeleteDocumentAction extends BasicAction
{

    private static final long serialVersionUID = 242115467906128133L;

    private final PDFSplitFrame mFrame;

    public DeleteDocumentAction(PDFSplitFrame aFrame)
    {
        mFrame = aFrame;
        setCommandKey("DELETE");
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

        String text = getMessageForSubKey("confirmDeleteText",
                panel.getFile().getName());
        String title = getMessageForSubKey("confirmDeleteTitle");

        int res = JOptionPane.showConfirmDialog(mFrame, text, title,
                JOptionPane.YES_NO_CANCEL_OPTION);
        if (res == JOptionPane.YES_OPTION)
        {
            mFrame.showError("ERROR: Delete Not implemented",
                    "Delete not implemented!", null);
        }
    }

}
