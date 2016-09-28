/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.nls.edit;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.nls.Messages;
import org.eclipse.wb.internal.core.nls.commands.AddKeyCommand;
import org.eclipse.wb.internal.core.nls.commands.AddLocaleCommand;
import org.eclipse.wb.internal.core.nls.commands.ExternalizePropertyCommand;
import org.eclipse.wb.internal.core.nls.commands.ICommandQueue;
import org.eclipse.wb.internal.core.nls.commands.InternalizeKeyCommand;
import org.eclipse.wb.internal.core.nls.commands.RemoveLocaleCommand;
import org.eclipse.wb.internal.core.nls.commands.RenameKeyCommand;
import org.eclipse.wb.internal.core.nls.commands.SetValuesCommand;
import org.eclipse.wb.internal.core.nls.model.AbstractSource;
import org.eclipse.wb.internal.core.nls.model.IKeyGeneratorStrategy;
import org.eclipse.wb.internal.core.nls.model.KeyToComponentsSupport;
import org.eclipse.wb.internal.core.nls.model.LocaleInfo;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;

import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation for editable source.
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public final class EditableSource implements IEditableSource {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Base information
  //
  ////////////////////////////////////////////////////////////////////////////
  private String m_shortTitle;
  private String m_longTitle;
  private final Set<String> m_keys = Sets.newHashSet();
  private final Set<String> m_formKeys = Sets.newHashSet();
  private final HashMap<String, String> m_keyToValue = new HashMap<String, String>();
  private KeyToComponentsSupport m_keyToComponentsSupport = new KeyToComponentsSupport(false); // initialize by default for case of new source
  private final Map<LocaleInfo, EditableLocaleInfo> m_localeToInfo = Maps.newTreeMap();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public EditableSource() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access: prepare for editing
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setShortTitle(String shortTitle) {
    m_shortTitle = shortTitle;
  }

  public void setLongTitle(String longTitle) {
    m_longTitle = longTitle;
  }

  /**
   * Add new locale with values.
   */
  public void add(LocaleInfo locale, Map<String, String> keyToValue) {
    m_localeToInfo.put(locale, new EditableLocaleInfo(locale, keyToValue));
    m_keys.addAll(keyToValue.keySet());
    m_keyToValue.putAll(keyToValue);
  }

  /**
   * Set keys used in current form.
   */
  public void setFormKeys(Set<String> currentFormKeys) {
    m_formKeys.clear();
    m_formKeys.addAll(currentFormKeys);
  }

  public void setKeyToComponentsSupport(KeyToComponentsSupport keyToComponentsSupport) {
    m_keyToComponentsSupport = keyToComponentsSupport;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Command queue
  //
  ////////////////////////////////////////////////////////////////////////////
  private ICommandQueue m_commandQueue;

  public void setCommandQueue(ICommandQueue commandQueue) {
    m_commandQueue = commandQueue;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Key generator
  //
  ////////////////////////////////////////////////////////////////////////////
  private IKeyGeneratorStrategy m_keyGeneratorStrategy;

  public void setKeyGeneratorStrategy(IKeyGeneratorStrategy keyGeneratorStrategy) {
    m_keyGeneratorStrategy = keyGeneratorStrategy;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IEditableSource: listener
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<IEditableSourceListener> m_listeners = Lists.newArrayList();

  public void addListener(IEditableSourceListener listener) {
    if (!m_listeners.contains(listener)) {
      m_listeners.add(listener);
    }
  }

  public void removeListener(IEditableSourceListener listener) {
    m_listeners.remove(listener);
  }

  private void fire_keyAdded(String key, StringPropertyInfo propertyInfo) {
    for (IEditableSourceListener listener : m_listeners) {
      listener.keyAdded(key, propertyInfo);
    }
  }

  private void fire_keyRemoved(String key) {
    for (IEditableSourceListener listener : m_listeners) {
      listener.keyRemoved(key);
    }
  }

  private void fire_keyRenamed(String oldKey, String newKey) {
    for (IEditableSourceListener listener : m_listeners) {
      listener.keyRenamed(oldKey, newKey);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IEditableSource: access
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getShortTitle() {
    return m_shortTitle;
  }

  public String getLongTitle() {
    return m_longTitle;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IEditableSource: locales
  //
  ////////////////////////////////////////////////////////////////////////////
  public LocaleInfo[] getLocales() {
    return m_localeToInfo.keySet().toArray(new LocaleInfo[m_localeToInfo.keySet().size()]);
  }

  public void addLocale(LocaleInfo locale, LocaleInfo baseLocale) {
    // prepare base key -> value map
    Map<String, String> keyToValue;
    if (baseLocale != null) {
      EditableLocaleInfo editableBaseLocale = getEditableLocale(baseLocale);
      keyToValue = new HashMap<String, String>(editableBaseLocale.m_keyToValue);
    } else {
      keyToValue = Maps.newHashMap();
    }
    // add new locale
    add(locale, keyToValue);
    // add command
    m_commandQueue.addCommand(new AddLocaleCommand(this, locale, keyToValue));
  }

  public void removeLocale(LocaleInfo locale) {
    m_localeToInfo.remove(locale);
    // add command
    m_commandQueue.addCommand(new RemoveLocaleCommand(this, locale));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IEditableSource: keys
  //
  ////////////////////////////////////////////////////////////////////////////
  public Set<String> getKeys() {
    return m_keys;
  }

  public Set<String> getFormKeys() {
    return m_formKeys;
  }

  public void renameKey(String oldKey, String newKey) {
    // if keys are equal, ignore replace request
    if (oldKey.equals(newKey)) {
      return;
    }
    // check, may be there is already such key in some bundle
    boolean containsNewKey = false;
    for (EditableLocaleInfo editableLocale : m_localeToInfo.values()) {
      containsNewKey |= editableLocale.containsKey(newKey);
    }
    // ask user, should we keep value of existing newKey or value of key that is renaming to newKey
    boolean keepNewKeyValue = false;
    if (containsNewKey) {
      MessageDialog dialog = new MessageDialog(DesignerPlugin.getShell(),
          Messages.EditableSource_renameConfirmTitle,
          null,
          MessageFormat.format(Messages.EditableSource_renameConfirmKeepExistingValue, newKey),
          MessageDialog.QUESTION,
          new String[]{
              Messages.EditableSource_renameConfirmYesKeep,
              Messages.EditableSource_renameConfirmNoUseRenaming},
          0);
      int openResult = dialog.open();
      if (openResult == SWT.DEFAULT) {
        // cancel pressed
        return;
      }
      keepNewKeyValue = openResult == 0;
    }
    // add "rename key" command
    m_commandQueue.addCommand(new RenameKeyCommand(this, oldKey, newKey));
    // replace key in locale informations
    for (EditableLocaleInfo editableLocale : m_localeToInfo.values()) {
      boolean renamed = editableLocale.renameKey(oldKey, newKey, keepNewKeyValue);
      // add "set values" command
      if (renamed) {
        addLocaleValuesCommand(editableLocale);
      }
    }
    // replace key in form keys
    m_formKeys.remove(oldKey);
    m_formKeys.add(newKey);
    // replace key in all keys
    m_keys.remove(oldKey);
    m_keys.add(newKey);
    String valueOfRenamedKey = m_keyToValue.get(oldKey);
    m_keyToValue.remove(oldKey);
    m_keyToValue.put(newKey, valueOfRenamedKey);
    // replace key in key -> components
    m_keyToComponentsSupport.rename(oldKey, newKey);
    // notify listeners that key was renamed
    fire_keyRenamed(oldKey, newKey);
  }

  public void externalize(StringPropertyInfo propertyInfo, boolean copyToAllLocales) {
    // prepare property "properties" ;-)
    JavaInfo component = propertyInfo.getComponent();
    GenericProperty property = propertyInfo.getProperty();
    String value = propertyInfo.getValue();
    // prepare key
    String baseKey = null;
    String key = null;
    {
      IPreferenceStore preferences = component.getDescription().getToolkit().getPreferences();
      if (preferences.getBoolean(IPreferenceConstants.P_NLS_KEY_AS_STRING_VALUE_ONLY)) {
        baseKey = AbstractSource.shrinkText(value);
        if (baseKey == null) {
          return;
        }
      } else {
        baseKey = m_keyGeneratorStrategy.generateBaseKey(component, property);
        if (preferences.getBoolean(IPreferenceConstants.P_NLS_KEY_HAS_STRING_VALUE)) {
          baseKey += "_" + AbstractSource.shrinkText(value);
        }
      }
      key = AbstractSource.generateUniqueKey(m_keyToValue, baseKey, value);
    }
    // add key to all/current key sets
    m_keys.add(key);
    m_formKeys.add(key);
    m_keyToValue.put(key, value);
    m_keyToComponentsSupport.add(component, key);
    // add "externalize property" command
    m_commandQueue.addCommand(new ExternalizePropertyCommand(this, component, property, key));
    // add current value to default (or all, if required) locale, this will also add "set values" command
    for (LocaleInfo locale : m_localeToInfo.keySet()) {
      if (locale.isDefault() || copyToAllLocales) {
        setValue(locale, key, value);
      }
    }
    // notify listeners that property was externalized with this key
    fire_keyAdded(key, propertyInfo);
  }

  public void internalizeKey(String key) {
    m_formKeys.remove(key);
    m_keys.remove(key);
    m_keyToValue.remove(key);
    m_keyToComponentsSupport.remove(key);
    // add "internalize key" command
    m_commandQueue.addCommand(new InternalizeKeyCommand(this, key));
    // remove key from locale informations
    for (EditableLocaleInfo editableLocale : m_localeToInfo.values()) {
      boolean removed = editableLocale.removeKey(key);
      // add "set values" command
      if (removed) {
        addLocaleValuesCommand(editableLocale);
      }
    }
    // notify listeners that key was removed
    fire_keyRemoved(key);
  }

  public void addKey(String key, String value) {
    // add key to set
    m_keys.add(key);
    m_keyToValue.put(key, value);
    // add "add key" command
    m_commandQueue.addCommand(new AddKeyCommand(this, key));
    // add value to  all locales, this will also add "set values" command
    for (LocaleInfo locale : getLocales()) {
      setValue(locale, key, value);
    }
    // notify listeners that key was added
    fire_keyAdded(key, null);
  }

  public Set<JavaInfo> getComponentsByKey(String key) {
    return m_keyToComponentsSupport.getComponentsByKey(key);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IEditableSource: values
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getValue(LocaleInfo locale, String key) {
    EditableLocaleInfo editableLocale = getEditableLocale(locale);
    if (editableLocale != null) {
      return editableLocale.getValue(key);
    }
    return null;
  }

  public void setValue(LocaleInfo locale, String key, String value) {
    EditableLocaleInfo editableLocale = getEditableLocale(locale);
    boolean changed = editableLocale.setValue(key, value);
    // add command
    if (changed) {
      addLocaleValuesCommand(editableLocale);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Get editable locale information for given locale.
   */
  private EditableLocaleInfo getEditableLocale(LocaleInfo locale) {
    return m_localeToInfo.get(locale);
  }

  /**
   * Add "set values" command for given editable locale.
   */
  private void addLocaleValuesCommand(EditableLocaleInfo editableLocale) {
    m_commandQueue.addCommand(
        new SetValuesCommand(this, editableLocale.m_locale, editableLocale.m_keyToValue));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editable locale information
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Information object for editing single locale.
   */
  private static class EditableLocaleInfo {
    private final LocaleInfo m_locale;
    private final Map<String, String> m_keyToValue;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public EditableLocaleInfo(LocaleInfo locale, Map<String, String> keyToValue) {
      m_locale = locale;
      m_keyToValue = new HashMap<String, String>(keyToValue);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Keys
    //
    ////////////////////////////////////////////////////////////////////////////
    public boolean containsKey(String key) {
      return m_keyToValue.containsKey(key);
    }

    public boolean renameKey(String oldKey, String newKey, boolean keepNewKeyValue) {
      String value = m_keyToValue.remove(oldKey);
      if (value != null) {
        if (!keepNewKeyValue) {
          m_keyToValue.put(newKey, value);
        }
      }
      return value != null;
    }

    public boolean removeKey(String key) {
      return m_keyToValue.remove(key) != null;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Values
    //
    ////////////////////////////////////////////////////////////////////////////
    public String getValue(String key) {
      return m_keyToValue.get(key);
    }

    /**
     * Change value and return <code>true</code> if value was different than previous one.
     */
    public boolean setValue(String key, String value) {
      String oldValue = getValue(key);
      // check, may be we already have same value
      if (oldValue == null && StringUtils.isEmpty(value)) {
        return false;
      }
      if (oldValue != null && oldValue.equals(value)) {
        return false;
      }
      // change value and mark locale as changed
      m_keyToValue.put(key, value);
      return true;
    }
  }
}
