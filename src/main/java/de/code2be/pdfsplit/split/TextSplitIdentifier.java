package de.code2be.pdfsplit.split;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * A {@link ISplitPageIdentifier} that searches the text within a document page
 * for a special split text.
 * 
 * @author Michael Weiss
 *
 */
public class TextSplitIdentifier implements ISplitPageIdentifier
{

    private static final long serialVersionUID = 4566878022203192763L;

    private static final Logger LOGGER = Logger
            .getLogger(TextSplitIdentifier.class.getName());

    /**
     * The list of possible text elements that identify a page as split page.
     */
    private final String[] mSplitTextArr;

    /**
     * The number of text elements from {@link #mSplitTextArr} that must match
     * to identify a page as split page.
     */
    private final int mRequiredCount;

    /**
     * Create a new instance.
     * 
     * @param aSplitTexts
     *            the text elements that identify a page as split page.
     * @param aRequiredCount
     *            the number of text elements that must match to identify a page
     *            as split page. This can be a number from 1 (at least one text
     *            element must match) to the number of text elements (all must
     *            match). This parameter allows to define something like 3 of 6
     *            possible text elements must match and therefore allows some
     *            errors in OCR.
     */
    public TextSplitIdentifier(String[] aSplitTexts, int aRequiredCount)
    {
        if (aSplitTexts == null)
        {
            throw new IllegalArgumentException(
                    "Parameter aSplitTexts must not be null!");
        }
        if (aSplitTexts.length == 0)
        {
            throw new IllegalArgumentException(
                    "Parameter aSplitTexts must contain at least one element!");
        }
        if (aRequiredCount < 1 || aRequiredCount > aSplitTexts.length)
        {
            throw new IllegalArgumentException(
                    "Parameter aRequiredCount is invalid. Expected 0 < x <= "
                            + aSplitTexts.length + " but is: "
                            + aRequiredCount);
        }
        mSplitTextArr = aSplitTexts;
        mRequiredCount = aRequiredCount;
    }


    /**
     * 
     * @return the number of text element matches that qualify a page to be a
     *         split page.
     */
    public int getRequiredCount()
    {
        return mRequiredCount;
    }


    /**
     * 
     * @return a copy of the split text element array.
     */
    public String[] getSplitTextArr()
    {
        String[] res = new String[mSplitTextArr.length];
        System.arraycopy(mSplitTextArr, 0, res, 0, mSplitTextArr.length);
        return res;
    }


    /**
     * Retrieve all text of the page with
     * 
     * @param aPageNumber
     *            the page index (first page is index 0).
     * @return the text of the given page or an empty string if text can not be
     *         retrieved.
     */
    protected String getTextofPage(PDDocument aDocument, PDPage aPage,
            int aPageIndex)
    {
        try
        {
            synchronized (aDocument)
            {
                PDFTextStripper ts = new PDFTextStripper();
                ts.setStartPage(aPageIndex + 1);
                ts.setEndPage(aPageIndex + 1);
                String text = ts.getText(aDocument);
                return text;
            }
        }
        catch (IOException ex)
        {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            return "";
        }
    }


    @Override
    public boolean isSplitPage(PDDocument aDocument, PDPage aPage,
            int aPageIndex)
        throws Exception
    {
        String text = getTextofPage(aDocument, aPage, aPageIndex);

        int found = 0;
        for (String txt : mSplitTextArr)
        {
            if (text.contains(txt))
            {
                found++;
                if (found >= getRequiredCount())
                {
                    // enough found --> we can stop and return true
                    return true;
                }
            }
        }
        // not enough found
        if (found > 0 && LOGGER.isLoggable(Level.FINE))
        {
            LOGGER.log(Level.FINE,
                    "Found {0} of {1} split texts on page {3}. Required would be {4}",
                    new Object[]
                    {
                            found, mSplitTextArr.length, aPageIndex,
                            mRequiredCount
                    });
        }
        return false;
    }

}
