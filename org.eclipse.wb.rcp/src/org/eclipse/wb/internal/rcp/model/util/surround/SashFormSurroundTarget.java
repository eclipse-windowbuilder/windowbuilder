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
import org.eclipse.wb.internal.rcp.model.widgets.SashFormInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.custom.SashForm;

/**
 * {@link ISurroundTarget} that uses {@link SashForm} as target container.
 *
 * @author scheglov_ke
 * @coverage rcp.model.util
 */
public final class SashFormSurroundTarget extends ISurroundTarget<SashFormInfo, ControlInfo> {
	private static final String CLASS_NAME = "org.eclipse.swt.custom.SashForm";
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final Object INSTANCE = new SashFormSurroundTarget();

	private SashFormSurroundTarget() {
	}

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
	// Operation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public SashFormInfo createContainer(AstEditor editor) throws Exception {
		return (SashFormInfo) JavaInfoUtils.createJavaInfo(
				editor,
				CLASS_NAME,
				new ConstructorCreationSupport());
	}

	@Override
	public void move(SashFormInfo container, ControlInfo component) throws Exception {
		container.command_MOVE(component, null);
	}
}
