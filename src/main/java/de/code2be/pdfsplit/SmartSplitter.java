package de.code2be.pdfsplit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
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

    public static PDPage importPage(PDDocument aTargetDocument, PDPage aPage)
        throws IOException
    {
        PDPage imported = aTargetDocument.importPage(aPage);
        if (aPage.getResources() != null
                && !aPage.getCOSObject().containsKey(COSName.RESOURCES))
        {
            imported.setResources(aPage.getResources());
            LOGGER.info("Resources imported in Splitter");
        }

        List<PDAnnotation> annotations = imported.getAnnotations();
        for (PDAnnotation annotation : annotations)
        {
            if (annotation instanceof PDAnnotationLink)
            {
                PDAnnotationLink link = (PDAnnotationLink) annotation;
                PDDestination destination = link.getDestination();
                PDAction action = link.getAction();
                if (destination == null && action instanceof PDActionGoTo)
                {
                    destination = ((PDActionGoTo) action).getDestination();
                }
                if (destination instanceof PDPageDestination)
                {
                    ((PDPageDestination) destination).setPage(null);
                }
            }
            annotation.setPage(null);
        }
        return imported;
    }


    public static PDDocument createNewDocument(MemoryUsageSetting aMemSet,
            PDDocument aDocument)
        throws IOException
    {
        PDDocument document = aMemSet == null ? new PDDocument()
                : new PDDocument(aMemSet);
        document.getDocument().setVersion(aDocument.getVersion());
        PDDocumentInformation sourceDocumentInformation = aDocument
                .getDocumentInformation();
        if (sourceDocumentInformation != null)
        {
            COSDictionary sourceDocumentInformationDictionary = sourceDocumentInformation
                    .getCOSObject();
            COSDictionary destDocumentInformationDictionary = new COSDictionary();
            for (COSName key : sourceDocumentInformationDictionary.keySet())
            {
                COSBase value = sourceDocumentInformationDictionary
                        .getDictionaryObject(key);
                if (value instanceof COSDictionary)
                {
                    LOGGER.warning("Nested entry for key '" + key.getName()
                            + "' skipped in document information dictionary");
                    if (aDocument.getDocumentCatalog()
                            .getCOSObject() != aDocument
                                    .getDocumentInformation().getCOSObject())
                        continue;
                    LOGGER.warning("/Root and /Info share the same dictionary");
                    continue;
                }
                if (COSName.TYPE.equals((Object) key)) continue;
                destDocumentInformationDictionary.setItem(key, value);
            }
            document.setDocumentInformation(new PDDocumentInformation(
                    destDocumentInformationDictionary));
        }
        document.getDocumentCatalog().setViewerPreferences(
                aDocument.getDocumentCatalog().getViewerPreferences());
        return document;
    }

    private PDDocument mSourceDoc;

    private PDDocument mTargetDoc;

    private int startPage = Integer.MIN_VALUE;

    private int endPage = Integer.MAX_VALUE;

    private List<PDDocument> mTargetDocs;

    private int mPageNumber;

    private MemoryUsageSetting memoryUsageSetting = null;

    private final String mSplitText;

    private final String[] mSplitTextArr;

    private ISplitStatusListener mListener;

    private volatile boolean mDoAbort = false;

    public SmartSplitter(String aSplitText)
    {
        mSplitText = aSplitText;
        mSplitTextArr = mSplitText.split("\\s+");
    }


    public int getNumDocuments()
    {
        return mTargetDocs != null ? mTargetDocs.size() : 0;
    }


    public int getCurrentPage()
    {
        return mPageNumber;
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
                mSourceDoc.getNumberOfPages(), mPageNumber + 1,
                mTargetDocs != null ? mTargetDocs.size() : 0, aDocument);

        mListener.splitStatusUpdate(evt);
    }


    public void doAbort()
    {
        mDoAbort = true;
    }


    public MemoryUsageSetting getMemoryUsageSetting()
    {
        return this.memoryUsageSetting;
    }


    public void setMemoryUsageSetting(MemoryUsageSetting memoryUsageSetting)
    {
        this.memoryUsageSetting = memoryUsageSetting;
    }


    public List<PDDocument> split(PDDocument document) throws IOException
    {
        this.mPageNumber = 0;
        this.mTargetDocs = new ArrayList<PDDocument>();
        this.mSourceDoc = document;
        this.processPages();
        return this.mTargetDocs;
    }


    public void setStartPage(int start)
    {
        if (start <= 0)
        {
            throw new IllegalArgumentException(
                    "Start page is smaller than one");
        }
        this.startPage = start;
    }


    public void setEndPage(int end)
    {
        if (end <= 0)
        {
            throw new IllegalArgumentException("End page is smaller than one");
        }
        this.endPage = end;
    }


    private void processPages() throws IOException
    {
        for (PDPage page : this.mSourceDoc.getPages())
        {
            if (this.mPageNumber + 1 >= this.startPage
                    && this.mPageNumber + 1 <= this.endPage)
            {
                this.processPage(page);
                ++this.mPageNumber;
                continue;
            }
            if (this.mPageNumber > this.endPage) break;
            ++this.mPageNumber;
            if (mDoAbort)
            {
                break;
            }
        }
        if (mTargetDoc != null)
        {
            sendStatusUpdate(SplitStatusEvent.EVENT_DOCUMENT_FINISHED,
                    mTargetDoc);
        }
    }


    protected String getTextofPage(int aPageNumber)
    {
        PDFTextStripper reader;
        try
        {
            reader = new PDFTextStripper();
            reader.setStartPage(aPageNumber);
            reader.setEndPage(aPageNumber);
            return reader.getText(getSourceDocument());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return "";
    }


    protected PDDocument createNewDocument() throws IOException
    {
        PDDocument document = this.memoryUsageSetting == null ? new PDDocument()
                : new PDDocument(this.memoryUsageSetting);
        document.getDocument()
                .setVersion(this.getSourceDocument().getVersion());
        PDDocumentInformation sourceDocumentInformation = this
                .getSourceDocument().getDocumentInformation();
        if (sourceDocumentInformation != null)
        {
            COSDictionary sourceDocumentInformationDictionary = sourceDocumentInformation
                    .getCOSObject();
            COSDictionary destDocumentInformationDictionary = new COSDictionary();
            for (COSName key : sourceDocumentInformationDictionary.keySet())
            {
                COSBase value = sourceDocumentInformationDictionary
                        .getDictionaryObject(key);
                if (value instanceof COSDictionary)
                {
                    LOGGER.warning(("Nested entry for key '" + key.getName()
                            + "' skipped in document information dictionary"));
                    if (this.mSourceDoc.getDocumentCatalog()
                            .getCOSObject() != this.mSourceDoc
                                    .getDocumentInformation().getCOSObject())
                        continue;
                    LOGGER.warning("/Root and /Info share the same dictionary");
                    continue;
                }
                if (COSName.TYPE.equals((Object) key)) continue;
                destDocumentInformationDictionary.setItem(key, value);
            }
            document.setDocumentInformation(new PDDocumentInformation(
                    destDocumentInformationDictionary));
        }
        document.getDocumentCatalog()
                .setViewerPreferences(this.getSourceDocument()
                        .getDocumentCatalog().getViewerPreferences());
        return document;
    }


    protected boolean containsSplitText(PDPage page)
    {
        String text = getTextofPage(mPageNumber + 1);
        for (String txt : mSplitTextArr)
        {
            if (!text.contains(txt))
            {
                return false;
            }
        }
        return true;
    }


    protected void processPage(PDPage aPage) throws IOException
    {
        if (containsSplitText(aPage))
        {
            if (mTargetDoc != null)
            {
                sendStatusUpdate(SplitStatusEvent.EVENT_DOCUMENT_FINISHED,
                        mTargetDoc);
            }
            mTargetDoc = null;
            return;
        }

        if (mTargetDoc == null)
        {
            mTargetDocs.add(mTargetDoc = createNewDocument());
            sendStatusUpdate(SplitStatusEvent.EVENT_NEW_DOCUMENT, mTargetDoc);
        }

        importPage(getDestinationDocument(), aPage);
    }


    protected final PDDocument getSourceDocument()
    {
        return mSourceDoc;
    }


    protected final PDDocument getDestinationDocument()
    {
        return mTargetDoc;
    }
}
