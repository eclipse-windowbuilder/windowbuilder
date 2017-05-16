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
package org.eclipse.wb.internal.core.gef.part.nonvisual;

import org.eclipse.wb.core.gef.policy.selection.NonResizableSelectionEditPolicy;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.core.model.nonvisual.NonVisualBeanInfo;
import org.eclipse.wb.internal.core.model.util.ObjectsLabelProvider;

import org.eclipse.swt.graphics.Image;

/**
 * {@link EditPart} for <i>non-visual bean</i> model.
 *
 * @author lobas_av
 * @coverage core.gef.nonvisual
 */
public final class NonVisualBeanEditPart extends GraphicalEditPart {
  private final NonVisualBeanInfo m_beanInfo;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public NonVisualBeanEditPart(JavaInfo javaInfo) {
    m_beanInfo = NonVisualBeanInfo.getNonVisualInfo(javaInfo);
    setModel(javaInfo);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public NonVisualBeanInfo getNonVisualInfo() {
    return m_beanInfo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createEditPolicies() {
    installEditPolicy(EditPolicy.SELECTION_ROLE, new NonResizableSelectionEditPolicy());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Figure
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Figure createFigure() {
    Image image = ObjectsLabelProvider.INSTANCE.getImage(m_beanInfo.getJavaInfo());
    return new BeanFigure(image);
  }

  @Override
  protected void refreshVisuals() {
    String text = ObjectsLabelProvider.INSTANCE.getText(m_beanInfo.getJavaInfo());
    BeanFigure figure = (BeanFigure) getFigure();
    figure.update(text, m_beanInfo.getLocation());
  }
}