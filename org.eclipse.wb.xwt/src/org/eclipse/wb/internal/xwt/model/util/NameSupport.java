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
package org.eclipse.wb.internal.xwt.model.util;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectInfoPresentationDecorateText;
import org.eclipse.wb.internal.core.model.ObjectInfoVisitor;
import org.eclipse.wb.internal.core.model.variable.NamesManager;
import org.eclipse.wb.internal.core.model.variable.NamesManager.ComponentNameDescription;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.utils.NamespacesHelper;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;

import org.apache.commons.lang.StringUtils;

import java.util.Set;

/**
 * Maintains special "Name" property for XWT.
 *
 * @author mitin_aa
 * @coverage XWT.model
 */
public final class NameSupport {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private NameSupport() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Name
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Sometimes widget has no text, so it is hard to identify in components tree. But if it has name,
	 * would be nice to show it.
	 */
	public static void decoratePresentationWithName(XmlObjectInfo root) {
		root.addBroadcastListener(new ObjectInfoPresentationDecorateText() {
			@Override
			public void invoke(ObjectInfo object, String[] text) throws Exception {
				if (object instanceof XmlObjectInfo xObject) {
					String name = getName(xObject);
					if (name != null) {
						text[0] = text[0] + " - " + name;
					}
				}
			}
		});
	}

	/**
	 * @return the existing name of the widget, or <code>null</code>.
	 */
	public static String getName(XmlObjectInfo object) {
		String nameAttribute = getNamePrefix(object) + "Name";
		return object.getAttribute(nameAttribute);
	}

	/**
	 * @return the existing or new name of the widget, can not be <code>null</code>. Actually, this is
	 *         the value of 'x:Name' attribute of the widget document element. If the widget has no
	 *         name set then new unique name will be generated and returned.
	 */
	public static String ensureName(XmlObjectInfo object) {
		String name = getName(object);
		if (name == null) {
			name = generateName(object);
			setName(object, name);
		}
		return name;
	}

	/**
	 * Sets new name of the widget.
	 */
	public static void setName(XmlObjectInfo object, String name) {
		String nameAttribute = getNamePrefix(object) + "Name";
		object.setAttribute(nameAttribute, name);
	}

	/**
	 * @return the namespace prefix for "Name" attribute.
	 */
	private static String getNamePrefix(XmlObjectInfo object) {
		String namespaceName =
				NamespacesHelper.ensureName(object.getElement(), "http://www.eclipse.org/xwt", "x");
		return namespaceName + ":";
	}

	/**
	 * Generates and returns unique name basing on settings and info in *.wbp-component.xml
	 * description.
	 */
	private static String generateName(XmlObjectInfo object) {
		String baseName = getBaseName(object);
		final Set<String> existingNames = getExistingNames(object);
		String uniqueName = CodeUtils.generateUniqueName(baseName, new Predicate<String>() {
			@Override
			public boolean apply(String name) {
				return !existingNames.contains(name);
			}
		});
		return uniqueName;
	}

	/**
	 * Traverses the entire hierarchy and gathers set of existing names.
	 */
	private static Set<String> getExistingNames(XmlObjectInfo object) {
		final Set<String> resultSet = Sets.newTreeSet();
		object.getRootXML().accept(new ObjectInfoVisitor() {
			@Override
			public void endVisit(ObjectInfo object) throws Exception {
				if (object instanceof XmlObjectInfo xmlObject) {
					if (XmlObjectUtils.isImplicit(xmlObject)) {
						return;
					}
					String name = getName(xmlObject);
					if (name != null) {
						resultSet.add(name);
					}
				}
			}
		});
		return resultSet;
	}

	/**
	 * @return the base variable name for given {@link XmlObjectInfo}.
	 */
	private static String getBaseName(XmlObjectInfo object) {
		ComponentDescription description = object.getDescription();
		// check type specific information
		{
			ComponentNameDescription nameDescription =
					NamesManager.getNameDescription(
							description.getToolkit(),
							description.getComponentClass().getName());
			if (nameDescription != null) {
				return nameDescription.getName();
			}
		}
		// check component parameter
		{
			String name = XmlObjectUtils.getParameter(object, NamesManager.NAME_PARAMETER);
			if (!StringUtils.isEmpty(name)) {
				return name;
			}
		}
		// use default name
		return NamesManager.getDefaultName(description.getComponentClass().getName());
	}
}
