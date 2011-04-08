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
package org.eclipse.wb.internal.swing.java6.laf;

import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.swing.java6.Messages;
import org.eclipse.wb.internal.swing.laf.ILayoutStyleSupport;

import java.awt.Container;
import java.text.MessageFormat;

import javax.swing.JComponent;
import javax.swing.LayoutStyle;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;

/**
 * Java6 LayoutStyle support implementation.
 * 
 * @author mitin_aa
 * @coverage swing.laf
 */
public final class LayoutStyleSupport implements ILayoutStyleSupport {
  private LayoutStyle m_layoutStyle;

  ////////////////////////////////////////////////////////////////////////////
  //
  // ILayoutStyleSupport
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setLayoutStyle(LookAndFeel laf) {
    Assert.isNotNull(laf);
    m_layoutStyle = laf.getLayoutStyle();
  }

  @Override
  public int getContainerGap(JComponent component, int position, Container parent) {
    return m_layoutStyle.getContainerGap(component, convertPositionConstants(position), parent);
  }

  @Override
  public int getPreferredGap(JComponent component1,
      JComponent component2,
      int componentPlacement,
      int position,
      Container parent) {
    return m_layoutStyle.getPreferredGap(
        component1,
        component2,
        convertPlacement(componentPlacement),
        convertPositionConstants(position),
        parent);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Converts from {@link IPositionConstants} into {@link SwingConstants}.
   */
  private static int convertPositionConstants(int positionConstant) {
    switch (positionConstant) {
      case IPositionConstants.TOP :
        return SwingConstants.NORTH;
      case IPositionConstants.BOTTOM :
        return SwingConstants.SOUTH;
      case IPositionConstants.LEFT :
        return SwingConstants.WEST;
      case IPositionConstants.RIGHT :
        return SwingConstants.EAST;
      default :
        throw new IllegalArgumentException(MessageFormat.format(
            Messages.LayoutStyleSupport_unsupportedPosition,
            positionConstant));
    }
  }

  /**
   * Converts integer into {@link ComponentPlacement} constant.
   */
  private ComponentPlacement convertPlacement(int componentPlacement) {
    ComponentPlacement[] values = ComponentPlacement.values();
    for (ComponentPlacement placement : values) {
      if (placement.ordinal() == componentPlacement) {
        return placement;
      }
    }
    throw new IllegalArgumentException(MessageFormat.format(
        Messages.LayoutStyleSupport_unsupportedPlacement,
        componentPlacement));
  }
}
