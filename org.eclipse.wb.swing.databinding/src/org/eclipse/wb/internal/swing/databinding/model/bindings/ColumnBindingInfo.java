/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
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
import org.eclipse.wb.internal.core.databinding.model.AstObjectInfoVisitor;
import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.parser.IModelResolver;
import org.eclipse.wb.internal.core.databinding.ui.editor.IPageListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.BindingContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.LabelUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.SeparatorUiContentProvider;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.utils.StringUtilities;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.swing.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.swing.databinding.Messages;
import org.eclipse.wb.internal.swing.databinding.model.ObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.generic.ClassGenericType;
import org.eclipse.wb.internal.swing.databinding.model.generic.GenericUtils;
import org.eclipse.wb.internal.swing.databinding.model.generic.IGenericType;
import org.eclipse.wb.internal.swing.databinding.model.properties.PropertyInfo;
import org.eclipse.wb.internal.swing.databinding.ui.contentproviders.BindingNameUiContentProvider;
import org.eclipse.wb.internal.swing.databinding.ui.contentproviders.ColumnBindingUiContentProvider;
import org.eclipse.wb.internal.swing.databinding.ui.contentproviders.ColumnClassUiContentProvider;
import org.eclipse.wb.internal.swing.databinding.ui.contentproviders.ColumnNameUiContentProvider;
import org.eclipse.wb.internal.swing.databinding.ui.contentproviders.ConverterUiContentProvider;
import org.eclipse.wb.internal.swing.databinding.ui.contentproviders.EditableUiContentProvider;
import org.eclipse.wb.internal.swing.databinding.ui.contentproviders.IEditableProvider;
import org.eclipse.wb.internal.swing.databinding.ui.contentproviders.ValidatorUiContentProvider;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeLiteral;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import java.util.List;

/**
 * Model for {@link org.jdesktop.swingbinding.JTableBinding.ColumnBinding}.
 *
 * @author lobas_av
 * @coverage bindings.swing.model.bindings
 */
public class ColumnBindingInfo extends BindingInfo implements IEditableProvider {
	private static final String SET_EDITABLE =
			"org.jdesktop.swingbinding.JTableBinding.ColumnBinding.setEditable(boolean)";
	private static final String SET_COLUMN_NAME =
			"org.jdesktop.swingbinding.JTableBinding.ColumnBinding.setColumnName(java.lang.String)";
	private static final String SET_COLUMN_CLASS =
			"org.jdesktop.swingbinding.JTableBinding.ColumnBinding.setColumnClass(java.lang.Class)";
	private static ChooseClassConfiguration m_columnClassConfiguration;
	private final JTableBindingInfo m_binding;
	private PropertyInfo m_detailProperty;
	private boolean m_editable = true;
	private int m_column = -1;
	private String m_columnName;
	private IGenericType m_columnType;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ColumnBindingInfo(ObserveInfo target,
			ObserveInfo targetProperty,
			PropertyInfo targetAstProperty,
			ObserveInfo model,
			ObserveInfo modelProperty,
			PropertyInfo modelAstProperty,
			JTableBindingInfo binding) {
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
		return m_columnType == null ? ClassGenericType.OBJECT_CLASS : m_columnType;
	}

	@Override
	public boolean isEditable() {
		return m_editable;
	}

	@Override
	public void setEditable(boolean editable) {
		m_editable = editable;
	}

	public int getColumn() {
		return m_column;
	}

	public void setColumn(int column) {
		m_column = column;
	}

	public String getColumnName() {
		return m_columnName;
	}

	public void setColumnName(String columnName) {
		m_columnName = columnName;
	}

	public IGenericType getColumnType() {
		return m_columnType;
	}

	public void setColumnType(IGenericType columnType) {
		m_columnType = columnType;
	}

	public PropertyInfo getDetailProperty() {
		return m_detailProperty;
	}

	public void setDetailProperty(PropertyInfo detailProperty) {
		Assert.isNotNull(detailProperty);
		m_detailProperty = detailProperty;
	}

	public JTableBindingInfo getJTableBinding() {
		return m_binding;
	}

	@Override
	public boolean delete(List<BindingInfo> bindings) throws Exception {
		m_binding.getColumns().remove(this);
		return true;
	}

	@Override
	public void move(List<BindingInfo> bindings) {
		m_binding.getColumns().remove(this);
		int index = bindings.indexOf(this) - bindings.indexOf(m_binding) - 1;
		m_binding.getColumns().add(index, this);
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
		listener.setTitle(Messages.ColumnBindingInfo_listenerTitle);
		listener.setMessage(Messages.ColumnBindingInfo_listenerMessage);
		// add target
		providers.add(new LabelUiContentProvider(Messages.ColumnBindingInfo_componentLabel,
				m_target.getPresentation().getTextForBinding()));
		// add model
		providers.add(new LabelUiContentProvider(Messages.ColumnBindingInfo_modelLabel,
				getModelPresentationText(false)));
		providers.add(new ColumnBindingUiContentProvider(this));
		// binding self properties
		providers.add(new SeparatorUiContentProvider());
		providers.add(new ColumnNameUiContentProvider(this));
		providers.add(new EditableUiContentProvider(this));
		providers.add(new ColumnClassUiContentProvider(createColumnConfiguration(), this));
		providers.add(new ConverterUiContentProvider(createConverterConfiguration(), this));
		providers.add(new ValidatorUiContentProvider(createValidatorConfiguration(), this));
		providers.add(new BindingNameUiContentProvider(this));
		providers.add(new BindingContentProvider(this, provider.getJavaInfoRoot()));
	}

