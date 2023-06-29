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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables;

import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.GlobalFactoryHelper;
import org.eclipse.wb.internal.rcp.databinding.model.IObservableFactory;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailListBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailSetBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailValueBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.properties.ListPropertyDetailCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.properties.SetPropertyDetailCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.properties.ValuePropertyDetailCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.standard.BeanObservableDetailListCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.standard.BeanObservableDetailSetCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.standard.BeanObservableDetailValueCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.CheckedElementsObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.FiltersObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.MultiSelectionObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.SingleSelectionObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.properties.ViewerPropertyCheckedElementsCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.properties.ViewerPropertyFiltersCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.properties.ViewerPropertyMultiSelectionCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.properties.ViewerPropertySingleSelectionCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.standard.CheckedElementsObservableCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.standard.FiltersObservableCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.standard.MultiSelectionObservableCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.standard.SingleSelectionObservableCodeSupport;

/**
 * {@link IObservableFactory} for <code>JFace</code> viewers.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public abstract class ViewerObservableFactory implements IObservableFactory {
	private final Type m_type;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ViewerObservableFactory(Type type) {
		Assert.isTrue(type == Type.OnlyValue
				|| type == Type.OnlyList
				|| type == Type.OnlySet
				|| type == Type.Detail);
		m_type = type;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IObservableFactory
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final Type getType() throws Exception {
		return m_type;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Factories
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Factory with type {@link Type#OnlyValue} for create {@link SingleSelectionObservableInfo}.
	 */
	public static final IObservableFactory SINGLE_SELECTION = new ViewerObservableFactory(
			Type.OnlyValue) {
		@Override
		public ObservableInfo createObservable(BindableInfo object,
				BindableInfo property,
				Type type,
				boolean version_1_3) throws Exception {
			SingleSelectionObservableInfo observable = new SingleSelectionObservableInfo(object, property);
			if (version_1_3) {
				observable.setCodeSupport(new ViewerPropertySingleSelectionCodeSupport());
			} else {
				observable.setCodeSupport(new SingleSelectionObservableCodeSupport());
			}
			return observable;
		}
	};
	/**
	 * Factory with type {@link Type#Detail} for create master-detail observable's.
	 */
	public static final IObservableFactory DETAIL_SINGLE_SELECTION = new ViewerObservableFactory(
			Type.Detail) {
		@Override
		public ObservableInfo createObservable(BindableInfo object,
				BindableInfo property,
				Type type,
				boolean version_1_3) throws Exception {
			// create master
			SingleSelectionObservableInfo masterObservable =
					(SingleSelectionObservableInfo) SINGLE_SELECTION.createObservable(
							object,
							property,
							type,
							version_1_3);
			// create global detail
			ObservableInfo detailObservable =
					GlobalFactoryHelper.createDetailObservable(masterObservable, object, type);
			if (detailObservable != null) {
				return detailObservable;
			}
			// create detail
			DetailBeanObservableInfo observable = null;
			switch (type) {
			case OnlyValue :
				observable = new DetailValueBeanObservableInfo(masterObservable, null, null, null);
				if (version_1_3) {
					observable.setCodeSupport(new ValuePropertyDetailCodeSupport());
				} else {
					observable.setCodeSupport(new BeanObservableDetailValueCodeSupport());
				}
				break;
			case OnlyList :
				observable = new DetailListBeanObservableInfo(masterObservable, null, null, null);
				if (version_1_3) {
					observable.setCodeSupport(new ListPropertyDetailCodeSupport());
				} else {
					observable.setCodeSupport(new BeanObservableDetailListCodeSupport());
				}
				break;
			case OnlySet :
				observable = new DetailSetBeanObservableInfo(masterObservable, null, null, null);
				if (version_1_3) {
					observable.setCodeSupport(new SetPropertyDetailCodeSupport());
				} else {
					observable.setCodeSupport(new BeanObservableDetailSetCodeSupport());
				}
				break;
			}
			Assert.isNotNull(observable);
			observable.setPojoBindable(masterObservable.isPojoBindable());
			return observable;
		}
	};
	/**
	 * Factory with type {@link Type#OnlyList} for create {@link MultiSelectionObservableInfo}.
	 */
	public static final IObservableFactory MULTI_SELECTION = new ViewerObservableFactory(
			Type.OnlyList) {
		@Override
		public ObservableInfo createObservable(BindableInfo object,
				BindableInfo property,
				Type type,
				boolean version_1_3) throws Exception {
			MultiSelectionObservableInfo observable = new MultiSelectionObservableInfo(object);
			if (version_1_3) {
				observable.setCodeSupport(new ViewerPropertyMultiSelectionCodeSupport());
			} else {
				observable.setCodeSupport(new MultiSelectionObservableCodeSupport());
			}
			return observable;
		}
	};
	/**
	 * Factory with type {@link Type#OnlySet} for create {@link CheckedElementsObservableInfo}.
	 */
	public static final IObservableFactory CHECKED_ELEMENTS = new ViewerObservableFactory(
			Type.OnlySet) {
		@Override
		public ObservableInfo createObservable(BindableInfo object,
				BindableInfo property,
				Type type,
				boolean version_1_3) throws Exception {
			CheckedElementsObservableInfo observable = new CheckedElementsObservableInfo(object);
			if (version_1_3) {
				observable.setCodeSupport(new ViewerPropertyCheckedElementsCodeSupport());
			} else {
				observable.setCodeSupport(new CheckedElementsObservableCodeSupport());
			}
			return observable;
		}
	};
	/**
	 * Factory with type {@link Type#OnlySet} for create {@link FiltersObservableInfo}.
	 */
	public static final IObservableFactory FILTERS = new ViewerObservableFactory(Type.OnlySet) {
		@Override
		public ObservableInfo createObservable(BindableInfo object,
				BindableInfo property,
				Type type,
				boolean version_1_3) throws Exception {
			FiltersObservableInfo observable = new FiltersObservableInfo((WidgetBindableInfo) object);
			if (version_1_3) {
				observable.setCodeSupport(new ViewerPropertyFiltersCodeSupport());
			} else {
				observable.setCodeSupport(new FiltersObservableCodeSupport());
			}
			return observable;
		}
	};
}