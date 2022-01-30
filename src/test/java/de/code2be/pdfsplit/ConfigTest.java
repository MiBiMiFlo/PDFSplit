package de.code2be.pdfsplit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ConfigTest
{

    static Map<File, File> mCfgFileRenameMap = new HashMap<>();

    @BeforeAll
    static void setUpBeforeClass() throws Exception
    {
        List<File> cfgFiles = Config.getConfigFiles();

        System.err.println("Will run in " + (new File(".").getAbsolutePath()));

        for (File f : cfgFiles)
        {
            if (f.isFile())
            {
                File f2 = new File(f.getParentFile(),
                        f.getName() + ".old4unittest");
                mCfgFileRenameMap.put(f, f2);

                if (f2.exists())
                {
                    throw new IllegalStateException(
                            "Can not rename config file "
                                    + f.getAbsolutePath());
                }
            }
        }

        for (Entry<File, File> e : mCfgFileRenameMap.entrySet())
        {
            File f = e.getKey();
            File f2 = e.getValue();
            if (!f.renameTo(f2))
            {
                System.err.println("Can not rename " + f.getAbsolutePath()
                        + " to " + f2.getAbsolutePath());
            }
        }
        System.err.println("Rename of config files done!");

    }


    @AfterAll
    static void tearDownAfterClass() throws Exception
    {
        for (Entry<File, File> e : mCfgFileRenameMap.entrySet())
        {
            File f = e.getValue();
            File f2 = e.getKey();

            if (!f.renameTo(f2))
            {
                System.err.println("Can not undo rename of "
                        + f.getAbsolutePath() + " to " + f2.getAbsolutePath());
            }
        }
        System.err.println("Rename of config files reverted!");

    }


    @Test
    void testConstructor()
    {
        Config cfg = new Config();
        assertEquals(0, cfg.size());

        cfg = new Config(null);
        assertEquals(0, cfg.size());

        cfg.setConfigValS("key1", "test1");
        cfg.setConfigValS("key2", "test2");
        assertEquals(2, cfg.size());

        Config cfg2 = new Config(cfg);
        assertEquals(2, cfg2.size());

        assertEquals("test1", cfg.getConfigValS("key1", null));
        assertEquals("test2", cfg.getConfigValS("key2", null));

    }


    @Test
    void testGetConfigFiles()
    {
        List<File> cfgFiles = Config.getConfigFiles();

        assertEquals(0, cfgFiles.size());

        List<File> possibleFiles = new ArrayList<>();
        possibleFiles.add(new File("./pdfsplit.cfg").getAbsoluteFile());

        List<String> envVars = Arrays.asList("HOME", "USERPROFILE");
        for (String env : envVars)
        {
            if (System.getenv().containsKey(env))
            {
                String profileDir = System.getenv(env);
                possibleFiles.add(new File(profileDir, ".pdfsplit.cfg")
                        .getAbsoluteFile());
                possibleFiles.add(
                        new File(profileDir, "pdfsplit.cfg").getAbsoluteFile());
            }
        }

        for (File f : possibleFiles)
        {
            try
            {
                f.createNewFile();
            }
            catch (Exception ex)
            {
                fail("Can not create config file " + f.getAbsolutePath(), ex);
            }
        }

        cfgFiles = Config.getConfigFiles();

        try
        {
            assertEquals(possibleFiles.size(), cfgFiles.size());
            assertArrayEquals(possibleFiles.toArray(), cfgFiles.toArray());
        }
        finally
        {
            for (File f : possibleFiles)
            {
                f.delete();
            }
        }
    }


    @Test
    void testGetConfigFilesForWrite()
    {
        List<String> envVars = Arrays.asList("HOME", "USERPROFILE");
        for (String env : envVars)
        {
            if (System.getenv().containsKey(env))
            {
                String profileDir = System.getenv(env);
                File f = new File(profileDir, "pdfsplit.cfg").getAbsoluteFile();
                assertEquals(f,
                        Config.getConfigFileForWrite().getAbsoluteFile());
                try
                {
                    f.createNewFile();
                    assertEquals(f,
                            Config.getConfigFileForWrite().getAbsoluteFile());

                }
                catch (IOException ex)
                {
                    fail(ex);
                }
                finally
                {
                    f.delete();
                }
                break;
            }
        }
    }


    @Test
    void testSaveLoadConfig()
    {
        Config cfg = new Config();

        cfg.setConfigValS("key1", "Value 1");
        cfg.setConfigValS("key2", "Value 2");

        File f = null;
        try
        {
            f = File.createTempFile("unittest_", ".cfg");
            Config.saveConfig(cfg, f);

            Config cfg2 = new Config();

            Config.loadConfig(cfg2, f);

            assertEquals(cfg.size(), cfg2.size());
            for (Object key : cfg.keySet())
            {
                assertEquals(cfg.getConfigValS(key.toString(), null),
                        cfg2.getConfigValS(key.toString(), null));
            }
        }
        catch (IOException ex)
        {
            fail(ex);
        }
        finally
        {
            if (f != null)
            {
                f.delete();
            }
        }
    }


    @Test
    void testSaveLoadConfig2()
    {
        Config cfg = new Config();

        cfg.setConfigValS("key1", "Value 1");
        cfg.setConfigValS("key2", "Value 2");

        File f = Config.getConfigFileForWrite();
        assertEquals(false, f.isFile());
        try
        {
            Config.saveConfig(cfg);

            assertEquals(true, f.isFile());
            Config cfg2 = Config.loadConfig();

            assertEquals(cfg.getConfigValS("key1", null),
                    cfg2.getConfigValS("key1", null));
            assertEquals(cfg.getConfigValS("key2", null),
                    cfg2.getConfigValS("key2", null));

        }
        catch (IOException ex)
        {
            fail(ex);
        }
        finally
        {
            if (f != null)
            {
                f.delete();
            }
        }
    }


    @Test
    void testCreateDefaultConfig()
    {
        Config cfg = Config.createDefaultConfig();

        Field[] fields = Config.class.getDeclaredFields();
        for (Field f : fields)
        {
            if ((Class<?>) f.getType() != (Class<?>) String.class)
            {
                continue;
            }
            if (f.getName() == "PROP_DIRECTORY_SAVE"
                    || !f.getName().startsWith("PROP_"))
            {
                continue;
            }
            f.setAccessible(true);
            try
            {
                assertNotNull(cfg.getProperty((String) f.get(null)),
                        f.getName());
            }
            catch (IllegalAccessException ex)
            {
                fail("Default value for " + f.getName() + " not set!");
            }
        }

        assertEquals("./tessdata", cfg.getProperty(Config.PROP_OCR_DATAPATH));
        assertEquals("deu+eng", cfg.getProperty(Config.PROP_OCR_LANG));
        assertEquals("3", cfg.getProperty(Config.PROP_OCR_ENGINE_MODE));
        assertEquals(Config.DEFAULT_QR_CODE,
                cfg.getProperty(Config.PROP_SEPARATOR_QR_CODE));
        assertEquals(Config.DEFAULT_SEP,
                cfg.getProperty(Config.PROP_SEPARATOR_TEXT));

        // TODO: check some more items
    }


    @Test
    void testSetGetConfigValI()
    {
        Config cfg = new Config();
        cfg.setConfigValI(Config.PROP_DIRECTORY_OPEN, 17);
        assertEquals(17, cfg.getConfigValI(Config.PROP_DIRECTORY_OPEN, 0));

        cfg.setConfigValI(Config.PROP_DIRECTORY_OPEN, 23);
        assertEquals(23, cfg.getConfigValF(Config.PROP_DIRECTORY_OPEN, 0));

        cfg.setConfigValI(Config.PROP_OCR_DATAPATH, 11);
        assertEquals(11, cfg.getConfigValF(Config.PROP_OCR_DATAPATH, 11));

        cfg.setConfigValS("invalid", null);
        assertEquals(5, cfg.getConfigValI("invalid", 5));
        cfg.setConfigValS("invalid", "Hello");
        assertEquals(10, cfg.getConfigValI("invalid", 10));

    }


    @Test
    void testSetGetConfigValF()
    {
        Config cfg = new Config();
        cfg.setConfigValF(Config.PROP_DIRECTORY_OPEN, 1.5f);
        assertEquals(1.5f, cfg.getConfigValF(Config.PROP_DIRECTORY_OPEN, 0.0f));

        cfg.setConfigValF(Config.PROP_DIRECTORY_OPEN, 3.6f);
        assertEquals(3.6f, cfg.getConfigValF(Config.PROP_DIRECTORY_OPEN, 0.0f));

        cfg.setConfigValF(Config.PROP_OCR_DATAPATH, 7.8f);
        assertEquals(7.8f, cfg.getConfigValF(Config.PROP_OCR_DATAPATH, 7.8f));

        cfg.setConfigValS("invalid", null);
        assertEquals(0.1f, cfg.getConfigValF("invalid", 0.1f));
        cfg.setConfigValS("invalid", "Hello");
        assertEquals(0.2f, cfg.getConfigValF("invalid", 0.2f));
    }


    @Test
    void testSetGetConfigValB()
    {
        Config cfg = new Config();
        cfg.setConfigValB(Config.PROP_DIRECTORY_OPEN, true);
        assertEquals(true,
                cfg.getConfigValB(Config.PROP_DIRECTORY_OPEN, false));

        cfg.setConfigValB(Config.PROP_DIRECTORY_OPEN, false);
        assertEquals(false,
                cfg.getConfigValB(Config.PROP_DIRECTORY_OPEN, true));

        cfg.setConfigValB(Config.PROP_OCR_DATAPATH, true);
        assertEquals(true, cfg.getConfigValB(Config.PROP_OCR_DATAPATH, true));

        assertEquals(true, cfg.getConfigValB("invalid", true));
        assertEquals(false, cfg.getConfigValB("invalid", false));

        List<String> trueVals = Arrays.asList("1", "yes", "YES", "TRUE", "true",
                String.valueOf(true));
        for (String trueVal : trueVals)
        {
            cfg.setConfigValS("test1", trueVal);
            assertEquals(true, cfg.getConfigValB("test1", false));
        }
    }


    @Test
    void testSetGetConfigValS()
    {
        Config cfg = new Config();
        cfg.setConfigValS(Config.PROP_DIRECTORY_OPEN, "C:\\");
        assertEquals("C:\\",
                cfg.getConfigValS(Config.PROP_DIRECTORY_OPEN, null));

        cfg.setConfigValS(Config.PROP_DIRECTORY_OPEN, "D:\\");
        assertEquals("D:\\",
                cfg.getConfigValS(Config.PROP_DIRECTORY_OPEN, null));

        cfg.setConfigValS(Config.PROP_OCR_DATAPATH, "./testDataPath");
        assertEquals("./testDataPath",
                cfg.getConfigValS(Config.PROP_OCR_DATAPATH, null));
        assertEquals(null, cfg.getConfigValS("invalid", null));
        assertEquals("default", cfg.getConfigValS("invalid", "default"));

    }

}
