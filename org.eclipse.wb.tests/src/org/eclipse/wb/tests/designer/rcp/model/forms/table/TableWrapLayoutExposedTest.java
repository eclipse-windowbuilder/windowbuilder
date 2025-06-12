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
package org.eclipse.wb.tests.designer.rcp.model.forms.table;

import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.ExposedPropertyCreationSupport;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.TableWrapDataInfo;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.TableWrapLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.ImplicitLayoutDataCreationSupport;
import org.eclipse.wb.internal.swt.model.layout.ImplicitLayoutDataVariableSupport;
import org.eclipse.wb.internal.swt.model.layout.LayoutDataInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.model.forms.AbstractFormsTest;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link TableWrapLayoutInfo} and exposed {@link ControlInfo}'s.
 *
 * @author scheglov_ke
 */
public class TableWrapLayoutExposedTest extends AbstractFormsTest {
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
	// Delete
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for simple exposed "Button" without explicit {@link LayoutDataInfo} set.
	 */
	@Test
	public void test_deleteExposedComponent_noExplicitData() throws Exception {
		configureForDelete();
		// parse
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    MyComposite myComposite = new MyComposite(this, SWT.NONE);",
						"  }",
						"}");
		shell.refresh();
		CompositeInfo myComposite = (CompositeInfo) shell.getChildrenControls().get(0);
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) myComposite.getLayout();
		ControlInfo button = myComposite.getChildrenControls().get(0);
		// check initial TableWrapData
		{
			TableWrapDataInfo layoutData = layout.getTableWrapData(button);
			assertInstanceOf(ImplicitLayoutDataCreationSupport.class, layoutData.getCreationSupport());
			assertInstanceOf(ImplicitLayoutDataVariableSupport.class, layoutData.getVariableSupport());
		}
		// do operations
		{
			assertInstanceOf(ExposedPropertyCreationSupport.class, button.getCreationSupport());
			// delete, no visible change expected
			assertTrue(button.canDelete());
			button.delete();
			assertEditor(
					"class Test extends Shell {",
					"  Test() {",
					"    MyComposite myComposite = new MyComposite(this, SWT.NONE);",
					"  }",
					"}");
			assertFalse(button.isDeleted());
			// check new TableWrapData
			{
				TableWrapDataInfo layoutData = layout.getTableWrapData(button);
				assertInstanceOf(ImplicitLayoutDataCreationSupport.class, layoutData.getCreationSupport());
				assertInstanceOf(ImplicitLayoutDataVariableSupport.class, layoutData.getVariableSupport());
			}
		}
	}

	/**
	 * Test for simple exposed "Button" with explicit {@link LayoutDataInfo} set.
	 */
	@Test
	public void test_deleteExposedComponent_withExplicitData() throws Exception {
		configureForDelete();
		// parse
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    MyComposite myComposite = new MyComposite(this, SWT.NONE);",
						"    myComposite.getButton().setLayoutData(new TableWrapData());",
						"  }",
						"}");
		shell.refresh();
		CompositeInfo myComposite = (CompositeInfo) shell.getChildrenControls().get(0);
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) myComposite.getLayout();
		ControlInfo button = myComposite.getChildrenControls().get(0);
		// check initial TableWrapData
		{
			TableWrapDataInfo layoutData = layout.getTableWrapData(button);
			assertInstanceOf(ConstructorCreationSupport.class, layoutData.getCreationSupport());
			assertInstanceOf(EmptyVariableSupport.class, layoutData.getVariableSupport());
		}
		// do operations
		{
			assertInstanceOf(ExposedPropertyCreationSupport.class, button.getCreationSupport());
			// delete, "explicit" TableWrapData is gone
			assertTrue(button.canDelete());
			button.delete();
			assertEditor(
					"class Test extends Shell {",
					"  Test() {",
					"    MyComposite myComposite = new MyComposite(this, SWT.NONE);",
					"  }",
					"}");
			assertTrue(myComposite.getChildren().contains(button));
			// check new TableWrapData
			{
				TableWrapDataInfo layoutData = layout.getTableWrapData(button);
				assertInstanceOf(ImplicitLayoutDataCreationSupport.class, layoutData.getCreationSupport());
				assertInstanceOf(ImplicitLayoutDataVariableSupport.class, layoutData.getVariableSupport());
			}
		}
	}

	/**
	 * Test when delete first of two exposed components.
	 */
	@Test
	public void test_deleteWhenTwoExposed() throws Exception {
		createASTCompilationUnit(
				"test",
				"MyComposite.java",
				getTestSource(
						"public class MyComposite extends Composite {",
						"  private Button m_button;",
						"  private Text m_text;",
						"  public MyComposite(Composite parent, int style) {",
						"    super(parent, style);",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      setLayout(layout);",
						"    }",
						"    m_button = new Button(this, SWT.NONE);",
						"    m_text = new Text(this, SWT.NONE);",
						"  }",
						"  public Button getButton() {",
						"    return m_button;",
						"  }",
						"  public Text getText() {",
						"    return m_text;",
						"  }",
						"}"));
		waitForAutoBuild();
		// parse
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    MyComposite myComposite = new MyComposite(this, SWT.NONE);",
						"  }",
						"}");
		shell.refresh();
		CompositeInfo myComposite = (CompositeInfo) shell.getChildrenControls().get(0);
		ControlInfo button = myComposite.getChildrenControls().get(0);
		// do operations
		{
			button.delete();
			assertEditor(
					"class Test extends Shell {",
					"  Test() {",
					"    MyComposite myComposite = new MyComposite(this, SWT.NONE);",
					"  }",
					"}");
		}
	}

	/**
	 * Configures project for delete tests.
	 */
	private void configureForDelete() throws Exception {
		createASTCompilationUnit(
				"test",
				"MyComposite.java",
				getTestSource(
						"public class MyComposite extends Composite {",
						"  private Button m_button;",
						"  public MyComposite(Composite parent, int style) {",
						"    super(parent, style);",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      layout.numColumns = 2;",
						"      setLayout(layout);",
						"    }",
						"    m_button = new Button(this, SWT.NONE);",
						"    m_button.setLayoutData(new TableWrapData(TableWrapData.FILL, TableWrapData.MIDDLE));",
						"  }",
						"  public Button getButton() {",
						"    return m_button;",
						"  }",
						"}"));
		waitForAutoBuild();
	}
}