package de.code2be.pdfsplit.split;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import net.sourceforge.tess4j.Tesseract;

/**
 * A {@link ISplitPageIdentifier} that searches the text within a document page
 * for a special split text. This instances is able to use Tesseract for OCR of
 * scanned (image) documents. This might be more precise (but possibly slower)
 * than the OCR software of the scanner.
 * 
 * @author Michael Weiss
 *
 */
public class TextSplitIdentifierOCR extends TextSplitIdentifier
{

    private static final long serialVersionUID = -2268075370178633007L;

    private static final Logger LOGGER = Logger
            .getLogger(TextSplitIdentifierOCR.class.getName());

    /**
     * The force OCR flag value.
     */
    private final boolean mForceOCR;

    /**
     * The tesseract instance to be used for OCR scanning. This is created on
     * first usage.
     */
    private Tesseract mTesseract;

    private float mScale = 4.0f;

    /**
     * Create a new instance of the identifier.
     * 
     * @param aSplitTexts
     *            the list of text elements to search for.
     * @param aRequiredCount
     *            the number of text elements that must match to indicate a page
     *            as split page.
     * @param aForceOCR
     *            a flag to indicate of OCR is forced. If this is true the page
     *            will be scanned with OCR even the page also contains text
     *            elements. In this case the page text will be a concatenation
     *            of the text from text elements and the text from OCR. If this
     *            is false only pages with no text elements available will be
     *            scanned using OCR.
     */
    public TextSplitIdentifierOCR(String[] aSplitTexts, int aRequiredCount,
            boolean aForceOCR)
    {
        super(aSplitTexts, aRequiredCount);
        mForceOCR = aForceOCR;
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
     * 
     * @param aTesseract
     *            the tesseract instance to be used for OCR.
     */
    public void setTesseract(Tesseract aTesseract)
    {
        mTesseract = aTesseract;
    }


    /**
     * 
     * @return the force OCR flag value.
     */
    public boolean isForceOCR()
    {
        return mForceOCR;
    }


    @Override
    protected String getTextofPage(PDDocument aDocument, PDPage aPage,
            int aPageIndex)
    {
        String text = super.getTextofPage(aDocument, aPage, aPageIndex);
        if (!isForceOCR() && text.trim().length() > 0)
        {
            // we found text and do not force OCR
            return text;
        }

        // here we need to do OCR
        try
        {
            if (mTesseract == null)
            {
                mTesseract = new Tesseract();
                File dataPath = new File("./tessdata");
                mTesseract.setLanguage("deu");
                mTesseract.setDatapath(dataPath.getAbsolutePath());
                // mTesseract.setOcrEngineMode(TessOcrEngineMode.OEM_TESSERACT_ONLY);
            }

            PDFRenderer renderer = new PDFRenderer(aDocument);

            BufferedImage img = renderer.renderImage(aPageIndex, mScale,
                    ImageType.BINARY);

            String ocrText = mTesseract.doOCR(img);
            if (ocrText != null)
            {
                // we have OCR text --> return original and OCR text
                StringBuilder sb = new StringBuilder();
                sb.append(text);
                if (text.trim().length() > 0)
                {
                    sb.append("\n");
                }
                sb.append(ocrText);
                return sb.toString();
            }
        }
        catch (Exception ex)
        {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
        return text;
    }

}
