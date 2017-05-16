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

import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.IDialogFieldListener;
import org.eclipse.wb.internal.core.utils.ui.PixelConverter;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Composite dialog field for source folder (package root) and package selection.
 *
 * @author scheglov_ke
 * @coverage core.util.jdt.ui
 */
public class PackageRootAndPackageSelectionDialogField extends DialogField {
  private final int m_textFieldWidth;
  private final PackageRootSelectionDialogField m_rootField;
  private final PackageSelectionDialogField m_packageField;
  private boolean m_dialogFieldChanging;
  private final IDialogFieldListener m_validateListener = new IDialogFieldListener() {
    public void dialogFieldChanged(DialogField field) {
      if (!m_dialogFieldChanging) {
        PackageRootAndPackageSelectionDialogField.this.dialogFieldChanged();
      }
    }
  };
  private PixelConverter m_pixelConverter;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PackageRootAndPackageSelectionDialogField(int textFieldWidth,
      String rootLabel,
      String rootButtonLabel,
      String packageLabel,
      String packageButtonLabel) {
    m_textFieldWidth = textFieldWidth;
    // create source folder field
    {
      m_rootField = PackageRootSelectionDialogField.create(rootLabel, rootButtonLabel);
      m_rootField.setUpdateListener(m_validateListener);
      m_rootField.setListener(new IPackageRootChangeListener() {
        public void rootChanged(IPackageFragmentRoot newRoot) {
          m_packageField.setRoot(m_rootField.getRoot());
        }
      });
    }
    // create package field
    {
      m_packageField = PackageSelectionDialogField.create(packageLabel, packageButtonLabel);
      m_packageField.setUpdateListener(m_validateListener);
    }
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
    m_rootField.setPackageRootFilter(packageRootFilter);
  }

  /**
   * Update fields using given compilation unit.
   */
  public void setCompilationUnit(ICompilationUnit compilationUnit) {
    m_dialogFieldChanging = true;
    try {
      m_rootField.setCompilationUnit(compilationUnit);
      m_packageField.setCompilationUnit(compilationUnit);
    } finally {
      m_dialogFieldChanging = false;
    }
    dialogFieldChanged();
  }

  /**
   * Return selected source folder (package root).
   */
  public IPackageFragmentRoot getRoot() {
    return m_rootField.getRoot();
  }

  /**
   * Set source folder (package root).
   */
  public void setRoot(IPackageFragmentRoot root) {
    m_rootField.setRoot(root);
  }

  /**
   * Return selected package.
   */
  public IPackageFragment getPackage() {
    return m_packageField.getPackage();
  }

  /**
   * Set package.
   */
  public void setPackage(IPackageFragment pkg) {
    if (pkg != null) {
      m_packageField.setPackage(pkg);
      m_rootField.setRoot((IPackageFragmentRoot) pkg.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT));
    } else {
      m_packageField.setPackage(null);
      m_rootField.setRoot(null);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Controls creation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public int getNumberOfControls() {
    return 1;
  }

  @Override
  public Control[] doFillIntoGrid(Composite parent, int nColumns) {
    assertEnoughColumns(nColumns);
    m_pixelConverter = new PixelConverter(parent);
    // create root field controls
    m_rootField.doFillIntoGrid(parent, nColumns);
    m_rootField.getTextControl(null).setLayoutData(createDataForText());
    // create package field controls
    m_packageField.doFillIntoGrid(parent, nColumns);
    m_packageField.getTextControl(null).setLayoutData(createDataForText());
    // we don't want return controls (does anybody use them?)
    return null;
  }

  private GridData createDataForText() {
    GridData gridData = new GridData();
    gridData.grabExcessHorizontalSpace = true;
    gridData.horizontalAlignment = GridData.FILL;
    gridData.widthHint = m_pixelConverter.convertWidthInCharsToPixels(m_textFieldWidth);
    return gridData;
  }
}
