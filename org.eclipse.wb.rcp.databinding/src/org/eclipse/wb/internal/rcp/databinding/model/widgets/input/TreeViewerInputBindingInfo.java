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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.input;

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfoVisitor;
import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.ui.editor.IPageListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassAndPropertiesRouter;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassAndPropertiesUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassRouter;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.LabelUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.MultiTargetRunnable;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.Messages;
import org.eclipse.wb.internal.rcp.databinding.model.GlobalFactoryHelper;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.DataBindingContextInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetPropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.designer.BeansListObservableFactoryInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.designer.BeansObservableFactoryInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.designer.BeansSetObservableFactoryInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.designer.TreeBeanAdvisorInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.designer.TreeObservableLabelProviderInfo;
import org.eclipse.wb.internal.rcp.databinding.ui.contentproviders.TreeDetailUiContentProvider;
import org.eclipse.wb.internal.rcp.databinding.ui.contentproviders.TreeInputElementUiContentProvider;

import java.util.List;

/**
 * Model for binding input to <code>JFace</code> tree viewer.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public final class TreeViewerInputBindingInfo extends AbstractViewerInputBindingInfo {
	private ObservableCollectionTreeContentProviderInfo m_contentProvider;
	private AbstractLabelProviderInfo m_labelProvider;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public TreeViewerInputBindingInfo(WidgetBindableInfo viewerBindable) throws Exception {
		super(viewerBindable);
	}

	public TreeViewerInputBindingInfo(WidgetBindableInfo viewerBindable,
			WidgetPropertyBindableInfo viewerBindableProperty) throws Exception {
		super(viewerBindable, viewerBindableProperty);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public ObservableCollectionTreeContentProviderInfo getContentProvider() {
		return m_contentProvider;
	}

	public void setContentProvider(ObservableCollectionTreeContentProviderInfo contentProvider) {
		m_contentProvider = contentProvider;
	}

	public AbstractLabelProviderInfo getLabelProvider() {
		return m_labelProvider;
	}

	public void setLabelProvider(AbstractLabelProviderInfo labelProvider) {
		m_labelProvider = labelProvider;
		if (m_labelProvider instanceof ObservableMapLabelProviderInfo mapsLabelProvider) {
			mapsLabelProvider.setBinding(this);
		}
	}

	@Override
	public Class<?> getElementType() {
		ObservableFactoryInfo factoryInfo = m_contentProvider.getFactoryInfo();
		if (factoryInfo instanceof BeansObservableFactoryInfo designerFactoryInfo) {
			return designerFactoryInfo.getElementType();
		}
		return super.getElementType();
	}

	public boolean isDesignerMode() {
		if (m_contentProvider.getFactoryInfo() instanceof BeansObservableFactoryInfo) {
			BeansObservableFactoryInfo factoryInfo =
					(BeansObservableFactoryInfo) m_contentProvider.getFactoryInfo();
			return factoryInfo.isDesignerMode();
		}
		return false;
	}

	@Override
	public void setDefaultProviders(boolean asList, Class<?> elementType, boolean useViewerSupport)
			throws Exception {
		// create advisor
		TreeBeanAdvisorInfo advisor = GlobalFactoryHelper.createTreeBeanAdvisor(m_inputObservable);
		if (advisor == null) {
			advisor = new TreeBeanAdvisorInfo();
		}
		advisor.setElementType(elementType);
		// create factory
		BeansObservableFactoryInfo factory =
				GlobalFactoryHelper.createTreeObservableFactory(m_inputObservable, asList);
		if (factory == null) {
			factory = asList ? new BeansListObservableFactoryInfo() : new BeansSetObservableFactoryInfo();
		}
		factory.setElementType(elementType);
		// create content provider
		m_contentProvider =
				asList
				? new ObservableListTreeContentProviderInfo(factory, advisor)
						: new ObservableSetTreeContentProviderInfo(factory, advisor);
		// create label provider
		KnownElementsObservableInfo allElementsObservable =
				new KnownElementsObservableInfo(m_contentProvider);
		TreeObservableLabelProviderInfo labelProvider =
				GlobalFactoryHelper.createTreeLabelProvider(m_inputObservable, allElementsObservable);
		if (labelProvider == null) {
			labelProvider = new TreeObservableLabelProviderInfo(allElementsObservable);
		}
		labelProvider.setElementType(elementType);
		m_labelProvider = labelProvider;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Editing
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void createContentProviders(List<IUiContentProvider> providers,
			IPageListener listener,
			DatabindingsProvider provider) throws Exception {
		// configure page
		listener.setTitle(Messages.TreeViewerInputBindingInfo_title);
		listener.setMessage(Messages.TreeViewerInputBindingInfo_errorMessage);
		//
		providers.add(new LabelUiContentProvider(Messages.TreeViewerInputBindingInfo_viewerLabel,
				m_viewerBindable.getPresentation().getTextForBinding()));
		providers.add(new LabelUiContentProvider(Messages.TreeViewerInputBindingInfo_inputLabel,
				m_inputObservable.getPresentationText()));
		//
		TreeInputElementUiContentProvider inputEditor =
				new TreeInputElementUiContentProvider(this, listener, provider);
		//
		if (m_inputObservable instanceof DetailBeanObservableInfo) {
			m_inputObservable.createContentProviders(providers, null, provider);
			//
			ChooseClassAndPropertiesUiContentProvider masterUIContentProvider =
					(ChooseClassAndPropertiesUiContentProvider) providers.get(providers.size() - 1);
			//
			ChooseClassConfiguration configuration = new ChooseClassConfiguration();
			configuration.setDialogFieldLabel(Messages.TreeViewerInputBindingInfo_chooseLabel);
			configuration.setValueScope("beans1");
			configuration.setChooseInterfaces(true);
			configuration.setEmptyClassErrorMessage(Messages.TreeViewerInputBindingInfo_chooseEmptyMessage);
			configuration.setErrorMessagePrefix(Messages.TreeViewerInputBindingInfo_chooseErrorPrefix);
			//
			TreeDetailUiContentProvider detailUIContentProvider =
					new TreeDetailUiContentProvider(configuration,
							(DetailBeanObservableInfo) m_inputObservable);
			providers.add(detailUIContentProvider);
			//
			new ChooseClassAndPropertiesRouter(masterUIContentProvider, detailUIContentProvider);
			new ChooseClassRouter(detailUIContentProvider,
					new MultiTargetRunnable(detailUIContentProvider, inputEditor.getElementTypeUIProviders()));
		}
		//
		providers.add(inputEditor);
		//
		super.createContentProviders(providers, listener, provider);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getPresentationText() throws Exception {
		return super.getPresentationText()
				+ "("
				+ m_contentProvider.getPresentationText()
				+ ", "
				+ m_labelProvider.getPresentationText()
				+ ")";
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Code generation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void addSourceCode(DataBindingContextInfo context,
			List<String> lines,
			CodeGenerationSupport generationSupport) throws Exception {
		addSourceCode(context, lines, generationSupport, m_contentProvider, m_labelProvider);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Parser
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void postParse() throws Exception {
		super.postParse();
		Assert.isNotNull(m_contentProvider);
		Assert.isNotNull(m_labelProvider);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Visiting
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void accept(AstObjectInfoVisitor visitor) throws Exception {
		accept(visitor, m_contentProvider, m_labelProvider);
	}
}