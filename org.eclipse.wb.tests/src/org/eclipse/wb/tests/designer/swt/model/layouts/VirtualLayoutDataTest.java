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

import org.eclipse.wb.core.model.association.EmptyAssociation;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.ExposedPropertyCreationSupport;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.swt.model.layout.FillLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.LayoutDataInfo;
import org.eclipse.wb.internal.swt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.RowDataInfo;
import org.eclipse.wb.internal.swt.model.layout.RowLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.VirtualLayoutDataCreationSupport;
import org.eclipse.wb.internal.swt.model.layout.VirtualLayoutDataVariableSupport;
import org.eclipse.wb.internal.swt.model.layout.grid.GridDataInfo;
import org.eclipse.wb.internal.swt.model.layout.grid.GridLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.BTestUtils;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.junit.jupiter.api.Test;

/**
 * @author lobas_av
 */
public class VirtualLayoutDataTest extends RcpModelTest {
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
	// Virtual LayoutDataInfo
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Check that {@link ControlInfo} has "virtual" {@link LayoutDataInfo} when there are no "real"
	 * one.
	 */
	@Test
	public void test_virtual_initial() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"    //",
						"    Button button = new Button(this, SWT.NONE);",
						"    button.setText('new button');",
						"  }",
						"}");
		shell.refresh();
		// prepare layout data
		ControlInfo button = shell.getChildrenControls().get(0);
		assertEquals(1, button.getChildrenJava().size());
		LayoutDataInfo dataInfo = (LayoutDataInfo) button.getChildrenJava().get(0);
		// check CreationSupport
		{
			CreationSupport creationSupport = dataInfo.getCreationSupport();
			assertInstanceOf(VirtualLayoutDataCreationSupport.class, creationSupport);
			assertTrue(creationSupport.canDelete());
			assertEquals(
					"virtual-layout_data: org.eclipse.swt.layout.RowData",
					creationSupport.toString());
		}
		// check VariableSupport
		{
			VariableSupport variableSupport = dataInfo.getVariableSupport();
			assertInstanceOf(VirtualLayoutDataVariableSupport.class, variableSupport);
			assertTrue(variableSupport.isDefault());
			assertEquals("(virtual layout data)", variableSupport.getTitle());
			assertEquals("virtual-layout-data", variableSupport.toString());
			// can not be reference because don't have presentation in source
			try {
				variableSupport.getStatementTarget();
				fail();
			} catch (IllegalStateException e) {
			}
		}
		// check association
		assertInstanceOf(EmptyAssociation.class, dataInfo.getAssociation());
		// check that "virtual" LayoutDataInfo has object
		assertNotNull(dataInfo.getObject());
	}

	/**
	 * Test that when we delete "real" {@link LayoutDataInfo}, "virtual" {@link LayoutDataInfo} will
	 * be created.
	 */
	@Test
	public void test_virtual_whenDeleteReal() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"    //",
						"    Button button = new Button(this, SWT.NONE);",
						"    button.setText('new button');",
						"    RowData data = new RowData();",
						"    button.setLayoutData(data);",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button = shell.getChildrenControls().get(0);
		// check real LayoutDataInfo
		LayoutDataInfo dataInfo;
		{
			assertEquals(1, button.getChildrenJava().size());
			dataInfo = (LayoutDataInfo) button.getChildrenJava().get(0);
			//
			assertNotInstanceOf(VirtualLayoutDataCreationSupport.class, dataInfo.getCreationSupport());
			assertNotInstanceOf(VirtualLayoutDataVariableSupport.class, dataInfo.getVariableSupport());
			assertInstanceOf(InvocationChildAssociation.class, dataInfo.getAssociation());
			// delete layout data
			dataInfo.delete();
			assertEditor(
					"public class Test extends Shell {",
					"  public Test() {",
					"    setLayout(new RowLayout());",
					"    //",
					"    Button button = new Button(this, SWT.NONE);",
					"    button.setText('new button');",
					"  }",
					"}");
		}
		// check virtual
		{
			assertEquals(1, button.getChildrenJava().size());
			LayoutDataInfo newDataInfo = (LayoutDataInfo) button.getChildrenJava().get(0);
			//
			assertNotSame(dataInfo, newDataInfo);
			assertInstanceOf(VirtualLayoutDataCreationSupport.class, newDataInfo.getCreationSupport());
			assertInstanceOf(VirtualLayoutDataVariableSupport.class, newDataInfo.getVariableSupport());
			assertInstanceOf(EmptyAssociation.class, newDataInfo.getAssociation());
		}
	}

	/**
	 * Check that when we try to set value of {@link Property}, "virtual" {@link LayoutDataInfo}
	 * becomes "real" one.
	 */
	@Test
	public void test_virtual_materialize() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"    Button button = new Button(this, SWT.NONE);",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button = shell.getChildrenControls().get(0);
		// check "virtual" supports
		assertEquals(1, button.getChildrenJava().size());
		LayoutDataInfo dataInfo = (LayoutDataInfo) button.getChildrenJava().get(0);
		assertInstanceOf(VirtualLayoutDataCreationSupport.class, dataInfo.getCreationSupport());
		assertInstanceOf(VirtualLayoutDataVariableSupport.class, dataInfo.getVariableSupport());
		assertInstanceOf(EmptyAssociation.class, dataInfo.getAssociation());
		// set property value, so "materialize" LayoutDataInfo
		dataInfo.getPropertyByTitle("width").setValue(100);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new RowLayout());",
				"    Button button = new Button(this, SWT.NONE);",
				"    button.setLayoutData(new RowData(100, SWT.DEFAULT));",
				"  }",
				"}");
		assertInstanceOf(ConstructorCreationSupport.class, dataInfo.getCreationSupport());
		assertInstanceOf(EmptyVariableSupport.class, dataInfo.getVariableSupport());
		// delete layout data, so "virtual" should be created
		dataInfo.delete();
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new RowLayout());",
				"    Button button = new Button(this, SWT.NONE);",
				"  }",
				"}");
		// check new "virtual"
		assertEquals(1, button.getChildrenJava().size());
		LayoutDataInfo newDataInfo = (LayoutDataInfo) button.getChildrenJava().get(0);
		assertNotSame(dataInfo, newDataInfo);
		assertInstanceOf(VirtualLayoutDataCreationSupport.class, newDataInfo.getCreationSupport());
		assertInstanceOf(VirtualLayoutDataVariableSupport.class, newDataInfo.getVariableSupport());
		assertInstanceOf(EmptyAssociation.class, newDataInfo.getAssociation());
	}

	/**
	 * Test that when we add new {@link ControlInfo}, "virtual" {@link LayoutDataInfo} for it can be
	 * asked.
	 */
	@Test
	public void test_virtual_whenAdd() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"  }",
						"}");
		shell.refresh();
		RowLayoutInfo layout = (RowLayoutInfo) shell.getLayout();
		// add new button
		ControlInfo button;
		{
			button = BTestUtils.createButton();
			layout.command_CREATE(button, null);
		}
		// check "virtual" supports
		assertEquals(1, button.getChildrenJava().size());
		LayoutDataInfo dataInfo = LayoutInfo.getLayoutData(button);
		assertInstanceOf(VirtualLayoutDataCreationSupport.class, dataInfo.getCreationSupport());
		assertInstanceOf(VirtualLayoutDataVariableSupport.class, dataInfo.getVariableSupport());
		assertInstanceOf(EmptyAssociation.class, dataInfo.getAssociation());
		// check source
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new RowLayout());",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Test that when we move {@link ControlInfo} on some {@link LayoutInfo}, it will have "virtual"
	 * {@link LayoutDataInfo}.
	 */
	@Test
	public void test_virtual_whenMove() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"    //",
						"    {",
						"      Composite composite = new Composite(this, SWT.NONE);",
						"      composite.setLayout(new FillLayout());",
						"      {",
						"        Button button = new Button(composite, SWT.NONE);",
						"        button.setText('New Button');",
						"      }",
						"    }",
						"  }",
						"}");
		shell.refresh();
		//
		RowLayoutInfo layout = (RowLayoutInfo) shell.getLayout();
		// move Label on Shell
		ControlInfo button;
		{
			CompositeInfo composite = (CompositeInfo) shell.getChildrenControls().get(0);
			button = composite.getChildrenControls().get(0);
			layout.command_MOVE(button, null);
		}
		// check "virtual" supports
		assertEquals(1, button.getChildrenJava().size());
		LayoutDataInfo dataInfo = (LayoutDataInfo) button.getChildrenJava().get(0);
		assertInstanceOf(VirtualLayoutDataCreationSupport.class, dataInfo.getCreationSupport());
		assertInstanceOf(VirtualLayoutDataVariableSupport.class, dataInfo.getVariableSupport());
		assertInstanceOf(EmptyAssociation.class, dataInfo.getAssociation());
		// check source
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new RowLayout());",
				"    //",
				"    {",
				"      Composite composite = new Composite(this, SWT.NONE);",
				"      composite.setLayout(new FillLayout());",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setText('New Button');",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Test that when we move {@link ControlInfo} from one {@link LayoutInfo} on {@link LayoutInfo}
	 * that does not support {@link LayoutDataInfo}, "virtual" {@link LayoutDataInfo} is removed.
	 */
	@Test
	public void test_virtual_removeWhenMoveFrom() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"    //",
						"    {",
						"      Composite composite = new Composite(this, SWT.NONE);",
						"      composite.setLayout(new FillLayout());",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('New Button');",
						"    }",
						"  }",
						"}");
		shell.refresh();
		//
		ControlInfo button = shell.getChildrenControls().get(1);
		// check "virtual" supports
		{
			assertEquals(1, button.getChildrenJava().size());
			LayoutDataInfo dataInfo = (LayoutDataInfo) button.getChildrenJava().get(0);
			assertInstanceOf(VirtualLayoutDataCreationSupport.class, dataInfo.getCreationSupport());
			assertInstanceOf(VirtualLayoutDataVariableSupport.class, dataInfo.getVariableSupport());
			assertInstanceOf(EmptyAssociation.class, dataInfo.getAssociation());
		}
		// move "button" on "composite"
		{
			CompositeInfo composite = (CompositeInfo) shell.getChildrenControls().get(0);
			FillLayoutInfo fillLayout = (FillLayoutInfo) composite.getLayout();
			fillLayout.command_MOVE(button, null);
		}
		// no "virtual" LayoutDataInfo expected
		assertTrue(button.getChildrenJava().isEmpty());
		// check source
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new RowLayout());",
				"    //",
				"    {",
				"      Composite composite = new Composite(this, SWT.NONE);",
				"      composite.setLayout(new FillLayout());",
				"      {",
				"        Button button = new Button(composite, SWT.NONE);",
				"        button.setText('New Button');",
				"      }",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Test that when we set new {@link LayoutInfo}, "virtual" {@link LayoutDataInfo} is removed.
	 */
	@Test
	public void test_virtual_whenNewLayout() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"    Button button = new Button(this, SWT.NONE);",
						"    button.setText('new button');",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button = shell.getChildrenControls().get(0);
		// check "virtual" supports
		{
			assertEquals(1, button.getChildrenJava().size());
			LayoutDataInfo dataInfo = (LayoutDataInfo) button.getChildrenJava().get(0);
			assertInstanceOf(VirtualLayoutDataCreationSupport.class, dataInfo.getCreationSupport());
			assertInstanceOf(VirtualLayoutDataVariableSupport.class, dataInfo.getVariableSupport());
			assertInstanceOf(EmptyAssociation.class, dataInfo.getAssociation());
		}
		// new new layout
		{
			LayoutInfo newLayout = BTestUtils.createLayout("org.eclipse.swt.layout.FillLayout");
			shell.setLayout(newLayout);
		}
		// no "virtual" LayoutDataInfo expected
		assertTrue(button.getChildrenJava().isEmpty());
		// check source
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new FillLayout(SWT.HORIZONTAL));",
				"    Button button = new Button(this, SWT.NONE);",
				"    button.setText('new button');",
				"  }",
				"}");
	}

	/**
	 * Test that when we set new {@link LayoutInfo}, "real" {@link LayoutDataInfo} is removed.
	 */
	@Test
	public void test_real_whenNewLayout() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"    Button button = new Button(this, SWT.NONE);",
						"    button.setText('new button');",
						"    RowData data = new RowData();",
						"    data.width = 50;",
						"    data.height = 70;",
						"    button.setLayoutData(data);",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button = shell.getChildrenControls().get(0);
		// check virtual Creation/Variable Support
		{
			assertEquals(1, button.getChildrenJava().size());
			LayoutDataInfo dataInfo = (LayoutDataInfo) button.getChildrenJava().get(0);
			assertNotInstanceOf(VirtualLayoutDataCreationSupport.class, dataInfo.getCreationSupport());
			assertNotInstanceOf(VirtualLayoutDataVariableSupport.class, dataInfo.getVariableSupport());
			assertInstanceOf(InvocationChildAssociation.class, dataInfo.getAssociation());
		}
		// set new layout
		{
			LayoutInfo newLayout = BTestUtils.createLayout("org.eclipse.swt.layout.FillLayout");
			shell.setLayout(newLayout);
		}
		// no "virtual" LayoutDataInfo expected
		assertTrue(button.getChildrenJava().isEmpty());
		// check source
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new FillLayout(SWT.HORIZONTAL));",
				"    Button button = new Button(this, SWT.NONE);",
				"    button.setText('new button');",
				"  }",
				"}");
	}

	/**
	 * Test that when we set new {@link LayoutInfo}, that requires different {@link LayoutDataInfo},
	 * old "virtual" {@link LayoutDataInfo} replaced with new "virtual" {@link LayoutDataInfo}.
	 */
	@Test
	public void test_virtual_whenNewLayout2() throws Exception {
		CompositeInfo composite =
				parseComposite(
						"public class Test extends Composite {",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"    setLayout(new GridLayout());",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('New Button');",
						"    }",
						"  }",
						"}");
		composite.refresh();
		assertInstanceOf(GridLayoutInfo.class, composite.getLayout());
		ControlInfo button = composite.getChildrenControls().get(0);
		// check "virtual" GridData
		{
			assertEquals(1, button.getChildrenJava().size());
			GridDataInfo dataInfo = (GridDataInfo) button.getChildrenJava().get(0);
			assertInstanceOf(VirtualLayoutDataCreationSupport.class, dataInfo.getCreationSupport());
			assertInstanceOf(VirtualLayoutDataVariableSupport.class, dataInfo.getVariableSupport());
			assertInstanceOf(EmptyAssociation.class, dataInfo.getAssociation());
		}
		// set new layout
		{
			LayoutInfo newLayout = BTestUtils.createLayout("org.eclipse.swt.layout.RowLayout");
			composite.setLayout(newLayout);
			assertSame(newLayout, composite.getLayout());
		}
		// check source
		assertEditor(
				"public class Test extends Composite {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    setLayout(new RowLayout(SWT.HORIZONTAL));",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setText('New Button');",
				"    }",
				"  }",
				"}");
		// check "virtual" RowData
		{
			assertEquals(1, button.getChildrenJava().size());
			RowDataInfo dataInfo = (RowDataInfo) button.getChildrenJava().get(0);
			assertInstanceOf(VirtualLayoutDataCreationSupport.class, dataInfo.getCreationSupport());
			assertInstanceOf(VirtualLayoutDataVariableSupport.class, dataInfo.getVariableSupport());
			assertInstanceOf(EmptyAssociation.class, dataInfo.getAssociation());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Implicit LayoutDataInfo
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Check that "implicit" {@link LayoutInfo} can set/restore "virtual" {@link LayoutDataInfo}.
	 */
	@Test
	public void test_implicitLayout_1() throws Exception {
		// create GridComposite with GridLayout
		setFileContentSrc(
				"test/GridComposite.java",
				getTestSource(
						"public class GridComposite extends Composite {",
						"  public GridComposite(Composite parent, int style) {",
						"    super(parent, style);",
						"    setLayout(new GridLayout());",
						"  }",
						"}"));
		waitForAutoBuild();
		// extend GridComposite
		CompositeInfo composite =
				parseComposite(
						"public class Test extends GridComposite {",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"    Button button = new Button(this, SWT.NONE);",
						"    button.setText('New Button');",
						"  }",
						"}");
		composite.refresh();
		ControlInfo button = composite.getChildrenControls().get(0);
		// implicit GridLayout expected
		assertInstanceOf(GridLayoutInfo.class, composite.getLayout());
		// check "virtual" GridData
		{
			GridDataInfo dataInfo = (GridDataInfo) button.getChildrenJava().get(0);
			assertInstanceOf(VirtualLayoutDataCreationSupport.class, dataInfo.getCreationSupport());
			assertInstanceOf(VirtualLayoutDataVariableSupport.class, dataInfo.getVariableSupport());
			assertInstanceOf(EmptyAssociation.class, dataInfo.getAssociation());
		}
		// set new layout: RowLayout
		{
			LayoutInfo newLayout = BTestUtils.createLayout("org.eclipse.swt.layout.RowLayout");
			composite.setLayout(newLayout);
			assertSame(newLayout, composite.getLayout());
			// check source
			assertEditor(
					"public class Test extends GridComposite {",
					"  public Test(Composite parent, int style) {",
					"    super(parent, style);",
					"    setLayout(new RowLayout(SWT.HORIZONTAL));",
					"    Button button = new Button(this, SWT.NONE);",
					"    button.setText('New Button');",
					"  }",
					"}");
			// check "virtual" RowData
			{
				assertEquals(1, button.getChildrenJava().size());
				RowDataInfo dataInfo = (RowDataInfo) button.getChildrenJava().get(0);
				assertInstanceOf(VirtualLayoutDataCreationSupport.class, dataInfo.getCreationSupport());
				assertInstanceOf(VirtualLayoutDataVariableSupport.class, dataInfo.getVariableSupport());
				assertInstanceOf(EmptyAssociation.class, dataInfo.getAssociation());
			}
			// reset to "implicit" layout
			newLayout.delete();
		}
		// we again have "implicit" GridLayout and "virtual" GridData
		{
			assertInstanceOf(GridLayoutInfo.class, composite.getLayout());
			assertEditor(
					"public class Test extends GridComposite {",
					"  public Test(Composite parent, int style) {",
					"    super(parent, style);",
					"    Button button = new Button(this, SWT.NONE);",
					"    button.setText('New Button');",
					"  }",
					"}");
			// check "virtual" GridData
			{
				assertEquals(1, button.getChildrenJava().size());
				GridDataInfo dataInfo = (GridDataInfo) button.getChildrenJava().get(0);
				assertInstanceOf(VirtualLayoutDataCreationSupport.class, dataInfo.getCreationSupport());
				assertInstanceOf(VirtualLayoutDataVariableSupport.class, dataInfo.getVariableSupport());
				assertInstanceOf(EmptyAssociation.class, dataInfo.getAssociation());
			}
		}
	}

	/**
	 * Check that "implicit" {@link LayoutInfo} can set/restore "virtual" {@link LayoutDataInfo}.
	 */
	@Test
	public void test_implicitLayout_2() throws Exception {
		// create GridComposite with GridLayout
		setFileContentSrc(
				"test/GridComposite.java",
				getTestSource(
						"public class GridComposite extends Composite {",
						"  public GridComposite(Composite parent, int style) {",
						"    super(parent, style);",
						"    setLayout(new GridLayout());",
						"  }",
						"}"));
		waitForAutoBuild();
		// extend GridComposite
		CompositeInfo composite =
				parseComposite(
						"public class Test extends GridComposite {",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"    setLayout(new RowLayout(SWT.HORIZONTAL));",
						"    Button button = new Button(this, SWT.NONE);",
						"    button.setText('New Button');",
						"  }",
						"}");
		composite.refresh();
		ControlInfo button = composite.getChildrenControls().get(0);
		// explicit RowLayout expected
		assertInstanceOf(RowLayoutInfo.class, composite.getLayout());
		// check "virtual" RowData
		{
			RowDataInfo dataInfo = (RowDataInfo) button.getChildrenJava().get(0);
			assertInstanceOf(VirtualLayoutDataCreationSupport.class, dataInfo.getCreationSupport());
			assertInstanceOf(VirtualLayoutDataVariableSupport.class, dataInfo.getVariableSupport());
			assertInstanceOf(EmptyAssociation.class, dataInfo.getAssociation());
			// set property, so "materialize"
			dataInfo.setWidth(101);
			// now it is not "virtual"
			assertNotInstanceOf(VirtualLayoutDataCreationSupport.class, dataInfo.getCreationSupport());
			assertNotInstanceOf(VirtualLayoutDataVariableSupport.class, dataInfo.getVariableSupport());
			assertInstanceOf(InvocationChildAssociation.class, dataInfo.getAssociation());
			// delete "real" RowData, at same time "virtual" RowData created
			dataInfo.delete();
		}
		// check new "virtual" RowData
		{
			RowDataInfo dataInfo = (RowDataInfo) button.getChildrenJava().get(0);
			assertInstanceOf(RowDataInfo.class, dataInfo);
			assertInstanceOf(VirtualLayoutDataCreationSupport.class, dataInfo.getCreationSupport());
			assertInstanceOf(VirtualLayoutDataVariableSupport.class, dataInfo.getVariableSupport());
			assertInstanceOf(EmptyAssociation.class, dataInfo.getAssociation());
		}
		// delete explicit RowLayout, so "implicit" GridLayout is active
		{
			composite.getLayout().delete();
			assertInstanceOf(GridLayoutInfo.class, composite.getLayout());
		}
		// check source
		assertEditor(
				"public class Test extends GridComposite {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    Button button = new Button(this, SWT.NONE);",
				"    button.setText('New Button');",
				"  }",
				"}");
		// check that button has "virtual" GridData
		{
			assertEquals(1, button.getChildrenJava().size());
			GridDataInfo dataInfo = (GridDataInfo) button.getChildrenJava().get(0);
			assertInstanceOf(VirtualLayoutDataCreationSupport.class, dataInfo.getCreationSupport());
			assertInstanceOf(VirtualLayoutDataVariableSupport.class, dataInfo.getVariableSupport());
			assertInstanceOf(EmptyAssociation.class, dataInfo.getAssociation());
		}
	}

	/**
	 * Test for exposed "Button" with explicit {@link LayoutDataInfo} set.
	 */
	@Test
	public void test_whenExposedControl_deleteExplicitData_restoreVirtual() throws Exception {
		setFileContentSrc(
				"test/MyComposite.java",
				getTestSource(
						"public class MyComposite extends Composite {",
						"  private Button m_button;",
						"  public MyComposite(Composite parent, int style) {",
						"    super(parent, style);",
						"    setLayout(new GridLayout(1, false));",
						"    m_button = new Button(this, SWT.NONE);",
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
		CompositeInfo myComposite = getJavaInfoByName("myComposite");
		ControlInfo button = getJavaInfoByName("getButton()");
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
				assertInstanceOf(VirtualLayoutDataCreationSupport.class, gridData.getCreationSupport());
				assertInstanceOf(VirtualLayoutDataVariableSupport.class, gridData.getVariableSupport());
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Delete LayoutDataInfo
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test that when we delete "virtual" {@link LayoutDataInfo}, instance of {@link LayoutDataInfo}
	 * is not removed or recreated, so is same as before delete.
	 */
	@Test
	public void test_delete_shouldKeepSameInstance() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"    Button button = new Button(this, SWT.NONE);",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button = shell.getChildrenControls().get(0);
		LayoutDataInfo initialData = LayoutInfo.getLayoutData(button);
		// delete and check that LayoutDataInfo is same
		initialData.delete();
		assertSame(initialData, LayoutInfo.getLayoutData(button));
	}
}