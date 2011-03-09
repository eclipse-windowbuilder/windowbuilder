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
package org.eclipse.wb.internal.css.editors;

import org.eclipse.wb.internal.css.Activator;

import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.editors.text.TextEditor;

/**
 * Editor for CSS files.
 * 
 * @author scheglov_ke
 * @coverage CSS.editor
 */
public class CssEditor extends TextEditor {
  private final TokenManager m_tokenManager = Activator.getDefault().getTokenManager();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CssEditor() {
    setDocumentProvider(new CssDocumentProvider());
    setSourceViewerConfiguration(new CssConfiguration());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Preferences
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean affectsTextPresentation(PropertyChangeEvent event) {
    return super.affectsTextPresentation(event) || m_tokenManager.affectsTextPresentation(event);
  }

  @Override
  protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
    m_tokenManager.handlePreferenceStoreChanged(event);
    super.handlePreferenceStoreChanged(event);
  }
}
