package de.code2be.pdfsplit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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

    public static final String DEFAULT_SEP = "$PWKM%U?5X4$;PDF-SPLIT-SPLIT-PAGE;PDF-SPLIT-TRENNSEITE";

    public static final String DEFAULT_QR_CODE = "https://github.com/MiBiMiFlo/PDFSplit";

    public static final String PROP_DIRECTORY_OPEN = "main.dirOpen";

    public static final String PROP_DIRECTORY_SAVE = "main.dirSave";

    public static final String PROP_SEPARATOR_USE_TEXT = "separator.useText";

    public static final String PROP_SEPARATOR_USE_QR = "separator.useQR";

    public static final String PROP_SEPARATOR_TEXT = "separator.text";

    public static final String PROP_SEPARATOR_MATCH_COUNT = "separator.matchCount";

    public static final String PROP_SEPARATOR_QR_CODE = "separator.qrcode";

    public static final String PROP_SEPARATOR_DO_OCR = "separator.ocr.enable";

    public static final String PROP_SEPARATOR_FORCE_OCR = "separator.ocr.force";

    public static final String PROP_FILTER_DO_OCR = "filter.ocr.enable";

    public static final String PROP_FILTER_DO_EMPTY_PAGE = "filter.emptyPage.enable";

    public static final String PROP_FILTER_EMPTY_PAGE_TH_PIXEL = "filter.emptyPage.th.pixel";

    public static final String PROP_FILTER_EMPTY_PAGE_TH_BLOCK = "filter.emptyPage.th.block";

    public static final String PROP_FILTER_EMPTY_PAGE_TH_PAGE = "filter.emptyPage.th.page";

    public static final String PROP_FILTER_EMPTY_PAGE_BLOCKCOUNT_H = "filter.emptyPage.blockCountH";

    public static final String PROP_FILTER_EMPTY_PAGE_BLOCKCOUNT_V = "filter.emptyPage.blockCountV";

    public static final String PROP_OCR_DATAPATH = "ocr.datapath";

    public static final String PROP_OCR_LANG = "ocr.language";

    public static final String PROP_OCR_ENGINE_MODE = "ocr.engineMode";

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
        String profileDir = ".";
        File f = new File(".", "pdfsplit.cfg").getAbsoluteFile();
        if (f.isFile())
        {
            res.add(f);
        }

        if (System.getenv().containsKey("HOME"))
        {
            profileDir = System.getenv("HOME");
            f = new File(profileDir, ".pdfsplit.cfg");
            if (f.isFile())
            {
                res.add(f);
            }

            f = new File(profileDir, "pdfsplit.cfg");
            if (f.isFile())
            {
                res.add(f);
            }
        }
        if (System.getenv().containsKey("USERPROFILE"))
        {
            profileDir = System.getenv("USERPROFILE");
            f = new File(profileDir, "pdfsplit.cfg");
            if (f.isFile())
            {
                res.add(f);
            }
            f = new File(profileDir, ".pdfsplit.cfg");
            if (f.isFile())
            {
                res.add(f);
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
        System.getenv(DEFAULT_SEP);
        Config res = createDefaultConfig();

        List<File> cfgFiles = getConfigFiles();

        for (File cfgFile : cfgFiles)
        {
            LOGGER.log(Level.INFO, "Will load config from {0}.", cfgFile);
            try (FileReader rd = new FileReader(cfgFile))
            {
                res.load(rd);
            }
        }

        return res;
    }


    public static File getConfigFileForWrite()
    {
        String profileDir = ".";
        File f = null;
        if (System.getenv().containsKey("HOME"))
        {
            profileDir = System.getenv("HOME");
            f = new File(profileDir, "pdfsplit.cfg");
            if (f.isFile())
            {
                return f;
            }
            return new File(profileDir, ".pdfsplit.cfg");
        }
        else if (System.getenv().containsKey("USERPROFILE"))
        {
            profileDir = System.getenv("USERPROFILE");
            f = new File(profileDir, "pdfsplit.cfg");
            if (f.isFile())
            {
                return f;
            }
            return new File(profileDir, ".pdfsplit.cfg");
        }
        f = new File(".", "pdfsplit.cfg").getAbsoluteFile();
        return f.isFile() ? f : null;
    }


    /**
     * Save the actual config to the default user based config file.
     * 
     * @throws IOException
     */
    public static void saveConfig(Config aConfig) throws IOException
    {
        File f = getConfigFileForWrite();
        try (FileWriter wr = new FileWriter(f))
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
        res.put(PROP_FILTER_EMPTY_PAGE_TH_PIXEL, "6");
        res.put(PROP_FILTER_EMPTY_PAGE_BLOCKCOUNT_H, "10");
        res.put(PROP_FILTER_EMPTY_PAGE_BLOCKCOUNT_V, "10");

        return res;

    }


    public Config()
    {

    }


    public Config(Config aConfig)
    {
        super(aConfig.defaults);
        for (Object aKey : aConfig.keySet())
        {
            setProperty(aKey.toString(), aConfig.getProperty(aKey.toString()));
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
        catch (Exception e)
        {
            return aDefault;
        }
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
        catch (Exception e)
        {
            return aDefault;
        }
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
        try
        {
            String strVal = getProperty(aKey);
            if (strVal == null)
            {
                return aDefault;
            }
            return strVal.equalsIgnoreCase("1")
                    || strVal.equalsIgnoreCase("yes")
                    || strVal.equalsIgnoreCase(String.valueOf(true));
        }
        catch (Exception e)
        {
            return aDefault;
        }
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
        try
        {
            String strVal = getProperty(aKey);
            return (strVal != null) ? strVal : aDefault;
        }
        catch (Exception e)
        {
            return aDefault;
        }

    }

}
