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
package org.eclipse.wb.tests.designer.core.util;

import org.eclipse.wb.core.branding.BrandingUtils;
import org.eclipse.wb.core.branding.IBrandingDescription;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

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
  protected void tearDown() throws Exception {
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
  public void test_getBranding_multi() throws Exception {
    IBrandingDescription branding = BrandingUtils.getBranding();
    assertNotNull(branding);
    // no check for name, may be default, may be GWT
    assertNotSame(null, branding.getProductName());
  }
}
