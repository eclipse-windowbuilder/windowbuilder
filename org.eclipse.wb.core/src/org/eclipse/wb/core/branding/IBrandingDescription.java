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
package org.eclipse.wb.core.branding;

import org.eclipse.wb.draw2d.Graphics;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.editor.DesignComposite;

/**
 * The interface for branding information.
 *
 * @author Jaime Wren
 * @coverage core.util
 */
public interface IBrandingDescription {
  /**
   * @return the product name.
   */
  String getProductName();

  /**
   * @return the url for bug-tracking system and discussion forum.
   */
  IBrandingSupportInfo getSupportInfo();

  /**
   * Called by {@link DesignComposite} to paint all product branding onto the canvas.
   */
  void paintBrandingOnCanvas(Rectangle clientArea, Graphics graphics);
}
