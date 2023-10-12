/*******************************************************************************
 * Copyright (c) 2023 Patrick Ziegler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.swt.model.property;

import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.swt.model.jface.resource.LocalResourceManagerInfo;
import org.eclipse.wb.internal.swt.model.jface.resource.ManagerContainerInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.junit.Test;

import java.util.List;

public class LocalResourceManagerTest extends RcpModelTest {
	@Test
	public void test_parseJavaInfo1() throws Exception {
		test_parseJavaInfo("public class Test extends Shell {",
							"  private ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());",
							"}");
	}

	@Test
	public void test_parseJavaInfo2() throws Exception {
		test_parseJavaInfo("public class Test extends Shell {",
							"  private ResourceManager resourceManager;",
							"  public Test() {",
							"    resourceManager = new LocalResourceManager(JFaceResources.getResources());",
							"  }",
							"}");
	}

	private void test_parseJavaInfo(String... lines) throws Exception {
		CompositeInfo shell = parseComposite(lines);
		shell.refresh();
		//
		List<LocalResourceManagerInfo> children = ManagerContainerInfo.getManagers(shell, LocalResourceManagerInfo.class);
		assertEquals(1, children.size());
		LocalResourceManagerInfo containerInfo = children.get(0);
		assertNotNull(containerInfo);
		VariableSupport variableSupport = containerInfo.getVariableSupport();
		assertEquals(variableSupport.getName(), "resourceManager");
	}

	@Test
	public void test_createResourceManager1() throws Exception {
		CompositeInfo shell = parseComposite(
				"public class Test extends Shell {",
				"}");
		ManagerContainerInfo.getResourceManagerInfo(shell);
		shell.refresh();
		assertEditor(
				"public class Test extends Shell {",
				"  private LocalResourceManager localResourceManager;",
				"  public Test() {",
				"    createResourceManager();",
				"  }",
				"  private void createResourceManager() {",
				"    localResourceManager = new LocalResourceManager(JFaceResources.getResources(),this);",
				"  }",
				"}");
	}

	@Test
	public void test_createResourceManager2() throws Exception {
		CompositeInfo shell = parseComposite(
				"public class Test {",
				"  private Shell shell;",
				"  public Test() {",
				"    shell = new Shell();",
				"  }",
				"}");
		ManagerContainerInfo.getResourceManagerInfo(shell);
		shell.refresh();
		assertEditor(
				"public class Test {",
				"  private Shell shell;",
				"  private LocalResourceManager localResourceManager;",
				"  public Test() {",
				"    shell = new Shell();",
				"    createResourceManager();",
				"  }",
				"  private void createResourceManager() {",
				"    localResourceManager = new LocalResourceManager(JFaceResources.getResources(),shell);",
				"  }",
				"}");
	}

	@Test
	public void test_createResourceManager3() throws Exception {
		CompositeInfo shell = parseComposite(
				"public class Test {",
				"  private Shell shell;",
				"  public Test() {",
				"    shell = new Shell();",
				"    new Composite(shell, SWT.NONE);",
				"  }",
				"}");
		ManagerContainerInfo.getResourceManagerInfo(shell);
		shell.refresh();
		assertEditor(
				"public class Test {",
				"  private Shell shell;",
				"  private LocalResourceManager localResourceManager;",
				"  public Test() {",
				"    shell = new Shell();",
				"    createResourceManager();",
				"    new Composite(shell, SWT.NONE);",
				"  }",
				"  private void createResourceManager() {",
				"    localResourceManager = new LocalResourceManager(JFaceResources.getResources(),shell);",
				"  }",
				"}");
	}

	@Test
	public void test_createResourceManager4() throws Exception {
		CompositeInfo shell = parseComposite(
				"public class Test extends Shell {",
				"  public Test() {",
				"    new Composite(this, SWT.NONE);",
				"  }",
				"}");
		ManagerContainerInfo.getResourceManagerInfo(shell);
		shell.refresh();
		assertEditor(
				"public class Test extends Shell {",
				"  private LocalResourceManager localResourceManager;",
				"  public Test() {",
				"    createResourceManager();",
				"    new Composite(this, SWT.NONE);",
				"  }",
				"  private void createResourceManager() {",
				"    localResourceManager = new LocalResourceManager(JFaceResources.getResources(),this);",
				"  }",
				"}");
	}

	@Test
	public void test_createResourceManager5() throws Exception {
		CompositeInfo shell = parseComposite(
				"public class Test extends Shell {",
				"  public Test() {",
				"    super(SWT.NONE);",
				"  }",
				"}");
		ManagerContainerInfo.getResourceManagerInfo(shell);
		shell.refresh();
		assertEditor(
				"public class Test extends Shell {",
				"  private LocalResourceManager localResourceManager;",
				"  public Test() {",
				"    super(SWT.NONE);",
				"    createResourceManager();",
				"  }",
				"  private void createResourceManager() {",
				"    localResourceManager = new LocalResourceManager(JFaceResources.getResources(),this);",
				"  }",
				"}");
	}
}
