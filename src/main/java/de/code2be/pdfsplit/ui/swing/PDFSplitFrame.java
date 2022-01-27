package de.code2be.pdfsplit.ui.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
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
import javax.swing.UIManager;

import org.apache.pdfbox.pdmodel.PDDocument;

import de.code2be.help.I18n;
import de.code2be.help.TesseractC;
import de.code2be.help.TesseractFactory;
import de.code2be.pdfsplit.EmptyPageChecker;
import de.code2be.pdfsplit.ISplitStatusListener;
import de.code2be.pdfsplit.SmartSplitter;
import de.code2be.pdfsplit.SplitStatusEvent;
import de.code2be.pdfsplit.filters.DocumentFilterEvent;
import de.code2be.pdfsplit.filters.OCRFilter;
import de.code2be.pdfsplit.split.QRCodeIdentifier;
import de.code2be.pdfsplit.split.TextSplitIdentifier;
import de.code2be.pdfsplit.split.TextSplitIdentifierOCR;
import de.code2be.pdfsplit.ui.swing.actions.DeleteDocumentAction;
import de.code2be.pdfsplit.ui.swing.actions.OpenFileAction;
import de.code2be.pdfsplit.ui.swing.actions.RenameAction;
import de.code2be.pdfsplit.ui.swing.actions.SaveAction;
import de.code2be.pdfsplit.ui.swing.actions.SaveAllAction;
import de.code2be.pdfsplit.ui.swing.actions.SaveAsAction;
import de.code2be.pdfsplit.ui.swing.actions.ShowSettingsAction;
import de.code2be.pdfsplit.ui.swing.actions.ZoomInAction;
import de.code2be.pdfsplit.ui.swing.actions.ZoomOutAction;
import net.sourceforge.tess4j.ITessAPI.TessOcrEngineMode;

/**
 * This is the main frame class. It holds basic methods to do all required
 * actions.
 * 
 * @author Michael Weiss
 *
 */
public class PDFSplitFrame extends JFrame
{

    private static final long serialVersionUID = 5992521065908641880L;

    private static final Logger LOGGER = Logger
            .getLogger(PDFSplitFrame.class.getName());

    public static final String DEFAULT_SEP = "$PWKM%U?5X4$;PDF-SPLIT-SPLIT-PAGE;PDF-SPLIT-TRENNSEITE";

    public static final String DEFAULT_QR_CODE = "https://github.com/MiBiMiFlo/PDFSplit";

    public static final String PROP_DIRECTORY_OPEN = "main.dirOpen";

    public static final String PROP_DIRECTORY_SAVE = "main.dirSave";

    public static final String PROP_SEPARATOR_USE_TEXT = "separator.useText";

    public static final String PROP_SEPARATOR_USE_QR = "separator.useQR";

    public static final String PROP_SEPARATOR_TEXT = "separator.text";

    public static final String PROP_SEPARATOR_MATCH_COUNT = "separator.matchCount";

    public static final String PROP_SEPARATOR_QR_CODE = "separator.qrcode";

    public static final String PROP_SEPARATOR_DO_OCR = "separator.ocr.enable";

    public static final String PROP_SEPARATOR_FORCE_OCR = "separator.ocr.force";

    public static final String PROP_FILTER_DO_OCR = "filter.ocr.enable";

    public static final String PROP_FILTER_DO_EMPTY_PAGE = "filter.emptyPage.enable";

    public static final String PROP_FILTER_EMPTY_PAGE_TH_PIXEL = "filter.emptyPage.th.pixel";

    public static final String PROP_FILTER_EMPTY_PAGE_TH_BLOCK = "filter.emptyPage.th.block";

    public static final String PROP_FILTER_EMPTY_PAGE_TH_PAGE = "filter.emptyPage.th.page";

    public static final String PROP_FILTER_EMPTY_PAGE_BLOCKCOUNT_H = "filter.emptyPage.blockCountH";

    public static final String PROP_FILTER_EMPTY_PAGE_BLOCKCOUNT_V = "filter.emptyPage.blockCountV";

    public static final String PROP_OCR_DATAPATH = "ocr.datapath";

    public static final String PROP_OCR_LANG = "ocr.language";

    public static final String PROP_OCR_ENGINE_MODE = "ocr.engineMode";

    public static final String PROP_OCR_IMG_SCALE = "ocr.scale";

