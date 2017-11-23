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
package org.eclipse.wb.tests.designer.swt.model.widgets;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.ConstructorAssociation;
import org.eclipse.wb.core.model.association.ImplicitObjectAssociation;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.IThisMethodParameterEvaluator;
import org.eclipse.wb.internal.core.model.order.ComponentOrder;
import org.eclipse.wb.internal.core.model.order.ComponentOrderFirst;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.model.layout.FillLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.ImplicitLayoutCreationSupport;
import org.eclipse.wb.internal.swt.model.layout.ImplicitLayoutVariableSupport;
import org.eclipse.wb.internal.swt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.RowLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.absolute.AbsoluteLayoutCreationSupport;
import org.eclipse.wb.internal.swt.model.layout.absolute.AbsoluteLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.absolute.AbsoluteLayoutVariableSupport;
import org.eclipse.wb.internal.swt.model.layout.grid.GridLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.support.ControlSupport;
import org.eclipse.wb.tests.designer.core.PreferencesRepairer;
import org.eclipse.wb.tests.designer.rcp.BTestUtils;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Test for {@link CompositeInfo}.
 * 
 * @author lobas_av
 * @author scheglov_ke
 */
public class CompositeTest extends RcpModelTest {
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
  // Style
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that even though "style" is evaluated using {@link IThisMethodParameterEvaluator}, we
   * still can ask for value of "style" argument in {@link SuperConstructorInvocation}.
   */
  public void test_styleValue() throws Exception {
    CompositeInfo composite =
        parseComposite(
            "class Test extends Composite{",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "}");
    composite.refresh();
    // prepare "style" argument of "super()"
    Expression styleArgument;
    {
      TypeDeclaration typeDeclaration = JavaInfoUtils.getTypeDeclaration(composite);
      MethodDeclaration constructorDeclaration = typeDeclaration.getMethods()[0];
      List<Statement> statements = DomGenerics.statements(constructorDeclaration.getBody());
      SuperConstructorInvocation constructorInvocation =
          (SuperConstructorInvocation) statements.get(0);
      styleArgument = DomGenerics.arguments(constructorInvocation).get(1);
    }
    // "style" should have value
    assertEquals(SWT.NONE, JavaInfoEvaluationHelper.getValue(styleArgument));
    // "Style" property on top level
    assertNotNull(composite.getPropertyByTitle("Style"));
  }

  /**
   * Explicit value for "style" argument of {@link SuperMethodInvocation}.
   */
  public void test_styleValue2() throws Exception {
    CompositeInfo composite =
        parseComposite(
            "class Test extends Composite{",
            "  public Test(Composite parent, int style) {",
            "    super(parent, SWT.BORDER);",
            "  }",
            "}");
    composite.refresh();
    // "style" should have value SWT.BORDER
    Expression styleArgument = (Expression) m_lastEditor.getEnclosingNode("SWT.BORDER").getParent();
    assertEquals(SWT.BORDER, JavaInfoEvaluationHelper.getValue(styleArgument));
  }

