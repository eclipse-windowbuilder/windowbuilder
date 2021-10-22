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
package org.eclipse.wb.tests.designer.core.model.property;

import org.eclipse.wb.internal.core.model.property.hierarchy.ComponentClassProperty;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipProvider;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipTextProvider;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.absolute.AbsoluteLayoutInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

/**
 * Test for {@link ComponentClassProperty}.
 *
 * @author lobas_av
 */
public class ComponentClassPropertyTest extends SwingModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Explicit {@link AbsoluteLayoutInfo} has <code>null</code> class, so can not have "Class"
   * property.
   */
  public void test_noForNullClass() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(null);",
            "  }",
            "}");
    assertNull(panel.getLayout().getPropertyByTitle("Class"));
  }

  public void test_property() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    // property
    ComponentClassProperty property = (ComponentClassProperty) panel.getPropertyByTitle("Class");
    assertNotNull(property);
    assertFalse(property.isModified());
    assertTrue(property.getCategory().isSystem());
    // value
    assertEquals("javax.swing.JPanel", property.getValue());
    property.setValue("can-not-set-value");
    assertEquals("javax.swing.JPanel", property.getValue());
    property.setValue(null);
    assertEquals("javax.swing.JPanel", property.getValue());
    // tooltip
    {
      assertNull(property.getAdapter(Object.class));
      PropertyTooltipProvider tooltipProvider = property.getAdapter(PropertyTooltipProvider.class);
      assertInstanceOf(PropertyTooltipTextProvider.class, tooltipProvider);
      assertNotNull(ReflectionUtils.invokeMethod(
          tooltipProvider,
          "getText(org.eclipse.wb.internal.core.model.property.Property)",
          property));
    }
    // editor
    assertEquals("javax.swing.JPanel", getPropertyText(property));
    assertNotNull(property.getEditor().getAdapter(PropertyTooltipProvider.class));
  }
}