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
package org.eclipse.wb.internal.swing.model.property.editor;

import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.StaticFieldPropertyEditor;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Specialized version of {@link StaticFieldPropertyEditor} that allows to select any
 * <code>VK_</code> field from {@link KeyEvent}.
 *
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class DisplayedMnemonicKeyPropertyEditor extends StaticFieldPropertyEditor {
	public static final PropertyEditor INSTANCE = new DisplayedMnemonicKeyPropertyEditor();

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DisplayedMnemonicKeyPropertyEditor() {
		// prepare VK_ fields
		final List<String> fieldNames = new ArrayList<>();
		for (Field field : KeyEvent.class.getFields()) {
			String fieldName = field.getName();
			if (fieldName.startsWith("VK_")) {
				fieldNames.add(fieldName);
			}
		}
		// do configure
		ExecutionUtils.runRethrow(new RunnableEx() {
			@Override
			public void run() throws Exception {
				configure(KeyEvent.class, fieldNames.toArray(new String[fieldNames.size()]));
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IConfigurablePropertyObject
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void configure(EditorState state, Map<String, Object> parameters) throws Exception {
		// no external configuration supported
	}
}
