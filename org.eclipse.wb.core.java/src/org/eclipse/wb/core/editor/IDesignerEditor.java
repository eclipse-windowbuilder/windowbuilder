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

import org.eclipse.wb.internal.core.views.IDesignCompositeProvider;

import org.eclipse.jdt.core.ICompilationUnit;

/**
 * Interface for multi-page GUI editor, where one of the pages in Java source editor.
 *
 * @author scheglov_ke
 * @author mitin_aa
 * @coverage core.editor
 */
public interface IDesignerEditor extends IDesignCompositeProvider {
	String ID = "org.eclipse.wb.core.guiEditor";

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link IMultiMode}.
	 */
	IMultiMode getMultiMode();

	/**
	 * @return the {@link ICompilationUnit} opened in this editor.
	 */
	ICompilationUnit getCompilationUnit();

	////////////////////////////////////////////////////////////////////////////
	//
	// Listeners
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds {@link DesignerEditorListener} for this editor.
	 */
	void addDesignPageListener(DesignerEditorListener listener);

	/**
	 * Removes {@link DesignerEditorListener} for this editor.
	 */
	void removeDesignPageListener(DesignerEditorListener listener);
}