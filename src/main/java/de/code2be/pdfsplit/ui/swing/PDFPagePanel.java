package de.code2be.pdfsplit.ui.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
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

    private static final Logger LOGGER = Logger
            .getLogger(PDFPagePanel.class.getName());

    private final PDPage mPage;

    private final int mPageIndex;

    private BufferedImage mPageImage;

    private boolean mRendering = false;

    private final PDFDocumentPanel mDocPanel;

    private boolean mEnabled = true;

    public PDFPagePanel(PDFDocumentPanel aDocPanel, PDPage aPage,
            int aPageIndex)
    {
        mDocPanel = aDocPanel;
        mPage = aPage;
        mPageIndex = aPageIndex;
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(Color.black));

        addMouseListener(mMouseListener);
    }


    @Override
    public void setPreferredSize(Dimension aPreferredSize)
    {
        super.setPreferredSize(aPreferredSize);
        rerenderPageImage();
    }


    public boolean isPageEnabled()
    {
        return mEnabled;
    }


    public void setPageEnabled(boolean aPageEnabled)
    {
        if (isPageEnabled() != aPageEnabled)
        {
            mEnabled = aPageEnabled;
            firePropertyChange("enabled", !aPageEnabled, aPageEnabled);
            repaint();
        }
    }


    public PDPage getPage()
    {
        return mPage;
    }


    public int getPageNumber()
    {
        return mPageIndex;
    }


    protected void rerenderPageImage()
    {
        mPageImage = null;
        revalidate();
        repaint();
    }


    protected void doRenderImage()
    {
        BufferedImage img = null;
        Dimension prefSize = getPreferredSize();
        try
        {
            PDRectangle mediaBox = mPage.getMediaBox();
            double scaleH = mediaBox.getHeight() / prefSize.getHeight();
            double scaleW = mediaBox.getWidth() / prefSize.getWidth();

            double scale = Math.max(scaleH, scaleW);

            PDFRenderer rdr = new PDFRenderer(mDocPanel.getDocument());

            img = rdr.renderImageWithDPI(mPageIndex, (float) (72.0d / scale));

        }
        catch (Exception ex)
        {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);

            img = new BufferedImage(prefSize.width, prefSize.height,
                    BufferedImage.TYPE_BYTE_GRAY);
            Graphics g = img.getGraphics();
            g.drawLine(0, 0, prefSize.width, prefSize.height);

        }
        finally
        {
            synchronized (this)
            {
                mRendering = false;
                if (prefSize.equals(getPreferredSize()))
                {
                    mPageImage = img;
                }
                SwingUtilities.invokeLater(() -> {
                    revalidate();
                    repaint();
                });
            }
        }
    }


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

            if (mPageImage != null)
            {
                if (mPageImage.getWidth() > width
                        || mPageImage.getHeight() > height)
                {
                    // TODO: ensure scaling is correct on painting
                    g.drawImage(mPageImage, x, y, width, height, this);
                }
                else
                {
                    g.drawImage(mPageImage, x, y, mPageImage.getWidth(),
                            mPageImage.getHeight(), this);
                }

                if (!mEnabled)
                {
                    g.setColor(Color.black);
                    g.drawLine(0, 0, getWidth(), getHeight());
                    g.drawLine(getWidth(), 0, 0, getHeight());
                    g.setColor(new Color(0, 0, 0, 20));
                    g.fillRect(0, 0, getWidth(), getHeight());
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