    private File mPDFFile;

    private PDDocument mPDFDocument;

    private boolean mWorking;

    private JLabel mLblFileInfo;

    private JTextField mStatusBar;

    private JTabbedPane mDocsPane;

    private Properties mConfig;

    private Dimension mPreviewSize;

    private int mTabIdx = 0;

    private ImageIcon mPdfFileIcon;

    /**
     * Create a new instance of the frame.
     */
    public PDFSplitFrame()
    {
        super(I18n.getMessage(PDFSplitFrame.class, "TITLE",
                I18n.getMessage(PDFSplitFrame.class, "version")));
        try
        {
            URL imgURL = getClass().getResource(
                    "/de/code2be/pdfsplit/ui/icons/64/pdfsplit.png");
            setIconImage(new ImageIcon(imgURL).getImage());
        }
        catch (Exception ex)
        {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initializeComponents();
        setMinimumSize(new Dimension(800, 900));
        setLocationByPlatform(true);
        pack();
        setVisible(true);
        mPdfFileIcon = new ImageIcon(getClass()
                .getResource("/de/code2be/pdfsplit/ui/icons/16/pdf.png"));
    }


    /**
     * Initialize the components to be displayed on the frame.
     */
    protected void initializeComponents()
    {
        setJMenuBar(createMenuBar());

        JPanel contentPane = new JPanel(new BorderLayout());
        setContentPane(contentPane);

        JPanel topPane = new JPanel(new BorderLayout());
        contentPane.add(topPane, BorderLayout.NORTH);

        JToolBar toolBar = createToolBar();
        if (toolBar != null)
        {
            topPane.add(toolBar, BorderLayout.NORTH);
        }

        JPanel statusPane = new JPanel(new FlowLayout(FlowLayout.LEADING));
        topPane.add(statusPane, BorderLayout.CENTER);

        statusPane.add(mLblFileInfo = new JLabel());
        mLblFileInfo.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mLblFileInfo.setFont(mLblFileInfo.getFont().deriveFont(14.0f));

        contentPane.add(mStatusBar = new JTextField(), BorderLayout.SOUTH);
        mStatusBar.setText("Ready");
        mStatusBar.setEditable(false);
        mStatusBar.setBackground(mLblFileInfo.getBackground());
        mStatusBar.setForeground(mLblFileInfo.getForeground());
        mStatusBar.setFont(mLblFileInfo.getFont());

        contentPane.add(mDocsPane = new JTabbedPane(), BorderLayout.CENTER);
        mDocsPane.setTabPlacement(JTabbedPane.LEFT);
        mDocsPane.setFont(mDocsPane.getFont().deriveFont(14.0f));

    }


    /**
     * Create a config Properties instance that is filled with default values.
     * 
     * @return a newly created and initially filled config.
     */
    protected Properties createDefaultConfig()
    {
        Properties res = new Properties();
        res.put(PROP_SEPARATOR_TEXT, DEFAULT_SEP);
        res.put(PROP_SEPARATOR_MATCH_COUNT, String.valueOf(1));
        String exedir = System.getProperty("launch4j.exedir");
        if (exedir == null)
        {
            exedir = new File(".").getAbsolutePath();
        }
        res.put(PROP_DIRECTORY_OPEN, exedir);
        res.put(PROP_SEPARATOR_DO_OCR, String.valueOf(true));
        res.put(PROP_SEPARATOR_FORCE_OCR, String.valueOf(false));

        res.put(PROP_FILTER_DO_OCR, String.valueOf(true));
        res.put(PROP_SEPARATOR_USE_TEXT, String.valueOf(true));
        res.put(PROP_SEPARATOR_USE_QR, String.valueOf(true));

        res.put(PROP_SEPARATOR_QR_CODE, DEFAULT_QR_CODE);
        res.put(PROP_OCR_DATAPATH, "./tessdata");
        res.put(PROP_OCR_LANG, "deu+eng");
        res.put(PROP_OCR_ENGINE_MODE, "3");
        res.put(PROP_OCR_IMG_SCALE, "2.5");

        res.put(PROP_FILTER_DO_EMPTY_PAGE, String.valueOf(true));
        res.put(PROP_FILTER_EMPTY_PAGE_TH_PIXEL, "25");
        res.put(PROP_FILTER_EMPTY_PAGE_TH_BLOCK, "2");
        res.put(PROP_FILTER_EMPTY_PAGE_TH_PIXEL, "6");
        res.put(PROP_FILTER_EMPTY_PAGE_BLOCKCOUNT_H, "10");
        res.put(PROP_FILTER_EMPTY_PAGE_BLOCKCOUNT_V, "10");

        return res;
    }


    /**
     * Retrieve the config files that stores the application config. The logic
     * is to return only existing config files in the order of the following
     * list:
     * <ul>
     * <li>./pdfsplit.cfg
     * <li>${HOME}/.pdfsplit.cfg
     * <li>${HOME}/pdfsplit.cfg
     * <li>${USERPROFILE}/.pdfsplit.cfg
     * <li>${USERPROFILE}/pdfsplit.cfg
     * </ul>
     * In case a config entry is available in multiple existing config files,
     * the entry in the last file of the list takes effect.
     * 
     * @return the config files to be used. Might be an empty list.
     */
    protected List<File> getConfigFiles()
    {
        List<File> res = new ArrayList<>();
        String profileDir = ".";
        File f = new File(".", "pdfsplit.cfg").getAbsoluteFile();
        if (f.isFile())
        {
            res.add(f);
        }

        if (System.getenv().containsKey("HOME"))
        {
            profileDir = System.getenv("HOME");
            f = new File(profileDir, ".pdfsplit.cfg");
            if (f.isFile())
            {
                res.add(f);
            }

            f = new File(profileDir, "pdfsplit.cfg");
            if (f.isFile())
            {
                res.add(f);
            }
        }
        if (System.getenv().containsKey("USERPROFILE"))
        {
            profileDir = System.getenv("USERPROFILE");
            f = new File(profileDir, "pdfsplit.cfg");
            if (f.isFile())
            {
                res.add(f);
            }
            f = new File(profileDir, ".pdfsplit.cfg");
            if (f.isFile())
            {
                res.add(f);
            }
        }
        return res;

    }


    protected File getConfigFileForWrite()
    {
        String profileDir = ".";
        File f = null;
        if (System.getenv().containsKey("HOME"))
        {
            profileDir = System.getenv("HOME");
            f = new File(profileDir, "pdfsplit.cfg");
            if (f.isFile())
            {
                return f;
            }
            return new File(profileDir, ".pdfsplit.cfg");
        }
        else if (System.getenv().containsKey("USERPROFILE"))
        {
            profileDir = System.getenv("USERPROFILE");
            f = new File(profileDir, "pdfsplit.cfg");
            if (f.isFile())
            {
                return f;
            }
            return new File(profileDir, ".pdfsplit.cfg");
        }
        f = new File(".", "pdfsplit.cfg").getAbsoluteFile();
        return f.isFile() ? f : null;
    }


    /**
     * Load the general config for the application. This is a Properties
     * instance filled will String key/value pairs. <br/>
     * The config file is taken from the result of the call to
     * {@link #getConfigFile()}.<br/>
     * 
     * To ensure the result is never uninitialized, the config is created by
     * calling {@link #createDefaultConfig()} and therefore initialized with
     * default values before actual values are loaded.
     * 
     * @return the config as available.
     */
    protected Properties loadConfig()
    {
        System.getenv(DEFAULT_SEP);
        Properties res = createDefaultConfig();

        List<File> cfgFiles = getConfigFiles();

        for (File cfgFile : cfgFiles)
        {
            LOGGER.log(Level.INFO, "Will load config from {0}.", cfgFile);
            try (FileReader rd = new FileReader(cfgFile))
            {
                res.load(rd);
            }
            catch (Exception ex)
            {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                setStatusText(I18n.getMessage("main.error.loadConfig",
                        cfgFile.getAbsolutePath(), ex.getLocalizedMessage()));
            }
        }

        return res;
    }


    /**
     * Save the actual config to the default user based config file.
     */
    public void saveConfig()
    {
        File f = getConfigFileForWrite();
        try (FileWriter wr = new FileWriter(f))
        {
            getConfig().store(wr, "# Config file for PDF Splitter Application");
        }
        catch (Exception ex)
        {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            showError(ex.getMessage(), "ERROR - Can not write config", ex);
        }
    }


    /**
     * Create the menu bar to be displayed at the top of the frame.
     * 
     * @return the menu bar to be used or null if no menu bar should be
     *         displayed.
     */
    protected JMenuBar createMenuBar()
    {
        JMenuBar res = new JMenuBar();
        JMenu fileMenu = new JMenu(
                I18n.getMessage(PDFSplitFrame.class, "MENU.FILE.name"));
        fileMenu.add(new OpenFileAction(this));
        fileMenu.addSeparator();
        fileMenu.add(new SaveAction(this));
        fileMenu.add(new SaveAsAction(this));
        fileMenu.add(new SaveAllAction(this));
        fileMenu.addSeparator();
        fileMenu.add(new RenameAction(this));
        fileMenu.addSeparator();
        fileMenu.add(new DeleteDocumentAction(this));
        fileMenu.addSeparator();
        fileMenu.add(new ShowSettingsAction(this));
        res.add(fileMenu);

        JMenu editMenu = new JMenu(
                I18n.getMessage(PDFSplitFrame.class, "MENU.EDIT.name"));
        editMenu.add(new RenameAction(this));
        res.add(editMenu);

        JMenu viewMenu = new JMenu(
                I18n.getMessage(PDFSplitFrame.class, "MENU.VIEW.name"));
        viewMenu.add(new ZoomInAction(this));
        viewMenu.add(new ZoomOutAction(this));
        res.add(viewMenu);
        return res;
    }


    /**
     * Create the tool bar to be displayed below the menu bar.
     * 
     * @return the tool bar to be displayed below the menu bar or null if no
     *         tool bar should be displayed.
     */
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
        toolBar.add(new DeleteDocumentAction(this));
        toolBar.addSeparator();
        toolBar.add(new ZoomInAction(this));
        toolBar.add(new ZoomOutAction(this));

        return toolBar;
    }


