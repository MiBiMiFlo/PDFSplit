package de.code2be.pdfsplit.ui.swing.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import de.code2be.help.I18n;

/**
 * This action extends the AbstractAction and implements some methods for the
 * common action parameter.
 * 
 * @author Michael Weiss
 * 
 */
public abstract class BasicAction extends AbstractAction
{

    private static final long serialVersionUID = 6516422925676999990L;

    protected static final Logger LOGGER = Logger
            .getLogger(BasicAction.class.getName());

    public static boolean mShowAcceleratorInToolTip = true;

    /**
     * Looks for a action resource bundle name for the given class.
     * 
     * @param aClass
     *            the class to find the resource bundle name for.
     * @return the name of the matching resource bundle, or null if no resource
     *         bundle was found.
     */
    public static String getResourceBundleName(Class<?> aClass)
    {
        String bundleName = null;
        String clsName = aClass.getName();
        int idx = clsName.lastIndexOf('.');
        if (idx >= 0)
        {
            bundleName = clsName.substring(0, idx) + ".actions";
        }
        else
        {
            bundleName = "actions";
        }

        URL resource = aClass.getResource("actions.properties");
        if (resource != null)
        {
            return bundleName;
        }
        Class<?> scls = aClass.getSuperclass();
        if (scls != null && scls != BasicAction.class)
        {
            return getResourceBundleName(scls);
        }
        return null;
    }

    /**
     * The oldState is a flag that is used between onShow and onHide. If you use
     * The {@link BasicMenu}, the onShow method will be called before the
     * element is shown. Than the element state will be saved in oldState and
     * the element state is set to canPerform. After the menu was hidden, the
     * onHide method will be called and the old state gets restored.
     */
    private Boolean oldState;

    /**
     * A flag to indicate if this action is already initialized.
     */
    private boolean mInitialized = false;

    /**
     * A flag to indicate if this action is currently initializing.
     */
    private boolean mInitializing = false;

    public void setInitialized(boolean aFlag)
    {
        mInitialized = aFlag;
    }


    public boolean isInitialized()
    {
        return mInitialized;
    }


    /**
     * Same as <code>putValue(Action.ACCELERATOR_KEY, aKeyStoke)</code>.
     * 
     * @param aKeyStoke
     *            the KeyStoke to set.
     */
    public void setKeyStoke(KeyStroke aKeyStoke)
    {
        putValue(ACCELERATOR_KEY, aKeyStoke);
    }


    public void setKeyStokeFromString(String aKeyStoke)
    {
        setKeyStoke(KeyStroke.getKeyStroke(aKeyStoke));
    }


    /**
     * Same as <code>putValue(Action.SHORT_DESCRIPTION, aToolTipText)</code>.
     * 
     * @param aToolTipText
     *            the tool tip text to display.
     */
    public void setToolTipText(String aToolTipText)
    {
        putValue(SHORT_DESCRIPTION, aToolTipText);
    }


    /**
     * Same as <code>putValue(Action.NAME, aName)</code>.
     * 
     * @param aName
     *            the name of this action.
     */
    public void setName(String aName)
    {
        putValue(NAME, aName);
    }


    /**
     * Same as <code>putValue(Action.SMALL_ICON, aIcon)</code>.
     * 
     * @param aIcon
     *            the icon to set.
     */
    public void setIcon(Icon aIcon)
    {
        putValue(SMALL_ICON, aIcon);
    }


    /**
     * The the icon by it's name. This method will use the
     * {@link IconController#getIconFor(String)} method to fetch the icon for
     * the given name.
     * 
     * @param aName
     *            the name of the icon to use.
     */
    protected void setIconByName(String aName)
    {
        if (aName != null)
        {
            try
            {
                URL iconURL = getClass().getResource(
                        "/de/code2be/pdfsplit/ui/icons/16/" + aName);
                if (iconURL != null)
                {
                    putValue(SMALL_ICON, new ImageIcon(iconURL));
                }
            }
            catch (Exception ex)
            {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            }

            try
            {
                URL iconURL = getClass().getResource(
                        "/de/code2be/pdfsplit/ui/icons/32/" + aName);
                if (iconURL != null)
                {
                    putValue(LARGE_ICON_KEY, new ImageIcon(iconURL));
                }
            }
            catch (Exception ex)
            {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            }

        }
    }


    /**
     * Same as <code>putValue(Action.ACTION_COMMAND_KEY, aCommandKey)</code>.
     * 
     * @param aCommandKey
     *            the string to use as command key.
     */
    public void setCommandKey(String aCommandKey)
    {
        putValue(ACTION_COMMAND_KEY, aCommandKey);
    }


    @Override
    public void putValue(String aKey, Object aNewValue)
    {
        super.putValue(aKey, aNewValue);
        if (isInitialized() && ACTION_COMMAND_KEY.equals(aKey))
        {
            initFromCommand();
        }
    }


