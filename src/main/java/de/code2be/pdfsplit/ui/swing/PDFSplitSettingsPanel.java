package de.code2be.pdfsplit.ui.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.code2be.help.I18n;
import de.code2be.pdfsplit.Config;

import static de.code2be.pdfsplit.Config.*;

public class PDFSplitSettingsPanel extends JPanel
{

    private static final long serialVersionUID = 7486716557110400546L;

    private static final Logger LOGGER = Logger
            .getLogger(PDFSplitSettingsPanel.class.getName());

    private Map<String, JComponent> mSettingComponents = new HashMap<>();

    public PDFSplitSettingsPanel()
    {
        setLayout(new ListLayout(5, ListLayout.LEFT,
                ListLayout.STRETCH_HORIZONTAL));

        JPanel pnlGeneral = new JPanel(new ListLayout(5, ListLayout.LEFT,
                ListLayout.STRETCH_HORIZONTAL));
        pnlGeneral.setBorder(BorderFactory.createTitledBorder(
                I18n.getMessage(PDFSplitSettingsPanel.class, "panel.general")));
        pnlGeneral.add(createLabeledTextFor(PROP_DIRECTORY_OPEN));
        pnlGeneral.add(createLabeledTextFor(PROP_DIRECTORY_SAVE));

        add(pnlGeneral);

        JPanel pnlFilter = new JPanel(new ListLayout(5, ListLayout.LEFT,
                ListLayout.STRETCH_HORIZONTAL));
        pnlFilter.setBorder(BorderFactory.createTitledBorder(
                I18n.getMessage(PDFSplitSettingsPanel.class, "panel.filter")));
        pnlFilter.add(createLabeledCheckBoxFor(PROP_FILTER_DO_OCR,
                "Use OCR to add Text"));
        pnlFilter.add(createLabeledCheckBoxFor(PROP_FILTER_DO_EMPTY_PAGE,
                "Auto Filter Empty Pages"));
        pnlFilter.add(createLabeledTextFor(PROP_FILTER_EMPTY_PAGE_TH_PIXEL,
                "Threshold Pixel:"));
        pnlFilter.add(createLabeledTextFor(PROP_FILTER_EMPTY_PAGE_TH_BLOCK,
                "Threshold Block:"));
        pnlFilter.add(createLabeledTextFor(PROP_FILTER_EMPTY_PAGE_TH_PAGE,
                "Threshold Page:"));
        pnlFilter.add(createLabeledTextFor(PROP_FILTER_EMPTY_PAGE_BLOCKCOUNT_H,
                "Blocks Horizontal:"));
        pnlFilter.add(createLabeledTextFor(PROP_FILTER_EMPTY_PAGE_BLOCKCOUNT_V,
                "Blocks Vertical:"));

        add(pnlFilter);

        JPanel pnlSplit = new JPanel(new ListLayout(5, ListLayout.LEFT,
                ListLayout.STRETCH_HORIZONTAL));
        pnlSplit.setBorder(BorderFactory.createTitledBorder(
                I18n.getMessage(PDFSplitSettingsPanel.class, "panel.split")));
        JPanel pnl1 = createColumnPanel(
                createLabeledCheckBoxFor(PROP_SEPARATOR_USE_TEXT,
                        "Use Text Separator"),
                createLabeledCheckBoxFor(PROP_SEPARATOR_USE_QR,
                        "Search For QR Code"));
        pnlSplit.add(pnl1);
        pnlSplit.add(
                createLabeledTextFor(PROP_SEPARATOR_TEXT, "Separator Text"));

        JPanel pnl2 = createColumnPanel(
                createLabeledCheckBoxFor(PROP_SEPARATOR_DO_OCR, "Use OCR"),
                createLabeledCheckBoxFor(PROP_SEPARATOR_FORCE_OCR,
                        "Force OCR"));
        pnlSplit.add(pnl2);
        add(pnlSplit);

        JPanel pnlOcr = new JPanel(new ListLayout(5, ListLayout.LEFT,
                ListLayout.STRETCH_HORIZONTAL));
        pnlOcr.setBorder(BorderFactory.createTitledBorder(
                I18n.getMessage(PDFSplitSettingsPanel.class, "panel.ocr")));
        pnlOcr.add(createLabeledTextFor(PROP_OCR_DATAPATH, "Data Path:"));
        pnlOcr.add(createLabeledTextFor(PROP_OCR_ENGINE_MODE, "Engine:"));
        pnlOcr.add(createLabeledTextFor(PROP_OCR_LANG, "Language:"));
        pnlOcr.add(createLabeledTextFor(PROP_OCR_IMG_SCALE, "Image Scale:"));
        add(pnlOcr);

        List<JLabel> labels = new ArrayList<>();
        int maxLength = 0;
        for (String key : mSettingComponents.keySet())
        {
            JComponent cmp = mSettingComponents.get(key);
            if (cmp instanceof JTextField)
            {
                Container parent = cmp.getParent();
                if (parent instanceof JPanel)
                {
                    Component lbl = ((JPanel) parent).getComponent(0);
                    if (lbl instanceof JLabel)
                    {
                        labels.add((JLabel) lbl);
                        maxLength = Math.max(maxLength,
                                lbl.getPreferredSize().width);
                    }
                }
            }
        }
        LOGGER.log(Level.INFO, "Set label width to {0}.", maxLength);
        Dimension pref = labels.get(0).getPreferredSize();
        Dimension min = new Dimension(maxLength, pref.height);
        for (JLabel lbl : labels)
        {
            lbl.setMinimumSize(min);
            lbl.setPreferredSize(min);
        }

    }


