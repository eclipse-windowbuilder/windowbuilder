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
package org.eclipse.wb.internal.core.utils.execution;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * No-op implementation of {@link IProgressMonitor}.
 *
 * @author scheglov_ke
 * @coverage core.util
 */
public final class NoOpProgressMonitor implements IProgressMonitor {
	////////////////////////////////////////////////////////////////////////////
	//
	// Task
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void beginTask(String name, int totalWork) {
	}

	@Override
	public void done() {
	}

	@Override
	public void setTaskName(String name) {
	}

	@Override
	public void subTask(String name) {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Canceled
	//
	////////////////////////////////////////////////////////////////////////////
	private boolean m_canceled;

	@Override
	public boolean isCanceled() {
		return m_canceled;
	}

	@Override
	public void setCanceled(boolean value) {
		m_canceled = value;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Worked
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void internalWorked(double work) {
	}

	@Override
	public void worked(int work) {
	}
}
