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

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.parser.IModelResolver;
import org.eclipse.wb.internal.core.databinding.ui.editor.IPageListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.TabContainerConfiguration;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swing.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.swing.databinding.Messages;
import org.eclipse.wb.internal.swing.databinding.model.ObserveCreationType;
import org.eclipse.wb.internal.swing.databinding.model.ObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.beans.BeanPropertyObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.beans.BeanSupport;
import org.eclipse.wb.internal.swing.databinding.model.generic.ClassGenericType;
import org.eclipse.wb.internal.swing.databinding.model.generic.GenericUtils;
import org.eclipse.wb.internal.swing.databinding.model.generic.IGenericType;
import org.eclipse.wb.internal.swing.databinding.model.properties.ObjectPropertyInfo;
import org.eclipse.wb.internal.swing.databinding.model.properties.PropertyInfo;
import org.eclipse.wb.internal.swing.databinding.ui.contentproviders.EditableUiContentProvider;
import org.eclipse.wb.internal.swing.databinding.ui.contentproviders.IEditableProvider;
import org.eclipse.wb.internal.swing.databinding.ui.contentproviders.JTableColumnContainerUiContentProvider;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import org.apache.commons.lang.StringEscapeUtils;

import java.util.List;

/**
 * Model for {@link org.jdesktop.swingbinding.JTableBinding}.
 *
 * @author lobas_av
 * @coverage bindings.swing.model.bindings
 */
