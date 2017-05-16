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
package org.eclipse.wb.internal.core.model.order;

/**
 * Default implementation of {@link MethodOrder} - put component in requested position, without
 * changes.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class ComponentOrderDefault extends ComponentOrder {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final ComponentOrder INSTANCE = new ComponentOrderDefault();

  private ComponentOrderDefault() {
  }
}
