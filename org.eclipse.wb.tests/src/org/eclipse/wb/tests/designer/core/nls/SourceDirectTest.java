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
package org.eclipse.wb.tests.designer.core.nls;

import org.eclipse.wb.internal.core.nls.NlsSupport;
import org.eclipse.wb.internal.core.nls.SourceDescription;
import org.eclipse.wb.internal.core.nls.bundle.pure.direct.DirectSource;
import org.eclipse.wb.internal.core.nls.bundle.pure.direct.DirectSourceNewComposite;
import org.eclipse.wb.internal.core.nls.bundle.pure.direct.SourceParameters;
import org.eclipse.wb.internal.core.nls.edit.IEditableSource;
import org.eclipse.wb.internal.core.nls.edit.IEditableSupport;
import org.eclipse.wb.internal.core.nls.edit.StringPropertyInfo;
import org.eclipse.wb.internal.core.nls.model.AbstractSource;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.ToolkitProvider;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.core.PreferencesRepairer;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;

import org.junit.Test;

import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;

/**
 * Tests for {@link DirectSource}.
 *
 * @author scheglov_ke
 */
public class SourceDirectTest extends AbstractNlsTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_notDirectCases_1() throws Exception {
		ContainerInfo frame =
				parseContainer(
						"public class Test extends JFrame {",
						"  public Test() {",
						"    setTitle('title');",
						"  }",
						"}");
		NlsSupport support = NlsSupport.get(frame);
		assertEquals(0, support.getSources().length);
	}

	@Test
	public void test_notDirectCases_2() throws Exception {
		m_waitForAutoBuild = true;
		ContainerInfo frame =
				parseContainer(
						"public class Test extends JFrame {",
						"  public Test() {",
						"    setTitle(getString2());",
						"    setName(getString('name'));",
						"  }",
						"  private static String getString2() {",
						"    return 'title';",
						"  }",
						"  private static String getString(String key) {",
						"    return key;",
						"  }",
						"}");
		NlsSupport support = NlsSupport.get(frame);
		assertEquals(0, support.getSources().length);
	}

	@Test
	public void test_notDirectCases_3() throws Exception {
		createASTCompilationUnit(
				"test",
				"MyBundle.java",
				getSourceDQ(
						"package test;",
						"public class MyBundle {",
						"  public static MyBundle getBundle(String name) {",
						"    return new MyBundle();",
						"  }",
						"  public String getString(String key) {",
						"    return key;",
						"  }",
						"}"));
		waitForAutoBuild();
		//
		ContainerInfo frame =
				parseContainer(
						"public class Test extends JFrame {",
						"  public Test() {",
						"    setTitle(MyBundle.getBundle('bundle.name').getString('title'));",
						"    setName(MyBundle.getBundle(null).getString('name'));",
						"  }",
						"}");
		NlsSupport support = NlsSupport.get(frame);
		assertEquals(0, support.getSources().length);
		// check that "title" is correct
		frame.refresh();
		try {
			JFrame jFrame = (JFrame) frame.getObject();
			assertEquals("title", jFrame.getTitle());
			assertEquals("name", jFrame.getName());
		} finally {
			frame.refresh_dispose();
		}
	}

	@Test
	public void test_parse() throws Exception {
		setFileContentSrc(
				"test/messages.properties",
				getSourceDQ("frame.title=My JFrame", "frame.name=My name"));
		waitForAutoBuild();
		//
		ContainerInfo frame =
				parseContainer(
						"import java.util.ResourceBundle;",
						"public class Test extends JFrame {",
						"  public Test() {",
						"    setTitle(ResourceBundle.getBundle('test.messages').getString('frame.title')); //$NON-NLS-1$ //$NON-NLS-2$",
						"    setName(ResourceBundle.getBundle('test.messages').getString('frame.name')); //$NON-NLS-1$ //$NON-NLS-2$",
						"  }",
						"}");
		NlsSupport support = NlsSupport.get(frame);
		// check that we have DirectSource
		DirectSource source;
		{
			AbstractSource[] sources = support.getSources();
			assertEquals(1, sources.length);
			source = (DirectSource) sources[0];
		}
		// check getBundleComment()
		assertEquals(
				"Direct ResourceBundle",
				ReflectionUtils.invokeMethod(source, "getBundleComment()"));
		// check that "title" is correct
		frame.refresh();
		try {
			JFrame jFrame = (JFrame) frame.getObject();
			assertEquals("My JFrame", jFrame.getTitle());
			assertEquals("My name", jFrame.getName());
		} finally {
			frame.refresh_dispose();
		}
	}

	@Test
	public void test_parse_inConstructor() throws Exception {
		setFileContentSrc("test/messages.properties", getSourceDQ("button.text=My JButton"));
		waitForAutoBuild();
		//
		ContainerInfo panel =
				parseContainer(
						"import java.util.ResourceBundle;",
						"public class Test extends JPanel {",
						"  public Test() {",
						"    add(new JButton(ResourceBundle.getBundle('test.messages').getString('button.text'))); //$NON-NLS-1$ //$NON-NLS-2$",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		NlsSupport support = NlsSupport.get(panel);
		// check that we have DirectSource
		DirectSource source;
		{
			AbstractSource[] sources = support.getSources();
			assertEquals(1, sources.length);
			source = (DirectSource) sources[0];
		}
		// check getBundleComment()
		assertEquals(
				"Direct ResourceBundle",
				ReflectionUtils.invokeMethod(source, "getBundleComment()"));
		// check that "text" is correct
		panel.refresh();
		try {
			JButton jButton = (JButton) button.getObject();
			assertEquals("My JButton", jButton.getText());
		} finally {
			panel.refresh_dispose();
		}
	}

	@Test
	public void test_possibleSources() throws Exception {
		setFileContentSrc("test/not-a-properties.text", "");
		setFileContentSrc(
				"test/messages2.properties",
				getSourceDQ("#Invalid comment for Direct ResourceBundle"));
		setFileContentSrc(
				"test/messages.properties",
				getSourceDQ("#Direct ResourceBundle", "frame.title=My JFrame"));
		setFileContentSrc(
				"test/messages_it.properties",
				getSourceDQ("#We need only default *.properties file"));
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
		IEditableSupport editableSupport = support.getEditable();
		//
		List<IEditableSource> editableSources = editableSupport.getEditableSources();
		assertEquals(1, editableSources.size());
		//
		IEditableSource editableSource = editableSources.get(0);
		assertEquals("test.messages (Direct ResourceBundle usage)", editableSource.getLongTitle());
	}

	@Test
	public void test_externalize() throws Exception {
		setFileContentSrc("test/messages.properties", getSourceDQ("#Direct ResourceBundle"));
		waitForAutoBuild();
		//
		ContainerInfo frame =
				parseContainer(
						"public class Test extends JFrame {",
						"  public Test() {",
						"    setTitle('My JFrame');",
						"  }",
						"}");
		NlsSupport support = NlsSupport.get(frame);
		IEditableSupport editableSupport = support.getEditable();
		// prepare possible source
		IEditableSource editableSource;
		{
			List<IEditableSource> editableSources = editableSupport.getEditableSources();
			assertEquals(1, editableSources.size());
			editableSource = editableSources.get(0);
		}
		// do externalize
		StringPropertyInfo propertyInfo = editableSupport.getProperties(frame).get(0);
		editableSupport.externalizeProperty(propertyInfo, editableSource, true);
		// apply commands
		support.applyEditable(editableSupport);
		// check
		assertEditor(
				"import java.util.ResourceBundle;",
				"public class Test extends JFrame {",
				"  public Test() {",
				"    setTitle(ResourceBundle.getBundle('test.messages').getString('Test.this.title')); //$NON-NLS-1$ //$NON-NLS-2$",
				"  }",
				"}");
		{
			String newProperties = getFileContentSrc("test/messages.properties");
			// comment expected
			assertTrue(newProperties.contains("#Direct ResourceBundle"));
			// line for title
			assertTrue(newProperties.contains("Test.this.title=My JFrame"));
		}
	}

	@Test
	public void test_externalize_qualifiedTypeName() throws Exception {
		setFileContentSrc("test/messages.properties", getSourceDQ("#Direct ResourceBundle"));
		waitForAutoBuild();
		//
		ContainerInfo frame =
				parseContainer(
						"public class Test extends JFrame {",
						"  public Test() {",
						"    setTitle('My JFrame');",
						"  }",
						"}");
		NlsSupport support = NlsSupport.get(frame);
		IEditableSupport editableSupport = support.getEditable();
		// prepare possible source
		IEditableSource editableSource;
		{
			List<IEditableSource> editableSources = editableSupport.getEditableSources();
			assertEquals(1, editableSources.size());
			editableSource = editableSources.get(0);
		}
		// do externalize
		PreferencesRepairer preferences =
				new PreferencesRepairer(ToolkitProvider.DESCRIPTION.getPreferences());
		try {
			preferences.setValue(IPreferenceConstants.P_NLS_KEY_QUALIFIED_TYPE_NAME, true);
			StringPropertyInfo propertyInfo = editableSupport.getProperties(frame).get(0);
			editableSupport.externalizeProperty(propertyInfo, editableSource, true);
		} finally {
			preferences.restore();
		}
		// apply commands
		support.applyEditable(editableSupport);
		// check
		assertEditor(
				"import java.util.ResourceBundle;",
				"public class Test extends JFrame {",
				"  public Test() {",
				"    setTitle(ResourceBundle.getBundle('test.messages').getString('test.Test.this.title')); //$NON-NLS-1$ //$NON-NLS-2$",
				"  }",
				"}");
		{
			String newProperties = getFileContentSrc("test/messages.properties");
			// comment expected
			assertTrue(newProperties.contains("#Direct ResourceBundle"));
			// line for title
			assertTrue(newProperties.contains("test.Test.this.title=My JFrame"));
		}
	}

	@Test
	public void test_renameKey() throws Exception {
		setFileContentSrc("test/messages.properties", getSourceDQ("frame.title=My JFrame"));
		waitForAutoBuild();
		//
		ContainerInfo frame =
				parseContainer(
						"import java.util.ResourceBundle;",
						"public class Test extends JFrame {",
						"  public Test() {",
						"    setTitle(ResourceBundle.getBundle('test.messages').getString('frame.title')); //$NON-NLS-1$ //$NON-NLS-2$",
						"  }",
						"}");
		NlsSupport support = NlsSupport.get(frame);
		IEditableSupport editableSupport = support.getEditable();
		IEditableSource editableSource = editableSupport.getEditableSources().get(0);
		// do rename
		editableSource.renameKey("frame.title", "frame.title2");
		// apply commands
		support.applyEditable(editableSupport);
		// check
		assertEditor(
				"import java.util.ResourceBundle;",
				"public class Test extends JFrame {",
				"  public Test() {",
				"    setTitle(ResourceBundle.getBundle('test.messages').getString('frame.title2')); //$NON-NLS-1$ //$NON-NLS-2$",
				"  }",
				"}");
		{
			String newProperties = getFileContentSrc("test/messages.properties");
			assertFalse(newProperties.contains("frame.title=My JFrame"));
			assertTrue(newProperties.contains("frame.title2=My JFrame"));
		}
	}

	@Test
	public void test_internalize() throws Exception {
		setFileContentSrc("test/messages.properties", getSourceDQ("frame.title=My JFrame"));
		waitForAutoBuild();
		//
		ContainerInfo frame =
				parseContainer(
						"import java.util.ResourceBundle;",
						"public class Test extends JFrame {",
						"  public Test() {",
						"    setTitle(ResourceBundle.getBundle('test.messages').getString('frame.title')); //$NON-NLS-1$ //$NON-NLS-2$",
						"  }",
						"}");
		NlsSupport support = NlsSupport.get(frame);
		IEditableSupport editableSupport = support.getEditable();
		IEditableSource editableSource = editableSupport.getEditableSources().get(0);
		// do internalize
		editableSource.internalizeKey("frame.title");
		// apply commands
		support.applyEditable(editableSupport);
		// check
		assertEditor(
				"import java.util.ResourceBundle;",
				"public class Test extends JFrame {",
				"  public Test() {",
				"    setTitle('My JFrame');",
				"  }",
				"}");
		{
			String newProperties = getFileContentSrc("test/messages.properties");
			assertFalse(newProperties.contains("frame.title=My JFrame"));
		}
	}

	@Test
	public void test_create() throws Exception {
		ContainerInfo frame =
				parseContainer(
						"public class Test extends JFrame {",
						"  public Test() {",
						"    setTitle('My JFrame');",
						"  }",
						"}");
		NlsSupport support = NlsSupport.get(frame);
		IEditableSupport editableSupport = support.getEditable();
		// prepare editable source
		IEditableSource editableSource = NlsTestUtils.createEmptyEditable("test.messages");
		// prepare parameters
		SourceParameters parameters = new SourceParameters();
		IJavaProject javaProject = m_lastEditor.getJavaProject();
		{
			parameters.m_propertySourceFolder =
					javaProject.findPackageFragmentRoot(new Path("/TestProject/src"));
			parameters.m_propertyPackage =
					javaProject.findPackageFragment(new Path("/TestProject/src/test"));
			parameters.m_propertyFileName = "messages.properties";
			parameters.m_propertyBundleName = "test.messages";
			parameters.m_propertyFileExists = false;
		}
		// add source
		editableSupport.addSource(editableSource, new SourceDescription(DirectSource.class,
				DirectSourceNewComposite.class), parameters);
		// do externalize
		StringPropertyInfo propertyInfo = editableSupport.getProperties(frame).get(0);
		editableSupport.externalizeProperty(propertyInfo, editableSource, true);
		// apply commands
		support.applyEditable(editableSupport);
		// checks
		assertEditor(
				"import java.util.ResourceBundle;",
				"public class Test extends JFrame {",
				"  public Test() {",
				"    setTitle(ResourceBundle.getBundle('test.messages').getString('Test.this.title')); //$NON-NLS-1$ //$NON-NLS-2$",
				"  }",
				"}");
		{
			String newProperties = getFileContentSrc("test/messages.properties");
			assertTrue(newProperties.contains("#Direct ResourceBundle"));
			assertTrue(newProperties.contains("Test.this.title=My JFrame"));
		}
	}
}
