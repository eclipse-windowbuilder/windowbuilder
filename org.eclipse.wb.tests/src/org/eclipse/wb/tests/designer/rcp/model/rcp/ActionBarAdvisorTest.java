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
package org.eclipse.wb.tests.designer.rcp.model.rcp;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.model.jface.action.ActionContainerInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.ActionInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.CoolBarManagerInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.MenuManagerInfo;
import org.eclipse.wb.internal.rcp.model.rcp.ActionBarAdvisorInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang.NotImplementedException;

/**
 * Test for {@link ActionBarAdvisorInfo}.
 * 
 * @author scheglov_ke
 */
public class ActionBarAdvisorTest extends RcpModelTest {
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
   * Basic test for {@link ActionBarAdvisorInfo}.
   */
  public void test_0() throws Exception {
    ActionBarAdvisorInfo advisor =
        parseJavaInfo(
            "public class Test extends ActionBarAdvisor {",
            "  public Test(IActionBarConfigurer configurer) {",
            "    super(configurer);",
            "  }",
            "  protected void fillCoolBar(ICoolBarManager coolBar) {",
            "  }",
            "  protected void fillMenuBar(IMenuManager menuBar) {",
            "  }",
            "  protected void makeActions(IWorkbenchWindow window) {",
            "  }",
            "}");
    // check hierarchy
    assertHierarchy(
        "{this: org.eclipse.ui.application.ActionBarAdvisor} {this} {}",
        "  {parameter} {menuBar} {}",
        "  {parameter} {coolBar} {}");
    {
      MenuManagerInfo menuBar = (MenuManagerInfo) advisor.getChildren().get(0);
      assertEquals("menuBar", menuBar.getVariableSupport().getName());
    }
    {
      CoolBarManagerInfo coolBar = (CoolBarManagerInfo) advisor.getChildren().get(1);
      assertEquals("coolBar", coolBar.getVariableSupport().getName());
    }
    // refresh
    {
      advisor.refresh();
      assertNoErrors(advisor);
      // check bounds
      assertThat(advisor.getBounds().width).isEqualTo(600);
      assertThat(advisor.getBounds().height).isEqualTo(500);
    }
    // check Proxy implementations for IActionBarConfigurer
    {
      Object o_IActionBarConfigurer =
          ReflectionUtils.invokeMethod2(advisor.getObject(), "getActionBarConfigurer");
      try {
        ReflectionUtils.invokeMethod(o_IActionBarConfigurer, "toString()");
        fail();
      } catch (NotImplementedException e) {
      }
      // IWorkbenchWindowConfigurer
      Object o_IWorkbenchWindowConfigurer =
          ReflectionUtils.invokeMethod(o_IActionBarConfigurer, "getWindowConfigurer()");
      try {
        ReflectionUtils.invokeMethod(o_IWorkbenchWindowConfigurer, "toString()");
        fail();
      } catch (NotImplementedException e) {
      }
      // IWorkbenchWindow
      Object o_IWorkbenchWindow =
          ReflectionUtils.invokeMethod(o_IWorkbenchWindowConfigurer, "getWindow()");
      assertSame(DesignerPlugin.getActiveWorkbenchWindow(), o_IWorkbenchWindow);
    }
  }

  /**
   * Test for {@link ActionBarAdvisor_TopBoundsSupport}.
   */
  public void test_ActionBarAdvisor_TopBoundsSupport() throws Exception {
    ActionBarAdvisorInfo advisor =
        parseJavaInfo(
            "public class Test extends ActionBarAdvisor {",
            "  public Test(IActionBarConfigurer configurer) {",
            "    super(configurer);",
            "  }",
            "}");
    // refresh
    {
      advisor.refresh();
      assertNoErrors(advisor);
      // check bounds
      assertThat(advisor.getBounds().width).isEqualTo(600);
      assertThat(advisor.getBounds().height).isEqualTo(500);
    }
    // set new size
    {
      advisor.getTopBoundsSupport().setSize(450, 300);
      advisor.refresh();
      assertThat(advisor.getBounds().width).isEqualTo(450);
      assertThat(advisor.getBounds().height).isEqualTo(300);
    }
  }

