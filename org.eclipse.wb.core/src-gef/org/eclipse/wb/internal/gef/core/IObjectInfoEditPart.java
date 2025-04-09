/*******************************************************************************
 * Copyright (c) 2024 Patrick Ziegler and others.
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
package org.eclipse.wb.internal.gef.core;

import org.eclipse.wb.core.model.IObjectInfo;

import org.eclipse.gef.commands.CompoundCommand;

/**
 * Marker interface to indicate that the compound command is editing an
 * {@link IObjectInfo}.
 */
public interface IObjectInfoEditPart {
	CompoundCommand createCompoundCommand();
}
