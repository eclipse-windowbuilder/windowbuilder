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
package org.eclipse.wb.internal.swing.java6.gef;

import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.AbsoluteComplexSelectionEditPolicy;
import org.eclipse.wb.internal.core.gef.policy.snapping.ComponentAttachmentInfo;
import org.eclipse.wb.internal.swing.java6.model.GroupLayoutInfo;

import org.eclipse.swt.graphics.Image;

import javax.swing.GroupLayout;

/**
 * Selection policy for edit containers with {@link GroupLayout}.
 * 
 * @author mitin_aa
 * @coverage swing.gef.policy
 */
public final class GroupSelectionEditPolicy extends AbsoluteComplexSelectionEditPolicy {
  private final GroupLayoutInfo m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GroupSelectionEditPolicy(GroupLayoutInfo layout) {
    super(layout);
    m_layout = layout;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Overrides
  //
  ////////////////////////////////////////////////////////////////////////////
  public Image getActionImage(String imageName) {
    return GroupLayoutInfo.getImage(imageName);
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
