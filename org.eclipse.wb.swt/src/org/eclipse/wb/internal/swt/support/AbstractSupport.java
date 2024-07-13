/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	 * @return <code>this</code> if active editor contains SWT (eRCP or RCP) GUI.
	 */
	public static boolean is_SWT() {
		String toolkitId = EditorState.getActiveJavaInfo().getDescription().getToolkit().getId();
		return toolkitId.equals("org.eclipse.wb.ercp") || toolkitId.equals("org.eclipse.wb.rcp");
	}

	/**
	 * @return <code>this</code> if active editor contains RCP GUI and <code>false</code> if eRCP.
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