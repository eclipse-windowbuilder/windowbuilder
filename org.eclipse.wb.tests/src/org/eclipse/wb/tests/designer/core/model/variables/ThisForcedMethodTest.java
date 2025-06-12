/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.tests.designer.core.model.variables;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.generation.GenerationSettings;
import org.eclipse.wb.internal.core.model.variable.ThisVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.swing.ToolkitProvider;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.LayoutInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jface.preference.IPreferenceStore;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.JFrame;

/**
 * Tests for {@link JavaInfoUtils#getTarget(JavaInfo, JavaInfo)} and {@link ThisVariableSupport}.
 *
 * @author scheglov_ke
 */
public class ThisForcedMethodTest extends SwingModelTest {
	private static final IPreferenceStore PREFERENCES = ToolkitProvider.DESCRIPTION.getPreferences();

	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@BeforeEach
	public void setUp() throws Exception {
		super.setUp();
		PREFERENCES.setValue(GenerationSettings.P_FORCED_METHOD, "init");
	}

	@Override
	@AfterEach
	public void tearDown() throws Exception {
		PREFERENCES.setToDefault(GenerationSettings.P_FORCED_METHOD);
		super.tearDown();
	}

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
	// Forced method
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Not a "this" component as root.
	 */
	@Test
	public void test_getTarget_bad_rootIsNotThis() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test {",
						"  public static void main(String args[]) {",
						"    JPanel panel = new JPanel();",
						"  }",
						"}");
		// prepare target
		StatementTarget target = JavaInfoUtils.getTarget(panel, null);
		assertEditor(
				"public class Test {",
				"  public static void main(String args[]) {",
				"    JPanel panel = new JPanel();",
				"  }",
				"}");
		// check target
		Statement expectedStatement = getStatement(panel, "main(java.lang.String[])", 0);
		assertTarget(target, null, expectedStatement, false);
	}

	/**
	 * Method invoked at the end of constructor has different name than "forced", so we don't use it.
	 * We also don't create new "forced" method, because there are related node outside of
	 * constructor.
	 */
	@Test
	public void test_getTarget_bad_notInitMethod() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    otherMethod();",
						"  }",
						"  private void otherMethod() {",
						"    setBackground(Color.ORANGE);",
						"  }",
						"}");
		// prepare target
		StatementTarget target = JavaInfoUtils.getTarget(panel, null);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    otherMethod();",
				"  }",
				"  private void otherMethod() {",
				"    setBackground(Color.ORANGE);",
				"  }",
				"}");
		// check target
		Statement expectedStatement = getStatement(panel, "otherMethod()", 0);
		assertTarget(target, null, expectedStatement, false);
	}

	/**
	 * Empty constructor, no any child or statement.
	 */
	@Test
	public void test_getTarget_emptyConstructor() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		// check target
		StatementTarget target = JavaInfoUtils.getTarget(panel, null);
		assertEditor(
				"// filler filler filler",
				"public class Test extends JPanel {",
				"  public Test() {",
				"    init();",
				"  }",
				"  private void init() {",
				"  }",
				"}");
		assertTarget(target, getMethod("init()").getBody(), null, false);
	}

	/**
	 * Constructor, with related statements and "super" constructor invocation.
	 */
	@Test
	public void test_getTarget_withSuper() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    super();",
						"    setBackground(Color.ORANGE);",
						"  }",
						"}");
		// check target
		StatementTarget target = JavaInfoUtils.getTarget(panel, null);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    super();",
				"    init();",
				"  }",
				"  private void init() {",
				"    setBackground(Color.ORANGE);",
				"  }",
				"}");
		assertTarget(target, getMethod("init()").getBody(), null, false);
	}

	/**
	 * Constructor, with setLayout(), so {@link LayoutInfo} child.
	 */
	@Test
	public void test_getTarget_withChild() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    super();",
						"    int hgap = 5;",
						"    int vgap = 10;",
						"    setLayout(new BorderLayout(hgap, vgap));",
						"  }",
						"}");
		// check target
		StatementTarget target = JavaInfoUtils.getTarget(panel, null);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    super();",
				"    init();",
				"  }",
				"  private void init() {",
				"    int hgap = 5;",
				"    int vgap = 10;",
				"    setLayout(new BorderLayout(hgap, vgap));",
				"  }",
				"}");
		assertTarget(target, getMethod("init()").getBody(), null, false);
	}

	/**
	 * Constructor of {@link JFrame}, ask for target on "contentPane".
	 */
	@Test
	public void test_getTarget_askForContentPane() throws Exception {
		ContainerInfo frame =
				parseContainer(
						"public class Test extends JFrame {",
						"  public Test() {",
						"    super();",
						"    getContentPane().setBackground(Color.ORANGE);",
						"  }",
						"}");
		ContainerInfo contentPane = (ContainerInfo) frame.getChildrenComponents().get(0);
		// check target
		StatementTarget target = JavaInfoUtils.getTarget(contentPane, null);
		assertEditor(
				"public class Test extends JFrame {",
				"  public Test() {",
				"    super();",
				"    init();",
				"  }",
				"  private void init() {",
				"    getContentPane().setBackground(Color.ORANGE);",
				"  }",
				"}");
		assertTarget(target, getMethod("init()").getBody(), null, false);
	}

	/**
	 * Constructor of {@link JFrame}, ask for target on "contentPane".
	 * <p>
	 * Case when {@link JFrame#setContentPane(java.awt.Container)} is used to replace standard content
	 * pane with custom one.
	 */
	@Test
	public void test_getTarget_askForContentPane_setContentPane() throws Exception {
		ContainerInfo frame =
				parseContainer(
						"public class Test extends JFrame {",
						"  JPanel contentPane = new JPanel();",
						"  public Test() {",
						"    setContentPane(contentPane);",
						"    contentPane.setBackground(Color.ORANGE);",
						"  }",
						"}");
		ContainerInfo contentPane = (ContainerInfo) frame.getChildrenComponents().get(0);
		// check target
		StatementTarget target = JavaInfoUtils.getTarget(contentPane, null);
		assertEditor(
				"public class Test extends JFrame {",
				"  JPanel contentPane = new JPanel();",
				"  public Test() {",
				"    init();",
				"  }",
				"  private void init() {",
				"    setContentPane(contentPane);",
				"    contentPane.setBackground(Color.ORANGE);",
				"  }",
				"}");
		assertTarget(target, getMethod("init()").getBody(), null, false);
	}

	@Test
	public void test_getTarget_existingFilled() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    init();",
						"  }",
						"  private void init() {",
						"    setEnabled(true);",
						"    int foo;",
						"  }",
						"}");
		String expectedSource = m_lastEditor.getSource();
		// check target
		StatementTarget target = JavaInfoUtils.getTarget(panel, null);
		assertEditor(expectedSource, m_lastEditor);
		assertTarget(target, getMethod("init()").getBody(), null, false);
	}

	@Test
	public void test_getTarget_existingEmpty() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    init();",
						"  }",
						"  private void init() {",
						"  }",
						"}");
		String expectedSource = m_lastEditor.getSource();
		// check target
		StatementTarget target = JavaInfoUtils.getTarget(panel, null);
		assertEditor(expectedSource, m_lastEditor);
		assertTarget(target, getMethod("init()").getBody(), null, false);
	}

	@Test
	public void test_getTarget_existingEmpty_tryStatement() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    try {",
						"      init();",
						"    } catch (Throwable e) {",
						"    }",
						"  }",
						"  private void init() {",
						"  }",
						"}");
		String expectedSource = m_lastEditor.getSource();
		// check target
		StatementTarget target = JavaInfoUtils.getTarget(panel, null);
		assertEditor(expectedSource, m_lastEditor);
		assertTarget(target, getMethod("init()").getBody(), null, false);
	}

	/**
	 * Method invoked at the end of constructor has different name than "forced", so we don't use it.
	 * We create new "forced" method.
	 */
	@Test
	public void test_getTarget_existingEmpty_withDifferentName() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    init2();",
						"  }",
						"  private void init2() {",
						"  }",
						"}");
		// check target
		StatementTarget target = JavaInfoUtils.getTarget(panel, null);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    init();",
				"  }",
				"  private void init() {",
				"    init2();",
				"  }",
				"  private void init2() {",
				"  }",
				"}");
		assertTarget(target, getMethod("init()").getBody(), null, false);
	}

	/**
	 * When parent is "lazy", we should ignore "forced method".
	 */
	@Test
	public void test_getTarget_lazy() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  private JPanel inner;",
						"  public Test() {",
						"    init();",
						"  }",
						"  private void init() {",
						"    add(getInner());",
						"  }",
						"  private JPanel getInner() {",
						"    if (inner == null) {",
						"      inner = new JPanel();",
						"    }",
						"    return inner;",
						"  }",
						"}");
		panel.refresh();
		ContainerInfo inner = getJavaInfoByName("inner");
		// check target
		String expectedSource = m_lastEditor.getSource();
		StatementTarget target = JavaInfoUtils.getTarget(inner, null);
		assertEditor(expectedSource, m_lastEditor);
		assertTarget(target, null, getStatement(inner, 0), false);
	}

	/**
	 * We should use "forced" target only for initially empty "this" container, but not when there is
	 * already some inner container.
	 */
	@Test
	public void test_getTarget_forInnerContainer() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    init();",
						"  }",
						"  private void init() {",
						"    {",
						"      JPanel inner = new JPanel();",
						"      add(inner);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		ContainerInfo inner = getJavaInfoByName("inner");
		// check target
		String expectedSource = m_lastEditor.getSource();
		StatementTarget target = JavaInfoUtils.getTarget(inner, null);
		assertEditor(expectedSource, m_lastEditor);
		assertTarget(target, null, getStatement(inner, 1), false);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// getStatementTarget
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * {@link ThisVariableSupport#getStatementTarget()} also uses "forced" method.
	 */
	@Test
	public void test_getStatementTarget_emptyConstructor() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		ThisVariableSupport variableSupport = (ThisVariableSupport) panel.getVariableSupport();
		// check target
		StatementTarget target = variableSupport.getStatementTarget();
		assertEditor(
				"// filler filler filler",
				"public class Test extends JPanel {",
				"  public Test() {",
				"    init();",
				"  }",
				"  private void init() {",
				"  }",
				"}");
		assertTarget(target, getMethod("init()").getBody(), null, true);
	}

	/**
	 * {@link ThisVariableSupport#getStatementTarget()} also uses "forced" method, filled constructor.
	 */
	@Test
	public void test_getStatementTarget_filledConstructor() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setEnabled(true);",
						"  }",
						"}");
		ThisVariableSupport variableSupport = (ThisVariableSupport) panel.getVariableSupport();
		// check target
		StatementTarget target = variableSupport.getStatementTarget();
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    init();",
				"  }",
				"  private void init() {",
				"    setEnabled(true);",
				"  }",
				"}");
		assertTarget(target, getMethod("init()").getBody(), null, true);
	}

	/**
	 * {@link ThisVariableSupport#getStatementTarget()} also uses "forced" method, when it already
	 * exists.
	 */
	@Test
	public void test_getStatementTarget_existingForced() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    init();",
						"  }",
						"  private void init() {",
						"    setEnabled(false);",
						"  }",
						"}");
		String expectedSource = m_lastEditor.getSource();
		ThisVariableSupport variableSupport = (ThisVariableSupport) panel.getVariableSupport();
		// check target
		StatementTarget target = variableSupport.getStatementTarget();
		assertEditor(expectedSource, m_lastEditor);
		assertTarget(target, getMethod("init()").getBody(), null, true);
	}

	/**
	 * {@link ThisVariableSupport#getStatementTarget()} also uses "forced" method, when it already
	 * exists, and even if there is {@link SuperConstructorInvocation} in constructor.
	 */
	@Test
	public void test_getStatementTarget_existingForced_withSuper() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    super();",
						"    init();",
						"  }",
						"  private void init() {",
						"    setEnabled(false);",
						"  }",
						"}");
		String expectedSource = m_lastEditor.getSource();
		ThisVariableSupport variableSupport = (ThisVariableSupport) panel.getVariableSupport();
		// check target
		StatementTarget target = variableSupport.getStatementTarget();
		assertEditor(expectedSource, m_lastEditor);
		assertTarget(target, getMethod("init()").getBody(), null, true);
	}

	/**
	 * {@link ThisVariableSupport#getStatementTarget()} also uses "forced" method, when it already
	 * exists, and even if there are other statements.
	 */
	@Test
	public void test_getStatementTarget_existingForced_otherStatements() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    int foo;",
						"    int bar;",
						"    init();",
						"  }",
						"  private void init() {",
						"    setEnabled(false);",
						"  }",
						"}");
		String expectedSource = m_lastEditor.getSource();
		ThisVariableSupport variableSupport = (ThisVariableSupport) panel.getVariableSupport();
		// check target
		StatementTarget target = variableSupport.getStatementTarget();
		assertEditor(expectedSource, m_lastEditor);
		assertTarget(target, getMethod("init()").getBody(), null, true);
	}

	/**
	 * One of the components created in field.
	 */
	@Test
	public void test_getStatementTarget_fieldInitializer() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  private final JButton button = new JButton();",
						"  public Test() {",
						"    add(button);",
						"  }",
						"}");
		ThisVariableSupport variableSupport = (ThisVariableSupport) panel.getVariableSupport();
		// check target
		StatementTarget target = variableSupport.getStatementTarget();
		assertEditor(
				"public class Test extends JPanel {",
				"  private final JButton button = new JButton();",
				"  public Test() {",
				"    init();",
				"  }",
				"  private void init() {",
				"    add(button);",
				"  }",
				"}");
		assertTarget(target, getMethod("init()").getBody(), null, true);
	}

	/**
	 * We should not move {@link Statement}'s which reference constructor parameters.
	 */
	@Test
	public void test_getStatementTarget_constructorParameters() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test(int a) {",
						"    System.out.println(a);",
						"    int foo;",
						"  }",
						"}");
		ThisVariableSupport variableSupport = (ThisVariableSupport) panel.getVariableSupport();
		// check target
		StatementTarget target = variableSupport.getStatementTarget();
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test(int a) {",
				"    System.out.println(a);",
				"    init();",
				"  }",
				"  private void init() {",
				"    int foo;",
				"  }",
				"}");
		assertTarget(target, getMethod("init()").getBody(), null, true);
	}
}
