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

import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swing.model.property.editor.border.BorderDialog;
import org.eclipse.wb.internal.swing.model.property.editor.border.fields.AbstractBorderField;
import org.eclipse.wb.internal.swing.model.property.editor.border.fields.BooleanField;
import org.eclipse.wb.internal.swing.model.property.editor.border.fields.BorderField;
import org.eclipse.wb.internal.swing.model.property.editor.border.fields.ColorField;
import org.eclipse.wb.internal.swing.model.property.editor.border.fields.ComboField;
import org.eclipse.wb.internal.swing.model.property.editor.border.fields.IntegerField;
import org.eclipse.wb.internal.swing.model.property.editor.border.fields.RadioField;
import org.eclipse.wb.internal.swing.model.property.editor.border.fields.TextField;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import javax.swing.border.Border;

/**
 * Abstract editor for some {@link Border} type.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public abstract class AbstractBorderComposite extends Composite {
  private final String m_title;
  protected BorderDialog m_borderDialog;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractBorderComposite(Composite parent, String title) {
    super(parent, SWT.NONE);
    m_title = title;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Initializes this {@link AbstractBorderComposite}.
   */
  public void initialize(BorderDialog borderDialog, AstEditor editor) {
    m_borderDialog = borderDialog;
  }

  /**
   * @return the title to display for user.
   */
  public final String getTitle() {
    return m_title;
  }

  /**
   * Sets the {@link Border} to edit.
   * 
   * @return <code>true</code> if this {@link AbstractBorderComposite} understands given
   *         {@link Border}.
   */
  public abstract boolean setBorder(Border border) throws Exception;

  /**
   * @return the source for updated {@link Border}.
   */
  public abstract String getSource() throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Components
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Binds given {@link AbstractBorderField}, so that when {@link SWT#Selection} event issued, we
   * notify {@link BorderDialog}.
   */
  private void bindField(AbstractBorderField field) {
    field.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        m_borderDialog.borderUpdated();
      }
    });
  }

  /**
   * @return the bound {@link TextField}.
   */
  protected final TextField createTextField(String label) {
    TextField field = new TextField(this, label);
    bindField(field);
    return field;
  }

  /**
   * @return the bound {@link IntegerField}.
   */
  protected final IntegerField createIntegerField(String label) {
    IntegerField field = new IntegerField(this, label);
    bindField(field);
    return field;
  }

  /**
   * @return the bound {@link RadioField}.
   */
  protected final RadioField createRadioField(String label,
      Class<?> clazz,
      String[] fields,
      String[] titles) {
    RadioField field = new RadioField(this, label, clazz, fields, titles);
    bindField(field);
    return field;
  }

  /**
   * @return the bound {@link ComboField}.
   */
  protected final ComboField createComboField(String label,
      Class<?> clazz,
      String[] fields,
      String[] titles) {
    ComboField field = new ComboField(this, label, clazz, fields, titles);
    bindField(field);
    return field;
  }

  /**
   * @return the bound {@link BooleanField}.
   */
  protected final BooleanField createBooleanField(String label, String[] titles) {
    BooleanField field = new BooleanField(this, label, titles);
    bindField(field);
    return field;
  }

  /**
   * @return the bound {@link ColorField}.
   */
  protected final ColorField createColorField(String label) {
    ColorField field = new ColorField(this, label);
    bindField(field);
    return field;
  }

  /**
   * @return the bound {@link BorderField}.
   */
  protected final BorderField createBorderField(String label, String buttonText) {
    BorderField field = new BorderField(this, label, buttonText);
    bindField(field);
    return field;
  }
}
