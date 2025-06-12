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
package org.eclipse.wb.tests.designer.swt.model.layouts.grid;

import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.ExposedPropertyCreationSupport;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.swt.model.layout.ImplicitLayoutDataCreationSupport;
import org.eclipse.wb.internal.swt.model.layout.ImplicitLayoutDataVariableSupport;
import org.eclipse.wb.internal.swt.model.layout.LayoutDataInfo;
import org.eclipse.wb.internal.swt.model.layout.grid.GridDataInfo;
import org.eclipse.wb.internal.swt.model.layout.grid.GridLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link GridLayoutInfo}.
 *
 * @author scheglov_ke
 */
public class GridLayoutExposedTest extends RcpModelTest {
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
		ControlInfo button = myComposite.getChildrenControls().get(0);
		// check initial GridData
		{
			GridDataInfo gridData = GridLayoutInfo.getGridData(button);
			assertInstanceOf(ImplicitLayoutDataCreationSupport.class, gridData.getCreationSupport());
			assertInstanceOf(ImplicitLayoutDataVariableSupport.class, gridData.getVariableSupport());
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
			// check new GridData
			{
				GridDataInfo gridData = GridLayoutInfo.getGridData(button);
				assertInstanceOf(ImplicitLayoutDataCreationSupport.class, gridData.getCreationSupport());
				assertInstanceOf(ImplicitLayoutDataVariableSupport.class, gridData.getVariableSupport());
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
						"    myComposite.getButton().setLayoutData(new GridData());",
						"  }",
						"}");
		shell.refresh();
		CompositeInfo myComposite = (CompositeInfo) shell.getChildrenControls().get(0);
		ControlInfo button = myComposite.getChildrenControls().get(0);
		// check initial GridData
		{
			GridDataInfo gridData = GridLayoutInfo.getGridData(button);
			assertInstanceOf(ConstructorCreationSupport.class, gridData.getCreationSupport());
			assertInstanceOf(EmptyVariableSupport.class, gridData.getVariableSupport());
		}
		// do operations
		{
			assertInstanceOf(ExposedPropertyCreationSupport.class, button.getCreationSupport());
			// delete, "explicit" GridData is gone
			assertTrue(button.canDelete());
			button.delete();
			assertEditor(
					"class Test extends Shell {",
					"  Test() {",
					"    MyComposite myComposite = new MyComposite(this, SWT.NONE);",
					"  }",
					"}");
			assertTrue(myComposite.getChildren().contains(button));
			// check new GridData
			{
				GridDataInfo gridData = GridLayoutInfo.getGridData(button);
				assertInstanceOf(ImplicitLayoutDataCreationSupport.class, gridData.getCreationSupport());
				assertInstanceOf(ImplicitLayoutDataVariableSupport.class, gridData.getVariableSupport());
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
						"    setLayout(new GridLayout(1, false));",
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
		setFileContentSrc(
				"test/MyComposite.java",
				getTestSource(
						"public class MyComposite extends Composite {",
						"  private Button m_button;",
						"  public MyComposite(Composite parent, int style) {",
						"    super(parent, style);",
						"    setLayout(new GridLayout(1, false));",
						"    m_button = new Button(this, SWT.NONE);",
						"    m_button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));",
						"  }",
						"  public Button getButton() {",
						"    return m_button;",
						"  }",
						"}"));
		waitForAutoBuild();
	}
}