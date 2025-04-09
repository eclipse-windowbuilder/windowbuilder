/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.internal.swt.support;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.state.GlobalState;

/**
 * Abstract superclass for SWT related supports.
 *
 * @author lobas_av
 * @coverage swt.support
 */
public class AbstractSupport {
	/**
	 * @return <code>this</code> if active editor contains SWT/RCP GUI.
	 */
	public static boolean is_SWT() {
		String toolkitId = EditorState.getActiveJavaInfo().getDescription().getToolkit().getId();
		return toolkitId.equals("org.eclipse.wb.rcp");
	}

	/**
	 * @return <code>true</code> if active editor contains RCP GUI.
	 */
	public static boolean is_RCP() {
		String toolkitId = GlobalState.getToolkit().getId();
		return toolkitId.equals("org.eclipse.wb.rcp");
	}

	/**
	 * Loads the {@link Class} that should always be successful, so it re-throws any exception.
	 *
	 * @return the {@link Class} with given name loaded from active editor {@link ClassLoader}.
	 */
	protected static Class<?> loadClass(final String name) {
		return ExecutionUtils.runObject(() -> GlobalState.getClassLoader().loadClass(name));
	}
}