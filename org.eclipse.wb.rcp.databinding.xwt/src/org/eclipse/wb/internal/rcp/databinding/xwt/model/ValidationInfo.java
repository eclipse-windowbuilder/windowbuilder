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

import org.eclipse.wb.internal.core.databinding.ui.editor.IPageListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.utils.xml.AbstractDocumentObject;
import org.eclipse.wb.internal.core.utils.xml.DocumentAttribute;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.rcp.databinding.xwt.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.xwt.Messages;
import org.eclipse.wb.internal.rcp.databinding.xwt.ui.contentproviders.ValidationUiContentProvider;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author lobas_av
 *
 */
public class ValidationInfo {
	private List<String> m_classNames = new ArrayList<>();
	private Map<String, String> m_namespaceToPackage = new HashMap<>();
	private final Map<String, String> m_packageToNamespace = new HashMap<>();
	private boolean m_update;

	////////////////////////////////////////////////////////////////////////////
	//
	//
	//
	////////////////////////////////////////////////////////////////////////////
	public void parse(DatabindingsProvider provider,
			DocumentElement validationRules,
			DocumentElement validationRule) throws Exception {
		if (validationRules != null) {
			parseNamespaces(validationRules.getRoot());
			for (DocumentElement child : validationRules.getChildren()) {
				parseClass(provider, child.getTag());
			}
		} else if (validationRule != null) {
			List<DocumentElement> children = validationRule.getChildren();
			if (!children.isEmpty()) {
				parseNamespaces(validationRule.getRoot());
				parseClass(provider, children.get(0).getTag());
			}
		}
		m_namespaceToPackage = null;
	}

	public void parse(DatabindingsProvider provider,
			DocumentElement root,
			String validationRules,
			String validationRule) throws Exception {
		if (validationRules != null) {
			parseNamespaces(root);
			String[] rules =
					StringUtils.split(StringUtils.substringBetween(validationRules, "{", "}"), ',');
			//
			for (String rule : rules) {
				parseClass(provider, rule.trim());
			}
		} else if (validationRule != null) {
			parseNamespaces(root);
			parseClass(provider, validationRule);
		} else {
			parseNamespaces(root);
		}
		m_namespaceToPackage = null;
	}

	private void parseNamespaces(DocumentElement root) {
		for (DocumentAttribute attribute : root.getDocumentAttributes()) {
			String name = attribute.getName();
			if (name.startsWith("xmlns:")) {
				String value = attribute.getValue();
				if (value.startsWith("clr-namespace:")) {
					String namespace = name.substring(6);
					String packageValue = value.substring(14);
					//
					m_namespaceToPackage.put(namespace, packageValue);
					m_packageToNamespace.put(packageValue, namespace);
				}
			}
		}
	}

