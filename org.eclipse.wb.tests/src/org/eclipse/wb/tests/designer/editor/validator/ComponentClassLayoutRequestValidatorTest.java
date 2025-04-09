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
package org.eclipse.wb.tests.designer.editor.validator;

import org.eclipse.wb.core.gef.policy.validator.ComponentClassLayoutRequestValidator;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.junit.Test;

/**
 * Test {@link ComponentClassLayoutRequestValidator}.
 *
 * @author scheglov_ke
 */
public class ComponentClassLayoutRequestValidatorTest extends AbstractLayoutRequestValidatorTest {
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
	public void test_0() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		ILayoutRequestValidator validator =
				new ComponentClassLayoutRequestValidator("javax.swing.JButton");
		// false: not JavaInfo
		{
			Object child = new Object();
			assert_validateCMA(validator, false, panel, child);
		}
		// false: java.awt.Button
		{
			Object child = createComponent("java.awt.Button");
			assert_validateCMA(validator, false, panel, child);
		}
		// true: JButton
		{
			Object child = createComponent("javax.swing.JButton");
			assert_validateCMA(validator, true, panel, child);
		}
	}
}
