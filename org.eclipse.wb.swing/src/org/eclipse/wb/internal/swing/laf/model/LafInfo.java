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
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.swing.laf.LafSupport;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

import javax.swing.LookAndFeel;

/**
 * Base class for {@link LookAndFeel} info.
 * 
 * @author mitin_aa
 * @coverage swing.laf.models
 */
public class LafInfo extends LafEntryInfo {
  private CategoryInfo m_category;
  private String m_className;
  private int m_usageCount;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public LafInfo(String id, String name, String className) {
    super(id, name);
    m_className = className;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the class name of LookAndFeel.
   */
  public final String getClassName() {
    return m_className;
  }

  /**
   * Sets the class name of LookAndFeel.
   */
  public void setClassName(String className) {
    m_className = className;
  }

  /**
   * @return the parent {@link CategoryInfo}.
   */
  public CategoryInfo getCategory() {
    return m_category;
  }

  /**
   * Sets the new parent {@link CategoryInfo}.
   */
  public void setCategory(CategoryInfo category) {
    m_category = category;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MRU support
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Indicates that this LAF was selected, increase number of usage times to place it first in MRU
   * list.
   */
  public void increaseUsageCount() {
    m_usageCount++;
  }

  /**
   * @return the number of selection times of this LAF.
   */
  public int getUsageCount() {
    return m_usageCount;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Load the LAF class and creates an instance of LAF class.
   * 
   * @return the instance of LAF class.
   */
  public LookAndFeel getLookAndFeelInstance() throws Exception {
    Class<?> lafClass = Class.forName(getClassName());
    return (LookAndFeel) lafClass.newInstance();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // apply LAF in main() method
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Places (or modifies existing) the code calling UIManager.setLookAndFeel() method in main
   * method. Called when appropriate preference enabled.
   */
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
      editor.addStatement("try {\n\tjavax.swing.UIManager.setLookAndFeel(\""
          + getClassName()
          + "\");\n} catch (Throwable e) {\n\te.printStackTrace();\n}", target);
    } else {
      // modify existing 
      String methodSignature = AstNodeUtils.getMethodSignature(setLookAndFeelMethod);
      if (LafSupport.SET_LOOK_AND_FEEL_LAF.equals(methodSignature)) {
        // UIManager.setLookAndFeel(javax.swing.LookAndFeel)
        editor.replaceExpression(DomGenerics.arguments(setLookAndFeelMethod).get(0), "new "
            + getClassName()
            + "()");
      } else if (LafSupport.SET_LOOK_AND_FEEL_STRING.equals(methodSignature)) {
        // UIManager.setLookAndFeel(java.lang.String) 
        editor.replaceExpression(DomGenerics.arguments(setLookAndFeelMethod).get(0), "\""
            + getClassName()
            + "\"");
      }
    }
    // commit changes
    editor.commitChanges();
  }
}
