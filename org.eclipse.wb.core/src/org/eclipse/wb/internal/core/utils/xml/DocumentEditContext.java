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
package org.eclipse.wb.internal.core.utils.xml;

import org.eclipse.jface.text.IDocument;

/**
 * Context for editing XML in {@link IDocument}.
 *
 * @author scheglov_ke
 * @coverage core.util.xml
 */
public abstract class DocumentEditContext extends AbstractDocumentEditContext {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public DocumentEditContext(IDocument document) throws Exception {
		parse(document);
	}
}
