package de.code2be.pdfsplit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A configuration class used to store and retrieve configuration items.
 * 
 * @author Michael Weiss
 *
 */
public class Config extends Properties
{

    private static final long serialVersionUID = 3299392832333435191L;

    private static final Logger LOGGER = Logger
            .getLogger(Config.class.getName());

    /**
     * Default value for {@link #PROP_SEPARATOR_TEXT}.
     */
    public static final String DEFAULT_SEP = "$PWKM%U?5X4$;PDF-SPLIT-SPLIT-PAGE;PDF-SPLIT-TRENNSEITE";

    /**
     * Default value for {@link #PROP_SEPARATOR_QR_CODE}
     */
    public static final String DEFAULT_QR_CODE = "https://github.com/MiBiMiFlo/PDFSplit";

    /**
     * Property key that stores the value for the directory to start an open
     * action in.
     */
    public static final String PROP_DIRECTORY_OPEN = "main.dirOpen";

    /**
     * Property key that stores the value for the directory to save split files
     * in.
     */
    public static final String PROP_DIRECTORY_SAVE = "main.dirSave";

    /**
     * Property key that stores the value for the flag if a text based splitter
     * is used as separator.
     */
    public static final String PROP_SEPARATOR_USE_TEXT = "separator.useText";

    /**
     * Property key that stores the value for the flag if a QR code based
     * splitter is used as separator.
     */
    public static final String PROP_SEPARATOR_USE_QR = "separator.useQR";

    /**
     * Property key that stores the value for the separator text to search.
     */
    public static final String PROP_SEPARATOR_TEXT = "separator.text";

    /**
     * Property key that stores the value for the number of text matches
     * required to identify a page as split page from the text based separator.
     */
    public static final String PROP_SEPARATOR_MATCH_COUNT = "separator.matchCount";

    /**
     * Property key that stores the QR code value that identifies a page as
     * split page in QR code separator.
     */
    public static final String PROP_SEPARATOR_QR_CODE = "separator.qrcode";

    /**
     * Property key that stores the value for the flag if OCR is to be performed
     * in case there is no text found on a page.
     */
    public static final String PROP_SEPARATOR_DO_OCR = "separator.ocr.enable";

    /**
     * Property key that stores the value for the flag if OCR is to be performed
     * on any page, even the once that already contain text.
     */
    public static final String PROP_SEPARATOR_FORCE_OCR = "separator.ocr.force";

    /**
     * Property key that stores the value for the flag if OCR is to be performed
     * as a input filter when opening the PDF file. This will result in the text
     * being available for searching in the output documents.
     */
    public static final String PROP_FILTER_DO_OCR = "filter.ocr.enable";

    /**
     * Property key that stores the value for the flag if empty pages should be
     * filtered and disabled automatically.
     */
    public static final String PROP_FILTER_DO_EMPTY_PAGE = "filter.emptyPage.enable";

    /**
     * Property key that stores the value for the threshold that must be passed
     * to see a pixel as not empty).
     */
    public static final String PROP_FILTER_EMPTY_PAGE_TH_PIXEL = "filter.emptyPage.th.pixel";

    /**
     * Property key that stores the value for the threshold that must be passed
     * to see a block of pixels as not empty).
     */
    public static final String PROP_FILTER_EMPTY_PAGE_TH_BLOCK = "filter.emptyPage.th.block";

    /**
     * Property key that stores the value for the threshold that must be passed
     * to see a page as not empty).
     */
    public static final String PROP_FILTER_EMPTY_PAGE_TH_PAGE = "filter.emptyPage.th.page";

    /**
     * Property key that stores the value for the number of horizontal blocks a
     * page is cut into for empty page detection.
     */
    public static final String PROP_FILTER_EMPTY_PAGE_BLOCKCOUNT_H = "filter.emptyPage.blockCountH";

    /**
     * Property key that stores the value for the number of vertical blocks a
     * page is cut into for empty page detection.
     */
    public static final String PROP_FILTER_EMPTY_PAGE_BLOCKCOUNT_V = "filter.emptyPage.blockCountV";

    /**
     * Property key that stores the value of the OCR data path (Tesseract data).
     */
    public static final String PROP_OCR_DATAPATH = "ocr.datapath";

    /**
     * Property key that stores the value of the OCR language to expect the
     * document to be in.
     */
    public static final String PROP_OCR_LANG = "ocr.language";

    /**
     * Property key that stores the value of the OCR engine mode.
     */
    public static final String PROP_OCR_ENGINE_MODE = "ocr.engineMode";

    /**
     * Property key that stores the value of the OCR image scaling factor.
     */
    public static final String PROP_OCR_IMG_SCALE = "ocr.scale";

