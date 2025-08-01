/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.core.model.parser;

import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.exception.ICoreExceptionConstants;
import org.eclipse.wb.internal.core.utils.state.EditorState.BadNodeInformation;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Tests for bad situations during parsing.
 *
 * @author scheglov_ke
 */
public class BadNodesTest extends SwingModelTest {
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
	 * If Eclipse is run using earlier version of JVM than version of {@link IJavaProject}, we should
	 * show warning about this.
	 */
	@Test
	public void test_incompatibleVersionJVM() throws Exception {
		EnvironmentUtils.setForcedJavaVersion(1.5f);
		try {
			parseContainer(
					"// filler filler filler filler filler",
					"public class Test extends JPanel {",
					"  public Test() {",
					"  }",
					"}");
			fail();
		} catch (DesignerException e) {
			assertEquals(ICoreExceptionConstants.PARSER_JAVA_VERSION, e.getCode());
			assertTrue(DesignerExceptionUtils.isWarning(e));
		} finally {
			EnvironmentUtils.setForcedJavaVersion(null);
		}
	}

	@Test
	public void test_emptyCompilationUnit() throws Exception {
		try {
			parseSource("test", "Test.java", "");
			fail();
		} catch (DesignerException e_) {
			DesignerException e = (DesignerException) DesignerExceptionUtils.getRootCause(e_);
			assertEquals(ICoreExceptionConstants.PARSER_NO_PRIMARY_TYPE, e.getCode());
		}
	}

	/**
	 * Parsing should fail because "text" is unknown.
	 */
	@Test
	public void test_badNodeCreation_unknownArgument() throws Exception {
		try {
			parseContainer(
					"class Test extends JPanel {",
					"  public Test(String text) {",
					"    JButton button = new JButton(text);",
					"    add(button);",
					"  }",
					"}");
			fail();
		} catch (Throwable e_) {
			DesignerException designerException =
					(DesignerException) DesignerExceptionUtils.getRootCause(e_);
			assertEquals(ICoreExceptionConstants.EVAL_NO_METHOD_INVOCATION, designerException.getCode());
		}
	}

