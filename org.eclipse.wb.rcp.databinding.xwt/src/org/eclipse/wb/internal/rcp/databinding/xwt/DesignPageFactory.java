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
package org.eclipse.wb.internal.rcp.databinding.xwt;

import org.eclipse.wb.internal.core.databinding.xml.ui.BindingXmlPage;
import org.eclipse.wb.internal.core.xml.editor.AbstractXmlEditor;
import org.eclipse.wb.internal.core.xml.editor.IXmlEditorPage;
import org.eclipse.wb.internal.core.xml.editor.IXmlEditorPageFactory;

import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import java.util.List;

/**
 *
 * @author lobas_av
 *
 */
public final class DesignPageFactory implements IXmlEditorPageFactory {
	////////////////////////////////////////////////////////////////////////////
	//
	// IXMLEditorPageFactory
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void createPages(AbstractXmlEditor editor, List<IXmlEditorPage> pages) {
		if (isXWT(editor)) {
			BindingXmlPage.addPage(pages);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private static boolean isXWT(AbstractXmlEditor editor) {
		try {
			IDocument document = editor.getDocument();
			FindReplaceDocumentAdapter finder = new FindReplaceDocumentAdapter(document);
			IRegion region = finder.find(0, "xmlns", true, false, false, false);
			if (region != null) {
				int start = region.getOffset() + region.getLength();
				region =
						finder.find(
								start,
								"\"http://www.eclipse.org/xwt/presentation\"",
								true,
								false,
								false,
								false);
				if (region != null) {
					return "=".equals(document.get(start, region.getOffset() - start).trim());
				}
			}
		} catch (Throwable e) {
		}
		return false;
	}
}