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
package org.eclipse.wb.internal.rcp.databinding.xwt.model.widgets;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.ISynchronizeProcessor;
import org.eclipse.wb.internal.core.databinding.model.SynchronizeManager;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.PropertiesSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetPropertyBindableInfo;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author lobas_av
 *
 */
public class WidgetBindableInfo extends BindableInfo {
	private final XmlObjectInfo m_xmlObjectInfo;
	private final WidgetBindableInfo m_parent;
	private final List<WidgetBindableInfo> m_children = Lists.newArrayList();
	private final List<WidgetPropertyBindableInfo> m_properties;
	private final XmlObjectObservePresentation m_presentation;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public WidgetBindableInfo(final XmlObjectInfo xmlObjectInfo, WidgetBindableInfo parent)
			throws Exception {
		super(xmlObjectInfo.getDescription().getComponentClass(),
				new XmlObjectReferenceProvider(xmlObjectInfo));
		m_xmlObjectInfo = xmlObjectInfo;
		m_parent = parent;
		m_presentation = new XmlObjectObservePresentation(xmlObjectInfo);
		// prepare children
		List<XmlObjectInfo> childrenInfos =
				SynchronizeManager.getChildren(xmlObjectInfo, XmlObjectInfo.class);
		for (XmlObjectInfo childInfo : childrenInfos) {
			m_children.add(new WidgetBindableInfo(childInfo, this));
		}
		// prepare properties
		m_properties = PropertiesSupport.getProperties(getClassLoader(), getObjectType());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public XmlObjectInfo getXMLObjectInfo() {
		return m_xmlObjectInfo;
	}

	public ClassLoader getClassLoader() {
		return m_xmlObjectInfo.getContext().getClassLoader();
	}

	public WidgetBindableInfo resolve(XmlObjectInfo xmlObjectInfo) {
		if (m_xmlObjectInfo == xmlObjectInfo) {
			return this;
		}
		for (WidgetBindableInfo child : m_children) {
			WidgetBindableInfo result = child.resolve(xmlObjectInfo);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	public WidgetBindableInfo resolve(DocumentElement element) {
		if (m_xmlObjectInfo.getElement() == element) {
			return this;
		}
		for (WidgetBindableInfo child : m_children) {
			WidgetBindableInfo result = child.resolve(element);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	public WidgetPropertyBindableInfo resolvePropertyByText(String text) throws Exception {
		for (WidgetPropertyBindableInfo property : m_properties) {
			if (text.equalsIgnoreCase(property.getPresentation().getText())) {
				return property;
			}
		}
		return null;
	}

	public void update() throws Exception {
		// prepare new objectInfo's
		List<XmlObjectInfo> objectInfos =
				SynchronizeManager.getChildren(m_xmlObjectInfo, XmlObjectInfo.class);
		//
		SynchronizeManager.synchronizeObjects(
				m_children,
				objectInfos,
				new ISynchronizeProcessor<XmlObjectInfo, WidgetBindableInfo>() {
					@Override
					public boolean handleObject(WidgetBindableInfo object) {
						return true;
					}

					@Override
					public XmlObjectInfo getKeyObject(WidgetBindableInfo widget) {
						return widget.m_xmlObjectInfo;
					}

					@Override
					public boolean equals(XmlObjectInfo key0, XmlObjectInfo key1) {
						return key0 == key1;
					}

					@Override
					public WidgetBindableInfo findObject(Map<XmlObjectInfo, WidgetBindableInfo> keyObjectToObject,
							XmlObjectInfo key) throws Exception {
						return null;
					}

					@Override
					public WidgetBindableInfo createObject(XmlObjectInfo objectInfo) throws Exception {
						return new WidgetBindableInfo(objectInfo, WidgetBindableInfo.this);
					}

					@Override
					public void update(WidgetBindableInfo widget) throws Exception {
						widget.update();
					}
				});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// BindableInfo
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected List<BindableInfo> getChildren() {
		return CoreUtils.cast(m_children);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Hierarchy
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public IObserveInfo getParent() {
		return m_parent;
	}

	@Override
	public List<IObserveInfo> getChildren(ChildrenContext context) {
		if (context == ChildrenContext.ChildrenForMasterTable) {
			return CoreUtils.cast(m_children);
		}
		if (context == ChildrenContext.ChildrenForPropertiesTable) {
			return CoreUtils.cast(m_properties);
		}
		return Collections.emptyList();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public IObservePresentation getPresentation() {
		return m_presentation;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ObserveType
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public ObserveType getType() {
		return ObserveType.WIDGETS;
	}
}