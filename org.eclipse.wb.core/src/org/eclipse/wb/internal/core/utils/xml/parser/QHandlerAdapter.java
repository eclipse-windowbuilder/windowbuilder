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
package org.eclipse.wb.internal.core.utils.xml.parser;

import java.util.List;
import java.util.Map;

/**
 * Empty implementation of {@link QHandler}.
 *
 * @author scheglov_ke
 * @coverage core.util.xml
 */
public class QHandlerAdapter implements QHandler {
	@Override
	public void startDocument() throws Exception {
	}

	@Override
	public void endDocument() throws Exception {
	}

	@Override
	public void startElement(int offset,
			int length,
			String tag,
			Map<String, String> attributes,
			List<QAttribute> attrList,
			boolean closed) throws Exception {
	}

	@Override
	public void endElement(int offset, int endOffset, String tag) throws Exception {
	}

	@Override
	public void text(String text, boolean isCDATA) throws Exception {
	}
}
