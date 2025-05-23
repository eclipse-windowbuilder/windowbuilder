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
package org.eclipse.wb.internal.swing.model.layout;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaInfoAddProperties;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import java.awt.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JComponent;

/**
 * Model for {@link BoxLayout}.
 *
 * @author scheglov_ke
 * @coverage swing.model.layout
 */
public final class BoxLayoutInfo extends GenericFlowLayoutInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public BoxLayoutInfo(AstEditor editor,
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
	 * @return <code>true</code> if this {@link BoxLayoutInfo} lays out {@link Component}'s
	 *         horizontally.
	 */
	public boolean isHorizontal() {
		int axis = getLayoutManager().getAxis();
		return axis == BoxLayout.X_AXIS || axis == BoxLayout.LINE_AXIS;
	}

	@Override
	public BoxLayout getLayoutManager() {
		return (BoxLayout) super.getObject();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Initialize
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void initialize() throws Exception {
		super.initialize();
		// add "Alignment" property for children JComponent's
		addBroadcastListener(new JavaInfoAddProperties() {
			@Override
			public void invoke(JavaInfo javaInfo, List<Property> properties) throws Exception {
				if (javaInfo instanceof ComponentInfo component
						&& javaInfo.getParent() == getContainer()
						&& JComponent.class.isAssignableFrom(javaInfo.getDescription().getComponentClass())) {
					Property alignmentProperty = addAlignmentProperty(component, properties);
					properties.add(alignmentProperty);
				}
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	////////////////////////////////////////////////////////////////////////////
	private final Map<ComponentInfo, Property> m_alignmentProperties = new HashMap<>();

	/**
	 * @return the composite "Alignment" property constructed from given {@link Property}'s.
	 */
	private Property addAlignmentProperty(ComponentInfo component, List<Property> properties)
			throws Exception {
		Property property = m_alignmentProperties.get(component);
		if (property == null) {
			ComplexProperty alignmentProperty = new ComplexProperty("Alignment", "(X/Y alignments)");
			alignmentProperty.setModified(true);
			alignmentProperty.setCategory(PropertyCategory.system(6));
			// add sub-properties
			Property alignmentX = getAlignmentSubProperty(properties, "alignmentX");
			Property alignmentY = getAlignmentSubProperty(properties, "alignmentY");
			alignmentProperty.setProperties(new Property[]{alignmentX, alignmentY});
			// remember
			m_alignmentProperties.put(component, alignmentProperty);
			property = alignmentProperty;
		}
		return property;
	}

	/**
	 * @return the non-advanced copy of {@link GenericPropertyImpl} with given title.
	 */
	private static Property getAlignmentSubProperty(List<Property> properties, String title)
			throws Exception {
		GenericPropertyImpl alignmentSubProperty = null;
		for (Property property : properties) {
			if (property instanceof GenericPropertyImpl genericProperty && property.getTitle().equals(title)) {
				alignmentSubProperty = new GenericPropertyImpl(genericProperty, property.getTitle());
				alignmentSubProperty.setCategory(PropertyCategory.NORMAL);
				break;
			}
		}
		Assert.isNotNull(alignmentSubProperty);
		return alignmentSubProperty;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Manage general layout data.
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void storeLayoutData(ComponentInfo component) throws Exception {
		storeLayoutDataDefault(component);
	}
}
