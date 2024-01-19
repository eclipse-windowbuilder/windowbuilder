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
package org.eclipse.wb.internal.swing.databinding.wizards.autobindings;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassAndPropertiesConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.PropertyAdapter;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.databinding.wizards.autobindings.DefaultAutomaticDatabindingProvider;
import org.eclipse.wb.internal.core.databinding.wizards.autobindings.DescriptorContainer;
import org.eclipse.wb.internal.core.databinding.wizards.autobindings.IAutomaticDatabindingProvider;
import org.eclipse.wb.internal.core.databinding.wizards.autobindings.IImageLoader;
import org.eclipse.wb.internal.core.model.generation.statement.StatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.variable.FieldUniqueVariableSupport;
import org.eclipse.wb.internal.core.model.variable.description.VariableSupportDescription;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.SwingToolkitDescription;
import org.eclipse.wb.internal.swing.ToolkitProvider;
import org.eclipse.wb.internal.swing.databinding.Activator;
import org.eclipse.wb.internal.swing.databinding.model.DataBindingsCodeUtils;
import org.eclipse.wb.internal.swing.databinding.model.ObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.beans.BeanSupport;
import org.eclipse.wb.internal.swing.databinding.model.generic.ClassGenericType;
import org.eclipse.wb.internal.swing.databinding.model.generic.GenericUtils;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage.ImportsManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Provider for support Swing bindings API.
 *
 * @author lobas_av
 * @coverage bindings.swing.wizard.auto
 */
public final class SwingDatabindingProvider extends DefaultAutomaticDatabindingProvider {
	private static DescriptorContainer m_componentContainer;
	private static DescriptorContainer m_strategyContainer;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public static IAutomaticDatabindingProvider create() {
		try {
			// check containers
			if (m_componentContainer == null && m_strategyContainer == null) {
				// load containers
				InputStream stream = Activator.getFile("templates/SwingEditors.xml");
				Map<String, DescriptorContainer> containers =
						DescriptorContainer.parseDescriptors(
								stream,
								SwingDatabindingProvider.class.getClassLoader(),
								new IImageLoader() {
									@Override
									public Image getImage(String name) {
										return Activator.getImage(name);
									}
								});
				IOUtils.closeQuietly(stream);
				// sets containers
				m_componentContainer = containers.get("Swing.Components");
				m_strategyContainer = containers.get("AutoBinding.UpdateStrategy");
			}
			// create provider
			return new SwingDatabindingProvider(m_componentContainer, m_strategyContainer);
		} catch (Throwable e) {
			DesignerPlugin.log(e);
			return null;
		}
	}

