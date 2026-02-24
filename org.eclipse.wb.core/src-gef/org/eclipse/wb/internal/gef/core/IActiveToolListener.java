/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
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
package org.eclipse.wb.internal.gef.core;

import org.eclipse.gef.Tool;

/**
 * Listener for {@link Tool} activation in {@link EditDomain}.
 *
 * @author scheglov_ke
 * @coverage gef.core
 */
public interface IActiveToolListener {
	/**
	 * Notifies that new {@link Tool} was activated.
	 */
	void toolActivated(Tool tool);
}
