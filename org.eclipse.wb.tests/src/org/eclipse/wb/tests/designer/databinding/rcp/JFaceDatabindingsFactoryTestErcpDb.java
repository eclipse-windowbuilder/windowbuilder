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
package org.eclipse.wb.tests.designer.databinding.rcp;

import org.eclipse.wb.internal.rcp.databinding.JFaceDatabindingsFactory;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.ercp.ErcpModelTest;

/**
 * @author lobas_av
 * 
 */
public class JFaceDatabindingsFactoryTestErcpDb extends ErcpModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void configureNewProject() throws Exception {
    super.configureNewProject();
    DatabindingTestUtils.configure(m_testProject);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_createProvider_1() throws Exception {
    CompositeInfo shell =
        DatabindingTestUtils.parseTestSource(this, new String[]{
            "public class Test {",
            "  protected Shell m_shell;",
            "  public static void main(String[] args) {",
            "    Test test = new Test();",
            "    test.open();",
            "  }",
            "  public void open() {",
            "    Display display = new Display();",
            "    createContents();",
            "    m_shell.open();",
            "    m_shell.layout();",
            "    while (!m_shell.isDisposed()) {",
            "      if (!display.readAndDispatch()) {",
            "        display.sleep();",
            "      }",
            "    }",
            "  }",
            "  protected void createContents() {",
            "    m_shell = new Shell();",
            "  }",
            "}"});
    assertNotNull(shell);
    //
    JFaceDatabindingsFactory factory = new JFaceDatabindingsFactory();
    assertNotNull(factory.createProvider(shell.getRootJava()));
  }

  public void test_createProvider_2() throws Exception {
    CompositeInfo shell =
        DatabindingTestUtils.parseTestSource(
            this,
            new String[]{
                "public class Test {",
                "  protected Shell m_shell;",
                "  private DataBindingContext m_bindingContext;",
                "  public static void main(String[] args) {",
                "    Test test = new Test();",
                "    test.open();",
                "  }",
                "  public void open() {",
                "    Display display = new Display();",
                "    createContents();",
                "    m_shell.open();",
                "    m_shell.layout();",
                "    while (!m_shell.isDisposed()) {",
                "      if (!display.readAndDispatch()) {",
                "        display.sleep();",
                "      }",
                "    }",
                "  }",
                "  protected void createContents() {",
                "    m_shell = new Shell();",
                "    m_bindingContext = initDataBindings();",
                "  }",
                "  private DataBindingContext initDataBindings() {",
                "    IObservableValue observeValue = BeansObservables.observeValue(getClass(), \"name\");",
                "    IObservableValue observeWidget = SWTObservables.observeText(m_shell);",
                "    DataBindingContext bindingContext = new DataBindingContext();",
                "    bindingContext.bindValue(observeWidget, observeValue, null, null);",
                "    return bindingContext;",
                "  }",
                "}"});
    assertNotNull(shell);
    //
    JFaceDatabindingsFactory factory = new JFaceDatabindingsFactory();
    assertNotNull(factory.createProvider(shell.getRootJava()));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Project disposing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void test_tearDown() throws Exception {
    do_projectDispose();
  }
}