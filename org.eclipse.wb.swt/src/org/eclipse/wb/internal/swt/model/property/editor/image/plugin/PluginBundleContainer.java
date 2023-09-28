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
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageContainer;

import org.eclipse.swt.graphics.Image;

import org.osgi.framework.Bundle;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation root {@link IImageContainer} for {@link Bundle} plugin.
 *
 * @author lobas_av
 * @coverage swt.property.editor.plugin
 */
public final class PluginBundleContainer extends BundleImageContainer {
	private final String m_symbolicName;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PluginBundleContainer(String name, Bundle bundle, String symbolicName) {
		super(name, bundle, symbolicName, "/");
		m_symbolicName = symbolicName;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IImageElement
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Image getImage() {
		return DesignerPlugin.getImage("project_open.gif");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Internal
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Object[] findResource(String symbolicName, String imagePath) {
		if (m_symbolicName.equals(symbolicName)) {
			List<Object> paths = new ArrayList<>();
			if (findResource(paths, imagePath)) {
				return paths.toArray();
			}
		}
		return null;
	}
}