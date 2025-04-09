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
package org.eclipse.wb.tests.designer.core.util.xml;

import org.eclipse.wb.internal.core.utils.xml.AbstractDocumentHandler;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.utils.xml.Model;

/**
 *
 * @author lobas_av
 */
public class TestDocumentHandler extends AbstractDocumentHandler {
	////////////////////////////////////////////////////////////////////////////
	//
	// AbstractDocumentHandler
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected DocumentElement getDocumentNode(String name, DocumentElement parent) {
		DocumentElement element;
		if ("special".equals(name)) {
			element = new SpecialDocumentNode();
		} else {
			element = new DocumentElement();
		}
		//
		if (parent == null) {
			element.setModel(new Model());
		}
		return element;
	}
}