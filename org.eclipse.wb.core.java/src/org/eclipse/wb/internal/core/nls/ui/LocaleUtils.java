/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.internal.core.nls.ui;

import org.eclipse.wb.internal.core.nls.model.LocaleInfo;

import org.eclipse.swt.graphics.Image;

import java.util.Arrays;
import java.util.Comparator;

/**
 * UI utils for {@link LocaleInfo}.
 *
 * @author scheglov_ke
 * @coverage core.nls.ui
 */
public class LocaleUtils {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private LocaleUtils() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the flag image for given {@link LocaleInfo}.
	 */
	public static Image getImage(LocaleInfo localeInfo) {
		if (localeInfo.isDefault()) {
			return FlagImagesRepository.getEmptyFlagImage();
		} else {
			return FlagImagesRepository.getFlagImage(localeInfo.getLocale());
		}
	}

	/**
	 * Sorts given array of {@link LocaleInfo}'s by title.
	 */
	public static void sortByTitle(LocaleInfo locales[]) {
		Arrays.sort(locales, new Comparator<LocaleInfo>() {
			@Override
			public int compare(LocaleInfo locale_1, LocaleInfo locale_2) {
				return locale_1.getTitle().compareTo(locale_2.getTitle());
			}
		});
	}
}
