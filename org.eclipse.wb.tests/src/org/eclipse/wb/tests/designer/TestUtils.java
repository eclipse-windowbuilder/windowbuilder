/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.tests.designer.core.TestProject;
import org.eclipse.wb.tests.designer.tests.Activator;

import org.eclipse.core.internal.registry.ExtensionRegistry;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.ContributorFactoryOSGi;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWizard;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.osgi.framework.Bundle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

/**
 * Some external utils for tests.
 *
 * @author scheglov_ke
 */
public final class TestUtils {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private TestUtils() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates file with empty PNG image of given size.
	 *
	 * @param testProject
	 *          the {@link TestProject} to access {@link IProject} where to create image.
	 * @param path
	 *          the path relative to {@link IProject}, for example <code>"src/test/MyImage.png"</code>
	 *          .
	 * @param width
	 *          the width of image.
	 * @param height
	 *          the height of image.
	 */
	public static IFile createImagePNG(TestProject testProject, String path, int width, int height)
			throws Exception {
		byte[] bytes = createImagePNG(width, height);
		IFile file = testProject.getProject().getFile(new Path(path));
		IOUtils2.setFileContents(file, new ByteArrayInputStream(bytes));
		return file;
	}

	/**
	 * @return the bytes of PNG image with given size.
	 */
	public static byte[] createImagePNG(int width, int height) {
		Image image = new Image(null, width, height);
		ImageLoader imageLoader = new ImageLoader();
		imageLoader.data = new ImageData[]{image.getImageData()};
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		imageLoader.save(baos, SWT.IMAGE_PNG);
		image.dispose();
		return baos.toByteArray();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Workbench
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Closes all {@link IViewPart}s.
	 */
	public static void closeAllViews() {
		// run event loop to allow any async's be executed
		while (Display.getCurrent().readAndDispatch()) {
			// do nothing
		}
		// do close
		IWorkbenchPage activePage = DesignerPlugin.getActivePage();
		IViewReference[] viewReferences = activePage.getViewReferences();
		if (viewReferences.length != 0) {
			for (IViewReference viewReference : viewReferences) {
				activePage.hideView(viewReference);
			}
			waitEventLoop(100);
		}
	}

	/**
	 * Closes all editors.
	 */
	public static void closeAllEditors() {
		// run event loop to allow any async's be executed
		while (Display.getCurrent().readAndDispatch()) {
			// do nothing
		}
		// close all editors
		{
			IWorkbenchPage activePage = DesignerPlugin.getActivePage();
			activePage.closeAllEditors(false);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Dynamic extensions
	//
	////////////////////////////////////////////////////////////////////////////
	private static final String EXTENSION_ID = "testExtension";
	private static Bundle m_contributionBundle;

	/**
	 * @return the {@link Bundle} from which contribution is performed, not <code>null</code>.
	 */
	private static Bundle getContributorBundle() {
		if (m_contributionBundle != null) {
			return m_contributionBundle;
		}
		return Activator.getDefault().getBundle();
	}

	/**
	 * Sets the {@link Bundle} from which contribution is performed, may be <code>null</code> to reset
	 * to the WindowBuilder bundle.
	 */
	public static void setContributionBundle(Bundle contributionBundle) {
		m_contributionBundle = contributionBundle;
	}

	/**
	 * @return the qualified exception id, i.e. contributor bundle plus simpel ID.
	 */
	private static String getQualifiedExtensionId(String simpleId) {
		return getContributorBundle().getSymbolicName() + "." + simpleId;
	}

	/**
	 * Adds dynamic extension.
	 *
	 * @param pointId
	 *          the qualified extension point id, e.g. <code>"org.eclipse.wb.core.toolkits"</code>.
	 * @param contribution
	 *          the {@link String} with contribution, without <code>"plugin"</code> and
	 *          <code>"extension"</code> tags.
	 */
	public static void addDynamicExtension(String pointId, String contribution) {
		addDynamicExtension(pointId, EXTENSION_ID, contribution);
	}

	/**
	 * Adds dynamic extension.
	 *
	 * @param pointId
	 *          the qualified extension point id, e.g. <code>"org.eclipse.wb.core.toolkits"</code>.
	 * @param extensionId
	 *          the (simple) id of added extension, for example <code>"myNature"</code>.
	 * @param contribution
	 *          the {@link String} with contribution, without <code>"plugin"</code> and
	 *          <code>"extension"</code> tags.
	 */
	public static void addDynamicExtension(String pointId, String extensionId, String contribution) {
		contribution =
				"<extension point='%pointId%' id='%extensionId%'>\n" + contribution + "\n</extension>";
		addDynamicExtension2(pointId, extensionId, contribution);
	}

	/**
	 * Adds dynamic extension.<br>
	 * In contrast to {@link #addDynamicExtension(String, String)}, this method accepts full
	 * contribution, that should have <code>"point", "id"</code> and other attributes.
	 *
	 * @param pointId
	 *          the qualified extension point id, e.g. <code>"org.eclipse.wb.core.toolkits"</code>.
	 * @param extensionId
	 *          the (simple) id of added extension, for example <code>"myNature"</code>.
	 * @param contribution
	 *          the {@link String} with full contribution, without <code>"plugin"</code> tag, but with
	 *          <code>"extension"</code> tag. It may use <code>"%pointId%"</code> and
	 *          <code>"%extensionId%"</code> template variables.
	 */
	public static void addDynamicExtension2(String pointId, String extensionId, String contribution) {
		// update contribution
		contribution = "<plugin>\n" + contribution + "\n</plugin>";
		contribution = StringUtils.replace(contribution, "%pointId%", pointId);
		contribution = StringUtils.replace(contribution, "%extensionId%", extensionId);
		// add extension into registry
		{
			Bundle bundle = getContributorBundle();
			IContributor contributor = ContributorFactoryOSGi.createContributor(bundle);
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			Object userToken = ((ExtensionRegistry) registry).getTemporaryUserToken();
			// do add
			registry.addContribution(
					new ByteArrayInputStream(contribution.getBytes()),
					contributor,
					false,
					null,
					null,
					userToken);
		}
		// wait for added extension
		{
			String qualifiedExtensionId = getQualifiedExtensionId(extensionId);
			while (ExternalFactoriesHelper.getExtension(pointId, qualifiedExtensionId) == null) {
				waitEventLoop(1);
			}
		}
	}

	/**
	 * Removes the test dynamic extension, added previously by
	 * {@link #addDynamicExtension(String, String)}.
	 *
	 * @param pointId
	 *          the qualified extension point id, e.g. <code>"org.eclipse.wb.core.toolkits"</code>.
	 */
	public static void removeDynamicExtension(String pointId) {
		removeDynamicExtension(pointId, EXTENSION_ID);
	}

	/**
	 * Removes the test dynamic extension, added previously by
	 * {@link #addDynamicExtension(String, String)}. This methods <em>does not</em> wait for
	 * processing asynchronous event by {@link ExternalFactoriesHelper}.
	 */
	public static void removeDynamicExtension_noWait(String pointId) {
		doRemoveDynamicExtension(pointId, EXTENSION_ID);
	}

	/**
	 * Removes dynamic extension from registry and waits until it will be removed also from
	 * {@link ExternalFactoriesHelper}.
	 *
	 * @param pointId
	 *          the qualified extension point id, e.g. <code>"org.eclipse.wb.core.toolkits"</code>.
	 * @param extensionId
	 *          the (simple) id of extension to remove, for example <code>"myNature"</code>.
	 */
	public static void removeDynamicExtension(String pointId, String extensionId) {
		String qualifiedExtensionId = getQualifiedExtensionId(extensionId);
		while (ExternalFactoriesHelper.getExtension(pointId, qualifiedExtensionId) != null) {
			doRemoveDynamicExtension(pointId, extensionId);
			waitEventLoop(1);
		}
	}

	/**
	 * Removes dynamic extension from {@link IExtensionRegistry}.
	 */
	private static void doRemoveDynamicExtension(String pointId, String extensionId) {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtension extension = registry.getExtension(pointId, getQualifiedExtensionId(extensionId));
		// do remove
		Object userToken = ((ExtensionRegistry) registry).getTemporaryUserToken();
		registry.removeExtension(extension, userToken);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// UI utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Waits given number of milliseconds and runs events loop every 1 millisecond.<br>
	 * At least one events loop will be executed.
	 */
	public static void waitEventLoop(int time) {
		long start = System.currentTimeMillis();
		do {
			try {
				Thread.sleep(0);
			} catch (Throwable e) {
			}
			while (Display.getCurrent().readAndDispatch()) {
				// do nothing
			}
		} while (System.currentTimeMillis() - start < time);
	}

	public static void runWizard(final IWizard wizard, IStructuredSelection selection) {
		IWorkbenchWindow workbenchWindow = DesignerPlugin.getActiveWorkbenchWindow();
		// initialize IWorkbenchWizard
		if (wizard instanceof IWorkbenchWizard) {
			((IWorkbenchWizard) wizard).init(workbenchWindow.getWorkbench(), selection);
		}
		// open Wizard UI
		WizardDialog dialog = new WizardDialog(workbenchWindow.getShell(), wizard);
		dialog.create();
		String title = wizard.getWindowTitle();
		if (title != null) {
			dialog.getShell().setText(title);
		}
		dialog.open();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Resources
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the path to the temporary "jar" file with single entry.
	 *
	 * @param entryName
	 *          the name of entry, for example <code>"myFolder/subFolder/file.txt"</code>.
	 * @param content
	 *          the {@link String} content of entry.
	 */
	public static String createTemporaryJar(String entryName, String content) throws Exception {
		File tempFile = File.createTempFile("wbpTests", ".jar");
		tempFile.deleteOnExit();
		// create "jar" with single entry
		{
			JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(tempFile));
			jarOutputStream.putNextEntry(new ZipEntry(entryName));
			jarOutputStream.write(content.getBytes());
			jarOutputStream.closeEntry();
			jarOutputStream.close();
		}
		// return path to "jar"
		return tempFile.getAbsolutePath();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// TestSuite
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Sorts test methods in the given {@link TestSuite} to same order as they are declared in source
	 * of test class.
	 */
	public static void sortTestSuiteMethods(Class<?> clazz, TestSuite suite) {
		try {
			final List<String> sourceMethodNames = getSourceMethodNames(clazz);
			Vector<TestCase> tests = getTestsVector(suite);
			Collections.sort(tests, new Comparator<TestCase>() {
				@Override
				public int compare(TestCase o1, TestCase o2) {
					String method_1 = o1.getName();
					String method_2 = o2.getName();
					if ("test_setUp".equals(method_1)) {
						return -1;
					}
					if ("test_setUp".equals(method_2)) {
						return 1;
					}
					if ("test_tearDown".equals(method_1)) {
						return 1;
					}
					if ("test_tearDown".equals(method_2)) {
						return -1;
					}
					return sourceMethodNames.indexOf(method_1) - sourceMethodNames.indexOf(method_2);
				}
			});
		} catch (Throwable e) {
			ReflectionUtils.propagate(e);
		}
	}

	/**
	 * @return the names of methods declared in the given {@link Class}, in same order as in source.
	 */
	private static List<String> getSourceMethodNames(Class<?> testClass) throws Exception {
		final List<String> sourceMethodNames = new ArrayList<>();
		String classPath = testClass.getName().replace('.', '/') + ".class";
		InputStream classStream = testClass.getClassLoader().getResourceAsStream(classPath);
		ClassReader classReader = new ClassReader(classStream);
		classReader.accept(new ClassVisitor(Opcodes.ASM9) {
			@Override
			public MethodVisitor visitMethod(int access,
					String name,
					String desc,
					String signature,
					String[] exceptions) {
				sourceMethodNames.add(name);
				return new MethodVisitor(Opcodes.ASM9) {};
			}
		}, 0);
		return sourceMethodNames;
	}

	/**
	 * @return the live {@link Vector} of tests in the given {@link TestSuite}.
	 */
	private static Vector<TestCase> getTestsVector(TestSuite suite) throws Exception {
		Field testsField = TestSuite.class.getDeclaredField("fTests");
		testsField.setAccessible(true);
		@SuppressWarnings("unchecked")
		Vector<TestCase> tests = (Vector<TestCase>) testsField.get(suite);
		return tests;
	}
}
