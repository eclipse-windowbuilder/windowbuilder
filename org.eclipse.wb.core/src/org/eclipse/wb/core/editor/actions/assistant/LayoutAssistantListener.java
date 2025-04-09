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
package org.eclipse.wb.core.editor.actions.assistant;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.editor.actions.assistant.LayoutAssistantAction;

import org.eclipse.swt.widgets.TabFolder;

import java.util.List;

/**
 * Listener for {@link LayoutAssistantAction} events.
 *
 * @author lobas_av
 * @coverage core.editor.action.assistant
 */
public abstract class LayoutAssistantListener {
	/**
	 * Adds pages based on selected {@link ObjectInfo}'s.
	 *
	 * @param objects
	 *          the {@link List} of selected {@link ObjectInfo}.
	 * @param folder
	 *          the {@link TabFolder} for add tabs.
	 * @param pages
	 *          the {@link List} of {@link ILayoutAssistantPage}.
	 */
	public void createAssistantPages(List<ObjectInfo> objects,
			TabFolder folder,
			List<ILayoutAssistantPage> pages) throws Exception {
	}
}