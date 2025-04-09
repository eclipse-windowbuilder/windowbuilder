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

import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.generation.GenerationSettings;
import org.eclipse.wb.internal.core.model.generation.statement.flat.FlatStatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.rcp.RcpToolkitDescription;
import org.eclipse.wb.internal.swt.model.layout.LayoutDataInfo;
import org.eclipse.wb.internal.swt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.RowDataInfo;
import org.eclipse.wb.internal.swt.model.layout.RowLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

import org.junit.Test;

/**
 * Tests for {@link LayoutDataInfo}.
 *
 * @author scheglov_ke
 * @author lobas_av
 */
public class LayoutDataTest extends RcpModelTest {
	private final ToolkitDescription m_toolkit = RcpToolkitDescription.INSTANCE;

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
	public void test_deleteLayoutData() throws Exception {
		CompositeInfo shellInfo =
				parseComposite(
						"class Test {",
						"  public static void main(String[] args) {",
						"    Shell shell = new Shell();",
						"    RowLayout layout = new RowLayout();",
						"    shell.setLayout(layout);",
						"    //",
						"    Button button = new Button(shell, SWT.NONE);",
						"    RowData data = new RowData();",
						"    data.width = 50;",
						"    data.height = 40;",
						"    button.setLayoutData(data);",
						"  }",
						"}");
		shellInfo.refresh();
		//
		ControlInfo button = shellInfo.getChildrenControls().get(0);
		Property property = button.getPropertyByTitle("LayoutData");
		//
		property.setValue(Property.UNKNOWN_VALUE);
		assertEditor(
				"class Test {",
				"  public static void main(String[] args) {",
				"    Shell shell = new Shell();",
				"    RowLayout layout = new RowLayout();",
				"    shell.setLayout(layout);",
				"    //",
				"    Button button = new Button(shell, SWT.NONE);",
				"  }",
				"}");
	}

