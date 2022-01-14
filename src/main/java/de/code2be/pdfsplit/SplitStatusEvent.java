package de.code2be.pdfsplit;

import java.util.EventObject;

import org.apache.pdfbox.pdmodel.PDDocument;

public class SplitStatusEvent extends EventObject
{

    private static final long serialVersionUID = -6821032865864106770L;

    public static final int EVENT_SPLITTING_STARTED = 1;

    public static final int EVENT_NEXT_PAGE = 2;

    public static final int EVENT_NEW_DOCUMENT = 3;

    public static final int EVENT_DOCUMENT_FINISHED = 4;

    public static final int EVENT_SPLITTING_FINISHED = 4;

    private final int mPageCount;

    private final int mCurrentPage;

    private final int mID;

    private final int mDocumentCount;

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


    public int getID()
    {
        return mID;
    }


    public PDDocument getDocument()
    {
        return mDocument;
    }


    @Override
    public SmartSplitter getSource()
    {
        return (SmartSplitter) super.getSource();
    }


    public int getPageCount()
    {
        return mPageCount;
    }


    public int getCurrentPage()
    {
        return mCurrentPage;
    }


    public int getDocumentCount()
    {
        return mDocumentCount;
    }

}
