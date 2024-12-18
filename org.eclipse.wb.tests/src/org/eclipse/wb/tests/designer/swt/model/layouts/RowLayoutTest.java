/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.swt.model.layouts;

import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.internal.core.model.generation.GenerationSettings;
import org.eclipse.wb.internal.core.model.generation.statement.StatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.generation.statement.flat.FlatStatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.swt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.RowDataInfo;
import org.eclipse.wb.internal.swt.model.layout.RowLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.BTestUtils;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.swt.SWT;

import org.junit.Test;

/**
 * Test for {@link RowLayoutInfo}.
 *
 * @author lobas_av
 */
public class RowLayoutTest extends RcpModelTest {
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
	public void test_typeField() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    RowLayout layout = new RowLayout(SWT.VERTICAL);",
						"    setLayout(layout);",
						"  }",
						"}");
		RowLayoutInfo layout = (RowLayoutInfo) shell.getLayout();
		// prepare "type" property
		GenericProperty typeProperty = (GenericProperty) layout.getPropertyByTitle("type");
		// set "type" to HORIZONTAL
		{
			typeProperty.setExpression("org.eclipse.swt.SWT.HORIZONTAL", SWT.HORIZONTAL);
			assertEditor(
					"class Test extends Shell {",
					"  Test() {",
					"    RowLayout layout = new RowLayout(SWT.HORIZONTAL);",
					"    setLayout(layout);",
					"  }",
					"}");
		}
		// set "type" to VERTICAL
		{
			typeProperty.setExpression("org.eclipse.swt.SWT.VERTICAL", SWT.VERTICAL);
			assertEditor(
					"class Test extends Shell {",
					"  Test() {",
					"    RowLayout layout = new RowLayout(SWT.VERTICAL);",
					"    setLayout(layout);",
					"  }",
					"}");
		}
	}

	@Test
	public void test_isHorizontal_1() throws Exception {
		test_isHorizontal(new String[]{
				"class Test {",
				"  public static void main(String[] args) {",
				"    Shell shell = new Shell();",
				"    shell.setLayout(new RowLayout(SWT.HORIZONTAL));",
				"  }",
		"}"});
	}

	@Test
	public void test_isHorizontal_2() throws Exception {
		test_isHorizontal(new String[]{
				"class Test {",
				"  public static void main(String[] args) {",
				"    Shell shell = new Shell();",
				"    shell.setLayout(new RowLayout());",
				"  }",
		"}"});
	}

	private void test_isHorizontal(String[] lines) throws Exception {
		CompositeInfo shellInfo = parseComposite(lines);
		// check layout
		RowLayoutInfo layoutInfo = (RowLayoutInfo) shellInfo.getLayout();
		assertNotNull(layoutInfo);
		// refresh
		shellInfo.refresh();
		// check isHorizontal == true
		assertTrue(layoutInfo.isHorizontal());
		// set vertical
		Property property = layoutInfo.getPropertyByTitle("type");
		assertNotNull(property);
		property.setValue(SWT.VERTICAL);
		// check isHorizontal == false
		assertFalse(layoutInfo.isHorizontal());
	}

	@Test
	public void test_AddControls() throws Exception {
		CompositeInfo shellInfo =
				parseComposite(
						"class Test {",
						"  public static void main(String[] args) {",
						"    Shell shell = new Shell();",
						"    shell.setLayout(new RowLayout());",
						"  }",
						"}");
		assertTrue(shellInfo.getChildrenControls().isEmpty());
		RowLayoutInfo layout = (RowLayoutInfo) shellInfo.getLayout();
		// add "Button" to end
		ControlInfo buttonInfo = BTestUtils.createButton();
		layout.command_CREATE(buttonInfo, null);
		assertNotNull(buttonInfo.getAssociation());
		assertEquals(1, shellInfo.getChildrenControls().size());
		assertSame(buttonInfo, shellInfo.getChildrenControls().get(0));
		assertEditor(
				"class Test {",
				"  public static void main(String[] args) {",
				"    Shell shell = new Shell();",
				"    shell.setLayout(new RowLayout());",
				"    {",
				"      Button button = new Button(shell, SWT.NONE);",
				"    }",
				"  }",
				"}");
		assertRelatedNodes(shellInfo, new String[]{
				"new Shell()",
				"shell.setLayout(new RowLayout())",
		"new Button(shell, SWT.NONE)"});
		// add "Label" before "Button"
		ControlInfo labelInfo = BTestUtils.createControl("org.eclipse.swt.widgets.Label");
		layout.command_CREATE(labelInfo, buttonInfo);
		assertNotNull(labelInfo.getAssociation());
		assertEquals(2, shellInfo.getChildrenControls().size());
		assertSame(labelInfo, shellInfo.getChildrenControls().get(0));
		assertSame(buttonInfo, shellInfo.getChildrenControls().get(1));
		assertEditor(
				"class Test {",
				"  public static void main(String[] args) {",
				"    Shell shell = new Shell();",
				"    shell.setLayout(new RowLayout());",
				"    {",
				"      Label label = new Label(shell, SWT.NONE);",
				"      label.setText('New Label');",
				"    }",
				"    {",
				"      Button button = new Button(shell, SWT.NONE);",
				"    }",
				"  }",
				"}");
		assertInstanceOf(RowDataInfo.class, LayoutInfo.getLayoutData(labelInfo));
	}

	@Test
	public void test_MoveControls() throws Exception {
		CompositeInfo shellInfo =
				parseComposite(
						"class Test {",
						"  public static void main(String[] args) {",
						"    Shell shell = new Shell();",
						"    shell.setLayout(new RowLayout());",
						"    {",
						"      Label label = new Label(shell, SWT.NONE);",
						"    }",
						"    {",
						"      Button button = new Button(shell, SWT.NONE);",
						"    }",
						"    {",
						"      Combo combo = new Combo(shell, SWT.READ_ONLY);",
						"    }",
						"  }",
						"}");
		RowLayoutInfo layout = (RowLayoutInfo) shellInfo.getLayout();
		ControlInfo labelInfo = shellInfo.getChildrenControls().get(0);
		ControlInfo buttonInfo = shellInfo.getChildrenControls().get(1);
		ControlInfo comboInfo = shellInfo.getChildrenControls().get(2);
		// associations
		Association labelAssociation = labelInfo.getAssociation();
		Association buttonAssociation = buttonInfo.getAssociation();
		Association comboAssociation = comboInfo.getAssociation();
		assertNotNull(labelAssociation);
		assertNotNull(buttonAssociation);
		assertNotNull(comboAssociation);
		// move "Combo" before "Button"
		layout.command_MOVE(comboInfo, buttonInfo);
		assertSame(labelInfo, shellInfo.getChildrenControls().get(0));
		assertSame(comboInfo, shellInfo.getChildrenControls().get(1));
		assertSame(buttonInfo, shellInfo.getChildrenControls().get(2));
		assertSame(labelAssociation, labelInfo.getAssociation());
		assertSame(buttonAssociation, buttonInfo.getAssociation());
		assertSame(comboAssociation, comboInfo.getAssociation());
		assertEditor(
				"class Test {",
				"  public static void main(String[] args) {",
				"    Shell shell = new Shell();",
				"    shell.setLayout(new RowLayout());",
				"    {",
				"      Label label = new Label(shell, SWT.NONE);",
				"    }",
				"    {",
				"      Combo combo = new Combo(shell, SWT.READ_ONLY);",
				"    }",
				"    {",
				"      Button button = new Button(shell, SWT.NONE);",
				"    }",
				"  }",
				"}");
		// move "Label" to end
		layout.command_MOVE(labelInfo, null);
		assertSame(comboInfo, shellInfo.getChildrenControls().get(0));
		assertSame(buttonInfo, shellInfo.getChildrenControls().get(1));
		assertSame(labelInfo, shellInfo.getChildrenControls().get(2));
		assertSame(labelAssociation, labelInfo.getAssociation());
		assertSame(buttonAssociation, buttonInfo.getAssociation());
		assertSame(comboAssociation, comboInfo.getAssociation());
		assertEditor(
				"class Test {",
				"  public static void main(String[] args) {",
				"    Shell shell = new Shell();",
				"    shell.setLayout(new RowLayout());",
				"    {",
				"      Combo combo = new Combo(shell, SWT.READ_ONLY);",
				"    }",
				"    {",
				"      Button button = new Button(shell, SWT.NONE);",
				"    }",
				"    {",
				"      Label label = new Label(shell, SWT.NONE);",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_ReparentControls() throws Exception {
		CompositeInfo shellInfo =
				parseComposite(
						"class Test {",
						"  public static void main(String[] args) {",
						"    Shell shell = new Shell();",
						"    shell.setLayout(new RowLayout());",
						"    {",
						"      Label label = new Label(shell, SWT.NONE);",
						"    }",
						"    {",
						"      Button button = new Button(shell, SWT.NONE);",
						"    }",
						"    {",
						"      Composite composite = new Composite(shell, SWT.NONE);",
						"      composite.setLayout(new RowLayout());",
						"      {",
						"        Combo combo = new Combo(composite, SWT.READ_ONLY);",
						"      }",
						"    }",
						"  }",
						"}");
		assertEquals(3, shellInfo.getChildrenControls().size());
		ControlInfo labelInfo = shellInfo.getChildrenControls().get(0);
		ControlInfo buttonInfo = shellInfo.getChildrenControls().get(1);
		CompositeInfo compositeInfo = (CompositeInfo) shellInfo.getChildrenControls().get(2);
		assertEquals(1, compositeInfo.getChildrenControls().size());
		ControlInfo comboInfo = compositeInfo.getChildrenControls().get(0);
		RowLayoutInfo layoutInfo = (RowLayoutInfo) compositeInfo.getLayout();
		// associations
		Association labelAssociation = labelInfo.getAssociation();
		Association buttonAssociation = buttonInfo.getAssociation();
		Association comboAssociation = comboInfo.getAssociation();
		assertNotNull(labelAssociation);
		assertNotNull(buttonAssociation);
		assertNotNull(comboAssociation);
		// move "Button" before "Combo"
		layoutInfo.command_MOVE(buttonInfo, comboInfo);
		assertEquals(2, shellInfo.getChildrenControls().size());
		assertEquals(2, compositeInfo.getChildrenControls().size());
		assertSame(labelInfo, shellInfo.getChildrenControls().get(0));
		assertSame(compositeInfo, shellInfo.getChildrenControls().get(1));
		assertSame(buttonInfo, compositeInfo.getChildrenControls().get(0));
		assertSame(comboInfo, compositeInfo.getChildrenControls().get(1));
		assertSame(labelAssociation, labelInfo.getAssociation());
		assertSame(buttonAssociation, buttonInfo.getAssociation());
		assertSame(comboAssociation, comboInfo.getAssociation());
		assertEditor(
				"class Test {",
				"  public static void main(String[] args) {",
				"    Shell shell = new Shell();",
				"    shell.setLayout(new RowLayout());",
				"    {",
				"      Label label = new Label(shell, SWT.NONE);",
				"    }",
				"    {",
				"      Composite composite = new Composite(shell, SWT.NONE);",
				"      composite.setLayout(new RowLayout());",
				"      {",
				"        Button button = new Button(composite, SWT.NONE);",
				"      }",
				"      {",
				"        Combo combo = new Combo(composite, SWT.READ_ONLY);",
				"      }",
				"    }",
				"  }",
				"}");
		// move "Label" to end of "Composite"
		layoutInfo.command_MOVE(labelInfo, null);
		assertEquals(1, shellInfo.getChildrenControls().size());
		assertEquals(3, compositeInfo.getChildrenControls().size());
		assertSame(compositeInfo, shellInfo.getChildrenControls().get(0));
		assertSame(buttonInfo, compositeInfo.getChildrenControls().get(0));
		assertSame(comboInfo, compositeInfo.getChildrenControls().get(1));
		assertSame(labelInfo, compositeInfo.getChildrenControls().get(2));
		assertSame(labelAssociation, labelInfo.getAssociation());
		assertSame(buttonAssociation, buttonInfo.getAssociation());
		assertSame(comboAssociation, comboInfo.getAssociation());
		assertEditor(
				"class Test {",
				"  public static void main(String[] args) {",
				"    Shell shell = new Shell();",
				"    shell.setLayout(new RowLayout());",
				"    {",
				"      Composite composite = new Composite(shell, SWT.NONE);",
				"      composite.setLayout(new RowLayout());",
				"      {",
				"        Button button = new Button(composite, SWT.NONE);",
				"      }",
				"      {",
				"        Combo combo = new Combo(composite, SWT.READ_ONLY);",
				"      }",
				"      {",
				"        Label label = new Label(composite, SWT.NONE);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_changeRowData() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test {",
						"  public static void main(String[] args) {",
						"    Shell shell = new Shell();",
						"    shell.setLayout(new RowLayout());",
						"    {",
						"      Button button = new Button(shell, SWT.NONE);",
						"      button.setText('New Button');",
						"    }",
						"  }",
						"}");
		//
		ControlInfo button = shell.getChildrenControls().get(0);
		RowDataInfo dataInfo = (RowDataInfo) button.getChildrenJava().get(0);
		//
		Property width = dataInfo.getPropertyByTitle("width");
		assertNotNull(width);
		assertEquals(-1, width.getValue());
		//
		Property height = dataInfo.getPropertyByTitle("height");
		assertNotNull(height);
		assertEquals(-1, height.getValue());
		//
		dataInfo.setWidth(40);
		assertEquals(40, width.getValue());
		dataInfo.setHeight(30);
		assertEquals(30, height.getValue());
		//
		assertEditor(
				"class Test {",
				"  public static void main(String[] args) {",
				"    Shell shell = new Shell();",
				"    shell.setLayout(new RowLayout());",
				"    {",
				"      Button button = new Button(shell, SWT.NONE);",
				"      button.setLayoutData(new RowData(40, 30));",
				"      button.setText('New Button');",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Test that when we add new <code>Button</code> before existing <code>Button</code> in flat
	 * style, non-conflicting name is generated.
	 */
	@Test
	public void test_addBefore_nameConflict() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"    ",
						"    Button button = new Button(this, SWT.NONE);",
						"    button.setText('0');",
						"  }",
						"}");
		RowLayoutInfo rowLayout = (RowLayoutInfo) shell.getLayout();
		ControlInfo existingButton = shell.getChildrenControls().get(0);
		// add new Button
		{
			GenerationSettings generationSettings =
					shell.getDescription().getToolkit().getGenerationSettings();
			StatementGeneratorDescription oldStatement = generationSettings.getStatement();
			try {
				generationSettings.setStatement(FlatStatementGeneratorDescription.INSTANCE);
				ControlInfo newButton = BTestUtils.createButton();
				rowLayout.command_CREATE(newButton, existingButton);
			} finally {
				generationSettings.setStatement(oldStatement);
			}
		}
		// check result
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new RowLayout());",
				"    ",
				"    Button button_1 = new Button(this, SWT.NONE);",
				"    ",
				"    Button button = new Button(this, SWT.NONE);",
				"    button.setText('0');",
				"  }",
				"}");
	}
}