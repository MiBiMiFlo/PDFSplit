package de.code2be.pdfsplit;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import de.code2be.pdfsplit.split.ISplitPageIdentifier;

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

    /**
     * The document that is to be split.
     */
    private PDDocument mSourceDoc;

    /**
     * The index of the first page in {@link #mSourceDoc} to process. The first
     * possible page is index 0.
     */
    private int mStartPage = Integer.MIN_VALUE;

    /**
     * The index of the first page to not process anymore. If
     * {@link #mStartPage} is 0, this is the number of pages to process.
     */
    private int mEndPage = Integer.MAX_VALUE;

    /**
     * The list of target documents, the {@link #mSourceDoc} was already split
     * into, This also contains {@link #mTargetDoc}.
     */
    private List<PDDocument> mTargetDocs;

    /**
     * The index of the page that is currently processed.
     */
    private int mCurrentPage;

    /**
     * The memory usage settings, that are to be applied for new documents.
     */
    private MemoryUsageSetting mMemoryUsageSetting = null;

    /**
     * A flag to indicate if abort was requested. If this is set to true, the
     * split process is aborted on next page.
     */
    private volatile boolean mAbort = false;

    /**
     * The list of registered {@link ISplitStatusListener}'s.
     */
    private final List<ISplitStatusListener> mListeners = new ArrayList<>();

    /**
     * The list of registered {@link ISplitPageIdentifier}'s.
     */
    private final List<ISplitPageIdentifier> mSplitPageIdentifiers = new ArrayList<>();

    private File mTargetDirectory;

    private String mNamePattern;

    public File getTargetDirectory()
    {
        return mTargetDirectory;
    }


    public void setTargetDirectory(File aTargetDirectory)
    {
        mTargetDirectory = aTargetDirectory;
    }


    public String getNamePattern()
    {
        return mNamePattern;
    }


    public void setNamePattern(String aNamePattern)
    {
        mNamePattern = aNamePattern;
    }


    /**
     * 
     * @return the number of target documents currently available.
     */
    public int getTargetDocumentCount()
    {
        return mTargetDocs != null ? mTargetDocs.size() : 0;
    }


    /**
     * 
     * @return a read only version of the target document list.
     */
    public List<PDDocument> getTargetDocuments()
    {
        return Collections.unmodifiableList(mTargetDocs);
    }


    /**
     * Add a new {@link ISplitPageIdentifier} instance to this splitter. No
     * input checks are performed, except a null check, so it is possible to add
     * the same identifier multiple times!
     * 
     * @param aIdentifier
     *            the identifier to be added. If this is null, nothing is done.
     */
    public void addSplitPageIdentifier(ISplitPageIdentifier aIdentifier)
    {
        if (aIdentifier != null)
        {
            mSplitPageIdentifiers.add(aIdentifier);
        }
    }


    /**
     * Remove a previously added {@link ISplitPageIdentifier}.
     * 
     * @param aIdentifier
     *            the identifier to be removed.
     * @return true if the identifier was removed, false otherwise.
     */
    public boolean removeSplitPageIdentifier(ISplitPageIdentifier aIdentifier)
    {
        return mSplitPageIdentifiers.remove(aIdentifier);
    }


    /**
     * 
     * @return a read only list of the previously registered
     *         {@link ISplitPageIdentifier}'s.
     */
    public List<ISplitPageIdentifier> getSplitPageIdentifiers()
    {
        return Collections.unmodifiableList(mSplitPageIdentifiers);
    }


    /**
     * Add a new split status listener.
     * 
     * @param aListener
     *            the listener to be added.
     */
    public void addStatusListener(ISplitStatusListener aListener)
    {
        if (aListener != null)
        {
            mListeners.add(aListener);
        }
    }


    /**
     * Remove a previously added status listener.
     * 
     * @param aListener
     *            the listener to be removed.
     * @return true if the listener was removed, false otherwise.
     */
    public boolean removeStatusListener(ISplitStatusListener aListener)
    {
        return mListeners.remove(aListener);
    }


    /**
     * 
     * @return a read only list of the currently registered status listeners.
     */
    public List<ISplitStatusListener> getStatusListeners()
    {
        return Collections.unmodifiableList(mListeners);
    }


    /**
     * Sends a status update to all registered listeners.
     * 
     * @param aID
     *            the id of the event to be send.
     * @param aDocument
     *            the document, this event is related to.
     */
    protected void sendStatusUpdate(int aID, PDDocument aDocument)
    {
        sendStatusUpdate(aID, aDocument, null);
    }


    protected void sendStatusUpdate(int aID, PDDocument aDocument, File aFile)
    {
        if (mListeners.size() == 0)
        {
            return;
        }

        int docCount = mTargetDocs != null ? mTargetDocs.size() : 0;
        final SplitStatusEvent evt = new SplitStatusEvent(this, aID,
                mSourceDoc.getNumberOfPages(), mCurrentPage, docCount,
                aDocument, aFile);

        for (ISplitStatusListener l : mListeners)
        {
            try
            {
                l.splitStatusUpdate(evt);
            }
            catch (Exception ex)
            {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }


    /**
     * Request to abort current split operation. This can be called from any
     * other thread but the one who called {@link #split(PDDocument)}, s this
     * call is only useful while {@link #split(PDDocument)} is executing.
     */
    public void doAbort()
    {
        mAbort = true;
    }


    /**
     * 
     * @return the currently assigned memory usage model.
     */
    public MemoryUsageSetting getMemoryUsageSetting()
    {
        return mMemoryUsageSetting;
    }


    /**
     * Set a new memory usage model for creation of further target documents.
     * 
     * @param aMemoryUsageSetting
     *            the new memory usage model. This can be null.
     */
    public void setMemoryUsageSetting(MemoryUsageSetting aMemoryUsageSetting)
    {
        mMemoryUsageSetting = aMemoryUsageSetting;
    }


    /**
     * Split the given documents.
     * 
     * @param aDocument
     *            the document to be split.
     * @return the list of documents, the given was was split into.
     * @throws IOException
     *             on internal errors.
     */
    public List<PDDocument> split(PDDocument aDocument) throws IOException
    {
        mCurrentPage = 0;
        mTargetDocs = new ArrayList<PDDocument>();
        mSourceDoc = aDocument;
        mAbort = false;
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


    protected File getNextDocumentFile() throws IOException
    {
        File f = null;
        if (mNamePattern != null && mTargetDirectory != null)
        {
            for (int i = 0; i < 1000; i++)
            {
                String name = MessageFormat.format(mNamePattern, i);
                f = new File(mTargetDirectory, name);
                if (f.createNewFile())
                {
                    return f;
                }
            }
        }

        if (f == null)
        {
            f = File.createTempFile("pdfsplit_", ".pdf", mTargetDirectory);
        }

        return f;
    }


    protected void performDocumentFinished(PDDocument aTargetDoc)
        throws IOException
    {
        File docFile = getNextDocumentFile();

        aTargetDoc.save(docFile);
        PDDocument savedDoc = Loader.loadPDF(docFile);
        mTargetDocs.add(savedDoc);

        sendStatusUpdate(SplitStatusEvent.EVENT_DOCUMENT_FINISHED, savedDoc,
                docFile);
    }


    /**
     * Process pages while splitting.
     * 
     * @throws IOException
     *             on internal error.
     */
    protected void processPages() throws IOException
    {
        sendStatusUpdate(SplitStatusEvent.EVENT_SPLITTING_STARTED, mSourceDoc);

        int startPage = Math.max(mStartPage, 0);
        int endPage = Math.min(mEndPage, mSourceDoc.getNumberOfPages());

        mCurrentPage = -1;
        PDDocument targetDoc = null;
        for (PDPage page : getSourceDocument().getPages())
        {
            mCurrentPage++;
            if (mCurrentPage < startPage)
            {
                continue;
            }

            if (mAbort || mCurrentPage >= endPage)
            {
                // abort processing.
                break;
            }

            if (isSplitPage(page, mCurrentPage))
            {
                // current page contains the split text --> end of previous
                // document (this page is dropped)
                if (targetDoc != null)
                {
                    performDocumentFinished(targetDoc);
                    targetDoc = null;
                }
            }
            else
            {
                // not a split page --> include into target document
                if (targetDoc == null)
                {
                    // no active target document --> create a new target
                    // document
                    targetDoc = PDFHelper.createNewDocument(
                            getMemoryUsageSetting(), getSourceDocument());
                    sendStatusUpdate(SplitStatusEvent.EVENT_NEW_DOCUMENT,
                            targetDoc);
                }

                // import the page into the new target document
                PDFHelper.importPage(targetDoc, page);
                sendStatusUpdate(SplitStatusEvent.EVENT_NEXT_PAGE, mSourceDoc);
            }
        }
        if (targetDoc != null)
        {
            performDocumentFinished(targetDoc);
            targetDoc = null;
        }

        sendStatusUpdate(SplitStatusEvent.EVENT_SPLITTING_FINISHED, mSourceDoc);
    }


    /**
     * Check if the given page is identified as a split page. This method uses
     * the registered {@link ISplitPageIdentifier}'s to check if a page is a
     * split page.
     * 
     * @param aPage
     *            the page to check for the defined split text.
     * @param aPageIndex
     *            the index of the page to check.
     * @return true if the split text was found, false otherwise.
     */
    protected boolean isSplitPage(PDPage aPage, int aPageIndex)
    {
        for (ISplitPageIdentifier i : mSplitPageIdentifiers)
        {
            try
            {
                if (i.isSplitPage(mSourceDoc, aPage, aPageIndex))
                {
                    return true;
                }
            }
            catch (Exception ex)
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
}
