/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swt.model.jface.resource;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.presentation.DefaultObjectPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.swt.Activator;
import org.eclipse.wb.internal.swt.model.ModelMessages;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jface.resource.ImageDescriptor;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

/**
 * Container for {@link ResourceRegistryInfo}, direct child of root {@link JavaInfo}.
 *
 * @author lobas_av
 * @coverage swt.model.jface
 */
public final class RegistryContainerInfo extends ObjectInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the existing or new {@link RegistryContainerInfo} for given root.
	 */
	public static RegistryContainerInfo get(JavaInfo root) throws Exception {
		// try to find existing container
		RegistryContainerInfo container = findContainer(root);
		if (container != null) {
			return container;
		}
		// add new container
		container = new RegistryContainerInfo();
		root.addChild(container);
		return container;
	}

	/**
	 * @return all registries for given root assignable from given {@link Class}.
	 */
	public static <T extends ResourceRegistryInfo> List<T> getRegistries(JavaInfo root,
			Class<T> componentClass) throws Exception {
		RegistryContainerInfo container = findContainer(root);
		if (container != null) {
			return container.getChildren(componentClass);
		}
		return Collections.emptyList();
	}

	/**
	 * @return {@link ResourceRegistryInfo} defined into given root represented given {@link ASTNode}.
	 */
	public static ResourceRegistryInfo getRegistry(JavaInfo root, ASTNode node) throws Exception {
		RegistryContainerInfo container = findContainer(root);
		Assert.isNotNull(container);
		//
		for (ResourceRegistryInfo registry : container.getRegistryChildren()) {
			if (registry.isRepresentedBy(node)) {
				return registry;
			}
		}
		//
		Assert.fail(MessageFormat.format(
				ModelMessages.RegistryContainerInfo_unknownRegistry,
				root,
				node));
		return null;
	}

	/**
	 * @return find the existing {@link RegistryContainerInfo} for given root.
	 */
	private static RegistryContainerInfo findContainer(JavaInfo root) {
		for (ObjectInfo child : root.getChildren()) {
			if (child instanceof RegistryContainerInfo) {
				return (RegistryContainerInfo) child;
			}
		}
		return null;
	}

	/**
	 * @return the list of {@link ResourceRegistryInfo} children.
	 */
	public List<ResourceRegistryInfo> getRegistryChildren() {
		return getChildren(ResourceRegistryInfo.class);
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
				return ModelMessages.RegistryContainerInfo_jfaceRegistries;
			}

			@Override
			public ImageDescriptor getIcon() throws Exception {
				return Activator.getImageDescriptor("components/registry_container.gif");
			}
		};
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