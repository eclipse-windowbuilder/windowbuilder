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
package org.eclipse.wb.internal.core.model.util.grid;

import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.internal.core.utils.state.IParametersProvider;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * Helper for for performing automatic alignment in grid-based layouts.
 *
 * @author scheglov_ke
 * @coverage core.model.util
 */
public final class GridAlignmentHelper {
  /**
   * Parameter of {@link ComponentDescription}, value <code>"true"</code> means that component
   * should automatically use horizontal grab/fill when added.
   */
  public static final String V_GRAB_HORIZONTAL = "gridLayout.grabHorizontal";
  /**
   * Parameter of {@link ComponentDescription}, value <code>"true"</code> means that component
   * should automatically use vertical grab/fill when added.
   */
  public static final String V_GRAB_VERTICAL = "gridLayout.grabVertical";
  /**
   * Parameter of {@link ComponentDescription}, value <code>"true"</code> means that this component
   * should be aligned right if component on same row is marked with {@link #V_RIGHT_TARGET}.
   */
  public static final String V_RIGHT_LABEL = "gridLayout.rightAlignment.isLabel";
  /**
   * Parameter of {@link ComponentDescription}, value <code>"true"</code> means that this component
   * can be used in combination with {@link #V_RIGHT_LABEL}.
   */
  public static final String V_RIGHT_TARGET = "gridLayout.rightAlignment.isTarget";
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private GridAlignmentHelper() {
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Alignment
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Performs automatic alignment, such as grab/fill for {@link JTextField} or {@link JTable}, right
   * alignment for {@link JLabel}.
   */
  public static <C extends IAbstractComponentInfo> void doAutomaticAlignment(C component,
      IAlignmentProcessor<C> processor) throws Exception {
    IParametersProvider parametersProvider = GlobalState.getParametersProvider();
    // grab/fill
    if (processor.grabEnabled()) {
      // grab/fill horizontally
      if (parametersProvider.hasTrueParameter(component, V_GRAB_HORIZONTAL)) {
        processor.setGrabFill(component, true);
      }
      // grab/fill vertically
      if (parametersProvider.hasTrueParameter(component, V_GRAB_VERTICAL)) {
        processor.setGrabFill(component, false);
      }
    }
    // right alignment
    if (processor.rightEnabled()) {
      // right alignment for new "label" before existing "text"
      if (parametersProvider.hasTrueParameter(component, V_RIGHT_LABEL)) {
        // prepare on right
        C rightComponent = processor.getComponentAtRight(component);
        // check for "text"
        if (rightComponent != null
            && parametersProvider.hasTrueParameter(rightComponent, V_RIGHT_TARGET)) {
          processor.setRightAlignment(component);
        }
      }
      // right alignment for existing "label" before new "text"
      if (parametersProvider.hasTrueParameter(component, V_RIGHT_TARGET)) {
        // prepare component on left
        C leftComponent = processor.getComponentAtLeft(component);
        // check for "label"
        if (leftComponent != null
            && parametersProvider.hasTrueParameter(leftComponent, V_RIGHT_LABEL)) {
          processor.setRightAlignment(leftComponent);
        }
      }
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // IAlignmentProcessor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Provider for automatic alignment information/operations for {@link GridAlignmentHelper}.
   */
  public static interface IAlignmentProcessor<C extends IAbstractComponentInfo> {
    /**
     * @return <code>true</code> if automatic grab/fill can be performed.
     */
    boolean grabEnabled();
    /**
     * @return <code>true</code> if automatic right alignment can be performed.
     */
    boolean rightEnabled();
    /**
     * @return the component located directly on the left to given one, on same row.
     */
    C getComponentAtLeft(C component);
    /**
     * @return the component located directly on the right to given one, on same row.
     */
    C getComponentAtRight(C component);
    /**
     * Sets grab/fill for given component.
     */
    void setGrabFill(C component, boolean horizontal) throws Exception;
    /**
     * Sets right alignment for given component.
     */
    void setRightAlignment(C component) throws Exception;
  }
}
