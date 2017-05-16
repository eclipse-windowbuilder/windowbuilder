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
package org.eclipse.wb.internal.core.utils;

/**
 * An generic interface for an adaptable object.
 * <p>
 * Adaptable objects can be dynamically extended to provide different interfaces (or "adapters").
 * Adapters are created by adapter factories, which are in turn managed by type by adapter managers.
 * </p>
 * For example,
 *
 * <pre>
 *     IAdaptable a = [some adaptable];
 *     IFoo x = a.getAdapter(IFoo.class);
 *     if (x != null)
 *         [do IFoo things with x]
 * </pre>
 *
 * @author mitin_aa
 * @coverage core.util
 */
public interface IAdaptable {
  /**
   * @param adapter
   *          the type of adapter.
   *
   * @return the adapter of required type.
   */
  <T> T getAdapter(Class<T> adapter);
}
