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
package org.eclipse.wb.tests.designer.swt.model.property;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.order.TabOrderInfo;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipProvider;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipTextProvider;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.model.property.TabOrderProperty;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.junit.Test;

import java.util.List;

/**
 * Test for {@link TabOrderProperty}.
 *
 * @author lobas_av
 */
public class TabOrderPropertyTest extends RcpModelTest {
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
	public void test_common() throws Exception {
		// create shell
		CompositeInfo composite =
				parseComposite(
						"public class Test extends Composite {",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"  }",
						"}");
		composite.refresh();
		// property
		TabOrderProperty property = (TabOrderProperty) composite.getPropertyByTitle("tab order");
		assertNotNull(property);
		assertFalse(property.isModified());
		// tooltip
		assertNull(property.getAdapter(Object.class));
		PropertyTooltipProvider tooltipProvider = property.getAdapter(PropertyTooltipProvider.class);
		assertInstanceOf(PropertyTooltipTextProvider.class, tooltipProvider);
		assertNotNull(ReflectionUtils.invokeMethod(
				tooltipProvider,
				"getText(org.eclipse.wb.internal.core.model.property.Property)",
				property));
	}

	@Test
	public void test_getValue_noValue() throws Exception {
		// create shell
		CompositeInfo composite =
				parseComposite(
						"public class Test extends Composite {",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"    setLayout(new GridLayout(2, false));",
						"    new Label(this, SWT.NONE);",
						"    {",
						"      Button button_1 = new Button(this, SWT.NONE);",
						"      button_1.setText('New Button');",
						"    }",
						"    new Label(this, SWT.NONE);",
						"    new Label(this, SWT.NONE);",
						"    new Label(this, SWT.NONE);",
						"    {",
						"      Combo combo_1 = new Combo(this, SWT.NONE);",
						"    }",
						"  }",
						"}");
		composite.refresh();
		// property
		TabOrderProperty property = (TabOrderProperty) composite.getPropertyByTitle("tab order");
		TabOrderInfo info = (TabOrderInfo) property.getValue();
		//
		List<ControlInfo> controls = composite.getChildrenControls();
		ControlInfo button = controls.get(1);
		ControlInfo combo = controls.get(5);
		//
		assertEquals(2, info.getInfos().size());
		assertSame(button, info.getInfos().get(0));
		assertSame(combo, info.getInfos().get(1));
		//
		assertEquals(2, info.getOrderedInfos().size());
		assertSame(button, info.getOrderedInfos().get(0));
		assertSame(combo, info.getOrderedInfos().get(1));
	}

	@Test
	public void test_getValue() throws Exception {
		// create shell
		CompositeInfo composite =
				parseComposite(
						"public class Test extends Composite {",
						"  private Button button_1;",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"    setLayout(new GridLayout(2, false));",
						"    new Label(this, SWT.NONE);",
						"    {",
						"      button_1 = new Button(this, SWT.NONE);",
						"      button_1.setText('New Button');",
						"    }",
						"    new Label(this, SWT.NONE);",
						"    new Label(this, SWT.NONE);",
						"    new Label(this, SWT.NONE);",
						"    {",
						"      Combo combo_1 = new Combo(this, SWT.NONE);",
						"    }",
						"    setTabList(new Control[]{button_1});",
						"  }",
						"}");
		composite.refresh();
		// property
		TabOrderProperty property = (TabOrderProperty) composite.getPropertyByTitle("tab order");
		TabOrderInfo info = (TabOrderInfo) property.getValue();
		//
		List<ControlInfo> controls = composite.getChildrenControls();
		ControlInfo button = controls.get(1);
		ControlInfo combo = controls.get(5);
		//
		assertEquals(2, info.getInfos().size());
		assertSame(button, info.getInfos().get(0));
		assertSame(combo, info.getInfos().get(1));
		//
		assertEquals(1, info.getOrderedInfos().size());
		assertSame(button, info.getOrderedInfos().get(0));
	}

