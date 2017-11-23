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

import org.eclipse.wb.internal.core.model.property.EmptyProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.xwt.model.property.editor.font.FontPropertyEditor;
import org.eclipse.wb.internal.xwt.model.property.editor.font.FontSupport;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.TabItem;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link FontPropertyEditor}.
 * 
 * @author scheglov_ke
 */
public class FontPropertyEditorTest extends XwtModelTest {
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
  // FontSupport
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link FontSupport#getFontFamilies()}.
   */
  public void test_FontSupport_getFontFamilies() throws Exception {
    String[] families = FontSupport.getFontFamilies();
    assertThat(families).contains("Segoe UI", "Arial");
  }

  /**
   * Test for {@link FontSupport#getFontStyleText(int)}.
   */
  public void test_FontSupport_getFontStyleText() throws Exception {
    assertEquals("", FontSupport.getFontStyleText(SWT.NORMAL));
    assertEquals("BOLD", FontSupport.getFontStyleText(SWT.BOLD));
    assertEquals("ITALIC", FontSupport.getFontStyleText(SWT.ITALIC));
    assertEquals("BOLD ITALIC", FontSupport.getFontStyleText(SWT.BOLD | SWT.ITALIC));
  }

  /**
   * Test for {@link FontSupport#getFontStyleSource(int)}.
   */
  public void test_FontSupport_getFontStyleSource() throws Exception {
    assertEquals("", FontSupport.getFontStyleSource(SWT.NORMAL));
    assertEquals("BOLD", FontSupport.getFontStyleSource(SWT.BOLD));
    assertEquals("ITALIC", FontSupport.getFontStyleSource(SWT.ITALIC));
    assertEquals("BOLD | ITALIC", FontSupport.getFontStyleSource(SWT.BOLD | SWT.ITALIC));
  }

  /**
   * Test for {@link FontSupport#getFontPreview(Font)}.
   */
  public void test_FontSupport_getFontPreview() throws Exception {
    Font font = new Font(null, "Arial", 14, SWT.NONE);
    try {
      Image preview = FontSupport.getFontPreview(font);
      assertNotNull(preview);
      assertFalse(preview.isDisposed());
      preview.dispose();
    } finally {
      font.dispose();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getText()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getText_notColorValue() throws Exception {
    Property property = new EmptyProperty(FontPropertyEditor.INSTANCE);
    // check state
    assertFalse(property.isModified());
    assertEquals(null, getPropertyText(property));
  }

  public void test_getText_defaultValue() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    refresh();
    XmlObjectInfo button = getObjectByName("button");
    // prepare property
    Property property = button.getPropertyByTitle("font");
    assertEquals("Segoe UI,9", getPropertyText(property));
    assertEquals("Segoe UI,9", getPropertyClipboardSource(property));
  }

  public void test_getText_withStyles() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button wbp:name='button' font='Arial,14,ITALIC|BOLD'/>",
        "</Shell>");
    refresh();
    XmlObjectInfo button = getObjectByName("button");
    // prepare property
    Property property = button.getPropertyByTitle("font");
    assertEquals("Arial,14,BOLD | ITALIC", getPropertyText(property));
    assertEquals("Arial,14,BOLD | ITALIC", getPropertyClipboardSource(property));
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
        "<Shell>",
        "  <Button wbp:name='button' font='Arial,14,BOLD'/>",
        "</Shell>");
    refresh();
    XmlObjectInfo button = getObjectByName("button");
    // prepare property
    final Property property = button.getPropertyByTitle("font");
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        openPropertyDialog(property);
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("Font chooser");
        TabItem tabItem = context.getTabItem("Construction");
        // prepare widgets
        java.util.List<List> listWidgets = context.findWidgets(tabItem, List.class);
        List familyList = listWidgets.get(0);
        List styleList = listWidgets.get(1);
        List sizeList = listWidgets.get(2);
        // initial state
        assertThat(familyList.getSelection()).containsOnly("Arial");
        assertThat(styleList.getSelection()).containsOnly("BOLD");
        assertThat(sizeList.getSelection()).containsOnly("14");
        // set new values
        UiContext.setSelection(familyList, "Verdana");
        UiContext.setSelection(styleList, "ITALIC");
        UiContext.setSelection(sizeList, "30");
        context.clickButton("OK");
      }
    });
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button wbp:name='button' font='Verdana,30,ITALIC'/>",
        "</Shell>");
  }
}