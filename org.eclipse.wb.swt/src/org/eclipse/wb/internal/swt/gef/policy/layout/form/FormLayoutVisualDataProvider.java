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
package org.eclipse.wb.internal.swt.gef.policy.layout.form;

import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.internal.core.gef.policy.snapping.IVisualDataProvider;
import org.eclipse.wb.internal.core.gef.policy.snapping.PlacementUtils;
import org.eclipse.wb.internal.core.laf.BaselineSupportHelper;
import org.eclipse.wb.internal.swt.model.layout.form.IFormLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import java.util.List;

/**
 * {@link IVisualDataProvider} for FormLayout automatic.
 *
 * @author mitin_aa
 * @coverage swt.gef.policy.form
 */
final class FormLayoutVisualDataProvider<C extends IControlInfo> implements IVisualDataProvider {
  private final IFormLayoutInfo<C> m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FormLayoutVisualDataProvider(IFormLayoutInfo<C> layout) {
    m_layout = layout;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IVisualDataProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public int getBaseline(IAbstractComponentInfo component) {
    return BaselineSupportHelper.getBaseline(component.getObject());
  }

  @Override
  @SuppressWarnings("unchecked")
  public Dimension getComponentPreferredSize(IAbstractComponentInfo component) {
    C componentInfo = (C) component;
    return componentInfo.getPreferredSize();
  }

  @Override
  public Dimension getContainerSize() {
    return m_layout.getContainerSize();
  }

  @Override
  public boolean useGridSnapping() {
    return false;
  }

  @Override
  public boolean useFreeSnapping() {
    return true;
  }

  @Override
  public boolean isSuppressingSnapping() {
    return false;
  }

  @Override
  public int getGridStepY() {
    return m_layout.getPreferences().getSnapSensitivity();
  }

  @Override
  public int getGridStepX() {
    return m_layout.getPreferences().getSnapSensitivity();
  }

  @Override
  public int getContainerGapValue(IAbstractComponentInfo component, int direction) {
    if (PlacementUtils.isHorizontalSide(direction)) {
      return m_layout.getPreferences().getHorizontalContainerGap();
    } else {
      return m_layout.getPreferences().getVerticalContainerGap();
    }
  }

  @Override
  public int getComponentGapValue(IAbstractComponentInfo component1,
      IAbstractComponentInfo component2,
      int direction) {
    return 6;
  }

  @Override
  public Point getClientAreaOffset() {
    return m_layout.getComposite().getClientArea().getLocation();
  }

  public int getPercentsGap(boolean isHorizontal) {
    return isHorizontal
        ? m_layout.getPreferences().getHorizontalPercentsGap()
        : m_layout.getPreferences().getVerticalPercentsGap();
  }

  public List<Integer> getPercentsValues(boolean isHorizontal) {
    return isHorizontal
        ? m_layout.getPreferences().getHorizontalPercents()
        : m_layout.getPreferences().getVerticalPercents();
  }
}