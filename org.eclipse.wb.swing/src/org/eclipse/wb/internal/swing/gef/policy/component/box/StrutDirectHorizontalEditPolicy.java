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

import org.eclipse.wb.gef.graphical.policies.DirectTextEditPolicy;
import org.eclipse.wb.internal.core.model.property.converter.IntegerConverter;
import org.eclipse.wb.internal.swing.gef.part.box.BoxStrutHorizontalEditPart;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

/**
 * Implementation of {@link DirectTextEditPolicy} for {@link BoxStrutHorizontalEditPart} that allows
 * to edit width of strut.
 * 
 * @author scheglov_ke
 * @coverage swing.gef.policy
 */
public final class StrutDirectHorizontalEditPolicy extends StrutDirectEditPolicy {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StrutDirectHorizontalEditPolicy(ComponentInfo strut) {
    super(strut);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // DirectTextEditPolicy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getText() {
    return "" + getHost().getFigure().getBounds().width;
  }

  @Override
  protected String getSource(ComponentInfo strut, String text) throws Exception {
    int value = Integer.parseInt(text);
    return IntegerConverter.INSTANCE.toJavaSource(strut, value);
  }
}
