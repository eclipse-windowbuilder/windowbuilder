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
package org.eclipse.wb.internal.rcp.databinding.xwt.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.databinding.ui.editor.IPageListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassConfiguration;
import org.eclipse.wb.internal.core.utils.xml.AbstractDocumentObject;
import org.eclipse.wb.internal.core.utils.xml.DocumentAttribute;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.xwt.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.xwt.Messages;
import org.eclipse.wb.internal.rcp.databinding.xwt.model.beans.BeansObserveTypeContainer;
import org.eclipse.wb.internal.rcp.databinding.xwt.ui.contentproviders.ConverterUiContentProvider;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.List;
import java.util.Set;

/**
 *
 * @author lobas_av
 *
 */
public class ConverterInfo {
	private boolean m_element;
	private boolean m_staticResurce;
	private String m_resourceReference;
	private String m_className;
	private String m_namespace;
	private boolean m_update;

	////////////////////////////////////////////////////////////////////////////
	//
	//
	//
	////////////////////////////////////////////////////////////////////////////
	public void parse(DatabindingsProvider provider, DocumentElement element) throws Exception {
		if (element != null) {
			List<DocumentElement> children = element.getChildren();
			if (children.size() == 1) {
				parseClass(provider, element.getRoot(), children.get(0).getTag());
			} else {
				provider.addWarning(
						MessageFormat.format(Messages.ConverterInfo_converterNotFound, element),
						new Throwable());
			}
			m_element = true;
		}
	}

	public void parse(DatabindingsProvider provider, DocumentElement root, String attribute)
			throws Exception {
		m_staticResurce = attribute != null && attribute.startsWith("{StaticResource");
		//
		if (m_staticResurce) {
			m_resourceReference = StringUtils.substringBetween(attribute, "{StaticResource", "}").trim();
			//
			BeansObserveTypeContainer beanContainer =
					(BeansObserveTypeContainer) provider.getContainer(ObserveType.BEANS);
			if (beanContainer.resolve(m_resourceReference) == null) {
				m_staticResurce = false;
				m_resourceReference = null;
				provider.addWarning(
						MessageFormat.format(Messages.ConverterInfo_converterNotFound, attribute),
						new Throwable());
			}
		} else {
			parseClass(provider, root, attribute);
		}
	}

