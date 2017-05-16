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
package org.eclipse.wb.internal.core.utils.dialogfields;

import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * Dialog field containing label, text and tool item.
 *
 * @author scheglov_ke
 */
public class StringItemDialogField extends StringDialogField {
  private final IStringItemAdapter m_adapter;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StringItemDialogField(IStringItemAdapter adapter) {
    m_adapter = adapter;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Fill
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public int getNumberOfControls() {
    return 3;
  }

  @Override
  public Control[] doFillIntoGrid(Composite parent, int nColumns) {
    if (nColumns < getNumberOfControls()) {
      throw new IllegalArgumentException("given number of columns is too small");
    }
    //
    Label label = getLabelControl(parent);
    GridDataFactory.create(label).fillH();
    //
    final Text text = getTextControl(parent);
    GridDataFactory.create(text).grabH().spanH(nColumns - 2).fillH();
    //
    ToolBar toolBar = new ToolBar(parent, SWT.FLAT | SWT.RIGHT);
    GridDataFactory.create(toolBar).alignVM();
    {
      ToolItem clearItem = new ToolItem(toolBar, SWT.NONE);
      clearItem.setImage(m_itemImage);
      clearItem.setToolTipText(m_itemToolTip);
      clearItem.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          m_adapter.itemPressed(StringItemDialogField.this);
        }
      });
    }
    //
    return new Control[]{label, text, toolBar};
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clear control
  //
  ////////////////////////////////////////////////////////////////////////////
  private Image m_itemImage;
  private String m_itemToolTip;

  public void setItemImage(Image clearImage) {
    m_itemImage = clearImage;
  }

  public void setItemToolTip(String clearToolTip) {
    m_itemToolTip = clearToolTip;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Adapter
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Change listener used by {@link StringItemDialogField}.
   *
   * @author scheglov_ke
   */
  public interface IStringItemAdapter {
    void itemPressed(DialogField field);
  }
}
