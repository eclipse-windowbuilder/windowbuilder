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

import org.eclipse.wb.internal.core.databinding.parser.ParseFactoryNoModelDatabindings;
import org.eclipse.wb.internal.rcp.databinding.JFaceDatabindingsFactory;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.junit.Test;

/**
 * @author lobas_av
 */
public class JFaceDatabindingsFactoryTestRcp extends RcpModelTest {
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
	@Test
	public void test_createProvider() throws Exception {
		CompositeInfo shell =
				parseComposite(
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
						"}");
		assertNotNull(shell);
		//
		JFaceDatabindingsFactory factory = new JFaceDatabindingsFactory();
		assertNotNull(factory.createProvider(shell.getRootJava()));
	}

	/**
	 * Test for {@link ParseFactoryNoModelDatabindings} restricting model creation in
	 * "initDataBindings" method.
	 */
	@Test
	public void test_noModel() throws Exception {
		CompositeInfo shell =
				parseComposite(
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
						"    initDataBindings();",
						"  }",
						"  protected void initDataBindings() {",
						"    new Button(m_shell, SWT.NONE);",
						"  }",
						"}");
		shell.refresh();
		//
		assertTrue(shell.getChildrenControls().isEmpty());
	}
}