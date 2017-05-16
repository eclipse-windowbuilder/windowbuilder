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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.Messages;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.IDialogFieldListener;
import org.eclipse.wb.internal.core.utils.dialogfields.IStringButtonAdapter;
import org.eclipse.wb.internal.core.utils.dialogfields.StringButtonDialogField;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Field editor for package selection.
 *
 * @author scheglov_ke
 * @coverage core.util.jdt.ui
 */
public final class PackageSelectionDialogField extends StringButtonDialogField
    implements
      IDialogFieldListener {
  private IDialogFieldListener m_updateListener;
  private IPackageFragmentRoot m_root;
  private IPackageFragment m_package;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  public static PackageSelectionDialogField create(String label, String buttonLabel) {
    ButtonAdapter adapter = new ButtonAdapter();
    PackageSelectionDialogField field =
        new PackageSelectionDialogField(label, buttonLabel, adapter);
    adapter.setReceiver(field);
    return field;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private PackageSelectionDialogField(String label, String buttonLabel, IStringButtonAdapter adapter) {
    super(adapter);
    setLabelText(label);
    setButtonLabel(buttonLabel);
    setDialogFieldListener(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Update listener
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setUpdateListener(IDialogFieldListener updateListener) {
    m_updateListener = updateListener;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setCompilationUnit(ICompilationUnit unit) {
    IPackageFragment pkg = (IPackageFragment) unit.getParent();
    setPackage(pkg);
  }

  public IPackageFragmentRoot getRoot() {
    return m_root;
  }

  public void setRoot(IPackageFragmentRoot root) {
    m_root = root;
    updatePackage();
  }

  public IPackageFragment getPackage() {
    return m_package;
  }

  public void setPackage(IPackageFragment pkg) {
    m_package = pkg;
    if (m_package != null) {
      String newText = getPackageString(m_package);
      if (!getText().equals(newText)) {
        setText(newText);
      }
    }
    //
    if (m_updateListener != null) {
      m_updateListener.dialogFieldChanged(this);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IDialogFieldListener
  //
  ////////////////////////////////////////////////////////////////////////////
  public void dialogFieldChanged(DialogField field) {
    updatePackage();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Update package after any change (for example when user edits text or when root was changed).
   */
  private void updatePackage() {
    IPackageFragment pkg = getPackageFromString(getText());
    setPackage(pkg);
  }

  /**
   * Return strings presentation of package.
   */
  private static String getPackageString(IPackageFragment pkg) {
    return pkg == null ? "" : pkg.getElementName();
  }

  /**
   * Tries to build a package fragment root out of a string and sets the string into this package
   * fragment root.
   */
  private IPackageFragment getPackageFromString(String packageString) {
    if (m_root == null) {
      return null;
    }
    // try to get existing package in current root
    IPackageFragment packageFragment = m_root.getPackageFragment(packageString);
    if (packageFragment.exists()) {
      return packageFragment;
    }
    //
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Button adapter
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class ButtonAdapter implements IStringButtonAdapter {
    private PackageSelectionDialogField m_receiver;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    public void setReceiver(PackageSelectionDialogField receiver) {
      m_receiver = receiver;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // IStringButtonAdapter
    //
    ////////////////////////////////////////////////////////////////////////////
    public void changeControlPressed(DialogField field) {
      ElementListSelectionDialog dialog =
          new ElementListSelectionDialog(Display.getCurrent().getActiveShell(),
              new JavaElementLabelProvider());
      // configure dialog
      dialog.setIgnoreCase(false);
      dialog.setTitle(Messages.PackageSelectionDialogField_dialogTitle);
      dialog.setMessage(Messages.PackageSelectionDialogField_dialogMessage);
      dialog.setElements(getPackagesList().toArray());
      // select package
      if (dialog.open() == Window.OK) {
        IPackageFragment selectedPackage = (IPackageFragment) dialog.getFirstResult();
        if (selectedPackage != null) {
          m_receiver.setPackage(selectedPackage);
        }
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Packages
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Return list of valid packages.
     */
    private List<IPackageFragment> getPackagesList() {
      try {
        List<IPackageFragment> packages = Lists.newArrayList();
        // add packages for selected root
        if (m_receiver.m_root != null) {
          addValidPackages(m_receiver.m_root, packages, Sets.<String>newTreeSet());
        }
        //
        return packages;
      } catch (JavaModelException e) {
        DesignerPlugin.log(e);
        return Collections.emptyList();
      }
    }

    /**
     * Add valid unique packages.
     */
    private static void addValidPackages(IPackageFragmentRoot root,
        List<IPackageFragment> packages,
        Set<String> addedPackageNames) throws JavaModelException {
      // prepare package root children
      IJavaElement[] children = null;
      try {
        children = root.getChildren();
      } catch (JavaModelException e) {
        return;
      }
      // iterate over children and add valid packages
      for (int i = 0; i < children.length; i++) {
        if (children[i] instanceof IPackageFragment) {
          IPackageFragment packageFragment = (IPackageFragment) children[i];
          String packageName = packageFragment.getElementName();
          // check for unique package name
          if (addedPackageNames != null && addedPackageNames.contains(packageName)) {
            continue;
          }
          // add valid package
          if (canAddPackage(packageFragment)) {
            packages.add(packageFragment);
            if (addedPackageNames != null) {
              addedPackageNames.add(packageName);
            }
          }
        }
      }
    }

    /**
     * Check that passed package fragment root is valid.
     */
    public static boolean canAddPackageRoot(IPackageFragmentRoot root) throws JavaModelException {
      if (!root.exists()) {
        return false;
      }
      if (root.isArchive()) {
        return false;
      }
      if (root.isExternal()) {
        return false;
      }
      if (root.isReadOnly()) {
        return false;
      }
      if (!root.isStructureKnown()) {
        return false;
      }
      return true;
    }

    /**
     * Check that passed package fragment is valid.
     */
    private static boolean canAddPackage(IPackageFragment pkg) throws JavaModelException {
      if (!pkg.exists()) {
        return false;
      }
      if (pkg.isReadOnly()) {
        return false;
      }
      if (!pkg.isStructureKnown()) {
        return false;
      }
      return true;
    }
  }
}
