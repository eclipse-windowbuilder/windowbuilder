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
package org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders;

/**
 * Configuration for {@link TabContainerUiContentProvider}.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public class TabContainerConfiguration {
  private boolean m_useAddButton;
  private boolean m_useMultiAddButton;
  private boolean m_useRemoveButton;
  private boolean m_useUpDownButtons;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Buttons
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean isUseAddButton() {
    return m_useAddButton;
  }

  public void setUseAddButton(boolean value) {
    m_useAddButton = value;
  }

  public boolean isUseMultiAddButton() {
    return m_useMultiAddButton;
  }

  public void setUseMultiAddButton(boolean value) {
    m_useMultiAddButton = value;
  }

  public boolean isUseRemoveButton() {
    return m_useRemoveButton;
  }

  public void setUseRemoveButton(boolean value) {
    m_useRemoveButton = value;
  }

  public boolean isUseUpDownButtons() {
    return m_useUpDownButtons;
  }

  public void setUseUpDownButtons(boolean value) {
    m_useUpDownButtons = value;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // EmptyPage
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_createEmptyPage;
  private String m_emptyPageTitle;
  private String m_emptyPageMessage;

  //
  public boolean isCreateEmptyPage() {
    return m_createEmptyPage;
  }

  public String getEmptyPageTitle() {
    return m_emptyPageTitle;
  }

  public String getEmptyPageMessage() {
    return m_emptyPageMessage;
  }

  public void setCreateEmptyPage(String title, String message) {
    m_createEmptyPage = true;
    m_emptyPageTitle = title;
    m_emptyPageMessage = message;
  }
}