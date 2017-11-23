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
package org.eclipse.wb.tests.designer.rcp.model.jface;

import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState.BadNodeInformation;
import org.eclipse.wb.internal.rcp.IExceptionConstants;
import org.eclipse.wb.internal.rcp.model.jface.WizardPageInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link WizardPageInfo}.
 * 
 * @author scheglov_ke
 */
public class WizardPageTest extends RcpModelTest {
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
   * Just parsing for some {@link WizardPage}.
   */
  public void test_0() throws Exception {
    WizardPageInfo wizardPage =
        parseJavaInfo(
            "import org.eclipse.jface.wizard.*;",
            "public class Test extends WizardPage {",
            "  public Test() {",
            "    super('pageName');",
            "  }",
            "  public void createControl(Composite parent) {",
            "    Composite container = new Composite(parent, SWT.NULL);",
            "    setControl(container);",
            "  }",
            "}");
    // check hierarchy
    assertHierarchy(
        "{this: org.eclipse.jface.wizard.WizardPage} {this} {/setControl(container)/}",
        "  {parameter} {parent} {/new Composite(parent, SWT.NULL)/}",
        "    {new: org.eclipse.swt.widgets.Composite} {local-unique: container} {/new Composite(parent, SWT.NULL)/ /setControl(container)/}",
        "      {implicit-layout: absolute} {implicit-layout} {}");
    CompositeInfo parentComposite = wizardPage.getChildren(CompositeInfo.class).get(0);
    CompositeInfo container = (CompositeInfo) parentComposite.getChildrenControls().get(0);
    // refresh()
    wizardPage.refresh();
    // check bounds
    assertThat(wizardPage.getBounds().width).isEqualTo(600);
    assertThat(wizardPage.getBounds().height).isEqualTo(500);
    assertThat(parentComposite.getBounds().width).isGreaterThan(500);
    assertThat(parentComposite.getBounds().height).isGreaterThan(250);
    assertThat(container.getBounds().width).isGreaterThan(500);
    assertThat(container.getBounds().height).isGreaterThan(250);
    // set new bounds
    wizardPage.getTopBoundsSupport().setSize(500, 400);
    wizardPage.refresh();
    assertThat(wizardPage.getBounds().width).isEqualTo(500);
    assertThat(wizardPage.getBounds().height).isEqualTo(400);
  }

  /**
   * When we test/preview {@link WizardPage} and close {@link WizardDialog} its {@link Shell}
   * becomes disposed, so we should not try to close {@link WizardDialog} again.
   */
  public void test_refresh_whenAlreadyDisposed() throws Exception {
    WizardPageInfo wizardPage =
        parseJavaInfo(
            "import org.eclipse.jface.wizard.*;",
            "public class Test extends WizardPage {",
            "  public Test() {",
            "    super('pageName');",
            "  }",
            "  public void createControl(Composite parent) {",
            "    Composite container = new Composite(parent, SWT.NULL);",
            "    setControl(container);",
            "  }",
            "}");
    wizardPage.refresh();
    // dispose Shell
    Shell shell = (Shell) wizardPage.getComponentObject();
    shell.dispose();
    // do refresh again
    wizardPage.refresh();
  }

  /**
   * User may override {@link WizardPage#getControl()}, but we allow to visit it only one time, and
   * second time return <code>null</code>, and this causes problems.
   */
  public void test_override_getControl() throws Exception {
    WizardPageInfo wizardPage =
        parseJavaInfo(
            "import org.eclipse.jface.wizard.*;",
            "public class Test extends WizardPage {",
            "  Composite container;",
            "  public Test() {",
            "    super('pageName');",
            "  }",
            "  public void createControl(Composite parent) {",
            "    container = new Composite(parent, SWT.NULL);",
            "    setControl(container);",
            "  }",
            "  public Control getControl() {",
            "    return (container);",
            "  }",
            "}");
    assertHierarchy(
        "{this: org.eclipse.jface.wizard.WizardPage} {this} {/setControl(container)/}",
        "  {parameter} {parent} {/new Composite(parent, SWT.NULL)/}",
        "    {new: org.eclipse.swt.widgets.Composite} {field-unique: container} {/new Composite(parent, SWT.NULL)/ /setControl(container)/ /(container)/ /container/}",
        "      {implicit-layout: absolute} {implicit-layout} {}");
    // refresh()
    wizardPage.refresh();
    assertNoErrors(wizardPage);
  }

  /**
   * Some not-so-smart users (no, Eric just reproduced this :-)) may try to edit {@link WizardPage}
   * without {@link Control}.
   */
  public void test_noControl() throws Exception {
    try {
      parseJavaInfo(
          "import org.eclipse.jface.wizard.*;",
          "public class Test extends WizardPage {",
          "  public Test() {",
          "    super('pageName');",
          "  }",
          "  public void createControl(Composite parent) {",
          "  }",
          "}");
      fail();
    } catch (Throwable e) {
      DesignerException de = DesignerExceptionUtils.getDesignerException(e);
      assertEquals(IExceptionConstants.NO_CONTROL_IN_WIZARD_PAGE, de.getCode());
      assertTrue(DesignerExceptionUtils.isWarning(e));
    }
  }

  public void test_simulateException_inCreate() throws Exception {
    String key = "__wbp_WizardPage_simulateException";
    try {
      System.setProperty(key, "true");
      parseJavaInfo(
          "import org.eclipse.jface.wizard.*;",
          "public class Test extends WizardPage {",
          "  public Test() {",
          "    super('pageName');",
          "  }",
          "  public void createControl(Composite parent) {",
          "    Composite container = new Composite(parent, SWT.NULL);",
          "    setControl(container);",
          "  }",
          "}");
      fail();
    } catch (Throwable e) {
      Throwable rootCause = DesignerExceptionUtils.getRootCause(e);
      assertEquals("Simulated exception", rootCause.getMessage());
    } finally {
      System.clearProperty(key);
    }
  }

