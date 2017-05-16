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

/**
 * Listener for {@link ObjectInfo} events.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public interface ObjectInfoChildAddBefore {
  /**
   * Before adding child to parent.
   *
   * @param parent
   *          the parent to add to.
   * @param child
   *          the child to add.
   * @param nextChild
   *          the array with single element - existing child of <code>parent</code> or
   *          <code>null</code>. During this event it is possible that we perform some manipulation
   *          with existing children of <code>parent</code>, so initial <code>nextChild</code> may
   *          become invalid (may be we remove it). So, we should put alternative "nextChild".
   */
  void invoke(ObjectInfo parent, ObjectInfo child, ObjectInfo[] nextChild) throws Exception;
}
