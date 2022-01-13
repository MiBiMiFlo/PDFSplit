package de.code2be.pdfsplit;

import java.util.EventObject;

public class SplitStatusEvent extends EventObject
{

    private static final long serialVersionUID = -6821032865864106770L;

    private final int mPageCount;

    private final int mCurrentPage;

    private final int mID;

    private final int mDocumentCount;

    public SplitStatusEvent(SmartSplitter aSplitter, int aID, int aPageCount,
            int aCurrentPage, int aDocumentCount)
    {
        super(aSplitter);
        mID = aID;
        mPageCount = aPageCount;
        mCurrentPage = aCurrentPage;
        mDocumentCount = aDocumentCount;
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


    public int getID()
    {
        return mID;
    }


    public int getDocumentCount()
    {
        return mDocumentCount;
    }

}
