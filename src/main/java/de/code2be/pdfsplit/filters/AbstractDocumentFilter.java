package de.code2be.pdfsplit.filters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

/**
 * An abstract class of the {@link IDocumentFilter} interface that handles
 * {@link IDocumentFilterListener} stuff.
 * 
 * @author Michael Weiss
 *
 */
public abstract class AbstractDocumentFilter implements IDocumentFilter
{

    private static final long serialVersionUID = 1600907743702158481L;

    private static final Logger LOGGER = Logger
            .getLogger(AbstractDocumentFilter.class.getName());

    /**
     * The list of registered listeners.
     */
    private List<IDocumentFilterListener> mListeners = new ArrayList<>();

    @Override
    public void addDocumentFilterListener(IDocumentFilterListener aListener)
    {
        if (aListener != null)
        {
            mListeners.add(aListener);
        }

    }


    @Override
    public boolean removeDocumentFilterListener(
            IDocumentFilterListener aListener)
    {
        return mListeners.remove(aListener);
    }


    @Override
    public List<IDocumentFilterListener> getDocumentFilterListeners()
    {
        return Collections.unmodifiableList(mListeners);
    }


    /**
     * Notify all registered listeners about an event.
     * 
     * @param aId
     *            the event ID.
     * @param aDocument
     *            the (input) document that is currently filtered.
     * @param aPage
     *            the page, the filter is currently processing.
     * @param aPageCount
     *            the number of pages in the (input) document.
     * @param aPageIndex
     *            the index of the page (in input document), currently
     *            processed.
     */
    protected void notifyEvent(int aId, PDDocument aDocument, PDPage aPage,
            int aPageCount, int aPageIndex)
    {
        notifyEvent(new DocumentFilterEvent(this, aId, aDocument, aPage,
                aPageCount, aPageIndex));
    }


    /**
     * Notify all registered listeners about an event.
     * 
     * @param aEvent
     *            the event to notify the listeners about.
     */
    protected void notifyEvent(DocumentFilterEvent aEvent)
    {
        for (IDocumentFilterListener l : mListeners)
        {
            try
            {
                l.documentFilterEvent(aEvent);
            }
            catch (Exception ex)
            {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }

}
