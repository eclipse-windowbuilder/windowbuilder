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
package org.eclipse.wb.tests.designer.rcp.model.forms;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.creation.factory.InstanceFactoryInfo;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.TableWrapLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.FillLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.BTestUtils;

import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Test for <code>FormToolkit</code>.
 * 
 * @author scheglov_ke
 */
public class FormToolkitTest extends AbstractFormsTest {
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
            "  private final FormToolkit m_toolkit = new FormToolkit(Display.getDefault());",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "  }",
            "}");
    shell.refresh();
    InstanceFactoryInfo toolkit = getToolkit();
    ComponentDescription description = toolkit.getDescription();
    assertEquals(
        "org.eclipse.ui.forms.widgets.FormToolkit",
        description.getComponentClass().getName());
    // check for adapt() methods
    {
      assertNotNull(description.getMethod("adapt(org.eclipse.swt.widgets.Composite)"));
      assertNotNull(description.getMethod("adapt(org.eclipse.swt.widgets.Control,boolean,boolean)"));
    }
  }

  /**
   * We should support creating of {@link FormToolkit} using static factory, for example to allow
   * users override {@link FormColors}.
   */
  public void test_createFormToolkit_usingStaticFactory() throws Exception {
    m_waitForAutoBuild = true;
    parseComposite(
        "public class Test extends Shell {",
        "  private final FormToolkit m_toolkit = createFormToolkit();",
        "  public Test() {",
        "  }",
        "  /**",
        "  * @wbp.factory",
        "  */",
        "  private static FormToolkit createFormToolkit() {",
        "    return new FormToolkit(Display.getDefault());",
        "  }",
        "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {}",
        "  {implicit-layout: absolute} {implicit-layout} {}",
        "  {instance factory container}",
        "    {static factory: test.Test createFormToolkit()} {field-initializer: m_toolkit} {/createFormToolkit()/}");
  }

  /**
   * We should support creating of {@link FormToolkit} in {@link Initializer}.
   */
  public void test_createFormToolkit_inInitializer_instance() throws Exception {
    parseComposite(
        "public class Test extends Shell {",
        "  private FormToolkit m_toolkit;",
        "  {",
        "    m_toolkit = new FormToolkit(Display.getDefault());",
        "  }",
        "  public Test() {",
        "  }",
        "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {}",
        "  {implicit-layout: absolute} {implicit-layout} {}",
        "  {instance factory container}",
        "    {new: org.eclipse.ui.forms.widgets.FormToolkit} {field-unique: m_toolkit} {/new FormToolkit(Display.getDefault())/}");
  }

  /**
   * We should support creating of {@link FormToolkit} in {@link Initializer}.
   */
  public void test_createFormToolkit_inInitializer_static() throws Exception {
    parseComposite(
        "public class Test extends Shell {",
        "  private static FormToolkit m_toolkit;",
        "  static {",
        "    m_toolkit = new FormToolkit(Display.getDefault());",
        "  }",
        "  public Test() {",
        "  }",
        "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {}",
        "  {implicit-layout: absolute} {implicit-layout} {}",
        "  {instance factory container}",
        "    {new: org.eclipse.ui.forms.widgets.FormToolkit} {field-unique: m_toolkit} {/new FormToolkit(Display.getDefault())/}");
  }

  public void test_createText() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  private final FormToolkit m_toolkit = new FormToolkit(Display.getDefault());",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    Text text = m_toolkit.createText(this, 'text', SWT.NONE);",
            "  }",
            "}");
    shell.refresh();
    // check for Text widget
    ControlInfo text = shell.getChildrenControls().get(0);
    // "Style" property should be copied from "Factory" to top level
    assertNotNull(text.getPropertyByTitle("Style"));
  }

  public void test_createTable_separateStatement() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  private final FormToolkit m_toolkit = new FormToolkit(Display.getDefault());",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    m_toolkit.createTable(this, SWT.NONE);",
            "  }",
            "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/setLayout(new FillLayout())/ /m_toolkit.createTable(this, SWT.NONE)/}",
        "  {new: org.eclipse.swt.layout.FillLayout} {empty} {/setLayout(new FillLayout())/}",
        "  {instance factory: {field-initializer: m_toolkit} createTable(org.eclipse.swt.widgets.Composite,int)} {empty} {/m_toolkit.createTable(this, SWT.NONE)/}",
        "  {instance factory container}",
        "    {new: org.eclipse.ui.forms.widgets.FormToolkit} {field-initializer: m_toolkit} {/new FormToolkit(Display.getDefault())/ /m_toolkit.createTable(this, SWT.NONE)/}");
    // refresh()
    shell.refresh();
    assertNoErrors(shell);
  }

  public void test_createLabel_separateStatement_GridLayout() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  private final FormToolkit m_toolkit = new FormToolkit(Display.getDefault());",
            "  public Test() {",
            "    setLayout(new GridLayout());",
            "    m_toolkit.createLabel(this, 'Some text', SWT.NONE);",
            "  }",
            "}");
    shell.refresh();
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/setLayout(new GridLayout())/ /m_toolkit.createLabel(this, 'Some text', SWT.NONE)/}",
        "  {new: org.eclipse.swt.layout.GridLayout} {empty} {/setLayout(new GridLayout())/}",
        "  {instance factory: {field-initializer: m_toolkit} createLabel(org.eclipse.swt.widgets.Composite,java.lang.String,int)} {empty} {/m_toolkit.createLabel(this, 'Some text', SWT.NONE)/}",
        "    {virtual-layout_data: org.eclipse.swt.layout.GridData} {virtual-layout-data} {}",
        "  {instance factory container}",
        "    {new: org.eclipse.ui.forms.widgets.FormToolkit} {field-initializer: m_toolkit} {/new FormToolkit(Display.getDefault())/ /m_toolkit.createLabel(this, 'Some text', SWT.NONE)/}");
    ControlInfo label = shell.getChildrenControls().get(0);
    // check that "label" is visible
    IObjectPresentation presentation = shell.getPresentation();
    {
      List<ObjectInfo> presentationChildren = presentation.getChildrenTree();
      assertThat(presentationChildren).contains(label);
    }
  }

  /**
   * We want to allow passing {@link FormToolkit} as parameter in constructor.
   */
  public void test_FormToolkit_asConstructorParameter() throws Exception {
    CompositeInfo composite =
        parseComposite(
            "public class Test extends Composite {",
            "  public Test(Composite parent, int style, FormToolkit toolkit) {",
            "    super(parent, style);",
            "    toolkit.createTable(this, SWT.NONE);",
            "  }",
            "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Composite} {this} {/toolkit.createTable(this, SWT.NONE)/}",
        "  {implicit-layout: absolute} {implicit-layout} {}",
        "  {instance factory: {toolkit} createTable(org.eclipse.swt.widgets.Composite,int)} {empty} {/toolkit.createTable(this, SWT.NONE)/}",
        "  {instance factory container}",
        "    {parameter} {toolkit} {/toolkit.createTable(this, SWT.NONE)/}");
    // refresh()
    composite.refresh();
    assertNoErrors(composite);
  }

  /**
   * We want to allow passing {@link FormToolkit} as parameter in constructor.
   * <p>
   * Same as {@link #test_FormToolkit_asConstructorParameter()}, but here we don't use
   * {@link FormToolkit} parameter and this caused problem.
   */
  public void test_FormToolkit_asConstructorParameter2() throws Exception {
    CompositeInfo composite =
        parseComposite(
            "public class Test extends Composite {",
            "  public Test(Composite parent, int style, FormToolkit toolkit) {",
            "    super(parent, style);",
            "  }",
            "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Composite} {this} {}",
        "  {implicit-layout: absolute} {implicit-layout} {}",
        "  {instance factory container}",
        "    {parameter} {toolkit} {}");
    // refresh()
    composite.refresh();
    assertNoErrors(composite);
  }

  /**
   * Sometimes we can not support UI as is, and want to allow user design just single method. And we
   * want to allow passing {@link FormToolkit} as parameter of this method.
   */
  public void test_FormToolkit_asEntryPoint_methodParameter() throws Exception {
    CompositeInfo composite =
        parseComposite(
            "public class Test {",
            "  /**",
            "  * @wbp.parser.entryPoint",
            "  */",
            "  public void createClient(Section section, FormToolkit toolkit) {",
            "    Composite container = toolkit.createComposite(section);",
            "  }",
            "}");
    assertHierarchy(
        "{instance factory: {toolkit} createComposite(org.eclipse.swt.widgets.Composite)} {local-unique: container} {/toolkit.createComposite(section)/}",
        "  {implicit-layout: absolute} {implicit-layout} {}",
        "  {instance factory container}",
        "    {parameter} {toolkit} {/toolkit.createComposite(section)/}");
    // refresh()
    composite.refresh();
    assertNoErrors(composite);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // paintBordersFor() and adapt()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_paintBordersFor_whenDropNewComposite() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  private final FormToolkit m_toolkit = new FormToolkit(Display.getDefault());",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "  }",
            "}");
    shell.refresh();
    //
    ControlInfo newComposite = createJavaInfo("org.eclipse.swt.widgets.Composite");
    ((FillLayoutInfo) shell.getLayout()).command_CREATE(newComposite, null);
    assertEditor(
        "public class Test extends Shell {",
        "  private final FormToolkit m_toolkit = new FormToolkit(Display.getDefault());",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    {",
        "      Composite composite = new Composite(this, SWT.NONE);",
        "      m_toolkit.adapt(composite);",
        "      m_toolkit.paintBordersFor(composite);",
        "    }",
        "  }",
        "}");
  }

  public void test_adapt_whenDropNonFormControl() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  private final FormToolkit m_toolkit = new FormToolkit(Display.getDefault());",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "  }",
            "}");
    shell.refresh();
    //
    ControlInfo newButton = BTestUtils.createButton();
    ((FillLayoutInfo) shell.getLayout()).command_CREATE(newButton, null);
    assertEditor(
        "public class Test extends Shell {",
        "  private final FormToolkit m_toolkit = new FormToolkit(Display.getDefault());",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      m_toolkit.adapt(button, true, true);",
        "    }",
        "  }",
        "}");
  }

  public void test_adapt_whenDropNonFormControl_ignoreFillers() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  private final FormToolkit m_toolkit = new FormToolkit(Display.getDefault());",
            "  public Test() {",
            "    {",
            "      TableWrapLayout layout = new TableWrapLayout();",
            "      setLayout(layout);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
    //
    ControlInfo newButton = BTestUtils.createButton();
    layout.command_CREATE(newButton, 0, false, 1, false);
    assertEditor(
        "public class Test extends Shell {",
        "  private final FormToolkit m_toolkit = new FormToolkit(Display.getDefault());",
        "  public Test() {",
        "    {",
        "      TableWrapLayout layout = new TableWrapLayout();",
        "      setLayout(layout);",
        "    }",
        "    new Label(this, SWT.NONE);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      m_toolkit.adapt(button, true, true);",
        "    }",
        "  }",
        "}");
  }
}