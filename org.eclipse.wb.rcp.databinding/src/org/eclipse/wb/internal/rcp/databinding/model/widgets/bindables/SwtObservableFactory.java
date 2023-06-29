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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables;

import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.IObservableFactory;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.ItemsSwtObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.SwtObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.TextSwtObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.properties.WidgetPropertiesCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.properties.WidgetPropertyItemsCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.properties.WidgetPropertyTextCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.standard.SwtObservableCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.standard.SwtObservableItemsCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.standard.SwtObservableTextCodeSupport;

/**
 * {@link IObservableFactory} for <code>SWT</code> widgets.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
abstract class SwtObservableFactory implements IObservableFactory {
	private final Type m_type;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SwtObservableFactory(Type type) {
		Assert.isTrue(type == Type.OnlyValue || type == Type.OnlyList);
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

	@Override
	public final ObservableInfo createObservable(BindableInfo object,
			BindableInfo property,
			Type type,
			boolean version_1_3) throws Exception {
		// prepare widget
		WidgetBindableInfo bindableWidget = (WidgetBindableInfo) object;
		// prepare property
		WidgetPropertyBindableInfo bindableProperty = (WidgetPropertyBindableInfo) property;
		// create observable
		return createObservable(bindableWidget, bindableProperty, version_1_3);
	}

	/**
	 * Create {@link ObservableInfo} for given <code>bindableWidget</code> and
	 * <code>bindableProperty</code>.
	 */
	protected abstract ObservableInfo createObservable(WidgetBindableInfo bindableWidget,
			WidgetPropertyBindableInfo bindableProperty,
			boolean version_1_3) throws Exception;

	////////////////////////////////////////////////////////////////////////////
	//
	// Factories
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Factory with type {@link Type#OnlyValue} for create {@link SwtObservableInfo}.
	 */
	public static final IObservableFactory SWT = new SwtObservableFactory(Type.OnlyValue) {
		@Override
		protected ObservableInfo createObservable(WidgetBindableInfo bindableWidget,
				WidgetPropertyBindableInfo bindableProperty,
				boolean version_1_3) throws Exception {
			SwtObservableInfo observable = new SwtObservableInfo(bindableWidget, bindableProperty);
			if (version_1_3) {
				observable.setCodeSupport(new WidgetPropertiesCodeSupport(bindableProperty.getReference()));
			} else {
				observable.setCodeSupport(new SwtObservableCodeSupport());
			}
			return observable;
		}
	};
	/**
	 * Factory with type {@link Type#OnlyValue} for create {@link TextSwtObservableInfo}.
	 */
	public static final IObservableFactory SWT_TEXT = new SwtObservableFactory(Type.OnlyValue) {
		@Override
		protected ObservableInfo createObservable(WidgetBindableInfo bindableWidget,
				WidgetPropertyBindableInfo bindableProperty,
				boolean version_1_3) throws Exception {
			TextSwtObservableInfo observable =
					new TextSwtObservableInfo(bindableWidget, bindableProperty);
			if (version_1_3) {
				observable.setCodeSupport(new WidgetPropertyTextCodeSupport());
			} else {
				observable.setCodeSupport(new SwtObservableTextCodeSupport());
			}
			return observable;
		}
	};
	/**
	 * Factory with type {@link Type#OnlyList} for create {@link ItemsSwtObservableInfo}.
	 */
	public static final IObservableFactory SWT_ITEMS = new SwtObservableFactory(Type.OnlyList) {
		@Override
		protected ObservableInfo createObservable(WidgetBindableInfo bindableWidget,
				WidgetPropertyBindableInfo bindableProperty,
				boolean version_1_3) throws Exception {
			ItemsSwtObservableInfo observable =
					new ItemsSwtObservableInfo(bindableWidget, bindableProperty);
			if (version_1_3) {
				observable.setCodeSupport(new WidgetPropertyItemsCodeSupport());
			} else {
				observable.setCodeSupport(new SwtObservableItemsCodeSupport());
			}
			return observable;
		}
	};
}