	private void parseClass(DatabindingsProvider provider, DocumentElement root, String value)
			throws Exception {
		m_namespace = StringUtils.substringBefore(value, ":");
		String packageValue = root.getAttribute("xmlns:" + m_namespace);
		//
		if (packageValue == null) {
			m_namespace = null;
			provider.addWarning(
					MessageFormat.format(Messages.ConverterInfo_converterNotFound, value),
					new Throwable());
		} else {
			m_className =
					StringUtils.substringAfter(packageValue, "clr-namespace:")
					+ "."
					+ StringUtils.substringAfter(value, ":");
			try {
				ClassLoader classLoader = provider.getXmlObjectRoot().getContext().getClassLoader();
				classLoader.loadClass(m_className);
			} catch (ClassNotFoundException e) {
				m_className = null;
				m_namespace = null;
				provider.addWarning(
						MessageFormat.format(Messages.ConverterInfo_converterClassNotFound, m_className),
						new Throwable());
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	//
	//
	////////////////////////////////////////////////////////////////////////////
	public String getValue() {
		return m_staticResurce ? m_resourceReference : m_className;
	}

	public void setValue(String value, boolean staticResurce) throws Exception {
		m_update = true;
		//
		if (value == null) {
			if (m_resourceReference == null && m_className == null) {
				return;
			}
			//
			m_staticResurce = false;
			m_resourceReference = null;
			m_className = null;
			m_namespace = null;
		} else {
			if (m_staticResurce == staticResurce && value.equals(getValue())) {
				return;
			}
			//
			m_staticResurce = staticResurce;
			//
			if (m_staticResurce) {
				m_resourceReference = value;
				m_className = null;
				m_namespace = null;
			} else {
				if (m_namespace != null) {
					String oldPackageName = ClassUtils.getPackageName(m_className);
					String newPackageName = ClassUtils.getPackageName(value);
					//
					if (!oldPackageName.equals(newPackageName)) {
						m_namespace = null;
					}
				}
				//
				m_resourceReference = null;
				m_className = value;
			}
		}
	}

	public void appendValue(StringBuffer value) throws Exception {
		if (m_update && getValue() != null) {
			value.append(", converter=");
			if (m_staticResurce) {
				value.append("{StaticResource " + m_resourceReference + "}");
			} else {
				value.append(StringUtils.defaultString(m_namespace)
						+ ":"
						+ ClassUtils.getShortClassName(m_className));
			}
		}
	}

	public boolean update() throws Exception {
		return m_update;
	}

	public void applyChanges(AbstractDocumentObject object) throws Exception {
		if (m_update) {
			if (object instanceof DocumentAttribute) {
				if (m_className != null && m_namespace == null) {
					DocumentAttribute attribute = (DocumentAttribute) object;
					createNamespace(attribute.getEnclosingElement().getRoot());
					//
					String value = attribute.getValue();
					int index = value.indexOf("converter=") + 10;
					attribute.setValue(value.substring(0, index) + m_namespace + value.substring(index));
				}
			} else {
				DocumentElement bindingElement = (DocumentElement) object;
				DocumentElement converterElement = bindingElement.getChild("Binding.converter", true);
				if (getValue() == null) {
					if (converterElement != null) {
						converterElement.remove();
					}
				} else {
					if (m_namespace == null) {
						createNamespace(bindingElement.getRoot());
					}
					String value = m_namespace + ":" + ClassUtils.getShortClassName(m_className);
					//
					if (converterElement == null) {
						converterElement = new DocumentElement("Binding.converter");
						converterElement.addChild(new DocumentElement(value));
						bindingElement.addChild(converterElement);
					} else {
						List<DocumentElement> children = converterElement.getChildren();
						if (children.size() == 1) {
							children.get(0).setTag(value);
						} else {
							converterElement.removeChildren();
							converterElement.addChild(new DocumentElement(value));
						}
					}
				}
			}
			m_update = false;
		}
	}

	private void createNamespace(DocumentElement rootElement) throws Exception {
		Set<String> domains = Sets.newHashSet();
		String domainName = "clr-namespace:" + ClassUtils.getPackageName(m_className);
		for (DocumentAttribute attribute : rootElement.getDocumentAttributes()) {
			if (attribute.getName().startsWith("xmlns:")
					&& attribute.getValue().startsWith("clr-namespace:")) {
				String domainNameSpace = attribute.getName().substring(6);
				if (attribute.getValue().equals(domainName)) {
					m_namespace = domainNameSpace;
					return;
				}
				domains.add(domainNameSpace);
			}
		}
		//
		int index = 0;
		while (true) {
			String domainNameSpace = "c" + Integer.toString(index);
			if (domains.contains(domainNameSpace)) {
				index++;
			} else {
				m_namespace = domainNameSpace;
				rootElement.setAttribute("xmlns:" + domainNameSpace, domainName);
				return;
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	//
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Create {@link IUiContentProvider} content providers for edit this model.
	 */
	public void createContentProviders(List<IUiContentProvider> providers,
			IPageListener listener,
			DatabindingsProvider provider) throws Exception {
		ChooseClassConfiguration configuration = new ChooseClassConfiguration();
		configuration.setDialogFieldLabel(Messages.ConverterInfo_providerTitle);
		configuration.setValueScope("org.eclipse.xwt.IValueConverter");
		configuration.setClearValue("N/S");
		configuration.setBaseClassName("org.eclipse.xwt.IValueConverter");
		configuration.setConstructorParameters(ArrayUtils.EMPTY_CLASS_ARRAY);
		configuration.setEmptyClassErrorMessage(Messages.ConverterInfo_errorMessage);
		configuration.setErrorMessagePrefix(Messages.ConverterInfo_errorMessagePrefix);
		//
		if (!m_element) {
			ClassLoader classLoader = provider.getXmlObjectRoot().getContext().getClassLoader();
			Class<?> ConverterClass = classLoader.loadClass("org.eclipse.xwt.IValueConverter");
			List<String> defaultValues = Lists.newArrayList();
			BeansObserveTypeContainer beanContainer =
					(BeansObserveTypeContainer) provider.getContainer(ObserveType.BEANS);
			for (BeanBindableInfo bindable : beanContainer.getObservables0()) {
				if (ConverterClass.isAssignableFrom(bindable.getObjectType())) {
					defaultValues.add(bindable.getReference());
				}
			}
			//
			if (!defaultValues.isEmpty()) {
				configuration.setDefaultValues(defaultValues.toArray(new String[defaultValues.size()]));
			}
		}
		//
		providers.add(new ConverterUiContentProvider(provider, configuration, this));
	}
}