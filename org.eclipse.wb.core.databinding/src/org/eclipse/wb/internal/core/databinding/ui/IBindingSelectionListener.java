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
package org.eclipse.wb.internal.core.databinding.ui;

import org.eclipse.wb.internal.core.databinding.model.IBindingInfo;

/**
 * A listener which is notified when {@link IBindingInfo} viewer's selection changes.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
interface IBindingSelectionListener {
  /**
   * Notifies that the selection has changed.
   */
  void selectionChanged(IBindingInfo binding);
}