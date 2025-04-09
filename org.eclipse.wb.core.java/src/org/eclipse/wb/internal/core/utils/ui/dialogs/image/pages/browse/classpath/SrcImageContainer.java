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
package org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.classpath;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageContainer;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.swt.graphics.Image;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation od {@link IImageContainer} for {@link IPackageFragmentRoot}.
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
final class SrcImageContainer implements IImageContainer, IClasspathImageContainer {
	private final IPackageFragmentRoot m_packageFragmentRoot;
	private final SrcPackageImageContainer[] m_packageContainers;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SrcImageContainer(String id, IPackageFragmentRoot packageFragmentRoot) throws Exception {
		m_packageFragmentRoot = packageFragmentRoot;
		//
		List<SrcPackageImageContainer> packageContainers = new ArrayList<>();
		{
			IJavaElement[] children = m_packageFragmentRoot.getChildren();
			for (IJavaElement child : children) {
				if (child instanceof IPackageFragment packageFragment) {
					SrcPackageImageContainer container =
							new SrcPackageImageContainer(id, packageFragmentRoot, packageFragment);
					if (!container.isEmpty()) {
						packageContainers.add(container);
					}
				}
			}
		}
		m_packageContainers =
				packageContainers.toArray(new SrcPackageImageContainer[packageContainers.size()]);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IImageElement
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Image getImage() {
		return DesignerPlugin.getImage("folder_package.gif");
	}

	@Override
	public String getName() {
		return m_packageFragmentRoot.getElementName();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IImageContainer
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public IImageContainer[] elements() {
		return m_packageContainers;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Internal access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean isEmpty() {
		return m_packageContainers.length == 0;
	}

	@Override
	public void dispose() {
		for (SrcPackageImageContainer container : m_packageContainers) {
			container.dispose();
		}
	}
}
