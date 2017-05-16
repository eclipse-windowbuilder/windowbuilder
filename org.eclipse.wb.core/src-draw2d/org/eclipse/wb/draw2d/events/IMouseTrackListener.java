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

import org.eclipse.wb.draw2d.Figure;

/**
 * A listener interface for receiving mouse tracking events.
 *
 * @author scheglov_ke
 * @coverage gef.draw2d
 */
public interface IMouseTrackListener {
  /**
   * Sent when a mouse enters this {@link Figure}.
   */
  void mouseEnter(MouseEvent event);

  /**
   * Sent when a mouse exists this {@link Figure}.
   */
  void mouseExit(MouseEvent event);

  /**
   * Sent when a mouse hovers this {@link Figure}.
   */
  void mouseHover(MouseEvent event);
}