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
package org.eclipse.wb.internal.core.xml.editor.actions;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ide.IDE;

import java.util.List;

/**
 * {@link Action} for switching between pair/companion {@link IFile}s.
 *
 * @author scheglov_ke
 * @coverage XML.editor.action
 */
public class SwitchPairEditorAction extends Action implements IEditorActionDelegate {
  private IEditorPart m_editor;
  private IFile m_pairFile;

  ////////////////////////////////////////////////////////////////////////////
  //
  // IEditorActionDelegate
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final void setActiveEditor(IAction action, IEditorPart editor) {
    m_editor = editor;
    m_pairFile = null;
  }

  private void preparePairFile() {
    if (m_pairFile != null) {
      return;
    }
    // may be no current editor
    if (m_editor == null) {
      return;
    }
    // prepare current file
    IFile editorFile;
    if (!(m_editor.getEditorInput() instanceof IFileEditorInput)) {
      return;
    }
    editorFile = ((IFileEditorInput) m_editor.getEditorInput()).getFile();
    // ask providers for pair
    List<IPairResourceProvider> providers =
        ExternalFactoriesHelper.getElementsInstances(
            IPairResourceProvider.class,
            "org.eclipse.wb.core.xml.pairResourceProviders",
            "provider");
    for (IPairResourceProvider provider : providers) {
      IFile pairFile = provider.getPair(editorFile);
      if (pairFile != null && pairFile.exists()) {
        m_pairFile = pairFile;
        break;
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IActionDelegate
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void selectionChanged(IAction action, ISelection selection) {
  }

  @Override
  public void run(IAction action) {
    run();
  }

  @Override
  public void run() {
    ExecutionUtils.runLog(new RunnableEx() {
      @Override
      public void run() throws Exception {
        preparePairFile();
        if (m_pairFile != null) {
          IDE.openEditor(DesignerPlugin.getActivePage(), m_pairFile);
        }
      }
    });
  }
}