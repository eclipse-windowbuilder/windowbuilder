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
package org.eclipse.wb.internal.swing.gef.policy.layout;

import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.AbsoluteComplexSelectionEditPolicy;
import org.eclipse.wb.internal.core.gef.policy.snapping.ComponentAttachmentInfo;
import org.eclipse.wb.internal.swing.model.layout.spring.SpringLayoutInfo;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormLayout;

/**
 * Selection policy for edit containers with {@link FormLayout}.
 * 
 * @author mitin_aa
 * @author scheglov_ke
 * @coverage swing.gef.policy
 */
public final class SpringSelectionEditPolicy extends AbsoluteComplexSelectionEditPolicy {
  private final SpringLayoutInfo m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SpringSelectionEditPolicy(SpringLayoutInfo layout) {
    super(layout);
    m_layout = layout;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Overrides
  //
  ////////////////////////////////////////////////////////////////////////////
  public Image getActionImage(String imageName) {
    return SpringLayoutInfo.getImage(imageName);
  }

  @Override
  protected ComponentAttachmentInfo getComponentAttachmentInfo(IAbstractComponentInfo widget,
      int side) throws Exception {
    return m_layout.getComponentAttachmentInfo(widget, side);
  }

  @Override
  protected void showSelection() {
    super.showSelection();
  }

  @Override
  protected void hideSelection() {
    super.hideSelection();
  }
}
