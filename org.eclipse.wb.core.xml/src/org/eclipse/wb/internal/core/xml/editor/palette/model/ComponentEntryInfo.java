/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.xml.editor.palette.model;

import com.google.common.collect.Sets;

import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.requests.ICreationFactory;
import org.eclipse.wb.gef.core.tools.CreationTool;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.palette.model.entry.LibraryInfo;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentPresentation;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorWarning;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.creation.ElementCreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.xml.model.description.ComponentPresentationHelper;
import org.eclipse.wb.internal.core.xml.model.description.CreationDescription;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Implementation of {@link EntryInfo} for "component" contribution.
 *
 * @author scheglov_ke
 * @coverage XML.editor.palette
 */
public final class ComponentEntryInfo extends ToolEntryInfo {
	public static final String KEY_SIMULATE_PRESENTATION = "ComponentEntryInfo.simulatePresentation";
	public static final ImageDescriptor DEFAULT_ICON = DesignerPlugin.getImageDescriptor("palette/Object.png");
	private String m_className;
	private String m_creationId;
	private String m_enabledScript;
	private ImageDescriptor m_icon;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public ComponentEntryInfo() {
	}

	public ComponentEntryInfo(CategoryInfo categoryInfo, IConfigurationElement element)
			throws Exception {
		this(categoryInfo, AttributesProviders.get(element));
		m_icon = ExternalFactoriesHelper.getImageDescriptor(element, "icon");
		addLibraries(element);
	}

