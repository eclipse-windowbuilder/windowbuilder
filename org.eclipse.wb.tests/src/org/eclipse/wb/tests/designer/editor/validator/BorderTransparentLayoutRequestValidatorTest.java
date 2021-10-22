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

import org.eclipse.wb.core.gef.policy.validator.BorderTransparentLayoutRequestValidator;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.LocationRequest;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;

/**
 * Test {@link BorderTransparentLayoutRequestValidator}.
 *
 * @author scheglov_ke
 */
public class BorderTransparentLayoutRequestValidatorTest extends SwingGefTest {
  private final ILayoutRequestValidator validator = new BorderTransparentLayoutRequestValidator(20,
      10);
  private ContainerInfo panel;
  private GraphicalEditPart panelEditPart;

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
  private void preparePanel() throws Exception {
    panel =
        openContainer(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    panelEditPart = canvas.getEditPart(panel);
  }

  public void test_CREATE() throws Exception {
    preparePanel();
    CreateRequest request = new CreateRequest(null);
    // in inner
    {
      setLocation(request, 100, 50);
      assertTrue(validator.validateCreateRequest(panelEditPart, request));
    }
    // on border
    {
      setLocation(request, 10, 5);
      assertFalse(validator.validateCreateRequest(panelEditPart, request));
    }
    // on border, but erase
    {
      setLocation(request, 10, 5);
      request.setEraseFeedback(true);
      assertTrue(validator.validateCreateRequest(panelEditPart, request));
    }
  }

  public void test_CREATE_tree() throws Exception {
    preparePanel();
    EditPart treeEditPart = tree.getEditPart(panel);
    CreateRequest request = new CreateRequest(null);
    // no such thing as "on border" in tree, so valid
    {
      setLocation(request, 10, 5);
      assertTrue(validator.validateCreateRequest(treeEditPart, request));
    }
  }

  public void test_PASTE() throws Exception {
    preparePanel();
    PasteRequest request = new PasteRequest(null);
    // in inner
    {
      setLocation(request, 100, 50);
      assertTrue(validator.validatePasteRequest(panelEditPart, request));
    }
    // on border
    {
      setLocation(request, 10, 5);
      assertFalse(validator.validatePasteRequest(panelEditPart, request));
    }
    // on border, but erase
    {
      setLocation(request, 10, 5);
      request.setEraseFeedback(true);
      assertTrue(validator.validatePasteRequest(panelEditPart, request));
    }
  }

  public void test_MOVE() throws Exception {
    preparePanel();
    ChangeBoundsRequest request = new ChangeBoundsRequest(Request.REQ_MOVE);
    // valid even on border
    {
      setLocation(request, 10, 5);
      assertTrue(validator.validateMoveRequest(panelEditPart, request));
    }
  }

  public void test_ADD() throws Exception {
    preparePanel();
    ChangeBoundsRequest request = new ChangeBoundsRequest(Request.REQ_ADD);
    // in inner
    {
      setLocation(request, 100, 50);
      assertTrue(validator.validateAddRequest(panelEditPart, request));
    }
    // on border
    {
      setLocation(request, 10, 5);
      assertFalse(validator.validateAddRequest(panelEditPart, request));
    }
    // on border, but erase
    {
      setLocation(request, 10, 5);
      request.setEraseFeedback(true);
      assertTrue(validator.validateAddRequest(panelEditPart, request));
    }
  }

  private void setLocation(LocationRequest request, int x, int y) {
    request.setLocation(getAbsoluteLocation(panelEditPart, x, y));
  }

  private void setLocation(ChangeBoundsRequest request, int x, int y) {
    request.setLocation(getAbsoluteLocation(panelEditPart, x, y));
  }

  /**
   * @return the absolute location for given relative location.
   */
  private static Point getAbsoluteLocation(GraphicalEditPart editPart, int x, int y) {
    Point location = new Point(x, y);
    FigureUtils.translateFigureToAbsolute2(editPart.getFigure(), location);
    return location;
  }
}
