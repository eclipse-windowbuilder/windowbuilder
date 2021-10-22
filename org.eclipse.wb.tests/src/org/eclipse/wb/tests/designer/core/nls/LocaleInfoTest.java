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
package org.eclipse.wb.tests.designer.core.nls;

import org.eclipse.wb.internal.core.nls.model.LocaleInfo;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import java.util.Locale;

/**
 * Tests for {@link LocaleInfo}.
 *
 * @author scheglov_ke
 */
public class LocaleInfoTest extends DesignerTestCase {
  public void test_default() throws Exception {
    LocaleInfo locale = LocaleInfo.DEFAULT;
    assertNull(locale.getLocale());
    assertTrue(locale.isDefault());
    assertEquals("(default)", locale.getTitle());
  }

  public void test_italian() throws Exception {
    LocaleInfo locale = new LocaleInfo(Locale.ITALIAN);
    assertSame(Locale.ITALIAN, locale.getLocale());
    assertEquals("it", locale.getTitle());
  }

  public void test_toString() throws Exception {
    assertEquals("it", new LocaleInfo(Locale.ITALIAN).toString());
  }

  public void test_equals() throws Exception {
    LocaleInfo locale_it = new LocaleInfo(Locale.ITALIAN);
    LocaleInfo locale_fr = new LocaleInfo(Locale.FRENCH);
    assertTrue(LocaleInfo.DEFAULT.equals(new LocaleInfo(null)));
    assertTrue(locale_it.equals(locale_it));
    assertFalse(locale_it.equals(locale_fr));
    assertFalse(locale_it.equals(this));
  }

  public void test_hasCode() throws Exception {
    assertEquals(0, LocaleInfo.DEFAULT.hashCode());
    assertTrue(new LocaleInfo(Locale.ITALIAN).hashCode() != 0);
  }

  public void test_getParent() throws Exception {
    LocaleInfo[] locales =
        new LocaleInfo[]{new LocaleInfo(new Locale("fr", "FR")), new LocaleInfo(new Locale("ru"))};
    assertSame(locales[1], new LocaleInfo(new Locale("ru", "RU")).getParent(locales));
    assertSame(LocaleInfo.DEFAULT, new LocaleInfo(new Locale("ru")).getParent(locales));
  }
}
