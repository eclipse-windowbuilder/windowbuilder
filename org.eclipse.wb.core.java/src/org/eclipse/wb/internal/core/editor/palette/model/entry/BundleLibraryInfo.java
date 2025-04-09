/*******************************************************************************
 * Copyright (c) 2024 Patrick Ziegler and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.editor.palette.model.entry;

import org.eclipse.wb.core.editor.palette.model.entry.ComponentEntryInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

import java.io.File;
import java.io.IOException;
import java.util.function.Predicate;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Library is description of OSGi bundles that should be added when
 * {@link ComponentEntryInfo} selected from palette.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette
 */
public final class BundleLibraryInfo implements LibraryInfo {
	public static final QualifiedName VERSION = new QualifiedName(DesignerPlugin.PLUGIN_ID, "version");
	private final String typeName;
	private final String symbolicName;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public BundleLibraryInfo(IConfigurationElement element) {
		typeName = ExternalFactoriesHelper.getRequiredAttribute(element, "type");
		symbolicName = ExternalFactoriesHelper.getRequiredAttribute(element, "symbolicName");
	}

	public BundleLibraryInfo(String symbolicName, String typeName) {
		this.typeName = typeName;
		this.symbolicName = symbolicName;
	}

	@Override
	public void ensure(IJavaProject javaProject) throws Exception {
		IType type = javaProject.findType(typeName);
		Bundle bundle = ExternalFactoriesHelper.getRequiredBundle(symbolicName);

		if (type != null) {
			IResource bundleFile = type.getResource();
			if (bundleFile == null) {
				// Added as external library to the classpath
				return;
			}

			Version bundleVersion = getBundleVersion(bundleFile);

			if (bundleVersion == null || bundleVersion.compareTo(bundle.getVersion()) < 0) {
				type = null;
				IPath bundlePath = bundleFile.getFullPath();
				Predicate<IClasspathEntry> versionComparator = classpath -> bundlePath.equals(classpath.getPath());
				ProjectUtils.removeClasspathEntries(javaProject, versionComparator);
				bundleFile.delete(true, null);
				ProjectUtils.waitForAutoBuild();
			}
		}

		if (type == null) {
			File bundleFile = FileLocator.getBundleFileLocation(bundle).orElse(null);
			if (bundleFile != null) {
				// add JAR
				ProjectUtils.addJar(javaProject, bundleFile.getAbsolutePath(), null);
				ProjectUtils.waitForAutoBuild();
			} else {
				DesignerPlugin.log(Status.warning("Unable to find bundle file " + bundleFile));
			}
		}
	}

	/**
	 * Extracts and returns the version of {@link #typeName} using the Manifest
	 * entry of the containing bundle. For performance reason, the Manifest is only
	 * read once and the version then stored in the file properties.
	 *
	 * @param bundleResource The jar file declaring {@link #typeName}.
	 * @return The bundle version or {@code null}, if none could be found.
	 */
	private Version getBundleVersion(IResource bundleResource) {
		if (bundleResource instanceof IFile bundleFile) {
			// Check file properties first to increase performance of successive reads
			try {
				String versionString = bundleResource.getPersistentProperty(VERSION);
				if (versionString != null) {
					return Version.parseVersion(versionString);
				}
			} catch (CoreException e) {
				DesignerPlugin.log(e);
			}
			// Read Bundle-Version header from manifest instead
			File javaFile = bundleFile.getLocation().toFile();
			try (JarFile jarFile = new JarFile(javaFile)) {
				Manifest manifest = jarFile.getManifest();
				Attributes attributes = manifest.getMainAttributes();
				String versionString = attributes.getValue(Constants.BUNDLE_VERSION);
				bundleResource.setPersistentProperty(VERSION, versionString);
				return Version.parseVersion(versionString);
			} catch (CoreException | IOException e) {
				DesignerPlugin.log(e);
			}
		}
		return null;
	}
}
