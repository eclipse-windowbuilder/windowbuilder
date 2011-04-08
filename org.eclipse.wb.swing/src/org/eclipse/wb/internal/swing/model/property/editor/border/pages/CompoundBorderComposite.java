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
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.swing.model.ModelMessages;
import org.eclipse.wb.internal.swing.model.property.editor.border.BorderDialog;
import org.eclipse.wb.internal.swing.model.property.editor.border.fields.BorderField;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

/**
 * Implementation of {@link AbstractBorderComposite} that sets {@link CompoundBorder}.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class CompoundBorderComposite extends AbstractBorderComposite {
  private final BorderField m_outsideField;
  private final BorderField m_insideField;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CompoundBorderComposite(Composite parent) {
    super(parent, "CompoundBorder");
    GridLayoutFactory.create(this);
    m_outsideField =
        createBorderField(
            ModelMessages.CompoundBorderComposite_outsideBorder,
            ModelMessages.CompoundBorderComposite_outsideEdit);
    m_insideField =
        createBorderField(
            ModelMessages.CompoundBorderComposite_insideBorder,
            ModelMessages.CompoundBorderComposite_insideEdit);
    {
      Button swapButton = new Button(this, SWT.NONE);
      GridDataFactory.create(swapButton).grabH().alignHR().hintHC(10);
      swapButton.setText(ModelMessages.CompoundBorderComposite_swap);
      swapButton.addListener(SWT.Selection, new Listener() {
        public void handleEvent(Event e) {
          ExecutionUtils.runLog(new RunnableEx() {
            public void run() throws Exception {
              Border outsideBorder = m_outsideField.getBorder();
              m_outsideField.setBorder(m_insideField.getBorder());
              m_insideField.setBorder(outsideBorder);
              m_borderDialog.borderUpdated();
            }
          });
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void initialize(BorderDialog borderDialog, AstEditor editor) {
    super.initialize(borderDialog, editor);
    m_outsideField.setEditor(editor);
    m_insideField.setEditor(editor);
  }

  @Override
  public boolean setBorder(Border border) throws Exception {
    if (border instanceof CompoundBorder) {
      CompoundBorder ourBorder = (CompoundBorder) border;
      m_outsideField.setBorder(ourBorder.getOutsideBorder());
      m_insideField.setBorder(ourBorder.getInsideBorder());
      // OK, this is our Border
      return true;
    } else {
      // no, we don't know this Border
      return false;
    }
  }

  @Override
  public String getSource() throws Exception {
    String outsideSource = m_outsideField.getSource();
    String insideSource = m_insideField.getSource();
    if (outsideSource == null && insideSource == null) {
      return "new javax.swing.border.CompoundBorder()";
    }
    return "new javax.swing.border.CompoundBorder(" + outsideSource + ", " + insideSource + ")";
  }
}
