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
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.core.xml.IExceptionConstants;
import org.eclipse.wb.internal.xwt.model.property.editor.style.StylePropertyEditor;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for basic parsing and {@link ControlInfo}.
 * 
 * @author scheglov_ke
 */
public class ControlTest extends XwtModelTest {
  private static final int SHELL_BORDER = 8;
  private static final int SHELL_HEADER = 30;
  public static final int BUTTON_ON_ROW_WIDTH = 68;
  public static final int BUTTON_ON_ROW_HEIGHT = 25;

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
  public void test_parse() throws Exception {
    ControlInfo shell =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<Shell text='Hello!'>",
            "  <Shell.layout>",
            "    <RowLayout/>",
            "  </Shell.layout>",
            "  <Button wbp:name='button' text='My button'/>",
            "</Shell>");
    shell.refresh();
    // shell
    {
      // has Image
      {
        Image image = shell.getImage();
        assertNotNull(image);
        assertEquals(450, image.getBounds().width);
        assertEquals(300, image.getBounds().height);
      }
      // has "model" bounds
      {
        Rectangle bounds = shell.getModelBounds();
        assertEquals(450, bounds.width);
        assertEquals(300, bounds.height);
      }
      // has "parent" bounds
      {
        Rectangle bounds = shell.getBounds();
        assertEquals(450, bounds.width);
        assertEquals(300, bounds.height);
      }
      // has "absolute" bounds
      {
        Rectangle bounds = shell.getAbsoluteBounds();
        assertEquals(450, bounds.width);
        assertEquals(300, bounds.height);
      }
      // client area insets
      {
        Insets expected = new Insets(SHELL_HEADER, SHELL_BORDER, SHELL_BORDER, SHELL_BORDER);
        assertEquals(expected, shell.getClientAreaInsets());
      }
    }
    // button
    {
      ControlInfo button = getObjectByName("button");
      int expectedWidth = BUTTON_ON_ROW_WIDTH;
      int expectedHeight = BUTTON_ON_ROW_HEIGHT;
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
        assertEquals(new Rectangle(SHELL_BORDER + 3,
            SHELL_HEADER + 3,
            expectedWidth,
            expectedHeight), bounds);
      }
      // has "absolute" bounds
      {
        Rectangle bounds = button.getAbsoluteBounds();
        assertEquals(new Rectangle(SHELL_BORDER + 3,
            SHELL_HEADER + 3,
            expectedWidth,
            expectedHeight), bounds);
      }
    }
  }

  /**
   * Test that {@link ControlInfo} has "Style" property.
   */
  public void test_hasStyleProperty() throws Exception {
    ControlInfo shell = parse("<Shell/>");
    refresh();
    // has "Style" property
    Property property = shell.getPropertyByTitle("Style");
    assertNotNull(property);
    assertThat(property.getEditor()).isInstanceOf(StylePropertyEditor.class);
    assertEquals(true, property.getCategory().isSystem());
    // no default value
    assertEquals(0, property.getValue());
  }

  /**
   * Test that custom component can be rendered as "root".
   */
  public void test_renderMyComponent() throws Exception {
    prepareMyComponent("public int myValue = 123;");
    ControlInfo model = parse("<t:MyComponent/>");
    refresh();
    // it should be really MyComponent, with default property value
    assertEquals("test.MyComponent", model.getObject().getClass().getName());
    assertEquals(123, model.getPropertyByTitle("myValue").getValue());
  }

  /**
   * Support for creating {@link Control} which is described in XWT file.
   * <p>
   * May be use normal {@link Composite} if XWT will support this.
   * <p>
   * http://www.eclipse.org/forums/index.php?t=msg&th=167978&start=0&
   */
  public void test_parse_nestedXWT() throws Exception {
    setFileContentSrc(
        "test/MyComposite.java",
        getSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "package test;",
            "public class MyComposite {",
            "}"));
    setFileContentSrc(
        "test/MyComposite.xwt",
        getTestSource(
            "// filler filler filler filler filler",
            "<Composite enabled='false'>",
            "  <Composite.layout>",
            "    <FillLayout/>",
            "  </Composite.layout>",
            "  <Button/>",
            "</Composite>"));
    waitForAutoBuild();
    // parse
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <t:MyComposite wbp:name='composite'/>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <RowLayout>",
        "  <t:MyComposite wbp:name='composite'>",
        "    implicit-layout: org.eclipse.swt.layout.FillLayout",
        "    virtual-LayoutData: org.eclipse.swt.layout.RowData");
    // check that "enabled" applied
    refresh();
    CompositeInfo composite = getObjectByName("composite");
    assertFalse(composite.getControl().isEnabled());
  }

  /**
   * Some stupid user attempts to use XWT in not Java project. This is bad by itself, because there
   * are no classpath, types, etc. But would be good to show good warning instead of just failure.
   */
  @DisposeProjectAfter
  public void test_parse_notJavaProject() throws Exception {
    ProjectUtils.removeNature(m_project, JavaCore.NATURE_ID);
    // parse
    try {
      parse("<Shell/>");
      fail();
    } catch (DesignerException e) {
      assertEquals(IExceptionConstants.NOT_JAVA_PROJECT, e.getCode());
    }
  }
}