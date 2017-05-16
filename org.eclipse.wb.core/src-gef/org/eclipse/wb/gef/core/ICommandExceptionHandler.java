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
package org.eclipse.wb.gef.core;

/**
 * {@link ICommandExceptionHandler} allows centralized exceptions handling for all executable
 * {@link Command}'s.
 *
 * @author scheglov_ke
 * @coverage gef.core
 */
public interface ICommandExceptionHandler {
  /**
   * Notifies that exception was happened.
   */
  void handleException(Throwable exception);
}