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
package org.eclipse.wb.internal.rcp.databinding.wizards.autobindings;

import com.google.common.collect.Sets;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.databinding.ui.editor.ICompleteListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassAndPropertiesConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.PropertyAdapter;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.databinding.wizards.autobindings.DefaultAutomaticDatabindingProvider;
import org.eclipse.wb.internal.core.databinding.wizards.autobindings.DescriptorContainer;
import org.eclipse.wb.internal.core.databinding.wizards.autobindings.IImageLoader;
import org.eclipse.wb.internal.core.model.generation.statement.StatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.variable.FieldUniqueVariableSupport;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.RcpToolkitDescription;
import org.eclipse.wb.internal.rcp.ToolkitProvider;
import org.eclipse.wb.internal.rcp.databinding.Activator;
import org.eclipse.wb.internal.rcp.databinding.model.ControllerSupport;
import org.eclipse.wb.internal.rcp.databinding.model.DataBindingsCodeUtils;
import org.eclipse.wb.internal.rcp.databinding.model.GlobalFactoryHelper;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanSupport;
import org.eclipse.wb.internal.rcp.databinding.ui.providers.PropertyAdapterLabelProvider;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage.ImportsManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;

import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Provider for support JFace bindings API.
 *
 * @author lobas_av
 * @coverage bindings.rcp.wizard.auto
 */
public class SwtDatabindingProvider extends DefaultAutomaticDatabindingProvider {
	private static DescriptorContainer m_widgetContainer;
	private static DescriptorContainer m_strategyContainer;
	private AutomaticDatabindingFirstPage m_firstPage;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public static SwtDatabindingProvider create() {
		try {
			// check containers
			if (m_widgetContainer == null && m_strategyContainer == null) {
				// load containers
				InputStream stream = Activator.getFile("templates/SwtEditors.xml");
				Map<String, DescriptorContainer> containers = DescriptorContainer.parseDescriptors(
						stream,
						SwtDatabindingProvider.class.getClassLoader(),
						new IImageLoader() {
							@Override
							public Image getImage(String name) {
								return Activator.getImage(name);
							}
						});
				IOUtils.closeQuietly(stream);
				// sets containers
				m_widgetContainer = containers.get("SWT.Widgets");
				m_strategyContainer = containers.get("JFaceBinding.Strategy");
			}
			// create provider
			return new SwtDatabindingProvider(m_widgetContainer, m_strategyContainer);
		} catch (Throwable e) {
			DesignerPlugin.log(e);
			return null;
		}
	}

	private SwtDatabindingProvider(DescriptorContainer widgetContainer,
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
		return new String[]{
				"org.eclipse.swt.widgets.Composite",
				"org.eclipse.jface.dialogs.Dialog",
		"org.eclipse.swt.widgets.Shell"};
	}

