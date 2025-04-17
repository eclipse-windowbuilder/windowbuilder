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
package org.eclipse.wb.tests.designer.swing.model.layout.FormLayout;

import org.eclipse.wb.internal.swing.FormLayout.model.FormColumnInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.FormLayoutInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.junit.Test;

/**
 * Test for {@link FormColumnInfo}.
 *
 * @author scheglov_ke
 */
public class FormColumnInfoTest extends AbstractFormLayoutTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Insert
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_insert_onColumn() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new FormLayout(new ColumnSpec[] {",
						"        FormSpecs.DEFAULT_COLSPEC,",
						"        FormSpecs.UNRELATED_GAP_COLSPEC,",
						"        FormSpecs.PREF_COLSPEC,},",
						"      new RowSpec[] {",
						"        FormSpecs.DEFAULT_ROWSPEC,}));",
						"    {",
						"      JButton button = new JButton();",
						"      add(button, '3, 1');",
						"    }",
						"  }",
						"}");
		FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
		//
		panel.refresh();
		try {
			layout.insertColumn(2);
		} finally {
			panel.refresh_dispose();
		}
		// check source
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new FormLayout(new ColumnSpec[] {",
				"        FormSpecs.DEFAULT_COLSPEC,",
				"        FormSpecs.UNRELATED_GAP_COLSPEC,",
				"        FormSpecs.DEFAULT_COLSPEC,",
				"        FormSpecs.RELATED_GAP_COLSPEC,",
				"        FormSpecs.PREF_COLSPEC,},",
				"      new RowSpec[] {",
				"        FormSpecs.DEFAULT_ROWSPEC,}));",
				"    {",
				"      JButton button = new JButton();",
				"      add(button, '5, 1');",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_insert_onGap() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new FormLayout(new ColumnSpec[] {",
						"        FormSpecs.DEFAULT_COLSPEC,",
						"        FormSpecs.UNRELATED_GAP_COLSPEC,",
						"        FormSpecs.PREF_COLSPEC,},",
						"      new RowSpec[] {",
						"        FormSpecs.DEFAULT_ROWSPEC,}));",
						"    {",
						"      JButton button = new JButton();",
						"      add(button, '3, 1');",
						"    }",
						"  }",
						"}");
		FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
		//
		panel.refresh();
		try {
			layout.insertColumn(1);
		} finally {
			panel.refresh_dispose();
		}
		// check source
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new FormLayout(new ColumnSpec[] {",
				"        FormSpecs.DEFAULT_COLSPEC,",
				"        FormSpecs.RELATED_GAP_COLSPEC,",
				"        FormSpecs.DEFAULT_COLSPEC,",
				"        FormSpecs.UNRELATED_GAP_COLSPEC,",
				"        FormSpecs.PREF_COLSPEC,},",
				"      new RowSpec[] {",
				"        FormSpecs.DEFAULT_ROWSPEC,}));",
				"    {",
				"      JButton button = new JButton();",
				"      add(button, '5, 1');",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_insert_lastWithGap() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new FormLayout(new ColumnSpec[] {",
						"        FormSpecs.DEFAULT_COLSPEC,",
						"        FormSpecs.UNRELATED_GAP_COLSPEC,},",
						"      new RowSpec[] {",
						"        FormSpecs.DEFAULT_ROWSPEC,}));",
						"  }",
						"}");
		FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
		//
		panel.refresh();
		try {
			layout.insertColumn(2);
		} finally {
			panel.refresh_dispose();
		}
		// check source
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new FormLayout(new ColumnSpec[] {",
				"        FormSpecs.DEFAULT_COLSPEC,",
				"        FormSpecs.UNRELATED_GAP_COLSPEC,",
				"        FormSpecs.DEFAULT_COLSPEC,},",
				"      new RowSpec[] {",
				"        FormSpecs.DEFAULT_ROWSPEC,}));",
				"  }",
				"}");
	}

	@Test
	public void test_insert_lastWithoutGap() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new FormLayout(new ColumnSpec[] {",
						"        FormSpecs.PREF_COLSPEC,},",
						"      new RowSpec[] {",
						"        FormSpecs.DEFAULT_ROWSPEC,}));",
						"  }",
						"}");
		FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
		//
		panel.refresh();
		try {
			layout.insertColumn(1);
		} finally {
			panel.refresh_dispose();
		}
		// check source
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new FormLayout(new ColumnSpec[] {",
				"        FormSpecs.PREF_COLSPEC,",
				"        FormSpecs.RELATED_GAP_COLSPEC,",
				"        FormSpecs.DEFAULT_COLSPEC,},",
				"      new RowSpec[] {",
				"        FormSpecs.DEFAULT_ROWSPEC,}));",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Delete
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_deleteColumn_nextGap() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new FormLayout(new ColumnSpec[] {",
						"        FormSpecs.DEFAULT_COLSPEC,",
						"        FormSpecs.RELATED_GAP_COLSPEC,",
						"        FormSpecs.PREF_COLSPEC,},",
						"      new RowSpec[] {",
						"        FormSpecs.DEFAULT_ROWSPEC,}));",
						"    {",
						"      JButton button = new JButton('111');",
						"      add(button, '1, 1');",
						"    }",
						"    {",
						"      JButton button = new JButton('333');",
						"      add(button, '3, 1');",
						"    }",
						"  }",
						"}");
		FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
		//
		panel.refresh();
		try {
			layout.deleteColumn(0);
		} finally {
			panel.refresh_dispose();
		}
		// check source
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new FormLayout(new ColumnSpec[] {",
				"        FormSpecs.PREF_COLSPEC,},",
				"      new RowSpec[] {",
				"        FormSpecs.DEFAULT_ROWSPEC,}));",
				"    {",
				"      JButton button = new JButton('333');",
				"      add(button, '1, 1');",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_deleteColumn_span() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new FormLayout(new ColumnSpec[] {",
						"        FormSpecs.DEFAULT_COLSPEC,",
						"        FormSpecs.MIN_COLSPEC,",
						"        FormSpecs.PREF_COLSPEC,},",
						"      new RowSpec[] {",
						"        FormSpecs.DEFAULT_ROWSPEC,}));",
						"    {",
						"      JButton button = new JButton('111');",
						"      add(button, '1, 1, 2, 1');",
						"    }",
						"    {",
						"      JButton button = new JButton('333');",
						"      add(button, '3, 1');",
						"    }",
						"  }",
						"}");
		FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
		//
		panel.refresh();
		try {
			layout.deleteColumn(1);
		} finally {
			panel.refresh_dispose();
		}
		// check source
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new FormLayout(new ColumnSpec[] {",
				"        FormSpecs.DEFAULT_COLSPEC,",
				"        FormSpecs.PREF_COLSPEC,},",
				"      new RowSpec[] {",
				"        FormSpecs.DEFAULT_ROWSPEC,}));",
				"    {",
				"      JButton button = new JButton('111');",
				"      add(button, '1, 1');",
				"    }",
				"    {",
				"      JButton button = new JButton('333');",
				"      add(button, '2, 1');",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_deleteColumn_prevGap() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new FormLayout(new ColumnSpec[] {",
						"        FormSpecs.RELATED_GAP_COLSPEC,",
						"        FormSpecs.DEFAULT_COLSPEC,",
						"        FormSpecs.UNRELATED_GAP_COLSPEC,",
						"        FormSpecs.PREF_COLSPEC,},",
						"      new RowSpec[] {",
						"        FormSpecs.DEFAULT_ROWSPEC,}));",
						"    {",
						"      JButton button = new JButton('111');",
						"      add(button, '2, 1');",
						"    }",
						"    {",
						"      JButton button = new JButton('333');",
						"      add(button, '4, 1');",
						"    }",
						"  }",
						"}");
		FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
		//
		panel.refresh();
		try {
			layout.deleteColumn(1);
		} finally {
			panel.refresh_dispose();
		}
		// check source
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new FormLayout(new ColumnSpec[] {",
				"        FormSpecs.UNRELATED_GAP_COLSPEC,",
				"        FormSpecs.PREF_COLSPEC,},",
				"      new RowSpec[] {",
				"        FormSpecs.DEFAULT_ROWSPEC,}));",
				"    {",
				"      JButton button = new JButton('333');",
				"      add(button, '2, 1');",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_deleteGap() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new FormLayout(new ColumnSpec[] {",
						"        FormSpecs.DEFAULT_COLSPEC,",
						"        FormSpecs.RELATED_GAP_COLSPEC,",
						"        FormSpecs.PREF_COLSPEC,},",
						"      new RowSpec[] {",
						"        FormSpecs.DEFAULT_ROWSPEC,}));",
						"    {",
						"      JButton button = new JButton('111');",
						"      add(button, '1, 1');",
						"    }",
						"    {",
						"      JButton button = new JButton('333');",
						"      add(button, '3, 1');",
						"    }",
						"  }",
						"}");
		FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
		//
		panel.refresh();
		try {
			layout.deleteColumn(1);
		} finally {
			panel.refresh_dispose();
		}
		// check source
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new FormLayout(new ColumnSpec[] {",
				"        FormSpecs.DEFAULT_COLSPEC,",
				"        FormSpecs.PREF_COLSPEC,},",
				"      new RowSpec[] {",
				"        FormSpecs.DEFAULT_ROWSPEC,}));",
				"    {",
				"      JButton button = new JButton('111');",
				"      add(button, '1, 1');",
				"    }",
				"    {",
				"      JButton button = new JButton('333');",
				"      add(button, '2, 1');",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Delete contents
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_deleteContents() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new FormLayout(new ColumnSpec[] {",
						"        FormSpecs.DEFAULT_COLSPEC,},",
						"      new RowSpec[] {",
						"        FormSpecs.DEFAULT_ROWSPEC,}));",
						"    {",
						"      JButton button = new JButton();",
						"      add(button, '1, 1');",
						"    }",
						"  }",
						"}");
		FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
		//
		panel.refresh();
		try {
			layout.deleteColumnContents(0);
		} finally {
			panel.refresh_dispose();
		}
		// check source
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new FormLayout(new ColumnSpec[] {",
				"        FormSpecs.DEFAULT_COLSPEC,},",
				"      new RowSpec[] {",
				"        FormSpecs.DEFAULT_ROWSPEC,}));",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Split
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_split() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new FormLayout(new ColumnSpec[] {",
						"        FormSpecs.PREF_COLSPEC,",
						"        FormSpecs.MIN_COLSPEC,},",
						"      new RowSpec[] {",
						"        FormSpecs.DEFAULT_ROWSPEC,}));",
						"    {",
						"      JButton button = new JButton();",
						"      add(button, '1, 1');",
						"    }",
						"    {",
						"      JButton button = new JButton();",
						"      add(button, '2, 1');",
						"    }",
						"  }",
						"}");
		FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
		//
		panel.refresh();
		try {
			layout.splitColumn(0);
		} finally {
			panel.refresh_dispose();
		}
		// check source
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new FormLayout(new ColumnSpec[] {",
				"        FormSpecs.PREF_COLSPEC,",
				"        FormSpecs.RELATED_GAP_COLSPEC,",
				"        FormSpecs.PREF_COLSPEC,",
				"        FormSpecs.MIN_COLSPEC,},",
				"      new RowSpec[] {",
				"        FormSpecs.DEFAULT_ROWSPEC,}));",
				"    {",
				"      JButton button = new JButton();",
				"      add(button, '1, 1, 3, 1');",
				"    }",
				"    {",
				"      JButton button = new JButton();",
				"      add(button, '4, 1');",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// MOVE column
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_moveColumn_noOp() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new FormLayout(new ColumnSpec[] {",
						"        FormSpecs.DEFAULT_COLSPEC,},",
						"      new RowSpec[] {",
						"        FormSpecs.DEFAULT_ROWSPEC,}));",
						"    {",
						"      JButton button = new JButton('222');",
						"      add(button, '1, 1');",
						"    }",
						"  }",
						"}");
		FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
		// move
		panel.refresh();
		try {
			String expectedSource = m_lastEditor.getSource();
			layout.command_MOVE_COLUMN(0, 0);
			assertEditor(expectedSource, m_lastEditor);
		} finally {
			panel.refresh_dispose();
		}
	}

	@Test
	public void test_moveColumn_backward_gap() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new FormLayout(new ColumnSpec[] {",
						"        FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,",
						"        FormSpecs.MIN_COLSPEC,",
						"        FormSpecs.UNRELATED_GAP_COLSPEC,},",
						"      new RowSpec[] {",
						"        FormSpecs.DEFAULT_ROWSPEC,}));",
						"    {",
						"      JButton button = new JButton();",
						"      add(button, '2, 1');",
						"    }",
						"  }",
						"}");
		FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
		// move
		panel.refresh();
		try {
			layout.command_MOVE_COLUMN(2, 1);
		} finally {
			panel.refresh_dispose();
		}
		// check source
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new FormLayout(new ColumnSpec[] {",
				"        FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,",
				"        FormSpecs.UNRELATED_GAP_COLSPEC,",
				"        FormSpecs.MIN_COLSPEC,},",
				"      new RowSpec[] {",
				"        FormSpecs.DEFAULT_ROWSPEC,}));",
				"    {",
				"      JButton button = new JButton();",
				"      add(button, '3, 1');",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_moveColumn_forward_gap() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new FormLayout(new ColumnSpec[] {",
						"        FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,",
						"        FormSpecs.MIN_COLSPEC,",
						"        FormSpecs.UNRELATED_GAP_COLSPEC,},",
						"      new RowSpec[] {",
						"        FormSpecs.DEFAULT_ROWSPEC,}));",
						"    {",
						"      JButton button = new JButton();",
						"      add(button, '2, 1');",
						"    }",
						"  }",
						"}");
		FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
		// move
		panel.refresh();
		try {
			layout.command_MOVE_COLUMN(0, 2);
		} finally {
			panel.refresh_dispose();
		}
		// check source
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new FormLayout(new ColumnSpec[] {",
				"        FormSpecs.MIN_COLSPEC,",
				"        FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,",
				"        FormSpecs.UNRELATED_GAP_COLSPEC,},",
				"      new RowSpec[] {",
				"        FormSpecs.DEFAULT_ROWSPEC,}));",
				"    {",
				"      JButton button = new JButton();",
				"      add(button, '1, 1');",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_moveColumn_noGaps() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new FormLayout(new ColumnSpec[] {",
						"        FormSpecs.DEFAULT_COLSPEC,",
						"        FormSpecs.MIN_COLSPEC,",
						"        FormSpecs.PREF_COLSPEC,},",
						"      new RowSpec[] {",
						"        FormSpecs.DEFAULT_ROWSPEC,}));",
						"  }",
						"}");
		FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
		// backward
		{
			panel.refresh();
			try {
				layout.command_MOVE_COLUMN(2, 1);
			} finally {
				panel.refresh_dispose();
			}
			// check source
			assertEditor(
					"public class Test extends JPanel {",
					"  public Test() {",
					"    setLayout(new FormLayout(new ColumnSpec[] {",
					"        FormSpecs.DEFAULT_COLSPEC,",
					"        FormSpecs.PREF_COLSPEC,",
					"        FormSpecs.MIN_COLSPEC,},",
					"      new RowSpec[] {",
					"        FormSpecs.DEFAULT_ROWSPEC,}));",
					"  }",
					"}");
		}
		// forward
		{
			panel.refresh();
			try {
				layout.command_MOVE_COLUMN(0, 2);
			} finally {
				panel.refresh_dispose();
			}
			// check source
			assertEditor(
					"public class Test extends JPanel {",
					"  public Test() {",
					"    setLayout(new FormLayout(new ColumnSpec[] {",
					"        FormSpecs.PREF_COLSPEC,",
					"        FormSpecs.DEFAULT_COLSPEC,",
					"        FormSpecs.MIN_COLSPEC,},",
					"      new RowSpec[] {",
					"        FormSpecs.DEFAULT_ROWSPEC,}));",
					"  }",
					"}");
		}
	}

	@Test
	public void test_moveColumn_backward_Last2Inner() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new FormLayout(new ColumnSpec[] {",
						"        FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,",
						"        FormSpecs.MIN_COLSPEC,",
						"        FormSpecs.RELATED_GAP_COLSPEC,",
						"        FormSpecs.PREF_COLSPEC,",
						"        FormSpecs.UNRELATED_GAP_COLSPEC,",
						"        FormSpecs.BUTTON_COLSPEC,},",
						"      new RowSpec[] {",
						"        FormSpecs.DEFAULT_ROWSPEC,}));",
						"    {",
						"      JButton button = new JButton('222');",
						"      add(button, '2, 1');",
						"    }",
						"    {",
						"      JButton button = new JButton('444');",
						"      add(button, '4, 1');",
						"    }",
						"    {",
						"      JButton button = new JButton('666');",
						"      add(button, '6, 1');",
						"    }",
						"  }",
						"}");
		FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
		// move
		panel.refresh();
		try {
			layout.command_MOVE_COLUMN(5, 2);
		} finally {
			panel.refresh_dispose();
		}
		// check source
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new FormLayout(new ColumnSpec[] {",
				"        FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,",
				"        FormSpecs.MIN_COLSPEC,",
				"        FormSpecs.UNRELATED_GAP_COLSPEC,",
				"        FormSpecs.BUTTON_COLSPEC,",
				"        FormSpecs.RELATED_GAP_COLSPEC,",
				"        FormSpecs.PREF_COLSPEC,},",
				"      new RowSpec[] {",
				"        FormSpecs.DEFAULT_ROWSPEC,}));",
				"    {",
				"      JButton button = new JButton('222');",
				"      add(button, '2, 1');",
				"    }",
				"    {",
				"      JButton button = new JButton('444');",
				"      add(button, '6, 1');",
				"    }",
				"    {",
				"      JButton button = new JButton('666');",
				"      add(button, '4, 1');",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_moveColumn_backward_Last2First() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new FormLayout(new ColumnSpec[] {",
						"        FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,",
						"        FormSpecs.MIN_COLSPEC,",
						"        FormSpecs.UNRELATED_GAP_COLSPEC,",
						"        FormSpecs.BUTTON_COLSPEC,},",
						"      new RowSpec[] {",
						"        FormSpecs.DEFAULT_ROWSPEC,}));",
						"    {",
						"      JButton button = new JButton('222');",
						"      add(button, '2, 1');",
						"    }",
						"    {",
						"      JButton button = new JButton('444');",
						"      add(button, '4, 1');",
						"    }",
						"  }",
						"}");
		FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
		// move
		panel.refresh();
		try {
			layout.command_MOVE_COLUMN(3, 0);
		} finally {
			panel.refresh_dispose();
		}
		// check source
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new FormLayout(new ColumnSpec[] {",
				"        FormSpecs.UNRELATED_GAP_COLSPEC,",
				"        FormSpecs.BUTTON_COLSPEC,",
				"        FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,",
				"        FormSpecs.MIN_COLSPEC,},",
				"      new RowSpec[] {",
				"        FormSpecs.DEFAULT_ROWSPEC,}));",
				"    {",
				"      JButton button = new JButton('222');",
				"      add(button, '4, 1');",
				"    }",
				"    {",
				"      JButton button = new JButton('444');",
				"      add(button, '2, 1');",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_moveColumn_backward_2FirstNoGap() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new FormLayout(new ColumnSpec[] {",
						"        FormSpecs.MIN_COLSPEC,",
						"        FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,",
						"        FormSpecs.PREF_COLSPEC,},",
						"      new RowSpec[] {",
						"        FormSpecs.DEFAULT_ROWSPEC,}));",
						"  }",
						"}");
		FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
		// move
		panel.refresh();
		try {
			layout.command_MOVE_COLUMN(2, 0);
		} finally {
			panel.refresh_dispose();
		}
		// check source
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new FormLayout(new ColumnSpec[] {",
				"        FormSpecs.PREF_COLSPEC,",
				"        FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,",
				"        FormSpecs.MIN_COLSPEC,},",
				"      new RowSpec[] {",
				"        FormSpecs.DEFAULT_ROWSPEC,}));",
				"  }",
				"}");
	}

	@Test
	public void test_moveColumn_spanned_backwardExpand_forwardCollapse() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new FormLayout(new ColumnSpec[] {",
						"        FormSpecs.RELATED_GAP_COLSPEC,",
						"        FormSpecs.DEFAULT_COLSPEC,",
						"        FormSpecs.RELATED_GAP_COLSPEC,",
						"        FormSpecs.DEFAULT_COLSPEC,",
						"        FormSpecs.RELATED_GAP_COLSPEC,",
						"        FormSpecs.DEFAULT_COLSPEC,},",
						"      new RowSpec[] {",
						"        FormSpecs.DEFAULT_ROWSPEC,",
						"        FormSpecs.DEFAULT_ROWSPEC,}));",
						"    {",
						"      JButton button = new JButton('spanned');",
						"      add(button, '2, 1, 3, 1');",
						"    }",
						"    {",
						"      JButton button = new JButton();",
						"      add(button, '6, 1');",
						"    }",
						"  }",
						"}");
		FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
		// backward expand
		{
			panel.refresh();
			try {
				layout.command_MOVE_COLUMN(5, 2);
			} finally {
				panel.refresh_dispose();
			}
			// check source
			assertEditor(
					"public class Test extends JPanel {",
					"  public Test() {",
					"    setLayout(new FormLayout(new ColumnSpec[] {",
					"        FormSpecs.RELATED_GAP_COLSPEC,",
					"        FormSpecs.DEFAULT_COLSPEC,",
					"        FormSpecs.RELATED_GAP_COLSPEC,",
					"        FormSpecs.DEFAULT_COLSPEC,",
					"        FormSpecs.RELATED_GAP_COLSPEC,",
					"        FormSpecs.DEFAULT_COLSPEC,},",
					"      new RowSpec[] {",
					"        FormSpecs.DEFAULT_ROWSPEC,",
					"        FormSpecs.DEFAULT_ROWSPEC,}));",
					"    {",
					"      JButton button = new JButton('spanned');",
					"      add(button, '2, 1, 5, 1');",
					"    }",
					"    {",
					"      JButton button = new JButton();",
					"      add(button, '4, 1');",
					"    }",
					"  }",
					"}");
		}
		// forward collapse
		{
			panel.refresh();
			try {
				layout.command_MOVE_COLUMN(3, 6);
			} finally {
				panel.refresh_dispose();
			}
			// check source
			assertEditor(
					"public class Test extends JPanel {",
					"  public Test() {",
					"    setLayout(new FormLayout(new ColumnSpec[] {",
					"        FormSpecs.RELATED_GAP_COLSPEC,",
					"        FormSpecs.DEFAULT_COLSPEC,",
					"        FormSpecs.RELATED_GAP_COLSPEC,",
					"        FormSpecs.DEFAULT_COLSPEC,",
					"        FormSpecs.RELATED_GAP_COLSPEC,",
					"        FormSpecs.DEFAULT_COLSPEC,},",
					"      new RowSpec[] {",
					"        FormSpecs.DEFAULT_ROWSPEC,",
					"        FormSpecs.DEFAULT_ROWSPEC,}));",
					"    {",
					"      JButton button = new JButton('spanned');",
					"      add(button, '2, 1, 3, 1');",
					"    }",
					"    {",
					"      JButton button = new JButton();",
					"      add(button, '6, 1');",
					"    }",
					"  }",
					"}");
		}
	}

	@Test
	public void test_moveColumn_spanned_backwardCollapse_forwardExpand() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new FormLayout(new ColumnSpec[] {",
						"        FormSpecs.RELATED_GAP_COLSPEC,",
						"        FormSpecs.DEFAULT_COLSPEC,",
						"        FormSpecs.RELATED_GAP_COLSPEC,",
						"        FormSpecs.DEFAULT_COLSPEC,",
						"        FormSpecs.RELATED_GAP_COLSPEC,",
						"        FormSpecs.DEFAULT_COLSPEC,",
						"        FormSpecs.RELATED_GAP_COLSPEC,",
						"        FormSpecs.DEFAULT_COLSPEC,},",
						"      new RowSpec[] {",
						"        FormSpecs.DEFAULT_ROWSPEC,",
						"        FormSpecs.DEFAULT_ROWSPEC,}));",
						"    {",
						"      JButton button = new JButton('spanned');",
						"      add(button, '4, 1, 5, 1');",
						"    }",
						"    {",
						"      JButton button = new JButton();",
						"      add(button, '6, 1');",
						"    }",
						"  }",
						"}");
		FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
		// backward collapse
		{
			panel.refresh();
			try {
				layout.command_MOVE_COLUMN(5, 2);
			} finally {
				panel.refresh_dispose();
			}
			// check source
			assertEditor(
					"public class Test extends JPanel {",
					"  public Test() {",
					"    setLayout(new FormLayout(new ColumnSpec[] {",
					"        FormSpecs.RELATED_GAP_COLSPEC,",
					"        FormSpecs.DEFAULT_COLSPEC,",
					"        FormSpecs.RELATED_GAP_COLSPEC,",
					"        FormSpecs.DEFAULT_COLSPEC,",
					"        FormSpecs.RELATED_GAP_COLSPEC,",
					"        FormSpecs.DEFAULT_COLSPEC,",
					"        FormSpecs.RELATED_GAP_COLSPEC,",
					"        FormSpecs.DEFAULT_COLSPEC,},",
					"      new RowSpec[] {",
					"        FormSpecs.DEFAULT_ROWSPEC,",
					"        FormSpecs.DEFAULT_ROWSPEC,}));",
					"    {",
					"      JButton button = new JButton('spanned');",
					"      add(button, '6, 1, 3, 1');",
					"    }",
					"    {",
					"      JButton button = new JButton();",
					"      add(button, '4, 1');",
					"    }",
					"  }",
					"}");
		}
		// forward expand
		{
			panel.refresh();
			try {
				layout.command_MOVE_COLUMN(3, 6);
			} finally {
				panel.refresh_dispose();
			}
			// check source
			assertEditor(
					"public class Test extends JPanel {",
					"  public Test() {",
					"    setLayout(new FormLayout(new ColumnSpec[] {",
					"        FormSpecs.RELATED_GAP_COLSPEC,",
					"        FormSpecs.DEFAULT_COLSPEC,",
					"        FormSpecs.RELATED_GAP_COLSPEC,",
					"        FormSpecs.DEFAULT_COLSPEC,",
					"        FormSpecs.RELATED_GAP_COLSPEC,",
					"        FormSpecs.DEFAULT_COLSPEC,",
					"        FormSpecs.RELATED_GAP_COLSPEC,",
					"        FormSpecs.DEFAULT_COLSPEC,},",
					"      new RowSpec[] {",
					"        FormSpecs.DEFAULT_ROWSPEC,",
					"        FormSpecs.DEFAULT_ROWSPEC,}));",
					"    {",
					"      JButton button = new JButton('spanned');",
					"      add(button, '4, 1, 5, 1');",
					"    }",
					"    {",
					"      JButton button = new JButton();",
					"      add(button, '6, 1');",
					"    }",
					"  }",
					"}");
		}
	}

	/**
	 * Backward inner move.
	 */
	@Test
	public void test_moveColumn_forward_firstGapPrev2Last() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new FormLayout(new ColumnSpec[] {",
						"        FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,",
						"        FormSpecs.MIN_COLSPEC,",
						"        FormSpecs.RELATED_GAP_COLSPEC,",
						"        FormSpecs.PREF_COLSPEC,",
						"        FormSpecs.UNRELATED_GAP_COLSPEC,",
						"        FormSpecs.BUTTON_COLSPEC,},",
						"      new RowSpec[] {",
						"        FormSpecs.DEFAULT_ROWSPEC,}));",
						"    {",
						"      JButton button = new JButton('222');",
						"      add(button, '2, 1');",
						"    }",
						"    {",
						"      JButton button = new JButton('444');",
						"      add(button, '4, 1');",
						"    }",
						"    {",
						"      JButton button = new JButton('666');",
						"      add(button, '6, 1');",
						"    }",
						"  }",
						"}");
		FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
		// move
		panel.refresh();
		try {
			layout.command_MOVE_COLUMN(1, 6);
		} finally {
			panel.refresh_dispose();
		}
		// check source
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new FormLayout(new ColumnSpec[] {",
				"        FormSpecs.RELATED_GAP_COLSPEC,",
				"        FormSpecs.PREF_COLSPEC,",
				"        FormSpecs.UNRELATED_GAP_COLSPEC,",
				"        FormSpecs.BUTTON_COLSPEC,",
				"        FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,",
				"        FormSpecs.MIN_COLSPEC,},",
				"      new RowSpec[] {",
				"        FormSpecs.DEFAULT_ROWSPEC,}));",
				"    {",
				"      JButton button = new JButton('222');",
				"      add(button, '6, 1');",
				"    }",
				"    {",
				"      JButton button = new JButton('444');",
				"      add(button, '2, 1');",
				"    }",
				"    {",
				"      JButton button = new JButton('666');",
				"      add(button, '4, 1');",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_moveColumn_forward_firstGapNext2Inner() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new FormLayout(new ColumnSpec[] {",
						"        FormSpecs.MIN_COLSPEC,",
						"        FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,",
						"        FormSpecs.PREF_COLSPEC,",
						"        FormSpecs.DEFAULT_COLSPEC,},",
						"      new RowSpec[] {",
						"        FormSpecs.DEFAULT_ROWSPEC,}));",
						"  }",
						"}");
		FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
		// move
		panel.refresh();
		try {
			layout.command_MOVE_COLUMN(0, 3);
		} finally {
			panel.refresh_dispose();
		}
		// check source
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new FormLayout(new ColumnSpec[] {",
				"        FormSpecs.PREF_COLSPEC,",
				"        FormSpecs.MIN_COLSPEC,",
				"        FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,",
				"        FormSpecs.DEFAULT_COLSPEC,},",
				"      new RowSpec[] {",
				"        FormSpecs.DEFAULT_ROWSPEC,}));",
				"  }",
				"}");
	}

	@Test
	public void test_moveColumn_forward_first2Last() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new FormLayout(new ColumnSpec[] {",
						"        FormSpecs.MIN_COLSPEC,",
						"        FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,",
						"        FormSpecs.PREF_COLSPEC,",
						"        FormSpecs.DEFAULT_COLSPEC,},",
						"      new RowSpec[] {",
						"        FormSpecs.DEFAULT_ROWSPEC,}));",
						"  }",
						"}");
		FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
		// move
		panel.refresh();
		try {
			layout.command_MOVE_COLUMN(0, 4);
		} finally {
			panel.refresh_dispose();
		}
		// check source
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new FormLayout(new ColumnSpec[] {",
				"        FormSpecs.PREF_COLSPEC,",
				"        FormSpecs.DEFAULT_COLSPEC,",
				"        FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,",
				"        FormSpecs.MIN_COLSPEC,},",
				"      new RowSpec[] {",
				"        FormSpecs.DEFAULT_ROWSPEC,}));",
				"  }",
				"}");
	}

	@Test
	public void test_moveColumn_forward_firstNextGap2BeforeGap() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new FormLayout(new ColumnSpec[] {",
						"        FormSpecs.MIN_COLSPEC,",
						"        FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,",
						"        FormSpecs.PREF_COLSPEC,",
						"        FormSpecs.RELATED_GAP_COLSPEC,",
						"        FormSpecs.DEFAULT_COLSPEC,},",
						"      new RowSpec[] {",
						"        FormSpecs.DEFAULT_ROWSPEC,}));",
						"  }",
						"}");
		FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
		// move
		panel.refresh();
		try {
			layout.command_MOVE_COLUMN(0, 3);
		} finally {
			panel.refresh_dispose();
		}
		// check source
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new FormLayout(new ColumnSpec[] {",
				"        FormSpecs.PREF_COLSPEC,",
				"        FormSpecs.RELATED_GAP_COLSPEC,",
				"        FormSpecs.MIN_COLSPEC,",
				"        FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,",
				"        FormSpecs.DEFAULT_COLSPEC,},",
				"      new RowSpec[] {",
				"        FormSpecs.DEFAULT_ROWSPEC,}));",
				"  }",
				"}");
	}
}
