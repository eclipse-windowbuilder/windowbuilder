/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.core;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.pde.core.IBaseModel;
import org.eclipse.pde.core.build.IBuild;
import org.eclipse.pde.core.build.IBuildEntry;
import org.eclipse.pde.core.plugin.IPlugin;
import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.core.plugin.IPluginImport;
import org.eclipse.pde.core.plugin.IPluginLibrary;
import org.eclipse.pde.core.plugin.IPluginModelFactory;
import org.eclipse.pde.core.project.IBundleProjectDescription;
import org.eclipse.pde.internal.core.TargetPlatformHelper;
import org.eclipse.pde.internal.core.WorkspaceModelManager;
import org.eclipse.pde.internal.core.build.WorkspaceBuildModel;
import org.eclipse.pde.internal.core.bundle.WorkspaceBundlePluginModel;
import org.eclipse.pde.internal.core.ibundle.IBundlePluginModelBase;
import org.eclipse.pde.internal.core.util.CoreUtility;
import org.eclipse.pde.internal.core.util.IdUtil;
import org.eclipse.pde.internal.ui.util.ModelModification;
import org.eclipse.pde.internal.ui.util.PDEModelUtility;
import org.eclipse.pde.internal.ui.wizards.tools.OrganizeManifest;

import org.osgi.framework.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilities for operating with PDE projects.
 *
 * @author scheglov_ke
 */
public class PdeProjectConversionUtils {
	public final static String BUNDLE_FILENAME_DESCRIPTOR = "META-INF/MANIFEST.MF";
	public final static String BUILD_FILENAME_DESCRIPTOR = "build.properties";

	////////////////////////////////////////////////////////////////////////////
	//
	// Static access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Converts given {@link IProject} into PDE project.
	 *
	 * If <code>hostPluginId</code> is not <code>null</code>, fragment will be created.
	 */
	public static void convertToPDE(IProject project, String hostPluginId) throws CoreException {
		new PdeProjectConversionUtils(hostPluginId).convertProject(project, new NullProgressMonitor());
	}

