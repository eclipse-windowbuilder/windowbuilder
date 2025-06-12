/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.tests.designer.rcp.model.widgets;

import org.eclipse.wb.internal.core.model.nonvisual.NonVisualBeanContainerInfo;
import org.eclipse.wb.internal.rcp.model.widgets.TrayItemInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.swt.widgets.TrayItem;

import org.junit.jupiter.api.Test;

/**
 * Test {@link TrayItemInfo}.
 *
 * @author scheglov_ke
 */
public class TrayItemTest extends RcpModelTest {
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
	public void test_disposeWithHierarchy() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  /**",
						"  * @wbp.nonvisual location=150,400",
						"  */",
						"  private final TrayItem trayItem = new TrayItem(Display.getDefault().getSystemTray(), SWT.NONE);",
						"  public Test() {",
						"  }",
						"}");
		shell.refresh();
		assertHierarchy(
				"{this: org.eclipse.swt.widgets.Shell} {this} {}",
				"  {implicit-layout: absolute} {implicit-layout} {}",
				"  {NonVisualBeans}",
				"    {new: org.eclipse.swt.widgets.TrayItem} {field-initializer: trayItem} {/new TrayItem(Display.getDefault().getSystemTray(), SWT.NONE)/}");
		// prepare TrayItem
		TrayItemInfo item = getTrayItem(shell);
		TrayItem itemObject = item.getWidget();
		// "live" now
		assertFalse(itemObject.isDisposed());
		// disposed with model
		disposeLastModel();
		assertTrue(itemObject.isDisposed());
	}

	@Test
	public void test_targetForProperty() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Composite {",
						"  /**",
						"  * @wbp.nonvisual location=150,400",
						"  */",
						"  private final TrayItem trayItem = new TrayItem(Display.getDefault().getSystemTray(), SWT.NONE);",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"  }",
						"}");
		shell.refresh();
		TrayItemInfo item = getTrayItem(shell);
		//
		item.getPropertyByTitle("text").setValue("abc");
		assertEditor(
				"public class Test extends Composite {",
				"  /**",
				"  * @wbp.nonvisual location=150,400",
				"  */",
				"  private final TrayItem trayItem = new TrayItem(Display.getDefault().getSystemTray(), SWT.NONE);",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    trayItem.setText('abc');",
				"  }",
				"}");
	}

	private static TrayItemInfo getTrayItem(CompositeInfo shell) {
		return NonVisualBeanContainerInfo.find(shell).getChildren(TrayItemInfo.class).get(0);
	}
}