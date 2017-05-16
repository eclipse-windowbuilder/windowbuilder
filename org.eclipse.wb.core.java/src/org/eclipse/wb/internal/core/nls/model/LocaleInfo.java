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

import org.eclipse.wb.internal.core.nls.bundle.BundleInfo;

import org.eclipse.jdt.core.dom.CompilationUnit;

import java.util.Locale;

/**
 * Information about {@link Locale}.
 *
 * We separate {@link LocaleInfo} from {@link BundleInfo} because {@link BundleInfo} is wrapper for
 * single *.properties file and we support more than one source of bundles in one
 * {@link CompilationUnit}.
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public final class LocaleInfo implements Comparable<LocaleInfo> {
  /**
   * The default {@link LocaleInfo}.
   */
  public static final LocaleInfo DEFAULT = new LocaleInfo(null);
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Locale m_locale;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LocaleInfo(Locale locale) {
    m_locale = locale;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return getTitle();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof LocaleInfo) {
      LocaleInfo localeInfo = (LocaleInfo) obj;
      if (isDefault()) {
        return localeInfo.isDefault();
      }
      return m_locale.equals(localeInfo.m_locale);
    }
    return false;
  }

  @Override
  public int hashCode() {
    if (isDefault()) {
      return 0;
    }
    return m_locale.hashCode();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Comparable
  //
  ////////////////////////////////////////////////////////////////////////////
  public int compareTo(LocaleInfo o) {
    if (m_locale == null) {
      if (o.m_locale == null) {
        return 0;
      }
      return -1;
    }
    if (o.m_locale == null) {
      return 1;
    }
    String localeNameA = m_locale.toString();
    String localeNameB = o.m_locale.toString();
    return localeNameA.compareTo(localeNameB);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link Locale} for this {@link LocaleInfo}.
   */
  public Locale getLocale() {
    return m_locale;
  }

  /**
   * @return <code>true</code> if that {@link Locale} is default.
   */
  public boolean isDefault() {
    return m_locale == null;
  }

  /**
   * @return the title to display in UI.
   */
  public String getTitle() {
    if (isDefault()) {
      return "(default)";
    }
    return m_locale.toString();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the "parent" {@link LocaleInfo} from given array.<br>
   *         Here "parent" is locale that is more general than current one.<br>
   *         For example "parent" locale for 'ru_RU' is 'ru'.
   *
   *         If there are no parent locale in array, return default locale.<br>
   */
  public LocaleInfo getParent(LocaleInfo locales[]) {
    String localeName = m_locale.toString();
    int lastSeparatorIndex = localeName.lastIndexOf('_');
    if (lastSeparatorIndex != -1) {
      String parentLocaleName = localeName.substring(0, lastSeparatorIndex);
      // try to find locale with parent name
      for (LocaleInfo locale : locales) {
        if (locale.getLocale() != null && locale.getLocale().toString().equals(parentLocaleName)) {
          return locale;
        }
      }
    }
    // use default
    return LocaleInfo.DEFAULT;
  }

  /**
   * @param localeName
   *          the name of locale, such as "en" or "ru_RU".
   * @param localeDescription
   *          the description of {@link Locale}, used in exception.
   * @return the {@link LocaleInfo} which wraps {@link Locale}.
   */
  public static LocaleInfo create(String localeName, String localeDescription) {
    // try to find locale in list of available locales
    Locale[] locales = Locale.getAvailableLocales();
    for (int i = 0; i < locales.length; i++) {
      Locale locale = locales[i];
      if (locale.toString().equals(localeName)) {
        return new LocaleInfo(locale);
      }
    }
    // try to create new, this constructor is since 1.4, so do this in try/catch
    try {
      Locale locale;
      int separatorIndex = localeName.indexOf('_');
      if (separatorIndex != -1) {
        String language = localeName.substring(0, separatorIndex);
        String country = localeName.substring(separatorIndex + 1);
        locale = new Locale(language, country);
      } else {
        locale = new Locale(localeName);
      }
      return new LocaleInfo(locale);
    } catch (Throwable e) {
      String msg = "Locale not found for " + localeDescription;
      throw new IllegalArgumentException(msg);
    }
  }
}
