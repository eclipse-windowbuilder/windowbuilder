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

import org.eclipse.wb.internal.core.model.property.EmptyProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.StringArrayPropertyEditor;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.swt.widgets.Text;

/**
 * Test for {@link StringArrayPropertyEditor}.
 *
 * @author scheglov_ke
 */
public class StringArrayPropertyEditorTest extends XwtModelTest {
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
  // getText()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getText_notValue() throws Exception {
    Property property = new EmptyProperty(StringArrayPropertyEditor.INSTANCE);
    // check state
    assertFalse(property.isModified());
    assertEquals("[]", getPropertyText(property));
  }

  public void test_getText_withItems() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell xmlns:p1='clr-namespace:java.lang'>",
        "  <List wbp:name='widget'>",
        "    <List.items>",
        "      <p1:String>aaa</p1:String>",
        "      <p1:String>bbb</p1:String>",
        "      <p1:String>ccc</p1:String>",
        "    </List.items>",
        "  </List>",
        "</Shell>");
    refresh();
    XmlObjectInfo widget = getObjectByName("widget");
    Property property = widget.getPropertyByTitle("items");
    // check state
    assertEquals("[aaa, bbb, ccc]", getPropertyText(property));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dialog
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_dialog() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell xmlns:p1='clr-namespace:java.lang'>",
        "  <List wbp:name='widget'/>",
        "</Shell>");
    refresh();
    XmlObjectInfo widget = getObjectByName("widget");
    // prepare property
    final Property property = widget.getPropertyByTitle("items");
    new UiContext().executeAndCheck(new UIRunnable() {
      @Override
      public void run(UiContext context) throws Exception {
        openPropertyDialog(property);
      }
    }, new UIRunnable() {
      @Override
      public void run(UiContext context) throws Exception {
        context.useShell("items");
        {
          Text text = context.getTextByLabel("&Elements:");
          text.setText("aaa\nbbb\nccc");
        }
        context.clickButton("OK");
      }
    });
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell xmlns:p1='clr-namespace:java.lang'>",
        "  <List wbp:name='widget'>",
        "    <List.items>",
        "      <p1:String>aaa</p1:String>",
        "      <p1:String>bbb</p1:String>",
        "      <p1:String>ccc</p1:String>",
        "    </List.items>",
        "  </List>",
        "</Shell>");
  }
}