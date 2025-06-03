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
package org.eclipse.wb.tests.designer.swing.swingx;

import org.eclipse.wb.internal.core.model.creation.factory.ImplicitFactoryCreationSupport;
import org.eclipse.wb.internal.swing.model.bean.ActionContainerInfo;
import org.eclipse.wb.internal.swing.model.bean.ActionInfo;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.swingx.JXTaskPaneInfo;

import org.eclipse.jdt.core.dom.CompilationUnit;

import org.junit.Test;

import java.awt.Component;
import java.util.List;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JToolBar;

/**
 * Test for {@link JXTaskPaneInfo}.
 *
 * @author sablin_aa
 */
public class JXTaskPaneTest extends SwingxModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Invocation of {@link JXTaskPane#add(Action)} creates {@link Component}, so we also should
	 * create {@link ComponentInfo} for such invocation.
	 */
	@Test
	public void test_Action_parse() throws Exception {
		createExternalAction();
		ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  private ExternalAction action = new ExternalAction();",
						"  Test() {",
						"    JXTaskPane pane = new JXTaskPane();",
						"    Component actionComponent = pane.add(action);",
						"    add(pane);",
						"  }",
						"}");
		panel.refresh();
		//
		assertEquals(ActionContainerInfo.getActions(panel).size(), 1);
		List<JXTaskPaneInfo> children = panel.getChildren(JXTaskPaneInfo.class);
		assertEquals(children.size(), 1);
		// check JXTaskPane
		JXTaskPaneInfo pane = children.get(0);
		assertEquals(pane.getChildrenComponents().size(), 2);// ContentPane & action Component
	}

	/**
	 * Use {@link ImplicitFactoryCreationSupport} with {@link JToolBar#add(Action)} to create
	 * {@link JButton}.
	 */
	@Test
	public void test_Action_CREATE() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  Test() {",
						"    JXTaskPane pane = new JXTaskPane();",
						"    add(pane);",
						"  }",
						"}");
		panel.refresh();
		JXTaskPaneInfo pane = (JXTaskPaneInfo) panel.getChildrenComponents().get(0);
		// create Action
		ActionInfo action = ActionInfo.createInner(pane.getEditor());
		pane.command_CREATE(action, null);
		// check
		assertEditor(
				"class Test extends JPanel {",
				"  private final Action action = new SwingAction();",
				"  Test() {",
				"    JXTaskPane pane = new JXTaskPane();",
				"    add(pane);",
				"    {",
				"      Component component = pane.add(action);",
				"    }",
				"  }",
				"  private class SwingAction extends AbstractAction {",
				"    public SwingAction() {",
				"      putValue(NAME, 'SwingAction');",
				"      putValue(SHORT_DESCRIPTION, 'Some short description');",
				"    }",
				"    public void actionPerformed(ActionEvent e) {",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Create {@link CompilationUnit} with external {@link Action}.
	 */
	private void createExternalAction() throws Exception {
		setFileContentSrc(
				"test/ExternalAction.java",
				getTestSource(
						"public class ExternalAction extends AbstractAction {",
						"  public ExternalAction() {",
						"    putValue(NAME, 'My name');",
						"    putValue(SHORT_DESCRIPTION, 'My short description');",
						"  }",
						"  public void actionPerformed(ActionEvent e) {",
						"  }",
						"}"));
		waitForAutoBuild();
	}
}
