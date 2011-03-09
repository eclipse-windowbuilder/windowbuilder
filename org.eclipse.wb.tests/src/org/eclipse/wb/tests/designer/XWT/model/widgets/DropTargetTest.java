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
package org.eclipse.wb.tests.designer.XWT.model.widgets;

import org.eclipse.wb.internal.xwt.model.widgets.DropTargetInfo;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

/**
 * Test for {@link DropTargetInfo}.
 * 
 * @author scheglov_ke
 */
public class DropTargetTest extends XwtModelTest {
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
  public void test_0() throws Exception {
    parse(
        "<Shell xmlns:p1='clr-namespace:org.eclipse.swt.dnd'>",
        "  <p1:DropTarget wbp:name='dropTarget'/>",
        "</Shell>");
    refresh();
    DropTargetInfo dropTarget = getObjectByName("dropTarget");
    assertNotNull(dropTarget);
  }
}