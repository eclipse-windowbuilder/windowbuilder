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
package org.eclipse.wb.tests.designer.swt.model.layouts;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfoUtils;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.core.model.broadcast.JavaInfoSetObjectAfter;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.layout.absolute.IPreferenceConstants;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.rcp.RcpToolkitDescription;
import org.eclipse.wb.internal.rcp.ToolkitProvider;
import org.eclipse.wb.internal.swt.model.layout.FillLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.absolute.AbsoluteLayoutCreationSupport;
import org.eclipse.wb.internal.swt.model.layout.absolute.AbsoluteLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.BTestUtils;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.IPreferenceStore;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;

/**
 * Tests for {@link AbsoluteLayoutInfo}.
 * 
 * @author mitin_aa
 */
public class AbsoluteLayoutTest extends RcpModelTest {
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
  // Test for bugs found
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * The sample below installs wrong toolkit for AbsoluteLayoutInfo. The sequence is:
   * 
   * <pre>
	 *   refresh_beforeCreate sets active JavaInfo for EditorState to CompositeInfo;
	 *   parser found "new java.util.ArrayList()", creates DefaultJavaInfo and sets it as active JavaInfo;
	 *   constructor of AbsoluteLayoutInfo called, which uses active JavaInfo to set toolkit;
	 *   because active JavaInfo is DefaultJavaInfo, then we've end up with invalid toolkit.
	 * </pre>
   */
  public void test_invalidAbsoluteLayoutToolkit() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test {",
            "  /**",
            "  * @wbp.nonvisual location=571,523",
            "  */",
            "  private static final Object myStaticBean = new java.util.ArrayList<Object>();",
            "  protected Shell shell;",
            "  public static void main(String[] args) {",
            "    Test window = new Test();",
            "    window.open();",
            "  }",
            "  public void open() {",
            "    shell = new Shell(SWT.NONE);",
            "    Button button_1 = new Button(shell, SWT.NONE);",
            "    button_1.setBounds(42, 118, 81, 20);",
            "  }",
            "}");
    shell.refresh();
    AbsoluteLayoutInfo layout = (AbsoluteLayoutInfo) shell.getLayout();
    assertSame(ToolkitProvider.DESCRIPTION, layout.getDescription().getToolkit());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Layout
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_parse() throws Exception {
    CompositeInfo shellInfo =
        parseComposite(
            "class Test {",
            "  public static void main(String[] args) {",
            "    Shell shell = new Shell();",
            "    shell.setSize(320, 240);",
            "    shell.setLayout(null);",
            "  }",
            "}");
    // prepare layout
    AbsoluteLayoutInfo layout = (AbsoluteLayoutInfo) shellInfo.getLayout();
    {
      ComponentDescription description = layout.getDescription();
      assertSame(ToolkitProvider.DESCRIPTION, description.getToolkit());
      assertNotNull(description.getIcon());
    }
    // check CreationSupport
    {
      CreationSupport creationSupport = layout.getCreationSupport();
      assertEquals("null", creationSupport.toString());
      {
        MethodInvocation setLayoutInvocation =
            shellInfo.getMethodInvocation("setLayout(org.eclipse.swt.widgets.Layout)");
        assertEquals(DomGenerics.arguments(setLayoutInvocation).get(0), creationSupport.getNode());
      }
    }
    // check association
    assertInstanceOf(InvocationChildAssociation.class, layout.getAssociation());
    // check ID
    assertNotNull(ObjectInfoUtils.getId(layout));
  }

