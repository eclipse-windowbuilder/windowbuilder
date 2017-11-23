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

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.xwt.model.property.editor.ObjectPropertyEditor;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;
import org.eclipse.wb.tests.designer.core.model.TestObjectInfo;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.TreeItem;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ObjectPropertyEditor}.
 * 
 * @author scheglov_ke
 */
public class ObjectPropertyEditorTest extends XwtModelTest {
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
  private void prepareMyComponent_setButton() throws Exception {
    prepareMyComponent(
        "  // filler filler filler filler filler",
        "  // filler filler filler filler filler",
        "  // filler filler filler filler filler",
        "  private Button m_button;",
        "  public void setValue(Button button) {",
        "    m_button = button;",
        "  }",
        "  public Button getValue() {",
        "    return m_button;",
        "  }",
        "");
    waitForAutoBuild();
  }

  public void test_setComponent() throws Exception {
    prepareMyComponent_setButton();
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Text/>",
        "  <Button/>",
        "  <t:MyComponent wbp:name='component'/>",
        "</Shell>");
    refresh();
    XmlObjectInfo component = getObjectByName("component");
    // prepare property
    final Property property = component.getPropertyByTitle("value");
    assertNotNull(property);
    assertSame(PropertyCategory.ADVANCED, property.getCategory());
    // no value
    assertFalse(property.isModified());
    assertEquals(null, getPropertyText(property));
    // prepare editor
    final PropertyEditor propertyEditor = property.getEditor();
    assertThat(propertyEditor).isSameAs(ObjectPropertyEditor.INSTANCE);
    // animate
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        openPropertyDialog(property);
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("value");
        Button okButton = context.getButtonByText("OK");
        // initially "Shell" selected, so invalid
        {
          TreeItem shellItem = context.getTreeItem("Shell");
          TreeItem[] selection = shellItem.getParent().getSelection();
          assertThat(selection).containsOnly(shellItem);
        }
        assertFalse(okButton.isEnabled());
        // "Button" - valid
        {
          TreeItem buttonItem = context.getTreeItem("Button");
          UiContext.setSelection(buttonItem);
          assertTrue(okButton.isEnabled());
        }
        // click OK
        context.clickButton(okButton);
      }
    });
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Text/>",
        "  <Button x:Name='button'/>",
        "  <t:MyComponent wbp:name='component' value='{Binding ElementName=button}'/>",
        "</Shell>");
    assertEquals("Button - button", getPropertyText(property));
  }

  /**
   * {@link ObjectPropertyEditor} should select current value in dialog.
   */
  public void test_initialSelection() throws Exception {
    prepareMyComponent_setButton();
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Text/>",
        "  <Button x:Name='button'/>",
        "  <t:MyComponent wbp:name='component' value='{Binding ElementName=button}'/>",
        "</Shell>");
    refresh();
    XmlObjectInfo component = getObjectByName("component");
    // prepare property
    final Property property = component.getPropertyByTitle("value");
    assertNotNull(property);
    assertSame(PropertyCategory.ADVANCED, property.getCategory());
    // has value
    assertTrue(property.isModified());
    assertEquals("Button - button", getPropertyText(property));
    // animate
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        openPropertyDialog(property);
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("value");
        // initially "Button" selected, so invalid
        try {
          TreeItem buttonItem = context.getTreeItem("Button - button");
          TreeItem[] selection = buttonItem.getParent().getSelection();
          assertThat(selection).containsOnly(buttonItem);
        } finally {
          context.clickButton("Cancel");
        }
      }
    });
  }

  /**
   * Test for case when one of the models is just {@link ObjectInfo}, not {@link XmlObjectInfo}.
   */
  public void test_withObjectInfo() throws Exception {
    prepareMyComponent_setButton();
    XmlObjectInfo shell =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <t:MyComponent wbp:name='component'/>",
            "</Shell>");
    refresh();
    XmlObjectInfo component = getObjectByName("component");
    shell.addChild(new TestObjectInfo());
    // animate, just check that no exceptions
    final Property property = component.getPropertyByTitle("value");
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        openPropertyDialog(property);
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("value");
        context.clickButton("Cancel");
      }
    });
  }
}
