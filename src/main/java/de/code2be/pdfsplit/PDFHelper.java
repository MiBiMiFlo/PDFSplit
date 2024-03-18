package de.code2be.pdfsplit;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.io.ScratchFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;

/**
 * Some helper functions for handling PDF Documents.
 * 
 * @author Michael Weiss
 *
 */
public class PDFHelper
{

    private static final Logger LOGGER = Logger
            .getLogger(PDFHelper.class.getName());

    /**
     * Create a new PDF document and copy the attributes from the given existing
     * document.
     * 
     * @param aMemSet
     *            The {@link MemoryUsageSetting} to use in the new PDF document.
     * @param aSource
     *            The document to copy the attributes from.
     * @return the newly created document.
     */
    public static PDDocument createNewDocument(MemoryUsageSetting aMemSet,
            PDDocument aSource)
    {
        PDDocument res;
        if (aMemSet == null)
        {
            res = new PDDocument();
        }
        else
        {
            res = new PDDocument(() -> {
                return new ScratchFile(aMemSet);
            });
        }

        copyDocumentAttributes(aSource, res);
        return res;
    }


    /**
     * Copy document attributes from a source document to a target document.
     * 
     * @param aSource
     *            the source document to read attributes from.
     * @param aTarget
     *            the target document to write attributes to.
     */
    public static void copyDocumentAttributes(PDDocument aSource,
            PDDocument aTarget)
    {
        if (aSource == null || aTarget == null)
        {
            return;
        }

        aTarget.getDocument().setVersion(aSource.getVersion());

        PDDocumentInformation sourceDocumentInformation = aSource
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
                    if (aSource.getDocumentCatalog().getCOSObject() != aSource
                            .getDocumentInformation().getCOSObject())
                        continue;
                    LOGGER.warning("/Root and /Info share the same dictionary");
                    continue;
                }
                if (COSName.TYPE.equals(key)) continue;
                destDocumentInformationDictionary.setItem(key, value);
            }
            aTarget.setDocumentInformation(new PDDocumentInformation(
                    destDocumentInformationDictionary));
        }
        aTarget.getDocumentCatalog().setViewerPreferences(
                aSource.getDocumentCatalog().getViewerPreferences());
    }


    /**
     * Import a page (either a new one or from an other document) into a
     * document.
     * 
     * @param aTarget
     *            the target document to import the page to.
     * @param aPage
     *            the page to import.
     * @return the imported page.
     * @throws IOException
     *             on internal errors.
     */
    public static PDPage importPage(PDDocument aTarget, PDPage aPage)
        throws IOException
    {
        PDPage imported = aTarget.importPage(aPage);
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

}
