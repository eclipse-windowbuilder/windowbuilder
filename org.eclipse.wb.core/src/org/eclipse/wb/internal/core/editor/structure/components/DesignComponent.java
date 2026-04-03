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
package org.eclipse.wb.internal.core.editor.structure.components;

import org.eclipse.wb.core.model.ObjectInfo;

import org.eclipse.gef.TreeEditPart;
import org.eclipse.swt.widgets.Tree;

/**
 * Virtual root of the {@link TreeViewer}. The {@link TreeEditPart} of this
 * object has the {@link Tree} as its widget and is not rendered. It is the
 * parent of the root {@link ObjectInfo}.
 */
public record DesignComponent(ObjectInfo objectInfo) {

}
