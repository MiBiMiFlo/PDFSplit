package de.code2be.help;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import net.sourceforge.tess4j.Tesseract;

class TesseractFactoryTest
{

    @Test
    void testSetVariable()
    {
        TesseractFactory f = new TesseractFactory();
        f.setVariable("variable1", "Value1");
        f.setVariable("variable2", "Value2");
        assertEquals("Value1", f.getVariable("variable1"));
        assertEquals("Value2", f.getVariable("variable2"));

    }


    @Test
    void testSetLanguage()
    {
        TesseractFactory f = new TesseractFactory();
        f.setLanguage("eng");
        assertEquals("eng", f.getLanguage());
        f.setLanguage("deu");
        assertEquals("deu", f.getLanguage());
    }


    @Test
    void testSetDatapath()
    {
        TesseractFactory f = new TesseractFactory();
        f.setDatapath("./tessdata");
        assertEquals("./tessdata", f.getDatapath());
        f.setDatapath("./tessdata2");
        assertEquals("./tessdata2", f.getDatapath());
    }


    @Test
    void testSetPageSegnMode()
    {
        TesseractFactory f = new TesseractFactory();
        f.setPageSegnMode(1);
        assertEquals(1, f.getPageSegnMode());
        f.setPageSegnMode(5);
        assertEquals(5, f.getPageSegnMode());
    }


    @Test
    void testSetOcrEngineMode()
    {
        TesseractFactory f = new TesseractFactory();
        f.setOcrEngineMode(0);
        assertEquals(0, f.getOcrEngineMode());
        f.setOcrEngineMode(2);
        assertEquals(2, f.getOcrEngineMode());
    }


    @Test
    void testSetConfigs()
    {
        TesseractFactory f = new TesseractFactory();
        List<String> configs1 = new ArrayList<>();
        configs1.add("config1");
        configs1.add("config2");
        f.setConfigs(configs1);
        assertLinesMatch(configs1, f.getConfigList());
        List<String> configs2 = new ArrayList<>();
        configs1.add("config3");
        configs1.add("config4");
        f.setConfigs(configs2);
        assertLinesMatch(configs2, f.getConfigList());
    }


    private Object getFieldValue(Object aObject, String aFieldName)
    {
        return getFieldValue(aObject.getClass(), aObject, aFieldName);
    }


    private Object getFieldValue(Class<?> aClass, Object aObject,
            String aFieldName)
    {
        try
        {
            Field f = aClass.getDeclaredField(aFieldName);
            f.setAccessible(true);
            return f.get(aObject);
        }
        catch (NoSuchFieldException ex)
        {
            Class<?> c = aClass.getSuperclass();
            if (c != null)
            {
                return getFieldValue(c, aObject, aFieldName);
            }
            throw new RuntimeException(ex);
        }
        catch (IllegalArgumentException | IllegalAccessException ex)
        {
            throw new RuntimeException(ex);
        }
    }


    @Test
    void testCreateCloseableInstance1()
    {
        TesseractFactory f = new TesseractFactory();
        List<String> configs1 = new ArrayList<>();
        configs1.add("config1");
        configs1.add("config2");
        f.setConfigs(configs1);
        f.setDatapath("./tessdata");
        f.setLanguage("eng");
        f.setOcrEngineMode(2);
        f.setPageSegnMode(3);
        f.setVariable("variable1", "Value1");
        f.setVariable("variable2", "Value2");

        try (TesseractC tc = f.createCloseableInstance())
        {
            assertEquals("eng", getFieldValue(tc, "language"));
            assertEquals("./tessdata", getFieldValue(tc, "datapath"));
            assertEquals(3, getFieldValue(tc, "psm"));
            assertEquals(2, getFieldValue(tc, "ocrEngineMode"));

            Properties props = (Properties) getFieldValue(tc, "prop");
            assertEquals("Value1", props.getProperty("variable1"));
            assertEquals("Value2", props.getProperty("variable2"));

            @SuppressWarnings("unchecked")
            List<String> configs = (List<String>) getFieldValue(tc,
                    "configList");
            assertLinesMatch(configs1, configs);
        }
    }


    @Test
    void testCreateCloseableInstance2()
    {
        TesseractFactory f = new TesseractFactory();

        List<String> configs2 = new ArrayList<>();
        configs2.add("config3");
        configs2.add("config4");
        f.setConfigs(configs2);
        f.setDatapath("./tessdata2");
        f.setLanguage("deu");
        f.setOcrEngineMode(5);
        f.setPageSegnMode(6);
        f.setVariable("variable3", "Value3");
        f.setVariable("variable4", "Value4");

        try (TesseractC tc = f.createCloseableInstance())
        {
            assertEquals("deu", getFieldValue(tc, "language"));
            assertEquals("./tessdata2", getFieldValue(tc, "datapath"));
            assertEquals(6, getFieldValue(tc, "psm"));
            assertEquals(5, getFieldValue(tc, "ocrEngineMode"));

            Properties props = (Properties) getFieldValue(tc, "prop");
            assertEquals("Value3", props.getProperty("variable3"));
            assertEquals("Value4", props.getProperty("variable4"));

            @SuppressWarnings("unchecked")
            List<String> configs = (List<String>) getFieldValue(tc,
                    "configList");
            assertLinesMatch(configs2, configs);
        }

    }


    @Test
    void testCreateInstance()
    {
        TesseractFactory f = new TesseractFactory();
        List<String> configs1 = new ArrayList<>();
        configs1.add("config1");
        configs1.add("config2");
        f.setConfigs(configs1);
        f.setDatapath("./tessdata");
        f.setLanguage("eng");
        f.setOcrEngineMode(2);
        f.setPageSegnMode(3);
        f.setVariable("variable1", "Value1");
        f.setVariable("variable2", "Value2");

        Tesseract t = f.createInstance();

        assertEquals("eng", getFieldValue(t, "language"));
        assertEquals("./tessdata", getFieldValue(t, "datapath"));
        assertEquals(3, getFieldValue(t, "psm"));
        assertEquals(2, getFieldValue(t, "ocrEngineMode"));

        Properties props = (Properties) getFieldValue(t, "prop");
        assertEquals("Value1", props.getProperty("variable1"));
        assertEquals("Value2", props.getProperty("variable2"));

        @SuppressWarnings("unchecked")
        List<String> configs = (List<String>) getFieldValue(t, "configList");
        assertLinesMatch(configs1, configs);

    }

}