	@Test
	public void test_setValue_UNKNOWN_VALUE() throws Exception {
		test_setValue(new String[]{
				"public class Test extends Composite {",
				"  private Button button_1;",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    setLayout(new GridLayout(2, false));",
				"    {",
				"      button_1 = new Button(this, SWT.NONE);",
				"      button_1.setText(\"New Button\");",
				"    }",
				"    {",
				"      Combo combo_1 = new Combo(this, SWT.NONE);",
				"    }",
				"    setTabList(new Control[]{button_1});",
				"  }",
		"}"}, Property.UNKNOWN_VALUE, new String[]{
				"public class Test extends Composite {",
				"  private Button button_1;",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    setLayout(new GridLayout(2, false));",
				"    {",
				"      button_1 = new Button(this, SWT.NONE);",
				"      button_1.setText(\"New Button\");",
				"    }",
				"    {",
				"      Combo combo_1 = new Combo(this, SWT.NONE);",
				"    }",
				"  }",
		"}"});
	}

	@Test
	public void test_setValue_noValue() throws Exception {
		test_setValue(new String[]{
				"public class Test extends Composite {",
				"  private Button button_1;",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    setLayout(new GridLayout(2, false));",
				"    {",
				"      button_1 = new Button(this, SWT.NONE);",
				"      button_1.setText(\"New Button\");",
				"    }",
				"    {",
				"      Combo combo_1 = new Combo(this, SWT.NONE);",
				"    }",
				"    setTabList(new Control[]{button_1});",
				"  }",
		"}"}, new TabOrderInfo(), new String[]{
				"public class Test extends Composite {",
				"  private Button button_1;",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    setLayout(new GridLayout(2, false));",
				"    {",
				"      button_1 = new Button(this, SWT.NONE);",
				"      button_1.setText(\"New Button\");",
				"    }",
				"    {",
				"      Combo combo_1 = new Combo(this, SWT.NONE);",
				"    }",
				"  }",
		"}"});
	}

	private void test_setValue(String[] startSource, Object value, String[] newSource)
			throws Exception {
		// create shell
		CompositeInfo composite = parseComposite(startSource);
		composite.refresh();
		// property
		TabOrderProperty property = (TabOrderProperty) composite.getPropertyByTitle("tab order");
		property.setValue(value);
		// check source
		assertEditor(newSource);
	}

