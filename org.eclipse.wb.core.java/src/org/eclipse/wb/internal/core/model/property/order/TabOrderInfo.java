/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
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