  /**
   * Test for adding new {@link ActionInfo} into {@link ActionBarAdvisorInfo}.
   * <p>
   * Right now {@link ActionContainerInfo} adds actions into fixed method
   * <code>createActions()</code>, however for {@link ActionBarAdvisor} we should add actions in
   * <code>makeActions(IWorkbenchWindow)</code>.
   */
  public void test_addAction() throws Exception {
    ActionBarAdvisorInfo advisor =
        parseJavaInfo(
            "public class Test extends ActionBarAdvisor {",
            "  public Test(IActionBarConfigurer configurer) {",
            "    super(configurer);",
            "  }",
            "  protected void fillMenuBar(IMenuManager menuBar) {",
            "  }",
            "  protected void makeActions(IWorkbenchWindow window) {",
            "  }",
            "}");
    advisor.refresh();
    assertNoErrors(advisor);
    //
    ActionInfo action = ActionContainerInfo.createNew(advisor);
    MenuManagerInfo menuManager = advisor.getMenuManager();
    menuManager.command_CREATE(action, null);
    assertEditor(
        "public class Test extends ActionBarAdvisor {",
        "  private Action action;",
        "  public Test(IActionBarConfigurer configurer) {",
        "    super(configurer);",
        "  }",
        "  protected void fillMenuBar(IMenuManager menuBar) {",
        "    menuBar.add(action);",
        "  }",
        "  protected void makeActions(IWorkbenchWindow window) {",
        "    {",
        "      action = new Action('New Action') {",
        "      };",
        "      register(action);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: org.eclipse.ui.application.ActionBarAdvisor} {this} {/register(action)/}",
        "  {parameter} {menuBar} {/menuBar.add(action)/}",
        "    {void} {void} {/menuBar.add(action)/}",
        "  {org.eclipse.wb.internal.rcp.model.jface.action.ActionContainerInfo}",
        "    {new: org.eclipse.jface.action.Action} {field-unique: action} {/new Action('New Action')/ /register(action)/ /menuBar.add(action)/}");
    // refresh
    advisor.refresh();
    assertNoErrors(advisor);
  }

  /**
   * Test for <code>window.getWorkbench().getSharedImages()</code>.
   */
  public void test_getWorkbench_getSharedImages() throws Exception {
    ActionBarAdvisorInfo advisor =
        parseJavaInfo(
            "public class Test extends ActionBarAdvisor {",
            "  private Action action;",
            "  public Test(IActionBarConfigurer configurer) {",
            "    super(configurer);",
            "  }",
            "  protected void makeActions(IWorkbenchWindow window) {",
            "    ISharedImages images = window.getWorkbench().getSharedImages();",
            "    {",
            "      action = new Action() {",
            "      };",
            "      action.setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_BACK));",
            "    }",
            "  }",
            "}");
    advisor.refresh();
    assertNoErrors(advisor);
  }

