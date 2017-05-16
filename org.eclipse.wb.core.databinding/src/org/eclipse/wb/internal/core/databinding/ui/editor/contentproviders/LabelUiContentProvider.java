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
package org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders;

import org.eclipse.wb.internal.core.databinding.ui.UiUtils;
import org.eclipse.wb.internal.core.databinding.ui.editor.UiContentProviderAdapter;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Content provider for view two labels:
 * <p>
 * title <b>value</b>
 * </p>
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public final class LabelUiContentProvider extends UiContentProviderAdapter {
  private final String m_title;
  private final String m_value;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LabelUiContentProvider(String title, String value) {
    m_title = title;
    m_value = value;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  public int getNumberOfControls() {
    return 2;
  }

  public void createContent(Composite parent, int columns) {
    // create title label
    Label titleLable = new Label(parent, SWT.NONE);
    titleLable.setText(m_title);
    // create value bold label
    Label valueLabel = new Label(parent, SWT.NONE);
    GridDataFactory.create(valueLabel).fillH().grabH().spanH(columns - 1);
    UiUtils.setBoldFont(valueLabel);
    valueLabel.setText(m_value);
  }
}