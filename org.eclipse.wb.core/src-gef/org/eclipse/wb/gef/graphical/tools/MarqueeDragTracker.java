/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
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