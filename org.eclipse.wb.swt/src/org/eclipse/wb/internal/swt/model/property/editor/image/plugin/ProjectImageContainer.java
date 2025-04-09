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
package org.eclipse.wb.internal.swt.model.property.editor.image.plugin;

import org.eclipse.wb.internal.core.DesignerPlugin;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.graphics.Image;

import java.util.ArrayList;
import java.util.List;

/**
 * Root container for file image resources into workspace plugin project.
 *
 * @author lobas_av
 * @coverage swt.property.editor.plugin
 */
public final class ProjectImageContainer extends FileImageContainer {
	private final String m_symbolicName;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ProjectImageContainer(IProject project, String symbolicName) {
		super(project, symbolicName);
		m_symbolicName = symbolicName;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IImageElement
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final Image getImage() {
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