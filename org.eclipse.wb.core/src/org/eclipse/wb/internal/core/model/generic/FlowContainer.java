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
package org.eclipse.wb.internal.core.model.generic;


/**
 * Interface of typical flow based container.
 *
 * @author scheglov_ke
 * @coverage core.model.generic
 */
public interface FlowContainer extends AbstractContainer {
	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return <code>true</code> if this container is horizontal.
	 */
	boolean isHorizontal();

	/**
	 * @return <code>true</code> if this container has RTL orientation.
	 */
	boolean isRtl();

	/**
	 * @return <code>true</code> if given existing child of container can be used as reference.
	 */
	boolean validateReference(Object reference);

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates new component on this container.
	 */
	void command_CREATE(Object newObject, Object referenceObject) throws Exception;

	/**
	 * Moves existing component on this container, internally or from other container.
	 */
	void command_MOVE(Object moveObject, Object referenceObject) throws Exception;
}
