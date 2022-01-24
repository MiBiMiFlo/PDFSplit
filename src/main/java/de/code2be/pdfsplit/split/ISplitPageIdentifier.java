package de.code2be.pdfsplit.split;

import java.io.Serializable;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

/**
 * Interface for classes that can identify a split page in a PDF document.
 * 
 * @author Michael Weiss
 *
 */
public interface ISplitPageIdentifier extends Serializable
{

    /**
     * Check if the page with given page index is a split page or not.
     * 
     * @param aDocument
     *            the document, the page is in.
     * @param aPage
     *            the page to check.
     * @param aPageIndex
     *            the index of the page to check (starting at 0).
     * @return true if the page with given index is identified as split page,
     *         false otherwise.
     * @throws Exception
     *             on internal exceptions
     */
    boolean isSplitPage(PDDocument aDocument, PDPage aPage, int aPageIndex)
        throws Exception;
}
