/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.xml.model;

import org.eclipse.jface.text.IDocument;

/**
 * Listener for "commit" event.
 *
 * @author scheglov_ke
 * @coverage XML.model
 */
public interface EditorContextCommitListener {
	/**
	 * Notifies that commit is about to be performed, so {@link IDocument} will be changed.
	 */
	void aboutToCommit();

	/**
	 * Notifies that commit into {@link IDocument} was done.
	 */
	void doneCommit();
}