    /**
     * 
     * @return the actual config element. This returns the original config, so
     *         modifications can be made.
     */
    public Properties getConfig()
    {
        if (mConfig == null)
        {
            mConfig = loadConfig();
        }
        return mConfig;
    }


    public int getConfigValI(String aKey, int aDefault)
    {
        try
        {
            return Integer.valueOf(getConfig().getProperty(aKey));
        }
        catch (Exception e)
        {
            return aDefault;
        }
    }


    public float getConfigValF(String aKey, float aDefault)
    {
        try
        {
            return Float.valueOf(getConfig().getProperty(aKey));
        }
        catch (Exception e)
        {
            return aDefault;
        }
    }


    public boolean getConfigValB(String aKey, boolean aDefault)
    {
        try
        {
            String strVal = getConfig().getProperty(aKey);
            if (strVal == null)
            {
                return aDefault;
            }
            return strVal.equalsIgnoreCase("1")
                    || strVal.equalsIgnoreCase("yes")
                    || strVal.equalsIgnoreCase(String.valueOf(true));
        }
        catch (Exception e)
        {
            return aDefault;
        }
    }


    public String getConfigValS(String aKey, String aDefault)
    {
        try
        {
            String strVal = getConfig().getProperty(aKey);
            return (strVal != null) ? strVal : aDefault;
        }
        catch (Exception e)
        {
            return aDefault;
        }

    }


