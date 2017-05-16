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
package org.eclipse.wb.internal.core.model.util;

import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.swt.widgets.Display;

/**
 * Utils for different Workspace related operations.
 *
 * @author scheglov_ke
 * @coverage core.model.util
 */
public class WorkspaceUtils {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private WorkspaceUtils() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Job utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Process UI input but do not return for the specified time interval.
   *
   * @param waitTimeMillis
   *          the number of milliseconds
   */
  public static void delay(long waitTimeMillis) {
    Display display = Display.getCurrent();
    // If this is the user interface thread, then process input
    if (display != null) {
      long endTimeMillis = System.currentTimeMillis() + waitTimeMillis;
      while (System.currentTimeMillis() < endTimeMillis) {
        if (!display.readAndDispatch()) {
          display.sleep();
        }
      }
      display.update();
    }
    // Otherwise perform a simple sleep
    else {
      try {
        Thread.sleep(waitTimeMillis);
      } catch (InterruptedException e) {
        // ignored
      }
    }
  }

  /**
   * Wait until all background tasks are complete
   */
  public static void waitForJobs() {
    while (Platform.getJobManager().currentJob() != null) {
      delay(10);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Class utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Wait until type with given name will appear in project. We use this function if we need open
   * this type in editor directly after creation.
   */
  public static IType waitForType(IJavaProject project, String fullClassName) {
    while (true) {
      // try to find type in model
      try {
        IType type = project.findType(fullClassName);
        if (type != null) {
          return type;
        }
      } catch (Throwable e) {
      }
      // delay
      delay(10);
    }
  }

  /**
   * Wait until class with given name will appear in project.<br>
   * We use this function during code generation for classes that will be used directly after
   * creation.
   */
  public static void waitForClass(AstEditor editor, String fullClassName) {
    Job.getJobManager().wakeUp(ResourcesPlugin.FAMILY_AUTO_BUILD);
    //
    IJavaProject project = editor.getJavaProject();
    ClassLoader editorLoader = EditorState.get(editor).getEditorLoader();
    while (true) {
      // try to find type in model
      IType type = null;
      try {
        type = project.findType(fullClassName);
      } catch (Throwable e) {
      }
      // try to load class
      if (type != null) {
        try {
          editorLoader.loadClass(fullClassName);
          break;
        } catch (ClassNotFoundException e) {
        } catch (Throwable e) {
          throw ReflectionUtils.propagate(e);
        }
      }
      // delay
      delay(10);
    }
  }
}
