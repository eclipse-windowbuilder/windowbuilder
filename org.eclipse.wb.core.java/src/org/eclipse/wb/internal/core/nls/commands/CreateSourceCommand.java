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
package org.eclipse.wb.internal.core.nls.commands;

import org.eclipse.wb.internal.core.nls.SourceDescription;
import org.eclipse.wb.internal.core.nls.edit.IEditableSource;

/**
 * Command for creating new strings source.
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public final class CreateSourceCommand extends AbstractCommand {
	private final SourceDescription m_sourceDescription;
	private final Object m_parameters;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public CreateSourceCommand(IEditableSource editableSource,
			SourceDescription sourceDescription,
			Object parameters) {
		super(editableSource);
		m_sourceDescription = sourceDescription;
		m_parameters = parameters;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public SourceDescription getSourceDescription() {
		return m_sourceDescription;
	}

	public Object getParameters() {
		return m_parameters;
	}
}
