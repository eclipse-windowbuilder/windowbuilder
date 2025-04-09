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
package org.eclipse.wb.tests.designer.core.nls;

import org.eclipse.wb.internal.core.nls.NlsSupport;
import org.eclipse.wb.internal.core.nls.SourceDescription;
import org.eclipse.wb.internal.core.nls.bundle.eclipse.old.EclipseSource;
import org.eclipse.wb.internal.core.nls.bundle.eclipse.old.SourceParameters;
import org.eclipse.wb.internal.core.nls.edit.EditableSupport;
import org.eclipse.wb.internal.core.nls.edit.IEditableSource;
import org.eclipse.wb.internal.core.nls.edit.IEditableSupport;
import org.eclipse.wb.internal.core.nls.model.LocaleInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;

import org.apache.commons.lang3.StringUtils;
import org.junit.Ignore;
import org.junit.Test;

import javax.swing.JFrame;

/**
 * Tests for {@link EclipseSource}.
 *
 * @author scheglov_ke
 */
public class SourceEclipseOldTest extends AbstractNlsTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Use constructor without accessor, only to create {@link IEditableSource} using existing
	 * *.properties.
	 */
	@Ignore
	@Test
	public void test_constructorWithoutAccessor() throws Exception {
		setFileContentSrc("test/messages.properties", getSourceDQ("frame.title=My JFrame"));
		waitForAutoBuild();
		//
		ContainerInfo frame =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JFrame {",
						"  public Test() {",
						"  }",
						"}");
		NlsSupport support = NlsSupport.get(frame);
		// prepare editable source
		IEditableSource editableSource;
		{
			EclipseSource source = new EclipseSource(frame, null, "test.messages");
			editableSource = source.getEditable();
			assertEquals("My JFrame", editableSource.getValue(LocaleInfo.DEFAULT, "frame.title"));
		}
		// prepare parameters
		SourceParameters parameters = new SourceParameters();
		IJavaProject javaProject = m_lastEditor.getJavaProject();
		{
			parameters.m_accessorSourceFolder =
					javaProject.findPackageFragmentRoot(new Path("/TestProject/src"));
			parameters.m_accessorPackage =
					javaProject.findPackageFragment(new Path("/TestProject/src/test"));
			parameters.m_accessorPackageName = parameters.m_accessorPackage.getElementName();
			parameters.m_accessorClassName = "Messages";
			parameters.m_accessorFullClassName = "test.Messages";
			parameters.m_accessorExists = false;
		}
		{
			parameters.m_propertySourceFolder = parameters.m_accessorSourceFolder;
			parameters.m_propertyPackage = parameters.m_accessorPackage;
			parameters.m_propertyFileName = "messages.properties";
			parameters.m_propertyBundleName = "test.messages";
			parameters.m_propertyFileExists = false;
		}
		// add source
		IEditableSupport editableSupport = support.getEditable();
		editableSupport.addSource(
				editableSource,
				NlsSupport.getSourceDescriptions(frame)[0],
				parameters);
		// apply commands
		support.applyEditable(editableSupport);
		// checks
		assertTrue(getFileSrc("/test/Messages.java").exists());
		{
			String newProperties = getFileContentSrc("test/messages.properties");
			// no comment expected because existing file should be used
			assertFalse(newProperties.contains("#Eclipse messages class"));
			// also no change
			assertTrue(newProperties.contains("frame.title=My JFrame"));
		}
	}

	/**
	 * Test for using '/' instead of '.' in qualified bundle name.
	 */
	@Test
	public void test_slashBundleName() throws Exception {
		NlsTestUtils.create_EclipseOld_Accessor(this, "test/messages", false);
		setFileContentSrc("test/messages.properties", getSourceDQ("frame.title=My JFrame"));
		waitForAutoBuild();
		//
		ContainerInfo frame =
				parseContainer(
						"public class Test extends JFrame {",
						"  public Test() {",
						"    setTitle(Messages.getString('frame.title'));",
						"  }",
						"}");
		NlsSupport support = NlsSupport.get(frame);
		assertEquals(1, support.getSources().length);
		//
		frame.refresh();
		try {
			assertEquals("My JFrame", ((JFrame) frame.getObject()).getTitle());
		} finally {
			frame.refresh_dispose();
		}
	}

	/**
	 * Test for referencing Messages class using qualified name.
	 */
	@Test
	public void test_qualifiedName() throws Exception {
		NlsTestUtils.create_EclipseOld_Accessor(this, false);
		setFileContentSrc("test/messages.properties", getSourceDQ("frame.title=My JFrame"));
		waitForAutoBuild();
		//
		ContainerInfo frame =
				parseContainer(
						"public class Test extends JFrame {",
						"  public Test() {",
						"    setTitle(test.Messages.getString('frame.title'));",
						"  }",
						"}");
		NlsSupport support = NlsSupport.get(frame);
		assertEquals(1, support.getSources().length);
		//
		frame.refresh();
		try {
			assertEquals("My JFrame", ((JFrame) frame.getObject()).getTitle());
		} finally {
			frame.refresh_dispose();
		}
	}

	/**
	 * Bad possible {@link EclipseSource} - no <code>BUNDLE_NAME</code> field.
	 */
	@Test
	public void test_badPossible_1() throws Exception {
		setFileContentSrc(
				"test/Messages.java",
				getSourceDQ(
						"package test;",
						"public class Messages {",
						"  private static final String BUNDLE_NAME2 = 'test.messages'; //$NON-NLS-1$",
						"}"));
		ContainerInfo frame =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JFrame {",
						"  public Test() {",
						"  }",
						"}");
		NlsSupport support = NlsSupport.get(frame);
		IEditableSupport editable = support.getEditable();
		assertEquals(0, editable.getEditableSources().size());
	}

	/**
	 * Bad possible {@link EclipseSource} - no <code>getString()</code> method.
	 */
	@Test
	public void test_badPossible_2() throws Exception {
		setFileContentSrc(
				"test/Messages.java",
				getSourceDQ(
						"package test;",
						"public class Messages {",
						"  private static final String BUNDLE_NAME = 'test.messages'; //$NON-NLS-1$",
						"}"));
		ContainerInfo frame =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JFrame {",
						"  public Test() {",
						"  }",
						"}");
		NlsSupport support = NlsSupport.get(frame);
		IEditableSupport editable = support.getEditable();
		assertEquals(0, editable.getEditableSources().size());
	}

	/**
	 * Not a {@link EclipseSource} - not a <code>getString()</code> method.
	 */
	@Test
	public void test_badSource_1() throws Exception {
		m_waitForAutoBuild = true;
		ContainerInfo frame =
				parseContainer(
						"public class Test extends JFrame {",
						"  public Test() {",
						"    setTitle(getString2('frame.title'));",
						"  }",
						"  private static String getString2(String key) {",
						"    return key;",
						"  }",
						"}");
		NlsSupport support = NlsSupport.get(frame);
		assertEquals(0, support.getSources().length);
	}

	/**
	 * Not a {@link EclipseSource} - locale <code>getString()</code> method.
	 */
	@Test
	public void test_badSource_2() throws Exception {
		m_waitForAutoBuild = true;
		ContainerInfo frame =
				parseContainer(
						"public class Test extends JFrame {",
						"  public Test() {",
						"    setTitle(getString('frame.title'));",
						"  }",
						"  private static String getString(String key) {",
						"    return key;",
						"  }",
						"}");
		NlsSupport support = NlsSupport.get(frame);
		assertEquals(0, support.getSources().length);
	}

	@Ignore
	@Test
	public void test_addSource() throws Exception {
		ContainerInfo frame =
				parseContainer(
						"public class Test extends JFrame {",
						"  public Test() {",
						"    setTitle('My JFrame');",
						"  }",
						"}");
		NlsSupport support = NlsSupport.get(frame);
		//
		EditableSupport editableSupport = (EditableSupport) support.getEditable();
		// addSource()
		IEditableSource editableSource;
		{
			editableSource = NlsTestUtils.createEmptyEditable("messages");
			SourceDescription sourceDescription = NlsSupport.getSourceDescriptions(frame)[0];
			assertSame(EclipseSource.class, sourceDescription.getSourceClass());
			// prepare parameters
			SourceParameters parameters = new SourceParameters();
			IJavaProject javaProject = m_lastEditor.getJavaProject();
			{
				parameters.m_accessorSourceFolder =
						javaProject.findPackageFragmentRoot(new Path("/TestProject/src"));
				parameters.m_accessorPackage =
						javaProject.findPackageFragment(new Path("/TestProject/src/test"));
				parameters.m_accessorPackageName = parameters.m_accessorPackage.getElementName();
				parameters.m_accessorClassName = "Messages";
				parameters.m_accessorFullClassName = "test.Messages";
				parameters.m_accessorExists = false;
			}
			{
				parameters.m_propertySourceFolder = parameters.m_accessorSourceFolder;
				parameters.m_propertyPackage = parameters.m_accessorPackage;
				parameters.m_propertyFileName = "messages.properties";
				parameters.m_propertyBundleName = "test.messages";
				parameters.m_propertyFileExists = false;
			}
			parameters.m_withDefaultValue = false;
			// do add
			editableSupport.addSource(editableSource, sourceDescription, parameters);
		}
		// apply commands
		support.applyEditable(editableSupport);
		assertEquals(
				getSourceDQ(
						"package test;",
						"",
						"import java.beans.Beans;",
						"import java.util.MissingResourceException;",
						"import java.util.ResourceBundle;",
						"",
						"public class Messages {",
						"  ////////////////////////////////////////////////////////////////////////////",
						"  //",
						"  // Constructor",
						"  //",
						"  ////////////////////////////////////////////////////////////////////////////",
						"  private Messages() {",
						"    // do not instantiate",
						"  }",
						"  ////////////////////////////////////////////////////////////////////////////",
						"  //",
						"  // Bundle access",
						"  //",
						"  ////////////////////////////////////////////////////////////////////////////",
						"  private static final String BUNDLE_NAME = 'test.messages'; //$NON-NLS-1$",
						"  private static final ResourceBundle RESOURCE_BUNDLE = loadBundle();",
						"  private static ResourceBundle loadBundle() {",
						"    return ResourceBundle.getBundle(BUNDLE_NAME);",
						"  }",
						"  ////////////////////////////////////////////////////////////////////////////",
						"  //",
						"  // Strings access",
						"  //",
						"  ////////////////////////////////////////////////////////////////////////////",
						"  public static String getString(String key) {",
						"    try {",
						"      ResourceBundle bundle = Beans.isDesignTime() ? loadBundle() : RESOURCE_BUNDLE;",
						"      return bundle.getString(key);",
						"    } catch (MissingResourceException e) {",
						"      return '!' + key + '!';",
						"    }",
						"  }",
						"}"),
				StringUtils.replace(getFileContentSrc("test/Messages.java"), "\r\n", "\n"));
		assertTrue(getFileSrc("test/messages.properties").exists());
	}
}
