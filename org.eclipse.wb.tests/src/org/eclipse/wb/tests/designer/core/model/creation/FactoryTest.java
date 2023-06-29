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
package org.eclipse.wb.tests.designer.core.model.creation;

import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.exception.ICoreExceptionConstants;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

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
