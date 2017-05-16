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
package org.eclipse.wb.internal.core.wizards.actions;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.UiMessages;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.NewProjectAction;

import java.util.Iterator;

public abstract class AbstractOpenWizardAction extends Action
    implements
      IWorkbenchWindowActionDelegate {
  private final Class<?>[] fActivatedOnTypes;
  private final boolean fAcceptEmptySelection;
  private final boolean fNoChecking;

  /**
   * Creates a AbstractOpenWizardAction.
   *
   * @param label
   *          The label of the action
   * @param acceptEmptySelection
   *          Specifies if the action allows an empty selection
   */
  public AbstractOpenWizardAction(String label, boolean acceptEmptySelection) {
    this(label, null, acceptEmptySelection);
  }

  /**
   * Creates a AbstractOpenWizardAction.
   *
   * @param label
   *          The label of the action
   * @param activatedOnTypes
   *          The action is only enabled when all objects in the selection are of the given types.
   *          <code>null</code> will allow all types.
   * @param acceptEmptySelection
   *          Specifies if the action allows an empty selection
   */
  public AbstractOpenWizardAction(String label,
      Class<?>[] activatedOnTypes,
      boolean acceptEmptySelection) {
    super(label);
    fActivatedOnTypes = activatedOnTypes;
    fAcceptEmptySelection = acceptEmptySelection;
    fNoChecking = false;
  }

  /**
   * Creates a AbstractOpenWizardAction with no restrictions on types, and does allow an empty
   * selection.
   */
  protected AbstractOpenWizardAction() {
    fActivatedOnTypes = null;
    fAcceptEmptySelection = true;
    fNoChecking = true;
  }

  /**
   * Answer the window for the currently active workbench or null if no workbench windows are open
   */
  public IWorkbenchWindow getWorkbenchWindow() {
    IWorkbench workbench = getWorkbench();
    IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
    if (window == null) {
      IWorkbenchWindow[] openWindows = workbench.getWorkbenchWindows();
      for (int i = 0; i < openWindows.length; i++) {
        if (!openWindows[i].getShell().isDisposed()) {
          window = openWindows[i];
          break;
        }
      }
    }
    return window;
  }

  protected IWorkbench getWorkbench() {
    return PlatformUI.getWorkbench();
  }

  private boolean isOfAcceptedType(Object obj) {
    if (fActivatedOnTypes != null) {
      for (int i = 0; i < fActivatedOnTypes.length; i++) {
        if (fActivatedOnTypes[i].isInstance(obj)) {
          return true;
        }
      }
      return false;
    }
    return true;
  }

  private boolean isEnabled(IStructuredSelection selection) {
    Iterator<?> iter = selection.iterator();
    while (iter.hasNext()) {
      Object obj = iter.next();
      if (!isOfAcceptedType(obj) || !shouldAcceptElement(obj)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Can be overridden to add more checks. obj is guaranteed to be instance of one of the accepted
   * types
   */
  protected boolean shouldAcceptElement(Object obj) {
    return true;
  }

  /**
   * Creates the specific wizard. (to be implemented by a subclass)
   */
  abstract protected Wizard createWizard();

  protected IStructuredSelection getCurrentSelection() {
    IWorkbenchWindow window = getWorkbenchWindow();
    if (window != null) {
      ISelection selection = window.getSelectionService().getSelection();
      if (selection instanceof IStructuredSelection) {
        return (IStructuredSelection) selection;
      }
      if (selection instanceof ITextSelection) {
        IEditorPart activeEditor = DesignerPlugin.getActiveEditor();
        if (activeEditor != null) {
          IEditorInput editorInput = activeEditor.getEditorInput();
          if (editorInput instanceof IFileEditorInput) {
            IFileEditorInput fileEditorInput = (IFileEditorInput) editorInput;
            return new StructuredSelection(fileEditorInput.getFile());
          }
        }
      }
    }
    return StructuredSelection.EMPTY;
  }

  /**
   * The user has invoked this action.
   */
  @Override
  public void run() {
    if (!fNoChecking && !canActionBeAdded()) {
      return;
    }
    if (!checkWorkspace()) {
      return;
    }
    Wizard wizard = createWizard();
    if (wizard instanceof IWorkbenchWizard) {
      ((IWorkbenchWizard) wizard).init(getWorkbench(), getCurrentSelection());
    }
    WizardDialog dialog = new WizardDialog(getWorkbenchWindow().getShell(), wizard);
    dialog.create();
    String title = wizard.getWindowTitle();
    if (title != null) {
      dialog.getShell().setText(title);
    }
    //$NON-NLS-1$
    dialog.open();
  }

  /**
   * Tests if the action can be run on the current selection.
   */
  public boolean canActionBeAdded() {
    IStructuredSelection selection = getCurrentSelection();
    if (selection == null || selection.isEmpty()) {
      return fAcceptEmptySelection;
    }
    return isEnabled(selection);
  }

  /*
   * @see IActionDelegate#run(IAction)
   */
  public void run(IAction action) {
    run();
  }

  /*
   * @see IWorkbenchWindowActionDelegate#dispose()
   */
  public void dispose() {
  }

  /*
   * @see IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
   */
  public void init(IWorkbenchWindow window) {
  }

  /*
   * @see IActionDelegate#selectionChanged(IAction, ISelection)
   */
  public void selectionChanged(IAction action, ISelection selection) {
    // selection taken from selectionprovider
  }

  /**
   * Check that workspace is in correct state (for example there are projects). Do nothing by
   * default.
   */
  protected boolean checkWorkspace() {
    return true;
  }

  protected final boolean checkWorkspaceNotEmpty() {
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    if (workspace.getRoot().getProjects().length == 0) {
      Shell shell = getWorkbenchWindow().getShell();
      String title = UiMessages.AbstractOpenWizardAction_emptyWorkspaceTitle;
      String message = UiMessages.AbstractOpenWizardAction_emptyWorkspaceMessage;
      if (MessageDialog.openQuestion(shell, title, message)) {
        new NewProjectAction().run();
        return workspace.getRoot().getProjects().length != 0;
      }
      return false;
    }
    return true;
  }
}