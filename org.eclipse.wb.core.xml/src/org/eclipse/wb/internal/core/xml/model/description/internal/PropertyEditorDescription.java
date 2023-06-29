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
package org.eclipse.wb.internal.core.xml.model.description.internal;

import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.property.IConfigurablePropertyObject;

/**
 * Description of {@link PropertyEditor}, can be {@link IConfigurablePropertyObject}.
 *
 * @author scheglov_ke
 * @coverage XML.model.description
 */
public final class PropertyEditorDescription extends AbstractConfigurableDescription {
	private final EditorContext m_context;
	private final PropertyEditor m_editor;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PropertyEditorDescription(EditorContext context, PropertyEditor editor) {
		m_context = context;
		m_editor = editor;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Editor
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return configured {@link PropertyEditor}.
	 */
	public PropertyEditor getConfiguredEditor() throws Exception {
		if (m_editor instanceof IConfigurablePropertyObject) {
			configure(m_context, (IConfigurablePropertyObject) m_editor);
		}
		return m_editor;
	}
}
