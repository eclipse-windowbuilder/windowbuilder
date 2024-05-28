/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swing.model.property.editor.border;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.clipboard.IClipboardSourceProvider;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDialogPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.string.StringPropertyEditor;
import org.eclipse.wb.internal.core.nls.model.INlsPropertyContributor;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jface.window.Window;

import java.util.List;

import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

/**
 * {@link PropertyEditor} for {@link Border}.
 *
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class BorderPropertyEditor extends TextDialogPropertyEditor
implements
INlsPropertyContributor,
IClipboardSourceProvider {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final PropertyEditor INSTANCE = new BorderPropertyEditor();

	private BorderPropertyEditor() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getText(Property property) throws Exception {
		Object value = property.getValue();
		if (value instanceof Border) {
			return CodeUtils.getShortClass(value.getClass().getName());
		}
		if (property.isModified() && value == null) {
			return "(no border)";
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IClipboardSourceProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getClipboardSource(GenericProperty property) throws Exception {
		Expression expression = property.getExpression();
		if (expression != null) {
			final String badExpressionMark = "__wbp_variableReference";
			// Ask "external" source.
			// Replace each reference on variable with "bad" mark.
			AstEditor editor = property.getJavaInfo().getEditor();
			String source = editor.getExternalSource(expression, input -> {
				IVariableBinding variableBinding = AstNodeUtils.getVariableBinding(input);
				if (variableBinding != null && !variableBinding.isField()) {
					return badExpressionMark;
				}
				return null;
			});
			// if source has variable references, fail
			if (source.contains(badExpressionMark)) {
				return null;
			}
			// OK, we have good external source
			return source;
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Editing
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void openDialog(Property property) throws Exception {
		GenericProperty genericProperty = (GenericProperty) property;
		BorderDialog borderDialog =
				new BorderDialog(DesignerPlugin.getShell(), genericProperty.getJavaInfo().getEditor());
		// set "modified" flag
		borderDialog.setBorderModified(property.isModified());
		// set initial value
		{
			Object value = property.getValue();
			if (value instanceof Border border) {
				borderDialog.setBorder(border);
			}
		}
		// open dialog
		if (borderDialog.open() == Window.OK) {
			String source = borderDialog.getBorderSource();
			genericProperty.setExpression(source, Property.UNKNOWN_VALUE);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// INlsPropertyContributor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void contributeNlsProperties(Property _property, List<Property> properties)
			throws Exception {
		GenericProperty property = (GenericProperty) _property;
		if (property.getValue() instanceof TitledBorder
				&& property.getExpression() instanceof ClassInstanceCreation) {
			ClassInstanceCreation borderCreation = (ClassInstanceCreation) property.getExpression();
			final JavaInfo javaInfo = property.getJavaInfo();
			for (final Expression argument : DomGenerics.arguments(borderCreation)) {
				if (AstNodeUtils.isSuccessorOf(argument, "java.lang.String")) {
					properties.add(new GenericProperty(javaInfo, property.getTitle() + "Title",
							StringPropertyEditor.INSTANCE) {
						@Override
						public Class<?> getType() {
							return String.class;
						}

						@Override
						public void setValue(Object value) throws Exception {
							if (value == UNKNOWN_VALUE) {
								setExpression(null, value);
							} else if (value instanceof String s) {
								setExpression(StringConverter.INSTANCE.toJavaSource(javaInfo, s), s);
							}
						}

						@Override
						public boolean isModified() throws Exception {
							return true;
						}

						@Override
						public void setExpression(final String source, Object value) throws Exception {
							ExecutionUtils.run(javaInfo, new RunnableEx() {
								@Override
								public void run() throws Exception {
									javaInfo.getEditor().replaceExpression(argument, source);
								}
							});
						}

						@Override
						public Object getValue() throws Exception {
							return JavaInfoEvaluationHelper.getValue(argument);
						}

						@Override
						public Expression getExpression() {
							return argument;
						}

						@Override
						public Object getDefaultValue() {
							return UNKNOWN_VALUE;
						}
					});
				}
			}
		}
	}
}
