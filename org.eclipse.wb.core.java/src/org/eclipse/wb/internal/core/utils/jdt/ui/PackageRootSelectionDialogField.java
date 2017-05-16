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
package org.eclipse.wb.internal.core.utils.jdt.ui;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.Messages;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.IDialogFieldListener;
import org.eclipse.wb.internal.core.utils.dialogfields.IStringButtonAdapter;
import org.eclipse.wb.internal.core.utils.dialogfields.StatusUtils;
import org.eclipse.wb.internal.core.utils.dialogfields.StringButtonDialogField;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaElementSorter;
import org.eclipse.jdt.ui.StandardJavaElementContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;

/**
 * Field editor for source folder selection.
 *
 * @author scheglov_ke
 * @coverage core.util.jdt.ui
 */
public final class PackageRootSelectionDialogField extends StringButtonDialogField
    implements
      IDialogFieldListener {
  private IPackageRootFilter m_packageRootFilter;
  private IDialogFieldListener m_updateListener;
  private IPackageRootChangeListener m_listener;
  private IPackageFragmentRoot m_root;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return source folder selection field with given field and button labels.
   */
  public static PackageRootSelectionDialogField create(String label, String buttonLabel) {
    ButtonAdapter adapter = new ButtonAdapter();
    PackageRootSelectionDialogField field =
        new PackageRootSelectionDialogField(label, buttonLabel, adapter);
    adapter.setReceiver(field);
    return field;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private PackageRootSelectionDialogField(String label,
      String buttonLabel,
      IStringButtonAdapter adapter) {
    super(adapter);
    setLabelText(label);
    setButtonLabel(buttonLabel);
    setDialogFieldListener(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets filter for package fragment root selection.
   */
  public void setPackageRootFilter(IPackageRootFilter packageRootFilter) {
    m_packageRootFilter = packageRootFilter;
  }

  public void setCompilationUnit(ICompilationUnit unit) {
    IPackageFragment pkg = (IPackageFragment) unit.getParent();
    IPackageFragmentRoot root = (IPackageFragmentRoot) pkg.getParent();
    setRoot(root);
  }

  public void setUpdateListener(IDialogFieldListener updateListener) {
    m_updateListener = updateListener;
  }

  public void setListener(IPackageRootChangeListener changeListener) {
    m_listener = changeListener;
  }

  public IPackageFragmentRoot getRoot() {
    return m_root;
  }

  public void setRoot(IPackageFragmentRoot packageFragmentRoot) {
    m_root = packageFragmentRoot;
    if (m_root != null) {
      String newText = getRootString(m_root);
      if (!getText().equals(newText)) {
        setText(newText);
      }
    }
    //
    if (m_listener != null) {
      m_listener.rootChanged(m_root);
    }
    if (m_updateListener != null) {
      m_updateListener.dialogFieldChanged(this);
    }
  }

  public void setRootWithoutUpdate(IPackageFragmentRoot packageFragmentRoot) {
    m_root = packageFragmentRoot;
    if (m_root != null) {
      String newText = getRootString(m_root);
      if (!getText().equals(newText)) {
        setTextWithoutUpdate(newText);
      }
    }
    //
    if (m_listener != null) {
      m_listener.rootChanged(m_root);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IDialogFieldListener
  //
  ////////////////////////////////////////////////////////////////////////////
  public void dialogFieldChanged(DialogField field) {
    setRoot(getRootFromString(getText()));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Return string presentation of package root.
   */
  private static String getRootString(IPackageFragmentRoot root) {
    return root == null ? "" : root.getPath().makeRelative().toString();
  }

  /**
   * Tries to build a package fragment root out of a string and sets the string into this package
   * fragment root.
   */
  private static IPackageFragmentRoot getRootFromString(String rootString) {
    if (rootString.length() == 0) {
      return null;
    }
    // prepare resource for given string
    IPath path = new Path(rootString);
    IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
    IResource resource = workspaceRoot.findMember(path);
    if (resource == null) {
      return null;
    }
    // resource should be project or source folder
    int resourceType = resource.getType();
    if (resourceType == IResource.PROJECT || resourceType == IResource.FOLDER) {
      // check project
      IProject project = resource.getProject();
      if (!project.isOpen()) {
        return null;
      }
      // try to convert resource into package fragment root
      IJavaProject javaProject = JavaCore.create(project);
      IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(resource);
      if (root.exists()) {
        return root;
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Button adapter
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class ButtonAdapter implements IStringButtonAdapter {
    private PackageRootSelectionDialogField m_receiver;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    public void setReceiver(PackageRootSelectionDialogField receiver) {
      m_receiver = receiver;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // IStringButtonAdapter
    //
    ////////////////////////////////////////////////////////////////////////////
    public void changeControlPressed(DialogField field) {
      IPackageFragmentRoot root = selectSourceFolder(m_receiver.m_root);
      if (root != null) {
        m_receiver.setRoot(root);
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Select source folder
    //
    ////////////////////////////////////////////////////////////////////////////
    private IPackageFragmentRoot selectSourceFolder(IPackageFragmentRoot initialSelection) {
      Shell shell = Display.getCurrent().getActiveShell();
      ILabelProvider labelProvider =
          new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);
      ITreeContentProvider contentProvider = new StandardJavaElementContentProvider();
      ElementTreeSelectionDialog dialog =
          new ElementTreeSelectionDialog(shell, labelProvider, contentProvider);
      //
      dialog.setTitle(Messages.PackageRootSelectionDialogField_dialogTitle);
      dialog.setMessage(Messages.PackageRootSelectionDialogField_dialogMessage);
      dialog.setSorter(new JavaElementSorter());
      //
      dialog.setValidator(new ISelectionStatusValidator() {
        public IStatus validate(Object[] selection) {
          if (selection.length == 1) {
            Object element = selection[0];
            if (isElementValid(element)) {
              return StatusUtils.OK_STATUS;
            }
          }
          return StatusUtils.ERROR_STATUS;
        }

        public boolean isElementValid(Object element) {
          try {
            if (element instanceof IJavaProject) {
              IJavaProject project = (IJavaProject) element;
              IPath path = project.getProject().getFullPath();
              return project.findPackageFragmentRoot(path) != null;
            } else if (element instanceof IPackageFragmentRoot) {
              return ((IPackageFragmentRoot) element).getKind() == IPackageFragmentRoot.K_SOURCE;
            }
            return true;
          } catch (JavaModelException e) {
            DesignerPlugin.log(e);
          }
          return false;
        }
      });
      //
      dialog.addFilter(m_javaFilter);
      //
      dialog.setInput(JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()));
      dialog.setInitialSelection(initialSelection);
      //
      if (dialog.open() == Window.OK) {
        Object element = dialog.getFirstResult();
        if (element instanceof IJavaProject) {
          IJavaProject javaProject = (IJavaProject) element;
          return javaProject.getPackageFragmentRoot(javaProject.getProject());
        } else if (element instanceof IPackageFragmentRoot) {
          return (IPackageFragmentRoot) element;
        }
        return null;
      }
      return null;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Filter
    //
    ////////////////////////////////////////////////////////////////////////////
    private final ViewerFilter m_javaFilter = new ViewerFilter() {
      @Override
      public boolean select(Viewer viewer, Object parentElement, Object element) {
        // check project
        if (element instanceof IJavaProject) {
          if (m_receiver.m_packageRootFilter != null) {
            return m_receiver.m_packageRootFilter.select((IJavaProject) element);
          }
          return true;
        }
        // check package fragment root
        if (element instanceof IPackageFragmentRoot) {
          try {
            IPackageFragmentRoot packageFragmentRoot = (IPackageFragmentRoot) element;
            if (packageFragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
              if (m_receiver.m_packageRootFilter != null) {
                return m_receiver.m_packageRootFilter.select(packageFragmentRoot);
              }
              return true;
            }
          } catch (JavaModelException e) {
            DesignerPlugin.log(e);
            return false;
          }
        }
        return false;
      }
    };
  }
}
