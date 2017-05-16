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
package org.eclipse.wb.core.model;

/**
 * Interface for operations with size of top level {@link IAbstractComponentInfo}.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public interface ITopBoundsSupport {
  /**
   * Sets new size of component.
   */
  void setSize(int width, int height) throws Exception;
}
