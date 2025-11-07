/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.internal.swt.model.jface.resource;

import org.eclipse.wb.core.editor.constants.CoreImages;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.presentation.DefaultObjectPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.utils.check.Assert;
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
public final class RegistryContainerInfo extends AbstractContainerInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the existing or new {@link RegistryContainerInfo} for given root.
	 */
	public static RegistryContainerInfo get(JavaInfo root) throws Exception {
		return get(root, new RegistryContainerInfo());
	}

	/**
	 * @return all registries for given root assignable from given {@link Class}.
	 */
	public static <T extends ResourceRegistryInfo> List<T> getRegistries(JavaInfo root,
			Class<T> componentClass) throws Exception {
		RegistryContainerInfo container = findContainer(root, RegistryContainerInfo.class);
		if (container != null) {
			return container.getChildren(componentClass);
		}
		return Collections.emptyList();
	}

	/**
	 * @return {@link ResourceRegistryInfo} defined into given root represented given {@link ASTNode}.
	 */
	public static ResourceRegistryInfo getRegistry(JavaInfo root, ASTNode node) throws Exception {
		RegistryContainerInfo container = findContainer(root, RegistryContainerInfo.class);
		Assert.isNotNull(container);
		//
		for (ResourceRegistryInfo registry : container.getChildren(ResourceRegistryInfo.class)) {
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
			public ImageDescriptor getIcon() {
				return CoreImages.FOLDER_OPEN;
			}
		};
	}
}