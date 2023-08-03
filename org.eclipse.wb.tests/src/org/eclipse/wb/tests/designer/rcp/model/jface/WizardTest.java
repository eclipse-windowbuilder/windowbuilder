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
package org.eclipse.wb.tests.designer.rcp.model.jface;

import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.rcp.IExceptionConstants;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.jface.wizard.Wizard;

import org.junit.Test;

/**
 * Test for {@link Wizard} support (rather not support).
 *
 * @author scheglov_ke
 */
public class WizardTest extends RcpModelTest {
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
	public void test_parsingException() throws Exception {
		try {
			parseJavaInfo(
					"import org.eclipse.jface.wizard.*;",
					"public class Test extends Wizard {",
					"  public Test() {",
					"  }",
					"  public void addPages() {",
					"  }",
					"  public boolean performFinish() {",
					"    return true;",
					"  }",
					"}");
			fail();
		} catch (DesignerException e) {
			assertEquals(IExceptionConstants.NO_DESIGN_WIZARD, e.getCode());
			assertTrue(DesignerExceptionUtils.isWarning(e));
		}
	}
}