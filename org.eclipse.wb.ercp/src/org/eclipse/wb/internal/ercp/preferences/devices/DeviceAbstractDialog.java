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
package org.eclipse.wb.internal.ercp.preferences.devices;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.dialogfields.AbstractValidationTitleAreaDialog;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogFieldUtils;
import org.eclipse.wb.internal.core.utils.dialogfields.IStringButtonAdapter;
import org.eclipse.wb.internal.core.utils.dialogfields.StringButtonDialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.StringDialogField;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.ercp.devices.DeviceMessages;
import org.eclipse.wb.internal.ercp.devices.model.DeviceInfo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;

import java.io.FileInputStream;

/**
 * Abstract dialog for {@link DeviceInfo}.
 * 
 * @author scheglov_ke
 * @coverage ercp.device.ui
 */
public abstract class DeviceAbstractDialog extends AbstractValidationTitleAreaDialog {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DeviceAbstractDialog(String shellText, String titleText) {
    super(DesignerPlugin.getShell(),
        DesignerPlugin.getDefault(),
        shellText,
        titleText,
        null,
        DeviceMessages.DeviceAbstractDialog_message);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ///////////////////////////////////////////////////////////////////////////
  protected StringDialogField m_nameField;
  protected StringButtonDialogField m_imageField;
  protected StringDialogField m_displayField_x;
  protected StringDialogField m_displayField_y;
  protected StringDialogField m_displayField_width;
  protected StringDialogField m_displayField_height;

  /**
   * Creates controls on this dialog.
   */
  @Override
  protected void createControls(Composite container) {
    m_fieldsContainer = container;
    GridLayoutFactory.create(container).columns(3);
    // name
    {
      m_nameField = new StringDialogField();
      doCreateField(m_nameField, DeviceMessages.DeviceAbstractDialog_nameLabel);
    }
    // name
    {
      m_imageField = new StringButtonDialogField(new IStringButtonAdapter() {
        public void changeControlPressed(DialogField field) {
          FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
          fileDialog.setFilterPath(m_imageField.getText());
          String newPath = fileDialog.open();
          if (newPath != null) {
            m_imageField.setText(newPath);
          }
        }
      });
      m_imageField.setButtonLabel(DeviceMessages.DeviceAbstractDialog_browseImage);
      doCreateField(m_imageField, DeviceMessages.DeviceAbstractDialog_imagePathLabel);
    }
    // display: x
    {
      m_displayField_x = new StringDialogField();
      doCreateField(m_displayField_x, DeviceMessages.DeviceAbstractDialog_xDisplayLabel);
    }
    // display: y
    {
      m_displayField_y = new StringDialogField();
      doCreateField(m_displayField_y, DeviceMessages.DeviceAbstractDialog_yDisplayLabel);
    }
    // display: width
    {
      m_displayField_width = new StringDialogField();
      doCreateField(m_displayField_width, DeviceMessages.DeviceAbstractDialog_widthDisplayLabel);
    }
    // display: height
    {
      m_displayField_height = new StringDialogField();
      doCreateField(m_displayField_height, DeviceMessages.DeviceAbstractDialog_heightDisplayLabel);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected final String validate() {
    // validate name
    {
      String name = m_nameField.getText().trim();
      if (name.length() == 0) {
        return DeviceMessages.DeviceAbstractDialog_validateEmptyName;
      }
    }
    // validate image
    {
      String imagePath = m_imageField.getText().trim();
      if (imagePath.length() == 0) {
        return DeviceMessages.DeviceAbstractDialog_validateEmptyImagePath;
      }
      try {
        Image image = new Image(null, new FileInputStream(imagePath));
        image.dispose();
      } catch (Throwable e) {
        return DeviceMessages.DeviceAbstractDialog_validateBadImage + e.getMessage();
      }
    }
    // display
    {
      try {
        Integer.parseInt(m_displayField_x.getText());
        Integer.parseInt(m_displayField_y.getText());
        Integer.parseInt(m_displayField_width.getText());
        Integer.parseInt(m_displayField_height.getText());
      } catch (Throwable e) {
        return DeviceMessages.DeviceAbstractDialog_validateBadDisplay + e.getMessage();
      }
    }
    // OK
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private Composite m_fieldsContainer;

  /**
   * Configures given {@link DialogField} for specific of this dialog.
   */
  protected final void doCreateField(DialogField dialogField, String labelText) {
    dialogField.setLabelText(labelText);
    dialogField.setDialogFieldListener(m_validateListener);
    DialogFieldUtils.fillControls(m_fieldsContainer, dialogField, 3, 30);
  }
}
