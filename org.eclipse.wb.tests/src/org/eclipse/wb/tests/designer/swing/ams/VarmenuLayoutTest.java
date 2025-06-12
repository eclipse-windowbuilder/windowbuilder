/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.tests.designer.swing.ams;

import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;

import org.eclipse.draw2d.PositionConstants;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test for <code>VarmenuLayout</code> support.
 *
 * @author scheglov_ke
 */
public class VarmenuLayoutTest extends SwingGefTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@BeforeEach
	public void setUp() throws Exception {
		super.setUp();
		setFileContentSrc(
				"ams/zpointcs/components/VarmenuConstraints.java",
				IOUtils2.readString(getClass().getResourceAsStream("VarmenuConstraints.txt")));
		setFileContentSrc(
				"ams/zpointcs/components/VarmenuLayout.java",
				IOUtils2.readString(getClass().getResourceAsStream("VarmenuLayout.txt")));
		prepareBox();
	}

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
	@Disabled
	@Test
	public void test_CREATE() throws Exception {
		ContainerInfo panel = openContainer("""
				import ams.zpointcs.components.*;
				public class Test extends JPanel {
					public Test() {
						setLayout(new VarmenuLayout());
					}
				}""");
		panel.refresh();
		//
		loadCreationBox();
		canvas.sideMode().create(100, 50);
		canvas.target(panel).in(150, 100).move();
		canvas.click();
		assertEditor("""
				import ams.zpointcs.components.*;
				public class Test extends JPanel {
					public Test() {
						setLayout(new VarmenuLayout());
						{
							Box box = new Box();
							add(box, new VarmenuConstraints(150, 100, 0, 0));
						}
					}
				}""");
	}

	@Disabled
	@Test
	public void test_RESIZE_width() throws Exception {
		ContainerInfo panel = openContainer("""
				import ams.zpointcs.components.*;
				public class Test extends JPanel {
					public Test() {
						setLayout(new VarmenuLayout());
						{
							Box box = new Box();
							add(box, new VarmenuConstraints(150, 100, 0, 0));
						}
					}
				}""");
		panel.refresh();
		ComponentInfo box = panel.getChildrenComponents().get(0);
		// drag to non-default width
		canvas.beginResize(box, PositionConstants.EAST).dragOn(30, 0).endDrag();
		assertEditor("""
				import ams.zpointcs.components.*;
				public class Test extends JPanel {
					public Test() {
						setLayout(new VarmenuLayout());
						{
							Box box = new Box();
							add(box, new VarmenuConstraints(150, 100, 130, 0));
						}
					}
				}""");
		// drag to default width
		canvas.beginResize(box, PositionConstants.EAST).dragOn(-30, 0).endDrag();
		assertEditor("""
				import ams.zpointcs.components.*;
				public class Test extends JPanel {
					public Test() {
						setLayout(new VarmenuLayout());
						{
							Box box = new Box();
							add(box, new VarmenuConstraints(150, 100, 0, 0));
						}
					}
				}""");
	}

	@Disabled
	@Test
	public void test_RESIZE_height() throws Exception {
		ContainerInfo panel = openContainer("""
				import ams.zpointcs.components.*;
				public class Test extends JPanel {
					public Test() {
						setLayout(new VarmenuLayout());
						{
							Box box = new Box();
							add(box, new VarmenuConstraints(150, 100, 0, 0));
						}
					}
				}""");
		panel.refresh();
		ComponentInfo box = panel.getChildrenComponents().get(0);
		// drag to non-default width
		canvas.beginResize(box, PositionConstants.SOUTH).dragOn(0, 50).endDrag();
		assertEditor("""
				import ams.zpointcs.components.*;
				public class Test extends JPanel {
					public Test() {
						setLayout(new VarmenuLayout());
						{
							Box box = new Box();
							add(box, new VarmenuConstraints(150, 100, 0, 100));
						}
					}
				}""");
		// drag to default width
		canvas.beginResize(box, PositionConstants.SOUTH).dragOn(0, -50).endDrag();
		assertEditor("""
				import ams.zpointcs.components.*;
				public class Test extends JPanel {
					public Test() {
						setLayout(new VarmenuLayout());
						{
							Box box = new Box();
							add(box, new VarmenuConstraints(150, 100, 0, 0));
						}
					}
				}""");
	}
}