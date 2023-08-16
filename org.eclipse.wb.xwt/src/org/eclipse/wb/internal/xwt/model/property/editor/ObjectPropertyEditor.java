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
package org.eclipse.wb.internal.xwt.model.property.editor;

import com.google.common.base.Predicate;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDialogPropertyEditor;
import org.eclipse.wb.internal.core.model.util.ObjectsLabelProvider;
import org.eclipse.wb.internal.core.model.util.ObjectsTreeContentProvider;
import org.eclipse.wb.internal.core.utils.dialogfields.StatusUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.property.GenericProperty;
import org.eclipse.wb.internal.xwt.model.util.NameSupport;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;

/**
 * {@link PropertyEditor} for selecting model of {@link Object} in XWT.
 *
 * @author scheglov_ke
 * @coverage XWT.model.property.editor
 */
public final class ObjectPropertyEditor extends TextDialogPropertyEditor {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final PropertyEditor INSTANCE = new ObjectPropertyEditor();

	private ObjectPropertyEditor() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	public XmlObjectInfo getValueComponent(Property property) throws Exception {
		Object value = property.getValue();
		GenericProperty genericProperty = (GenericProperty) property;
		return genericProperty.getObject().getRootXML().getChildByObject(value);
	}

	@Override
	protected String getText(Property property) throws Exception {
		XmlObjectInfo component = getValueComponent(property);
		if (component != null) {
			return ObjectInfo.getText(component);
		}
		// unknown value
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Editing
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void openDialog(Property property_) throws Exception {
		GenericProperty property = (GenericProperty) property_;
		// prepare dialog
		ElementTreeSelectionDialog selectionDialog;
		{
			final XmlObjectInfo thisComponent = property.getObject();
			final Class<?> propertyType = property.getType();
			// providers
			ISelectionStatusValidator validator = new ISelectionStatusValidator() {
				@Override
				public IStatus validate(Object[] selection) {
					if (selection.length == 1) {
						if (isValidComponent(propertyType, selection[0])) {
							return StatusUtils.OK_STATUS;
						}
					}
					return StatusUtils.ERROR_STATUS;
				}
			};
			ITreeContentProvider contentProvider = createContentProvider(propertyType);
			// create dialog
			selectionDialog =
					new ElementTreeSelectionDialog(DesignerPlugin.getShell(), new ObjectsLabelProvider(),
							contentProvider) {
				@Override
				public void create() {
					super.create();
					getTreeViewer().expandAll();
				}
			};
			selectionDialog.setAllowMultiple(false);
			selectionDialog.setTitle(property.getTitle());
			selectionDialog.setMessage("Select component:");
			selectionDialog.setValidator(validator);
			// set input
			selectionDialog.setInput(new Object[]{thisComponent.getRoot()});
			// set initial selection
			{
				XmlObjectInfo component = getValueComponent(property);
				selectionDialog.setInitialSelection(component);
			}
		}
		// open dialog
		if (selectionDialog.open() == Window.OK) {
			XmlObjectInfo component = (XmlObjectInfo) selectionDialog.getFirstResult();
			setComponent(property, component);
		}
	}

	private ITreeContentProvider createContentProvider(final Class<?> propertyType) {
		final ITreeContentProvider[] contentProvider = new ITreeContentProvider[1];
		contentProvider[0] = new ObjectsTreeContentProvider(new Predicate<ObjectInfo>() {
			@Override
			public boolean apply(ObjectInfo t) {
				return isValidComponent(propertyType, t) || hasValidComponents(t);
			}

			private boolean hasValidComponents(ObjectInfo t) {
				return contentProvider[0].getChildren(t).length != 0;
			}
		});
		return contentProvider[0];
	}

	private boolean isValidComponent(Class<?> propertyType, Object element) {
		if (element instanceof XmlObjectInfo) {
			XmlObjectInfo component = (XmlObjectInfo) element;
			Class<?> componentClass = component.getDescription().getComponentClass();
			return componentClass != null && propertyType.isAssignableFrom(componentClass);
		}
		return false;
	}

	/**
	 * Sets new {@link XmlObjectInfo} value.
	 */
	public void setComponent(final GenericProperty property, final XmlObjectInfo component)
			throws Exception {
		ExecutionUtils.run(property.getObject(), new RunnableEx() {
			@Override
			public void run() throws Exception {
				setComponent0(property, component);
			}
		});
	}

	/**
	 * Implementation for {@link #setComponent(GenericProperty, XmlObjectInfo)}.
	 */
	private void setComponent0(GenericProperty property, XmlObjectInfo component) throws Exception {
		if (component != null) {
			String name = NameSupport.ensureName(component);
			String expression = "{Binding ElementName=" + name + "}";
			property.setExpression(expression, component);
		}
	}
}
