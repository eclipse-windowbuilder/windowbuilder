/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
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