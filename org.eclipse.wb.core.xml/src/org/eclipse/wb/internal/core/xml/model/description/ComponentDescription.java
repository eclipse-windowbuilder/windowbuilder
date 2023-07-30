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
package org.eclipse.wb.internal.core.xml.model.description;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.description.ComponentPresentation;
import org.eclipse.wb.internal.core.model.description.IComponentDescription;
import org.eclipse.wb.internal.core.model.description.MorphingTargetDescription;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.utils.StringUtilities;
import org.eclipse.wb.internal.core.utils.ui.ImageImageDescriptor;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Description of {@link Class} based component in XML.
 *
 * @author scheglov_ke
 * @coverage XML.model.description
 */
public final class ComponentDescription extends AbstractDescription
implements
IComponentDescription {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ComponentDescription(Class<?> componentClass) {
		m_componentClass = componentClass;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Component class
	//
	////////////////////////////////////////////////////////////////////////////
	private final Class<?> m_componentClass;

	@Override
	public Class<?> getComponentClass() {
		return m_componentClass;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// GUI toolkit
	//
	////////////////////////////////////////////////////////////////////////////
	private ToolkitDescription m_toolkit;

	@Override
	public ToolkitDescription getToolkit() {
		return m_toolkit;
	}

	/**
	 * Sets the {@link ToolkitDescription} for this component.
	 */
	public void setToolkit(ToolkitDescription toolkit) {
		m_toolkit = toolkit;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Model class
	//
	////////////////////////////////////////////////////////////////////////////
	private Class<?> m_modelClass;

	@Override
	public Class<?> getModelClass() {
		return m_modelClass;
	}

	/**
	 * Sets the {@link Class} of {@link ObjectInfo} that should be used for this component.
	 */
	public void setModelClass(Class<?> modelClass) {
		m_modelClass = modelClass;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Icon
	//
	////////////////////////////////////////////////////////////////////////////
	private Image m_icon;

	@Override
	public ImageDescriptor getIcon() {
		return new ImageImageDescriptor(m_icon);
	}

	/**
	 * Sets the icon for this component.
	 */
	public void setIcon(Image icon) {
		m_icon = icon;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Description
	//
	////////////////////////////////////////////////////////////////////////////
	private String m_description;

	/**
	 * @return the description text for this component.
	 */
	public String getDescription() {
		return m_description;
	}

	/**
	 * Sets the description text for this component.
	 */
	public void setDescription(String description) {
		if (description != null) {
			m_description = StringUtilities.normalizeWhitespaces(description);
			m_description = StringUtils.replace(m_description, "\\n", "\n");
		} else {
			m_description = m_componentClass.getName();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Caching presentation
	//
	////////////////////////////////////////////////////////////////////////////
	private boolean m_presentationCached;

	/**
	 * @return <code>true</code> if {@link ComponentPresentation} for this
	 *         {@link ComponentDescription} can be cached.
	 */
	public boolean isPresentationCached() {
		return m_presentationCached;
	}

	/**
	 * Specifies if {@link ComponentPresentation} for this {@link ComponentDescription} can be cached.
	 */
	public void setPresentationCached(boolean presentationCached) {
		m_presentationCached = presentationCached;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Generic properties
	//
	////////////////////////////////////////////////////////////////////////////
	private final List<GenericPropertyDescription> m_properties = Lists.newArrayList();
	private final Map<String, GenericPropertyDescription> m_idToProperty = Maps.newHashMap();

	/**
	 * @return the {@link GenericPropertyDescription}'s of this component.
	 */
	public List<GenericPropertyDescription> getProperties() {
		return Collections.unmodifiableList(m_properties);
	}

	/**
	 * @return the {@link GenericPropertyDescription} with given id.
	 */
	public GenericPropertyDescription getProperty(String id) {
		return m_idToProperty.get(id);
	}

	/**
	 * Adds new {@link GenericPropertyDescription}.
	 */
	public void addProperty(GenericPropertyDescription property) {
		String id = property.getId();
		if (!m_idToProperty.containsKey(id)) {
			m_idToProperty.put(id, property);
			m_properties.add(property);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Creation
	//
	////////////////////////////////////////////////////////////////////////////
	private final Map<String, CreationDescription> m_creations = Maps.newHashMap();

	/**
	 * @return all {@link CreationDescription}'s.
	 */
	public List<CreationDescription> getCreations() {
		return ImmutableList.copyOf(m_creations.values());
	}

	/**
	 * @return the {@link CreationDescription} for this component with given ID.
	 */
	public CreationDescription getCreation(String id) {
		return m_creations.get(id);
	}

	/**
	 * Adds the {@link CreationDescription} for component with exactly same class.
	 */
	public void addCreation(CreationDescription creation) {
		m_creations.put(creation.getId(), creation);
	}

	/**
	 * Removes all existing {@link CreationDescription}'s.
	 */
	public void clearCreations() {
		m_creations.clear();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Parameters
	//
	////////////////////////////////////////////////////////////////////////////
	private final Map<String, String> m_parameters = Maps.newTreeMap();

	/**
	 * Adds new parameter.
	 */
	public void addParameter(String name, String value) {
		m_parameters.put(name, value);
	}

	/**
	 * @return the read only {@link Map} of parameters.
	 */
	public Map<String, String> getParameters() {
		return Collections.unmodifiableMap(m_parameters);
	}

	/**
	 * @return the value of parameter with given name.
	 */
	public String getParameter(String name) {
		return m_parameters.get(name);
	}

	/**
	 * @return <code>true</code> description has parameter with value "true".
	 */
	public boolean hasTrueParameter(String name) {
		String parameter = getParameter(name);
		return "true".equals(parameter);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Morphing
	//
	////////////////////////////////////////////////////////////////////////////
	private final List<MorphingTargetDescription> m_morphingTargets = Lists.newArrayList();

	/**
	 * @return the {@link MorphingTargetDescription}'s registered for this component.
	 */
	public List<MorphingTargetDescription> getMorphingTargets() {
		return m_morphingTargets;
	}

	/**
	 * Registers new {@link MorphingTargetDescription}.
	 */
	public void addMorphingTarget(MorphingTargetDescription morphingTarget) {
		m_morphingTargets.add(morphingTarget);
	}

	/**
	 * Clear registered {@link MorphingTargetDescription}'s list.
	 */
	public void clearMorphingTargets() {
		m_morphingTargets.clear();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Post processing
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Post processing after loading complete.
	 */
	public void postProcess() throws Exception {
		// remove properties without editors
		for (Iterator<GenericPropertyDescription> I = m_properties.iterator(); I.hasNext();) {
			GenericPropertyDescription property = I.next();
			if (property.getEditor() == null) {
				I.remove();
				m_idToProperty.remove(property.getId());
			}
		}
		// ensure default creation
		if (m_creations.isEmpty()) {
			CreationDescription defaultCreation = new CreationDescription(this, null, null);
			addCreation(defaultCreation);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Visiting
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void visit(XmlObjectInfo object, int state) throws Exception {
		super.visit(object, state);
		for (GenericPropertyDescription property : m_idToProperty.values()) {
			property.visit(object, state);
		}
	}
}
