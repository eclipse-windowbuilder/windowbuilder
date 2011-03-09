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
package org.eclipse.wb.internal.ercp.parser;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.core.eval.ExecutionFlowUtils;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ThisCreationSupport;
import org.eclipse.wb.internal.core.model.variable.ThisVariableSupport;
import org.eclipse.wb.internal.core.parser.IParseFactory;
import org.eclipse.wb.internal.core.parser.ParseRootContext;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.BundleClassLoader;
import org.eclipse.wb.internal.core.utils.reflect.CompositeClassLoader;
import org.eclipse.wb.internal.core.utils.reflect.ProjectClassLoader;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.ercp.Activator;
import org.eclipse.wb.internal.ercp.preferences.IPreferenceConstants;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import org.osgi.framework.Bundle;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * {@link IParseFactory} for eRCP.
 * 
 * @author lobas_av
 * @author mitin_aa
 * @coverage ercp.parser
 */
public final class ParseFactory extends org.eclipse.wb.internal.swt.parser.ParseFactory {
  private static final String ESWT_CONVERGED = "eswt-converged";
  private static final String ESWT_CONVERGED_JAR = ESWT_CONVERGED + ".jar";
  private static final String ESWT_CONVERGED_DLL = ESWT_CONVERGED + ".dll";

  ////////////////////////////////////////////////////////////////////////////
  //
  // IParseFactory
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public ParseRootContext getRootContext(AstEditor editor,
      TypeDeclaration typeDeclaration,
      ITypeBinding typeBinding) throws Exception {
    // check for eRCP project
    if (!is_eRCPContext(editor)) {
      return null;
    }
    // check for SWT type
    if (!hasValidObjects(typeDeclaration)) {
      return null;
    }
    // initialize ClassLoader
    initializeClassLoader(editor);
    // check for @wbp.parser.entryPoint
    {
      MethodDeclaration method = ExecutionFlowUtils.getExecutionFlow_entryPoint(typeDeclaration);
      if (method != null) {
        List<MethodDeclaration> rootMethods = Lists.newArrayList(method);
        return new ParseRootContext(null, new ExecutionFlowDescription(rootMethods));
      }
    }
    // support for org.eclipse.swt.widgets.Widget
    boolean isWidget = AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.swt.widgets.Widget");
    boolean isPreferencePage =
        AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.jface.preference.PreferencePage");
    boolean isViewPart = AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.ui.part.ViewPart");
    if (isWidget || isPreferencePage || isViewPart) {
      ITypeBinding typeBinding_super = typeBinding.getSuperclass();
      // prepare class of component
      Class<?> superClass = getClass(editor, typeBinding_super);
      // prepare creation
      MethodDeclaration constructor = getConstructor(editor, typeDeclaration);
      ThisCreationSupport creationSupport = new ThisCreationSupport(constructor);
      // try to create JavaInfo
      JavaInfo javaInfo = JavaInfoUtils.createJavaInfo(editor, superClass, creationSupport);
      if (javaInfo != null) {
        javaInfo.setVariableSupport(new ThisVariableSupport(javaInfo, constructor));
        // prepare root context
        List<MethodDeclaration> rootMethods = Lists.newArrayList();
        rootMethods.add(constructor);
        return new ParseRootContext(javaInfo, new ExecutionFlowDescription(rootMethods));
      }
    }
    // no root found
    return null;
  }

  @Override
  public JavaInfo create(AstEditor editor,
      ClassInstanceCreation creation,
      IMethodBinding methodBinding,
      ITypeBinding typeBinding,
      Expression[] arguments,
      JavaInfo[] argumentInfos) throws Exception {
    if (!hasERCP(editor)) {
      return null;
    }
    if (creation.getAnonymousClassDeclaration() != null) {
      typeBinding = typeBinding.getSuperclass();
    }
    return super.create(editor, creation, methodBinding, typeBinding, arguments, argumentInfos);
  }

