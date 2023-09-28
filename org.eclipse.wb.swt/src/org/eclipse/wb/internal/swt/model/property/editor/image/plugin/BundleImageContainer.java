/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swt.model.property.editor.image.plugin;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IHasChildren;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageContainer;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageElement;

import org.eclipse.swt.graphics.Image;

import org.osgi.framework.Bundle;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Implementation {@link IImageContainer} for {@link Bundle} plugin.
 *
 * @author lobas_av
 * @coverage swt.property.editor.plugin
 */
public class BundleImageContainer extends ImageContainer implements IHasChildren {
	private static final String[] PATTERNS = {".gif", ".png", ".jpg", ".jpeg", ".bmp", ".ico"};
	private final String m_name;
	private final Bundle m_bundle;
	private final String m_symbolicName;
	private final String m_path;
	private IImageElement[] m_resources;
	private boolean m_calculateHasChildren = true;
	private boolean m_hasChildren;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public BundleImageContainer(String name, Bundle bundle, String symbolicName, String path) {
		m_name = name;
		m_bundle = bundle;
		m_symbolicName = symbolicName;
		m_path = path;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Elements
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Lazy loading resources for this container.
	 */
	private void ensureResources() {
		if (m_resources == null) {
			Enumeration<?> entryPaths = m_bundle.getEntryPaths(m_path);
			List<IImageElement> resources = new ArrayList<>();
			while (entryPaths.hasMoreElements()) {
				String entry = (String) entryPaths.nextElement();
				if (entry.endsWith("/")) {
					// add not empty container
					if (isContainsResources(m_bundle, entry)) {
						String entryName = entry.substring(0, entry.length() - 1);
						int lastSlashIndex = entryName.lastIndexOf('/');
						if (lastSlashIndex != -1) {
							entryName = entryName.substring(lastSlashIndex + 1);
						}
						resources.add(new BundleImageContainer(entryName, m_bundle, m_symbolicName, entry));
					}
				} else {
					// add image resource
					for (int i = 0; i < PATTERNS.length; i++) {
						if (entry.endsWith(PATTERNS[i])) {
							resources.add(new BundleImageResource(m_bundle.getEntry(entry), m_symbolicName));
							break;
						}
					}
				}
			}
			m_resources = resources.toArray(new IImageElement[resources.size()]);
		}
	}

	/**
	 * @return <code>true</code> if given container contains image resources and <code>false</code>
	 *         otherwise.
	 */
	private static boolean isContainsResources(Bundle bundle, String path) {
		Enumeration<?> entryPaths = bundle.getEntryPaths(path);
		List<String> folders = new ArrayList<>();
		// handle only file resources
		while (entryPaths.hasMoreElements()) {
			String entry = (String) entryPaths.nextElement();
			if (entry.endsWith("/")) {
				folders.add(entry);
			} else {
				for (int i = 0; i < PATTERNS.length; i++) {
					if (entry.endsWith(PATTERNS[i])) {
						return true;
					}
				}
			}
		}
		// handle child containers
		for (String entry : folders) {
			if (isContainsResources(bundle, entry)) {
				return true;
			}
		}
		return false;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IHasChildren
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean hasChildren() {
		try {
			if (m_calculateHasChildren) {
				m_calculateHasChildren = false;
				m_hasChildren = isContainsResources(m_bundle, m_path);
			}
			return m_hasChildren;
		} catch (Throwable e) {
		}
		return false;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IImageContainer
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public IImageElement[] elements() {
		ensureResources();
		return m_resources;
	}

	@Override
	protected IImageElement[] directElements() {
		return m_resources;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IImageElement
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Image getImage() {
		return DesignerPlugin.getImage("folder_open.gif");
	}

	@Override
	public String getName() {
		return m_name;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Internal
	//
	////////////////////////////////////////////////////////////////////////////
	protected final boolean findResource(List<Object> paths, String imagePath) {
		paths.add(this);
		for (IImageElement element : elements()) {
			if (element instanceof BundleImageContainer container) {
				if (container.findResource(paths, imagePath)) {
					return true;
				}
			} else if (element instanceof BundleImageResource resource) {
				if (resource.getPath().equals(imagePath)) {
					paths.add(resource);
					return true;
				}
			}
		}
		paths.remove(this);
		return false;
	}

	@Override
	public Object[] findResource(String symbolicName, String imagePath) {
		return null;
	}
}