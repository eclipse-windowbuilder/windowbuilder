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
package org.eclipse.wb.internal.swing.model.layout.gbl;

import org.eclipse.wb.internal.core.model.util.grid.GridAlignmentHelper;

import java.awt.GridBagConstraints;

/**
 * Preference constants for {@link AbstractGridBagLayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.preferences.layout
 */
public interface IPreferenceConstants {
  /**
   * When <code>true</code>, {@link AbstractGridBagLayoutInfo} can use
   * {@link GridAlignmentHelper#V_GRAB_HORIZONTAL} and {@link GridAlignmentHelper#V_GRAB_VERTICAL}.
   */
  String P_ENABLE_GRAB = "GridBagLayout.enableGrab";
  /**
   * When <code>true</code>, {@link AbstractGridBagLayoutInfo} can use
   * {@link GridAlignmentHelper#V_RIGHT_LABEL} and {@link GridAlignmentHelper#V_RIGHT_TARGET}.
   */
  String P_ENABLE_RIGHT_ALIGNMENT = "GridBagLayout.enableRightAlignment";
  /**
   * When <code>true</code>, {@link AbstractGridBagLayoutInfo} should use long form of
   * {@link GridBagConstraints} constructor.
   */
  String P_GBC_LONG = "GridBagLayout.longConstraints";
  /**
   * Allow automatically change insets to generate gaps between components.
   */
  String P_CHANGE_INSETS_FOR_GAPS = "GridBagLayout.changeInsets.forGaps";
  /**
   * Gap between columns.
   */
  String P_GAP_COLUMN = "GridBagLayout.gap.column";
  /**
   * Gap between rows.
   */
  String P_GAP_ROW = "GridBagLayout.gap.row";
  /**
   * The template for <code>GridBagConstraints</code> variable name.
   */
  String P_CONSTRAINTS_NAME_TEMPLATE = "templateConstraintsName";
}
