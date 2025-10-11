/*******************************************************************************
 * Copyright (c) 2025 Patrick Ziegler and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.model.layout;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.Messages;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.LayoutDescription;
import org.eclipse.wb.internal.core.model.description.helpers.LayoutDescriptionHelper;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;

import java.util.List;
import java.util.Objects;

/**
 * Common base class for editing the {@code layout} of a UI container.
 *
 * @param <T> The UI container. Either an SWT {@code Composite} or Swing
 *            {@code Container}.
 */
public abstract class AbstractLayoutInfo<T extends JavaInfo> extends JavaInfo {

	public AbstractLayoutInfo(AstEditor editor, ComponentDescription description, CreationSupport creationSupport)
			throws Exception {
		super(editor, description, creationSupport);
	}

	/**
	 * Removes the previous layout and sets the default layout as specified in the
	 * {@link LayoutsPreferencePage}.
	 */
	protected void setDefaultLayout() throws Exception {
		IPreferenceStore prefs = getDescription().getToolkit().getPreferences();
		String defaultValue = prefs.getString(IPreferenceConstants.P_LAYOUT_DEFAULT);
		if (defaultValue.isEmpty()) {
			delete();
			return;
		}
		List<LayoutDescription> descriptions = LayoutDescriptionHelper.get(getDescription().getToolkit());
		String creationId = null;
		ClassLoader editorLoader = null;
		Class<?> layoutClass = null;
		for (LayoutDescription description : descriptions) {
			if (Objects.equals(defaultValue, description.getId())) {
				creationId = description.getCreationId();
				editorLoader = EditorState.get(getParentJava().getEditor()).getEditorLoader();
				layoutClass = editorLoader.loadClass(description.getLayoutClassName());
			}
		}
		if (layoutClass == null) {
			DesignerPlugin.log(NLS.bind(Messages.AbstractLayoutInfo_unknownDefaultLayout, defaultValue));
			delete();
			return;
		}
		@SuppressWarnings("unchecked")
		T defaultLayoutInfo = (T) JavaInfoUtils.createJavaInfo(getParentJava().getEditor(), layoutClass,
				new ConstructorCreationSupport(creationId, true));
		setLayout(defaultLayoutInfo);
	}

	/**
	 * Sets new {@code LayoutInfo}.
	 */
	protected abstract void setLayout(T layoutInfo) throws Exception;
}
