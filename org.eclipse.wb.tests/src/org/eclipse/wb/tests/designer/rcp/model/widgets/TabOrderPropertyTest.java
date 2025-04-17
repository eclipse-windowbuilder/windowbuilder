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
package org.eclipse.wb.tests.designer.rcp.model.widgets;

import org.eclipse.wb.internal.core.model.property.order.TabOrderInfo;
import org.eclipse.wb.internal.swt.model.property.TabOrderProperty;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.junit.Test;

/**
 * Test for {@link TabOrderProperty}, RCP variant.
 *
 * @author lobas_av
 * @author scheglov_ke
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
						"  }",
						"}");
		composite.refresh();
		// property
		TabOrderProperty property = (TabOrderProperty) composite.getPropertyByTitle("tab order");
		// include "button"
		{
			TabOrderInfo newValue = new TabOrderInfo();
			newValue.addOrderedInfo(composite.getChildrenControls().get(0));
			property.setValue(newValue);
		}
		// check source
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
	}

	@Test
	public void test_wrongCode() throws Exception {
		m_ignoreCompilationProblems = true;
		// create shell
		CompositeInfo composite =
				parseComposite(
						"public class Test extends Composite {",
						"  private Button button;",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"    setLayout(new FillLayout());",
						"    {",
						"      button = new Button(this, SWT.NONE);",
						"    }",
						"    setTabList(new Control[]{button, label});",
						"  }",
						"}");
		composite.refresh();
		// property
		TabOrderProperty property = (TabOrderProperty) composite.getPropertyByTitle("tab order");
		assertEquals("[button]", property.getDisplayText());
		//
		property.setValue(property.getValue());
		// check source
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
	}
}