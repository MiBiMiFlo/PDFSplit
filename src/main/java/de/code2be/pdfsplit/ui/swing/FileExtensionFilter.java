package de.code2be.pdfsplit.ui.swing;

import java.io.File;
import java.security.InvalidParameterException;

import javax.swing.filechooser.FileFilter;

/**
 * A simple file extension filter that filters for a given file extension.
 * Filter is always case insensitive.
 * 
 * @author Michael Weiss
 *
 */
public class FileExtensionFilter extends FileFilter
{

    /**
     * The file name extension.
     */
    private final String mExtension;

    private final String mDescription;

    public FileExtensionFilter(String aExtension, String aDescription)
    {
        if (aExtension == null)
        {
            throw new InvalidParameterException(
                    "aExtension is not allowed to be null!");
        }
        mExtension = aExtension;
        if (aDescription != null)
        {
            mDescription = aDescription;
        }
        else
        {
            mDescription = aExtension;
        }
    }


    @Override
    public boolean accept(File aF)
    {
        if (aF.isDirectory())
        {
            return true;
        }
        String name = aF.getName();
        if (name.length() < mExtension.length() + 1)
        {
            return false;
        }
        return name.regionMatches(true, name.length() - mExtension.length(),
                mExtension, 0, mExtension.length());
    }


    @Override
    public String getDescription()
    {
        return mDescription;
    }

}
