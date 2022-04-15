package de.code2be.pdfsplit.ui.swing.actions;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;

import de.code2be.pdfsplit.Config;
import de.code2be.pdfsplit.ui.swing.FileExtensionFilter;
import de.code2be.pdfsplit.ui.swing.PDFSplitFrame;

/**
 * The UI action that is responsible for opening a PDF file for splitting.
 * 
 * @author Michael Weiss
 *
 */
public class OpenFileAction extends BasicAction
{

    private static final long serialVersionUID = 8386886689858092404L;

    public static final String ACTION_NAME = "OPEN";

    private final PDFSplitFrame mFrame;

    public OpenFileAction(PDFSplitFrame aFrame)
    {
        mFrame = aFrame;
        setCommandKey(ACTION_NAME);
    }


    /**
     * 
     * @return the directory that should be initially selected on open.
     */
    protected File getCurrentDirectory()
    {
        String dir = mFrame.getConfig()
                .getConfigValS(Config.PROP_DIRECTORY_OPEN, null);
        if (dir != null)
        {
            return new File(dir);
        }
        return null;
    }


    /**
     * Ask the user which file to be opened.
     * 
     * @return the file that is to be opened.
     */
    protected File getFile2Open()
    {
        JFileChooser chooser = new JFileChooser();
        chooser.addChoosableFileFilter(new FileExtensionFilter(".pdf",
                getMessageForSubKey("pdfFilter")));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        File curDir = getCurrentDirectory();
        if (curDir != null)
        {
            chooser.setCurrentDirectory(curDir);
        }

        if (chooser.showOpenDialog(mFrame) != JFileChooser.APPROVE_OPTION)
        {
            return null;
        }

        File f = chooser.getSelectedFile();
        if (f == null)
        {
            return null;
        }
        File dir = f.getParentFile();
        mFrame.getConfig().put(Config.PROP_DIRECTORY_OPEN,
                dir.getAbsolutePath());
        return f;
    }


    @Override
    public void actionPerformed(ActionEvent aE)
    {
        if (mFrame.isWorking())
        {
            mFrame.setStatusText(getMessageForSubKey("errorAlreadyWorking"));
            return;
        }

        File f = getFile2Open();
        if (f == null)
        {
            return;
        }
        // we call open in a separate daemon thread. This ensures non blocking
        // UI for big documents.
        Thread t = new Thread(() -> {
            mFrame.openPDFFile(f);
        });
        t.setDaemon(true);
        t.start();

    }

}
