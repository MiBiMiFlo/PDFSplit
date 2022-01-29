package de.code2be.help;

import static org.junit.jupiter.api.Assertions.*;

import java.util.MissingResourceException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class I18nTest extends I18nTestSuper
{

    @Test
    void testGetMessageClassOfQStringObjectArray()
    {
        assertEquals(I18n.getMessage(I18nTest.class, "main.test3.value", 10),
                "This is for one param: 10.");
        assertEquals(I18n.getMessage(I18nTest.class, "main.test4.value", 10, 20,
                "Hello"), "This is for three params: 10 - Hello - 20.");
        assertEquals(I18n.getMessage(I18nTest.class, "main.super.test1.value"),
                "This value overwrites I18nTestSuperSuper and is from I18nTestSuper");
        assertEquals(I18n.getMessage(I18nTest.class, "main.super.test2.value"),
                "This value is only in I18nTestSuperSuper");
        assertEquals(I18n.getMessage(I18nTest.class, "main.super.test3.value"),
                "This value is only in I18nTestSuper");

        MissingResourceException ex = Assertions
                .assertThrows(MissingResourceException.class, () -> {
                    I18n.getMessage(I18nTest.class,
                            "main.super.test4.notAvail");
                });

        assertEquals(ex.getMessage(),
                "Can't find resource for bundle java.util.PropertyResourceBundle, key main.super.test4.notAvail");
    }


    @Test
    void testGetMessageStringString()
    {
        assertEquals(
                I18n.getMessage(I18nTest.class.getName(), "main.test1.value"),
                "This is the value of test 1.");
        assertEquals(
                I18n.getMessage(I18nTest.class.getName(), "main.test2.value"),
                "This is the value of test 2.");
    }


    @Test
    void testGetMessageStringStringObjectArray()
    {
        assertEquals(I18n.getMessage(I18nTest.class.getName(),
                "main.test3.value", 10), "This is for one param: 10.");
        assertEquals(I18n.getMessage(I18nTest.class.getName(),
                "main.test3.value", new Object[0]),
                "This is for one param: {0}.");
        assertEquals(I18n.getMessage(I18nTest.class.getName(),
                "main.test3.value", (Object[]) null),
                "This is for one param: {0}.");

        assertEquals(
                I18n.getMessage(I18nTest.class.getName(), "main.test4.value",
                        10, 20, "Hello"),
                "This is for three params: 10 - Hello - 20.");
    }


    @Test
    void testCreateInstance()
    {
        assertEquals(I18n.createInstance().getClass(), I18n.class);
    }
}
