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
package org.eclipse.wb.internal.swing.laf.model;

import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.swing.laf.LafSupport;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TryStatement;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import javax.swing.UIManager;

/**
 * Used when no LAF selected explicitly.
 * 
 * @author mitin_aa
 * @coverage swing.laf.model
 */
public class UndefinedLafInfo extends LafInfo {
  public static final String UNDEFINED_LAF_NAME = "<undefined>";
  public static final LafInfo INSTANCE = new UndefinedLafInfo();
  private static Properties m_swingProperties;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private UndefinedLafInfo() {
    super("__wbp_" + UNDEFINED_LAF_NAME, UNDEFINED_LAF_NAME, getClassName0());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // apply LAF in main() method
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void applyInMain(AstEditor editor) throws Exception {
    // remove any setLookAndFeel method invocation, remove try..catch clauses if they are empty
    MethodDeclaration mainMethod = LafSupport.getMainMethod(editor);
    if (mainMethod == null) {
      // no main method 
      return;
    }
    // look up for setLookAndFeel method
    MethodInvocation setLookAndFeelMethod = LafSupport.getSetLookAndFeelMethod(mainMethod);
    if (setLookAndFeelMethod != null) {
      // remove it
      Statement enclosingMethodStatement = AstNodeUtils.getEnclosingStatement(setLookAndFeelMethod);
      // look for try..catch
      TryStatement tryStatement =
          AstNodeUtils.getEnclosingNode(enclosingMethodStatement, TryStatement.class);
      editor.removeStatement(enclosingMethodStatement);
      // remove try..catch if try statement has no statements now
      if (tryStatement != null && tryStatement.getBody().statements().isEmpty()) {
        editor.removeEnclosingStatement(tryStatement);
      }
      // done
      editor.commitChanges();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // LAFInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Try to find which LAF used as "undefined", i.e. when no LAF explicitly specified in main.
   */
  private static synchronized String getClassName0() {
    // 1. Try to get system LAF class name from "swing.systemlaf" property name 
    // in $(java.home)/lib/swing.properties file.
    if (m_swingProperties == null) {
      String javaHome = System.getProperty("java.home");
      if (javaHome != null) {
        String propertiesFileName =
            javaHome + File.separator + "lib" + File.separator + "swing.properties";
        m_swingProperties = new Properties();
        File file = new File(propertiesFileName);
        if (file.exists()) {
          try {
            FileInputStream inputStream = new FileInputStream(file);
            try {
              m_swingProperties.load(inputStream);
            } finally {
              IOUtils.closeQuietly(inputStream);
            }
          } catch (Throwable e) {
            // Just ignore any exceptions.
          }
        }
      }
    }
    // 2. If no LAF defined in "swing.systemlaf" use cross-platform LAF like UIManager does.
    String systemLAFClassName = m_swingProperties.getProperty("swing.systemlaf");
    if (systemLAFClassName == null) {
      systemLAFClassName = UIManager.getCrossPlatformLookAndFeelClassName();
    }
    return systemLAFClassName;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // No access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setClassName(String className) {
  }
}
