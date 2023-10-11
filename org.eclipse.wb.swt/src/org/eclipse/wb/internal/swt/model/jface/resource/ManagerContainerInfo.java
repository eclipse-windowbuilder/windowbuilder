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
import org.eclipse.wb.internal.core.model.presentation.DefaultObjectPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.swt.Activator;
import org.eclipse.wb.internal.swt.model.ModelMessages;

import org.eclipse.jface.resource.ImageDescriptor;

import java.util.Collections;
import java.util.List;

/**
 * Container for {@link ResourceManagerInfo}, direct child of root
 * {@link JavaInfo}.
 */
public final class ManagerContainerInfo extends AbstractContainerInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the existing or new {@link ManagerContainerInfo} for given root.
	 */
	public static ManagerContainerInfo get(JavaInfo root) throws Exception {
		return get(root, new ManagerContainerInfo());
	}

	/**
	 * @return all registries for given root assignable from given {@link Class}.
	 */
	public static <T extends ResourceManagerInfo> List<T> getManagers(JavaInfo root, Class<T> componentClass)
			throws Exception {
		ManagerContainerInfo container = findContainer(root, ManagerContainerInfo.class);
		if (container != null) {
			return container.getChildren(componentClass);
		}
		return Collections.emptyList();
	}

	/**
	 * Returns the {@link ResourceManagerInfo} attached to the given root. If none
	 * exists, a new {@link LocalResourceManagerInfo} object info is created. If
	 * multiple exist, the first one is returned.
	 *
	 * @param root the {@link JavaInfo} of the root object.
	 * @return the {@link ResourceManagerInfo} bound to {@code root}.
	 */
	public static ResourceManagerInfo getResourceManagerInfo(JavaInfo root) throws Exception {
		List<ResourceManagerInfo> resourceManagers = getManagers(root, ResourceManagerInfo.class);
		if (resourceManagers.isEmpty()) {
			return LocalResourceManagerInfo.createNew(root);
		}
		return resourceManagers.get(0);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public IObjectPresentation getPresentation() {
		return new DefaultObjectPresentation(this) {
			@Override
			public String getText() throws Exception {
				return ModelMessages.ManagerContainerInfo_jfaceManagers;
			}

			@Override
			public ImageDescriptor getIcon() throws Exception {
				return Activator.getImageDescriptor("components/manager_container.png");
			}
		};
	}
}