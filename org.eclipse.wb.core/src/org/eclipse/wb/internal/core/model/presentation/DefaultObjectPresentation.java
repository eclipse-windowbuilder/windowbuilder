/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.internal.core.model.presentation;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildGraphical;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildTree;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildrenGraphical;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildrenTree;

import org.eclipse.jface.resource.ImageDescriptor;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of {@link IObjectPresentation}.
 *
 * @author scheglov_ke
 * @coverage core.model.presentation
 */
public abstract class DefaultObjectPresentation implements IObjectPresentation {
	private final ObjectInfo m_object;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DefaultObjectPresentation(ObjectInfo object) {
		m_object = object;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IObjectPresentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public List<ObjectInfo> getChildrenTree() throws Exception {
		List<ObjectInfo> children = new ArrayList<>();
		for (ObjectInfo child : m_object.getChildren()) {
			// ask listeners if child should be displayed
			boolean[] visible = new boolean[]{true};
			visible[0] &= child.getPresentation().isVisible();
			m_object.getBroadcast(ObjectInfoChildTree.class).invoke(child, visible);
			// check if we can add this child
			if (visible[0]) {
				children.add(child);
			}
		}
		m_object.getBroadcast(ObjectInfoChildrenTree.class).invoke(m_object, children);
		return children;
	}

	@Override
	public List<ObjectInfo> getChildrenGraphical() throws Exception {
		List<ObjectInfo> children = new ArrayList<>();
		for (ObjectInfo child : m_object.getChildren()) {
			// ask listeners if child should be displayed
			boolean[] visible = new boolean[]{true};
			visible[0] &= child.getPresentation().isVisible();
			m_object.getBroadcast(ObjectInfoChildGraphical.class).invoke(child, visible);
			// check if we can add this child
			if (visible[0]) {
				children.add(child);
			}
		}
		m_object.getBroadcast(ObjectInfoChildrenGraphical.class).invoke(children);
		return children;
	}

	@Override
	public ImageDescriptor getIcon() throws Exception {
		return null;
	}

	@Override
	public boolean isVisible() throws Exception {
		return true;
	}
}
