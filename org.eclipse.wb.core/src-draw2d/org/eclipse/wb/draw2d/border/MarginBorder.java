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
package org.eclipse.wb.draw2d.border;

import org.eclipse.wb.draw2d.Graphics;
import org.eclipse.wb.draw2d.geometry.Insets;

/**
 * A border that provides blank padding.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public class MarginBorder extends Border {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Constructs a {@link MarginBorder} with dimensions specified by <i>insets</i>.
   */
  public MarginBorder(Insets insets) {
    super(insets);
  }

  /**
   * Constructs a {@link MarginBorder} with padding specified by the passed values.
   */
  public MarginBorder(int allsides) {
    this(new Insets(allsides));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Border
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * This method does nothing, since this border is just for spacing.
   */
  @Override
  protected void paint(int ownerWidth, int ownerHeight, Graphics graphics) {
  }
}