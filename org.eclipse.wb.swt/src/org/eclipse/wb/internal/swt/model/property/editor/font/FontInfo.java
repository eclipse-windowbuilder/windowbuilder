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
package org.eclipse.wb.internal.swt.model.property.editor.font;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.swt.graphics.Font;

/**
 * Information object about {@link Font}.
 *
 * @author lobas_av
 * @coverage swt.property.editor
 */
public final class FontInfo {
	private final String m_name;
	private final Object m_font;
	private final String m_sourceCode;
	private final boolean m_doDispose;
	private Object m_data;
	private String m_pageId;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FontInfo(String name, Object font, String sourceCode, boolean doDispose) {
		m_name = name;
		m_font = font;
		m_sourceCode = sourceCode;
		m_doDispose = doDispose;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Dispose
	//
	////////////////////////////////////////////////////////////////////////////
	public void dispose() {
		ExecutionUtils.runLog(new RunnableEx() {
			@Override
			public void run() throws Exception {
				if (m_doDispose) {
					ReflectionUtils.invokeMethod(m_font, "dispose()");
				}
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the name of font.
	 */
	public String getName() {
		return m_name;
	}

	/**
	 * @return the inner {@link Font}.
	 */
	public Object getFont() {
		return m_font;
	}

	/**
	 * @return source code association with this font info.
	 */
	public String getSourceCode() {
		return m_sourceCode;
	}

	/**
	 * @return the data associated with this {@link FontInfo}.
	 */
	public Object getData() {
		return m_data;
	}

	/**
	 * Sets the data associated with this {@link FontInfo}.
	 */
	public void setData(Object data) {
		m_data = data;
	}

	/**
	 * @return owner page id for this value.
	 */
	public String getPageId() {
		return m_pageId;
	}

	/**
	 * Sets owner page id for this value.
	 */
	public void setPageId(String pageId) {
		m_pageId = pageId;
	}
}