  /**
   * In the past we detected "style" parameter just as 1-th parameter of {@link Composite}
   * constructor. However custom {@link Composite}-s may use 1-th parameter for different parameter,
   * not just for "style".
   */
  public void test_betterStyleParameterDetection() throws Exception {
    setFileContentSrc(
        "test/MyComposite.java",
        getTestSource(
            "// filler filler filler filler filler",
            "public class MyComposite extends Composite {",
            "  public MyComposite(Composite parent, String title) {",
            "    super(parent, SWT.NONE);",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    useStrictEvaluationMode(false);
    parseComposite(
        "public class Test extends Composite {",
        "  public Test(Composite parent, String title) {",
        "    super(parent, SWT.NONE);",
        "    new MyComposite(parent, title);",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Layout tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for implicit absolute layout.
   */
  public void test_AbsoluteLayout_1() throws Exception {
    CompositeInfo shellInfo =
        parseComposite(
            "class Test {",
            "  public static void main(String[] args) {",
            "    Shell shell = new Shell();",
            "    Button button = new Button(shell, SWT.NONE);",
            "    button.setBounds(10, 20, 50, 30);",
            "    button.setText('push');",
            "  }",
            "}");
    //
    List<JavaInfo> children = shellInfo.getChildrenJava();
    assertEquals(2, children.size());
    // layout is first: apply implicit layout
    assertSame(children.get(0), shellInfo.getLayout());
    assertInstanceOf(AbsoluteLayoutInfo.class, shellInfo.getLayout());
    // button is second
    assertInstanceOf(ControlInfo.class, children.get(1));
  }

  /**
   * Test for absolute layout set as "setLayout(null)".
   */
  public void test_AbsoluteLayout_2() throws Exception {
    CompositeInfo shellInfo =
        parseComposite(
            "class Test {",
            "  public static void main(String[] args) {",
            "    Shell shell = new Shell();",
            "    shell.setLayout(null);",
            "    Button button = new Button(shell, SWT.NONE);",
            "    button.setBounds(10, 20, 50, 30);",
            "    button.setText('push');",
            "  }",
            "}");
    //
    List<JavaInfo> children = shellInfo.getChildrenJava();
    assertEquals(2, children.size());
    // button is first: implicit layout not apply
    assertInstanceOf(ControlInfo.class, children.get(0));
    // layout is second: apply absolute layout direct
    LayoutInfo layout = shellInfo.getLayout();
    assertSame(children.get(1), layout);
    assertInstanceOf(AbsoluteLayoutInfo.class, layout);
    assertEquals("null", layout.getCreationSupport().toString());
    //
    VariableSupport variableSupport = layout.getVariableSupport();
    assertInstanceOf(AbsoluteLayoutVariableSupport.class, variableSupport);
    assertEquals("absolute", variableSupport.toString());
    // name
    assertFalse(variableSupport.hasName());
    try {
      variableSupport.getName();
      fail();
    } catch (IllegalStateException e) {
    }
    try {
      variableSupport.setName("foo");
      fail();
    } catch (IllegalStateException e) {
    }
    // title
    try {
      variableSupport.getTitle();
      fail();
    } catch (IllegalStateException e) {
    }
    // local -> field
    assertFalse(variableSupport.canConvertLocalToField());
    try {
      variableSupport.convertLocalToField();
      fail();
    } catch (IllegalStateException e) {
    }
    // field -> local
    assertFalse(variableSupport.canConvertFieldToLocal());
    try {
      variableSupport.convertFieldToLocal();
      fail();
    } catch (IllegalStateException e) {
    }
    // target
    try {
      variableSupport.getStatementTarget();
      fail();
    } catch (IllegalStateException e) {
    }
    // reference expression
    try {
      variableSupport.getReferenceExpression((NodeTarget) null);
      fail();
    } catch (IllegalStateException e) {
    }
    // access expression
    try {
      variableSupport.getAccessExpression((NodeTarget) null);
      fail();
    } catch (IllegalStateException e) {
    }
  }

  /**
   * Test for {@link CompositeInfo#hasLayout()} and {@link CompositeInfo#markNoLayout()}.
   */
  public void test_hasLayout() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "// filler filler filler",
            "public class Test extends Shell {",
            "  public Test() {",
            "  }",
            "}");
    // by default Shell's have Layout
    assertTrue(shell.hasLayout());
    assertThat(shell.getChildren(LayoutInfo.class)).hasSize(1);
    // but we can mark instance as not having Layout
    shell.markNoLayout();
    assertFalse(shell.hasLayout());
    assertThat(shell.getChildren(LayoutInfo.class)).isEmpty();
  }

  public void test_setLayout_1() throws Exception {
    CompositeInfo shellInfo =
        parseComposite(
            "class Test {",
            "  public static void main(String[] args) {",
            "    Shell shell = new Shell();",
            "  }",
            "}");
    // check layout
    LayoutInfo oldLayout = shellInfo.getLayout();
    assertInstanceOf(ImplicitLayoutCreationSupport.class, oldLayout.getCreationSupport());
    // set layout
    FillLayoutInfo newLayout = createJavaInfo("org.eclipse.swt.layout.FillLayout");
    shellInfo.setLayout(newLayout);
    //
    assertSame(newLayout, shellInfo.getLayout());
    assertEditor(
        "class Test {",
        "  public static void main(String[] args) {",
        "    Shell shell = new Shell();",
        "    shell.setLayout(new FillLayout(SWT.HORIZONTAL));",
        "  }",
        "}");
    // delete new layout
    newLayout.delete();
    assertEditor(
        "class Test {",
        "  public static void main(String[] args) {",
        "    Shell shell = new Shell();",
        "  }",
        "}");
  }

  public void test_setLayout_2() throws Exception {
    CompositeInfo shellInfo =
        parseComposite(
            "class Test {",
            "  public static void main(String[] args) {",
            "    Shell shell = new Shell();",
            "    shell.setLayout(null);",
            "  }",
            "}");
    shellInfo.refresh();
    // check layout
    LayoutInfo oldLayout = shellInfo.getLayout();
    assertNotInstanceOf(ImplicitLayoutCreationSupport.class, oldLayout.getCreationSupport());
    // set layout
    FillLayoutInfo newLayout = createJavaInfo("org.eclipse.swt.layout.FillLayout");
    shellInfo.setLayout(newLayout);
    //
    assertSame(newLayout, shellInfo.getLayout());
    assertEditor(
        "class Test {",
        "  public static void main(String[] args) {",
        "    Shell shell = new Shell();",
        "    shell.setLayout(new FillLayout(SWT.HORIZONTAL));",
        "  }",
        "}");
    // delete new layout
    Property property = shellInfo.getPropertyByTitle("Layout");
    assertTrue(property.isModified());
    property.setValue(Property.UNKNOWN_VALUE);
    assertEditor(
        "class Test {",
        "  public static void main(String[] args) {",
        "    Shell shell = new Shell();",
        "  }",
        "}");
  }

  public void test_setLayout_3() throws Exception {
    CompositeInfo shellInfo =
        parseComposite(
            "class Test {",
            "  public static void main(String[] args) {",
            "    Shell shell = new Shell();",
            "    shell.setLayout(new FillLayout());",
            "  }",
            "}");
    // check layout
    LayoutInfo layout = shellInfo.getLayout();
    assertInstanceOf(FillLayoutInfo.class, layout);
    // set grid layout
    GridLayoutInfo gridLayout = createJavaInfo("org.eclipse.swt.layout.GridLayout");
    shellInfo.setLayout(gridLayout);
    assertSame(gridLayout, shellInfo.getLayout());
    assertEditor(
        "class Test {",
        "  public static void main(String[] args) {",
        "    Shell shell = new Shell();",
        "    shell.setLayout(new GridLayout(1, false));",
        "  }",
        "}");
    // set row layout
    RowLayoutInfo rowLayout = createJavaInfo("org.eclipse.swt.layout.RowLayout");
    shellInfo.setLayout(rowLayout);
    assertSame(rowLayout, shellInfo.getLayout());
    assertEditor(
        "class Test {",
        "  public static void main(String[] args) {",
        "    Shell shell = new Shell();",
        "    shell.setLayout(new RowLayout(SWT.HORIZONTAL));",
        "  }",
        "}");
  }

  /**
   * Set {@link LayoutInfo} that asks about adding itself into {@link Block}.
   */
  public void test_setLayout_4() throws Exception {
    setFileContentSrc(
        "test/MyLayout.java",
        getTestSource(
            "public class MyLayout extends Layout {",
            "  protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {",
            "    return new Point(100, 100);",
            "  }",
            "  protected void layout(Composite composite, boolean flushCache) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyLayout.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='layout.setInBlock'>true</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // parse
    CompositeInfo shell =
        parseComposite(
            "// filler filler filler",
            "class Test extends Shell {",
            "  Test() {",
            "  }",
            "}");
    shell.refresh();
    // set new layout
    LayoutInfo newLayout = createJavaInfo("test.MyLayout");
    shell.setLayout(newLayout);
    assertSame(newLayout, shell.getLayout());
    assertEditor(
        "// filler filler filler",
        "class Test extends Shell {",
        "  Test() {",
        "    {",
        "      setLayout(new MyLayout());",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test that {@link LayoutInfo} uses correct {@link ComponentOrder}.
   */
  public void test_setLayout_order() throws Exception {
    CompositeInfo composite =
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
    // prepare layout
    LayoutInfo layout = createJavaInfo("org.eclipse.swt.layout.RowLayout");
    assertSame(ComponentOrderFirst.INSTANCE, layout.getDescription().getOrder());
    // set layout
    composite.setLayout(layout);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new RowLayout(SWT.HORIZONTAL));",
        "    new Button(this, SWT.NONE);",
        "  }",
        "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/new Button(this, SWT.NONE)/ /setLayout(new RowLayout(SWT.HORIZONTAL))/}",
        "  {new: org.eclipse.swt.layout.RowLayout} {empty} {/setLayout(new RowLayout(SWT.HORIZONTAL))/}",
        "  {new: org.eclipse.swt.widgets.Button} {empty} {/new Button(this, SWT.NONE)/}",
        "    {virtual-layout_data: org.eclipse.swt.layout.RowData} {virtual-layout-data} {}");
  }

  /**
   * If {@link Composite} marked as "no layout", then {@link Composite#setLayout(Layout)} should not
   * be association.
   */
  public void test_setLayout_ifMarkedAsNoLayout() throws Exception {
    setFileContentSrc(
        "test/MyComposite.java",
        getTestSource(
            "public class MyComposite extends Composite {",
            "  public MyComposite(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyComposite.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='layout.has'>false</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // parse
    CompositeInfo composite =
        parseComposite(
            "// filler filler filler",
            "public class Test extends MyComposite {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "    setLayout(new RowLayout());",
            "  }",
            "}");
    assertFalse(composite.hasLayout());
    assertHierarchy("{this: test.MyComposite} {this} {/setLayout(new RowLayout())/}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implicit layouts
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_ImplicitLayout_1() throws Exception {
    setFileContentSrc(
        "test/ImplicitComposite.java",
        getTestSource(
            "public class ImplicitComposite extends Composite {",
            "  public ImplicitComposite(Composite parent, int style) {",
            "    super(parent, style);",
            "    setLayout(new GridLayout(2, false));",
            "  }",
            "}"));
    waitForAutoBuild();
    CompositeInfo composite =
        parseComposite(
            "public class Test extends ImplicitComposite {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "}");
    composite.refresh();
    //
    LayoutInfo layout = composite.getLayout();
    //
    CreationSupport creationSupport = layout.getCreationSupport();
    assertInstanceOf(ImplicitLayoutCreationSupport.class, creationSupport);
    assertTrue(creationSupport.canDelete());
    assertEquals("implicit-layout: org.eclipse.swt.layout.GridLayout", creationSupport.toString());
    //
    VariableSupport variableSupport = layout.getVariableSupport();
    assertInstanceOf(ImplicitLayoutVariableSupport.class, variableSupport);
    assertTrue(variableSupport.isDefault());
    assertEquals("implicit-layout", variableSupport.toString());
    assertEquals("(implicit layout)", variableSupport.getTitle());
    // Materializing
    Property property = layout.getPropertyByTitle("marginWidth");
    assertNotNull(property);
    assertEquals(5, property.getValue());
    property.setValue(10);
    assertEquals(10, property.getValue());
    //
    assertEditor(
        "public class Test extends ImplicitComposite {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    GridLayout gridLayout = (GridLayout) getLayout();",
        "    gridLayout.marginWidth = 10;",
        "  }",
        "}");
    assertNotInstanceOf(ImplicitLayoutVariableSupport.class, layout.getVariableSupport());
    //
    composite.getLayout().delete();
    assertInstanceOf(ImplicitLayoutVariableSupport.class, layout.getVariableSupport());
    assertEditor(
        "public class Test extends ImplicitComposite {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "  }",
        "}");
  }

  public void test_ImplicitLayout_2() throws Exception {
    setFileContentSrc(
        "test/ImplicitComposite.java",
        getTestSource(
            "public class ImplicitComposite extends Composite {",
            "  public ImplicitComposite(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "}"));
    waitForAutoBuild();
    CompositeInfo composite =
        parseComposite(
            "public class Test extends ImplicitComposite {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "}");
    composite.refresh();
    //
    LayoutInfo layout = composite.getLayout();
    //
    CreationSupport creationSupport = layout.getCreationSupport();
    assertInstanceOf(ImplicitLayoutCreationSupport.class, creationSupport);
    assertTrue(creationSupport.canDelete());
    assertEquals("implicit-layout: absolute", creationSupport.toString());
    //
    VariableSupport variableSupport = layout.getVariableSupport();
    assertInstanceOf(ImplicitLayoutVariableSupport.class, variableSupport);
    assertTrue(variableSupport.isDefault());
    assertEquals("implicit-layout", variableSupport.toString());
    assertEquals("(implicit layout)", variableSupport.getTitle());
  }

  public void test_ImplicitLayout_3() throws Exception {
    setFileContentSrc(
        "test/ImplicitComposite.java",
        getTestSource(
            "public class ImplicitComposite extends Composite {",
            "  public ImplicitComposite(Composite parent, int style) {",
            "    super(parent, style);",
            "    setLayout(new GridLayout(2, false));",
            "  }",
            "}"));
    waitForAutoBuild();
    CompositeInfo composite =
        parseComposite(
            "public class Test extends ImplicitComposite {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "}");
    // check layout
    LayoutInfo implicitLayout = composite.getLayout();
    assertInstanceOf(GridLayoutInfo.class, implicitLayout);
    assertInstanceOf(ImplicitLayoutCreationSupport.class, implicitLayout.getCreationSupport());
    // set grid layout
    GridLayoutInfo gridLayout = createJavaInfo("org.eclipse.swt.layout.GridLayout");
    composite.setLayout(gridLayout);
    assertSame(gridLayout, composite.getLayout());
    assertEditor(
        "public class Test extends ImplicitComposite {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setLayout(new GridLayout(1, false));",
        "  }",
        "}");
    // remove layout
    gridLayout.delete();
    assertSame(implicitLayout, composite.getLayout());
    assertEditor(
        "public class Test extends ImplicitComposite {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "  }",
        "}");
  }

  public void test_ImplicitLayout_overset() throws Exception {
    setFileContentSrc(
        "test/ImplicitComposite.java",
        getTestSource(
            "public class ImplicitComposite extends Composite {",
            "  public ImplicitComposite(Composite parent, int style) {",
            "    super(parent, style);",
            "    setLayout(new GridLayout(2, false));",
            "  }",
            "}"));
    waitForAutoBuild();
    CompositeInfo composite =
        parseComposite(
            "public class Test extends ImplicitComposite {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "    setLayout(null);",
            "  }",
            "}");
    composite.refresh();
    // we have absolute layout
    {
      LayoutInfo layout = composite.getLayout();
      assertInstanceOf(AbsoluteLayoutInfo.class, layout);
      assertInstanceOf(AbsoluteLayoutCreationSupport.class, layout.getCreationSupport());
      assertInstanceOf(AbsoluteLayoutVariableSupport.class, layout.getVariableSupport());
      assertInstanceOf(InvocationChildAssociation.class, layout.getAssociation());
      // delete absolute layout
      assertTrue(layout.canDelete());
      layout.delete();
    }
    // check that current layout is implicit
    {
      LayoutInfo layout = composite.getLayout();
      assertNotInstanceOf(AbsoluteLayoutInfo.class, layout);
      assertInstanceOf(ImplicitLayoutCreationSupport.class, layout.getCreationSupport());
      assertInstanceOf(ImplicitLayoutVariableSupport.class, layout.getVariableSupport());
      assertInstanceOf(ImplicitObjectAssociation.class, layout.getAssociation());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Placeholder
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * If {@link Composite} was replaced with placeholder, it should not have layout.
   */
  public void test_placeholder_hasLayout() throws Exception {
    setFileContentSrc(
        "test/MyComposite.java",
        getTestSource(
            "public class MyComposite extends Composite {",
            "  public MyComposite(Composite parent, int style) {",
            "    super(parent, style);",
            "    throw new IllegalStateException('actual');",
            "  }",
            "}"));
    waitForAutoBuild();
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    new MyComposite(this, SWT.NONE);",
            "  }",
            "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/new MyComposite(this, SWT.NONE)/}",
        "  {implicit-layout: absolute} {implicit-layout} {}",
        "  {new: test.MyComposite} {empty} {/new MyComposite(this, SWT.NONE)/}");
    shell.refresh();
    CompositeInfo composite = (CompositeInfo) shell.getChildrenControls().get(0);
    // no Layout for placeholder
    assertFalse(composite.hasLayout());
  }

  /**
   * If {@link Composite} was replaced with placeholder, we still can create children on it.
   */
  public void test_placeholder_andChildren() throws Exception {
    setFileContentSrc(
        "test/MyComposite.java",
        getTestSource(
            "public class MyComposite extends Composite {",
            "  public MyComposite(Composite parent, int style) {",
            "    super(parent, style);",
            "    throw new IllegalStateException('actual');",
            "  }",
            "}"));
    waitForAutoBuild();
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Composite composite = new MyComposite(this, SWT.NONE);",
            "    new Button(composite, SWT.NONE);",
            "  }",
            "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/new MyComposite(this, SWT.NONE)/}",
        "  {implicit-layout: absolute} {implicit-layout} {}",
        "  {new: test.MyComposite} {local-unique: composite} {/new MyComposite(this, SWT.NONE)/ /new Button(composite, SWT.NONE)/}",
        "    {new: org.eclipse.swt.widgets.Button} {empty} {/new Button(composite, SWT.NONE)/}");
    shell.refresh();
    // we still have some objects for "composite" (placeholder) and its children
    CompositeInfo composite = (CompositeInfo) shell.getChildrenControls().get(0);
    ControlInfo button = composite.getChildrenControls().get(0);
    assertNotNull(composite.getObject());
    assertNotNull(button.getObject());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Other
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link IPreferenceConstants#P_LAYOUT_DEFAULT}, i.e. installation for default layout.
   */
  public void test_setDefaultLayout() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "  }",
            "}");
    FillLayoutInfo fillLayout = (FillLayoutInfo) shell.getLayout();
    // prepare preferences
    PreferencesRepairer preferencesRepairer;
    {
      IPreferenceStore preferences = shell.getDescription().getToolkit().getPreferences();
      preferencesRepairer = new PreferencesRepairer(preferences);
      // no default layout by default
      assertTrue(StringUtils.isEmpty(preferences.getString(IPreferenceConstants.P_LAYOUT_DEFAULT)));
      // use GridLayout as default
      preferencesRepairer.setValue(IPreferenceConstants.P_LAYOUT_DEFAULT, "gridLayout");
    }
    // add new Composite
    CompositeInfo composite;
    try {
      composite = (CompositeInfo) BTestUtils.createControl("org.eclipse.swt.widgets.Composite");
      composite.putArbitraryValue(JavaInfo.FLAG_MANUAL_COMPONENT, Boolean.TRUE);
      fillLayout.command_CREATE(composite, null);
      // execute pending async's
      waitEventLoop(1);
    } finally {
      preferencesRepairer.restore();
    }
    // check result
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    {",
        "      Composite composite = new Composite(this, SWT.NONE);",
        "      composite.setLayout(new GridLayout(1, false));",
        "    }",
        "  }",
        "}");
    assertNotSame(fillLayout, composite.getLayout());
  }

  /**
   * Test for {@link IPreferenceConstants#P_LAYOUT_OF_PARENT}, that enables layout inheritance.
   */
  public void test_inheritParentLayout() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "  }",
            "}");
    FillLayoutInfo fillLayout = (FillLayoutInfo) shell.getLayout();
    // prepare preferences
    PreferencesRepairer preferencesRepairer;
    {
      IPreferenceStore preferences = shell.getDescription().getToolkit().getPreferences();
      preferencesRepairer = new PreferencesRepairer(preferences);
      // no inheritance by default
      assertFalse(preferences.getBoolean(IPreferenceConstants.P_LAYOUT_OF_PARENT));
      // enable inheritance
      preferencesRepairer.setValue(IPreferenceConstants.P_LAYOUT_OF_PARENT, true);
    }
    // add new Composite
    CompositeInfo composite;
    try {
      composite = (CompositeInfo) BTestUtils.createControl("org.eclipse.swt.widgets.Composite");
      composite.putArbitraryValue(JavaInfo.FLAG_MANUAL_COMPONENT, Boolean.TRUE);
      fillLayout.command_CREATE(composite, null);
      // execute pending async's
      waitEventLoop(1);
    } finally {
      preferencesRepairer.restore();
    }
    // check result
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    {",
        "      Composite composite = new Composite(this, SWT.NONE);",
        "      composite.setLayout(new FillLayout(SWT.HORIZONTAL));",
        "    }",
        "  }",
        "}");
    assertNotSame(fillLayout, composite.getLayout());
  }

  /**
   * No inheritance of implicit layout.
   */
  public void test_inheritParentLayout_nullImplicit() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "// filler filler filler filler filler",
            "public class Test extends Shell {",
            "  public Test() {",
            "  }",
            "}");
    // prepare preferences
    PreferencesRepairer preferencesRepairer;
    {
      IPreferenceStore preferences = shell.getDescription().getToolkit().getPreferences();
      preferencesRepairer = new PreferencesRepairer(preferences);
      // no inheritance by default
      assertFalse(preferences.getBoolean(IPreferenceConstants.P_LAYOUT_OF_PARENT));
      // enable inheritance
      preferencesRepairer.setValue(IPreferenceConstants.P_LAYOUT_OF_PARENT, true);
    }
    // add new Composite
    CompositeInfo composite;
    try {
      composite = createJavaInfo("org.eclipse.swt.widgets.Composite");
      shell.getLayout().command_CREATE(composite, null);
      // execute pending async's
      waitEventLoop(1);
    } finally {
      preferencesRepairer.restore();
    }
    // check result
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends Shell {",
        "  public Test() {",
        "    {",
        "      Composite composite = new Composite(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
  }

  /**
   * No inheritance of implicit layout.
   */
  public void test_inheritParentLayout_nullExplicit() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "// filler filler filler filler filler",
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(null);",
            "  }",
            "}");
    // prepare preferences
    PreferencesRepairer preferencesRepairer;
    {
      IPreferenceStore preferences = shell.getDescription().getToolkit().getPreferences();
      preferencesRepairer = new PreferencesRepairer(preferences);
      // no inheritance by default
      assertFalse(preferences.getBoolean(IPreferenceConstants.P_LAYOUT_OF_PARENT));
      // enable inheritance
      preferencesRepairer.setValue(IPreferenceConstants.P_LAYOUT_OF_PARENT, true);
    }
    // add new Composite
    CompositeInfo composite;
    try {
      composite = createJavaInfo("org.eclipse.swt.widgets.Composite");
      shell.getLayout().command_CREATE(composite, null);
      // execute pending async's
      waitEventLoop(1);
    } finally {
      preferencesRepairer.restore();
    }
    // check result
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(null);",
        "    {",
        "      Composite composite = new Composite(this, SWT.NONE);",
        "      composite.setLayout(null);",
        "    }",
        "  }",
        "}");
  }

