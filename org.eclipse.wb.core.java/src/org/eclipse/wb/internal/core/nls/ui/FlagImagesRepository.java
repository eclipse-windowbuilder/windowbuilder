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
package org.eclipse.wb.internal.core.nls.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.DesignerPlugin;

import org.eclipse.swt.graphics.Image;

import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Repository of flags for locale/country.
 *
 * @author scheglov_ke
 * @coverage core.nls.ui
 */
public class FlagImagesRepository {
  private static Map<String, Image> m_countriesFlags = Maps.newHashMap();
  private static Locale[] m_locales;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialization
  //
  ////////////////////////////////////////////////////////////////////////////
  private static void init() {
    if (m_locales == null) {
      // prepare sorted Locale's
      {
        List<Locale> locales = Lists.newArrayList();
        Collections.addAll(locales, Locale.getAvailableLocales());
        Collections.sort(locales, new Comparator<Locale>() {
          public int compare(Locale o1, Locale o2) {
            return o1.toString().compareTo(o2.toString());
          }
        });
        m_locales = locales.toArray(new Locale[locales.size()]);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the array of {@link Locale}'s sorted by title.
   */
  public static Locale[] getSortedLocales() {
    init();
    return m_locales;
  }

  /**
   * @return the {@link Image} of flag for default {@link Locale}.
   */
  public static Image getEmptyFlagImage() {
    return DesignerPlugin.getImage("nls/flags/flag_empty.png");
  }

  /**
   * @return the {@link Image} of flag for given {@link Locale}.
   */
  public static Image getFlagImage(Locale locale) {
    init();
    String localeCountry = locale.getCountry();
    String localeLanguage = locale.getLanguage();
    // if locale has no assosiated country set, try to find the locale with the same language but with the country set
    if (localeCountry.length() == 0) {
      // special cases
      if (localeLanguage.equals("ar")) {
        localeCountry = "AE";
      } else if (localeLanguage.equals("zh")) {
        localeCountry = "CN";
      } else if (localeLanguage.equals("en")) {
        localeCountry = "US";
      } else {
        // try to guess
        String localeCountryCandidate = "";
        for (int i = 0; i < m_locales.length; i++) {
          Locale lookupLocale = m_locales[i];
          String lookupLanguage = lookupLocale.getLanguage();
          if (lookupLanguage.equals(localeLanguage)) {
            if (lookupLocale.getCountry().length() != 0) {
              localeCountryCandidate = lookupLocale.getCountry();
              if (localeCountryCandidate.equalsIgnoreCase(lookupLanguage)) {
                localeCountry = localeCountryCandidate;
                break;
              }
            }
          }
        }
        if (localeCountry.length() == 0) {
          localeCountry = localeCountryCandidate;
        }
      }
    }
    //
    Image flagImage = m_countriesFlags.get(localeCountry);
    if (flagImage == null) {
      try {
        String flagFileName = null;
        if (localeCountry.equalsIgnoreCase("YU")) {
          localeCountry = "CS"; // use Serbia and Montenegro
        }
        if (StringUtils.isEmpty(localeCountry)) {
          return null;
        }
        flagFileName = localeCountry.toLowerCase() + ".png";
        flagImage = DesignerPlugin.getImage("nls/flags/" + flagFileName);
        m_countriesFlags.put(localeCountry, flagImage);
      } catch (Throwable e) {
        return null;
      }
    }
    return flagImage;
  }
}
