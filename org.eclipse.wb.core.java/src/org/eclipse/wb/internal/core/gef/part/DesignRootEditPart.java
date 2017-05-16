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
package org.eclipse.wb.internal.core.gef.part;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.core.gef.part.nonvisual.NonVisualBeanEditPart;
import org.eclipse.wb.internal.core.gef.policy.nonvisual.NonVisualLayoutEditPolicy;
import org.eclipse.wb.internal.core.model.DesignRootObject;
import org.eclipse.wb.internal.core.model.nonvisual.NonVisualBeanInfo;
import org.eclipse.wb.internal.draw2d.FigureCanvas;
import org.eclipse.wb.internal.draw2d.IPreferredSizeProvider;

import java.util.List;

/**
 * {@link EditPart} for Designer root object.
 *
 * @author lobas_av
 * @coverage core.gef
 */
public final class DesignRootEditPart extends GraphicalEditPart {
  private final DesignRootObject m_designRootObject;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DesignRootEditPart(DesignRootObject designRootObject) {
    m_designRootObject = designRootObject;
    setModel(m_designRootObject);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void activate() {
    refreshVisualsOnModelRefresh();
    super.activate();
  }

  private void refreshVisualsOnModelRefresh() {
    m_designRootObject.getRootObject().addBroadcastListener(new ObjectEventListener() {
      @Override
      public void refreshDispose() throws Exception {
        if (isActive()) {
          getFigureCanvas().setDrawCached(true);
        }
      }

      @Override
      public void refreshed() throws Exception {
        getFigureCanvas().setDrawCached(false);
        getFigureCanvas().redraw();
        refresh();
      }

      private FigureCanvas getFigureCanvas() {
        return (FigureCanvas) getViewer().getControl();
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policies
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createEditPolicies() {
    installEditPolicy(
        EditPolicy.LAYOUT_ROLE,
        new NonVisualLayoutEditPolicy(m_designRootObject.getRootObject()));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Children
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<?> getModelChildren() {
    return m_designRootObject.getChildren();
  }

  @Override
  protected EditPart createEditPart(Object model) {
    if (m_designRootObject.getRootObject() != model) {
      // direct create non visual bean part
      JavaInfo javaInfo = (JavaInfo) model;
      NonVisualBeanInfo nonVisualInfo = NonVisualBeanInfo.getNonVisualInfo(javaInfo);
      // create EditPart only if location specified
      if (nonVisualInfo != null && nonVisualInfo.getLocation() != null) {
        return new NonVisualBeanEditPart(javaInfo);
      } else {
        return null;
      }
    }
    // use factory
    return super.createEditPart(model);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return java root {@link EditPart}.
   */
  public EditPart getJavaRootEditPart() {
    return getChildren().get(0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Figure
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Figure createFigure() {
    return new TopFigure();
  }

  /**
   * Special {@link Figure} that cover full area of parent.
   */
  private static final class TopFigure extends Figure implements IPreferredSizeProvider {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Figure
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public Rectangle getBounds() {
      Figure parentFigure = getParent();
      if (parentFigure != null) {
        return new Rectangle(new Point(), parentFigure.getSize());
      }
      return super.getBounds();
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // IPreferredSizeProvider
    //
    ////////////////////////////////////////////////////////////////////////////
    public Dimension getPreferredSize(Dimension originalPreferredSize) {
      Rectangle preferred = new Rectangle();
      for (Figure figure : getChildren()) {
        if (figure.isVisible()) {
          preferred.union(figure.getBounds());
        }
      }
      return preferred.getSize();
    }
  }
}