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

package org.eclipse.wb.internal.discovery.ui.wizard;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.wb.internal.discovery.core.WBToolkit;
import org.eclipse.wb.internal.discovery.core.WBToolkitRegistry;
import org.eclipse.wb.internal.discovery.ui.WBDiscoveryUiPlugin;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;

/**
 * A wizard to allow the user to install a WindowBuilder toolkit. Typically,
 * this wizard will show up in the New... wizard dialog.
 */
public class InstallToolkitWizard extends Wizard implements INewWizard, IExecutableExtension {
  private String toolkitId;
  private WBToolkit toolkit;
  
  private InstallToolkitWizardPage page;
  
  /**
   * Create a new instance of InstallToolkitWizard.
   */
  public InstallToolkitWizard() {
    setWindowTitle("Install WindowBuilder Toolkit");
    setNeedsProgressMonitor(true);
  }
  
  public void setInitializationData(IConfigurationElement config,
      String propertyName, Object data) throws CoreException {
    if (data instanceof String) {
      toolkitId = (String)data;
      
      toolkit = WBToolkitRegistry.getRegistry().getToolkit(toolkitId);
    }
  }
  
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    if (toolkit != null) {
      setWindowTitle("Install " + toolkit.getName());
    }
  }
  
  @Override
  public void addPages() {
    page = new InstallToolkitWizardPage(toolkit);
    addPage(page);
  }
  
  @Override
  public boolean performFinish() {
    try {
      getContainer().run(true, true, new IRunnableWithProgress() {
        public void run(IProgressMonitor monitor) throws InvocationTargetException,
            InterruptedException {
          try {
            WBDiscoveryUiPlugin.getPlugin().installToolkits(Collections.singletonList(toolkit), monitor);
          } catch (ProvisionException e) {
            throw new InvocationTargetException(e);
          } catch (OperationCanceledException e) {
            throw new InvocationTargetException(e);
          }
        }
      });
    } catch (InterruptedException ie) {
      // ignore this
      
    } catch (InvocationTargetException e) {
      if (e.getCause() instanceof OperationCanceledException) {
        // the user canceled - no need to show an error.
        
      } else {
        MessageDialog.openError(getShell(), "Error Installing Toolkit", e.getCause().getMessage());
      }
    }
    
    return true;
  }

}
