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

import com.google.common.collect.Lists;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.gef.policy.snapping.BaselineComponentSnapPoint;
import org.eclipse.wb.internal.core.gef.policy.snapping.ComponentSnapPoint;
import org.eclipse.wb.internal.core.gef.policy.snapping.ContainerSnapPoint;
import org.eclipse.wb.internal.core.gef.policy.snapping.IVisualDataProvider;
import org.eclipse.wb.internal.core.gef.policy.snapping.IndentedComponentSnapPoint;
import org.eclipse.wb.internal.core.gef.policy.snapping.PlacementInfo;
import org.eclipse.wb.internal.core.gef.policy.snapping.PlacementUtils;
import org.eclipse.wb.internal.core.gef.policy.snapping.SameSizeSnapPoint;
import org.eclipse.wb.internal.core.gef.policy.snapping.SnapPoint;
import org.eclipse.wb.internal.core.gef.policy.snapping.SnapPointCommand;
import org.eclipse.wb.internal.core.gef.policy.snapping.SnapPoints;
import org.eclipse.wb.internal.swt.model.layout.form.FormLayoutInfoImplAutomatic;
import org.eclipse.wb.internal.swt.model.layout.form.IFormLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import java.util.List;

/**
 * Provides FormLayout-specific snap points for snapping engine.
 * 
 * @author mitin_aa
 */
