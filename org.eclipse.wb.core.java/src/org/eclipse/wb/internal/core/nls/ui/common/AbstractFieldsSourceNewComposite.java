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
package org.eclipse.wb.internal.core.nls.ui.common;

import com.google.common.collect.Maps;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.nls.Messages;
import org.eclipse.wb.internal.core.nls.edit.EditableSource;
import org.eclipse.wb.internal.core.nls.edit.IEditableSource;
import org.eclipse.wb.internal.core.nls.model.LocaleInfo;
import org.eclipse.wb.internal.core.nls.ui.AbstractSourceNewComposite;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogFieldUtils;
import org.eclipse.wb.internal.core.utils.dialogfields.IDialogFieldListener;
import org.eclipse.wb.internal.core.utils.dialogfields.IStringButtonAdapter;
import org.eclipse.wb.internal.core.utils.dialogfields.StatusUtils;
import org.eclipse.wb.internal.core.utils.dialogfields.StringButtonDialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.StringDialogField;
import org.eclipse.wb.internal.core.utils.jdt.ui.PackageRootAndPackageSelectionDialogField;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SimpleName;
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

import java.text.MessageFormat;
import java.util.Map;

/**
 * Abstract composite for creating strings source using {@link DialogField}'s.
 *
 * @author scheglov_ke
 * @coverage core.nls.ui
 */
