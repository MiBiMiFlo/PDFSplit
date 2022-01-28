package de.code2be.pdfsplit.filters;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.encoding.Encoding;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;

import de.code2be.help.TesseractC;
import de.code2be.help.TesseractFactory;
import net.sourceforge.tess4j.TessAPI;
import net.sourceforge.tess4j.Word;

/**
 * OCR filter that enhances image only pages by text retrieved from an OCR
 * engine (Tesseract).
 * 
 * 
 * TODO: use PrintImageLocations example and extract RAW images from PDF, then
 * only process these with OCR instead of rendering the complete page (no
 * scaling / rendering needed anymore)
 * 
 * @author Michael Weiss
 *
 */
public class OCRFilter extends AbstractDocumentFilter
{

    private static final long serialVersionUID = 7575485665208369756L;

    private static final Logger LOGGER = Logger
            .getLogger(OCRFilter.class.getName());

    /**
     * The tesseract engine instance to use for OCR.
     */
    private final TesseractFactory mTF;

    /**
     * The number of threads to be used for OCR.
     */
    private int mThreadCount = 4;

    /**
     * The scale factor to use for converting PDF to image (1.0 means 72dpi).
     */
    private float mScale = 3.0f;

    public OCRFilter(TesseractFactory aTF)
    {
        mTF = aTF;
    }


    /**
     * 
     * @param aThreadCount
     *            the maximum number of threads to be used for OCR. The default
     *            is 4.
     */
    public void setThreadCount(int aThreadCount)
    {
        mThreadCount = aThreadCount;
    }


    /**
     * 
     * @return the maximum number of threads to be used for OCR. The default is
     *         4.
     */
    public int getThreadCount()
    {
        return mThreadCount;
    }


    /**
     * 
     * @param aScale
     *            the new scale (image size) to be used for pdf image rendering.
     *            1.0 means 72dpi.
     * 
     */
    public void setScale(float aScale)
    {
        mScale = aScale;
    }


    /**
     * 
     * @return the current image scale factor used for image rendering. 1.0
     *         means 72dpi.
     */
    public float getScale()
    {
        return mScale;
    }


