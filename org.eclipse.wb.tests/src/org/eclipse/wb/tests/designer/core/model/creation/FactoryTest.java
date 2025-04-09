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
package org.eclipse.wb.tests.designer.core.model.creation;

import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.exception.ICoreExceptionConstants;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.junit.Test;

/**
 * Test for any factory.
 *
 * @author scheglov_ke
 */
public class FactoryTest extends SwingModelTest {
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
	// Parsing
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_parseFactory() throws Exception {
		try {
			m_waitForAutoBuild = true;
			parseContainer(
					"public final class Test {",
					"  /**",
					"  * @wbp.factory",
					"  */",
					"  public static JButton createButton() {",
					"    return new JButton();",
					"  }",
					"}");
			fail();
		} catch (DesignerException e) {
			assertEquals(ICoreExceptionConstants.PARSER_FACTORY_NOT_SUPPORTED, e.getCode());
			assertTrue(DesignerExceptionUtils.isWarning(e));
		}
	}
}
