/*******************************************************************************
 * Copyright (c) 2023 Patrick Ziegler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swt.model.jface.resource;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;

/**
 * Container for resource infos, direct child of root {@link JavaInfo}.
 */
public abstract class AbstractContainerInfo extends ObjectInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the existing or new {@link AbstractContainerInfo} for given root.
	 */
	public static <T extends AbstractContainerInfo> T get(JavaInfo root, T defaultValue) throws Exception {
		// try to find existing container
		@SuppressWarnings("unchecked")
		T container = findContainer(root, (Class<T>) defaultValue.getClass());
		if (container != null) {
			return container;
		}
		// add new container
		container = defaultValue;
		root.addChild(container);
		return container;
	}

	/**
	 * @return find the existing {@link AbstractContainerInfo} for given root.
	 */
	protected static <T extends AbstractContainerInfo> T findContainer(JavaInfo root, Class<T> clazz) {
		for (T child : root.getChildren(clazz)) {
			return child;
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Delete
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean canDelete() {
		return false;
	}

	@Override
	public void delete() throws Exception {
	}
}