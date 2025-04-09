/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.rcp.databinding.model.beans.bindables;

import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.reference.IReferenceProvider;
import org.eclipse.wb.internal.core.databinding.ui.decorate.IObserveDecorator;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.IObservableFactory;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.WritableListBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.WritableSetBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.properties.SelfListCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.properties.SelfSetCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.standard.WritableListCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.standard.WritableSetCodeSupport;

import java.util.Collections;
import java.util.List;

/**
 * Model for fake property for collection objects.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public final class CollectionPropertyBindableInfo extends PropertyBindableInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public CollectionPropertyBindableInfo(BeanSupport beanSupport,
			IObserveInfo parent,
			String text,
			Class<?> objectType,
			IReferenceProvider referenceProvider) {
		super(beanSupport, parent, text, objectType, referenceProvider);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Hierarchy
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public List<IObserveInfo> getChildren(ChildrenContext context) {
		return Collections.emptyList();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// PropertyBindableInfo
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public IObservableFactory getObservableFactory() throws Exception {
		return new IObservableFactory() {
			@Override
			public Type getType() throws Exception {
				return Type.InputCollection;
			}

			@Override
			public ObservableInfo createObservable(BindableInfo object,
					BindableInfo property,
					Type type,
					boolean version_1_3) throws Exception {
				BeanBindableInfo bindableObject = (BeanBindableInfo) object;
				CollectionPropertyBindableInfo bindableProperty = (CollectionPropertyBindableInfo) property;
				//
				if (List.class.isAssignableFrom(getObjectType())) {
					WritableListBeanObservableInfo observable =
							new WritableListBeanObservableInfo(bindableObject, bindableProperty, null);
					if (version_1_3) {
						observable.setCodeSupport(new SelfListCodeSupport());
					} else {
						observable.setCodeSupport(new WritableListCodeSupport());
					}
					return observable;
				}
				WritableSetBeanObservableInfo observable =
						new WritableSetBeanObservableInfo(bindableObject, bindableProperty, null);
				if (version_1_3) {
					observable.setCodeSupport(new SelfSetCodeSupport());
				} else {
					observable.setCodeSupport(new WritableSetCodeSupport());
				}
				return observable;
			}
		};
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public IObserveDecorator getDecorator() {
		return IObserveDecorator.BOLD;
	}
}