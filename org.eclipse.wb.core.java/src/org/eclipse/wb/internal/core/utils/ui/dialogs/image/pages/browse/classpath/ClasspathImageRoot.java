/*******************************************************************************
 * Copyright (c) 2011, 2022 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *    Marcel du Preez - Addition of preference to specify a specific classpath for icon packs
 *******************************************************************************/
package org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.classpath;

import org.eclipse.wb.core.editor.constants.IEditorPreferenceConstants;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageContainer;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageElement;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageRoot;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link IImageRoot} for browsing classpath.
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
public final class ClasspathImageRoot implements IImageRoot {
	private final IJavaProject m_project;
	private final IClasspathImageContainer m_containers[];

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ClasspathImageRoot(String id, IJavaProject project) {
		m_project = project;
		//
		List<IImageContainer> containers = new ArrayList<>();
		try {
			IPackageFragmentRoot[] roots = m_project.getAllPackageFragmentRoots();
			for (int i = 0; i < roots.length; i++) {
				IPackageFragmentRoot root = roots[i];
				try {
					if (root.isArchive()) {
						JarImageContainer jarContainer = new JarImageContainer(id, root);
						String iconClasspaths = InstanceScope.INSTANCE.getNode(
								IEditorPreferenceConstants.WB_BASIC_UI_PREFERENCE_NODE).get(
										IEditorPreferenceConstants.WB_CLASSPATH_ICONS,
										null);
						if (iconClasspaths == null || jarContainer.getName().equals(iconClasspaths)) {
							if (!jarContainer.isEmpty()) {
								containers.add(jarContainer);
							}
						}
					} else {
						SrcImageContainer srcContainer = new SrcImageContainer(id, root);
						if (!srcContainer.isEmpty()) {
							containers.add(srcContainer);
						}
					}
				} catch (Throwable e) {
				}
			}
		} catch (Throwable e) {
		}
		m_containers = containers.toArray(new IClasspathImageContainer[containers.size()]);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IImageRoot
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void dispose() {
		for (IClasspathImageContainer container : m_containers) {
			container.dispose();
		}
	}

	@Override
	public IClasspathImageContainer[] elements() {
		return m_containers;
	}

	@Override
	public Object[] getSelectionPath(Object data) {
		if (data instanceof String path) {
			if (!path.startsWith("/")) {
				return null;
			}
			path = path.substring(1);
			// prepare package and resource names
			IPath pathObject = new Path(path);
			String packageName = pathObject.removeLastSegments(1).toPortableString().replace('/', '.');
			String resourceName = pathObject.lastSegment();
			// try each root container
			for (IClasspathImageContainer rootContainer : m_containers) {
				// try each package container
				IImageContainer[] packageContainers = rootContainer.elements();
				for (IImageContainer packageContainer : packageContainers) {
					if (packageContainer.getName().equals(packageName)) {
						// try each resource in package
						IImageElement[] elements = packageContainer.elements();
						for (IImageElement element : elements) {
							if (element.getName().equals(resourceName)) {
								return new Object[]{rootContainer, packageContainer, element};
							}
						}
					}
				}
			}
		}
		// don't know
		return null;
	}
}
