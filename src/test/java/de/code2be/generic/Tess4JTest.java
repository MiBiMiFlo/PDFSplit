package de.code2be.generic;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;

import net.sourceforge.tess4j.ITesseract.RenderedFormat;
import net.sourceforge.tess4j.Tesseract1;

/**
 * This is a test how the new tess4j feature works to perform OCR in PDFs and
 * add the text to the PDF behind the image.
 */
public class Tess4JTest
{

    private static final Logger LOGGER = System
            .getLogger(Tess4JTest.class.getName());

    public static void main(String[] args) throws Exception
    {
        Tesseract1 t1 = new Tesseract1();
        t1.setLanguage("deu");
        List<RenderedFormat> fmts = List.of(RenderedFormat.PDF);

        t1.createDocuments("./examples/example_2.pdf",
                "./examples/example_2_ocr", fmts);
        LOGGER.log(Level.INFO, "All done.");
    }
}
