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
package org.eclipse.wb.tests.designer.swt.model.widgets;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ScrollableInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

/**
 * Test for {@link ScrollableInfo}.
 *
 * @author scheglov_ke
 */
public class ScrollableTest extends RcpModelTest {

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getClientArea_Composite() throws Exception {
    CompositeInfo composite =
        parseComposite(
            "public class Test extends Composite {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "}");
    composite.refresh();
    //
    assertEquals(new Rectangle(0, 0, 450, 300), composite.getClientArea());
  }

}