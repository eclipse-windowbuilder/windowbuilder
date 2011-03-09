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
package org.eclipse.wb.tests.designer.swing.model.property;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.property.editor.border.BorderPropertyEditor;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

/**
 * Test for {@link BorderPropertyEditor}.
 * 
 * @author scheglov_ke
 */
public class BorderPropertyEditorTest extends SwingModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // getText()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getText_defaultBorder() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    // property
    Property borderProperty = panel.getPropertyByTitle("border");
    assertEquals(null, getPropertyText(borderProperty));
  }

  public void test_getText_noBorder() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setBorder(null);",
            "  }",
            "}");
    panel.refresh();
    // property
    Property borderProperty = panel.getPropertyByTitle("border");
    assertEquals("(no border)", getPropertyText(borderProperty));
  }

  public void test_getText_EmptyBorder() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setBorder(new EmptyBorder(0, 0, 0, 0));",
            "  }",
            "}");
    panel.refresh();
    // property
    Property borderProperty = panel.getPropertyByTitle("border");
    assertEquals("EmptyBorder", getPropertyText(borderProperty));
  }
}