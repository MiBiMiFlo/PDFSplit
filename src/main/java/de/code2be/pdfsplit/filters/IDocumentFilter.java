package de.code2be.pdfsplit.filters;

import java.io.Serializable;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * Interface for filters that some how process a document end enhance / change
 * it according to the filter implementation.<br/>
 * Possible implementations are:
 * <ul>
 * <li>Apply OCR to scanned (image only) pages and add real text
 * <li>Filter empty pages
 * <li>Flag pages
 * <li>Update document properties
 * <li>...
 * </ul>
 * 
 * @author Michael Weiss
 *
 */
public interface IDocumentFilter extends Serializable
{

    /**
     * Filter the given document.
     * 
     * @param aDocument
     *            the document to filter.
     * @return the filtered document. This might be the same as the input
     *         document or a new one, depending on the implementation.
     */
    PDDocument filter(PDDocument aDocument);


    /**
     * Add a listener to this filter. The listeners are informed on important
     * points in the filter. This can be used for slow filters to provide user
     * feedback.
     * 
     * @param aListener
     *            the listener to be added. Null values are allowed, but ignored
     *            in the call.
     */
    void addDocumentFilterListener(IDocumentFilterListener aListener);


    /**
     * Remove a previously added listener.
     * 
     * @param aListener
     *            the listener to be removed.
     * @return true if the listener waas removed, false otherwise.
     */
    boolean removeDocumentFilterListener(IDocumentFilterListener aListener);


    /**
     * 
     * @return a read-only list of registered listeners.
     */
    List<IDocumentFilterListener> getDocumentFilterListeners();

}
