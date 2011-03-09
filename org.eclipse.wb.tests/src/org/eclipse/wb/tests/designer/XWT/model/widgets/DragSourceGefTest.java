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

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.xwt.model.widgets.DragSourceInfo;
import org.eclipse.wb.tests.designer.XWT.gef.XwtGefTest;

/**
 * Test for {@link DragSourceInfo} in GEF.
 * 
 * @author scheglov_ke
 */
public class DragSourceGefTest extends XwtGefTest {
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
   * We should have {@link EditPart} in tree and on canvas.
   */
  public void test_editParts() throws Exception {
    openEditor(
        "<Shell xmlns:p1='clr-namespace:org.eclipse.swt.dnd'>",
        "  <p1:DragSource wbp:name='dragSource'/>",
        "</Shell>");
    DragSourceInfo dragSource = getObjectByName("dragSource");
    // in tree
    tree.assertNotNullEditPart(dragSource);
    // on canvas
    {
      GraphicalEditPart editPart = canvas.getEditPart(dragSource);
      assertNotNull(editPart);
      assertEquals(new Rectangle(25, 5, 16, 16), editPart.getFigure().getBounds());
    }
  }

  public void test_CREATE() throws Exception {
    XmlObjectInfo shell = openEditor("<Shell/>");
    //
    DragSourceInfo dragSource = loadCreationTool("org.eclipse.swt.dnd.DragSource");
    canvas.moveTo(shell, 0.5, 0.5);
    canvas.assertFeedbacks(canvas.getTargetPredicate(shell));
    canvas.click();
    assertXML(
        "// filler filler filler filler filler",
        "<Shell xmlns:p1='clr-namespace:org.eclipse.swt.dnd'>",
        "  <p1:DragSource/>",
        "</Shell>");
    canvas.assertPrimarySelected(dragSource);
  }
}
