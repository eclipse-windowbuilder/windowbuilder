/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.tests.designer.swing.model.layout.MigLayout;

import org.eclipse.wb.core.editor.constants.CoreImages;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.internal.swing.MigLayout.model.CellConstraintsSupport;
import org.eclipse.wb.internal.swing.MigLayout.model.CellConstraintsSupport.DockSide;
import org.eclipse.wb.internal.swing.MigLayout.model.MigColumnInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.MigLayoutInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.MigRowInfo;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.swing.SwingImages;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;

import net.miginfocom.layout.LC;

import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableRunnable;
import org.junit.Test;

import java.awt.Container;
import java.util.List;

/**
 * Test for {@link CellConstraintsSupport}.
 *
 * @author scheglov_ke
 */
public class MigLayoutConstraintsTest extends AbstractMigLayoutTest {
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
	// Bounds
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_newConstraints() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel implements IConstants {",
						"  public Test() {",
						"    setLayout(new MigLayout());",
						"    add(new JButton(C_1), 'cell 1 2 3 4');",
						"  }",
						"}");
		panel.refresh();
		//
		ComponentInfo newButton = createJButton();
		panel.addChild(newButton);
		CellConstraintsSupport constraints = MigLayoutInfo.getConstraints(newButton);
		// default values
		assertEquals(0, constraints.getX());
		assertEquals(0, constraints.getY());
		assertEquals(1, constraints.getWidth());
		assertEquals(1, constraints.getHeight());
		// set new values
		constraints.setX(1);
		constraints.setWidth(2);
		// new values are visible
		assertEquals(1, constraints.getX());
		assertEquals(2, constraints.getWidth());
	}

	@Test
	public void test_parseString_getBounds() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel implements IConstants {",
						"  public Test() {",
						"    setLayout(new MigLayout());",
						"    add(new JButton(C_1), 'cell 1 2 3 4');",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// check constraints
		assertCellBounds(button, 1, 2, 3, 4);
	}

	@Test
	public void test_parseCC_getBounds() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel implements IConstants {",
						"  public Test() {",
						"    setLayout(new MigLayout());",
						"    add(new JButton(C_1), new CC().cell(1,2,3,4));",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// check constraints
		assertCellBounds(button, 1, 2, 3, 4);
	}

	/**
	 * No <code>cell</code> tag in constraints, but still can access component bounds in cells.
	 */
	@Test
	public void test_parseString_noCell() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel implements IConstants {",
						"  public Test() {",
						"    setLayout(new MigLayout('', '[][]'));",
						"    add(new JButton(C_1), 'wrap');",
						"    add(new JButton(C_2), 'skip');",
						"  }",
						"}");
		panel.refresh();
		{
			ComponentInfo button_1 = panel.getChildrenComponents().get(0);
			assertCellBounds(button_1, 0, 0, 1, 1);
		}
		{
			ComponentInfo button_2 = panel.getChildrenComponents().get(1);
			assertCellBounds(button_2, 1, 1, 1, 1);
		}
	}

	/**
	 * Horizontal span without limit. It causes "cell width" = "30000 - x", but we need actual width.
	 */
	@Test
	public void test_parseString_span() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel implements IConstants {",
						"  public Test() {",
						"    setLayout(new MigLayout('', '[]'));",
						"    add(new JButton(C_1), 'span');",
						"    add(new JButton(C_2), 'cell 0 1');",
						"    add(new JButton(C_3), 'cell 1 1');",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button_1 = panel.getChildrenComponents().get(0);
		assertCellBounds(button_1, 0, 0, 2, 1);
	}

	/**
	 * If "hidemode 3" is used and component is invisible, we should set it some valid bounds.
	 */
	@Test
	public void test_parse_hidemode3() throws Exception {
		// we don't execute java.awt.Component.setVisible(), so use other method
		setFileContentSrc(
				"test/MyButton.java",
				getTestSource(
						"public class MyButton extends JButton {",
						"  public MyButton() {",
						"  }",
						"  public void setVisible2(boolean b) {",
						"    setVisible(b);",
						"  }",
						"}"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel implements IConstants {",
						"  public Test() {",
						"    setLayout(new MigLayout('hidemode 3', '[][]'));",
						"    {",
						"      MyButton button = new MyButton();",
						"      button.setVisible2(false);",
						"      add(button);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		{
			ComponentInfo button = panel.getChildrenComponents().get(0);
			assertCellBounds(button, 0, 0, 0, 0);
		}
	}

	/**
	 * MigLayout 3.7.4 generates "hideMode", but uses "hidemode" for parsing.
	 */
	@Test
	public void test_write_hidemode() throws Exception {
		parseContainer(
				"public class Test extends JPanel implements IConstants {",
				"  public Test() {",
				"    setLayout(new MigLayout());",
				"    {",
				"      JButton button = new JButton();",
				"      add(button, 'hidemode 0,cell 0 0');",
				"    }",
				"  }",
				"}");
		refresh();
		ComponentInfo button = getJavaInfoByName("button");
		// update constraints
		CellConstraintsSupport constraints = MigLayoutInfo.getConstraints(button);
		constraints.setX(1);
		constraints.write();
		assertEditor(
				"public class Test extends JPanel implements IConstants {",
				"  public Test() {",
				"    setLayout(new MigLayout());",
				"    {",
				"      JButton button = new JButton();",
				"      add(button, 'hidemode 0,cell 1 0');",
				"    }",
				"  }",
				"}");
		refresh();
	}

	/**
	 * {@link Container#add(java.awt.Component, Object)} used for association.
	 */
	@Test
	public void test_setBounds_1() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel implements IConstants {",
						"  public Test() {",
						"    setLayout(new MigLayout());",
						"    add(new JButton(C_1), 'cell 1 2 3 4');",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// update constraints
		CellConstraintsSupport constraints = MigLayoutInfo.getConstraints(button);
		constraints.setX(2);
		constraints.setY(3);
		constraints.setWidth(4);
		constraints.setHeight(5);
		constraints.write();
		assertEditor(
				"public class Test extends JPanel implements IConstants {",
				"  public Test() {",
				"    setLayout(new MigLayout());",
				"    add(new JButton(C_1), 'cell 2 3 4 5');",
				"  }",
				"}");
	}

	/**
	 * {@link Container#add(java.awt.Component)} used for association.
	 */
	@Test
	public void test_setBounds_2() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel implements IConstants {",
						"  public Test() {",
						"    setLayout(new MigLayout());",
						"    add(new JButton(C_1));",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// update constraints
		CellConstraintsSupport constraints = MigLayoutInfo.getConstraints(button);
		constraints.setX(2);
		constraints.setY(3);
		constraints.setWidth(4);
		constraints.setHeight(5);
		constraints.write();
		assertEditor(
				"public class Test extends JPanel implements IConstants {",
				"  public Test() {",
				"    setLayout(new MigLayout());",
				"    add(new JButton(C_1), 'cell 2 3 4 5');",
				"  }",
				"}");
	}

	/**
	 * Test for {@link CellConstraintsSupport#updateX(int)}, etc.
	 */
	@Test
	public void test_updateBounds() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel implements IConstants {",
						"  public Test() {",
						"    setLayout(new MigLayout());",
						"    add(new JButton(C_1), 'cell 1 2 3 4');",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// update constraints
		CellConstraintsSupport constraints = MigLayoutInfo.getConstraints(button);
		constraints.updateX(1);
		constraints.updateY(1);
		constraints.updateWidth(1);
		constraints.updateHeight(1);
		constraints.write();
		assertEditor(
				"public class Test extends JPanel implements IConstants {",
				"  public Test() {",
				"    setLayout(new MigLayout());",
				"    add(new JButton(C_1), 'cell 2 3 4 5');",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// String
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link CellConstraintsSupport#getString()} and
	 * {@link CellConstraintsSupport#setString(String)}.
	 */
	@Test
	public void test_setString() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new MigLayout());",
						"    add(new JButton(), '');",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		CellConstraintsSupport constraints = MigLayoutInfo.getConstraints(button);
		// initially empty
		assertEquals("", constraints.getString());
		// set 1
		{
			String sourceString = "cell 1 2 3 4,wrap";
			String expectedString = "cell 1 2 3 4,wrap";
			constraints.setString(sourceString);
			assertEquals(expectedString, constraints.getString());
			// write
			constraints.write();
			assertEquals(expectedString, getCellConstraintsSource(constraints));
		}
		// set 2
		{
			String sourceString = "wRaP,cell 1 2 3 4 ";
			String expectedString = "cell 1 2 3 4,wrap";
			constraints.setString(sourceString);
			assertEquals(expectedString, constraints.getString());
		}
		// set bad
		try {
			constraints.setString("somethingBad");
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

	private String getCellConstraintsSource(CellConstraintsSupport cell) {
		ComponentInfo m_component = cell.getComponent();
		if (m_component.getAssociation() instanceof InvocationChildAssociation) {
			MethodInvocation invocation =
					((InvocationChildAssociation) m_component.getAssociation()).getInvocation();
			Expression expression = DomGenerics.arguments(invocation).get(1);
			return ((StringLiteral) expression).getLiteralValue();
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// cleanUpSource()
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_cleanUpSource_gapleft() throws Exception {
		parseContainer(
				"public class Test extends JPanel implements IConstants {",
				"  public Test() {",
				"    setLayout(new MigLayout());",
				"    add(new JButton(C_1), 'gap 1');",
				"    add(new JButton(C_2), 'gap 2,wrap');",
				"  }",
				"}");
		refresh();
		// write
		rewriteConstraints();
		assertEditor(
				"public class Test extends JPanel implements IConstants {",
				"  public Test() {",
				"    setLayout(new MigLayout());",
				"    add(new JButton(C_1), 'gapx 1');",
				"    add(new JButton(C_2), 'gapx 2,wrap');",
				"  }",
				"}");
	}

	@Test
	public void test_cleanUpSource_gapright() throws Exception {
		parseContainer(
				"public class Test extends JPanel implements IConstants {",
				"  public Test() {",
				"    setLayout(new MigLayout());",
				"    add(new JButton(C_1), 'gapright 1');",
				"    add(new JButton(C_2), 'gapright 2,wrap');",
				"  }",
				"}");
		refresh();
		// write
		rewriteConstraints();
		assertEditor(
				"public class Test extends JPanel implements IConstants {",
				"  public Test() {",
				"    setLayout(new MigLayout());",
				"    add(new JButton(C_1), 'gapright 1');",
				"    add(new JButton(C_2), 'gapright 2,wrap');",
				"  }",
				"}");
	}

	@Test
	public void test_cleanUpSource_gaptop() throws Exception {
		parseContainer(
				"public class Test extends JPanel implements IConstants {",
				"  public Test() {",
				"    setLayout(new MigLayout());",
				"    add(new JButton(C_1), 'gaptop 1');",
				"    add(new JButton(C_2), 'gaptop 2,wrap');",
				"  }",
				"}");
		refresh();
		// write
		rewriteConstraints();
		assertEditor(
				"public class Test extends JPanel implements IConstants {",
				"  public Test() {",
				"    setLayout(new MigLayout());",
				"    add(new JButton(C_1), 'gapy 1');",
				"    add(new JButton(C_2), 'gapy 2,wrap');",
				"  }",
				"}");
	}

	@Test
	public void test_cleanUpSource_gapbottom() throws Exception {
		parseContainer(
				"public class Test extends JPanel implements IConstants {",
				"  public Test() {",
				"    setLayout(new MigLayout());",
				"    add(new JButton(C_1), 'gapbottom 1');",
				"    add(new JButton(C_2), 'gapbottom 2,wrap');",
				"  }",
				"}");
		refresh();
		// write
		rewriteConstraints();
		assertEditor(
				"public class Test extends JPanel implements IConstants {",
				"  public Test() {",
				"    setLayout(new MigLayout());",
				"    add(new JButton(C_1), 'gapbottom 1');",
				"    add(new JButton(C_2), 'gapbottom 2,wrap');",
				"  }",
				"}");
	}

	private static void rewriteConstraints() throws Exception {
		ContainerInfo panel = (ContainerInfo) GlobalState.getActiveObject();
		List<ComponentInfo> components = panel.getChildrenComponents();
		for (ComponentInfo component : components) {
			CellConstraintsSupport constraints = MigLayoutInfo.getConstraints(component);
			constraints.write0();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// makeExplicitCell()
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_makeExplicitCell_flow() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel implements IConstants {",
						"  public Test() {",
						"    setLayout(new MigLayout());",
						"    add(new JButton(C_1), '');",
						"  }",
						"}");
		panel.refresh();
		// convert to explicit cells
		makeExplicitCell(panel);
		assertEditor(
				"public class Test extends JPanel implements IConstants {",
				"  public Test() {",
				"    setLayout(new MigLayout());",
				"    add(new JButton(C_1), 'cell 0 0');",
				"  }",
				"}");
	}

	@Test
	public void test_makeExplicitCell_wrap() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel implements IConstants {",
						"  public Test() {",
						"    setLayout(new MigLayout());",
						"    add(new JButton(C_1), 'wrap');",
						"    add(new JButton(C_2), '');",
						"  }",
						"}");
		panel.refresh();
		// convert to explicit cells
		makeExplicitCell(panel);
		assertEditor(
				"public class Test extends JPanel implements IConstants {",
				"  public Test() {",
				"    setLayout(new MigLayout());",
				"    add(new JButton(C_1), 'cell 0 0');",
				"    add(new JButton(C_2), 'cell 0 1');",
				"  }",
				"}");
	}

	@Test
	public void test_makeExplicitCell_newline() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel implements IConstants {",
						"  public Test() {",
						"    setLayout(new MigLayout());",
						"    add(new JButton(C_1), '');",
						"    add(new JButton(C_2), 'newline');",
						"  }",
						"}");
		panel.refresh();
		// convert to explicit cells
		makeExplicitCell(panel);
		assertEditor(
				"public class Test extends JPanel implements IConstants {",
				"  public Test() {",
				"    setLayout(new MigLayout());",
				"    add(new JButton(C_1), 'cell 0 0');",
				"    add(new JButton(C_2), 'cell 0 1');",
				"  }",
				"}");
	}

	@Test
	public void test_makeExplicitCell_skip() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel implements IConstants {",
						"  public Test() {",
						"    setLayout(new MigLayout());",
						"    add(new JButton(C_1), '');",
						"    add(new JButton(C_2), 'skip');",
						"  }",
						"}");
		panel.refresh();
		// convert to explicit cells
		makeExplicitCell(panel);
		assertEditor(
				"public class Test extends JPanel implements IConstants {",
				"  public Test() {",
				"    setLayout(new MigLayout());",
				"    add(new JButton(C_1), 'cell 0 0');",
				"    add(new JButton(C_2), 'cell 2 0');",
				"  }",
				"}");
	}

	@Test
	public void test_makeExplicitCell_wrap_skip() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel implements IConstants {",
						"  public Test() {",
						"    setLayout(new MigLayout());",
						"    add(new JButton(C_1), 'wrap');",
						"    add(new JButton(C_2), 'skip');",
						"  }",
						"}");
		panel.refresh();
		// convert to explicit cells
		makeExplicitCell(panel);
		assertEditor(
				"public class Test extends JPanel implements IConstants {",
				"  public Test() {",
				"    setLayout(new MigLayout());",
				"    add(new JButton(C_1), 'cell 0 0');",
				"    add(new JButton(C_2), 'cell 1 1');",
				"  }",
				"}");
	}

	@Test
	public void test_makeExplicitCell_hasDock() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel implements IConstants {",
						"  public Test() {",
						"    setLayout(new MigLayout());",
						"    add(new JButton(C_1), 'north');",
						"    add(new JButton(C_2), 'skip');",
						"  }",
						"}");
		panel.refresh();
		// convert to explicit cells
		makeExplicitCell(panel);
		assertEditor(
				"public class Test extends JPanel implements IConstants {",
				"  public Test() {",
				"    setLayout(new MigLayout());",
				"    add(new JButton(C_1), 'north');",
				"    add(new JButton(C_2), 'cell 1 0');",
				"  }",
				"}");
	}

	@Test
	public void test_makeExplicitCell_split_span_wrap() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel implements IConstants {",
						"  public Test() {",
						"    setLayout(new MigLayout());",
						"    add(new JButton(C_1), 'split, span');",
						"    add(new JButton(C_2), 'wrap');",
						"    add(new JButton(C_3), '');",
						"  }",
						"}");
		panel.refresh();
		// convert to explicit cells
		makeExplicitCell(panel);
		assertEditor(
				"public class Test extends JPanel implements IConstants {",
				"  public Test() {",
				"    setLayout(new MigLayout());",
				"    add(new JButton(C_1), 'cell 0 0');",
				"    add(new JButton(C_2), 'cell 0 0');",
				"    add(new JButton(C_3), 'cell 0 1');",
				"  }",
				"}");
	}

	/**
	 * Invokes {@link CellConstraintsSupport#makeExplicitCell()} for all {@link ComponentInfo}
	 * children.
	 */
	private static void makeExplicitCell(ContainerInfo container) throws Exception {
		for (ComponentInfo button : container.getChildrenComponents()) {
			CellConstraintsSupport constraints = MigLayoutInfo.getConstraints(button);
			constraints.makeExplicitCell();
			constraints.write();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Alignment
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_alignment_UNKNOWN() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel implements IConstants {",
						"  public Test() {",
						"    setLayout(new MigLayout());",
						"    add(new JButton(C_1), 'cell 0 0');",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		CellConstraintsSupport constraints = MigLayoutInfo.getConstraints(button);
		// default alignments
		assertSame(MigColumnInfo.Alignment.DEFAULT, constraints.getHorizontalAlignment());
		assertSame(MigRowInfo.Alignment.DEFAULT, constraints.getVerticalAlignment());
		// new alignments: 1
		{
			constraints.setHorizontalAlignment(MigColumnInfo.Alignment.CENTER);
			constraints.setVerticalAlignment(MigRowInfo.Alignment.BOTTOM);
			constraints.write();
			assertEditor(
					"public class Test extends JPanel implements IConstants {",
					"  public Test() {",
					"    setLayout(new MigLayout());",
					"    add(new JButton(C_1), 'cell 0 0,alignx center,aligny bottom');",
					"  }",
					"}");
		}
		// new alignments: 2
		{
			constraints.setHorizontalAlignment(MigColumnInfo.Alignment.FILL);
			constraints.setVerticalAlignment(MigRowInfo.Alignment.FILL);
			constraints.write();
			assertEditor(
					"public class Test extends JPanel implements IConstants {",
					"  public Test() {",
					"    setLayout(new MigLayout());",
					"    add(new JButton(C_1), 'cell 0 0,grow');",
					"  }",
					"}");
		}
		// new alignments: 3
		{
			constraints.setHorizontalAlignment(MigColumnInfo.Alignment.LEFT);
			constraints.setVerticalAlignment(MigRowInfo.Alignment.FILL);
			constraints.write();
			assertEditor(
					"public class Test extends JPanel implements IConstants {",
					"  public Test() {",
					"    setLayout(new MigLayout());",
					"    add(new JButton(C_1), 'cell 0 0,alignx left,growy');",
					"  }",
					"}");
		}
		// new alignments: 3
		{
			constraints.setHorizontalAlignment(MigColumnInfo.Alignment.LEADING);
			constraints.setVerticalAlignment(MigRowInfo.Alignment.BASELINE);
			constraints.write();
			assertEditor(
					"public class Test extends JPanel implements IConstants {",
					"  public Test() {",
					"    setLayout(new MigLayout());",
					"    add(new JButton(C_1), 'cell 0 0,alignx leading,aligny baseline');",
					"  }",
					"}");
		}
		// new alignments: 4
		{
			constraints.setHorizontalAlignment(MigColumnInfo.Alignment.DEFAULT);
			constraints.setVerticalAlignment(MigRowInfo.Alignment.DEFAULT);
			constraints.write();
			assertEditor(
					"public class Test extends JPanel implements IConstants {",
					"  public Test() {",
					"    setLayout(new MigLayout());",
					"    add(new JButton(C_1), 'cell 0 0');",
					"  }",
					"}");
		}
	}

	@Test
	public void test_setAlignment_2() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel implements IConstants {",
						"  public Test() {",
						"    setLayout(new MigLayout());",
						"    add(new JButton(C_1), 'cell 0 0, grow');",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		CellConstraintsSupport constraints = MigLayoutInfo.getConstraints(button);
		// default alignments
		assertSame(MigColumnInfo.Alignment.FILL, constraints.getHorizontalAlignment());
		assertSame(MigRowInfo.Alignment.FILL, constraints.getVerticalAlignment());
		// new alignments: 1
		{
			constraints.setHorizontalAlignment(MigColumnInfo.Alignment.DEFAULT);
			constraints.write();
			assertEditor(
					"public class Test extends JPanel implements IConstants {",
					"  public Test() {",
					"    setLayout(new MigLayout());",
					"    add(new JButton(C_1), 'cell 0 0,growy');",
					"  }",
					"}");
		}
		// new alignments: 2
		{
			constraints.setVerticalAlignment(MigRowInfo.Alignment.DEFAULT);
			constraints.write();
			assertEditor(
					"public class Test extends JPanel implements IConstants {",
					"  public Test() {",
					"    setLayout(new MigLayout());",
					"    add(new JButton(C_1), 'cell 0 0');",
					"  }",
					"}");
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Alignment images: horizontal
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_alignmentImageHorizontal_UNKNOWN() throws Exception {
		check_alignmentImageHorizontal(MigColumnInfo.Alignment.UNKNOWN, CoreImages.ALIGNMENT_H_SMALL_DEFAULT);
	}

	@Test
	public void test_alignmentImageHorizontal_LEFT() throws Exception {
		check_alignmentImageHorizontal(MigColumnInfo.Alignment.LEFT, CoreImages.ALIGNMENT_H_SMALL_LEFT);
	}

	@Test
	public void test_alignmentImageHorizontal_CENTER() throws Exception {
		check_alignmentImageHorizontal(MigColumnInfo.Alignment.CENTER, CoreImages.ALIGNMENT_H_SMALL_CENTER);
	}

	@Test
	public void test_alignmentImageHorizontal_RIGHT() throws Exception {
		check_alignmentImageHorizontal(MigColumnInfo.Alignment.RIGHT, CoreImages.ALIGNMENT_H_SMALL_RIGHT);
	}

	@Test
	public void test_alignmentImageHorizontal_FILL() throws Exception {
		check_alignmentImageHorizontal(MigColumnInfo.Alignment.FILL, CoreImages.ALIGNMENT_H_SMALL_FILL);
	}

	@Test
	public void test_alignmentImageHorizontal_LEADING() throws Exception {
		check_alignmentImageHorizontal(MigColumnInfo.Alignment.LEADING, CoreImages.ALIGNMENT_H_SMALL_LEADING);
	}

	@Test
	public void test_alignmentImageHorizontal_TRAILING() throws Exception {
		check_alignmentImageHorizontal(MigColumnInfo.Alignment.TRAILING, CoreImages.ALIGNMENT_H_SMALL_TRAILING);
	}

	private void check_alignmentImageHorizontal(MigColumnInfo.Alignment alignment, ImageDescriptor descriptor)
			throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel implements IConstants {",
						"  public Test() {",
						"    setLayout(new MigLayout());",
						"    add(new JButton(C_1), 'cell 0 0');",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// check constraints
		CellConstraintsSupport constraints = MigLayoutInfo.getConstraints(button);
		if (alignment != MigColumnInfo.Alignment.UNKNOWN) {
			constraints.setHorizontalAlignment(alignment);
		}
		assertSame(descriptor, constraints.getSmallAlignmentImageDescriptor(true));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Alignment images: vertical
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_alignmentImageVertical_UNKNOWN() throws Exception {
		check_alignmentImageVertical(MigRowInfo.Alignment.UNKNOWN, CoreImages.ALIGNMENT_V_SMALL_DEFAULT);
	}

	@Test
	public void test_alignmentImageVertical_TOP() throws Exception {
		check_alignmentImageVertical(MigRowInfo.Alignment.TOP, CoreImages.ALIGNMENT_V_SMALL_TOP);
	}

	@Test
	public void test_alignmentImageVertical_CENTER() throws Exception {
		check_alignmentImageVertical(MigRowInfo.Alignment.CENTER, CoreImages.ALIGNMENT_V_SMALL_CENTER);
	}

	@Test
	public void test_alignmentImageVertical_BOTTOM() throws Exception {
		check_alignmentImageVertical(MigRowInfo.Alignment.BOTTOM, CoreImages.ALIGNMENT_V_SMALL_BOTTOM);
	}

	@Test
	public void test_alignmentImageVertical_FILL() throws Exception {
		check_alignmentImageVertical(MigRowInfo.Alignment.FILL, CoreImages.ALIGNMENT_V_SMALL_FILL);
	}

	@Test
	public void test_alignmentImageVertical_BASELINE() throws Exception {
		check_alignmentImageVertical(MigRowInfo.Alignment.BASELINE, SwingImages.ALIGNMENT_V_SMALL_BASELINE);
	}

	private void check_alignmentImageVertical(MigRowInfo.Alignment alignment, ImageDescriptor descriptor)
			throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel implements IConstants {",
						"  public Test() {",
						"    setLayout(new MigLayout());",
						"    add(new JButton(C_1), 'cell 0 0');",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// check constraints
		CellConstraintsSupport constraints = MigLayoutInfo.getConstraints(button);
		if (alignment != MigRowInfo.Alignment.UNKNOWN) {
			constraints.setVerticalAlignment(alignment);
		}
		assertSame(descriptor, constraints.getSmallAlignmentImageDescriptor(false));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Context menu
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Assert that "Constraints..." action contributed to each {@link ComponentInfo} on
	 * {@link MigLayoutInfo}.
	 */
	@Test
	public void test_contextMenu_ConstraintsAction() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new MigLayout());",
						"    add(new JButton(), '');",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		final CellConstraintsSupport constraints = MigLayoutInfo.getConstraints(button);
		// prepare "Constraints..." action
		final IAction action;
		{
			IMenuManager manager = getContextMenu(button);
			action = findChildAction(manager, "Constraints...");
			assertNotNull(action);
		}
		// initial getString()
		assertEquals("", constraints.getString());
		// open dialog, but cancel
		{
			new UiContext().executeAndCheck(new FailableRunnable<>() {
				@Override
				public void run() {
					action.run();
				}
			}, new FailableConsumer<>() {
				@Override
				public void accept(SWTBot bot) {
					SWTBot shell = bot.shell("Cell properties").bot();
					{
						SWTBotText text = shell.textWithLabel("Specification:");
						text.setText("width 100px");
					}
					// changes applied into "constraints"
					assertEquals("width 100px", constraints.getString());
					shell.button("Cancel").click();
				}
			});
			// changes of "constraints" rolled back
			assertEquals("", constraints.getString());
			// editor also not changed
			assertEditor(
					"public class Test extends JPanel {",
					"  public Test() {",
					"    setLayout(new MigLayout());",
					"    add(new JButton(), '');",
					"  }",
					"}");
		}
		// open dialog, commit changes
		new UiContext().executeAndCheck(new FailableRunnable<>() {
			@Override
			public void run() {
				action.run();
			}
		}, new FailableConsumer<>() {
			@Override
			public void accept(SWTBot bot) {
				SWTBot shell = bot.shell("Cell properties").bot();
				{
					SWTBotText text = shell.textWithLabel("Specification:");
					text.setText("width 100px");
				}
				shell.button("OK").click();
			}
		});
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new MigLayout());",
				"    add(new JButton(), 'width 100px');",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Alignment context menu
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_alignmentContentMenuHorizontal_LEFT() throws Exception {
		check_contextMenu_alignmentHorizontal("Left", MigColumnInfo.Alignment.LEFT, "alignx left");
	}

	@Test
	public void test_alignmentContentMenuHorizontal_CENTER() throws Exception {
		check_contextMenu_alignmentHorizontal("Center", MigColumnInfo.Alignment.CENTER, "alignx center");
	}

	@Test
	public void test_alignmentContentMenuHorizontal_RIGHT() throws Exception {
		check_contextMenu_alignmentHorizontal("Right", MigColumnInfo.Alignment.RIGHT, "alignx right");
	}

	@Test
	public void test_alignmentContentMenuHorizontal_FILL() throws Exception {
		check_contextMenu_alignmentHorizontal("Fill", MigColumnInfo.Alignment.FILL, "growx");
	}

	@Test
	public void test_alignmentContentMenuHorizontal_LEADING() throws Exception {
		check_contextMenu_alignmentHorizontal(
				"Leading",
				MigColumnInfo.Alignment.LEADING,
				"alignx leading");
	}

	@Test
	public void test_alignmentContentMenuHorizontal_TRAILING() throws Exception {
		check_contextMenu_alignmentHorizontal(
				"Trailing",
				MigColumnInfo.Alignment.TRAILING,
				"alignx trailing");
	}

	@Test
	public void test_alignmentContentMenuVertical_TOP() throws Exception {
		check_contextMenu_alignmentVertical("Top", MigRowInfo.Alignment.TOP, "aligny top");
	}

	@Test
	public void test_alignmentContentMenuVertical_CENTER() throws Exception {
		check_contextMenu_alignmentVertical("Center", MigRowInfo.Alignment.CENTER, "aligny center");
	}

	@Test
	public void test_alignmentContentMenuVertical_BOTTOM() throws Exception {
		check_contextMenu_alignmentVertical("Bottom", MigRowInfo.Alignment.BOTTOM, "aligny bottom");
	}

	@Test
	public void test_alignmentContentMenuVertical_FILL() throws Exception {
		check_contextMenu_alignmentVertical("Fill", MigRowInfo.Alignment.FILL, "growy");
	}

	@Test
	public void test_alignmentContentMenuVertical_BASELINE() throws Exception {
		check_contextMenu_alignmentVertical(
				"Baseline",
				MigRowInfo.Alignment.BASELINE,
				"aligny baseline");
	}

	private void check_contextMenu_alignmentHorizontal(String actionText,
			MigColumnInfo.Alignment expectedHorizontalAlignment,
			String expectedConstraintsSource) throws Exception {
		check_contextMenu_alignment(
				"Horizontal alignment",
				actionText,
				expectedHorizontalAlignment,
				MigRowInfo.Alignment.DEFAULT,
				expectedConstraintsSource);
	}

	public void check_contextMenu_alignmentVertical(String actionText,
			MigRowInfo.Alignment expectedVerticalAlignment,
			String expectedConstraintsSource) throws Exception {
		check_contextMenu_alignment(
				"Vertical alignment",
				actionText,
				MigColumnInfo.Alignment.DEFAULT,
				expectedVerticalAlignment,
				expectedConstraintsSource);
	}

	private void check_contextMenu_alignment(String managerText,
			String actionText,
			MigColumnInfo.Alignment expectedHorizontalAlignment,
			MigRowInfo.Alignment expectedVerticalAlignment,
			String expectedConstraintsSource) throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel implements IConstants {",
						"  public Test() {",
						"    setLayout(new MigLayout());",
						"    add(new JButton(C_1), 'cell 0 0');",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// prepare alignment manager
		IMenuManager alignmentManager;
		{
			MenuManager contextMenu = getDesignerMenuManager();
			panel.getBroadcastObject().addContextMenu(List.of(button), button, contextMenu);
			alignmentManager = findChildMenuManager(contextMenu, managerText);
			assertNotNull(alignmentManager);
		}
		// set alignment
		IAction alignmentAction = findChildAction(alignmentManager, actionText);
		assertNotNull(actionText, alignmentAction);
		alignmentAction.setChecked(true);
		alignmentAction.run();
		// check result
		CellConstraintsSupport constraints = MigLayoutInfo.getConstraints(button);
		assertSame(expectedHorizontalAlignment, constraints.getHorizontalAlignment());
		assertSame(expectedVerticalAlignment, constraints.getVerticalAlignment());
		assertEditor(
				"public class Test extends JPanel implements IConstants {",
				"  public Test() {",
				"    setLayout(new MigLayout());",
				"    add(new JButton(C_1), 'cell 0 0," + expectedConstraintsSource + "');",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Docking
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_dock_getDockSide() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel implements IConstants {",
						"  public Test() {",
						"    setLayout(new MigLayout());",
						"    add(new JButton(C_1), 'north');",
						"    add(new JButton(C_2), 'west');",
						"    add(new JButton(C_3), 'south');",
						"    add(new JButton(C_4), 'east');",
						"    add(new JButton(C_5), '');",
						"  }",
						"}");
		panel.refresh();
		{
			ComponentInfo button_1 = panel.getChildrenComponents().get(0);
			CellConstraintsSupport constraints = MigLayoutInfo.getConstraints(button_1);
			assertSame(CellConstraintsSupport.DockSide.NORTH, constraints.getDockSide());
		}
		{
			ComponentInfo button_2 = panel.getChildrenComponents().get(1);
			CellConstraintsSupport constraints = MigLayoutInfo.getConstraints(button_2);
			assertSame(CellConstraintsSupport.DockSide.WEST, constraints.getDockSide());
		}
		{
			ComponentInfo button_3 = panel.getChildrenComponents().get(2);
			CellConstraintsSupport constraints = MigLayoutInfo.getConstraints(button_3);
			assertSame(CellConstraintsSupport.DockSide.SOUTH, constraints.getDockSide());
		}
		{
			ComponentInfo button_4 = panel.getChildrenComponents().get(3);
			CellConstraintsSupport constraints = MigLayoutInfo.getConstraints(button_4);
			assertSame(CellConstraintsSupport.DockSide.EAST, constraints.getDockSide());
		}
		{
			ComponentInfo button_5 = panel.getChildrenComponents().get(4);
			CellConstraintsSupport constraints = MigLayoutInfo.getConstraints(button_5);
			assertSame(null, constraints.getDockSide());
		}
	}

	@Test
	public void test_dock_setDockSide() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel implements IConstants {",
						"  public Test() {",
						"    setLayout(new MigLayout());",
						"    add(new JButton(C_1), '');",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		CellConstraintsSupport constraints = MigLayoutInfo.getConstraints(button);
		// try different dock sides
		check_setDockSide(constraints, CellConstraintsSupport.DockSide.NORTH, "north");
		check_setDockSide(constraints, CellConstraintsSupport.DockSide.WEST, "west");
		check_setDockSide(constraints, CellConstraintsSupport.DockSide.SOUTH, "south");
		check_setDockSide(constraints, CellConstraintsSupport.DockSide.EAST, "east");
		check_setDockSide(constraints, null, "");
	}

	private void check_setDockSide(CellConstraintsSupport constraints,
			DockSide sideToSet,
			String expectedConstraints) throws Exception {
		constraints.setDockSide(sideToSet);
		assertSame(sideToSet, constraints.getDockSide());
		constraints.write();
		assertEditor(
				"public class Test extends JPanel implements IConstants {",
				"  public Test() {",
				"    setLayout(new MigLayout());",
				"    add(new JButton(C_1), '" + expectedConstraints + "');",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// isHorizontalSplit()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link CellConstraintsSupport#isHorizontalSplit()}.
	 * <p>
	 * Explicit "flowx" for component.
	 */
	@Test
	public void test_isHorizontalSplit_1() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel implements IConstants {",
						"  public Test() {",
						"    setLayout(new MigLayout());",
						"    add(new JButton(C_1), 'cell 0 0,flowx');",
						"    add(new JButton(C_2), 'cell 0 0');",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button_1 = panel.getChildrenComponents().get(0);
		//
		assertTrue(MigLayoutInfo.getConstraints(button_1).isHorizontalSplit());
	}

	/**
	 * Test for {@link CellConstraintsSupport#isHorizontalSplit()}.
	 * <p>
	 * Explicit "flowy" for component.
	 */
	@Test
	public void test_isHorizontalSplit_2() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel implements IConstants {",
						"  public Test() {",
						"    setLayout(new MigLayout());",
						"    add(new JButton(C_1), 'cell 0 0,flowy');",
						"    add(new JButton(C_2), 'cell 0 0');",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button_1 = panel.getChildrenComponents().get(0);
		//
		assertFalse(MigLayoutInfo.getConstraints(button_1).isHorizontalSplit());
	}

	/**
	 * Test for {@link CellConstraintsSupport#isHorizontalSplit()}.
	 * <p>
	 * Implicit "flowx" from {@link LC}.
	 */
	@Test
	public void test_isHorizontalSplit_3() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel implements IConstants {",
						"  public Test() {",
						"    setLayout(new MigLayout());",
						"    add(new JButton(C_1), 'cell 0 0');",
						"    add(new JButton(C_2), 'cell 0 0');",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button_1 = panel.getChildrenComponents().get(0);
		//
		assertTrue(MigLayoutInfo.getConstraints(button_1).isHorizontalSplit());
	}

	/**
	 * Test for {@link CellConstraintsSupport#isHorizontalSplit()}.
	 * <p>
	 * Explicit "flowy" from {@link LC}.
	 */
	@Test
	public void test_isHorizontalSplit_4() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel implements IConstants {",
						"  public Test() {",
						"    setLayout(new MigLayout(new LC().flowY()));",
						"    add(new JButton(C_1), 'cell 0 0');",
						"    add(new JButton(C_2), 'cell 0 0');",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button_1 = panel.getChildrenComponents().get(0);
		//
		assertFalse(MigLayoutInfo.getConstraints(button_1).isHorizontalSplit());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// setHorizontalSplit()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link CellConstraintsSupport#setHorizontalSplit(Boolean)}.
	 */
	@Test
	public void test_setHorizontalSplit_setExplicitTrue() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel implements IConstants {",
						"  public Test() {",
						"    setLayout(new MigLayout());",
						"    add(new JButton(C_1), 'cell 0 0');",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button_1 = panel.getChildrenComponents().get(0);
		//
		CellConstraintsSupport constraints = MigLayoutInfo.getConstraints(button_1);
		constraints.setHorizontalSplit(Boolean.TRUE);
		assertTrue(constraints.isHorizontalSplit());
		constraints.write();
		assertEditor(
				"public class Test extends JPanel implements IConstants {",
				"  public Test() {",
				"    setLayout(new MigLayout());",
				"    add(new JButton(C_1), 'flowx,cell 0 0');",
				"  }",
				"}");
	}

	/**
	 * Test for {@link CellConstraintsSupport#setHorizontalSplit(Boolean)}.
	 */
	@Test
	public void test_setHorizontalSplit_setExplicitFalse() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel implements IConstants {",
						"  public Test() {",
						"    setLayout(new MigLayout());",
						"    add(new JButton(C_1), 'cell 0 0');",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button_1 = panel.getChildrenComponents().get(0);
		//
		CellConstraintsSupport constraints = MigLayoutInfo.getConstraints(button_1);
		constraints.setHorizontalSplit(Boolean.FALSE);
		assertFalse(constraints.isHorizontalSplit());
		constraints.write();
		assertEditor(
				"public class Test extends JPanel implements IConstants {",
				"  public Test() {",
				"    setLayout(new MigLayout());",
				"    add(new JButton(C_1), 'flowy,cell 0 0');",
				"  }",
				"}");
	}

	/**
	 * Test for {@link CellConstraintsSupport#setHorizontalSplit(Boolean)}.
	 */
	@Test
	public void test_setHorizontalSplit_removeExplicit() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel implements IConstants {",
						"  public Test() {",
						"    setLayout(new MigLayout());",
						"    add(new JButton(C_1), 'flowy,cell 0 0');",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button_1 = panel.getChildrenComponents().get(0);
		//
		CellConstraintsSupport constraints = MigLayoutInfo.getConstraints(button_1);
		assertFalse(constraints.isHorizontalSplit());
		constraints.setHorizontalSplit(null);
		assertTrue(constraints.isHorizontalSplit());
		constraints.write();
		assertEditor(
				"public class Test extends JPanel implements IConstants {",
				"  public Test() {",
				"    setLayout(new MigLayout());",
				"    add(new JButton(C_1), 'cell 0 0');",
				"  }",
				"}");
	}
}
