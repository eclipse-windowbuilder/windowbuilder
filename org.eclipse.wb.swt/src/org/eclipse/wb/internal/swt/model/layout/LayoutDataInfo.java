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
package org.eclipse.wb.internal.swt.model.layout;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.core.model.broadcast.JavaInfoAddProperties;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoTreeComplete;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.generation.GenerationSettings;
import org.eclipse.wb.internal.core.model.generation.statement.block.BlockStatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.LocalUniqueVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowData;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Model for layout data object, for example {@link GridData}, {@link RowData}, etc.
 *
 * @author scheglov_ke
 * @author lobas_av
 * @coverage swt.model.layout
 */
public abstract class LayoutDataInfo extends JavaInfo implements ILayoutDataInfo {
	final LayoutDataInfo m_this = this;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public LayoutDataInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
		removeIfCompositeHasNoLayout();
		contributeLayoutDataProperties_toControl();
		addMaterializeSupport();
		turnIntoBlock_whenEnsureVariable();
		new LayoutDataNameSupport(this);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Broadcast events
	//
	////////////////////////////////////////////////////////////////////////////
	private void removeIfCompositeHasNoLayout() {
		addBroadcastListener(new ObjectInfoTreeComplete() {
			@Override
			public void invoke() throws Exception {
				removeBroadcastListener(this);
				// if dangling LayoutData, ignore it
				if (getParent() == null) {
					return;
				}
				if (!getParent().getChildren().contains(m_this)) {
					return;
				}
				// if no parent with Layout, remove this LayoutData
				if (!hasCompositeLayout()) {
					getParent().removeChild(m_this);
				}
			}

			private boolean hasCompositeLayout() {
				ObjectInfo composite = getParent().getParent();
				if (composite != null && wantsLayoutData_forChildChild(composite.getParent())) {
					return true;
				}
				if (composite instanceof CompositeInfo) {
					List<LayoutInfo> layouts = composite.getChildren(LayoutInfo.class);
					if (layouts.isEmpty()) {
						return false;
					}
					return isCompatibleWithLayout(layouts.get(0));
				}
				return false;
			}

			private boolean wantsLayoutData_forChildChild(ObjectInfo object) {
				if (object != null) {
					List<JavaInfo> children = object.getChildren(JavaInfo.class);
					for (JavaInfo child : children) {
						if (JavaInfoUtils.hasTrueParameter(child, "layout.managesChildChild")) {
							return true;
						}
					}
				}
				return false;
			}
		});
	}

	/**
	 * @return <code>true</code> if this {@link LayoutDataInfo} with given {@link LayoutInfo}.
	 */
	protected boolean isCompatibleWithLayout(LayoutInfo layout) {
		{
			String compatibleLayout = JavaInfoUtils.getParameter(this, "layoutData.compatibleLayout");
			if (compatibleLayout != null) {
				return ReflectionUtils.isSuccessorOf(
						layout.getDescription().getComponentClass(),
						compatibleLayout);
			}
		}
		return true;
	}

	/**
	 * Contribute "LayoutData" complex property to our {@link ControlInfo}.
	 */
	private void contributeLayoutDataProperties_toControl() {
		addBroadcastListener(new JavaInfoAddProperties() {
			@Override
			public void invoke(JavaInfo javaInfo, List<Property> properties) throws Exception {
				if (isActiveForControl(javaInfo)) {
					addLayoutDataProperties(properties);
				}
			}

			private boolean isActiveForControl(JavaInfo control) {
				return control.getChildren().contains(m_this);
			}
		});
	}

	/**
	 * When we set value of property for virtual {@link LayoutDataInfo}, often we want to set this
	 * value in constructor. But constructor does not exists yet, because our {@link LayoutDataInfo}
	 * is virtual. So virtual {@link CreationSupport} adds no any new {@link ExpressionAccessor}. This
	 * method adds the listener which forces the {@link LayoutDataInfo} materialization, so
	 * {@link VariableSupport} has a chance to replace the {@link CreationSupport} instance for
	 * underlying {@link JavaInfo}.
	 */
	private void addMaterializeSupport() {
		addBroadcastListener(new JavaEventListener() {
			@Override
			public void setPropertyExpression(GenericPropertyImpl property,
					String[] source,
					Object[] value,
					boolean[] shouldSet) throws Exception {
				if (property.getJavaInfo() == m_this) {
					materialize();
				}
			}
		});
	}

	protected final void materialize() throws Exception {
		if (isVirtual()) {
			((VirtualLayoutDataVariableSupport) getVariableSupport()).materialize();
		}
	}

