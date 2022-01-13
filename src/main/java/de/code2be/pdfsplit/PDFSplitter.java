package de.code2be.pdfsplit;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import org.apache.pdfbox.pdmodel.PDDocument;

public class PDFSplitter
{

    private static final Logger LOGGER = Logger
            .getLogger(PDFSplitter.class.getName());

    public static final String DEFAULT_SEP = "$PWKM%U?5X4$";

    private static File mDirectory;

    private static String mSeparator = DEFAULT_SEP;

    private static void loadSettings()
    {
        String exedir = System.getProperty("launch4j.exedir");
        if (exedir == null)
        {
            exedir = ".";
        }
        mDirectory = new File(exedir);

        File cfgFile = new File(mDirectory, "pdfsplit.cfg");

        try (BufferedReader rd = new BufferedReader(new FileReader(cfgFile)))
        {
            String line;
            while ((line = rd.readLine()) != null)
            {
                if (line.startsWith("#"))
                {
                    continue;
                }
                String[] parts = line.split("=", 2);
                if (parts.length == 2)
                {
                    if (parts[0].toUpperCase().equals("DIR"))
                    {
                        mDirectory = new File(parts[1]);
                    }
                    if (parts[0].toUpperCase().equals("SEPARATOR"))
                    {
                        mSeparator = parts[1].trim();
                    }
                }
            }
        }
        catch (Exception ex)
        {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }


    public static void main(String[] args) throws Exception
    {
        loadSettings();

        if (args.length > 0)
        {
            File input = new File(args[0]);
            if (args.length > 1)
            {
                mSeparator = args[1];
            }
            performSplit(input, mSeparator, input.getParentFile());
        }
        else
        {
            showFrame();
        }

    }


    protected static int performSplit(File aFile, String aSeparator,
            File aDirectory)
        throws IOException
    {
        if (!aFile.exists() || !aFile.isFile() || !aFile.canRead())
        {
            throw new RuntimeException(
                    "File " + aFile + " does not exist or can not be read!");
        }
        if (!aDirectory.isDirectory())
        {
            throw new RuntimeException(
                    "Directory " + aDirectory + " does not exist!");
        }

        PDDocument doc = PDDocument.load(aFile);
        int pgCount = doc.getNumberOfPages();
        System.out.println("Found " + pgCount + " pages!");

        List<PDDocument> allDocs = new SmartSplitter(aSeparator).split(doc);

        System.out.println("Found " + allDocs.size() + " documents!");

        int id = 0;

        for (PDDocument newDoc : allDocs)
        {
            File f = new File(aDirectory,
                    aFile.getName().replace(".pdf", "_" + id + ".pdf"));
            newDoc.save(f);
            id++;
        }

        return id;
    }

    private static JFrame mFrame;

    private static JButton mButtonOpen;

    private static JButton mButtonSplit;

    private static JLabel mLblFilename;

    private static JTextField mSplitText;

    protected static void showFrame()
    {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        mFrame = new JFrame("PDFSplit");
        mFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPnl = new JPanel(new BorderLayout());
        JPanel pnlButton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        mButtonOpen = new JButton(new ActionOpen());
        pnlButton.add(new JLabel("SplitText: "));
        pnlButton.add(mSplitText = new JTextField(mSeparator));
        pnlButton.add(mButtonOpen);
        mainPnl.add(pnlButton, BorderLayout.CENTER);
        mFrame.setContentPane(mainPnl);
        mFrame.setMinimumSize(new Dimension(400, 300));
        mFrame.setLocationByPlatform(true);
        mFrame.pack();
        mFrame.setVisible(true);
    }

    private static class ActionOpen extends AbstractAction
    {

        private static final long serialVersionUID = 139453373914591276L;

        public ActionOpen()
        {
            putValue(Action.NAME, "Split");
        }


        @Override
        public void actionPerformed(ActionEvent aE)
        {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileFilter()
            {

                @Override
                public String getDescription()
                {
                    return "*.pdf - PDF Files";
                }


                @Override
                public boolean accept(File aF)
                {
                    return aF.isDirectory()
                            || aF.getName().toLowerCase().endsWith(".pdf");
                }
            });
            if (mDirectory != null)
            {
                chooser.setCurrentDirectory(mDirectory);
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
            Thread t = new Thread(new Runnable()
            {

                @Override
                public void run()
                {
                    int count = 0;
                    try
                    {
                        count = performSplit(f, mSplitText.getText(),
                                f.getParentFile());
                    }
                    catch (Exception ex)
                    {
                        JOptionPane.showMessageDialog(mFrame, ex.getMessage(),
                                "ERROR!", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                    finally
                    {
                        JOptionPane.showMessageDialog(mFrame,
                                "All done, created " + count + " documents!");
                    }
                }
            });
            t.start();

        }

    }

}
