/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.wb.internal.core.utils.ui.ImageImageDescriptor;
import org.eclipse.wb.internal.rcp.model.widgets.CTabFolderInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.custom.CTabFolder;

/**
 * {@link ISurroundTarget} that uses {@link CTabFolder} as target container.
 *
 * @author scheglov_ke
 * @coverage rcp.model.util
 */
public final class CTabFolderSurroundTarget extends ISurroundTarget<CTabFolderInfo, ControlInfo> {
	private static final String FOLDER_CLASS_NAME = "org.eclipse.swt.custom.CTabFolder";
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final Object INSTANCE = new CTabFolderSurroundTarget();

	private CTabFolderSurroundTarget() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public ImageDescriptor getIcon(AstEditor editor) throws Exception {
		return new ImageImageDescriptor(ComponentDescriptionHelper.getDescription(editor, FOLDER_CLASS_NAME).getIcon());
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
	public CTabFolderInfo createContainer(AstEditor editor) throws Exception {
		return (CTabFolderInfo) JavaInfoUtils.createJavaInfo(
				editor,
				FOLDER_CLASS_NAME,
				new ConstructorCreationSupport());
	}

	@Override
	public void move(CTabFolderInfo container, ControlInfo component) throws Exception {
		container.command_MOVE(component, null);
	}
}
