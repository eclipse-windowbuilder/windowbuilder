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
import org.eclipse.wb.internal.core.databinding.model.AstObjectInfoVisitor;
import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.ui.editor.IPageListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.BindingContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.LabelUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.SeparatorUiContentProvider;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.swing.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.swing.databinding.Messages;
import org.eclipse.wb.internal.swing.databinding.model.ObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.generic.ClassGenericType;
import org.eclipse.wb.internal.swing.databinding.model.generic.IGenericType;
import org.eclipse.wb.internal.swing.databinding.model.properties.ObjectPropertyInfo;
import org.eclipse.wb.internal.swing.databinding.model.properties.PropertyInfo;
import org.eclipse.wb.internal.swing.databinding.ui.contentproviders.BindingNameUiContentProvider;
import org.eclipse.wb.internal.swing.databinding.ui.contentproviders.ChooseClassAndPropertiesConfiguration;
import org.eclipse.wb.internal.swing.databinding.ui.contentproviders.ConverterUiContentProvider;
import org.eclipse.wb.internal.swing.databinding.ui.contentproviders.DetailBindingUiContentProvider;
import org.eclipse.wb.internal.swing.databinding.ui.contentproviders.ValidatorUiContentProvider;

import java.util.List;

/**
 * Model for {@link org.jdesktop.swingbinding.JListBinding.DetailBinding}.
 *
 * @author lobas_av
 * @coverage bindings.swing.model.bindings
 */
