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
package org.eclipse.wb.internal.rcp.databinding.model;

import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassAndPropertiesConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.PropertyAdapter;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.binding.DataBindManager;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.rcp.databinding.model.IObservableFactory.Type;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.MapsBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.KnownElementsObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.designer.BeansObservableFactoryInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.designer.TreeBeanAdvisorInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.designer.TreeObservableLabelProviderInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.SingleSelectionObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.wizards.autobindings.IAutomaticWizardStub;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.swt.widgets.Composite;

import java.beans.PropertyDescriptor;
import java.util.List;

/**
 * Helper class for work with {@link IGlobalObservableFactory}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model
 */
public final class GlobalFactoryHelper {
	private static List<IGlobalObservableFactory> m_factories;

	////////////////////////////////////////////////////////////////////////////
	//
	// Observable
	//
	////////////////////////////////////////////////////////////////////////////
	public static final ObservableInfo createDetailObservable(SingleSelectionObservableInfo masterObservable,
			BindableInfo object,
			Type type) throws Exception {
		for (IGlobalObservableFactory factory : getFactories()) {
			ObservableInfo detailObservable =
					factory.createDetailObservable(masterObservable, object, type);
			if (detailObservable != null) {
				return detailObservable;
			}
		}
		return null;
	}

	public static final MapsBeanObservableInfo createObserveMaps(ObservableInfo inputObservable,
			ObservableInfo domainObservable,
			Class<?> elementType,
			boolean[] useViewerSupport) throws Exception {
		for (IGlobalObservableFactory factory : getFactories()) {
			MapsBeanObservableInfo observeMaps =
					factory.createObserveMaps(
							inputObservable,
							domainObservable,
							elementType,
							useViewerSupport);
			if (observeMaps != null) {
				return observeMaps;
			}
		}
		return null;
	}

	public static BeansObservableFactoryInfo createTreeObservableFactory(ObservableInfo inputObservable,
			boolean asList) throws Exception {
		for (IGlobalObservableFactory factory : getFactories()) {
			BeansObservableFactoryInfo treeObservableFactory =
					factory.createTreeObservableFactory(inputObservable, asList);
			if (treeObservableFactory != null) {
				return treeObservableFactory;
			}
		}
		return null;
	}

	public static TreeBeanAdvisorInfo createTreeBeanAdvisor(ObservableInfo inputObservable)
			throws Exception {
		for (IGlobalObservableFactory factory : getFactories()) {
			TreeBeanAdvisorInfo treeBeanAdvisor = factory.createTreeBeanAdvisor(inputObservable);
			if (treeBeanAdvisor != null) {
				return treeBeanAdvisor;
			}
		}
		return null;
	}

	public static TreeObservableLabelProviderInfo createTreeLabelProvider(ObservableInfo inputObservable,
			KnownElementsObservableInfo allElementsObservable) throws Exception {
		for (IGlobalObservableFactory factory : getFactories()) {
			TreeObservableLabelProviderInfo treeLabelProvider =
					factory.createTreeLabelProvider(inputObservable, allElementsObservable);
			if (treeLabelProvider != null) {
				return treeLabelProvider;
			}
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// UI
	//
	////////////////////////////////////////////////////////////////////////////
	public static void configureChooseElementForViewerInput(ObservableInfo inputObservable,
			ChooseClassAndPropertiesConfiguration configuration) throws Exception {
		for (IGlobalObservableFactory factory : getFactories()) {
			factory.configureChooseElementForViewerInput(inputObservable, configuration);
		}
	}

	public static void configureChooseElementForTreeViewerInput(ObservableInfo inputObservable,
			ChooseClassConfiguration configuration) throws Exception {
		for (IGlobalObservableFactory factory : getFactories()) {
			factory.configureChooseElementForTreeViewerInput(inputObservable, configuration);
		}
	}

	public static void filterElementPropertiesForViewerInput(ObservableInfo inputObservable,
			Class<?> elementType,
			List<PropertyDescriptor> descriptors) throws Exception {
		for (IGlobalObservableFactory factory : getFactories()) {
			factory.filterElementPropertiesForTreeViewerInput(inputObservable, elementType, descriptors);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Automatic Wizard
	//
	////////////////////////////////////////////////////////////////////////////
	public static void automaticWizardConfigure(ChooseClassAndPropertiesConfiguration configuration)
			throws Exception {
		for (IGlobalObservableFactory factory : getFactories()) {
			factory.automaticWizardConfigure(configuration);
		}
	}

	public static List<PropertyAdapter> automaticWizardGetProperties(IJavaProject javaProject,
			ClassLoader classLoader,
			Class<?> choosenClass) throws Exception {
		for (IGlobalObservableFactory factory : getFactories()) {
			List<PropertyAdapter> properties =
					factory.automaticWizardGetProperties(javaProject, classLoader, choosenClass);
			if (properties != null) {
				return properties;
			}
		}
		return null;
	}

	public static IAutomaticWizardStub automaticWizardCreateStub(IJavaProject javaProject,
			ClassLoader classLoader,
			Class<?> choosenClass) throws Exception {
		for (IGlobalObservableFactory factory : getFactories()) {
			IAutomaticWizardStub automaticWizardStub =
					factory.automaticWizardCreateStub(javaProject, classLoader, choosenClass);
			if (automaticWizardStub != null) {
				return automaticWizardStub;
			}
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Controller
	//
	////////////////////////////////////////////////////////////////////////////
	public static void moveBean(IObserveInfo observe,
			AstEditor controllerEditor,
			TypeDeclaration controllerRootNode) throws Exception {
		for (IGlobalObservableFactory factory : getFactories()) {
			if (factory.moveBean(observe, controllerEditor, controllerRootNode)) {
				break;
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Preferences
	//
	////////////////////////////////////////////////////////////////////////////
	public static void confgureCodeGenerationPreferencePage(Composite parent,
			DataBindManager bindManager) throws Exception {
		for (IGlobalObservableFactory factory : getFactories()) {
			factory.confgureCodeGenerationPreferencePage(parent, bindManager);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private static List<IGlobalObservableFactory> getFactories() throws Exception {
		if (m_factories == null) {
			m_factories =
					ExternalFactoriesHelper.getElementsInstances(
							IGlobalObservableFactory.class,
							"org.eclipse.wb.rcp.databinding.observeFactory",
							"factory");
		}
		return m_factories;
	}
}