public abstract class AbstractFieldsSourceNewComposite extends AbstractSourceNewComposite {
  protected final JavaInfo m_root;
  protected final AstEditor m_editor;
  protected final ICompilationUnit m_compilationUnit;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractFieldsSourceNewComposite(Composite parent, int style, JavaInfo root) {
    super(parent, style);
    m_root = root;
    m_editor = m_root.getEditor();
    m_compilationUnit = m_editor.getModelUnit();
    //
    GridLayoutFactory.create(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Utility method for creating text field controls and tweaking layout properties.
   */
  protected final void createTextFieldControls(Composite parent,
      StringDialogField field,
      int nColumns) {
    DialogFieldUtils.fillControls(parent, field, nColumns, 60);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final IDialogFieldListener m_validateListener = new IDialogFieldListener() {
    public void dialogFieldChanged(DialogField field) {
      validateAll();
    }
  };

  /**
   * Validate fields and notify about possible status change.
   */
  protected void validateAll() {
    // force dialog status request
    firePropertyChanged("status", null, null);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation: utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Map<String, IStatus> m_statusMap = Maps.newTreeMap();

  protected final void setStatus(String key, IStatus status) {
    m_statusMap.put(key, status);
  }

  /**
   * Mark given status slot as invalid with given message.
   */
  protected final void setInvalid(String key, String message) {
    setStatus(key, StatusUtils.createError(message));
  }

  /**
   * Mark given status slot as valid.
   */
  protected final void setValid(String key) {
    setStatus(key, StatusUtils.OK_STATUS);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation: keys
  //
  ////////////////////////////////////////////////////////////////////////////
  private static int m_nextKeyId;

  /**
   * @return unique key for validation.
   */
  protected final String getUniqueKey() {
    return Integer.toString(m_nextKeyId++);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Check
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final IStatus getStatus() {
    return StatusUtils.getMostSevere(m_statusMap.values());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creating
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Create new empty {@link IEditableSource} with given name.
   */
  protected final IEditableSource createEmptyEditable(String name) {
    EditableSource editableSource = new EditableSource();
    String title = "NEW: " + name;
    editableSource.setShortTitle(title);
    editableSource.setLongTitle(title);
    editableSource.add(LocaleInfo.DEFAULT, Maps.<String, String>newHashMap());
    return editableSource;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Group for class selection
  //
  ////////////////////////////////////////////////////////////////////////////
  protected class ClassSelectionGroup {
    private final PackageRootAndPackageSelectionDialogField m_packageField;
    private final StringButtonDialogField m_classField;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public ClassSelectionGroup(Composite parent, String groupTitle, final String selectPattern) {
      Group classGroup = new Group(parent, SWT.NONE);
      GridDataFactory.create(classGroup).grabH().fillH();
      GridLayoutFactory.create(classGroup).columns(3);
      classGroup.setText(groupTitle);
      // create source folder and package selection field
      {
        m_packageField =
            new PackageRootAndPackageSelectionDialogField(60,
                Messages.AbstractFieldsSourceNewComposite_sourceFolder,
                Messages.AbstractFieldsSourceNewComposite_sourceFolderBrowse,
                Messages.AbstractFieldsSourceNewComposite_package,
                Messages.AbstractFieldsSourceNewComposite_packageBrowse);
        m_packageField.setDialogFieldListener(m_validateListener);
        m_packageField.doFillIntoGrid(classGroup, 3);
      }
      // create class field
      {
        m_classField = new StringButtonDialogField(new IStringButtonAdapter() {
          public void changeControlPressed(DialogField field) {
            try {
              // prepare dialog parameters
              IPackageFragmentRoot sourceFolder = m_packageField.getRoot();
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
                      selectPattern);
              dialog.setTitle(Messages.AbstractFieldsSourceNewComposite_chooseTitle);
              dialog.setMessage(Messages.AbstractFieldsSourceNewComposite_chooseMessage);
              // select type
              if (dialog.open() != Window.OK) {
                return;
              }
              IType type = (IType) dialog.getResult()[0];
              // update source folder, package and class fields
              ICompilationUnit compilationUnit = type.getCompilationUnit();
              m_packageField.setCompilationUnit(compilationUnit);
              m_classField.setText(type.getElementName());
            } catch (Throwable e) {
              DesignerPlugin.log(e);
            }
          }
        });
        m_classField.setDialogFieldListener(m_validateListener);
        m_classField.setLabelText(Messages.AbstractFieldsSourceNewComposite_chooseLabel);
        m_classField.setButtonLabel(Messages.AbstractFieldsSourceNewComposite_chooseBrowse);
        createTextFieldControls(classGroup, m_classField, 3);
      }
      // create additional fields
      createAdditionalFields(classGroup);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Additional handling
    //
    ////////////////////////////////////////////////////////////////////////////
    protected void createAdditionalFields(Composite parent) {
    }

    protected void setClassExists(boolean exists) {
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Validation
    //
    ////////////////////////////////////////////////////////////////////////////
    private final String KEY_FOLDER = getUniqueKey();
    private final String KEY_PACKAGE = getUniqueKey();
    private final String KEY_CLASS = getUniqueKey();

    public void validate() {
      // validate source folder
      {
        IPackageFragmentRoot root = m_packageField.getRoot();
        if (root == null || !root.exists()) {
          setInvalid(KEY_FOLDER, Messages.AbstractFieldsSourceNewComposite_validateSourceFolder);
        } else {
          setValid(KEY_FOLDER);
        }
      }
      // validate package
      String packageName = null;
      {
        IPackageFragment pkg = m_packageField.getPackage();
        packageName = pkg == null ? null : pkg.getElementName();
        if (pkg == null || !pkg.exists()) {
          setInvalid(KEY_PACKAGE, Messages.AbstractFieldsSourceNewComposite_validatePackage);
        } else if (pkg.getElementName().length() == 0) {
          setInvalid(KEY_PACKAGE, Messages.AbstractFieldsSourceNewComposite_validatePackageDefault);
        } else {
          setValid(KEY_PACKAGE);
        }
      }
      // validate class name
      String className = m_classField.getText();
      {
        IStatus status = JavaConventions.validateJavaTypeName(className);
        if (className.indexOf('.') != -1) {
          setInvalid(KEY_CLASS, Messages.AbstractFieldsSourceNewComposite_validateClassDot);
        } else if (status.getSeverity() != IStatus.OK) {
          setStatus(KEY_CLASS, status);
        } else {
          setValid(KEY_CLASS);
        }
        // check such class already exists
        try {
          String fullClassName = packageName + "." + className;
          IType type = m_editor.getJavaProject().findType(fullClassName);
          setClassExists(type == null);
        } catch (Throwable e) {
          setInvalid(KEY_CLASS, "Exception: " + e.getMessage());
        }
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    public final void initialize(String initialClassName) {
      m_packageField.setCompilationUnit(m_compilationUnit);
      m_classField.setText(initialClassName);
    }

    public final SourceClassParameters getParameters() throws Exception {
      SourceClassParameters parameters = new SourceClassParameters();
      parameters.m_sourceFolder = m_packageField.getRoot();
      parameters.m_package = m_packageField.getPackage();
      parameters.m_packageFolder = (IFolder) parameters.m_package.getUnderlyingResource();
      parameters.m_packageName = parameters.m_package.getElementName();
      parameters.m_className = m_classField.getText();
      parameters.m_fullClassName = parameters.m_packageName + "." + parameters.m_className;
      // prepare m_exists
      {
        IJavaProject javaProject = m_editor.getJavaProject();
        parameters.m_exists = javaProject.findType(parameters.m_fullClassName) != null;
      }
      //
      return parameters;
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Group for field name
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final class FieldNameGroup {
    private final StringDialogField m_field;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public FieldNameGroup(Composite parent, String groupTitle) {
      Group fieldGroup = new Group(parent, SWT.NONE);
      GridDataFactory.create(fieldGroup).grabH().fillH();
      GridLayoutFactory.create(fieldGroup).columns(2);
      fieldGroup.setText(groupTitle);
      // create field name field
      {
        m_field = new StringDialogField();
        m_field.setDialogFieldListener(m_validateListener);
        m_field.setLabelText(Messages.AbstractFieldsSourceNewComposite_fieldName);
        createTextFieldControls(fieldGroup, m_field, 2);
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Validation
    //
    ////////////////////////////////////////////////////////////////////////////
    private final String KEY_FIELD_NAME = getUniqueKey();

    public void validate() {
      final String fieldName = m_field.getText();
      // check that there are no field or variable with same name
      {
        // check, may be there is such name in CU
        final boolean hasSuchName[] = new boolean[1];
        m_editor.getAstUnit().accept(new ASTVisitor() {
          @Override
          public void endVisit(SimpleName node) {
            hasSuchName[0] |= node.getIdentifier().equals(fieldName);
          }
        });
        // set status
        if (hasSuchName[0]) {
          setInvalid(KEY_FIELD_NAME, MessageFormat.format(
              Messages.AbstractFieldsSourceNewComposite_validateFieldNameUsed,
              fieldName));
          return;
        }
      }
      // validate that field is valid identifier
      {
        IStatus status = JavaConventions.validateFieldName(fieldName);
        if (!status.isOK()) {
          setStatus(KEY_FIELD_NAME, status);
          return;
        }
      }
      // all is good
      setValid(KEY_FIELD_NAME);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    public void initialize(String initialFieldName) {
      m_field.setText(initialFieldName);
    }

    public String getName() {
      return m_field.getText();
    }
  }
}
