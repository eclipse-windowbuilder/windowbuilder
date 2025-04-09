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
 * Interface of typical container with single child.
 *
 * @author scheglov_ke
 * @coverage core.model.generic
 */
public interface SimpleContainer extends AbstractContainer {
	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return <code>true</code> if there are no existing child.
	 */
	boolean isEmpty();

	/**
	 * @return the existing child, may be <code>null</code>.
	 */
	Object getChild();

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	void command_CREATE(Object newObject) throws Exception;

	void command_ADD(Object moveObject) throws Exception;
}
