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
package org.eclipse.wb.gef.graphical.tools;

import org.eclipse.wb.gef.core.tools.Tool;

/**
 * A {@link MarqueeSelectionTool} that used as drag tracker {@link Tool}.
 *
 * @author lobas_av
 * @coverage gef.graphical
 */
public class MarqueeDragTracker extends MarqueeSelectionTool {
  /**
   * Called when the mouse button is released. Overridden to do nothing, since a drag tracker does
   * not need to unload when finished.
   */
  @Override
  protected void handleFinished() {
  }
}