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
package org.eclipse.wb.internal.core.model.generic;

/**
 * Interface of abstract container that can accept one or more children.
 *
 * @author scheglov_ke
 * @coverage core.model.generic
 */
public interface AbstractContainer {
  /**
   * @return <code>true</code> if given component can be added to container.
   */
  boolean validateComponent(Object component);
}
