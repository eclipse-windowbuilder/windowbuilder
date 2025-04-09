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
package org.eclipse.wb.internal.core.utils.ast;

import org.eclipse.jdt.core.ICompilationUnit;

/**
 * Allows listening for {@link AstEditor#commitChanges()} events.
 *
 * @author scheglov_ke
 * @coverage core.util.ast
 */
public interface IASTEditorCommitListener {
	/**
	 * Notifies that {@link AstEditor} is going to commit changes to {@link ICompilationUnit} buffer.
	 */
	void aboutToCommit();

	/**
	 * Request editable state for file. When file is in CVS and project configured to use edit/watch
	 * mode, files are read only and we should request edit to change them. {@link AstEditor} calls
	 * this method to ensure that changes made in shadow buffer can be saved in main file buffer.
	 */
	boolean canEditBaseFile();

	/**
	 * Notifies that {@link AstEditor} done commit changes to {@link ICompilationUnit} buffer.
	 */
	void commitDone();
}