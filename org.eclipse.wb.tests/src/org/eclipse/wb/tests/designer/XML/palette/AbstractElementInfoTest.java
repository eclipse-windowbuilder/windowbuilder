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
package org.eclipse.wb.tests.designer.XML.palette;

import org.eclipse.wb.internal.core.utils.check.AssertionFailedException;
import org.eclipse.wb.internal.core.xml.editor.palette.model.AbstractElementInfo;
import org.eclipse.wb.internal.core.xml.editor.palette.model.ComponentEntryInfo;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

/**
 * Tests for {@link AbstractElementInfo}.
 *
 * @author scheglov_ke
 */
public class AbstractElementInfoTest extends DesignerTestCase {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for hashCode/equals.
   */
  public void test_AbstractElementInfo_hash_equals() throws Exception {
    String id = "some id";
    // prepare component_1
    ComponentEntryInfo component_1 = new ComponentEntryInfo();
    component_1.setId(id);
    // prepare component_2
    ComponentEntryInfo component_2 = new ComponentEntryInfo();
    component_2.setId(id);
    // check hashCode()
    assertEquals(id.hashCode(), component_1.hashCode());
    assertEquals(id.hashCode(), component_2.hashCode());
    // check equals()
    assertTrue(component_1.equals(component_1));
    assertTrue(component_1.equals(component_2));
    assertFalse(component_1.equals(this));
  }

  /**
   * Test for "id" property.
   */
  public void test_AbstractElementInfo_id() throws Exception {
    ComponentEntryInfo component = new ComponentEntryInfo();
    // try to set "null" - failed
    try {
      component.setId(null);
      fail();
    } catch (AssertionFailedException e) {
    }
    // set first name, success
    String id = "some id";
    component.setId(id);
    assertSame(id, component.getId());
    // try to set second time - failed
    try {
      component.setId("new id");
      fail();
    } catch (AssertionFailedException e) {
    }
  }

  /**
   * Test for "visible" property.
   */
  public void test_AbstractElementInfo_visible() throws Exception {
    ComponentEntryInfo component = new ComponentEntryInfo();
    // visible initially
    assertTrue(component.isVisible());
    // make not visible
    component.setVisible(false);
    assertFalse(component.isVisible());
  }

  /**
   * Test for "name" property.
   */
  public void test_AbstractElementInfo_name() throws Exception {
    ComponentEntryInfo component = new ComponentEntryInfo();
    // no name initially
    assertNull(component.getName());
    // set name
    component.setName("my name");
    assertEquals("my name", component.getName());
  }

  /**
   * Test for "description" property.
   */
  public void test_AbstractElementInfo_description() throws Exception {
    ComponentEntryInfo component = new ComponentEntryInfo();
    // no description initially
    assertNull(component.getDescription());
    // set description
    component.setDescription("my description");
    assertEquals("my description", component.getDescription());
  }
}
