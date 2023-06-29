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

import com.google.common.collect.ImmutableList;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.util.factory.FactoryActionsSupport;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;

import java.util.Arrays;

/**
 * Tests for {@link FactoryActionsSupport}.
 *
 * @author scheglov_ke
 */
public class FactoryActionsSupportTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Previous type names
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_previousTypeNames() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		// check empty
		clearPreviousTypeNames(panel);
		assertTrue(Arrays.equals(new String[]{}, getPreviousTypeNames(panel)));
		// add several
		addPreviousTypeName(panel, "a");
		assertTrue(Arrays.equals(new String[]{"a"}, getPreviousTypeNames(panel)));
		addPreviousTypeName(panel, "b");
		assertTrue(Arrays.equals(new String[]{"b", "a"}, getPreviousTypeNames(panel)));
		addPreviousTypeName(panel, "c");
		assertTrue(Arrays.equals(new String[]{"c", "b", "a"}, getPreviousTypeNames(panel)));
		// add existing, so reorder
		addPreviousTypeName(panel, "b");
		assertTrue(Arrays.equals(new String[]{"b", "c", "a"}, getPreviousTypeNames(panel)));
		// add more, up to removing oldest
		addPreviousTypeName(panel, "1");
		assertTrue(Arrays.equals(new String[]{"1", "b", "c", "a"}, getPreviousTypeNames(panel)));
		addPreviousTypeName(panel, "2");
		assertTrue(Arrays.equals(new String[]{"2", "1", "b", "c", "a"}, getPreviousTypeNames(panel)));
		addPreviousTypeName(panel, "3");
		assertTrue(Arrays.equals(new String[]{"3", "2", "1", "b", "c"}, getPreviousTypeNames(panel)));
		// clear
		clearPreviousTypeNames(panel);
	}

	private static void clearPreviousTypeNames(JavaInfo component) throws Exception {
		ReflectionUtils.invokeMethod2(
				FactoryActionsSupport.class,
				"clearPreviousTypeNames",
				JavaInfo.class,
				component);
	}

	private static String[] getPreviousTypeNames(JavaInfo component) throws Exception {
		return (String[]) ReflectionUtils.invokeMethod2(
				FactoryActionsSupport.class,
				"getPreviousTypeNames",
				JavaInfo.class,
				component);
	}

	private static void addPreviousTypeName(JavaInfo component, String typeName) throws Exception {
		ReflectionUtils.invokeMethod2(
				FactoryActionsSupport.class,
				"addPreviousTypeName",
				JavaInfo.class,
				String.class,
				component,
				typeName);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Actions
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test that factories from same package are automatically added to menu.
	 */
	public void test_actions_thisPackage() throws Exception {
		setFileContentSrc(
				"test/StaticFactory.java",
				getTestSource(
						"public final class StaticFactory {",
						"  public static JButton create_1(String text) {",
						"    return new JButton(text);",
						"  }",
						"  public static JButton create_2(String text) {",
						"    return new JButton(text);",
						"  }",
						"}"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    add(new JButton());",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// no "Create Factory..." expected for "panel" - wrong variable
		assertNull(getFactoryManager(panel));
		// check "Factory" sub-menu
		{
			IMenuManager factoryManager = getFactoryManager(button);
			assertNotNull(factoryManager);
			assertNotNull(findChildAction(factoryManager, "Create factory..."));
			{
				IAction action = findChildAction(factoryManager, "StaticFactory.create_1(...)");
				assertNotNull(action);
				assertNotNull(action.getImageDescriptor());
			}
			{
				IAction action = findChildAction(factoryManager, "StaticFactory.create_2(...)");
				assertNotNull(action);
				assertNotNull(action.getImageDescriptor());
			}
		}
	}

	/**
	 * Test that factories from history are also added to menu.
	 */
	public void test_actions_fromHistory() throws Exception {
		setFileContentSrc(
				"test2/SecondStaticFactory.java",
				getSourceDQ(
						"package test2;",
						"import javax.swing.*;",
						"public final class SecondStaticFactory {",
						"  public static JButton create_3(String text) {",
						"    return new JButton(text);",
						"  }",
						"}"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    add(new JButton());",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// no factory in history, so no such action
		clearPreviousTypeNames(button);
		{
			IMenuManager factoryManager = getFactoryManager(button);
			assertNotNull(factoryManager);
			assertNotNull(findChildAction(factoryManager, "Select factory..."));
			assertNull(findChildAction(factoryManager, "SecondStaticFactory.create_3(...)"));
		}
		// factory is in history, so we have action
		try {
			addPreviousTypeName(button, "test2.SecondStaticFactory");
			//
			IMenuManager factoryManager = getFactoryManager(button);
			assertNotNull(factoryManager);
			assertNotNull(findChildAction(factoryManager, "Select factory..."));
			assertNotNull(findChildAction(factoryManager, "SecondStaticFactory.create_3(...)"));
		} finally {
			clearPreviousTypeNames(button);
		}
	}

	/**
	 * Only factory methods that have compatible return type, can be added to menu.
	 */
	public void test_actions_onlyCompatibleTypes() throws Exception {
		setFileContentSrc(
				"test/StaticFactory.java",
				getTestSource(
						"public final class StaticFactory {",
						"  public static JButton createButton(String text) {",
						"    return new JButton(text);",
						"  }",
						"  public static JComponent createComponent(String text) {",
						"    return new JButton(text);",
						"  }",
						"}"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    add(new JButton());",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// check "Factory" sub-menu
		{
			IMenuManager factoryManager = getFactoryManager(button);
			assertNotNull(factoryManager);
			assertNotNull(findChildAction(factoryManager, "Create factory..."));
			assertNotNull(findChildAction(factoryManager, "StaticFactory.createButton(...)"));
			assertNull(findChildAction(factoryManager, "StaticFactory.createComponent(...)"));
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the "Factory" {@link IMenuManager} for creating/applying factory from component, may be
	 *         <code>null</code> if component can not be converted into factory.
	 */
	private IMenuManager getFactoryManager(ComponentInfo component) throws Exception {
		MenuManager menuManager = getDesignerMenuManager();
		component.getBroadcastObject().addContextMenu(
				ImmutableList.of(component),
				component,
				menuManager);
		return findChildMenuManager(menuManager, "Factory");
	}
}
