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
package org.eclipse.wb.internal.core.utils.binding.editors.controls;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;

/**
 * Default manager for installing/unistalling global handlers for {@link Control} actions commands.
 *
 * @author sablin_aa
 */
public class DefaultControlActionsManager extends AbstractControlActionsManager {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DefaultControlActionsManager(final Control control) {
		super(control);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Handlers
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected IHandler getHandlerFor(String actionName) {
		if (actionName.equalsIgnoreCase(IWorkbenchActionDefinitionIds.SELECT_ALL)) {
			return SELECTALL_HANDLER;
		}
		return super.getHandlerFor(actionName);
	}

	/**
	 * Handler for process "Select all" action.
	 */
	private final IHandler SELECTALL_HANDLER = new AbstractHandler() {
		@Override
		public Object execute(ExecutionEvent event) throws ExecutionException {
			selectAllExecuted();
			return null;
		}

		@Override
		public boolean isEnabled() {
			return true;
		}

		@Override
		public boolean isHandled() {
			return true;
		}
	};

	protected void selectAllExecuted() {
	}
}
