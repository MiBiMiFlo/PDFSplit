package de.code2be.pdfsplit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * This is an adapted version of the
 * #{@link org.apache.pdfbox.multipdf.Splitter} that allows to split a big
 * document on split pages identified by special strings.
 * 
 * @author Michael Weiss
 *
 */
public class SmartSplitter
{

    private static final Logger LOGGER = Logger
            .getLogger(SmartSplitter.class.getName());

    private PDDocument mSourceDoc;

    private PDDocument mTargetDoc;

    private int mStartPage = Integer.MIN_VALUE;

    private int mEndPage = Integer.MAX_VALUE;

    private List<PDDocument> mTargetDocs;

    private int mCurrentPage;

    private MemoryUsageSetting mMemoryUsageSetting = null;

    private String[] mSplitTextArr;

    private ISplitStatusListener mListener;

    private volatile boolean mDoAbort = false;

    private QRCodeExtractor mQRCodeExtractor;

    /**
     * 
     * @return the number of target documents currently available.
     */
    public int getDocumentCount()
    {
        return mTargetDocs != null ? mTargetDocs.size() : 0;
    }


    public void setStatusListener(ISplitStatusListener aListener)
    {
        mListener = aListener;
    }


    protected void sendStatusUpdate(int aID, PDDocument aDocument)
    {
        if (mListener == null)
        {
            return;
        }

        SplitStatusEvent evt = new SplitStatusEvent(this, aID,
                mSourceDoc.getNumberOfPages(), mCurrentPage,
                mTargetDocs != null ? mTargetDocs.size() : 0, aDocument);

        mListener.splitStatusUpdate(evt);
    }


    public void doAbort()
    {
        mDoAbort = true;
    }


    public MemoryUsageSetting getMemoryUsageSetting()
    {
        return mMemoryUsageSetting;
    }


    public void setMemoryUsageSetting(MemoryUsageSetting aMemoryUsageSetting)
    {
        mMemoryUsageSetting = aMemoryUsageSetting;
    }


    public List<PDDocument> split(PDDocument aDocument, String aSplitText)
        throws IOException
    {
        mCurrentPage = 0;
        mTargetDocs = new ArrayList<PDDocument>();
        mTargetDoc = null;
        mSourceDoc = aDocument;
        mSplitTextArr = aSplitText.split("\\s+");
        processPages();
        return mTargetDocs;
    }


    /**
     * Set the index of the first page to process.
     * 
     * @param aStartPage
     *            the index of the first page to process. The first page has
     *            index 1.
     */
    public void setStartPage(int aStartPage)
    {
        if (aStartPage < 0)
        {
            throw new IllegalArgumentException(
                    "Start page is not allowed to be negative.");
        }
        mStartPage = aStartPage;
    }


    /**
     * Set the index of the first page to not process anymore.
     * 
     * @param aEnd
     *            the index of the first page to not process anymore. If this is
     *            5 than pages 0-4 are included (5 pages) but page 5 (6'th page
     *            is not included anymore).
     */
    public void setEndPage(int aEnd)
    {
        if (aEnd < 0)
        {
            throw new IllegalArgumentException(
                    "End page is not allowed to be negative");
        }
        mEndPage = aEnd;
    }


    protected void processPages() throws IOException
    {
        sendStatusUpdate(SplitStatusEvent.EVENT_SPLITTING_STARTED, mSourceDoc);

        int startPage = Math.max(mStartPage, 0);
        int endPage = Math.min(mEndPage, mSourceDoc.getNumberOfPages());

        for (int pageIdx = startPage; pageIdx < endPage; pageIdx++)
        {
            if (mDoAbort)
            {
                // abort processing.
                break;
            }
            mCurrentPage = pageIdx;
            if (isSplitPage(mCurrentPage))
            {
                // current page contains the split text --> end of previous
                // document (this page is dropped)
                if (mTargetDoc != null)
                {
                    sendStatusUpdate(SplitStatusEvent.EVENT_DOCUMENT_FINISHED,
                            mTargetDoc);
                }
                mTargetDoc = null;
            }
            else
            {
                // not a split page --> include into target document
                if (mTargetDoc == null)
                {
                    // no active target document --> create a new target
                    // document
                    mTargetDocs.add(mTargetDoc = PDFHelper.createNewDocument(
                            getMemoryUsageSetting(), getSourceDocument()));
                    sendStatusUpdate(SplitStatusEvent.EVENT_NEW_DOCUMENT,
                            mTargetDoc);
                }

                // import the page into the new target document
                PDFHelper.importPage(getTargetDocument(),
                        mSourceDoc.getPage(pageIdx));
                sendStatusUpdate(SplitStatusEvent.EVENT_NEXT_PAGE, mSourceDoc);
            }
        }
        if (mTargetDoc != null)
        {
            sendStatusUpdate(SplitStatusEvent.EVENT_DOCUMENT_FINISHED,
                    mTargetDoc);
        }

        sendStatusUpdate(SplitStatusEvent.EVENT_SPLITTING_FINISHED, mSourceDoc);
        mTargetDoc = null;
    }


    /**
     * Retrieve all text of the page with
     * 
     * @param aPageNumber
     *            the page index (first page is index 0).
     * @return the text of the given page or an empty string if text can not be
     *         retrieved.
     */
    protected String getTextofPage(int aPageNumber)
    {
        PDFTextStripper reader;
        try
        {
            reader = new PDFTextStripper();
            reader.setStartPage(aPageNumber + 1);
            reader.setEndPage(aPageNumber + 1);
            return reader.getText(getSourceDocument());
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        return "";
    }


    /**
     * Check if the given page contains the defined split text.
     * 
     * @param page
     *            the page to check for the defined split text.
     * @return true if the split text was found, false otherwise.
     */
    protected boolean isSplitPage(PDPage page)
    {
        // TODO: map from given page to index
        return isSplitPage(mCurrentPage);
    }


    protected boolean isSplitPage(int aPageIndex)
    {
        String text = getTextofPage(aPageIndex);
        boolean textFound = true;
        for (String txt : mSplitTextArr)
        {
            if (!text.contains(txt))
            {
                textFound = false;
            }
        }
        if (textFound)
        {
            return true;
        }

        // if (text == null || text.trim().length() == 0)
        {
            if (mQRCodeExtractor == null)
            {
                mQRCodeExtractor = new QRCodeExtractor(mSourceDoc);
            }
            try
            {
                List<String> qrCodes = mQRCodeExtractor
                        .extractQRCodesFrom(aPageIndex);
                for (String qrCode : qrCodes)
                {
                    if (qrCode.equals("https://github.com/MiBiMiFlo/PDFSplit"))
                    {
                        return true;
                    }
                }
            }
            catch (IOException ex)
            {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }

        return false;
    }


    /**
     * 
     * @return the current source document. This is the docuemnt that is
     *         currently processed or was processed last.
     */
    protected final PDDocument getSourceDocument()
    {
        return mSourceDoc;
    }


    /**
     * The current destination document. This is the document that get's the
     * next page assigned.
     * 
     * @return the current target document.
     */
    protected final PDDocument getTargetDocument()
    {
        return mTargetDoc;
    }


    public List<PDDocument> getTargetDocuments()
    {
        return new ArrayList<>(mTargetDocs);
    }
}
