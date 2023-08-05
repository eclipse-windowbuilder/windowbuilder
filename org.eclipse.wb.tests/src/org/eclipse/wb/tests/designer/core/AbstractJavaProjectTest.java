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
package org.eclipse.wb.tests.designer.core;

import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectBefore;
import org.eclipse.wb.tests.designer.core.annotations.WaitForAutoBuildAfter;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Test with helpers for manipulating {@link IProject} and {@link IJavaProject}.
 *
 * @author scheglov_ke
 */
public abstract class AbstractJavaProjectTest extends DesignerTestCase {
	private static final List<IFile> m_createdResources = new ArrayList<>();

	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@After
	public void tearDown() throws Exception {
		// remove resources (with retries)
		{
			for (IFile resource : m_createdResources) {
				int maxCount = 5000;
				for (int i = 0; i < maxCount; i++) {
					try {
						// remove read-only attr, if has
						if (resource.isReadOnly()) {
							ResourceAttributes attributes = new ResourceAttributes();
							attributes.setReadOnly(false);
							resource.setResourceAttributes(attributes);
						}
						// do deleting
						forceDeleteFile(resource);
						break;
					} catch (Exception e) {
						if (i == maxCount - 1) {
							throw e;
						}
					}
					waitEventLoop(10);
				}
			}
			m_createdResources.clear();
		}
		// continue
		super.tearDown();
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		do_projectDispose();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Single test
	//
	////////////////////////////////////////////////////////////////////////////

	@Rule
	public TestRule methodRule = new TestRule() {
		@Override
		public Statement apply(Statement base, Description description) {
			return new Statement() {
				@Override
				public void evaluate() throws Throwable {
					List<Throwable> errors = new ArrayList<>();
					try {
						if (description.getAnnotation(DisposeProjectBefore.class) != null) {
							do_projectDispose();
						}
						base.evaluate();
						if (description.getAnnotation(DisposeProjectAfter.class) != null) {
							waitEventLoop(0);
							do_projectDispose();
						}
						if (description.getAnnotation(WaitForAutoBuildAfter.class) != null) {
							waitForAutoBuild();
						}
					} catch (Throwable t) {
						errors.add(t);
					}
					MultipleFailureException.assertEmpty(errors);
				}
			};
		}
	};

	////////////////////////////////////////////////////////////////////////////
	//
	// Project operations
	//
	////////////////////////////////////////////////////////////////////////////
	public static TestProject m_testProject;
	protected static IProject m_project;
	protected static IJavaProject m_javaProject;

	public static void do_projectCreate() throws Exception {
		if (m_testProject == null) {
			m_testProject = new TestProject();
			m_project = m_testProject.getProject();
			m_javaProject = m_testProject.getJavaProject();
		}
	}

	public static void do_projectDispose() throws Exception {
		if (m_testProject != null) {
			// wait for finishing all jobs, such as JDT indexing
			// XXX too slow!
			/*while (!Job.getJobManager().isIdle()) {
      	waitEventLoop(0);
      }*/
			// dispose project
			TestProject testProject = m_testProject;
			m_testProject = null;
			m_project = null;
			m_javaProject = null;
			disposeProjectWithRetry(testProject);
			// print memory XXX
			/*{
      	//int count = 15;
      	int count = 2;
      	for (int i = 0; i < count; i++) {
      		System.gc();
      		Thread.sleep(10);
      	}
      	System.out.println(getClass().getName()
      		+ "\n\t\t\t"
      		+ (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
      }*/
			// print Display.controlTable
			/*{
      	Control[] controlTable =
      			(Control[]) ReflectionUtils.getFieldObject(Display.getDefault(), "controlTable");
      	System.out.println("controls: " + controlTable.length);
      }*/
		}
	}

	/**
	 * Dispose project, wait if fails several time.
	 */
	private static void disposeProjectWithRetry(TestProject testProject) throws Exception {
		Throwable error = null;
		for (int i = 0; i < 100; i++) {
			try {
				testProject.dispose();
				return;
			} catch (Throwable e) {
				error = e;
				System.gc();
			}
			waitForAutoBuild();
			waitEventLoop(10);
		}
		if (error != null) {
			throw new Error(error);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Specifies that all resources created before should not be deleted of {@link #tearDown()}, but
	 * they will be deleted with project on {@link #test_tearDown()}.
	 */
	public static void forgetCreatedResources() {
		m_createdResources.clear();
	}

	/**
	 * Does auto-build and checks that created {@link ICompilationUnit}'s have no compilation
	 * problems.
	 */
	public static void waitForAutoBuild() throws Exception {
		// Wait for workspace jobs such as file creation
		waitEventLoop(25);
		// Wait for auto-builder to handle all newly created files
		TestProject.waitForAutoBuild();
		// check for compilation problems
		String problemsText = "";
		for (IFile file : m_createdResources) {
			// When we test refactorings, we may rename/move resources, so IFile may disappear.
			// Note, that after refactorings we dispose project to restore initial, clean state.
			if (!file.exists()) {
				continue;
			}
			// check for problem markers
			IMarker[] markers =
					file.findMarkers(
							IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER,
							true,
							IResource.DEPTH_INFINITE);
			for (IMarker marker : markers) {
				if (marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR) == IMarker.SEVERITY_ERROR) {
					problemsText +=
							"\n"
									+ file.getFullPath()
									+ "\n\tline "
									+ marker.getAttribute(IMarker.LINE_NUMBER)
									+ "\n\t"
									+ marker.getAttribute(IMarker.MESSAGE);
				}
			}
		}
		assertEquals("", problemsText);
	}

	/**
	 * Creates {@link ICompilationUnit} with given name and source.
	 */
	public final ICompilationUnit createModelCompilationUnit(String packageName,
			String unitName,
			String code) throws Exception {
		IPackageFragment pkg = m_testProject.getPackage(packageName);
		// create unit
		ICompilationUnit compilationUnit = m_testProject.createUnit(pkg, unitName, code);
		IFile resource = (IFile) compilationUnit.getUnderlyingResource();
		m_createdResources.add(resource);
		// OK, return unit
		return compilationUnit;
	}

	/**
	 * Creates {@link ICompilationUnit} with given name and source.
	 *
	 * @return the main {@link IType}.
	 */
	public final IType createModelType(String packageName, String unitName, String code)
			throws Exception {
		ICompilationUnit compilationUnit = createModelCompilationUnit(packageName, unitName, code);
		return compilationUnit.getTypes()[0];
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IFile: getFile()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link IFile} with given folder/name, relative to "src" folder.
	 */
	public static IFile getFileSrc(String folderName, String fileName) {
		return getFile("src/" + folderName, fileName);
	}

	/**
	 * @return the {@link IFile} with given folder/name, relative to {@link IProject}.
	 */
	public static IFile getFile(String folderName, String fileName) {
		return getFile(m_project, folderName, fileName);
	}

	/**
	 * @return the {@link IFile} with given folder/name, relative to {@link IProject}.
	 */
	public static IFile getFile(IProject project, String folderName, String fileName) {
		return getFile(project, folderName + "/" + fileName);
	}

	/**
	 * @return the {@link IFile} with given path, relative to "src" folder.
	 */
	public static IFile getFileSrc(String path) {
		return getFile("src/" + path);
	}

	/**
	 * @return the {@link IFile} with given path, relative to {@link IProject}.
	 */
	public static IFile getFile(String path) {
		return getFile(m_project, path);
	}

	/**
	 * @return the {@link IFile} with given path, relative to {@link IProject}.
	 */
	public static IFile getFile(IProject project, String path) {
		return project.getFile(new Path(path));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IFile: getContent()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link String} contents of existing {@link IFile}, relative to "src" folder.
	 */
	public static String getFileContentSrc(IProject project, String path) throws Exception {
		return getFileContent(project, "src/" + path);
	}

	/**
	 * @return the {@link String} contents of existing {@link IFile}, relative to "src" folder.
	 */
	public static String getFileContentSrc(String path) {
		return getFileContent("src/" + path);
	}

	/**
	 * @return the {@link String} contents of existing {@link IFile}, relative to {@link IProject}.
	 */
	public static String getFileContent(IProject project, String path) {
		IFile file = getFile(project, path);
		return getFileContent(file);
	}

	/**
	 * @return the {@link String} contents of existing {@link IFile}, relative to {@link IProject}.
	 */
	public static String getFileContent(String path) {
		IFile file = getFile(path);
		return getFileContent(file);
	}

	/**
	 * @return the {@link String} contents of existing {@link IFile}.
	 */
	public static String getFileContent(IFile file) {
		if (!file.exists()) {
			fail("File " + file + " does not exist.");
		}
		try {
			return IOUtils2.readString(file.getContents());
		} catch (Throwable e) {
			throw ReflectionUtils.propagate(e);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IFile: setContent()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates/updates {@link IFile} with given folder/name, relative to "src" folder.
	 */
	public static IFile setFileContentSrc(String folderName, String fileName, String content)
			throws Exception {
		return setFileContent("src/" + folderName, fileName, content);
	}

	/**
	 * Creates/updates {@link IFile} with given folder/name, relative to {@link IProject}.
	 */
	public static IFile setFileContent(String folderName, String fileName, String content)
			throws Exception {
		return setFileContent(folderName + "/" + fileName, content);
	}

	/**
	 * Creates/updates {@link IFile} with given folder/name, relative to {@link IProject}.
	 *
	 * @return the created/updated {@link IFile}.
	 */
	public static IFile setFileContent(IProject project,
			String folderName,
			String fileName,
			String content) throws Exception {
		return setFileContent(project, folderName + "/" + fileName, content);
	}

	/**
	 * Creates/updates {@link IFile} with given path, relative to "src" folder.
	 */
	public static IFile setFileContentSrc(String path, String content) throws Exception {
		return setFileContent("src/" + path, content);
	}

	/**
	 * Creates/updates {@link IFile} with given path, relative to {@link IProject}.
	 */
	public static IFile setFileContent(String path, String content) throws Exception {
		IFile file = getFile(path);
		setFileContent(file, content);
		return file;
	}

	/**
	 * Creates/updates {@link IFile} with given folder/name, relative to "src" folder.
	 *
	 * @return the created/updated {@link IFile}.
	 */
	public static IFile setFileContentSrc(IProject project, String path, String content)
			throws Exception {
		return setFileContent(project, "src/" + path, content);
	}

	/**
	 * Creates/updates {@link IFile} with given folder/name, relative to {@link IProject}.
	 *
	 * @return the created/updated {@link IFile}.
	 */
	public static IFile setFileContent(IProject project, String path, String content)
			throws Exception {
		IFile file = getFile(project, path);
		setFileContent0(file, content);
		return file;
	}

	/**
	 * Creates/updates {@link IFile} with given content.
	 */
	public static void setFileContent(IFile file, String content) throws Exception {
		boolean created = setFileContent0(file, content);
		if (created) {
			m_createdResources.add(file);
		}
	}

	/**
	 * Creates/updates {@link IFile} with given content.
	 *
	 * @return <code>true</code> if {@link IFile} was created.
	 */
	public static boolean setFileContent0(IFile file, String content) throws Exception {
		return setFileContent(file, content.getBytes());
	}

	/**
	 * Creates/updates {@link IFile} with given content.
	 *
	 * @return <code>true</code> if {@link IFile} was created.
	 */
	public static boolean setFileContent(IFile file, byte[] bytes) throws CoreException {
		return IOUtils2.setFileContents(file, new ByteArrayInputStream(bytes));
	}

	/**
	 * Creates/updates {@link IFile} with given content.
	 *
	 * @return <code>true</code> if {@link IFile} was created.
	 */
	public static boolean setFileContent(IFile file, InputStream inputStream) throws CoreException {
		return IOUtils2.setFileContents(file, inputStream);
	}

	/**
	 * Asserts that {@value #m_testProject} has {@link IFile} at given path.
	 */
	public static void assertFileExists(String pathString) {
		IFile file = getFile(pathString);
		assertTrue(file.exists());
	}

	/**
	 * Asserts that {@value #m_testProject} has not {@link IFile} at given path.
	 */
	public static void assertFileNotExists(String pathString) {
		IFile file = getFile(pathString);
		assertFalse(file.exists());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IFolder utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the existing {@link IFolder} with given path, relative to "src" folder.
	 */
	public static IFolder getFolderSrc(String path) throws CoreException {
		return getFolder("src/" + path);
	}

	/**
	 * @return the (may be not existing) {@link IFolder} with given path, relative to {@link IProject}
	 *         .
	 */
	public static IFolder getFolder0(String fullPath) throws CoreException {
		IPath path = new Path(fullPath);
		return m_project.getFolder(path);
	}

	/**
	 * @return the existing {@link IFolder} with given path, relative to {@link IProject}.
	 */
	public static IFolder getFolder(String path) throws CoreException {
		IFolder folder = getFolder0(path);
		IOUtils2.ensureFolderExists(folder);
		return folder;
	}

	/**
	 * Ensures that {@link IFolder} with given name exists, so exist all its parent {@link IFolder}'s.
	 */
	public final IFolder ensureFolderExists(String path) throws CoreException {
		return IOUtils2.ensureFolderExists(m_project, path);
	}

	/**
	 * Deletes {@link IFile}'s in given {@link IFolder} recursively.
	 */
	public static void deleteFiles(IFolder folder) throws Exception {
		for (IResource resource : folder.members()) {
			if (resource instanceof IFolder) {
				deleteFiles((IFolder) resource);
			}
			resource.delete(true, null);
		}
	}

	/**
	 * Force deletes {@link IFile}.
	 */
	public static void forceDeleteFile(IFile file) {
		while (file.exists()) {
			try {
				file.refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (Throwable e) {
			}
			try {
				file.delete(true, null);
			} catch (Throwable e) {
				waitEventLoop(100);
			}
		}
	}

	/**
	 * Force deletes {@link ICompilationUnit}.
	 */
	public static void forceDeleteCompilationUnit(ICompilationUnit cu) {
		while (cu.exists()) {
			try {
				cu.getResource().refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (Throwable e) {
			}
			try {
				cu.delete(true, null);
			} catch (Throwable e) {
				waitEventLoop(100);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// PNG image creation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates {@link IFile} with PNG image, schedules this file for clean up.
	 */
	public final IFile createImagePNG(String path, int width, int height) throws Exception {
		IFile file = getFile(path);
		byte[] bytes = createImageBytesPNG(width, height);
		boolean created = IOUtils2.setFileContents(file, new ByteArrayInputStream(bytes));
		if (created) {
			m_createdResources.add(file);
		}
		return file;
	}

	private byte[] createImageBytesPNG(int width, int height) {
		Image myImage = new Image(null, width, height);
		ImageLoader imageLoader = new ImageLoader();
		imageLoader.data = new ImageData[]{myImage.getImageData()};
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		imageLoader.save(baos, SWT.IMAGE_PNG);
		myImage.dispose();
		return baos.toByteArray();
	}
}
