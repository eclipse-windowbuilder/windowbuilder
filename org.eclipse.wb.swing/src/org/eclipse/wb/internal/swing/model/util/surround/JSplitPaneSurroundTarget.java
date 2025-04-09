/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.internal.swing.model.util.surround;

import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.util.surround.ISurroundTarget;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.JSplitPaneInfo;

import org.eclipse.jface.resource.ImageDescriptor;

import java.util.List;

import javax.swing.JSplitPane;

/**
 * {@link ISurroundTarget} that uses {@link JSplitPane} as target container.
 *
 * @author scheglov_ke
 * @coverage swing.model.util
 */
public final class JSplitPaneSurroundTarget extends ISurroundTarget<JSplitPaneInfo, ComponentInfo> {
	private static final String CLASS_NAME = "javax.swing.JSplitPane";

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public ImageDescriptor getIcon(AstEditor editor) throws Exception {
		return ComponentDescriptionHelper.getDescription(editor, CLASS_NAME).getIcon();
	}

	@Override
	public String getText(AstEditor editor) throws Exception {
		return CLASS_NAME;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean validate(List<ComponentInfo> components) throws Exception {
		return components.size() <= 2;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Operation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public JSplitPaneInfo createContainer(AstEditor editor) throws Exception {
		return (JSplitPaneInfo) JavaInfoUtils.createJavaInfo(
				editor,
				CLASS_NAME,
				new ConstructorCreationSupport());
	}

	@Override
	public void move(JSplitPaneInfo container, ComponentInfo component) throws Exception {
		if (container.getChildrenComponents().isEmpty()) {
			container.command_ADD(component, true);
		} else {
			container.command_ADD(component, false);
		}
	}
}
