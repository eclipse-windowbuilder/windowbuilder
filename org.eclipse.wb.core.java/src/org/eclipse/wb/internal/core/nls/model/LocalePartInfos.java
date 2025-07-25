/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.nls.model;

import org.eclipse.wb.internal.core.nls.ui.FlagImagesRepository;

import org.eclipse.swt.graphics.Image;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Utilities for {@link LocaleInfo}.
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public final class LocalePartInfos {
	private static LocalePartInfo m_languages[];
	private static LocalePartInfo m_countries[];

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public static LocalePartInfo[] getLanguages() {
		initLanguagesAndCountries();
		return m_languages;
	}

	public static LocalePartInfo[] getCountries() {
		initLanguagesAndCountries();
		return m_countries;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Find items
	//
	////////////////////////////////////////////////////////////////////////////
	public static int indexByName(LocalePartInfo[] parts, String name) {
		for (int i = 0; i < parts.length; i++) {
			LocalePartInfo part = parts[i];
			if (part.getName().equals(name)) {
				return i;
			}
		}
		return -1;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Initialization
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Prepare arrays of all available languages and countries with titles and flags.
	 */
	private static void initLanguagesAndCountries() {
		if (m_languages == null) {
			// languages
			{
				Set<LocalePartInfo> languagesSet = new HashSet<>();
				// fill
				for (String language : Locale.getISOLanguages()) {
					Locale locale = Locale.of(language);
					Image flagImage = FlagImagesRepository.getFlagImage(locale);
					languagesSet.add(new LocalePartInfo(locale.getLanguage(),
							locale.getDisplayLanguage(),
							flagImage));
				}
				// remember as array
				m_languages = languagesSet.toArray(new LocalePartInfo[languagesSet.size()]);
				Arrays.sort(m_languages);
			}
			// countries
			{
				Set<LocalePartInfo> countriesSet = new HashSet<>();
				countriesSet.add(new LocalePartInfo("", "(none)", FlagImagesRepository.getEmptyFlagImage()));
				// fill
				for (String country : Locale.getISOCountries()) {
					Locale locale = Locale.of("", country);
					Image flagImage = FlagImagesRepository.getFlagImage(locale);
					countriesSet.add(new LocalePartInfo(locale.getCountry(),
							locale.getDisplayCountry(),
							flagImage));
				}
				// remember as array
				m_countries = countriesSet.toArray(new LocalePartInfo[countriesSet.size()]);
				Arrays.sort(m_countries);
			}
		}
	}
}
