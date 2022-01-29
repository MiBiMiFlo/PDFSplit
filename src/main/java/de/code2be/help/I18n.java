package de.code2be.help;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Helper class for i18n stuff.
 *
 * @author Michael Weiss
 *
 */
public final class I18n
{

    /**
     * This constructor only exists for 100% code coverage in JUnit tests.
     * 
     * @return an new instance.
     */
    protected static final I18n createInstance()
    {
        return new I18n();
    }


    public I18n()
    {

    }


    /**
     * Fetch a formatted and localized message for a given class and a key in
     * the resource file. There has be exist a resource file with the same name
     * as the class in the same package.
     *
     * @param aClass
     *            the class to fetch the string for.
     * @param aKey
     *            the key inside the resource file.
     * @param aObjects
     *            the objects to use for formatting.
     * @return the formatted and localized string.
     */
    public static final String getMessage(Class<?> aClass, String aKey,
            Object... aObjects)
    {
        try
        {
            return getMessage(aClass.getName(), aKey, aObjects);
        }
        catch (RuntimeException ex)
        {
            String msg = getMessage0(aClass.getSuperclass(), aKey, aObjects);
            if (msg == null)
            {
                throw ex;
            }
            return msg;
        }
    }


    private static final String getMessage0(Class<?> aClass, String aKey,
            Object... aObjects)
    {
        String msg = null;
        try
        {
            msg = getMessage(aClass.getName(), aKey, aObjects);
        }
        catch (Exception ex)
        {
        }
        if (msg == null)
        {
            Class<?> superClass = aClass.getSuperclass();
            if (superClass != null)
            {
                msg = getMessage0(superClass, aKey, aObjects);
            }
        }
        return msg;
    }


    /**
     * Fetch a localized message for a given resource file and a key in the
     * resource file.
     *
     * @param aResource
     *            the resource file to use.
     * @param aKey
     *            the key to fetch the string from.
     * @return the localized string.
     */
    public static final String getMessage(String aResource, String aKey)
    {
        return getMessage(aResource, aKey, new Object[0]);
    }


    /**
     * Fetch a formatted and localized message for a given resource file and a
     * key in the resource file.
     *
     * @param aResource
     *            the resource file to use.
     * @param aKey
     *            the key to fetch the string from.
     * @param aObjects
     *            the objects to use for formatting.
     * @return the formatted and localized string.
     */
    public static final String getMessage(String aResource, String aKey,
            Object... aObjects)
    {
        ResourceBundle rb = ResourceBundle.getBundle(aResource);
        String msg = rb.getString(aKey);
        if (aObjects == null || aObjects.length == 0)
        {
            return msg;
        }
        return MessageFormat.format(msg, aObjects);
    }
}