  /**
   * If {@link Composite} overrides "checkSubclass", we intercept its invocation and try to prepare
   * exposed children. Unfortunately, "checkSubclass" is invoked before assigning {@link Display},
   * so SWT thinks that widget is disposed.
   * <p>
   * So, we should ignore all invocations from standard SWT constructors.
   */
  public void test_checkSubclass_andBinaryExecutionFlow() throws Exception {
    setFileContentSrc(
        "test/MyComposite.java",
        getTestSource(
            "public class MyComposite extends Composite {",
            "  public MyComposite(Composite parent, int style) {",
            "    super(parent, style);",
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
            "  protected void checkSubclass() {",
            "  }",
            "}");
    composite.refresh();
    assertNoErrors(composite);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Set Layout" action in context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that {@link CompositeInfo} contributes "Set layout" sub-menu in context menu.
   */
  public void test_setLayoutMenu_1() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "// filler filler filler",
            "public class Test extends Shell {",
            "  public Test() {",
            "  }",
            "}");
    assertTrue(shell.hasLayout());
    // prepare "Set Layout" menu manager
    IMenuManager layoutManager;
    {
      IMenuManager menuManager = getContextMenu(shell);
      layoutManager = findChildMenuManager(menuManager, "Set layout");
      assertNotNull(layoutManager);
    }
    // check for existing actions
    assertNotNull(findChildAction(layoutManager, "GridLayout"));
    assertNotNull(findChildAction(layoutManager, "FillLayout"));
    assertNotNull(findChildAction(layoutManager, "RowLayout"));
    // use one of the actions to set new layout
    {
      IAction action = findChildAction(layoutManager, "RowLayout");
      action.run();
      assertEditor(
          "// filler filler filler",
          "public class Test extends Shell {",
          "  public Test() {",
          "    setLayout(new RowLayout(SWT.HORIZONTAL));",
          "  }",
          "}");
    }
    // set "absolute" layout
    {
      IAction action = findChildAction(layoutManager, "Absolute layout");
      action.run();
      assertEditor(
          "// filler filler filler",
          "public class Test extends Shell {",
          "  public Test() {",
          "    setLayout(null);",
          "  }",
          "}");
    }
  }

