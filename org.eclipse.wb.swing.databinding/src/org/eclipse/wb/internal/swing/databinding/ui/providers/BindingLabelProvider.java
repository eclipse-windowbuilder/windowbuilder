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
package org.eclipse.wb.internal.swing.databinding.ui.providers;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.swing.databinding.Activator;
import org.eclipse.wb.internal.swing.databinding.model.bindings.AutoBindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.bindings.BindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.bindings.ColumnBindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.bindings.DetailBindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.bindings.JComboBoxBindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.bindings.JListBindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.bindings.JTableBindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.bindings.VirtualBindingInfo;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import org.apache.commons.lang3.StringUtils;

/**
 * @author lobas_av
 * @coverage bindings.swing.ui
 */
public final class BindingLabelProvider extends LabelProvider
implements
ITableLabelProvider,
IColorProvider {
	private static final ImageDescriptor AUTO_BINDING_IMAGE = Activator.getImageDescriptor("autobinding2.png");
	private static final ImageDescriptor JLIST_BINDING_IMAGE = Activator.getImageDescriptor("JList.gif");
	private static final ImageDescriptor JLIST_DETAIL_BINDING_IMAGE = Activator.getImageDescriptor("JListDetail2.png");
	private static final ImageDescriptor JCOMBO_BOX_BINDING_IMAGE = Activator.getImageDescriptor("JComboBox.gif");
	private static final ImageDescriptor JTABLE_BINDING_IMAGE = Activator.getImageDescriptor("JTable.gif");
	private static final ImageDescriptor JTABLE_COLUMN_BINDING_IMAGE = Activator.getImageDescriptor("JTableColumnBinding.png");
	private final ResourceManager m_resourceManager = new LocalResourceManager(JFaceResources.getResources());

	@Override
	public void dispose() {
		super.dispose();
		m_resourceManager.dispose();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ITableLabelProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getColumnText(final Object element, final int column) {
		return getText(element, column);
	}

	public static String getText(Object element, int column) {
		return ExecutionUtils.runObjectLog(() -> {
			BindingInfo binding = (BindingInfo) element;
			switch (column) {
			case 1:
				// target
				return binding.getTargetPresentationText(true);
			case 2:
				// model
				return binding.getModelPresentationText(true);
			case 3:
				// strategy
				if (binding instanceof AutoBindingInfo autoBinding) {
					return autoBinding.getStrategyInfo().getStrategyValue();
				}
				return null;
			case 4:
			// binding
			{
				String variable = binding.getVariableIdentifier();
				if (variable != null) {
					String name = binding.getName();
					if (StringUtils.isEmpty(name)) {
						return variable;
					}
					return variable + " - " + name;
				}
			}
			}
			return null;
		}, "<exception, see log>");
	}

	@Override
	public Image getColumnImage(Object element, int column) {
		if (column == 0) {
			return m_resourceManager.createImageWithDefault(getIcon(element));
		}
		return null;
	}

	public static ImageDescriptor getIcon(Object element) {
		if (element instanceof JListBindingInfo) {
			return JLIST_BINDING_IMAGE;
		}
		if (element instanceof DetailBindingInfo) {
			return JLIST_DETAIL_BINDING_IMAGE;
		}
		if (element instanceof JComboBoxBindingInfo) {
			return JCOMBO_BOX_BINDING_IMAGE;
		}
		if (element instanceof JTableBindingInfo) {
			return JTABLE_BINDING_IMAGE;
		}
		if (element instanceof ColumnBindingInfo) {
			return JTABLE_COLUMN_BINDING_IMAGE;
		}
		if (element instanceof AutoBindingInfo) {
			return AUTO_BINDING_IMAGE;
		}
		if (element instanceof VirtualBindingInfo binding) {
			switch (binding.getSwingType()) {
			case JListBinding :
				return JLIST_BINDING_IMAGE;
			case JTableBinding :
				return JTABLE_BINDING_IMAGE;
			case JComboBoxBinding :
				return JCOMBO_BOX_BINDING_IMAGE;
			}
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IColorProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Color getForeground(Object element) {
		if (element instanceof VirtualBindingInfo) {
			return Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY);
		}
		return null;
	}

	@Override
	public Color getBackground(Object element) {
		return null;
	}
}