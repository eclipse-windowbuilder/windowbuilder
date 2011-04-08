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
package org.eclipse.wb.internal.swing.model.property.editor.models.spinner;

import org.eclipse.wb.internal.core.model.property.converter.StringArrayConverter;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.swing.model.ModelMessages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import org.apache.commons.lang.StringUtils;

import javax.swing.SpinnerListModel;
import javax.swing.SpinnerModel;

/**
 * Implementation of {@link AbstractSpinnerComposite} for {@link SpinnerListModel}.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
final class ListSpinnerComposite extends AbstractSpinnerComposite {
  private final Text m_textWidget;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ListSpinnerComposite(Composite parent, SpinnerModelDialog modelDialog) {
    super(parent, modelDialog);
    GridLayoutFactory.create(this);
    // Text with items
    {
      new Label(this, SWT.NONE).setText(ModelMessages.ListSpinnerComposite_itemsLabel);
      {
        m_textWidget = new Text(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        GridDataFactory.create(m_textWidget).spanH(2).grab().fill().hintC(50, 8);
        // update preview on modify
        m_textWidget.addListener(SWT.Modify, new Listener() {
          public void handleEvent(Event event) {
            m_modelDialog.validateAll();
          }
        });
      }
      new Label(this, SWT.NONE).setText(ModelMessages.ListSpinnerComposite_hint);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getTitle() {
    return ModelMessages.ListSpinnerComposite_title;
  }

  @Override
  public boolean setModel(SpinnerModel model) {
    if (model instanceof SpinnerListModel) {
      SpinnerListModel listModel = (SpinnerListModel) model;
      String text = StringUtils.join(listModel.getList().iterator(), "\n");
      m_textWidget.setText(text);
      // OK, this is our model
      return true;
    }
    return false;
  }

  @Override
  public String validate() {
    return null;
  }

  @Override
  public SpinnerModel getModel() {
    String[] items = getItems();
    return new SpinnerListModel(items);
  }

  @Override
  public String getSource() throws Exception {
    String[] items = getItems();
    String itemsSource = StringArrayConverter.INSTANCE.toJavaSource(null, items);
    return "new javax.swing.SpinnerListModel(" + itemsSource + ")";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the items entered by user into {@link #m_textWidget}.
   */
  private String[] getItems() {
    return StringUtils.split(m_textWidget.getText(), "\r\n");
  }
}
