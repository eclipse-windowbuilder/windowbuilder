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
package org.eclipse.wb.internal.swt.model.util.surround;

import org.eclipse.wb.internal.core.model.util.surround.ISurroundTarget;

import org.eclipse.swt.widgets.Composite;

/**
 * {@link ISurroundTarget} for using {@link Composite} as target container.
 *
 * @author scheglov_ke
 * @coverage swt.model.util
 */
public final class GroupSurroundTarget extends AbstractCompositeSurroundTarget {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final Object INSTANCE = new GroupSurroundTarget();

	private GroupSurroundTarget() {
		super("org.eclipse.swt.widgets.Group");
	}
}
