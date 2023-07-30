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
package org.eclipse.wb.internal.rcp.model.rcp.perspective;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.ObjectInfoUtils;
import org.eclipse.wb.core.model.association.InvocationVoidAssociation;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.presentation.DefaultJavaInfoPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.variable.VoidInvocationVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.rcp.model.rcp.PdeUtils;
import org.eclipse.wb.internal.rcp.model.rcp.PdeUtils.ViewInfo;
import org.eclipse.wb.internal.swt.support.CoordinateUtils;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IFolderLayout;

/**
 * Model for {@link IFolderLayout#addView(String)}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
public final class FolderViewInfo extends AbstractComponentInfo implements IRenderableInfo {
	private final PageLayoutCreateFolderInfo m_container;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FolderViewInfo(PageLayoutCreateFolderInfo container, MethodInvocation invocation)
			throws Exception {
		super(container.getEditor(),
				new ComponentDescription(null),
				new PageLayoutAddCreationSupport(container, invocation));
		m_container = container;
		ObjectInfoUtils.setNewId(this);
		getDescription().setToolkit(container.getDescription().getToolkit());
		setAssociation(new InvocationVoidAssociation());
		setVariableSupport(new VoidInvocationVariableSupport(this));
		container.addChild(this);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the <code>ID</code> of this view.
	 */
	public final String getId() {
		return (String) getInvocationArgument(0);
	}

	/**
	 * @return the underlying {@link MethodInvocation}.
	 */
	MethodInvocation getInvocation() {
		return (MethodInvocation) getCreationSupport().getNode();
	}

	/**
	 * @return the value of argument of underlying {@link MethodInvocation}.
	 */
	private Object getInvocationArgument(int index) {
		MethodInvocation invocation = getInvocation();
		Expression argument = DomGenerics.arguments(invocation).get(index);
		return JavaInfoEvaluationHelper.getValue(argument);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	private final IObjectPresentation m_presentation = new DefaultJavaInfoPresentation(this) {
		@Override
		public ImageDescriptor getIcon() throws Exception {
			return getPresentationIcon();
		}

		@Override
		public String getText() throws Exception {
			return getPresentationText();
		}
	};

	@Override
	public IObjectPresentation getPresentation() {
		return m_presentation;
	}

	/**
	 * @return the icon to show in component tree.
	 */
	private ImageDescriptor getPresentationIcon() throws Exception {
		return getViewInfo().getIcon();
	}

	/**
	 * @return the text to show in component tree.
	 */
	private String getPresentationText() throws Exception {
		return "\"" + getViewInfo().getName() + "\" - " + getId();
	}

	/**
	 * @return the {@link ViewInfo} for this view.
	 */
	private ViewInfo getViewInfo() {
		return PdeUtils.getViewInfoDefault(getId());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Rendering
	//
	////////////////////////////////////////////////////////////////////////////
	private CLabel m_viewLabel;

	@Override
	public Object render() throws Exception {
		CTabFolder folder = m_container.getFolder();
		Composite viewsComposite = m_container.getViewsComposite();
		// prepare presentation
		ImageDescriptor iconDescriptor = getViewInfo().getIcon();
		Image icon = iconDescriptor == null ? null : iconDescriptor.createImage();
		String name = getViewInfo().getName();
		if (icon != null) {
			folder.addDisposeListener(event -> icon.dispose());
		}
		// create item for view
		{
			CTabItem item = new CTabItem(folder, SWT.CLOSE);
			item.setImage(icon);
			item.setText(name);
			folder.setSelection(item);
		}
		// create control to show in client area of folder
		{
			m_viewLabel = new CLabel(viewsComposite, SWT.NONE);
			m_viewLabel.setImage(icon);
			m_viewLabel.setText(name);
		}
		// use CLabel as object
		return m_viewLabel;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refresh_fetch() throws Exception {
		setModelBounds(CoordinateUtils.getBounds(m_container.getFolder(), m_viewLabel));
		super.refresh_fetch();
	}
}