    /**
     * Check if the given string is empty.
     * 
     * @param aString
     *            the string to check.
     * @param aTrim
     *            if true a trim() is done on the string to ignore whitespace
     *            chars.
     * @return true if the string is null or has a length of 0. In case aTrim is
     *         true, also none empty whitespace strings return true.
     */
    public static final boolean isEmpty(String aString, boolean aTrim)
    {
        if (aString == null)
        {
            // null == empty
            return true;
        }
        int cnt = aString.length();
        if (cnt == 0)
        {
            // no chars == empty
            return true;
        }
        if (aTrim && Character.isWhitespace(aString.charAt(0)))
        {
            for (int i = 1; i < cnt; i++)
            {
                if (!Character.isWhitespace(aString.charAt(i)))
                {
                    // none whitespace char found == not empty
                    return false;
                }
            }
            // all chars are whitespace chars == empty
            return true;
        }
        // not empty
        return false;
    }


    @Override
    public Object getValue(String aKey)
    {
        if (!(mInitializing || isInitialized()))
        {
            mInitializing = true;
            initFromCommand();
            mInitializing = false;
        }
        Object val = super.getValue(aKey);
        if (mShowAcceleratorInToolTip && SHORT_DESCRIPTION.equals(aKey))
        {
            String accText = getAcceleratorText();

            String cmd = getCommandKey();

            if (!isEmpty(accText, true) || !isEmpty(cmd, true))
            {
                StringBuilder sb = new StringBuilder();
                sb.append("(");
                if (!isEmpty(cmd, true))
                {
                    sb.append(cmd);
                    if (!isEmpty(accText, true))
                    {
                        sb.append("=").append(accText);
                    }
                }
                else if (!isEmpty(accText, true))
                {
                    sb.append(accText);
                }
                sb.append(")");
                accText = sb.toString();
                if (val != null)
                {
                    String valStr = val.toString();
                    if (!valStr.endsWith(accText))
                    {
                        val = new StringBuilder().append(val).append(" ")
                                .append(accText).toString();
                    }
                }
                else
                {
                    val = new StringBuilder().append(accText).toString();
                }
            }
        }
        return val;
    }


    /**
     * 
     * @return the actually set command key.
     */
    public String getCommandKey()
    {
        return (String) getValue(ACTION_COMMAND_KEY);
    }


    /**
     * 
     * @param aKeyStroke
     *            the keystroke to take the mnemonic key from.
     */
    public void setMnemonicKey(KeyStroke aKeyStroke)
    {
        putValue(MNEMONIC_KEY, aKeyStroke.getKeyCode());
    }


    /**
     * The {@link BasicMenu} and {@link BasicPopupMenu} classes call this
     * method, right before they become visible. This method can be used for
     * example to set the action disabled, if it would not make sense to execute
     * it for now.
     */
    public void onShow()
    {
        oldState = isEnabled();
        setEnabled(canPerform());
    }


    /**
     * This method is called be the {@link BasicMenu} and the
     * {@link BasicPopupMenu} right after a menu became invisible. It can be
     * used to reactivate a prior disabled action to allow it to be called by
     * Keyboard shortcut.
     */
    public void onHide()
    {
        if (oldState != null)
        {
            setEnabled(oldState.booleanValue());
            oldState = null;
        }
    }


    /**
     * This method checks itself, if it can be performed. This default
     * implementation always returns true, if you overwrite it please notice
     * that this method is called very often and that it should return as soon
     * as possible.
     * 
     * @return true if it can be performed, false otherwise. (The default
     *         implementation always returns true!)
     */
    public boolean canPerform()
    {
        return true;
    }


    /**
     * Checks if this action can perform the given action event. This default
     * implementation always returns {@link #canPerform()}, if you overwrite it
     * please notice that this method is called very often and that it should
     * return as soon as possible.
     * 
     * @param aE
     *            the action event to check.
     * @return true if the action can perform the event.
     */
    public boolean canPerform(ActionEvent aE)
    {
        return canPerform();
    }


    /**
     * A delegating method to {@link I18n#getMessage(String, String)} with
     * cached exception.
     * 
     * @param aResourceBundle
     *            the name of the resource bundle to use.
     * @param aKey
     *            the key inside the bundle.
     * @param aObjects
     *            the list of parameter to pass to
     *            {@link I18n#getMessage(String, String, Object...)}.
     * @return the string value for the given key or null if no value could be
     *         found.
     */
    protected String getMessage(String aResourceBundle, String aKey,
            Object... aObjects)
    {
        try
        {
            return I18n.getMessage(aResourceBundle, aKey, aObjects);
        }
        catch (Exception ex)
        {
            LOGGER.log(Level.FINEST, ex.getMessage(), ex);
        }
        return null;
    }


    /**
     * A short call of {@link #getMessage(String, String)} with
     * {@link #getResourceBundleName()} as resource bundle name and the given
     * key.
     * 
     * @param aKey
     *            the message key to use.
     * @param aObjects
     *            the list of parameter to pass to
     *            {@link I18n#getMessage(String, String, Object...)}.
     * @return the message for the given key or null if no message could be
     *         retrieved.
     */
    protected String getMessage(String aKey, Object... aObjects)
    {
        return getMessage(getResourceBundleName(), aKey, aObjects);
    }


