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
package org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author lobas_av
 *
 */
public class MultiTargetRunnable implements Runnable {
	private final ChooseClassUiContentProvider m_source;
	private final List<ChooseClassUiContentProvider> m_targets = new ArrayList<>();

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public MultiTargetRunnable(ChooseClassUiContentProvider source) {
		m_source = source;
	}

	public MultiTargetRunnable(ChooseClassUiContentProvider source,
			ChooseClassUiContentProvider[] targets) {
		this(source);
		for (ChooseClassUiContentProvider target : targets) {
			addTarget(target, false);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public void addTarget(ChooseClassUiContentProvider target, boolean update) {
		m_targets.add(target);
		target.getDialogField().setEnabled(false);
		if (update) {
			target.setClassName(m_source.getClassName());
		}
	}

	public void removeTarget(ChooseClassUiContentProvider target) {
		m_targets.remove(target);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Runnable
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void run() {
		for (ChooseClassUiContentProvider target : m_targets) {
			target.setClassName(m_source.getClassName());
		}
	}
}