	private SwingDatabindingProvider(DescriptorContainer widgetContainer,
			DescriptorContainer strategyContainer) {
		super(widgetContainer, strategyContainer);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// SuperClass
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String[] getSuperClasses() {
		return new String[]{"javax.swing.JPanel", "javax.swing.JDialog", "javax.swing.JFrame"};
	}

	@Override
	public String getInitialSuperClass() {
		return "javax.swing.JPanel";
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// GUI
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void configure(ChooseClassAndPropertiesConfiguration configuration) {
		configuration.setPropertiesLabelProvider(new ObservePropertyAdapterLabelProvider());
		configuration.setValueScope("beans");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected List<PropertyAdapter> getProperties0(Class<?> choosenClass) throws Exception {
		List<PropertyAdapter> adapters = new ArrayList<>();
		BeanSupport beanSupport = new BeanSupport();
		beanSupport.doAddELProperty(false);
		beanSupport.doAddSelfProperty(false);
		ClassGenericType objectType = new ClassGenericType(choosenClass, null, null);
		for (ObserveInfo property : beanSupport.createProperties(null, objectType)) {
			adapters.add(new ObservePropertyAdapter(property));
		}
		return adapters;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Finish
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public InputStream getTemplateFile(String superClassName) {
		return Activator.getFile("templates/" + ClassUtils.getShortClassName(superClassName) + ".jvt");
	}

	@Override
	public String performSubstitutions(String code, ImportsManager imports) throws Exception {
		// calculate states
		boolean blockMode = useBlockMode();
		boolean lazyVariables = createLazyVariables();
		//
		DataBindingsCodeUtils.ensureDBLibraries(m_javaProject);
		CodeGenerationSupport generationSupport =
				new CodeGenerationSupport(CoreUtils.useGenerics(m_javaProject));
		// prepare imports
		Collection<String> importList = new HashSet<>();
		importList.add("java.awt.GridBagLayout");
		importList.add("java.awt.GridBagConstraints");
		importList.add("java.awt.Insets");
		importList.add("javax.swing.JLabel");
		importList.add("org.jdesktop.beansbinding.AutoBinding");
		importList.add("org.jdesktop.beansbinding.Bindings");
		importList.add("org.jdesktop.beansbinding.BeanProperty");
		//
		if (!generationSupport.useGenerics()) {
			importList.add("org.jdesktop.beansbinding.Property");
		}
		// bean class, field, name, field access
		String beanClassName = m_beanClass.getName().replace('$', '.');
		String beanClassShortName = ClassUtils.getShortClassName(beanClassName);
		String fieldPrefix = JavaCore.getOption(JavaCore.CODEASSIST_FIELD_PREFIXES);
		String fieldName = fieldPrefix + StringUtils.uncapitalize(beanClassShortName);
		//
		code = StringUtils.replace(code, "%BeanClass%", beanClassName);
		//
		if (ReflectionUtils.getConstructorBySignature(m_beanClass, "<init>()") == null) {
			code = StringUtils.replace(code, "%BeanField%", fieldName);
		} else {
			code = StringUtils.replace(code, "%BeanField%", fieldName + " = new " + beanClassName + "()");
		}
		//
		IPreferenceStore preferences = ToolkitProvider.DESCRIPTION.getPreferences();
		String accessPrefix =
				preferences.getBoolean(FieldUniqueVariableSupport.P_PREFIX_THIS) ? "this." : "";
		String beanFieldAccess = accessPrefix + fieldName;
		//
		code = StringUtils.replace(code, "%BeanName%", StringUtils.capitalize(beanClassShortName));
		code = StringUtils.replace(code, "%BeanFieldAccess%", accessPrefix + fieldName);
		// prepare properties
		final List<PropertyAdapter> properties = new ArrayList<>();
		Display.getDefault().syncExec(() -> CollectionUtils.addAll(properties, (PropertyAdapter[]) m_propertiesViewer.getCheckedElements()));
		// prepare code
		StringBuffer componentFields = new StringBuffer();
		StringBuffer components = new StringBuffer();
		String swingContainer = StringUtils.substringBetween(code, "%Components%", "%");
		String swingContainerWithDot = "this".equals(swingContainer) ? "" : swingContainer + ".";
		//
		int propertiesCount = properties.size();
		int lastPropertyIndex = propertiesCount - 1;
		StringBuffer bindings = new StringBuffer();
		// prepare layout code
		components.append("\t\tGridBagLayout gridBagLayout = new GridBagLayout();\r\n");
		components.append("\t\tgridBagLayout.columnWidths = new int[]{0, 0, 0};\r\n");
		components.append("\t\tgridBagLayout.rowHeights = new int[]{"
				+ StringUtils.repeat("0, ", propertiesCount)
				+ "0};\r\n");
		components.append("\t\tgridBagLayout.columnWeights = new double[]{0.0, 1.0, 1.0E-4};\r\n");
		components.append("\t\tgridBagLayout.rowWeights = new double[]{"
				+ StringUtils.repeat("0.0, ", propertiesCount)
				+ "1.0E-4};\r\n");
		components.append("\t\t" + swingContainerWithDot + "setLayout(gridBagLayout);\r\n");
		//
		StringBuffer group = new StringBuffer();
		generationSupport.generateLocalName("bindingGroup");
		//
		StringBuffer lazy = new StringBuffer();
		//
		for (int i = 0; i < propertiesCount; i++) {
			String index = Integer.toString(i);
			//
			PropertyAdapter property = properties.get(i);
			Object[] editorData = m_propertyToEditor.get(property);
			SwingComponentDescriptor componentDescriptor = (SwingComponentDescriptor) editorData[0];
			//
			String propertyName = property.getName();
			// label
			addLabelCode(
					componentFields,
					components,
					lazy,
					swingContainerWithDot,
					blockMode,
					lazyVariables,
					propertyName,
					index);
			//
			String componentClassName = componentDescriptor.getComponentClass();
			String componentShortClassName = ClassUtils.getShortClassName(componentClassName);
			String componentFieldName = fieldPrefix + propertyName + componentShortClassName;
			String componentFieldAccess = accessPrefix + componentFieldName;
			String componentLazyAccess =
					"get" + StringUtils.capitalize(propertyName) + componentShortClassName + "()";
			String componentAccess = lazyVariables ? componentLazyAccess : componentFieldAccess;
			//
			importList.add(componentClassName);
			// field
			componentFields.append("\r\nfield\r\n\tprivate "
					+ componentShortClassName
					+ " "
					+ componentFieldName
					+ ";");
			// component
			addComponentCode(
					components,
					lazy,
					swingContainerWithDot,
					blockMode,
					lazyVariables,
					componentShortClassName,
					componentFieldAccess,
					componentLazyAccess,
					index);
			// binding properties
			AutoBindingUpdateStrategyDescriptor strategyDescriptor =
					(AutoBindingUpdateStrategyDescriptor) editorData[1];
			//
			String modelPropertyName = generationSupport.generateLocalName(propertyName, "Property");
			String targetPropertyName =
					generationSupport.generateLocalName(componentDescriptor.getName(1), "Property");
			String bindingName = generationSupport.generateLocalName("autoBinding");
			String modelGeneric = null;
			String targetGeneric = null;
			//
			if (generationSupport.useGenerics()) {
				modelGeneric =
						beanClassName + ", " + GenericUtils.convertPrimitiveType(property.getType().getName());
				bindings.append("\t\tBeanProperty<" + modelGeneric + "> ");
			} else {
				bindings.append("\t\tProperty ");
			}
			bindings.append(modelPropertyName + " = BeanProperty.create(\"" + propertyName + "\");\r\n");
			//
			if (generationSupport.useGenerics()) {
				targetGeneric =
						componentDescriptor.getComponentClass() + ", " + componentDescriptor.getPropertyClass();
				bindings.append("\t\tBeanProperty<" + targetGeneric + "> ");
			} else {
				bindings.append("\t\tProperty ");
			}
			bindings.append(targetPropertyName
					+ " = BeanProperty.create(\""
					+ componentDescriptor.getName(1)
					+ "\");\r\n");
			// binding
			bindings.append("\t\tAutoBinding");
			if (generationSupport.useGenerics()) {
				bindings.append("<" + modelGeneric + ", " + targetGeneric + ">");
			}
			bindings.append(" "
					+ bindingName
					+ " = Bindings.createAutoBinding("
					+ strategyDescriptor.getSourceCode()
					+ ", "
					+ beanFieldAccess
					+ ", "
					+ modelPropertyName
					+ ", "
					+ componentAccess
					+ ", "
					+ targetPropertyName
					+ ");\r\n");
			bindings.append("\t\t" + bindingName + ".bind();");
			//
			group.append("\t\tbindingGroup.addBinding(" + bindingName + ");");
			//
			if (i < lastPropertyIndex) {
				componentFields.append("\r\n");
				components.append("\r\n");
				bindings.append("\r\n\t\t//\r\n");
				group.append("\r\n");
			}
			//
		}
		// replace template patterns
		code = StringUtils.replace(code, "%ComponentFields%", componentFields.toString());
		code = StringUtils.replace(code, "%Components%" + swingContainer + "%", components.toString());
		code = StringUtils.replace(code, "%Bindings%", bindings.toString());
		code = StringUtils.replace(code, "%Group%", group.toString());
		code = StringUtils.replace(code, "%LAZY%", lazy.toString());
		// add imports
		for (String qualifiedTypeName : importList) {
			imports.addImport(qualifiedTypeName);
		}
		//
		return code;
	}

	private static void addLabelCode(StringBuffer componentFields,
			StringBuffer components,
			StringBuffer lazy,
			String swingContainerWithDot,
			boolean blockMode,
			boolean lazyVariables,
			String propertyName,
			String index) {
		String componentAccess = null;
		if (lazyVariables) {
			// field
			String fieldPrefix = JavaCore.getOption(JavaCore.CODEASSIST_FIELD_PREFIXES);
			String label = propertyName + "Label";
			componentFields.append("\r\nfield\r\n\tprivate JLabel " + fieldPrefix + label + ";\r\n");
			// lazy method
			IPreferenceStore preferences = ToolkitProvider.DESCRIPTION.getPreferences();
			String accessPrefix =
					preferences.getBoolean(FieldUniqueVariableSupport.P_PREFIX_THIS) ? "this." : "";
			componentAccess = "get" + StringUtils.capitalize(label) + "()";
			String labelAccess = accessPrefix + fieldPrefix + label;
			lazy.append("\r\nmethod\r\n\tprivate JLabel "
					+ componentAccess
					+ " {\r\n\t\tif ("
					+ labelAccess
					+ " == null) {\r\n\t\t\t"
					+ labelAccess
					+ " = new JLabel(\""
					+ StringUtils.capitalize(propertyName)
					+ ":\");\r\n\t\t}\r\n\t\treturn "
					+ labelAccess
					+ ";\r\n\t}\r\n\r\n");
		}
		if (blockMode) {
			// block
			components.append("\t\t{\r\n");
			// component
			if (!lazyVariables) {
				componentAccess = "label";
				components.append("\t\t\tJLabel label = new JLabel(\""
						+ StringUtils.capitalize(propertyName)
						+ ":\");\r\n");
			}
			// layout
			components.append("\t\t\tGridBagConstraints gbc = new GridBagConstraints();\r\n");
			components.append("\t\t\tgbc.insets = new Insets(5, 5, 5, 5);\r\n");
			components.append("\t\t\tgbc.gridx = 0;\r\n");
			components.append("\t\t\tgbc.gridy = " + index + ";\r\n");
			components.append("\t\t\t" + swingContainerWithDot + "add(" + componentAccess + ", gbc);\r\n");
			// block
			components.append("\t\t}\r\n");
		} else {
			// separator
			components.append("\t\r\n");
			// component
			if (!lazyVariables) {
				componentAccess = propertyName + "Label";
				components.append("\t\tJLabel "
						+ componentAccess
						+ " = new JLabel(\""
						+ StringUtils.capitalize(propertyName)
						+ ":\");\r\n");
			}
			// layout
			String gbc = "labelGbc_" + index;
			components.append("\t\tGridBagConstraints " + gbc + " = new GridBagConstraints();\r\n");
			components.append("\t\t" + gbc + ".insets = new Insets(5, 5, 5, 5);\r\n");
			components.append("\t\t" + gbc + ".gridx = 0;\r\n");
			components.append("\t\t" + gbc + ".gridy = " + index + ";\r\n");
			components.append("\t\t"
					+ swingContainerWithDot
					+ "add("
					+ componentAccess
					+ ", "
					+ gbc
					+ ");\r\n");
			// separator
			components.append("\t\r\n");
		}
	}

	private static boolean useBlockMode() {
		StatementGeneratorDescription statement =
				SwingToolkitDescription.INSTANCE.getGenerationSettings().getStatement();
		return statement != null && "Block".equals(statement.getName());
	}

	private static boolean createLazyVariables() {
		VariableSupportDescription variable =
				SwingToolkitDescription.INSTANCE.getGenerationSettings().getVariable();
		return variable != null && "Lazy".equals(variable.getName());
	}

	private static void addComponentCode(StringBuffer components,
			StringBuffer lazy,
			String swingContainerWithDot,
			boolean blockMode,
			boolean lazyVariable,
			String componentShortClassName,
			String componentFieldAccess,
			String componentLazyAccess,
			String index) {
		String componentAccess = null;
		if (lazyVariable) {
			// lazy method
			componentAccess = componentLazyAccess;
			lazy.append("\r\nmethod\r\n\tprivate "
					+ componentShortClassName
					+ " "
					+ componentLazyAccess
					+ " {\r\n\t\tif ("
					+ componentFieldAccess
					+ " == null) {\r\n\t\t\t"
					+ componentFieldAccess
					+ " = new "
					+ componentShortClassName
					+ "();\r\n\t\t}\r\n\t\treturn "
					+ componentFieldAccess
					+ ";\r\n\t}\r\n\r\n");
		}
		if (blockMode) {
			// block
			components.append("\t\t{\r\n");
			// component
			if (!lazyVariable) {
				componentAccess = componentFieldAccess;
				components.append("\t\t\t "
						+ componentFieldAccess
						+ " = new "
						+ componentShortClassName
						+ "();\r\n");
			}
			// layout
			components.append("\t\t\tGridBagConstraints gbc = new GridBagConstraints();\r\n");
			components.append("\t\t\tgbc.insets = new Insets(5, 0, 5, 5);\r\n");
			components.append("\t\t\tgbc.fill = GridBagConstraints.HORIZONTAL;\r\n");
			components.append("\t\t\tgbc.gridx = 1;\r\n");
			components.append("\t\t\tgbc.gridy = " + index + ";\r\n");
			components.append("\t\t\t" + swingContainerWithDot + "add(" + componentAccess + ", gbc);\r\n");
			// block
			components.append("\t\t}");
		} else {
			// separator
			components.append("\t\r\n");
			//component
			if (!lazyVariable) {
				componentAccess = componentFieldAccess;
				components.append("\t\t "
						+ componentFieldAccess
						+ " = new "
						+ componentShortClassName
						+ "();\r\n");
			}
			// layout
			String gbc = "componentGbc_" + index;
			components.append("\t\tGridBagConstraints " + gbc + " = new GridBagConstraints();\r\n");
			components.append("\t\t" + gbc + ".insets = new Insets(5, 0, 5, 5);\r\n");
			components.append("\t\t" + gbc + ".fill = GridBagConstraints.HORIZONTAL;\r\n");
			components.append("\t\t" + gbc + ".gridx = 1;\r\n");
			components.append("\t\t" + gbc + ".gridy = " + index + ";\r\n");
			components.append("\t\t"
					+ swingContainerWithDot
					+ "add("
					+ componentAccess
					+ ", "
					+ gbc
					+ ");\r\n");
			// separator
			components.append("\t\t");
		}
	}
}