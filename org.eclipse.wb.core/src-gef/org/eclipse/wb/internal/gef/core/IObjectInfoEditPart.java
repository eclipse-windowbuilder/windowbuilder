/*******************************************************************************
 * Copyright (c) 2024 Patrick Ziegler and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
