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
package org.eclipse.wb.tests.designer.core.model.util;

import org.eclipse.wb.internal.core.model.util.ExposeComponentSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;

import org.junit.Test;

/**
 * Tests for {@link ExposeComponentSupport}.
 *
 * @author scheglov_ke
 */
public class ExposeComponentSupportTest extends SwingModelTest {
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
	 * No "expose" action, because not supported {@link VariableSupport} type.
	 */
	@Test
	public void test_1_unsupportedVariable() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		IAction exposeAction = getExposeAction(panel);
		assertNull(exposeAction);
	}

	/**
	 * No "expose" action, because component is already exposed.
	 */
	@Test
	public void test_2_alreadyExposed() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  private final JButton button = new JButton();",
						"  public Test() {",
						"    add(button);",
						"  }",
						"  public JButton getButton() {",
						"    return button;",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// already exposed, no action
		IAction exposeAction = getExposeAction(button);
		assertNull(exposeAction);
	}

	/**
	 * Method with "return;", i.e. without expression should not cause problems.
	 */
	@Test
	public void test_returnWithoutExpression() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  private final JButton button = new JButton();",
						"  public Test() {",
						"    add(button);",
						"  }",
						"  public void foo() {",
						"    return;",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// no exception
		IAction exposeAction = getExposeAction(button);
		assertNotNull(exposeAction);
	}

	/**
	 * Method without body should not cause problems.
	 */
	@Test
	public void test_abstractMethod() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public abstract class Test extends JPanel {",
						"  private final JButton button = new JButton();",
						"  public Test() {",
						"    add(button);",
						"  }",
						"  public abstract void foo();",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// no exception
		IAction exposeAction = getExposeAction(button);
		assertNotNull(exposeAction);
	}

	/**
	 * Has "expose" action, can be exposed.
	 */
	@Test
	public void test_3_hasAction() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton button = new JButton();",
						"    add(button);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		IAction exposeAction = getExposeAction(button);
		assertNotNull(exposeAction);
	}

	/**
	 * Do expose.
	 */
	@Test
	public void test_expose() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton button = new JButton();",
						"    add(button);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		ReflectionUtils.invokeMethod(
				ExposeComponentSupport.class,
				"expose(org.eclipse.wb.core.model.JavaInfo,java.lang.String,java.lang.String)",
				button,
				"getButton",
				"public");
		assertEditor(
				"public class Test extends JPanel {",
				"  private JButton button;",
				"  public Test() {",
				"    button = new JButton();",
				"    add(button);",
				"  }",
				"  public JButton getButton() {",
				"    return button;",
				"  }",
				"}");
	}

	/**
	 * @return the {@link IAction} for exposing given component, may be <code>null</code> if component
	 *         can not be exposed.
	 */
	private static IAction getExposeAction(ComponentInfo button) {
		// prepare manager
		MenuManager menuManager = getDesignerMenuManager();
		// add action
		String text = "Expose component...";
		ExposeComponentSupport.contribute(button, menuManager, text);
		return findChildAction(menuManager, text);
	}
}
