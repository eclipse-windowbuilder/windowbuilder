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
package org.eclipse.wb.internal.rcp.model.rcp;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.swt.custom.CTabItem;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.services.IServiceLocator;

import org.apache.commons.lang.NotImplementedException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Model for {@link EditorPart}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
public class EditorPartInfo extends WorkbenchPartLikeInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public EditorPartInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    contributeExtensionProperty();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  private void contributeExtensionProperty() throws Exception {
    new ExtensionPropertyHelper(this, "org.eclipse.ui.editors", "editor") {
      @Override
      protected Property[] createProperties() {
        return new Property[]{
            createStringProperty("name"),
            createIconProperty("icon"),
            createStringProperty("extensions"),
            createBooleanProperty("default", false)};
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Rendering
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void render() throws Exception {
    setEditorSite();
    super.render();
  }

  private void setEditorSite() throws Exception {
    ClassLoader editorLoader = JavaInfoUtils.getClassLoader(this);
    Class<?> editorSiteClass = editorLoader.loadClass("org.eclipse.ui.IEditorSite");
    Class<?> editorInputClass = editorLoader.loadClass("org.eclipse.ui.IEditorInput");
    // create IEditorSite
    Object editorSite =
        Proxy.newProxyInstance(
            editorLoader,
            new Class<?>[]{editorSiteClass},
            new InvocationHandler() {
              @Override
              public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String signature = ReflectionUtils.getMethodSignature(method);
                if (signature.equals("toString()")) {
                  return "IEditorSite_stub";
                }
                if (signature.equals("hashCode()")) {
                  return 0;
                }
                if (signature.equals("getId()")) {
                  return getID();
                }
                if (signature.equals("getWorkbenchWindow()")) {
                  return DesignerPlugin.getActiveWorkbenchWindow();
                }
                // IServiceLocator
                if (signature.equals("hasService(java.lang.Class)")) {
                  IServiceLocator serviceLocator = DesignerPlugin.getActiveWorkbenchWindow();
                  return serviceLocator.hasService((Class<?>) args[0]);
                }
                if (signature.equals("getService(java.lang.Class)")) {
                  IServiceLocator serviceLocator = DesignerPlugin.getActiveWorkbenchWindow();
                  return serviceLocator.getService((Class<?>) args[0]);
                }
                // not implemented
                throw new NotImplementedException(method.toString());
              }
            });
    // create org.eclipse.ui.IEditorInput
    Object editorInput =
        Proxy.newProxyInstance(
            editorLoader,
            new Class<?>[]{editorInputClass},
            new InvocationHandler() {
              @Override
              public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Class<?> returnType = method.getReturnType();
                return ReflectionUtils.getDefaultValue(returnType);
              }
            });
    // call init(IEditorSite,IEditorInput)
    ReflectionUtils.invokeMethod(
        getObject(),
        "init(org.eclipse.ui.IEditorSite,org.eclipse.ui.IEditorInput)",
        editorSite,
        editorInput);
  }

  @Override
  protected String getGUIMethodName() {
    return "createPartControl";
  }

  @Override
  protected void configureTabItem(CTabItem tabItem) throws Exception {
    configureTabItem_fromExtension(tabItem, "EditorPart");
  }
}