  /**
   * We replace bad control with placeholder in {@link WizardPage#createControl(Composite)} too.
   */
  public void test_exceptionInCreate() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends Button {",
            "  public MyButton(Composite parent, int style) {",
            "    super(parent, style);",
            "    throw new IllegalStateException('actual');",
            "  }",
            "  protected void checkSubclass() {",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    WizardPageInfo wizardPage =
        parseJavaInfo(
            "import org.eclipse.jface.wizard.*;",
            "public class Test extends WizardPage {",
            "  public Test() {",
            "    super('pageName');",
            "  }",
            "  public void createControl(Composite parent) {",
            "    setControl(new MyButton(parent, SWT.NONE));",
            "  }",
            "}");
    wizardPage.refresh();
    assertHierarchy(
        "{this: org.eclipse.jface.wizard.WizardPage} {this} {/setControl(new MyButton(parent, SWT.NONE))/}",
        "  {parameter} {parent} {/new MyButton(parent, SWT.NONE)/}",
        "    {new: test.MyButton} {empty} {/setControl(new MyButton(parent, SWT.NONE))/}");
    // check logged exceptions 
    List<BadNodeInformation> badNodes = m_lastState.getBadRefreshNodes().nodes();
    assertThat(badNodes).hasSize(1);
  }

  /**
   * We should invoke method only one time, and handle this correctly even for case of
   * "special rendering", i.e. when execution flow is not known for parser.
   */
  public void test_duplicateMethodInvocation() throws Exception {
    WizardPageInfo wizardPage =
        parseJavaInfo(
            "import org.eclipse.jface.wizard.*;",
            "public class Test extends WizardPage {",
            "  public Test() {",
            "    super('pageName');",
            "  }",
            "  public void createControl(Composite parent) {",
            "    Composite container = new Composite(parent, SWT.NULL);",
            "    setControl(container);",
            "    createButton(container, SWT.NONE);",
            "    createButton(container, SWT.CHECK);",
            "  }",
            "  private Button createButton(Composite parent, int style) {",
            "    Button button = new Button(parent, style);",
            "    return button;",
            "  }",
            "}");
    assertHierarchy(
        "{this: org.eclipse.jface.wizard.WizardPage} {this} {/setControl(container)/}",
        "  {parameter} {parent} {/new Composite(parent, SWT.NULL)/}",
        "    {new: org.eclipse.swt.widgets.Composite} {local-unique: container} {/new Composite(parent, SWT.NULL)/ /setControl(container)/ /new Button(parent, style)/ /createButton(container, SWT.NONE)/ /createButton(container, SWT.CHECK)/}",
        "      {implicit-layout: absolute} {implicit-layout} {}",
        "      {new: org.eclipse.swt.widgets.Button} {local-unique: button} {/new Button(parent, style)/ /button/ /createButton(container, SWT.NONE)/ /createButton(container, SWT.CHECK)/}");
    //
    wizardPage.refresh();
    assertNoErrors(wizardPage);
  }

  /**
   * Here we test, that assignments made in constructor are visible in "render" method.
   */
  public void test_specialRendering_andAssignmentInConstructor() throws Exception {
    WizardPageInfo wizardPage =
        parseJavaInfo(
            "import java.util.*;",
            "import org.eclipse.jface.wizard.*;",
            "public class Test extends WizardPage {",
            "  private Map m_map;",
            "  public Test() {",
            "    super('pageName');",
            "    m_map = new HashMap();",
            "  }",
            "  public void createControl(Composite parent) {",
            "    Composite container = new Composite(parent, SWT.NULL);",
            "    setControl(container);",
            "    container.setEnabled(m_map == null);",
            "  }",
            "}");
    assertHierarchy(
        "{this: org.eclipse.jface.wizard.WizardPage} {this} {/setControl(container)/}",
        "  {parameter} {parent} {/new Composite(parent, SWT.NULL)/}",
        "    {new: org.eclipse.swt.widgets.Composite} {local-unique: container} {/new Composite(parent, SWT.NULL)/ /setControl(container)/ /container.setEnabled(m_map == null)/}",
        "      {implicit-layout: absolute} {implicit-layout} {}");
    wizardPage.refresh();
    //
    ASTNode containerNode = m_lastEditor.getEnclosingNode("container =");
    CompositeInfo container = (CompositeInfo) wizardPage.getChildRepresentedBy(containerNode);
    assertEquals(false, ((Composite) container.getObject()).isEnabled());
  }

  /**
   * Use tried to render {@link WizardPage} which requires not <code>null</code>
   * {@link IStructuredSelection} argument. We always can provide good, empty instance for it.
   */
  public void test_ISelection_constructorArgument() throws Exception {
    setFileContentSrc(
        "test/MyWizardPage.java",
        getTestSource(
            "import org.eclipse.jface.wizard.*;",
            "public abstract class MyWizardPage extends WizardPage {",
            "  public MyWizardPage(ISelection selection) {",
            "    super('pageName');",
            "    if (selection == null) {",
            "      throw new IllegalArgumentException();",
            "    }",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    useStrictEvaluationMode(false);
    parseJavaInfo(
        "public class Test extends MyWizardPage {",
        "  public Test(ISelection selection) {",
        "    super(selection);",
        "  }",
        "  public void createControl(Composite parent) {",
        "    Composite container = new Composite(parent, SWT.NULL);",
        "    setControl(container);",
        "  }",
        "}");
    refresh();
    // no exceptions
    assertNoErrors(m_lastParseInfo);
  }
}