package de.code2be.help;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.ITessAPI.TessOcrEngineMode;
import net.sourceforge.tess4j.Tesseract;

/**
 * A factory class that can store tesseract configuration and create
 * pre-configured instances.
 * 
 * @author Michael Weiss
 *
 */
public class TesseractFactory
{

    /**
     * The properties containing the variables to be applied to a new
     * {@link Tesseract} instance.
     */
    private Properties mSettings = new Properties();

    /**
     * The language to expect when scanning.
     */
    private String mLanguage = "eng";

    /**
     * The path to trained models.
     */
    private String mDatapath;

    /**
     * The page segmentation mode. See {@link ITessAPI.TessPageSegMode} for
     * valid values.
     */
    private int mPageSegMode = ITessAPI.TessPageSegMode.PSM_AUTO_OSD;

    /**
     * The OCR engine mode to use. See {@link ITessAPI.TessOcrEngineMode} for
     * valid values.
     */
    private int mOcrEngineMode = TessOcrEngineMode.OEM_DEFAULT;

    /**
     * The list of configs.
     */
    private final List<String> mConfigList = new ArrayList<String>();

    /**
     * 
     * @param aKey
     *            the variable key
     * @param aValue
     *            the value.
     */
    public void setVariable(String aKey, String aValue)
    {
        mSettings.setProperty(aKey, aValue);
    }


    public String getVariable(String aKey)
    {
        return mSettings.getProperty(aKey);
    }


    public void setLanguage(String aLanguage)
    {
        mLanguage = aLanguage;
    }


    public String getLanguage()
    {
        return mLanguage;
    }


    public void setDatapath(String aDatapath)
    {
        mDatapath = aDatapath;
    }


    public String getDatapath()
    {
        return mDatapath;
    }


    /**
     * 
     * @param aPageSegnMode
     *            the page segmentation mode. See
     *            {@link ITessAPI.TessPageSegMode} for valid values.
     */
    public void setPageSegnMode(int aPageSegnMode)
    {
        mPageSegMode = aPageSegnMode;
    }


    public int getPageSegnMode()
    {
        return mPageSegMode;
    }


    /**
     * 
     * @param aOcrEngineMode
     *            The OCR engine mode to use. See
     *            {@link ITessAPI.TessOcrEngineMode}
     */
    public void setOcrEngineMode(int aOcrEngineMode)
    {
        mOcrEngineMode = aOcrEngineMode;
    }


    public int getOcrEngineMode()
    {
        return mOcrEngineMode;
    }


    public void setConfigs(List<String> aConfigs)
    {
        mConfigList.clear();
        if (aConfigs != null)
        {
            mConfigList.addAll(aConfigs);
        }
    }


    public List<String> getConfigList()
    {
        return Collections.unmodifiableList(mConfigList);
    }


    /**
     * Initialize the given (newly created) instance.
     * 
     * @param <T>
     *            the final class that was initialized.
     * @param aTesseract
     *            the new instance.
     * @return the given instance for further processing.
     */
    protected <T extends Tesseract> T init(T aTesseract)
    {
        for (Object key : mSettings.keySet())
        {
            aTesseract.setVariable((String) key,
                    mSettings.getProperty((String) key));
        }

        aTesseract.setLanguage(mLanguage);
        aTesseract.setDatapath(mDatapath);
        aTesseract.setOcrEngineMode(mOcrEngineMode);
        aTesseract.setPageSegMode(mPageSegMode);
        aTesseract.setConfigs(mConfigList);
        return aTesseract;
    }


    /**
     * Create a new {@link Closeable} {@link Tesseract} instance.
     * 
     * @return the new instance.
     */
    public TesseractC createCloseableInstance()
    {
        return init(new TesseractC());
    }


    /**
     * 
     * @return the new instance.
     */
    public Tesseract createInstance()
    {
        return init(new Tesseract());
    }

}
