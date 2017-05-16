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
package org.eclipse.wb.core.gef.part;

import org.eclipse.wb.core.gef.command.CompoundEditCommand;
import org.eclipse.wb.core.gef.policy.selection.NonResizableSelectionEditPolicy;
import org.eclipse.wb.core.gef.policy.selection.TopSelectionEditPolicy;
import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.broadcast.DisplayEventListener;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.Graphics;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.requests.DragPermissionRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.core.requests.SelectionRequest;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.gef.policy.OpenErrorLogEditPolicy;
import org.eclipse.wb.internal.core.gef.policy.OpenListenerEditPolicy;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.draw2d.EventManager;
import org.eclipse.wb.internal.gef.core.CompoundCommand;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;

import java.util.Collections;
import java.util.List;

/**
 * {@link GraphicalEditPart} for {@link AbstractComponentInfo}.
 *
 * @author scheglov_ke
 * @coverage core.gef
 */
public abstract class AbstractComponentEditPart extends GraphicalEditPart {
  public static final Point TOP_LOCATION = EnvironmentUtils.IS_MAC
      ? new Point(20, 28)
      : new Point(20, 20);
  private final AbstractComponentInfo m_component;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractComponentEditPart(AbstractComponentInfo component) {
    m_component = component;
    setModel(m_component);
    listenFor_delayEvents();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link AbstractComponentInfo} for this {@link AbstractComponentEditPart}.
   */
  public final AbstractComponentInfo getComponent() {
    return m_component;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Figure
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Figure createFigure() {
    return new Figure() {
      @Override
      protected void paintClientArea(Graphics graphics) {
        if (m_component.isRoot()) {
          Image image = m_component.getImage();
          graphics.drawImage(image, 0, 0);
        }
        drawCustomBorder(this, graphics);
      }
    };
  }

  /**
   * Draw custom "control specific" graphics objects for given {@link Figure}.
   */
  protected void drawCustomBorder(Figure figure, Graphics graphics) {
  }

  @Override
  protected void refreshVisuals() {
    Rectangle bounds = m_component.getBounds();
    if (m_component.isRoot()) {
      Point rootLocation = getRootLocation();
      bounds = bounds.getCopy().setLocation(rootLocation);
    }
    // make it safe
    if (bounds == null) {
      bounds = new Rectangle(0, 0, 0, 0);
    }
    // set bounds
    getFigure().setBounds(bounds);
  }

  /**
   * @return the location to use, if this {@link AbstractComponentInfo} is root.
   */
  protected Point getRootLocation() {
    return TOP_LOCATION;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policies
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createEditPolicies() {
    if (m_component.isRoot()) {
      installEditPolicy(EditPolicy.SELECTION_ROLE, new TopSelectionEditPolicy(m_component));
    } else {
      installEditPolicy(EditPolicy.SELECTION_ROLE, new NonResizableSelectionEditPolicy());
    }
    installEditPolicy(new OpenListenerEditPolicy(m_component));
    OpenErrorLogEditPolicy.install(this);
    refreshEditPolicies();
  }

  /**
   * Installs {@link EditPolicy}'s after model refresh. For example we should install new
   * {@link LayoutEditPolicy} if component has now new layout.
   */
  protected void refreshEditPolicies() {
    OpenErrorLogEditPolicy.refresh(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Requests/Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public EditPart getTargetEditPart(Request request) {
    // sometimes we want to redirect selection to parent
    if (request instanceof SelectionRequest) {
      if (JavaInfoUtils.hasTrueParameter(m_component, "GEF.clickToParent")) {
        return getParent().getTargetEditPart(request);
      }
    }
    return super.getTargetEditPart(request);
  }

  @Override
  public void performRequest(Request request) {
    super.performRequest(request);
    if (request instanceof DragPermissionRequest) {
      DragPermissionRequest permissionRequest = (DragPermissionRequest) request;
      permissionRequest.setMove(JavaInfoUtils.canMove(m_component));
      permissionRequest.setReparent(JavaInfoUtils.canReparent(m_component));
    }
  }

  @Override
  public CompoundCommand createCompoundCommand() {
    return new CompoundEditCommand(m_component);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void refresh() {
    refreshEditPolicies();
    super.refresh();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Children
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<?> getModelChildren() {
    return ExecutionUtils.runObjectLog(new RunnableObjectEx<List<?>>() {
      public List<?> runObject() throws Exception {
        return m_component.getPresentation().getChildrenGraphical();
      }
    }, Collections.emptyList());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Events
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Delays events that happen during model-driven messages loops.
   */
  private void listenFor_delayEvents() {
    if (m_component.isRoot()) {
      m_component.addBroadcastListener(new DisplayEventListener() {
        private int m_level = 0;

        ////////////////////////////////////////////////////////////////////////////
        //
        // DisplayEventListener
        //
        ////////////////////////////////////////////////////////////////////////////
        @Override
        public void beforeMessagesLoop() {
          if (isEnabled()) {
            m_level++;
            if (m_level == 1) {
              delayEvents(true);
            }
          }
        }

        @Override
        public void afterMessagesLoop() {
          if (isEnabled()) {
            m_level--;
            if (m_level == 0) {
              delayEvents(false);
              runDelayedEvents();
            }
          }
        }

        ////////////////////////////////////////////////////////////////////////////
        //
        // Utils
        //
        ////////////////////////////////////////////////////////////////////////////
        private boolean isEnabled() {
          if (!isActive()) {
            return false;
          }
          if (getViewer().getControl().isDisposed()) {
            return false;
          }
          return true;
        }

        private void delayEvents(boolean delay) {
          Control viewerControl = getViewer().getControl();
          EventManager.delayEvents(viewerControl, delay);
        }

        private void runDelayedEvents() {
          ExecutionUtils.runLogLater(new RunnableEx() {
            public void run() throws Exception {
              if (isEnabled()) {
                Control viewerControl = getViewer().getControl();
                EventManager.runDelayedEvents(viewerControl);
              }
            }
          });
        }
      });
    }
  }
}