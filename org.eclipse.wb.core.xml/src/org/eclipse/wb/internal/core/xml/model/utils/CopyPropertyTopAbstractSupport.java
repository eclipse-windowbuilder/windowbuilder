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
package org.eclipse.wb.internal.core.xml.model.utils;

import com.google.common.collect.MapMaker;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectAddProperties;
import org.eclipse.wb.internal.core.xml.model.property.GenericPropertyImpl;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * This helper allows to create top-level {@link Property} as copy of other {@link Property}
 * (usually part of complex property).
 *
 * @author scheglov_ke
 * @coverage XML.model.util
 */
public abstract class CopyPropertyTopAbstractSupport {
	private final String m_prefix;
	private final Map<XmlObjectInfo, List<CopyProcessor>> processorsMap =
			new MapMaker().weakKeys().makeMap();

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	protected CopyPropertyTopAbstractSupport(ObjectInfo root, String prefix) {
		m_prefix = prefix;
		root.addBroadcastListener(new XmlObjectAddProperties() {
			@Override
			public void invoke(XmlObjectInfo object, List<Property> properties) throws Exception {
				// Prepare copy processors.
				List<CopyProcessor> processors = processorsMap.get(object);
				if (processors == null) {
					processors = createProcessors(object);
					processorsMap.put(object, processors);
				}
				// Run copy processors.
				for (CopyProcessor processor : processors) {
					processor.addCopy(object, properties);
				}
			}
		});
	}

	private List<CopyProcessor> createProcessors(XmlObjectInfo object) {
		List<CopyProcessor> processors = new ArrayList<>();
		Predicate<XmlObjectInfo> targetPredicate = createTargetPredicate(object);
		for (String parameter : object.getDescription().getParameters().keySet()) {
			String sourcePath = null;
			String copyTitle = null;
			PropertyCategory category = PropertyCategory.NORMAL;
			if (parameter.startsWith(m_prefix)) {
				String[] parts = StringUtils.split(parameter);
				for (String part : parts) {
					if (part.startsWith("from=")) {
						sourcePath = StringUtils.removeStart(part, "from=");
					}
					if (part.startsWith("to=")) {
						copyTitle = StringUtils.removeStart(part, "to=");
					}
					if (part.startsWith("category=")) {
						String categoryText = StringUtils.removeStart(part, "category=");
						category = PropertyCategory.get(categoryText, category);
					}
				}
				// validate
				if (sourcePath == null || copyTitle == null) {
					continue;
				}
				// OK, create copy processor
				processors.add(new CopyProcessor(object, targetPredicate, sourcePath, copyTitle, category));
			}
		}
		return processors;
	}

	/**
	 * @param object
	 *          the {@link XmlObjectInfo} passed into {@link #install(XmlObjectInfo, String)}.
	 *
	 * @return the {@link Predicate} to check if property of some {@link XmlObjectInfo} should be
	 *         copied to its top properties.
	 */
	protected abstract Predicate<XmlObjectInfo> createTargetPredicate(XmlObjectInfo object);

	////////////////////////////////////////////////////////////////////////////
	//
	// Copy processor
	//
	////////////////////////////////////////////////////////////////////////////
	static class CopyProcessor {
		private final Predicate<XmlObjectInfo> m_targetPredicate;
		private final String m_sourcePath;
		private final String m_copyTitle;
		private final PropertyCategory m_category;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		CopyProcessor(XmlObjectInfo hierarchyObject,
				Predicate<XmlObjectInfo> targetPredicate,
				String sourcePath,
				String copyTitle,
				PropertyCategory category) {
			m_targetPredicate = targetPredicate;
			m_sourcePath = sourcePath;
			m_copyTitle = copyTitle;
			m_category = category;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Implementation
		//
		////////////////////////////////////////////////////////////////////////////
		private Property m_oldSource;
		private Property m_oldCopy;

		public void addCopy(XmlObjectInfo object, List<Property> properties) throws Exception {
			if (m_targetPredicate.test(object)) {
				Property copy = getCopy(properties);
				if (copy != null) {
					properties.add(copy);
				}
			}
		}

		private Property getCopy(List<Property> properties) throws Exception {
			Property source = PropertyUtils.getByPath(properties, m_sourcePath);
			if (m_oldSource != source) {
				m_oldSource = source;
				m_oldCopy = createCopy(source);
			}
			return m_oldCopy;
		}

		private Property createCopy(Property source) {
			if (source instanceof GenericPropertyImpl genericProperty) {
				Property copy = new GenericPropertyImpl(genericProperty, m_copyTitle);
				copy.setCategory(m_category);
				return copy;
			}
			return null;
		}
	}
}
