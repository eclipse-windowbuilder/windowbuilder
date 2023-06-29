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
import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.swing.databinding.model.ObserveCreationType;
import org.eclipse.wb.internal.swing.databinding.model.ObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.beans.BeanPropertyObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.beans.BeanSupport;
import org.eclipse.wb.internal.swing.databinding.model.generic.ClassGenericType;
import org.eclipse.wb.internal.swing.databinding.model.generic.GenericUtils;
import org.eclipse.wb.internal.swing.databinding.model.generic.IGenericType;
import org.eclipse.wb.internal.swing.databinding.model.properties.ObjectPropertyInfo;
import org.eclipse.wb.internal.swing.databinding.model.properties.PropertyInfo;

import java.util.List;

/**
 *
 * @author lobas_av
 *
 */
public final class JComboBoxBindingInfo extends AutoBindingInfo {
	private static final IGenericType JCOMBO_BOX_CLASS =
			new ClassGenericType(javax.swing.JComboBox.class, null, null);

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public JComboBoxBindingInfo(UpdateStrategyInfo strategyInfo,
			ObserveInfo target,
			ObserveInfo targetProperty,
			PropertyInfo targetAstProperty,
			ObserveInfo model,
			ObserveInfo modelProperty,
			PropertyInfo modelAstProperty) {
		super(strategyInfo,
				target,
				targetProperty,
				targetAstProperty,
				model,
				modelProperty,
				modelAstProperty);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public IGenericType getInputElementType() {
		if (isJComboBoxBinding(m_target, m_targetProperty)) {
			return m_modelProperty.getObjectType().getSubType(0);
		}
		return m_targetProperty.getObjectType().getSubType(0);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	//  Code generation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void addSourceCode(List<String> lines, CodeGenerationSupport generationSupport)
			throws Exception {
		if (isJComboBoxBinding(m_target, m_targetProperty)) {
			addSourceCode(
					m_target,
					m_model,
					m_modelProperty,
					m_modelAstProperty,
					lines,
					generationSupport);
		} else {
			addSourceCode(
					m_model,
					m_target,
					m_targetProperty,
					m_targetAstProperty,
					lines,
					generationSupport);
		}
	}

	private void addSourceCode(ObserveInfo widget,
			ObserveInfo model,
			ObserveInfo modelProperty,
			PropertyInfo modelAstProperty,
			List<String> lines,
			CodeGenerationSupport generationSupport) throws Exception {
		if (getVariableIdentifier() == null) {
			setVariableIdentifier(generationSupport.generateLocalName("JComboBinding"));
		}
		StringBuffer line = new StringBuffer();
		boolean localVariable = !isField();
		if (localVariable) {
			line.append("org.jdesktop.swingbinding.JComboBoxBinding");
		}
		if (modelAstProperty instanceof ObjectPropertyInfo) {
			if (localVariable) {
				if (generationSupport.useGenerics()) {
					line.append(GenericUtils.getTypesSource(
							model.getObjectType().getSubType(0),
							model.getObjectType(),
							JCOMBO_BOX_CLASS));
				}
				line.append(" ");
			}
			line.append(getVariableIdentifier());
			line.append(" = org.jdesktop.swingbinding.SwingBindings.createJComboBoxBinding(");
			line.append(m_strategyInfo.getStrategySourceCode());
			line.append(", ");
			line.append(model.getReference());
			line.append(", ");
			line.append(widget.getReference());
		} else {
			generationSupport.addSourceCode(modelAstProperty, lines);
			if (localVariable) {
				if (generationSupport.useGenerics()) {
					line.append(GenericUtils.getTypesSource(
							modelProperty.getObjectType().getSubType(0),
							model.getObjectType(),
							JCOMBO_BOX_CLASS));
				}
				line.append(" ");
			}
			line.append(getVariableIdentifier());
			line.append(" = org.jdesktop.swingbinding.SwingBindings.createJComboBoxBinding(");
			line.append(m_strategyInfo.getStrategySourceCode());
			line.append(", ");
			line.append(model.getReference());
			line.append(", ");
			line.append(modelAstProperty.getVariableIdentifier());
			line.append(", ");
			line.append(widget.getReference());
		}
		line.append(getCreateMethodHeaderEnd());
		line.append(";");
		lines.add(line.toString());
		addFinishSourceCode(lines, generationSupport, true);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IASTObjectInfo2
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void setVariableIdentifier(JavaInfo javaInfoRoot, String variable, boolean field) {
		String type = "org.jdesktop.swingbinding.JComboBoxBinding";
		if (CoreUtils.useGenerics(javaInfoRoot.getEditor().getJavaProject())) {
			if (isJComboBoxBinding(m_target, m_targetProperty)) {
				type += getTypeSource(m_model, m_modelProperty, m_modelAstProperty, JCOMBO_BOX_CLASS);
			} else {
				type += getTypeSource(m_target, m_targetProperty, m_targetAstProperty, JCOMBO_BOX_CLASS);
			}
		}
		setVariableIdentifier(javaInfoRoot, type, variable, field);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Parser
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void preCreate() throws Exception {
		BeanPropertyObserveInfo selectedElement = getSelectedItemProperty();
		selectedElement.setHostedType(getInputElementType());
	}

	@Override
	public void postDelete() throws Exception {
		super.postDelete();
		BeanPropertyObserveInfo selectedElement = getSelectedItemProperty();
		selectedElement.setHostedType(ClassGenericType.OBJECT_CLASS);
	}

	private BeanPropertyObserveInfo getSelectedItemProperty() throws Exception {
		return BeanSupport.getProperty(
				this,
				isJComboBoxBinding(m_target, m_targetProperty),
				"selectedItem");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	public static boolean isJComboBoxBinding(ObserveInfo observe, ObserveInfo propertyObserve) {
		return observe.getCreationType() == ObserveCreationType.JComboBoxBinding
				&& propertyObserve.getCreationType() == ObserveCreationType.SelfProperty;
	}
}