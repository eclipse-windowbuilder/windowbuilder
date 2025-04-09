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
package org.eclipse.wb.internal.core.utils.state;

/**
 * Helper for reorder operation.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public interface IOrderProcessor {
	/**
	 * Moves component on its current container before "nextComponent".
	 */
	void move(Object component, Object nextComponent) throws Exception;
}
