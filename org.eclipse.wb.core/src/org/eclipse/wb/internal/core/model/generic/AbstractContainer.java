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
 * Interface of abstract container that can accept one or more children.
 *
 * @author scheglov_ke
 * @coverage core.model.generic
 */
public interface AbstractContainer {
	/**
	 * @return <code>true</code> if given component can be added to container.
	 */
	boolean validateComponent(Object component);
}
