/*******************************************************************************
 * Copyright (c) 2014 Google, Inc.
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

/**
 * Optional interface of {@link ObjectInfo} for models which have position in source.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public interface HasSourcePosition {
	/**
	 * @return the position of this component in source.
	 */
	int getSourcePosition();
}
