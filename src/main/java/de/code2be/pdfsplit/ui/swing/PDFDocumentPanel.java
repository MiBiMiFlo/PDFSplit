package de.code2be.pdfsplit.ui.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import de.code2be.help.I18n;
import de.code2be.pdfsplit.PDFHelper;

public class PDFDocumentPanel extends JPanel
{

    private static final long serialVersionUID = 5228203932797387514L;

    private static final Logger LOGGER = System
            .getLogger(PDFDocumentPanel.class.getName());

    private final PDFSplitFrame mFrame;

    private final PDDocument mDocument;

    private Dimension mPageSize;

    private PDFPagesPanel mMainPanel;

    private File mFile;

    private JLabel mLblFileName;

    private final JScrollPane mScrollPane;

    private boolean mUnsaved = true;

    public PDFDocumentPanel(PDFSplitFrame aFrame, PDDocument aDocument,
            File aFile)
    {
        mFrame = aFrame;
        mDocument = aDocument;
        mFile = aFile;
        mMainPanel = new PDFPagesPanel();

        int pageWidth = 200;
        int pageHeight = (int) ((297.0d / 210.0d) * pageWidth);
        mPageSize = new Dimension(pageWidth, pageHeight);
        setLayout(new BorderLayout());

        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        pnlTop.add(mLblFileName = new JLabel(
                "File Name: " + mFile.getAbsolutePath()));
        mLblFileName.setFont(mLblFileName.getFont().deriveFont(16.0f));

        add(pnlTop, BorderLayout.NORTH);
        add(mScrollPane = new JScrollPane(mMainPanel), BorderLayout.CENTER);
        mScrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mScrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mScrollPane.addMouseWheelListener((aE) -> {

            mFrame.zoomOnMouseWheel(aE);
            if (!aE.isConsumed())
            {
                mScrollPane.getParent().dispatchEvent(aE);
            }

        });
        refillPages();
        updateStatusLabel();
    }


    protected void updateStatusLabel()
    {
        StringBuilder sb = new StringBuilder("<html>");
        sb.append(I18n.getMessage(PDFDocumentPanel.class,
                "main.fileInfo.fileName",
                (mFile != null) ? mFile.getAbsolutePath() : ""));
        List<PDFPagePanel> pages = getPagePanels();
        int enabled = 0;
        for (PDFPagePanel page : pages)
        {
            if (page.isPageEnabled())
            {
                enabled++;
            }
        }
        sb.append("<br/>").append(I18n.getMessage(PDFDocumentPanel.class,
                "main.fileInfo.pages", enabled, pages.size()));

        SwingUtilities.invokeLater(() -> {
            mLblFileName.setText(sb.toString());
        });
    }


    public PDDocument getDocument()
    {
        return mDocument;
    }


    public void setPreviewSize(Dimension aSize)
    {
        mMainPanel.setPreviewSize(aSize);
    }


    public File getFile()
    {
        return mFile;
    }


    public void setFile(File aFile)
    {
        String oldName = getName();
        mFile = aFile;
        String newName = getName();
        mLblFileName.setText("File Name: " + mFile.getAbsolutePath());
        firePropertyChange("name", oldName, newName);
    }


    public boolean isUnsaved()
    {
        return mUnsaved;
    }


    public void setUnsaved(boolean aUnsaved)
    {
        if (mUnsaved != aUnsaved)
        {
            String oldName = getName();
            mUnsaved = aUnsaved;
            String newName = getName();
            firePropertyChange("name", oldName, newName);
        }
    }


    public void save()
    {
        if (mFile == null)
        {
            JOptionPane.showMessageDialog(this,
                    "Save wothout file is not implemented for PDFDocumentPanel!",
                    "ERROR: Not implemented.", JOptionPane.ERROR_MESSAGE);
        }
        try
        {
            PDDocument newDoc = PDFHelper.createNewDocument(null, mDocument);

            for (PDFPagePanel p : getPagePanels())
            {
                if (p.isPageEnabled())
                {
                    PDFHelper.importPage(newDoc, p.getPage());
                }
            }
            newDoc.save(mFile);
            setUnsaved(false);
        }
        catch (Exception ex)
        {
            LOGGER.log(Level.ERROR, ex.getMessage(), ex);
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "ERROR: Can not save to file", JOptionPane.ERROR_MESSAGE);
        }
    }


    public void saveAs(File aFile)
    {
        setFile(aFile);
        save();
    }


    @Override
    public String getName()
    {
        StringBuilder sb = new StringBuilder();
        if (isUnsaved())
        {
            sb.append("*");
        }
        if (mFile != null)
        {
            sb.append(mFile.getName());
        }
        else
        {
            sb.append(super.getName());
        }
        return sb.toString();
    }


    public int getPageCount()
    {
        return mDocument.getNumberOfPages();
    }


    public int getEnabledPageCount()
    {
        int res = 0;
        for (Component c : mMainPanel.getComponents())
        {
            if (c instanceof PDFPagePanel)
            {
                if (((PDFPagePanel) c).isPageEnabled())
                {
                    res++;
                }
            }
        }
        return res;
    }


    public List<PDFPagePanel> getPagePanels()
    {
        List<PDFPagePanel> res = new ArrayList<>();
        for (Component c : mMainPanel.getComponents())
        {
            if (c instanceof PDFPagePanel)
            {
                res.add((PDFPagePanel) c);
            }
        }
        return res;
    }


    protected void refillPages()
    {
        for (Component c : mMainPanel.getComponents())
        {
            if (c instanceof PDFPagePanel)
            {
                mMainPanel.remove(c);
                ((PDFPagePanel) c).removePropertyChangeListener("enabled",
                        mPageEnabledChanged);
            }
        }
        int pnum = 0;
        for (PDPage page : mDocument.getPages())
        {
            PDFPagePanel pp = new PDFPagePanel(this, page, pnum);
            // pp.addMouseWheelListener((aE) -> mFrame.zoomOnMouseWheel(aE));
            pp.setPreferredSize(mPageSize);
            pp.addPropertyChangeListener("enabled", mPageEnabledChanged);
            mMainPanel.add(pp);
            pnum++;
        }
    }

    private final PropertyChangeListener mPageEnabledChanged = new PropertyChangeListener()
    {

        @Override
        public void propertyChange(PropertyChangeEvent aEvt)
        {
            updateStatusLabel();
            setUnsaved(true);
        }
    };
}
