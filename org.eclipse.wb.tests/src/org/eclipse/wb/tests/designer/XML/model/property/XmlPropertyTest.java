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
package org.eclipse.wb.tests.designer.XML.model.property;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.property.EmptyXmlProperty;
import org.eclipse.wb.internal.core.xml.model.property.XmlProperty;
import org.eclipse.wb.tests.designer.XML.model.description.AbstractCoreTest;

import org.eclipse.jdt.core.IJavaProject;

/**
 * Test for {@link XmlProperty}.
 *
 * @author scheglov_ke
 */
public class XmlPropertyTest extends AbstractCoreTest {
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
  public void test_access() throws Exception {
    XmlObjectInfo shell = parse("<Shell/>");
    // create XMLProperty
    XmlProperty property = new EmptyXmlProperty(shell);
    assertSame(null, property.getTitle());
    assertSame(PropertyCategory.NORMAL, property.getCategory());
    // object
    assertSame(shell, property.getObjectInfo());
    assertSame(shell, property.getObject());
    assertSame(shell, property.getAdapter(ObjectInfo.class));
    // adapter
    assertEquals(null, property.getAdapter(null));
    assertEquals(m_javaProject, property.getAdapter(IJavaProject.class));
  }

  /**
   * Test for constructor with {@link PropertyCategory}.
   */
  public void test_constructorWithCategory() throws Exception {
    XmlObjectInfo shell = parse("<Shell/>");
    // create XMLProperty
    XmlProperty property = new EmptyXmlProperty(shell, PropertyCategory.system(6));
    assertTrue(property.getCategory().isSystem());
  }
}