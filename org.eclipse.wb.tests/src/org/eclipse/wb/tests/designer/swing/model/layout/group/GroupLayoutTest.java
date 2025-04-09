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
package org.eclipse.wb.tests.designer.swing.model.layout.group;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.LayoutInfo;
import org.eclipse.wb.tests.designer.swing.model.layout.AbstractLayoutTest;

import org.junit.Test;

import java.awt.FlowLayout;

import javax.swing.GroupLayout;

/**
 * Tests for {@link GroupLayout}.
 *
 * @author mitin_aa
 */
public class GroupLayoutTest extends AbstractLayoutTest {
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
	public void test_set_another_layout_on_GroupLayout_lazyCodeGen() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"import javax.swing.GroupLayout.Alignment;",
						"public class Test extends JPanel {",
						"  private JButton button;",
						"  public Test() {",
						"    GroupLayout groupLayout = new GroupLayout(this);",
						"    groupLayout.setHorizontalGroup(",
						"      groupLayout.createParallelGroup(Alignment.LEADING)",
						"        .addGroup(groupLayout.createSequentialGroup()",
						"          .addContainerGap()",
						"          .addComponent(getButton())",
						"          .addContainerGap(353, Short.MAX_VALUE))",
						"    );",
						"    groupLayout.setVerticalGroup(",
						"      groupLayout.createParallelGroup(Alignment.LEADING)",
						"        .addGroup(groupLayout.createSequentialGroup()",
						"          .addContainerGap()",
						"          .addComponent(getButton())",
						"        .addContainerGap(259, Short.MAX_VALUE))",
						"    );",
						"    setLayout(groupLayout);",
						"  }",
						"  private JButton getButton() {",
						"    if (button == null) {",
						"      button = new JButton('New button');",
						"    }",
						"    return button;",
						"  }",
						"}");
		//
		panel.refresh();
		JavaInfo newLayout =
				JavaInfoUtils.createJavaInfo(
						m_lastEditor,
						FlowLayout.class,
						new ConstructorCreationSupport());
		panel.setLayout((LayoutInfo) newLayout);
		// test
		assertEditor(
				"import javax.swing.GroupLayout.Alignment;",
				"public class Test extends JPanel {",
				"  private JButton button;",
				"  public Test() {",
				"    setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));",
				"    add(getButton());",
				"  }",
				"  private JButton getButton() {",
				"    if (button == null) {",
				"      button = new JButton('New button');",
				"    }",
				"    return button;",
				"  }",
				"}");
	}

	@Test
	public void test_set_another_layout_on_GroupLayout_localVarCodeGen() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"import javax.swing.GroupLayout.Alignment;",
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton button = new JButton('New button');",
						"    GroupLayout groupLayout = new GroupLayout(this);",
						"    groupLayout.setHorizontalGroup(",
						"      groupLayout.createParallelGroup(Alignment.LEADING)",
						"        .addGroup(groupLayout.createSequentialGroup()",
						"          .addContainerGap()",
						"          .addComponent(button)",
						"          .addContainerGap(353, Short.MAX_VALUE))",
						"    );",
						"    groupLayout.setVerticalGroup(",
						"      groupLayout.createParallelGroup(Alignment.LEADING)",
						"        .addGroup(groupLayout.createSequentialGroup()",
						"          .addContainerGap()",
						"          .addComponent(button)",
						"        .addContainerGap(259, Short.MAX_VALUE))",
						"    );",
						"    setLayout(groupLayout);",
						"  }",
						"}");
		//
		panel.refresh();
		JavaInfo newLayout =
				JavaInfoUtils.createJavaInfo(
						m_lastEditor,
						FlowLayout.class,
						new ConstructorCreationSupport());
		panel.setLayout((LayoutInfo) newLayout);
		// test
		assertEditor(
				"import javax.swing.GroupLayout.Alignment;",
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));",
				"    JButton button = new JButton('New button');",
				"    add(button);",
				"  }",
				"}");
	}
}
