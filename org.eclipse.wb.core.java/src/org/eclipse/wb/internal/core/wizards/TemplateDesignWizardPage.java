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
package org.eclipse.wb.internal.core.wizards;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.description.LayoutDescription;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.description.ToolkitDescriptionJava;
import org.eclipse.wb.internal.core.model.description.helpers.LayoutDescriptionHelper;
import org.eclipse.wb.internal.core.model.variable.FieldUniqueVariableSupport;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

/**
 * Wizard page responsible to create Java elements use special template.
 *
 * @author lobas_av
 * @coverage core.wizards.ui
 */
public abstract class TemplateDesignWizardPage extends AbstractDesignWizardPage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Template
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Add to <code>newType</code> imports, fields, methods and constructor from special template file
   * <code>file</code>.
   */
  protected void fillTypeFromTemplate(IType newType,
      ImportsManager imports,
      IProgressMonitor monitor,
      InputStream file) throws CoreException {
    try {
      StringBuffer method = null;
      StringBuffer constructor = null;
      boolean isMethod = false;
      boolean isConstructor = false;
      //
      List<String> lines = IOUtils.readLines(file);
      for (Iterator<String> I = lines.iterator(); I.hasNext();) {
        String line = I.next();
        // handle import
        if (line.startsWith("import ")) {
          int start = 7;
          int end = line.indexOf(';', start + 1);
          if (end != -1) {
            imports.addImport(line.substring(start, end));
          }
        }
        // handle start of constructor
        if (line.startsWith("constructor")) {
          isMethod = false;
          isConstructor = true;
          constructor = new StringBuffer();
          method = null;
          continue;
        }
        // handle start of method
        if (line.startsWith("method")) {
          isMethod = true;
          isConstructor = false;
          method = new StringBuffer();
          constructor = null;
          continue;
        }
        // handle field
        if (line.startsWith("field")) {
          line = I.next();
          StringBuffer field = new StringBuffer(line);
          field = performSubstitutions(field, imports);
          String string = field.toString();
          newType.createField(string, null, false, null);
          continue;
        }
        // handle inner type
        if (line.startsWith("innerTypeLine")) {
          line = I.next();
          StringBuffer buffer = new StringBuffer(line);
          buffer = performSubstitutions(buffer, imports);
          String content = buffer.toString();
          newType.createType(content, null, false, null);
          continue;
        }
        // handle separator
        if (line.length() == 0) {
          // replace special keys (%...%) on values
          if (method != null) {
            method = performSubstitutions(method, imports);
          }
          if (constructor != null) {
            constructor = performSubstitutions(constructor, imports);
          }
          // add line to method or constructor body
          if (isMethod && method != null && method.length() > 0) {
            newType.createMethod(method.toString(), null, false, null);
          } else if (isConstructor && constructor != null && constructor.length() > 0) {
            newType.createMethod(
                "public " + getTypeName() + constructor.toString(),
                null,
                false,
                null);
          }
          // reset state
          isMethod = false;
          isConstructor = false;
          method = null;
          constructor = null;
          continue;
        }
        // add method or constructor
        if (isMethod && method != null) {
          if (ProjectUtils.isJDK15(newType.getJavaProject()) || !line.trim().equals("@Override")) {
            method.append(line + AstEditor.DEFAULT_END_OF_LINE);
          }
        } else if (isConstructor && constructor != null) {
          constructor.append(line + AstEditor.DEFAULT_END_OF_LINE);
        }
        // check handle last line
        if (!I.hasNext() && isMethod && method != null && method.length() > 0) {
          newType.createMethod(method.toString(), null, false, null);
        }
      }
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    } finally {
      IOUtils.closeQuietly(file);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Substitution support
  //
  ////////////////////////////////////////////////////////////////////////////
  private StringBuffer performSubstitutions(StringBuffer buffer, ImportsManager imports) {
    String code = performSubstitutions(buffer.toString(), imports);
    return new StringBuffer(code);
  }

  /**
   * Replace special keys (%...%) on values.
   */
  protected String performSubstitutions(String code, ImportsManager imports) {
    loadUIClasses();
    code = StringUtils.replace(code, "%TypeName%", getTypeName());
    code = StringUtils.replace(code, "%DefaultFormSize%", getDefaultFormSize());
    code = StringUtils.replace(code, "%this%", getInstanceFieldQualification());
    code = performFieldPrefixesSubstitutions(code);
    return code;
  }

  protected static String performFieldPrefixesSubstitutions(String code) {
    code =
        StringUtils.replace(
            code,
            "%field-prefix%",
            JavaCore.getOption(JavaCore.CODEASSIST_FIELD_PREFIXES));
    code =
        StringUtils.replace(
            code,
            "%static-field-prefix%",
            JavaCore.getOption(JavaCore.CODEASSIST_STATIC_FIELD_PREFIXES));
    return code;
  }

  /**
   * Load classes what need UI thread.
   */
  protected void loadUIClasses() {
    Display.getDefault().syncExec(new Runnable() {
      public void run() {
        try {
          Class.forName("org.eclipse.wb.internal.draw2d.IColorConstants");
        } catch (Throwable e) {
        }
      }
    });
  }

  public String getDefaultFormSize() {
    IPreferenceStore preferences = getToolkitDescription().getPreferences();
    int width = preferences.getInt(IPreferenceConstants.P_GENERAL_DEFAULT_TOP_WIDTH);
    int height = preferences.getInt(IPreferenceConstants.P_GENERAL_DEFAULT_TOP_HEIGHT);
    return Integer.toString(width) + ", " + Integer.toString(height);
  }

  public String getInstanceFieldQualification() {
    IPreferenceStore preferences = getToolkitDescription().getPreferences();
    return preferences.getBoolean(FieldUniqueVariableSupport.P_PREFIX_THIS) ? "this." : "";
  }

  public final String getCreateMethod(String defaultMethod) {
    String forcedMethodName = getToolkitDescription().getGenerationSettings().getForcedMethodName();
    return StringUtils.isEmpty(forcedMethodName) ? defaultMethod : forcedMethodName;
  }

  public final String getLayoutCode(String codePrefix, ImportsManager imports) {
    ToolkitDescription toolkit = getToolkitDescription();
    LayoutDescription layoutDescription =
        LayoutDescriptionHelper.get(
            toolkit,
            toolkit.getPreferences().getString(IPreferenceConstants.P_LAYOUT_DEFAULT));
    if (layoutDescription != null) {
      imports.addImport(layoutDescription.getLayoutClassName());
      return codePrefix + "setLayout(" + layoutDescription.getSourceSmart() + ");";
    }
    return "";
  }

  protected abstract ToolkitDescriptionJava getToolkitDescription();
}