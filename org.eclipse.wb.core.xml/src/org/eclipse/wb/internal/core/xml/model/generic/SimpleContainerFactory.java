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
package org.eclipse.wb.internal.core.xml.model.generic;

import org.eclipse.wb.internal.core.model.generic.ContainerObjectValidator;
import org.eclipse.wb.internal.core.model.generic.ContainerObjectValidators;
import org.eclipse.wb.internal.core.model.generic.SimpleContainer;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.association.Association;
import org.eclipse.wb.internal.core.xml.model.association.Associations;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory for accessing {@link SimpleContainer} for {@link XmlObjectInfo}.
 *
 * @author scheglov_ke
 * @coverage XML.model.generic
 */
public final class SimpleContainerFactory {
	private final XmlObjectInfo m_object;
	private final boolean m_forCanvas;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SimpleContainerFactory(XmlObjectInfo object, boolean forCanvas) {
		m_object = object;
		m_forCanvas = forCanvas;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public List<SimpleContainer> get() {
		List<SimpleContainer> containers = new ArrayList<>();
		addConfigurableContainers(containers);
		return containers;
	}

	private void addConfigurableContainers(List<SimpleContainer> containers) {
		List<SimpleContainerConfiguration> configurations = getConfigurations();
		for (SimpleContainerConfiguration configuration : configurations) {
			SimpleContainer container = new SimpleContainerConfigurable(m_object, configuration);
			containers.add(container);
		}
	}

	public List<SimpleContainerConfiguration> getConfigurations() {
		List<SimpleContainerConfiguration> configurations = new ArrayList<>();
		for (String prefix : getConfigurationPrefixes()) {
			SimpleContainerConfiguration configuration = createConfiguration(prefix);
			configurations.add(configuration);
		}
		return configurations;
	}

	private List<String> getConfigurationPrefixes() {
		List<String> prefixes = new ArrayList<>();
		addConfigurationPrefixes(prefixes, "simpleContainer");
		if (m_forCanvas) {
			addConfigurationPrefixes(prefixes, "simpleContainer.canvas");
		} else {
			addConfigurationPrefixes(prefixes, "simpleContainer.tree");
		}
		return prefixes;
	}

	private void addConfigurationPrefixes(List<String> prefixes, String basePrefix) {
		for (int i = 0; i < 10; i++) {
			String prefix = basePrefix + (i == 0 ? "" : "." + i);
			String validatorText = getParameter(prefix);
			if (validatorText != null) {
				if (ContainerObjectValidators.validateContainer(m_object, validatorText)) {
					prefixes.add(prefix);
				}
			}
		}
	}

	private SimpleContainerConfiguration createConfiguration(String prefix) {
		return new SimpleContainerConfiguration(getComponentValidator(prefix), getAssociation(prefix));
	}

	private ContainerObjectValidator getComponentValidator(String prefix) {
		// try to find "component-validator"
		{
			String validatorExpression = getParameter(prefix + ".component-validator");
			if (validatorExpression != null) {
				return ContainerObjectValidators.forComponentExpression(validatorExpression);
			}
		}
		// component should be list of types
		{
			String componentString = getComponentString(prefix);
			Assert.isNotNull(componentString, "No 'component' validator.");
			String[] componentTypes = StringUtils.split(componentString);
			return ContainerObjectValidators.forList(componentTypes);
		}
	}

	private String getComponentString(String prefix) {
		String componentString = getParameter(prefix + ".component");
		// if no "component", try to get from "defaultComponent"
		if (componentString == null) {
			componentString = getParameter("simpleContainer.defaultComponent");
		}
		// return what we have
		return componentString;
	}

	private Association getAssociation(String prefix) {
		String associationString = getParameter(prefix + ".x-association");
		if (associationString == null) {
			return Associations.direct();
		} else if (associationString.startsWith("inter ")) {
			associationString = StringUtils.removeStart(associationString, "inter ");
			// extract tag
			String tag = StringUtils.substringBefore(associationString, " ");
			associationString = StringUtils.substringAfter(associationString, " ");
			// extract attributes
			Map<String, String> attributes = parseAttributes(associationString);
			return Associations.intermediate(tag, attributes);
		} else {
			Assert.isTrue(associationString.startsWith("property "));
			String property = StringUtils.removeStart(associationString, "property ");
			return Associations.property(property);
		}
	}

	/**
	 * Parses attributes in format: attrA='a' attrB='b b'
	 */
	private static Map<String, String> parseAttributes(String s) {
		Map<String, String> attributes = new HashMap<>();
		while (s.length() != 0) {
			s = s.trim();
			// extract name/value
			int attrNameEnd = s.indexOf("='");
			int attrValueEnd = s.indexOf("'", attrNameEnd + 2);
			String attrName = s.substring(0, attrNameEnd);
			String attrValue = s.substring(attrNameEnd + 2, attrValueEnd);
			attributes.put(attrName, attrValue);
			// next attribute
			s = s.substring(attrValueEnd + 1);
		}
		return attributes;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private String getParameter(String name) {
		return XmlObjectUtils.getParameter(m_object, name);
	}
}
