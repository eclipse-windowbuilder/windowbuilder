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
package org.eclipse.wb.gef.core.events;

import org.eclipse.wb.gef.core.EditPart;

/**
 * The listener interface for receiving basic events from an {@link EditPart}.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public interface IEditPartListener {
  /**
   * Called after a child {@link EditPart} has been added to its parent.
   */
  void childAdded(EditPart child, int index);

  /**
   * Called before a child {@link EditPart} is removed from its parent.
   */
  void removingChild(EditPart child, int index);
}