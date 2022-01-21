package de.code2be.pdfsplit;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;

public class QRCodeExtractor
{

    private final PDDocument mDocument;

    private PDFRenderer mRenderer;

    private QRCodeMultiReader mQRCodeReader;

    public QRCodeExtractor(PDDocument aDocument)
    {
        mDocument = aDocument;
    }


    public List<String> extractQRCodesFrom(int aPageIndex) throws IOException
    {
        List<String> resultList = new ArrayList<>();
        if (mRenderer == null)
        {
            mRenderer = new PDFRenderer(mDocument);
        }
        if (mQRCodeReader == null)
        {
            mQRCodeReader = new QRCodeMultiReader();
        }

        BufferedImage img = mRenderer.renderImage(aPageIndex);

        LuminanceSource source = new BufferedImageLuminanceSource(img);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        try
        {
            Result[] results = mQRCodeReader.decodeMultiple(bitmap);
            if (results != null && results.length > 0)
            {
                for (Result res : results)
                {
                    if (res != null)
                    {
                        String text = res.getText();
                        if (text != null)
                        {
                            resultList.add(text);
                        }
                    }
                }
            }
        }
        catch (NotFoundException ex)
        {
            // simply ignore this as it is expected that no all pages have a QR
            // code
        }
        return resultList;

    }

}
