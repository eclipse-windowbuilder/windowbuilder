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
package org.eclipse.wb.internal.swt.model.util.surround;

import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.util.surround.ISurroundTarget;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.RowLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;

import java.util.List;

/**
 * {@link ISurroundTarget} for using any {@link Composite} as target container.
 *
 * @author scheglov_ke
 * @coverage swt.model.util
 */
public abstract class AbstractCompositeSurroundTarget
extends
ISurroundTarget<CompositeInfo, ControlInfo> {
	private final String m_className;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AbstractCompositeSurroundTarget(String className) {
		m_className = className;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public ImageDescriptor getIcon(AstEditor editor) throws Exception {
		return ComponentDescriptionHelper.getDescription(editor, m_className).getIcon();
	}

	@Override
	public String getText(AstEditor editor) throws Exception {
		return m_className;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Operation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public CompositeInfo createContainer(AstEditor editor) throws Exception {
		return (CompositeInfo) JavaInfoUtils.createJavaInfo(
				editor,
				m_className,
				new ConstructorCreationSupport());
	}

	@Override
	public void beforeComponentsMove(CompositeInfo container, List<ControlInfo> components)
			throws Exception {
		LayoutInfo layout = createLayout(container.getEditor(), "org.eclipse.swt.layout.RowLayout");
		container.setLayout(layout);
	}

	@Override
	public void move(CompositeInfo container, ControlInfo component) throws Exception {
		RowLayoutInfo layout = (RowLayoutInfo) container.getLayout();
		layout.command_MOVE(component, null);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the instance of {@link LayoutInfo} by its class name.
	 */
	private static LayoutInfo createLayout(AstEditor editor, String layoutClassName) throws Exception {
		return (LayoutInfo) JavaInfoUtils.createJavaInfo(
				editor,
				layoutClassName,
				new ConstructorCreationSupport());
	}
}
