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
package org.eclipse.wb.core.editor;

import org.eclipse.jdt.core.ICompilationUnit;

/**
 * "Design" page of {@link IDesignerEditor}.
 *
 * @author scheglov_ke
 * @coverage core.editor
 */
public interface IDesignPage extends IEditorPage {
	/**
	 * Specifies if synchronization of source code and model should be active or not.
	 */
	void setSourceModelSynchronizationEnabled(boolean active);

	/**
	 * @return the {@link DesignerState} of parsing.
	 */
	DesignerState getDesignerState();

	/**
	 * Parses {@link ICompilationUnit} and displays it in GEF.
	 */
	void refreshGEF();
}