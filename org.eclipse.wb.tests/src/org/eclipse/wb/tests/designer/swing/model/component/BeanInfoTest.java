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
package org.eclipse.wb.tests.designer.swing.model.component;

import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.junit.Test;

import java.beans.BeanInfo;

/**
 * Test for special flags from {@link BeanInfo}.
 *
 * @author scheglov_ke
 */
public class BeanInfoTest extends SwingModelTest {
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
	// "isContainer" support
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Value <code>true</code> for "isContainer" can be ignored.
	 */
	@Test
	public void test_isContainer_true() throws Exception {
		prepareComponentFor_isContainer("Boolean.TRUE");
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends MyComponent {",
						"  public Test() {",
						"  }",
						"}");
		assertTrue(panel.hasLayout());
	}

	/**
	 * Value <code>false</code> for "isContainer" means that component is not container, so has no
	 * layout.
	 */
	@Test
	public void test_isContainer_false() throws Exception {
		prepareComponentFor_isContainer("Boolean.FALSE");
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends MyComponent {",
						"  public Test() {",
						"  }",
						"}");
		assertFalse(panel.hasLayout());
	}

	private void prepareComponentFor_isContainer(String value) throws Exception {
		setFileContentSrc(
				"test/MyComponent.java",
				getTestSource(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"public class MyComponent extends JPanel {",
						"}"));
		setFileContentSrc(
				"test/MyComponentBeanInfo.java",
				getTestSource(
						"import java.beans.*;",
						"public class MyComponentBeanInfo extends SimpleBeanInfo {",
						"  public BeanDescriptor getBeanDescriptor() {",
						"    BeanDescriptor descriptor = new BeanDescriptor(MyComponent.class);",
						"    descriptor.setValue('isContainer', " + value + ");",
						"    return descriptor;",
						"  }",
						"}"));
		waitForAutoBuild();
	}
}
