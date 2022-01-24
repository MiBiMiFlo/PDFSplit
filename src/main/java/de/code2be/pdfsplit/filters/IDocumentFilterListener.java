package de.code2be.pdfsplit.filters;

/**
 * A listener interface for listeners that wait for
 * {@link DocumentFilterEvent}'s from {@link IDocumentFilter}'s.
 * 
 * @author Michael Weiss
 *
 */
public interface IDocumentFilterListener
{

    void documentFilterEvent(DocumentFilterEvent aEvent);
}
