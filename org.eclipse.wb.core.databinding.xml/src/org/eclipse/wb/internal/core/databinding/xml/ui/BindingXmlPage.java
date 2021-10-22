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
package org.eclipse.wb.internal.core.databinding.xml.ui;

import org.eclipse.wb.core.editor.DesignerState;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.parser.ParseState;
import org.eclipse.wb.internal.core.databinding.ui.EditComposite;
import org.eclipse.wb.internal.core.databinding.ui.EditSelection;
import org.eclipse.wb.internal.core.databinding.ui.UiUtils;
import org.eclipse.wb.internal.core.databinding.xml.Messages;
import org.eclipse.wb.internal.core.databinding.xml.parser.DatabindingRootProcessor;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.xml.editor.IXmlEditorPage;
import org.eclipse.wb.internal.core.xml.editor.UndoManager;
import org.eclipse.wb.internal.core.xml.editor.XmlEditorPage;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import java.util.List;

/**
 * Bindings design page.
 *
 * @author lobas_av
 * @coverage bindings.xml.ui
 */
public final class BindingXmlPage extends XmlEditorPage {
  private Composite m_composite;
  private EditComposite m_editComposite;
  private CLabel m_errorLabel;
  private IDatabindingsProvider m_databindingsProvider;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  public static void addPage(List<IXmlEditorPage> pages) {
    // check that binding page is already added
    for (IXmlEditorPage page : pages) {
      if (page instanceof BindingXmlPage) {
        return;
      }
    }
    // add new binding page
    pages.add(new BindingXmlPage());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void dispose() {
    // remove information about this editor
    DatabindingRootProcessor.STATES.remove(m_editor.getDocument());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Activation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setActive(boolean active) {
    UndoManager undoManager = m_editor.getDesignPage().getUndoManager();
    if (active) {
      undoManager.activate();
      handleUpdatePage();
    } else {
      undoManager.deactivate();
    }
  }

  private void handleUpdatePage() {
    // check state
    if (m_editor.getDesignPage().getDesignerState() != DesignerState.Successful) {
      if (m_editComposite != null) {
        m_editComposite.dispose();
        m_editComposite = null;
      }
      if (m_errorLabel != null) {
        m_errorLabel.dispose();
      }
      //
      m_errorLabel = new CLabel(m_composite, SWT.NONE);
      m_errorLabel.setText(Messages.BindingXmlPage_errorMessage);
      m_errorLabel.setImage(org.eclipse.wb.internal.core.databinding.Activator.getImage("errors.gif"));
      GridDataFactory.create(m_errorLabel).fillH().grabH();
      //
      m_databindingsProvider = null;
      m_composite.layout();
      return;
    }
    // prepare old selection
    EditSelection selection = m_editComposite == null ? null : m_editComposite.getEditSelection();
    // prepare state
    ParseState state = DatabindingRootProcessor.STATES.get(m_editor.getDocument());
    Assert.isNotNull(state);
    boolean isNew = m_databindingsProvider != state.databindingsProvider;
    //
    if (isNew) {
      // clear old composite
      if (m_editComposite != null) {
        m_editComposite.dispose();
      }
      if (m_errorLabel != null) {
        m_errorLabel.dispose();
        m_errorLabel = null;
      }
      //
      m_databindingsProvider = state.databindingsProvider;
      IDialogSettings settings =
          UiUtils.getSettings(
              state.plugin.getDialogSettings(),
              m_databindingsProvider.getClass().getName());
      // create new composite
      m_editComposite =
          new EditComposite(m_composite, SWT.NONE, state.plugin, m_databindingsProvider, settings);
      GridDataFactory.create(m_editComposite).fill().grab();
      m_composite.layout();
    }
    // set input
    m_editComposite.setInput(!isNew, selection);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  public Control createControl(Composite parent) {
    m_composite = new Composite(parent, SWT.NONE);
    GridLayoutFactory.create(m_composite).noMargins().noSpacing();
    return m_composite;
  }

  public Control getControl() {
    return m_composite;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getName() {
    return Messages.BindingXmlPage_name;
  }

  public Image getImage() {
    return org.eclipse.wb.internal.core.databinding.Activator.getImage("paperclip.png");
  }
}