    /**
     * Retrieve the config files that stores the application config. The logic
     * is to return only existing config files in the order of the following
     * list:
     * <ul>
     * <li>./pdfsplit.cfg
     * <li>${HOME}/.pdfsplit.cfg
     * <li>${HOME}/pdfsplit.cfg
     * <li>${USERPROFILE}/.pdfsplit.cfg
     * <li>${USERPROFILE}/pdfsplit.cfg
     * </ul>
     * In case a config entry is available in multiple existing config files,
     * the entry in the last file of the list takes effect.
     * 
     * @return the config files to be used. Might be an empty list.
     */
    protected static List<File> getConfigFiles()
    {
        List<File> res = new ArrayList<>();

        String exedir = System.getProperty("launch4j.exedir");
        if (exedir == null)
        {
            exedir = new File(".").getAbsolutePath();
        }

        File f = new File(exedir, "pdfsplit.cfg");
        if (f.isFile())
        {
            res.add(f.getAbsoluteFile());
        }

        List<String> envVars = Arrays.asList("HOME", "USERPROFILE");
        for (String env : envVars)
        {
            if (System.getenv().containsKey(env))
            {
                File dir = new File(System.getenv(env));
                if (!dir.isDirectory())
                {
                    continue;
                }
                f = new File(dir, ".pdfsplit.cfg");
                if (f.isFile())
                {
                    res.add(f.getAbsoluteFile());
                }

                f = new File(dir, "pdfsplit.cfg");
                if (f.isFile())
                {
                    res.add(f.getAbsoluteFile());
                }
            }
        }
        return res;
    }


    /**
     * Load the general config for the application. This is a Properties
     * instance filled will String key/value pairs. <br/>
     * The config file is taken from the result of the call to
     * {@link #getConfigFile()}.<br/>
     * 
     * To ensure the result is never uninitialized, the config is created by
     * calling {@link #createDefaultConfig()} and therefore initialized with
     * default values before actual values are loaded.
     * 
     * @return the config as available.
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static Config loadConfig() throws FileNotFoundException, IOException
    {
        Config res = createDefaultConfig();

        List<File> cfgFiles = getConfigFiles();
        loadConfig(res, cfgFiles.toArray(new File[cfgFiles.size()]));
        return res;
    }


    /**
     * Fill the given config with values from the given files.
     * 
     * @param aConfig
     *            the config to fill values in.
     * @param aFiles
     *            the files to read.
     * @throws FileNotFoundException
     *             in case one of the given files is not found.
     * @throws IOException
     *             in case of any read error.
     */
    public static void loadConfig(Config aConfig, File... aFiles)
        throws FileNotFoundException, IOException
    {
        for (File cfgFile : aFiles)
        {
            LOGGER.log(Level.INFO, "Will load config from {0}.", cfgFile);
            try (FileReader rd = new FileReader(cfgFile))
            {
                aConfig.load(rd);
            }
        }
    }


    /**
     * 
     * @return the config file to write user based config values to.
     */
    public static File getConfigFileForWrite()
    {
        List<String> envVars = Arrays.asList("HOME", "USERPROFILE");
        for (String envVar : envVars)
        {
            String val = System.getenv(envVar);
            if (val != null)
            {
                File dir = new File(val);
                if (dir.isDirectory())
                {
                    return new File(dir, "pdfsplit.cfg").getAbsoluteFile();
                }
            }
        }

        return new File(".", "pdfsplit.cfg").getAbsoluteFile();
    }


    /**
     * Save the actual config to the default user based config file. This method
     * writes to the file returned by {@link #getConfigFileForWrite()}.
     * 
     * @throws IOException
     *             on write error.
     */
    public static void saveConfig(Config aConfig) throws IOException
    {
        saveConfig(aConfig, getConfigFileForWrite());
    }


    /**
     * Save the actual config to the given config file.
     * 
     * @param aConfig
     *            the config to write.
     * @param aFile
     *            the file to write to.
     * @throws IOException
     *             on write error.
     */
    public static void saveConfig(Config aConfig, File aFile) throws IOException
    {
        try (FileWriter wr = new FileWriter(aFile))
        {
            aConfig.store(wr, "# Config file for PDF Splitter Application");
        }
    }


