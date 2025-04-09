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
package org.eclipse.wb.tests.designer.core.nls.ui;

import org.eclipse.wb.internal.core.nls.model.LocaleInfo;
import org.eclipse.wb.internal.core.nls.ui.FlagImagesRepository;
import org.eclipse.wb.internal.core.nls.ui.LocaleUtils;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.junit.Test;

import java.util.Locale;

/**
 * Tests for {@link LocaleUtils}.
 *
 * @author scheglov_ke
 */
public class LocaleUtilsTest extends DesignerTestCase {
	////////////////////////////////////////////////////////////////////////////
	//
	// getImage
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_getImage_default() throws Exception {
		assertSame(FlagImagesRepository.getEmptyFlagImage(), LocaleUtils.getImage(LocaleInfo.DEFAULT));
	}

	@Test
	public void test_getImage_fr() throws Exception {
		Locale locale = Locale.FRENCH;
		LocaleInfo localeInfo = new LocaleInfo(locale);
		assertSame(FlagImagesRepository.getFlagImage(locale), LocaleUtils.getImage(localeInfo));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// sortByTitle
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_sortByTitle() throws Exception {
		LocaleInfo locales[] =
				new LocaleInfo[]{
						new LocaleInfo(new Locale("en")),
						new LocaleInfo(new Locale("ar")),
						new LocaleInfo(new Locale("ru")),};
		// initial check
		assertEquals("en", locales[0].getTitle());
		assertEquals("ar", locales[1].getTitle());
		assertEquals("ru", locales[2].getTitle());
		// sort and check
		LocaleUtils.sortByTitle(locales);
		assertEquals("ar", locales[0].getTitle());
		assertEquals("en", locales[1].getTitle());
		assertEquals("ru", locales[2].getTitle());
	}
}
