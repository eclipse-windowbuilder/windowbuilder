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
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.css.semantics.AbstractSidedProperty;
import org.eclipse.wb.internal.css.semantics.AbstractValue;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

/**
 * Abstract group for editing {@link AbstractSidedProperty}.
 * 
 * @author scheglov_ke
 * @coverage CSS.ui
 */
public abstract class AbstractSidedPropertyGroup extends Group {
  private static final String[] SIDE_TITLES = new String[]{"Top:", "Right:", "Bottom:", "Left:"};
  protected final StyleEditOptions m_options;
  private final int m_numColumns;
  private final AbstractSidedProperty m_property;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractSidedPropertyGroup(Composite parent,
      int style,
      StyleEditOptions options,
      String title,
      int numColumns,
      AbstractSidedProperty property) {
    super(parent, style);
    m_options = options;
    m_numColumns = numColumns;
    m_property = property;
    GridLayoutFactory.create(this).columns(numColumns);
    setText(title);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Enable subclassing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void checkSubclass() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Side parts creation
  //
  ////////////////////////////////////////////////////////////////////////////
  private final AbstractValueEditor[] m_editors = new AbstractValueEditor[4];

  /**
   * Invokes {@link #createPart(AbstractValue, String)} for each side.
   */
  protected final void createParts() {
    createTopButtons();
    for (int side = 0; side < 4; side++) {
      AbstractValueEditor editor = createPart(m_property.getValue(side), SIDE_TITLES[side]);
      editor.doFillGrid(this, m_numColumns);
      m_editors[side] = editor;
    }
  }

  protected abstract AbstractValueEditor createPart(AbstractValue value, String title);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final void createTopButtons() {
    createSameForAll();
    createClearAll();
  }

  /**
   * Creates "Same for all" button.
   */
  private final void createSameForAll() {
    Control control =
        AbstractValueEditor.createButtonControl(
            this,
            "synced.gif",
            "Copy 'top' value into all sides",
            new SelectionAdapter() {
              @Override
              public void widgetSelected(SelectionEvent e) {
                String topValue = m_property.getValue(0).get();
                for (int side = 1; side < 4; side++) {
                  m_property.getValue(side).set(topValue);
                }
              }
            });
    GridDataFactory.create(control).spanH(m_numColumns - 1).alignHR();
  }

  /**
   * Creates "Clear all" button.
   */
  private final void createClearAll() {
    AbstractValueEditor.createButtonControl(this, "clear.gif", "Clear all", new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        for (int side = 0; side < 4; side++) {
          m_property.getValue(side).set(null);
        }
      }
    });
  }
}
