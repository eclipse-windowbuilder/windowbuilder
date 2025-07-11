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
package org.eclipse.wb.tests.designer.rcp.swing2swt;

import org.eclipse.wb.internal.core.model.generic.FlowContainerFactory;
import org.eclipse.wb.internal.rcp.swing2swt.layout.BoxLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.swing.BoxLayout;

/**
 * Test {@link BoxLayoutInfo}.
 *
 * @author scheglov_ke
 */
public class BoxLayoutTest extends AbstractSwing2SwtTest {
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
	public void test_parse() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"import swing2swt.layout.BoxLayout;",
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new BoxLayout(BoxLayout.X_AXIS));",
						"  }",
						"}");
		shell.refresh();
		assertHierarchy(
				"{this: org.eclipse.swt.widgets.Shell} {this} {/setLayout(new BoxLayout(BoxLayout.X_AXIS))/}",
				"  {new: swing2swt.layout.BoxLayout} {empty} {/setLayout(new BoxLayout(BoxLayout.X_AXIS))/}");
		BoxLayoutInfo layout = (BoxLayoutInfo) shell.getLayout();
		// BoxLayout is "flow container"
		Assertions.assertThat(new FlowContainerFactory(layout, true).get()).isNotEmpty();
		Assertions.assertThat(new FlowContainerFactory(layout, false).get()).isNotEmpty();
	}

	/**
	 * Test for "axis" property and {@link BoxLayoutInfo#isHorizontal()}.
	 */
	@Test
	public void test_axis() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"import swing2swt.layout.BoxLayout;",
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new BoxLayout(BoxLayout.X_AXIS));",
						"  }",
						"}");
		shell.refresh();
		BoxLayoutInfo layout = (BoxLayoutInfo) shell.getLayout();
		// X_AXIS, so horizontal
		assertTrue(layout.isHorizontal());
		// set Y_AXIS
		layout.getPropertyByTitle("axis").setValue(BoxLayout.Y_AXIS);
		assertEditor(
				"import swing2swt.layout.BoxLayout;",
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new BoxLayout(BoxLayout.Y_AXIS));",
				"  }",
				"}");
		// Y_AXIS, so vertical
		assertFalse(layout.isHorizontal());
	}
}