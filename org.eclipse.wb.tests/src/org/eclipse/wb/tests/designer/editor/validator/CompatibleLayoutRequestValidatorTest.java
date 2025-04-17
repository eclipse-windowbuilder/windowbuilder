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
package org.eclipse.wb.tests.designer.editor.validator;

import org.eclipse.wb.core.gef.policy.validator.CompatibleLayoutRequestValidator;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.junit.Test;

/**
 * Test {@link CompatibleLayoutRequestValidator}.
 *
 * @author scheglov_ke
 */
public class CompatibleLayoutRequestValidatorTest extends AbstractLayoutRequestValidatorTest {
	private static final ILayoutRequestValidator validator =
			CompatibleLayoutRequestValidator.INSTANCE;

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
	public void test_notJavaInfo() throws Exception {
		parseContainer(
				"// filler filler filler",
				"public class Test extends JPanel {",
				"  public Test() {",
				"  }",
				"}");
		Object parent = new Object();
		Object child = new Object();
		// validate
		assert_validateCMA(validator, true, parent, child);
	}

	@Test
	public void test_noChecks() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button = createJButton();
		// validate
		assert_validateCMA(validator, true, panel, button);
	}

	@Test
	public void test_childNotJavaInfo() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		// validate
		Object child = new Object();
		assert_validateCMA(validator, true, panel, child);
	}

	@Test
	public void test_parentNotJavaInfo() throws Exception {
		parseContainer(
				"// filler filler filler",
				"public class Test extends JPanel {",
				"  public Test() {",
				"  }",
				"}");
		//
		Object parent = new Object();
		ComponentInfo child = createJButton();
		assert_validateCMA(validator, true, parent, child);
	}

	@Test
	public void test_parentScript_alwaysTrue() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		JavaInfoUtils.setParameter(panel, "GEF.requestValidator.parent", "true");
		// validate
		{
			ComponentInfo button = createJButton();
			assert_validateCMA(validator, true, panel, button);
		}
	}

	@Test
	public void test_parentScript_alwaysFalse() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		JavaInfoUtils.setParameter(panel, "GEF.requestValidator.parent", "false");
		ComponentInfo button = createJButton();
		// validate
		assert_validateCMA(validator, false, panel, button);
	}

	@Test
	public void test_parentScript_acceptOnlyJButton() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		JavaInfoUtils.setParameter(
				panel,
				"GEF.requestValidator.parent",
				"isComponentType(child, 'javax.swing.JButton')");
		// false: JTextField
		{
			ComponentInfo textField = createComponent("javax.swing.JTextField");
			assert_validateCMA(validator, false, panel, textField);
		}
		// true: JButton
		{
			ComponentInfo button = createComponent("javax.swing.JButton");
			assert_validateCMA(validator, true, panel, button);
		}
	}

	@Test
	public void test_childScript_canBeDroppedOnlyOnJPanel() throws Exception {
		ContainerInfo frame =
				parseContainer(
						"public class Test extends JFrame {",
						"  public Test() {",
						"    JPanel panel = new JPanel();",
						"    getContentPane().add(panel);",
						"  }",
						"}");
		// prepare JButton
		ComponentInfo button = createJButton();
		JavaInfoUtils.setParameter(
				button,
				"GEF.requestValidator.child",
				"isComponentType(parent, 'javax.swing.JPanel')");
		// false: on JFrame
		{
			assert_validateCMA(validator, false, frame, button);
		}
		// true: on JPanel
		{
			ContainerInfo contentPane = (ContainerInfo) frame.getChildrenComponents().get(0);
			ComponentInfo panel = contentPane.getChildrenComponents().get(0);
			assert_validateCMA(validator, true, panel, button);
		}
	}

	@Test
	public void test_childWithJPanelConstructor() throws Exception {
		setFileContentSrc(
				"test/MyButton.java",
				getTestSource(
						"public class MyButton extends JButton {",
						"  public MyButton(JPanel parent) {",
						"  }",
						"}"));
		setFileContentSrc(
				"test/MyButton.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <creation>",
						"    <source><![CDATA[new test.MyButton(%parent%)]]></source>",
						"  </creation>",
						"  <constructors>",
						"    <constructor>",
						"      <parameter type='javax.swing.JPanel' parent='true'/>",
						"    </constructor>",
						"  </constructors>",
						"</component>"));
		waitForAutoBuild();
		// parse
		ContainerInfo frame =
				parseContainer(
						"public class Test extends JFrame {",
						"  public Test() {",
						"    JPanel panel = new JPanel();",
						"    getContentPane().add(panel);",
						"  }",
						"}");
		// prepare MyButton
		ComponentInfo button = createComponent("test.MyButton");
		// false: on JFrame
		{
			assert_validateCMA(validator, false, frame, button);
		}
		// true: on JPanel
		{
			ContainerInfo contentPane = (ContainerInfo) frame.getChildrenComponents().get(0);
			ComponentInfo panel = contentPane.getChildrenComponents().get(0);
			assert_validateCMA(validator, true, panel, button);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// with ComponentDescription
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_paste_noChecks() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    add(new JButton());",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// validate
		assert_validatePasteRequest(validator, true, panel, button);
	}
}
