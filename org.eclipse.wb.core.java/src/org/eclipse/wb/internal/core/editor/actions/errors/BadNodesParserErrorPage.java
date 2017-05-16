/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.editor.actions.errors;

import org.eclipse.wb.internal.core.editor.Messages;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.state.EditorState.BadNodesCollection;

/**
 * Implementation of {@link BadNodesErrorPage} for errors during refresh.
 *
 * @author scheglov_ke
 * @coverage core.editor.action.error
 */
public final class BadNodesParserErrorPage extends BadNodesErrorPage {
  public String getTitle() {
    return Messages.BadNodesParserErrorPage_title;
  }

  @Override
  protected BadNodesCollection getCollection(EditorState editorState) {
    return editorState.getBadParserNodes();
  }
}