	private void parseClass(DatabindingsProvider provider, String value) throws Exception {
		String namespace = StringUtils.substringBefore(value, ":");
		String packageValue = m_namespaceToPackage.get(namespace);
		//
		if (packageValue == null) {
			provider.addWarning(
					MessageFormat.format(Messages.ValidationInfo_validatorNotFound, value),
					new Throwable());
		} else {
			String className = packageValue + "." + StringUtils.substringAfter(value, ":");
			//
			try {
				ClassLoader classLoader = provider.getXmlObjectRoot().getContext().getClassLoader();
				classLoader.loadClass(className);
				m_classNames.add(className);
			} catch (ClassNotFoundException e) {
				provider.addWarning(
						MessageFormat.format(Messages.ValidationInfo_validatorClassNotFound, className),
						new Throwable());
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	//
	//
	////////////////////////////////////////////////////////////////////////////
	public List<String> getClassNames() {
		return m_classNames;
	}

	public void setClassNames(List<String> classNames) {
		m_update = true;
		if (!classNames.equals(m_classNames)) {
			m_classNames = classNames;
		}
	}

	public void appendValue(StringBuffer value) throws Exception {
		if (m_update && !m_classNames.isEmpty()) {
			if (m_classNames.size() == 1) {
				String className = m_classNames.get(0);
				String namespace = m_packageToNamespace.get(ClassUtils.getPackageName(className));
				value.append(", validationRule="
						+ StringUtils.defaultString(namespace)
						+ ":"
						+ ClassUtils.getShortClassName(className));
			} else {
				value.append(", validationRules={");
				int index = 0;
				for (String className : m_classNames) {
					if (index++ > 0) {
						value.append(", ");
					}
					String namespace = m_packageToNamespace.get(ClassUtils.getPackageName(className));
					value.append(StringUtils.defaultString(namespace)
							+ ":"
							+ ClassUtils.getShortClassName(className));
				}
				value.append("}");
			}
		}
	}

	public boolean update() throws Exception {
		return m_update;
	}

	public void applyChanges(AbstractDocumentObject object) throws Exception {
		if (m_update) {
			if (object instanceof DocumentAttribute) {
				if (m_classNames.size() == 1) {
					String className = m_classNames.get(0);
					String namespace = m_packageToNamespace.get(ClassUtils.getPackageName(className));
					if (namespace == null) {
						DocumentAttribute attribute = (DocumentAttribute) object;
						namespace = getNamespace(attribute.getEnclosingElement().getRoot(), className);
						//
						String value = attribute.getValue();
						int index = value.indexOf("validationRule=") + 15;
						attribute.setValue(value.substring(0, index) + namespace + value.substring(index));
					}
				} else {
					List<String> nonExistingNamespace = new ArrayList<>();
					DocumentAttribute attribute = (DocumentAttribute) object;
					for (String className : m_classNames) {
						String namespace = m_packageToNamespace.get(ClassUtils.getPackageName(className));
						if (namespace == null || nonExistingNamespace.contains(namespace)) {
							if (namespace == null) {
								// create non-existing namespace
								namespace = getNamespace(attribute.getEnclosingElement().getRoot(), className);
								nonExistingNamespace.add(namespace);
							}
							//
							String value = attribute.getValue();
							int index =
									value.indexOf(
											":" + ClassUtils.getShortClassName(className),
											value.indexOf("validationRules=") + 16);
							attribute.setValue(value.substring(0, index) + namespace + value.substring(index));
						}
					}
				}
			} else {
				DocumentElement bindingElement = (DocumentElement) object;
				if (m_classNames.isEmpty()) {
					DocumentElement validationRules =
							bindingElement.getChild("Binding.validationRules", true);
					if (validationRules != null) {
						validationRules.remove();
					}
					//
					DocumentElement validationRule = bindingElement.getChild("Binding.validationRule", true);
					if (validationRule != null) {
						validationRule.remove();
					}
				} else {
					DocumentElement validationRule = bindingElement.getChild("Binding.validationRule", true);
					if (validationRule != null) {
						validationRule.remove();
					}
					//
					DocumentElement validationRules =
							bindingElement.getChild("Binding.validationRules", true);
					if (validationRules == null) {
						validationRules = new DocumentElement("Binding.validationRule");
						bindingElement.addChild(validationRules);
					} else {
						validationRules.removeChildren();
					}
					//
					DocumentElement rootElement = bindingElement.getRoot();
					//
					for (String className : m_classNames) {
						DocumentElement ruleElement = new DocumentElement();
						ruleElement.setTag(getNamespace(rootElement, className)
								+ ":"
								+ ClassUtils.getShortClassName(className));
						validationRules.addChild(ruleElement);
					}
				}
			}
			m_update = false;
		}
	}

	private String getNamespace(DocumentElement rootElement, String className) throws Exception {
		String packageValue = ClassUtils.getPackageName(className);
		String namespace = m_packageToNamespace.get(packageValue);
		if (namespace == null) {
			int index = 0;
			while (true) {
				namespace = "v" + Integer.toString(index);
				if (m_packageToNamespace.containsValue(namespace)) {
					index++;
				} else {
					m_packageToNamespace.put(packageValue, namespace);
					rootElement.setAttribute("xmlns:" + namespace, "clr-namespace:" + packageValue);
					break;
				}
			}
		}
		return namespace;
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
		providers.add(new ValidationUiContentProvider(provider, this));
	}
}