package de.code2be.pdfsplit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.UIManager;

import org.apache.pdfbox.pdmodel.PDDocument;

import de.code2be.pdfsplit.ui.swing.PDFSplitFrame;

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

        if (args.length > 0)
        {
            loadSettings();
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

        mFrame = new PDFSplitFrame();
        mFrame.setVisible(true);
    }

}