public final class DetailBindingInfo extends BindingInfo {
	private final JListBindingInfo m_binding;
	private PropertyInfo m_detailProperty;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DetailBindingInfo(ObserveInfo target,
			ObserveInfo targetProperty,
			PropertyInfo targetAstProperty,
			ObserveInfo model,
			ObserveInfo modelProperty,
			PropertyInfo modelAstProperty,
			JListBindingInfo binding) {
		super(target, targetProperty, targetAstProperty, model, modelProperty, modelAstProperty);
		m_binding = binding;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public IGenericType getModelPropertyType() {
		if (m_detailProperty != null) {
			return m_detailProperty.getValueType();
		}
		return super.getModelPropertyType();
	}

	@Override
	public IGenericType getTargetPropertyType() {
		return ClassGenericType.OBJECT_CLASS;
	}

	public PropertyInfo getDetailProperty() {
		return m_detailProperty;
	}

	public void setDetailProperty(PropertyInfo detailProperty) {
		Assert.isNotNull(detailProperty);
		m_detailProperty = detailProperty;
	}

	public JListBindingInfo getJListBinding() {
		return m_binding;
	}

	@Override
	public boolean delete(List<BindingInfo> bindings) throws Exception {
		m_detailProperty = new ObjectPropertyInfo(m_binding.getInputElementType());
		m_converter = null;
		m_validator = null;
		return false;
	}

	public boolean isVirtual() {
		return m_converter == null
				&& m_validator == null
				&& m_detailProperty instanceof ObjectPropertyInfo;
	}

	@Override
	public String getVariableIdentifier() throws Exception {
		if (isVirtual()) {
			return m_binding.getVariableIdentifier() + ".getDetailBinding()";
		}
		String variable = super.getVariableIdentifier();
		if (variable == null) {
			return m_binding.getVariableIdentifier() + ".setDetailBinding()";
		}
		return variable;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Editing
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Create {@link IUiContentProvider} content providers for edit this model.
	 */
	@Override
	public void createContentProviders(List<BindingInfo> bindings,
			List<IUiContentProvider> providers,
			IPageListener listener,
			DatabindingsProvider provider) throws Exception {
		// configure page
		listener.setTitle(Messages.DetailBindingInfo_listenerTitle);
		listener.setMessage(Messages.DetailBindingInfo_listenerMessage);
		// add target
		providers.add(new LabelUiContentProvider(Messages.DetailBindingInfo_component,
				m_target.getPresentation().getTextForBinding()));
		// add model
		providers.add(new LabelUiContentProvider(Messages.DetailBindingInfo_model,
				getModelPresentationText(false)));
		providers.add(new DetailBindingUiContentProvider(createConfiguration(), this));
		// binding self properties
		providers.add(new SeparatorUiContentProvider());
		providers.add(new ConverterUiContentProvider(createConverterConfiguration(), this));
		providers.add(new ValidatorUiContentProvider(createValidatorConfiguration(), this));
		providers.add(new BindingNameUiContentProvider(this));
		providers.add(new BindingContentProvider(this, provider.getJavaInfoRoot()));
	}

	private static ChooseClassAndPropertiesConfiguration createConfiguration() {
		ChooseClassAndPropertiesConfiguration configuration =
				new ChooseClassAndPropertiesConfiguration();
		configuration.setDialogFieldLabel(Messages.DetailBindingInfo_chooseLabel);
		configuration.setValueScope("beans");
		configuration.setChooseInterfaces(true);
		configuration.setEmptyClassErrorMessage(Messages.DetailBindingInfo_chooseError);
		configuration.setErrorMessagePrefix(Messages.DetailBindingInfo_chooseErrorPrefix);
		configuration.setPropertiesLabel(Messages.DetailBindingInfo_choosePropertiesLabel);
		configuration.setPropertiesErrorMessage(Messages.DetailBindingInfo_choosePropertiesError);
		return configuration;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	//  Code generation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean addSourceCodeSeparator() {
		return false;
	}

	@Override
	public boolean isManaged() {
		return true;
	}

	@Override
	public void addSourceCode(List<String> lines, CodeGenerationSupport generationSupport)
			throws Exception {
	}

	public void addDetailSourceCode(List<String> lines, CodeGenerationSupport generationSupport)
			throws Exception {
		if (isVirtual()) {
			return;
		}
		generationSupport.addSourceCode(m_detailProperty, lines);
		boolean localVariable = !isField();
		if (localVariable && m_converter == null && m_validator == null) {
			setVariableIdentifier(null);
			lines.add(m_binding.getVariableIdentifier()
					+ ".setDetailBinding("
					+ m_detailProperty.getVariableIdentifier()
					+ getCreateMethodHeaderEnd()
					+ ";");
		} else {
			if (super.getVariableIdentifier() == null) {
				setVariableIdentifier(generationSupport.generateLocalName("JListDetail"));
			}
			if (localVariable) {
				lines.add(m_binding.getTypeSourceCode(generationSupport) + ".DetailBinding ");
			}
			lines.add(super.getVariableIdentifier()
					+ " = "
					+ m_binding.getVariableIdentifier()
					+ ".setDetailBinding("
					+ m_detailProperty.getVariableIdentifier()
					+ getCreateMethodHeaderEnd()
					+ ";");
			addFinishSourceCode(lines, generationSupport, false);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IASTObjectInfo2
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void setVariableIdentifier(JavaInfo javaInfoRoot, String variable, boolean field) {
		boolean useGenerics = CoreUtils.useGenerics(javaInfoRoot.getEditor().getJavaProject());
		CodeGenerationSupport generationSupport = new CodeGenerationSupport(useGenerics);
		String type = m_binding.getTypeSourceCode(generationSupport) + ".DetailBinding";
		setVariableIdentifier(javaInfoRoot, type, variable, field);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getTargetPresentationText(boolean full) throws Exception {
		return m_target.getPresentation().getTextForBinding() + ".detail";
	}

	@Override
	public String getModelPresentationText(boolean full) throws Exception {
		if (m_detailProperty != null) {
			return m_detailProperty.getPresentationText(m_model, m_modelProperty, full);
		}
		return super.getModelPresentationText(full);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Visiting
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void accept(AstObjectInfoVisitor visitor) throws Exception {
		super.accept(visitor);
		if (m_detailProperty != null) {
			m_detailProperty.accept(visitor);
		}
	}
}