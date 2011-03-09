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

import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.css.semantics.AbstractValue;
import org.eclipse.wb.internal.css.semantics.IValueListener;
import org.eclipse.wb.internal.css.semantics.SimpleValue;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * Abstract editor for {@link SimpleValue} that has {@link Text} and {@link Button}.
 * 
 * @author scheglov_ke
 * @coverage CSS.ui
 */
public abstract class AbstractTextButtonValueEditor extends AbstractValueEditor {
  protected final SimpleValue m_value;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractTextButtonValueEditor(StyleEditOptions options, String title, SimpleValue value) {
    super(options, title, value);
    m_value = value;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  private Text m_text;

  @Override
  public void doFillGrid(Composite parent, int numColumns) {
    requireColumns(4, numColumns);
    createTitleLabel(parent);
    // text
    {
      m_text = new Text(parent, SWT.BORDER);
      GridDataFactory.create(m_text).spanH(numColumns - 3).minHC(20).grabH().fill();
      // add listener
      m_text.addModifyListener(new ModifyListener() {
        public void modifyText(ModifyEvent e) {
          m_value.setValue(m_text.getText());
        }
      });
    }
    // button
    {
      final Button button = new Button(parent, SWT.NONE);
      button.setText("...");
      button.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          onButtonClick(button);
        }
      });
    }
    // add value listener
    m_value.addListener(new IValueListener() {
      public void changed(AbstractValue value) {
        updateControlsFromValue();
      }
    });
    // clear button
    createClearButton(parent);
  }

  private void updateControlsFromValue() {
    if (m_value.hasValue()) {
      String newValue = m_value.getValue();
      if (!m_text.getText().equals(newValue)) {
        m_text.setText(newValue);
      }
    } else {
      m_text.setText("");
    }
  }

  /**
   * Subclasses should override this method to handle click on the button.
   */
  protected abstract void onButtonClick(Button button);
}
