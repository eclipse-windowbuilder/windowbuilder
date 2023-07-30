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
import org.eclipse.wb.internal.rcp.model.widgets.ScrolledCompositeInfo;
import org.eclipse.wb.internal.swt.model.util.surround.CompositeSurroundTarget;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.custom.ScrolledComposite;

import java.util.List;

/**
 * {@link ISurroundTarget} that uses {@link ScrolledComposite} as target container.
 *
 * @author scheglov_ke
 * @coverage rcp.model.util
 */
public final class ScrolledCompositeSurroundTarget
extends
ISurroundTarget<ScrolledCompositeInfo, ControlInfo> {
	private static final String CLASS_NAME = "org.eclipse.swt.custom.ScrolledComposite";
	private static final CompositeSurroundTarget COMPOSITE_TARGET = CompositeSurroundTarget.INSTANCE;
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final Object INSTANCE = new ScrolledCompositeSurroundTarget();

	private ScrolledCompositeSurroundTarget() {
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
	public ScrolledCompositeInfo createContainer(AstEditor editor) throws Exception {
		return (ScrolledCompositeInfo) JavaInfoUtils.createJavaInfo(
				editor,
				CLASS_NAME,
				new ConstructorCreationSupport());
	}

	@Override
	public void beforeComponentsMove(ScrolledCompositeInfo scrolledComposite,
			List<ControlInfo> components) throws Exception {
		if (components.size() > 1) {
			CompositeInfo container = COMPOSITE_TARGET.createContainer(scrolledComposite.getEditor());
			scrolledComposite.command_CREATE(container);
			container.getRoot().refreshLight();
			COMPOSITE_TARGET.afterContainerAdd(container, components);
			COMPOSITE_TARGET.beforeComponentsMove(container, components);
		}
	}

	@Override
	public void move(ScrolledCompositeInfo scrolledComposite, ControlInfo component) throws Exception {
		if (scrolledComposite.getContent() == null) {
			scrolledComposite.command_ADD(component);
		} else {
			CompositeInfo container = (CompositeInfo) scrolledComposite.getContent();
			COMPOSITE_TARGET.move(container, component);
		}
	}
}
