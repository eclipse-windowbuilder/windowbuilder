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
package org.eclipse.wb.internal.core.model.description.internal;

import org.eclipse.wb.internal.core.model.property.IConfigurablePropertyObject;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.utils.state.EditorState;

/**
 * Description of {@link PropertyEditor}, can be {@link IConfigurablePropertyObject}.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class PropertyEditorDescription extends AbstractConfigurableDescription {
	private final EditorState m_state;
	private final PropertyEditor m_editor;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PropertyEditorDescription(EditorState state, PropertyEditor editor) {
		m_state = state;
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
			configure(m_state, (IConfigurablePropertyObject) m_editor);
		}
		return m_editor;
	}
}
