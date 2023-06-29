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
package org.eclipse.wb.internal.xwt.parser;

import com.google.common.collect.ImmutableMap;

import org.eclipse.wb.internal.core.model.description.resource.IDescriptionVersionsProvider;
import org.eclipse.wb.internal.core.model.description.resource.IDescriptionVersionsProviderFactory;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.utils.reflect.BundleClassLoader;
import org.eclipse.wb.internal.core.utils.reflect.CompositeClassLoader;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.ILiveEditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.rcp.RcpToolkitDescription;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import org.apache.commons.lang.StringUtils;

import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.List;

/**
 * {@link EditorContext} for XWT.
 *
 * @author scheglov_ke
 * @coverage XWT.parser
 */
public final class XwtEditorContext extends EditorContext {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public XwtEditorContext(IFile file, IDocument document) throws Exception {
		super(RcpToolkitDescription.INSTANCE, file, document);
		configureDescriptionVersionsProviders();
		addVersions(ImmutableMap.of("isXWT", "true"));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ClassLoader
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void addParentClassLoaders(final CompositeClassLoader parentClassLoader)
			throws Exception {
		super.addParentClassLoaders(parentClassLoader);
		AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
			public Object run() throws Exception {
				addParentClassLoaders_impl(parentClassLoader);
				return null;
			}
		});
	}

	private static void addParentClassLoaders_impl(CompositeClassLoader parentClassLoader) {
		parentClassLoader.add(new BundleClassLoader("org.eclipse.wb.xwt"), null);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Live support
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public ILiveEditorContext getLiveContext() {
		return m_liveEditorContext;
	}

	private final ILiveEditorContext m_liveEditorContext = new ILiveEditorContext() {
		public XmlObjectInfo parse(String[] sourceLines) throws Exception {
			XmlObjectInfo root;
			{
				String source = StringUtils.join(sourceLines, "\n");
				IDocument document = new Document(source);
				XwtParser parser = new XwtParser(m_file, document);
				root = parser.parse();
			}
			root.getContext().setLiveComponent(true);
			return root;
		}

		public void dispose() throws Exception {
		}
	};

	////////////////////////////////////////////////////////////////////////////
	//
	// IDescriptionVersionsProvider
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Installs {@link IDescriptionVersionsProvider}'s.
	 */
	private void configureDescriptionVersionsProviders() throws Exception {
		List<IDescriptionVersionsProviderFactory> factories =
				ExternalFactoriesHelper.getElementsInstances(
						IDescriptionVersionsProviderFactory.class,
						"org.eclipse.wb.core.descriptionVersionsProviderFactories",
						"factory");
		for (IDescriptionVersionsProviderFactory factory : factories) {
			// versions
			addVersions(factory.getVersions(m_javaProject, m_classLoader));
			// version providers
			{
				IDescriptionVersionsProvider provider = factory.getProvider(m_javaProject, m_classLoader);
				if (provider != null) {
					addDescriptionVersionsProvider(provider);
				}
			}
		}
	}
}
