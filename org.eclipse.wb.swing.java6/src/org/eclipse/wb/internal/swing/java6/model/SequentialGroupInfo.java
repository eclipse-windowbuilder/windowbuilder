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

/**
 * Represents sequential group.
 *
 * @author mitin_aa
 * @coverage swing.model.layout.group
 */
public final class SequentialGroupInfo extends GroupInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Dump
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void dump(int level, StringBuffer buffer) {
		buffer.append(StringUtils.repeat(" ", level));
		buffer.append("S\n");
		for (SpringInfo child : m_children) {
			child.dump(level + 1, buffer);
		}
	}
}
