/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.tests.gef;

/**
 * Interface for running UI actions.
 *
 * @author scheglov_ke
 * @deprecated Deprecated together with {@link UIContext}
 */
@Deprecated
public interface UIRunnable {
	/**
	 * Executes some actions in {@link UiContext}.
	 */
	void run(UiContext context) throws Exception;
}
