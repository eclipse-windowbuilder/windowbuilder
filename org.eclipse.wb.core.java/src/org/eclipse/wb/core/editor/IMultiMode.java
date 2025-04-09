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

import org.eclipse.wb.internal.core.editor.multi.DesignerEditor;

/**
 * The mode for presentation source/design parts of {@link DesignerEditor}.
 *
 * @author scheglov_ke
 * @coverage core.editor
 */
public interface IMultiMode {
	/**
	 * @return the {@link IDesignPage}.
	 */
	IDesignPage getDesignPage();

	/**
	 * Activates "Source" page of editor.
	 */
	void showSource();

	/**
	 * Activates "Design" page of editor.
	 */
	void showDesign();

	/**
	 * Switches between "Source" and "Design" pages.
	 */
	void switchSourceDesign();
}