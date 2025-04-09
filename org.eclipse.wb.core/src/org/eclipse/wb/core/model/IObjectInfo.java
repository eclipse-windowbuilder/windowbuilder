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
package org.eclipse.wb.core.model;

import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.property.Property;

/**
 * Interface model of any {@link Object}.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public interface IObjectInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the underlying {@link ObjectInfo} model.
	 */
	ObjectInfo getUnderlyingModel();

	/**
	 * @return <code>true</code> if this object is in process of deleting.
	 */
	boolean isDeleting();

	/**
	 * @return <code>true</code> if this object was deleted from its parent.
	 */
	boolean isDeleted();

	/**
	 * @return the parent of this object
	 */
	ObjectInfo getParent();

	////////////////////////////////////////////////////////////////////////////
	//
	// Broadcasting
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds new listener with type of superclass.
	 * <p>
	 * Given listener is bound to this instance of {@link IObjectInfo}, so if {@link IObjectInfo} will
	 * be removed from its parent, after next refresh() listener also will be removed. We should do
	 * this to prevent memory leaks.
	 */
	public void addBroadcastListener(Object listenerImpl);

	////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link Property} with given title or <code>null</code> if no such {@link Property}
	 *         found.
	 */
	Property getPropertyByTitle(String title) throws Exception;

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return {@link IObjectPresentation} for visual presentation of this object.
	 */
	IObjectPresentation getPresentation();

	////////////////////////////////////////////////////////////////////////////
	//
	// Delete
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Deletes this {@link IObjectInfo} from its parent.
	 */
	void delete() throws Exception;
}
