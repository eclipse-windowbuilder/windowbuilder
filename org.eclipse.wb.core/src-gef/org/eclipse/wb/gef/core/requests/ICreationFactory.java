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
package org.eclipse.wb.gef.core.requests;

import org.eclipse.wb.gef.core.tools.CreationTool;

/**
 * A factory used to create new objects.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public interface ICreationFactory {
	/**
	 * Activates this factory, during {@link CreationTool} activation (including reloading). This
	 * allows factory do any operations that are too expensive to perform them in
	 * {@link #getNewObject()}.
	 *
	 * If any exception thrown, then {@link CreationTool} will be unloaded.
	 */
	void activate() throws Exception;

	/**
	 * @return the new object.
	 */
	Object getNewObject();
}