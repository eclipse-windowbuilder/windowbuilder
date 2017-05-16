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
package org.eclipse.wb.internal.core.nls.bundle;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.nls.Messages;
import org.eclipse.wb.internal.core.nls.ui.common.AbstractFieldsSourceNewComposite;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.IStringButtonAdapter;
import org.eclipse.wb.internal.core.utils.dialogfields.StringButtonDialogField;
import org.eclipse.wb.internal.core.utils.jdt.ui.PackageRootAndPackageSelectionDialogField;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract composite for creating new bundle-based source.
 *
 * @author scheglov_ke
 * @coverage core.nls.ui
 */
public abstract class AbstractBundleSourceNewComposite extends AbstractFieldsSourceNewComposite {
  private Group m_propertyGroup;
  private PackageRootAndPackageSelectionDialogField m_propertyPackageField;
  private StringButtonDialogField m_propertyFileField;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractBundleSourceNewComposite(Composite parent, int style, JavaInfo root) {
    super(parent, style, root);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Property group
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final void createPropertyGroup() {
    m_propertyGroup = new Group(this, SWT.NONE);
    GridDataFactory.create(m_propertyGroup).grabH().fillH();
    GridLayoutFactory.create(m_propertyGroup).columns(3);
    m_propertyGroup.setText(Messages.AbstractBundleSourceNewComposite_propertiesGroup);
    {
      m_propertyPackageField =
          new PackageRootAndPackageSelectionDialogField(60,
              Messages.AbstractBundleSourceNewComposite_propertiesSourceFolder,
              Messages.AbstractBundleSourceNewComposite_propertiesSourceFolderBrowse,
              Messages.AbstractBundleSourceNewComposite_propertiesPackage,
              Messages.AbstractBundleSourceNewComposite_propertiesPackageBrowse);
      m_propertyPackageField.setDialogFieldListener(m_validateListener);
      m_propertyPackageField.doFillIntoGrid(m_propertyGroup, 3);
      // create property file field
      {
        m_propertyFileField = new StringButtonDialogField(new IStringButtonAdapter() {
          public void changeControlPressed(DialogField field) {
            ElementListSelectionDialog dialog =
                new ElementListSelectionDialog(getShell(), new JavaElementLabelProvider());
            dialog.setIgnoreCase(false);
            dialog.setTitle(Messages.AbstractBundleSourceNewComposite_propertiesChooseTitle);
            dialog.setMessage(Messages.AbstractBundleSourceNewComposite_propertiesChooseMessage);
            dialog.setElements(createFileListInput());
            dialog.setFilter("*.properties");
            // select file
            if (dialog.open() != Window.OK) {
              return;
            }
            IFile selectedFile = (IFile) dialog.getFirstResult();
            // update property file field
            m_propertyFileField.setText(selectedFile.getName());
          }

          /**
           * Return list of ".properties" files in current property package.
           */
          private Object[] createFileListInput() {
            try {
              // prepare current package
              IPackageFragment currentPackage = m_propertyPackageField.getPackage();
              if (currentPackage == null) {
                return ArrayUtils.EMPTY_OBJECT_ARRAY;
              }
              // check each non-Java file in current package
              List<IFile> result = new ArrayList<IFile>(1);
              Object[] nonJava = currentPackage.getNonJavaResources();
              for (int i = 0; i < nonJava.length; i++) {
                if (isPropertyFile(nonJava[i])) {
                  result.add((IFile) nonJava[i]);
                }
              }
              // return result
              return result.toArray();
            } catch (JavaModelException e) {
              DesignerPlugin.log(e);
              return new Object[0];
            }
          }

          /**
           * Check that given object if IFile with ".properties" extension.
           */
          private boolean isPropertyFile(Object o) {
            if (o instanceof IFile) {
              IFile file = (IFile) o;
              return ".properties".equals('.' + file.getFileExtension());
            }
            return false;
          }
        });
        m_propertyFileField.setDialogFieldListener(m_validateListener);
        m_propertyFileField.setLabelText(Messages.AbstractBundleSourceNewComposite_propertiesLabel);
        m_propertyFileField.setButtonLabel(Messages.AbstractBundleSourceNewComposite_propertiesChooseButton);
        createTextFieldControls(m_propertyGroup, m_propertyFileField, 3);
      }
    }
  }

  protected final void initializePropertyGroup() {
    m_propertyPackageField.setCompilationUnit(m_compilationUnit);
    m_propertyFileField.setText("messages.properties");
  }

  protected final void setPropertyGroupEnable(boolean enable) {
    UiUtils.changeControlEnable(m_propertyGroup, enable);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Property group: validation
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String KEY_PROPERTY_FOLDER = "KEY_PROPERTY_FOLDER";
  private static final String KEY_PROPERTY_PACKAGE = "KEY_PROPERTY_PACKAGE";
  private static final String KEY_PROPERTY_FILE = "KEY_PROPERTY_FILE";

  /**
   * Validate property fields.
   */
  protected void validatePropertyFields() {
    // validate source folder
    {
      IPackageFragmentRoot root = m_propertyPackageField.getRoot();
      if (root == null || !root.exists()) {
        setInvalid(
            KEY_PROPERTY_FOLDER,
            Messages.AbstractBundleSourceNewComposite_validatePropertiesInvalidSourceFolder);
      } else {
        setValid(KEY_PROPERTY_FOLDER);
      }
    }
    // validate package
    {
      IPackageFragment fragment = m_propertyPackageField.getPackage();
      if (fragment == null || !fragment.exists()) {
        setInvalid(
            KEY_PROPERTY_PACKAGE,
            Messages.AbstractBundleSourceNewComposite_validatePropertiesInvalidPackage);
      } else {
        setValid(KEY_PROPERTY_PACKAGE);
      }
    }
    // validate file name
    {
      String fileName = m_propertyFileField.getText();
      if (fileName.length() == 0) {
        setInvalid(
            KEY_PROPERTY_FILE,
            Messages.AbstractBundleSourceNewComposite_validatePropertiesFileEmpty);
      } else if (!fileName.endsWith(".properties")) {
        setInvalid(
            KEY_PROPERTY_FILE,
            Messages.AbstractBundleSourceNewComposite_validatePropertiesFileExtension);
      } else {
        setValid(KEY_PROPERTY_FILE);
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
    validatePropertyFields();
    super.validateAll();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parameters
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final void fillPropertyParameters(AbstractBundleSourceParameters parameters)
      throws Exception {
    parameters.m_propertySourceFolder = m_propertyPackageField.getRoot();
    parameters.m_propertyPackage = m_propertyPackageField.getPackage();
    parameters.m_propertyFileName = m_propertyFileField.getText();
    // prepare m_propertyBundleName
    {
      IPackageFragment property_package = m_propertyPackageField.getPackage();
      if (property_package != null) {
        String property_packageName = property_package.getElementName();
        String property_fileName = m_propertyFileField.getText();
        String property_bundleName =
            StringUtils.substring(property_fileName, 0, -".properties".length());
        if (property_packageName.length() != 0) {
          parameters.m_propertyBundleName = property_packageName + "." + property_bundleName;
        } else {
          parameters.m_propertyBundleName = property_bundleName;
        }
      }
    }
    // prepare m_propertyFileExists
    if (parameters.m_propertyPackage != null) {
      IFolder property_folder = (IFolder) parameters.m_propertyPackage.getUnderlyingResource();
      parameters.m_propertyFileExists =
          property_folder.getFile(parameters.m_propertyFileName).exists();
    }
  }
}
