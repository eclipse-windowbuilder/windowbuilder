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
package org.eclipse.wb.tests.designer.databinding.rcp;

import org.eclipse.wb.internal.rcp.databinding.JFaceDatabindingsFactory;
import org.eclipse.wb.internal.swing.databinding.model.DataBindingsCodeUtils;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.junit.jupiter.api.Test;

/**
 * @author lobas_av
 */
public class JFaceDatabindingsFactoryTestSwing extends SwingModelTest {
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
	public void test_createProvider() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JList list = new JList();",
						"    add(list);",
						"  }",
						"}");
		assertNotNull(panel);
		//
		JFaceDatabindingsFactory factory = new JFaceDatabindingsFactory();
		assertNull(factory.createProvider(panel.getRootJava()));
		if (DataBindingsCodeUtils.getExtrasBundle() == null) {
			assertNull(panel.getPropertyByTitle("bindings"));
		}
	}
}