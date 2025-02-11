package de.code2be.pdfsplit.ui.swing;

import static de.code2be.pdfsplit.Config.PROP_FILTER_DO_EMPTY_PAGE;
import static de.code2be.pdfsplit.Config.PROP_FILTER_DO_OCR;
import static de.code2be.pdfsplit.Config.PROP_FILTER_EMPTY_PAGE_BLOCKCOUNT_H;
import static de.code2be.pdfsplit.Config.PROP_FILTER_EMPTY_PAGE_BLOCKCOUNT_V;
import static de.code2be.pdfsplit.Config.PROP_FILTER_EMPTY_PAGE_TH_BLOCK;
import static de.code2be.pdfsplit.Config.PROP_FILTER_EMPTY_PAGE_TH_PAGE;
import static de.code2be.pdfsplit.Config.PROP_FILTER_EMPTY_PAGE_TH_PIXEL;
import static de.code2be.pdfsplit.Config.PROP_OCR_DATAPATH;
import static de.code2be.pdfsplit.Config.PROP_OCR_ENGINE_MODE;
import static de.code2be.pdfsplit.Config.PROP_OCR_IMG_SCALE;
import static de.code2be.pdfsplit.Config.PROP_OCR_LANG;
import static de.code2be.pdfsplit.Config.PROP_SEPARATOR_DO_OCR;
import static de.code2be.pdfsplit.Config.PROP_SEPARATOR_FORCE_OCR;
import static de.code2be.pdfsplit.Config.PROP_SEPARATOR_MATCH_COUNT;
import static de.code2be.pdfsplit.Config.PROP_SEPARATOR_QR_CODE;
import static de.code2be.pdfsplit.Config.PROP_SEPARATOR_TEXT;
import static de.code2be.pdfsplit.Config.PROP_SEPARATOR_USE_QR;
import static de.code2be.pdfsplit.Config.PROP_SEPARATOR_USE_TEXT;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

