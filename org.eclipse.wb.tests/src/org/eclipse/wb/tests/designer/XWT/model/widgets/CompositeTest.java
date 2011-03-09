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
package org.eclipse.wb.tests.designer.XWT.model.widgets;

import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.xml.model.clipboard.XmlObjectMemento;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

/**
 * Test for {@link CompositeInfo}.
 * 
 * @author scheglov_ke
 */
public class CompositeTest extends XwtModelTest {
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
  public void test_parse_empty() throws Exception {
    CompositeInfo composite = parse("<Composite/>");
    composite.refresh();
    // has object
    {
      assertNotNull(composite.getObject());
      assertSame(composite.getComposite(), composite.getObject());
    }
    // has Image
    {
      Image image = composite.getImage();
      assertNotNull(image);
      assertEquals(450, image.getBounds().width);
      assertEquals(300, image.getBounds().height);
    }
    // has "model" bounds
    {
      Rectangle bounds = composite.getModelBounds();
      assertEquals(450, bounds.width);
      assertEquals(300, bounds.height);
    }
    // has "parent" bounds
    {
      Rectangle bounds = composite.getBounds();
      assertEquals(450, bounds.width);
      assertEquals(300, bounds.height);
    }
    // client area insets
    {
      Insets expected = new Insets(0, 0, 0, 0);
      assertEquals(expected, composite.getClientAreaInsets());
    }
  }

  /**
   * Test for {@link CompositeInfo#getClientAreaInsets2()}.
   */
  public void test_getClientAreaInsets2_forComposite() throws Exception {
    CompositeInfo composite = parse("<Composite/>");
    composite.refresh();
    //
    {
      Insets expected = new Insets(0, 0, 0, 0);
      assertEquals(expected, composite.getClientAreaInsets2());
    }
  }

  /**
   * Test for {@link CompositeInfo#getClientAreaInsets2()}.
   */
  public void test_getClientAreaInsets2_forGroup() throws Exception {
    CompositeInfo composite = parse("<Group/>");
    composite.refresh();
    //
    {
      Insets expected = new Insets(15, 3, 3, 3);
      assertEquals(expected, composite.getClientAreaInsets2());
    }
  }

