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
package org.eclipse.wb.tests.designer.swing.model.layout.gbl;

import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.EmptyAssociation;
import org.eclipse.wb.core.model.association.InvocationSecondaryAssociation;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.LocalUniqueVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.Activator;
import org.eclipse.wb.internal.swing.IExceptionConstants;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.ColumnInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.ColumnInfo.Alignment;
import org.eclipse.wb.internal.swing.model.layout.gbl.GridBagConstraintsInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.GridBagLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.IPreferenceConstants;
import org.eclipse.wb.internal.swing.model.layout.gbl.RowInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.VirtualConstraintsCreationSupport;
import org.eclipse.wb.internal.swing.model.layout.gbl.VirtualConstraintsVariableSupport;
import org.eclipse.wb.internal.swing.model.layout.gbl.actions.SetGrowAction;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;

import org.apache.commons.lang3.StringUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

/**
 * Test for {@link GridBagConstraintsInfo}.
 *
 * @author scheglov_ke
 */
public class GridBagConstraintsTest extends AbstractGridBagLayoutTest {
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
	 * Test for case when {@link GridBagConstraints} used for component, but container does not use
	 * {@link GridBagLayout}
	 */
	@Test
	public void test_notGridBagLayout() throws Exception {
		final ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  public Test() {",
						"    {",
						"      JButton button = new JButton();",
						"      GridBagConstraints gbc = new GridBagConstraints();",
						"      add(button, gbc);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		assertNoErrors(panel);
		// check hierarchy
		assertHierarchy(
				"{this: javax.swing.JPanel} {this} {/add(button, gbc)/}",
				"  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
				"  {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /add(button, gbc)/}",
				"    {new: java.awt.GridBagConstraints} {local-unique: gbc} {/new GridBagConstraints()/ /add(button, gbc)/}");
	}

	/**
	 * Test for {@link GridBagConstraintsInfo#getColumn()} and {@link GridBagConstraintsInfo#getRow()}
	 * .
	 */
	@Test
	public void test_access() throws Exception {
		final ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  public Test() {",
						"    GridBagLayout layout = new GridBagLayout();",
						"    layout.columnWidths = new int[] {0, 0};",
						"    layout.rowHeights = new int[] {0, 0};",
						"    layout.columnWeights = new double[] {0.0, Double.MIN_VALUE};",
						"    layout.rowWeights = new double[] {0.0, Double.MIN_VALUE};",
						"    setLayout(layout);",
						"    {",
						"      JButton button = new JButton();",
						"      GridBagConstraints gbc = new GridBagConstraints();",
						"      gbc.gridx = 0;",
						"      gbc.gridy = 0;",
						"      add(button, gbc);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		GridBagConstraintsInfo constraints = GridBagLayoutInfo.getConstraintsFor(button);
		// column/row can be accessed from "layout" and "constraints"
		assertSame(layout.getColumns().get(0), constraints.getColumn());
		assertSame(layout.getRows().get(0), constraints.getRow());
	}

	@Test
	public void test_properties() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new GridBagLayout());",
						"    {",
						"      JButton button = new JButton('button');",
						"      add(button, new GridBagConstraints(99, 0, 1, 1, 0.0, 0.0,",
						"          GridBagConstraints.CENTER, GridBagConstraints.BOTH,",
						"          new Insets(0, 0, 5, 5), 0, 0));",
						"    }",
						"  }",
						"}");
		panel.refresh();
		// prepare models
		ComponentInfo button = panel.getChildrenComponents().get(0);
		GridBagConstraintsInfo constraints = GridBagLayoutInfo.getConstraintsFor(button);
		assertInstanceOf(ConstructorCreationSupport.class, constraints.getCreationSupport());
		assertInstanceOf(EmptyVariableSupport.class, constraints.getVariableSupport());
		// check presentation
		assertVisible(constraints, false);
		// "Constraints" property should be contributed
		ComplexProperty constraintsProperty =
				(ComplexProperty) button.getPropertyByTitle("Constraints");
		assertNotNull(constraintsProperty);
		assertTrue(constraintsProperty.isModified());
		// check gridx
		{
			Property property = getConstraintsProperty(button, "gridx");
			// check current value
			assertEquals(99, property.getValue());
			assertEquals(99, constraints.getX());
			// set value
			{
				String expectedSource =
						StringUtils.replace(
								m_lastEditor.getSource(),
								"GridBagConstraints(99, ",
								"GridBagConstraints(1, ");
				property.setValue(1);
				assertEditor(expectedSource, m_lastEditor);
			}
			// check that internal field is also updated on refresh()
			assertEquals(1, constraints.getX());
		}
	}

	/**
	 * We should know "real" location even in {@link GridBagConstraints#RELATIVE} used as location.
	 */
	@Test
	public void test_locationRelative_1() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new GridBagLayout());",
						"    {",
						"      JButton button_1 = new JButton('button 1');",
						"      add(button_1, new GridBagConstraints());",
						"    }",
						"    {",
						"      JButton button_2 = new JButton('button 1');",
						"      add(button_2, new GridBagConstraints());",
						"    }",
						"  }",
						"}");
		panel.refresh();
		GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
		// button_1
		{
			ComponentInfo button_1 = panel.getChildrenComponents().get(0);
			GridBagConstraintsInfo constraints = GridBagLayoutInfo.getConstraintsFor(button_1);
			assertEquals(0, constraints.getX());
			assertEquals(0, constraints.getY());
			assertEquals(1, constraints.getWidth());
			assertEquals(1, constraints.getHeight());
		}
		// button_2
		{
			ComponentInfo button_2 = panel.getChildrenComponents().get(1);
			GridBagConstraintsInfo constraints = GridBagLayoutInfo.getConstraintsFor(button_2);
			assertEquals(1, constraints.getX());
			assertEquals(0, constraints.getY());
			assertEquals(1, constraints.getWidth());
			assertEquals(1, constraints.getHeight());
		}
		// IGridInfo
		{
			IGridInfo gridInfo = layout.getGridInfo();
			assertEquals(2, gridInfo.getColumnCount());
			assertEquals(1, gridInfo.getRowCount());
			assertEquals(2, layout.getColumns().size());
			assertEquals(1, layout.getRows().size());
		}
	}

	/**
	 * We should know "real" location even in {@link GridBagConstraints#RELATIVE} used as location.
	 */
	@Test
	public void test_locationRelative_2() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new GridBagLayout());",
						"    {",
						"      JButton button_1 = new JButton('button 1');",
						"      add(button_1);",
						"    }",
						"    {",
						"      JButton button_2 = new JButton('button 2');",
						"      add(button_2);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
		ComponentInfo button_1 = panel.getChildrenComponents().get(0);
		ComponentInfo button_2 = panel.getChildrenComponents().get(1);
		// button_1
		{
			GridBagConstraintsInfo constraints = GridBagLayoutInfo.getConstraintsFor(button_1);
			assertEquals(0, constraints.getX());
			assertEquals(0, constraints.getY());
			assertEquals(1, constraints.getWidth());
			assertEquals(1, constraints.getHeight());
		}
		// button_2
		{
			GridBagConstraintsInfo constraints = GridBagLayoutInfo.getConstraintsFor(button_2);
			assertEquals(1, constraints.getX());
			assertEquals(0, constraints.getY());
			assertEquals(1, constraints.getWidth());
			assertEquals(1, constraints.getHeight());
		}
		// IGridInfo
		{
			IGridInfo gridInfo = layout.getGridInfo();
			assertEquals(2, gridInfo.getColumnCount());
			assertEquals(1, gridInfo.getRowCount());
			assertEquals(2, layout.getColumns().size());
			assertEquals(1, layout.getRows().size());
			// no gaps, columns/rows are directly on component bounds
			{
				// columns: begin
				assertEquals(button_1.getComponent().getX(), gridInfo.getColumnIntervals()[0].begin());
				assertEquals(button_2.getComponent().getX(), gridInfo.getColumnIntervals()[1].begin());
				// rows: begin
				assertEquals(button_2.getComponent().getY(), gridInfo.getRowIntervals()[0].begin());
				// columns: length
				assertEquals(button_1.getComponent().getWidth(), gridInfo.getColumnIntervals()[0].length());
				assertEquals(button_2.getComponent().getWidth(), gridInfo.getColumnIntervals()[1].length());
				// rows: length
				assertEquals(button_1.getComponent().getHeight(), gridInfo.getRowIntervals()[0].length());
				assertEquals(button_2.getComponent().getHeight(), gridInfo.getRowIntervals()[0].length());
			}
		}
	}

	/**
	 * Test for virtual {@link GridBagConstraintsInfo} and materializing it in short form.
	 */
	@Test
	public void test_virtualShort() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new GridBagLayout());",
						"    {",
						"      JButton button = new JButton('button');",
						"      add(button);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		String initialSource = m_lastEditor.getSource();
		// prepare models
		ComponentInfo button = panel.getChildrenComponents().get(0);
		GridBagConstraintsInfo constraints = GridBagLayoutInfo.getConstraintsFor(button);
		// we just asked for constraints, nothing should be changed yet
		assertEditor(initialSource, m_lastEditor);
		// check virtual state
		{
			{
				CreationSupport creation = constraints.getCreationSupport();
				assertInstanceOf(VirtualConstraintsCreationSupport.class, creation);
				assertFalse(creation.isJavaInfo(null));
				assertNull(creation.getNode());
				// object
				assertEquals("virtual-GBL-constraints", creation.toString());
			}
			{
				VariableSupport variable = constraints.getVariableSupport();
				assertInstanceOf(VirtualConstraintsVariableSupport.class, variable);
				assertEquals("(virtual GBL constraints)", variable.getTitle());
				assertEquals("virtual-GBL-constraints", variable.toString());
				try {
					variable.getStatementTarget();
					fail();
				} catch (IllegalStateException e) {
				}
			}
			assertInstanceOf(EmptyAssociation.class, constraints.getAssociation());
		}
		// delete
		assertTrue(constraints.canDelete());
		constraints.delete();
		{
			GridBagConstraintsInfo newConstraints = GridBagLayoutInfo.getConstraintsFor(button);
			assertNotSame(constraints, newConstraints);
			constraints = newConstraints;
		}
		// set property, so materialize
		Activator.getDefault().getPreferenceStore().setValue(IPreferenceConstants.P_GBC_LONG, false);
		Property property = getConstraintsProperty(button, "gridx");
		property.setValue(1);
		assertEditor(
				"class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new GridBagLayout());",
				"    {",
				"      JButton button = new JButton('button');",
				"      GridBagConstraints gbc = new GridBagConstraints();",
				"      gbc.gridx = 1;",
				"      add(button, gbc);",
				"    }",
				"  }",
				"}");
		// new state of "constraints"
		{
			{
				CreationSupport creationSupport = constraints.getCreationSupport();
				assertInstanceOf(ConstructorCreationSupport.class, creationSupport);
			}
			{
				VariableSupport variableSupport = constraints.getVariableSupport();
				assertInstanceOf(LocalUniqueVariableSupport.class, variableSupport);
				assertEquals("gbc", variableSupport.getName());
			}
			{
				Association association = constraints.getAssociation();
				assertInstanceOf(InvocationSecondaryAssociation.class, association);
				assertEquals("add(button, gbc)", association.getSource());
			}
			// composite check for creation, variable and especially for related nodes
			assertEquals(
					"{new: java.awt.GridBagConstraints} {local-unique: gbc} {/new GridBagConstraints()/ /add(button, gbc)/ /gbc.gridx = 1/}",
					constraints.toString());
		}
	}

	/**
	 * Test for virtual {@link GridBagConstraintsInfo} and materializing it in long form.
	 */
	@Test
	public void test_virtualLong() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new GridBagLayout());",
						"    {",
						"      JButton button = new JButton('button');",
						"      add(button);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// set property, so materialize
		Activator.getDefault().getPreferenceStore().setValue(IPreferenceConstants.P_GBC_LONG, true);
		Property property = getConstraintsProperty(button, "gridx");
		property.setValue(1);
		assertEditor(
				"class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new GridBagLayout());",
				"    {",
				"      JButton button = new JButton(\"button\");",
				"      add(button, new GridBagConstraints(1, GridBagConstraints.RELATIVE, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Better message for "more than one constraints".
	 */
	@Test
	public void test_moreThanOneConstraints() throws Exception {
		try {
			parseContainer(
					"public class Test extends JPanel {",
					"  public Test() {",
					"    setLayout(new GridBagLayout());",
					"    {",
					"      JButton button = new JButton();",
					"      add(button, new GridBagConstraints());",
					"      add(button, new GridBagConstraints());",
					"    }",
					"  }",
					"}");
		} catch (Throwable e) {
			DesignerException de = DesignerExceptionUtils.getDesignerException(e);
			assertEquals(IExceptionConstants.MORE_THAN_ONE_CONSTRAINTS, de.getCode());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access: location
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test that {@link GridBagConstraintsInfo} materialized when we try to use method like
	 * {@link GridBagConstraintsInfo#setX(int)}.
	 */
	@Test
	public void test_accessMaterialize() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new GridBagLayout());",
						"    {",
						"      JButton button = new JButton();",
						"      add(button);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		GridBagConstraintsInfo constraints = GridBagLayoutInfo.getConstraintsFor(button);
		// modify
		Activator.getDefault().getPreferenceStore().setValue(IPreferenceConstants.P_GBC_LONG, false);
		constraints.setX(1);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new GridBagLayout());",
				"    {",
				"      JButton button = new JButton();",
				"      GridBagConstraints gbc = new GridBagConstraints();",
				"      gbc.gridx = 1;",
				"      add(button, gbc);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Test for {@link GridBagConstraintsInfo#materializeLocation()}.
	 */
	@Test
	public void test_accessMaterialize_2() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new GridBagLayout());",
						"    {",
						"      JButton button = new JButton();",
						"      add(button);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		GridBagConstraintsInfo constraints = GridBagLayoutInfo.getConstraintsFor(button);
		// materialize
		Activator.getDefault().getPreferenceStore().setValue(IPreferenceConstants.P_GBC_LONG, false);
		constraints.materializeLocation();
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new GridBagLayout());",
				"    {",
				"      JButton button = new JButton();",
				"      GridBagConstraints gbc = new GridBagConstraints();",
				"      gbc.gridx = 0;",
				"      gbc.gridy = 0;",
				"      add(button, gbc);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Test for {@link GridBagConstraintsInfo#setX(int)}.
	 */
	@Test
	public void test_setX() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new GridBagLayout());",
						"    {",
						"      JButton button = new JButton();",
						"      GridBagConstraints c = new GridBagConstraints();",
						"      add(button, c);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		GridBagConstraintsInfo constraints = GridBagLayoutInfo.getConstraintsFor(button);
		// x := 1
		constraints.setX(1);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new GridBagLayout());",
				"    {",
				"      JButton button = new JButton();",
				"      GridBagConstraints c = new GridBagConstraints();",
				"      c.gridx = 1;",
				"      add(button, c);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Test for "gridx" property.
	 */
	@Test
	public void test_setProperty_gridx() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    GridBagLayout gridBagLayout = new GridBagLayout();",
						"    gridBagLayout.columnWidths = new int[]{100, 200};",
						"    gridBagLayout.rowHeights = new int[]{50, 75};",
						"    setLayout(gridBagLayout);",
						"    {",
						"      JButton button = new JButton();",
						"      GridBagConstraints c = new GridBagConstraints();",
						"      add(button, c);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// x := 1, in grid
		getConstraintsProperty(button, "gridx").setValue(1);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    GridBagLayout gridBagLayout = new GridBagLayout();",
				"    gridBagLayout.columnWidths = new int[]{100, 200};",
				"    gridBagLayout.rowHeights = new int[]{50, 75};",
				"    setLayout(gridBagLayout);",
				"    {",
				"      JButton button = new JButton();",
				"      GridBagConstraints c = new GridBagConstraints();",
				"      c.gridx = 1;",
				"      add(button, c);",
				"    }",
				"  }",
				"}");
		// x := 100, too big, ignored
		getConstraintsProperty(button, "gridx").setValue(100);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    GridBagLayout gridBagLayout = new GridBagLayout();",
				"    gridBagLayout.columnWidths = new int[]{100, 200};",
				"    gridBagLayout.rowHeights = new int[]{50, 75};",
				"    setLayout(gridBagLayout);",
				"    {",
				"      JButton button = new JButton();",
				"      GridBagConstraints c = new GridBagConstraints();",
				"      c.gridx = 1;",
				"      add(button, c);",
				"    }",
				"  }",
				"}");
		// x := 2, expand grid
		getConstraintsProperty(button, "gridx").setValue(2);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    GridBagLayout gridBagLayout = new GridBagLayout();",
				"    gridBagLayout.columnWidths = new int[]{100, 200, 0};",
				"    gridBagLayout.rowHeights = new int[]{50, 75};",
				"    setLayout(gridBagLayout);",
				"    {",
				"      JButton button = new JButton();",
				"      GridBagConstraints c = new GridBagConstraints();",
				"      c.gridx = 2;",
				"      add(button, c);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Test for {@link GridBagConstraintsInfo#setY(int)}.
	 */
	@Test
	public void test_setY() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new GridBagLayout());",
						"    {",
						"      JButton button = new JButton();",
						"      GridBagConstraints c = new GridBagConstraints();",
						"      add(button, c);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		GridBagConstraintsInfo constraints = GridBagLayoutInfo.getConstraintsFor(button);
		// modify
		constraints.setY(1);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new GridBagLayout());",
				"    {",
				"      JButton button = new JButton();",
				"      GridBagConstraints c = new GridBagConstraints();",
				"      c.gridy = 1;",
				"      add(button, c);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Test for "gridy" property.
	 */
	@Test
	public void test_setProperty_gridy() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    GridBagLayout gridBagLayout = new GridBagLayout();",
						"    gridBagLayout.columnWidths = new int[]{100, 200};",
						"    gridBagLayout.rowHeights = new int[]{50, 75};",
						"    setLayout(gridBagLayout);",
						"    {",
						"      JButton button = new JButton();",
						"      GridBagConstraints c = new GridBagConstraints();",
						"      add(button, c);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// y := 1, in grid
		getConstraintsProperty(button, "gridy").setValue(1);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    GridBagLayout gridBagLayout = new GridBagLayout();",
				"    gridBagLayout.columnWidths = new int[]{100, 200};",
				"    gridBagLayout.rowHeights = new int[]{50, 75};",
				"    setLayout(gridBagLayout);",
				"    {",
				"      JButton button = new JButton();",
				"      GridBagConstraints c = new GridBagConstraints();",
				"      c.gridy = 1;",
				"      add(button, c);",
				"    }",
				"  }",
				"}");
		// y := 3, expand grid
		getConstraintsProperty(button, "gridy").setValue(3);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    GridBagLayout gridBagLayout = new GridBagLayout();",
				"    gridBagLayout.columnWidths = new int[]{100, 200};",
				"    gridBagLayout.rowHeights = new int[]{50, 75, 0, 0};",
				"    setLayout(gridBagLayout);",
				"    {",
				"      JButton button = new JButton();",
				"      GridBagConstraints c = new GridBagConstraints();",
				"      c.gridy = 3;",
				"      add(button, c);",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access: size
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link GridBagConstraintsInfo#setWidth(int)}.
	 */
	@Test
	public void test_accessLocation_width() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new GridBagLayout());",
						"    {",
						"      JButton button = new JButton();",
						"      GridBagConstraints c = new GridBagConstraints();",
						"      add(button, c);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		GridBagConstraintsInfo constraints = GridBagLayoutInfo.getConstraintsFor(button);
		// modify
		constraints.setWidth(2);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new GridBagLayout());",
				"    {",
				"      JButton button = new JButton();",
				"      GridBagConstraints c = new GridBagConstraints();",
				"      c.gridwidth = 2;",
				"      add(button, c);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Test for {@link GridBagConstraintsInfo#setHeight(int)}.
	 */
	@Test
	public void test_accessLocation_height() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new GridBagLayout());",
						"    {",
						"      JButton button = new JButton();",
						"      GridBagConstraints c = new GridBagConstraints();",
						"      add(button, c);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		GridBagConstraintsInfo constraints = GridBagLayoutInfo.getConstraintsFor(button);
		// modify
		constraints.setHeight(2);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new GridBagLayout());",
				"    {",
				"      JButton button = new JButton();",
				"      GridBagConstraints c = new GridBagConstraints();",
				"      c.gridheight = 2;",
				"      add(button, c);",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access: insets
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link GridBagConstraintsInfo#setInsets(String, int)}.
	 */
	@Test
	public void test_accessOther_insets() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new GridBagLayout());",
						"    {",
						"      JButton button = new JButton();",
						"      GridBagConstraints c = new GridBagConstraints();",
						"      add(button, c);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		GridBagConstraintsInfo constraints = GridBagLayoutInfo.getConstraintsFor(button);
		// read
		assertEquals(0, constraints.getInsets("right"));
		// modify
		constraints.setInsets("right", 5);
		assertEquals(5, constraints.getInsets("right"));
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new GridBagLayout());",
				"    {",
				"      JButton button = new JButton();",
				"      GridBagConstraints c = new GridBagConstraints();",
				"      c.insets = new Insets(0, 0, 0, 5);",
				"      add(button, c);",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Horizontal/vertical alignment
	//
	////////////////////////////////////////////////////////////////////////////
	private static final class AlignmentTestData {
		int fill;
		int anchor;
		ColumnInfo.Alignment m_horizontalAlignment;
		RowInfo.Alignment m_verticalAlignment;

		public AlignmentTestData(int fill,
				int anchor,
				Alignment horizontalAlignment,
				RowInfo.Alignment verticalAlignment) {
			this.anchor = anchor;
			this.fill = fill;
			m_horizontalAlignment = horizontalAlignment;
			m_verticalAlignment = verticalAlignment;
		}
	}

	/**
	 * Array with several alignment test cases.
	 */
	private static final AlignmentTestData[] m_alignmentTestArray = {
			new AlignmentTestData(GridBagConstraints.NONE,
					GridBagConstraints.CENTER,
					ColumnInfo.Alignment.CENTER,
					RowInfo.Alignment.CENTER),
			new AlignmentTestData(GridBagConstraints.HORIZONTAL,
					GridBagConstraints.CENTER,
					ColumnInfo.Alignment.FILL,
					RowInfo.Alignment.CENTER),
			new AlignmentTestData(GridBagConstraints.BOTH,
					GridBagConstraints.CENTER,
					ColumnInfo.Alignment.FILL,
					RowInfo.Alignment.FILL),
			new AlignmentTestData(GridBagConstraints.NONE,
					GridBagConstraints.BASELINE_LEADING,
					ColumnInfo.Alignment.LEFT,
					RowInfo.Alignment.BASELINE),
			new AlignmentTestData(GridBagConstraints.NONE,
					GridBagConstraints.ABOVE_BASELINE_TRAILING,
					ColumnInfo.Alignment.RIGHT,
					RowInfo.Alignment.BASELINE_ABOVE),
			new AlignmentTestData(GridBagConstraints.HORIZONTAL,
					GridBagConstraints.BELOW_BASELINE_TRAILING,
					ColumnInfo.Alignment.FILL,
					RowInfo.Alignment.BASELINE_BELOW),};

	/**
	 * Test alignments using prepared array.
	 */
	@Test
	public void test_alignments() throws Exception {
		for (AlignmentTestData testData : m_alignmentTestArray) {
			assertSame(testData.m_horizontalAlignment, ReflectionUtils.invokeMethod2(
					GridBagConstraintsInfo.class,
					"getHorizontalAlignment",
					int.class,
					int.class,
					testData.fill,
					testData.anchor));
			assertSame(testData.m_verticalAlignment, ReflectionUtils.invokeMethod2(
					GridBagConstraintsInfo.class,
					"getVerticalAlignment",
					int.class,
					int.class,
					testData.fill,
					testData.anchor));
		}
	}

	@Test
	public void test_alignments_unknownHorizontal() throws Exception {
		try {
			ReflectionUtils.invokeMethod2(
					GridBagConstraintsInfo.class,
					"getHorizontalAlignment",
					int.class,
					int.class,
					-1,
					-1);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void test_alignments_unknownVertical() throws Exception {
		try {
			ReflectionUtils.invokeMethod2(
					GridBagConstraintsInfo.class,
					"getVerticalAlignment",
					int.class,
					int.class,
					-1,
					-1);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

	/**
	 * Test {@link GridBagConstraintsInfo#getHorizontalAlignment()} and
	 * {@link GridBagConstraintsInfo#getVerticalAlignment()}, i.e. from model parsed from source.
	 */
	@Test
	public void test_getAlignment_bySource() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new GridBagLayout());",
						"    {",
						"      JButton button = new JButton();",
						"      GridBagConstraints gbc = new GridBagConstraints();",
						"      gbc.anchor = GridBagConstraints.SOUTHWEST;",
						"      add(button, gbc);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo component = panel.getChildrenComponents().get(0);
		GridBagConstraintsInfo constraints = GridBagLayoutInfo.getConstraintsFor(component);
		assertSame(ColumnInfo.Alignment.LEFT, constraints.getHorizontalAlignment());
		assertSame(RowInfo.Alignment.BOTTOM, constraints.getVerticalAlignment());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// setAlignment()
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_setAlignment() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new GridBagLayout());",
						"    {",
						"      JButton button = new JButton();",
						"      GridBagConstraints gbc = new GridBagConstraints();",
						"      add(button, gbc);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		// do checks
		{
			// only horizontal
			check_setAlignment(panel, ColumnInfo.Alignment.LEFT, null, null, "WEST");
			check_setAlignment(panel, ColumnInfo.Alignment.RIGHT, null, null, "EAST");
			check_setAlignment(panel, ColumnInfo.Alignment.FILL, null, "HORIZONTAL", null);
			// only vertical
			check_setAlignment(panel, ColumnInfo.Alignment.CENTER, RowInfo.Alignment.CENTER, null, null);
			check_setAlignment(panel, null, RowInfo.Alignment.FILL, "VERTICAL", null);
			check_setAlignment(panel, null, RowInfo.Alignment.BOTTOM, null, "SOUTH");
			// mixed
			check_setAlignment(
					panel,
					ColumnInfo.Alignment.RIGHT,
					RowInfo.Alignment.TOP,
					null,
					"NORTHEAST");
			check_setAlignment(
					panel,
					ColumnInfo.Alignment.CENTER,
					RowInfo.Alignment.BOTTOM,
					null,
					"SOUTH");
			check_setAlignment(panel, ColumnInfo.Alignment.FILL, RowInfo.Alignment.FILL, "BOTH", null);
		}
	}

	/**
	 * Checks that
	 * {@link GridBagConstraintsInfo#setAlignment(org.eclipse.wb.internal.swing.model.layout.gbl.ColumnInfo.Alignment, org.eclipse.wb.internal.swing.model.layout.gbl.RowInfo.Alignment)}
	 * assigned correct source into fill/anchor properties.
	 */
	private void check_setAlignment(ContainerInfo panel,
			ColumnInfo.Alignment hAlignment,
			RowInfo.Alignment vAlignment,
			String expectedFillSource,
			String expectedAnchorSource) throws Exception {
		ComponentInfo component = panel.getChildrenComponents().get(0);
		GridBagConstraintsInfo constraints = GridBagLayoutInfo.getConstraintsFor(component);
		// set alignment
		if (hAlignment == null) {
			ColumnInfo.Alignment old_hAlignment = constraints.getHorizontalAlignment();
			constraints.setVerticalAlignment(vAlignment);
			assertSame(vAlignment, constraints.getVerticalAlignment());
			assertSame(old_hAlignment, constraints.getHorizontalAlignment());
		} else if (vAlignment == null) {
			RowInfo.Alignment old_vAlignment = constraints.getVerticalAlignment();
			constraints.setHorizontalAlignment(hAlignment);
			assertSame(hAlignment, constraints.getHorizontalAlignment());
			assertSame(old_vAlignment, constraints.getVerticalAlignment());
		} else {
			constraints.setAlignment(hAlignment, vAlignment);
			assertSame(hAlignment, constraints.getHorizontalAlignment());
			assertSame(vAlignment, constraints.getVerticalAlignment());
		}
		// fill
		{
			GenericPropertyImpl property = (GenericPropertyImpl) constraints.getPropertyByTitle("fill");
			Expression expression = property.getExpression();
			if (expectedFillSource == null) {
				assertNull(expression);
			} else {
				assertEquals("GridBagConstraints." + expectedFillSource, m_lastEditor.getSource(expression));
			}
		}
		// anchor
		{
			GenericPropertyImpl property = (GenericPropertyImpl) constraints.getPropertyByTitle("anchor");
			Expression expression = property.getExpression();
			if (expectedAnchorSource == null) {
				assertNull(expression);
			} else {
				assertEquals(
						"GridBagConstraints." + expectedAnchorSource,
						m_lastEditor.getSource(expression));
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Context menu
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_contextMenu_horizontalLeft() throws Exception {
		check_contextMenu_alignmentHorizontal("&Left", ColumnInfo.Alignment.LEFT, "WEST", "NONE");
	}

	@Test
	public void test_contextMenu_horizontalCenter() throws Exception {
		check_contextMenu_alignmentHorizontal("&Center", ColumnInfo.Alignment.CENTER, "CENTER", "NONE");
	}

	@Test
	public void test_contextMenu_horizontalRight() throws Exception {
		check_contextMenu_alignmentHorizontal("&Right", ColumnInfo.Alignment.RIGHT, "EAST", "NONE");
	}

	@Test
	public void test_contextMenu_horizontalFill() throws Exception {
		check_contextMenu_alignmentHorizontal(
				"&Fill",
				ColumnInfo.Alignment.FILL,
				"CENTER",
				"HORIZONTAL");
	}

	@Test
	public void test_contextMenu_verticalTop() throws Exception {
		check_contextMenu_alignmentVertical("&Top", RowInfo.Alignment.TOP, "NORTH", "NONE");
	}

	@Test
	public void test_contextMenu_verticalCenter() throws Exception {
		check_contextMenu_alignmentVertical("&Center", RowInfo.Alignment.CENTER, "CENTER", "NONE");
	}

	@Test
	public void test_contextMenu_verticalBottom() throws Exception {
		check_contextMenu_alignmentVertical("&Bottom", RowInfo.Alignment.BOTTOM, "SOUTH", "NONE");
	}

	@Test
	public void test_contextMenu_verticalFill() throws Exception {
		check_contextMenu_alignmentVertical("&Fill", RowInfo.Alignment.FILL, "CENTER", "VERTICAL");
	}

	@Ignore
	@Test
	public void test_contextMenu_verticalBaseline() throws Exception {
		check_contextMenu_alignmentVertical("Baseline", RowInfo.Alignment.BASELINE, "BASELINE", "NONE");
	}

	@Ignore
	@Test
	public void test_contextMenu_verticalBaselineAbove() throws Exception {
		check_contextMenu_alignmentVertical(
				"Above baseline",
				RowInfo.Alignment.BASELINE_ABOVE,
				"ABOVE_BASELINE",
				"NONE");
	}

	@Ignore
	@Test
	public void test_contextMenu_verticalBaselineBelow() throws Exception {
		check_contextMenu_alignmentVertical(
				"Below baseline",
				RowInfo.Alignment.BASELINE_BELOW,
				"BELOW_BASELINE",
				"NONE");
	}

	private void check_contextMenu_alignmentHorizontal(String actionText,
			ColumnInfo.Alignment expectedHorizontalAlignment,
			String expectedAnchorSource,
			String expectedFillSource) throws Exception {
		check_contextMenu_alignment(
				"Horizontal alignment",
				actionText,
				expectedHorizontalAlignment,
				RowInfo.Alignment.CENTER,
				expectedAnchorSource,
				expectedFillSource);
	}

	private void check_contextMenu_alignmentVertical(String actionText,
			RowInfo.Alignment expectedVerticalAlignment,
			String expectedAnchorSource,
			String expectedFillSource) throws Exception {
		check_contextMenu_alignment(
				"Vertical alignment",
				actionText,
				ColumnInfo.Alignment.CENTER,
				expectedVerticalAlignment,
				expectedAnchorSource,
				expectedFillSource);
	}

	private void check_contextMenu_alignment(String managerText,
			String actionText,
			ColumnInfo.Alignment expectedHorizontalAlignment,
			RowInfo.Alignment expectedVerticalAlignment,
			String expectedAnchorSource,
			String expectedFillSource) throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new GridBagLayout());",
						"    {",
						"      JButton button = new JButton();",
						"      GridBagConstraints gbc = new GridBagConstraints();",
						"      gbc.anchor = GridBagConstraints.CENTER;",
						"      gbc.fill = GridBagConstraints.NONE;",
						"      add(button, gbc);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// prepare constraints
		GridBagConstraintsInfo constraints = GridBagLayoutInfo.getConstraintsFor(button);
		assertSame(ColumnInfo.Alignment.CENTER, constraints.getHorizontalAlignment());
		assertSame(RowInfo.Alignment.CENTER, constraints.getVerticalAlignment());
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
		alignmentAction.setChecked(true);
		alignmentAction.run();
		assertSame(expectedHorizontalAlignment, constraints.getHorizontalAlignment());
		assertSame(expectedVerticalAlignment, constraints.getVerticalAlignment());
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new GridBagLayout());",
				"    {",
				"      JButton button = new JButton();",
				"      GridBagConstraints gbc = new GridBagConstraints();",
				"      gbc.anchor = GridBagConstraints." + expectedAnchorSource + ";",
				"      gbc.fill = GridBagConstraints." + expectedFillSource + ";",
				"      add(button, gbc);",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Context menu: grow
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link SetGrowAction}.
	 */
	@Test
	public void test_contextMenu_grow() throws Exception {
		final ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  public Test() {",
						"    GridBagLayout layout = new GridBagLayout();",
						"    layout.columnWidths = new int[] {0, 0};",
						"    layout.rowHeights = new int[] {0, 0};",
						"    layout.columnWeights = new double[] {0.0, Double.MIN_VALUE};",
						"    layout.rowWeights = new double[] {0.0, Double.MIN_VALUE};",
						"    setLayout(layout);",
						"    {",
						"      JButton button = new JButton();",
						"      GridBagConstraints gbc = new GridBagConstraints();",
						"      gbc.gridx = 0;",
						"      gbc.gridy = 0;",
						"      add(button, gbc);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// no weight initially
		assertFalse(layout.getColumns().get(0).hasWeight());
		// prepare alignment manager
		IMenuManager alignmentManager;
		{
			MenuManager contextMenu = getDesignerMenuManager();
			panel.getBroadcastObject().addContextMenu(List.of(button), button, contextMenu);
			alignmentManager = findChildMenuManager(contextMenu, "Horizontal alignment");
			assertNotNull(alignmentManager);
		}
		// set grow
		IAction growAction = findChildAction(alignmentManager, "Grow");
		growAction.setChecked(true);
		growAction.run();
		assertTrue(layout.getColumns().get(0).hasWeight());
		assertEditor(
				"class Test extends JPanel {",
				"  public Test() {",
				"    GridBagLayout layout = new GridBagLayout();",
				"    layout.columnWidths = new int[] {0, 0};",
				"    layout.rowHeights = new int[] {0, 0};",
				"    layout.columnWeights = new double[] {1.0, Double.MIN_VALUE};",
				"    layout.rowWeights = new double[] {0.0, Double.MIN_VALUE};",
				"    setLayout(layout);",
				"    {",
				"      JButton button = new JButton();",
				"      GridBagConstraints gbc = new GridBagConstraints();",
				"      gbc.gridx = 0;",
				"      gbc.gridy = 0;",
				"      add(button, gbc);",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Code generation: name, based on template
	//
	////////////////////////////////////////////////////////////////////////////
	private void check_nameTemplate(String template, String... lines) throws Exception {
		ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new GridBagLayout());",
						"    {",
						"      JButton button = new JButton('button');",
						"      add(button, new GridBagConstraints());",
						"    }",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button = getJavaInfoByName("button");
		Activator.getDefault().getPreferenceStore().setValue(
				org.eclipse.wb.internal.swing.model.layout.gbl.IPreferenceConstants.P_CONSTRAINTS_NAME_TEMPLATE,
				template);
		GridBagLayoutInfo.getConstraintsFor(button).getPropertyByTitle("fill").setValue(
				java.awt.GridBagConstraints.HORIZONTAL);
		assertEditor(lines);
	}

	/**
	 * Template "${defaultName}" means that name should be based on name of type.
	 */
	@Test
	public void test_nameTemplate_useDefaultName() throws Exception {
		check_nameTemplate(
				org.eclipse.wb.internal.core.model.variable.SyncParentChildVariableNameSupport.TEMPLATE_FOR_DEFAULT,
				"class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new GridBagLayout());",
				"    {",
				"      JButton button = new JButton('button');",
				"      GridBagConstraints gbc = new GridBagConstraints();",
				"      gbc.fill = GridBagConstraints.HORIZONTAL;",
				"      add(button, gbc);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Generate name using "${dataAcronym}${controlName-cap}" template.
	 */
	@Test
	public void test_nameTemplate_alternativeTemplate_1() throws Exception {
		check_nameTemplate(
				"${constraintsAcronym}${componentName-cap}",
				"class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new GridBagLayout());",
				"    {",
				"      JButton button = new JButton('button');",
				"      GridBagConstraints gbcButton = new GridBagConstraints();",
				"      gbcButton.fill = GridBagConstraints.HORIZONTAL;",
				"      add(button, gbcButton);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Generate name using "${controlName}${dataClassName}" template.
	 */
	@Test
	public void test_nameTemplate_alternativeTemplate_2() throws Exception {
		check_nameTemplate(
				"${componentName}${constraintsClassName}",
				"class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new GridBagLayout());",
				"    {",
				"      JButton button = new JButton('button');",
				"      GridBagConstraints buttonGridBagConstraints = new GridBagConstraints();",
				"      buttonGridBagConstraints.fill = GridBagConstraints.HORIZONTAL;",
				"      add(button, buttonGridBagConstraints);",
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
	 * @return the sub-property of "Constraints" property.
	 */
	private Property getConstraintsProperty(ComponentInfo component, String title) throws Exception {
		ComplexProperty constraintsProperty =
				(ComplexProperty) component.getPropertyByTitle("Constraints");
		Property[] properties = constraintsProperty.getProperties();
		return getPropertyByTitle(properties, title);
	}
}