	/**
	 * Test for excluding some unneeded layout data properties, ex. "Constructor" property.
	 */
	@Test
	public void test_excludeProperties() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      RowData data = new RowData(100, 100);",
						"      button.setLayoutData(data);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button = shell.getChildrenControls().get(0);
		Property layoutDataProperty = button.getPropertyByTitle("LayoutData");
		Property[] subProperties = getSubProperties(layoutDataProperty);
		assertNull(getPropertyByTitle(subProperties, "Constructor"));
		assertNull(getPropertyByTitle(subProperties, "Class"));
		assertNotNull(getPropertyByTitle(subProperties, "height"));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Code generation
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_codeGeneration_Block() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button = shell.getChildrenControls().get(0);
		RowDataInfo layoutData = RowLayoutInfo.getRowData(button);
		// set "width" property, so materialize RowData
		layoutData.getPropertyByTitle("width").setValue(100);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new RowLayout());",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setLayoutData(new RowData(100, SWT.DEFAULT));",
				"    }",
				"  }",
				"}");
		// set "exclude" property, so force variable
		layoutData.getPropertyByTitle("exclude").setValue(true);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new RowLayout());",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      {",
				"        RowData rowData = new RowData(100, SWT.DEFAULT);",
				"        rowData.exclude = true;",
				"        button.setLayoutData(rowData);",
				"      }",
				"    }",
				"  }",
				"}");
		// set "exclude" property to default, so no reason to have variable
		layoutData.getPropertyByTitle("exclude").setValue(false);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new RowLayout());",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setLayoutData(new RowData(100, SWT.DEFAULT));",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_codeGeneration_Flat() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"    ",
						"    Button button = new Button(this, SWT.NONE);",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button = shell.getChildrenControls().get(0);
		RowDataInfo layoutData = RowLayoutInfo.getRowData(button);
		//
		GenerationSettings generationSettings =
				shell.getDescription().getToolkit().getGenerationSettings();
		generationSettings.setStatement(FlatStatementGeneratorDescription.INSTANCE);
		try {
			// set "width" property, so materialize RowData
			layoutData.getPropertyByTitle("width").setValue(100);
			assertEditor(
					"public class Test extends Shell {",
					"  public Test() {",
					"    setLayout(new RowLayout());",
					"    ",
					"    Button button = new Button(this, SWT.NONE);",
					"    button.setLayoutData(new RowData(100, SWT.DEFAULT));",
					"  }",
					"}");
			// set "exclude" property, so force variable
			layoutData.getPropertyByTitle("exclude").setValue(true);
			assertEditor(
					"public class Test extends Shell {",
					"  public Test() {",
					"    setLayout(new RowLayout());",
					"    ",
					"    Button button = new Button(this, SWT.NONE);",
					"    RowData rowData = new RowData(100, SWT.DEFAULT);",
					"    rowData.exclude = true;",
					"    button.setLayoutData(rowData);",
					"  }",
					"}");
			// set "exclude" property to default, so no reason to have variable
			layoutData.getPropertyByTitle("exclude").setValue(false);
			assertEditor(
					"public class Test extends Shell {",
					"  public Test() {",
					"    setLayout(new RowLayout());",
					"    ",
					"    Button button = new Button(this, SWT.NONE);",
					"    button.setLayoutData(new RowData(100, SWT.DEFAULT));",
					"  }",
					"}");
		} finally {
			generationSettings.setStatement(generationSettings.getDefaultStatement());
		}
	}

	/**
	 * There was problem that we tried to inline when {@link LayoutDataInfo} was already deleted.
	 */
	@Test
	public void test_codeGeneration_ignoreIfDelete() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      {",
						"        RowData rowData = new RowData(100, SWT.DEFAULT);",
						"        rowData.exclude = false;",
						"        button.setLayoutData(rowData);",
						"      }",
						"    }",
						"  }",
						"}");
		shell.refresh();
		final ControlInfo button = shell.getChildrenControls().get(0);
		// do delete
		button.delete();
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new RowLayout());",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Code generation: name, based on template
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Template "${defaultName}" means that name should be based on name of type.
	 */
	@Test
	public void test_nameTemplate_useDefaultName() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button = getJavaInfoByName("button");
		//
		m_toolkit.getPreferences().setValue(
				org.eclipse.wb.internal.swt.preferences.IPreferenceConstants.P_LAYOUT_DATA_NAME_TEMPLATE,
				org.eclipse.wb.internal.core.model.variable.SyncParentChildVariableNameSupport.TEMPLATE_FOR_DEFAULT);
		RowLayoutInfo.getRowData(button).getPropertyByTitle("exclude").setValue(true);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new RowLayout());",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      {",
				"        RowData rowData = new RowData(SWT.DEFAULT, SWT.DEFAULT);",
				"        rowData.exclude = true;",
				"        button.setLayoutData(rowData);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Generate name using "${dataAcronym}${controlName-cap}" template.
	 */
	@Test
	public void test_nameTemplate_alternativeTemplate_1() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button = getJavaInfoByName("button");
		//
		m_toolkit.getPreferences().setValue(
				org.eclipse.wb.internal.swt.preferences.IPreferenceConstants.P_LAYOUT_DATA_NAME_TEMPLATE,
				"${dataAcronym}${controlName-cap}");
		RowLayoutInfo.getRowData(button).getPropertyByTitle("exclude").setValue(true);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new RowLayout());",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      {",
				"        RowData rdButton = new RowData(SWT.DEFAULT, SWT.DEFAULT);",
				"        rdButton.exclude = true;",
				"        button.setLayoutData(rdButton);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Generate name using "${controlName}${dataClassName}" template.
	 */
	@Test
	public void test_nameTemplate_alternativeTemplate_2() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button = getJavaInfoByName("button");
		//
		m_toolkit.getPreferences().setValue(
				org.eclipse.wb.internal.swt.preferences.IPreferenceConstants.P_LAYOUT_DATA_NAME_TEMPLATE,
				"${controlName}${dataClassName}");
		RowLayoutInfo.getRowData(button).getPropertyByTitle("exclude").setValue(true);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new RowLayout());",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      {",
				"        RowData buttonRowData = new RowData(SWT.DEFAULT, SWT.DEFAULT);",
				"        buttonRowData.exclude = true;",
				"        button.setLayoutData(buttonRowData);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	/**
	 * {@link LayoutDataInfo} should be renamed when its parent {@link ControlInfo} is renamed.
	 */
	@Test
	public void test_nameTemplate_renameWithControl() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      {",
						"        RowData rowData = new RowData(100, SWT.DEFAULT);",
						"        rowData.exclude = false;",
						"        button.setLayoutData(rowData);",
						"      }",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button = shell.getChildrenControls().get(0);
		//
		button.getVariableSupport().setName("myButton");
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new RowLayout());",
				"    {",
				"      Button myButton = new Button(this, SWT.NONE);",
				"      {",
				"        RowData rowData = new RowData(100, SWT.DEFAULT);",
				"        rowData.exclude = false;",
				"        myButton.setLayoutData(rowData);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Delete if default
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_deleteIfDefault() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setLayoutData(new RowData(SWT.DEFAULT, SWT.DEFAULT));",
						"    }",
						"  }",
						"}");
		shell.refresh();
		// perform edit operation, see that no reason to have RowData
		ExecutionUtils.refresh(shell);
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

	@Test
	public void test_deleteIfDefault_negativeValue() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setLayoutData(new RowData(-1, SWT.DEFAULT));",
						"    }",
						"  }",
						"}");
		shell.refresh();
		// perform edit operation, see that no reason to have RowData
		ExecutionUtils.refresh(shell);
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

	@Test
	public void test_deleteIfDefault_notLiterals() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      int width = SWT.DEFAULT;",
						"      button.setLayoutData(new RowData(width, SWT.DEFAULT));",
						"    }",
						"  }",
						"}");
		shell.refresh();
		// perform edit operation, variable in constructor - keep LayoutData
		ExecutionUtils.refresh(shell);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new RowLayout());",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      int width = SWT.DEFAULT;",
				"      button.setLayoutData(new RowData(width, SWT.DEFAULT));",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_deleteIfDefault_hasVariable() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      {",
						"        RowData rowData = new RowData(SWT.DEFAULT, SWT.DEFAULT);",
						"        button.setLayoutData(rowData);",
						"      }",
						"    }",
						"  }",
						"}");
		shell.refresh();
		// perform edit operation, see that no reason to have RowData
		ExecutionUtils.refresh(shell);
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

	@Test
	public void test_deleteIfDefault_hasMethodInvocation() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      {",
						"        RowData rowData = new RowData(SWT.DEFAULT, SWT.DEFAULT);",
						"        rowData.hashCode();",
						"        button.setLayoutData(rowData);",
						"      }",
						"    }",
						"  }",
						"}");
		shell.refresh();
		// no changes, because has MethodInvocation
		ExecutionUtils.refresh(shell);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new RowLayout());",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      {",
				"        RowData rowData = new RowData(SWT.DEFAULT, SWT.DEFAULT);",
				"        rowData.hashCode();",
				"        button.setLayoutData(rowData);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_deleteIfDefault_hasFieldAssignment() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      {",
						"        RowData rowData = new RowData(SWT.DEFAULT, SWT.DEFAULT);",
						"        rowData.exclude = true;",
						"        button.setLayoutData(rowData);",
						"      }",
						"    }",
						"  }",
						"}");
		shell.refresh();
		// no changes, because has assignment
		ExecutionUtils.refresh(shell);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new RowLayout());",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      {",
				"        RowData rowData = new RowData(SWT.DEFAULT, SWT.DEFAULT);",
				"        rowData.exclude = true;",
				"        button.setLayoutData(rowData);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Special
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * {@link LayoutDataInfo} may be dangling, not attached to any {@link ControlInfo}.
	 */
	@Test
	public void test_noParentControl() throws Exception {
		CompositeInfo composite =
				parseComposite(
						"class Test extends Shell {",
						"  public Test() {",
						"    RowData dangling = new RowData();",
						"  }",
						"}");
		assertHierarchy(
				"{this: org.eclipse.swt.widgets.Shell} {this} {}",
				"  {implicit-layout: absolute} {implicit-layout} {}");
		//
		composite.refresh();
		assertNoErrors(composite);
	}

	/**
	 * <code>LayoutData</code> may be set for "this" {@link Composite}, that has no parent, so no
	 * enclosing {@link Layout}. This should not cause problems.
	 */
	@Test
	public void test_noParentComposite_noLayout() throws Exception {
		CompositeInfo composite =
				parseComposite(
						"class Test extends Composite {",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"    setLayoutData(new RowData());",
						"  }",
						"}");
		assertHierarchy(
				"{this: org.eclipse.swt.widgets.Composite} {this} {/setLayoutData(new RowData())/}",
				"  {implicit-layout: absolute} {implicit-layout} {}");
		//
		composite.refresh();
		assertNoErrors(composite);
	}

	/**
	 * We should ignore <code>LayoutData</code> if it is not compatible with layout of "parent".
	 */
	@Test
	public void test_hasParentLayout_notCompatible() throws Exception {
		CompositeInfo composite =
				parseComposite(
						"class Test extends Shell {",
						"  public Test() {",
						"    Button button = new Button(this, SWT.NONE);",
						"    button.setLayoutData(new RowData());",
						"  }",
						"}");
		assertHierarchy(
				"{this: org.eclipse.swt.widgets.Shell} {this} {/new Button(this, SWT.NONE)/}",
				"  {implicit-layout: absolute} {implicit-layout} {}",
				"  {new: org.eclipse.swt.widgets.Button} {local-unique: button} {/new Button(this, SWT.NONE)/ /button.setLayoutData(new RowData())/}");
		//
		composite.refresh();
		assertNoErrors(composite);
	}

	/**
	 * <code>LayoutData</code> was set twice, so first {@link LayoutDataInfo} was already removed. But
	 * it was also incompatible, so we tried to remove it second time.
	 */
	@Test
	public void test_hasParentLayout_notCompatible_alreadyRemoved() throws Exception {
		CompositeInfo composite =
				parseComposite(
						"class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new GridLayout());",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setLayoutData(new RowData());",
						"      button.setLayoutData(new GridData());",
						"    }",
						"  }",
						"}");
		assertHierarchy(
				"{this: org.eclipse.swt.widgets.Shell} {this} {/setLayout(new GridLayout())/ /new Button(this, SWT.NONE)/}",
				"  {new: org.eclipse.swt.layout.GridLayout} {empty} {/setLayout(new GridLayout())/}",
				"  {new: org.eclipse.swt.widgets.Button} {local-unique: button} {/new Button(this, SWT.NONE)/ /button.setLayoutData(new RowData())/ /button.setLayoutData(new GridData())/}",
				"    {new: org.eclipse.swt.layout.GridData} {empty} {/button.setLayoutData(new GridData())/}");
		//
		composite.refresh();
		assertNoErrors(composite);
	}

	/**
	 * User may mistakenly try to set {@link LayoutDataInfo} for {@link ControlInfo} on
	 * {@link CompositeInfo} that has no {@link LayoutInfo}. We should remove such invalid
	 * {@link LayoutDataInfo}.
	 */
	@Test
	public void test_hasParentComposite_noLayout() throws Exception {
		setFileContentSrc(
				"test/MyComposite.java",
				getTestSource(
						"public class MyComposite extends Composite {",
						"  public MyComposite(Composite parent, int style) {",
						"    super(parent, style);",
						"    setLayout(new RowLayout());",
						"  }",
						"}"));
		setFileContentSrc(
				"test/MyComposite.wbp-component.xml",
				getSource(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <parameters>",
						"    <parameter name='layout.has'>false</parameter>",
						"  </parameters>",
						"</component>"));
		waitForAutoBuild();
		// parse
		CompositeInfo composite =
				parseComposite(
						"public class Test extends MyComposite {",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"    Button button = new Button(this, SWT.NONE);",
						"    button.setLayoutData(new RowData());",
						"  }",
						"}");
		assertHierarchy(
				"{this: test.MyComposite} {this} {/new Button(this, SWT.NONE)/}",
				"  {new: org.eclipse.swt.widgets.Button} {local-unique: button} {/new Button(this, SWT.NONE)/ /button.setLayoutData(new RowData())/}");
		//
		composite.refresh();
		assertNoErrors(composite);
	}
}