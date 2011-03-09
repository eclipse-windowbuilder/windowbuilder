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

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.css.Activator;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import java.util.Map;

/**
 * "Source" page of {@link MultiPageEditor}.
 * 
 * @author sablin_aa
 * @coverage CSS.editor
 */
public final class SourcePage implements IDesignPage {
  private MultiPageEditor m_editor;
  private final Map<String, IAction> m_idToTextEditorAction = Maps.newHashMap();
  private Composite m_composite;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  public void initialize(MultiPageEditor cssEditor) {
    m_editor = cssEditor;
  }

  public void dispose() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Activation
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_active = false;

  public void handleActiveState(boolean activate) {
    if (m_active == activate) {
      return;
    }
    m_active = activate;
    //
    updateSourceActions(m_active);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  public Control createControl(Composite parent) {
    m_composite = new Composite(parent, SWT.NONE);
    m_composite.setLayout(new FillLayout());
    m_editor.super_createPartControl(m_composite);
    return m_composite;
  }

  public Control getControl() {
    return m_composite;
  }

  public void setFocus() {
    getTextWidget().setFocus();
  }

  private StyledText getTextWidget() {
    return m_editor.super_getSourceViewer().getTextWidget();
  }

  /**
   * @return <code>true</code> if page is active.
   */
  public boolean isActive() {
    return getTextWidget().isFocusControl();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getName() {
    return "Source";
  }

  public Image getImage() {
    return Activator.getImage("editor_source_page.png");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setAction(String actionID, IAction action) {
    if (!"save".equals(actionID) && !"undo".equals(actionID) && !"redo".equals(actionID)) {
      m_idToTextEditorAction.put(actionID, action);
    }
  }

  /**
   * Installs/removes source editor actions depending on given flag. We need this to prevent
   * activation of source editor actions on Design page, such as auto-completion (Ctrl+Space).
   */
  private void updateSourceActions(boolean install) {
    for (Map.Entry<String, IAction> entry : m_idToTextEditorAction.entrySet()) {
      String id = entry.getKey();
      IAction action = entry.getValue();
      action.setEnabled(install);
      if (install) {
        m_editor.super_setAction(id, action);
      } else {
        m_editor.super_setAction(id, null);
      }
    }
  }
}