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
package org.eclipse.wb.tests.designer.core;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.tests.Activator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Helper for creating temporary {@link Bundle}.
 *
 * @author scheglov_ke
 */
public final class TestBundle {
  private final BundleContext m_context = Activator.getDefault().getBundle().getBundleContext();
  private final String m_id;
  private final File m_bundleFolder;
  private File m_bundleFile;
  private boolean m_created;
  private Bundle m_bundle;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TestBundle() throws Exception {
    this("org.eclipse.wb.tests.testBundle");
  }

  public TestBundle(String id) throws Exception {
    m_id = id;
    m_bundleFolder = IOUtils2.createTempDirectory("wbpTestBundle", "");
    m_bundleFile = m_bundleFolder;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Configuring
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates in this {@link TestBundle} same class as it exists in given {@link ClassLoader}. Note,
   * that {@link ClassLoader} should be able to provide <code>*.class</code> resource.
   */
  public void addClass(Class<?> clazz) throws Exception {
    ClassLoader classLoader = clazz.getClassLoader();
    String name = clazz.getName();
    // add main class
    addClass(classLoader, name);
    // add anonymous inner classes
    try {
      for (int i = 1;; i++) {
        addClass(classLoader, name + "$" + i);
      }
    } catch (Throwable e) {
    }
  }

  /**
   * Creates in this {@link TestBundle} same class as it exists in given {@link ClassLoader}. Note,
   * that {@link ClassLoader} should be able to provide <code>*.class</code> resource.
   */
  public void addClass(ClassLoader classLoader, String name) throws Exception {
    addClass(name, getClassBytes(classLoader, name));
  }

  public void addClass(String name, InputStream inputStream) throws Exception {
    assertNotCreated();
    File classFile = getFileInExistingFolder(name.replace('.', '/') + ".class");
    IOUtils2.writeBytes(classFile, inputStream);
  }

  public void setFile(String path, String content) throws Exception {
    setFile(path, content.getBytes());
  }

  public void setFile(String path, byte[] content) throws Exception {
    assertNotCreated();
    File file = getFileInExistingFolder(path);
    IOUtils2.writeBytes(file, content);
  }

  public ZipFileFactory addJar(String path) throws Exception {
    assertNotCreated();
    File jarFile = getFileInExistingFolder(path);
    return new ZipFileFactory(new FileOutputStream(jarFile));
  }

  private void assertNotCreated() {
    Assert.isTrue(!m_created, "Bundle %s is already created.", m_id);
  }

  private File getFileInExistingFolder(String path) {
    File file = new File(m_bundleFolder, path);
    file.getParentFile().mkdirs();
    return file;
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // plugin.xml
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class ExtensionDeclaration {
    String m_pointId;
    String m_id;
    String m_qualifiedId;
    String[] m_lines;

    boolean isInstalled() {
      return ExternalFactoriesHelper.getExtension(m_pointId, m_qualifiedId) != null;
    }
  }
  private static int m_nextExtensionId = 0;
  private final List<ExtensionDeclaration> m_extensions = Lists.newArrayList();

  public void addExtension(String pointId, String... lines) {
    ExtensionDeclaration extension = new ExtensionDeclaration();
    extension.m_pointId = pointId;
    extension.m_id = "test_" + m_nextExtensionId++;
    extension.m_qualifiedId = m_id + "." + extension.m_id;
    extension.m_lines = lines;
    m_extensions.add(extension);
  }

  public void setPluginExtensions() throws Exception {
    String content = "";
    content += "<plugin>\n";
    for (ExtensionDeclaration extension : m_extensions) {
      content += "\t<extension point='" + extension.m_pointId + "' id='" + extension.m_id + "'>\n";
      content += "\t\t" + StringUtils.join(extension.m_lines, "\n\t\t");
      content += "\n";
      content += "\t</extension>\n";
    }
    content += "</plugin>";
    setFile("plugin.xml", content);
  }

  private void waitForExtensionsInstalled() {
    while (!areAllExtensionsInstalled()) {
      TestUtils.waitEventLoop(1);
    }
  }

  private void waitForExtensionsUnInstalled() {
    while (!areAllExtensionsUnInstalled()) {
      TestUtils.waitEventLoop(1);
    }
  }

  private boolean areAllExtensionsInstalled() {
    for (ExtensionDeclaration extension : m_extensions) {
      if (!extension.isInstalled()) {
        return false;
      }
    }
    return true;
  }

  private boolean areAllExtensionsUnInstalled() {
    for (ExtensionDeclaration extension : m_extensions) {
      if (extension.isInstalled()) {
        return false;
      }
    }
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Class utils
  //
  ////////////////////////////////////////////////////////////////////////////
  public static InputStream getClassBytes(Class<?> clazz) {
    return getClassBytes(clazz.getClassLoader(), clazz.getName());
  }

  public static InputStream getClassBytes(ClassLoader classLoader, String className) {
    String bytesPath = className.replace('.', '/') + ".class";
    InputStream inputStream = classLoader.getResourceAsStream(bytesPath);
    Assert.isNotNull(inputStream, "Unable to find %s bytes.", className);
    return inputStream;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Control
  //
  ////////////////////////////////////////////////////////////////////////////
  public void install() throws Exception {
    install(false);
  }

  public void install(boolean pack2jar) throws Exception {
    if (!m_extensions.isEmpty()) {
      setPluginExtensions();
    }
    install0(pack2jar);
    waitForExtensionsInstalled();
  }

  private void install0(boolean pack2jar) throws Exception, BundleException {
    doCreate(pack2jar);
    doInstall();
    m_bundle.start();
  }

  private void doCreate(boolean pack2jar) throws Exception {
    setFile(
        "META-INF/MANIFEST.MF",
        StringUtils.join(new String[]{
            "Manifest-Version: 1.0",
            "Bundle-ManifestVersion: 2",
            MessageFormat.format("Bundle-SymbolicName: {0};singleton:=true", m_id),
            "Bundle-Version: 1.0.0",
            "Bundle-ClassPath: .",
            "Require-Bundle: org.eclipse.wb.core,org.eclipse.wb.core.java,org.eclipse.wb.core.xml",
            "Bundle-ActivationPolicy: lazy"}, "\n"));
    if (pack2jar) {
      pack2jar();
    }
    m_created = true;
  }

  private void pack2jar() throws Exception {
    File bundleJarFile = new File(m_bundleFolder.getParent(), m_id + ".jar");
    // pack into JAR file
    {
      ZipFileFactory jarFile = new ZipFileFactory(new FileOutputStream(bundleJarFile));
      pack2jar(m_bundleFolder, jarFile, StringUtils.EMPTY);
      jarFile.close();
    }
    // use JAR file instead of folder (already deleted)
    m_bundleFile = bundleJarFile;
  }

  private void pack2jar(File folder, ZipFileFactory jar, String root) throws Exception {
    for (File file : folder.listFiles()) {
      if (file.isFile()) {
        jar.add(root + file.getName(), new FileInputStream(file));
      }
      if (file.isDirectory()) {
        pack2jar(file, jar, root + file.getName() + "/");
      }
      file.delete();
    }
    folder.delete();
  }

  private void doInstall() throws Exception {
    m_bundle = m_context.installBundle(m_bundleFile.toURI().toURL().toExternalForm());
  }

  public void uninstall() throws Exception {
    Assert.isNotNull(m_bundle, "Bundle %s is not installed.", m_id);
    // we should wait until OSGi framework notifies all listeners that Bundle was uninstalled
    final AtomicBoolean uninstallEventProcessed = new AtomicBoolean();
    m_context.addBundleListener(new BundleListener() {
      public void bundleChanged(BundleEvent event) {
        if (event.getType() == BundleEvent.UNINSTALLED && event.getBundle() == m_bundle) {
          uninstallEventProcessed.set(true);
          m_context.removeBundleListener(this);
        }
      }
    });
    // request uninstalling
    m_bundle.uninstall();
    // wait for Bundle uninstalling event
    while (!uninstallEventProcessed.get()) {
      try {
        Thread.sleep(0);
      } catch (Throwable e) {
      }
    }
    // wait for uninstalling extensions
    waitForExtensionsUnInstalled();
    // done, we can clear reference now
    m_bundle = null;
  }

  public void dispose() throws Exception {
    // uninstall, if not yet
    if (m_bundle != null) {
      uninstall();
    }
    // remove folder or file
    FileUtils.deleteDirectory(m_bundleFolder);
    m_bundleFile.delete();
  }

  public void waitForExtension(String pointId, String extensionId) throws Exception {
    String qualifiedExtensionId = m_id + "." + extensionId;
    while (ExternalFactoriesHelper.getExtension(pointId, qualifiedExtensionId) == null) {
      TestUtils.waitEventLoop(0);
    }
  }

  public void waitForNoExtension(String pointId, String extensionId) throws Exception {
    String qualifiedExtensionId = m_id + "." + extensionId;
    while (ExternalFactoriesHelper.getExtension(pointId, qualifiedExtensionId) != null) {
      TestUtils.waitEventLoop(0);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getId() {
    return m_id;
  }

  public Bundle getBundle() {
    return m_bundle;
  }
}
