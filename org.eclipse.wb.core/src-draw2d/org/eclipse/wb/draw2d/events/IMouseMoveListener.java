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
package org.eclipse.wb.draw2d.events;

/**
 * A listener interface for receiving mouse motion event.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public interface IMouseMoveListener {
	/**
	 * Sent when the mouse moves.
	 */
	void mouseMove(MouseEvent event);
}