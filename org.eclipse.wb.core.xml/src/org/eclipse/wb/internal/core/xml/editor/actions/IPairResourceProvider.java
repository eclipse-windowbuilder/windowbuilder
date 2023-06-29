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
package org.eclipse.wb.internal.core.xml.editor.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorPart;

/**
 * Provider for {@link IFile} pair/companion which corresponds {@link IFile} of active
 * {@link IEditorPart}, such as Java for XML and vice-versa.
 *
 * @author scheglov_ke
 * @coverage XML.editor.action
 */
public interface IPairResourceProvider {
	/**
	 * @return the {@link IFile} pair for given {@link IFile}, may be <code>null</code>.
	 */
	IFile getPair(IFile file);
}