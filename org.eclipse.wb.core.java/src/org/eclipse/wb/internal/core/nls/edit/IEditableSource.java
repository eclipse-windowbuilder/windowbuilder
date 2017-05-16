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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.nls.commands.ICommandQueue;
import org.eclipse.wb.internal.core.nls.model.IKeyGeneratorStrategy;
import org.eclipse.wb.internal.core.nls.model.LocaleInfo;

import java.util.Set;

/**
 * Interface that allows editing of NLS source.
 *
 * We separate this interface from EditableNLSSource implementation to keep clean interaface for
 * users (editors).
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public interface IEditableSource {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Command queue
  //
  ////////////////////////////////////////////////////////////////////////////
  void setCommandQueue(ICommandQueue commandQueue);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Key generator
  //
  ////////////////////////////////////////////////////////////////////////////
  void setKeyGeneratorStrategy(IKeyGeneratorStrategy keyGeneratorStrategy);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Listener
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Add listener.
   */
  void addListener(IEditableSourceListener listener);

  /**
   * Remove listener.
   */
  void removeListener(IEditableSourceListener listener);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Return "shoft title" that can be used in places where we have litle space.
   */
  String getShortTitle();

  /**
   * Return "long title" that can be used in places where we have much space.
   */
  String getLongTitle();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Locales
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Return all locales in this source.
   */
  LocaleInfo[] getLocales();

  /**
   * Add new locale information based on (possible null) baseLocale.
   */
  void addLocale(LocaleInfo locale, LocaleInfo baseLocale);

  /**
   * Remove locale information.
   */
  void removeLocale(LocaleInfo locale);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Keys
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Return all key's of this source.
   */
  Set<String> getKeys();

  /**
   * Return key's used in in current form.
   */
  Set<String> getFormKeys();

  /**
   * Replace key in all locales.
   */
  void renameKey(String oldKey, String newKey);

  /**
   * Mark passed property as externalized.
   */
  void externalize(StringPropertyInfo propertyInfo, boolean copyToAllLocales);

  /**
   * Internalize key: remove key from all locales and replace externalize expression with default
   * string literal.
   */
  void internalizeKey(String key);

  /**
   * Set value for given key in all locales.
   */
  void addKey(String key, String value);

  /**
   * @return {@link Set} of components for that have externalized properties with given key.
   */
  Set<JavaInfo> getComponentsByKey(String key);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Values
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Get value for given key and locale.
   */
  String getValue(LocaleInfo locale, String key);

  /**
   * Set value for given key and locale.
   */
  void setValue(LocaleInfo locale, String key, String value);
}
