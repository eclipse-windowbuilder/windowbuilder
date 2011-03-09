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
package org.eclipse.wb.internal.xwt.model.widgets;

import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;

/**
 * Model for {@link Button}.
 * 
 * @author sablin_aa
 * @coverage XWT.model.widgets
 */
public final class ButtonInfo extends ControlInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ButtonInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IObjectPresentation m_presentation = new StylePresentation(this) {
    @Override
    protected void initImages() throws Exception {
      addImage(SWT.CHECK, "wbp-meta/org/eclipse/swt/widgets/Button_check.gif");
      addImage(SWT.RADIO, "wbp-meta/org/eclipse/swt/widgets/Button_radio.gif");
    }
  };

  @Override
  public IObjectPresentation getPresentation() {
    return m_presentation;
  }
}
