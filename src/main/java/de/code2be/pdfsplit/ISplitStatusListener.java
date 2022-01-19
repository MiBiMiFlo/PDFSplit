package de.code2be.pdfsplit;

import java.util.EventListener;

/**
 * A status listener that can receive events from {@link SmartSplitter}.
 * 
 * @author Michael Weiss
 *
 */
public interface ISplitStatusListener extends EventListener
{

    /**
     * Called whenever a split status update is encountered.
     * 
     * @param aEvent
     *            the status event object that describes the event that caused
     *            to call this method.
     */
    void splitStatusUpdate(SplitStatusEvent aEvent);
}
