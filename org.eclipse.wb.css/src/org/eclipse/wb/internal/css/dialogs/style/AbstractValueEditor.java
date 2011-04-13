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
package org.eclipse.wb.internal.css.dialogs.style;

import org.eclipse.wb.internal.css.Activator;
import org.eclipse.wb.internal.css.semantics.AbstractValue;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import java.text.MessageFormat;

/**
 * Abstract editor for {@link AbstractValue}.
 * 
 * @author scheglov_ke
 * @coverage CSS.ui
 */
public abstract class AbstractValueEditor {
  protected StyleEditOptions m_options;
  private final String m_title;
  private final AbstractValue m_value;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractValueEditor(StyleEditOptions options, String title, AbstractValue value) {
    m_options = options;
    m_title = title;
    m_value = value;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Fills with controls given parent with {@link GridLayout} with given number of columns.
   */
  public abstract void doFillGrid(Composite parent, int numColumns);

  /**
   * Clears value.
   */
  protected final void clearValue() {
    m_value.set(null);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Check that given number of columns is not less than required or throws {@link
   * IllegalArgumentException#}.
   */
  protected final void requireColumns(int requiredColumns, int givenColumns) {
    if (requiredColumns > givenColumns) {
      throw new IllegalArgumentException(MessageFormat.format(
          "{0} number of column required, but only {1} given.",
          requiredColumns,
          givenColumns));
    }
  }

  /**
   * Creates title label on given {@link Composite}
   */
  protected final void createTitleLabel(Composite parent) {
    new Label(parent, SWT.NONE).setText(m_title);
  }

  /**
   * Returns title for this editor.
   */
  protected final String getTitle() {
    return m_title;
  }

  /**
   * Creates "Clear" button on given {@link Composite}.
   */
  protected final void createClearButton(Composite parent) {
    createButtonControl(parent, "clear.gif", "Clear", new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        clearValue();
      }
    });
  }

  /**
   * Creates button control with given parameters and adds given {@link SelectionListener}.
   */
  public static Control createButtonControl(Composite parent,
      String imagePath,
      String tooltip,
      SelectionListener selectionListener) {
    Image image = Activator.getImage(imagePath);
    // create button
    ToolBar toolBar = new ToolBar(parent, SWT.FLAT | SWT.RIGHT);
    ToolItem toolItem = new ToolItem(toolBar, SWT.NONE);
    toolItem.setImage(image);
    toolItem.setToolTipText(tooltip);
    toolItem.addSelectionListener(selectionListener);
    return toolBar;
  }
}
