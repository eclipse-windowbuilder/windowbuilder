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
package org.eclipse.wb.internal.core.editor.actions.errors;

import org.eclipse.wb.core.model.ObjectInfo;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Interface for displaying error in {@link ErrorsDialog}.
 *
 * @author scheglov_ke
 * @coverage core.editor.action.error
 */
public interface IErrorPage {
	/**
	 * Sets the root {@link ObjectInfo}.
	 */
	void setRoot(ObjectInfo rootObject);

	/**
	 * @return <code>true</code> if page has errors.
	 */
	boolean hasErrors();

	/**
	 * @return the title of this page.
	 */
	String getTitle();

	/**
	 * Creates {@link Control} on given parent.
	 */
	Control create(Composite parent);
}
