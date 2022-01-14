package de.code2be.pdfsplit.ui.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

/**
 * A simple {@link LayoutManager} that aligns all components vertically.
 *
 * @author Michael Weiss
 *
 */
public class ListLayout implements LayoutManager
{

    /**
     * Align components horizontally left.
     */
    public static final int LEFT = 1;

    /**
     * Align components horizontally right.
     */
    public static final int RIGHT = 2;

    /**
     * Align components horizontally centered.
     */
    public static final int CENTER = 3;

    public static final int NO_STRETCH = 0;

    public static final int STRETCH_HORIZONTAL = 1;

    public static final int STRETCH_RATIO = 2;

    /**
     * The gap to leave between components.
     */
    private int mGap;

    /**
     * The actual alignment.
     */
    private int mAlignment;

    private int mStretchMode = NO_STRETCH;

    public ListLayout()
    {
        this(5);
    }


    public ListLayout(int aGap)
    {
        this(5, CENTER);
    }


    public ListLayout(int aGap, int aAlignment)
    {
        this(aGap, aAlignment, NO_STRETCH);
    }


    public ListLayout(int aGap, int aAlignment, int aStrechMode)
    {
        setGap(aGap);
        setAlignment(aAlignment);
        setStretchMode(aStrechMode);
    }


    /**
     * Set the vertical gap between components.
     *
     * @param aGap
     *            the gap to use.
     */
    public void setGap(int aGap)
    {
        mGap = aGap;
    }


    /**
     *
     * @return the actual gap.
     */
    public int getGap()
    {
        return mGap;
    }


    /**
     * Set the alignment to use.
     *
     * @param aAlignment
     *            the alignment. Can be one of {@link #LEFT}, {@link #RIGHT},
     *            {@link #CENTER}.
     */
    public void setAlignment(int aAlignment)
    {
        mAlignment = aAlignment;
    }


    /**
     *
     * @return the actual used alignment.
     */
    public int getAlignment()
    {
        return mAlignment;
    }


    public void setStretchMode(int aStretchMode)
    {
        mStretchMode = aStretchMode;
    }


    public int getStretchMode()
    {
        return mStretchMode;
    }


    @Override
    public void addLayoutComponent(String aName, Component aComp)
    {
        // we don't care about this
    }


    /**
     * Calculate the requested container size.
     *
     * @param aParent
     *            the container to calculate the size for.
     * @param aUsePreferred
     *            a flag to indicate if minimum or preferred size should be
     *            calculated.
     * @return a dimension that is the requested size.
     */
    private Dimension calculateSize(Container aParent, boolean aUsePreferred)
    {
        synchronized (aParent.getTreeLock())
        {
            Dimension dim = new Dimension(0, 0);
            int nmembers = aParent.getComponentCount();

            for (int i = 0; i < nmembers; i++)
            {
                Component m = aParent.getComponent(i);
                if (m.isVisible())
                {
                    Dimension d;

                    if (aUsePreferred)
                    {
                        d = m.getPreferredSize();
                        // on preferred size we consider max size as limiting
                        Dimension max = m.getMaximumSize();

                        if (d.height > max.height)
                        {
                            d.height = max.height;
                        }
                        if (d.width > max.width)
                        {
                            d.width = max.width;
                        }
                    }
                    else
                    {
                        d = m.getMinimumSize();
                    }

                    dim.height += d.height + mGap;
                    dim.width = Math.max(dim.width, d.width);
                }
            }
            dim.height -= mGap;
            Insets insets = aParent.getInsets();
            dim.width += insets.left + insets.right;
            dim.height += insets.top + insets.bottom;
            return dim;
        }
    }


    @Override
    public void layoutContainer(Container aParent)
    {
        synchronized (aParent.getTreeLock())
        {
            int nmembers = aParent.getComponentCount();
            Insets insets = aParent.getInsets();
            int width = aParent.getWidth() - (insets.left + insets.right);
            int height = aParent.getHeight() - (insets.top + insets.bottom);
            int y = insets.top;
            for (int i = 0; i < nmembers; i++)
            {
                Component m = aParent.getComponent(i);
                if (m.isVisible())
                {
                    Dimension d = m.getPreferredSize();
                    Dimension max = m.getMaximumSize();
                    if (d.width > width)
                    {
                        d.width = width;
                    }
                    if (d.width < width)
                    {
                        if (mStretchMode == STRETCH_HORIZONTAL)
                        {
                            d.width = width;
                        }
                        else if (mStretchMode == STRETCH_RATIO)
                        {
                            double ratio = ((double) width)
                                    / ((double) d.width);
                            d.width = width;
                            d.height = (int) (d.height * ratio);

                        }
                    }

                    if (mStretchMode == NO_STRETCH)
                    {
                        // ensure we do not resize over the maximum size.
                        if (d.width > max.width)
                        {
                            d.width = max.width;
                        }
                        if (d.height > max.height)
                        {
                            d.height = max.height;
                        }
                    }

                    int offs;
                    switch (mAlignment)
                    {
                    case LEFT:
                        offs = 0;
                        break;
                    case RIGHT:
                        offs = Math.max(0, (width - d.width));
                        break;
                    default:
                        offs = Math.max(0, (width - d.width) / 2);
                        break;
                    }
                    int x = insets.left + offs;
                    m.setLocation(x, y);
                    if ((y + d.height) > height)
                    {
                        Dimension min = m.getMinimumSize();
                        int newHeight = height - y;

                        if (newHeight < min.height)
                        {
                            newHeight = min.height;
                        }
                        if (newHeight != d.height)
                        {
                            d.height = newHeight;
                        }
                    }
                    m.setSize(d.width, d.height);
                    y += d.height + mGap;
                }
            }
        }
    }


    @Override
    public Dimension minimumLayoutSize(Container aParent)
    {
        return calculateSize(aParent, false);
    }


    @Override
    public Dimension preferredLayoutSize(Container aParent)
    {
        return calculateSize(aParent, true);
    }


    @Override
    public void removeLayoutComponent(Component aComp)
    {
        // we don't care about this
    }

}
