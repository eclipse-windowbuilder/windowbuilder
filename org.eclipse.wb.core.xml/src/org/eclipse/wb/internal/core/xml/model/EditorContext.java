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
package org.eclipse.wb.internal.core.xml.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.core.model.broadcast.BroadcastSupport;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.description.helpers.DescriptionHelper;
import org.eclipse.wb.internal.core.model.description.helpers.ILoadingContext;
import org.eclipse.wb.internal.core.model.description.resource.IDescriptionVersionsProvider;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.utils.reflect.BundleClassLoader;
import org.eclipse.wb.internal.core.utils.reflect.CompositeClassLoader;
import org.eclipse.wb.internal.core.utils.reflect.IByteCodeProcessor;
import org.eclipse.wb.internal.core.utils.reflect.ProjectClassLoader;
import org.eclipse.wb.internal.core.utils.state.EditorWarning;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.internal.core.utils.xml.AbstractDocumentHandler;
import org.eclipse.wb.internal.core.utils.xml.DocumentEditContext;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.IExceptionConstants;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.IDocument;

import org.apache.commons.lang.StringUtils;
import org.osgi.framework.Bundle;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;

/**
 * Context of XML editing.
 *
 * @author scheglov_ke
 * @coverage XML.model
 */
public abstract class EditorContext {
	private final BroadcastSupport m_broadcastSupport = new BroadcastSupport();
	private final ToolkitDescription m_toolkit;
	private final String m_toolkitId;
	private final IDocument m_document;
	protected final IFile m_file;
	protected final IProject m_project;
	protected final IJavaProject m_javaProject;
	protected ClassLoader m_classLoader;
	private ILoadingContext m_loadingContext;
	private DocumentEditContext m_documentEditContext;
	private DocumentElement m_rootElement;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public EditorContext(ToolkitDescription toolkit, IFile file, IDocument document) throws Exception {
		m_toolkit = toolkit;
		m_toolkitId = toolkit.getId();
		m_document = document;
		m_file = file;
		m_project = file.getProject();
		// Java project
		m_javaProject = JavaCore.create(m_project);
		if (!m_javaProject.exists()) {
			throw new DesignerException(IExceptionConstants.NOT_JAVA_PROJECT,
					m_file.getFullPath().toPortableString(),
					m_project.getFullPath().toPortableString());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Performs heavy initialization.
	 */
	public void initialize() throws Exception {
		createClassLoader();
		m_loadingContext = new XmlLoadingContext(this);
		m_documentEditContext = new DocumentEditContext(m_document) {
			@Override
			protected AbstractDocumentHandler createDocumentHandler() {
				return new AbstractDocumentHandler();
			}
		};
		m_rootElement = m_documentEditContext.getRoot();
		// set charset
		{
			String charset = m_file.getCharset();
			m_rootElement.getModel().setCharset(charset);
		}
	}

	/**
	 * Commits changes into underlying XML.
	 */
	public void commit() throws Exception {
		m_broadcastSupport.getListener(EditorContextCommitListener.class).aboutToCommit();
		m_documentEditContext.commit();
		m_broadcastSupport.getListener(EditorContextCommitListener.class).doneCommit();
	}

	/**
	 * Disposes resources allocated globally for this editor.
	 */
	public void dispose() throws Exception {
		m_documentEditContext.disconnect();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ClassLoader
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates {@link ClassLoader} in {@link #m_classLoader}.
	 */
	protected void createClassLoader() throws Exception {
		// prepare "project" ClassLoader
		ClassLoader projectClassLoader;
		{
			CompositeClassLoader parentClassLoader = new CompositeClassLoader();
			addParentClassLoaders(parentClassLoader);
			projectClassLoader = createProjectClassLoader(parentClassLoader);
		}
		// fill CompositeClassLoader
		CompositeClassLoader compositeClassLoader = new CompositeClassLoader();
		compositeClassLoader.add(projectClassLoader, null);
		addLibraryClassLoaders(compositeClassLoader, projectClassLoader);
		// done
		m_classLoader = compositeClassLoader;
	}

	/**
	 * Creates {@link ClassLoader} that includes classes of {@link IJavaProject}.
	 */
	protected ClassLoader createProjectClassLoader(CompositeClassLoader parentClassLoader)
			throws Exception {
		return ProjectClassLoader.create(parentClassLoader, m_javaProject);
	}

	/**
	 * Adds {@link ClassLoader}s to use as parent for project {@link ClassLoader}.
	 */
	protected void addParentClassLoaders(CompositeClassLoader parentClassLoader) throws Exception {
		// add class loaders for "classLoader-bundle" contributions
		List<IConfigurationElement> toolkitElements = DescriptionHelper.getToolkitElements(m_toolkitId);
		for (IConfigurationElement toolkitElement : toolkitElements) {
			IConfigurationElement[] contributorElements =
					toolkitElement.getChildren("classLoader-bundle");
			for (IConfigurationElement contributorElement : contributorElements) {
				Bundle bundle = getExistingBundle(contributorElement);
				List<String> namespaces = getBundleClassLoaderNamespaces(contributorElement);
				ClassLoader classLoader = BundleClassLoader.create(bundle);
				parentClassLoader.add(classLoader, namespaces);
			}
		}
	}

	/**
	 * @return the namespaces for {@link CompositeClassLoader} from "namespaces" attribute.
	 */
	private static List<String> getBundleClassLoaderNamespaces(IConfigurationElement contributorElement) {
		String namespacesString = contributorElement.getAttribute("namespaces");
		if (namespacesString != null) {
			return ImmutableList.copyOf(StringUtils.split(namespacesString));
		}
		return null;
	}

	/**
	 * @return the composite {@link ClassLoader} based on given main {@link ClassLoader} with addition
	 *         of {@link BundleClassLoader}'s and {@link IByteCodeProcessor}'s.
	 */
	private void addLibraryClassLoaders(CompositeClassLoader compositeClassLoader,
			ClassLoader mainClassLoader) throws Exception {
		List<IConfigurationElement> toolkitElements = DescriptionHelper.getToolkitElements(m_toolkitId);
		for (IConfigurationElement toolkitElement : toolkitElements) {
			IConfigurationElement[] contributorElements =
					toolkitElement.getChildren("classLoader-library");
			if (contributorElements.length != 0) {
				URL[] urls = new URL[contributorElements.length];
				for (int i = 0; i < contributorElements.length; i++) {
					IConfigurationElement contributorElement = contributorElements[i];
					Bundle bundle = getExistingBundle(contributorElement);
					// prepare URL for JAR
					String jarPath = contributorElement.getAttribute("jar");
					URL jarEntry = bundle.getEntry(jarPath);
					Assert.isNotNull(jarEntry, "Unable to find %s in %s", jarPath, bundle.getSymbolicName());
					urls[i] = FileLocator.toFileURL(jarEntry);
				}
				// add ClassLoader with all libraries
				compositeClassLoader.add(new URLClassLoader(urls, mainClassLoader), null);
			}
		}
	}

	/**
	 * @return {@link Bundle} specified in attribute "bundle".
	 */
	private static Bundle getExistingBundle(IConfigurationElement contributorElement) {
		String bundleId = ExternalFactoriesHelper.getRequiredAttribute(contributorElement, "bundle");
		return ExternalFactoriesHelper.getRequiredBundle(bundleId);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link ToolkitDescription} of UI in this editor.
	 */
	public ToolkitDescription getToolkit() {
		return m_toolkit;
	}

	/**
	 * @return the {@link BroadcastSupport} for editor.
	 */
	public BroadcastSupport getBroadcastSupport() {
		return m_broadcastSupport;
	}

	/**
	 * @return the {@link ClassLoader} for loading classes in this editing context.
	 */
	public ClassLoader getClassLoader() {
		return m_classLoader;
	}

	/**
	 * @return the {@link ILoadingContext} to use in D2Core helpers.
	 */
	public ILoadingContext getLoadingContext() {
		return m_loadingContext;
	}

	/**
	 * @return the XML file.
	 */
	public IFile getFile() {
		return m_file;
	}

	/**
	 * @return the enclosing {@link IJavaProject}.
	 */
	public IJavaProject getJavaProject() {
		return m_javaProject;
	}

	/**
	 * @return the {@link IDocument} to commit changes to.
	 */
	public IDocument getDocument() {
		return m_document;
	}

	/**
	 * @return the the content of XML file.
	 */
	public String getContent() {
		return m_documentEditContext.getText();
	}

	/**
	 * @return the root {@link DocumentElement} in XML.
	 */
	public DocumentElement getRootElement() {
		return m_rootElement;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Parsing flag
	//
	////////////////////////////////////////////////////////////////////////////
	private boolean m_parsing;

	/**
	 * @return <code>true</code> if model hierarchy is in process of parsing.
	 */
	public boolean isParsing() {
		return m_parsing;
	}

	/**
	 * Specifies if model hierarchy is in process of parsing.
	 */
	public void setParsing(boolean parsing) {
		m_parsing = parsing;
		GlobalState.setParsing(parsing);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IDescriptionVersionsProvider
	//
	////////////////////////////////////////////////////////////////////////////
	private final Map<String, Object> m_versions = Maps.newHashMap();
	private final List<IDescriptionVersionsProvider> m_descriptionVersionsProviders =
			Lists.newArrayList();

	/**
	 * @return the {@link Map} of versions for toolkit in this {@link EditorContext}.
	 */
	public Map<String, Object> getVersions() {
		return m_versions;
	}

	/**
	 * @see IDescriptionVersionsProvider
	 */
	public List<IDescriptionVersionsProvider> getDescriptionVersionsProviders() {
		return m_descriptionVersionsProviders;
	}

	public void addVersions(Map<String, ?> versions) {
		m_versions.putAll(versions);
	}

	public void addDescriptionVersionsProvider(IDescriptionVersionsProvider provider) {
		Assert.isNotNull(provider);
		if (!m_descriptionVersionsProviders.contains(provider)) {
			m_descriptionVersionsProviders.add(provider);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Warnings
	//
	////////////////////////////////////////////////////////////////////////////
	private final List<EditorWarning> m_warnings = Lists.newArrayList();

	/**
	 * @return the {@link List} of {@link EditorWarning}'s.
	 */
	public List<EditorWarning> getWarnings() {
		return m_warnings;
	}

	/**
	 * Adds new {@link EditorWarning}.
	 */
	public void addWarning(EditorWarning warning) {
		m_warnings.add(warning);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// "Live component" flag
	//
	////////////////////////////////////////////////////////////////////////////
	private boolean m_isLiveComponent;

	/**
	 * @return <code>true</code> if model hierarchy is in process of "live component" operation.
	 */
	public boolean isLiveComponent() {
		return m_isLiveComponent;
	}

	/**
	 * Specifies if model hierarchy is in process of "live component".
	 */
	public void setLiveComponent(boolean isLiveComponent) {
		m_isLiveComponent = isLiveComponent;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ILiveEditorContext
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link ILiveEditorContext} for current toolkit.
	 */
	public abstract ILiveEditorContext getLiveContext();
}
