package de.code2be.pdfsplit.ui.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * This panel is used to display a preview of the assigned PDF page.
 * 
 * @author Michael Weiss
 *
 */
public class PDFPagePanel extends JComponent
{

    private static final long serialVersionUID = -8626887910399256241L;

    private static final Logger LOGGER = System
            .getLogger(PDFPagePanel.class.getName());

    /**
     * The PDF page object.
     */
    private final PDPage mPage;

    /**
     * The index of the page.
     */
    private final int mPageIndex;

    /**
     * The parent PDF document panel.
     */
    private final PDFDocumentPanel mDocPanel;

    /**
     * The image of the page to be displayed.
     */
    private BufferedImage mPageImage;

    /**
     * A previously rendered PDF page. This is used to display the page on
     * re-scaling while the new image is being rendered.
     */
    private BufferedImage mOldPageImage;

    /**
     * A flag to indicate if the page image is currently rendering.
     */
    private boolean mRendering = false;

    /**
     * A flag to indicate if the page is enabled, not enabled pages will not be
     * included in saved documents.
     */
    private boolean mPageEnabled = true;

    /**
     * Create a new instance of a page panel.
     * 
     * @param aDocPanel
     *            the parent PDF document panel.
     * @param aPage
     *            the page to be displayed.
     * @param aPageIndex
     *            the index of the page to be displayed.
     */
    public PDFPagePanel(PDFDocumentPanel aDocPanel, PDPage aPage,
            int aPageIndex)
    {
        mDocPanel = aDocPanel;
        mPage = aPage;
        mPageIndex = aPageIndex;
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(Color.black));

        addMouseListener(mMouseListener);
        addPropertyChangeListener("preferredSize",
                (evt) -> rerenderPageImage());
    }


    /**
     * 
     * @return true if the page is flagged as enabled, false otherwise. <br/>
     *         An enabled page is saved into a target document, where as an not
     *         enabled page is removed.
     */
    public boolean isPageEnabled()
    {
        return mPageEnabled;
    }


    /**
     * Change the enabled status of the page. Changing this attribute triggers a
     * pageEnabled property change event.
     * 
     * @param aPageEnabled
     *            the new value of the enabled attribute.
     */
    public void setPageEnabled(boolean aPageEnabled)
    {
        setEnabled(aPageEnabled);
        if (isPageEnabled() != aPageEnabled)
        {
            mPageEnabled = aPageEnabled;
            firePropertyChange("pageEnabled", !aPageEnabled, aPageEnabled);
            repaint();
        }
    }


    /**
     * 
     * @return the PDF page that is assigned top this panel.
     */
    public PDPage getPage()
    {
        return mPage;
    }


    /**
     * 
     * @return the page index that is assigned to this panel.
     */
    public int getPageNumber()
    {
        return mPageIndex;
    }


    /**
     * Trigger a re rendering of the PDF page image. This method only triggers
     * the re rendering. The rendering itself is done in an asynchronous thread
     * and will likely not be finished when the method returns.
     */
    protected void rerenderPageImage()
    {
        BufferedImage curImg = mPageImage;
        mPageImage = null;
        firePropertyChange("pageImage", curImg, mPageImage);
        // we keep the old image until the new one is ready to be able to render
        // something at all.
        if (curImg != null)
        {
            mOldPageImage = mPageImage;
        }
        revalidate();
        repaint();
    }


    /**
     * This method is called in a separate thread and performs the rendering of
     * the PDF page into an image.
     */
    protected void doRenderImage()
    {
        BufferedImage img = null;
        Dimension prefSize = getPreferredSize();
        boolean successed = false;
        try
        {
            PDRectangle mediaBox = mPage.getMediaBox();
            double scaleH = mediaBox.getHeight() / prefSize.getHeight();
            double scaleW = mediaBox.getWidth() / prefSize.getWidth();

            double scale = Math.max(scaleH, scaleW);

            PDDocument pdfDoc = mDocPanel.getDocument();
            synchronized (pdfDoc)
            {
                float dpi = (float) (72.0f / scale);
                img = new PDFRenderer(pdfDoc).renderImageWithDPI(mPageIndex,
                        dpi, ImageType.RGB);
                successed = true;
            }
        }
        catch (Exception ex)
        {
            // in case of a rendering exception create a special image
            LOGGER.log(Level.ERROR, ex.getMessage(), ex);

            img = new BufferedImage(prefSize.width, prefSize.height,
                    BufferedImage.TYPE_BYTE_GRAY);
            Graphics g = img.getGraphics();
            g.setColor(Color.lightGray);
            g.fillRect(0, 0, prefSize.width, prefSize.height);
            g.setColor(Color.darkGray);
            g.setFont(g.getFont().deriveFont((float) (prefSize.height * 1.0f)));
            g.drawString("?", 0, prefSize.height);
            g.drawLine(0, 0, prefSize.width, prefSize.height);
        }
        finally
        {
            synchronized (this)
            {
                mRendering = false;

                // only keep the image if the preferred size did not change in
                // between
                if (prefSize.equals(getPreferredSize()))
                {
                    // here mPageImage is likely null but to be sure we store it
                    // for correct property change event
                    BufferedImage curImg = mPageImage;
                    mPageImage = img;
                    if (successed)
                    {
                        // clear the old image if the new one was successfully
                        // rendered.
                        mOldPageImage = null;
                    }
                    firePropertyChange("pageImage", curImg, mPageImage);
                }
                SwingUtilities.invokeLater(() -> {
                    revalidate();
                    repaint();
                });
            }
        }
    }


    /**
     * Helper method that ensures the page is already rendered, or rendering is
     * triggered.
     */
    protected void ensureRendered()
    {
        synchronized (this)
        {
            if (mPageImage != null || mRendering == true)
            {
                return;
            }
            mRendering = true;
        }

        Thread t = new Thread(() -> doRenderImage());
        t.setDaemon(true);
        t.start();
    }


    @Override
    protected void paintComponent(Graphics aG)
    {
        super.paintComponent(aG);
        Graphics g = aG.create();
        try
        {
            ensureRendered();
            int x = 0;
            int y = 0;
            int width = getWidth();
            int height = getHeight();
            g.setColor(Color.white);
            g.fillRect(0, 0, width, height);

            BufferedImage img = mPageImage != null ? mPageImage : mOldPageImage;
            if (img != null)
            {
                g.drawImage(img, x, y, width, height, this);

                if (!mPageEnabled)
                {
                    g.setColor(new Color(0, 0, 0, 20));
                    g.fillRect(0, 0, getWidth(), getHeight());
                    g.setColor(Color.black);
                    g.drawLine(0, 0, getWidth(), getHeight());
                    g.drawLine(getWidth(), 0, 0, getHeight());
                }
            }
        }
        finally
        {
            g.dispose();
        }
    }

    private final MouseListener mMouseListener = new MouseListener()
    {

        @Override
        public void mouseReleased(MouseEvent aE)
        {
            if (contains(aE.getPoint()) && SwingUtilities.isLeftMouseButton(aE))
            {
                setPageEnabled(!isPageEnabled());
            }
        }


        @Override
        public void mousePressed(MouseEvent aE)
        {
        }


        @Override
        public void mouseExited(MouseEvent aE)
        {
        }


        @Override
        public void mouseEntered(MouseEvent aE)
        {
        }


        @Override
        public void mouseClicked(MouseEvent aE)
        {
        }
    };
}
