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
package org.eclipse.wb.gef.core.policies;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;

/**
 * The listener interface for receiving decorate/undecorate events from {@link LayoutEditPolicy}.
 *
 * @author scheglov_ke
 * @coverage gef.core
 */
public interface IEditPartDecorationListener {
  /**
   * Notifies that {@link EditPart} should be decorated.
   */
  void decorate(EditPart child);

  /**
   * Notifies that {@link EditPart} should be undecorated.
   */
  void undecorate(EditPart child);
}