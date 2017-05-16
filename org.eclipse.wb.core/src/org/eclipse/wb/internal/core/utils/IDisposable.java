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
 * Interface for classes which needs to release internal resources.
 *
 * @author lobas_av
 * @author mitin_aa
 * @coverage core.model
 */
public interface IDisposable {
  /**
   * Disposes the object.<br>
   * The implementors are responsible to check this object instance for to be already disposed.
   */
  void dispose();
}