public final class JTableBindingInfo extends AutoBindingInfo implements IEditableProvider {
	private static final String SET_EDITABLE =
			"org.jdesktop.swingbinding.JTableBinding.setEditable(boolean)";
	private static final String ADD_COLUMN_BINDING_1 =
			"org.jdesktop.swingbinding.JTableBinding.addColumnBinding(org.jdesktop.beansbinding.Property)";
	private static final String ADD_COLUMN_BINDING_2 =
			"org.jdesktop.swingbinding.JTableBinding.addColumnBinding(org.jdesktop.beansbinding.Property,java.lang.String)";
	private static final String ADD_COLUMN_BINDING_3 =
			"org.jdesktop.swingbinding.JTableBinding.addColumnBinding(int,org.jdesktop.beansbinding.Property)";
	private static final String ADD_COLUMN_BINDING_4 =
			"org.jdesktop.swingbinding.JTableBinding.addColumnBinding(int,org.jdesktop.beansbinding.Property,java.lang.String)";
	//
	private static final IGenericType JTABLE_CLASS = new ClassGenericType(javax.swing.JTable.class,
			null,
			null);
	//
	private boolean m_editable = true;
	private List<ColumnBindingInfo> m_columns = Lists.newArrayList();

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public JTableBindingInfo(UpdateStrategyInfo strategyInfo,
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
	public boolean isEditable() {
		return m_editable;
	}

	public void setEditable(boolean editable) {
		m_editable = editable;
	}

	public List<ColumnBindingInfo> getColumns() {
		return m_columns;
	}

	public void setColumns(List<ColumnBindingInfo> columns, List<BindingInfo> bindings)
			throws Exception {
		int index = bindings.indexOf(this);
		if (index == -1) {
			m_columns = columns;
		} else {
			// remove old
			bindings.removeAll(m_columns);
			for (ColumnBindingInfo column : m_columns) {
				column.postDelete();
			}
			// add new
			m_columns = columns;
			if (!m_columns.isEmpty()) {
				bindings.addAll(index + 1, m_columns);
			}
		}
		// force create
		for (ColumnBindingInfo column : m_columns) {
			column.create(bindings);
		}
	}

	public IGenericType getInputElementType() {
		if (isJTableBinding(m_target, m_targetProperty)) {
			return m_modelProperty.getObjectType().getSubType(0);
		}
		return m_targetProperty.getObjectType().getSubType(0);
	}

	public String getTypeSourceCode(CodeGenerationSupport generationSupport) {
		// check generic
		if (!generationSupport.useGenerics()) {
			return "org.jdesktop.swingbinding.JTableBinding";
		}
		// calculate types
		IGenericType type0;
		IGenericType type1;
		if (isJTableBinding(m_target, m_targetProperty)) {
			if (m_modelAstProperty instanceof ObjectPropertyInfo) {
				type0 = m_model.getObjectType().getSubType(0);
				type1 = m_model.getObjectType();
			} else {
				type0 = m_modelProperty.getObjectType().getSubType(0);
				type1 = m_model.getObjectType();
			}
		} else {
			if (m_targetAstProperty instanceof ObjectPropertyInfo) {
				type0 = m_target.getObjectType().getSubType(0);
				type1 = m_target.getObjectType();
			} else {
				type0 = m_targetProperty.getObjectType().getSubType(0);
				type1 = m_target.getObjectType();
			}
		}
		// source code
		return "org.jdesktop.swingbinding.JTableBinding"
		+ GenericUtils.getTypesSource(type0, type1, JTABLE_CLASS);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Parser
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public AstObjectInfo parseExpression(AstEditor editor,
			String signature,
			MethodInvocation invocation,
			Expression[] arguments,
			IModelResolver resolver,
			IDatabindingsProvider provider) throws Exception {
		// JTableBinding.addColumnBinding(Property, [String])
		if (ADD_COLUMN_BINDING_1.equals(signature) || ADD_COLUMN_BINDING_2.equals(signature)) {
			createColumnBinding(editor, signature, invocation, arguments, -1, arguments[0], resolver);
			return null;
		}
		// JTableBinding.addColumnBinding(int, Property, [String])
		if (ADD_COLUMN_BINDING_3.equals(signature) || ADD_COLUMN_BINDING_4.equals(signature)) {
			int column = CoreUtils.evaluate(Integer.class, editor, arguments[0]);
			createColumnBinding(editor, signature, invocation, arguments, column, arguments[1], resolver);
			return null;
		}
		// JTableBinding.setEditable(boolean)
		if (SET_EDITABLE.equals(signature)) {
			m_editable = CoreUtils.evaluate(Boolean.class, editor, arguments[0]);
			return null;
		}
		return super.parseExpression(editor, signature, invocation, arguments, resolver, provider);
	}

	private void createColumnBinding(AstEditor editor,
			String signature,
			MethodInvocation invocation,
			Expression[] arguments,
			int column,
			Expression propertyExpression,
			IModelResolver resolver) throws Exception {
		ColumnBindingInfo binding = createColumnBinding(column);
		binding.setDetailProperty((PropertyInfo) resolver.getModel(propertyExpression));
		if (signature.endsWith(",java.lang.String)")) {
			String name = CoreUtils.evaluate(String.class, editor, arguments[arguments.length - 1]);
			binding.setName(StringEscapeUtils.unescapeJava(name));
		}
		m_columns.add(binding);
		resolver.addModelSupport(new ColumnBindingModelSupport(binding, invocation));
	}

	private ColumnBindingInfo createColumnBinding(int column) {
		ColumnBindingInfo binding;
		if (isJTableBinding(m_target, m_targetProperty)) {
			binding =
					new ColumnBindingInfo(m_target,
							m_targetProperty,
							m_targetAstProperty,
							m_model,
							m_modelProperty,
							m_modelAstProperty,
							this);
		} else {
			binding =
					new ColumnBindingInfo(m_model,
							m_modelProperty,
							m_modelAstProperty,
							m_target,
							m_targetProperty,
							m_targetAstProperty,
							this);
		}
		binding.setColumn(column);
		return binding;
	}

	public ColumnBindingInfo createNewColumnBinding(int column) {
		ColumnBindingInfo binding = createColumnBinding(column);
		binding.setDetailProperty(new ObjectPropertyInfo(getInputElementType()));
		binding.setColumnName(Messages.JTableBindingInfo_newColumn);
		return binding;
	}

	@Override
	public void create(List<BindingInfo> bindings) throws Exception {
		super.create(bindings);
		if (!m_columns.isEmpty()) {
			int index = bindings.indexOf(this) + 1;
			bindings.addAll(index, m_columns);
		}
		preCreate();
	}

	@Override
	public boolean delete(List<BindingInfo> bindings) throws Exception {
		for (ColumnBindingInfo column : m_columns) {
			column.postDelete();
		}
		bindings.removeAll(m_columns);
		return true;
	}

	@Override
	public void preCreate() throws Exception {
		BeanPropertyObserveInfo selectedElement = getSelectedElementProperty();
		selectedElement.setHostedType(getInputElementType());
	}

	@Override
	public void postDelete() throws Exception {
		super.postDelete();
		BeanPropertyObserveInfo selectedElement = getSelectedElementProperty();
		selectedElement.setHostedType(ClassGenericType.OBJECT_CLASS);
	}

	@Override
	public void move(List<BindingInfo> bindings) {
		if (!m_columns.isEmpty()) {
			bindings.removeAll(m_columns);
			int index = bindings.indexOf(this) + 1;
			bindings.addAll(index, m_columns);
		}
	}

	private BeanPropertyObserveInfo getSelectedElementProperty() throws Exception {
		return BeanSupport.getProperty(
				this,
				isJTableBinding(m_target, m_targetProperty),
				"selectedElement");
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
		super.createContentProviders(bindings, providers, listener, provider);
		providers.add(new EditableUiContentProvider(this));
		providers.add(new JTableColumnContainerUiContentProvider(createTabConfiguration(),
				this,
				bindings,
				provider));
	}

	private TabContainerConfiguration createTabConfiguration() {
		TabContainerConfiguration configuration = new TabContainerConfiguration();
		configuration.setUseAddButton(true);
		configuration.setUseRemoveButton(true);
		configuration.setUseUpDownButtons(true);
		configuration.setCreateEmptyPage(
				Messages.JTableBindingInfo_tabTitle,
				Messages.JTableBindingInfo_tabMessage);
		return configuration;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	//  Code generation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void addSourceCode(List<String> lines, CodeGenerationSupport generationSupport)
			throws Exception {
		if (isJTableBinding(m_target, m_targetProperty)) {
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
		// handle variable
		if (getVariableIdentifier() == null) {
			setVariableIdentifier(generationSupport.generateLocalName("JTableBinding"));
		}
		// begin
		StringBuffer line = new StringBuffer();
		boolean localVariable = !isField();
		if (localVariable) {
			line.append("org.jdesktop.swingbinding.JTableBinding");
		}
		if (modelAstProperty instanceof ObjectPropertyInfo) {
			if (localVariable) {
				if (generationSupport.useGenerics()) {
					line.append(GenericUtils.getTypesSource(
							model.getObjectType().getSubType(0),
							model.getObjectType(),
							JTABLE_CLASS));
				}
				line.append(" ");
			}
			line.append(getVariableIdentifier());
			line.append(" = org.jdesktop.swingbinding.SwingBindings.createJTableBinding(");
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
							JTABLE_CLASS));
				}
				line.append(" ");
			}
			line.append(getVariableIdentifier());
			line.append(" = org.jdesktop.swingbinding.SwingBindings.createJTableBinding(");
			line.append(m_strategyInfo.getStrategySourceCode());
			line.append(", ");
			line.append(model.getReference());
			line.append(", ");
			line.append(modelAstProperty.getVariableIdentifier());
			line.append(", ");
			line.append(widget.getReference());
		}
		// end
		line.append(getCreateMethodHeaderEnd());
		line.append(";");
		lines.add(line.toString());
		// columns
		for (ColumnBindingInfo column : m_columns) {
			lines.add("//");
			column.addColumnSourceCode(lines, generationSupport);
		}
		if (!m_columns.isEmpty()) {
			lines.add("//");
		}
		// editable
		if (!m_editable) {
			lines.add(getVariableIdentifier() + ".setEditable(false);");
		}
		// converter & validator
		addFinishSourceCode(lines, generationSupport, true);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IASTObjectInfo2
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void setVariableIdentifier(JavaInfo javaInfoRoot, String variable, boolean field) {
		String type = "org.jdesktop.swingbinding.JTableBinding";
		if (CoreUtils.useGenerics(javaInfoRoot.getEditor().getJavaProject())) {
			if (isJTableBinding(m_target, m_targetProperty)) {
				type += getTypeSource(m_model, m_modelProperty, m_modelAstProperty, JTABLE_CLASS);
			} else {
				type += getTypeSource(m_target, m_targetProperty, m_targetAstProperty, JTABLE_CLASS);
			}
		}
		setVariableIdentifier(javaInfoRoot, type, variable, field);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private static boolean isJTableBinding(ObserveInfo observe, ObserveInfo propertyObserve) {
		return observe.getCreationType() == ObserveCreationType.JTableBinding
				&& propertyObserve.getCreationType() == ObserveCreationType.SelfProperty;
	}
}