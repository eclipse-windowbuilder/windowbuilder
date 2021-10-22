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

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.nls.bundle.AbstractBundleSource;
import org.eclipse.wb.internal.core.nls.bundle.eclipse.modern.ModernEclipseSource;
import org.eclipse.wb.internal.core.nls.bundle.eclipse.old.EclipseSource;
import org.eclipse.wb.internal.core.nls.edit.EditableSource;
import org.eclipse.wb.internal.core.nls.edit.IEditableSource;
import org.eclipse.wb.internal.core.nls.model.LocaleInfo;
import org.eclipse.wb.tests.designer.core.AbstractJavaProjectTest;
import org.eclipse.wb.tests.designer.core.AbstractJavaTest;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

/**
 * Utilities for testing NLS.
 *
 * @author scheglov_ke
 */
public class NlsTestUtils {
  /**
   * Create new empty {@link IEditableSource} with given name.
   */
  public static IEditableSource createEmptyEditable(String name) {
    EditableSource editableSource = new EditableSource();
    String title = "NEW: " + name;
    editableSource.setShortTitle(title);
    editableSource.setLongTitle(title);
    editableSource.add(LocaleInfo.DEFAULT, Maps.<String, String>newHashMap());
    editableSource.setKeyGeneratorStrategy(AbstractBundleSource.KEY_GENERATOR);
    return editableSource;
  }

  /**
   * Creates accessor and properties for {@link ModernEclipseSource}.
   */
  public static void create_EclipseModern_AccessorAndProperties() throws Exception {
    AbstractJavaProjectTest.setFileContentSrc(
        "test/messages.properties",
        DesignerTestCase.getSourceDQ("frame_title=My JFrame", "frame_name=My name"));
    AbstractJavaProjectTest.setFileContentSrc(
        "test/messages_it.properties",
        DesignerTestCase.getSourceDQ("frame_title=My JFrame", "frame_name=My name"));
    AbstractJavaProjectTest.setFileContentSrc("test/Messages.java", DesignerTestCase.getSourceDQ(
        "package test;",
        "import org.eclipse.osgi.util.NLS;",
        "public class Messages extends NLS {",
        "  private static final String BUNDLE_NAME = 'test.messages'; //$NON-NLS-1$",
        "  public static String frame_title;",
        "  public static String frame_name;",
        "  private Messages() {}",
        "  static {",
        "    NLS.initializeMessages(BUNDLE_NAME, Messages.class);",
        "  }",
        "}"));
    AbstractJavaProjectTest.waitForAutoBuild();
  }

  /**
   * Creates accessor of {@link EclipseSource}.
   */
  public static void create_EclipseOld_Accessor(AbstractJavaTest javaTest, boolean withDefault)
      throws Exception {
    create_EclipseOld_Accessor(javaTest, "test.messages", withDefault);
  }

  /**
   * Creates accessor of {@link EclipseSource}.
   */
  public static void create_EclipseOld_Accessor(AbstractJavaTest javaTest,
      String bundleName,
      boolean withDefault) throws Exception {
    javaTest.createModelCompilationUnit("test", "Messages.java", DesignerTestCase.getSourceDQ(
        "package test;",
        "import java.beans.Beans;",
        "import java.util.MissingResourceException;",
        "import java.util.ResourceBundle;",
        "public class Messages {",
        "  private static final String BUNDLE_NAME = '" + bundleName + "'; //$NON-NLS-1$",
        "  private static final ResourceBundle RESOURCE_BUNDLE = loadBundle();",
        "  private static ResourceBundle loadBundle() {",
        "    return ResourceBundle.getBundle(BUNDLE_NAME);",
        "  }",
        withDefault
            ? "  public static String getString(String key, String defValue) {"
            : "  public static String getString(String key) {",
        "    try {",
        "      ResourceBundle bundle = Beans.isDesignTime() ? loadBundle() : RESOURCE_BUNDLE;",
        "      return bundle.getString(key);",
        "    } catch (MissingResourceException e) {",
        withDefault ? "      return defValue;" : "      return '!' + key + '!';",
        "    }",
        "  }",
        "}"));
  }
}
