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

import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectResolveTag;
import org.eclipse.wb.internal.core.xml.model.utils.NamespacesHelper;
import org.eclipse.wb.internal.xwt.parser.XwtDescriptionProcessor;
import org.eclipse.wb.internal.xwt.parser.XwtParser;

import org.apache.commons.lang.StringUtils;

/**
 * {@link XmlObjectResolveTag} for XWT.
 *
 * @author scheglov_ke
 * @coverage XWT.model
 */
public final class XwtTagResolver extends NamespacesHelper {
	private final EditorContext m_context;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public XwtTagResolver(XmlObjectInfo rootObject) {
		super(rootObject.getCreationSupport().getElement());
		m_context = rootObject.getContext();
		rootObject.addBroadcastListener(new XmlObjectResolveTag() {
			@Override
			public void invoke(XmlObjectInfo object, Class<?> clazz, String[] namespace, String[] tag)
					throws Exception {
				invoke0(object, clazz, namespace, tag);
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// XMLObject_resolveTag
	//
	////////////////////////////////////////////////////////////////////////////
	private void invoke0(XmlObjectInfo object, Class<?> clazz, String[] namespace, String[] tag)
			throws Exception {
		if (XwtDescriptionProcessor.isXWT(object)) {
			String className = clazz.getName();
			namespace[0] = getNamespace(className);
			tag[0] = StringUtils.substringAfterLast(className, ".");
		}
	}

	/**
	 * @return the existing namespace (may be empty {@link String}), or <code>null</code> if new
	 *         namespace should be added.
	 */
	private String getNamespace(String className) {
		prepareNamespaces();
		String packageName = StringUtils.substringBeforeLast(className, ".");
		boolean isNotImportedForms =
				className.equals("org.eclipse.ui.forms.widgets.ColumnLayout")
				|| className.equals("org.eclipse.ui.forms.widgets.ColumnLayoutData")
				|| className.equals("org.eclipse.ui.forms.widgets.TableWrapLayout")
				|| className.equals("org.eclipse.ui.forms.widgets.TableWrapData");
		boolean isStandardCustom = packageName.equals("org.eclipse.swt.custom");
		boolean isForms =
				XwtParser.hasForms(m_context)
				&& packageName.equals("org.eclipse.ui.forms.widgets")
				&& !isNotImportedForms;
		// standard XWT
		if (packageName.equals("org.eclipse.swt.widgets")
				|| isStandardCustom
				|| packageName.equals("org.eclipse.swt.layout")
				|| packageName.equals("org.eclipse.jface.viewers")
				|| isForms) {
			String name = m_nameForURI.get("http://www.eclipse.org/xwt/presentation");
			// if standard XWT namespace declared
			if (name != null) {
				return name;
			}
			// if no default namespace, then standard XWT is implied
			if (!m_names.contains("")) {
				return "";
			}
		}
		// existing for this package
		String packageURI = "clr-namespace:" + packageName;
		return ensureName(packageURI, "p");
	}
}
