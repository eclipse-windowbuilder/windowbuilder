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
package org.eclipse.wb.tests.designer.XWT.model.property;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.xwt.model.property.editor.style.IStyleClassResolver;
import org.eclipse.wb.internal.xwt.model.property.editor.style.XwtStyleClassResolver;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

/**
 * Test for {@link XwtStyleClassResolver}.
 *
 * @author scheglov_ke
 */
public class XwtStyleClassResolverTest extends XwtModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Exit zone :-) XXX
  //
  ////////////////////////////////////////////////////////////////////////////
  public void _test_exit() throws Exception {
    System.exit(0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_0() throws Exception {
    XmlObjectInfo object = parse("<Shell/>");
    Property property = object.getPropertyByTitle("enabled");
    IStyleClassResolver resolver = XwtStyleClassResolver.INSTANCE;
    // SWT
    assertEquals("", resolver.resolve(property, "org.eclipse.swt.SWT"));
    assertXML("<Shell/>");
    // existing package
    assertEquals("(t:Constants).", resolver.resolve(property, "test.Constants"));
    assertXML("<Shell/>");
    // new package
    assertEquals("(p1:Constants).", resolver.resolve(property, "my.own.package.Constants"));
    assertXML("<Shell xmlns:p1='clr-namespace:my.own.package'/>");
  }
}