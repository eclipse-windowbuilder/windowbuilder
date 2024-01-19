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
package org.eclipse.wb.internal.swt.model.property.editor.image.plugin;

import org.eclipse.wb.internal.core.utils.xml.AbstractDocumentHandler;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.utils.xml.FileDocumentEditContext;
import org.eclipse.wb.internal.core.utils.xml.Model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Special utils for union different code depending on Eclipse version (3.3.4).
 *
 * @author lobas_av
 * @coverage swt.property.editor.plugin
 */
public final class WorkspacePluginInfo {
	public static final IPath MANIFEST_PATH = new Path("META-INF/MANIFEST.MF");
	public static final IPath PLUGIN_PATH = new Path("plugin.xml");

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return <i>bundle symbolic name</i> for given project or <code>null</code> if project is't
	 *         plugin.
	 */
	public static String getBundleSymbolicName(IProject project) {
		// handle META-INF/MANIFEST.MF
		try {
			IFile manifestFile = project.getFile(MANIFEST_PATH);
			if (manifestFile.exists()) {
				Map<String, String> manifest = parseBundleManifest(manifestFile);
				String bundleSymbolicName = manifest.get("Bundle-SymbolicName");
				if (!StringUtils.isEmpty(bundleSymbolicName)) {
					bundleSymbolicName = StringUtils.substringBefore(bundleSymbolicName, ";").trim();
					if (!StringUtils.isEmpty(bundleSymbolicName)) {
						return bundleSymbolicName;
					}
				}
			}
		} catch (Exception e) {
		}
		// handle plugin.xml
		try {
			IFile pluginFile = project.getFile(MANIFEST_PATH);
			if (pluginFile.exists()) {
				DocumentElement pluginNode = parsePluginXml(pluginFile);
				if ("plugin".equalsIgnoreCase(pluginNode.getTag())) {
					String bundleSymbolicName = pluginNode.getAttribute("id");
					if (!StringUtils.isEmpty(bundleSymbolicName)) {
						return bundleSymbolicName;
					}
				}
			}
		} catch (Exception e) {
		}
		return null;
	}

	public static String getBundleActivator(IProject project) {
		// handle META-INF/MANIFEST.MF
		try {
			IFile manifestFile = project.getFile(MANIFEST_PATH);
			if (manifestFile.exists()) {
				Map<String, String> manifest = parseBundleManifest(manifestFile);
				String bundleActivator = manifest.get("Bundle-Activator");
				if (!StringUtils.isEmpty(bundleActivator)) {
					return bundleActivator;
				}
			}
		} catch (Exception e) {
		}
		// handle plugin.xml
		try {
			IFile pluginFile = project.getFile(MANIFEST_PATH);
			if (pluginFile.exists()) {
				DocumentElement pluginNode = parsePluginXml(pluginFile);
				if ("plugin".equalsIgnoreCase(pluginNode.getTag())) {
					String bundleActivator = pluginNode.getAttribute("class");
					if (!StringUtils.isEmpty(bundleActivator)) {
						return bundleActivator;
					}
				}
			}
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * @return {@link List} collection with plugin requirements for give project.
	 */
	public static List<String> getRequiredPlugins(IProject project) {
		// handle META-INF/MANIFEST.MF
		try {
			IFile manifestFile = project.getFile(MANIFEST_PATH);
			if (manifestFile.exists()) {
				Map<String, String> manifest = parseBundleManifest(manifestFile);
				String requireBundle = manifest.get("Require-Bundle");
				if (!StringUtils.isEmpty(requireBundle)) {
					String[] requireBundles = StringUtils.split(requireBundle.trim(), ",");
					List<String> result = new ArrayList<>();
					for (String bundle : requireBundles) {
						if (!StringUtils.isEmpty(bundle)) {
							result.add(bundle);
						}
					}
					return result;
				}
			}
		} catch (Exception e) {
		}
		// handle plugin.xml
		try {
			IFile pluginFile = project.getFile(MANIFEST_PATH);
			if (pluginFile.exists()) {
				DocumentElement pluginNode = parsePluginXml(pluginFile);
				if ("plugin".equalsIgnoreCase(pluginNode.getTag())) {
					for (DocumentElement child : pluginNode.getChildren()) {
						if ("requires".equalsIgnoreCase(child.getTag())) {
							List<String> result = new ArrayList<>();
							for (DocumentElement importNode : child.getChildren()) {
								if ("import".equalsIgnoreCase(importNode.getTag())) {
									String bundle = importNode.getAttribute("plugin");
									if (!StringUtils.isEmpty(bundle)) {
										result.add(bundle);
									}
								}
								return result;
							}
						}
					}
				}
			}
		} catch (Exception e) {
		}
		return Collections.emptyList();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Manifest
	//
	////////////////////////////////////////////////////////////////////////////
	private static Map<String, String> parseBundleManifest(IFile manifestFile) throws Exception {
		Map<String, String> headers = new HashMap<>();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(manifestFile.getContents(true), "UTF8"));
		} catch (UnsupportedEncodingException e) {
			reader = new BufferedReader(new InputStreamReader(manifestFile.getContents(true)));
		}
		try {
			String header = null;
			StringBuffer value = new StringBuffer(256);
			boolean firstLine = true;
			while (true) {
				String line = reader.readLine();
				if (line == null || line.length() == 0) {
					if (!firstLine) {
						headers.put(header, value.toString().trim());
					}
					break;
				}
				if (line.charAt(0) == ' ') {
					if (firstLine) {
						throw new Exception();
					}
					value.append(line.substring(1));
					continue;
				}
				if (!firstLine) {
					headers.put(header, value.toString().trim());
					value.setLength(0);
				}
				int colon = line.indexOf(':');
				if (colon == -1) {
					throw new Exception();
				}
				header = line.substring(0, colon).trim();
				value.append(line.substring(colon + 1));
				firstLine = false;
			}
		} finally {
			IOUtils.closeQuietly(reader);
		}
		return headers;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// XML
	//
	////////////////////////////////////////////////////////////////////////////
	private static DocumentElement parsePluginXml(IFile pluginFile) throws Exception {
		FileDocumentEditContext context = new FileDocumentEditContext(pluginFile) {
			@Override
			protected AbstractDocumentHandler createDocumentHandler() {
				return new AbstractDocumentHandler() {
					@Override
					protected DocumentElement getDocumentNode(String name, DocumentElement parent) {
						DocumentElement documentNode = new DocumentElement();
						if (parent == null) {
							documentNode.setModel(new Model());
						}
						return documentNode;
					}
				};
			}
		};
		context.disconnect();
		return context.getRoot();
	}
}