package de.code2be.pdfsplit.ui.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

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

    private final PDPage mPage;

    private final int mPageIndex;

    private BufferedImage mPageImage;

    private boolean mRendering = false;

    private final PDFDocumentPanel mDocPanel;

    private boolean mEnabled = true;

    private long mImageValue = -1;

    private boolean mCalcImageValue = false;

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


    public long getImageValue()
    {
        return mImageValue;
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
        repaint();
    }


    protected long calcImageValue(BufferedImage aImage)
    {
        long imgVal = 0;
        int imgWidth = aImage.getWidth();
        int imgHeight = aImage.getHeight();
        for (int y = 0; y < imgHeight; y++)
        {
            long lineVal = 0;
            for (int x = 0; x < imgWidth; x++)
            {
                int val = aImage.getRGB(x, y);
                lineVal += (val & 0xFF);
                lineVal += ((val >> 8) & 0xFF);
                lineVal += ((val >> 16) & 0xFF);
            }
            imgVal += (lineVal / 3);
            // TOOD: check if dark enought to stop now
        }
        long pxCount = (long) imgWidth * (long) imgHeight;
        imgVal = imgVal / pxCount;
        return imgVal;
    }


    protected void doRenderImage()
    {
        BufferedImage img = null;
        Dimension prefSize = getPreferredSize();
        long imgVal = 0;
        try
        {
            PDRectangle mediaBox = mPage.getMediaBox();
            double scaleH = mediaBox.getHeight() / prefSize.getHeight();
            double scaleW = mediaBox.getWidth() / prefSize.getWidth();

            double scale = Math.max(scaleH, scaleW);

            PDFRenderer rdr = new PDFRenderer(mDocPanel.getDocument());

            img = rdr.renderImageWithDPI(mPageIndex, (float) (72.0d / scale));

            if (mCalcImageValue)
            {
                imgVal = calcImageValue(img);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            img = new BufferedImage(prefSize.width, prefSize.height,
                    BufferedImage.TYPE_BYTE_GRAY);
            Graphics g = img.getGraphics();
            g.drawLine(0, 0, prefSize.width, prefSize.height);

        }
        finally
        {
            synchronized (this)
            {
                mPageImage = img;
                mRendering = false;
                mImageValue = imgVal;
                SwingUtilities.invokeLater(() -> repaint());
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
            if (SwingUtilities.isLeftMouseButton(aE))
            {
                setPageEnabled(!isPageEnabled());
            }
        }
    };
}
