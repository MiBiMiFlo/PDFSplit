package de.code2be.pdfsplit.ui.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.apache.pdfbox.pdmodel.PDDocument;

import de.code2be.help.I18n;
import de.code2be.pdfsplit.ISplitStatusListener;
import de.code2be.pdfsplit.SmartSplitter;
import de.code2be.pdfsplit.SplitStatusEvent;
import de.code2be.pdfsplit.ui.swing.actions.OpenFileAction;
import de.code2be.pdfsplit.ui.swing.actions.RenameAction;
import de.code2be.pdfsplit.ui.swing.actions.SaveAction;
import de.code2be.pdfsplit.ui.swing.actions.SaveAllAction;
import de.code2be.pdfsplit.ui.swing.actions.SaveAsAction;
import de.code2be.pdfsplit.ui.swing.actions.ShowSettingsAction;
import de.code2be.pdfsplit.ui.swing.actions.ZoomInAction;
import de.code2be.pdfsplit.ui.swing.actions.ZoomOutAction;

public class PDFSplitFrame extends JFrame
{

    private static final long serialVersionUID = 5992521065908641880L;

    private static final Logger LOGGER = Logger
            .getLogger(PDFSplitFrame.class.getName());

    public static final String DEFAULT_SEP = "$PWKM%U?5X4$";

    public static final String PROP_DIRECTORY_OPEN = "DIR_OPEN";

    public static final String PROP_DIRECTORY_SAVE = "DIR_SAVE";

    public static final String PROP_SEPARATOR = "SEPARATOR";

    private static JTextField mSplitText;

    private File mPDFFile;

    private PDDocument mPDFDocument;

    private boolean mWorking;

    private JLabel mLblStatus;

    private SmartSplitter mSmartSplitter;

    private JTabbedPane mDocsPane;

    private Properties mConfig;

    private Dimension mPreviewSize;

