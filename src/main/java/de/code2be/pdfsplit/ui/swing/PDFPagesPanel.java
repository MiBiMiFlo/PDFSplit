package de.code2be.pdfsplit.ui.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.Scrollable;

/**
 * This is a special JPanel that will hold all {@link PDFPagePanel}'s of a
 * document. This panel is normally placed inside of an
 * {@link PDFDocumentPanel}.
 * 
 * @author Michael Weiss
 *
 */
public class PDFPagesPanel extends JPanel implements Scrollable
{

    private static final long serialVersionUID = -9001224467365315544L;

    private Dimension mPreviewSize = new Dimension(210, 295);

    public PDFPagesPanel()
    {
        super(new FlowWrapLayout(FlowLayout.LEFT, 5, 5));
    }


    public void setPreviewSize(Dimension aPanelSize)
    {
        mPreviewSize = aPanelSize;
        for (Component c : getComponents())
        {
            if (c instanceof PDFPagePanel)
            {
                PDFPagePanel pagePanel = (PDFPagePanel) c;
                pagePanel.setPreferredSize(mPreviewSize);
            }
        }
        revalidate();

    }


    public Dimension getPanelSize()
    {
        return mPreviewSize;
    }


    @Override
    public Dimension getPreferredScrollableViewportSize()
    {
        return getPreferredSize();
    }


    @Override
    public int getScrollableUnitIncrement(Rectangle aVisibleRect,
            int aOrientation, int aDirection)
    {
        return 10;
    }


    @Override
    public int getScrollableBlockIncrement(Rectangle aVisibleRect,
            int aOrientation, int aDirection)
    {
        return 100;
    }


    @Override
    public boolean getScrollableTracksViewportWidth()
    {
        return true;
    }


    @Override
    public boolean getScrollableTracksViewportHeight()
    {
        return false;
    }

}