    public void initialize(Properties aConfig)
    {
        for (String key : mSettingComponents.keySet())
        {
            String value = aConfig.getProperty(key);
            JComponent cmp = mSettingComponents.get(key);
            if (cmp instanceof JCheckBox)
            {
                ((JCheckBox) cmp).setSelected(
                        String.valueOf(true).equalsIgnoreCase(value));
            }
            else if (cmp instanceof JTextField)
            {
                ((JTextField) cmp).setText(value);
            }
        }
    }


    public void saveTo(Config aConfig)
    {
        for (String key : mSettingComponents.keySet())
        {
            JComponent cmp = mSettingComponents.get(key);
            if (cmp instanceof JCheckBox)
            {
                aConfig.setProperty(key,
                        String.valueOf(((JCheckBox) cmp).isSelected()));
            }
            else if (cmp instanceof JTextField)
            {
                aConfig.setProperty(key, ((JTextField) cmp).getText());
            }
        }
    }


    protected JCheckBox createLabeledCheckBoxFor(String aKey)
    {
        return createLabeledCheckBoxFor(aKey, null);
    }


    protected JCheckBox createLabeledCheckBoxFor(String aKey, String aLabel)
    {
        JCheckBox res = new JCheckBox(aLabel);
        res.setName(aKey);
        mSettingComponents.put(aKey, res);
        return res;
    }


    protected JComponent createLabeledTextFor(String aKey)
    {
        return createLabeledTextFor(aKey, null);
    }


    protected JComponent createLabeledTextFor(String aKey, String aLabel)
    {
        if (aLabel == null)
        {
            aLabel = I18n.getMessage(PDFSplitSettingsPanel.class,
                    "label." + aKey);
        }
        JPanel pnlRes = new JPanel(new BorderLayout());
        pnlRes.add(new JLabel(aLabel), BorderLayout.WEST);
        JTextField txtField = new JTextField();
        txtField.setName(aKey);
        mSettingComponents.put(aKey, txtField);
        pnlRes.add(txtField, BorderLayout.CENTER);
        return pnlRes;
    }


    protected JPanel createColumnPanel(JComponent... aComponents)
    {
        JPanel pnl = new JPanel(new GridLayout(1, aComponents.length));
        for (JComponent cmp : aComponents)
        {
            pnl.add(cmp);
        }
        return pnl;

    }
}
