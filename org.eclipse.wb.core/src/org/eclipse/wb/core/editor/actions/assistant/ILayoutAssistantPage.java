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

/**
 * Pages created in {@link LayoutAssistantListener} add this interface to receive update events.
 *
 * @author lobas_av
 * @coverage core.editor.action.assistant
 */
public interface ILayoutAssistantPage {
	/**
	 * @return <code>true</code> if this page handle valid objects.
	 */
	boolean isPageValid();

	/**
	 * Notifies page that it should update its controls from model.
	 */
	void updatePage();
}