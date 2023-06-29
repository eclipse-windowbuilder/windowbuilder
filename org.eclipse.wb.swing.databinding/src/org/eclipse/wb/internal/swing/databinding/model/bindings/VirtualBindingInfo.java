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
package org.eclipse.wb.internal.swing.databinding.model.bindings;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.parser.IModelResolver;
import org.eclipse.wb.internal.core.databinding.ui.editor.IPageListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.LabelUiContentProvider;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swing.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.swing.databinding.Messages;
import org.eclipse.wb.internal.swing.databinding.model.ObserveCreationType;
import org.eclipse.wb.internal.swing.databinding.model.ObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.beans.BeanPropertyObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.beans.BeanSupport;
import org.eclipse.wb.internal.swing.databinding.model.generic.ClassGenericType;
import org.eclipse.wb.internal.swing.databinding.model.generic.IGenericType;
import org.eclipse.wb.internal.swing.databinding.model.properties.PropertyInfo;
import org.eclipse.wb.internal.swing.databinding.ui.contentproviders.ElementTypeUiContentProvider;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.List;

/**
 * @author lobas_av
 * @coverage bindings.swing.model.bindings
 */
public final class VirtualBindingInfo extends BindingInfo {
	private IGenericType m_elementType;
	private final boolean m_isTargetVirtual;
	private final ObserveCreationType m_swingType;
	private static ChooseClassConfiguration m_configuration;
	private List<BindingInfo> m_bindings;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public VirtualBindingInfo(ObserveInfo target,
			ObserveInfo targetProperty,
			PropertyInfo targetAstProperty,
			ObserveInfo model,
			ObserveInfo modelProperty,
			PropertyInfo modelAstProperty) {
		super(target, targetProperty, targetAstProperty, model, modelProperty, modelAstProperty);
		m_isTargetVirtual = target.getCreationType() == ObserveCreationType.VirtualBinding;
		m_swingType = m_isTargetVirtual ? model.getCreationType() : target.getCreationType();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public IGenericType getElementType() {
		return m_elementType;
	}

	public void setElementType(IGenericType elementType) {
		m_elementType = elementType;
	}

	public ObserveCreationType getSwingType() {
		return m_swingType;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Editing
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void createContentProviders(List<BindingInfo> bindings,
			List<IUiContentProvider> providers,
			IPageListener listener,
			DatabindingsProvider provider) throws Exception {
		listener.setTitle(Messages.VirtualBindingInfo_listenerTitle);
		listener.setMessage(Messages.VirtualBindingInfo_listenerMessage);
		// add target editors
		providers.add(new LabelUiContentProvider(Messages.VirtualBindingInfo_target,
				getTargetPresentationText(false)));
		// add model editors
		providers.add(new LabelUiContentProvider(Messages.VirtualBindingInfo_model,
				getModelPresentationText(false)));
		//
		if (m_configuration == null) {
			m_configuration = new ChooseClassConfiguration();
			m_configuration.setDialogFieldLabel(Messages.VirtualBindingInfo_chooseTitle);
			m_configuration.setValueScope("beans");
			m_configuration.setChooseInterfaces(true);
			m_configuration.setEmptyClassErrorMessage(Messages.VirtualBindingInfo_chooseError);
			m_configuration.setErrorMessagePrefix(Messages.VirtualBindingInfo_chooseErrorPrefix);
		}
		//
		providers.add(new ElementTypeUiContentProvider(m_configuration, this));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	//  Code generation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void addSourceCode(List<String> lines, CodeGenerationSupport generationSupport)
			throws Exception {
		String reference = m_isTargetVirtual ? m_model.getReference() : m_target.getReference();
		String index = Integer.toString(m_bindings.indexOf(this));
		String target = m_isTargetVirtual ? "model" : "target";
		lines.add("// [Virtual] "
				+ index
				+ " "
				+ target
				+ " "
				+ reference
				+ " "
				+ m_elementType.getFullTypeName());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IASTObjectInfo2
	//
	////////////////////////////////////////////////////////////////////////////
	public void setVariableIdentifier(JavaInfo javaInfoRoot, String variable, boolean field) {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Parser
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void create(List<BindingInfo> bindings) throws Exception {
		m_bindings = bindings;
		super.create(bindings);
		preCreate();
	}

	@Override
	public void preCreate() throws Exception {
		String propertyName =
				m_swingType == ObserveCreationType.JComboBoxBinding ? "selectedItem" : "selectedElement";
		BeanPropertyObserveInfo selectedElement =
				BeanSupport.getProperty(this, !m_isTargetVirtual, propertyName);
		selectedElement.setHostedType(m_elementType);
	}

	@Override
	public void postDelete() throws Exception {
		super.postDelete();
		String propertyName =
				m_swingType == ObserveCreationType.JComboBoxBinding ? "selectedItem" : "selectedElement";
		BeanPropertyObserveInfo selectedElement =
				BeanSupport.getProperty(this, !m_isTargetVirtual, propertyName);
		selectedElement.setHostedType(ClassGenericType.OBJECT_CLASS);
	}

	@Override
	public AstObjectInfo parseExpression(AstEditor editor,
			String signature,
			MethodInvocation invocation,
			Expression[] arguments,
			IModelResolver resolver,
			IDatabindingsProvider provider) throws Exception {
		return null;
	}
}