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
package org.eclipse.wb.internal.swing.laf;

import org.eclipse.wb.draw2d.IPositionConstants;

import java.awt.Container;

import javax.swing.JComponent;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;

/**
 * Interface providing LayoutStyle functionality.
 * 
 * @author mitin_aa
 * @coverage swing.laf
 */
public interface ILayoutStyleSupport {
  String LAYOUT_STYLE_POINT = "org.eclipse.wb.swing.layoutStyle";

  /**
   * Sets the LayoutStyle instance for given <code>laf</code>.
   * 
   * @param laf
   *          a {@link LookAndFeel} which LayoutStyle would be used.
   */
  void setLayoutStyle(LookAndFeel laf);

  /**
   * Calls appropriate LayoutStyle.getPreferredGap() method of LayoutStyle class using reflection.
   * Note, the <code>position</code> should be one of {@link IPositionConstants#LEFT},
   * {@link IPositionConstants#RIGHT}, {@link IPositionConstants#TOP},
   * {@link IPositionConstants#BOTTOM} (it would be converted to appropriate {@link SwingConstants}
   * values).
   */
  int getPreferredGap(JComponent component1,
      JComponent component2,
      int componentPlacement,
      int position,
      Container parent);

  /**
   * Calls appropriate LayoutStyle.getContainerGap() method of LayoutStyle class using reflection.
   * Note, the <code>position</code> should be one of {@link IPositionConstants#LEFT},
   * {@link IPositionConstants#RIGHT}, {@link IPositionConstants#TOP},
   * {@link IPositionConstants#BOTTOM} (it would be converted to appropriate {@link SwingConstants}
   * values).
   */
  int getContainerGap(JComponent component, int position, Container parent);
}
