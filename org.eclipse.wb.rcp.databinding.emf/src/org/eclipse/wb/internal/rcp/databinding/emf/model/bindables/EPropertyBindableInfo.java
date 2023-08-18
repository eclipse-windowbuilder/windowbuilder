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
package org.eclipse.wb.internal.rcp.databinding.emf.model.bindables;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.databinding.model.IObserveDecoration;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.presentation.SimpleObservePresentation;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.databinding.ui.decorate.IObserveDecorator;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.rcp.databinding.emf.model.EmfObserveTypeContainer;
import org.eclipse.wb.internal.rcp.databinding.emf.model.bindables.PropertiesSupport.PropertyInfo;
import org.eclipse.wb.internal.rcp.databinding.emf.model.observables.EmfObservableListCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.emf.model.observables.EmfObservableValueCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.emf.model.observables.ListEmfObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.emf.model.observables.ValueEmfObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.emf.model.observables.properties.EmfListPropertyCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.emf.model.observables.properties.EmfValuePropertyCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.IObservableFactory;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanSupport;
import org.eclipse.wb.internal.rcp.databinding.ui.providers.TypeImageProvider;

import java.util.Collections;
import java.util.List;

/**
 * Model for EMF properties.
 *
 * @author lobas_av
 * @coverage bindings.rcp.emf.model
 */
public class EPropertyBindableInfo extends BindableInfo implements IObserveDecoration {
	private PropertiesSupport m_propertiesSupport;
	private EPropertyBindableInfo m_parent;
	private List<EPropertyBindableInfo> m_properties;
	private final IObservePresentation m_presentation;
	private final IObserveDecorator m_decorator;
	private String m_internalReference;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public EPropertyBindableInfo(PropertiesSupport propertiesSupport,
			EPropertyBindableInfo parent,
			Class<?> objectType,
			String text,
			String reference) {
		this(objectType, reference, new SimpleObservePresentation(text,
				TypeImageProvider.getImageDescriptor(objectType)));
		m_propertiesSupport = propertiesSupport;
		m_parent = parent;
		m_internalReference = reference;
	}

	protected EPropertyBindableInfo(Class<?> objectType,
			String reference,
			IObservePresentation presentation) {
		super(objectType, reference);
		m_presentation = presentation;
		m_decorator = BeanSupport.getDecorator(objectType);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	protected EPropertyBindableInfo resolvePropertyReference(String[] reference, int index)
			throws Exception {
		if (reference.length == index) {
			return this;
		}
		getChildren(ChildrenContext.ChildrenForPropertiesTable);
		String referenceValue = reference[index];
		for (EPropertyBindableInfo property : m_properties) {
			if (referenceValue.equals(property.m_internalReference)) {
				return property.resolvePropertyReference(reference, index + 1);
			}
		}
		return null;
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
	protected List<BindableInfo> getChildren() {
		return Collections.emptyList();
	}

	@Override
	public List<IObserveInfo> getChildren(ChildrenContext context) {
		if (context == ChildrenContext.ChildrenForPropertiesTable
				&& m_propertiesSupport.availableEMFProperties()) {
			if (m_properties == null) {
				try {
					// add properties
					if (m_propertiesSupport != null
							&& m_propertiesSupport.getEObjectClass().isAssignableFrom(getObjectType())) {
						List<PropertyInfo> properties = m_propertiesSupport.getProperties(getObjectType());
						if (properties.isEmpty()) {
							m_properties = Collections.emptyList();
						} else {
							StringBuffer reference = new StringBuffer();
							EPropertyBindableInfo eProperty = this;
							do {
								if (reference.length() > 0) {
									reference.insert(0, ", ");
								}
								reference.insert(0, eProperty.m_internalReference);
								eProperty = eProperty.m_parent;
							} while (eProperty != null);
							reference.insert(0, "org.eclipse.emf.databinding.FeaturePath.fromList(");
							reference.append(", ");
							//
							m_properties = Lists.newArrayList();
							for (PropertyInfo propertyInfo : properties) {
								EPropertyBindableInfo property =
										new EPropertyBindableInfo(m_propertiesSupport,
												this,
												propertyInfo.type,
												propertyInfo.name,
												reference.toString() + propertyInfo.reference + ")");
								property.m_internalReference = propertyInfo.reference;
								m_properties.add(property);
							}
						}
					} else {
						m_properties = Collections.emptyList();
					}
				} catch (Throwable e) {
					DesignerPlugin.log(e);
					m_properties = Collections.emptyList();
				}
			}
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
	// IObserveDecoration
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public IObserveDecorator getDecorator() {
		return m_decorator;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ObserveType
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public ObserveType getType() {
		return EmfObserveTypeContainer.TYPE;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Creation
	//
	////////////////////////////////////////////////////////////////////////////
	private final IObservableFactory m_observableFactory = new IObservableFactory() {
		private Type m_type;

		@Override
		public Type getType() throws Exception {
			if (m_type == null) {
				// calculate type
				if (List.class.isAssignableFrom(getObjectType())) {
					m_type = Type.List;
				} else {
					m_type = Type.Any;
				}
			}
			return m_type;
		}

		@Override
		public ObservableInfo createObservable(BindableInfo object,
				BindableInfo eProperty,
				Type type,
				boolean version_1_3) throws Exception {
			Assert.isNotNull(type);
			Assert.isTrue(eProperty == EPropertyBindableInfo.this);
			EObjectBindableInfo eObject = (EObjectBindableInfo) object;
			boolean version_2_5 = m_parent != null || eObject.getPropertiesSupport().isEMFProperties();
			// create observable
			ObservableInfo observable = null;
			switch (type) {
			case OnlyValue :
				observable = new ValueEmfObservableInfo(eObject, EPropertyBindableInfo.this);
				if (version_2_5) {
					observable.setCodeSupport(new EmfValuePropertyCodeSupport());
				} else {
					observable.setCodeSupport(new EmfObservableValueCodeSupport());
				}
				break;
			case OnlyList :
				observable = new ListEmfObservableInfo(eObject, EPropertyBindableInfo.this);
				if (version_2_5) {
					observable.setCodeSupport(new EmfListPropertyCodeSupport());
				} else {
					observable.setCodeSupport(new EmfObservableListCodeSupport());
				}
				break;
			}
			//
			Assert.isNotNull(observable);
			return observable;
		}
	};

	@Override
	public IObservableFactory getObservableFactory() throws Exception {
		return m_observableFactory;
	}
}