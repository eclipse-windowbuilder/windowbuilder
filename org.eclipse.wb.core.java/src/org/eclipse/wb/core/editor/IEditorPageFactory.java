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