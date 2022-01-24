package de.code2be.pdfsplit.split;

import java.awt.image.BufferedImage;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;

/**
 * A ISplitPageIdentifier that searches for an QR code for a given string to be
 * contained in the page.
 * 
 * @author Michael Weiss
 *
 */
public class QRCodeIdentifier implements ISplitPageIdentifier
{

    private static final long serialVersionUID = -5911656988492757368L;

    /**
     * The content of a QR code that identifies a split page.
     */
    private final String mSplitString;

    /**
     * Create a new instance of the identifier for the given QR code string (the
     * value of the QR code as string).
     * 
     * @param aSplitString
     *            the string value of the QR code that identifies a split page.
     */
    public QRCodeIdentifier(String aSplitString)
    {
        mSplitString = aSplitString;
    }


    /**
     * 
     * @return the value that serves as the separator string. This is the string
     *         that is encoded in a QR code on the separator page.
     */
    public String getSplitString()
    {
        return mSplitString;
    }


    @Override
    public boolean isSplitPage(PDDocument aDocument, PDPage aPage,
            int aPageIndex)
        throws Exception
    {
        PDFRenderer renderer = new PDFRenderer(aDocument);
        BufferedImage img = renderer.renderImage(aPageIndex, 1.0f,
                ImageType.BINARY);

        LuminanceSource source = new BufferedImageLuminanceSource(img);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        Result[] results;
        try
        {
            results = new QRCodeMultiReader().decodeMultiple(bitmap);
        }
        catch (NotFoundException ex)
        {
            // simply ignore this as it is expected that no all pages have a QR
            // code
            return false;
        }

        for (Result res : results)
        {
            if (res != null)
            {
                String text = res.getText();
                if (text != null)
                {
                    if (text.equals(mSplitString))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
