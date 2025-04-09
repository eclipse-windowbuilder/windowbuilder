/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.core.gef.command;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;

import org.eclipse.gef.commands.CompoundCommand;

/**
 * Implementation of {@link CompoundCommand} for editing {@link ObjectInfo}.
 *
 * @author scheglov_ke
 * @coverage core.gef
 */
public final class CompoundEditCommand extends CompoundCommand {
	private final ObjectInfo m_object;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public CompoundEditCommand(ObjectInfo object) {
		Assert.isNotNull(object);
		m_object = object;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Command
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final void execute() {
		ExecutionUtils.run(m_object, () -> CompoundEditCommand.super.execute());
	}
}
