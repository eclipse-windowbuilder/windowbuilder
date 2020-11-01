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
package org.eclipse.wb.internal.core.model.property.order;

import org.eclipse.wb.core.model.AbstractComponentInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Information about container child ordering: all components, ordered components and presentation
 * order value.
 *
 * @author lobas_av
 * @coverage core.model.property.order
 */
public final class TabOrderInfo {
  private final List<AbstractComponentInfo> m_infos = new ArrayList<>();
  private final List<AbstractComponentInfo> m_orderedInfos = new ArrayList<>();
  private boolean m_isDefault;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void addOrderedInfo(AbstractComponentInfo info) throws Exception {
    m_orderedInfos.add(info);
  }

  void reorder() {
    m_infos.removeAll(m_orderedInfos);
    m_infos.addAll(0, m_orderedInfos);
  }

  public List<AbstractComponentInfo> getInfos() {
    return m_infos;
  }

  public List<AbstractComponentInfo> getOrderedInfos() {
    return m_orderedInfos;
  }

  boolean isDefault() {
    return m_isDefault;
  }

  void setDefault() {
    m_isDefault = true;
  }
}