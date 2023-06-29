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

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.xml.DocumentAttribute;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.xwt.model.beans.XmlElementBeanBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.xwt.model.widgets.WidgetBindableInfo;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 *
 * @author lobas_av
 *
 */
public class ElementDocumentEditor implements IDocumentEditor {
	private final BindingInfo m_binding;
	private final DocumentElement m_element;
	private final List<AttributeEditor> m_editors = Lists.newArrayList();

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ElementDocumentEditor(BindingInfo binding, DocumentElement element) {
		m_binding = binding;
		m_element = element;
		//
		m_editors.add(new AttributeEditor("ElementName") {
			@Override
			protected String getValue() throws Exception {
				if (m_binding.getModel() instanceof WidgetBindableInfo) {
					WidgetBindableInfo model = (WidgetBindableInfo) m_binding.getModel();
					return model.getReference();
				}
				return null;
			}
		});
		m_editors.add(new AttributeEditor("Path") {
			@Override
			protected String getValue() throws Exception {
				if (m_binding.getModel() instanceof WidgetBindableInfo) {
					return m_binding.getModelProperty().getPresentation().getText();
				}
				//
				BindableInfo modelProperty = (BindableInfo) m_binding.getModelProperty();
				String property = StringUtils.remove(modelProperty.getReference(), '"');
				//
				XmlElementBeanBindableInfo model = (XmlElementBeanBindableInfo) m_binding.getModel();
				if (!model.isDataContext()) {
					property = "{StaticResource " + property + "}";
				}
				//
				return property;
			}
		});
		m_editors.add(new AttributeEditor("Mode") {
			@Override
			protected String getValue() throws Exception {
				return m_binding.getMode() == 0 ? null : BindingInfo.MODES[m_binding.getMode()];
			}
		});
		m_editors.add(new AttributeEditor("UpdateSourceTrigger") {
			@Override
			protected String getValue() throws Exception {
				return m_binding.getTriger() == 0 ? null : BindingInfo.TRIGGERS[m_binding.getTriger()];
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IDocumentEditor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void add() throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete() throws Exception {
		m_binding.modify(new RunnableEx() {
			@Override
			public void run() throws Exception {
				m_element.remove();
			}
		});
	}

	@Override
	public void update() throws Exception {
		boolean updates = false;
		for (AttributeEditor editor : m_editors) {
			updates |= editor.update();
		}
		//
		updates |= m_binding.getConverter().update();
		updates |= m_binding.getValidator().update();
		//
		if (updates) {
			m_binding.modify(new RunnableEx() {
				@Override
				public void run() throws Exception {
					for (AttributeEditor editor : m_editors) {
						editor.save();
					}
					m_binding.getConverter().applyChanges(m_element);
					m_binding.getValidator().applyChanges(m_element);
				}
			});
		}
	}

	@Override
	public int getDefinitionOffset() {
		return m_element.getOffset() + m_element.getTag().length() + 1;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	//
	//
	////////////////////////////////////////////////////////////////////////////
	private abstract class AttributeEditor {
		static final int ADD = 1;
		static final int REMOVE = 2;
		static final int UPDATE = 3;
		//
		private final String m_name;
		private DocumentAttribute m_attribute;
		private boolean m_update;
		private String m_value;
		private int m_command;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public AttributeEditor(String name) {
			m_name = name;
			for (DocumentAttribute attribute : m_element.getDocumentAttributes()) {
				if (name.equalsIgnoreCase(attribute.getName())) {
					m_attribute = attribute;
					break;
				}
			}
		}

		////////////////////////////////////////////////////////////////////////////
		//
		//
		//
		////////////////////////////////////////////////////////////////////////////
		public boolean update() throws Exception {
			m_value = getValue();
			if (m_attribute == null) {
				if (m_value == null) {
					clear();
				} else {
					m_update = true;
					m_command = ADD;
				}
			} else {
				if (m_value == null) {
					m_update = true;
					m_command = REMOVE;
				} else if (m_value.equals(m_attribute.getValue())) {
					clear();
				} else {
					m_update = true;
					m_command = UPDATE;
				}
			}
			return m_update;
		}

		public void save() throws Exception {
			if (m_update) {
				switch (m_command) {
				case ADD :
					m_attribute = m_element.setAttribute(m_name, m_value);
					break;
				case REMOVE :
					m_element.removeDocumentAttribute(m_attribute);
					m_attribute = null;
					break;
				case UPDATE :
					m_attribute.setValue(m_value);
					break;
				}
				//
				clear();
			}
		}

		private void clear() {
			m_update = false;
			m_command = 0;
			m_value = null;
		}

		protected abstract String getValue() throws Exception;
	}
}