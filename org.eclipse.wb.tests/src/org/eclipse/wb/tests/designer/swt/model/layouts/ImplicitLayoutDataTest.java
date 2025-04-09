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
package org.eclipse.wb.tests.designer.swt.model.layouts;

import org.eclipse.wb.core.model.association.ImplicitObjectAssociation;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.LocalUniqueVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.swt.model.layout.FillLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.ImplicitLayoutDataCreationSupport;
import org.eclipse.wb.internal.swt.model.layout.ImplicitLayoutDataVariableSupport;
import org.eclipse.wb.internal.swt.model.layout.LayoutDataInfo;
import org.eclipse.wb.internal.swt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.grid.GridDataInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.swt.widgets.Control;

import org.junit.Test;

/**
 * @author lobas_av
 * @author scheglov_ke
 */
public class ImplicitLayoutDataTest extends RcpModelTest {
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
	 * Test that when exposed {@link ControlInfo} has existing {@link LayoutDataInfo}, it handled as
	 * "implicit".
	 */
	@Test
	public void test_implicitData() throws Exception {
		prepareGridComposite_forImplicit();
		CompositeInfo composite =
				parseComposite(
						"public class Test extends GridComposite {",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"  }",
						"}");
		composite.refresh();
		ControlInfo button = composite.getChildrenControls().get(0);
		//
		GridDataInfo gridData = (GridDataInfo) button.getChildrenJava().get(0);
		// check CreationSupport
		CreationSupport creationSupport = gridData.getCreationSupport();
		assertInstanceOf(ImplicitLayoutDataCreationSupport.class, creationSupport);
		assertEquals(
				"implicit-layout-data: org.eclipse.swt.layout.GridData",
				creationSupport.toString());
		assertTrue(creationSupport.canDelete());
		assertEquals(button.getCreationSupport().getNode(), creationSupport.getNode());
		// check VariableSupport
		VariableSupport variableSupport = gridData.getVariableSupport();
		assertInstanceOf(ImplicitLayoutDataVariableSupport.class, variableSupport);
		assertEquals("implicit-layout-data", variableSupport.toString());
		assertEquals("(implicit layout data)", variableSupport.getTitle());
		assertTrue(variableSupport.isDefault());
		// check association
		assertInstanceOf(ImplicitObjectAssociation.class, gridData.getAssociation());
		// set property value, so "materialize" GridData
		{
			Property property = gridData.getPropertyByTitle("widthHint");
			assertNotNull(property);
			// check existing value (set in GridComposite)
			assertEquals(100, property.getValue());
			// set new value for "widthHint"
			property.setValue(110);
			assertEquals(110, property.getValue());
			assertEditor(
					"public class Test extends GridComposite {",
					"  public Test(Composite parent, int style) {",
					"    super(parent, style);",
					"    ((GridData) getButton().getLayoutData()).widthHint = 110;",
					"  }",
					"}");
			// check supports
			assertSame(creationSupport, gridData.getCreationSupport());
			assertNotSame(variableSupport, gridData.getVariableSupport());
			assertNotInstanceOf(ImplicitLayoutDataVariableSupport.class, gridData.getVariableSupport());
			assertInstanceOf(ImplicitObjectAssociation.class, gridData.getAssociation());
		}
		// delete, so any "explicit" ASTNode's are removed, we GridData is still "implicit"
		gridData.delete();
		assertEditor(
				"public class Test extends GridComposite {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"  }",
				"}");
		assertSame(creationSupport, gridData.getCreationSupport());
		assertInstanceOf(ImplicitLayoutDataVariableSupport.class, gridData.getVariableSupport());
		assertInstanceOf(ImplicitObjectAssociation.class, gridData.getAssociation());
		assertEquals(100, gridData.getPropertyByTitle("widthHint").getValue());
	}

	/**
	 * Test for advanced "implicit" materialize/inline tricks.
	 */
	@Test
	public void test_implicitData_advanced() throws Exception {
		prepareGridComposite_forImplicit();
		// extend GridComposite
		CompositeInfo composite =
				parseComposite(
						"public class Test extends GridComposite {",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"  }",
						"}");
		composite.refresh();
		ControlInfo button = composite.getChildrenControls().get(0);
		//
		GridDataInfo gridData = (GridDataInfo) button.getChildrenJava().get(0);
		// check CreationSupport
		CreationSupport creationSupport = gridData.getCreationSupport();
		assertInstanceOf(ImplicitLayoutDataCreationSupport.class, creationSupport);
		assertEquals(
				"implicit-layout-data: org.eclipse.swt.layout.GridData",
				creationSupport.toString());
		assertTrue(creationSupport.canDelete());
		assertEquals(button.getCreationSupport().getNode(), creationSupport.getNode());
		// check VariableSupport
		VariableSupport variableSupport = gridData.getVariableSupport();
		assertInstanceOf(ImplicitLayoutDataVariableSupport.class, variableSupport);
		assertEquals("implicit-layout-data", variableSupport.toString());
		assertEquals("(implicit layout data)", variableSupport.getTitle());
		assertTrue(variableSupport.isDefault());
		// check association
		assertInstanceOf(ImplicitObjectAssociation.class, gridData.getAssociation());
		// set property value, so "materialize" GridData
		{
			Property property = gridData.getPropertyByTitle("widthHint");
			assertNotNull(property);
			// check existing value (set in GridComposite)
			assertEquals(100, property.getValue());
			// set new value for "widthHint"
			property.setValue(110);
			assertEquals(110, property.getValue());
			assertEditor(
					"public class Test extends GridComposite {",
					"  public Test(Composite parent, int style) {",
					"    super(parent, style);",
					"    ((GridData) getButton().getLayoutData()).widthHint = 110;",
					"  }",
					"}");
			// check supports
			assertSame(creationSupport, gridData.getCreationSupport());
			assertNotSame(variableSupport, gridData.getVariableSupport());
			assertNotInstanceOf(ImplicitLayoutDataVariableSupport.class, gridData.getVariableSupport());
			assertInstanceOf(ImplicitObjectAssociation.class, gridData.getAssociation());
		}
		// use "heightHint"
		{
			Property property = gridData.getPropertyByTitle("heightHint");
			assertEquals(50, property.getValue());
			// set value, so force variable (and Block)
			property.setValue(60);
			assertEquals(60, property.getValue());
			assertEditor(
					"public class Test extends GridComposite {",
					"  public Test(Composite parent, int style) {",
					"    super(parent, style);",
					"    {",
					"      GridData gridData = (GridData) getButton().getLayoutData();",
					"      gridData.heightHint = 60;",
					"      gridData.widthHint = 110;",
					"    }",
					"  }",
					"}");
			// remove value, so inline
			property.setValue(Property.UNKNOWN_VALUE);
			assertEquals(50, property.getValue());
			assertEditor(
					"public class Test extends GridComposite {",
					"  public Test(Composite parent, int style) {",
					"    super(parent, style);",
					"    ((GridData) getButton().getLayoutData()).widthHint = 110;",
					"  }",
					"}");
		}
	}

	private void prepareGridComposite_forImplicit() throws Exception {
		setFileContentSrc(
				"test/GridComposite.java",
				getTestSource(
						"public class GridComposite extends Composite {",
						"  private Button m_button;",
						"  public GridComposite(Composite parent, int style) {",
						"    super(parent, style);",
						"    setLayout(new GridLayout());",
						"    {",
						"      m_button = new Button(this, SWT.NONE);",
						"      GridData data = new GridData();",
						"      data.widthHint = 100;",
						"      data.heightHint = 50;",
						"      m_button.setLayoutData(data);",
						"    }",
						"  }",
						"  public Button getButton() {",
						"    return m_button;",
						"  }",
						"}"));
		waitForAutoBuild();
	}

	/**
	 * Test that we can replace implicit {@link LayoutDataInfo} with explicit one.
	 */
	@Test
	public void test_implicitReplace_withExplicitData() throws Exception {
		createASTCompilationUnit(
				"test",
				"MyComposite.java",
				getTestSource(
						"public class MyComposite extends Composite {",
						"  private Button m_button;",
						"  public MyComposite(Composite parent, int style) {",
						"    super(parent, style);",
						"    setLayout(new GridLayout(1, false));",
						"    m_button = new Button(this, SWT.NONE);",
						"    m_button.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));",
						"  }",
						"  public Button getButton() {",
						"    return m_button;",
						"  }",
						"}"));
		waitForAutoBuild();
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
		// check GridData
		{
			LayoutDataInfo gridData = LayoutInfo.getLayoutData(button);
			assertInstanceOf(ConstructorCreationSupport.class, gridData.getCreationSupport());
			assertInstanceOf(EmptyVariableSupport.class, gridData.getVariableSupport());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Delete LayoutDataInfo
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test that when we delete "implicit" {@link LayoutDataInfo}, instance of {@link LayoutDataInfo}
	 * is not removed or recreated, so is same as before delete.
	 */
	@Test
	public void test_delete_shouldKeepSameInstance() throws Exception {
		createASTCompilationUnit(
				"test",
				"MyComposite.java",
				getTestSource(
						"public class MyComposite extends Composite {",
						"  private Button m_button;",
						"  public MyComposite(Composite parent, int style) {",
						"    super(parent, style);",
						"    setLayout(new GridLayout(1, false));",
						"    m_button = new Button(this, SWT.NONE);",
						"    m_button.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));",
						"  }",
						"  public Button getButton() {",
						"    return m_button;",
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
		// check initial GridData
		LayoutDataInfo initialData;
		{
			initialData = LayoutInfo.getLayoutData(button);
			assertInstanceOf(ImplicitLayoutDataCreationSupport.class, initialData.getCreationSupport());
			assertInstanceOf(ImplicitLayoutDataVariableSupport.class, initialData.getVariableSupport());
			assertEquals(
					Boolean.TRUE,
					initialData.getPropertyByTitle("grabExcessHorizontalSpace").getValue());
			assertEquals(
					Boolean.FALSE,
					initialData.getPropertyByTitle("grabExcessVerticalSpace").getValue());
		}
		// delete "button", same LayoutData should be kept
		assertTrue(button.canDelete());
		button.delete();
		assertEditor(
				"class Test extends Shell {",
				"  Test() {",
				"    MyComposite myComposite = new MyComposite(this, SWT.NONE);",
				"  }",
				"}");
		{
			LayoutDataInfo newData = LayoutInfo.getLayoutData(button);
			assertSame(initialData, newData);
			assertInstanceOf(ImplicitLayoutDataCreationSupport.class, newData.getCreationSupport());
			assertInstanceOf(ImplicitLayoutDataVariableSupport.class, newData.getVariableSupport());
		}
	}

	/**
	 * Test that when we delete "implicit" {@link LayoutDataInfo}, this deletes any "materialized"
	 * nodes, restores {@link ImplicitLayoutDataVariableSupport}, but keeps same
	 * {@link LayoutDataInfo} instance.
	 */
	@Test
	public void test_delete_removeMaterialized() throws Exception {
		createASTCompilationUnit(
				"test",
				"MyComposite.java",
				getTestSource(
						"public class MyComposite extends Composite {",
						"  private Button m_button;",
						"  public MyComposite(Composite parent, int style) {",
						"    super(parent, style);",
						"    setLayout(new GridLayout(1, false));",
						"    m_button = new Button(this, SWT.NONE);",
						"    m_button.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));",
						"  }",
						"  public Button getButton() {",
						"    return m_button;",
						"  }",
						"}"));
		waitForAutoBuild();
		// parse
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    MyComposite myComposite = new MyComposite(this, SWT.NONE);",
						"    GridData gridData = (GridData) myComposite.getButton().getLayoutData();",
						"    gridData.grabExcessVerticalSpace = true;",
						"  }",
						"}");
		shell.refresh();
		CompositeInfo myComposite = (CompositeInfo) shell.getChildrenControls().get(0);
		ControlInfo button = myComposite.getChildrenControls().get(0);
		// check initial GridData
		LayoutDataInfo initialData;
		{
			initialData = LayoutInfo.getLayoutData(button);
			assertInstanceOf(ImplicitLayoutDataCreationSupport.class, initialData.getCreationSupport());
			assertInstanceOf(LocalUniqueVariableSupport.class, initialData.getVariableSupport());
			assertEquals(
					Boolean.TRUE,
					initialData.getPropertyByTitle("grabExcessHorizontalSpace").getValue());
			assertEquals(
					Boolean.TRUE,
					initialData.getPropertyByTitle("grabExcessVerticalSpace").getValue());
		}
		// delete "button", same LayoutData should be kept
		assertTrue(button.canDelete());
		button.delete();
		assertEditor(
				"class Test extends Shell {",
				"  Test() {",
				"    MyComposite myComposite = new MyComposite(this, SWT.NONE);",
				"  }",
				"}");
		{
			LayoutDataInfo newData = LayoutInfo.getLayoutData(button);
			assertSame(initialData, newData);
			assertInstanceOf(ImplicitLayoutDataCreationSupport.class, newData.getCreationSupport());
			assertInstanceOf(ImplicitLayoutDataVariableSupport.class, newData.getVariableSupport());
			assertEquals(
					Boolean.TRUE,
					initialData.getPropertyByTitle("grabExcessHorizontalSpace").getValue());
			assertEquals(
					Boolean.FALSE,
					initialData.getPropertyByTitle("grabExcessVerticalSpace").getValue());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Problems
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * There was problem that {@link ControlInfo} was initialized before creating implicit
	 * {@link LayoutDataInfo}, so it was not able to get "data" object from {@link Control}.
	 */
	@Test
	public void test_addNewComposite_withExposedControl_andImplicitLayoutData() throws Exception {
		setFileContentSrc(
				"test/MyComposite.java",
				getTestSource(
						"public class MyComposite extends Composite {",
						"  private Button m_button;",
						"  public MyComposite(Composite parent, int style) {",
						"    super(parent, style);",
						"    setLayout(new GridLayout());",
						"    {",
						"      m_button = new Button(this, SWT.NONE);",
						"      m_button.setLayoutData(new GridData());",
						"    }",
						"  }",
						"  public Button getButton() {",
						"    return m_button;",
						"  }",
						"}"));
		waitForAutoBuild();
		// parse
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    setLayout(new FillLayout());",
						"  }",
						"}");
		FillLayoutInfo layout = (FillLayoutInfo) shell.getLayout();
		// create MyComposite
		CompositeInfo myComposite = createJavaInfo("test.MyComposite");
		layout.command_CREATE(myComposite, null);
		assertEditor(
				"class Test extends Shell {",
				"  Test() {",
				"    setLayout(new FillLayout());",
				"    {",
				"      MyComposite myComposite = new MyComposite(this, SWT.NONE);",
				"    }",
				"  }",
				"}");
		assertHierarchy(
				"{this: org.eclipse.swt.widgets.Shell} {this} {/setLayout(new FillLayout())/ /new MyComposite(this, SWT.NONE)/}",
				"  {new: org.eclipse.swt.layout.FillLayout} {empty} {/setLayout(new FillLayout())/}",
				"  {new: test.MyComposite} {local-unique: myComposite} {/new MyComposite(this, SWT.NONE)/}",
				"    {implicit-layout: org.eclipse.swt.layout.GridLayout} {implicit-layout} {}",
				"    {method: public org.eclipse.swt.widgets.Button test.MyComposite.getButton()} {property} {}",
				"      {implicit-layout-data: org.eclipse.swt.layout.GridData} {implicit-layout-data} {}");
	}
}