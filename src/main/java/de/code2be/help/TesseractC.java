package de.code2be.help;

import java.io.Closeable;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import net.sourceforge.tess4j.Tesseract;

/**
 * This is a closeable instance of the {@link Tesseract} class. This is faster
 * as it does only call the API initialization once for the first OCR call and
 * disposes on {@link #close()} instead of in each OCR call. Therefore the
 * {@link #close()} method has to be called when the instance is to be released.
 * 
 * @author Michael Weiss
 *
 */
public class TesseractC extends Tesseract implements Closeable
{

    private static final Logger LOGGER = System
            .getLogger(TesseractC.class.getName());

    /**
     * A flag to hold initialized status. This is set on the first call to
     * {@link #init()} and reset on the call to {@link #close()}.
     */
    private boolean mInitialized = false;

    /**
     * A flag to hold status of variables are already set. This is set on the
     * first call to {@link #setVariables()} and released on {@link #close()}.
     */
    private boolean mVariablesSet = false;

    @Override
    protected void init()
    {
        if (!mInitialized)
        {
            // not initialized --> do initialization
            super.init();
            mInitialized = true;
        }
        else
        {
            // already initialized --> do nothing, but log
            LOGGER.log(Level.DEBUG, "Already initialized, ignoring!");
        }
    }


    @Override
    protected void setVariables()
    {
        if (!mVariablesSet)
        {
            // variables not set
            super.setVariables();
            mVariablesSet = true;
        }
        else
        {
            // variables already set --> do nothing bug log
            LOGGER.log(Level.DEBUG, "Variables already set, ignoring!");
        }
    }


    @Override
    protected void dispose()
    {
        // we fully ignore calls to dispose
        LOGGER.log(Level.DEBUG, "Ignoring call to dispose.");
    }


    @Override
    public void close()
    {
        try
        {
            // on close we dispose
            super.dispose();
        }
        finally
        {
            mInitialized = false;
        }

    }
}
