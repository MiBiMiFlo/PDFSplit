package de.code2be.pdfsplit;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class SplitStatusPanel extends JPanel implements ISplitStatusListener
{

    private static final long serialVersionUID = 7809451586155846558L;

    private JProgressBar mProgress;

    private JLabel mStatusLabel;

    private SmartSplitter mSplitter;

    public SplitStatusPanel(SmartSplitter aSplitter)
    {
        mSplitter = aSplitter;
        mSplitter.setStatusListener(this);
        mProgress = new JProgressBar();
        mStatusLabel = new JLabel();

        setLayout(new BorderLayout());
        add(mStatusLabel, BorderLayout.NORTH);
        add(mProgress, BorderLayout.CENTER);

        JPanel pnl = new JPanel();

        JButton btnAbort = new JButton();
    }


    @Override
    public void splitStatusUpdate(SplitStatusEvent aEvent)
    {
        mProgress.setMaximum(aEvent.getPageCount());

    }

    private class AbortAction extends AbstractAction
    {

        public AbortAction()
        {
            putValue(NAME, "Abort");
        }


        @Override
        public void actionPerformed(ActionEvent aE)
        {
            mSplitter.doAbort();
        }
    }
}
