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

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.presentation.DefaultObjectPresentation;

/**
 * Listener for {@link ObjectInfo} events.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public interface ObjectInfoChildTree {
  /**
   * This method is invoked from {@link DefaultObjectPresentation#getChildrenTree()} to check if
   * given {@link ObjectInfo} can be displayed in components tree.
   *
   * @param object
   *          the {@link ObjectInfo} to check.
   * @param visible
   *          the array with single boolean flag, with initial <code>true</code> value, any can
   *          listener set it to <code>false</code>.
   */
  void invoke(ObjectInfo object, boolean[] visible) throws Exception;
}
