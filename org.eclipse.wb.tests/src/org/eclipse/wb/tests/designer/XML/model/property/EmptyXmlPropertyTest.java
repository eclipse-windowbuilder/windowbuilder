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

import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.property.EmptyXmlProperty;
import org.eclipse.wb.tests.designer.XML.model.description.AbstractCoreTest;

/**
 * Test for {@link EmptyXmlProperty}.
 * 
 * @author scheglov_ke
 */
public class EmptyXmlPropertyTest extends AbstractCoreTest {
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
    EmptyXmlProperty property = new EmptyXmlProperty(object);
    assertFalse(property.isModified());
    assertSame(null, property.getValue());
    property.setValue(null);
  }
}