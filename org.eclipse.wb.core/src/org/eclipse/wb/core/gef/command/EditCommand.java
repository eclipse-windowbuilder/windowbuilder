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

import org.eclipse.wb.core.model.IObjectInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;

import org.eclipse.gef.commands.Command;

/**
 * Implementation of {@link Command} for editing {@link ObjectInfo}.
 *
 * @author scheglov_ke
 * @coverage core.gef
 */
public abstract class EditCommand extends Command {
	private final ObjectInfo m_object;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public EditCommand(ObjectInfo object) {
		Assert.isNotNull(object);
		m_object = object;
	}

	public EditCommand(IObjectInfo object) {
		this(object.getUnderlyingModel());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Command
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final void execute() {
		ExecutionUtils.run(m_object, () -> executeEdit());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// EditCommand
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Does some editing in start/commit/endEdit cycle.
	 */
	protected abstract void executeEdit() throws Exception;
}
