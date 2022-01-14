package de.code2be.pdfsplit.ui.swing;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class FileExtensionFilter extends FileFilter
{

    private final String mExtension;

    private final String mDescription;

    public FileExtensionFilter(String aExtension, String aDescription)
    {
        mExtension = aExtension;
        mDescription = aDescription;
    }


    @Override
    public boolean accept(File aF)
    {
        if (aF.isDirectory())
        {
            return true;
        }
        String name = aF.getName();
        return name.regionMatches(true, name.length() - mExtension.length(),
                mExtension, 0, mExtension.length());
    }


    @Override
    public String getDescription()
    {
        return mDescription;
    }

}
