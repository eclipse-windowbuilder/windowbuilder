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
package org.eclipse.wb.internal.css.editors.multi;

import org.eclipse.wb.internal.css.editors.CssEditor;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.widgets.Composite;

/**
 * Multi page editor with textual CSS editor and {@link DesignPage}.
 * 
 * @author sablin_aa
 * @coverage CSS.editor
 */
public final class MultiPageEditor extends CssEditor {
  public static final String ID = "org.eclipse.wb.css.editors.guiEditor";
  private final MultiMode m_multiMode;
  private boolean m_firstActivation = true;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MultiPageEditor() {
    m_multiMode = new MultiPagesMode(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editor
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void createPartControl(Composite parent) {
    m_multiMode.create(parent);
  }

  @Override
  public void setFocus() {
    m_multiMode.setFocus();
  }

  void activated() {
    if (m_firstActivation) {
      m_firstActivation = false;
      m_multiMode.editorActivatedFirstTime();
    }
  }

  @Override
  public void dispose() {
    super.dispose();
    m_multiMode.dispose();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Invokes "super" {@link #createPartControl(Composite)}.
   */
  void super_createPartControl(Composite parent) {
    super.createPartControl(parent);
  }

  /**
   * Invokes "super" {@link #getSourceViewer()}.
   */
  ISourceViewer super_getSourceViewer() {
    return super.getSourceViewer();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link MultiMode}.
   */
  public MultiMode getMultiMode() {
    return m_multiMode;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Actions
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setAction(String actionID, IAction action) {
    super.setAction(actionID, action);
    m_multiMode.getSourcePage().setAction(actionID, action);
  }

  void super_setAction(String actionID, IAction action) {
    super.setAction(actionID, action);
  }
}