    /**
     * A short call of {@link #getMessage(String, String)} with
     * {@link #getResourceBundleName()} as resource bundle name and the result
     * of {@link #buildKey(String)} as the message key.
     * 
     * @param aKey
     *            the message key to pass to {@link #buildKey(String)} to
     *            generate the full key.
     * @param aObjects
     *            the list of parameter to pass to
     *            {@link I18n#getMessage(String, String, Object...)}.
     * @return the message for the given key or null if no message could be
     *         retrieved.
     */
    protected String getMessageForSubKey(String aKey, Object... aObjects)
    {
        return getMessage(getResourceBundleName(), buildKey(aKey), aObjects);
    }


    /**
     * build the complete message key. The complete key is used by concatenating
     * the result of {@link #getCommandKey()} and the given sub key with a dot
     * (.). So for the command key <code>FRED</code> and the message key
     * <code>message</code> the result will be <code>FRED.message</code>!
     * 
     * @param aMessageKey
     *            the message key to add to the command key.
     * @return the combined message key.
     */
    protected String buildKey(String aMessageKey)
    {
        return new StringBuilder().append(getCommandKey()).append(".")
                .append(aMessageKey).toString();
    }


    /**
     * 
     * @return the name of the resource bundle to use or null if none could be
     *         found.
     */
    protected String getResourceBundleName()
    {
        return getResourceBundleName(getClass());
    }


    /**
     * Initialize the action from a resource file by command name.
     */
    protected void initFromCommand()
    {
        try
        {
            String cmdName = getCommandKey();
            if (cmdName != null)
            {
                String rbName = getResourceBundleName();
                String name = getMessage(rbName, cmdName + ".text");
                if (name != null)
                {
                    setName(name);
                }
                String tooltip = getMessage(rbName, cmdName + ".tooltip");
                if (tooltip != null)
                {
                    setToolTipText(tooltip);
                }
                String iconName = getMessage(rbName, cmdName + "." + "icon");
                if (iconName != null)
                {
                    setIconByName(iconName);
                }
                String keyStroke = getMessage(rbName, cmdName + ".keystroke");
                if (keyStroke != null)
                {
                    setKeyStokeFromString(keyStroke);
                }
                String mnStr = getMessage(rbName, cmdName + ".mnemonic");
                if (mnStr != null)
                {
                    KeyStroke ks = KeyStroke.getKeyStroke(mnStr);
                    setMnemonicKey(ks);
                }
            }
        }
        catch (Exception ex)
        {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
        setInitialized(true);
    }


    /**
     * Retrieve the command name (first word in action command).
     * 
     * @param aE
     *            the action event to read the command name from.
     * @return the command name (first word of action command) in upper case. If
     *         action command is null, null is returned.
     */
    protected String getCommandName(ActionEvent aE)
    {
        return getCommandName(aE, true);
    }


    /**
     * Retrieve the command name (first word in action command).
     * 
     * @param aE
     *            the action event to read the command name from.
     * @param aUpcase
     *            if true the result will be in upper case.
     * @return the command name (first word of action command). If action
     *         command is null, null is returned.
     */
    protected String getCommandName(ActionEvent aE, boolean aUpcase)
    {
        String cmd = aE.getActionCommand();
        if (cmd == null)
        {
            return null;
        }

        cmd = cmd.trim();
        int idx = cmd.indexOf(' ');
        if (idx > 0)
        {
            cmd = cmd.substring(0, idx);
        }

        if (aUpcase)
        {
            cmd = cmd.toUpperCase();
        }
        return cmd;
    }


    /**
     * Retrieve the command parameter string (everything after the first word in
     * action command).
     * 
     * @param aE
     *            the action event to read the command parameter from.
     * @return the command parameter string (everything after the first word in
     *         action command). If the action command is null, null is returned.
     *         if the action command does not contain a parameter, an empty
     *         string is returned.
     */
    protected String getCommandParam(ActionEvent aE)
    {
        String cmd = aE.getActionCommand();
        if (cmd == null)
        {
            return null;
        }
        cmd = cmd.trim();
        int idx = cmd.indexOf(' ');
        if (idx > 0)
        {
            return cmd.substring(idx).trim();
        }
        return "";
    }


    /**
     * Method copied from sun.swing.MenuItemLayoutHelper#getAccText(). Creates a
     * readable string representation of the accelerator key.
     * 
     */
    private String getAcceleratorText()
    {
        try
        {
            KeyStroke keystroke = (KeyStroke) getValue(ACCELERATOR_KEY);
            if (keystroke != null)
            {
                StringBuilder res = new StringBuilder();
                int i = keystroke.getModifiers();
                if (i > 0)
                {
                    res.append(KeyEvent.getModifiersExText(i)).append("+");
                }
                int j = keystroke.getKeyCode();
                if (j != 0)
                {
                    res.append(KeyEvent.getKeyText(j));
                }
                else
                {
                    res.append(keystroke.getKeyChar());
                }
                return res.toString().toUpperCase();
            }
        }
        catch (Exception ex)
        {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
        return null;
    }
}