	private static ChooseClassConfiguration createColumnConfiguration() {
		if (m_columnClassConfiguration == null) {
			m_columnClassConfiguration = new ChooseClassConfiguration();
			m_columnClassConfiguration.setDialogFieldLabel(Messages.ColumnBindingInfo_chooseTitle);
			m_columnClassConfiguration.setValueScope("beans");
			m_columnClassConfiguration.setClearValue("N/S");
			m_columnClassConfiguration.setChooseInterfaces(true);
			m_columnClassConfiguration.setEmptyClassErrorMessage(Messages.ColumnBindingInfo_chooseError);
			m_columnClassConfiguration.setErrorMessagePrefix(Messages.ColumnBindingInfo_chooseErrorPrefix);
		}
		return m_columnClassConfiguration;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Parse
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public AstObjectInfo parseExpression(AstEditor editor,
			String signature,
			MethodInvocation invocation,
			Expression[] arguments,
			IModelResolver resolver,
			IDatabindingsProvider provider) throws Exception {
		// JTableBinding.ColumnBinding.setEditable(boolean)
		if (SET_EDITABLE.equals(signature)) {
			m_editable = CoreUtils.evaluate(Boolean.class, editor, arguments[0]);
			configureModelSupport(invocation, resolver);
			return null;
		}
		// JTableBinding.ColumnBinding.setColumnName(String)
		if (SET_COLUMN_NAME.equals(signature)) {
			m_columnName =
					StringEscapeUtils.unescapeJava(CoreUtils.evaluate(String.class, editor, arguments[0]));
			configureModelSupport(invocation, resolver);
			return null;
		}
		// JTableBinding.ColumnBinding.setColumnClass(Class)
		if (SET_COLUMN_CLASS.equals(signature)) {
			if (arguments[0] instanceof TypeLiteral typeLiteral) {
				ITypeBinding binding = AstNodeUtils.getTypeBinding(typeLiteral.getType());
				if (binding != null) {
					m_columnType = GenericUtils.getObjectType(editor, binding);
				}
			} else {
				m_columnType =
						new ClassGenericType(CoreUtils.evaluate(Class.class, editor, arguments[0]), null, null);
			}
			configureModelSupport(invocation, resolver);
			return null;
		}
		return super.parseExpression(editor, signature, invocation, arguments, resolver, provider);
	}

	private void configureModelSupport(MethodInvocation invocation, IModelResolver resolver)
			throws Exception {
		ColumnBindingModelSupport modelSupport =
				(ColumnBindingModelSupport) resolver.getModelSupport(invocation.getExpression());
		Assert.isNotNull(modelSupport);
		Assert.isTrue(this == modelSupport.getModel());
		modelSupport.addInvocation(invocation);
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

	public void addColumnSourceCode(List<String> lines, CodeGenerationSupport generationSupport)
			throws Exception {
		// detail
		generationSupport.addSourceCode(m_detailProperty, lines);
		// handle variable
		if (getVariableIdentifier() == null) {
			if (m_converter == null && m_validator == null) {
				StringBuffer line = new StringBuffer();
				line.append(m_binding.getVariableIdentifier()
						+ ".addColumnBinding("
						+ m_detailProperty.getVariableIdentifier()
						+ getCreateMethodHeaderEnd());
				if (!StringUtils.isEmpty(m_columnName)) {
					line.append(".setColumnName(\"" + StringUtilities.escapeJava(m_columnName) + "\")");
				}
				if (!m_editable) {
					line.append(".setEditable(false)");
				}
				if (m_columnType != null) {
					line.append(".setColumnClass(" + m_columnType.getFullTypeName() + ".class)");
				}
				line.append(";");
				lines.add(line.toString());
				return;
			}
			//
			setVariableIdentifier(generationSupport.generateLocalName("columnBinding"));
		}
		// begin
		if (!isField()) {
			lines.add(m_binding.getTypeSourceCode(generationSupport) + ".ColumnBinding ");
		}
		lines.add(getVariableIdentifier()
				+ " = "
				+ m_binding.getVariableIdentifier()
				+ ".addColumnBinding("
				+ m_detailProperty.getVariableIdentifier()
				+ getCreateMethodHeaderEnd()
				+ ";");
		// column name
		if (!StringUtils.isEmpty(m_columnName)) {
			lines.add(getVariableIdentifier()
					+ ".setColumnName(\""
					+ StringUtilities.escapeJava(m_columnName)
					+ "\");");
		}
		// editable
		if (!m_editable) {
			lines.add(getVariableIdentifier() + ".setEditable(false);");
		}
		// column class
		if (m_columnType != null) {
			lines.add(getVariableIdentifier()
					+ ".setColumnClass("
					+ m_columnType.getFullTypeName()
					+ ".class);");
		}
		// converter & validator
		addFinishSourceCode(lines, generationSupport, false);
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
		String type = m_binding.getTypeSourceCode(generationSupport) + ".ColumnBinding";
		setVariableIdentifier(javaInfoRoot, type, variable, field);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getTargetPresentationText(boolean full) throws Exception {
		return m_target.getPresentation().getTextForBinding()
				+ ".column"
				+ CoreUtils.getDefaultString(m_columnName, " - ", "");
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