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
package org.eclipse.wb.internal.rcp.model.rcp.perspective;

/**
 * Model for object that can be rendered from {@link PageLayoutAddCreationSupport}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
public interface IRenderableInfo {
	/**
	 * Renders this {@link IRenderableInfo} by creating some {@link Object}.
	 *
	 * @return the {@link Object} that represents this {@link IRenderableInfo}.
	 */
	Object render() throws Exception;
}