    /**
     * Helper method that cleans characters not able to encode in the given
     * encoding.
     * 
     * @param aText
     *            the text to scan for characters that can not be encoded.
     * @param aEncoding
     *            the encoding to check the given text against.
     * @return the text with only valid characters.
     */
    public static String cleanForEncoding(String aText, Encoding aEncoding)
    {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < aText.length(); i++)
        {
            if (aEncoding.contains(aText.charAt(i)))
            {
                b.append(aText.charAt(i));
            }
        }
        return b.toString();
    }


    /**
     * Check if the given page already contains text.
     * 
     * @param aDocument
     *            the document to check in.
     * @param aPage
     *            the page to check for text content.
     * @param aPageIndex
     *            the index of the page to check for text.
     * @return true if the given page contains already text, false otherwise.
     */
    public boolean containsText(PDDocument aDocument, PDPage aPage,
            int aPageIndex)
    {
        try
        {
            PDFTextStripper ts = new PDFTextStripper();
            ts.setStartPage(aPageIndex + 1);
            ts.setEndPage(aPageIndex + 1);
            String text = ts.getText(aDocument);
            return text.trim().length() > 0;
        }
        catch (IOException ex)
        {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            return false;
        }
    }


    /**
     * Check if the given page is to be processed by the filter or not.
     * 
     * @param aDocument
     *            the document that is processed.
     * @param aPage
     *            the page that is to be checked.
     * @param aPageIndex
     *            the index of the page to be checked.
     * @return true if the page is to be processed, false otherwise.
     */
    protected boolean isToProcess(PDDocument aDocument, PDPage aPage,
            int aPageIndex)
    {
        return !containsText(aDocument, aPage, aPageIndex);
    }


    /**
     * Apply the found words to the page.
     * 
     * @param aPageMetaData
     *            the metadata that contain document, page and word list.
     */
    protected void applyWords(PageMetaData aPageMetaData)
    {
        try (PDPageContentStream cs = new PDPageContentStream(
                aPageMetaData.getDocument(), aPageMetaData.getPage(),
                AppendMode.APPEND, false, true))
        {
            PDRectangle pageBox = aPageMetaData.getPage().getBBox();

            PDType1Font font = PDType1Font.TIMES_ROMAN;
            // we do not show the text (invisible overlay above
            // image)
            cs.setRenderingMode(RenderingMode.NEITHER);
            // we use green and red for debug
            cs.setStrokingColor(Color.red);
            cs.setNonStrokingColor(Color.green);
            for (Word w : aPageMetaData.getWords())
            {
                String text = w.getText();
                String clearedText = cleanForEncoding(text, font.getEncoding());
                if (clearedText.trim().length() == 0)
                {
                    continue;
                }

                Rectangle r = w.getBoundingBox();
                Rectangle r_scaled = new Rectangle((int) (r.x / mScale),
                        (int) (r.y / mScale), (int) (r.width / mScale),
                        (int) (r.height / mScale));

                float fontSize = (float) r_scaled.height;

                float textWidth = font.getStringWidth(clearedText) / 1000
                        * fontSize;
                while (textWidth > r_scaled.width && fontSize > 1)
                {
                    fontSize -= 0.2f;
                    textWidth = font.getStringWidth(clearedText) / 1000
                            * fontSize;
                }

                float sfx = 1.0f; // (txtWidth * 0.021f) / (r.width
                                  // /
                                  // renderScale);
                if (sfx == 0.0)
                {
                    sfx = 1.0f;
                }

                cs.beginText();
                cs.setFont(font, fontSize);

                cs.newLineAtOffset(r_scaled.x,
                        pageBox.getHeight() - (r_scaled.y + r_scaled.height));
                cs.showText(clearedText);
                cs.endText();
            }
        }
        catch (Exception ex)
        {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }


    protected void notifyEvent(int aId, PageMetaData aPMD)
    {
        notifyEvent(aId, aPMD.getDocument(), aPMD.getPage(),
                aPMD.getPageCount(), aPMD.getPageIndex());
    }


    /**
     * Process OCR rendering for the given items. This method is to be called
     * multiple times in parallel threads.
     * 
     * @param aItems
     *            the items to process.
     */
    protected void process(ItemProvider<PageMetaData> aItems)
    {
        PDFRenderer renderer = null;
        PDDocument lastDoc = null;
        try (TesseractC trOCR = mTF.createCloseableInstance())
        {
            PageMetaData pmd;
            while ((pmd = aItems.next()) != null)
            {
                notifyEvent(DocumentFilterEvent.EVENT_NEXT_PAGE, pmd);

                if (!isToProcess(pmd.getDocument(), pmd.getPage(),
                        pmd.getPageIndex()))
                {
                    notifyEvent(DocumentFilterEvent.EVENT_PAGE_IGNORED, pmd);
                    continue;
                }

                if (lastDoc != pmd.getDocument())
                {
                    renderer = null;
                }

                try
                {
                    if (renderer == null)
                    {
                        renderer = new PDFRenderer(pmd.getDocument());
                    }

                    long start = System.currentTimeMillis();
                    BufferedImage img = renderer.renderImage(pmd.getPageIndex(),
                            mScale, ImageType.BINARY);

                    LOGGER.log(Level.FINE, "Rendering took: {0}ms",
                            (System.currentTimeMillis() - start));
                    start = System.currentTimeMillis();
                    List<Word> words = trOCR.getWords(img,
                            TessAPI.TessPageIteratorLevel.RIL_WORD);
                    LOGGER.log(Level.FINE, "OCR took: {0}ms",
                            (System.currentTimeMillis() - start));
                    pmd.setWords(words);
                }
                catch (Exception ex)
                {
                    LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                }
                finally
                {
                    notifyEvent(DocumentFilterEvent.EVENT_PAGE_DONE, pmd);
                }
            }
        }
    }


    @Override
    public PDDocument filter(PDDocument aDocument)
    {
        int pageCount = aDocument.getNumberOfPages();
        notifyEvent(DocumentFilterEvent.EVENT_NEW_DOCUMENT, aDocument, null,
                pageCount, -1);

        List<PageMetaData> pmds = new ArrayList<>();
        int pidx = -1;
        for (PDPage page : aDocument.getPages())
        {
            pidx++;
            pmds.add(new PageMetaData(aDocument, pageCount, page, pidx));
        }

        final ItemProvider<PageMetaData> metaProvider = new ItemProvider<>(
                pmds);

        Runnable r = () -> {
            process(metaProvider);
        };

        int tc = Math.min(mThreadCount, pmds.size());

        if (tc <= 1)
        {
            LOGGER.log(Level.FINE,
                    "Will process in actual thread (single threaded).");
            r.run();
        }
        else
        {
            // start a list of threads
            LOGGER.log(Level.FINE, "Will create {0} threads.", tc);
            Thread[] threads = new Thread[tc];
            for (int i = 0; i < threads.length; i++)
            {
                threads[i] = new Thread(r, "OCRFilter-" + i);
                threads[i].setDaemon(true);
                threads[i].setPriority(Thread.MIN_PRIORITY);
                threads[i].start();
            }

            // wait for all threads
            for (Thread t : threads)
            {
                try
                {
                    t.join();
                }
                catch (Exception ex)
                {
                    LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
        }

        for (PageMetaData pmd : pmds)
        {
            applyWords(pmd);
        }

        notifyEvent(DocumentFilterEvent.EVENT_DOCUMENT_DONE, aDocument, null,
                pageCount, -1);

        return aDocument;
    }

    private class ItemProvider<T>
    {

        private final List<T> mItems;

        private volatile int mCur = -1;

        ItemProvider(List<T> aItems)
        {
            mItems = aItems;
        }


        public synchronized T next()
        {
            mCur++;
            if (mCur < mItems.size())
            {
                return mItems.get(mCur);
            }
            return null;
        }
    }


    private class PageMetaData
    {

        private final PDDocument mDocument;

        private final int mPageCount;

        private final PDPage mPage;

        private final int mPageIndex;

        private List<Word> mWords;

        public PageMetaData(PDDocument aDocument, int aPageCount, PDPage aPage,
                int aPageIndex)
        {
            mDocument = aDocument;
            mPageCount = aPageCount;
            mPage = aPage;
            mPageIndex = aPageIndex;
        }


        public void setWords(List<Word> aWords)
        {
            mWords = aWords;
        }


        public List<Word> getWords()
        {
            return mWords;
        }


        public PDDocument getDocument()
        {
            return mDocument;
        }


        public int getPageCount()
        {
            return mPageCount;
        }


        public PDPage getPage()
        {
            return mPage;
        }


        public int getPageIndex()
        {
            return mPageIndex;
        }
    }
}
