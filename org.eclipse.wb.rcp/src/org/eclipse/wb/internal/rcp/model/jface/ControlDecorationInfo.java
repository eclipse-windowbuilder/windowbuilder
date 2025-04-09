/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.internal.rcp.model.jface;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.presentation.DefaultJavaInfoPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.DrawUtils;
import org.eclipse.wb.internal.rcp.Activator;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

/**
 * Model for {@link org.eclipse.jface.fieldassist.ControlDecoration}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.jface
 */
public final class ControlDecorationInfo extends AbstractComponentInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ControlDecorationInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link ControlInfo} that is decorated by this {@link ControlDecorationInfo}.
	 */
	public ControlInfo getControl() {
		return (ControlInfo) getParent();
	}

	/**
	 * Convenience class that returns the {@link ControlDecoration} contained by
	 * this {@link ControlDecorationInfo}.
	 */
	protected ControlDecoration getDecoration() {
		return (ControlDecoration) getObject();
	}

	/**
	 * Convenience method that calls the protected method
	 * {@code getDecorationRectangle(Control)} of {@link ControlDecoration}.
	 */
	protected org.eclipse.swt.graphics.Rectangle getDecorationRectangle() throws Exception {
		return (org.eclipse.swt.graphics.Rectangle) ReflectionUtils.invokeMethod(getDecoration(),
				"getDecorationRectangle(org.eclipse.swt.widgets.Control)", getDecoration().getControl());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	private Image m_decorationImage;
	private Image m_iconImage;
	private final IObjectPresentation m_presentation = new DefaultJavaInfoPresentation(this) {
		@Override
		public ImageDescriptor getIcon() throws Exception {
			if (m_decorationImage != null && m_iconImage != null) {
				return ImageDescriptor.createFromImage(m_iconImage);
			}
			return super.getIcon();
		}
	};

	@Override
	public IObjectPresentation getPresentation() {
		return m_presentation;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void refresh_dispose() throws Exception {
		super.refresh_dispose();
		// dispose image
		if (m_iconImage != null) {
			m_iconImage.dispose();
			m_iconImage = null;
		}
	}

	@Override
	protected void refresh_afterCreate() throws Exception {
		super.refresh_afterCreate();
		// prepare "real" image
		m_decorationImage = getDecoration().getImage();
		// if no "real" image, set default one
		if (m_decorationImage == null) {
			getDecoration().setImage(Activator.getImage("info/ControlDecoration/default.gif"));
		}
	}

	@Override
	protected void refresh_fetch() throws Exception {
		// bounds
		{
			Rectangle decorationRectangle = new Rectangle(getDecorationRectangle());
			setModelBounds(decorationRectangle);
		}
		// image icon
		if (m_decorationImage != null) {
			m_iconImage = new Image(null, 16, 16);
			GC gc = new GC(m_iconImage);
			try {
				DrawUtils.drawImageCHCV(gc, m_decorationImage, 0, 0, 16, 16);
			} finally {
				gc.dispose();
			}
		}
		// continue
		super.refresh_fetch();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Decorates {@link ControlInfo} with this new {@link ControlDecorationInfo}.
	 */
	public void command_CREATE(ControlInfo target) throws Exception {
		JavaInfoUtils.add(this, null, target, null);
	}

	/**
	 * Decorates {@link ControlInfo} with this (already existing) {@link ControlDecorationInfo}.
	 */
	public void command_ADD(ControlInfo target) throws Exception {
		JavaInfoUtils.move(this, null, target, null);
	}
}
