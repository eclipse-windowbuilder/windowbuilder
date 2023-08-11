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
package org.eclipse.wb.internal.rcp.databinding.xwt.model.beans;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.reference.IReferenceProvider;
import org.eclipse.wb.internal.core.databinding.model.reference.StringReferenceProvider;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorWarning;
import org.eclipse.wb.internal.core.utils.xml.DocumentAttribute;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.utils.xml.DocumentModelVisitor;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanSupport;
import org.eclipse.wb.internal.rcp.databinding.xwt.model.ObserveTypeContainer;

import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author lobas_av
 *
 */
public final class BeansObserveTypeContainer extends ObserveTypeContainer {
	public static final String NAMESPACE_KEY = "xmlns:";
	private static final String NAMESPACE_VALUE = "clr-namespace:";
	//
	private XmlObjectInfo m_xmlObjectRoot;
	private List<BeanBindableInfo> m_observables = Collections.emptyList();
	private BeanBindableInfo m_dataContext;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public BeansObserveTypeContainer() {
		super(ObserveType.BEANS, false, true);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IObserveInfo
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void createObservables(XmlObjectInfo xmlObjectRoot) throws Exception {
		m_observables = Lists.newArrayList();
		m_xmlObjectRoot = xmlObjectRoot;
		// handle XML elements
		final ClassLoader classLoader = m_xmlObjectRoot.getContext().getClassLoader();
		final BeanSupport beanSupport = new BeanSupport(classLoader, null);
		final Class<?> BindingContextClass =
				classLoader.loadClass("org.eclipse.xwt.databinding.BindingContext");
		//
		DocumentElement rootElement = m_xmlObjectRoot.getElement();
		//
		final Map<String, String> namespaceToPackage = Maps.newHashMap();
		final String[] xKey = new String[1];
		final String[] dataContextReference = new String[1];
		//
		for (DocumentAttribute attribute : rootElement.getDocumentAttributes()) {
			String name = attribute.getName();
			String value = attribute.getValue();
			if (name.startsWith(NAMESPACE_KEY)) {
				if (value.startsWith(NAMESPACE_VALUE)) {
					namespaceToPackage.put(
							name.substring(NAMESPACE_KEY.length()),
							value.substring(NAMESPACE_VALUE.length()));
				} else if (value.equals("http://www.eclipse.org/xwt")) {
					xKey[0] = name.substring(NAMESPACE_KEY.length()) + ":Key";
				}
			} else if (name.equalsIgnoreCase("DataContext")) {
				dataContextReference[0] =
						StringUtils.substringBetween(value, "{StaticResource", "}").trim();
			}
		}
		//
		rootElement.accept(new DocumentModelVisitor() {
			@Override
			public boolean visit(DocumentElement element) {
				if (element.getTag().endsWith(".Resources")) {
					for (DocumentElement child : element.getChildren()) {
						try {
							String elementName = child.getTag();
							Class<?> beanClass = null;
							//
							if (elementName.equalsIgnoreCase("BindingContext")) {
								beanClass = BindingContextClass;
							} else {
								int index = elementName.indexOf(':');
								String beanClassName =
										namespaceToPackage.get(elementName.substring(0, index))
										+ "."
										+ elementName.substring(index + 1);
								beanClass = CoreUtils.load(classLoader, beanClassName);
							}
							//
							IReferenceProvider referenceProvider =
									new StringReferenceProvider(child.getAttribute(xKey[0]));
							boolean dataContext =
									referenceProvider.getReference().equals(dataContextReference[0]);
							//
							XmlElementBeanBindableInfo bindable =
									new XmlElementBeanBindableInfo(beanSupport,
											beanClass,
											referenceProvider,
											null,
											dataContext);
							m_observables.add(bindable);
							if (dataContext) {
								m_dataContext = bindable;
							}
						} catch (ClassNotFoundException e) {
							m_xmlObjectRoot.getContext().addWarning(
									new EditorWarning("ClassNotFoundException: " + element, new Throwable()));
						} catch (Throwable e) {
							throw ReflectionUtils.propagate(e);
						}
					}
					return false;
				}
				return true;
			}
		});
	}

	@Override
	public List<IObserveInfo> getObservables() {
		return CoreUtils.cast(m_observables);
	}

	public List<BeanBindableInfo> getObservables0() {
		return m_observables;
	}

	public BeanBindableInfo getDataContext() {
		return m_dataContext;
	}

	public final BindableInfo resolve(String reference) throws Exception {
		for (BeanBindableInfo bindable : m_observables) {
			BindableInfo result = bindable.resolveReference(reference);
			if (result != null) {
				return result;
			}
		}
		return null;
	}
}