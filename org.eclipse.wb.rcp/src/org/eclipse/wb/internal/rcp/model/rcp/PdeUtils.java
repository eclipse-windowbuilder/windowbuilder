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
package org.eclipse.wb.internal.rcp.model.rcp;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.rcp.Activator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.pde.core.IBaseModel;
import org.eclipse.pde.core.plugin.IExtensionsModelFactory;
import org.eclipse.pde.core.plugin.IPluginAttribute;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginImport;
import org.eclipse.pde.core.plugin.IPluginLibrary;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.eclipse.pde.core.plugin.IPluginParent;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.ui.util.ModelModification;
import org.eclipse.pde.internal.ui.util.PDEModelUtility;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.part.EditorPart;

import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Bundle;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Predicate;

/**
 * Helper for working with PDE model.
 *
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
@SuppressWarnings("restriction")
public final class PdeUtils {
	public final static String BUNDLE_FILENAME_DESCRIPTOR = "META-INF/MANIFEST.MF";
	public final static String BUILD_FILENAME_DESCRIPTOR = "build.properties";
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance access
	//
	////////////////////////////////////////////////////////////////////////////
	private static final Map<IProject, PdeUtils> m_projectToUtilitiesMap = new HashMap<>();

	/**
	 * @return the instance of {@link PdeUtils} for given {@link IProject}, existing from cache or
	 *         new.
	 */
	public static PdeUtils get(IProject project) {
		PdeUtils utilities = m_projectToUtilitiesMap.get(project);
		if (utilities == null) {
			utilities = new PdeUtils(project);
			m_projectToUtilitiesMap.put(project, utilities);
		}
		return utilities;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Instance fields
	//
	////////////////////////////////////////////////////////////////////////////
	private final IProject m_project;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private PdeUtils(IProject project) {
		m_project = project;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return {@link IProject} that contains <code>plugin.xml</code> file.
	 */
	public IProject getProject() {
		return m_project;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	public static final String PDE_NATURE_ID = "org.eclipse.pde.PluginNature";

	/**
	 * @return <code>true</code> if {@link IProject} has PDE nature.
	 */
	public static boolean hasPDENature(IProject project) {
		return ProjectUtils.hasNature(project, PDE_NATURE_ID);
	}

	/**
	 * Ensures that <code>MANIFEST.MF</code> declares this bundle as <code>singleton</code>.
	 */
	public void ensureSingleton() throws Exception {
		IFile m_manifestFile = m_project.getFile(BUNDLE_FILENAME_DESCRIPTOR);
		if (m_manifestFile.exists()) {
			String contents = IOUtils2.readString(m_manifestFile.getContents());
			String[] lines = StringUtils.split(contents, "\r\n");
			for (String line : lines) {
				if (line.startsWith("Bundle-SymbolicName:")) {
					if (!line.endsWith("singleton:=true")) {
						contents = StringUtils.replace(contents, line, line + "; singleton:=true");
						IOUtils2.setFileContents(
								m_manifestFile,
								new ByteArrayInputStream(contents.getBytes(m_manifestFile.getCharset())));
					}
					break;
				}
			}
		}
	}

	/**
	 * Adds a new plug-in import to this plugin.
	 *
	 * @param pluginId
	 *          the id of plugin.
	 */
	public void addPluginImport(final List<String> pluginIds) throws Exception {
		final List<String> pluginIdsForAdding = new ArrayList<>(pluginIds);
		// check exist imports
		for (IPluginImport pluginImport : getModel().getPluginBase().getImports()) {
			pluginIdsForAdding.remove(pluginImport.getId());
		}
		if (pluginIdsForAdding.isEmpty()) {
			return;
		}
		// perform modification
		ModelModification modification = new ModelModification(m_project) {
			@Override
			protected void modifyModel(IBaseModel model, IProgressMonitor monitor) throws CoreException {
				IPluginModelBase plugin = (IPluginModelBase) model;
				for (String pluginId : pluginIdsForAdding) {
					// create import
					IPluginImport pluginImport = plugin.getPluginFactory().createImport();
					pluginImport.setId(pluginId);
					// add import
					plugin.getPluginBase().add(pluginImport);
				}
			}
		};
		modifyModel(modification);
	}

	public void addPluginImport(String... pluginIds) throws Exception {
		addPluginImport(Arrays.asList(pluginIds));
	}

	/**
	 * Adds a new library to this plugin.
	 *
	 * @param name
	 *          the name of library (i.e. jar) file.
	 */
	public void addLibrary(final String name) throws Exception {
		ModelModification modification = new ModelModification(m_project) {
			@Override
			protected void modifyModel(IBaseModel model, IProgressMonitor monitor) throws CoreException {
				IPluginModelBase plugin = (IPluginModelBase) model;
				// if first library, add "." entry
				if (plugin.getPluginBase().getLibraries().length == 0) {
					addLibrary0(plugin, ".");
				}
				// add library
				addLibrary0(plugin, name);
			}

			private void addLibrary0(IPluginModelBase plugin, String name) throws CoreException {
				// create library
				IPluginLibrary library = plugin.getPluginFactory().createLibrary();
				library.setName(name);
				// add library
				plugin.getPluginBase().add(library);
			}
		};
		modifyModel(modification);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IPluginModelBase utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the ID of plugin, may be <code>null</code>.
	 */
	public static String getId(IPluginModelBase pluginModel) {
		BundleDescription bundleDescription = pluginModel.getBundleDescription();
		if (bundleDescription != null) {
			return bundleDescription.getSymbolicName();
		} else {
			return null;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ID generation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the unique ID, see {@link CodeUtils#generateUniqueName(String, Predicate)}.
	 */
	public String generateUniqueID(String baseId) {
		final Set<String> idSet = getIDSet();
		return CodeUtils.generateUniqueName(baseId, t -> !idSet.contains(t));
	}

	/**
	 * @return the {@link Set} of all ID's in this <code>plugin.xml</code> file.
	 */
	private Set<String> getIDSet() {
		Set<String> idSet = new TreeSet<>();
		for (IPluginExtension extension : getExtensions(getModel())) {
			for (IPluginObject pluginObject : extension.getChildren()) {
				if (pluginObject instanceof IPluginElement element) {
					String id = getAttribute(element, "id");
					if (id != null) {
						idSet.add(id);
					}
				}
			}
		}
		return idSet;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IPluginElement attributes
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @param element
	 *          the {@link IPluginElement} to get attribute name.
	 * @param attributeName
	 *          the name of attribute to get value from.
	 *
	 * @return the value of attribute with given name.
	 */
	public static String getAttribute(IPluginElement element, String attributeName) {
		if (element == null) {
			return null;
		}
		// prepare attribute
		IPluginAttribute attribute = element.getAttribute(attributeName);
		if (attribute == null) {
			return null;
		}
		// prepare value, may be localized
		String value = attribute.getValue();
		value = attribute.getPluginBase().getResourceString(value);
		return value;
	}

	/**
	 * Sets value of attribute in given {@link IPluginElement}.
	 * <p>
	 * Note that given {@link IPluginElement} itself is <em>not</em> modified, but underlying
	 * <code>plugin.xml</code> is modified. Only after request of {@link IPluginElement} using for
	 * example {@link #getExtensionElementById(String, String, String)} you will see updated
	 * attributes.
	 *
	 * @param element
	 *          the direct child of <code>extension</code>.
	 * @param attributeName
	 *          the name of attribute to change.
	 * @param value
	 *          the new value for attribute, <code>null</code> to remove attribute.
	 */
	public void setAttribute(final IPluginElement element,
			final String attributeName,
			final String value) throws Exception {
		IFile pluginFile = (IFile) element.getModel().getUnderlyingResource();
		ModelModification modification = new ModelModification(pluginFile) {
			@Override
			protected void modifyModel(IBaseModel model, IProgressMonitor monitor) throws CoreException {
				IPluginElement updatableElement = getCorrespondingElement(model, element);
				updatableElement.setAttribute(attributeName, value);
			}
		};
		modifyModel(modification);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IPluginModelBase access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link IPluginModelBase} for this plugin {@link IProject}.
	 */
	private IPluginModelBase getModel() {
		return PluginRegistry.findModel(m_project);
	}

	/**
	 * @return all plug-ins and fragments in the workspace as well as all target plug-ins and
	 *         fragments, regardless whether or not they are checked on the Target Platform preference
	 *         page.
	 */
	private static IPluginModelBase[] getAllModels() {
		return PluginRegistry.getActiveModels();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IPluginElement access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @param pointId
	 *          the ID of extension point.
	 * @param elementName
	 *          the name of extension element.
	 *
	 * @return the {@link IPluginElement}'s for direct child of extension.
	 */
	public List<IPluginElement> getExtensionElements(String pointId, String elementName) {
		return getExtensionElements(getModel(), pointId, elementName);
	}

	/**
	 * @return same as {@link #getExtensionElementById(String, String, String)}, but waits for not
	 *         <code>null</code> result.
	 */
	public IPluginElement waitExtensionElementById(String pointId, String elementName, String id) {
		while (true) {
			IPluginElement element = getExtensionElementById(pointId, elementName, id);
			if (element != null) {
				return element;
			}
			ExecutionUtils.waitEventLoop(10);
		}
	}

	/**
	 * @return the {@link IPluginElement} of given extension, with required <code>id</code> attribute
	 *         value, may be <code>null</code>.
	 */
	public IPluginElement getExtensionElementById(String pointId, String elementName, String id) {
		return getExtensionElement(pointId, elementName, "id", id);
	}

	/**
	 * @return the {@link IPluginElement} of given extension, with required <code>class</code>
	 *         attribute value, may be <code>null</code>.
	 */
	public IPluginElement getExtensionElementByClass(String pointId,
			String elementName,
			String className) {
		return getExtensionElement(pointId, elementName, "class", className);
	}

	/**
	 * @param pointId
	 *          the ID of extension point.
	 * @param elementName
	 *          the name of extension element.
	 * @param filterAttrName
	 *          the name of attribute to check.
	 * @param filterAttrValue
	 *          the value of attribute to check.
	 *
	 * @return the {@link IPluginElement} of direct extension child that satisfies given filter on
	 *         attribute value.
	 */
	private IPluginElement getExtensionElement(String pointId,
			String elementName,
			String filterAttrName,
			String filterAttrValue) {
		return getExtensionElement(getModel(), pointId, elementName, filterAttrName, filterAttrValue);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IPluginExtension access (implementation)
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @param pointId
	 *          the ID of extension point.
	 *
	 * @return the {@link IPluginExtension}'s for given extension point.
	 */
	private static List<IPluginExtension> getExtensions(IPluginModelBase pluginModel, String pointId) {
		List<IPluginExtension> extensions = new ArrayList<>();
		if (pluginModel != null) {
			for (IPluginExtension extension : getExtensions(pluginModel)) {
				if (extension.getPoint().equals(pointId)) {
					extensions.add(extension);
				}
			}
		}
		return extensions;
	}

	private static IPluginExtension[] getExtensions(IPluginModelBase pluginModel) {
		if (pluginModel.getClass().getName().equals(
				"org.eclipse.pde.internal.core.plugin.ExternalPluginModel")) {
			return pluginModel.getExtensions().getExtensions();
		}
		return pluginModel.getPluginBase().getExtensions();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IPluginElement access (implementation)
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link IPluginElement}'s for direct children of extension.
	 */
	private static List<IPluginElement> getExtensionElements(IPluginModelBase pluginModel,
			String pointId,
			String elementName) {
		List<IPluginElement> elements = new ArrayList<>();
		for (IPluginExtension extension : getExtensions(pluginModel, pointId)) {
			for (IPluginObject pluginObject : extension.getChildren()) {
				if (pluginObject instanceof IPluginElement element) {
					if (element.getName().equals(elementName)) {
						elements.add(element);
					}
				}
			}
		}
		return elements;
	}

	/**
	 * @return the {@link IPluginElement} that satisfies given filter on attribute value.
	 */
	private static IPluginElement getExtensionElement(IPluginModelBase pluginModel,
			String pointId,
			String elementName,
			String filterAttrName,
			String filterAttrValue) {
		List<IPluginElement> elements = getExtensionElements(pluginModel, pointId, elementName);
		for (IPluginElement element : elements) {
			String attributeValue = getAttribute(element, filterAttrName);
			if (filterAttrValue.equals(attributeValue)) {
				return element;
			}
		}
		// not found
		return null;
	}

	/**
	 * Ensures that this {@link IProject} has <code>plugin.xml</code> file.
	 */
	private void ensurePluginXML() throws Exception {
		IFile pluginXML = m_project.getFile("plugin.xml");
		if (!pluginXML.exists()) {
			List<String> lines =
					List.of(
							"<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
							"<?eclipse version=\"3.2\"?>",
							"<plugin>",
							"</plugin>",
							"");
			String contents = StringUtils.join(lines.iterator(), "\n");
			IOUtils2.setFileContents(pluginXML, new ByteArrayInputStream(contents.getBytes()));
			// close existing ManifestEditor, because it has no page for plugin.xml so will
			// not allow us later work with plugin.xml, in particular - access factory
			ExecutionUtils.runRethrowUI(new RunnableEx() {
				@Override
				public void run() throws Exception {
					EditorPart manifestEditor = PDEModelUtility.getOpenManifestEditor(m_project);
					if (manifestEditor != null) {
						DesignerPlugin.getActivePage().closeEditor(manifestEditor, true);
					}
				}
			});
		}
	}

	/**
	 * Runs {@link ModelModification} in UI thread.
	 */
	private static void modifyModel(final ModelModification modification) throws Exception {
		PDEModelUtility.modifyModel(modification, null);
	}

	/**
	 * Creates new extension element, for example "view" in "org.eclipse.ui.views" extension.
	 */
	public void createExtensionElement(final String pointId,
			final String elementName,
			final Map<String, String> attributes) throws Exception {
		if (!hasPDENature(m_project)) {
			return;
		}
		ensurePluginXML();
		ModelModification modification = new ModelModification(m_project) {
			@Override
			protected void modifyModel(IBaseModel model, IProgressMonitor monitor) throws CoreException {
				IPluginModelBase pluginModel = (IPluginModelBase) model;
				IExtensionsModelFactory extensionsFactory = pluginModel.getFactory();
				// prepare IPluginExtension to create new IPluginElement
				boolean newExtension;
				IPluginExtension extension;
				{
					List<IPluginExtension> extensions = getExtensions(pluginModel, pointId);
					if (extensions.isEmpty()) {
						newExtension = true;
						extension = extensionsFactory.createExtension();
						extension.setPoint(pointId);
					} else {
						newExtension = false;
						extension = extensions.get(extensions.size() - 1);
					}
				}
				// create IPluginElement
				IPluginElement element;
				{
					element = extensionsFactory.createElement(extension);
					element.setName(elementName);
					extension.add(element);
				}
				// set attributes
				for (Map.Entry<String, String> entry : attributes.entrySet()) {
					element.setAttribute(entry.getKey(), entry.getValue());
				}
				// if new IPluginExtension, add it into model
				// (if we add it before "element", we will have many <extension> elements,
				// probably because of some problem with PDE model listeners)
				if (newExtension) {
					pluginModel.getExtensions().add(extension);
				}
			}
		};
		modifyModel(modification);
	}

	/**
	 * Removes {@link IPluginElement} from {@link IPluginExtension}.
	 */
	public void removeElement(final IPluginElement element) throws Exception {
		IFile pluginFile = (IFile) element.getModel().getUnderlyingResource();
		ModelModification modification = new ModelModification(pluginFile) {
			@Override
			protected void modifyModel(IBaseModel model, IProgressMonitor monitor) throws CoreException {
				IPluginElement updatableElement = getCorrespondingElement(model, element);
				((IPluginParent) updatableElement.getParent()).remove(updatableElement);
			}
		};
		modifyModel(modification);
	}

	/**
	 * @return the {@link IPluginElement} that corresponds to the given one in new {@link IBaseModel}.
	 */
	private static IPluginElement getCorrespondingElement(IBaseModel model, IPluginElement element) {
		// check preconditions
		Assert.instanceOf(IPluginExtension.class, element.getParent());
		// prepare attributes
		final String pointId = ((IPluginExtension) element.getParent()).getPoint();
		final String elementName = element.getName();
		final String extensionId = getAttribute(element, "id");
		Assert.isNotNull(extensionId, "No point ID for extension.");
		Assert.isNotNull(extensionId, "No ID for element.");
		// find corresponding IPluginElement
		IPluginElement correspondingElement =
				getExtensionElement((IPluginModelBase) model, pointId, elementName, "id", extensionId);
		Assert.isNotNull(
				correspondingElement,
				"Can not find %s/%s/%s in %s",
				pointId,
				elementName,
				extensionId,
				model);
		return correspondingElement;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Icon access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * {@link Map} for <code>bundleId + path</code> into loaded {@link Image}.
	 */
	private static Map<String, ImageDescriptor> m_bundleIcons = new TreeMap<>();
	/**
	 * {@link Map} for <code>projectName + path</code> into loaded {@link Image}.
	 */
	private static Map<String, ImageDescriptor> m_projectIcons = new TreeMap<>();

	/**
	 * Returns icon from {@link IPluginElement}, attribute <code>"icon"</code>.
	 *
	 * @param element
	 *          the {@link IPluginElement} to get attribute from.
	 * @param defaultIconPath
	 *          the path to the default icon in {@link Activator}.
	 */
	public static ImageDescriptor getElementIcon(final IPluginElement element,
			final String attribute,
			ImageDescriptor defaultIcon) {
		return ExecutionUtils.runObjectIgnore(() -> {
			String iconPath = getAttribute(element, attribute);
			Assert.isNotNull(iconPath, "No attribute 'icon' in %s.", element);
			IPluginModelBase pluginModel = element.getPluginModel();
			IResource underlyingResource = pluginModel.getUnderlyingResource();
			if (underlyingResource != null) {
				IProject project = underlyingResource.getProject();
				String key = project.getName() + "/" + iconPath;
				// get icon from cache, or fill cache
				ImageDescriptor icon = m_projectIcons.get(key);
				if (icon == null) {
					IFile iconFile = project.getFile(new Path(iconPath));
					Assert.isTrue(iconFile.exists(), "Image " + key + " does not exists.");
					icon = ImageDescriptor.createFromURL(iconFile.getLocationURI().toURL());
					// remember icon in cache
					m_projectIcons.put(key, icon);
				}
				// OK, we should have icon
				return icon;
			} else {
				String bundleId = pluginModel.getBundleDescription().getSymbolicName();
				String key = bundleId + "/" + iconPath;
				// get icon from cache, or fill cache
				ImageDescriptor icon = m_bundleIcons.get(key);
				if (icon == null) {
					// prepare entry from Bundle
					URL entry;
					{
						Bundle bundle = Platform.getBundle(bundleId);
						entry = FileLocator.find(bundle, new Path(iconPath), null);
						Assert.isNotNull(entry, key);
					}
					// load Image from entry
					{
						icon = ImageDescriptor.createFromURL(entry);
					}
					// remember icon in cache
					m_bundleIcons.put(key, icon);
				}
				// OK, we should have icon
				return icon;
			}
		}, defaultIcon);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access for view categories
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Information about category in "org.eclipse.ui.views" extension.
	 */
	public static final class ViewCategoryInfo {
		private final String m_id;
		private final String m_name;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public ViewCategoryInfo(String id, String name) {
			m_id = id;
			m_name = name;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Object
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public String toString() {
			return "(" + m_id + ", " + m_name + ")";
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Access
		//
		////////////////////////////////////////////////////////////////////////////
		/**
		 * @return the ID of this {@link ViewCategoryInfo}.
		 */
		public String getId() {
			return m_id;
		}

		/**
		 * @return the name of this {@link ViewCategoryInfo}.
		 */
		public String getName() {
			return m_name;
		}

		/**
		 * @return the {@link ViewInfo}'s for views in this category.
		 */
		public List<ViewInfo> getViews() {
			final List<ViewInfo> views = new ArrayList<>();
			visitExtensions("org.eclipse.ui.views", "view", new IExtensionVisitor() {
				@Override
				public boolean visit(IPluginElement element) {
					String categoryId = getAttribute(element, "category");
					if (Objects.equals(m_id, categoryId)) {
						views.add(createViewInfo(element));
					}
					return false;
				}
			});
			return views;
		}
	}

	/**
	 * @return the {@link ViewCategoryInfo} constructed from given {@link IPluginElement}.
	 */
	private static ViewCategoryInfo createViewCategoryInfo(IPluginElement element) {
		String id = getAttribute(element, "id");
		String name = getAttribute(element, "name");
		return new ViewCategoryInfo(id, name);
	}

	/**
	 * @return the {@link ViewCategoryInfo}'s for each views category in Eclipse runtime/workspace.
	 */
	public static List<ViewCategoryInfo> getViewCategories() {
		final List<ViewCategoryInfo> categories = new ArrayList<>();
		categories.add(new ViewCategoryInfo(null, "Other"));
		visitExtensions("org.eclipse.ui.views", "category", new IExtensionVisitor() {
			@Override
			public boolean visit(IPluginElement element) {
				categories.add(createViewCategoryInfo(element));
				return false;
			}
		});
		return categories;
	}

	/**
	 * @return the {@link ViewCategoryInfo} for view with given ID, may be <code>null</code> if no
	 *         such category found.
	 */
	public static ViewCategoryInfo getViewCategoryInfo(final String categoryId) {
		final ViewCategoryInfo result[] = new ViewCategoryInfo[1];
		visitExtensions("org.eclipse.ui.views", "category", new IExtensionVisitor() {
			@Override
			public boolean visit(IPluginElement element) {
				if (categoryId != null) {
					String id = getAttribute(element, "id");
					if (categoryId.equals(id)) {
						result[0] = createViewCategoryInfo(element);
					}
				}
				return result[0] != null;
			}
		});
		// OK, we have result
		return result[0];
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access for views information (id, name, icon).
	// Read only, for perspectives.
	//
	////////////////////////////////////////////////////////////////////////////
	private static final ImageDescriptor DEFAULT_VIEW_ICON = Activator.getImageDescriptor("info/perspective/view.gif");
	/**
	 * Cache for view ID to {@link ViewInfo}.
	 */
	private static final Map<String, ViewInfo> m_viewsById = new TreeMap<>();

	/**
	 * Information about view in "org.eclipse.ui.views" extension.
	 */
	public static final class ViewInfo {
		private final String m_id;
		private final String m_className;
		private final String m_category;
		private final String m_name;
		private final ImageDescriptor m_icon;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public ViewInfo(String id, String className, String category, String name, ImageDescriptor icon) {
			m_id = id;
			m_className = className;
			m_category = category;
			m_name = name;
			m_icon = icon;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Object
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public String toString() {
			return "(" + m_id + ", " + m_className + ", " + m_category + ", " + m_name + ")";
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Access
		//
		////////////////////////////////////////////////////////////////////////////
		public String getId() {
			return m_id;
		}

		public String getClassName() {
			return m_className;
		}

		public String getCategory() {
			return m_category;
		}

		public String getName() {
			return m_name;
		}

		public ImageDescriptor getIcon() {
			return m_icon;
		}
	}

	/**
	 * @return the {@link ViewInfo} constructed from given {@link IPluginElement}.
	 */
	private static ViewInfo createViewInfo(IPluginElement element) {
		String id = getAttribute(element, "id");
		String className = getAttribute(element, "class");
		String category = getAttribute(element, "category");
		String name = getAttribute(element, "name");
		ImageDescriptor icon = getElementIcon(element, "icon", DEFAULT_VIEW_ICON);
		//
		return new ViewInfo(id, className, category, name, icon);
	}

	/**
	 * @return the {@link ViewInfo}'s for each view in Eclipse runtime/workspace.
	 */
	public static List<ViewInfo> getViews() {
		final List<ViewInfo> views = new ArrayList<>();
		visitExtensions("org.eclipse.ui.views", "view", new IExtensionVisitor() {
			@Override
			public boolean visit(IPluginElement element) {
				views.add(createViewInfo(element));
				return false;
			}
		});
		return views;
	}

	/**
	 * Creates new {@link IPluginElement} for Eclipse view category.
	 */
	public IPluginElement createViewCategoryElement(String id, String name) throws Exception {
		Map<String, String> attributes = Map.of("id", id, "name", name);
		createExtensionElement("org.eclipse.ui.views", "category", attributes);
		return waitExtensionElementById("org.eclipse.ui.views", "category", id);
	}

	/**
	 * Creates new {@link IPluginElement} for Eclipse view.
	 */
	public void createViewElement(String id, String name, String className) throws Exception {
		Map<String, String> attributes = Map.of("id", id, "name", name, "class", className);
		createExtensionElement("org.eclipse.ui.views", "view", attributes);
	}

	/**
	 * @return the {@link ViewInfo} for view with given ID, may be <code>null</code> if no such view
	 *         found.
	 */
	public static ViewInfo getViewInfo(final String viewId) {
		// check in cache
		if (m_viewsById.containsKey(viewId)) {
			return m_viewsById.get(viewId);
		}
		// visit all views
		final ViewInfo result[] = new ViewInfo[1];
		visitExtensions("org.eclipse.ui.views", "view", new IExtensionVisitor() {
			@Override
			public boolean visit(IPluginElement element) {
				if (viewId != null) {
					String id = getAttribute(element, "id");
					if (viewId.equals(id)) {
						result[0] = createViewInfo(element);
					}
				}
				return result[0] != null;
			}
		});
		// OK, we have result
		m_viewsById.put(viewId, result[0]);
		return result[0];
	}

	/**
	 * @return the {@link ViewInfo} for view with given ID, returns not <code>null</code> even if no
	 *         view with such ID.
	 */
	public static ViewInfo getViewInfoDefault(String viewId) {
		ViewInfo viewInfo = getViewInfo(viewId);
		if (viewInfo == null) {
			viewInfo = new ViewInfo(viewId, "NoClass", "NoCategory", viewId, DEFAULT_VIEW_ICON);
		}
		return viewInfo;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Perspectives access
	//
	////////////////////////////////////////////////////////////////////////////
	private static final ImageDescriptor DEFAULT_PERSPECTIVE_ICON = Activator
			.getImageDescriptor("info/perspective/perspective.gif");
	/**
	 * Cache for view ID to {@link ViewInfo}.
	 */
	private static final Map<String, PerspectiveInfo> m_perspectivesById = new TreeMap<>();

	/**
	 * Information about perspective in "org.eclipse.ui.perspectives" extension.
	 */
	public static class PerspectiveInfo {
		private final String m_id;
		private final String m_className;
		private final String m_name;
		private final ImageDescriptor m_icon;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public PerspectiveInfo(String id, String className, String name, ImageDescriptor icon) {
			m_id = id;
			m_className = className;
			m_name = name;
			m_icon = icon;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Object
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public String toString() {
			return "(" + m_id + ", " + m_className + ", " + m_name + ")";
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Access
		//
		////////////////////////////////////////////////////////////////////////////
		public String getId() {
			return m_id;
		}

		public String getClassName() {
			return m_className;
		}

		public String getName() {
			return m_name;
		}

		public ImageDescriptor getIcon() {
			return m_icon;
		}
	}

	/**
	 * @return the {@link PerspectiveInfo} constructed from given {@link IPluginElement}.
	 */
	private static PerspectiveInfo createPerspectiveInfo(IPluginElement element) {
		String id = getAttribute(element, "id");
		String className = getAttribute(element, "class");
		String name = getAttribute(element, "name");
		ImageDescriptor icon = getElementIcon(element, "icon", DEFAULT_PERSPECTIVE_ICON);
		return new PerspectiveInfo(id, className, name, icon);
	}

	/**
	 * @return the {@link PerspectiveInfo}'s for each perspective in Eclipse runtime/workspace.
	 */
	public static List<PerspectiveInfo> getPerspectives() {
		final List<PerspectiveInfo> perspectives = new ArrayList<>();
		visitExtensions("org.eclipse.ui.perspectives", "perspective", new IExtensionVisitor() {
			@Override
			public boolean visit(IPluginElement element) {
				perspectives.add(createPerspectiveInfo(element));
				return false;
			}
		});
		return perspectives;
	}

	/**
	 * Creates new {@link IPluginElement} for Eclipse perspective.
	 */
	public void createPerspectiveElement(String id, String name, String className) throws Exception {
		Map<String, String> attributes = Map.of("id", id, "name", name, "class", className);
		createExtensionElement("org.eclipse.ui.perspectives", "perspective", attributes);
	}

	/**
	 * @return the {@link PerspectiveInfo} for perspective with given ID, may be <code>null</code> if
	 *         no such perspective found.
	 */
	public static PerspectiveInfo getPerspectiveInfo(final String perspectiveId) {
		// check in cache
		if (m_perspectivesById.containsKey(perspectiveId)) {
			return m_perspectivesById.get(perspectiveId);
		}
		// visit all perspectives
		final PerspectiveInfo result[] = new PerspectiveInfo[1];
		visitExtensions("org.eclipse.ui.perspectives", "perspective", new IExtensionVisitor() {
			@Override
			public boolean visit(IPluginElement element) {
				String id = getAttribute(element, "id");
				if (perspectiveId.equals(id)) {
					result[0] = createPerspectiveInfo(element);
					return true;
				}
				return false;
			}
		});
		// OK, we have result
		m_perspectivesById.put(perspectiveId, result[0]);
		return result[0];
	}

	/**
	 * @return the {@link PerspectiveInfo} for perspective with given ID, returns not
	 *         <code>null</code> even if no perspective with such ID.
	 */
	public static PerspectiveInfo getPerspectiveInfoDefault(final String perspectiveId) {
		PerspectiveInfo perspectiveInfo = getPerspectiveInfo(perspectiveId);
		if (perspectiveInfo == null) {
			perspectiveInfo =
					new PerspectiveInfo(perspectiveId, "NoClass", perspectiveId, DEFAULT_PERSPECTIVE_ICON);
		}
		return perspectiveInfo;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Editor access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates new {@link IPluginElement} for Eclipse editor.
	 */
	public void createEditorElement(String id, String name, String className) throws Exception {
		Map<String, String> attributes = Map.of("id", id, "name", name, "class", className);
		createExtensionElement("org.eclipse.ui.editors", "editor", attributes);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Extensions visiting
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Visitor interface to use with
	 * {@link PdeUtils#visitExtensions(String, String, IExtensionVisitor)}.
	 */
	private interface IExtensionVisitor {
		/**
		 * Visits single extension {@link IPluginElement}.
		 *
		 * @return <code>true</code> if required result was found, so visiting should be terminated.
		 */
		boolean visit(IPluginElement element);
	}

	/**
	 * Visits extensions to the extension point with given ID and element name.
	 */
	private static void visitExtensions(String pointId, String elementName, IExtensionVisitor visitor) {
		IPluginModelBase[] plugins = getAllModels();
		for (IPluginModelBase plugin : plugins) {
			List<IPluginElement> elements = getExtensionElements(plugin, pointId, elementName);
			for (IPluginElement element : elements) {
				if (visitor.visit(element)) {
					return;
				}
			}
		}
	}
}
