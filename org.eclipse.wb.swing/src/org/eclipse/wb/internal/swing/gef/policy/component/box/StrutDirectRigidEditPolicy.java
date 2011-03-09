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
package org.eclipse.wb.internal.swing.gef.policy.component.box;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.graphical.policies.DirectTextEditPolicy;
import org.eclipse.wb.internal.swing.gef.part.box.BoxRigidAreaEditPart;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.property.converter.DimensionConverter;

import org.apache.commons.lang.StringUtils;

/**
 * Implementation of {@link DirectTextEditPolicy} for {@link BoxRigidAreaEditPart} that allows to
 * edit dimension of rigid area.
 * 
 * @author scheglov_ke
 * @coverage swing.gef.policy
 */
public final class StrutDirectRigidEditPolicy extends StrutDirectEditPolicy {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StrutDirectRigidEditPolicy(ComponentInfo strut) {
    super(strut);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // DirectTextEditPolicy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getText() {
    Rectangle bounds = getHost().getFigure().getBounds();
    return bounds.width + " " + bounds.height;
  }

  @Override
  protected String getSource(ComponentInfo strut, String text) throws Exception {
    String[] parts = StringUtils.split(text);
    int width = Integer.parseInt(parts[0]);
    int height = Integer.parseInt(parts[1]);
    return DimensionConverter.INSTANCE.toJavaSource(strut, new java.awt.Dimension(width, height));
  }
}