    /**
     * 
     * @param aProperties
     *            the new config in case it need to be replaced.
     */
    public void setConfig(Properties aProperties)
    {
        mConfig = (Properties) aProperties.clone();
    }


    /**
     * 
     * @return the actual preview size for PDF pages.
     */
    public Dimension getPreviewSize()
    {
        return mPreviewSize != null ? mPreviewSize : new Dimension(210, 297);
    }


    /**
     * 
     * @param aSize
     *            the new size to be set for PDF pages.
     */
    public void setPreviewSize(Dimension aSize)
    {
        mPreviewSize = aSize;
        for (PDFDocumentPanel docPane : getDocumentPanels())
        {
            docPane.setPreviewSize(aSize);
        }

        setStatusText(I18n.getMessage(PDFSplitFrame.class,
                "main.setPreviewSize", aSize.width, aSize.height));
    }


    /**
     * 
     * @return true if the application is currently working (opening PDF file,
     *         splitting, ...), false otherwise.
     */
    public boolean isWorking()
    {
        return mWorking;
    }


    /**
     * Save all open (split) documents. This saves the documents and ignores the
     * disabled pages.
     */
    public void saveAll()
    {
        for (PDFDocumentPanel docPane : getDocumentPanels())
        {
            docPane.save();
        }
    }