    /**
     * Create a config Properties instance that is filled with default values.
     * 
     * @return a newly created and initially filled config.
     */
    public static Config createDefaultConfig()
    {
        Config res = new Config();
        res.put(PROP_SEPARATOR_TEXT, DEFAULT_SEP);
        res.put(PROP_SEPARATOR_MATCH_COUNT, String.valueOf(1));
        String exedir = System.getProperty("launch4j.exedir");
        if (exedir == null)
        {
            exedir = new File(".").getAbsolutePath();
        }
        res.put(PROP_DIRECTORY_OPEN, exedir);
        res.put(PROP_SEPARATOR_DO_OCR, String.valueOf(true));
        res.put(PROP_SEPARATOR_FORCE_OCR, String.valueOf(false));

        res.put(PROP_FILTER_DO_OCR, String.valueOf(true));
        res.put(PROP_SEPARATOR_USE_TEXT, String.valueOf(true));
        res.put(PROP_SEPARATOR_USE_QR, String.valueOf(true));

        res.put(PROP_SEPARATOR_QR_CODE, DEFAULT_QR_CODE);
        res.put(PROP_OCR_DATAPATH, "./tessdata");
        res.put(PROP_OCR_LANG, "deu+eng");
        res.put(PROP_OCR_ENGINE_MODE, "3");
        res.put(PROP_OCR_IMG_SCALE, "2.5");

        res.put(PROP_FILTER_DO_EMPTY_PAGE, String.valueOf(true));
        res.put(PROP_FILTER_EMPTY_PAGE_TH_PIXEL, "25");
        res.put(PROP_FILTER_EMPTY_PAGE_TH_BLOCK, "2");
        res.put(PROP_FILTER_EMPTY_PAGE_TH_PAGE, "6");
        res.put(PROP_FILTER_EMPTY_PAGE_BLOCKCOUNT_H, "10");
        res.put(PROP_FILTER_EMPTY_PAGE_BLOCKCOUNT_V, "10");

        return res;

    }


    /**
     * Create a new empty config.
     */
    public Config()
    {

    }


    /**
     * Create a new instance as copy of the given one.
     * 
     * @param aConfig
     *            the config instance to copy from.
     */
    public Config(Config aConfig)
    {
        super(aConfig != null ? aConfig.defaults : null);
        if (aConfig != null)
        {
            for (Object aKey : aConfig.keySet())
            {
                setProperty(aKey.toString(),
                        aConfig.getProperty(aKey.toString()));
            }
        }
    }


    /**
     * Retrieve a value of the config as int.
     * 
     * @param aKey
     *            the value key.
     * @param aDefault
     *            the default to be returned in case the key is not available or
     *            value can not be parsed to int.
     * @return the value for the given key or the given default.
     */
    public int getConfigValI(String aKey, int aDefault)
    {
        try
        {
            return Integer.valueOf(getProperty(aKey));
        }
        catch (Exception ex)
        {
            return aDefault;
        }
    }


    /**
     * Set a config item to an int(eger) value. The value will be internally
     * stored as String using {@link String#valueOf(int)}.
     * 
     * @param aKey
     *            the config key to set the value for.
     * @param aValue
     *            the new value.
     */
    public void setConfigValI(String aKey, int aValue)
    {
        setProperty(aKey, String.valueOf(aValue));
    }


    /**
     * Retrieve a value of the config as float.
     * 
     * @param aKey
     *            the value key.
     * @param aDefault
     *            the default to be returned in case the key is not available or
     *            value can not be parsed to float.
     * @return the value for the given key or the given default.
     */
    public float getConfigValF(String aKey, float aDefault)
    {
        try
        {
            return Float.valueOf(getProperty(aKey));
        }
        catch (Exception ex)
        {
            return aDefault;
        }
    }


    /**
     * Set a config item to a float value. The value will be internally stored
     * as String using {@link String#valueOf(float)}.
     * 
     * @param aKey
     *            the config key to set the value for.
     * @param aValue
     *            the new value.
     */
    public void setConfigValF(String aKey, float aValue)
    {
        setProperty(aKey, String.valueOf(aValue));
    }


    /**
     * Retrieve a value of the config as boolean.
     * 
     * @param aKey
     *            the value key.
     * @param aDefault
     *            the default to be returned in case the key is not available or
     *            value can not be parsed to boolean.
     * @return the value for the given key or the given default.
     */
    public boolean getConfigValB(String aKey, boolean aDefault)
    {
        String strVal = getProperty(aKey);
        if (strVal == null)
        {
            return aDefault;
        }
        return strVal.equalsIgnoreCase("1") || strVal.equalsIgnoreCase("yes")
                || strVal.equalsIgnoreCase("true")
                || strVal.equalsIgnoreCase(String.valueOf(true));
    }


    /**
     * Set a config item to a boolean value. The value will be internally stored
     * as String using {@link String#valueOf(boolean)}.
     * 
     * @param aKey
     *            the config key to set the value for.
     * @param aValue
     *            the new value.
     */
    public void setConfigValB(String aKey, boolean aValue)
    {
        setProperty(aKey, String.valueOf(aValue));
    }


    /**
     * Retrieve a value of the config as String.
     * 
     * @param aKey
     *            the value key.
     * @param aDefault
     *            the default to be returned in case the key is not available or
     *            value can not be parsed to String.
     * @return the value for the given key or the given default.
     */
    public String getConfigValS(String aKey, String aDefault)
    {
        String strVal = getProperty(aKey);
        return (strVal != null) ? strVal : aDefault;
    }


    /**
     * Set a config item to a String value.
     * 
     * @param aKey
     *            the config key to set the value for.
     * @param aValue
     *            the new value.
     */
    public void setConfigValS(String aKey, String aValue)
    {
        if (aValue == null)
        {
            remove(aKey);
        }
        else
        {
            setProperty(aKey, aValue);
        }
    }
}
