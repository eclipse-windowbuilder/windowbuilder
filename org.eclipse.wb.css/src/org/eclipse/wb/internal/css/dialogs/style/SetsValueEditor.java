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
import org.eclipse.wb.internal.css.semantics.AbstractValue;
import org.eclipse.wb.internal.css.semantics.IValueListener;
import org.eclipse.wb.internal.css.semantics.SimpleValue;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Editor for {@link SimpleValue} for set values.
 * 
 * It supports several exclusive sets. For example for "text-decoration" we need support for "none"
 * (set with single element) and "underline"/"overline"/etc (set with several elements).
 * 
 * @author scheglov_ke
 * @coverage CSS.ui
 */
public final class SetsValueEditor extends AbstractValueEditor {
  private final SimpleValue m_value;
  private final String[][] m_sets;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SetsValueEditor(StyleEditOptions options, SimpleValue value, String title, String[][] sets) {
    super(options, title, value);
    m_value = value;
    m_sets = sets;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void doFillGrid(Composite parent, int numColumns) {
    requireColumns(1, numColumns);
    //
    final Group group = new Group(parent, SWT.NONE);
    GridDataFactory.create(group).spanH(numColumns);
    GridLayoutFactory.create(group);
    group.setText(getTitle());
    // create check boxes
    for (int setIndex = 0; setIndex < m_sets.length; setIndex++) {
      final String[] set = m_sets[setIndex];
      for (int elementIndex = 0; elementIndex < set.length; elementIndex++) {
        String element = set[elementIndex];
        //
        Button button = new Button(group, SWT.CHECK);
        button.setText(element);
        button.setData("set", set);
        button.setData("element", element);
        button.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            String value = "";
            // modify selection, prepare result value
            Control[] children = group.getChildren();
            for (int i = 0; i < children.length; i++) {
              Button child = (Button) children[i];
              // remove selection for all check boxes from other sets
              if (child.getData("set") != set) {
                child.setSelection(false);
              }
              // add value of check box to the result
              if (child.getSelection()) {
                value += " " + child.getData("element");
              }
            }
            // update value
            m_value.set(value);
          }
        });
      }
    }
    // add value listener
    m_value.addListener(new IValueListener() {
      public void changed(AbstractValue value) {
        String[] elements = StringUtils.split(m_value.get());
        Control[] children = group.getChildren();
        for (int i = 0; i < children.length; i++) {
          Button child = (Button) children[i];
          boolean selected = ArrayUtils.contains(elements, child.getData("element"));
          child.setSelection(selected);
        }
      }
    });
  }
}
