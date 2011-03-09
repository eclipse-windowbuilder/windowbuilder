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
package org.eclipse.wb.internal.swt.model.jface.resource;

import org.eclipse.wb.core.model.IRootProcessor;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.model.util.GlobalStateJava;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.internal.swt.support.AbstractSupport;
import org.eclipse.wb.internal.swt.support.DisplaySupport;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.ResourceRegistry;
import org.eclipse.swt.widgets.Display;

import java.lang.reflect.Array;
import java.util.List;

/**
 * When we create instances of various {@link ResourceRegistry}'s, for example {@link ColorRegistry}
 * , they usually want to be disposed with {@link Display}, so they use
 * {@link Display#disposeExec(Runnable)}. But our {@link Display} instance lives all time while
 * Eclipse lives. So, practically we have memory leak: we keep in memory instance of
 * {@link ResourceRegistry}, its allocated resources, and what is much worse - instance of
 * {@link ClassLoader} that loaded this {@link ResourceRegistry}.
 * <p>
 * So, we need some trick to dispose {@link ResourceRegistry}'s with editor and remove its dispose
 * {@link Runnable} from {@link Display}.
 * 
 * @author scheglov_ke
 * @author sablin_aa
 * @coverage swt.model.jface
 */
public final class ResourceRegistryRootProcessor implements IRootProcessor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IRootProcessor
  //
  ////////////////////////////////////////////////////////////////////////////
  public void process(JavaInfo root, List<JavaInfo> components) throws Exception {
    processRoot(root);
    processComponents(root, components);
  }

  private void processRoot(final JavaInfo root) {
    root.addBroadcastListener(new ObjectEventListener() {
      @Override
      public void refreshDispose() throws Exception {
        disposeResourceRegistries(root);
      }

      @Override
      public void dispose() throws Exception {
        disposeResourceRegistries(root);
      }
    });
  }

  private void processComponents(final JavaInfo root, final List<JavaInfo> components)
      throws Exception {
    // bind {@link ResourceRegistryInfo}'s into hierarchy.
    for (JavaInfo javaInfo : components) {
      if (javaInfo instanceof ResourceRegistryInfo) {
        ResourceRegistryInfo resourceRegistryInfo = (ResourceRegistryInfo) javaInfo;
        RegistryContainerInfo.get(root).addChild(resourceRegistryInfo);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Disposes all {@link ResourceRegistry}'s, loaded for given hierarchy.
   * 
   * @param javaInfo
   *          the root {@link JavaInfo} of hierarchy.
   */
  private static void disposeResourceRegistries(JavaInfo javaInfo) throws Exception {
    EditorState editorState = EditorState.get(javaInfo.getEditor());
    // SWT utilities require "activeJavaInfo"
    ObjectInfo activeObject = GlobalState.getActiveObject();
    if (!(activeObject instanceof JavaInfo)) {
      return;
    }
    JavaInfo activeJavaInfo = (JavaInfo) activeObject;
    try {
      GlobalStateJava.activate(javaInfo);
      if (AbstractSupport.is_SWT()) {
        Object display = DisplaySupport.getDefault();
        Object disposeList = ReflectionUtils.getFieldObject(display, "disposeList");
        if (disposeList != null) {
          int length = Array.getLength(disposeList);
          for (int i = 0; i < length; i++) {
            Object runnable = Array.get(disposeList, i);
            if (runnable != null) {
              try {
                Object registry = ReflectionUtils.getFieldObject(runnable, "this$0");
                Class<?> registryClass = registry.getClass();
                if (editorState.isLoadedFrom(registryClass)) {
                  // remove listener
                  Array.set(disposeList, i, null);
                  // clear resources
                  ReflectionUtils.invokeMethod(runnable, "run()");
                }
              } catch (Throwable e) {
              }
            }
          }
        }
      }
    } finally {
      GlobalStateJava.activate(activeJavaInfo);
    }
  }
}
