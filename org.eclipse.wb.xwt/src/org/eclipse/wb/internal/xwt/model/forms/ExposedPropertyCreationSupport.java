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
package org.eclipse.wb.internal.xwt.model.forms;

import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectSetObjectAfter;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.creation.ElementCreationSupport;
import org.eclipse.wb.internal.core.xml.model.creation.IImplicitCreationSupport;
import org.eclipse.wb.internal.xwt.parser.XwtParserBindToElement;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Method;

/**
 * {@link CreationSupport} for {@link XmlObjectInfo} accessible using property.
 *
 * @author scheglov_ke
 * @coverage XWT.model.forms
 */
public class ExposedPropertyCreationSupport extends CreationSupport
implements
IImplicitCreationSupport {
	private final XmlObjectInfo m_host;
	private final String m_property;
	private final Method m_method;
	private DocumentElement m_element;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ExposedPropertyCreationSupport(XmlObjectInfo host, String property) {
		m_host = host;
		m_property = property;
		// prepare method
		{
			String signature = "get" + StringUtils.capitalize(property) + "()";
			m_method =
					ReflectionUtils.getMethodBySignature(
							m_host.getDescription().getComponentClass(),
							signature);
			Assert.isNotNull2(m_method, "No method {0} in {1}", signature, m_host);
		}
		// get object
		host.addBroadcastListener(new XmlObjectSetObjectAfter() {
			@Override
			public void invoke(XmlObjectInfo target, Object o) throws Exception {
				if (target == m_host) {
					Object object = m_method.invoke(o);
					m_object.setObject(object);
				}
			}
		});
		// prepare "element"
		{
			DocumentElement viewerElement = m_host.getCreationSupport().getElement();
			if (viewerElement != null) {
				String controlTag = viewerElement.getTag() + "." + m_property;
				m_element = viewerElement.getChild(controlTag, true);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		if (m_element == null) {
			return "<" + getTitle() + "?>";
		}
		return ElementCreationSupport.getElementString(m_element);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getTitle() {
		return m_host.getCreationSupport().getTitle() + "." + m_property;
	}

	@Override
	public void setObject(XmlObjectInfo object) throws Exception {
		super.setObject(object);
		if (m_element != null) {
			m_host.getBroadcast(XwtParserBindToElement.class).invoke(m_object, m_element);
		}
	}

	@Override
	public DocumentElement getElement() {
		if (m_element == null) {
			ExecutionUtils.runRethrow(new RunnableEx() {
				@Override
				public void run() throws Exception {
					DocumentElement viewerElement = m_host.getCreationSupport().getElement();
					// create "control" element
					m_element = new DocumentElement();
					m_element.setTag(viewerElement.getTag() + "." + m_property);
					// add it
					viewerElement.addChild(m_element, 0);
				}
			});
		}
		return m_element;
	}
}
