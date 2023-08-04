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
package org.eclipse.wb.tests.designer.XML.editor;

import org.eclipse.wb.internal.core.xml.editor.AbstractXmlEditor;
import org.eclipse.wb.internal.core.xml.editor.XmlDesignPage;
import org.eclipse.wb.tests.designer.XWT.gef.XwtGefTest;

import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.part.NullEditorInput;
import org.eclipse.wst.sse.ui.StructuredTextEditor;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * Test for {@link AbstractXmlEditor}.
 *
 * @author scheglov_ke
 */
public class AbstractXmlEditorTest extends XwtGefTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Exit zone :-) XXX
	//
	////////////////////////////////////////////////////////////////////////////
	public void _test_exit() throws Exception {
		System.exit(0);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_getAdapter_StructuredTextEditor() throws Exception {
		openEditor("<Shell/>");
		// we can access it
		Object adapter = m_designerEditor.getAdapter(StructuredTextEditor.class);
		assertSame(m_sourcePage.getXmlEditor(), adapter);
	}

	@Test
	public void test_doSaveAs() throws Exception {
		openEditor("<Shell/>");
		// disabled
		assertEquals(false, m_designerEditor.isSaveAsAllowed());
		// ignored
		m_designerEditor.doSaveAs();
	}

	/**
	 * Our editor accepts only {@link IFileEditorInput}.
	 */
	@Test
	public void test_init_notFile() throws Exception {
		AbstractXmlEditor editor = new AbstractXmlEditor() {
			@Override
			protected XmlDesignPage createDesignPage() {
				return null;
			}
		};
		try {
			editor.init(null, new NullEditorInput());
			fail();
		} catch (PartInitException e) {
			Assertions.assertThat(e.getMessage()).contains("IFileEditorInput");
		}
	}
}
