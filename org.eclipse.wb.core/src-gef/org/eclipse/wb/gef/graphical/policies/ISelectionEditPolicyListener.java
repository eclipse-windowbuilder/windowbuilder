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
package org.eclipse.wb.gef.graphical.policies;

/**
 * Listener for {@link SelectionEditPolicy}.
 *
 * @author scheglov_ke
 * @coverage gef.graphical
 */
public interface ISelectionEditPolicyListener {
  /**
   * Notifies that {@link SelectionEditPolicy#showSelection()} was executed.
   */
  void showSelection(SelectionEditPolicy policy);

  /**
   * Notifies that {@link SelectionEditPolicy#hideSelection()} was executed.
   */
  void hideSelection(SelectionEditPolicy policy);
}