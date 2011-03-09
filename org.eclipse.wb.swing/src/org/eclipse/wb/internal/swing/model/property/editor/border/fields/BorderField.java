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
package org.eclipse.wb.internal.swing.model.property.editor.border.fields;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.swing.model.property.editor.border.BorderDialog;
import org.eclipse.wb.internal.swing.model.property.editor.border.pages.AbstractBorderComposite;
import org.eclipse.wb.internal.swing.model.property.editor.border.pages.BevelBorderComposite;
import org.eclipse.wb.internal.swing.model.property.editor.border.pages.CompoundBorderComposite;
import org.eclipse.wb.internal.swing.model.property.editor.border.pages.EmptyBorderComposite;
import org.eclipse.wb.internal.swing.model.property.editor.border.pages.EtchedBorderComposite;
import org.eclipse.wb.internal.swing.model.property.editor.border.pages.LineBorderComposite;
import org.eclipse.wb.internal.swing.model.property.editor.border.pages.MatteBorderComposite;
import org.eclipse.wb.internal.swing.model.property.editor.border.pages.SoftBevelBorderComposite;
import org.eclipse.wb.internal.swing.model.property.editor.border.pages.SwingBorderComposite;
import org.eclipse.wb.internal.swing.model.property.editor.border.pages.TitledBorderComposite;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import java.util.Iterator;
import java.util.List;

import javax.swing.border.Border;

/**
 * {@link AbstractBorderField} for editing inner {@link Border}.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class BorderField extends AbstractBorderField {
  private AstEditor m_editor;
  private Text m_text;
  private Border m_border;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BorderField(Composite parent, String labelText, String buttonText) {
    super(parent, 2, labelText);
    {
      m_text = new Text(this, SWT.BORDER | SWT.READ_ONLY);
      GridDataFactory.create(m_text).hintHC(35);
    }
    {
      Button button = new Button(this, SWT.NONE);
      GridDataFactory.create(button).hintHC(10);
      button.setText(buttonText);
      button.addListener(SWT.Selection, new Listener() {
        public void handleEvent(Event e) {
          BorderDialog borderDialog = new BorderDialog(getShell(), m_editor);
          borderDialog.setBorderModified(m_border != null);
          borderDialog.setBorder(m_border);
          if (borderDialog.open() == Window.OK) {
            m_border = borderDialog.getBorder();
            showBorder();
            notifyListeners(SWT.Selection, new Event());
          }
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the {@link AstEditor} to use with {@link BorderDialog}.
   */
  public void setEditor(AstEditor editor) {
    m_editor = editor;
  }

  /**
   * @return current {@link Border}.
   */
  public Border getBorder() {
    return m_border;
  }

  /**
   * Sets the {@link Border} value.
   */
  public void setBorder(Border border) throws Exception {
    m_border = border;
    showBorder();
  }

  @Override
  public String getSource() throws Exception {
    // try to use AbstractBorderComposite's to convert Border into source
    if (m_border != null) {
      for (Class<?> compositeClass : COMPOSITE_CLASSES) {
        AbstractBorderComposite borderComposite = getBorderComposite(compositeClass);
        try {
          if (borderComposite.setBorder(m_border)) {
            return borderComposite.getSource();
          }
        } finally {
          returnBorderComposite(borderComposite);
        }
      }
    }
    // no, we don't understand this Border
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Borders
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final Shell INVISIBLE_SHELL = new Shell();
  private static final Class<?>[] COMPOSITE_CLASSES = {
      BevelBorderComposite.class,
      CompoundBorderComposite.class,
      EmptyBorderComposite.class,
      EtchedBorderComposite.class,
      LineBorderComposite.class,
      MatteBorderComposite.class,
      SoftBevelBorderComposite.class,
      TitledBorderComposite.class,
      SwingBorderComposite.class,};
  private static final List<AbstractBorderComposite> m_borderComposites = Lists.newLinkedList();

  /**
   * Note, that we can not reuse {@link AbstractBorderComposite}'s, so when we get some
   * {@link AbstractBorderComposite}, we should return it back, else new instance will be created.
   * 
   * @return the instance free of {@link AbstractBorderComposite}.
   */
  private static final AbstractBorderComposite getBorderComposite(Class<?> compositeClass)
      throws Exception {
    for (Iterator<AbstractBorderComposite> I = m_borderComposites.iterator(); I.hasNext();) {
      AbstractBorderComposite borderComposite = I.next();
      if (borderComposite.getClass() == compositeClass) {
        I.remove();
        return borderComposite;
      }
    }
    // create new instance
    return (AbstractBorderComposite) compositeClass.getConstructor(Composite.class).newInstance(
        INVISIBLE_SHELL);
  }

  /**
   * Returns given {@link AbstractBorderComposite} to the list of available.
   */
  private static final void returnBorderComposite(AbstractBorderComposite borderComposite) {
    m_borderComposites.add(borderComposite);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Shows current value of {@link #m_border}.
   */
  private void showBorder() {
    if (m_border != null) {
      m_text.setText(m_border.getClass().getName());
    } else {
      m_text.setText("");
    }
  }
}
