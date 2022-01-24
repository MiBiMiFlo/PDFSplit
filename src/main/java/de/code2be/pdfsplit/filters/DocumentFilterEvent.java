package de.code2be.pdfsplit.filters;

import java.util.EventObject;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

/**
 * A document filter event.
 * 
 * @author Michael Weiss
 *
 */
public class DocumentFilterEvent extends EventObject
{

    private static final long serialVersionUID = 7672371826294757372L;

    /**
     * The event ID of the event triggered when the filter starts a new
     * document.
     */
    public static final int EVENT_NEW_DOCUMENT = 1;

    /**
     * The event ID of the event triggered when the filter starts a new page.
     */
    public static final int EVENT_NEXT_PAGE = 2;

    /**
     * The event ID of the event triggered when the filter ignores a page.
     */
    public static final int EVENT_PAGE_IGNORED = 3;

    /**
     * The event ID of the event triggered when the filter processed a page.
     */
    public static final int EVENT_PAGE_DONE = 4;

    /**
     * The event ID of the event triggered when the filter finished a document.
     */
    public static final int EVENT_DOCUMENT_DONE = 5;

    /**
     * The event's ID.
     */
    private final int mID;

    /**
     * The document, that is filtered while the event occurred.
     */
    private final PDDocument mDocuemnt;

    /**
     * The page that is processed while the event occurred.
     */
    private final PDPage mPage;

    /**
     * The number of pages in the input document.
     */
    private final int mPageCount;

    /**
     * The index of the currently processed page in the input document.
     */
    private final int mPageIndex;

    /**
     * Create a new document event.
     * 
     * @param aSource
     *            the filter that caused the event.
     * @param aId
     *            the event ID.
     * @param aDocuemnt
     *            the (input) document that is filtered.
     * @param aPage
     *            the page that is currently processed.
     * @param aPageCount
     *            the number of pages in (input) document.
     * @param aPageIndex
     *            the index of the currently processed page in (input) document.
     */
    public DocumentFilterEvent(IDocumentFilter aSource, int aId,
            PDDocument aDocuemnt, PDPage aPage, int aPageCount, int aPageIndex)
    {
        super(aSource);
        mID = aId;
        mDocuemnt = aDocuemnt;
        mPage = aPage;
        mPageCount = aPageCount;
        mPageIndex = aPageIndex;
    }


    @Override
    public IDocumentFilter getSource()
    {
        return (IDocumentFilter) super.getSource();
    }


    /**
     * 
     * @return the event ID.
     */
    public int getID()
    {
        return mID;
    }


    /**
     * 
     * @return the (input) document that is currently processed.
     */
    public PDDocument getDocuemnt()
    {
        return mDocuemnt;
    }


    /**
     * 
     * @return the page that is currently processed.
     */
    public PDPage getPage()
    {
        return mPage;
    }


    /**
     * 
     * @return the number of pages in (input) document.
     */
    public int getPageCount()
    {
        return mPageCount;
    }


    /**
     * 
     * @return the index of the curently processed page in (input) document.
     */
    public int getPageIndex()
    {
        return mPageIndex;
    }
}
