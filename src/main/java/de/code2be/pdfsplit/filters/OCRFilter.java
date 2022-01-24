package de.code2be.pdfsplit.filters;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
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

import net.sourceforge.tess4j.TessAPI;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Word;

/**
 * OCR filter that enhances image only pages by text retrieved from an OCR
 * engine (Tesseract).
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
    private Tesseract mTesseract;

    /**
     * The scale factor to use for converting PDF to image (1.0 means 72dpi).
     */
    private float mScale = 4.0f;

    public OCRFilter(Tesseract aTesseract)
    {
        mTesseract = aTesseract;
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
            ts.setStartPage(aPageIndex);
            ts.setEndPage(aPageIndex);
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
        return containsText(aDocument, aPage, aPageIndex);
    }


    @Override
    public PDDocument filter(PDDocument aDocument)
    {
        int pageCount = aDocument.getNumberOfPages();

        notifyEvent(DocumentFilterEvent.EVENT_NEW_DOCUMENT, aDocument, null,
                pageCount, -1);

        PDFRenderer renderer = new PDFRenderer(aDocument);

        int pidx = -1;
        for (PDPage page : aDocument.getPages())
        {
            pidx++;
            notifyEvent(DocumentFilterEvent.EVENT_NEXT_PAGE, aDocument, page,
                    pageCount, pidx);

            if (!isToProcess(aDocument, page, pidx))
            {
                notifyEvent(DocumentFilterEvent.EVENT_PAGE_IGNORED, aDocument,
                        page, pageCount, pidx);
                continue;
            }

            try
            {
                PDRectangle pageBox = page.getBBox();

                BufferedImage img = renderer.renderImage(pidx, mScale,
                        ImageType.BINARY);

                List<Word> words = mTesseract.getWords(img,
                        TessAPI.TessPageIteratorLevel.RIL_WORD);

                try (PDPageContentStream cs = new PDPageContentStream(aDocument,
                        page, AppendMode.APPEND, false, true))
                {
                    PDType1Font font = PDType1Font.TIMES_ROMAN;
                    // we do not show the text (invisible overlay above image)
                    cs.setRenderingMode(RenderingMode.NEITHER);
                    // we use green and red for debug
                    cs.setStrokingColor(Color.red);
                    cs.setNonStrokingColor(Color.green);
                    cs.beginText();
                    for (Word w : words)
                    {
                        String text = w.getText();
                        String clearedText = cleanForEncoding(text,
                                font.getEncoding());
                        if (clearedText.trim().length() == 0)
                        {
                            continue;
                        }

                        Rectangle r = w.getBoundingBox();
                        Rectangle r_scaled = new Rectangle((int) (r.x / mScale),
                                (int) (r.y / mScale), (int) (r.width / mScale),
                                (int) (r.height / mScale));

                        float fontSize = (float) r_scaled.height;

                        float textWidth = font.getStringWidth(clearedText)
                                / 1000 * fontSize;
                        while (textWidth > r_scaled.width && fontSize > 1)
                        {
                            fontSize -= 0.2f;
                            textWidth = font.getStringWidth(clearedText) / 1000
                                    * fontSize;
                        }

                        float sfx = 1.0f; // (txtWidth * 0.021f) / (r.width /
                                          // renderScale);
                        if (sfx == 0.0)
                        {
                            sfx = 1.0f;
                        }

                        cs.setFont(font, fontSize);

                        cs.newLineAtOffset(r_scaled.x, pageBox.getHeight()
                                - (r_scaled.y + r_scaled.height));
                        cs.showText(clearedText);
                    }
                    cs.endText();
                }
            }
            catch (Exception ex)
            {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            }
            notifyEvent(DocumentFilterEvent.EVENT_PAGE_DONE, aDocument, page,
                    pageCount, pidx);
        }
        notifyEvent(DocumentFilterEvent.EVENT_DOCUMENT_DONE, aDocument, null,
                pageCount, -1);

        return aDocument;
    }
}
