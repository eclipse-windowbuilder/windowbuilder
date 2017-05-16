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
package org.eclipse.wb.core.model;

import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.model.AbstractWrapper;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.WrapperMethodControlCreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.description.helpers.DescriptionHelper;
import org.eclipse.wb.internal.core.model.description.helpers.EditorStateLoadingContext;
import org.eclipse.wb.internal.core.model.description.helpers.ILoadingContext;
import org.eclipse.wb.internal.core.model.generation.statement.PureFlatStatementGenerator;
import org.eclipse.wb.internal.core.model.variable.LocalUniqueVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ClassMap;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import java.util.List;

/**
 * Implementation {@link AbstractWrapper} for wrappers object accessing wrapped object by method
 * getter.
 *
 * @author sablin_aa
 * @coverage core.model
 */
public class WrapperMethodInfo extends JavaInfo implements IWrapperInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public WrapperMethodInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialize
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void initialize() throws Exception {
    super.initialize();
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void clipboardCopy(JavaInfo source, List<ClipboardCommand> commands) throws Exception {
        // add command for pasting Viewer, if Viewer created around Control
        if (source == getParent()
            && !(source.getCreationSupport() instanceof WrapperMethodControlCreationSupport)) {
          clipboardCopy_addCommands(commands);
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  //@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SE_BAD_FIELD")
  private void clipboardCopy_addCommands(List<ClipboardCommand> commands) throws Exception {
    final JavaInfoMemento wrapperMemento = JavaInfoMemento.createMemento(WrapperMethodInfo.this);
    commands.add(new ClipboardCommand() {
      private static final long serialVersionUID = 0L;

      @Override
      public void execute(JavaInfo javaInfo) throws Exception {
        WrapperMethodInfo viewer = (WrapperMethodInfo) wrapperMemento.create(javaInfo);
        JavaInfoUtils.add(
            viewer,
            new LocalUniqueVariableSupport(viewer),
            PureFlatStatementGenerator.INSTANCE,
            null,
            javaInfo,
            null);
        wrapperMemento.apply();
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_deleting = false;

  @Override
  public void delete() throws Exception {
    if (!m_deleting) {
      JavaInfo wrapped = getWrapper().getWrappedInfo();
      if (!wrapped.isDeleting()) {
        m_deleting = true;
        try {
          wrapped.delete();
        } finally {
          m_deleting = false;
        }
        return;
      }
    }
    super.delete();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IWrapperInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  private WrapperByMethod m_wrapper;

  public WrapperByMethod getWrapper() {
    if (m_wrapper == null) {
      m_wrapper = createWrapper();
    }
    return m_wrapper;
  }

  protected WrapperByMethod createWrapper() {
    return new WrapperByMethod(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Check for wrapper
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final ClassMap<Boolean> m_hasSpecialDescription = new ClassMap<Boolean>();

  /**
   * @return <code>true</code> if given class is the wrapper class.
   */
  public static boolean isWrapper(AstEditor editor, Class<?> clazz) throws Exception {
    EditorState state = EditorState.get(editor);
    ILoadingContext context = EditorStateLoadingContext.get(state);
    if (hasSpecialDescription(context, clazz)) {
      ComponentDescription description = ComponentDescriptionHelper.getDescription(editor, clazz);
      return WrapperMethodInfo.class.isAssignableFrom(description.getModelClass());
    }
    return false;
  }

  private static boolean hasSpecialDescription(ILoadingContext context, Class<?> clazz)
      throws Exception {
    // stop on Object
    if (clazz == Object.class) {
      return false;
    }
    // interfaces not allowed
    if (clazz.isInterface()) {
      return false;
    }
    // get cached or new
    Boolean result = m_hasSpecialDescription.get(clazz);
    if (result == null) {
      result = hasSpecialDescription0(context, clazz);
      m_hasSpecialDescription.put(clazz, result);
    }
    // done
    return result;
  }

  private static boolean hasSpecialDescription0(ILoadingContext context, Class<?> clazz)
      throws Exception {
    boolean hasDescription = DescriptionHelper.hasComponentDescriptionResource(context, clazz);
    if (hasDescription) {
      return true;
    } else {
      return hasSpecialDescription(context, clazz.getSuperclass());
    }
  }
}
