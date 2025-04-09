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
package org.eclipse.wb.internal.core.editor.actions.errors;

import org.eclipse.wb.internal.core.editor.Messages;
import org.eclipse.wb.internal.core.parser.JavaInfoParser;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.state.EditorState.BadNodesCollection;

/**
 * Implementation of {@link BadNodesErrorPage} for {@link JavaInfoParser} errors.
 *
 * @author scheglov_ke
 * @coverage core.editor.action.error
 */
public final class BadNodesRefreshErrorPage extends BadNodesErrorPage {
	@Override
	public String getTitle() {
		return Messages.BadNodesRefreshErrorPage_title;
	}

	@Override
	protected BadNodesCollection getCollection(EditorState editorState) {
		return editorState.getBadRefreshNodes();
	}
}
