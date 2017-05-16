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

import org.eclipse.wb.core.model.JavaInfo;

/**
 * Listener for {@link JavaInfo} events.
 *
 * Used to get notified before object of {@link JavaInfo} becomes set using
 * {@link JavaInfo#setObject(Object)}
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public interface JavaInfosetObjectBefore {
  /**
   * Invoked before object of {@link JavaInfo} becomes set using {@link JavaInfo#setObject(Object)}.
   */
  void invoke(JavaInfo target, Object[] objectRef) throws Exception;
}