import de.code2be.help.I18n;
import de.code2be.help.TesseractC;
import de.code2be.help.TesseractFactory;
import de.code2be.pdfsplit.Config;
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

    private static final Logger LOGGER = System
            .getLogger(PDFSplitFrame.class.getName());

    private File mPDFFile;

    private PDDocument mPDFDocument;

    private boolean mWorking;

    private JLabel mLblFileInfo;

    private JTextField mStatusBar;

    private JTabbedPane mDocsPane;

    private Config mConfig;

    private Dimension mPreviewSize;

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
            LOGGER.log(Level.ERROR, ex.getMessage(), ex);
        }
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(mWindowCloseListener);

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
        JPanel contentPane = new JPanel(new BorderLayout());
        setContentPane(contentPane);

        registerActions();

        setJMenuBar(createMenuBar());

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

        mDocsPane.addMouseWheelListener((aE) -> zoomOnMouseWheel(aE));

        mDocsPane.setDropTarget(mDocPaneDropTarget);

    }


    /**
     * Retrieve an action from the {@link JComponent#getActionMap()} of the
     * content pane.
     * 
     * @param aKey
     *            the key of the action to retrieve.
     * @return the action for the given key or null if no action is available
     *         for the given key.
     */
    protected Action getAction(String aKey)
    {
        return ((JComponent) getContentPane()).getActionMap().get(aKey);
    }


    /**
     * Register the given action to the content pane's action map and input map.
     * 
     * @param aAction
     *            the action to register. This must not be null.
     */
    protected void registerAction(Action aAction)
    {
        Object key = aAction.getValue(Action.ACTION_COMMAND_KEY);
        ((JComponent) getContentPane()).getActionMap().put(key, aAction);

        Object ks = aAction.getValue(Action.ACCELERATOR_KEY);
        if (ks instanceof KeyStroke)
        {
            ((JComponent) getContentPane()).getInputMap().put((KeyStroke) ks,
                    key);
        }
    }


    /**
     * Register all actions that belong to this application.
     */
    protected void registerActions()
    {
        registerAction(new OpenFileAction(this));
        registerAction(new SaveAction(this));
        registerAction(new SaveAsAction(this));
        registerAction(new SaveAllAction(this));
        registerAction(new RenameAction(this));
        registerAction(new DeleteDocumentAction(this));
        registerAction(new ShowSettingsAction(this));
        registerAction(new RenameAction(this));
        registerAction(new ZoomInAction(this));
        registerAction(new ZoomOutAction(this));
    }


    protected int getKeyCodeForMnemonic(String aMnemonic)
    {
        try
        {
            KeyStroke ks = KeyStroke.getKeyStroke(aMnemonic);
            if (ks != null)
            {
                return ks.getKeyCode();
            }
        }
        catch (Exception ex)
        {
            LOGGER.log(Level.ERROR, ex.getMessage(), ex);
        }
        return 0;
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
        fileMenu.setMnemonic(getKeyCodeForMnemonic(
                I18n.getMessage(PDFSplitFrame.class, "MENU.FILE.mnemonic")));
        fileMenu.add(getAction(OpenFileAction.ACTION_NAME));
        fileMenu.addSeparator();
        fileMenu.add(getAction(SaveAction.ACTION_NAME));
        fileMenu.add(getAction(SaveAsAction.ACTION_NAME));
        fileMenu.add(getAction(SaveAllAction.ACTION_NAME));
        fileMenu.addSeparator();
        fileMenu.add(getAction(RenameAction.ACTION_NAME));
        fileMenu.addSeparator();
        fileMenu.add(getAction(DeleteDocumentAction.ACTION_NAME));
        fileMenu.addSeparator();
        fileMenu.add(getAction(ShowSettingsAction.ACTION_NAME));
        res.add(fileMenu);

        JMenu editMenu = new JMenu(
                I18n.getMessage(PDFSplitFrame.class, "MENU.EDIT.name"));
        editMenu.setMnemonic(getKeyCodeForMnemonic(
                I18n.getMessage(PDFSplitFrame.class, "MENU.EDIT.mnemonic")));
        editMenu.add(getAction(RenameAction.ACTION_NAME));
        res.add(editMenu);

        JMenu viewMenu = new JMenu(
                I18n.getMessage(PDFSplitFrame.class, "MENU.VIEW.name"));
        viewMenu.setMnemonic(getKeyCodeForMnemonic(
                I18n.getMessage(PDFSplitFrame.class, "MENU.VIEW.mnemonic")));
        viewMenu.add(getAction(ZoomInAction.ACTION_NAME));
        viewMenu.add(getAction(ZoomOutAction.ACTION_NAME));
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
        toolBar.add(getAction(OpenFileAction.ACTION_NAME));
        toolBar.addSeparator();
        toolBar.add(getAction(SaveAction.ACTION_NAME));
        toolBar.add(getAction(SaveAllAction.ACTION_NAME));
        toolBar.add(getAction(SaveAsAction.ACTION_NAME));
        toolBar.add(getAction(RenameAction.ACTION_NAME));
        toolBar.addSeparator();
        toolBar.add(getAction(DeleteDocumentAction.ACTION_NAME));
        toolBar.addSeparator();
        toolBar.add(getAction(ZoomInAction.ACTION_NAME));
        toolBar.add(getAction(ZoomOutAction.ACTION_NAME));

        return toolBar;
    }


    /**
     * 
     * @return the actual config element. This returns the original config, so
     *         modifications can be made.
     */
    public Config getConfig()
    {
        if (mConfig == null)
        {
            try
            {
                mConfig = Config.loadConfig();
            }
            catch (Exception ex)
            {
                LOGGER.log(Level.ERROR, ex.getMessage(), ex);
                setStatusText(I18n.getMessage("main.error.loadConfig",
                        ex.getLocalizedMessage()));
            }

        }
        return mConfig;
    }


    /**
     * 
     * @param aConfig
     *            the new config in case it need to be replaced.
     */
    public void setConfig(Config aConfig)
    {
        mConfig = new Config(aConfig);
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
                getConfig().getConfigValS(PROP_OCR_DATAPATH, "./tessdata"));

        String language = getConfig().getConfigValS(PROP_OCR_LANG, "eng");

        if (dataPath.isDirectory())
        {
            res.setDatapath(dataPath.getAbsolutePath());
        }

        res.setLanguage(language);

        try
        {
            int engineMode = getConfig().getConfigValI(PROP_OCR_ENGINE_MODE,
                    TessOcrEngineMode.OEM_LSTM_ONLY);
            res.setOcrEngineMode(engineMode);
        }
        catch (Exception ex)
        {
            LOGGER.log(Level.ERROR, ex.getMessage(), ex);
        }

        return res;
    }


    protected TesseractC createOCREngine()
    {
        File dataPath = new File(
                getConfig().getConfigValS(PROP_OCR_DATAPATH, "./tessdata"));

        String language = getConfig().getConfigValS(PROP_OCR_LANG, "eng");

        TesseractC tesseract = new TesseractC();
        if (dataPath.isDirectory())
        {
            tesseract.setDatapath(dataPath.getAbsolutePath());
        }

        tesseract.setLanguage(language);

        try
        {
            int engineMode = getConfig().getConfigValI(PROP_OCR_ENGINE_MODE, 0);
            tesseract.setOcrEngineMode(engineMode);
        }
        catch (Exception ex)
        {
            LOGGER.log(Level.ERROR, ex.getMessage(), ex);
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
                LOGGER.log(Level.ERROR, ex.getMessage(), ex);
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
            mPDFDocument = Loader.loadPDF(mPDFFile);
            if (getConfig().getConfigValB(PROP_FILTER_DO_OCR, true))
            {
                TesseractFactory tf = createOCRFactory();
                OCRFilter ocrFilter = new OCRFilter(tf);
                ocrFilter.setScale(
                        getConfig().getConfigValF(PROP_OCR_IMG_SCALE, 1.0f));
                ocrFilter.addDocumentFilterListener((aEvent) -> {
                    if (aEvent.getID() == DocumentFilterEvent.EVENT_NEXT_PAGE)
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

            String namePattern = mPDFFile.getName().replace(".pdf", "_{0}.pdf");
            smsp.setNamePattern(namePattern);
            smsp.setTargetDirectory(mPDFFile.getParentFile());

            boolean doQrSep = getConfig().getConfigValB(PROP_SEPARATOR_USE_QR,
                    true);
            boolean doTextSep = getConfig()
                    .getConfigValB(PROP_SEPARATOR_USE_TEXT, true);
            if (doQrSep)
            {
                String qrCode = getConfig()
                        .getConfigValS(PROP_SEPARATOR_QR_CODE, null);
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
                sepStr = getConfig().getConfigValS(PROP_SEPARATOR_TEXT, null);
                sepArr = (sepStr != null && sepStr.trim().length() > 0)
                        ? sepStr.split(";")
                        : new String[0];
                try
                {
                    reqFindCount = getConfig()
                            .getConfigValI(PROP_SEPARATOR_MATCH_COUNT, 1);
                }
                catch (Exception ex)
                {
                    LOGGER.log(Level.ERROR, ex.getMessage(), ex);
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
                    boolean doOCR = getConfig()
                            .getConfigValB(PROP_SEPARATOR_DO_OCR, true);
                    if (doOCR)
                    {
                        boolean forceOCR = getConfig()
                                .getConfigValB(PROP_SEPARATOR_FORCE_OCR, false);

                        LOGGER.log(Level.INFO,
                                "Will use OCR based text splitter for: {0} (findCount= {1}, ForceOCR={2})",
                                new Object[]
                                {
                                        sepStr, reqFindCount, forceOCR
                                });
                        TextSplitIdentifierOCR ocrSplitter = new TextSplitIdentifierOCR(
                                sepArr, reqFindCount, forceOCR);
                        ocrSplitter.setTesseract(ocrEng = createOCREngine());
                        ocrSplitter.setScale(getConfig()
                                .getConfigValF(PROP_OCR_IMG_SCALE, 1.0f));
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
                setStatusText("Ready");
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
            LOGGER.log(Level.ERROR, ex.getMessage(), ex);
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
    protected void addTabForDoc(PDDocument aDocument, File aFile)
    {
        PDFDocumentPanel pnl = new PDFDocumentPanel(this, aDocument, aFile);

        if (getConfig().getConfigValB(PROP_FILTER_DO_EMPTY_PAGE, true))
        {
            EmptyPageChecker epc = new EmptyPageChecker(aDocument);
            epc.setBlockCountH(Integer.valueOf(getConfig()
                    .getConfigValI(PROP_FILTER_EMPTY_PAGE_BLOCKCOUNT_H, 10)));
            epc.setBlockCountV(getConfig()
                    .getConfigValI(PROP_FILTER_EMPTY_PAGE_BLOCKCOUNT_V, 10));
            epc.setPixelFilledThreshold(getConfig()
                    .getConfigValI(PROP_FILTER_EMPTY_PAGE_TH_PIXEL, 25));
            epc.setBlockFilledThreshold(getConfig()
                    .getConfigValI(PROP_FILTER_EMPTY_PAGE_TH_BLOCK, 2));
            epc.setPageFilledThreshold(getConfig()
                    .getConfigValI(PROP_FILTER_EMPTY_PAGE_TH_PAGE, 6));

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

        JLabel lbl = new JLabel(pnl.getName(), mPdfFileIcon, JLabel.LEFT);
        lbl.setFont(mDocsPane.getFont());

        pnl.setPreviewSize(getPreviewSize());
        pnl.addPropertyChangeListener("name", (aEvt) -> {
            lbl.setText((String) aEvt.getNewValue());
        });

        Runnable r = () -> {
            int size = mDocsPane.getTabCount();
            mDocsPane.insertTab(pnl.getName(), mPdfFileIcon, pnl, pnl.getName(),
                    size);
            mDocsPane.setSelectedIndex(size);
            mDocsPane.setTabComponentAt(size, lbl);
        };

        SwingUtilities.invokeLater(r);
    }


    public void zoomOnMouseWheel(MouseWheelEvent aEvent)
    {
        if (aEvent.isControlDown())
        {
            if (aEvent.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL)
            {
                float factor = 1.0f + ((aEvent.getScrollAmount()) * 0.1f);

                Dimension oldSize = getPreviewSize();
                Dimension newSize = null;

                if (aEvent.getWheelRotation() > 0)
                {
                    // zoom out
                    newSize = new Dimension((int) (oldSize.getWidth() / factor),
                            (int) (oldSize.getHeight() / factor));
                }
                else
                {
                    // zoom in
                    newSize = new Dimension((int) (oldSize.getWidth() * factor),
                            (int) (oldSize.getHeight() * factor));
                }
                setPreviewSize(newSize);
                aEvent.consume();
            }
        }
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
                addTabForDoc(aEvent.getDocument(), aEvent.getFile());
            }
        }
    };

    private final WindowListener mWindowCloseListener = new WindowAdapter()
    {

        @Override
        public void windowClosing(WindowEvent e)
        {
            int unsaved = 0;
            for (PDFDocumentPanel docPnl : getDocumentPanels())
            {
                if (docPnl.isUnsaved())
                {
                    unsaved++;
                }
            }
            if (unsaved > 0)
            {
                String msg = I18n.getMessage(PDFSplitFrame.class,
                        "main.confirm.closeUnsaved.message", unsaved);
                String title = I18n.getMessage(PDFSplitFrame.class,
                        "main.confirm.closeUnsaved.title", unsaved);
                int res = JOptionPane.showConfirmDialog(PDFSplitFrame.this, msg,
                        title, JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (res != JOptionPane.YES_OPTION)
                {
                    // do not close
                    return;
                }
            }

            PDFSplitFrame.this.setVisible(false);
            PDFSplitFrame.this.dispose();
        }
    };

    private final DropTarget mDocPaneDropTarget = new DropTarget()
    {

        private static final long serialVersionUID = -6714739497977861416L;

        @Override
        @SuppressWarnings("unchecked")
        public synchronized void drop(DropTargetDropEvent aEvent)
        {
            try
            {
                if (aEvent.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
                {
                    aEvent.acceptDrop(DnDConstants.ACTION_COPY);
                    final List<File> droppedFiles = (List<File>) aEvent
                            .getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);
                    aEvent.dropComplete(true);
                    Thread t = new Thread(() -> {
                        for (File file : droppedFiles)
                        {
                            openPDFFile(file);
                        }
                    });
                    t.setDaemon(true);
                    t.start();
                }
                else
                {
                    aEvent.rejectDrop();
                }

            }
            catch (Exception ex)
            {
                LOGGER.log(Level.ERROR, ex.getMessage(), ex);
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
