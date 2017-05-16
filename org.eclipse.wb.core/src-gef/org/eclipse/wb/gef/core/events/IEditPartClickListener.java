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
 * Interface for listening to click on {@link EditPart}.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public interface IEditPartClickListener {
  /**
   * Called when user does left-click on given {@link EditPart} (only click, i.e. down/up, without
   * drag) and part is selected (become selected, or already was selected before click).
   */
  void clickNotify(EditPart editPart);
}