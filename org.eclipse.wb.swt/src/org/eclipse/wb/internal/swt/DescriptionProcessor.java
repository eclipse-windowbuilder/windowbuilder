/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.internal.swt;

import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.ConstructorDescription;
import org.eclipse.wb.internal.core.model.description.CreationDescription;
import org.eclipse.wb.internal.core.model.description.IDescriptionProcessor;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.property.editor.DisplayExpressionPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.style.StylePropertyEditor;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstParser;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.ui.ImageUtils;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

import org.apache.commons.lang3.StringUtils;

import java.beans.BeanInfo;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Implementation of {@link IDescriptionProcessor} for RCP.
 * <p>
 * It analyzes {@link ConstructorDescription}'s and treats <code>Composite, int</code> parameters as
 * parent and style.
 *
 * @author scheglov_ke
 * @coverage swt
 */
public final class DescriptionProcessor implements IDescriptionProcessor {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final IDescriptionProcessor INSTANCE = new DescriptionProcessor();

	private DescriptionProcessor() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IDescriptionProcessor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void process(final AstEditor editor, final ComponentDescription componentDescription) {
		ExecutionUtils.runIgnore(new RunnableEx() {
			@Override
			public void run() throws Exception {
				new DescriptionProcessor_Single(editor, componentDescription);
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Component processor
	//
	////////////////////////////////////////////////////////////////////////////
	private static class DescriptionProcessor_Single {
		private final ComponentDescription componentDescription;
		private final EditorState editorState;
		private final ClassLoader classLoader;
		private final Class<?> componentClass;
		private final Class<?> compositeClass;
		private final AstEditor editor;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public DescriptionProcessor_Single(AstEditor editor, ComponentDescription componentDescription)
				throws Exception {
			this.editor = editor;
			this.componentDescription = componentDescription;
			componentClass = componentDescription.getComponentClass();
			editorState = EditorState.get(editor);
			classLoader = editorState.getEditorLoader();
			compositeClass = classLoader.loadClass("org.eclipse.swt.widgets.Composite");
			configureConstructors();
			configureDefaultCreation();
			markCreateMethodsAsExecutable();
			ifHasNoLayout_then_setLayout_isNotAssociation();
			configureIconFromBeanInfo();
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Configure constructor
		//
		////////////////////////////////////////////////////////////////////////////
		private void configureConstructors() throws Exception {
			for (ConstructorDescription constructor : componentDescription.getConstructors()) {
				List<ParameterDescription> parameters = constructor.getParameters();
				// check for <init>(Composite[, *])
				if (parameters.size() >= 1) {
					ParameterDescription parameter_0 = parameters.get(0);
					if (isComposite(parameter_0.getType())) {
						markFirstParameterAsParent(parameter_0);
						// check for <init>(Composite, int[, *])
						if (parameters.size() >= 2) {
							ParameterDescription parameter_1 = parameters.get(1);
							if (parameter_1.getType() == int.class) {
								ensureStylePropertyEditor(parameter_1);
							}
						}
					}
				}
			}
		}

		/**
		 * Ensures that given integer {@link ParameterDescription} has {@link StylePropertyEditor}. If
		 * needed, uses {@link StylePropertyEditor} from superclass constructor.
		 */
		private void ensureStylePropertyEditor(ParameterDescription parameter) throws Exception {
			boolean hasSpecialPropertyEditor =
					parameter.getEditor() instanceof StylePropertyEditor
					|| parameter.getEditor() instanceof DisplayExpressionPropertyEditor;
			if (!hasSpecialPropertyEditor) {
				hasSpecialPropertyEditor = configureStylePropertyEditor_fromBeanInfo(parameter);
			}
			if (!hasSpecialPropertyEditor) {
				ComponentDescription superComponentDescription =
						ComponentDescriptionHelper.getDescription(editor, componentClass.getSuperclass());
				for (ConstructorDescription constructor : superComponentDescription.getConstructors()) {
					List<ParameterDescription> parameters = constructor.getParameters();
					if (parameters.size() >= 2) {
						ParameterDescription parameter_0 = parameters.get(0);
						ParameterDescription parameter_1 = parameters.get(1);
						if (isComposite(parameter_0.getType())
								&& parameter_1.getType() == int.class
								&& parameter_1.getEditor() instanceof StylePropertyEditor) {
							parameter.setEditor(parameter_1.getEditor());
							parameter.setDefaultSource(parameter_1.getDefaultSource());
							break;
						}
					}
				}
			}
		}

		private boolean configureStylePropertyEditor_fromBeanInfo(ParameterDescription parameter)
				throws Exception {
			Object styleObject =
					componentDescription.getBeanDescriptor().getValue("org.eclipse.wb.swt.style");
			if (styleObject instanceof String[][]) {
				Map<String, Object> parameters = new TreeMap<>();
				parameters.put("class", "org.eclipse.swt.SWT");
				// fill parameters
				int selectIndex = 0;
				String[][] lines = (String[][]) styleObject;
				for (String[] line : lines) {
					// set
					if (line.length >= 3 && line[0].equals("set") && line[1] == null) {
						String setString = "";
						for (int i = 2; i < line.length; i++) {
							setString += " ";
							setString += line[i].toUpperCase(Locale.ENGLISH);
						}
						parameters.put("set", setString.trim());
					}
					// select
					if (line.length >= 5 && line[0].equals("select") && line[3] == null) {
						String title = line[1];
						String defaultField = line[2].toUpperCase(Locale.ENGLISH);
						String selectString = title + " " + defaultField;
						for (int i = 4; i < line.length; i++) {
							selectString += " ";
							selectString += line[i].toUpperCase(Locale.ENGLISH);
						}
						parameters.put("select" + selectIndex, selectString.trim());
						selectIndex++;
					}
				}
				// set StylePropertyEditor
				StylePropertyEditor stylePropertyEditor = new StylePropertyEditor();
				stylePropertyEditor.configure(editorState, parameters);
				parameter.setEditor(stylePropertyEditor);
				return true;
			}
			return false;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Configure default creation
		//
		////////////////////////////////////////////////////////////////////////////
		private void configureDefaultCreation() {
			Constructor<?> constructor = ReflectionUtils.getShortestConstructor(componentClass);
			if (constructor != null) {
				String source =
						"new " + componentClass.getName() + "(" + getConstructorArguments(constructor) + ")";
				CreationDescription creationDefault =
						new CreationDescription(componentDescription, null, null);
				creationDefault.setSource(source);
				componentDescription.setCreationDefault(creationDefault);
			}
		}

		private String getConstructorArguments(Constructor<?> constructor) {
			String arguments = "";
			Class<?>[] parameters = constructor.getParameterTypes();
			boolean wasComposite = false;
			for (int i = 0; i < parameters.length; i++) {
				Class<?> parameter = parameters[i];
				if (i == 0 && isComposite(parameter)) {
					arguments += "%parent%";
					wasComposite = true;
				} else if (i == 1 && parameter == int.class && wasComposite) {
					arguments += "org.eclipse.swt.SWT.NONE";
				} else {
					arguments += AstParser.getDefaultValue(parameter.getCanonicalName());
				}
				arguments += ", ";
			}
			arguments = StringUtils.removeEnd(arguments, ", ");
			return arguments;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Mark createX(Composite[, *]) as executable
		//
		////////////////////////////////////////////////////////////////////////////
		private void markCreateMethodsAsExecutable() throws Exception {
			Collection<Method> methods = ReflectionUtils.getMethods(componentClass).values();
			for (Method method : methods) {
				if (ReflectionUtils.isPrivate(method)) {
					continue;
				}
				if (method.getName().startsWith("create")) {
					Class<?>[] parameterTypes = method.getParameterTypes();
					if (parameterTypes.length != 0 && isComposite(parameterTypes[0])) {
						componentDescription.addMethod(method);
					}
				}
			}
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Other
		//
		////////////////////////////////////////////////////////////////////////////
		/**
		 * If {@link Composite} marked as "no layout", then {@link Composite#setLayout(Layout)} should
		 * not be association and should not be executable.
		 */
		private void ifHasNoLayout_then_setLayout_isNotAssociation() {
			if (!isComposite(componentClass)) {
				return;
			}
			if (!Objects.equals(componentDescription.getParameter("layout.has"), "false")) {
				return;
			}
			// remove setLayout() method
			List<MethodDescription> methods = componentDescription.getMethods();
			for (MethodDescription methodDescription : methods) {
				if (methodDescription.getSignature().equals("setLayout(org.eclipse.swt.widgets.Layout)")) {
					methodDescription.getParameter(0).setChild(false);
					break;
				}
			}
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Icon
		//
		////////////////////////////////////////////////////////////////////////////
		private void configureIconFromBeanInfo() throws Exception {
			BeanInfo beanInfo = componentDescription.getBeanInfo();
			if (beanInfo != null) {
				java.awt.Image awtIcon = beanInfo.getIcon(BeanInfo.ICON_COLOR_16x16);
				if (awtIcon != null) {
					componentDescription.setIcon(ImageUtils.convertToSWT(awtIcon));
				}
			}
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Utils
		//
		////////////////////////////////////////////////////////////////////////////
		private boolean isComposite(Class<?> type) {
			return compositeClass.isAssignableFrom(type);
		}
	}

	/**
	 * If not disabled, marks given {@link ParameterDescription} as parent.
	 */
	private static void markFirstParameterAsParent(ParameterDescription parameter) {
		// check for disabling this default marking as parent
		if ("false".equals(parameter.getTag("parent"))) {
			return;
		}
		// OK, mark as parent
		parameter.setParent(true);
	}
}
