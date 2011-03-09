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
package org.eclipse.wb.internal.core.nls.model;

import com.google.common.collect.Sets;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.nls.NlsSupport;
import org.eclipse.wb.internal.core.nls.edit.IEditableSource;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.preference.IPreferenceStore;

import java.util.Map;
import java.util.Set;

/**
 * Abstract source for NLS information.
 * 
 * @author scheglov_ke
 * @coverage core.nls
 */
public abstract class AbstractSource {
  protected final JavaInfo m_root;
  protected final IJavaProject m_javaProject;
  private final KeyToComponentsSupport m_keyToComponentsSupport = new KeyToComponentsSupport(true);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractSource(JavaInfo root) {
    m_root = root;
    m_javaProject = m_root.getEditor().getJavaProject();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Possible sources
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * This method is invoked when we externalize properties into "possible" source, i.e. source that
   * was not existed or added to compilation unit, but just existed in same package.
   * 
   * For example "ResourceBundle in field" source can add field here.
   */
  public void attachPossible() throws Exception {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Current form keys
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Set<String> m_formKeys = Sets.newTreeSet();

  /**
   * @return the {@link Set} of keys used on current form.
   */
  protected final Set<String> getFormKeys() {
    return m_formKeys;
  }

  /**
   * @return the {@link KeyToComponentsSupport}.
   */
  public KeyToComponentsSupport getKeyToComponentsSupport() {
    return m_keyToComponentsSupport;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the title that can be shown to user.
   */
  public abstract String getTitle() throws Exception;

  /**
   * @return the string that tells about type of this source.
   */
  public abstract String getTypeTitle() throws Exception;

  /**
   * @return the list of {@link LocaleInfo} used in this source.
   */
  public abstract LocaleInfo[] getLocales() throws Exception;

  /**
   * @return all keys existing in this source.
   */
  public abstract Set<String> getKeys() throws Exception;

  /**
   * Externalize {@link Expression} of given {@link GenericProperty}.
   */
  public abstract void externalize(JavaInfo component, GenericProperty property, String value)
      throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Locale access
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String KEY_LOCALE = "KEY_LOCALE";

  /**
   * Sets the {@link LocaleInfo} for given component.
   */
  public static void setLocaleInfo(JavaInfo javaInfo, LocaleInfo localeInfo) {
    javaInfo.getRoot().putArbitraryValue(KEY_LOCALE, localeInfo);
  }

  /**
   * @return the {@link LocaleInfo} for given component.
   */
  public static LocaleInfo getLocaleInfo(JavaInfo javaInfo) {
    LocaleInfo localeInfo = (LocaleInfo) javaInfo.getRoot().getArbitraryValue(KEY_LOCALE);
    if (localeInfo == null) {
      localeInfo = LocaleInfo.DEFAULT;
    }
    return localeInfo;
  }

  /**
   * @return the {@link LocaleInfo} for this source.
   */
  protected final LocaleInfo getLocaleInfo() {
    return getLocaleInfo(m_root);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Value access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link String} value of {@link Expression}.
   */
  public abstract String getValue(Expression expression) throws Exception;

  /**
   * Changes value of {@link Expression} in external file (for example in *.properties).
   */
  public abstract void setValue(Expression expression, String value) throws Exception;

  /**
   * @return the key for given {@link Expression} or <code>null</code> if expression is not
   *         externalized.
   */
  public abstract String getKey(Expression expression) throws Exception;

  /**
   * @return <code>true</code> if NLS strings for current locale were externally changed. For
   *         example, if user changed *.properties file in other editor.
   */
  public abstract boolean isExternallyChanged() throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Edit support
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link IEditableSource} for this source.<br>
   * 
   *         When we open dialog, we have "Ok" and "Cancel" buttons. So, we can not just change
   *         values directly in source using {@link #setValue(JavaInfo, Expression, String)},
   *         because user can cancel dialog and expect that this will cancel edit. So, we need some
   *         temporary storage.
   */
  public abstract IEditableSource getEditable() throws Exception;

  /**
   * Apply new values map for given locale.
   */
  public abstract void apply_setValues(LocaleInfo locale, Map<String, String> values)
      throws Exception;

  /**
   * Rename key.
   */
  public abstract void apply_renameKeys(Map<String, String> oldToNew) throws Exception;

  /**
   * Add key.
   */
  public abstract void apply_addKey(String key) throws Exception;

  /**
   * Externalize property.
   */
  public abstract void apply_externalizeProperty(GenericProperty property, String key)
      throws Exception;

  /**
   * Internalize keys.
   */
  public abstract void apply_internalizeKeys(Set<String> keys) throws Exception;

  /**
   * Add new locale with some initial values.
   */
  public abstract void apply_addLocale(LocaleInfo locale, Map<String, String> values)
      throws Exception;

  /**
   * Remove locale.
   */
  public abstract void apply_removeLocale(LocaleInfo locale) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Replace
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Replaces externalized {@link GenericProperty} with given value.
   */
  public abstract void replace_toStringLiteral(GenericProperty property, String value)
      throws Exception;

  /**
   * Ensures that given {@link GenericProperty} uses key to get value.
   */
  public abstract void useKey(GenericProperty property, String key) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Update source on key operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Subclasses should invoke this method after registering correspondence between given component
   * and key to allow superclass update its information.
   */
  protected final void onKeyAdd(JavaInfo component, String key) {
    // update form keys
    {
      m_formKeys.add(key);
    }
    // update key -> components map
    m_keyToComponentsSupport.add(component, key);
  }

  /**
   * Subclasses should invoke this method after key renaming to allow superclass update its
   * information.
   */
  protected final void onKeyRename(String oldKey, String newKey) {
    // update form keys
    {
      m_formKeys.remove(oldKey);
      m_formKeys.add(newKey);
    }
    // update key -> components map
    m_keyToComponentsSupport.rename(oldKey, newKey);
  }

  /**
   * Subclasses should invoke this method after key internalizing to allow superclass update its
   * information.
   */
  protected final void onKeyInternalize(String key) {
    // update form keys
    {
      m_formKeys.remove(key);
    }
    // update key -> components map
    m_keyToComponentsSupport.remove(key);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Generate unique key for given base key and existing keys.
   */
  public static String generateUniqueKey(Set<String> keys, String baseKey) {
    // check, may be base key is already unique
    if (!keys.contains(baseKey)) {
      return baseKey;
    }
    // try keys base_1, base_2, etc
    for (int index = 1;; index++) {
      String key = baseKey + "_" + index;
      if (!keys.contains(key)) {
        return key;
      }
    }
  }

  /**
   * @return the name of enclosing {@link TypeDeclaration}, short or qualified accordingly to
   *         preferences.
   */
  protected static String getTypeName(JavaInfo component) {
    TypeDeclaration typeDeclaration = JavaInfoUtils.getTypeDeclaration(component);
    IPreferenceStore preferences = component.getDescription().getToolkit().getPreferences();
    if (preferences.getBoolean(IPreferenceConstants.P_NLS_KEY_QUALIFIED_TYPE_NAME)) {
      return AstNodeUtils.getFullyQualifiedName(typeDeclaration, false);
    } else {
      return typeDeclaration.getName().getIdentifier();
    }
  }

  /**
   * Ensures that given {@link GenericProperty} is not externalized and has some
   * {@link StringLiteral} as expression.
   */
  protected static void ensureStringLiteral(GenericProperty property) throws Exception {
    Expression expression = property.getExpression();
    if (expression != null) {
      // ask existing source to "de-externalize" expression
      AbstractSource oldSource = NlsSupport.getSource(expression);
      if (oldSource != null) {
        oldSource.replace_toStringLiteral(property, null);
      }
    } else {
      property.setExpression("(java.lang.String) null", Property.UNKNOWN_VALUE);
    }
  }
}
