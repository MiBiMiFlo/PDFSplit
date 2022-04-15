package de.code2be.pdfsplit.ui.swing.actions;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import de.code2be.pdfsplit.ui.swing.PDFSplitFrame;

public class ZoomInAction extends BasicAction
{

    private static final long serialVersionUID = -1592964331110414714L;

    public static final String ACTION_NAME = "ZOOM_IN";

    private final PDFSplitFrame mFrame;

    public ZoomInAction(PDFSplitFrame aFrame)
    {
        mFrame = aFrame;
        setCommandKey(ACTION_NAME);
    }


    @Override
    public void actionPerformed(ActionEvent aE)
    {
        Dimension oldSize = mFrame.getPreviewSize();

        Dimension newSize = new Dimension((int) (oldSize.getWidth() * 1.3f),
                (int) (oldSize.getHeight() * 1.3f));
        mFrame.setPreviewSize(newSize);
    }

}
