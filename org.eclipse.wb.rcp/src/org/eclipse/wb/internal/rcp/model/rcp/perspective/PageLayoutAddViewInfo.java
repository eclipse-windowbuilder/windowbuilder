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
package org.eclipse.wb.internal.rcp.model.rcp.perspective;

import org.eclipse.wb.core.model.ObjectInfoUtils;
import org.eclipse.wb.core.model.association.InvocationVoidAssociation;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.JavaProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.BooleanPropertyEditor;
import org.eclipse.wb.internal.core.model.variable.VoidInvocationVariableSupport;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.rcp.model.rcp.PdeUtils;
import org.eclipse.wb.internal.rcp.model.rcp.PdeUtils.ViewInfo;

import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IPageLayout;

import java.util.List;

/**
 * Model for {@link IPageLayout#addView(String, int, float, String)}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
public final class PageLayoutAddViewInfo extends AbstractPartInfo {
	private final PageLayoutInfo m_page;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PageLayoutAddViewInfo(PageLayoutInfo page, MethodInvocation invocation) throws Exception {
		super(page.getEditor(), new ComponentDescription(null), new PageLayoutAddCreationSupport(page,
				invocation));
		m_page = page;
		ObjectInfoUtils.setNewId(this);
		getDescription().setToolkit(page.getDescription().getToolkit());
		setVariableSupport(new VoidInvocationVariableSupport(this));
		setAssociation(new InvocationVoidAssociation());
		page.addChild(this);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected int getIndexOffest() {
		if (getInvocationSignature().equals(
				"addStandaloneView(java.lang.String,boolean,int,float,java.lang.String)")) {
			return 1;
		}
		return super.getIndexOffest();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	////////////////////////////////////////////////////////////////////////////
	private final Property m_standaloneProperty = new JavaProperty(this, "standalone",
			BooleanPropertyEditor.INSTANCE) {
		@Override
		public boolean isModified() throws Exception {
			return true;
		}

		@Override
		public Object getValue() throws Exception {
			return isStandalone();
		}

		@Override
		public void setValue(final Object value) throws Exception {
			ExecutionUtils.run(m_page, new RunnableEx() {
				@Override
				public void run() throws Exception {
					setStandalone((Boolean) value);
				}
			});
		}
	};
	private final Property m_placeholderProperty = new JavaProperty(this, "placeholder",
			BooleanPropertyEditor.INSTANCE) {
		@Override
		public boolean isModified() throws Exception {
			return true;
		}

		@Override
		public Object getValue() throws Exception {
			return isPlaceholder2();
		}

		@Override
		public void setValue(final Object value) throws Exception {
			ExecutionUtils.run(m_page, new RunnableEx() {
				@Override
				public void run() throws Exception {
					setPlaceholder((Boolean) value);
				}
			});
		}
	};

	@Override
	protected List<Property> getPropertyList() throws Exception {
		List<Property> properties = super.getPropertyList();
		properties.add(m_standaloneProperty);
		properties.add(m_placeholderProperty);
		return properties;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// State
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return <code>true</code> if this view is stand-alone, i.e. created using
	 *         {@link IPageLayout#addStandaloneView(String, boolean, int, float, String)} or
	 *         {@link IPageLayout#addStandaloneViewPlaceholder(String, int, float, String, boolean)}.
	 */
	public boolean isStandalone() {
		String signature = getInvocationSignature();
		return signature.equals("addStandaloneView(java.lang.String,boolean,int,float,java.lang.String)")
				|| signature.equals("addStandaloneViewPlaceholder(java.lang.String,int,float,java.lang.String,boolean)");
	}

	/**
	 * @return <code>true</code> if this view is place-holder, i.e. created using
	 *         {@link IPageLayout#addPlaceholder(String, int, float, String)} or
	 *         {@link IPageLayout#addStandaloneViewPlaceholder(String, int, float, String, boolean)}.
	 */
	public boolean isPlaceholder2() {
		String signature = getInvocationSignature();
		return signature.equals("addPlaceholder(java.lang.String,int,float,java.lang.String)")
				|| signature.equals("addStandaloneViewPlaceholder(java.lang.String,int,float,java.lang.String,boolean)");
	}

	/**
	 * If given <code>makeStandalone</code> argument is <code>true</code>,converts creation to use
	 * {@link IPageLayout#addStandaloneView(String, boolean, int, float, String)} or
	 * {@link IPageLayout#addStandaloneViewPlaceholder(String, int, float, String, boolean)}.
	 */
	public void setStandalone(boolean makeStandalone) throws Exception {
		boolean wasStandalone = isStandalone();
		boolean isPlaceholder = isPlaceholder2();
		if (makeStandalone && !wasStandalone) {
			if (isPlaceholder) {
				// was "addPlaceholder", make "addStandaloneViewPlaceholder"
				morphInvocation("addStandaloneViewPlaceholder(%s, %s, %s, %s, true)");
			} else {
				// was "addView", make "addStandaloneView"
				morphInvocation("addStandaloneView(%s, true, %s, %s, %s)");
			}
		} else if (!makeStandalone && wasStandalone) {
			if (isPlaceholder) {
				// was "addStandaloneViewPlaceholder", make "addPlaceholder"
				morphInvocation("addPlaceholder(%s, %s, %s, %s)");
			} else {
				// was "addStandaloneView", make "addView"
				morphInvocation("addView(%s, %s, %s, %s)");
			}
		}
	}

	/**
	 * If given <code>makePlaceholder</code> argument is <code>true</code>,converts creation to use
	 * {@link IPageLayout#addPlaceholder(String, int, float, String)} or
	 * {@link IPageLayout#addStandaloneViewPlaceholder(String, int, float, String, boolean)}.
	 */
	public void setPlaceholder(boolean makePlaceholder) throws Exception {
		boolean isStandalone = isStandalone();
		boolean wasPlaceholder = isPlaceholder2();
		if (makePlaceholder && !wasPlaceholder) {
			if (isStandalone) {
				// was "addStandaloneView", make "addStandaloneViewPlaceholder"
				morphInvocation("addStandaloneViewPlaceholder(%s, %s, %s, %s, true)");
			} else {
				// was "addView", make "addPlaceholder"
				morphInvocation("addPlaceholder(%s, %s, %s, %s)");
			}
		} else if (!makePlaceholder && wasPlaceholder) {
			if (isStandalone) {
				// was "addStandaloneViewPlaceholder", make "addStandaloneView"
				morphInvocation("addStandaloneView(%s, true, %s, %s, %s)");
			} else {
				// was "addPlaceholder", make "addView"
				morphInvocation("addView(%s, %s, %s, %s)");
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected ImageDescriptor getPresentationIcon() {
		return getViewInfo().getIcon();
	}

	@Override
	protected String getPresentationText() {
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
	@Override
	public Control render() throws Exception {
		CTabFolder folder = PageLayoutInfo.createPartFolder(m_page.getPartsComposite());
		// create CTabItem
		CTabItem item = new CTabItem(folder, SWT.CLOSE);
		ImageDescriptor imageDescriptor = getPresentationIcon();
		if (imageDescriptor != null) {
			Image image = imageDescriptor.createImage();
			item.addDisposeListener(event -> image.dispose());
			item.setImage(image);
		}
		item.setText(getViewInfo().getName());
		// return CTabFolder
		folder.setSelection(item);
		return folder;
	}
}
