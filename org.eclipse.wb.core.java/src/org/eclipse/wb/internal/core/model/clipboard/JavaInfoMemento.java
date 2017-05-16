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
package org.eclipse.wb.internal.core.model.clipboard;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.IImplicitCreationSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;

/**
 * Container for copy/paste information about {@link JavaInfo}.
 *
 * <p>
 * During "copy" operation method {@link #createMemento(JavaInfo)} should be used to create instance
 * of {@link JavaInfoMemento} with all information about {@link JavaInfo}.
 *
 * <p>
 * Later, during "paste", following methods should be invoked (only one time and only in this
 * sequence):
 * <ul>
 * <li> {@link #create(JavaInfo)} to create {@link JavaInfo} that can be used to bind to the
 * {@link JavaInfo} hierarchy.</li>
 * <li> {@link #apply()}, after adding {@link JavaInfo} to the hierarchy, to apply all
 * {@link ClipboardCommand}'s and do other things for configuring created {@link JavaInfo}.</li>
 * </ul>
 *
 * @author scheglov_ke
 * @coverage core.model.clipboard
 */
public class JavaInfoMemento implements Serializable {
  private static final long serialVersionUID = 0L;
  /**
   * The key under which {@link JavaInfoMemento} registers itself in created {@link JavaInfo}.
   */
  public static final String KEY_MEMENTO = "KEY_MEMENTO";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link JavaInfoMemento} for given {@link JavaInfo}.
   */
  public static JavaInfoMemento createMemento(JavaInfo javaInfo) throws Exception {
    if (javaInfo instanceof AbstractComponentInfo) {
      return new ComponentInfoMemento((AbstractComponentInfo) javaInfo);
    }
    return new JavaInfoMemento(javaInfo);
  }

  /**
   * Checks that component can be copy/paste, i.e. {@link JavaInfoMemento} can be created, but don't
   * really create it. We use this to enable/disable copy/cut actions on selection change.
   *
   * @return <code>true</code> if {@link JavaInfoMemento} can be created for given {@link JavaInfo}.
   */
  public static boolean hasMemento(JavaInfo javaInfo) {
    try {
      IClipboardCreationSupport clipboard = javaInfo.getCreationSupport().getClipboard();
      return clipboard != null;
    } catch (Throwable e) {
      return false;
    }
  }

  /**
   * If given {@link JavaInfo} was created from some {@link JavaInfoMemento}, then applies this
   * memento.
   */
  public static void apply(JavaInfo javaInfo) throws Exception {
    JavaInfoMemento memento = (JavaInfoMemento) javaInfo.getArbitraryValue(KEY_MEMENTO);
    memento.apply();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final String m_componentClassName;
  private final IClipboardCreationSupport m_creationSupport;
  private final List<ClipboardCommand> m_commands = Lists.newArrayList();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  protected JavaInfoMemento(JavaInfo javaInfo) throws Exception {
    m_componentClassName = javaInfo.getDescription().getComponentClass().getName();
    // creation
    {
      m_creationSupport = javaInfo.getCreationSupport().getClipboard();
      Assert.isNotNull(m_creationSupport, "No clipboard CreationSupport for %s", javaInfo);
      cleanUpAnonymous(m_creationSupport);
    }
    // prepare commands
    addCommands(javaInfo, m_commands);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Copy utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds {@link ClipboardCommand}'s for restoring properties, children, etc for given
   * {@link JavaInfo}.
   *
   * @param javaInfo
   *          the {@link JavaInfo} to add commands for.
   * @param commands
   *          the container to add commands to.
   */
  static void addCommands(JavaInfo javaInfo, List<ClipboardCommand> commands) throws Exception {
    // properties command
    commands.add(new PropertiesClipboardCommand(javaInfo));
    // implicit/exposed children commands
    for (JavaInfo child : javaInfo.getChildrenJava()) {
      if (child.getCreationSupport() instanceof IImplicitCreationSupport) {
        commands.add(new ImplicitChildCommand(child));
      }
    }
    // broadcast commands
    javaInfo.getBroadcastJava().clipboardCopy(javaInfo, commands);
    // clean up anonymous commands
    for (ClipboardCommand command : commands) {
      cleanUpAnonymous(command);
    }
  }

  /**
   * Clears "this$0", "this$1", etc, because it is convenient to use clipboard anonymous classes,
   * and we sure that they don't reference enclosing classes, even if Java thinks that they do this.
   */
  static void cleanUpAnonymous(Object o) throws Exception {
    for (int i = 0; i < 10; i++) {
      Field field = ReflectionUtils.getFieldByName(o.getClass(), "this$" + i);
      if (field != null) {
        field.set(o, null);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation/paste
  //
  ////////////////////////////////////////////////////////////////////////////
  private transient JavaInfo m_javaInfo;
  private transient boolean m_applied;

  /**
   * @return the class of component represented by this {@link JavaInfoMemento}. This can be used to
   *         check component class/model in more light way than creating real {@link JavaInfo}.
   */
  public String getComponentClassName() {
    return m_componentClassName;
  }

  /**
   * Creates new {@link JavaInfo} using remembered values.
   *
   * @param existingHierarchyObject
   *          some {@link JavaInfo} in model hierarchy.
   *
   * @return the new {@link JavaInfo}.
   */
  public JavaInfo create(JavaInfo existingHierarchyObject) throws Exception {
    Assert.isLegal(!m_applied, "This memento already applied.");
    if (m_javaInfo == null) {
      JavaInfo rootObject = existingHierarchyObject.getRootJava();
      AstEditor editor = rootObject.getEditor();
      Class<?> componentClass =
          JavaInfoUtils.getClassLoader(rootObject).loadClass(m_componentClassName);
      // prepare creation
      CreationSupport creationSupport = m_creationSupport.create(rootObject);
      // create JavaInfo
      m_javaInfo = JavaInfoUtils.createJavaInfo(editor, componentClass, creationSupport);
      m_javaInfo.putArbitraryValue(KEY_MEMENTO, this);
    }
    return m_javaInfo;
  }

  /**
   * Performs configuring for this {@link JavaInfo}.
   */
  public void apply() throws Exception {
    Assert.isNotNull(m_javaInfo, "JavaInfo should be already created using create().");
    Assert.isLegal(
        m_javaInfo.getParent() != null,
        "JavaInfo should be already bounds to the hierarchy.");
    Assert.isLegal(!m_applied, "This memento already applied.");
    m_applied = true;
    // do apply
    m_javaInfo.getRootJava().refreshLight();
    m_creationSupport.apply(m_javaInfo);
    // execute commands
    executeCommands();
  }

  /**
   * Executes remembered {@link ClipboardCommand}'s for this {@link JavaInfo}.
   */
  private void executeCommands() throws Exception {
    for (ClipboardCommand command : m_commands) {
      command.execute(m_javaInfo);
    }
  }
}
