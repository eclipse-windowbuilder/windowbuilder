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
package org.eclipse.wb.internal.core.editor.multi;

import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditorActionContributor;
import org.eclipse.ui.IEditorPart;

/**
 * Contributor for {@link DesignerEditor}.
 *
 * @author scheglov_ke
 * @coverage core.editor
 */
@SuppressWarnings("restriction")
public class DesignerEditorContributor extends CompilationUnitEditorActionContributor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setActiveEditor(IEditorPart part) {
    super.setActiveEditor(part);
    if (part != null) {
      ((DesignerEditor) part).activated();
    }
  }
}