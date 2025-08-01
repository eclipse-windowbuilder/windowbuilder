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
package org.eclipse.wb.tests.designer.core.nls;

import org.eclipse.wb.internal.core.nls.model.LocaleInfo;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.junit.jupiter.api.Test;

import java.util.Locale;

/**
 * Tests for {@link LocaleInfo}.
 *
 * @author scheglov_ke
 */
public class LocaleInfoTest extends DesignerTestCase {
	@Test
	public void test_default() throws Exception {
		LocaleInfo locale = LocaleInfo.DEFAULT;
		assertNull(locale.getLocale());
		assertTrue(locale.isDefault());
		assertEquals("(default)", locale.getTitle());
	}

	@Test
	public void test_italian() throws Exception {
		LocaleInfo locale = new LocaleInfo(Locale.ITALIAN);
		assertSame(Locale.ITALIAN, locale.getLocale());
		assertEquals("it", locale.getTitle());
	}

	@Test
	public void test_toString() throws Exception {
		assertEquals("it", new LocaleInfo(Locale.ITALIAN).toString());
	}

	@Test
	public void test_equals() throws Exception {
		LocaleInfo locale_it = new LocaleInfo(Locale.ITALIAN);
		LocaleInfo locale_fr = new LocaleInfo(Locale.FRENCH);
		assertTrue(LocaleInfo.DEFAULT.equals(new LocaleInfo(null)));
		assertTrue(locale_it.equals(locale_it));
		assertFalse(locale_it.equals(locale_fr));
		assertFalse(locale_it.equals(this));
	}

	@Test
	public void test_hasCode() throws Exception {
		assertEquals(0, LocaleInfo.DEFAULT.hashCode());
		assertTrue(new LocaleInfo(Locale.ITALIAN).hashCode() != 0);
	}

	@Test
	public void test_getParent() throws Exception {
		LocaleInfo[] locales =
				new LocaleInfo[]{new LocaleInfo(Locale.of("fr", "FR")), new LocaleInfo(Locale.of("ru"))};
		assertSame(locales[1], new LocaleInfo(Locale.of("ru", "RU")).getParent(locales));
		assertSame(LocaleInfo.DEFAULT, new LocaleInfo(Locale.of("ru")).getParent(locales));
	}
}
