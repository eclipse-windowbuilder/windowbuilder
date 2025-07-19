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
package org.eclipse.wb.tests.designer.core.nls.ui;

import org.eclipse.wb.internal.core.nls.ui.FlagImagesRepository;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.eclipse.swt.graphics.Image;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;

import java.util.Locale;

/**
 * Tests for {@link FlagImagesRepository}.
 *
 * @author scheglov_ke
 */
public class FlagRepositoryTest extends DesignerTestCase {
	////////////////////////////////////////////////////////////////////////////
	//
	// getSortedLocales
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_sortedLocales() throws Exception {
		Locale[] sortedLocales = FlagImagesRepository.getSortedLocales();
		assertTrue(sortedLocales.length >= 140);
		{
			int index_1 = ArrayUtils.indexOf(sortedLocales, Locale.of("en"));
			int index_2 = ArrayUtils.indexOf(sortedLocales, Locale.of("ru"));
			assertTrue(index_1 < index_2);
		}
		{
			int index_1 = ArrayUtils.indexOf(sortedLocales, Locale.of("de"));
			int index_2 = ArrayUtils.indexOf(sortedLocales, Locale.of("it"));
			assertTrue(index_1 < index_2);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// getEmptyFlagImage
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_getEmptyFlagImage() throws Exception {
		assertNotNull(FlagImagesRepository.getEmptyFlagImage());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// getFlagImage
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_getFlagImage_noSuchLocale() throws Exception {
		assertNull(FlagImagesRepository.getFlagImage(Locale.of("noSuchLocale")));
	}

	@Test
	public void test_getFlagImage_ru() throws Exception {
		Image ruImage = FlagImagesRepository.getFlagImage(Locale.of("ru"));
		assertNotNull(ruImage);
		assertSame(FlagImagesRepository.getFlagImage(Locale.of("ru", "RU")), ruImage);
	}

	@Test
	public void test_getFlagImage_en() throws Exception {
		assertSame(
				FlagImagesRepository.getFlagImage(Locale.of("en", "US")),
				FlagImagesRepository.getFlagImage(Locale.of("en")));
	}

	@Test
	public void test_getFlagImage_zh() throws Exception {
		assertSame(
				FlagImagesRepository.getFlagImage(Locale.of("zh", "CN")),
				FlagImagesRepository.getFlagImage(Locale.of("zh")));
	}

	@Test
	public void test_getFlagImage_ar() throws Exception {
		assertSame(
				FlagImagesRepository.getFlagImage(Locale.of("ar", "AE")),
				FlagImagesRepository.getFlagImage(Locale.of("ar")));
	}

	@Test
	public void test_getFlagImage_YU() throws Exception {
		assertNotNull(FlagImagesRepository.getFlagImage(Locale.of("se", "YU")));
	}
}
