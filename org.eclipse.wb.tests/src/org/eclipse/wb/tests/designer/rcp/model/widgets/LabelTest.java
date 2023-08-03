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

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.swt.model.widgets.ButtonStylePresentation;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.model.widgets.LabelInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.swt.widgets.Label;

import org.junit.Test;

/**
 * Test for {@link Label}.
 *
 * @author scheglov_ke
 */
public class LabelTest extends RcpModelTest {
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
	public void test_setText() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    Label label = new Label(this, SWT.NONE);",
						"    label.setText('New label');",
						"  }",
						"}");
		shell.refresh();
		ControlInfo label = shell.getChildrenControls().get(0);
		// set "text" property
		Property textProperty = label.getPropertyByTitle("text");
		textProperty.setValue("New text");
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    Label label = new Label(this, SWT.NONE);",
				"    label.setText('New text');",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ButtonStylePresentation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test that {@link ButtonStylePresentation} returns different icons for buttons with different
	 * styles.
	 */
	@Test
	public void test_ButtonStylePresentation() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    new Label(this, SWT.NONE);",
						"    new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);",
						"    new Label(this, SWT.SEPARATOR | SWT.VERTICAL);",
						"    new Label(this, SWT.SEPARATOR);",
						"  }",
						"}");
		shell.refresh();
		// prepare Label's
		LabelInfo labelDefault = (LabelInfo) shell.getChildrenControls().get(0);
		LabelInfo labelSeparatorHorizontal = (LabelInfo) shell.getChildrenControls().get(1);
		LabelInfo labelSeparatorVertical = (LabelInfo) shell.getChildrenControls().get(2);
		LabelInfo labelSeparatorVerticalDef = (LabelInfo) shell.getChildrenControls().get(3);
		// check icons
		assertNotSame(
				labelDefault.getPresentation().getIcon(),
				labelSeparatorHorizontal.getPresentation().getIcon());
		assertNotSame(
				labelSeparatorHorizontal.getPresentation().getIcon(),
				labelSeparatorVertical.getPresentation().getIcon());
		assertSame(
				labelSeparatorVertical.getPresentation().getIcon(),
				labelSeparatorVerticalDef.getPresentation().getIcon());
	}
}