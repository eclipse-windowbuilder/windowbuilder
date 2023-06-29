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
package org.eclipse.wb.internal.core.xml.editor;

import java.util.List;

/**
 * This interface is contributed via extension point and used by {@link AbstractXmlEditor} for
 * creating {@link IXmlEditorPage} pages for Designer multi page XML editor.
 *
 * @author lobas_av
 * @coverage XML.editor
 */
public interface IXmlEditorPageFactory {
	/**
	 * Create {@link IXmlEditorPage} pages for given {@link AbstractXmlEditor} editor.
	 */
	void createPages(AbstractXmlEditor editor, List<IXmlEditorPage> pages);
}