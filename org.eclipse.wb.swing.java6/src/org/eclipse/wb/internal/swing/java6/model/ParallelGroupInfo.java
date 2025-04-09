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
package org.eclipse.wb.internal.swing.java6.model;

import org.apache.commons.lang3.StringUtils;

import javax.swing.GroupLayout.Alignment;

/**
 * Reflects parallel group of GroupLayout.
 *
 * @author mitin_aa
 * @coverage swing.model.layout.group
 */
public final class ParallelGroupInfo extends GroupInfo {
	private Alignment m_alignment = Alignment.LEADING;
	private boolean m_resizeable = true;
	private boolean m_anchoredToTop = false;

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public void setGroupAlignment(Alignment alignment) {
		m_alignment = alignment;
	}

	public void setGroupResizeable(boolean resizeable) {
		m_resizeable = resizeable;
	}

	public void setGroupAnchorBaselineToTop(boolean anchoredToTop) {
		// only for baseline alignment
		m_anchoredToTop = anchoredToTop;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Save
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getCode(int level) throws Exception {
		String code = StringUtils.repeat(" ", level);
		if (m_alignment == Alignment.BASELINE && m_anchoredToTop) {
			code += GroupLayoutInfo.IDENTIFIER_CREATE_BASELINE_GROUP + "(";
		} else {
			code += GroupLayoutInfo.IDENTIFIER_CREATE_PARALLEL_GROUP + "(";
		}
		return code;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Dump
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void dump(int level, StringBuffer buffer) {
		buffer.append(StringUtils.repeat(" ", level));
		buffer.append("P align=" + m_alignment + " resiz=" + m_resizeable + "\n");
		for (SpringInfo child : m_children) {
			child.dump(level + 1, buffer);
		}
	}
}
