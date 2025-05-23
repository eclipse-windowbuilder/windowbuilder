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
package org.eclipse.wb.internal.swing.laf.command;

import org.eclipse.wb.internal.swing.laf.LafSupport;
import org.eclipse.wb.internal.swing.laf.model.LafInfo;

import org.xml.sax.Attributes;

import java.util.Iterator;
import java.util.List;

/**
 * Implementation of {@link Command} that edits just a name of LAF.
 *
 * @author mitin_aa
 * @coverage swing.laf.model
 */
public class EditNameCommand extends LookAndFeelCommand {
	// constants
	public static final String ID = "edit-name";

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public EditNameCommand(String id, String name) {
		super(id, name);
	}

	public EditNameCommand(Attributes attributes) {
		super(attributes);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Execution
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void execute() {
		LafInfo lafInfo = LafSupport.getLookAndFeel(m_id);
		if (lafInfo != null) {
			lafInfo.setName(m_name);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void addToCommandList(List<Command> commands) {
		// remove other edit commands for this
		for (Iterator<Command> I = commands.iterator(); I.hasNext();) {
			Command command = I.next();
			if (command instanceof EditNameCommand editCommand) {
				if (editCommand.m_id.equals(m_id)) {
					I.remove();
				}
			}
		}
		// do add
		commands.add(this);
	}
}