final class FormLayoutSnapPointsProvider<C extends IControlInfo>
    extends
      SnapPoints.DefaultSnapPoints {
  private final FormLayoutVisualDataProvider<C> m_vdProvider;
  private final List<? extends IAbstractComponentInfo> m_allWidgets;
  private final IFormLayoutInfo<C> m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////	
  public FormLayoutSnapPointsProvider(IFormLayoutInfo<C> layout,
      FormLayoutVisualDataProvider<C> visualDataProvider,
      List<? extends IAbstractComponentInfo> allWidgets) {
    super(visualDataProvider, allWidgets);
    m_layout = layout;
    m_vdProvider = visualDataProvider;
    m_allWidgets = allWidgets;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ISnapPointsProvider 
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public List<SnapPoint> forComponent(IAbstractComponentInfo target, boolean isHorizontal) {
    List<SnapPoint> pts = Lists.newArrayList();
    int lSide = PlacementUtils.getSide(isHorizontal, true);
    int tSide = PlacementUtils.getSide(isHorizontal, false);
    if (isHorizontal) {
      // snap to child on left side with indent
      SnapPoint snapPoint = new IndentedComponentSnapPoint(m_vdProvider, target);
      pts.add(snapPoint);
    } else {
      // baseline snap
      SnapPoint snapPoint = new BaselineComponentSnapPoint(m_vdProvider, target);
      pts.add(snapPoint);
    }
    // snap to child on leading side with gap
    {
      SnapPoint snapPoint =
          new ComponentSnapPoint(m_vdProvider, target, lSide, PlacementInfo.TRAILING, true);
      snapPoint.setCommand(new MoveToComponentCommand<C>(m_layout, snapPoint));
      pts.add(snapPoint);
    }
    // snap to child on leading side
    {
      ComponentSnapPoint snapPoint =
          new ComponentSnapPoint(m_vdProvider, target, lSide, PlacementInfo.LEADING);
      snapPoint.setCommand(new MoveToComponentCommand<C>(m_layout, snapPoint));
      pts.add(snapPoint);
    }
    // snap to child on trailing side with gap
    {
      SnapPoint snapPoint =
          new ComponentSnapPoint(m_vdProvider, target, tSide, PlacementInfo.LEADING, true);
      snapPoint.setCommand(new MoveToComponentCommand<C>(m_layout, snapPoint));
      pts.add(snapPoint);
    }
    // snap to child on trailing side
    {
      SnapPoint snapPoint =
          new ComponentSnapPoint(m_vdProvider, target, tSide, PlacementInfo.TRAILING);
      snapPoint.setCommand(new MoveToComponentCommand<C>(m_layout, snapPoint));
      pts.add(snapPoint);
    }
    return pts;
  }

  @Override
  public List<SnapPoint> forContainer(boolean isHorizontal) {
    List<SnapPoint> pts = Lists.newArrayList();
    int leadingSide = PlacementUtils.getSide(isHorizontal, true);
    int trailingSide = PlacementUtils.getSide(isHorizontal, false);
    // snap to parent at leading side with gap
    {
      SnapPoint snapPoint = new ContainerSnapPoint(m_vdProvider, leadingSide, true);
      snapPoint.setCommand(new MoveToContainerCommand<C>(m_layout, snapPoint));
      pts.add(snapPoint);
    }
    // snap to parent at leading side
    {
      SnapPoint snapPoint = new ContainerSnapPoint(m_vdProvider, leadingSide);
      snapPoint.setCommand(new MoveToContainerCommand<C>(m_layout, snapPoint));
      pts.add(snapPoint);
    }
    // snap to parent at trailing side with gap
    {
      SnapPoint snapPoint = new ContainerSnapPoint(m_vdProvider, trailingSide, true);
      snapPoint.setCommand(new MoveToContainerCommand<C>(m_layout, snapPoint));
      pts.add(snapPoint);
    }
    // snap to parent at trailing side
    {
      SnapPoint snapPoint = new ContainerSnapPoint(m_vdProvider, trailingSide);
      snapPoint.setCommand(new MoveToContainerCommand<C>(m_layout, snapPoint));
      pts.add(snapPoint);
    }
    // 'same size'
    {
      SnapPoint snapPoint = new SameSizeSnapPoint(m_vdProvider, m_allWidgets, leadingSide);
      pts.add(snapPoint);
    }
    {
      SnapPoint snapPoint = new SameSizeSnapPoint(m_vdProvider, m_allWidgets, trailingSide);
      pts.add(snapPoint);
    }
    // percentage
    for (Integer percent : m_vdProvider.getPercentsValues(isHorizontal)) {
      {
        SnapPoint snapPoint = new PercentageSnapPoint<C>(m_vdProvider, leadingSide, percent, true);
        snapPoint.setCommand(new MoveToPercentCommand<C>(m_layout, snapPoint));
        pts.add(snapPoint);
      }
      {
        SnapPoint snapPoint = new PercentageSnapPoint<C>(m_vdProvider, leadingSide, percent);
        snapPoint.setCommand(new MoveToPercentCommand<C>(m_layout, snapPoint));
        pts.add(snapPoint);
      }
      {
        SnapPoint snapPoint = new PercentageSnapPoint<C>(m_vdProvider, trailingSide, percent, true);
        snapPoint.setCommand(new MoveToPercentCommand<C>(m_layout, snapPoint));
        pts.add(snapPoint);
      }
      {
        SnapPoint snapPoint = new PercentageSnapPoint<C>(m_vdProvider, trailingSide, percent);
        snapPoint.setCommand(new MoveToPercentCommand<C>(m_layout, snapPoint));
        pts.add(snapPoint);
      }
    }
    return pts;
  }

  private static final class MoveToContainerCommand<C extends IControlInfo>
      extends
        SnapPointCommand {
    private final IFormLayoutInfo<C> m_layout;

    public MoveToContainerCommand(IFormLayoutInfo<C> layoutInfo, SnapPoint snapPoint) {
      super(layoutInfo.getUnderlyingModel(), snapPoint);
      m_layout = layoutInfo;
    }

    @Override
    protected void executeEdit() throws Exception {
      SnapPoint snapPoint = getSnapPoint();
      int value = snapPoint.getValue();
      FormLayoutInfoImplAutomatic<C> impl = (FormLayoutInfoImplAutomatic<C>) m_layout.getImpl();
      impl.command_moveToContainer(
          snapPoint.getWorkingSet(),
          snapPoint.getNearestBeingSnapped(),
          snapPoint.getSide(),
          value);
    }
  }
  private static final class MoveToComponentCommand<C extends IControlInfo>
      extends
        SnapPointCommand {
    private final IFormLayoutInfo<C> m_layout;

    public MoveToComponentCommand(IFormLayoutInfo<C> layoutInfo, SnapPoint snapPoint) {
      super(layoutInfo.getUnderlyingModel(), snapPoint);
      m_layout = layoutInfo;
    }

    @Override
    protected void executeEdit() throws Exception {
      ComponentSnapPoint snapPoint = (ComponentSnapPoint) getSnapPoint();
      int targetSide = snapPoint.getSide();
      int gap = snapPoint.getGap();
      int sourceSide = gap > 0 ? PlacementUtils.getOppositeSide(targetSide) : targetSide;
      FormLayoutInfoImplAutomatic<C> impl = (FormLayoutInfoImplAutomatic<C>) m_layout.getImpl();
      impl.command_moveAsAttachedToComponent(
          snapPoint.getWorkingSet(),
          snapPoint.getNearestBeingSnapped(),
          sourceSide,
          snapPoint.getComponent(),
          targetSide,
          gap);
    }
  }
  private static final class MoveToPercentCommand<C extends IControlInfo> extends SnapPointCommand {
    private final IFormLayoutInfo<C> m_layout;

    public MoveToPercentCommand(IFormLayoutInfo<C> layoutInfo, SnapPoint snapPoint) {
      super(layoutInfo.getUnderlyingModel(), snapPoint);
      m_layout = layoutInfo;
    }

    @Override
    protected void executeEdit() throws Exception {
      @SuppressWarnings("unchecked")
      PercentageSnapPoint<C> snapPoint = (PercentageSnapPoint<C>) getSnapPoint();
      FormLayoutInfoImplAutomatic<C> impl = (FormLayoutInfoImplAutomatic<C>) m_layout.getImpl();
      impl.command_moveToPercent(
          snapPoint.getWorkingSet(),
          snapPoint.getNearestBeingSnapped(),
          snapPoint.getSide(),
          snapPoint.getPercent(),
          snapPoint.getGap());
    }
  }
  static final class MoveFreelyCommand<C extends IControlInfo> extends EditCommand {
    private final List<? extends IAbstractComponentInfo> m_components;
    private final int m_moveDirection;
    private final boolean m_isHorizontal;
    private final IFormLayoutInfo<C> m_layout;
    private final IVisualDataProvider m_visualDataProvider;
    private final Rectangle m_bounds;

    MoveFreelyCommand(IFormLayoutInfo<C> layoutInfo,
        Rectangle bounds,
        List<? extends IAbstractComponentInfo> components,
        int moveDirection,
        boolean isHorizontal,
        IVisualDataProvider provider) {
      super(layoutInfo);
      m_layout = layoutInfo;
      m_bounds = bounds;
      m_components = components;
      m_moveDirection = moveDirection;
      m_isHorizontal = isHorizontal;
      m_visualDataProvider = provider;
    }

    @Override
    protected void executeEdit() throws Exception {
      FormLayoutInfoImplAutomatic<C> impl = (FormLayoutInfoImplAutomatic<C>) m_layout.getImpl();
      IAbstractComponentInfo nearestComponentToSide =
          SnapPoint.getNearestComponentToSide(
              m_components,
              m_moveDirection,
              m_isHorizontal,
              m_visualDataProvider);
      impl.command_moveFreely(
          m_bounds,
          m_components,
          nearestComponentToSide,
          m_moveDirection,
          m_isHorizontal);
    }
  }
}