  public void test_setLayout() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "  }",
            "}");
    // check initial layout
    {
      LayoutInfo layout = shell.getLayout();
      assertInstanceOf(FillLayoutInfo.class, layout);
    }
    // set absolute layout
    final AbsoluteLayoutInfo absoluteLayout = AbsoluteLayoutInfo.createExplicit(shell);
    shell.setLayout(absoluteLayout);
    // check result
    assertSame(absoluteLayout, shell.getLayout());
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(null);",
        "  }",
        "}");
    // listen for AbsoluteLayoutInfo setObject() during refresh
    {
      final boolean[] absoluteLayout_objectSet = new boolean[1];
      shell.addBroadcastListener(new JavaInfoSetObjectAfter() {
        public void invoke(JavaInfo target, Object o) throws Exception {
          if (target == absoluteLayout) {
            assertNull(o);
            absoluteLayout_objectSet[0] = true;
          }
        }
      });
      // do refresh
      shell.refresh();
      assertTrue(absoluteLayout_objectSet[0]);
    }
  }

  /**
   * Test that {@link LayoutInfo#onSet()} for {@link AbsoluteLayoutInfo} adds
   * <code>setBounds()</code> invocation for {@link ControlInfo}'s.
   */
  public void test_onSet() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setLayoutData(new RowData(200, 100));",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // set absolute layout
    AbsoluteLayoutInfo absoluteLayout =
        new AbsoluteLayoutInfo(m_lastEditor,
            shell.getDescription().getToolkit(),
            new AbsoluteLayoutCreationSupport());
    shell.setLayout(absoluteLayout);
    // check result
    assertSame(absoluteLayout, shell.getLayout());
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(null);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setBounds(3, 3, 200, 100);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // When set "complex" layout for child Composite
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When we set layout for child {@link CompositeInfo} on {@link AbsoluteLayoutInfo}, we should
   * warn user.
   */
  public void test_onSetLayout_forChild_keepAbsolute() throws Exception {
    parseComposite(
        "public class Test extends Shell {",
        "  public Test() {",
        "    {",
        "      Composite inner = new Composite(this, SWT.NONE);",
        "      inner.setBounds(10, 20, 200, 100);",
        "    }",
        "  }",
        "}");
    refresh();
    // set RowLayout for "inner"
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        CompositeInfo inner = getJavaInfoByName("inner");
        LayoutInfo rowLayout = createJavaInfo("org.eclipse.swt.layout.RowLayout");
        inner.setLayout(rowLayout);
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("Confirm");
        context.clickButton("No, keep 'null' layout");
      }
    });
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    {",
        "      Composite inner = new Composite(this, SWT.NONE);",
        "      inner.setBounds(10, 20, 200, 100);",
        "      inner.setLayout(new RowLayout(SWT.HORIZONTAL));",
        "    }",
        "  }",
        "}");
  }

  /**
   * When we set layout for child {@link CompositeInfo} on {@link AbsoluteLayoutInfo}, we should
   * warn user.
   */
  public void test_onSetLayout_forChild_useFormLayout() throws Exception {
    parseComposite(
        "public class Test extends Shell {",
        "  public Test() {",
        "    {",
        "      Composite inner = new Composite(this, SWT.NONE);",
        "      inner.setBounds(10, 20, 200, 100);",
        "    }",
        "  }",
        "}");
    refresh();
    // set RowLayout for "inner"
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        CompositeInfo inner = getJavaInfoByName("inner");
        LayoutInfo rowLayout = createJavaInfo("org.eclipse.swt.layout.RowLayout");
        inner.setLayout(rowLayout);
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("Confirm");
        context.clickButton("Yes, use FormLayout");
      }
    });
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FormLayout());",
        "    {",
        "      Composite inner = new Composite(this, SWT.NONE);",
        "      {",
        "        FormData formData = new FormData();",
        "        formData.bottom = new FormAttachment(0, 120);",
        "        formData.right = new FormAttachment(0, 210);",
        "        formData.top = new FormAttachment(0, 20);",
        "        formData.left = new FormAttachment(0, 10);",
        "        inner.setLayoutData(formData);",
        "      }",
        "      inner.setLayout(new RowLayout(SWT.HORIZONTAL));",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for delete {@link AbsoluteLayoutInfo}, any location/size related invocation should be
   * removed.
   */
  public void test_delete() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(null);",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('New Button');",
            "      button.setLocation(0, 0);",
            "      button.setLocation(new Point(0, 0));",
            "      button.setSize(0, 0);",
            "      button.setSize(new Point(0, 0));",
            "      button.setBounds(0, 0, 0, 0);",
            "      button.setBounds(new Rectangle(0, 0, 0, 0));",
            "    }",
            "  }",
            "}");
    //
    AbsoluteLayoutInfo absoluteLayout = (AbsoluteLayoutInfo) shell.getLayout();
    assertInstanceOf(InvocationChildAssociation.class, absoluteLayout.getAssociation());
    assertTrue(absoluteLayout.canDelete());
    //
    LayoutInfo rowLayout = BTestUtils.createLayout("org.eclipse.swt.layout.RowLayout");
    shell.setLayout(rowLayout);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new RowLayout(SWT.HORIZONTAL));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('New Button');",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // command_create
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AbsoluteLayoutInfo#commandCreate(ControlInfo, ControlInfo)}.
   */
  public void test_create() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(null);",
            "  }",
            "}");
    AbsoluteLayoutInfo layout = (AbsoluteLayoutInfo) shell.getLayout();
    //
    ControlInfo button = BTestUtils.createButton();
    layout.commandCreate(button, null);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(null);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link AbsoluteLayoutInfo#commandCreate(ControlInfo, ControlInfo)}.
   */
  public void test_create_withReference() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(null);",
            "    {",
            "      Text text = new Text(this, SWT.BORDER);",
            "    }",
            "  }",
            "}");
    AbsoluteLayoutInfo layout = (AbsoluteLayoutInfo) shell.getLayout();
    ControlInfo referenceControl = shell.getChildrenControls().get(0);
    //
    ControlInfo button = BTestUtils.createButton();
    layout.commandCreate(button, referenceControl);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(null);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "    {",
        "      Text text = new Text(this, SWT.BORDER);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // command_changeBounds
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * generic check for bounds changing.
   */
  private void check_changeBounds(String[] initial,
      String[] expected,
      Point location,
      Dimension size) throws Exception {
    String[] initialCode =
        new String[]{
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(null);",
            "    Button button = new Button(this, SWT.NONE);",
            "    button.setText(\"test\");",
            "  }",
            "}"};
    // prepare initial code
    String[] initialLines = (String[]) ArrayUtils.clone(initialCode);
    for (int i = 0; i < initial.length; i++) {
      if (!StringUtils.isEmpty(initial[i])) {
        initialLines = (String[]) ArrayUtils.add(initialLines, 4 + i, initial[i]);
      }
    }
    // prepare expected code
    String[] expectedLines = (String[]) ArrayUtils.clone(initialCode);
    for (int i = 0; i < expected.length; i++) {
      if (!StringUtils.isEmpty(expected[i])) {
        expectedLines = (String[]) ArrayUtils.add(expectedLines, 4 + i, expected[i]);
      }
    }
    // prepare model
    CompositeInfo shellInfo = parseComposite(initialLines);
    shellInfo.refresh();
    ControlInfo buttonInfo = shellInfo.getChildrenControls().get(0);
    AbsoluteLayoutInfo layoutInfo = (AbsoluteLayoutInfo) shellInfo.getLayout();
    // perform code modifications
    layoutInfo.commandChangeBounds(buttonInfo, location, size);
    // check the results
    assertEditor(expectedLines);
  }

  public void test_command_changeBounds_set_location_only() throws Exception {
    check_changeBounds(
        new String[]{""},
        new String[]{"    button.setLocation(10, 10);"},
        new Point(10, 10),
        null);
  }

  public void test_command_changeBounds_set_size_after_location() throws Exception {
    check_changeBounds(new String[]{"    button.setLocation(10, 10);"}, new String[]{
        "    button.setSize(25, 60);",
        "    button.setLocation(10, 10);"}, new Point(10, 10), new Dimension(25, 60));
  }

  public void test_command_changeBounds_set_size_only() throws Exception {
    check_changeBounds(new String[]{
        "    button.setSize(25, 60);",
        "    button.setLocation(20, 20);"}, new String[]{
        "    button.setSize(30, 100);",
        "    button.setLocation(20, 20);"}, null, new Dimension(30, 100));
  }

  public void test_command_changeBounds_set_location_and_size() throws Exception {
    check_changeBounds(
        new String[]{""},
        new String[]{"    button.setBounds(10, 10, 25, 70);"},
        new Point(10, 10),
        new Dimension(25, 70));
  }

  public void test_command_changeBounds_set_location_only_after_setBounds_set() throws Exception {
    check_changeBounds(
        new String[]{"    button.setBounds(10, 10, 25, 70);"},
        new String[]{"    button.setBounds(30, 30, 25, 70);"},
        new Point(30, 30),
        null);
  }

  public void test_command_changeBounds_set_size_only_after_setBounds_set() throws Exception {
    check_changeBounds(
        new String[]{"    button.setBounds(30, 30, 25, 70);"},
        new String[]{"    button.setBounds(30, 30, 100, 100);"},
        null,
        new Dimension(100, 100));
  }

  public void test_command_changeBounds_set_location_and_size_after_setBounds_set()
      throws Exception {
    check_changeBounds(
        new String[]{"    button.setBounds(30, 30, 100, 100);"},
        new String[]{"    button.setBounds(10, 10, 25, 70);"},
        new Point(10, 10),
        new Dimension(25, 70));
  }

  public void test_command_changeBounds_set_location_only_as_Rectangle() throws Exception {
    check_changeBounds(
        new String[]{"    button.setBounds(new Rectangle(10, 10, 25, 80));"},
        new String[]{"    button.setBounds(new Rectangle(0, 0, 25, 80));"},
        new Point(0, 0),
        null);
  }

  public void test_command_changeBounds_set_size_only_as_Rectangle() throws Exception {
    check_changeBounds(
        new String[]{"    button.setBounds(new Rectangle(0, 0, 25, 80));"},
        new String[]{"    button.setBounds(new Rectangle(0, 0, 20, 60));"},
        null,
        new Dimension(20, 60));
  }

  public void test_command_changeBounds_set_location_and_size_as_Rectangle() throws Exception {
    check_changeBounds(
        new String[]{"    button.setBounds(new Rectangle(0, 0, 20, 60));"},
        new String[]{"    button.setBounds(new Rectangle(10, 10, 25, 80));"},
        new Point(10, 10),
        new Dimension(25, 80));
  }

  public void test_command_changeBounds_set_location_only_as_Point() throws Exception {
    check_changeBounds(
        new String[]{"    button.setLocation(new Point(10, 10));"},
        new String[]{"    button.setLocation(new Point(0, 0));"},
        new Point(0, 0),
        null);
  }

  public void test_command_changeBounds_set_size_only_as_Point() throws Exception {
    check_changeBounds(
        new String[]{"    button.setSize(new Point(20, 50));"},
        new String[]{"    button.setSize(new Point(25, 40));"},
        null,
        new Dimension(25, 40));
  }

  public void test_command_changeBounds_setBoundsInts_removeUnused() throws Exception {
    check_changeBounds(
        new String[]{
            "    button.setBounds(10, 20, 100, 50);",
            "    button.setLocation(10, 20);",
            "    button.setSize(100, 50);",
            "    button.setLocation(new Point(10, 20));",
            "    button.setSize(new Point(100, 50));"},
        new String[]{"    button.setBounds(1, 2, 25, 40);"},
        new Point(1, 2),
        new Dimension(25, 40));
  }

  public void test_command_changeBounds_setBoundsRectangle_removeUnused() throws Exception {
    check_changeBounds(
        new String[]{
            "    button.setBounds(new Rectangle(10, 20, 100, 50));",
            "    button.setLocation(10, 20);",
            "    button.setSize(100, 50);",
            "    button.setLocation(new Point(10, 20));",
            "    button.setSize(new Point(100, 50));"},
        new String[]{"    button.setBounds(new Rectangle(1, 2, 25, 40));"},
        new Point(1, 2),
        new Dimension(25, 40));
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
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(null);",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setBounds(0, 0, 10, 10);",
            "    }",
            "  }",
            "}");
    preferences.setValue(IPreferenceConstants.P_AUTOSIZE_ON_PROPERTY_CHANGE, true);
    //
    ControlInfo button = shell.getChildrenControls().get(0);
    Property enabledProperty = button.getPropertyByTitle("enabled");
    enabledProperty.setValue(Boolean.FALSE);
    waitEventLoop(1);
    //
    Rectangle bounds = button.getBounds();
    assertEquals(10, bounds.width);
    assertEquals(10, bounds.height);
  }

  /**
   * Modify "text" property, so bigger size expected.
   */
  public void test_autoSize_textProperty() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(null);",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('New');",
            "      button.setBounds(0, 0, 10, 10);",
            "    }",
            "  }",
            "}");
    preferences.setValue(IPreferenceConstants.P_AUTOSIZE_ON_PROPERTY_CHANGE, true);
    //
    ControlInfo button = shell.getChildrenControls().get(0);
    Property textProperty = button.getPropertyByTitle("text");
    textProperty.setValue("Very long text");
    waitEventLoop(1);
    //
    Rectangle bounds = button.getBounds();
    assertTrue(bounds.width > 10);
    assertTrue(bounds.height > 10);
  }

  /**
   * Modify "image" property, so bigger size expected.
   */
  public void test_autoSize_imageProperty() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(null);",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setBounds(0, 0, 10, 10);",
            "    }",
            "  }",
            "}");
    preferences.setValue(IPreferenceConstants.P_AUTOSIZE_ON_PROPERTY_CHANGE, true);
    //
    ControlInfo button = shell.getChildrenControls().get(0);
    GenericProperty imageProperty = (GenericProperty) button.getPropertyByTitle("image");
    imageProperty.setExpression(
        "new org.eclipse.swt.graphics.Image(null, Object.class.getClassLoader().getResourceAsStream(\"/javax/swing/plaf/basic/icons/JavaCup16.png\"))",
        imageProperty);
    waitEventLoop(1);
    //
    Rectangle bounds = button.getBounds();
    assertTrue(bounds.width > 10);
    assertTrue(bounds.height > 10);
  }

  /**
   * Modify "text" property, but {@link IPreferenceConstants#P_AUTOSIZE_ON_PROPERTY_CHANGE} is not
   * enabled, so no auto-size
   */
  public void test_autoSize_textProperty_notEnabled() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(null);",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('New');",
            "      button.setBounds(0, 0, 10, 10);",
            "    }",
            "  }",
            "}");
    preferences.setValue(IPreferenceConstants.P_AUTOSIZE_ON_PROPERTY_CHANGE, false);
    //
    ControlInfo button = shell.getChildrenControls().get(0);
    Property textProperty = button.getPropertyByTitle("text");
    textProperty.setValue("Very long text");
    waitEventLoop(1);
    //
    Rectangle bounds = button.getBounds();
    assertEquals(10, bounds.width);
    assertEquals(10, bounds.height);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Auto-size
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for <code>Autosize control</code> action.
   */
  public void test_autoSizeAction_1() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('New');",
            "      button.setBounds(10, 20, 10, 10);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    ControlInfo button = shell.getChildrenControls().get(0);
    Dimension buttonPrefSize = button.getPreferredSize();
    // prepare action
    IAction autoSizeAction;
    {
      MenuManager manager = getDesignerMenuManager();
      button.getBroadcastObject().addContextMenu(Collections.singletonList(button), button, manager);
      autoSizeAction = findChildAction(manager, "Autosize control");
      assertNotNull(autoSizeAction);
    }
    // perform auto-size
    autoSizeAction.run();
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('New');",
        "      button.setBounds(10, 20, "
            + buttonPrefSize.width
            + ", "
            + buttonPrefSize.height
            + ");",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for <code>Autosize control</code> action.
   */
  public void test_autoSizeAction_2() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test {",
            "  public static void main(String[] args) {",
            "    Shell shell = new Shell();",
            "    {",
            "      Button button = new Button(shell, SWT.NONE);",
            "      button.setText('New');",
            "      button.setBounds(10, 20, 10, 10);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    ControlInfo button = shell.getChildrenControls().get(0);
    // prepare action
    IAction autoSizeAction;
    {
      MenuManager manager = getDesignerMenuManager();
      button.getBroadcastObject().addContextMenu(Collections.singletonList(button), button, manager);
      autoSizeAction = findChildAction(manager, "Autosize control");
      assertNotNull(autoSizeAction);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Bounds" property
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Helper method preparing to test "Bounds" property.
   */
  private Property prepareBoundsProperty() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(null);",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setBounds(10, 30, 100, 200);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    Property boundsProperty = shell.getChildrenControls().get(0).getPropertyByTitle("Bounds");
    return boundsProperty;
  }

  /**
   * Helper method returning the sub-property of Bounds property by title.
   */
  private Property getBoundsPropertySubProperty(String title) throws Exception {
    ComplexProperty boundsProperty = (ComplexProperty) prepareBoundsProperty();
    Property[] subProperties = boundsProperty.getProperties();
    return getPropertyByTitle(subProperties, title);
  }

  /**
   * Test "Bounds" property.
   */
  public void test_BoundsProperty() throws Exception {
    Property boundsProperty = prepareBoundsProperty();
    assertNotNull(boundsProperty);
    assertTrue(boundsProperty instanceof ComplexProperty);
    assertTrue(boundsProperty.isModified());
    //
    ComplexProperty boundsComplexProperty = (ComplexProperty) boundsProperty;
    Property[] subProperties = boundsComplexProperty.getProperties();
    assertEquals(subProperties.length, 4);
    //
    String actualText = getPropertyText(boundsProperty);
    assertEquals("(10, 30, 100, 200)", actualText);
  }

  /**
   * Test setting "x" sub-property of "Bounds" property.
   */
  public void test_BoundsProperty_set_x() throws Exception {
    Property subProperty = getBoundsPropertySubProperty("x");
    assertNotNull(subProperty);
    subProperty.setValue(0);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(null);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setBounds(0, 30, 100, 200);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test getting "x" sub-property of "Bounds" property.
   */
  public void test_BoundsProperty_get_x() throws Exception {
    Property subProperty = getBoundsPropertySubProperty("x");
    assertNotNull(subProperty);
    Object value = subProperty.getValue();
    assertInstanceOf(Integer.class, value);
    assertEquals(10, ((Integer) value).intValue());
  }

  /**
   * Test setting "y" sub-property of "Bounds" property.
   */
  public void test_BoundsProperty_set_y() throws Exception {
    Property subProperty = getBoundsPropertySubProperty("y");
    assertNotNull(subProperty);
    subProperty.setValue(5);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(null);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setBounds(10, 5, 100, 200);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test getting "y" sub-property of "Bounds" property.
   */
  public void test_BoundsProperty_get_y() throws Exception {
    Property subProperty = getBoundsPropertySubProperty("y");
    assertNotNull(subProperty);
    Object value = subProperty.getValue();
    assertInstanceOf(Integer.class, value);
    assertEquals(30, ((Integer) value).intValue());
  }

  /**
   * Test setting "width" sub-property of "Bounds" property.
   */
  public void test_BoundsProperty_set_width() throws Exception {
    Property subProperty = getBoundsPropertySubProperty("width");
    assertNotNull(subProperty);
    subProperty.setValue(150);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(null);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setBounds(10, 30, 150, 200);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test getting "width" sub-property of "Bounds" property.
   */
  public void test_BoundsProperty_get_width() throws Exception {
    Property subProperty = getBoundsPropertySubProperty("width");
    assertNotNull(subProperty);
    Object value = subProperty.getValue();
    assertInstanceOf(Integer.class, value);
    assertEquals(100, ((Integer) value).intValue());
  }

  /**
   * Test setting "height" sub-property of "Bounds" property.
   */
  public void test_BoundsProperty_set_height() throws Exception {
    Property subProperty = getBoundsPropertySubProperty("height");
    assertNotNull(subProperty);
    subProperty.setValue(220);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(null);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setBounds(10, 30, 100, 220);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test getting "height" sub-property of "Bounds" property.
   */
  public void test_BoundsProperty_get_height() throws Exception {
    Property subProperty = getBoundsPropertySubProperty("height");
    assertNotNull(subProperty);
    Object value = subProperty.getValue();
    assertInstanceOf(Integer.class, value);
    assertEquals(200, ((Integer) value).intValue());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creaton flow
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test applying creation flow order.
   */
  public void test_changeBounds_CreationFlow() throws Exception {
    preferences.setValue(IPreferenceConstants.P_CREATION_FLOW, true);
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(null);",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setBounds(5, 5, 100, 30);",
            "    }",
            "    {",
            "      Text text = new Text(this, SWT.BORDER);",
            "      text.setBounds(50, 50, 100, 30);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    AbsoluteLayoutInfo layout = (AbsoluteLayoutInfo) shell.getLayout();
    ControlInfo button = getJavaInfoByName("button");
    // Change bounds
    layout.commandChangeBounds(button, new Point(100, 100), null);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(null);",
        "    {",
        "      Text text = new Text(this, SWT.BORDER);",
        "      text.setBounds(50, 50, 100, 30);",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setBounds(100, 100, 100, 30);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_clipboard() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    {",
            "      Composite composite = new Composite(this, SWT.NONE);",
            "      {",
            "        Button button = new Button(composite, SWT.NONE);",
            "        button.setBounds(10, 20, 100, 50);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    FillLayoutInfo fillLayout = (FillLayoutInfo) shell.getLayout();
    // prepare memento
    JavaInfoMemento memento;
    {
      CompositeInfo composite = (CompositeInfo) shell.getChildrenControls().get(0);
      memento = JavaInfoMemento.createMemento(composite);
    }
    // paste
    {
      CompositeInfo composite = (CompositeInfo) memento.create(shell);
      fillLayout.command_CREATE(composite, null);
      memento.apply();
    }
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    {",
        "      Composite composite = new Composite(this, SWT.NONE);",
        "      {",
        "        Button button = new Button(composite, SWT.NONE);",
        "        button.setBounds(10, 20, 100, 50);",
        "      }",
        "    }",
        "    {",
        "      Composite composite = new Composite(this, SWT.NONE);",
        "      {",
        "        Button button = new Button(composite, SWT.NONE);",
        "        button.setBounds(10, 20, 100, 50);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Permissions
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * On absolute layout components can not be reordered, but can be moved, i.e. bounds can be
   * changed.
   */
  public void test_canMove() throws Exception {
    setFileContentSrc(
        "test/MyShell.java",
        getTestSource(
            "public class MyShell extends Shell {",
            "  private Button m_button;",
            "  public MyShell() {",
            "    setLayout(null);",
            "    m_button = new Button(this, SWT.NONE);",
            "  }",
            "  public Button getButton() {",
            "    return m_button;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    CompositeInfo panel =
        parseComposite(
            "// filler filler filler",
            "public class Test extends MyShell {",
            "  public Test() {",
            "  }",
            "}");
    assertInstanceOf(AbsoluteLayoutInfo.class, panel.getLayout());
    ControlInfo button = panel.getChildrenControls().get(0);
    // check permissions
    assertFalse(button.getCreationSupport().canReorder());
    assertFalse(button.getCreationSupport().canReparent());
    assertTrue(JavaInfoUtils.canMove(button));
    assertFalse(JavaInfoUtils.canReparent(button));
  }
}