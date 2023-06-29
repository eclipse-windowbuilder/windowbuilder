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
package org.eclipse.wb.internal.xwt.editor;

import org.eclipse.wb.internal.core.xml.editor.XmlDesignPage;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.xwt.parser.XwtParser;

/**
 * {@link XmlDesignPage} for XWT.
 *
 * @author scheglov_ke
 * @coverage XWT.editor
 */
public final class XwtDesignPage extends XmlDesignPage {
	////////////////////////////////////////////////////////////////////////////
	//
	// Render
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected XmlObjectInfo parse() throws Exception {
		XwtParser parser = new XwtParser(m_file, m_document);
		return parser.parse();
	}
}
