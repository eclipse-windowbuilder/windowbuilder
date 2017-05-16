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
package org.eclipse.wb.internal.core.model.property.editor.geometry;

import org.eclipse.wb.core.controls.CSpinner;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.ModelMessages;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import java.lang.reflect.Field;

/**
 * Abstract {@link Dialog} for editing different toolkit geometry objects.
 *
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public abstract class AbstractGeometryDialog extends Dialog {
  private final String m_title;
  private final Object m_object;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractGeometryDialog(String title, Object object) {
    super(DesignerPlugin.getShell());
    m_title = title;
    m_object = object;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  private Composite m_area;

  @Override
  protected final Control createDialogArea(Composite parent) {
    m_area = new Composite(parent, SWT.NONE);
    GridDataFactory.create(m_area).grab().fill();
    GridLayoutFactory.create(m_area).margins(10).columns(3);
    //
    createEditors();
    return m_area;
  }

  /**
   * Creates editors using {@link #createEditor(String, String)}.
   */
  protected abstract void createEditors();

  @Override
  protected final void createButtonsForButtonBar(Composite parent) {
    createButton(
        parent,
        IDialogConstants.IGNORE_ID,
        ModelMessages.AbstractGeometryDialog_defaultButton,
        false);
    super.createButtonsForButtonBar(parent);
  }

  @Override
  protected final void buttonPressed(int buttonId) {
    if (buttonId == IDialogConstants.IGNORE_ID) {
      setReturnCode(buttonId);
      close();
    }
    super.buttonPressed(buttonId);
  }

  @Override
  protected final void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(m_title);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final void createEditor(String title, String fieldName) {
    // title
    {
      Label titleLabel = new Label(m_area, SWT.NONE);
      GridDataFactory.create(titleLabel).hintHC(15);
      titleLabel.setText(title);
    }
    // spinner
    {
      final CSpinner spinner = new CSpinner(m_area, SWT.BORDER);
      GridDataFactory.create(spinner).hintHC(8).grabH().fillH();
      spinner.setMinimum(0);
      spinner.setMaximum(Integer.MAX_VALUE);
      // bind control to field
      try {
        final Field field = ReflectionUtils.getFieldByName(m_object.getClass(), fieldName);
        // copy value from Object to control
        {
          int value = field.getInt(m_object);
          spinner.setSelection(value);
        }
        // add listeners
        spinner.addListener(SWT.Selection, new Listener() {
          public void handleEvent(Event event) {
            try {
              field.setInt(m_object, spinner.getSelection());
            } catch (Throwable e) {
            }
          }
        });
      } catch (Throwable e) {
      }
    }
    // pixels
    {
      Label pixelsLabel = new Label(m_area, SWT.NONE);
      pixelsLabel.setText(ModelMessages.AbstractGeometryDialog_pixelsLabel);
    }
  }
}