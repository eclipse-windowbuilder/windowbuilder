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
package org.eclipse.wb.core.editor;

import java.util.List;

/**
 * This interface is contributed via extension point and used for creating {@link IEditorPage} pages
 * for {@link IDesignerEditor}.
 *
 * @author scheglov_ke
 * @coverage core.editor
 */
public interface IEditorPageFactory {
	/**
	 * Create {@link IEditorPage} pages for given {@link IDesignerEditor} editor.
	 */
	void createPages(IDesignerEditor editor, List<IEditorPage> pages);
}