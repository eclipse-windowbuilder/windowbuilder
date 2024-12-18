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
import org.eclipse.wb.core.model.association.ConstructorAssociation;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.model.layout.FillLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.LayoutDataInfo;
import org.eclipse.wb.internal.swt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.BTestUtils;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.swt.SWT;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.Test;

/**
 * Test for {@link FillLayoutInfo}.
 *
 * @author lobas_av
 */
public class FillLayoutTest extends RcpModelTest {
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
	public void test_isHorizontal_1() throws Exception {
		test_isHorizontal(new String[]{
				"class Test {",
				"  public static void main(String[] args) {",
				"    Shell shell = new Shell();",
				"    shell.setLayout(new FillLayout(SWT.HORIZONTAL));",
				"  }",
		"}"});
	}

	@Test
	public void test_isHorizontal_2() throws Exception {
		test_isHorizontal(new String[]{
				"class Test {",
				"  public static void main(String[] args) {",
				"    Shell shell = new Shell();",
				"    shell.setLayout(new FillLayout());",
				"  }",
		"}"});
	}

	private void test_isHorizontal(String[] lines) throws Exception {
		CompositeInfo shellInfo = parseComposite(lines);
		// check layout
		FillLayoutInfo layoutInfo = (FillLayoutInfo) shellInfo.getLayout();
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

	/**
	 * Test that {@link LayoutInfo#getDefaultVirtualDataObject()} throws
	 * {@link NotImplementedException} if {@link LayoutDataInfo} is not supported by this
	 * {@link LayoutInfo}.
	 */
	@Test
	public void test_NoVirtualLayoutData() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FillLayout());",
						"  }",
						"}");
		assertTrue(shell.getChildrenControls().isEmpty());
		FillLayoutInfo layout = (FillLayoutInfo) shell.getLayout();
		//
		try {
			ReflectionUtils.invokeMethod(layout, "getDefaultVirtualDataObject()");
			fail();
		} catch (NotImplementedException e) {
		}
	}

	@Test
	public void test_AddControls() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test {",
						"  public static void main(String[] args) {",
						"    Shell shell = new Shell();",
						"    shell.setLayout(new FillLayout());",
						"  }",
						"}");
		assertTrue(shell.getChildrenControls().isEmpty());
		FillLayoutInfo layout = (FillLayoutInfo) shell.getLayout();
		// add "Button" to end
		ControlInfo button = BTestUtils.createButton();
		layout.command_CREATE(button, null);
		{
			ConstructorAssociation association = (ConstructorAssociation) button.getAssociation();
			assertNotNull(association);
			assertSame(
					((ConstructorCreationSupport) button.getCreationSupport()).getCreation(),
					association.getCreation());
		}
		assertEquals(1, shell.getChildrenControls().size());
		assertSame(button, shell.getChildrenControls().get(0));
		assertEditor(
				"class Test {",
				"  public static void main(String[] args) {",
				"    Shell shell = new Shell();",
				"    shell.setLayout(new FillLayout());",
				"    {",
				"      Button button = new Button(shell, SWT.NONE);",
				"    }",
				"  }",
				"}");
		// add "Label" before "Button"
		ControlInfo label = BTestUtils.createControl("org.eclipse.swt.widgets.Label");
		layout.command_CREATE(label, button);
		assertNotNull(label.getAssociation());
		assertEquals(2, shell.getChildrenControls().size());
		assertSame(label, shell.getChildrenControls().get(0));
		assertSame(button, shell.getChildrenControls().get(1));
		assertEditor(
				"class Test {",
				"  public static void main(String[] args) {",
				"    Shell shell = new Shell();",
				"    shell.setLayout(new FillLayout());",
				"    {",
				"      Label label = new Label(shell, SWT.NONE);",
				"      label.setText('New Label');",
				"    }",
				"    {",
				"      Button button = new Button(shell, SWT.NONE);",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_MoveControls() throws Exception {
		CompositeInfo shellInfo =
				parseComposite(
						"class Test {",
						"  public static void main(String[] args) {",
						"    Shell shell = new Shell();",
						"    shell.setLayout(new FillLayout());",
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
		FillLayoutInfo layout = (FillLayoutInfo) shellInfo.getLayout();
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
				"    shell.setLayout(new FillLayout());",
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
				"    shell.setLayout(new FillLayout());",
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
						"    shell.setLayout(new FillLayout());",
						"    {",
						"      Label label = new Label(shell, SWT.NONE);",
						"    }",
						"    {",
						"      Button button = new Button(shell, SWT.NONE);",
						"    }",
						"    {",
						"      Composite composite = new Composite(shell, SWT.NONE);",
						"      composite.setLayout(new FillLayout());",
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
		FillLayoutInfo layoutInfo = (FillLayoutInfo) compositeInfo.getLayout();
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
				"    shell.setLayout(new FillLayout());",
				"    {",
				"      Label label = new Label(shell, SWT.NONE);",
				"    }",
				"    {",
				"      Composite composite = new Composite(shell, SWT.NONE);",
				"      composite.setLayout(new FillLayout());",
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
				"    shell.setLayout(new FillLayout());",
				"    {",
				"      Composite composite = new Composite(shell, SWT.NONE);",
				"      composite.setLayout(new FillLayout());",
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
}