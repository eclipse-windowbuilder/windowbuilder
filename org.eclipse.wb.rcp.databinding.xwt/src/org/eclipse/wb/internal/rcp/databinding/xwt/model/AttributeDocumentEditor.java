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
package org.eclipse.wb.internal.rcp.databinding.xwt.model;

import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.xml.DocumentAttribute;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.xwt.model.beans.XmlElementBeanBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.xwt.model.widgets.WidgetBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.xwt.model.widgets.XmlObjectReferenceProvider;

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author lobas_av
 *
 */
public class AttributeDocumentEditor implements IDocumentEditor {
	private final BindingInfo m_binding;
	private DocumentAttribute m_attribute;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public AttributeDocumentEditor(BindingInfo binding) {
		m_binding = binding;
	}

	public AttributeDocumentEditor(BindingInfo binding, DocumentAttribute attribute) {
		m_binding = binding;
		m_attribute = attribute;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	//
	//
	////////////////////////////////////////////////////////////////////////////
	private String getValue() throws Exception {
		StringBuffer value = new StringBuffer("{Binding ");
		//
		if (m_binding.getModel() instanceof WidgetBindableInfo) {
			WidgetBindableInfo model = (WidgetBindableInfo) m_binding.getModel();
			String modelProperty = m_binding.getModelProperty().getPresentation().getText();
			value.append(" elementName=" + model.getReference() + ", Path=" + modelProperty);
		} else {
			BindableInfo modelProperty = (BindableInfo) m_binding.getModelProperty();
			String property = StringUtils.remove(modelProperty.getReference(), '"');
			//
			XmlElementBeanBindableInfo model = (XmlElementBeanBindableInfo) m_binding.getModel();
			if (!model.isDataContext()) {
				property = "{StaticResource " + property + "}";
			}
			//
			value.append(" Path=" + property);
		}
		//
		if (m_binding.getMode() != 0) {
			value.append(", Mode=" + BindingInfo.MODES[m_binding.getMode()]);
		}
		if (m_binding.getTriger() != 0) {
			value.append(", updateSourceTrigger=" + BindingInfo.TRIGGERS[m_binding.getTriger()]);
		}
		//
		m_binding.getConverter().appendValue(value);
		m_binding.getValidator().appendValue(value);
		//
		value.append("}");
		//
		return value.toString();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IDocumentEditor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void add() throws Exception {
		m_binding.modify(new RunnableEx() {
			@Override
			public void run() throws Exception {
				if (m_binding.getModel() instanceof WidgetBindableInfo) {
					WidgetBindableInfo model = (WidgetBindableInfo) m_binding.getModel();
					XmlObjectReferenceProvider.generateName(model.getXMLObjectInfo());
				}
				//
				String property = m_binding.getTargetProperty().getPresentation().getText();
				DocumentElement element = m_binding.getTargetElement();
				m_attribute = element.setAttribute(property, getValue());
				m_binding.getConverter().applyChanges(m_attribute);
				m_binding.getValidator().applyChanges(m_attribute);
			}
		});
	}

	@Override
	public void delete() throws Exception {
		m_binding.modify(new RunnableEx() {
			@Override
			public void run() throws Exception {
				m_binding.getTargetElement().removeDocumentAttribute(m_attribute);
			}
		});
	}

	@Override
	public void update() throws Exception {
		final String value = getValue();
		if (!value.equals(m_attribute.getValue())) {
			m_binding.modify(new RunnableEx() {
				@Override
				public void run() throws Exception {
					m_attribute.setValue(value);
					m_binding.getConverter().applyChanges(m_attribute);
					m_binding.getValidator().applyChanges(m_attribute);
				}
			});
		}
	}

	@Override
	public int getDefinitionOffset() {
		return m_attribute == null ? -1 : m_attribute.getValueOffset();
	}
}