  @Override
  public boolean isToolkitObject(AstEditor editor, ITypeBinding typeBinding) throws Exception {
    return is_eRCPContext(editor) && isSWTObject(typeBinding);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String KEY_HAS_ERCP = ParseFactory.class.getName();

  /**
   * @return <code>true</code> if given {@link AstEditor} can use eRCP.
   */
  private static boolean hasERCP(AstEditor editor) {
    Boolean result = (Boolean) editor.getGlobalValue(KEY_HAS_ERCP);
    if (result == null) {
      IJavaProject javaProject = editor.getJavaProject();
      try {
        result = javaProject.findType("org.eclipse.ercp.swt.mobile.MobileShell") != null;
      } catch (Throwable e) {
        result = false;
      }
      editor.putGlobalValue(KEY_HAS_ERCP, result);
    }
    return result.booleanValue();
  }

  /**
   * @return <code>true</code> if {@link IJavaProject} of given {@link AstEditor} is eRCP project.
   */
  private static boolean is_eRCPContext(AstEditor editor) throws Exception {
    return editor.getJavaProject().findType("org.eclipse.ercp.swt.mobile.MobileShell") != null;
  }

  /**
   * @return <code>true</code> if given {@link TypeDeclaration} contains eRCP objects.
   */
  private static boolean hasValidObjects(TypeDeclaration typeDeclaration) throws Exception {
    final boolean[] isSwt = new boolean[1];
    // check top level type
    {
      ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(typeDeclaration);
      if (AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.swt.widgets.Dialog")
          || AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.jface.preference.PreferencePage")) {
        return true;
      }
    }
    // check for Widget's creations
    typeDeclaration.accept(new ASTVisitor() {
      @Override
      public void postVisit(ASTNode node) {
        if (!isSwt[0] && node instanceof Expression) {
          Expression expression = (Expression) node;
          ITypeBinding expressionBinding = AstNodeUtils.getTypeBinding(expression);
          if (AstNodeUtils.isSuccessorOf(expressionBinding, "org.eclipse.swt.widgets.Widget")) {
            isSwt[0] = true;
          }
        }
      }
    });
    return isSwt[0];
  }

  /**
   * @return <code>true</code> if given type binding is eRCP object.
   */
  private static boolean isSWTObject(ITypeBinding typeBinding) throws Exception {
    if (typeBinding == null) {
      return false;
    }
    // check Widget
    if (AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.swt.widgets.Widget")) {
      return true;
    }
    // check Dialog
    if (AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.swt.widgets.Dialog")) {
      return true;
    }
    // check JFace resources
    if (AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.jface.resource.ResourceRegistry")) {
      return true;
    }
    // check Layout
    if (AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.swt.widgets.Layout")) {
      return true;
    }
    // check GridLayout
    if (AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.swt.layout.GridData")) {
      return true;
    }
    // check FormLayout
    if (AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.swt.layout.FormAttachment")) {
      return true;
    }
    if (AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.swt.layout.FormData")) {
      return true;
    }
    // check RowLayout
    if (AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.swt.layout.RowData")) {
      return true;
    }
    // check ListBoxItem
    if (AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.ercp.swt.mobile.ListBoxItem")) {
      return true;
    }
    //
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ClassLoader
  //
  ////////////////////////////////////////////////////////////////////////////
  private static ClassLoader eSwtClassLoader;

  @Override
  protected String getToolkitId() {
    return IPreferenceConstants.TOOLKIT_ID;
  }

  @Override
  protected void initializeClassLoader_parent(AstEditor editor,
      CompositeClassLoader parentClassLoader) throws Exception {
    parentClassLoader.add(get_eSWTClassLoader(editor), null);
    super.initializeClassLoader_parent(editor, parentClassLoader);
  }

  /**
   * @return the shared {@link ClassLoader} for eSWT.
   */
  private static ClassLoader get_eSWTClassLoader(AstEditor editor) throws Exception {
    // lazy-creation of main eSWT class loader
    if (eSwtClassLoader == null) {
      // prepare native libraryPath's
      String nativeOsPath =
          Activator.getAbsolutePath("os")
              + "/"
              + Platform.getOS()
              + "/"
              + Platform.getOSArch()
              + "/";
      System.setProperty(
          "EmbeddedShot-A4D28DAF-F036-4219-A942-C8244AC5595D",
          nativeOsPath + System.mapLibraryName("EmbeddedShot"));
      System.setProperty(
          "SWTDesigner-613C5105-E42D-4b7c-B97B-AE127B880576",
          nativeOsPath + System.mapLibraryName("SWTDesigner"));
      // prepare %eSWT%.jar library
      String eSwtJarPath = "/ws/" + Platform.getWS() + "/" + ESWT_CONVERGED_JAR;
      String eSWTLocation = null;
      {
        String[] locations =
            ProjectClassLoader.computeFullRuntimeClassPath(editor.getJavaProject());
        for (String location : locations) {
          if (location.endsWith(eSwtJarPath)) {
            eSWTLocation = location;
            break;
          }
        }
      }
      // prepare URL's
      URL[] urls = new URL[2];
      urls[0] = Activator.getAbsoluteEntry("eswt.shot-support.jar");
      Assert.isNotNull(urls[0]);
      urls[1] = new File(eSWTLocation).toURI().toURL();
      Assert.isNotNull(urls[1]);
      // prepare "parent" ClassLoader
      BundleClassLoader parentClassLoader;
      {
        Bundle runtimeBundle = Platform.getBundle("org.eclipse.wb.runtime");
        parentClassLoader = new BundleClassLoader(runtimeBundle);
      }
      // create class loader
      final String eSwtPluginPath = eSWTLocation.substring(0, eSWTLocation.indexOf(eSwtJarPath));
      eSwtClassLoader = new URLClassLoader(urls, parentClassLoader) {
        @Override
        protected String findLibrary(String libname) {
          // be able to load eswt-converged.dll from plugin
          if (ESWT_CONVERGED.equals(libname)) {
            return eSwtPluginPath
                + "/os/"
                + Platform.getOS()
                + "/"
                + Platform.getOSArch()
                + "/"
                + ESWT_CONVERGED_DLL;
          }
          return super.findLibrary(libname);
        };
      };
      // freeze handle eSWT events for widgets
      installCallbacks(eSwtClassLoader);
    }
    // OK, we have eSWT ClassLoader
    return eSwtClassLoader;
  }

  private static void installCallbacks(ClassLoader classLoader) throws Exception {
    installCallbacks(classLoader, "org.eclipse.swt.widgets.Table", "eventsRegistered");
    installCallbacks(classLoader, "org.eclipse.swt.widgets.Tree", "eventsRegistered");
    installCallbacks(classLoader, "org.eclipse.ercp.swt.mobile.ConstrainedText", "isInit");
    installCallbacks(classLoader, "org.eclipse.ercp.swt.mobile.DateEditor", "isInit");
    installCallbacks(classLoader, "org.eclipse.ercp.swt.mobile.ListView", "isInit");
    installCallbacks(classLoader, "org.eclipse.swt.browser.Browser", "eventsRegistered");
  }

  private static void installCallbacks(ClassLoader classLoader, String className, String fieldName)
      throws Exception {
    Class<?> componentClass = classLoader.loadClass(className);
    ReflectionUtils.getFieldByName(componentClass, fieldName).set(null, Boolean.TRUE);
  }
}