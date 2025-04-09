/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.core.editor.palette.model.entry;

import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.gef.core.EditDomain;

/**
 * Implementation of {@link EntryInfo} that sets {@link Tool} for {@link EditDomain}.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette
 */
public abstract class ToolEntryInfo extends EntryInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// EntryInfo
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final Tool createTool(final boolean reload) {
		return ExecutionUtils.runObjectLog(() -> {
			// prepare tool
			Tool tool;
			{
				tool = createTool();
				if (tool == null) {
					return null;
				}
				tool.setUnloadWhenFinished(!reload);
			}
			// OK
			m_editPartViewer.getEditDomain().setActiveTool(tool);
			return tool;
		}, null);
	}

	/**
	 * @return the {@link Tool} that should be set on activation, or <code>null</code> if no
	 *         {@link Tool} can be activated.
	 */
	public abstract Tool createTool() throws Exception;
}
