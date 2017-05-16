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
package org.eclipse.wb.internal.core.editor.actions;

import org.eclipse.wb.internal.core.editor.multi.DesignerEditor;

/**
 * This action does switching between "Source" and "Design" tabs of {@link DesignerEditor}.
 *
 * @author scheglov_ke
 * @coverage core.editor.action
 */
public class SwitchAction extends EditorRelatedAction {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IActionDelegate
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void run() {
    DesignerEditor editor = getEditor();
    if (editor != null) {
      editor.getMultiMode().switchSourceDesign();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Shows "Source" page.
   */
  public static void showSource() {
    showSource(-1);
  }

  /**
   * Shows "Source" page and at given source position.
   *
   * @param position
   *          the position to show in source, if <code>-1</code>, then ignored.
   */
  public static void showSource(int position) {
    DesignerEditor editor = getActiveEditor();
    if (editor != null) {
      editor.getMultiMode().showSource();
      if (position != -1) {
        editor.showSourcePosition(position);
      }
    }
  }
}