	/**
	 * Converts given {@link IProject} into PDE project.
	 *
	 * If <code>hostPluginId</code> is not <code>null</code>, fragment will be created.
	 */
	public static void convertToPDE(IProject project, String hostPluginId, String pluginActivator)
			throws CoreException {
		new PdeProjectConversionUtils(hostPluginId, pluginActivator).convertProject(
				project,
				new NullProgressMonitor());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Instance fields
	//
	////////////////////////////////////////////////////////////////////////////
	private final String m_hostPluginId;
	private final String m_pluginActivator;
	private String fLibraryName;
	private String[] fSrcEntries;
	private String[] fLibEntries;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PdeProjectConversionUtils(String hostPluginId) {
		this(hostPluginId, null);
	}

	public PdeProjectConversionUtils(String hostPluginId, String pluginActivator) {
		m_hostPluginId = hostPluginId;
		m_pluginActivator = pluginActivator;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	////////////////////////////////////////////////////////////////////////////
	private void convertProject(IProject project, IProgressMonitor monitor) throws CoreException {
		CoreUtility.addNatureToProject(project, IBundleProjectDescription.PLUGIN_NATURE, monitor);
		//
		loadClasspathEntries(project, monitor);
		loadLibraryName(project);
		//
		if (!WorkspaceModelManager.isPluginProject(project)) {
			createManifestFile(project.getFile(BUNDLE_FILENAME_DESCRIPTOR), monitor);
		}
		IFile buildFile = project.getFile(BUILD_FILENAME_DESCRIPTOR);
		if (!buildFile.exists()) {
			WorkspaceBuildModel model = new WorkspaceBuildModel(buildFile);
			IBuild build = model.getBuild(true);
			IBuildEntry entry = model.getFactory().createEntry("bin.includes"); //$NON-NLS-1$
			if (project.getFile("plugin.xml").exists()) {
				entry.addToken("plugin.xml"); //$NON-NLS-1$
			}
			if (project.getFile("META-INF/MANIFEST.MF").exists()) {
				entry.addToken("META-INF/"); //$NON-NLS-1$
			}
			for (int i = 0; i < fLibEntries.length; i++) {
				entry.addToken(fLibEntries[i]);
			}
			//
			if (fSrcEntries.length > 0) {
				entry.addToken(fLibraryName);
				IBuildEntry source = model.getFactory().createEntry("source." + fLibraryName); //$NON-NLS-1$
				for (int i = 0; i < fSrcEntries.length; i++) {
					source.addToken(fSrcEntries[i]);
				}
				build.add(source);
			}
			if (entry.getTokens().length > 0) {
				build.add(entry);
			}
			model.save();
		}
	}

	private void loadClasspathEntries(IProject project, IProgressMonitor monitor) {
		IJavaProject javaProject = JavaCore.create(project);
		IClasspathEntry[] currentClassPath = new IClasspathEntry[0];
		List<String> sources = new ArrayList<>();
		List<String> libraries = new ArrayList<>();
		try {
			currentClassPath = javaProject.getRawClasspath();
		} catch (JavaModelException e) {
		}
		for (int i = 0; i < currentClassPath.length; i++) {
			int contentType = currentClassPath[i].getEntryKind();
			if (contentType == IClasspathEntry.CPE_SOURCE) {
				String relativePath = getRelativePath(currentClassPath[i], project);
				if (relativePath.equals("")) { //$NON-NLS-1$
					sources.add("."); //$NON-NLS-1$
				} else {
					sources.add(relativePath + "/"); //$NON-NLS-1$
				}
			} else if (contentType == IClasspathEntry.CPE_LIBRARY) {
				String path = getRelativePath(currentClassPath[i], project);
				if (path.length() > 0) {
					libraries.add(path);
				} else {
					libraries.add("."); //$NON-NLS-1$
				}
			}
		}
		fSrcEntries = sources.toArray(new String[sources.size()]);
		fLibEntries = libraries.toArray(new String[libraries.size()]);
		//
		IClasspathEntry[] classPath = new IClasspathEntry[currentClassPath.length + 1];
		System.arraycopy(currentClassPath, 0, classPath, 0, currentClassPath.length);
		try {
			classPath[classPath.length - 1] = createContainerEntry();
			javaProject.setRawClasspath(classPath, monitor);
		} catch (Throwable e) {
		}
	}

	private static IClasspathEntry createContainerEntry() throws Exception {
		return (IClasspathEntry) ReflectionUtils.invokeMethod(
				getClass_ClasspathComputer(),
				"createContainerEntry()");
	}

	private static Object getClass_ClasspathComputer() throws ClassNotFoundException {
		try {
			return Class.forName("org.eclipse.pde.internal.core.ClasspathComputer");
		} catch (Throwable e) {
			return Class.forName("org.eclipse.pde.internal.ui.wizards.plugin.ClasspathComputer");
		}
	}

	private String getRelativePath(IClasspathEntry cpe, IProject project) {
		IPath path = project.getFile(cpe.getPath()).getProjectRelativePath();
		return path.removeFirstSegments(1).toString();
	}

	private void loadLibraryName(IProject project) {
		if (isOldTarget() || fLibEntries.length > 0 && fSrcEntries.length > 0) {
			String libName = project.getName();
			int i = libName.lastIndexOf("."); //$NON-NLS-1$
			if (i != -1) {
				libName = libName.substring(i + 1);
			}
			fLibraryName = libName + ".jar"; //$NON-NLS-1$
		} else {
			fLibraryName = "."; //$NON-NLS-1$
		}
	}

	private boolean isOldTarget() {
		return TargetPlatformHelper.getTargetVersion() < 3.1;
	}

	private String createInitialName(String id) {
		if (m_hostPluginId != null) {
			id += " Fragment";
		}
		//
		int loc = id.lastIndexOf('.');
		if (loc != -1) {
			return id;
		}
		StringBuffer buf = new StringBuffer(id.substring(loc + 1));
		buf.setCharAt(0, Character.toUpperCase(buf.charAt(0)));
		return buf.toString();
	}

	// XXX
	private void createManifestFile(IFile file, IProgressMonitor monitor) throws CoreException {
		WorkspaceBundlePluginModel model = new WorkspaceBundlePluginModel(file, null);
		model.load();
		IPlugin plugin = model.getPlugin();
		plugin.setId(IdUtil.getValidId(file.getProject().getName()));
		plugin.setName(createInitialName(plugin.getId()));
		plugin.setVersion("1.0.0"); //$NON-NLS-1$
		//
		if (m_pluginActivator != null) {
			plugin.setClassName(m_pluginActivator);
		}
		//
		IPluginModelFactory factory = model.getPluginFactory();
		IPluginBase base = model.getPluginBase();
		if (fLibraryName != null && !fLibraryName.equals(".")) { //$NON-NLS-1$
			IPluginLibrary library = factory.createLibrary();
			library.setName(fLibraryName);
			library.setExported(true);
			base.add(library);
		}
		for (int i = 0; i < fLibEntries.length; i++) {
			IPluginLibrary library = factory.createLibrary();
			library.setName(fLibEntries[i]);
			library.setExported(true);
			base.add(library);
		}
		if (m_pluginActivator != null) {
			IPluginImport uiImport = factory.createImport();
			uiImport.setId("org.eclipse.ui");
			base.add(uiImport);
			//
			IPluginImport uiFormsImport = factory.createImport();
			uiFormsImport.setId("org.eclipse.ui.forms");
			base.add(uiFormsImport);
			//
			IPluginImport resourceImport = factory.createImport();
			resourceImport.setId("org.eclipse.core.resources");
			base.add(resourceImport);
			//
			IPluginImport runtimeImport = factory.createImport();
			runtimeImport.setId("org.eclipse.core.runtime");
			base.add(runtimeImport);
		}
		if (TargetPlatformHelper.getTargetVersion() >= 3.1) {
			model.getBundleModel().getBundle().setHeader(Constants.BUNDLE_MANIFESTVERSION, "2"); //$NON-NLS-1$
		}
		if (m_hostPluginId != null) {
			model.getBundleModel().getBundle().setHeader(Constants.FRAGMENT_HOST, m_hostPluginId);
		}
		model.save();
		monitor.done();
		organizeExports(file.getProject());
	}

	private void organizeExports(final IProject project) {
		PDEModelUtility.modifyModel(new ModelModification(project.getFile(BUNDLE_FILENAME_DESCRIPTOR)) {
			@Override
			protected void modifyModel(IBaseModel model, IProgressMonitor monitor) throws CoreException {
				if (!(model instanceof IBundlePluginModelBase)) {
					return;
				}
				OrganizeManifest.organizeExportPackages(
						((IBundlePluginModelBase) model).getBundleModel().getBundle(),
						project,
						true,
						true);
			}
		}, null);
	}
}
