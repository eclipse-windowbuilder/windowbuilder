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
package org.eclipse.wb.internal.rcp.model.layout;

import org.eclipse.wb.core.editor.actions.assistant.AbstractAssistantPage;
import org.eclipse.wb.internal.swt.model.layout.ILayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.LayoutAssistantSupport;

import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * Assistant pages provider for {@link StackLayout}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.layout
 */
public final class StackLayoutAssistant extends LayoutAssistantSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StackLayoutAssistant(ILayoutInfo<?> layout) {
    super(layout);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Pages
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected AbstractAssistantPage createLayoutPage(Composite parent) {
    return new StackLayoutAssistantPage(parent, m_layout);
  }
}
