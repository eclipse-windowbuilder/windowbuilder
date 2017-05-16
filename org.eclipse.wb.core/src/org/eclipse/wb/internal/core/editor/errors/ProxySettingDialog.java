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
package org.eclipse.wb.internal.core.editor.errors;

import org.eclipse.wb.internal.core.editor.Messages;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import java.util.Properties;

/**
 * Dialog for set proxy settings.
 *
 * @author mitin_aa
 * @coverage core.editor.errors
 */
public class ProxySettingDialog extends Dialog {
  private static final String PROXY_SET = "http.proxySet";
  private static final String PROXY_HOST = "http.proxyHost";
  private static final String PROXY_PORT = "http.proxyPort";
  private Text m_portText;
  private Text m_addressText;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ProxySettingDialog(Shell parentShell) {
    super(parentShell);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Contents
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Control createDialogArea(Composite parent) {
    Composite area = (Composite) super.createDialogArea(parent);
    Label hintLabel = new Label(area, SWT.NONE);
    hintLabel.setText(Messages.ProxySettingDialog_hint);
    {
      Group group = new Group(area, SWT.NONE);
      group.setText(Messages.ProxySettingDialog_groupText);
      GridDataFactory.modify(group).grab().fill();
      GridLayoutFactory.create(group).columns(2);
      {
        {
          Label label = new Label(group, SWT.NONE);
          label.setText(Messages.ProxySettingDialog_addressLabel);
          m_addressText = new Text(group, SWT.BORDER);
          m_addressText.setText(getSystemProperty(PROXY_HOST));
          GridDataFactory.create(m_addressText).grabH().fillH();
        }
        {
          Label label = new Label(group, SWT.NONE);
          label.setText(Messages.ProxySettingDialog_portLabel);
          m_portText = new Text(group, SWT.BORDER);
          m_portText.setText(getSystemProperty(PROXY_PORT));
          GridDataFactory.create(m_portText).grabH().fillH();
        }
      }
    }
    return area;
  }

  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(Messages.ProxySettingDialog_title);
  }

  @Override
  protected void okPressed() {
    String addressString = m_addressText.getText().trim();
    String portString = m_portText.getText().trim();
    // user removing proxy settings
    if (addressString.length() == 0 && portString.length() == 0) {
      setSystemProperty(PROXY_SET, null);
      setSystemProperty(PROXY_HOST, null);
      setSystemProperty(PROXY_PORT, null);
    } else {
      // user sets proxy values
      boolean allOK = false;
      try {
        int port = Integer.parseInt(portString);
        allOK = port > 0 && port < 65536;
      } catch (Throwable e) {
        MessageDialog.openError(
            getShell(),
            Messages.ProxySettingDialog_errPortTitle,
            Messages.ProxySettingDialog_errPortMessage);
        return;
      }
      allOK = addressString.length() > 0;
      if (!allOK) {
        MessageDialog.openError(
            getShell(),
            Messages.ProxySettingDialog_errAddressTitle,
            Messages.ProxySettingDialog_arrAddressMessage);
        return;
      }
      setSystemProperty(PROXY_SET, "true");
      setSystemProperty(PROXY_HOST, addressString);
      setSystemProperty(PROXY_PORT, portString);
    }
    super.okPressed();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Helper utils
   */
  private static String getSystemProperty(String key) {
    Object value = System.getProperties().get(key);
    if (value instanceof String) {
      return (String) value;
    }
    return null;
    //return License.getLicenseFile().getValue(key);
  }

  private static void setSystemProperty(String key, String value) {
    Properties properties = System.getProperties();
    if (value != null && value.length() > 0) {
      properties.setProperty(key, value);
    } else {
      properties.remove(key);
    }
    //License.getLicenseFile().putValue(key, value);
  }
}
