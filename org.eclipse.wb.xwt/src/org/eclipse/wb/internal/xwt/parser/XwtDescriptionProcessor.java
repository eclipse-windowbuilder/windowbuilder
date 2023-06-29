/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.xwt.parser;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipProvider;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipTextProvider;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.description.DescriptionPropertiesHelper;
import org.eclipse.wb.internal.core.xml.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.xml.model.description.IDescriptionProcessor;
import org.eclipse.wb.internal.core.xml.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.xwt.model.property.editor.ObjectPropertyEditor;

import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.widgets.Widget;

import java.util.List;

/**
 * {@link IDescriptionProcessor} for XWT.
 *
 * @author scheglov_ke
 * @coverage XWT.parser
 */
public class XwtDescriptionProcessor implements IDescriptionProcessor {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final IDescriptionProcessor INSTANCE = new XwtDescriptionProcessor();

	private XwtDescriptionProcessor() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IDescriptionProcessor
	//
	////////////////////////////////////////////////////////////////////////////
	public void process(EditorContext context, ComponentDescription componentDescription)
			throws Exception {
		if (isXWT(context)) {
			useObjectPropertyEditor(componentDescription);
			addProperties_TableViewerColumn(componentDescription);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return <code>true</code> if given {@link XmlObjectInfo} is XWT.
	 */
	public static boolean isXWT(XmlObjectInfo object) {
		EditorContext context = object.getContext();
		return isXWT(context);
	}

	/**
	 * @return <code>true</code> if given {@link EditorContext} is XWT.
	 */
	public static boolean isXWT(EditorContext context) {
		return context.getToolkit().getId().equals("org.eclipse.wb.rcp");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Use {@link ObjectPropertyEditor} to select {@link Widget} model.
	 */
	private void useObjectPropertyEditor(ComponentDescription componentDescription) {
		List<GenericPropertyDescription> properties = componentDescription.getProperties();
		for (GenericPropertyDescription property : properties) {
			if (property.getEditor() == null
					&& ReflectionUtils.isSuccessorOf(property.getType(), "org.eclipse.swt.widgets.Widget")) {
				property.setEditor(ObjectPropertyEditor.INSTANCE);
				property.setCategory(PropertyCategory.ADVANCED);
			}
		}
	}

	private void addProperties_TableViewerColumn(ComponentDescription componentDescription)
			throws Exception {
		if (componentDescription.getComponentClass() != TableViewerColumn.class) {
			return;
		}
		// make other properties "normal"
		for (GenericPropertyDescription property : componentDescription.getProperties()) {
			if (property.getCategory().isPreferred()) {
				property.setCategory(PropertyCategory.NORMAL);
			}
		}
		// text
		{
			GenericPropertyDescription property =
					createPropertyDescription(
							"text",
							"Sets the column text.",
							String.class,
							"object.column.text");
			property.putTag("isText", "true");
			property.setCategory(PropertyCategory.PREFERRED);
			componentDescription.addProperty(property);
		}
		// width
		{
			GenericPropertyDescription property =
					createPropertyDescription(
							"width",
							"Sets the column width in pixels.",
							int.class,
							"object.column.width");
			property.setCategory(PropertyCategory.PREFERRED);
			componentDescription.addProperty(property);
		}
	}

	private GenericPropertyDescription createPropertyDescription(String attribute,
			final String tooltip,
			Class<?> type,
			final String script) throws Exception {
		ExpressionAccessor accessor = new ExpressionAccessor(attribute) {
			@Override
			public Object getDefaultValue(XmlObjectInfo object) throws Exception {
				return ScriptUtils.evaluate(script, object);
			}

			@Override
			public <T> T getAdapter(Class<T> adapter) {
				if (adapter == PropertyTooltipProvider.class) {
					return adapter.cast(new PropertyTooltipTextProvider() {
						@Override
						protected String getText(Property property) throws Exception {
							return tooltip;
						}
					});
				}
				return super.getAdapter(adapter);
			}
		};
		GenericPropertyDescription property =
				new GenericPropertyDescription(attribute, attribute, type, accessor);
		property.setConverter(DescriptionPropertiesHelper.getConverterForType(type));
		property.setEditor(DescriptionPropertiesHelper.getEditorForType(type));
		return property;
	}
}
