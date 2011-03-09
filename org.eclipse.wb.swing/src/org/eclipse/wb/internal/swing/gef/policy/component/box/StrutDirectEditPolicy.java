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

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.requests.KeyRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.policies.DirectTextEditPolicy;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.swing.gef.part.box.BoxStrutHorizontalEditPart;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.layout.BoxSupport;

/**
 * Implementation of {@link DirectTextEditPolicy} for {@link BoxStrutHorizontalEditPart} that allows
 * to edit width strut.
 * 
 * @author scheglov_ke
 * @coverage swing.gef.policy
 */
abstract class StrutDirectEditPolicy extends DirectTextEditPolicy {
  private final ComponentInfo m_strut;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StrutDirectEditPolicy(ComponentInfo strut) {
    m_strut = strut;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // DirectTextEditPolicy
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the source corresponding to the text, entered by user.
   */
  protected abstract String getSource(ComponentInfo strut, String text) throws Exception;

  @Override
  protected final void setText(String text) {
    // prepare source
    final String source;
    try {
      source = getSource(m_strut, text);
    } catch (Throwable e) {
      return;
    }
    // set source
    ExecutionUtils.run(m_strut, new RunnableEx() {
      public void run() throws Exception {
        BoxSupport.setStrutSize(m_strut, source);
      }
    });
  }

  @Override
  protected final Point getTextWidgetLocation(Rectangle hostBounds, Dimension textSize) {
    int x = hostBounds.getCenter().x - textSize.width / 2;
    int y = hostBounds.getCenter().y - textSize.height / 2;
    return new Point(x, y);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Request
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final void performRequest(Request request) {
    if (request instanceof KeyRequest) {
      KeyRequest keyRequest = (KeyRequest) request;
      if (keyRequest.isPressed() && keyRequest.getCharacter() == ' ') {
        beginEdit();
      }
    }
    super.performRequest(request);
  }
}
