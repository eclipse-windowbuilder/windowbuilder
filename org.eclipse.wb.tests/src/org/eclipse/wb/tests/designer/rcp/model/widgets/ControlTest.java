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
package org.eclipse.wb.tests.designer.rcp.model.widgets;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.string.StringPropertyEditor;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.exception.MultipleConstructorsError;
import org.eclipse.wb.internal.core.utils.exception.NoEntryPointError;
import org.eclipse.wb.internal.rcp.IExceptionConstants;
import org.eclipse.wb.internal.rcp.model.widgets.TabFolderInfo;
import org.eclipse.wb.internal.rcp.model.widgets.TabItemInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.Expectations;
import org.eclipse.wb.tests.designer.Expectations.InsValue;
import org.eclipse.wb.tests.designer.Expectations.IntValue;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;

import java.util.List;

/**
 * Simple SWT {@link ControlInfo} test.
 * 
 * @author scheglov_ke
 */
public class ControlTest extends RcpModelTest {
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
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setSize(450, 300);",
            "    Button button = new Button(this, SWT.NONE);",
            "    button.setBounds(10, 20, 50, 30);",
            "    button.setText('button');",
            "  }",
            "}");
    // prepare button
    ControlInfo buttonInfo;
    {
      List<ControlInfo> children = shell.getChildrenControls();
      assertEquals(1, children.size());
      buttonInfo = children.get(0);
    }
    // check "text" property
    {
      Property buttonProperty = buttonInfo.getPropertyByTitle("text");
      assertNotNull(buttonProperty);
      assertEquals("button", buttonProperty.getValue());
      assertTrue(buttonProperty.getEditor() instanceof StringPropertyEditor);
    }
    //
    shell.refresh();
    //
    assertNotNull(shell.getImage());
    assertEquals(
        new org.eclipse.swt.graphics.Rectangle(0, 0, 450, 300),
        shell.getImage().getBounds());
  }

  public void test_parse_unknownSuperClassForAnonymous() throws Exception {
    m_ignoreCompilationProblems = true;
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    new UnknownType() {};",
            "  }",
            "}");
    shell.refresh();
    assertNoErrors(shell);
  }

  /**
   * Check for "Chinese problem" - using SWT without configuring project for SWT.
   */
  @DisposeProjectAfter
  public void test_parse_notConfiguredForSWT() throws Exception {
    do_projectDispose();
    do_projectCreate();
    m_ignoreCompilationProblems = true;
    try {
      parseComposite(
          "// filler filler filler",
          "public class Test extends Shell {",
          "  public Test() {",
          "  }",
          "}");
      fail();
    } catch (Throwable e) {
      DesignerException de = DesignerExceptionUtils.getDesignerException(e);
      assertEquals(IExceptionConstants.NOT_CONFIGURED_FOR_SWT, de.getCode());
      assertTrue(DesignerExceptionUtils.isWarning(e));
    }
  }

  /**
   * If several constructors, then default (with Composite and style) should be used.
   */
  public void test_severalConstructors_useDefault_forComposite() throws Exception {
    CompositeInfo composite =
        parseComposite(
            "public class Test extends Composite {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "  public Test(Composite parent, int style, boolean a) {",
            "    super(parent, style);",
            "  }",
            "}");
    composite.refresh();
    assertNoErrors(composite);
  }

  /**
   * For {@link Shell} default constructor is constructor without parameters.
   */
  public void test_severalConstructors_useDefault_forShell() throws Exception {
    CompositeInfo composite =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "  }",
            "  public Test(Shell parent) {",
            "    super(parent);",
            "  }",
            "}");
    composite.refresh();
    assertNoErrors(composite);
  }

  /**
   * If several constructors, and no default (with Composite and style), so fail.
   */
  public void test_severalConstructors_noDefault() throws Exception {
    try {
      parseComposite(
          "public class Test extends Composite{",
          "  public Test(Composite parent, int style, boolean a) {",
          "    super(parent, style);",
          "  }",
          "  public Test(Composite parent, int style, int b) {",
          "    super(parent, style);",
          "  }",
          "}");
      fail();
    } catch (MultipleConstructorsError e) {
      assertThat(e.getEditor()).isNotNull();
      assertThat(e.getTypeDeclaration()).isNotNull();
    }
  }

  /**
   * Test for using @wbp.parser.entryPoint to force starting execution flow from some constructor,
   * even if we don't know superclass.
   */
  public void test_entryPointTag() throws Exception {
    useStrictEvaluationMode(false);
    CompositeInfo shell =
        parseComposite(
            "public class Test {",
            "  /**",
            "  * @wbp.parser.entryPoint",
            "  */",
            "  public Test(Shell parent) {",
            "    Shell shell = new Shell(parent);",
            "  }",
            "}");
    assertHierarchy(
        "{new: org.eclipse.swt.widgets.Shell} {local-unique: shell} {/new Shell(parent)/}",
        "  {implicit-layout: absolute} {implicit-layout} {}");
    shell.refresh();
    assertNoErrors(shell);
  }

  /**
   * Automatically use constructor as entry point: good guess.
   */
  public void test_alwaysTryConstructor_success() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test {",
            "  public Test() {",
            "    Shell shell = new Shell();",
            "  }",
            "}");
    assertHierarchy(
        "{new: org.eclipse.swt.widgets.Shell} {local-unique: shell} {/new Shell()/}",
        "  {implicit-layout: absolute} {implicit-layout} {}");
    shell.refresh();
    assertNoErrors(shell);
  }

  /**
   * Automatically use constructor as entry point: no, still no GUI in constructor.
   */
  public void test_alwaysTryConstructor_fail() throws Exception {
    try {
      parseComposite(
          "public class Test {",
          "  Shell shell; // just to have reference on RCP",
          "  public Test() {",
          "  }",
          "}");
      fail();
    } catch (Throwable e_) {
      Throwable e = DesignerExceptionUtils.getRootCause(e_);
      assertThat(e).isExactlyInstanceOf(NoEntryPointError.class);
    }
  }

  public void test_constructor_withShellParameter_asSecondArgument() throws Exception {
    useStrictEvaluationMode(false);
    CompositeInfo shell =
        parseComposite(
            "public class Test {",
            "  public Test(Object filler, Shell parent) {",
            "    Shell shell = new Shell(parent, SWT.SHELL_TRIM);",
            "  }",
            "}");
    assertHierarchy(
        "{new: org.eclipse.swt.widgets.Shell} {local-unique: shell} {/new Shell(parent, SWT.SHELL_TRIM)/}",
        "  {implicit-layout: absolute} {implicit-layout} {}");
    shell.refresh();
    assertNoErrors(shell);
  }

  /**
   * Test for parsing {@link AnonymousClassDeclaration} in <code>Realm</code>.
   */
  public void test_parse_Realm_runWithDefault() throws Exception {
    try {
      m_testProject.addPlugin("org.eclipse.core.databinding.observable");
      m_testProject.addPlugin("org.eclipse.jface.databinding");
      CompositeInfo shell =
          parseComposite(
              "import org.eclipse.core.databinding.observable.Realm;",
              "import org.eclipse.jface.databinding.swt.SWTObservables;",
              "public class Test {",
              "  public static void main(String[] args) {",
              "    Display display = Display.getDefault();",
              "    Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {",
              "      public void run() {",
              "        Test window = new Test();",
              "      }",
              "    });",
              "  }",
              "  public Test() {",
              "    Shell shell = new Shell();",
              "  }",
              "}");
      assertHierarchy(
          "{new: org.eclipse.swt.widgets.Shell} {local-unique: shell} {/new Shell()/}",
          "  {implicit-layout: absolute} {implicit-layout} {}");
      // refresh()
      shell.refresh();
      assertNoErrors(shell);
    } finally {
      do_projectDispose();
    }
  }

  public void test_parseSeparate_ClassInstanceCreation() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    new Button(this, SWT.NONE);",
            "  }",
            "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/new Button(this, SWT.NONE)/}",
        "  {implicit-layout: absolute} {implicit-layout} {}",
        "  {new: org.eclipse.swt.widgets.Button} {empty} {/new Button(this, SWT.NONE)/}");
    // refresh()
    shell.refresh();
    assertNoErrors(shell);
  }

  /**
   * Test for {@link CompositeInfo#getClientAreaInsets2()}.
   */
  public void test_insetsWithGroup() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    Group group = new Group(this, SWT.NONE);",
            "    group.setBounds(0, 0, 200, 200);",
            "  }",
            "}");
    shell.refresh();
    // "shell" insets
    {
      Insets expected =
          Expectations.get(new Insets(30, 8, 8, 8), new InsValue[]{
              new InsValue("flanker-windows", new Insets(30, 8, 8, 8)),
              new InsValue("scheglov-win", new Insets(30, 8, 8, 8))});
      assertEquals(expected, shell.getClientAreaInsets());
      assertEquals(new Insets(0, 0, 0, 0), shell.getClientAreaInsets2());
    }
    // "group" insets
    {
      CompositeInfo group = (CompositeInfo) shell.getChildrenControls().get(0);
      assertEquals(new Insets(0, 0, 0, 0), group.getClientAreaInsets());
      Insets expected =
          Expectations.get(new Insets(15, 3, 3, 3), new InsValue[]{
              new InsValue("flanker-windows", new Insets(15, 3, 3, 3)),
              new InsValue("scheglov-win", new Insets(15, 3, 3, 3))});
      assertEquals(expected, group.getClientAreaInsets2());
    }
  }

  public void test_visualInheritance_withOverride() throws Exception {
    setFileContentSrc(
        "test/MyComposite.java",
        getTestSource(
            "public class MyComposite extends Composite {",
            "  public MyComposite(Composite parent, int style) {",
            "    super(parent, style);",
            "    init();",
            "  }",
            "  protected void init() {",
            "    setLayout(new FillLayout());",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    CompositeInfo composite =
        parseComposite(
            "public class Test extends MyComposite {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "  protected void init() {",
            "    super.init();",
            "  }",
            "}");
    assertHierarchy(
        "{this: test.MyComposite} {this} {}",
        "  {implicit-layout: org.eclipse.swt.layout.FillLayout} {implicit-layout} {}");
    // refresh()
    composite.refresh();
    assertNoErrors(composite);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * There are no "getter" for {@link Control#setCapture(boolean)} and
   * {@link Control#setRedraw(boolean)}, so we should provide default value in component
   * description.
   */
  public void test_properties_defaultValues() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "// filler filler filler",
            "public class Test extends Shell {",
            "  public Test() {",
            "  }",
            "}");
    assertEquals(Boolean.FALSE, shell.getPropertyByTitle("capture").getValue());
    assertEquals(Boolean.TRUE, shell.getPropertyByTitle("redraw").getValue());
  }

  /**
   * There are no "getter" for {@link Control#setCapture(boolean)}, so we should provide default
   * value in component description.
   */
  public void test_properties_setSize() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setSize(new Point(399, 299));",
            "  }",
            "}");
    shell.refresh();
    // check current size
    assertEquals(new Dimension(399, 299), shell.getBounds().getSize());
    assertEquals(new Point(399, 299), ((Control) shell.getObject()).getSize());
    // use "size" property
    Property sizeProperty = shell.getPropertyByTitle("size");
    assertEquals(new Point(399, 299), sizeProperty.getValue());
    sizeProperty.setValue(new Point(500, 300));
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setSize(new Point(500, 300));",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Shell
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_setIME_property() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "// filler filler filler",
            "public class Test extends Shell {",
            "  public Test() {",
            "  }",
            "}");
    Property property = shell.getPropertyByTitle("IME");
    assertNull(property);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Swing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for parsing Swing GUI in RCP project.
   */
  public void test_swingForm_InRCPproject() throws Exception {
    ContainerInfo panel =
        parseJavaInfo(
            "import javax.swing.*;",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertNoErrors(panel);
  }

  /**
   * Test for parsing RCP form in RCP project, but with reference on Swing class.
   */
  public void test_swingClass_InRCPform() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  private final javax.swing.JButton button = null;",
            "  public Test() {",
            "  }",
            "}");
    shell.refresh();
    assertNoErrors(shell);
  }

  /**
   * There was problem that if Swing NVO hierarchy is bigger than SWT one, then it was used as root.
   * However we add NVO container to it, and in same time, as NVO it should be added to this NVO
   * container. So, this causes hierarchy exception.
   */
  public void test_swingNVO_inRCP() throws Exception {
    parseComposite(
        "import javax.swing.*;",
        "public class Test extends Shell {",
        "  /**",
        "  * @wbp.nonvisual location=0,0",
        "  */",
        "  private final JPanel panel = new JPanel();",
        "  public Test() {",
        "    panel.add(new JButton());",
        "  }",
        "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {}",
        "  {implicit-layout: absolute} {implicit-layout} {}",
        "  {NonVisualBeans}",
        "    {new: javax.swing.JPanel} {field-initializer: panel} {/new JPanel()/ /panel.add(new JButton())/}",
        "      {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "      {new: javax.swing.JButton} {empty} {/panel.add(new JButton())/}");
  }

  /**
   * Test for parsing RCP (in main method) in RCP project, but with reference on Swing class.
   */
  public void test_swingClass_InRCP_mainMethod() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test {",
            "  private final javax.swing.JButton button = null;",
            "  public static void main(String[] args) {",
            "    Shell shell = new Shell();",
            "  }",
            "}");
    shell.refresh();
    assertNoErrors(shell);
  }

  public void test_BeanInfo_icon() throws Exception {
    setFileContentSrc(
        "test/MyShell.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyShell extends Shell {",
            "}"));
    setFileContentSrc(
        "test/MyShellBeanInfo.java",
        getSourceDQ(
            "package test;",
            "import java.awt.Image;",
            "import java.awt.image.BufferedImage;",
            "public class MyShellBeanInfo extends java.beans.SimpleBeanInfo {",
            "  public Image getIcon(int iconKind) {",
            "    return new BufferedImage(10, 15, BufferedImage.TYPE_INT_RGB);",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    CompositeInfo shell =
        parseComposite(
            "// filler filler filler",
            "public class Test extends MyShell {",
            "  public Test() {",
            "  }",
            "}");
    // check ComponentDescription 
    ComponentDescription description = shell.getDescription();
    Assertions.assertThat(description.getBeanInfo()).isNotNull();
    Image icon = description.getIcon();
    assertEquals(10, icon.getBounds().width);
    assertEquals(15, icon.getBounds().height);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "null" layout and real layouts
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * If {@link Composite} with some real {@link Layout} is in "null" layout, then we should manually
   * call {@link Composite#layout()} to apply real {@link Layout}.
   */
  public void test_inAbsoluteLayout_realLayout() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    {",
            "      Composite composite_1 = new Composite(this, SWT.NONE);",
            "      composite_1.setBounds(10, 10, 200, 150);",
            "      composite_1.setLayout(new FillLayout());",
            "      {",
            "        Composite composite_2 = new Composite(composite_1, SWT.NONE);",
            "        composite_2.setLayout(new RowLayout());",
            "        {",
            "          new Button(composite_2, SWT.NONE);",
            "        }",
            "      }",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/new Composite(this, SWT.NONE)/}",
        "  {implicit-layout: absolute} {implicit-layout} {}",
        "  {new: org.eclipse.swt.widgets.Composite} {local-unique: composite_1} {/new Composite(this, SWT.NONE)/ /composite_1.setBounds(10, 10, 200, 150)/ /composite_1.setLayout(new FillLayout())/ /new Composite(composite_1, SWT.NONE)/}",
        "    {new: org.eclipse.swt.layout.FillLayout} {empty} {/composite_1.setLayout(new FillLayout())/}",
        "    {new: org.eclipse.swt.widgets.Composite} {local-unique: composite_2} {/new Composite(composite_1, SWT.NONE)/ /composite_2.setLayout(new RowLayout())/ /new Button(composite_2, SWT.NONE)/}",
        "      {new: org.eclipse.swt.layout.RowLayout} {empty} {/composite_2.setLayout(new RowLayout())/}",
        "      {new: org.eclipse.swt.widgets.Button} {empty} {/new Button(composite_2, SWT.NONE)/}",
        "        {virtual-layout_data: org.eclipse.swt.layout.RowData} {virtual-layout-data} {}");
    shell.refresh();
    CompositeInfo composite_1 = (CompositeInfo) shell.getChildrenControls().get(0);
    CompositeInfo composite_2 = (CompositeInfo) composite_1.getChildrenControls().get(0);
    ControlInfo button = composite_2.getChildrenControls().get(0);
    assertFalse(button.getBounds().isEmpty());
  }

  /**
   * If {@link Composite} with some real {@link Layout} is in {@link TabFolder} which is on "null"
   * layout, then we should manually call {@link Composite#layout()} to apply real {@link Layout}.
   */
  public void test_inAbsoluteLayout_tabFolder() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    {",
            "      TabFolder tabFolder = new TabFolder(this, SWT.NONE);",
            "      tabFolder.setBounds(10, 10, 200, 150);",
            "      TabItem tabItem = new TabItem(tabFolder, SWT.NONE);",
            "      {",
            "        Composite composite = new Composite(tabFolder, SWT.NONE);",
            "        tabItem.setControl(composite);",
            "        composite.setLayout(new FillLayout());",
            "        {",
            "          new Button(composite, SWT.NONE);",
            "        }",
            "      }",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/new TabFolder(this, SWT.NONE)/}",
        "  {implicit-layout: absolute} {implicit-layout} {}",
        "  {new: org.eclipse.swt.widgets.TabFolder} {local-unique: tabFolder} {/new TabFolder(this, SWT.NONE)/ /tabFolder.setBounds(10, 10, 200, 150)/ /new TabItem(tabFolder, SWT.NONE)/ /new Composite(tabFolder, SWT.NONE)/}",
        "    {new: org.eclipse.swt.widgets.TabItem} {local-unique: tabItem} {/new TabItem(tabFolder, SWT.NONE)/ /tabItem.setControl(composite)/}",
        "    {new: org.eclipse.swt.widgets.Composite} {local-unique: composite} {/new Composite(tabFolder, SWT.NONE)/ /tabItem.setControl(composite)/ /composite.setLayout(new FillLayout())/ /new Button(composite, SWT.NONE)/}",
        "      {new: org.eclipse.swt.layout.FillLayout} {empty} {/composite.setLayout(new FillLayout())/}",
        "      {new: org.eclipse.swt.widgets.Button} {empty} {/new Button(composite, SWT.NONE)/}");
    shell.refresh();
    TabFolderInfo tabFolder = (TabFolderInfo) shell.getChildrenControls().get(0);
    TabItemInfo tabItem = tabFolder.getItems2().get(0);
    // Composite has bounds, and it should perform layout()
    CompositeInfo composite = (CompositeInfo) tabItem.getControl();
    assertFalse(composite.getBounds().isEmpty());
    // ...so Button also should have bounds
    ControlInfo button = composite.getChildrenControls().get(0);
    assertFalse(button.getBounds().isEmpty());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // RTL orientation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * We should correctly handle bounds for {@link Control}s on {@link Composite} with
   * {@link SWT#RIGHT_TO_LEFT} style.
   */
  public void test_RTL() throws Exception {
    CompositeInfo composite =
        parseComposite(
            "public class Test extends Composite {",
            "  public Test(Composite parent) {",
            "    super(parent, SWT.RIGHT_TO_LEFT);",
            "    setLayout(new RowLayout());",
            "    Button button_1 = new Button(this, SWT.NONE);",
            "    Button button_2 = new Button(this, SWT.NONE);",
            "  }",
            "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Composite} {this} {/setLayout(new RowLayout())/ /new Button(this, SWT.NONE)/ /new Button(this, SWT.NONE)/}",
        "  {new: org.eclipse.swt.layout.RowLayout} {empty} {/setLayout(new RowLayout())/}",
        "  {new: org.eclipse.swt.widgets.Button} {local-unique: button_1} {/new Button(this, SWT.NONE)/}",
        "    {virtual-layout_data: org.eclipse.swt.layout.RowData} {virtual-layout-data} {}",
        "  {new: org.eclipse.swt.widgets.Button} {local-unique: button_2} {/new Button(this, SWT.NONE)/}",
        "    {virtual-layout_data: org.eclipse.swt.layout.RowData} {virtual-layout-data} {}");
    composite.refresh();
    // this
    assertTrue(composite.isRTL());
    assertEquals(new Insets(0, 0, 0, 0), composite.getClientAreaInsets());
    // bounds of Button-s
    int m = 3;
    int s = 3;
    int w = 12;
    int h =
        Expectations.get(25, new IntValue[]{
            new IntValue("scheglov-win", 25),
            new IntValue("Flanker-Windows", 25),
            new IntValue("sablin-aa", 25),});
    {
      ControlInfo button_1 = getJavaInfoByName("button_1");
      assertEquals(new Rectangle(m, m, w, h), button_1.getModelBounds());
      assertEquals(new Rectangle(450 - w - m, m, w, h), button_1.getBounds());
    }
    {
      ControlInfo button_2 = getJavaInfoByName("button_2");
      assertEquals(new Rectangle(m + w + s, m, w, h), button_2.getModelBounds());
      assertEquals(new Rectangle(450 - w - m - s - w, m, w, h), button_2.getBounds());
    }
  }

  /**
   * Two levels of {@link Composite} with {@link SWT#RIGHT_TO_LEFT} style.
   */
  public void test_RTL_withInnerComposite() throws Exception {
    CompositeInfo composite =
        parseComposite(
            "public class Test extends Composite {",
            "  public Test(Composite parent) {",
            "    super(parent, SWT.RIGHT_TO_LEFT);",
            "    setLayout(new FillLayout());",
            "    {",
            "      Composite inner = new Composite(this, SWT.RIGHT_TO_LEFT);",
            "      inner.setLayout(new RowLayout());",
            "      Button button = new Button(inner, SWT.NONE);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Composite} {this} {/setLayout(new FillLayout())/ /new Composite(this, SWT.RIGHT_TO_LEFT)/}",
        "  {new: org.eclipse.swt.layout.FillLayout} {empty} {/setLayout(new FillLayout())/}",
        "  {new: org.eclipse.swt.widgets.Composite} {local-unique: inner} {/new Composite(this, SWT.RIGHT_TO_LEFT)/ /inner.setLayout(new RowLayout())/ /new Button(inner, SWT.NONE)/}",
        "    {new: org.eclipse.swt.layout.RowLayout} {empty} {/inner.setLayout(new RowLayout())/}",
        "    {new: org.eclipse.swt.widgets.Button} {local-unique: button} {/new Button(inner, SWT.NONE)/}",
        "      {virtual-layout_data: org.eclipse.swt.layout.RowData} {virtual-layout-data} {}");
    composite.refresh();
    // "this" has RTL orientation		
    assertTrue(composite.isRTL());
    assertEquals(new Insets(0, 0, 0, 0), composite.getClientAreaInsets());
    // "inner"
    {
      CompositeInfo inner = getJavaInfoByName("inner");
      assertTrue(inner.isRTL());
      assertEquals(new Insets(0, 0, 0, 0), inner.getClientAreaInsets());
      assertEquals(new Rectangle(0, 0, 450, 300), inner.getModelBounds());
      assertEquals(new Rectangle(0, 0, 450, 300), inner.getBounds());
    }
    // "button"
    int m = 3;
    int w = 12;
    int h =
        Expectations.get(25, new IntValue[]{
            new IntValue("scheglov_win", 25),
            new IntValue("Flanker-Windows", 25),
            new IntValue("sablin-aa", 25)});
    {
      ControlInfo button = getJavaInfoByName("button");
      assertEquals(new Rectangle(m, m, w, h), button.getModelBounds());
      assertEquals(new Rectangle(450 - w - m, m, w, h), button.getBounds());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Special cases
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * If instance of anonymous {@link Control} subclass is created, create instead nearest
   * non-abstract {@link Control} super class.
   */
  public void test_newAnonymousControl() throws Exception {
    setFileContentSrc(
        "test/MyAbstractButton.java",
        getTestSource(
            "public class MyAbstractButton extends Button {",
            "  public MyAbstractButton(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "  protected void checkSubclass() {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    useStrictEvaluationMode(false);
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    new MyAbstractButton(this, SWT.NONE) {};",
            "  }",
            "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/new MyAbstractButton(this, SWT.NONE)/}",
        "  {implicit-layout: absolute} {implicit-layout} {}",
        "  {new: test.MyAbstractButton} {empty} {/new MyAbstractButton(this, SWT.NONE)/}");
    // refresh
    shell.refresh();
    assertNoErrors(shell);
  }
}