package de.code2be.pdfsplit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;

import javax.swing.UIManager;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

import de.code2be.pdfsplit.split.TextSplitIdentifier;
import de.code2be.pdfsplit.ui.swing.PDFSplitFrame;

public class PDFSplitter
{

    private static final Logger LOGGER = System
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
            LOGGER.log(Level.ERROR, ex.getMessage(), ex);
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

        PDDocument doc = Loader.loadPDF(aFile);
        int pgCount = doc.getNumberOfPages();
        System.out.println("Found " + pgCount + " pages!");

        SmartSplitter ss = new SmartSplitter();
        ss.addSplitPageIdentifier(new TextSplitIdentifier(new String[]
        {
                aSeparator
        }, 1));
        List<PDDocument> allDocs = ss.split(doc);

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

        new PDFSplitFrame().setVisible(true);
    }

}
