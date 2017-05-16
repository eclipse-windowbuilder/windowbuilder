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
package org.eclipse.wb.internal.core.gef.policy.snapping;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.IAbstractComponentInfo;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Structure class intended to keep information about occurred placement for one axis.
 *
 * @author mitin_aa
 * @coverage core.gef.policy.snapping
 */
public final class PlacementInfo {
  // constants
  // directions
  public static final int LEADING = 0;
  public static final int TRAILING = 1;
  public static final int UNDEFINED = -1;
  // distance
  public static final int UNDEFINED_DISTANCE = Integer.MAX_VALUE;

  // attachment types
  public enum AttachmentTypes {
    Free, Container, Component, ComponentWithOffset, Baseline
  }

  /**
   * The direction in which mouse moved when snapping occurred.
   */
  private int m_direction;
  /**
   * Attachment type. <code>Container</code> and <code>Component</code> means that gap value should
   * be taken from LayoutStyle (if possible) or from settings and no any intelligence should be
   * applied to determine the attachments. <code>Free</code> means that it up to placement routine
   * to place the component.
   */
  private AttachmentTypes m_attachmentType;
  /**
   * Neighbors of this placement, leading or trailing. If snapping to component1 sequentially (ex,
   * one by one horizontally), then this is the neighbor. Else it is nearest component intersecting
   * this component(s) in opposite dimension.
   */
  private final IAbstractComponentInfo m_neighbors[] = new IAbstractComponentInfo[2];
  private final List<IAbstractComponentInfo> m_overlappings[];
  /**
   * Distance to the neighbor component or to the container boundary (parent). It positive when
   * components do not overlap and negative otherwise. In this case this means the distance is a
   * value on which the overlapping components should be moved (resized) to fit this component(s).
   */
  private final int[] m_distances = new int[]{UNDEFINED_DISTANCE, UNDEFINED_DISTANCE};
  private IAbstractComponentInfo m_attachedToWidget;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  @SuppressWarnings("unchecked")
  public PlacementInfo() {
    m_overlappings = (List<IAbstractComponentInfo>[]) Array.newInstance(ArrayList.class, 2);
    m_overlappings[LEADING] = Lists.newArrayList();
    m_overlappings[TRAILING] = Lists.newArrayList();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public final int getDirection() {
    return m_direction;
  }

  public final void setDirection(int direction) {
    m_direction = direction;
  }

  public final AttachmentTypes getAttachmentType() {
    return m_attachmentType;
  }

  public final void setAttachmentType(AttachmentTypes gapType) {
    m_attachmentType = gapType;
  }

  public final IAbstractComponentInfo[] getNeighbors() {
    return m_neighbors;
  }

  public final List<IAbstractComponentInfo>[] getOverlappings() {
    return m_overlappings;
  }

  public final int[] getDistances() {
    return m_distances;
  }

  public final void setAttachedToWidget(IAbstractComponentInfo widget) {
    m_attachedToWidget = widget;
  }

  public final IAbstractComponentInfo getAttachedToWidget() {
    return m_attachedToWidget;
  }

  public final void cleanup() {
    m_overlappings[LEADING] = Lists.newArrayList();
    m_overlappings[TRAILING] = Lists.newArrayList();
    m_distances[LEADING] = UNDEFINED_DISTANCE;
    m_distances[TRAILING] = UNDEFINED_DISTANCE;
    m_neighbors[LEADING] = null;
    m_neighbors[TRAILING] = null;
    m_attachedToWidget = null;
  }
}
