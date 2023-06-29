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
package org.eclipse.wb.tests.designer.core.nls.ui;

import org.eclipse.wb.internal.core.nls.ui.FlagImagesRepository;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.eclipse.swt.graphics.Image;

import org.apache.commons.lang.ArrayUtils;

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
	public void test_sortedLocales() throws Exception {
		Locale[] sortedLocales = FlagImagesRepository.getSortedLocales();
		assertTrue(sortedLocales.length >= 140);
		{
			int index_1 = ArrayUtils.indexOf(sortedLocales, new Locale("en"));
			int index_2 = ArrayUtils.indexOf(sortedLocales, new Locale("ru"));
			assertTrue(index_1 < index_2);
		}
		{
			int index_1 = ArrayUtils.indexOf(sortedLocales, new Locale("de"));
			int index_2 = ArrayUtils.indexOf(sortedLocales, new Locale("it"));
			assertTrue(index_1 < index_2);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// getEmptyFlagImage
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_getEmptyFlagImage() throws Exception {
		assertNotNull(FlagImagesRepository.getEmptyFlagImage());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// getFlagImage
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_getFlagImage_noSuchLocale() throws Exception {
		assertNull(FlagImagesRepository.getFlagImage(new Locale("noSuchLocale")));
	}

	public void test_getFlagImage_ru() throws Exception {
		Image ruImage = FlagImagesRepository.getFlagImage(new Locale("ru"));
		assertNotNull(ruImage);
		assertSame(FlagImagesRepository.getFlagImage(new Locale("ru", "RU")), ruImage);
	}

	public void test_getFlagImage_en() throws Exception {
		assertSame(
				FlagImagesRepository.getFlagImage(new Locale("en", "US")),
				FlagImagesRepository.getFlagImage(new Locale("en")));
	}

	public void test_getFlagImage_zh() throws Exception {
		assertSame(
				FlagImagesRepository.getFlagImage(new Locale("zh", "CN")),
				FlagImagesRepository.getFlagImage(new Locale("zh")));
	}

	public void test_getFlagImage_ar() throws Exception {
		assertSame(
				FlagImagesRepository.getFlagImage(new Locale("ar", "AE")),
				FlagImagesRepository.getFlagImage(new Locale("ar")));
	}

	public void test_getFlagImage_YU() throws Exception {
		assertNotNull(FlagImagesRepository.getFlagImage(new Locale("se", "YU")));
	}
}
