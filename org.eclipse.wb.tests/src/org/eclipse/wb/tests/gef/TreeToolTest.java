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
package org.eclipse.wb.tests.gef;

import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.core.requests.SelectionRequest;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.gef.tree.TreeEditPart;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.gef.core.EditDomain;
import org.eclipse.wb.internal.gef.tree.TreeViewer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import java.util.List;

/**
 * @author lobas_av
 *
 */
public abstract class TreeToolTest extends GefTestCase {
  protected Shell m_shell;
  protected EditDomain m_domain;
  protected TreeViewer m_viewer;
  protected EventSender m_sender;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TreeToolTest() {
    super(LayoutEditPolicy.class);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // SetUp
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    //
    m_shell = new Shell();
    // create domain
    m_domain = new EditDomain() {
      @Override
      public Tool getDefaultTool() {
        return null;
      }
    };
    // create viewer
    m_viewer = new TreeViewer(m_shell, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
    m_viewer.getControl().setSize(500, 400);
    m_viewer.setEditDomain(m_domain);
    // create sender
    m_sender = new EventSender(m_viewer.getControl());
  }

  @Override
  protected void tearDown() throws Exception {
    m_shell.dispose();
    m_shell = null;
    m_domain = null;
    m_viewer = null;
    m_sender = null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Location Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>(x + width / 2, y + 1)</code> location of part tree item bounds.
   */
  protected static final Point getBeforeLocation(TreeEditPart part) {
    Rectangle bounds = new Rectangle(part.getWidget().getBounds());
    Point location = bounds.getTop();
    location.y++;
    return location;
  }

  /**
   * @return <code>(x + width / 2, y + height / 2)</code> location of part tree item bounds.
   */
  protected static final Point getOnLocation(TreeEditPart part) {
    Rectangle bounds = new Rectangle(part.getWidget().getBounds());
    return bounds.getCenter();
  }

  /**
   * @return <code>(x + width / 2, y + height - 1)</code> location of part tree item bounds.
   */
  protected static final Point getAfterLocation(TreeEditPart part) {
    Rectangle bounds = new Rectangle(part.getWidget().getBounds());
    Point location = bounds.getBottom();
    location.y--;
    return location;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Asserts that given {@link RequestsLogger}'s contain same sequence of events.
   */
  protected static final void assertLoggers(RequestsLogger expectedLogger,
      RequestsLogger actualLogger) {
    actualLogger.assertEquals(expectedLogger);
    actualLogger.clear();
    expectedLogger.clear();
  }

  protected static final void refreshTreeParst(TreeEditPart part) throws Exception {
    ReflectionUtils.invokeMethod(part, "refreshVisuals()");
    for (EditPart child : part.getChildren()) {
      refreshTreeParst((TreeEditPart) child);
    }
  }

  protected final TreeEditPart addEditPart(EditPart parentEditPart,
      String name,
      RequestsLogger actualLogger,
      ILayoutEditPolicy ipolicy) throws Exception {
    RequestTreeEditPart editPart = new RequestTreeEditPart(name, actualLogger, ipolicy);
    if (m_viewer.getRootEditPart() == parentEditPart) {
      m_viewer.getRootContainer().setContent(editPart);
    } else {
      addChildEditPart(parentEditPart, editPart);
    }
    return editPart;
  }

  protected static interface ILayoutEditPolicy {
    boolean isGoodReferenceChild(Request request, EditPart editPart);
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // EditPart implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final class RequestTreeEditPart extends TreeEditPart {
    private final String m_name;
    private final RequestsLogger m_logger;
    private final ILayoutEditPolicy m_ipolicy;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public RequestTreeEditPart(String name, RequestsLogger logger, ILayoutEditPolicy ipolicy) {
      m_name = name;
      m_logger = logger;
      m_ipolicy = ipolicy;
      setModel(m_name);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // EditPart
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected void createEditPolicies() {
      super.createEditPolicies();
      if (m_ipolicy != null) {
        installEditPolicy(EditPolicy.LAYOUT_ROLE, new LayoutEditPolicy() {
          @Override
          protected boolean isGoodReferenceChild(Request request, EditPart editPart) {
            return m_ipolicy.isGoodReferenceChild(request, editPart);
          }

          @Override
          protected Command getPasteCommand(PasteRequest request, Object referenceObject) {
            m_logger.log(RequestTreeEditPart.this, "getPasteCommand", request);
            return null;
          }

          @Override
          protected Command getMoveCommand(List<EditPart> moveParts, Object referenceObject) {
            m_logger.log(RequestTreeEditPart.this, "getMoveCommand(parts="
                + moveParts
                + ", next="
                + referenceObject
                + ")");
            return null;
          }

          @Override
          protected Command getCreateCommand(Object newObject, Object referenceObject) {
            m_logger.log(RequestTreeEditPart.this, "getCreateCommand(object="
                + newObject
                + ", next="
                + referenceObject
                + ")");
            return null;
          }

          @Override
          protected Command getAddCommand(List<EditPart> addParts, Object referenceObject) {
            m_logger.log(RequestTreeEditPart.this, "getAddCommand(parts="
                + addParts
                + ", next="
                + referenceObject
                + ")");
            return null;
          }
        });
      }
    }

    @Override
    public EditPart getTargetEditPart(Request request) {
      if (request instanceof SelectionRequest) {
        return this;
      }
      return super.getTargetEditPart(request);
    }

    @Override
    public void performRequest(Request request) {
      m_logger.log(this, "performRequest", request);
      super.performRequest(request);
    }

    @Override
    protected void refreshVisuals() {
      getWidget().setText(m_name);
    }

    @Override
    public String toString() {
      return m_name;
    }
  }
}