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
package org.eclipse.wb.internal.core.nls.bundle.eclipse;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.nls.Messages;
import org.eclipse.wb.internal.core.nls.bundle.AbstractBundleSourceNewComposite;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.IStringButtonAdapter;
import org.eclipse.wb.internal.core.utils.dialogfields.StringButtonDialogField;
import org.eclipse.wb.internal.core.utils.jdt.ui.PackageRootAndPackageSelectionDialogField;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * Composite for creating new accessor-based source.
 *
 * @author scheglov_ke
 * @coverage core.nls.ui
 */
public abstract class AbstractAccessorSourceNewComposite extends AbstractBundleSourceNewComposite {
  private PackageRootAndPackageSelectionDialogField m_accessorPackageField;
  private StringButtonDialogField m_accessorClassField;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractAccessorSourceNewComposite(Composite parent, int style, JavaInfo root) {
    super(parent, style, root);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Accessor group
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final void createAccessorGroup() {
    Group accessorGroup = new Group(this, SWT.NONE);
    GridDataFactory.create(accessorGroup).grabH().fillH();
    GridLayoutFactory.create(accessorGroup).columns(3);
    accessorGroup.setText(Messages.AbstractAccessorSourceNewComposite_accessorGroup);
    // create source folder and package selection field
    {
      m_accessorPackageField =
          new PackageRootAndPackageSelectionDialogField(60,
              Messages.AbstractAccessorSourceNewComposite_accessorSourceFolder,
              Messages.AbstractAccessorSourceNewComposite_accessorSourceFolderBrowse,
              Messages.AbstractAccessorSourceNewComposite_accessorPackage,
              Messages.AbstractAccessorSourceNewComposite_accessorPackageBrowse);
      m_accessorPackageField.setDialogFieldListener(m_validateListener);
      m_accessorPackageField.doFillIntoGrid(accessorGroup, 3);
    }
    // create class field
    {
      m_accessorClassField = new StringButtonDialogField(new IStringButtonAdapter() {
        public void changeControlPressed(DialogField field) {
          try {
            // prepare dialog parameters
            IPackageFragmentRoot sourceFolder = m_accessorPackageField.getRoot();
            ProgressMonitorDialog context = new ProgressMonitorDialog(DesignerPlugin.getShell());
            IJavaSearchScope scope =
                sourceFolder != null
                    ? SearchEngine.createJavaSearchScope(new IJavaElement[]{sourceFolder})
                    : SearchEngine.createWorkspaceScope();
            // prepare dialog
            SelectionDialog dialog =
                JavaUI.createTypeDialog(
                    getShell(),
                    context,
                    scope,
                    IJavaElementSearchConstants.CONSIDER_TYPES,
                    false,
                    "*Messages<");
            dialog.setTitle(Messages.AbstractAccessorSourceNewComposite_accessorChooseTitle);
            dialog.setMessage(Messages.AbstractAccessorSourceNewComposite_accessorChooseMessage);
            // select type
            if (dialog.open() != Window.OK) {
              return;
            }
            IType type = (IType) dialog.getResult()[0];
            // update source folder, package and class fields
            ICompilationUnit compilationUnit = type.getCompilationUnit();
            m_accessorPackageField.setCompilationUnit(compilationUnit);
            m_accessorClassField.setText(type.getElementName());
          } catch (Throwable e) {
            DesignerPlugin.log(e);
          }
        }
      });
      m_accessorClassField.setDialogFieldListener(m_validateListener);
      m_accessorClassField.setLabelText(Messages.AbstractAccessorSourceNewComposite_accessorChooseLabel);
      m_accessorClassField.setButtonLabel(Messages.AbstractAccessorSourceNewComposite_accessorChooseButton);
      createTextFieldControls(accessorGroup, m_accessorClassField, 3);
    }
    // create additional fields
    createAdditionalAccessorFields(accessorGroup);
  }

  protected void createAdditionalAccessorFields(Composite parent) {
  }

  protected final void initializeAccessorGroup() {
    m_accessorPackageField.setCompilationUnit(m_compilationUnit);
    m_accessorClassField.setText("Messages");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Accessor group: validation
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String KEY_ACCESSOR_FOLDER = "KEY_ACCESSOR_FOLDER";
  private static final String KEY_ACCESSOR_PACKAGE = "KEY_ACCESSOR_PACKAGE";
  protected static final String KEY_ACCESSOR_CLASS = "KEY_ACCESSOR_CLASS";

  /**
   * Validate accessor fields.
   */
  protected void validateAccessorFields() {
    // validate source folder
    {
      IPackageFragmentRoot root = m_accessorPackageField.getRoot();
      if (root == null || !root.exists()) {
        setInvalid(
            KEY_ACCESSOR_FOLDER,
            Messages.AbstractAccessorSourceNewComposite_validateAccessorSourceFolder);
      } else {
        setValid(KEY_ACCESSOR_FOLDER);
      }
    }
    // validate package
    String packageName = null;
    {
      IPackageFragment pkg = m_accessorPackageField.getPackage();
      packageName = pkg == null ? null : pkg.getElementName();
      if (pkg == null || !pkg.exists()) {
        setInvalid(
            KEY_ACCESSOR_PACKAGE,
            Messages.AbstractAccessorSourceNewComposite_validateAccessorPackageEmpty);
      } else if (pkg.isDefaultPackage()) {
        setInvalid(
            KEY_ACCESSOR_PACKAGE,
            Messages.AbstractAccessorSourceNewComposite_validateAccessorPackageDefault);
      } else {
        setValid(KEY_ACCESSOR_PACKAGE);
      }
    }
    // validate class name
    String className = m_accessorClassField.getText();
    {
      IStatus status = JavaConventions.validateJavaTypeName(className);
      if (className.indexOf('.') != -1) {
        setInvalid(
            KEY_ACCESSOR_CLASS,
            Messages.AbstractAccessorSourceNewComposite_validateAccessorClassDot);
      } else if (status.getSeverity() != IStatus.OK) {
        setStatus(KEY_ACCESSOR_CLASS, status);
      } else {
        setValid(KEY_ACCESSOR_CLASS);
      }
      // if such class already exists, disable property group
      try {
        String fullClassName = packageName + "." + className;
        IType type = m_editor.getJavaProject().findType(fullClassName);
        setPropertyGroupEnable(type == null);
      } catch (Throwable e) {
        setInvalid(KEY_ACCESSOR_CLASS, "Exception: " + e.getMessage());
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void validateAll() {
    validateAccessorFields();
    super.validateAll();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creating
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final void fillAccessorParameters(AbstractAccessorSourceParameters parameters)
      throws Exception {
    parameters.m_accessorSourceFolder = m_accessorPackageField.getRoot();
    parameters.m_accessorPackage = m_accessorPackageField.getPackage();
    parameters.m_accessorPackageName = parameters.m_accessorPackage.getElementName();
    parameters.m_accessorClassName = m_accessorClassField.getText();
    parameters.m_accessorFullClassName =
        parameters.m_accessorPackageName + "." + parameters.m_accessorClassName;
    // prepare m_accessorExists
    {
      IJavaProject javaProject = m_editor.getJavaProject();
      parameters.m_accessorExists =
          javaProject.findType(parameters.m_accessorFullClassName) != null;
    }
  }
}