	public ComponentEntryInfo(CategoryInfo categoryInfo, AttributesProvider attributes) {
		// class
		{
			m_className = attributes.getAttribute("class");
			Assert.isNotNull(m_className, "Component must have 'class' attribute.");
		}
		// creationId
		{
			m_creationId = attributes.getAttribute("creationId");
		}
		// id
		{
			String id = attributes.getAttribute("id");
			if (id == null) {
				id = categoryInfo.getId() + " " + m_className;
				if (m_creationId != null) {
					id += " " + m_creationId;
				}
			}
			setId(id);
		}
		// other
		setName(attributes.getAttribute("name"));
		setDescription(attributes.getAttribute("description"));
		setVisible(getBoolean(attributes, "visible", true));
		m_enabledScript = attributes.getAttribute("enabled");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		return "Component(class='" + m_className + "')";
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Creation id
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the "id" for {@link ConstructorCreationSupport}, may be <code>null</code>.
	 */
	public String getCreationId() {
		return m_creationId;
	}

	/**
	 * Sets the "id" for {@link ConstructorCreationSupport}, may be <code>null</code>.
	 */
	public void setCreationId(String creationId) {
		m_creationId = creationId;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Component class name
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the name of component class.
	 */
	public String getClassName() {
		return m_className;
	}

	/**
	 * Sets the name of component class.
	 */
	public void setComponentClassName(String componentClassName) {
		m_className = componentClassName;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Libraries
	//
	////////////////////////////////////////////////////////////////////////////
	private final List<LibraryInfo> m_libraries = new ArrayList<>();

	/**
	 * Adds new {@link LibraryInfo} to ensure.
	 */
	private void addLibraries(IConfigurationElement componentElement) {
		for (IConfigurationElement libraryElement : componentElement.getChildren("library")) {
			m_libraries.add(new LibraryInfo(libraryElement));
		}
	}

	/**
	 * Ensures all {@link LibraryInfo}'s.
	 */
	private void ensureLibraries() throws Exception {
		for (LibraryInfo library : m_libraries) {
			library.ensure(m_javaProject);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Name
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getNameDefault() {
		return m_className;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// EntryInfo
	//
	////////////////////////////////////////////////////////////////////////////
	private Class<?> m_class;
	private ComponentPresentation m_presentation;
	private ComponentDescription m_description;
	private CreationDescription m_creation;

	@Override
	public boolean initialize(IEditPartViewer editPartViewer, XmlObjectInfo rootObject) {
		super.initialize(editPartViewer, rootObject);
		// prepare presentation
		if (!preparePresentation()) {
			return false;
		}
		// updates
		{
			// update entry icon
			if (m_icon == null) {
				m_icon = m_presentation.getIcon();
			}
			// update entry name
			if (getNameRaw() == null) {
				setName(m_presentation.getName());
			}
			// update entry description text
			{
				String description = getDescription();
				if (StringUtils.isEmpty(description) || m_className.equals(description)) {
					setDescription(m_presentation.getDescription());
				}
			}
		}
		// OK, initialized
		return true;
	}

	/**
	 * Prepares {@link #m_presentation}.
	 */
	private boolean preparePresentation() {
		if (m_rootJavaInfo.getArbitraryValue(KEY_SIMULATE_PRESENTATION) != null) {
			m_presentation =
					new ComponentPresentation("key", "toolkitId", m_className, m_className, (ImageDescriptor) null);
			return true;
		}
		// check if Class exists (may be optimized by loading only "witness Class")
		if (!hasClass()) {
			return false;
		}
		// prepare presentation
		try {
			m_presentation =
					ComponentPresentationHelper.getPresentation(m_context, m_className, m_creationId);
			if (m_presentation == null) {
				String message = "Palette: no presentation for component " + m_className;
				m_context.addWarning(new EditorWarning(message));
				return false;
			}
		} catch (Throwable e) {
			String message = "Palette: can not load component " + m_className;
			m_context.addWarning(new EditorWarning(message, e));
			return false;
		}
		// done
		return true;
	}

	private boolean ensureDescriptions() {
		if (m_creation == null) {
			try {
				m_class = m_context.getClassLoader().loadClass(m_className);
				m_description = ComponentDescriptionHelper.getDescription(m_context, m_class);
				m_creation = m_description.getCreation(m_creationId);
			} catch (Throwable e) {
				String message = "Palette: can not load component " + m_className;
				m_context.addWarning(new EditorWarning(message, e));
				return false;
			}
		}
		// OK, can activate
		return true;
	}

	@Override
	public boolean isEnabled() {
		// try "enabled"
		if (m_enabledScript != null) {
			boolean enabled = ExecutionUtils.runObjectIgnore(new RunnableObjectEx<Boolean>() {
				@Override
				public Boolean runObject() throws Exception {
					ClassLoader classLoader = m_context.getClassLoader();
					return (Boolean) ScriptUtils.evaluate(classLoader, m_enabledScript);
				}
			}, false);
			if (!enabled) {
				return false;
			}
		}
		// OK
		return true;
	}

	@Override
	public ImageDescriptor getIcon() {
		if (m_icon == null) {
			return DEFAULT_ICON;
		}
		return m_icon;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private static Map<ClassLoader, Set<String>> m_contextHasClasses =
			new WeakHashMap<>();
	private static Map<ClassLoader, Set<String>> m_contextNoClasses =
			new WeakHashMap<>();

	/**
	 * @return <code>true</code> if {@link #m_className} most probably exists in {@link ClassLoader}.
	 */
	private boolean hasClass() {
		// try to optimize
		List<IConfigurationElement> elements =
				ExternalFactoriesHelper.getElements(
						"org.eclipse.wb.core.paletteComponentExists",
						"component");
		for (IConfigurationElement element : elements) {
			String pkg = element.getAttribute("package");
			if (pkg != null && m_className.startsWith(pkg)) {
				// witness
				{
					String witnessClassName = element.getAttribute("witness");
					if (witnessClassName != null) {
						return hasClass0(witnessClassName);
					}
				}
				// hasType
				if (element.getAttribute("hasType") != null) {
					return ProjectUtils.hasType(m_javaProject, m_className);
				}
				// always
				if (element.getAttribute("always") != null) {
					return true;
				}
			}
		}
		// check Class directly
		return hasClass0(m_className);
	}

	/**
	 * @return <code>true</code> if {@link Class} with given name exists in {@link ClassLoader}.
	 */
	private boolean hasClass0(String className) {
		ClassLoader classLoader = m_context.getClassLoader();
		// prepare "has" cache
		Set<String> hasClasses;
		{
			hasClasses = m_contextHasClasses.get(classLoader);
			if (hasClasses == null) {
				hasClasses = Sets.newHashSet();
				m_contextHasClasses.put(classLoader, hasClasses);
			}
		}
		// may be we already know that there is such Class
		if (hasClasses.contains(className)) {
			return true;
		}
		// prepare "no" cache
		Set<String> noClasses;
		{
			noClasses = m_contextNoClasses.get(classLoader);
			if (noClasses == null) {
				noClasses = Sets.newHashSet();
				m_contextNoClasses.put(classLoader, noClasses);
			}
		}
		// may be we already know that there are no such Class
		if (noClasses.contains(className)) {
			return false;
		}
		// ask ClassLoader
		try {
			classLoader.loadClass(className);
			hasClasses.add(className);
			return true;
		} catch (ClassNotFoundException e) {
		}
		// remember that there are no such Class
		noClasses.add(className);
		return false;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ToolEntryInfo
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Tool createTool() throws Exception {
		if (!ensureDescriptions()) {
			return null;
		}
		if (ReflectionUtils.isAbstract(m_class)) {
			Shell parentShell = IPaletteSite.Helper.getSite(m_rootJavaInfo).getShell();
			UiUtils.openError(parentShell, "Error", "You can not drop abstract component.");
			return null;
		}
		// prepare factory
		ICreationFactory factory = new ICreationFactory() {
			private XmlObjectInfo m_object;

			@Override
			public void activate() throws Exception {
				CreationSupport creationSupport = new ElementCreationSupport(m_creationId);
				m_object = XmlObjectUtils.createObject(m_context, m_class, creationSupport);
				m_object = XmlObjectUtils.getWrapped(m_object);
				m_object.putArbitraryValue(XmlObjectInfo.FLAG_MANUAL_COMPONENT, Boolean.TRUE);
			}

			@Override
			public Object getNewObject() {
				return m_object;
			}
		};
		// return tool
		ensureLibraries();
		return new CreationTool(factory);
	}
}
