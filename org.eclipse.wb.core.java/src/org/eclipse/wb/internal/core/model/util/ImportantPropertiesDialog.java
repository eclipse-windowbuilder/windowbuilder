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
package org.eclipse.wb.internal.core.model.util;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.ModelMessages;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.PropertyManager;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ResizableDialog;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import java.util.List;

/**
 * Dialog that show important (preferred and system) properties.<br>
 * We use it directly after adding new {@link JavaInfo}.
 *
 * @author scheglov_ke
 * @coverage core.model.util.ui
 */
public class ImportantPropertiesDialog extends ResizableDialog {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Activation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Schedules opening {@link ImportantPropertiesDialog} after adding given {@link JavaInfo} to
   * parent.
   */
  public static void scheduleImportantProperties(final JavaInfo javaInfo) {
    IPreferenceStore preferences = javaInfo.getDescription().getToolkit().getPreferences();
    if (preferences.getBoolean(IPreferenceConstants.P_GENERAL_IMPORTANT_PROPERTIES_AFTER_ADD)
        && javaInfo.getArbitraryValue(JavaInfo.FLAG_MANUAL_COMPONENT) == Boolean.TRUE) {
      Display.getDefault().asyncExec(new Runnable() {
        public void run() {
          ImportantPropertiesDialog dialog =
              new ImportantPropertiesDialog(DesignerPlugin.getShell(), javaInfo);
          dialog.open();
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final JavaInfo m_javaInfo;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private ImportantPropertiesDialog(Shell parentShell, JavaInfo javaInfo) {
    super(parentShell, DesignerPlugin.getDefault());
    m_javaInfo = javaInfo;
    setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  private PropertyTable m_propertyTable;
  private final ObjectEventListener m_refreshListener = new ObjectEventListener() {
    @Override
    public void refreshed() throws Exception {
      m_propertyTable.redraw();
    }
  };

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite area = (Composite) super.createDialogArea(parent);
    area.setLayout(new GridLayout());
    //
    m_propertyTable = new PropertyTable(area, SWT.BORDER);
    GridDataFactory.create(m_propertyTable).grab().fill().hintC(55, 20);
    // install refresh listener
    m_javaInfo.addBroadcastListener(m_refreshListener);
    // show important properties
    try {
      showImportantProperties();
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
    //
    return area;
  }

  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(ModelMessages.ImportantPropertiesDialog_title);
  }

  @Override
  public boolean close() {
    m_javaInfo.removeBroadcastListener(m_refreshListener);
    return super.close();
  }

  @Override
  protected void okPressed() {
    m_propertyTable.deactivateEditor(true);
    super.okPressed();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  private void showImportantProperties() throws Exception {
    List<Property> importantProperties = Lists.newArrayList();
    for (Property property : m_javaInfo.getProperties()) {
      PropertyCategory category = PropertyManager.getCategory(property);
      if (category.isPreferred() || category.isSystem()) {
        importantProperties.add(property);
      }
    }
    m_propertyTable.setInput(importantProperties.toArray(new Property[importantProperties.size()]));
  }
}
