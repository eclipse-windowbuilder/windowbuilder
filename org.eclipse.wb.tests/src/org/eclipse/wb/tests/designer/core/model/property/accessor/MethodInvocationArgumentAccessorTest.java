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
package org.eclipse.wb.tests.designer.core.model.property.accessor;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.IAccessibleExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.MethodInvocationArgumentAccessor;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipProvider;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * Tests for {@link MethodInvocationArgumentAccessor}.
 *
 * @author scheglov_ke
 */
public class MethodInvocationArgumentAccessorTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Project creation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		// prepare MyPanel
		setFileContentSrc(
				"test/MyPanel.java",
				getTestSource(
						"public class MyPanel extends JPanel {",
						"  public void setText(String text, boolean html) {",
						"  }",
						"}"));
		setFileContentSrc(
				"test/MyPanel.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <methods>",
						"    <method name='setText'>",
						"      <parameter type='java.lang.String' name='text'/>",
						"      <parameter type='boolean' name='html'/>",
						"    </method>",
						"  </methods>",
						"  <method-property title='text' method='setText(java.lang.String,boolean)'/>",
						"</component>"));
		waitForAutoBuild();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_access() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends MyPanel {",
						"  public Test() {",
						"  }",
						"}");
		ExpressionAccessor accessor = getTestAccessor(panel, -1);
		// no adapters
		assertNull(accessor.getAdapter(null));
		assertNull(accessor.getAdapter(IAccessibleExpressionAccessor.class));
		assertNull(accessor.getAdapter(PropertyTooltipProvider.class));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// getExpression()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link MethodInvocationArgumentAccessor#getExpression(JavaInfo)}.
	 */
	@Test
	public void test_getExpression_noInvocation() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends MyPanel {",
						"  public Test() {",
						"  }",
						"}");
		ExpressionAccessor accessor = getTestAccessor(panel, -1);
		// do check
		assertNull(accessor.getExpression(panel));
	}

	/**
	 * Test for {@link MethodInvocationArgumentAccessor#getExpression(JavaInfo)}.
	 */
	@Test
	public void test_getExpression_hasInvocation() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends MyPanel {",
						"  public Test() {",
						"    setText(null, false);",
						"  }",
						"}");
		ExpressionAccessor accessor = getTestAccessor(panel, 1);
		// do check
		assertEquals("false", m_lastEditor.getSource(accessor.getExpression(panel)));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// setExpression()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link MethodInvocationArgumentAccessor#setExpression(JavaInfo, String)}.
	 */
	@Test
	public void test_setExpression_replaceExisting() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends MyPanel {",
						"  public Test() {",
						"    setText(null, false);",
						"  }",
						"}");
		ExpressionAccessor accessor = getTestAccessor(panel, 1);
		// do check
		accessor.setExpression(panel, "true");
		assertEditor(
				"public class Test extends MyPanel {",
				"  public Test() {",
				"    setText(null, true);",
				"  }",
				"}");
	}

	/**
	 * Test for {@link MethodInvocationArgumentAccessor#setExpression(JavaInfo, String)}.<br>
	 * Remove invocation because we set for first argument same source as its defaults, so all
	 * arguments become default.
	 */
	@Test
	public void test_setExpression_removeExisting_1() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends MyPanel {",
						"  public Test() {",
						"    setText('foo', false);",
						"  }",
						"}");
		ExpressionAccessor accessor = getTestAccessor(panel, 0);
		// do check
		accessor.setExpression(panel, "(String) null");
		assertEditor(
				"// filler filler filler",
				"public class Test extends MyPanel {",
				"  public Test() {",
				"  }",
				"}");
	}

	/**
	 * Test for {@link MethodInvocationArgumentAccessor#setExpression(JavaInfo, String)}.<br>
	 * Remove invocation because we reset (set default source) for first argument, so all arguments
	 * become default.
	 */
	@Test
	public void test_setExpression_removeExisting_2() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends MyPanel {",
						"  public Test() {",
						"    setText('foo', false);",
						"  }",
						"}");
		ExpressionAccessor accessor = getTestAccessor(panel, 0);
		// do check
		accessor.setExpression(panel, null);
		assertEditor(
				"// filler filler filler",
				"public class Test extends MyPanel {",
				"  public Test() {",
				"  }",
				"}");
	}

	/**
	 * Test for {@link MethodInvocationArgumentAccessor#setExpression(JavaInfo, String)}.
	 */
	@Test
	public void test_setExpression_addNew() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends MyPanel {",
						"  public Test() {",
						"  }",
						"}");
		ExpressionAccessor accessor = getTestAccessor(panel, 1);
		// do check
		accessor.setExpression(panel, "true");
		assertEditor(
				"// filler filler filler",
				"public class Test extends MyPanel {",
				"  public Test() {",
				"    setText((String) null, true);",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link MethodInvocationArgumentAccessor} for <code>MyPanel.setText()</code>.
	 */
	private ExpressionAccessor getTestAccessor(ContainerInfo panel, int index) throws Exception {
		Method method =
				ReflectionUtils.getMethodBySignature(
						panel.getDescription().getComponentClass(),
						"setText(java.lang.String,boolean)");
		return new MethodInvocationArgumentAccessor(method, index);
	}
}
