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

/**
 * A visitor for {@link DocumentElement} model.
 *
 * @author scheglov_ke
 * @coverage core.util.xml
 */
public class DocumentModelVisitor {
	////////////////////////////////////////////////////////////////////////////
	//
	// DocumentNode
	//
	////////////////////////////////////////////////////////////////////////////
	public boolean visit(DocumentElement element) {
		return true;
	}

	public void endVisit(DocumentElement element) {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Other
	//
	////////////////////////////////////////////////////////////////////////////
	public void visit(DocumentAttribute attribute) {
	}

	public void visit(DocumentTextNode node) {
	}
}
