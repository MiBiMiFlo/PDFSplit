package de.code2be.pdfsplit;

import java.util.EventObject;

import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * The event class for event objects that are caused by the
 * {@link SmartSplitter} for status changes.
 * 
 * @author Michael Weiss
 *
 */
public class SplitStatusEvent extends EventObject
{

    private static final long serialVersionUID = -6821032865864106770L;

    /**
     * The id of the event that signals a new splitting was started.
     */
    public static final int EVENT_SPLITTING_STARTED = 1;

    /**
     * The id of the event that signals a new page is processed.
     */
    public static final int EVENT_NEXT_PAGE = 2;

    /**
     * The id of the event that signals a new target document is started. This
     * is called before first page is processed in each target document.
     */
    public static final int EVENT_NEW_DOCUMENT = 3;

    /**
     * The id of the event that signals a target document is finished. This is
     * called either when a split page is determined or after all pages are
     * processed and a last target document has at least one page.
     */
    public static final int EVENT_DOCUMENT_FINISHED = 4;

    /**
     * The id of the event that signals splitting is finished. This is called
     * after all pages are processed.
     */
    public static final int EVENT_SPLITTING_FINISHED = 5;

    /**
     * The id of the event.
     */
    private final int mID;

    /**
     * The number of pages in the source document.
     */
    private final int mPageCount;

    /**
     * The currently processed page.
     */
    private final int mCurrentPage;

    /**
     * The number of target documents already opened.
     */
    private final int mDocumentCount;

    /**
     * The document that is attached to this event. This either is the source
     * document for ({@link #EVENT_SPLITTING_STARTED}, {@link #EVENT_NEXT_PAGE}
     * and {@link #EVENT_SPLITTING_FINISHED} or the target document for
     * {@link #EVENT_NEW_DOCUMENT}, {@link #EVENT_DOCUMENT_FINISHED} and
     * {@link #EVENT_NEXT_PAGE}.
     */
    private final PDDocument mDocument;

    public SplitStatusEvent(SmartSplitter aSplitter, int aID, int aPageCount,
            int aCurrentPage, int aDocumentCount, PDDocument aDocument)
    {
        super(aSplitter);
        mID = aID;
        mPageCount = aPageCount;
        mCurrentPage = aCurrentPage;
        mDocumentCount = aDocumentCount;
        mDocument = aDocument;
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
     * @return the document that is attached to this event. <br/>
     *         This either is the source document for
     *         ({@link #EVENT_SPLITTING_STARTED}, {@link #EVENT_NEXT_PAGE} and
     *         {@link #EVENT_SPLITTING_FINISHED} or the target document for
     *         {@link #EVENT_NEW_DOCUMENT}, {@link #EVENT_DOCUMENT_FINISHED} and
     *         {@link #EVENT_NEXT_PAGE}.
     */
    public PDDocument getDocument()
    {
        return mDocument;
    }


    /**
     * @return the splitter that triggered the event.
     */
    @Override
    public SmartSplitter getSource()
    {
        return (SmartSplitter) super.getSource();
    }


    /**
     * 
     * @return the number of pages in source document (document currently being
     *         split).
     */
    public int getPageCount()
    {
        return mPageCount;
    }


    /**
     * 
     * @return the page number in source document (document currently being
     *         split).
     */
    public int getCurrentPage()
    {
        return mCurrentPage;
    }


    /**
     * 
     * @return the number of target documents. This is the number of documents
     *         the source document is already split into.
     */
    public int getDocumentCount()
    {
        return mDocumentCount;
    }

}
