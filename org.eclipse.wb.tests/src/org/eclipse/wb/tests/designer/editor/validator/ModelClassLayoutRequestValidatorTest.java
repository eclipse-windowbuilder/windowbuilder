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
package org.eclipse.wb.tests.designer.editor.validator;

import org.eclipse.wb.core.gef.policy.validator.ModelClassLayoutRequestValidator;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.junit.Test;

/**
 * Test {@link ModelClassLayoutRequestValidator}.
 *
 * @author scheglov_ke
 */
public class ModelClassLayoutRequestValidatorTest extends AbstractLayoutRequestValidatorTest {
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
	/**
	 * Test for create/move/add.
	 */
	@Test
	public void test_CMA() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		ILayoutRequestValidator validator = new ModelClassLayoutRequestValidator(ContainerInfo.class);
		// false: not JavaInfo
		{
			Object child = new Object();
			assert_validateCMA(validator, false, panel, child);
		}
		// false: java.awt.Button
		{
			ComponentInfo child = createComponent("java.awt.Button");
			assert_validateCMA(validator, false, panel, child);
		}
		// true: JPanel
		{
			ComponentInfo child = createComponent("javax.swing.JPanel");
			assert_validateCMA(validator, true, panel, child);
		}
	}

	/**
	 * Test for paste.
	 */
	@Test
	public void test_validatePasteRequest() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    add(new java.awt.Button());",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// false: require ContainerInfo
		{
			ILayoutRequestValidator validator = new ModelClassLayoutRequestValidator(ContainerInfo.class);
			assert_validatePasteRequest(validator, false, panel, button);
		}
		// true: require ComponentInfo
		{
			ILayoutRequestValidator validator = new ModelClassLayoutRequestValidator(ComponentInfo.class);
			assert_validatePasteRequest(validator, true, panel, button);
		}
	}
}
