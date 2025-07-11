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
package org.eclipse.wb.tests.designer.swing.model.component;

import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.component.JInternalFrameInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.draw2d.geometry.Rectangle;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.swing.JInternalFrame;

/**
 * Tests for {@link JInternalFrame} support.
 *
 * @author scheglov_ke
 */
public class JInternalFrameTest extends SwingModelTest {
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
	public void test_this() throws Exception {
		JInternalFrameInfo frame =
				parseJavaInfo(
						"// filler filler filler",
						"public class Test extends JInternalFrame {",
						"  public Test() {",
						"  }",
						"}");
		assertHierarchy(
				"{this: javax.swing.JInternalFrame} {this} {}",
				"  {method: public java.awt.Container javax.swing.JInternalFrame.getContentPane()} {property} {}",
				"    {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}");
		frame.refresh();
		assertNoErrors(frame);
		// check bounds of JInternalFrame and its "contentPane"
		ComponentInfo contentPane = frame.getChildrenComponents().get(0);
		{
			Rectangle bounds = frame.getBounds();
			assertEquals(bounds.width, 450);
			assertEquals(bounds.height, 300);
		}
		{
			Rectangle bounds = contentPane.getBounds();
			Assertions.assertThat(bounds.x).isGreaterThanOrEqualTo(0);
			Assertions.assertThat(bounds.y).isGreaterThanOrEqualTo(0);
			Assertions.assertThat(bounds.width).isGreaterThan(420);
			Assertions.assertThat(bounds.height).isGreaterThan(250);
		}
	}

	@Test
	public void test_onJDesktopPane() throws Exception {
		ContainerInfo panel =
				parseJavaInfo(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JDesktopPane desktop = new JDesktopPane();",
						"    add(desktop);",
						"    {",
						"      JInternalFrame frame = new JInternalFrame();",
						"      desktop.add(frame);",
						"      frame.setBounds(5, 5, 200, 150);",
						"    }",
						"  }",
						"}");
		assertHierarchy(
				"{this: javax.swing.JPanel} {this} {/add(desktop)/}",
				"  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
				"  {new: javax.swing.JDesktopPane} {local-unique: desktop} {/new JDesktopPane()/ /add(desktop)/ /desktop.add(frame)/}",
				"    {implicit-layout: absolute} {implicit-layout} {}",
				"    {new: javax.swing.JInternalFrame} {local-unique: frame} {/new JInternalFrame()/ /desktop.add(frame)/ /frame.setBounds(5, 5, 200, 150)/}",
				"      {method: public java.awt.Container javax.swing.JInternalFrame.getContentPane()} {property} {}",
				"        {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}");
		panel.refresh();
		JInternalFrameInfo frame = getJavaInfoByName("frame");
		// JInternalFrame should be visible
		assertEquals(true, frame.getComponent().isVisible());
	}
}
