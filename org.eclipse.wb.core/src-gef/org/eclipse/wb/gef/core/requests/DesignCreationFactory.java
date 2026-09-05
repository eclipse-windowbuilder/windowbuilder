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
package org.eclipse.wb.gef.core.requests;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.gef.core.tools.CreationTool;

import org.eclipse.gef.requests.CreationFactory;

/**
 * A factory used to create new {@link ObjectInfo} objects.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public abstract class DesignCreationFactory implements CreationFactory {

	/**
	 * Activates this factory, during {@link CreationTool} activation (including reloading). This
	 * allows factory do any operations that are too expensive to perform them in
	 * {@link #getNewObject()}.
	 *
	 * If any exception thrown, then {@link CreationTool} will be unloaded.
	 */
	public abstract void activate() throws Exception;

	@Override
	public abstract ObjectInfo getNewObject();

	@Override
	public Object getObjectType() {
		return ObjectInfo.class;
	}
}