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
package org.eclipse.wb.internal.core.model.presentation;

import org.eclipse.wb.core.model.ObjectInfo;

import org.eclipse.jface.resource.ImageDescriptor;

import java.util.List;

/**
 * Interface for visual presentation of {@link ObjectInfo} - title, icon, etc.
 *
 * @author scheglov_ke
 * @coverage core.model.presentation
 */
public interface IObjectPresentation {
	/**
	 * @return <code>true</code> if object should be displayed for user. For example we can display
	 *         some containers only if there is at least one child.
	 */
	boolean isVisible();

	/**
	 * @return the text to display for user. This can be some static text, variable name, etc.
	 */
	String getText() throws Exception;

	/**
	 * @return the icon to display for user.
	 */
	ImageDescriptor getIcon() throws Exception;

	/**
	 * @return the list of {@link ObjectInfo} children to display for user in components tree. This
	 *         list can be different than {@link ObjectInfo#getChildren()}.
	 */
	List<ObjectInfo> getChildrenTree() throws Exception;

	/**
	 * @return the list of {@link ObjectInfo} children to display for user on design canvas. This list
	 *         can be different than {@link ObjectInfo#getChildren()}.
	 */
	List<ObjectInfo> getChildrenGraphical() throws Exception;
}