	/**
	 * If we can not evaluate parameter, we should use default value.
	 */
	@Test
	public void test_badNodeCreation_exceptionInConstructorArgument() throws Exception {
		setFileContentSrc(
				"test/MyObject.java",
				getSource(
						"package test;",
						"public class MyObject {",
						"  public static String getText() {",
						"    throw new IllegalStateException();",
						"  }",
						"}"));
		waitForAutoBuild();
		// parse
		useStrictEvaluationMode(false);
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"    String text = MyObject.getText();",
						"    add(new JButton(text));",
						"  }",
						"}");
		assertHierarchy(
				"{this: javax.swing.JPanel} {this} {/add(new JButton(text))/}",
				"  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
				"  {new: javax.swing.JButton} {empty} {/add(new JButton(text))/}");
		// has logged refresh error
		{
			List<BadNodeInformation> badNodes = m_lastState.getBadRefreshNodes().nodes();
			Assertions.assertThat(badNodes).hasSize(1);
			BadNodeInformation badNode = badNodes.get(0);
			Throwable rootCause = DesignerExceptionUtils.getRootCause(badNode.getException());
			assertInstanceOf(IllegalStateException.class, rootCause);
		}
		// refresh
		panel.refresh();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		assertEquals(((JButton) button.getObject()).getText(), "<dynamic>");
	}

	/**
	 * Parsing is OK, because we specify value of parameter.
	 */
	@Test
	public void test_forcedMethodParameter() throws Exception {
		ContainerInfo panelInfo =
				parseContainer(
						"class Test extends JPanel {",
						"  /**",
						"  * Some leading comment...",
						"  * @wbp.eval.method.parameter text 'ab' + 'c'",
						"  * @param text 'abc'",
						"  * Some trailing comment...",
						"  */",
						"  public Test(String text) {",
						"    JButton button = new JButton(text);",
						"    add(button);",
						"  }",
						"}");
		panelInfo.refresh();
		assertNoErrors(m_lastParseInfo);
		// check Button text
		JPanel panelComponent = (JPanel) panelInfo.getComponent();
		JButton buttonComponent = (JButton) panelComponent.getComponent(0);
		assertEquals("abc", buttonComponent.getText());
		// clean up
		panelInfo.refresh_dispose();
	}

	/**
	 * Parsing is OK, but there is bad node because "text" is unknown.
	 */
	@Test
	public void test_badNodeInvocation() throws Exception {
		parseContainer(
				"class Test extends JPanel {",
				"  public Test(String text) {",
				"    JButton button = new JButton();",
				"    button.setText(text);",
				"    add(button);",
				"  }",
				"}");
		assertHierarchy(
				"{this: javax.swing.JPanel} {this} {/add(button)/}",
				"  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
				"  {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /button.setText(text)/ /add(button)/}");
		// check bad nodes
		List<BadNodeInformation> badParseNodes = m_lastState.getBadParserNodes().nodes();
		Assertions.assertThat(badParseNodes).hasSize(1);
		{
			BadNodeInformation badNodeInformation = badParseNodes.get(0);
			assertEquals("button.setText(text);", m_lastEditor.getSource(badNodeInformation.getNode()));
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Compilation errors
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Hard error - syntax error. Parsing stops.
	 */
	@Test
	public void test_error_syntax_1() throws Exception {
		m_ignoreCompilationProblems = true;
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    add(new JBu();",
						"    add(new JButton();",
						"  }",
						"}");
		assertEquals(0, panel.getChildrenComponents().size());
	}

	/**
	 * Hard error - syntax error. Parsing stops.
	 */
	@Test
	public void test_error_syntax_2() throws Exception {
		m_ignoreCompilationProblems = true;
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    {",
						"      add(new JBu();",
						"    }",
						"    add(new JButton();",
						"  }",
						"}");
		assertEquals(0, panel.getChildrenComponents().size());
	}

	/**
	 * Test for case when syntax is correct, but not existing class is used.<br>
	 * No other components.
	 */
	@Test
	public void test_error_noSuchClass_1() throws Exception {
		// parsing is important
		m_ignoreCompilationProblems = true;
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    add(new NoSuchClass());",
						"  }",
						"}");
		assertEquals(0, panel.getChildrenComponents().size());
		// rendering is also important
		panel.refresh();
		assertNotNull(panel.getObject());
	}

	/**
	 * Test for case when syntax is correct, but not existing class is used.<br>
	 * Other component can be parsed.
	 */
	@Test
	public void test_error_noSuchClass_2() throws Exception {
		// parsing is important
		m_ignoreCompilationProblems = true;
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    add(new NoSuchClass());",
						"    add(new JButton());",
						"  }",
						"}");
		// not existing class ignored, but other is parsed
		assertEquals(1, panel.getChildrenComponents().size());
		// rendering is also important
		panel.refresh();
		assertNotNull(panel.getObject());
	}

	/**
	 * Invalid method is invoked, it just ignore, other things are parsed and executed.
	 */
	@Test
	public void test_error_noSuchMethod() throws Exception {
		// parsing is important
		m_ignoreCompilationProblems = true;
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton button = new JButton();",
						"    button.setEnaAabled(false);",
						"    button.setSelected(true);",
						"    add(button);",
						"  }",
						"}");
		// not existing class ignored, but other is parsed
		assertEquals(1, panel.getChildrenComponents().size());
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// rendering is also important
		panel.refresh();
		assertNotNull(panel.getObject());
		{
			JButton buttonObject = (JButton) button.getObject();
			// "setEnaAabled(false)" is invalid, so JButton stays enabled
			assertTrue(buttonObject.isEnabled());
			// "setSelected(true)" is however valid, so executed
			assertTrue(buttonObject.isSelected());
		}
	}

	/**
	 * Eric requested to ignore such problems.
	 * <p>
	 * When we have compilation problem, this may cause typeBinding/variable exceptions.
	 */
	@Test
	public void test_noSuchType_forVariable() throws Exception {
		m_ignoreCompilationProblems = true;
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  private Foo foo = null;",
						"  public Test() {",
						"    JButton button = new JButton();",
						"    add(button);",
						"  }",
						"}");
		assertHierarchy(
				"{this: javax.swing.JPanel} {this} {/add(button)/}",
				"  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
				"  {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /add(button)/}");
		// refresh
		panel.refresh();
		assertNoErrors(panel);
	}

	/**
	 * {@link MethodDeclaration} with compilation errors does not have {@link IMethodBinding}. We
	 * ignore this.
	 */
	@Test
	public void test_noMethodBinding_noInvocation() throws Exception {
		m_ignoreCompilationProblems = true;
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setEnabled(true);",
						"  }",
						"  private badMethod(Foo foo) {",
						"  }",
						"}");
		// check hierarchy
		assertHierarchy(
				"{this: javax.swing.JPanel} {this} {/setEnabled(true)/}",
				"  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
		// refresh
		panel.refresh();
		assertNoErrors(panel);
	}

	/**
	 * {@link MethodDeclaration} with compilation errors does not have {@link IMethodBinding}. We
	 * ignore this. We don't enter this method because we can not find it, so not related nodes from
	 * it.
	 */
	@Test
	public void test_noMethodBinding_itIsInvoked() throws Exception {
		m_ignoreCompilationProblems = true;
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    badMethod(null);",
						"  }",
						"  private badMethod(Foo foo) {",
						"    setEnabled(true);",
						"  }",
						"}");
		// check hierarchy
		assertHierarchy(
				"{this: javax.swing.JPanel} {this} {}",
				"  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
		// refresh
		panel.refresh();
		assertNoErrors(panel);
	}

	/**
	 * We should throw {@link DesignerException} with good information for user.
	 */
	@Test
	public void test_doubleAssociationException() throws Exception {
		m_ignoreCompilationProblems = true;
		try {
			parseContainer(
					"public class Test extends JPanel {",
					"  public Test() {",
					"    JPanel panel2 = new JPanel();",
					"    add(panel2);",
					"    //",
					"    JButton button = new JButton();",
					"    add(button);",
					"    panel2.add(button);",
					"  }",
					"}");
			fail();
		} catch (Throwable e) {
			DesignerException de = DesignerExceptionUtils.getDesignerException(e);
			assertEquals(ICoreExceptionConstants.PARSER_DOUBLE_ASSOCIATION, de.getCode());
			assertTrue(DesignerExceptionUtils.isWarning(e));
		}
	}
}
