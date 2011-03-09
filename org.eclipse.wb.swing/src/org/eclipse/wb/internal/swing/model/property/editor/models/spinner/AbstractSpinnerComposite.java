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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import javax.swing.JSpinner;
import javax.swing.SpinnerModel;

/**
 * Abstract editor for some {@link SpinnerModel} type.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
abstract class AbstractSpinnerComposite extends Composite {
  protected final SpinnerModelDialog m_modelDialog;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractSpinnerComposite(Composite parent, SpinnerModelDialog modelDialog) {
    super(parent, SWT.NONE);
    m_modelDialog = modelDialog;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the title to display.
   */
  public abstract String getTitle();

  /**
   * Sets the {@link SpinnerModel} to display/edit.
   * 
   * @return <code>true</code> if this {@link AbstractSpinnerComposite} understands given model.
   */
  public abstract boolean setModel(SpinnerModel model);

  /**
   * @return the error message, or <code>null</code> if model configured correctly.
   */
  public abstract String validate();

  /**
   * @return the {@link SpinnerModel} that corresponds to this {@link AbstractSpinnerComposite} and
   *         configuration. This {@link SpinnerModel} is used later for preview in {@link JSpinner}.
   */
  public abstract SpinnerModel getModel();

  /**
   * @return the source to apply.
   */
  public abstract String getSource() throws Exception;
}
