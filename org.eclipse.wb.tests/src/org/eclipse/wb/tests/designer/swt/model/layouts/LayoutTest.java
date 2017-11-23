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

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.rcp.RcpToolkitDescription;
import org.eclipse.wb.internal.swt.IExceptionConstants;
import org.eclipse.wb.internal.swt.model.layout.FillLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.LayoutDataInfo;
import org.eclipse.wb.internal.swt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.RowDataInfo;
import org.eclipse.wb.internal.swt.model.layout.RowLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.grid.GridLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.BTestUtils;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author lobas_av
 */
public class LayoutTest extends RcpModelTest {
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
  public void test_parse_setLayout_single() throws Exception {
    parseComposite(
        "class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "  }",
        "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/setLayout(new RowLayout())/}",
        "  {new: org.eclipse.swt.layout.RowLayout} {empty} {/setLayout(new RowLayout())/}");
  }

  public void test_parse_setLayout_double() throws Exception {
    try {
      parseComposite(
          "class Test extends Shell {",
          "  public Test() {",
          "    setLayout(new RowLayout());",
          "    setLayout(new FillLayout());",
          "  }",
          "}");
    } catch (Throwable e_) {
      DesignerException e = DesignerExceptionUtils.getDesignerException(e_);
      assertEquals(IExceptionConstants.DOUBLE_SET_LAYOUT, e.getCode());
    }
  }

  /**
   * Test for {@link LayoutInfo#isActive()}.
   */
  public void test_isActive() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "// filler filler filler",
            "public class Test extends Shell {",
            "  public Test() {",
            "  }",
            "}");
    // prepare implicit layout
    LayoutInfo oldLayout = shell.getLayout();
    assertTrue(oldLayout.isActive());
    // prepare new Layout
    LayoutInfo newLayout = createJavaInfo("org.eclipse.swt.layout.FillLayout");
    // not active before set
    assertFalse(newLayout.isActive());
    // set new Layout
    shell.setLayout(newLayout);
    assertFalse(oldLayout.isActive());
    assertTrue(newLayout.isActive());
    // remove new layout
    newLayout.delete();
    assertTrue(oldLayout.isActive());
    assertFalse(newLayout.isActive());
  }

  public void test_changeLayout() throws Exception {
    CompositeInfo shellInfo =
        parseComposite(
            "class Test {",
            "  public static void main(String[] args) {",
            "    Shell shell = new Shell();",
            "    RowLayout layout = new RowLayout();",
            "    shell.setLayout(layout);",
            "    //",
            "    Button button = new Button(shell, SWT.NONE);",
            "    RowData data = new RowData();",
            "    data.width = 50;",
            "    data.height = 40;",
            "    button.setLayoutData(data);",
            "  }",
            "}");
    // check layout
    RowLayoutInfo layoutInfo = (RowLayoutInfo) shellInfo.getLayout();
    assertNotNull(layoutInfo);
    // check layout data
    ControlInfo buttonInfo = shellInfo.getChildrenControls().get(0);
    RowDataInfo dataInfo = (RowDataInfo) buttonInfo.getChildrenJava().get(0);
    assertNotNull(dataInfo);
    // change layout;
    {
      FillLayoutInfo newLayout = createJavaInfo("org.eclipse.swt.layout.FillLayout");
      shellInfo.setLayout(newLayout);
      //
      assertSame(newLayout, shellInfo.getLayout());
      for (ObjectInfo child : shellInfo.getChildren()) {
        assertFalse(child instanceof GridLayoutInfo);
      }
      //
      assertEditor(
          "class Test {",
          "  public static void main(String[] args) {",
          "    Shell shell = new Shell();",
          "    shell.setLayout(new FillLayout(SWT.HORIZONTAL));",
          "    //",
          "    Button button = new Button(shell, SWT.NONE);",
          "  }",
          "}");
      assertTrue(buttonInfo.getChildrenJava().isEmpty());
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
  // isManagedObject()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link LayoutInfo#isManagedObject(ObjectInfo)}.
   */
  public void test_isManagedObject_simpleFalse() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "// filler filler filler",
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "  }",
            "}");
    LayoutInfo layout = shell.getLayout();
    // not ControlInfo
    {
      ObjectInfo newObject = createJavaInfo("org.eclipse.swt.layout.FillLayout");
      assertFalse(layout.isManagedObject(newObject));
    }
    // not child on Composite
    {
      ObjectInfo newObject = BTestUtils.createButton();
      assertFalse(layout.isManagedObject(newObject));
    }
  }

  /**
   * Test for {@link LayoutInfo#isManagedObject(ObjectInfo)}.
   */
  public void test_isManagedObject_simpleTrue() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "// filler filler filler",
            "public class Test extends Shell {",
            "  public Test() {",
            "    Button button = new Button(this, SWT.NONE);",
            "  }",
            "}");
    ControlInfo button = getJavaInfoByName("button");
    LayoutInfo layout = shell.getLayout();
    //
    assertTrue(layout.isManagedObject(button));
  }

  /**
   * Test for {@link LayoutInfo#isManagedObject(ObjectInfo)}.
   */
  public void test_isManagedObject_falseBecauseNotActive() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "// filler filler filler",
            "public class Test extends Shell {",
            "  public Test() {",
            "    Button button = new Button(this, SWT.NONE);",
            "  }",
            "}");
    ControlInfo button = getJavaInfoByName("button");
    // prepare implicit layout
    LayoutInfo oldLayout = shell.getLayout();
    LayoutInfo newLayout = createJavaInfo("org.eclipse.swt.layout.FillLayout");
    // set new Layout
    shell.setLayout(newLayout);
    assertFalse(oldLayout.isActive());
    assertTrue(newLayout.isActive());
    // "oldLayout" does not manage 
    assertFalse(oldLayout.isManagedObject(button));
    // "newLayout" manages 
    assertTrue(newLayout.isManagedObject(button));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implicit cases
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * If {@link Composite} has implicit children {@link Control}'s, then we should ignore its
   * {@link Layout}. Handling this {@link Layout} as specific one, with layout data and editing
   * support, may cause problems.
   * <p>
   * However right now I think that it is more-less safe to show {@link RowLayout}.
   */
  public void test_hasImplicitControls_RowLayout() throws Exception {
    setFileContentSrc(
        "test/ImplicitComposite.java",
        getTestSource(
            "public class ImplicitComposite extends Composite {",
            "  public ImplicitComposite(Composite parent, int style) {",
            "    super(parent, style);",
            "    setLayout(new RowLayout());",
            "    new Button(this, SWT.NONE);",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    CompositeInfo composite =
        parseComposite(
            "public class Test extends ImplicitComposite {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "}");
    assertHierarchy(
        "{this: test.ImplicitComposite} {this} {}",
        "  {implicit-layout: org.eclipse.swt.layout.RowLayout} {implicit-layout} {}");
    assertTrue(composite.hasLayout());
    // refresh
    composite.refresh();
    assertNoErrors(composite);
  }

  /**
   * If {@link Composite} has implicit children {@link Control}'s, then we should ignore its
   * {@link Layout}. Handling this {@link Layout} as specific one, with layout data and editing
   * support, may cause problems.
   * <p>
   * However right now I think that it is more-less safe to show {@link GridLayout}, at least until
   * we don't try to edit it visually.
   */
  public void test_hasImplicitControls_GridLayout() throws Exception {
    setFileContentSrc(
        "test/ImplicitComposite.java",
        getTestSource(
            "public class ImplicitComposite extends Composite {",
            "  public ImplicitComposite(Composite parent, int style) {",
            "    super(parent, style);",
            "    setLayout(new GridLayout(1, false));",
            "    new Button(this, SWT.NONE);",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    CompositeInfo composite =
        parseComposite(
            "public class Test extends ImplicitComposite {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "}");
    assertHierarchy(
        "{this: test.ImplicitComposite} {this} {}",
        "  {implicit-layout: org.eclipse.swt.layout.GridLayout} {implicit-layout} {}");
    assertTrue(composite.hasLayout());
    // refresh
    composite.refresh();
    assertNoErrors(composite);
  }

  /**
   * If {@link Composite} has implicit children {@link Control}'s, then we should ignore its
   * {@link Layout}. Handling this {@link Layout} as specific one, with layout data and editing
   * support, may cause problems.
   * <p>
   * Here "getButton()" exposes child from this {@link Composite}, so it is managed.
   */
  public void test_hasImplicitControls_directExposedChild() throws Exception {
    setFileContentSrc(
        "test/ImplicitComposite.java",
        getTestSource(
            "public class ImplicitComposite extends Composite {",
            "  private Button m_button;",
            "  public ImplicitComposite(Composite parent, int style) {",
            "    super(parent, style);",
            "    setLayout(new GridLayout());",
            "    {",
            "      m_button = new Button(this, SWT.NONE);",
            "    }",
            "  }",
            "  public Button getButton() {",
            "    return m_button;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    CompositeInfo composite =
        parseComposite(
            "public class Test extends ImplicitComposite {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "}");
    assertHierarchy(
        "{this: test.ImplicitComposite} {this} {}",
        "  {implicit-layout: org.eclipse.swt.layout.GridLayout} {implicit-layout} {}",
        "  {method: public org.eclipse.swt.widgets.Button test.ImplicitComposite.getButton()} {property} {}",
        "    {virtual-layout_data: org.eclipse.swt.layout.GridData} {virtual-layout-data} {}");
    assertTrue(composite.hasLayout());
    LayoutInfo layout = composite.getLayout();
    assertThat(layout.getControls()).hasSize(1);
    // refresh
    composite.refresh();
    assertNoErrors(composite);
  }

  /**
   * If {@link Composite} has implicit children {@link Control}'s, then we should ignore its
   * {@link Layout}. Handling this {@link Layout} as specific one, with layout data and editing
   * support, may cause problems.
   * <p>
   * Here "getButton()" exposes child from inner {@link Composite}, so we don't consider it as
   * managed.
   */
  public void test_hasImplicitControls_indirectExposedChild() throws Exception {
    setFileContentSrc(
        "test/ImplicitComposite.java",
        getTestSource(
            "public class ImplicitComposite extends Composite {",
            "  private Button m_button;",
            "  public ImplicitComposite(Composite parent, int style) {",
            "    super(parent, style);",
            "    setLayout(new GridLayout());",
            "    {",
            "      Composite container = new Composite(this, SWT.NONE);",
            "      container.setLayout(new RowLayout());",
            "      {",
            "        m_button = new Button(container, SWT.NONE);",
            "        m_button.setLayoutData(new RowData());",
            "      }",
            "    }",
            "  }",
            "  public Button getButton() {",
            "    return m_button;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    CompositeInfo composite =
        parseComposite(
            "public class Test extends ImplicitComposite {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "}");
    assertHierarchy(
        "{this: test.ImplicitComposite} {this} {}",
        "  {implicit-layout: org.eclipse.swt.layout.GridLayout} {implicit-layout} {}",
        "  {method: public org.eclipse.swt.widgets.Button test.ImplicitComposite.getButton()} {property} {}");
    assertTrue(composite.hasLayout());
    LayoutInfo layout = composite.getLayout();
    assertThat(layout.getControls()).isEmpty();
    // refresh
    composite.refresh();
    assertNoErrors(composite);
  }

  /**
   * If {@link Composite} has implicit children {@link Control}'s, then we should ignore its
   * {@link Layout}. Handling this {@link Layout} as specific one, with layout data and editing
   * support, may cause problems.
   * <p>
   * Here "getViewer()" exposes child from this {@link Composite}, so it is managed.
   */
  public void test_hasImplicitControls_directExposedChild_Viewer() throws Exception {
    setFileContentSrc(
        "test/ImplicitComposite.java",
        getTestSource(
            "public class ImplicitComposite extends Composite {",
            "  private TableViewer m_viewer;",
            "  public ImplicitComposite(Composite parent, int style) {",
            "    super(parent, style);",
            "    setLayout(new GridLayout());",
            "    {",
            "      m_viewer = new TableViewer(this, SWT.NONE);",
            "    }",
            "  }",
            "  public TableViewer getViewer() {",
            "    return m_viewer;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    CompositeInfo composite =
        parseComposite(
            "public class Test extends ImplicitComposite {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "}");
    assertHierarchy(
        "{this: test.ImplicitComposite} {this} {}",
        "  {implicit-layout: org.eclipse.swt.layout.GridLayout} {implicit-layout} {}",
        "  {viewer: public org.eclipse.swt.widgets.Table org.eclipse.jface.viewers.TableViewer.getTable()} {viewer} {}",
        "    {method: public org.eclipse.jface.viewers.TableViewer test.ImplicitComposite.getViewer()} {property} {}",
        "    {virtual-layout_data: org.eclipse.swt.layout.GridData} {virtual-layout-data} {}");
    assertTrue(composite.hasLayout());
    LayoutInfo layout = composite.getLayout();
    assertThat(layout.getControls()).hasSize(1);
    // refresh
    composite.refresh();
    assertNoErrors(composite);
  }

  /**
   * If {@link Composite} has implicit children {@link Control}'s, then we should ignore its
   * {@link Layout}. Handling this {@link Layout} as specific one, with layout data and editing
   * support, may cause problems.
   * <p>
   * Here "getViewer()" exposes child from inner {@link Composite}, so we don't consider it as
   * managed.
   */
  public void test_hasImplicitControls_indirectExposedChild_Viewer() throws Exception {
    setFileContentSrc(
        "test/ImplicitComposite.java",
        getTestSource(
            "public class ImplicitComposite extends Composite {",
            "  private TableViewer m_viewer;",
            "  public ImplicitComposite(Composite parent, int style) {",
            "    super(parent, style);",
            "    setLayout(new GridLayout());",
            "    {",
            "      Composite container = new Composite(this, SWT.NONE);",
            "      container.setLayout(new RowLayout());",
            "      {",
            "        m_viewer = new TableViewer(container, SWT.NONE);",
            "      }",
            "    }",
            "  }",
            "  public TableViewer getViewer() {",
            "    return m_viewer;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    CompositeInfo composite =
        parseComposite(
            "public class Test extends ImplicitComposite {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "}");
    assertHierarchy(
        "{this: test.ImplicitComposite} {this} {}",
        "  {implicit-layout: org.eclipse.swt.layout.GridLayout} {implicit-layout} {}",
        "  {viewer: public org.eclipse.swt.widgets.Table org.eclipse.jface.viewers.TableViewer.getTable()} {viewer} {}",
        "    {method: public org.eclipse.jface.viewers.TableViewer test.ImplicitComposite.getViewer()} {property} {}");
    assertTrue(composite.hasLayout());
    LayoutInfo layout = composite.getLayout();
    assertThat(layout.getControls()).isEmpty();
    // refresh
    composite.refresh();
    assertNoErrors(composite);
  }

  /**
   * Conflict between two cases. Here "getButton()" exposes child from inner {@link Composite}, so
   * initially we decided to disable layout. However there is local {@link Control} with
   * {@link GridData}, and it requires {@link GridLayout}.
   * <p>
   * Conflict is resolved by adding {@link LayoutDataInfo} only for {@link ControlInfo} managed by
   * this {@link LayoutInfo}, see {@link LayoutInfo#getControls()}. Indirectly exposed children are
   * not included.
   */
  public void test_indirectExposedChildren_andLocalLayoutData() throws Exception {
    setFileContentSrc(
        "test/ImplicitComposite.java",
        getTestSource(
            "public class ImplicitComposite extends Composite {",
            "  private Button m_button;",
            "  public ImplicitComposite(Composite parent, int style) {",
            "    super(parent, style);",
            "    setLayout(new GridLayout());",
            "    {",
            "      Composite container = new Composite(this, SWT.NONE);",
            "      container.setLayout(new RowLayout());",
            "      {",
            "        m_button = new Button(container, SWT.NONE);",
            "        m_button.setLayoutData(new RowData());",
            "      }",
            "    }",
            "  }",
            "  public Button getButton() {",
            "    return m_button;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    CompositeInfo composite =
        parseComposite(
            "public class Test extends ImplicitComposite {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "    {",
            "      Text text = new Text(this, SWT.BORDER);",
            "      text.setLayoutData(new GridData());",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: test.ImplicitComposite} {this} {/new Text(this, SWT.BORDER)/}",
        "  {implicit-layout: org.eclipse.swt.layout.GridLayout} {implicit-layout} {}",
        "  {method: public org.eclipse.swt.widgets.Button test.ImplicitComposite.getButton()} {property} {}",
        "  {new: org.eclipse.swt.widgets.Text} {local-unique: text} {/new Text(this, SWT.BORDER)/ /text.setLayoutData(new GridData())/}",
        "    {new: org.eclipse.swt.layout.GridData} {empty} {/text.setLayoutData(new GridData())/}");
    assertTrue(composite.hasLayout());
    // refresh
    composite.refresh();
    assertNoErrors(composite);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Code generation: name, based on template
  //
  ////////////////////////////////////////////////////////////////////////////
  private void check_nameTemplate(String template, String... lines) throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    {",
            "      Composite composite = new Composite(this, SWT.NONE);",
            "      composite.setLayout(new FillLayout(SWT.HORIZONTAL));",
            "    }",
            "  }",
            "}");
    shell.refresh();
    CompositeInfo composite = (CompositeInfo) shell.getChildrenControls().get(0);
    LayoutInfo layout = composite.getLayout();
    // set template
    RcpToolkitDescription.INSTANCE.getPreferences().setValue(
        org.eclipse.wb.internal.swt.preferences.IPreferenceConstants.P_LAYOUT_NAME_TEMPLATE,
        template);
    //
    layout.getPropertyByTitle("spacing").setValue(5);
    assertEditor(lines);
  }

  /**
   * Template "${defaultName}" means that name should be based on name of type.
   */
  public void test_nameTemplate_useDefaultName() throws Exception {
    check_nameTemplate(
        org.eclipse.wb.internal.core.model.variable.SyncParentChildVariableNameSupport.TEMPLATE_FOR_DEFAULT,
        "class Test extends Shell {",
        "  public Test() {",
        "    {",
        "      Composite composite = new Composite(this, SWT.NONE);",
        "      FillLayout fillLayout = new FillLayout(SWT.HORIZONTAL);",
        "      fillLayout.spacing = 5;",
        "      composite.setLayout(fillLayout);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Generate name using "${layoutAcronym}_${compositeName}" template.
   */
  public void test_nameTemplate_alternativeTemplate_1() throws Exception {
    check_nameTemplate(
        "${layoutAcronym}_${compositeName}",
        "class Test extends Shell {",
        "  public Test() {",
        "    {",
        "      Composite composite = new Composite(this, SWT.NONE);",
        "      FillLayout fl_composite = new FillLayout(SWT.HORIZONTAL);",
        "      fl_composite.spacing = 5;",
        "      composite.setLayout(fl_composite);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Generate name using "${compositeName}${layoutClassName}" template.
   */
  public void test_nameTemplate_alternativeTemplate_2() throws Exception {
    check_nameTemplate(
        "${compositeName}${layoutClassName}",
        "class Test extends Shell {",
        "  public Test() {",
        "    {",
        "      Composite composite = new Composite(this, SWT.NONE);",
        "      FillLayout compositeFillLayout = new FillLayout(SWT.HORIZONTAL);",
        "      compositeFillLayout.spacing = 5;",
        "      composite.setLayout(compositeFillLayout);",
        "    }",
        "  }",
        "}");
  }
}