    /**
     * 
     * @return the list of open (split) PDF document panels.
     */
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


    /**
     * 
     * @return the currently active (selected) PDF document panel.
     */
    public PDFDocumentPanel getSelectedDocumentPanel()
    {
        Component c = mDocsPane.getSelectedComponent();
        if (c instanceof PDFDocumentPanel)
        {
            return (PDFDocumentPanel) c;
        }
        return null;
    }


    /**
     * Show an error message to the user. This displays an modal
     * {@link JOptionPane} rendered as error message in an asynchronous
     * way.<br/>
     * This method is thread save and ensures the message is displayed non
     * blocking inside of the AWT thread.
     * 
     * @param aMessage
     *            the message to be displayed.
     * @param aTitle
     *            the message title.
     * @param aException
     *            the exception that caused this error message. This might be
     *            null.
     */
    public void showError(String aMessage, String aTitle, Exception aException)
    {
        setStatusText(I18n.getMessage(PDFSplitFrame.class, "main.errorMessage",
                aMessage));
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


    /**
     * Display the given text as status text in the status bar at the bottom of
     * the frame. This method is thread save and non blocking. It is ensured
     * that the status text is displayed within the AWT thread.
     * 
     * @param aText
     *            the new status text to be displayed.
     */
    public void setStatusText(final String aText)
    {
        SwingUtilities.invokeLater(() -> mStatusBar.setText(aText));
    }


    /**
     * Update the file info label for the input file. This method is thread save
     * and non blocking. It is ensured that the text is displayed within the AWT
     * thread.
     * 
     * @param aEvent
     *            the event that caused the into text to be changed.
     */
    protected void updateFileInfoLabel(SplitStatusEvent aEvent)
    {
        StringBuilder sb = new StringBuilder("<html>");

        sb.append(I18n.getMessage(PDFSplitFrame.class, "main.fileInfo.fileName",
                (mPDFFile != null) ? mPDFFile.getAbsolutePath() : ""));
        sb.append("<br/>");
        if (aEvent != null)
        {
            sb.append(I18n.getMessage(PDFSplitFrame.class,
                    "main.fileInfo.pages", aEvent.getPageCount()));
            sb.append("<br/>");

            if (aEvent.getID() == SplitStatusEvent.EVENT_SPLITTING_FINISHED)
            {
                int realPageCount = 0;
                for (PDDocument doc : aEvent.getSource().getTargetDocuments())
                {
                    realPageCount += doc.getNumberOfPages();
                }
                sb.append(I18n.getMessage(PDFSplitFrame.class,
                        "main.fileInfo.finished", realPageCount,
                        aEvent.getDocumentCount()));
            }
            else
            {
                sb.append(I18n.getMessage(PDFSplitFrame.class,
                        "main.fileInfo.processed",
                        (aEvent.getCurrentPage() + 1),
                        aEvent.getDocumentCount()));
            }
            sb.append("<br/>");
        }

        final String text = sb.toString();
        SwingUtilities.invokeLater(() -> {
            mLblFileInfo.setText(text);
        });

    }


    /**
     * 
     * @return a new instance of configured OCR engine.
     */
    protected TesseractFactory createOCRFactory()
    {
        TesseractFactory res = new TesseractFactory();
        File dataPath = new File(
                getConfigValS(PROP_OCR_DATAPATH, "./tessdata"));

        String language = getConfigValS(PROP_OCR_LANG, "eng");

        if (dataPath.isDirectory())
        {
            res.setDatapath(dataPath.getAbsolutePath());
        }

        res.setLanguage(language);

        try
        {
            int engineMode = getConfigValI(PROP_OCR_ENGINE_MODE,
                    TessOcrEngineMode.OEM_LSTM_ONLY);
            res.setOcrEngineMode(engineMode);
        }
        catch (Exception ex)
        {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }

        return res;
    }


    protected TesseractC createOCREngine()
    {
        File dataPath = new File(
                getConfigValS(PROP_OCR_DATAPATH, "./tessdata"));

        String language = getConfigValS(PROP_OCR_LANG, "eng");

        TesseractC tesseract = new TesseractC();
        if (dataPath.isDirectory())
        {
            tesseract.setDatapath(dataPath.getAbsolutePath());
        }

        tesseract.setLanguage(language);

        try
        {
            int engineMode = getConfigValI(PROP_OCR_ENGINE_MODE, 0);
            tesseract.setOcrEngineMode(engineMode);
        }
        catch (Exception ex)
        {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }

        return tesseract;
    }


    /**
     * Open a PDF file and split it into multiple documents. This method is
     * blocking and should not be called from within the AWT thread. All UI
     * modifications are ensured to be called from within the AWT thread in an
     * asynchronous way. Therefore it is not guaranteed that all PDF document
     * panels are added to the UI after the method returned.
     * 
     * @param aFile
     *            the file to be opened and split.
     */
    public void openPDFFile(File aFile)
    {
        if (aFile == null)
        {
            throw new IllegalArgumentException("Null is not allowed as File!");
        }

        if (!aFile.isFile())
        {
            throw new IllegalArgumentException(
                    aFile.getAbsolutePath() + " is not a valid file!");
        }

        synchronized (this)
        {
            if (isWorking())
            {
                return;
            }
            mWorking = true;
        }

        if (mPDFDocument != null)
        {
            try
            {
                mPDFDocument.close();
            }
            catch (Exception ex)
            {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                showError(ex.getMessage(), "ERROR - Closing old PDF File", ex);
            }
            finally
            {
                mPDFDocument = null;
                mPDFFile = null;
            }
        }

        try
        {
            mPDFFile = aFile;
            updateFileInfoLabel(null);
            setStatusText(I18n.getMessage(PDFSplitFrame.class,
                    "open.msgWillOpen", mPDFFile.getAbsolutePath()));
            mPDFDocument = PDDocument.load(mPDFFile);
            if (getConfigValB(PROP_FILTER_DO_OCR, true))
            {
                TesseractFactory tf = createOCRFactory();
                OCRFilter ocrFilter = new OCRFilter(tf);
                ocrFilter.setScale(getConfigValF(PROP_OCR_IMG_SCALE, 1.0f));
                ocrFilter.addDocumentFilterListener((aEvent) -> {
                    if (aEvent.getID() == DocumentFilterEvent.EVENT_PAGE_DONE)
                    {
                        setStatusText(I18n.getMessage(PDFSplitFrame.class,
                                "open.msgOCR", mPDFFile.getName(),
                                aEvent.getPageIndex() + 1,
                                aEvent.getPageCount()));
                    }
                });
                mPDFDocument = ocrFilter.filter(mPDFDocument);
            }
            setStatusText(I18n.getMessage(PDFSplitFrame.class,
                    "open.msgSplitting", mPDFFile.getAbsolutePath()));

            SmartSplitter smsp = new SmartSplitter();
            smsp.addStatusListener(mSplitListener);

            boolean doQrSep = getConfigValB(PROP_SEPARATOR_USE_QR, true);
            boolean doTextSep = getConfigValB(PROP_SEPARATOR_USE_TEXT, true);
            if (doQrSep)
            {
                String qrCode = getConfigValS(PROP_SEPARATOR_QR_CODE, null);
                if (qrCode != null && qrCode.trim().length() > 0)
                {
                    LOGGER.log(Level.INFO, "Will use QR code splitter for: {0}",
                            qrCode);
                    smsp.addSplitPageIdentifier(new QRCodeIdentifier(qrCode));
                }
            }

            String sepStr = null;
            String[] sepArr = null;
            int reqFindCount = 1;

            if (doTextSep)
            {
                sepStr = getConfigValS(PROP_SEPARATOR_TEXT, null);
                sepArr = (sepStr != null && sepStr.trim().length() > 0)
                        ? sepStr.split(";")
                        : new String[0];
                try
                {
                    reqFindCount = getConfigValI(PROP_SEPARATOR_MATCH_COUNT, 1);
                }
                catch (Exception ex)
                {
                    LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
            else
            {
                sepArr = new String[0];
            }

            TesseractC ocrEng = null;
            try
            {
                if (sepArr.length > 0)
                {
                    boolean doOCR = getConfigValB(PROP_SEPARATOR_DO_OCR, true);
                    if (doOCR)
                    {
                        boolean forceOCR = getConfigValB(
                                PROP_SEPARATOR_FORCE_OCR, false);

                        LOGGER.log(Level.INFO,
                                "Will use OCR based text splitter for: {0} (findCount= {1}, ForceOCR={2})",
                                new Object[]
                                {
                                        sepStr, reqFindCount, forceOCR
                                });
                        TextSplitIdentifierOCR ocrSplitter = new TextSplitIdentifierOCR(
                                sepArr, reqFindCount, forceOCR);
                        ocrSplitter.setTesseract(ocrEng = createOCREngine());
                        ocrSplitter.setScale(
                                getConfigValF(PROP_OCR_IMG_SCALE, 1.0f));
                        smsp.addSplitPageIdentifier(ocrSplitter);
                    }
                    else
                    {
                        LOGGER.log(Level.INFO,
                                "Will use normal text splitter for: {0} (findCount={1})",
                                new Object[]
                                {
                                        sepStr, reqFindCount
                                });
                        smsp.addSplitPageIdentifier(
                                new TextSplitIdentifier(sepArr, reqFindCount));
                    }
                }
                setStatusText(
                        "Will split file " + mPDFFile.getAbsolutePath() + ".");
                smsp.split(mPDFDocument);
            }
            finally
            {
                if (ocrEng != null)
                {
                    ocrEng.close();
                }
            }

        }
        catch (Exception ex)
        {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            showError(ex.getMessage(), "ERROR - Open PDF File", ex);
        }
        finally
        {
            mWorking = false;
        }
    }


    /**
     * Add a {@link PDFDocumentPanel} to this frame for the given document.
     * 
     * @param aDocument
     *            the document that should be displayed in a new PDF document
     *            panel.
     */
    protected void addTabForDoc(PDDocument aDocument)
    {
        mTabIdx++;
        String fileName = mPDFFile.getName().replace(".pdf",
                "_" + mTabIdx + ".pdf");
        PDFDocumentPanel pnl = new PDFDocumentPanel(aDocument,
                new File(mPDFFile.getParent(), fileName));

        if (getConfigValB(PROP_FILTER_DO_EMPTY_PAGE, true))
        {
            EmptyPageChecker epc = new EmptyPageChecker(aDocument);
            epc.setBlockCountH(Integer.valueOf(
                    getConfigValI(PROP_FILTER_EMPTY_PAGE_BLOCKCOUNT_H, 10)));
            epc.setBlockCountV(
                    getConfigValI(PROP_FILTER_EMPTY_PAGE_BLOCKCOUNT_V, 10));
            epc.setPixelFilledThreshold(
                    getConfigValI(PROP_FILTER_EMPTY_PAGE_TH_PIXEL, 25));
            epc.setBlockFilledThreshold(
                    getConfigValI(PROP_FILTER_EMPTY_PAGE_TH_BLOCK, 2));
            epc.setPageFilledThreshold(
                    getConfigValI(PROP_FILTER_EMPTY_PAGE_TH_PAGE, 6));

            int idx = 0;
            for (PDFPagePanel pagePanel : pnl.getPagePanels())
            {
                if (epc.isPageEmpty(pagePanel.getPage(), idx))
                {
                    pagePanel.setPageEnabled(false);
                }
                idx++;
            }
        }

        JLabel lbl = new JLabel(fileName, mPdfFileIcon, JLabel.LEFT);
        lbl.setFont(mDocsPane.getFont());

        pnl.setPreviewSize(getPreviewSize());
        pnl.addPropertyChangeListener("name", (aEvt) -> {
            lbl.setText((String) aEvt.getNewValue());
        });

        Runnable r = () -> {
            int size = mDocsPane.getTabCount();
            mDocsPane.insertTab(fileName, mPdfFileIcon, pnl, fileName, size);
            mDocsPane.setSelectedIndex(size);
            mDocsPane.setTabComponentAt(size, lbl);
        };

        SwingUtilities.invokeLater(r);
    }

    /**
     * The listener that gets notified for new {@link SplitStatusEvent}'s from
     * the {@link SmartSplitter} that is splitting the opened document.
     */
    private final ISplitStatusListener mSplitListener = new ISplitStatusListener()
    {

        @Override
        public void splitStatusUpdate(SplitStatusEvent aEvent)
        {
            updateFileInfoLabel(aEvent);
            if (aEvent.getID() == SplitStatusEvent.EVENT_DOCUMENT_FINISHED)
            {
                final PDDocument doc = aEvent.getDocument();
                addTabForDoc(doc);
            }
        }
    };

    /**
     * The main method that can be used to show the frame.
     * 
     * @param args
     *            the arguments to this application.
     */
    public static void main(String[] args)
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
