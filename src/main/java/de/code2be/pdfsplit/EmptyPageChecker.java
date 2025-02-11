package de.code2be.pdfsplit;

import java.awt.image.BufferedImage;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * A simple helper class that checks if the given page is empty.
 * 
 * @author Michael Weiss
 *
 */
public class EmptyPageChecker
{

    private static final Logger LOGGER = System
            .getLogger(EmptyPageChecker.class.getName());

    /**
     * The threshold value (in %) used to identify a pixel as filled. Range is
     * from 0 (white) to 100 (full black) so the threshold should be between
     * these values.
     */
    private int mPixelFilledThreshold = 25;

    /**
     * A threshold that marks a block as filled if at least the given amount of
     * pixels (in %) is filled.
     */
    private int mBlockFilledThreshold = 2;

    /**
     * The threshold that marks a page as filled if at least the given number of
     * blocks are rated filled.
     */
    private int mPageFilledThreshold = 6;

    /**
     * The number of horizontal blocks to split a page into.
     */
    private int mBlockCountH = 10;

    /**
     * The number of vertical blocks to split a page into.
     */
    private int mBlockCountV = 10;

    /**
     * The document pages are checked from.
     */
    private final PDDocument mDocument;

    /**
     * The renderer used generating images to be checked.
     */
    private PDFRenderer mRenderer = null;

    /**
     * Create a new instance for the given document.
     * 
     * @param aDocument
     *            the document to check pages from.
     */
    public EmptyPageChecker(PDDocument aDocument)
    {
        mDocument = aDocument;
    }


    /**
     * 
     * @param aPixelFilledThreshold
     *            The threshold value (in %) used to identify a pixel as filled.
     *            Range is from 0 (white) to 100 (full black).
     */
    public void setPixelFilledThreshold(int aPixelFilledThreshold)
    {
        mPixelFilledThreshold = aPixelFilledThreshold;
    }


    /**
     * 
     * @return The threshold value (in %) used to identify a pixel as filled.
     *         Range is from 0 (white) to 100 (full black).
     */
    public int getPixelFilledThreshold()
    {
        return mPixelFilledThreshold;
    }


    /**
     * 
     * @param aBlockFilledThreshold
     *            the threshold value (in %) for a block that needs to be passed
     *            to count the block as filled.
     */
    public void setBlockFilledThreshold(int aBlockFilledThreshold)
    {
        mBlockFilledThreshold = aBlockFilledThreshold;
    }


    /**
     * 
     * @return the threshold value (in %) for a block that needs to be passed to
     *         count the block as filled.
     */
    public int getBlockFilledThreshold()
    {
        return mBlockFilledThreshold;
    }


    /**
     * 
     * @param aPageFilledThreshold
     *            the threshold that need to be passed to consider a page as
     *            filled.
     */
    public void setPageFilledThreshold(int aPageFilledThreshold)
    {
        mPageFilledThreshold = aPageFilledThreshold;
    }


    /**
     * 
     * @return the current threshold that need to be passed to consider a page
     *         as filled.
     */
    public int getPageFilledThreshold()
    {
        return mPageFilledThreshold;
    }


    /**
     * 
     * @param aBlockCountH
     *            the number of blocks to be created in horizontal direction.
     */
    public void setBlockCountH(int aBlockCountH)
    {
        mBlockCountH = aBlockCountH;
    }


    /**
     * 
     * @return the number of blocks to be created in horizontal direction.
     */
    public int getBlockCountH()
    {
        return mBlockCountH;
    }


    /**
     * 
     * @param aBlockCountV
     *            the number of blocks to be created in vertical direction.
     */
    public void setBlockCountV(int aBlockCountV)
    {
        mBlockCountV = aBlockCountV;
    }


    /**
     * 
     * @return the number of blocks to be created in vertical direction.
     */
    public int getBlockCountV()
    {
        return mBlockCountV;
    }


    /**
     * Check if the block for the given buffer is rated filled.
     * 
     * @param aBuffer
     *            the buffer to check.
     * @return true if the buffer shows that the block is filled, false
     *         otherwise.
     */
    protected boolean isBlockFilled(int[] aBuffer)
    {
        int pxCount = aBuffer.length / 3;

        int pixelThreshold = (mPixelFilledThreshold * 765) / 100;
        int blockThreshold = mBlockFilledThreshold * pxCount / 100;
        int pxFilled = 0;
        for (int idx = 0; idx < aBuffer.length; idx += 3)
        {
            if ((765 - aBuffer[idx] - aBuffer[idx + 1]
                    - aBuffer[idx + 2]) > pixelThreshold)
            {
                pxFilled++;
                if (pxFilled > blockThreshold)
                {
                    return true;
                }
            }
        }

        return false;
    }


    /**
     * Check if a given page is empty.
     * 
     * @param aPage
     *            the page to check.
     * @param aPageIndex
     *            the index of the page to check.
     * @return true if the page is rated empty based on configured thresholds,
     *         false otherwise.
     */
    public boolean isPageEmpty(PDPage aPage, int aPageIndex)
    {
        if (mDocument == null)
        {
            LOGGER.log(Level.ERROR, "Called with null document!");
            return false;
        }

        try
        {
            BufferedImage img;
            synchronized (mDocument)
            {
                if (mRenderer == null)
                {
                    mRenderer = new PDFRenderer(mDocument);
                }
                img = mRenderer.renderImage(aPageIndex);
            }

            int pW = img.getWidth();
            int pH = img.getHeight();
            int bW = pW / mBlockCountH;
            int bH = pH / mBlockCountV;
            int pixelCount = (bW * bH);
            int[] buffer = new int[pixelCount * 3];
            int sumBlocks = 0;

            for (int block_w = 0; block_w < 10; block_w++)
            {
                for (int block_h = 0; block_h < 10; block_h++)
                {
                    buffer = img.getRaster().getPixels(block_w * bW,
                            block_h * bH, bW, bH, (int[]) buffer);
                    if (isBlockFilled(buffer))
                    {
                        sumBlocks++;
                        if (sumBlocks >= mPageFilledThreshold)
                        {
                            return false;
                        }
                    }
                }

            }
            return true;
        }
        catch (Exception ex)
        {
            LOGGER.log(Level.ERROR, ex.getMessage(), ex);
            // can not say that page is empty
            return false;
        }
    }

}