	/**
	 * @return <code>true</code> if this {@link LayoutDataInfo} is virtual.
	 */
	private boolean isVirtual() {
		return getVariableSupport() instanceof VirtualLayoutDataVariableSupport;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Code generation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Performs following optimizations:
	 * <ul>
	 * <li>When <code>LayoutData</code> gets converted from simple
	 * <code>setLayoutData(new SomeData())</code> into real variable, wrap its {@link Statement}s with
	 * {@link Block}.</li>
	 * <li>When <code>LayoutData</code> has {@link LocalUniqueVariableSupport}, but it is used just to
	 * call <code>setLayoutData()</code>, then variable may be inlined, and enclosing {@link Block}
	 * probably too.</li>
	 * <li>When <code>LayoutData</code> has all default values, then we can delete it at all.</li>
	 * </ul>
	 */
	private void turnIntoBlock_whenEnsureVariable() {
		// empty -> Block
		addBroadcastListener(new JavaEventListener() {
			@Override
			public void variable_emptyMaterializeBefore(EmptyVariableSupport variableSupport)
					throws Exception {
				if (variableSupport.getJavaInfo() == m_this && isBlockMode()) {
					ASTNode creationNode = variableSupport.getInitializer();
					Statement creationStatement = AstNodeUtils.getEnclosingStatement(creationNode);
					getEditor().encloseInBlock(creationStatement);
				}
			}

			private boolean isBlockMode() {
				GenerationSettings settings = getDescription().getToolkit().getGenerationSettings();
				return settings.getStatement() == BlockStatementGeneratorDescription.INSTANCE;
			}
		});
		// no invocations/fields -> inline Block
		addBroadcastListener(new ObjectEventListener() {
			@Override
			public void endEdit_aboutToRefresh() throws Exception {
				if (getVariableSupport() instanceof LocalUniqueVariableSupport) {
					LocalUniqueVariableSupport variableSupport =
							(LocalUniqueVariableSupport) getVariableSupport();
					if (variableSupport.canInline()) {
						variableSupport.inline();
						inlineBlockIfSingleStatement();
					}
				}
			}

			private void inlineBlockIfSingleStatement() throws Exception {
				ASTNode node = ((EmptyVariableSupport) getVariableSupport()).getInitializer();
				Statement statement = AstNodeUtils.getEnclosingStatement(node);
				if (statement != null) {
					Block block = (Block) statement.getParent();
					if (block.statements().size() == 1) {
						getEditor().inlineBlock(block);
					}
				}
			}
		});
		// is default -> delete
		addBroadcastListener(new ObjectEventListener() {
			@Override
			public void endEdit_aboutToRefresh() throws Exception {
				if (!isDeleted()
						&& getCreationSupport() instanceof ConstructorCreationSupport
						&& getMethodInvocations().isEmpty()
						&& getFieldAssignments().isEmpty()) {
					ConstructorCreationSupport creationSupport =
							(ConstructorCreationSupport) getCreationSupport();
					ClassInstanceCreation creation = creationSupport.getCreation();
					String signature = creationSupport.getDescription().getSignature();
					// prepare arguments
					List<Expression> arguments = DomGenerics.arguments(creation);
					if (!AstNodeUtils.areLiterals(arguments)) {
						return;
					}
					// evaluate arguments
					List<Object> argumentValues;
					{
						EditorState state = JavaInfoUtils.getState(m_this);
						EvaluationContext context =
								new EvaluationContext(state.getEditorLoader(), state.getFlowDescription());
						argumentValues = new ArrayList<>();
						for (Expression argument : arguments) {
							Object value = AstEvaluationEngine.evaluate(context, argument);
							JavaInfoEvaluationHelper.setValue(argument, value);
							argumentValues.add(value);
						}
					}
					// delete, if default constructor arguments
					if (isDefault(signature, argumentValues)) {
						delete();
					}
				}
			}

			/**
			 * @return <code>true</code> if existing <code>isDefault</code> script says that object of
			 *         this {@link LayoutDataInfo} is in default state.
			 */
			private boolean isDefault(String signature, List<Object> args) throws Exception {
				String script = JavaInfoUtils.getParameter(m_this, "isDefault");
				if (script != null) {
					Map<String, Object> variables = Map.of("signature", signature, "args", args);
					return (Boolean) ScriptUtils.evaluate(
							JavaInfoUtils.getClassLoader(m_this),
							script,
							variables);
				}
				return false;
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// "LayoutData" property
	//
	////////////////////////////////////////////////////////////////////////////
	private ComplexProperty m_complexProperty;

	/**
	 * Adds properties of this {@link LayoutDataInfo} to the properties of its {@link ControlInfo}.
	 */
	private void addLayoutDataProperties(List<Property> properties) throws Exception {
		// prepare complex property
		if (m_complexProperty == null) {
			String text;
			{
				Class<?> componentClass = getDescription().getComponentClass();
				text = "(" + componentClass.getName() + ")";
			}
			// prepare ComplexProperty
			m_complexProperty = new ComplexProperty("LayoutData", text) {
				@Override
				public boolean isModified() throws Exception {
					return true;
				}

				@Override
				public void setValue(Object value) throws Exception {
					if (value == UNKNOWN_VALUE) {
						delete();
					}
				}
			};
			m_complexProperty.setCategory(PropertyCategory.system(5));
			// set sub-properties
			m_complexProperty.setProperties(getFilteredProperties());
		}
		// add property
		properties.add(m_complexProperty);
	}

	/**
	 * @return the {@link Property}'s to show in complex property of {@link LayoutDataInfo} parent.
	 */
	private Property[] getFilteredProperties() throws Exception {
		Property[] properties = getProperties();
		// For some layout data it needs to exclude some properties, such as "Class" or "Constructor".
		// This can be done using "layoutData.exclude-properties" parameter of class description.
		String propertiesExcludeString =
				JavaInfoUtils.getParameter(this, "layoutData.exclude-properties");
		if (propertiesExcludeString != null) {
			List<Property> filteredProperties = new ArrayList<>();
			String[] propertiesExclude = StringUtils.split(propertiesExcludeString);
			props : for (Property property : properties) {
				for (String propertyExclude : propertiesExclude) {
					if (property.getTitle().equals(propertyExclude)) {
						continue props;
					}
				}
				filteredProperties.add(property);
			}
			properties = filteredProperties.toArray(new Property[filteredProperties.size()]);
		}
		return properties;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final IObjectPresentation getPresentation() {
		return new LayoutDataPresentation(this);
	}
}