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
package org.eclipse.wb.internal.rcp.model.util.surround;

import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.util.surround.ISurroundTarget;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.rcp.model.widgets.TabFolderInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.TabFolder;

/**
 * {@link ISurroundTarget} that uses {@link TabFolder} as target container.
 *
 * @author scheglov_ke
 * @coverage rcp.model.util
 */
public final class TabFolderSurroundTarget extends ISurroundTarget<TabFolderInfo, ControlInfo> {
	private static final String FOLDER_CLASS_NAME = "org.eclipse.swt.widgets.TabFolder";
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final Object INSTANCE = new TabFolderSurroundTarget();

	private TabFolderSurroundTarget() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public ImageDescriptor getIcon(AstEditor editor) throws Exception {
		return ComponentDescriptionHelper.getDescription(editor, FOLDER_CLASS_NAME).getIcon();
	}

	@Override
	public String getText(AstEditor editor) throws Exception {
		return FOLDER_CLASS_NAME;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Operation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public TabFolderInfo createContainer(AstEditor editor) throws Exception {
		return (TabFolderInfo) JavaInfoUtils.createJavaInfo(
				editor,
				FOLDER_CLASS_NAME,
				new ConstructorCreationSupport());
	}

	@Override
	public void move(TabFolderInfo container, ControlInfo component) throws Exception {
		container.command_MOVE(component, null);
	}
}
