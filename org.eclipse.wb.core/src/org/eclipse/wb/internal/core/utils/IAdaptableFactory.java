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
 * External implementation of {@link IAdaptable}.
 *
 * @author scheglov_ke
 * @coverage core.util
 */
public interface IAdaptableFactory {
  /**
   * @param object
   *          the {@link Object} to adapt.
   * @param adapter
   *          the type of adapter.
   *
   * @return the adapter of required type.
   */
  <T> T getAdapter(Object object, Class<T> adapter);
}
