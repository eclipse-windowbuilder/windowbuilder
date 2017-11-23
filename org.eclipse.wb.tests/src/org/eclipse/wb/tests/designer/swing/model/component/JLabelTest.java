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
package org.eclipse.wb.tests.designer.swing.model.component;

import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.CharacterPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.ObjectPropertyEditor;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.property.editor.DisplayedMnemonicKeyPropertyEditor;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import static org.assertj.core.api.Assertions.assertThat;

import javax.swing.JLabel;

/**
 * Tests for {@link JLabel} support.
 * 
 * @author scheglov_ke
 */
public class JLabelTest extends SwingModelTest {
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
  public void test_displayedMnemonic_property() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new JLabel());",
            "  }",
            "}");
    ComponentInfo label = panel.getChildrenComponents().get(0);
    // we have both variants in ComponentDescription
    {
      GenericPropertyDescription propertyInt =
          label.getDescription().getProperty("setDisplayedMnemonic(int)");
      GenericPropertyDescription propertyChar =
          label.getDescription().getProperty("setDisplayedMnemonic(char)");
      assertNotNull(propertyChar);
      assertNotNull(propertyInt);
    }
    // we have both variants of Property's
    {
      Property propertyChar = label.getPropertyByTitle("displayedMnemonic(char)");
      Property propertyInt = label.getPropertyByTitle("displayedMnemonic(int)");
      assertNotNull(propertyChar);
      assertNotNull(propertyInt);
      // "char" should be marked as preferred, and "int" as "advanced"
      assertSame(PropertyCategory.PREFERRED, propertyChar.getCategory());
      assertSame(PropertyCategory.ADVANCED, propertyInt.getCategory());
      // check editors
      assertInstanceOf(CharacterPropertyEditor.class, propertyChar.getEditor());
      assertInstanceOf(DisplayedMnemonicKeyPropertyEditor.class, propertyInt.getEditor());
    }
  }

  /**
   * To be useful, "setDisplayedMnemonicIndex" should be added <em>after</em>
   * "setDisplayedMnemonic".
   */
  public void test_displayedMnemonicIndex_location() throws Exception {
    dontConvertSingleQuotesToDouble();
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JLabel label = new JLabel();",
            "    label.setDisplayedMnemonic('C');",
            "    add(label);",
            "  }",
            "}");
    ComponentInfo label = panel.getChildrenComponents().get(0);
    // set "displayedMnemonicIndex"
    label.getPropertyByTitle("displayedMnemonicIndex").setValue(1);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JLabel label = new JLabel();",
        "    label.setDisplayedMnemonic('C');",
        "    label.setDisplayedMnemonicIndex(1);",
        "    add(label);",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "labelFor" property
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_labelFor_getText_noInvocation() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JLabel label = new JLabel();",
            "      add(label);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo label = panel.getChildrenComponents().get(0);
    // check "labelFor" property
    Property labelForProperty = label.getPropertyByTitle("labelFor");
    assertNotNull(labelForProperty);
    assertSame(PropertyCategory.PREFERRED, labelForProperty.getCategory());
    assertThat(labelForProperty.getEditor()).isSameAs(ObjectPropertyEditor.INSTANCE);
    // no "setLabelFor()" invocation, so no text
    assertFalse(labelForProperty.isModified());
    assertNull(getPropertyText(labelForProperty));
  }

  public void test_labelFor_getText_hasInvocation() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private final JLabel label = new JLabel();",
            "  private final JTextField textField = new JTextField();",
            "  public Test() {",
            "    add(label);",
            "    label.setLabelFor(textField);",
            "    add(textField);",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo label = panel.getChildrenComponents().get(0);
    // check "labelFor" property
    Property labelForProperty = label.getPropertyByTitle("labelFor");
    assertTrue(labelForProperty.isModified());
    assertEquals("textField", getPropertyText(labelForProperty));
  }
}
