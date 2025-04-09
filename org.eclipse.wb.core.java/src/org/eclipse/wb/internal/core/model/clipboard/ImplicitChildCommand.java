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
package org.eclipse.wb.internal.core.model.clipboard;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.creation.IImplicitCreationSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link ClipboardCommand} for applying commands for implicit/exposed components.
 *
 * @author scheglov_ke
 * @coverage core.model.clipboard
 */
final class ImplicitChildCommand extends ClipboardCommand {
	private static final long serialVersionUID = 0L;
	private final IClipboardImplicitCreationSupport m_implicitCreation;
	private final List<ClipboardCommand> m_commands = new ArrayList<>();

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ImplicitChildCommand(JavaInfo child) throws Exception {
		m_implicitCreation =
				((IImplicitCreationSupport) child.getCreationSupport()).getImplicitClipboard();
		if (m_implicitCreation != null) {
			JavaInfoMemento.cleanUpAnonymous(m_implicitCreation);
			JavaInfoMemento.addCommands(child, m_commands);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Command
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void execute(JavaInfo host) throws Exception {
		if (m_implicitCreation != null) {
			JavaInfo child = m_implicitCreation.find(host);
			for (ClipboardCommand command : m_commands) {
				command.execute(child);
			}
		}
	}
}
