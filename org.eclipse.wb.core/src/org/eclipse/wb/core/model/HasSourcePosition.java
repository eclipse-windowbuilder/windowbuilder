/*******************************************************************************
 * Copyright (c) 2014 Google, Inc.
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
 * Optional interface of {@link ObjectInfo} for models which have position in source.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public interface HasSourcePosition {
  /**
   * @return the position of this component in source.
   */
  int getSourcePosition();
}