  public void test_parse_withButton() throws Exception {
    CompositeInfo composite =
        parse(
            "<Composite>",
            "  <Composite.layout>",
            "    <RowLayout/>",
            "  </Composite.layout>",
            "  <Button wbp:name='button' text='My button'/>",
            "</Composite>");
    assertHierarchy(
        "// filler filler filler",
        "<Composite>",
        "  <RowLayout>",
        "  <Button wbp:name='button' text='My button'>",
        "    virtual-LayoutData: org.eclipse.swt.layout.RowData");
    composite.refresh();
    // button
    {
      ControlInfo button = getObjectByName("button");
      int expectedWidth = ControlTest.BUTTON_ON_ROW_WIDTH;
      int expectedHeight = ControlTest.BUTTON_ON_ROW_HEIGHT;
      // has Image
      {
        Image image = button.getImage();
        assertNotNull(image);
        assertEquals(expectedWidth, image.getBounds().width);
        assertEquals(expectedHeight, image.getBounds().height);
      }
      // has "model" bounds
      {
        Rectangle bounds = button.getModelBounds();
        assertEquals(new Rectangle(3, 3, expectedWidth, expectedHeight), bounds);
      }
      // has "parent" bounds
      {
        Rectangle bounds = button.getBounds();
        assertEquals(new Rectangle(3, 3, expectedWidth, expectedHeight), bounds);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Set Layout" action in context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that {@link CompositeInfo} contributes "Set layout" sub-menu in context menu.
   */
  public void test_setLayoutMenu_absolute() throws Exception {
    CompositeInfo container =
        parse(
            "// filler filler filler filler filler",
            "<Composite>",
            "  <Composite.layout>",
            "    <RowLayout/>",
            "  </Composite.layout>",
            "</Composite>");
    container.refresh();
    assertTrue(container.hasLayout());
    // prepare "Set Layout"
    IMenuManager layoutManager = getSetLayoutMenu(container);
    // set "absolute"
    {
      IAction action = findChildAction(layoutManager, "Absolute layout");
      assertNotNull(action);
      assertNotNull(action.getImageDescriptor());
      action.run();
      assertXML(
          "// filler filler filler filler filler",
          "// filler filler filler filler filler",
          "<Composite layout='{x:Null}'/>");
    }
  }

  /**
   * Test that {@link CompositeInfo} contributes "Set layout" sub-menu in context menu.
   */
  public void test_setLayoutMenu_normal() throws Exception {
    CompositeInfo container = parse("<Composite/>");
    container.refresh();
    assertTrue(container.hasLayout());
    // prepare "Set Layout"
    IMenuManager layoutManager = getSetLayoutMenu(container);
    // check for existing actions
    assertHasChildAction(layoutManager, "GridLayout");
    assertHasChildAction(layoutManager, "FillLayout");
    assertHasChildAction(layoutManager, "RowLayout");
    assertHasChildAction(layoutManager, "FormLayout");
    assertHasChildAction(layoutManager, "StackLayout");
    // use one of the actions to set new layout
    {
      IAction action = findChildAction(layoutManager, "RowLayout");
      assertNotNull(action.getImageDescriptor());
      action.run();
      assertXML(
          "// filler filler filler filler filler",
          "<Composite>",
          "  <Composite.layout>",
          "    <RowLayout/>",
          "  </Composite.layout>",
          "</Composite>");
    }
  }

  /**
   * We don't support "GroupLayout" in XWT, so "Set layout" sub-menu context menu should not have
   * it.
   */
  public void test_setLayoutMenu_noGroupLayout() throws Exception {
    CompositeInfo container = parse("<Composite/>");
    // prepare "Set Layout" menu manager
    IMenuManager layoutManager = getSetLayoutMenu(container);
    // no GroupLayout
    IAction action = findChildAction(layoutManager, "GroupLayout");
    assertNull(action);
  }

  private static IMenuManager getSetLayoutMenu(CompositeInfo container) throws Exception {
    IMenuManager menuManager = getContextMenu(container);
    IMenuManager layoutManager = findChildMenuManager(menuManager, "Set layout");
    assertNotNull(layoutManager);
    return layoutManager;
  }

  private static void assertHasChildAction(IMenuManager layoutManager, String text) {
    assertNotNull(findChildAction(layoutManager, text));
  }

  /**
   * No "Set Layout" sub-menu if {@link Composite} has no layout.
   */
  public void test_setLayoutMenu_2() throws Exception {
    parse(
        "<Composite>",
        "  <Composite.layout>",
        "    <FillLayout/>",
        "  </Composite.layout>",
        "  <Table wbp:name='table'/>",
        "</Composite>");
    refresh();
    CompositeInfo table = getObjectByName("table");
    // no layout
    assertFalse(table.hasLayout());
    // ...so, no "Set layout" menu
    {
      IMenuManager menuManager = getContextMenu(table);
      IMenuManager layoutManager = findChildMenuManager(menuManager, "Set layout");
      assertNull(layoutManager);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // shouldDrawDotsBorder()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link CompositeInfo#shouldDrawDotsBorder()}.
   */
  public void test_shouldDrawDotsBorder() throws Exception {
    CompositeInfo shell =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Table wbp:name='table'/>",
            "  <Composite wbp:name='composite_1'/>",
            "  <Composite wbp:name='composite_2' x:Style='BORDER'/>",
            "</Shell>");
    refresh();
    CompositeInfo table = getObjectByName("table");
    CompositeInfo composite_1 = getObjectByName("composite_1");
    CompositeInfo composite_2 = getObjectByName("composite_2");
    // not Composite itself
    assertFalse(shell.shouldDrawDotsBorder());
    assertFalse(table.shouldDrawDotsBorder());
    // Composite without border
    assertTrue(composite_1.shouldDrawDotsBorder());
    // Composite with border
    assertFalse(composite_2.shouldDrawDotsBorder());
  }

  /**
   * Test for {@link CompositeInfo#shouldDrawDotsBorder()}.
   */
  public void test_shouldDrawDotsBorder_noScript() throws Exception {
    prepareMyComponent(new String[]{}, new String[]{
        "// filler filler filler filler filler",
        "<parameters>",
        "  <parameter name='shouldDrawBorder'></parameter>",
        "</parameters>"});
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <t:MyComponent wbp:name='myComponent'/>",
        "</Shell>");
    refresh();
    // 
    CompositeInfo myComponent = getObjectByName("myComponent");
    assertFalse(myComponent.shouldDrawDotsBorder());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_clipboard_copyLayout() throws Exception {
    CompositeInfo shell =
        parse(
            "<Shell>",
            "  <Shell.layout>",
            "    <FillLayout/>",
            "  </Shell.layout>",
            "  <Composite wbp:name='composite'>",
            "    <Composite.layout>",
            "      <RowLayout fill='true'/>",
            "    </Composite.layout>",
            "  </Composite>",
            "</Shell>");
    refresh();
    CompositeInfo composite = getObjectByName("composite");
    //
    {
      XmlObjectMemento memento = XmlObjectMemento.createMemento(composite);
      CompositeInfo newComposite = (CompositeInfo) memento.create(shell);
      shell.getLayout().command_CREATE(newComposite, null);
      memento.apply();
    }
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <Composite wbp:name='composite'>",
        "    <Composite.layout>",
        "      <RowLayout fill='true'/>",
        "    </Composite.layout>",
        "  </Composite>",
        "  <Composite>",
        "    <Composite.layout>",
        "      <RowLayout fill='true'/>",
        "    </Composite.layout>",
        "  </Composite>",
        "</Shell>");
  }

  public void test_clipboard_implicitLayout() throws Exception {
    setFileContentSrc(
        "test/MyComposite.java",
        getSource(
            "package test;",
            "import org.eclipse.swt.widgets.Composite;",
            "import org.eclipse.swt.layout.RowLayout;",
            "public class MyComposite extends Composite {",
            "  public MyComposite(Composite parent, int style) {",
            "    super(parent, style);",
            "    setLayout(new RowLayout());",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    CompositeInfo shell =
        parse(
            "<Shell>",
            "  <Shell.layout>",
            "    <FillLayout/>",
            "  </Shell.layout>",
            "  <t:MyComposite wbp:name='composite'/>",
            "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <FillLayout>",
        "  <t:MyComposite wbp:name='composite'>",
        "    implicit-layout: org.eclipse.swt.layout.RowLayout");
    refresh();
    CompositeInfo composite = getObjectByName("composite");
    // paste
    {
      XmlObjectMemento memento = XmlObjectMemento.createMemento(composite);
      CompositeInfo newComposite = (CompositeInfo) memento.create(shell);
      shell.getLayout().command_CREATE(newComposite, null);
      memento.apply();
    }
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <t:MyComposite wbp:name='composite'/>",
        "  <t:MyComposite/>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <FillLayout>",
        "  <t:MyComposite wbp:name='composite'>",
        "    implicit-layout: org.eclipse.swt.layout.RowLayout",
        "  <t:MyComposite>",
        "    implicit-layout: org.eclipse.swt.layout.RowLayout");
  }
}