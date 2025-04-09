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

import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.AbstractBrowseImagePage;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageContainer;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageElement;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.graphics.Image;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation od {@link IImageContainer} for {@link IPackageFragment}.
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
final class SrcPackageImageContainer implements IImageContainer {
	private final IPackageFragment m_packageFragment;
	private final SrcImageResource[] m_resources;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SrcPackageImageContainer(String id,
			IPackageFragmentRoot packageFragmentRoot,
			IPackageFragment packageFragment) throws Exception {
		m_packageFragment = packageFragment;
		//
		List<SrcImageResource> resources = new ArrayList<>();
		{
			Object[] nonJavaResources = m_packageFragment.getNonJavaResources();
			for (Object nonJavaResource : nonJavaResources) {
				if (nonJavaResource instanceof IFile resource) {
					String extension = resource.getLocation().getFileExtension();
					if (AbstractBrowseImagePage.isImageExtension(extension)) {
						SrcImageResource imageResource =
								new SrcImageResource(id, packageFragmentRoot, resource);
						resources.add(imageResource);
					}
				}
			}
		}
		m_resources = resources.toArray(new SrcImageResource[resources.size()]);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IImageElement
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Image getImage() {
		return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PACKAGE);
	}

	@Override
	public String getName() {
		return m_packageFragment.getElementName();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IImageContainer
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public IImageElement[] elements() {
		return m_resources;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Internal access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return <code>true</code> if this jar does not have any images.
	 */
	boolean isEmpty() {
		return m_resources.length == 0;
	}

	/**
	 * Disposes any allocated resources.
	 */
	void dispose() {
		for (SrcImageResource resource : m_resources) {
			resource.dispose();
		}
	}
}
