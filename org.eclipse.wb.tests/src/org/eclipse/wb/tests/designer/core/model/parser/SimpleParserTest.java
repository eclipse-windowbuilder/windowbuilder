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
package org.eclipse.wb.tests.designer.core.model.parser;

import org.eclipse.wb.internal.core.parser.ParseFactoryNoModel;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Simples tests of parser to use during optimizations.
 *
 * @author scheglov_ke
 */
public class SimpleParserTest extends SwingModelTest {
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
	// JPanel
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Empty "this" {@link JPanel}.
	 */
	public void test_thisPanel_empty() throws Exception {
		parseContainer(
				"// filler filler filler",
				"public class Test extends JPanel {",
				"  public Test() {",
				"  }",
				"}");
		assertHierarchy(
				"{this: javax.swing.JPanel} {this} {}",
				"  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
	}

	/**
	 * "This" {@link JPanel} with single local {@link JButton}.
	 */
	public void test_thisPanel_withButton() throws Exception {
		parseContainer(
				"// filler filler filler",
				"public class Test extends JPanel {",
				"  public Test() {",
				"    {",
				"      JButton button = new JButton();",
				"      add(button);",
				"    }",
				"  }",
				"}");
		assertHierarchy(
				"{this: javax.swing.JPanel} {this} {/add(button)/}",
				"  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
				"  {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /add(button)/}");
	}

	/**
	 * "This" {@link JPanel} with single local {@link JButton}.
	 */
	public void test_thisPanel_withButton_casted() throws Exception {
		parseContainer(
				"// filler filler filler",
				"public class Test extends JPanel {",
				"  public Test() {",
				"    {",
				"      JButton button = (JButton) new JButton();",
				"      add(button);",
				"    }",
				"  }",
				"}");
		assertHierarchy(
				"{this: javax.swing.JPanel} {this} {/add(button)/}",
				"  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
				"  {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /(JButton) new JButton()/ /add(button)/}");
	}

	/**
	 * Test for parsing materialized implicit layout with {@link CastExpression}.
	 */
	public void test_thisPanel_implicitLayout_casted() throws Exception {
		parseContainer(
				"// filler filler filler",
				"class Test extends JPanel {",
				"  public Test() {",
				"    FlowLayout flowLayout = (FlowLayout) getLayout();",
				"  }",
				"}");
		assertHierarchy(
				"{this: javax.swing.JPanel} {this} {}",
				"  {implicit-layout: java.awt.FlowLayout} {local-unique: flowLayout} {/getLayout()/ /(FlowLayout) getLayout()/}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// JFrame
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Empty "this" {@link JFrame}.
	 */
	public void test_thisFrame_empty() throws Exception {
		parseContainer(
				"// filler filler filler",
				"public class Test extends JFrame {",
				"  public Test() {",
				"  }",
				"}");
		assertHierarchy(
				"{this: javax.swing.JFrame} {this} {}",
				"  {method: public java.awt.Container javax.swing.JFrame.getContentPane()} {property} {}",
				"    {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}");
	}

	/**
	 * "This" {@link JFrame} with single local {@link JButton}.
	 */
	public void test_thisFrame_withButton() throws Exception {
		parseContainer(
				"// filler filler filler",
				"public class Test extends JFrame {",
				"  public Test() {",
				"    {",
				"      JButton button = new JButton();",
				"      getContentPane().add(button);",
				"    }",
				"  }",
				"}");
		assertHierarchy(
				"{this: javax.swing.JFrame} {this} {}",
				"  {method: public java.awt.Container javax.swing.JFrame.getContentPane()} {property} {/getContentPane().add(button)/}",
				"    {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}",
				"    {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /getContentPane().add(button)/}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Lazy
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_lazy() throws Exception {
		parseContainer(
				"public class Test extends JPanel {",
				"  private JButton button;",
				"  public Test() {",
				"    add(getButton());",
				"  }",
				"  private JButton getButton() {",
				"    if (button == null) {",
				"      button = new JButton();",
				"    }",
				"    return button;",
				"  }",
				"}");
		assertHierarchy(
				"{this: javax.swing.JPanel} {this} {/add(getButton())/}",
				"  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
				"  {new: javax.swing.JButton} {lazy: button getButton()} {/new JButton()/ /button/ /add(getButton())/}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Exposed field
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test that we can access exposed component, using {@link SimpleName}.
	 */
	public void test_exposedField_reference_SimpleName() throws Exception {
		setFileContentSrc(
				"test/MyPanel.java",
				getTestSource(
						"public class MyPanel extends JPanel {",
						"  public final JPanel m_container;",
						"  public MyPanel() {",
						"    setLayout(new BorderLayout());",
						"    {",
						"      m_container = new JPanel();",
						"      add(m_container, BorderLayout.CENTER);",
						"    }",
						"  }",
						"}"));
		waitForAutoBuild();
		// parse
		parseContainer(
				"public class Test extends MyPanel {",
				"  public Test() {",
				"    m_container.setEnabled(false);",
				"  }",
				"}");
		assertHierarchy(
				"{this: test.MyPanel} {this} {}",
				"  {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}",
				"  {field: javax.swing.JPanel} {m_container} {/m_container.setEnabled(false)/}",
				"    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
	}

	/**
	 * Test that we can access exposed component, using {@link QualifiedName}.
	 */
	public void test_exposedField_reference_QualifiedName() throws Exception {
		setFileContentSrc(
				"test/MyPanel.java",
				getTestSource(
						"public class MyPanel extends JPanel {",
						"  public final JPanel m_container;",
						"  public MyPanel() {",
						"    setLayout(new BorderLayout());",
						"    {",
						"      m_container = new JPanel();",
						"      add(m_container, BorderLayout.CENTER);",
						"    }",
						"  }",
						"}"));
		waitForAutoBuild();
		// parse
		parseContainer(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    MyPanel myPanel = new MyPanel();",
				"    add(myPanel);",
				"    myPanel.m_container.setEnabled(false);",
				"  }",
				"}");
		assertHierarchy(
				"{this: javax.swing.JPanel} {this} {/add(myPanel)/}",
				"  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
				"  {new: test.MyPanel} {local-unique: myPanel} {/new MyPanel()/ /add(myPanel)/ /myPanel.m_container/}",
				"    {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}",
				"    {field: javax.swing.JPanel} {m_container} {/myPanel.m_container.setEnabled(false)/}",
				"      {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
	}

	/**
	 * Test for {@link ParseFactoryNoModel}.
	 */
	public void test_noModel() throws Exception {
		// contribute special {@link HierarchyProvider}
		TestUtils.addDynamicExtension(PARSE_FACTORIES_POINT_ID, //
				"  <noModel class='" + TestParseFactory_noModel.class.getName() + "'/>");
		//
		try {
			parseContainer(
					"// filler filler filler",
					"public class Test extends JPanel {",
					"  public Test() {",
					"    add(new JButton());",
					"    add(new JTextField());",
					"    createLabel();",
					"  }",
					"  void createLabel() {",
					"    add(new JLabel());",
					"  }",
					"}");
			assertHierarchy(
					"{this: javax.swing.JPanel} {this} {/add(new JButton())/ /add(new JTextField())/ /add(new JLabel())/}",
					"  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
					"  {new: javax.swing.JButton} {empty} {/add(new JButton())/}");
		} finally {
			TestUtils.removeDynamicExtension(PARSE_FACTORIES_POINT_ID);
		}
	}

	private static String PARSE_FACTORIES_POINT_ID = "org.eclipse.wb.core.java.parseFactories";

	public static final class TestParseFactory_noModel extends ParseFactoryNoModel {
		@Override
		public boolean noModel(ASTNode node) {
			MethodDeclaration enclosingMethod = AstNodeUtils.getEnclosingMethod(node);
			if (enclosingMethod != null) {
				return "createLabel".equals(enclosingMethod.getName().toString());
			}
			return false;
		}

		@Override
		public boolean noModel(ClassInstanceCreation creation, ITypeBinding typeBinding) {
			if (AstNodeUtils.isSuccessorOf(typeBinding, "javax.swing.JTextField")) {
				return true;
			}
			return false;
		}
	}
}
