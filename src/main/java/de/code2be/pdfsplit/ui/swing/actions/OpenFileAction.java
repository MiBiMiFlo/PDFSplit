package de.code2be.pdfsplit.ui.swing.actions;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;

import de.code2be.pdfsplit.ui.swing.FileExtensionFilter;
import de.code2be.pdfsplit.ui.swing.PDFSplitFrame;

public class OpenFileAction extends BasicAction
{

    private static final long serialVersionUID = 8386886689858092404L;

    private final PDFSplitFrame mFrame;

    private File mDirectory;

    public OpenFileAction(PDFSplitFrame aFrame)
    {
        mFrame = aFrame;
        setCommandKey("OPEN");
    }


    protected File getCurrentDirectory()
    {
        if (mDirectory == null)
        {
            String dir = mFrame.getConfig()
                    .getProperty(PDFSplitFrame.PROP_DIRECTORY_OPEN);
            if (dir != null)
            {
                mDirectory = new File(dir);
            }
        }
        return mDirectory;
    }


    @Override
    public void actionPerformed(ActionEvent aE)
    {
        if (mFrame.isWorking())
        {
            mFrame.setStatusText(getMessageForSubKey("errorAlreadyWorking"));
            return;
        }

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
            return;
        }

        File f = chooser.getSelectedFile();
        if (f == null)
        {
            return;
        }
        File dir = f.getParentFile();
        mDirectory = dir;

        mFrame.openPDFFile(f);
    }

}
