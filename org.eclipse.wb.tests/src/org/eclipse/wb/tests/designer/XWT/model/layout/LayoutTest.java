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
package org.eclipse.wb.tests.designer.XWT.model.layout;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.presentation.PropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.xwt.IExceptionConstants;
import org.eclipse.wb.internal.xwt.model.layout.AbsoluteLayoutInfo;
import org.eclipse.wb.internal.xwt.model.layout.ImplicitLayoutCreationSupport;
import org.eclipse.wb.internal.xwt.model.layout.LayoutDataInfo;
import org.eclipse.wb.internal.xwt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.xwt.model.layout.VirtualLayoutDataCreationSupport;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang.ArrayUtils;

import java.util.Collection;
import java.util.List;

/**
 * Test for {@link LayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class LayoutTest extends XwtModelTest {
  private static final String[] ESA = ArrayUtils.EMPTY_STRING_ARRAY;

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
  /**
   * When {@link CompositeInfo} has explicit {@link LayoutInfo}, we should not have implicit
   * {@link LayoutInfo} in children.
   */
  public void test_hasExplicitLayout() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <FillLayout>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * We don't show {@link LayoutInfo} in tree or on canvas.
   */
  public void test_presentation_FillLayout() throws Exception {
    CompositeInfo composite =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <FillLayout/>",
            "  </Shell.layout>",
            "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <FillLayout>");
    LayoutInfo layout = composite.getLayout();
    // not visible
    assertVisible(layout, false);
  }

  /**
   * We don't show {@link LayoutInfo} in tree or on canvas.
   */
  public void test_presentation_AbsoluteLayout() throws Exception {
    CompositeInfo composite = parse("<Shell/>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  implicit-layout: absolute");
    LayoutInfo layout = composite.getLayout();
    // not visible
    assertVisible(layout, false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implicit
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_noImplicitLayout() throws Exception {
    prepareMyComposite(ESA, ESA, new String[]{
        "<parameters>",
        "  <parameter name='layout.has'>false</parameter>",
        "</parameters>"});
    // parse
    CompositeInfo composite = parse("<t:MyComposite/>");
    assertHierarchy("<t:MyComposite>");
    // no layout
    assertFalse(composite.hasLayout());
    try {
      composite.getLayout();
    } catch (DesignerException de) {
      assertEquals(IExceptionConstants.NO_LAYOUT_EXPECTED, de.getCode());
    }
  }

  /**
   * Test for implicit {@link FillLayout} and {@link ImplicitLayoutCreationSupport}.
   */
  public void test_implicit_FillLayout() throws Exception {
    prepareMyComposite(new String[]{
        "// filler filler filler filler filler",
        "setLayout(new FillLayout());"}, ESA, ESA);
    // parse
    CompositeInfo composite = parse("<t:MyComposite/>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "<t:MyComposite>",
        "  implicit-layout: org.eclipse.swt.layout.FillLayout");
    refresh();
    // has implicit layout
    assertTrue(composite.hasLayout());
    LayoutInfo layout = composite.getLayout();
    assertNotNull(layout);
    assertEquals(
        "org.eclipse.swt.layout.FillLayout",
        layout.getDescription().getComponentClass().getName());
    // implicit layout has Object
    assertNotNull(layout.getObject());
    // ImplicitLayoutCreationSupport
    {
      ImplicitLayoutCreationSupport creationSupport =
          (ImplicitLayoutCreationSupport) layout.getCreationSupport();
      assertSame(composite.getCreationSupport().getElement(), creationSupport.getElement());
      assertEquals("implicit-layout: org.eclipse.swt.layout.FillLayout", creationSupport.toString());
      assertEquals("implicit-layout: org.eclipse.swt.layout.FillLayout", creationSupport.getTitle());
      assertTrue(creationSupport.canDelete());
      // delete() ignored
      layout.delete();
      assertXML("<t:MyComposite/>");
      assertHierarchy(
          "// filler filler filler filler filler",
          "<t:MyComposite>",
          "  implicit-layout: org.eclipse.swt.layout.FillLayout");
    }
    // simulate Layout_Info absence
    ((Collection<?>) ReflectionUtils.getFieldObject(composite, "m_children")).clear();
    try {
      composite.getLayout();
      fail();
    } catch (IllegalStateException e) {
    }
  }

  /**
   * Test for implicit {@link AbsoluteLayoutInfo}.
   */
  public void test_implicit_absolute() throws Exception {
    CompositeInfo composite = parse("<Shell/>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  implicit-layout: absolute");
    // has implicit layout
    assertTrue(composite.hasLayout());
    LayoutInfo layout = composite.getLayout();
    assertNotNull(layout);
    assertSame(null, layout.getDescription().getComponentClass());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // delete()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for deleting {@link LayoutInfo}.
   * <p>
   * Also test for {@link LayoutInfo#isActive()}.
   */
  public void test_delete_isActive() throws Exception {
    CompositeInfo shell =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <FillLayout/>",
            "  </Shell.layout>",
            "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <FillLayout>");
    LayoutInfo layout = shell.getLayout();
    //
    assertTrue(layout.isActive());
    // delete
    layout.delete();
    assertFalse(layout.isActive());
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell/>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  implicit-layout: absolute");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setLayout()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link CompositeInfo#setLayout(LayoutInfo)}.
   */
  public void test_setLayout_replaceImplicit() throws Exception {
    CompositeInfo shell = parse("<Shell/>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  implicit-layout: absolute");
    // set new Layout
    LayoutInfo layout = createObject("org.eclipse.swt.layout.FillLayout");
    shell.setLayout(layout);
    // source
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <FillLayout>");
    // object
    shell.refresh();
    assertInstanceOf("org.eclipse.swt.layout.FillLayout", shell.getComposite().getLayout());
  }

  /**
   * Test for {@link CompositeInfo#setLayout(LayoutInfo)}.
   */
  public void test_setLayout_replaceExplicit() throws Exception {
    CompositeInfo shell =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <FillLayout/>",
            "  </Shell.layout>",
            "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <FillLayout>");
    // set new Layout
    LayoutInfo layout = createObject("org.eclipse.swt.layout.FillLayout");
    shell.setLayout(layout);
    // source
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <FillLayout>");
    // object
    shell.refresh();
    assertInstanceOf("org.eclipse.swt.layout.FillLayout", shell.getComposite().getLayout());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link LayoutInfo#command_CREATE(ControlInfo, ControlInfo)}.
   */
  public void test_CREATE() throws Exception {
    CompositeInfo composite =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <FillLayout/>",
            "  </Shell.layout>",
            "  <Button wbp:name='existing'/>",
            "</Shell>");
    //
    ControlInfo newButton = createButtonWithText();
    composite.getLayout().command_CREATE(newButton, null);
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='existing'/>",
        "  <Button text='New Button'/>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <FillLayout>",
        "  <Button wbp:name='existing'>",
        "  <Button text='New Button'>");
  }

  /**
   * Test for {@link LayoutInfo#command_CREATE(ControlInfo, ControlInfo)}.
   */
  public void test_CREATE_withNext() throws Exception {
    CompositeInfo composite =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <FillLayout/>",
            "  </Shell.layout>",
            "  <Button wbp:name='refButton'/>",
            "</Shell>");
    ControlInfo refButton = getObjectByName("refButton");
    //
    ControlInfo newButton = createButtonWithText();
    composite.getLayout().command_CREATE(newButton, refButton);
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <Button text='New Button'/>",
        "  <Button wbp:name='refButton'/>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <FillLayout>",
        "  <Button text='New Button'>",
        "  <Button wbp:name='refButton'>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link LayoutInfo#command_MOVE(ControlInfo, ControlInfo)}.
   */
  public void test_MOVE_last() throws Exception {
    CompositeInfo composite =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <FillLayout/>",
            "  </Shell.layout>",
            "  <Button wbp:name='buttonA'/>",
            "  <Button wbp:name='button'/>",
            "  <Button wbp:name='buttonB'/>",
            "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <FillLayout>",
        "  <Button wbp:name='buttonA'>",
        "  <Button wbp:name='button'>",
        "  <Button wbp:name='buttonB'>");
    ControlInfo button = getObjectByName("button");
    //
    composite.getLayout().command_MOVE(button, null);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='buttonA'/>",
        "  <Button wbp:name='buttonB'/>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    assertHierarchy(
        "<Shell>",
        "  <FillLayout>",
        "  <Button wbp:name='buttonA'>",
        "  <Button wbp:name='buttonB'>",
        "  <Button wbp:name='button'>");
  }

  /**
   * Test for {@link LayoutInfo#command_MOVE(ControlInfo, ControlInfo)}.
   */
  public void test_MOVE_withNext() throws Exception {
    CompositeInfo composite =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <FillLayout/>",
            "  </Shell.layout>",
            "  <Button wbp:name='buttonA'/>",
            "  <Button wbp:name='button'/>",
            "  <Button wbp:name='buttonB'/>",
            "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <FillLayout>",
        "  <Button wbp:name='buttonA'>",
        "  <Button wbp:name='button'>",
        "  <Button wbp:name='buttonB'>");
    ControlInfo button = getObjectByName("button");
    ControlInfo buttonA = getObjectByName("buttonA");
    //
    composite.getLayout().command_MOVE(button, buttonA);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'/>",
        "  <Button wbp:name='buttonA'/>",
        "  <Button wbp:name='buttonB'/>",
        "</Shell>");
    assertHierarchy(
        "<Shell>",
        "  <FillLayout>",
        "  <Button wbp:name='button'>",
        "  <Button wbp:name='buttonA'>",
        "  <Button wbp:name='buttonB'>");
  }

  /**
   * Test for {@link LayoutInfo#command_MOVE(ControlInfo, ControlInfo)}.
   */
  public void test_MOVE_reparent() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <Composite wbp:name='target'>",
        "    <Composite.layout>",
        "      <FillLayout/>",
        "    </Composite.layout>",
        "  </Composite>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <FillLayout>",
        "  <Composite wbp:name='target'>",
        "    <FillLayout>",
        "  <Button wbp:name='button'>");
    ControlInfo button = getObjectByName("button");
    CompositeInfo target = getObjectByName("target");
    //
    target.getLayout().command_MOVE(button, null);
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <Composite wbp:name='target'>",
        "    <Composite.layout>",
        "      <FillLayout/>",
        "    </Composite.layout>",
        "    <Button wbp:name='button'/>",
        "  </Composite>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <FillLayout>",
        "  <Composite wbp:name='target'>",
        "    <FillLayout>",
        "    <Button wbp:name='button'>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Layout" property
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for "Layout" complex property.
   */
  public void test_Layout_complexProperty() throws Exception {
    CompositeInfo composite =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <RowLayout/>",
            "  </Shell.layout>",
            "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <RowLayout>");
    refresh();
    //
    Property layoutProperty = composite.getPropertyByTitle("Layout");
    assertNotNull(layoutProperty);
    assertEquals("(org.eclipse.swt.layout.RowLayout)", getPropertyText(layoutProperty));
    assertTrue(layoutProperty.isModified());
    // sub-properties
    Property[] subProperties = PropertyUtils.getChildren(layoutProperty);
    String[] subTitles = PropertyUtils.getTitles(subProperties);
    assertThat(subTitles).contains("Class", "center", "fill", "spacing", "marginLeft");
    // delete
    layoutProperty.setValue(Property.UNKNOWN_VALUE);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell/>");
  }

  /**
   * Test for "Layout" complex property.
   */
  public void test_Layout_complexProperty_absolute() throws Exception {
    CompositeInfo composite = parse("<Shell/>");
    refresh();
    //
    Property layoutProperty = composite.getPropertyByTitle("Layout");
    assertNotNull(layoutProperty);
    assertEquals("(absolute)", getPropertyText(layoutProperty));
    // sub-properties
    Property[] subProperties = PropertyUtils.getChildren(layoutProperty);
    assertThat(subProperties).isEmpty();
  }

  /**
   * Test for drop-down menu of "Layout" complex property.
   */
  public void test_Layout_complexProperty_dropDown() throws Exception {
    CompositeInfo composite = parse("<Shell/>");
    refresh();
    //
    Property layoutProperty = composite.getPropertyByTitle("Layout");
    PropertyEditorPresentation presentation =
        (PropertyEditorPresentation) ReflectionUtils.getFieldObject(
            layoutProperty.getEditor(),
            "m_presentation");
    assertNotNull(presentation);
    assertNotNull(ReflectionUtils.invokeMethod(presentation, "getImage()"));
    // show drop-down
    Shell shell = new Shell();
    PropertyTable propertyTable = new PropertyTable(shell, SWT.NONE);
    try {
      ReflectionUtils.invokeMethod(
          presentation,
          "onClick(org.eclipse.wb.internal.core.model.property.table.PropertyTable,"
              + "org.eclipse.wb.internal.core.model.property.Property)",
          propertyTable,
          layoutProperty);
      //
      UiContext context = new UiContext();
      Menu dropDown = context.getLastPopup();
      try {
        assertNotNull(dropDown);
        dropDown.notifyListeners(SWT.Show, null);
        {
          List<String> itemTexts = Lists.newArrayList();
          for (MenuItem menuItem : dropDown.getItems()) {
            itemTexts.add(menuItem.getText());
          }
          assertThat(itemTexts).contains("FillLayout", "RowLayout", "GridLayout");
        }
      } finally {
        dropDown.setVisible(false);
      }
    } finally {
      shell.dispose();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // LayoutData
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that we can parse "real" {@link LayoutDataInfo} and bind it to {@link ControlInfo}.
   */
  public void test_LayoutData_parse() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'>",
        "    <Button.layoutData>",
        "      <RowData width='100'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <RowLayout>",
        "  <Button wbp:name='button'>",
        "    <RowData width='100'>");
    refresh();
    //
    ControlInfo button = getObjectByName("button");
    LayoutDataInfo layoutData = LayoutInfo.getLayoutData(button);
    assertNotNull(layoutData);
    assertEquals(100, layoutData.getPropertyByTitle("width").getValue());
    // not visible
    assertVisible(layoutData, false);
  }

  /**
   * Test for "LayoutData" complex property.
   */
  public void test_LayoutData_complexProperty() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'>",
        "    <Button.layoutData>",
        "      <RowData width='100'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <RowLayout>",
        "  <Button wbp:name='button'>",
        "    <RowData width='100'>");
    refresh();
    //
    ControlInfo button = getObjectByName("button");
    // "LayoutData" property
    {
      Property dataProperty = button.getPropertyByTitle("LayoutData");
      assertNotNull(dataProperty);
      assertEquals("(org.eclipse.swt.layout.RowData)", getPropertyText(dataProperty));
      assertTrue(dataProperty.isModified());
      // sub-properties
      Property[] subProperties = PropertyUtils.getChildren(dataProperty);
      String[] subTitles = PropertyUtils.getTitles(subProperties);
      assertThat(subTitles).contains("Class", "exclude", "width", "height");
      // delete
      dataProperty.setValue(Property.UNKNOWN_VALUE);
      assertXML(
          "// filler filler filler filler filler",
          "<Shell>",
          "  <Shell.layout>",
          "    <RowLayout/>",
          "  </Shell.layout>",
          "  <Button wbp:name='button'/>",
          "</Shell>");
    }
    // still has "LayoutData" property, but it is virtual, so not modified
    {
      Property dataProperty = button.getPropertyByTitle("LayoutData");
      assertFalse(dataProperty.isModified());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // LayoutData: virtual and materialize
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that after parsing {@link ControlInfo} has virtual {@link LayoutDataInfo}.
   */
  public void test_LayoutData_virtual() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <RowLayout>",
        "  <Button wbp:name='button'>",
        "    virtual-LayoutData: org.eclipse.swt.layout.RowData");
    refresh();
    //
    ControlInfo button = getObjectByName("button");
    {
      LayoutDataInfo layoutData = LayoutInfo.getLayoutData(button);
      assertNotNull(layoutData);
      assertEquals(SWT.DEFAULT, layoutData.getPropertyByTitle("width").getValue());
      // CreationSupport
      VirtualLayoutDataCreationSupport creationSupport =
          (VirtualLayoutDataCreationSupport) layoutData.getCreationSupport();
      assertEquals("virtual-LayoutData: org.eclipse.swt.layout.RowData", creationSupport.toString());
      assertEquals("virtual-LayoutData: org.eclipse.swt.layout.RowData", creationSupport.getTitle());
    }
  }

  /**
   * Test for materializing virtual {@link LayoutDataInfo}.
   */
  public void test_LayoutData_materialize() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <RowLayout>",
        "  <Button wbp:name='button'>",
        "    virtual-LayoutData: org.eclipse.swt.layout.RowData");
    refresh();
    //
    ControlInfo button = getObjectByName("button");
    LayoutDataInfo layoutData = LayoutInfo.getLayoutData(button);
    layoutData.getPropertyByTitle("width").setValue(100);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'>",
        "    <Button.layoutData>",
        "      <RowData width='100'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <RowLayout>",
        "  <Button wbp:name='button'>",
        "    <RowData width='100'>");
  }

  public void test_LayoutData_createVirtual_whenDeleteReal() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'>",
        "    <Button.layoutData>",
        "      <RowData width='100'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <RowLayout>",
        "  <Button wbp:name='button'>",
        "    <RowData width='100'>");
    refresh();
    //
    ControlInfo button = getObjectByName("button");
    LayoutDataInfo layoutData = LayoutInfo.getLayoutData(button);
    layoutData.delete();
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <RowLayout>",
        "  <Button wbp:name='button'>",
        "    virtual-LayoutData: org.eclipse.swt.layout.RowData");
  }

  public void test_LayoutData_createVirtual_whenAddControl() throws Exception {
    CompositeInfo composite =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <RowLayout/>",
            "  </Shell.layout>",
            "</Shell>");
    //
    ControlInfo newButton = createButtonWithText();
    composite.getLayout().command_CREATE(newButton, null);
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <Button text='New Button'/>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <RowLayout>",
        "  <Button text='New Button'>",
        "    virtual-LayoutData: org.eclipse.swt.layout.RowData");
  }

  public void test_LayoutData_createVirtual_whenSetLayout() throws Exception {
    CompositeInfo composite =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <FillLayout/>",
            "  </Shell.layout>",
            "  <Button wbp:name='button'/>",
            "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <FillLayout>",
        "  <Button wbp:name='button'>");
    // set new Layout
    LayoutInfo layout = createObject("org.eclipse.swt.layout.RowLayout");
    composite.setLayout(layout);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <RowLayout>",
        "  <Button wbp:name='button'>",
        "    virtual-LayoutData: org.eclipse.swt.layout.RowData");
  }

  public void test_LayoutData_deleteIt_whenNoAttributes() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'>",
        "    <Button.layoutData>",
        "      <RowData width='100'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
    // remove "width" attribute
    ExecutionUtils.run(m_lastObject, new RunnableEx() {
      public void run() throws Exception {
        ControlInfo button = getObjectByName("button");
        XmlObjectInfo rowData = LayoutInfo.getLayoutData(button);
        rowData.removeAttribute("width");
      }
    });
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <RowLayout>",
        "  <Button wbp:name='button'>",
        "    virtual-LayoutData: org.eclipse.swt.layout.RowData");
  }

  public void test_LayoutData_deleteIt_hasChildElement_dontDelete() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FormLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'>",
        "    <Button.layoutData>",
        "      <FormData width='100'>",
        "        <FormData.left>",
        "          <FormAttachment offset='50'/>",
        "        </FormData.left>",
        "      </FormData>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
    // remove "width" attribute, but there is "left" sub-element, so FormData should not be deleted
    ExecutionUtils.run(m_lastObject, new RunnableEx() {
      public void run() throws Exception {
        ControlInfo button = getObjectByName("button");
        XmlObjectInfo rowData = LayoutInfo.getLayoutData(button);
        rowData.removeAttribute("width");
      }
    });
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FormLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'>",
        "    <Button.layoutData>",
        "      <FormData>",
        "        <FormData.left>",
        "          <FormAttachment offset='50'/>",
        "        </FormData.left>",
        "      </FormData>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
  }

  public void test_LayoutData_deleteIt_whenReparentControl() throws Exception {
    CompositeInfo composite =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <FillLayout/>",
            "  </Shell.layout>",
            "  <Composite wbp:name='target'>",
            "    <Composite.layout>",
            "      <RowLayout/>",
            "    </Composite.layout>",
            "    <Button wbp:name='button'/>",
            "  </Composite>",
            "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <FillLayout>",
        "  <Composite wbp:name='target'>",
        "    <RowLayout>",
        "    <Button wbp:name='button'>",
        "      virtual-LayoutData: org.eclipse.swt.layout.RowData");
    ControlInfo button = getObjectByName("button");
    //
    composite.getLayout().command_MOVE(button, null);
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <Composite wbp:name='target'>",
        "    <Composite.layout>",
        "      <RowLayout/>",
        "    </Composite.layout>",
        "  </Composite>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <FillLayout>",
        "  <Composite wbp:name='target'>",
        "    <RowLayout>",
        "  <Button wbp:name='button'>");
  }

  public void test_LayoutData_deleteThem_whenReplaceLayout() throws Exception {
    CompositeInfo composite =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <RowLayout/>",
            "  </Shell.layout>",
            "  <Button wbp:name='withExplicit'>",
            "    <Button.layoutData>",
            "      <RowData width='100'/>",
            "    </Button.layoutData>",
            "  </Button>",
            "  <Button wbp:name='withVirtual'/>",
            "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <RowLayout>",
        "  <Button wbp:name='withExplicit'>",
        "    <RowData width='100'>",
        "  <Button wbp:name='withVirtual'>",
        "    virtual-LayoutData: org.eclipse.swt.layout.RowData");
    // set new Layout
    LayoutInfo layout = createObject("org.eclipse.swt.layout.FillLayout");
    composite.setLayout(layout);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='withExplicit'/>",
        "  <Button wbp:name='withVirtual'/>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <FillLayout>",
        "  <Button wbp:name='withExplicit'>",
        "  <Button wbp:name='withVirtual'>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // test.MyComposite support
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Prepares empty <code>test.MyComposite</code> class with additional lines in constructor and
   * body, and with special <code>wbp-component.xml</code> description.
   */
  protected final void prepareMyComposite(String[] constructorLines,
      String[] bodyLines,
      String[] descriptionLines) throws Exception {
    // java
    {
      String[] lines =
          new String[]{
              "package test;",
              "import org.eclipse.swt.SWT;",
              "import org.eclipse.swt.widgets.*;",
              "import org.eclipse.swt.layout.*;",
              "public class MyComposite extends Composite {",
              "  public MyComposite(Composite parent, int style) {",
              "    super(parent, style);"};
      lines = CodeUtils.join(lines, constructorLines);
      lines = CodeUtils.join(lines, new String[]{"  }"});
      lines = CodeUtils.join(lines, bodyLines);
      lines = CodeUtils.join(lines, new String[]{"}"});
      setFileContentSrc("test/MyComposite.java", getSourceDQ(lines));
    }
    // description
    {
      String[] lines =
          new String[]{
              "<?xml version='1.0' encoding='UTF-8'?>",
              "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>"};
      lines = CodeUtils.join(lines, descriptionLines);
      lines = CodeUtils.join(lines, new String[]{"</component>"});
      setFileContentSrc("test/MyComposite.wbp-component.xml", getSourceDQ(lines));
    }
    waitForAutoBuild();
  }
}