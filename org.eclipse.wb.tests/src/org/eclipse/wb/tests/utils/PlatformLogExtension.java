/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.utils;

import org.eclipse.wb.internal.core.DesignerPlugin;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ILogListener;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Custom extension used to keep track of any
 */
public class PlatformLogExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {
	private int m_numberOfExceptionsDuringThisEditorSession = 0;
	private final ILogListener m_logListener = (status, plugin) -> {
		System.out.println("#####" + status + ", " + plugin);
		m_numberOfExceptionsDuringThisEditorSession++;
	};

	@Override
	public void beforeTestExecution(ExtensionContext context) throws Exception {
		m_numberOfExceptionsDuringThisEditorSession = 0;
		ILog log = DesignerPlugin.getDefault().getLog();
		log.addLogListener(m_logListener);
	}

	@Override
	public void afterTestExecution(ExtensionContext context) throws Exception {
		ILog log = DesignerPlugin.getDefault().getLog();
		log.removeLogListener(m_logListener);
		assertEquals(0, m_numberOfExceptionsDuringThisEditorSession, "Check console for logged exceptions.");
	}
}
