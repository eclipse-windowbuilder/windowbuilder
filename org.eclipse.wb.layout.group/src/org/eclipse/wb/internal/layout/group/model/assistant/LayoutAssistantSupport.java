/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.layout.group.model.assistant;

import org.eclipse.wb.core.editor.actions.assistant.ILayoutAssistantPage;
import org.eclipse.wb.core.editor.actions.assistant.LayoutAssistantListener;
import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.JavaInfoAddProperties;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.presentation.ButtonPropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.core.utils.ui.TabFactory;
import org.eclipse.wb.internal.layout.group.Messages;
import org.eclipse.wb.internal.layout.group.model.IGroupLayoutInfo;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;

import java.util.List;

/**
 * Provides support for Layout Assistant and surrogate 'Constraints' property.
 *
 * @author mitin_aa
 */
public class LayoutAssistantSupport {
	private final IGroupLayoutInfo m_layout;
	private ConstraintsDialog m_constraintsDialog;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public LayoutAssistantSupport(IGroupLayoutInfo layout) {
		m_layout = layout;
		// assistant
		getJavaInfo().addBroadcastListener(new LayoutAssistantListener() {
			@Override
			public void createAssistantPages(List<ObjectInfo> objects,
					TabFolder folder,
					List<ILayoutAssistantPage> pages) throws Exception {
				LayoutAssistantSupport.this.createAssistantPages(objects, folder, pages);
			}
		});
		// constraints property
		getJavaInfo().addBroadcastListener(new JavaInfoAddProperties() {
			@Override
			public void invoke(JavaInfo javaInfo, List<Property> properties) throws Exception {
				if (m_layout.isRelatedComponent(javaInfo)) {
					addConstraintsProperty(properties, (AbstractComponentInfo) javaInfo);
				}
			}
		});
		getJavaInfo().addBroadcastListener(new ObjectEventListener() {
			@Override
			public void refreshed() throws Exception {
				updateConstraintsDialog();
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Assistant
	//
	////////////////////////////////////////////////////////////////////////////
	private void createAssistantPages(List<ObjectInfo> objects,
			TabFolder folder,
			List<ILayoutAssistantPage> pages) throws Exception {
		if (!objects.isEmpty() && m_layout.isRelatedComponent(objects.get(0))) {
			Composite page;
			if (objects.size() == 1) {
				page = new GroupLayoutSpacesPage(folder, m_layout, objects.get(0));
			} else {
				page = new GroupLayoutAlignmentPage(folder, m_layout, objects);
			}
			TabFactory.item(folder).text(Messages.LayoutAssistantSupport_layoutTabTitle).control(page);
			pages.add((ILayoutAssistantPage) page);
		}
	}

	protected void addConstraintsProperty(List<Property> properties,
			final AbstractComponentInfo component) {
		String name = getJavaInfo().getDescription().getComponentClass().getName();
		ComplexProperty constraintsProperty = new ComplexProperty(Messages.LayoutAssistantSupport_constraintsTabTitle, "(" + name + ")") {
			@Override
			public boolean isModified() throws Exception {
				return true;
			}
		};
		constraintsProperty.setCategory(PropertyCategory.system(6));
		constraintsProperty.setEditorPresentation(new ButtonPropertyEditorPresentation() {
			@Override
			protected void onClick(PropertyTable propertyTable, Property property) throws Exception {
				m_constraintsDialog = new ConstraintsDialog(propertyTable.getControl().getShell(), m_layout, component);
				m_constraintsDialog.create();
				m_constraintsDialog.getShell().addDisposeListener(new DisposeListener() {
					@Override
					public void widgetDisposed(DisposeEvent e) {
						m_constraintsDialog = null;
					}
				});
				m_constraintsDialog.updateUI();
				m_constraintsDialog.open();
			}
		});
		properties.add(constraintsProperty);
	}

	protected void updateConstraintsDialog() {
		if (m_constraintsDialog != null) {
			m_constraintsDialog.updateUI();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Misc
	//
	////////////////////////////////////////////////////////////////////////////
	private JavaInfo getJavaInfo() {
		return m_layout.getAdapter(JavaInfo.class);
	}
}
