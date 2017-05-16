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
package org.eclipse.wb.draw2d.geometry;

/**
 * A translatable object can be translated (or moved) vertically and/or horizontally.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public interface Translatable {
  /**
   * Translates this object horizontally by <code>point.x</code> and vertically by
   * <code>point.y</code>.
   */
  void translate(Point point);

  /**
   * Translates this object horizontally by <code>dimension.width</code> and vertically by
   * <code>dimension.height</code>.
   */
  void translate(Dimension dimension);

  /**
   * Translates this object horizontally by <code>insets.left</code> and vertically by
   * <code>insets.top</code>.
   */
  void translate(Insets insets);

  /**
   * Translates this object horizontally by <code>dx</code> and vertically by <code>dy</code>.
   */
  void translate(int dx, int dy);
}