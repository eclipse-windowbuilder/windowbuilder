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
package org.eclipse.wb.tests.designer.swing.model.component;

import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.junit.Test;

import java.util.Dictionary;

import javax.swing.JSlider;

/**
 * Tests for {@link JSlider} support.
 *
 * @author scheglov_ke
 */
public class JSliderTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * {@link JSlider#setLabelTable(java.util.Dictionary)} requires filled {@link Dictionary}, but
	 * right now we don't evaluate invocations of object. So, we should ignore this method invocation,
	 * or at least don't crash.
	 */
	@Test
	public void test_36471() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JSlider slider = new JSlider();",
						"    slider.setLabelTable(new java.util.Hashtable());",
						"    slider.setPaintLabels(true);",
						"    add(slider);",
						"  }",
						"}");
		panel.refresh();
	}
}