  /**
   * Test for using {@link ToolBarContributionItem}.
   */
  public void test_newToolBarContributionItem() throws Exception {
    ActionBarAdvisorInfo advisor =
        parseJavaInfo(
            "public class Test extends ActionBarAdvisor {",
            "  public Test(IActionBarConfigurer configurer) {",
            "    super(configurer);",
            "  }",
            "  protected void fillCoolBar(ICoolBarManager coolBar) {",
            "    IToolBarManager toolBar = new ToolBarManager(SWT.FLAT | SWT.RIGHT);",
            "    coolBar.add(new ToolBarContributionItem(toolBar));",
            "  }",
            "}");
    advisor.refresh();
    assertNoErrors(advisor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // WorkbenchWindowAdvisor properties
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that we show {@link IWorkbenchWindowConfigurer} properties.
   */
  public void test_WorkbenchWindowAdvisor_properties_allNames() throws Exception {
    setFileContentSrc(
        "test/ApplicationWorkbenchWindowAdvisor.java",
        getSourceDQ(
            "package test;",
            "import org.eclipse.ui.application.*;",
            "public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {",
            "  public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {",
            "    super(configurer);",
            "  }",
            "  public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {",
            "    return new Test(configurer);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/Test.java",
        getSourceDQ(
            "package test;",
            "import org.eclipse.ui.application.*;",
            "public class Test extends ActionBarAdvisor {",
            "  public Test(IActionBarConfigurer configurer) {",
            "    super(configurer);",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    ICompilationUnit unit =
        m_testProject.getJavaProject().findType("test.Test").getCompilationUnit();
    ActionBarAdvisorInfo advisor = (ActionBarAdvisorInfo) parseCompilationUnit(unit);
    // check known properties
    assertNotNull(advisor.getPropertyByTitle("initialSize"));
    assertNotNull(advisor.getPropertyByTitle("shellStyle"));
    assertNotNull(advisor.getPropertyByTitle("showCoolBar"));
    assertNotNull(advisor.getPropertyByTitle("showFastViewBars"));
    assertNotNull(advisor.getPropertyByTitle("showMenuBar"));
    assertNotNull(advisor.getPropertyByTitle("showPerspectiveBar"));
    assertNotNull(advisor.getPropertyByTitle("showProgressIndicator"));
    assertNotNull(advisor.getPropertyByTitle("showStatusLine"));
    assertNotNull(advisor.getPropertyByTitle("title"));
    // properties should be cached
    assertSame(advisor.getPropertyByTitle("title"), advisor.getPropertyByTitle("title"));
  }

  /**
   * Use property "showStatusLine".
   */
  public void test_WorkbenchWindowAdvisor_properties_showStatusLine() throws Exception {
    setFileContentSrc(
        "test/ApplicationWorkbenchWindowAdvisor.java",
        getSourceDQ(
            "package test;",
            "import org.eclipse.ui.application.*;",
            "public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {",
            "  public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {",
            "    super(configurer);",
            "  }",
            "  public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {",
            "    return new Test(configurer);",
            "  }",
            "  public void preWindowOpen() {",
            "    IWorkbenchWindowConfigurer configurer = getWindowConfigurer();",
            "    configurer.setShowStatusLine(false);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/Test.java",
        getSourceDQ(
            "package test;",
            "import org.eclipse.ui.application.*;",
            "public class Test extends ActionBarAdvisor {",
            "  public Test(IActionBarConfigurer configurer) {",
            "    super(configurer);",
            "  }",
            "}"));
    waitForAutoBuild();
    ActionBarAdvisorInfo advisor =
        (ActionBarAdvisorInfo) parseCompilationUnit(m_testProject.getCompilationUnit("test.Test"));
    Property propertySL = advisor.getPropertyByTitle("showStatusLine");
    // get value
    assertTrue(propertySL.isModified());
    assertEquals(Boolean.FALSE, propertySL.getValue());
    // set value
    propertySL.setValue(true);
    assertUnitContents(
        getSourceDQ(
            "package test;",
            "import org.eclipse.ui.application.*;",
            "public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {",
            "  public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {",
            "    super(configurer);",
            "  }",
            "  public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {",
            "    return new Test(configurer);",
            "  }",
            "  public void preWindowOpen() {",
            "    IWorkbenchWindowConfigurer configurer = getWindowConfigurer();",
            "  }",
            "}"),
        "test.ApplicationWorkbenchWindowAdvisor");
  }

  /**
   * Use property "title".
   */
  public void test_WorkbenchWindowAdvisor_properties_title() throws Exception {
    setFileContentSrc(
        "test/ApplicationWorkbenchWindowAdvisor.java",
        getSourceDQ(
            "package test;",
            "import org.eclipse.ui.application.*;",
            "public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {",
            "  public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {",
            "    super(configurer);",
            "  }",
            "  public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {",
            "    return new Test(configurer);",
            "  }",
            "  public void preWindowOpen() {",
            "    IWorkbenchWindowConfigurer configurer = getWindowConfigurer();",
            "    configurer.setTitle('A');",
            "  }",
            "}"));
    setFileContentSrc(
        "test/Test.java",
        getSourceDQ(
            "package test;",
            "import org.eclipse.ui.application.*;",
            "public class Test extends ActionBarAdvisor {",
            "  public Test(IActionBarConfigurer configurer) {",
            "    super(configurer);",
            "  }",
            "}"));
    waitForAutoBuild();
    ActionBarAdvisorInfo advisor =
        (ActionBarAdvisorInfo) parseCompilationUnit(m_testProject.getCompilationUnit("test.Test"));
    Property propertyTitle = advisor.getPropertyByTitle("title");
    // initially value "A"
    assertTrue(propertyTitle.isModified());
    assertEquals("A", propertyTitle.getValue());
    // set value "B"
    propertyTitle.setValue("B");
    assertUnitContents(
        getSourceDQ(
            "package test;",
            "import org.eclipse.ui.application.*;",
            "public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {",
            "  public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {",
            "    super(configurer);",
            "  }",
            "  public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {",
            "    return new Test(configurer);",
            "  }",
            "  public void preWindowOpen() {",
            "    IWorkbenchWindowConfigurer configurer = getWindowConfigurer();",
            "    configurer.setTitle('B');",
            "  }",
            "}"),
        "test.ApplicationWorkbenchWindowAdvisor");
    assertTrue(propertyTitle.isModified());
    assertEquals("B", propertyTitle.getValue());
    // remove value
    propertyTitle.setValue(Property.UNKNOWN_VALUE);
    assertUnitContents(
        getSourceDQ(
            "package test;",
            "import org.eclipse.ui.application.*;",
            "public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {",
            "  public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {",
            "    super(configurer);",
            "  }",
            "  public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {",
            "    return new Test(configurer);",
            "  }",
            "  public void preWindowOpen() {",
            "    IWorkbenchWindowConfigurer configurer = getWindowConfigurer();",
            "  }",
            "}"),
        "test.ApplicationWorkbenchWindowAdvisor");
    assertFalse(propertyTitle.isModified());
    assertEquals(Property.UNKNOWN_VALUE, propertyTitle.getValue());
    // set value "C"
    propertyTitle.setValue("C");
    assertUnitContents(
        getSourceDQ(
            "package test;",
            "import org.eclipse.ui.application.*;",
            "public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {",
            "  public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {",
            "    super(configurer);",
            "  }",
            "  public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {",
            "    return new Test(configurer);",
            "  }",
            "  public void preWindowOpen() {",
            "    IWorkbenchWindowConfigurer configurer = getWindowConfigurer();",
            "    configurer.setTitle('C');",
            "  }",
            "}"),
        "test.ApplicationWorkbenchWindowAdvisor");
    assertTrue(propertyTitle.isModified());
    assertEquals("C", propertyTitle.getValue());
  }

  /**
   * No "preWindowOpen()", create new method.
   */
  public void test_WorkbenchWindowAdvisor_properties_newMethod() throws Exception {
    setFileContentSrc(
        "test/ApplicationWorkbenchWindowAdvisor.java",
        getSourceDQ(
            "package test;",
            "import org.eclipse.ui.application.*;",
            "public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {",
            "  public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {",
            "    super(configurer);",
            "  }",
            "  public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {",
            "    return new Test(configurer);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/Test.java",
        getSourceDQ(
            "package test;",
            "import org.eclipse.ui.application.*;",
            "public class Test extends ActionBarAdvisor {",
            "  public Test(IActionBarConfigurer configurer) {",
            "    super(configurer);",
            "  }",
            "}"));
    waitForAutoBuild();
    ActionBarAdvisorInfo advisor =
        (ActionBarAdvisorInfo) parseCompilationUnit(m_testProject.getCompilationUnit("test.Test"));
    Property propertyTitle = advisor.getPropertyByTitle("title");
    // set value "B"
    propertyTitle.setValue("B");
    assertUnitContents(
        getSourceDQ(
            "package test;",
            "import org.eclipse.ui.application.*;",
            "public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {",
            "  public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {",
            "    super(configurer);",
            "  }",
            "  public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {",
            "    return new Test(configurer);",
            "  }",
            "  public void preWindowOpen() {",
            "    IWorkbenchWindowConfigurer configurer = getWindowConfigurer();",
            "    configurer.setTitle('B');",
            "  }",
            "}"),
        "test.ApplicationWorkbenchWindowAdvisor");
    assertTrue(propertyTitle.isModified());
    assertEquals("B", propertyTitle.getValue());
  }

  private static void assertUnitContents(String content, String typeName) throws JavaModelException {
    assertEquals(content, m_testProject.getCompilationUnit(typeName).getBuffer().getContents());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Source
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String[] getTestSource_decorate(String... lines) {
    lines =
        CodeUtils.join(new String[]{
            "package test;",
            "import org.eclipse.swt.SWT;",
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.ui.*;",
            "import org.eclipse.ui.application.*;"}, lines);
    return lines;
  }
}