	@Test
	public void test_setValue() throws Exception {
		// create shell
		CompositeInfo composite =
				parseComposite(
						"public class Test extends Composite {",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"    setLayout(new FillLayout());",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"    }",
						"    {",
						"      Combo combo = new Combo(this, SWT.NONE);",
						"    }",
						"  }",
						"}");
		composite.refresh();
		// property
		TabOrderProperty property = (TabOrderProperty) composite.getPropertyByTitle("tab order");
		// include "combo"
		{
			TabOrderInfo newValue = new TabOrderInfo();
			newValue.addOrderedInfo(composite.getChildrenControls().get(1));
			property.setValue(newValue);
		}
		// check source
		assertEditor(
				"public class Test extends Composite {",
				"  private Combo combo;",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    setLayout(new FillLayout());",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"    }",
				"    {",
				"      combo = new Combo(this, SWT.NONE);",
				"    }",
				"    setTabList(new Control[]{combo});",
				"  }",
				"}");
		// add new control
		ControlInfo newControl = createJavaInfo("org.eclipse.swt.widgets.Label");
		composite.getLayout().command_CREATE(newControl, null);
		// check source
		assertEditor(
				"public class Test extends Composite {",
				"  private Combo combo;",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    setLayout(new FillLayout());",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"    }",
				"    {",
				"      combo = new Combo(this, SWT.NONE);",
				"    }",
				"    {",
				"      Label label = new Label(this, SWT.NONE);",
				"      label.setText('New Label');",
				"    }",
				"    setTabList(new Control[]{combo});",
				"  }",
				"}");
	}

	@Test
	public void test_delete() throws Exception {
		// create shell
		CompositeInfo composite =
				parseComposite(
						"public class Test extends Composite {",
						"  private Button button;",
						"  private Combo combo;",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"    setLayout(new FillLayout());",
						"    {",
						"      button = new Button(this, SWT.NONE);",
						"    }",
						"    {",
						"      combo = new Combo(this, SWT.NONE);",
						"    }",
						"    {",
						"      Combo combo_2 = new Combo(this, SWT.NONE);",
						"    }",
						"    setTabList(new Control[]{button, combo});",
						"  }",
						"}");
		composite.refresh();
		// property
		TabOrderProperty property = (TabOrderProperty) composite.getPropertyByTitle("tab order");
		TabOrderInfo info = (TabOrderInfo) property.getValue();
		//
		List<ControlInfo> components = composite.getChildrenControls();
		ControlInfo button = components.get(0);
		ControlInfo combo = components.get(1);
		ControlInfo combo2 = components.get(2);
		//
		assertEquals(3, info.getInfos().size());
		assertSame(button, info.getInfos().get(0));
		assertSame(combo, info.getInfos().get(1));
		assertSame(combo2, info.getInfos().get(2));
		//
		assertEquals(2, info.getOrderedInfos().size());
		assertSame(button, info.getOrderedInfos().get(0));
		assertSame(combo, info.getOrderedInfos().get(1));
		//
		combo2.delete();
		assertEditor(
				"public class Test extends Composite {",
				"  private Button button;",
				"  private Combo combo;",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    setLayout(new FillLayout());",
				"    {",
				"      button = new Button(this, SWT.NONE);",
				"    }",
				"    {",
				"      combo = new Combo(this, SWT.NONE);",
				"    }",
				"    setTabList(new Control[]{button, combo});",
				"  }",
				"}");
		//
		combo.delete();
		assertEditor(
				"public class Test extends Composite {",
				"  private Button button;",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    setLayout(new FillLayout());",
				"    {",
				"      button = new Button(this, SWT.NONE);",
				"    }",
				"    setTabList(new Control[]{button});",
				"  }",
				"}");
		//
		button.delete();
		assertEditor(
				"public class Test extends Composite {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    setLayout(new FillLayout());",
				"  }",
				"}");
	}

	@Test
	public void test_delete_2() throws Exception {
		CompositeInfo composite =
				parseComposite(
						"public class Test extends Composite {",
						"  private Button button_1;",
						"  private Combo combo;",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"    setLayout(new FillLayout());",
						"    {",
						"      button_1 = new Button(this, SWT.NONE);",
						"    }",
						"    {",
						"      combo = new Combo(this, SWT.NONE);",
						"    }",
						"    {",
						"      Label label = new Label(this, SWT.NONE);",
						"    }",
						"  }",
						"}");
		composite.refresh();
		// property
		TabOrderProperty property = (TabOrderProperty) composite.getPropertyByTitle("tab order");
		TabOrderInfo info = (TabOrderInfo) property.getValue();
		//
		List<ControlInfo> components = composite.getChildrenControls();
		ControlInfo button = components.get(0);
		ControlInfo combo = components.get(1);
		ControlInfo label = components.get(2);
		//
		assertEquals(3, info.getInfos().size());
		assertSame(button, info.getInfos().get(0));
		assertSame(combo, info.getInfos().get(1));
		assertSame(label, info.getInfos().get(2));
		//
		assertEquals(2, info.getOrderedInfos().size());
		assertSame(button, info.getOrderedInfos().get(0));
		assertSame(combo, info.getOrderedInfos().get(1));
		//
		label.delete();
		assertEditor(
				"public class Test extends Composite {",
				"  private Button button_1;",
				"  private Combo combo;",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    setLayout(new FillLayout());",
				"    {",
				"      button_1 = new Button(this, SWT.NONE);",
				"    }",
				"    {",
				"      combo = new Combo(this, SWT.NONE);",
				"    }",
				"  }",
				"}");
		//
		combo.delete();
		assertEditor(
				"public class Test extends Composite {",
				"  private Button button_1;",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    setLayout(new FillLayout());",
				"    {",
				"      button_1 = new Button(this, SWT.NONE);",
				"    }",
				"  }",
				"}");
	}
}