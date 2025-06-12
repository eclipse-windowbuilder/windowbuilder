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
package org.eclipse.wb.tests.designer.core.util;

import org.eclipse.wb.core.branding.BrandingUtils;
import org.eclipse.wb.core.branding.IBrandingDescription;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link BrandingUtils}.
 *
 * @author Jaime Wren
 * @author scheglov_ke
 */
public class BrandingUtilsTest extends DesignerTestCase {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@AfterEach
	public void tearDown() throws Exception {
		super.tearDown();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// getBranding()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * We run tests with multiple products.
	 */
	@Test
	public void test_getBranding_multi() throws Exception {
		IBrandingDescription branding = BrandingUtils.getBranding();
		assertNotNull(branding);
		// no check for name, may be default, may be GWT
		assertNotSame(null, branding.getProductName());
	}
}
