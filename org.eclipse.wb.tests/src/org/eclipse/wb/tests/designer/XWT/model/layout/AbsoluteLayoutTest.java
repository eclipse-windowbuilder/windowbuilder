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

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.layout.absolute.IPreferenceConstants;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.property.GenericProperty;
import org.eclipse.wb.internal.rcp.RcpToolkitDescription;
import org.eclipse.wb.internal.xwt.model.layout.AbsoluteLayoutInfo;
import org.eclipse.wb.internal.xwt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.IPreferenceStore;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link AbsoluteLayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class AbsoluteLayoutTest extends XwtModelTest {
  private static final IPreferenceStore preferences =
      RcpToolkitDescription.INSTANCE.getPreferences();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    preferences.setValue(IPreferenceConstants.P_CREATION_FLOW, false);
    preferences.setToDefault(IPreferenceConstants.P_AUTOSIZE_ON_PROPERTY_CHANGE);
  }

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
  public void test_noProperties() throws Exception {
    CompositeInfo composite = parse("<Shell/>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  implicit-layout: absolute");
    AbsoluteLayoutInfo layout = (AbsoluteLayoutInfo) composite.getLayout();
    assertThat(layout.getProperties()).isEmpty();
  }

  public void test_onRemove() throws Exception {
    CompositeInfo composite =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Button text='New Button' bounds='1, 2, 3, 4' location='1, 2' size='3, 4'/>",
            "</Shell>");
    refresh();
    // set new Layout
    LayoutInfo layout = createObject("org.eclipse.swt.layout.FillLayout");
    composite.setLayout(layout);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <Button text='New Button'/>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <FillLayout>",
        "  <Button text='New Button'>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Explicit
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_parseExplicit() throws Exception {
    setFileContentSrc(
        "test/MyComposite.java",
        getJavaSource(
            "public class MyComposite extends Composite {",
            "  public MyComposite(Composite parent, int style) {",
            "    super(parent, style);",
            "    setLayout(new RowLayout());",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    CompositeInfo composite = parse("<t:MyComposite layout='{x:Null}'/>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<t:MyComposite layout='{x:Null}'>",
        "  explicit-layout: absolute");
    refresh();
    LayoutInfo layout = composite.getLayout();
    // layout should be "null"
    assertSame(null, composite.getComposite().getLayout());
    assertThat(layout).isInstanceOf(AbsoluteLayoutInfo.class);
    // check CreationSupport
    {
      CreationSupport creationSupport = layout.getCreationSupport();
      assertEquals("explicit-layout: absolute", creationSupport.toString());
      assertEquals("explicit-layout: absolute", creationSupport.getTitle());
      assertEquals(composite.getElement(), creationSupport.getElement());
    }
    // delete
    assertTrue(layout.canDelete());
    layout.delete();
    assertXML("<t:MyComposite/>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<t:MyComposite>",
        "  implicit-layout: org.eclipse.swt.layout.RowLayout");
  }

  /**
   * Test for setting {@link AbsoluteLayoutInfo}.
   */
  public void test_setExplicit() throws Exception {
    setFileContentSrc(
        "test/MyComposite.java",
        getJavaSource(
            "public class MyComposite extends Composite {",
            "  public MyComposite(Composite parent, int style) {",
            "    super(parent, style);",
            "    setLayout(new RowLayout());",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    CompositeInfo composite =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<t:MyComposite>",
            "  <Button/>",
            "</t:MyComposite>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<t:MyComposite>",
        "  implicit-layout: org.eclipse.swt.layout.RowLayout",
        "  <Button>",
        "    virtual-LayoutData: org.eclipse.swt.layout.RowData");
    refresh();
    // set "null"
    {
      AbsoluteLayoutInfo layout = AbsoluteLayoutInfo.createExplicitModel(m_lastContext);
      composite.setLayout(layout);
    }
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<t:MyComposite layout='{x:Null}'>",
        "  <Button bounds='3, 3, 12, 25'/>",
        "</t:MyComposite>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<t:MyComposite layout='{x:Null}'>",
        "  explicit-layout: absolute",
        "  <Button bounds='3, 3, 12, 25'>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AbsoluteLayoutInfo#commandCreate(ControlInfo, ControlInfo)}.
   */
  public void test_commandCreate() throws Exception {
    CompositeInfo composite = parse("<Shell/>");
    refresh();
    AbsoluteLayoutInfo layout = (AbsoluteLayoutInfo) composite.getLayout();
    //
    ControlInfo button = createButton();
    layout.commandCreate(button, null);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button/>",
        "</Shell>");
  }

  /**
   * Test for {@link AbsoluteLayoutInfo#commandMove(ControlInfo, ControlInfo)}.
   */
  public void test_commandMove() throws Exception {
    CompositeInfo composite =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Button wbp:name='button_1'/>",
            "  <Button wbp:name='button_2'/>",
            "</Shell>");
    refresh();
    AbsoluteLayoutInfo layout = (AbsoluteLayoutInfo) composite.getLayout();
    ControlInfo button_1 = getObjectByName("button_1");
    ControlInfo button_2 = getObjectByName("button_2");
    //
    layout.commandMove(button_2, button_1);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button wbp:name='button_2'/>",
        "  <Button wbp:name='button_1'/>",
        "</Shell>");
  }

  /**
   * Test for {@link AbsoluteLayoutInfo#commandChangeBounds(ControlInfo, Point, Dimension)}.
   */
  public void test_commandChangeBounds_creationFlow() throws Exception {
    CompositeInfo composite =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Button wbp:name='button_1' bounds='100, 100, 50, 10'/>",
            "  <Button wbp:name='button_2' bounds='110, 150, 50, 10'/>",
            "</Shell>");
    refresh();
    AbsoluteLayoutInfo layout = (AbsoluteLayoutInfo) composite.getLayout();
    ControlInfo button_2 = getObjectByName("button_2");
    preferences.setValue(IPreferenceConstants.P_CREATION_FLOW, true);
    layout.commandChangeBounds(button_2, new Point(10, 10), null);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button wbp:name='button_2' bounds='10, 10, 50, 10'/>",
        "  <Button wbp:name='button_1' bounds='100, 100, 50, 10'/>",
        "</Shell>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // commandChangeBounds()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_commandChangeBounds_hasBounds_setLS_removeLS() throws Exception {
    String initial = " bounds='1, 2, 3, 4' location='1, 2' size='3, 4'";
    Point newLocation = new Point(10, 20);
    Dimension newSize = new Dimension(30, 40);
    String expected = " bounds='10, 20, 30, 40'";
    check_commandChangeBounds(initial, newLocation, newSize, expected);
  }

  public void test_commandChangeBounds_hasBounds_setL() throws Exception {
    String initial = " bounds='1, 2, 3, 4'";
    Point newLocation = new Point(10, 20);
    Dimension newSize = null;
    String expected = " bounds='10, 20, 3, 4'";
    check_commandChangeBounds(initial, newLocation, newSize, expected);
  }

  public void test_commandChangeBounds_noAttributes_setLS() throws Exception {
    String initial = "";
    Point newLocation = new Point(10, 20);
    Dimension newSize = new Dimension(30, 40);
    String expected = " bounds='10, 20, 30, 40'";
    check_commandChangeBounds(initial, newLocation, newSize, expected);
  }

  public void test_commandChangeBounds_noAttributes_setL() throws Exception {
    String initial = "";
    Point newLocation = new Point(10, 20);
    Dimension newSize = null;
    String expected = " location='10, 20'";
    check_commandChangeBounds(initial, newLocation, newSize, expected);
  }

  public void test_commandChangeBounds_noAttributes_setS() throws Exception {
    String initial = "";
    Point newLocation = null;
    Dimension newSize = new Dimension(30, 40);
    String expected = " size='30, 40'";
    check_commandChangeBounds(initial, newLocation, newSize, expected);
  }

  public void test_commandChangeBounds_hasL_setS() throws Exception {
    String initial = " location='1, 2'";
    Point newLocation = null;
    Dimension newSize = new Dimension(30, 40);
    String expected = " bounds='1, 2, 30, 40'";
    check_commandChangeBounds(initial, newLocation, newSize, expected);
  }

  public void test_commandChangeBounds_hasS_setL() throws Exception {
    String initial = " size='3, 4'";
    Point newLocation = new Point(10, 20);
    Dimension newSize = null;
    String expected = " bounds='10, 20, 3, 4'";
    check_commandChangeBounds(initial, newLocation, newSize, expected);
  }

  /**
   * Test for {@link AbsoluteLayoutInfo#commandChangeBounds(ControlInfo, Point, Dimension)}.
   */
  private void check_commandChangeBounds(String initialAttributes,
      Point newLocation,
      Dimension newSize,
      String expectedAttributes) throws Exception {
    String initialLine = "  <Button wbp:name='button'" + initialAttributes + "/>";
    CompositeInfo composite =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<Shell>",
            initialLine,
            "</Shell>");
    refresh();
    AbsoluteLayoutInfo layout = (AbsoluteLayoutInfo) composite.getLayout();
    ControlInfo button = getObjectByName("button");
    layout.commandChangeBounds(button, newLocation, newSize);
    String expectedLine = "  <Button wbp:name='button'" + expectedAttributes + "/>";
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        expectedLine,
        "</Shell>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Auto size
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Modify not "text" or "image" property, no size change.
   */
  public void test_autoSize_otherProperty() throws Exception {
    preferences.setValue(IPreferenceConstants.P_AUTOSIZE_ON_PROPERTY_CHANGE, true);
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button wbp:name='button' bounds='0, 0, 1, 2'/>",
        "</Shell>");
    refresh();
    ControlInfo button = getObjectByName("button");
    // set value
    Property enabledProperty = button.getPropertyByTitle("enabled");
    enabledProperty.setValue(Boolean.FALSE);
    waitEventLoop(1);
    // check
    Rectangle bounds = button.getModelBounds();
    assertThat(bounds.width).isEqualTo(1);
    assertThat(bounds.height).isEqualTo(2);
  }

  /**
   * Modify "text" property, so bigger size expected.
   */
  public void test_autoSize_textProperty() throws Exception {
    preferences.setValue(IPreferenceConstants.P_AUTOSIZE_ON_PROPERTY_CHANGE, true);
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button wbp:name='button' bounds='0, 0, 1, 2'/>",
        "</Shell>");
    refresh();
    ControlInfo button = getObjectByName("button");
    // set value
    Property textProperty = button.getPropertyByTitle("text");
    textProperty.setValue("Very long text");
    waitEventLoop(1);
    // check
    Rectangle bounds = button.getBounds();
    assertThat(bounds.width).isGreaterThan(50);
    assertThat(bounds.height).isGreaterThan(20);
  }

  /**
   * Modify "image" property, so bigger size expected.
   */
  public void test_autoSize_imageProperty() throws Exception {
    preferences.setValue(IPreferenceConstants.P_AUTOSIZE_ON_PROPERTY_CHANGE, true);
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button wbp:name='button' bounds='0, 0, 1, 2'/>",
        "</Shell>");
    refresh();
    ControlInfo button = getObjectByName("button");
    // set value
    GenericProperty imageProperty = (GenericProperty) button.getPropertyByTitle("image");
    imageProperty.setExpression("foo.png", null);
    waitEventLoop(1);
    // check
    Rectangle bounds = button.getBounds();
    assertThat(bounds.width).isGreaterThan(10);
    assertThat(bounds.height).isGreaterThan(20);
  }

  /**
   * Modify "text" property, but {@link IPreferenceConstants#P_AUTOSIZE_ON_PROPERTY_CHANGE} is not
   * enabled, so no auto-size
   */
  public void test_autoSize_textProperty_notEnabled() throws Exception {
    preferences.setValue(IPreferenceConstants.P_AUTOSIZE_ON_PROPERTY_CHANGE, false);
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button wbp:name='button' bounds='0, 0, 1, 2'/>",
        "</Shell>");
    refresh();
    ControlInfo button = getObjectByName("button");
    // set value
    Property textProperty = button.getPropertyByTitle("text");
    textProperty.setValue("Very long text");
    waitEventLoop(1);
    // check
    Rectangle bounds = button.getBounds();
    assertThat(bounds.width).isEqualTo(1);
    assertThat(bounds.height).isEqualTo(2);
  }

  /**
   * Test for <code>Autosize control</code> action.
   */
  public void test_autoSizeAction() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button wbp:name='button' text='New' bounds='0, 0, 1, 2'/>",
        "</Shell>");
    refresh();
    ControlInfo button = getObjectByName("button");
    Dimension buttonPrefSize = button.getPreferredSize();
    // prepare action
    IAction autoSizeAction;
    {
      IMenuManager manager = getContextMenu(button);
      autoSizeAction = findChildAction(manager, "Autosize control");
      assertNotNull(autoSizeAction);
    }
    // perform auto-size
    autoSizeAction.run();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button wbp:name='button' text='New' bounds='0, 0, "
            + buttonPrefSize.width
            + ", "
            + buttonPrefSize.height
            + "'/>",
        "</Shell>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Bounds" property
  //
  ////////////////////////////////////////////////////////////////////////////
  private ControlInfo m_propertyBoundsControl;
  private Property m_propertyBounds;

  public void test_propertyBounds_get() throws Exception {
    prepareBoundsProperty();
    assertEquals("(1, 2, 3, 4)", getPropertyText(m_propertyBounds));
  }

  public void test_propertyBounds_setX() throws Exception {
    check_propertyBoundsPart("x", 10, "(10, 2, 3, 4)");
  }

  public void test_propertyBounds_setY() throws Exception {
    check_propertyBoundsPart("y", 20, "(1, 20, 3, 4)");
  }

  public void test_propertyBounds_setWidth() throws Exception {
    check_propertyBoundsPart("width", 30, "(1, 2, 30, 4)");
  }

  public void test_propertyBounds_setHeight() throws Exception {
    check_propertyBoundsPart("height", 40, "(1, 2, 3, 40)");
  }

  private void prepareBoundsProperty() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button wbp:name='button' bounds='1, 2, 3, 4'/>",
        "</Shell>");
    refresh();
    m_propertyBoundsControl = getObjectByName("button");
    //
    m_propertyBounds = PropertyUtils.getByPath(m_propertyBoundsControl, "Bounds");
  }

  private void check_propertyBoundsPart(String partName, int value, String expected)
      throws Exception {
    prepareBoundsProperty();
    // set part value
    {
      Property partProperty =
          PropertyUtils.getByPath(m_propertyBoundsControl, "Bounds/" + partName);
      partProperty.setValue(value);
    }
    // get "Bounds" again
    m_propertyBounds = PropertyUtils.getByPath(m_propertyBoundsControl, "Bounds");
    assertEquals(expected, getPropertyText(m_propertyBounds));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_clipboard() throws Exception {
    final CompositeInfo shell =
        parse(
            "<Shell>",
            "  <Shell.layout>",
            "    <FillLayout/>",
            "  </Shell.layout>",
            "  <Composite wbp:name='composite'>",
            "    <Button text='Button 1' bounds='1, 2, 3, 4'/>",
            "    <Button text='Button 2' bounds='10, 20, 30, 40'/>",
            "  </Composite>",
            "</Shell>");
    refresh();
    // do copy/paste
    {
      CompositeInfo composite = getObjectByName("composite");
      doCopyPaste(composite, new PasteProcedure<ControlInfo>() {
        public void run(ControlInfo copy) throws Exception {
          shell.getLayout().command_CREATE(copy, null);
        }
      });
    }
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <Composite wbp:name='composite'>",
        "    <Button text='Button 1' bounds='1, 2, 3, 4'/>",
        "    <Button text='Button 2' bounds='10, 20, 30, 40'/>",
        "  </Composite>",
        "  <Composite>",
        "    <Button bounds='1, 2, 3, 4' text='Button 1'/>",
        "    <Button bounds='10, 20, 30, 40' text='Button 2'/>",
        "  </Composite>",
        "</Shell>");
  }
}