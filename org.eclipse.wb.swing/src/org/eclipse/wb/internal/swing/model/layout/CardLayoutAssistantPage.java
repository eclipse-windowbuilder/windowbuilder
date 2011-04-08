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
package org.eclipse.wb.internal.swing.model.layout;

import org.eclipse.wb.core.editor.actions.assistant.AbstractAssistantPage;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.swing.model.ModelMessages;

import org.eclipse.swt.widgets.Composite;

import java.awt.CardLayout;

/**
 * Layout assistant for {@link CardLayout}.
 * 
 * @author lobas_av
 * @coverage swing.assistant
 */
public final class CardLayoutAssistantPage extends AbstractAssistantPage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CardLayoutAssistantPage(Composite parent, Object selection) {
    super(parent, selection);
    GridLayoutFactory.create(this).columns(2);
    addIntegerProperty(this, "hgap", ModelMessages.CardLayoutAssistantPage_horizontalGap);
    addIntegerProperty(this, "vgap", ModelMessages.CardLayoutAssistantPage_verticalGap);
  }
}