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
package org.eclipse.wb.internal.core.wizards;

import org.eclipse.wb.internal.core.DesignerPlugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.IThreadListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;

/**
 * Non internal version for org.eclipse.jdt.internal.ui.wizards.NewElementWizard.
 *
 * @author lobas_av
 * @coverage core.wizards.ui
 */
public abstract class DesignerNewElementWizard extends Wizard implements INewWizard {
  private IWorkbench fWorkbench;
  private IStructuredSelection fSelection;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DesignerNewElementWizard() {
    setNeedsProgressMonitor(true);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // DesignerNewElementWizard
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final void openResource(final IFile resource, final String editorId) {
    final IWorkbenchPage activePage = DesignerPlugin.getActivePage();
    if (activePage != null) {
      final Display display = getShell().getDisplay();
      if (display != null) {
        display.asyncExec(new Runnable() {
          public void run() {
            try {
              IDE.setDefaultEditor(resource, editorId);
              BasicNewResourceWizard.selectAndReveal(
                  resource,
                  DesignerPlugin.getActiveWorkbenchWindow());
              activePage.openEditor(new FileEditorInput(resource), editorId);
            } catch (PartInitException e) {
              DesignerPlugin.log(e);
            }
          }
        });
      }
    }
  }

  /**
   * Subclasses should override to perform the actions of the wizard. This method is run in the
   * wizard container's context as a workspace runnable.
   */
  protected abstract void finishPage(IProgressMonitor monitor) throws Exception;

  protected boolean canRunForked() {
    return true;
  }

  protected void handleFinishException(Shell shell, InvocationTargetException e) {
    ExceptionHandler.perform(
        e,
        shell,
        Messages.DesignerNewElementWizard_errorTitle,
        Messages.DesignerNewElementWizard_errorMessage);
  }

  @Override
  public boolean performFinish() {
    IWorkspaceRunnable op = new IWorkspaceRunnable() {
      public void run(IProgressMonitor monitor) throws CoreException {
        try {
          finishPage(monitor);
        } catch (Throwable e) {
          throw new CoreException(new Status(IStatus.ERROR,
              DesignerPlugin.PLUGIN_ID,
              IStatus.OK,
              e.getMessage(),
              e));
        }
      }
    };
    try {
      ISchedulingRule rule = null;
      {
        Job job = Job.getJobManager().currentJob();
        if (job != null) {
          rule = job.getRule();
        }
      }
      IRunnableWithProgress runnable = null;
      if (rule == null) {
        runnable = new WorkbenchRunnableAdapter(op, ResourcesPlugin.getWorkspace().getRoot());
      } else {
        runnable = new WorkbenchRunnableAdapter(op, rule, true);
      }
      getContainer().run(canRunForked(), true, runnable);
    } catch (InvocationTargetException e) {
      handleFinishException(getShell(), e);
      return false;
    } catch (InterruptedException e) {
      return false;
    }
    return true;
  }

  public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
    fWorkbench = workbench;
    fSelection = currentSelection;
  }

  public final IStructuredSelection getSelection() {
    return fSelection;
  }

  public final IWorkbench getWorkbench() {
    return fWorkbench;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Job Runnable
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class WorkbenchRunnableAdapter implements IRunnableWithProgress, IThreadListener {
    private boolean fTransfer = false;
    private final IWorkspaceRunnable fWorkspaceRunnable;
    private final ISchedulingRule fRule;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructors
    //
    ////////////////////////////////////////////////////////////////////////////
    public WorkbenchRunnableAdapter(IWorkspaceRunnable runnable, ISchedulingRule rule) {
      fWorkspaceRunnable = runnable;
      fRule = rule;
    }

    public WorkbenchRunnableAdapter(IWorkspaceRunnable runnable,
        ISchedulingRule rule,
        boolean transfer) {
      fWorkspaceRunnable = runnable;
      fRule = rule;
      fTransfer = transfer;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // IThreadListener
    //
    ////////////////////////////////////////////////////////////////////////////
    public void threadChange(Thread thread) {
      if (fTransfer) {
        Job.getJobManager().transferRule(fRule, thread);
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // IRunnableWithProgress
    //
    ////////////////////////////////////////////////////////////////////////////
    public void run(IProgressMonitor monitor) throws InvocationTargetException,
        InterruptedException {
      try {
        JavaCore.run(fWorkspaceRunnable, fRule, monitor);
      } catch (OperationCanceledException e) {
        throw new InterruptedException(e.getMessage());
      } catch (CoreException e) {
        throw new InvocationTargetException(e);
      }
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Error Dialog
  //
  ////////////////////////////////////////////////////////////////////////////
  protected static final class ExceptionHandler {
    public static void perform(InvocationTargetException e,
        Shell shell,
        String title,
        String message) {
      Throwable target = e.getTargetException();
      if (target instanceof CoreException) {
        perform((CoreException) target, shell, title, message);
      } else {
        DesignerPlugin.log(e);
        if (e.getMessage() != null && e.getMessage().length() > 0) {
          displayMessageDialog(e, e.getMessage(), shell, title, message);
        } else {
          displayMessageDialog(e, target.getMessage(), shell, title, message);
        }
      }
    }

    private static void perform(CoreException e, Shell shell, String title, String message) {
      DesignerPlugin.log(e);
      IStatus status = e.getStatus();
      if (status == null) {
        displayMessageDialog(e, e.getMessage(), shell, title, message);
      } else {
        ErrorDialog.openError(shell, title, message, status);
      }
    }

    private static void displayMessageDialog(Throwable t,
        String exceptionMessage,
        Shell shell,
        String title,
        String message) {
      StringWriter msg = new StringWriter();
      if (message != null) {
        msg.write(message);
        msg.write("\n\n");
      }
      if (exceptionMessage == null || exceptionMessage.length() == 0) {
        msg.write(Messages.DesignerNewElementWizard_errorSeeLog);
      } else {
        msg.write(exceptionMessage);
      }
      MessageDialog.openError(shell, title, msg.toString());
    }
  }
}