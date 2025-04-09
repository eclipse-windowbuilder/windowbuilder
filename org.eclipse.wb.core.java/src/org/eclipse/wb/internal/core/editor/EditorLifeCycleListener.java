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
package org.eclipse.wb.internal.core.editor;

import org.eclipse.jdt.core.ICompilationUnit;

/**
 * This interface is used to notify external listeners about editor life cycle.
 *
 * @author scheglov_ke
 * @coverage core.editor
 */
public abstract class EditorLifeCycleListener {
	/**
	 * @return <code>false</code> if given {@link ICompilationUnit} can be parsed quickly, so no
	 *         progress required; or <code>true</code> if progress should be displayed.
	 */
	public boolean parseWithProgress(Object editor, ICompilationUnit unit) {
		return true;
	}

	/**
	 * Parsing is about to start.
	 */
	public void parseStart(Object editor) throws Exception {
	}

	/**
	 * Parsing was finished (successfully or not).
	 */
	public void parseEnd(Object editor) throws Exception {
	}

	/**
	 * Hierarchy was disposed in editor, so may be context of editor also should be disposed.
	 * Sometimes however we don't want to throw away all information about editor.
	 *
	 * @param force
	 *          is <code>true</code> if user closes editor, so context should be disposed.
	 */
	public void disposeContext(Object editor, boolean force) throws Exception {
	}
}