	@Override
	public String getInitialSuperClass() {
		return "org.eclipse.swt.widgets.Composite";
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// GUI
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void configure(ChooseClassAndPropertiesConfiguration configuration) {
		configuration.setPropertiesLabelProvider(new PropertyAdapterLabelProvider());
		configuration.setValueScope("beans");
		try {
			GlobalFactoryHelper.automaticWizardConfigure(configuration);
		} catch (Throwable e) {
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// WizardPage
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void setCurrentWizardData(
			org.eclipse.wb.internal.core.databinding.wizards.autobindings.AutomaticDatabindingFirstPage firstPage,
			ICompleteListener pageListener) {
		super.setCurrentWizardData(firstPage, pageListener);
		m_firstPage = (AutomaticDatabindingFirstPage) firstPage;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected List<PropertyAdapter> getProperties0(Class<?> choosenClass) throws Exception {
		List<PropertyAdapter> properties = GlobalFactoryHelper.automaticWizardGetProperties(
				m_javaProject,
				m_classLoader,
				choosenClass);
		if (properties != null) {
			return properties;
		}
		properties = new ArrayList<>();
		for (PropertyDescriptor descriptor : BeanSupport.getPropertyDescriptors(choosenClass)) {
			properties.add(new PropertyAdapter(descriptor));
		}
		return properties;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Finish
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public InputStream getTemplateFile(String superClassName) {
		String subName = m_firstPage.isCreateControlClass() ? "Controller" : "";
		return Activator.getFile(
				"templates/" + ClassUtils.getShortClassName(superClassName) + subName + ".jvt");
	}

	@Override
	public String performSubstitutions(String code, ImportsManager imports) throws Exception {
		// prepare properties
		final List<PropertyAdapter> properties = new ArrayList<>();
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				CollectionUtils.addAll(properties, m_propertiesViewer.getCheckedElements());
			}
		});
		//
		if (m_firstPage.isCreateControlClass()) {
			return ControllerSupport.automaticWizardPerformSubstitutions(
					m_firstPage,
					code,
					imports,
					m_javaProject,
					m_classLoader,
					m_beanClass,
					properties,
					m_propertyToEditor);
		}
		//
		String begin = "";
		String end = "\t\t";
		String widgetStart = "";
		boolean blockMode = useBlockMode();
		if (blockMode) {
			begin = "\t\t{\r\n";
			end = "\t\t}";
			widgetStart = "\t";
		}
		// prepare imports
		Collection<String> importList = Sets.newHashSet();
		importList.add(SWT.class.getName());
		importList.add("org.eclipse.jface.databinding.swt.SWTObservables");
		importList.add("org.eclipse.core.databinding.observable.value.IObservableValue");
		importList.add("org.eclipse.core.databinding.UpdateValueStrategy");
		importList.add("org.eclipse.swt.widgets.Label");
		importList.add("org.eclipse.swt.layout.GridLayout");
		importList.add("org.eclipse.swt.layout.GridData");
		//
		DataBindingsCodeUtils.ensureDBLibraries(m_javaProject);
		//
		IAutomaticWizardStub automaticWizardStub =
				GlobalFactoryHelper.automaticWizardCreateStub(m_javaProject, m_classLoader, m_beanClass);
		//
		String observeMethod = null;
		if (automaticWizardStub == null) {
			if (ObservableInfo.isPojoBean(m_beanClass)) {
				String pojoClass = DataBindingsCodeUtils.getPojoObservablesClass();
				observeMethod =
						"ObserveValue = " + ClassUtils.getShortClassName(pojoClass) + ".observeValue(";
				importList.add(pojoClass);
			} else {
				observeMethod = "ObserveValue = BeansObservables.observeValue(";
				importList.add("org.eclipse.core.databinding.beans.BeansObservables");
			}
		} else {
			automaticWizardStub.addImports(importList);
		}
		// prepare bean
		String beanClassName = CoreUtils.getClassName(m_beanClass);
		String beanClassShortName = ClassUtils.getShortClassName(beanClassName);
		String fieldPrefix = JavaCore.getOption(JavaCore.CODEASSIST_FIELD_PREFIXES);
		String fieldName = fieldPrefix + StringUtils.uncapitalize(beanClassShortName);
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
		code = StringUtils.replace(code, "%BeanFieldAccess%", accessPrefix + fieldName);
		//
		code = StringUtils.replace(code, "%BeanName%", StringUtils.capitalize(beanClassShortName));
		// prepare code
		StringBuffer widgetFields = new StringBuffer();
		StringBuffer widgets = new StringBuffer();
		String swtContainer = StringUtils.substringBetween(code, "%Widgets%", "%");
		String swtContainerWithDot = "this".equals(swtContainer) ? "" : swtContainer + ".";
		//
		StringBuffer observables = new StringBuffer();
		StringBuffer bindings = new StringBuffer();
		//
		importList.add(GridLayout.class.getName());
		widgets.append("\t\t" + swtContainerWithDot + "setLayout(new GridLayout(2, false));\r\n");
		if (!blockMode) {
			widgets.append("\t\t\r\n");
		}
		//
		for (Iterator<PropertyAdapter> I = properties.iterator(); I.hasNext();) {
			PropertyAdapter property = I.next();
			Object[] editorData = m_propertyToEditor.get(property);
			SwtWidgetDescriptor widgetDescriptor = (SwtWidgetDescriptor) editorData[0];
			JFaceBindingStrategyDescriptor strategyDescriptor =
					(JFaceBindingStrategyDescriptor) editorData[1];
			//
			String propertyName = property.getName();
			String widgetClassName = widgetDescriptor.getClassName();
			String widgetFieldName = fieldPrefix + propertyName + widgetClassName;
			String widgetFieldAccess = accessPrefix + widgetFieldName;
			// field
			widgetFields.append(
					"\r\nfield\r\n\tprivate " + widgetClassName + " " + widgetFieldName + ";");
			// widget
			widgets.append(begin);
			widgets.append(
					widgetStart
					+ "\t\tnew Label("
					+ swtContainer
					+ ", SWT.NONE).setText(\""
					+ StringUtils.capitalize(propertyName)
					+ ":\");\r\n");
			widgets.append(end + "\r\n");
			//
			widgets.append(begin);
			widgets.append(
					"\t\t"
							+ widgetFieldAccess
							+ " = "
							+ widgetDescriptor.getCreateCode(swtContainer)
							+ ";\r\n");
			widgets.append(
					widgetStart
					+ "\t\t"
					+ widgetFieldAccess
					+ ".setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));\r\n");
			widgets.append(end);
			// observables
			observables.append(
					"\t\tIObservableValue "
							+ propertyName
							+ "ObserveWidget = "
							+ widgetDescriptor.getBindingCode(widgetFieldName)
							+ ";\r\n");
			if (automaticWizardStub == null) {
				observables.append(
						"\t\tIObservableValue "
								+ propertyName
								+ observeMethod
								+ fieldName
								+ ", \""
								+ propertyName
								+ "\");");
			} else {
				observables.append(automaticWizardStub.createSourceCode(fieldName, propertyName));
			}
			// bindings
			bindings.append(
					"\t\tbindingContext.bindValue("
							+ propertyName
							+ "ObserveWidget, "
							+ propertyName
							+ "ObserveValue, "
							+ getStrategyValue(strategyDescriptor.getTargetStrategyCode())
							+ ", "
							+ getStrategyValue(strategyDescriptor.getModelStrategyCode())
							+ ");");
			//
			if (I.hasNext()) {
				widgetFields.append("\r\n");
				widgets.append("\r\n");
				observables.append("\r\n");
				bindings.append("\r\n");
			}
			//
			importList.add(widgetDescriptor.getFullClassName());
		}
		// replace template patterns
		code = StringUtils.replace(code, "%WidgetFields%", widgetFields.toString());
		code = StringUtils.replace(code, "%Widgets%" + swtContainer + "%", widgets.toString());
		//
		code = StringUtils.replace(code, "%Observables%", observables.toString());
		code = StringUtils.replace(code, "%Bindings%", bindings.toString());
		// add imports
		for (String qualifiedTypeName : importList) {
			imports.addImport(qualifiedTypeName);
		}
		//
		return code;
	}

	private static String getStrategyValue(String strategyCode) {
		return StringUtils.isEmpty(strategyCode) || "null".equalsIgnoreCase(strategyCode)
				? "null"
						: "new org.eclipse.core.databinding.UpdateValueStrategy(" + strategyCode + ")";
	}

	public static boolean useBlockMode() {
		StatementGeneratorDescription statement =
				RcpToolkitDescription.INSTANCE.getGenerationSettings().getStatement();
		return statement != null && "Block".equals(statement.getName());
	}
}