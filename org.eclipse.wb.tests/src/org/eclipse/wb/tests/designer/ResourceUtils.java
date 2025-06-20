/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.tests.designer;

import org.eclipse.wb.tests.designer.core.TestProject;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.JavaModelException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Bundle;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

/**
 * Resources utils for tests.
 *
 * @author sablin_aa
 */
public class ResourceUtils {
	private static Bundle m_testBundle = Platform.getBundle("org.eclipse.wb.tests");

	/**
	 * @return {@link URL} on resource entry.
	 */
	public static URL getEntry(String name) {
		return m_testBundle.getEntry(name);
	}

	public static URL getEntry(String path, String name) {
		return getEntry(path + "/" + name);
	}

	private static void createFolderStructure(IContainer container) throws CoreException {
		if (!container.exists() && container instanceof IFolder && container.getParent() != null) {
			if (container.getParent().exists()) {
				((IFolder) container).create(true, true, null);
			} else {
				createFolderStructure(container.getParent());
			}
		}
	}

	/**
	 * Copy test resources from specified subpath to test project.
	 */
	public static void resources2project(TestProject project, String path, String[] skipEntries)
			throws IOException, CoreException, JavaModelException, Exception {
		Enumeration<URL> pathEntries = m_testBundle.findEntries(path, "*", true);
		assertNotNull(pathEntries);
		while (pathEntries.hasMoreElements()) {
			URL entryURL = pathEntries.nextElement();
			String entryPathBase = entryURL.getPath();
			String entryPath = entryPathBase.replaceFirst(path, StringUtils.EMPTY);
			try {
				// file
				InputStream entryStream = entryURL.openStream();
				IFile file = project.getProject().getFile(entryPath);
				if (entryPathBase.endsWith("/CVS/" + file.getName())) {
					// CVS-file
					continue;
				}
				if (!ArrayUtils.contains(skipEntries, file.getName())) {
					if (file.exists()) {
						file.setContents(entryStream, true, false, null);
					} else {
						createFolderStructure(file.getParent());
						file.create(entryStream, true, null);
					}
				}
			} catch (FileNotFoundException e) {
				// folder
				IFolder folder = project.getProject().getFolder(entryPath);
				if (folder.getName().equals("CVS")) {
					// CVS-directory
					continue;
				}
				if (!folder.exists()) {
					folder.create(true, true, null);
				}
			}
		}
	}

	public static void resources2project(TestProject project, String path)
			throws IOException, CoreException, JavaModelException, Exception {
		resources2project(project, path, new String[]{});
	}
}
