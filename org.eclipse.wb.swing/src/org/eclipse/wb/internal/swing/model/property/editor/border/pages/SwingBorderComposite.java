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
package org.eclipse.wb.internal.swing.model.property.editor.border.pages;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.swing.model.property.editor.font.FontInfo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;

import java.awt.Font;
import java.util.Iterator;
import java.util.Set;

import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.Border;

/**
 * Implementation of {@link AbstractBorderComposite} that sets {@link Border} from {@link UIManager}
 * .
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class SwingBorderComposite extends AbstractBorderComposite {
  private final List m_bordersList;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SwingBorderComposite(Composite parent) {
    super(parent, "Swing");
    GridLayoutFactory.create(this);
    m_bordersList = new List(this, SWT.BORDER | SWT.V_SCROLL);
    GridDataFactory.create(m_bordersList).hintVC(10).grab().fill();
    // fill Border's
    prepareBorders();
    for (String key : m_borderKeys) {
      m_bordersList.add(key);
    }
    // selection listener
    m_bordersList.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event e) {
        m_borderDialog.borderUpdated();
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean setBorder(Border border) throws Exception {
    // note, that this algorithm is not ideal, because we can not identify "key" by Border,
    // and we don't have AST Expression, so we try to do our best, but can fail...
    if (border != null) {
      String borderClassName = border.getClass().getName();
      if (borderClassName.indexOf('$') != -1) {
        for (int i = 0; i < m_borders.size(); i++) {
          if (m_borders.get(i).getClass() == border.getClass()) {
            m_bordersList.select(i);
            // when setBorder() invoked, our page may not have size yet, so wait for finishing
            DesignerPlugin.getStandardDisplay().asyncExec(new Runnable() {
              public void run() {
                m_bordersList.showSelection();
              }
            });
            // OK, this is our Border
            return true;
          }
        }
      }
    }
    // no, we don't know this Border
    m_bordersList.deselectAll();
    return false;
  }

  @Override
  public String getSource() throws Exception {
    int index = m_bordersList.getSelectionIndex();
    if (index != -1) {
      String key = m_borderKeys.get(index);
      return "javax.swing.UIManager.getBorder(\"" + key + "\")";
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal
  //
  ////////////////////////////////////////////////////////////////////////////
  private static java.util.List<String> m_borderKeys;
  private static java.util.List<Border> m_borders;

  /**
   * Prepares {@link FontInfo}'s for {@link Font}'s from {@link UIManager}.
   */
  private static synchronized void prepareBorders() {
    if (m_borders == null) {
      m_borderKeys = Lists.newArrayList();
      m_borders = Lists.newArrayList();
      UIDefaults defaults = UIManager.getLookAndFeelDefaults();
      // prepare set of all String keys in UIManager
      Set<String> allKeys = Sets.newTreeSet();
      for (Iterator<?> I = defaults.keySet().iterator(); I.hasNext();) {
        Object key = I.next();
        if (key instanceof String) {
          allKeys.add((String) key);
        }
      }
      // add Border for each key
      for (String key : allKeys) {
        Border border = defaults.getBorder(key);
        if (border != null) {
          m_borderKeys.add(key);
          m_borders.add(border);
        }
      }
    }
  }
}