    public PDFSplitFrame()
    {
        super("PDFSplit-UI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setJMenuBar(createMenuBar());

        JPanel contentPane = new JPanel(new BorderLayout());

        JPanel topPane = new JPanel(new BorderLayout());

        JPanel mStatusPanel = new JPanel(new ListLayout(5, ListLayout.LEFT,
                ListLayout.STRETCH_HORIZONTAL));

        topPane.add(mStatusPanel, BorderLayout.CENTER);
        topPane.add(createToolBar(), BorderLayout.NORTH);
        contentPane.add(topPane, BorderLayout.NORTH);
        mStatusPanel.add(mLblStatus = new JLabel());
        mLblStatus.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mLblStatus.setFont(mLblStatus.getFont().deriveFont(14.0f));
        contentPane.add(mDocsPane = new JTabbedPane(), BorderLayout.CENTER);
        mDocsPane.setTabPlacement(JTabbedPane.LEFT);
        setContentPane(contentPane);
        setMinimumSize(new Dimension(800, 900));
        setLocationByPlatform(true);
        pack();
        setVisible(true);
    }


    protected Properties loadConfig()
    {
        Properties res = new Properties();
        res.put(PROP_SEPARATOR, DEFAULT_SEP);
        String exedir = System.getProperty("launch4j.exedir");
        if (exedir == null)
        {
            exedir = new File(".").getAbsolutePath();
        }
        res.put(PROP_DIRECTORY_OPEN, exedir);
        File dir = new File(exedir);

        File cfgFile = new File(dir, "pdfsplit.cfg");

        if (!cfgFile.isFile())
        {
            return res;
        }

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
                    if (parts[0].toUpperCase().equals(PROP_DIRECTORY_OPEN))
                    {
                        res.put(PROP_DIRECTORY_OPEN, parts[1].trim());
                    }
                    if (parts[0].toUpperCase().equals(PROP_DIRECTORY_SAVE))
                    {
                        res.put(PROP_DIRECTORY_SAVE, parts[1].trim());
                    }
                    if (parts[0].toUpperCase().equals(PROP_SEPARATOR))
                    {
                        res.put(PROP_SEPARATOR, parts[1].trim());
                    }
                }
            }
        }
        catch (Exception ex)
        {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
        return res;
    }


    private JMenuBar createMenuBar()
    {
        JMenuBar res = new JMenuBar();
        String txtFile = I18n.getMessage("de.code2be.pdfsplit.ui.swing.frame",
                "MENU.FILE.name");
        String txtView = I18n.getMessage("de.code2be.pdfsplit.ui.swing.frame",
                "MENU.VIEW.name");
        JMenu fileMenu = new JMenu(txtFile);
        fileMenu.add(new OpenFileAction(this));
        fileMenu.addSeparator();
        fileMenu.add(new SaveAction(this));
        fileMenu.add(new SaveAsAction(this));
        fileMenu.add(new SaveAllAction(this));
        fileMenu.addSeparator();
        fileMenu.add(new RenameAction(this));
        fileMenu.addSeparator();
        fileMenu.add(new ShowSettingsAction(this));
        res.add(fileMenu);

        JMenu editMenu = new JMenu(txtView);
        editMenu.add(new ZoomInAction(this));
        editMenu.add(new ZoomOutAction(this));
        res.add(editMenu);
        return res;
    }


    private JToolBar createToolBar()
    {
        JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
        toolBar.add(new OpenFileAction(this));
        toolBar.addSeparator();
        toolBar.add(new SaveAction(this));
        toolBar.add(new SaveAllAction(this));
        toolBar.add(new SaveAsAction(this));
        toolBar.add(new RenameAction(this));
        toolBar.addSeparator();
        toolBar.add(new ZoomInAction(this));
        toolBar.add(new ZoomOutAction(this));

        return toolBar;
    }


    public Properties getConfig()
    {
        if (mConfig == null)
        {
            mConfig = loadConfig();
        }
        return mConfig;
    }


    public void setSeparatorText(String aText)
    {
        mSplitText.setText(aText);
    }


    public Dimension getPreviewSize()
    {
        return mPreviewSize != null ? mPreviewSize : new Dimension(210, 297);
    }


    public void setPreviewSize(Dimension aSize)
    {
        mPreviewSize = aSize;
        for (PDFDocumentPanel docPane : getDocumentPanels())
        {
            docPane.setPreviewSize(aSize);
        }
    }


    public boolean isWorking()
    {
        return mWorking;
    }


    public void saveAll()
    {
        for (PDFDocumentPanel docPane : getDocumentPanels())
        {
            docPane.save();
        }
    }


    public List<PDFDocumentPanel> getDocumentPanels()
    {
        List<PDFDocumentPanel> res = new ArrayList<>();
        for (int i = 0; i < mDocsPane.getTabCount(); i++)
        {
            Component c = mDocsPane.getComponentAt(i);
            if (c instanceof PDFDocumentPanel)
            {
                res.add((PDFDocumentPanel) c);
            }
        }
        return res;
    }


    public PDFDocumentPanel getSelectedDocumentPanel()
    {
        Component c = mDocsPane.getSelectedComponent();
        if (c instanceof PDFDocumentPanel)
        {
            return (PDFDocumentPanel) c;
        }
        return null;
    }


    public void showError(String aMessage, String aTitle, Exception aException)
    {
        String longMessage = "";
        if (aMessage != null)
        {
            longMessage += aMessage;
        }
        if (aException != null)
        {
            if (longMessage.length() > 0)
            {
                longMessage += "\n";
            }
            longMessage += aException.getMessage();
        }
        final String finalMessage = longMessage;
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, finalMessage, aTitle,
                    JOptionPane.ERROR_MESSAGE);
        });

    }


    protected void updateStatusLabel()
    {
        StringBuilder sb = new StringBuilder("<html>");
        sb.append("File Name: ");
        if (mPDFFile != null)
        {
            sb.append(mPDFFile.getAbsolutePath());
        }
        sb.append("<br/>Pages: ");
        if (mPDFDocument != null)
        {
            sb.append(mPDFDocument.getNumberOfPages());
        }
        sb.append("<br/>");
        if (mSmartSplitter != null)
        {
            sb.append("Processed: ").append(mSmartSplitter.getCurrentPage())
                    .append("/").append(mPDFDocument.getNumberOfPages())
                    .append(" (").append(mSmartSplitter.getNumDocuments())
                    .append(" Documents)");
            sb.append("<br/>");
        }
        final String text = sb.toString();
        SwingUtilities.invokeLater(() -> {
            mLblStatus.setText(text);
        });

    }


    private void openFileImpl(File aFile)
    {
        try
        {
            mPDFFile = aFile;
            SwingUtilities.invokeLater(() -> {
                updateStatusLabel();
            });
            mPDFDocument = PDDocument.load(mPDFFile);

            String sepStr = getConfig().getProperty(PROP_SEPARATOR);
            mSmartSplitter = new SmartSplitter(sepStr);
            mSmartSplitter.setStatusListener(mSplitListener);
            mSmartSplitter.split(mPDFDocument);
        }
        catch (Exception ex)
        {
            showError(null, "ERROR - Open PDF File", ex);
        }
        finally
        {
            mWorking = false;
        }

    }


    public void openPDFFile(File aFile)
    {
        if (!aFile.isFile())
        {
            showError(aFile.getName() + " is not a valid file!",
                    "ERROR - Open PDF File", null);
            return;
        }
        synchronized (this)
        {
            if (isWorking())
            {
                return;
            }
            mWorking = true;
        }
        Thread t = new Thread(() -> {
            openFileImpl(aFile);
        });
        t.setDaemon(true);
        t.start();
    }


    public void splitPDFFile()
    {
        synchronized (this)
        {
            if (isWorking())
            {
                return;
            }
            mWorking = true;
        }
    }


    protected void addTabForDoc(PDDocument aDocument)
    {
        Thread t = new Thread(() -> {

            int id = mDocsPane.getTabCount() + 1;
            String fileName = mPDFFile.getName().replace(".pdf",
                    "_" + id + ".pdf");
            PDFDocumentPanel pnl = new PDFDocumentPanel(aDocument,
                    new File(mPDFFile.getParent(), fileName));
            pnl.setPreviewSize(getPreviewSize());
            pnl.addPropertyChangeListener("name", (aEvt) -> {
                for (int i = 0; i < mDocsPane.getTabCount(); i++)
                {
                    if (mDocsPane.getComponent(i) == aEvt.getSource())
                    {
                        mDocsPane.setTitleAt(i, aEvt.getNewValue().toString());
                    }
                }
            });

            SwingUtilities.invokeLater(() -> {
                mDocsPane.addTab(fileName, pnl);
                mDocsPane.setSelectedComponent(pnl);
            });

        });
        t.setDaemon(true);
        t.start();
    }

    private final ISplitStatusListener mSplitListener = new ISplitStatusListener()
    {

        @Override
        public void splitStatusUpdate(SplitStatusEvent aEvent)
        {
            updateStatusLabel();
            if (aEvent.getID() == SplitStatusEvent.EVENT_DOCUMENT_FINISHED)
            {
                final PDDocument doc = aEvent.getDocument();
                addTabForDoc(doc);
            }
        }
    };
}