  /**
   * No "Set Layout" sub-menu if Composite has no layout.
   */
  public void test_setLayoutMenu_2() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    new Table(this, SWT.BORDER);",
            "  }",
            "}");
    CompositeInfo table = (CompositeInfo) shell.getChildrenControls().get(0);
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
  // "Set minimal size, as after pack()" action in context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_contextMenu_setMinimalSize_forRoot() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('Some long long long long long long text');",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // run "Set minimal size" action
    IMenuManager contextMenu = getContextMenu(shell);
    IAction action = findChildAction(contextMenu, "Set minimal size, as after pack()");
    assertNotNull(action);
    action.run();
    // validate
    assertEquals(shell.getPreferredSize(), shell.getBounds().getSize());
  }

  public void test_contextMenu_setMinimalSize_noActionForInner() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    {",
            "      Composite composite = new Composite(this, SWT.NONE);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    CompositeInfo composite = (CompositeInfo) shell.getChildrenControls().get(0);
    // not root, so no "Set minimal size" action
    IMenuManager contextMenu = getContextMenu(composite);
    IAction action = findChildAction(contextMenu, "Set minimal size, as after pack()");
    assertNull(action);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Remove setSize()" action in context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_contextMenu_removeSize() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "// filler filler filler",
            "public class Test extends Shell {",
            "  public Test() {",
            "    setSize(400, 500);",
            "  }",
            "}");
    shell.refresh();
    // run "Set minimal size" action
    IMenuManager contextMenu = getContextMenu(shell);
    IAction action = findChildAction(contextMenu, "Remove setSize()");
    assertNotNull(action);
    action.run();
    // validate
    assertEditor(
        "// filler filler filler",
        "public class Test extends Shell {",
        "  public Test() {",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Association
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Check for {@link ControlInfo} association using constructor.
   */
  public void test_getAssociation_Control() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    Button button = new Button(this, SWT.NONE);",
            "    button.setText('');",
            "  }",
            "}");
    ControlInfo button = shell.getChildrenControls().get(0);
    // check association
    ConstructorCreationSupport support = (ConstructorCreationSupport) button.getCreationSupport();
    ConstructorAssociation association = (ConstructorAssociation) button.getAssociation();
    assertSame(support.getCreation(), association.getCreation());
  }

  /**
   * Test for {@link LayoutInfo} association using
   * {@link Composite#setLayout(org.eclipse.swt.widgets.Layout)}.
   */
  public void test_getAssociation_setLayout() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    Button button = new Button(this, SWT.NONE);",
            "    button.setText('');",
            "  }",
            "}");
    LayoutInfo layout = shell.getLayout();
    // check association
    InvocationChildAssociation association = (InvocationChildAssociation) layout.getAssociation();
    assertSame(
        shell.getMethodInvocation("setLayout(org.eclipse.swt.widgets.Layout)"),
        association.getInvocation());
  }

  public void test_createInMethod() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    createButton(this).setEnabled(false);",
            "  }",
            "  private Button createButton(Composite parent) {",
            "    Button button = new Button(parent, SWT.NONE);",
            "    button.setText('abc');",
            "    return button;",
            "  }",
            "}");
    ControlInfo buttonInfo = shell.getChildrenControls().get(0);
    shell.refresh();
    // check properties values
    Object button = buttonInfo.getObject();
    assertEquals("abc", ReflectionUtils.invokeMethod(button, "getText()"));
    assertEquals(Boolean.FALSE, ReflectionUtils.invokeMethod(button, "getEnabled()"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Target
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_target_Shell() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test {",
            "  public static void main(String[] args) {",
            "    Display display = Display.getDefault();",
            "    Shell shell = new Shell();",
            "    shell.setLayout(new GridLayout());",
            "    shell.open();",
            "    shell.layout();",
            "    while (!shell.isDisposed()) {",
            "      if (!display.readAndDispatch()) {",
            "        display.sleep();",
            "      }",
            "    }",
            "  }",
            "}");
    StatementTarget target = JavaInfoUtils.getTarget(shell, null);
    assertNull(target.getBlock());
    assertEquals(
        "shell.setLayout(new GridLayout());",
        m_lastEditor.getSource(target.getStatement()));
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
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    new Composite(this, SWT.NONE);",
            "    new Composite(this, SWT.BORDER);",
            "    new Table(this, SWT.NONE);",
            "  }",
            "}");
    shell.refresh();
    // prepare Composite's
    CompositeInfo composite_noBorder = (CompositeInfo) shell.getChildrenControls().get(0);
    CompositeInfo composite_withBorder = (CompositeInfo) shell.getChildrenControls().get(1);
    CompositeInfo table = (CompositeInfo) shell.getChildrenControls().get(2);
    // do checks
    assertTrue(composite_noBorder.shouldDrawDotsBorder());
    assertFalse(composite_withBorder.shouldDrawDotsBorder());
    assertFalse(table.shouldDrawDotsBorder());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Use separate Shell as "parent"
  //
  ////////////////////////////////////////////////////////////////////////////
  private static Object m_shouldNotBeSharedShell;

  /**
   * We can not use same {@link Shell} for more than one {@link Composite}.
   * <p>
   * First part of test, remembers used {@link Shell}.
   */
  public void test_useSeparateShell_1() throws Exception {
    CompositeInfo composite =
        parseComposite(
            "class Test extends Composite{",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "}");
    composite.refresh();
    //
    Object shell_1 = ControlSupport.getShell(composite.getObject());
    m_shouldNotBeSharedShell = shell_1;
  }

  /**
   * We can not use same {@link Shell} for more than one {@link Composite}.
   */
  public void test_useSeparateShell_2() throws Exception {
    CompositeInfo composite =
        parseComposite(
            "class Test extends Composite{",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "}");
    composite.refresh();
    // separate Shell expected
    Object shell_2 = ControlSupport.getShell(composite.getObject());
    assertNotSame(shell_2, m_shouldNotBeSharedShell);
    // and previous Shell should be disposed
    assertTrue(ControlSupport.isDisposed(m_shouldNotBeSharedShell));
    m_shouldNotBeSharedShell = null;
  }
}