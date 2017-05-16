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
package org.eclipse.wb.internal.core.nls.bundle;

import org.eclipse.wb.internal.core.nls.model.LocaleInfo;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;

import org.eclipse.core.resources.IFile;

import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Information/accessor for single .properties file.
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public final class BundleInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Static utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link BundleInfo} for given bundle name and {@link LocaleInfo} using one of the
   *         {@link IFile} from array. Can return <code>null</code> if there are no {@link IFile}
   *         for given {@link LocaleInfo}.
   */
  public static BundleInfo createBundle(IPropertiesAccessor propertiesAccessor,
      String bundleName,
      LocaleInfo localeInfo,
      IFile files[]) throws Exception {
    for (int i = 0; i < files.length; i++) {
      IFile file = files[i];
      if (localeInfo.equals(getLocale(bundleName, file))) {
        return new BundleInfo(propertiesAccessor, bundleName, localeInfo, file);
      }
    }
    return null;
  }

  /**
   * @return the {@link LocaleInfo} extracted from {@link IFile} using name of bundle. For example,
   *         return ru_RU locale for bundle "com.mycorp.messages" and file
   *         "com/mycorp/messages_ru_RU.properties".
   */
  public static LocaleInfo getLocale(String bundleName, IFile file) {
    // prepare name of locale, for example "_ru_RU"
    String shortBundleName = CodeUtils.getShortClass(bundleName);
    String localeName = file.getName();
    localeName = StringUtils.substring(localeName, 0, -".properties".length());
    localeName = localeName.substring(shortBundleName.length());
    //
    if (localeName.length() == 0) {
      return LocaleInfo.DEFAULT;
    } else {
      String localeDescription = "'" + localeName + "' for file '" + file + "'.";
      // should start with '_'
      if (!localeName.startsWith("_")) {
        throw new IllegalArgumentException("'_' expected, but " + localeDescription);
      }
      localeName = localeName.substring(1);
      // create
      return LocaleInfo.create(localeName, localeDescription);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IPropertiesAccessor m_propertiesAccessor;
  private final String m_bundleName;
  private final LocaleInfo m_locale;
  private final IFile m_file;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private BundleInfo(IPropertiesAccessor propertiesAccessor,
      String bundleName,
      LocaleInfo localeInfo,
      IFile file) throws Exception {
    m_propertiesAccessor = propertiesAccessor;
    m_bundleName = bundleName;
    m_file = file;
    m_locale = localeInfo;
    // remember initial stamp
    m_propertiesStamp = m_file.getModificationStamp();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the fully qualified name of bundle.
   */
  public String getBundleName() {
    return m_bundleName;
  }

  /**
   * @return the {@link LocaleInfo} of this bundle.
   */
  public LocaleInfo getLocale() {
    return m_locale;
  }

  /**
   * @return the {@link IFile} with *.properties file.
   */
  public IFile getFile() {
    return m_file;
  }

  /**
   * @return <code>true</code> if properties file was changed externally, for example in other
   *         editor in Eclipse.
   */
  public boolean isExternallyChanged() {
    return m_file.getModificationStamp() != m_propertiesStamp;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  private Map<String, String> m_properties;
  private long m_propertiesStamp;

  /**
   * @return the copy of map key -> value.
   */
  public Map<String, String> getMap() throws Exception {
    return new HashMap<String, String>(getProperties());
  }

  /**
   * Set map key -> value.
   */
  public void setMap(Map<String, String> map) {
    m_properties = new HashMap<String, String>(map);
  }

  /**
   * @return the cached or reloaded (if changed externally) map key -> value.
   */
  private Map<String, String> getProperties() throws Exception {
    if (m_properties == null || isExternallyChanged()) {
      InputStream is = m_file.getContents(true);
      try {
        String charset = m_file.getCharset();
        m_properties = m_propertiesAccessor.load(is, charset);
      } finally {
        is.close();
      }
      //
      m_propertiesStamp = m_file.getModificationStamp();
    }
    return m_properties;
  }

  /**
   * Saves current map key -> value into host {@link IFile}.
   *
   * @param comments
   *          the parameters for add in file headers.
   */
  public void save(String comments) throws Exception {
    if (m_properties != null) {
      // prepare bytes for properties
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      String charset = m_file.getCharset();
      m_propertiesAccessor.save(baos, charset, m_properties, comments);
      // set bytes for file
      {
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        m_file.setContents(bais, true, true, null);
      }
      // remember new stamp
      m_propertiesStamp = m_file.getModificationStamp();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Values and keys
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the value for given key or <code>null</code>, if there are no such key in this bundle.
   */
  public String getValue(String key) throws Exception {
    return getProperties().get(key);
  }

  /**
   * Sets the value for given key. If value is empty string, key will be removed.
   */
  public void setValue(String key, String value) throws Exception {
    Map<String, String> properties = getProperties();
    if (value.length() == 0) {
      properties.remove(key);
    } else {
      properties.put(key, value);
    }
  }

  /**
   * Replaces old key with new one, optionally keeping existing value of <code>newKey</code>.
   */
  public void replaceKey(String oldKey, String newKey, boolean keepOldValue) throws Exception {
    // if we should keep existing value, this means that we should just remove old key
    if (keepOldValue && containsKey(newKey)) {
      removeKey(oldKey);
    } else {
      Map<String, String> properties = getProperties();
      String value = properties.get(oldKey);
      properties.remove(oldKey);
      setValue(newKey, value);
    }
  }

  /**
   * @return the {@link Set} of keys.
   */
  public Set<String> getKeys() throws Exception {
    return getProperties().keySet();
  }

  /**
   * @return <code>true</code> if given bundle contains given key.
   */
  public boolean containsKey(String key) throws Exception {
    return getProperties().containsKey(key);
  }

  /**
   * Removes given key (and value) from bundle.
   */
  public void removeKey(String key) throws Exception {
    getProperties().remove(key);
  }
}
