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
package org.eclipse.wb.internal.swing.laf;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.swing.laf.model.LafInfo;
import org.eclipse.wb.internal.swing.laf.model.UserDefinedLafInfo;

import org.eclipse.core.runtime.IProgressMonitor;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.LookAndFeel;

/**
 * Helper class to manage user-defined LAFs. Used by UI providing add/edit/delete operations.
 *
 * @author mitin_aa
 * @coverage swing.laf
 */
public final class LafUtils {
	////////////////////////////////////////////////////////////////////////////
	//
	// Private constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private LafUtils() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// JAR utilities
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Opens given <code>jarFile</code>, loads every class inside own {@link ClassLoader} and checks
	 * loaded class for to be instance of {@link LookAndFeel}. Returns the array of {@link LafInfo}
	 * containing all found {@link LookAndFeel} classes.
	 *
	 * @param jarFileName
	 *          the absolute OS path pointing to source JAR file.
	 * @param monitor
	 *          the progress monitor which checked for interrupt request.
	 * @return the array of {@link UserDefinedLafInfo} containing all found {@link LookAndFeel}
	 *         classes.
	 */
	public static UserDefinedLafInfo[] scanJarForLookAndFeels(String jarFileName,
			IProgressMonitor monitor) throws Exception {
		List<UserDefinedLafInfo> lafList = new ArrayList<>();
		File jarFile = new File(jarFileName);
		URLClassLoader ucl = new URLClassLoader(new URL[]{jarFile.toURI().toURL()});
		JarFile jar = new JarFile(jarFile);
		Enumeration<?> entries = jar.entries();
		while (entries.hasMoreElements()) {
			JarEntry entry = (JarEntry) entries.nextElement();
			String entryName = entry.getName();
			if (entry.isDirectory() || !entryName.endsWith(".class") || entryName.indexOf('$') >= 0) {
				continue;
			}
			String className = entryName.replace('/', '.').replace('\\', '.');
			className = className.substring(0, className.lastIndexOf('.'));
			Class<?> clazz = null;
			try {
				clazz = ucl.loadClass(className);
			} catch (Throwable e) {
				continue;
			}
			// check loaded class to be a non-abstract subclass of javax.swing.LookAndFeel class
			if (LookAndFeel.class.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.getModifiers())) {
				// use the class name as name of LAF
				String shortClassName = CodeUtils.getShortClass(className);
				// strip trailing "LookAndFeel"
				String lafName = StringUtils.removeEnd(shortClassName, "LookAndFeel");
				lafList.add(new UserDefinedLafInfo(StringUtils.isEmpty(lafName) ? shortClassName : lafName,
						className,
						jarFileName));
			}
			// check for Cancel button pressed
			if (monitor.isCanceled()) {
				return lafList.toArray(new UserDefinedLafInfo[lafList.size()]);
			}
			// update ui
			while (DesignerPlugin.getStandardDisplay().readAndDispatch()) {
			}
		}
		return lafList.toArray(new UserDefinedLafInfo[lafList.size()]);
	}
}
