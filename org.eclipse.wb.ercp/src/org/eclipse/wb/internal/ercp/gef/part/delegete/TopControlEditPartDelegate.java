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
package org.eclipse.wb.internal.ercp.gef.part.delegete;

import org.eclipse.wb.core.gef.part.AbstractComponentEditPart;
import org.eclipse.wb.core.gef.policy.selection.EmptySelectionEditPolicy;
import org.eclipse.wb.core.gef.policy.selection.TopSelectionEditPolicy;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.Graphics;
import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.internal.ercp.devices.model.DeviceInfo;
import org.eclipse.wb.internal.ercp.model.widgets.CompositeTopBoundsSupport;
import org.eclipse.wb.internal.swt.gef.part.ControlEditPart;
import org.eclipse.wb.internal.swt.gef.part.delegate.IControlEditPartDelegate;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;

import org.eclipse.swt.graphics.Image;

/**
 * Implementation of {@link IControlEditPartDelegate} for top level {@link ControlEditPart}.
 * 
 * @author scheglov_ke
 * @coverage ercp.gef.part
 */
public final class TopControlEditPartDelegate implements IControlEditPartDelegate {
  private static final Point DEVICE_LOCATION = new Point(5, 5);
  private final ControlEditPart m_editPart;
  private final CompositeInfo m_component;
  private final CompositeTopBoundsSupport m_topBounds;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TopControlEditPartDelegate(ControlEditPart editPart) {
    m_editPart = editPart;
    m_component = (CompositeInfo) m_editPart.getModel();
    m_topBounds = (CompositeTopBoundsSupport) m_component.getTopBoundsSupport();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visuals
  //
  ////////////////////////////////////////////////////////////////////////////
  public Figure createFigure() {
    return new Figure() {
      @Override
      protected void paintClientArea(Graphics graphics) {
        Image image = m_component.getImage();
        if (image != null) {
          graphics.drawImage(image, 0, 0);
        }
      }
    };
  }

  public void refreshVisuals() {
    try {
      DeviceInfo device = m_topBounds.getDevice();
      if (device != null) {
        // update figure with device image
        {
          Image image = device.getImage();
          m_deviceFigure.setLocation(DEVICE_LOCATION);
          m_deviceFigure.setSize(image.getBounds().width, image.getBounds().height);
        }
        // update EditPart figure
        {
          Rectangle bounds = device.getDisplayBounds().getCopy();
          bounds.translate(DEVICE_LOCATION);
          m_editPart.getFigure().setBounds(bounds);
        }
      } else {
        Rectangle bounds = m_component.getBounds();
        bounds =
            new Rectangle(AbstractComponentEditPart.TOP_LOCATION.x,
                AbstractComponentEditPart.TOP_LOCATION.y,
                bounds.width,
                bounds.height);
        m_editPart.getFigure().setBounds(bounds);
      }
    } catch (Throwable e) {
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Notify
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Figure m_deviceFigure = new Figure() {
    @Override
    protected void paintClientArea(Graphics graphics) {
      try {
        DeviceInfo device = m_topBounds.getDevice();
        if (device != null) {
          Image image = device.getImage();
          if (image != null) {
            graphics.drawImage(image, 0, 0);
          }
        }
      } catch (Throwable e) {
      }
    }
  };

  public void addNotify() {
    getDeviceFigureLayer().add(m_deviceFigure);
  }

  public void removeNotify() {
    FigureUtils.removeFigure(m_deviceFigure);
  }

  /**
   * @return the {@link Layer} for device figure.
   */
  private Layer getDeviceFigureLayer() {
    return m_editPart.getViewer().getLayer(IEditPartViewer.PRIMARY_LAYER_SUB_1);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policies
  //
  ////////////////////////////////////////////////////////////////////////////
  public void refreshEditPolicies() {
    try {
      DeviceInfo device = m_topBounds.getDevice();
      if (device != null) {
        // we can not resize on device, so use empty selection policy
        m_editPart.installEditPolicy(EditPolicy.SELECTION_ROLE, new EmptySelectionEditPolicy());
      } else {
        m_editPart.installEditPolicy(
            EditPolicy.SELECTION_ROLE,
            new TopSelectionEditPolicy(m_component));
      }
    } catch (Throwable e) {
    }
  }
}
