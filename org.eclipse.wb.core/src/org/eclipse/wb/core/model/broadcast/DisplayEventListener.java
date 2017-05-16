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
package org.eclipse.wb.core.model.broadcast;

import org.eclipse.swt.widgets.Display;

/**
 * Listener for {@link Display} or GEF (but we avoid this word in name of this listener, because in
 * theory we could use any presentation for model) events.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public abstract class DisplayEventListener {
  /**
   * Notifies that model is going to begin some operation that may cause running messages loop in
   * {@link Display}, so presentation should be ready that user may "push" some events in this
   * period.
   * <p>
   * For example, in GWT we often should run messages loop to finalize component rendering, to get
   * exact image of component, including getting "live" image from policy (so in its own messages
   * loop!). So, we have to delay all events that will happen during these model-driven messages
   * loops, in other case GEF will be confused by recursive events.
   */
  public void beforeMessagesLoop() {
  }

  /**
   * Notifies that model finished operation {@link #beforeMessagesLoop()}.
   */
  public void afterMessagesLoop() {
  }
}
