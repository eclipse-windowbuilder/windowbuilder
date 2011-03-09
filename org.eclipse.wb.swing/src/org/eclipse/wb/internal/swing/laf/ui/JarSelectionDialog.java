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
package org.eclipse.wb.internal.swing.laf.ui;

import org.eclipse.wb.internal.core.DesignerPlugin;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import org.apache.commons.lang.ArrayUtils;

import java.util.HashSet;

/**
 * Dialog allowing to select JAR files from workspace.
 * 
 * @author mitin_aa
 * @author lobas_av
 * @coverage swing.laf.ui
 */
public final class JarSelectionDialog extends ElementTreeSelectionDialog {
  private Object[] m_expanded;
  private Object m_selection;
  private final JarFileFilter m_filter;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JarSelectionDialog(Shell parent) {
    super(parent, new WorkbenchLabelProvider(), new WorkbenchContentProvider());
    m_filter = new JarFileFilter();
    addFilter(m_filter);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Contents
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected TreeViewer createTreeViewer(Composite parent) {
    TreeViewer viewer = super.createTreeViewer(parent);
    if (m_expanded != null && m_expanded.length > 0) {
      viewer.setExpandedElements(m_expanded);
    }
    return viewer;
  }

  public void setInitialExpanded(Object[] initExpanded) {
    m_expanded = initExpanded;
  }

  @Override
  protected void updateOKStatus() {
    TreeViewer viewer = getTreeViewer();
    m_expanded = viewer.getExpandedElements();
    IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
    m_selection = selection.getFirstElement();
    super.updateOKStatus();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public final IFile getSelectedJarFile() {
    Object[] selectionResult = getResult();
    if (ArrayUtils.isEmpty(selectionResult)) {
      return null;
    }
    for (Object object : selectionResult) {
      if (m_filter.select(null, null, object)) {
        return (IFile) object;
      }
    }
    return null;
  }

  public Object[] getExpandedElements() {
    return m_expanded;
  }

  public Object getSelection() {
    return m_selection;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // @see org.eclipse.pde.internal.ui.editor.build.JARFileFilter
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class JarFileFilter extends ViewerFilter {
    private final static String jarExt = "jar"; //$NON-NLS-1$
    private final HashSet<IPath> fPaths;

    //
    public JarFileFilter() {
      fPaths = new HashSet<IPath>();
    }

    @Override
    public boolean select(Viewer viewer, Object parent, Object element) {
      if (element instanceof IFile) {
        return isFileValid(((IFile) element).getProjectRelativePath());
      }
      if (element instanceof IContainer) { // i.e. IProject, IFolder
        try {
          if (!((IContainer) element).isAccessible()) {
            return false;
          }
          IResource[] resources = ((IContainer) element).members();
          for (int i = 0; i < resources.length; i++) {
            if (select(viewer, parent, resources[i])) {
              return true;
            }
          }
        } catch (CoreException e) {
          DesignerPlugin.log(e);
          return false;
        }
      }
      return false;
    }

    public boolean isFileValid(IPath path) {
      String ext = path.getFileExtension();
      if (isPathValid(path) && ext != null && ext.length() != 0) {
        return ext.equals(jarExt);
      }
      return false;
    }

    public boolean isPathValid(IPath path) {
      return !fPaths.contains(path);
    }
  }
}
