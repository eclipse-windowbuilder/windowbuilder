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
package org.eclipse.wb.tests.designer.rcp.model.property;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.model.property.editor.AcceleratorPropertyEditor;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.swt.SWT;

/**
 * Tests for {@link AcceleratorPropertyEditor}.
 *
 * @author scheglov_ke
 */
public class AcceleratorPropertyEditorTest extends RcpModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		// prepare Control with "accelerator" property
		setFileContentSrc(
				"test/MyControl.java",
				getTestSource(
						"public class MyControl extends Composite {",
						"  public MyControl(Composite parent, int style) {",
						"    super(parent, style);",
						"  }",
						"  public void setAccelerator(int accelerator) {",
						"  }",
						"}"));
		setFileContentSrc(
				"test/MyControl.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <property id='setAccelerator(int)'>",
						"    <editor id='swtAccelerator'/>",
						"  </property>",
						"</component>"));
		waitForAutoBuild();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// getText()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for <code>getText()</code>.
	 */
	public void test_getText_0() throws Exception {
		CompositeInfo composite =
				parseComposite(
						"public class Test extends MyControl {",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"  }",
						"}");
		Property property = composite.getPropertyByTitle("accelerator");
		assertEquals(null, getPropertyText(property));
	}

	/**
	 * Test for <code>getText()</code>.
	 */
	public void test_getText_1() throws Exception {
		CompositeInfo composite =
				parseComposite(
						"public class Test extends MyControl {",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"    setAccelerator(SWT.CTRL | SWT.F2);",
						"  }",
						"}");
		Property property = composite.getPropertyByTitle("accelerator");
		assertEquals("CTRL+F2", getPropertyText(property));
	}

	/**
	 * Test for <code>getText()</code>.
	 */
	public void test_getText_2() throws Exception {
		dontConvertSingleQuotesToDouble();
		CompositeInfo composite =
				parseComposite(
						"public class Test extends MyControl {",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"    setAccelerator(SWT.CTRL | SWT.SHIFT | 'T');",
						"  }",
						"}");
		Property property = composite.getPropertyByTitle("accelerator");
		assertEquals("CTRL+SHIFT+T", getPropertyText(property));
	}

	/**
	 * Test for <code>getText()</code>.
	 */
	public void test_getText_3() throws Exception {
		dontConvertSingleQuotesToDouble();
		CompositeInfo composite =
				parseComposite(
						"public class Test extends MyControl {",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"    setAccelerator(SWT.SHIFT | SWT.CTRL | 'T');",
						"  }",
						"}");
		Property property = composite.getPropertyByTitle("accelerator");
		assertEquals("CTRL+SHIFT+T", getPropertyText(property));
	}

	/**
	 * Test for <code>getText()</code>.
	 */
	public void test_getText_4() throws Exception {
		CompositeInfo composite =
				parseComposite(
						"public class Test extends MyControl {",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"    setAccelerator(SWT.CTRL | SWT.ALT | SWT.SHIFT | SWT.DEL);",
						"  }",
						"}");
		Property property = composite.getPropertyByTitle("accelerator");
		assertEquals("ALT+CTRL+SHIFT+DEL", getPropertyText(property));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// getSource()
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_getSource() throws Exception {
		// only key
		{
			int accelerator = SWT.F1;
			assertEquals("org.eclipse.swt.SWT.F1", getAcceleratorSource(accelerator));
		}
		// modifiers and character
		{
			int accelerator = SWT.CTRL | 'T';
			assertEquals("org.eclipse.swt.SWT.CTRL | 'T'", getAcceleratorSource(accelerator));
		}
		// modifiers and key code
		{
			int accelerator = SWT.CTRL | SWT.F2;
			assertEquals(
					"org.eclipse.swt.SWT.CTRL | org.eclipse.swt.SWT.F2",
					getAcceleratorSource(accelerator));
		}
	}

	private static String getAcceleratorSource(int accelerator) throws Exception {
		return (String) ReflectionUtils.invokeMethod2(
				AcceleratorPropertyEditor.class,
				"getSource",
				int.class,
				accelerator);
	}
}