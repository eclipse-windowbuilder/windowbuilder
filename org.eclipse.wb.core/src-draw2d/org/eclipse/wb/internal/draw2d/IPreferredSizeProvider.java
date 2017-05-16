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
package org.eclipse.wb.internal.draw2d;

import org.eclipse.wb.draw2d.geometry.Dimension;

/**
 * An interface to size providers for preferred-size-oriented figures.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public interface IPreferredSizeProvider {
  /**
   * Calculate new preferred size use original preferred size.
   */
  Dimension getPreferredSize(Dimension originalPreferredSize);
}