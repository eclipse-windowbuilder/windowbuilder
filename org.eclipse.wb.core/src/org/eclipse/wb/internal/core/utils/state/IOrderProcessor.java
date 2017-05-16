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
package org.eclipse.wb.internal.core.utils.state;

/**
 * Helper for reorder operation.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public interface IOrderProcessor {
  /**
   * Moves component on its current container before "nextComponent".
   */
  void move(Object component, Object nextComponent) throws Exception;
}
