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
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.swing.laf.LafSupport;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;

/**
 * Class describing {@link LookAndFeel} which represents the default LAF of this system.
 * 
 * @author mitin_aa
 * @coverage swing.laf.model
 */
public class SystemLafInfo extends LafInfo {
  public static final String SYSTEM_LAF_NAME = "<system>";
  public static final LafInfo INSTANCE = new SystemLafInfo();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Private constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private SystemLafInfo() {
    super("__wbp_" + SYSTEM_LAF_NAME, SYSTEM_LAF_NAME, UIManager.getSystemLookAndFeelClassName());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // apply LAF in main() method
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void applyInMain(AstEditor editor) throws Exception {
    MethodDeclaration mainMethod = LafSupport.getMainMethod(editor);
    if (mainMethod == null) {
      // no main method 
      return;
    }
    // look up for setLookAndFeel method
    MethodInvocation setLookAndFeelMethod = LafSupport.getSetLookAndFeelMethod(mainMethod);
    if (setLookAndFeelMethod == null) {
      // no any setLookAndFeel method invocation, just add the new one
      StatementTarget target = new StatementTarget(mainMethod, true);
      editor.addStatement("try {\n\tjavax.swing.UIManager.setLookAndFeel("
          + "javax.swing.UIManager.getSystemLookAndFeelClassName());\n"
          + "} catch (Throwable e) {\n\te.printStackTrace();\n}", target);
    } else {
      // modify any existing to set system LAF class name 
      editor.replaceExpression(
          DomGenerics.arguments(setLookAndFeelMethod).get(0),
          "javax.swing.UIManager.getSystemLookAndFeelClassName()");
    }
    // commit changes
    editor.commitChanges();
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
