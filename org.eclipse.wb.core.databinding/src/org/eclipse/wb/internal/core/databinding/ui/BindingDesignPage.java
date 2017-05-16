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
package org.eclipse.wb.internal.core.databinding.ui;

import org.eclipse.wb.core.editor.DesignerEditorListener;
import org.eclipse.wb.core.editor.DesignerState;
import org.eclipse.wb.core.editor.IDesignPage;
import org.eclipse.wb.core.editor.IDesignerEditor;
import org.eclipse.wb.core.editor.IEditorPage;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.databinding.Activator;
import org.eclipse.wb.internal.core.databinding.Messages;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.parser.DatabindingRootProcessor;
import org.eclipse.wb.internal.core.databinding.parser.ParseState;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

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
 * @coverage bindings.ui
 */
public class BindingDesignPage implements IEditorPage {
  private IDesignerEditor m_designerEditor;
  private IDesignPage m_designPage;
  private Composite m_composite;
  private EditComposite m_editComposite;
  private CLabel m_errorLabel;
  private IDatabindingsProvider m_databindingsProvider;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  public static void addPage(List<IEditorPage> pages) {
    for (IEditorPage page : pages) {
      if (page instanceof BindingDesignPage) {
        return;
      }
    }
    pages.add(new BindingDesignPage());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  public void initialize(IDesignerEditor designerEditor) {
    m_designerEditor = designerEditor;
    m_designPage = m_designerEditor.getMultiMode().getDesignPage();
  }

  public void dispose() {
    // remove information about this editor
    DatabindingRootProcessor.STATES.remove(m_designerEditor.getCompilationUnit());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Activation
  //
  ////////////////////////////////////////////////////////////////////////////
  private final DesignerEditorListener m_designPageListener = new DesignerEditorListener() {
    public void reparsed() {
      handleUpdatePage();
    }
  };

  public void handleActiveState(boolean activate) {
    if (activate) {
      m_designPage.setSourceModelSynchronizationEnabled(true);
      m_designerEditor.addDesignPageListener(m_designPageListener);
      handleUpdatePage();
    } else {
      m_designerEditor.removeDesignPageListener(m_designPageListener);
      m_designPage.setSourceModelSynchronizationEnabled(false);
    }
  }

  private void handleUpdatePage() {
    // check state
    if (m_designPage.getDesignerState() != DesignerState.Successful) {
      if (m_editComposite != null) {
        m_editComposite.dispose();
        m_editComposite = null;
      }
      if (m_errorLabel != null) {
        m_errorLabel.dispose();
      }
      //
      m_errorLabel = new CLabel(m_composite, SWT.NONE);
      m_errorLabel.setText(Messages.BindingDesignPage_erorrMessage);
      m_errorLabel.setImage(Activator.getImage("errors.gif"));
      GridDataFactory.create(m_errorLabel).fillH().grabH();
      //
      m_databindingsProvider = null;
      m_composite.layout();
      return;
    }
    // prepare old selection
    EditSelection selection = m_editComposite == null ? null : m_editComposite.getEditSelection();
    // prepare state
    ParseState state = DatabindingRootProcessor.STATES.get(m_designerEditor.getCompilationUnit());
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
      m_databindingsProvider.setBindingPage(this);
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

  private void handleReparse() {
    m_designPage.refreshGEF();
    handleUpdatePage();
  }

  public static void handleReparse(final BindingDesignPage bindingPage, JavaInfo javaInfoRoot) {
    if (EnvironmentUtils.isTestingTime()) {
      return;
    }
    javaInfoRoot.addBroadcastListener(new ObjectEventListener() {
      @Override
      public void refreshed2() throws Exception {
        if (bindingPage == null) {
          IDesignerEditor editor = (IDesignerEditor) DesignerPlugin.getActiveEditor();
          editor.getMultiMode().getDesignPage().refreshGEF();
        } else {
          bindingPage.handleReparse();
        }
      }
    });
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

  public void setFocus() {
    m_composite.setFocus();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getName() {
    return Messages.BindingDesignPage_name;
  }

  public Image getImage() {
    return Activator.getImage("paperclip.png");
  }
}