/*******************************************************************************
 * Copyright (c) 2026 Patrick Ziegler and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.model;

import org.eclipse.wb.core.model.ObjectInfo;

import org.eclipse.gef.ui.parts.TreeViewer;

/**
 * Container for an {@link ObjectInfo} to be used as input for the
 * {@link TreeViewer}. This container is not shown.
 */
public record ObjectInfoContainer(ObjectInfo objectInfo) {
	// Accessors created automatically by Java
}
