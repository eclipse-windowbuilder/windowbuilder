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
package org.eclipse.wb.tests.designer.core.model.util;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.ObjectInfoUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.junit.jupiter.api.Test;

public class ObjectInfoUtilsTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// ID
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link ObjectInfoUtils#getId(ObjectInfo)}.
	 */
	@Test
	public void test_getId() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		assertNotNull(ObjectInfoUtils.getId(panel));
	}

	/**
	 * Test for {@link ObjectInfoUtils#getById(String)}.
	 */
	@Test
	public void test_getById() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    add(new JButton());",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// ask using "panel"
		assertSame(panel, ObjectInfoUtils.getById(ObjectInfoUtils.getId(panel)));
		assertSame(button, ObjectInfoUtils.getById(ObjectInfoUtils.getId(button)));
		// ask using "button"
		assertSame(panel, ObjectInfoUtils.getById(ObjectInfoUtils.getId(panel)));
		assertSame(button, ObjectInfoUtils.getById(ObjectInfoUtils.getId(button)));
	}
}
