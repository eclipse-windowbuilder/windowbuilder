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

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.ModelMessages;
import org.eclipse.wb.internal.core.model.variable.AbstractNamedVariableSupport;
import org.eclipse.wb.internal.core.model.variable.FieldReuseVariableSupport;
import org.eclipse.wb.internal.core.model.variable.FieldUniqueVariableSupport;
import org.eclipse.wb.internal.core.model.variable.FieldVariableSupport;
import org.eclipse.wb.internal.core.model.variable.LocalReuseVariableSupport;
import org.eclipse.wb.internal.core.model.variable.LocalUniqueVariableSupport;
import org.eclipse.wb.internal.core.model.variable.LocalVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.dialogfields.AbstractValidationTitleAreaDialog;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import java.util.List;
import java.util.Map;

/**
 * Helper for rename/convert multiple {@link JavaInfo}'s as single operation.
 *
 * @author scheglov_ke
 * @coverage core.model.util
 */
public final class RenameConvertSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Contribution
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * If possible, contributes "rename" action.
   *
   * @param objects
   *          The components that should be renamed/converted.
   * @param manager
   *          The {@link IContributionManager} to add action to.
   */
  public static void contribute(List<? extends ObjectInfo> objects, IContributionManager manager) {
    // ensure objects (during tests we sometimes send "null")
    if (objects == null) {
      return;
    }
    // prepare components
    Iterable<JavaInfo> components = Iterables.filter(objects, JavaInfo.class);
    RenameConvertSupport support = new RenameConvertSupport(components);
    // add action
    if (components.iterator().hasNext()) {
      Action action = support.new RenameAction();
      manager.appendToGroup(IContextMenuConstants.GROUP_INHERITANCE, action);
    }
  }

  /**
   * Initiates the rename/convert operation for <code>objects</code> by bringing up rename dialog.
   *
   * @param objects
   *          The components that should be renamed/converted.
   */
  public static void rename(List<? extends ObjectInfo> objects) {
    Iterable<JavaInfo> components = Iterables.filter(objects, JavaInfo.class);
    RenameConvertSupport support = new RenameConvertSupport(components);
    support.doRename();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Iterable<? extends JavaInfo> m_components;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private RenameConvertSupport(Iterable<? extends JavaInfo> components) {
    m_components = components;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Action
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link Action} for rename/convert components.
   */
  private class RenameAction extends Action {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public RenameAction() {
      setImageDescriptor(DesignerPlugin.getImageDescriptor("actions/rename/rename_convert.png"));
      setText(ModelMessages.RenameConvertSupport_text);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Object - make "singleton"
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public int hashCode() {
      return 0;
    }

    @Override
    public boolean equals(Object obj) {
      return obj instanceof RenameAction;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Run
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void run() {
      doRename();
    }
  }

  /**
   * Opens rename/convert dialog and perform one or more operations if user pressed OK.
   */
  private void doRename() {
    RenameDialog dialog = new RenameDialog();
    if (dialog.open() == Window.OK) {
      executeCommands();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Map<JavaInfo, RenameCommand> m_commands = Maps.newLinkedHashMap();

  /**
   * Executes pending {@link RenameCommand}'s.
   */
  private void executeCommands() {
    if (!m_commands.isEmpty()) {
      JavaInfo anyComponent = m_components.iterator().next();
      ExecutionUtils.run(anyComponent, new RunnableEx() {
        public void run() throws Exception {
          for (RenameCommand command : m_commands.values()) {
            command.execute();
          }
        }
      });
    }
  }

  /**
   * Validates that all pending {@link RenameCommand}'s are valid.<br>
   * In particular we verify that variable names are valid and unique.
   *
   * @return the error message or <code>null</code>.
   */
  private String validateCommands() {
    // check that variable names are valid
    for (RenameCommand command : m_commands.values()) {
      String name = command.m_name;
      if (name != null) {
        IStatus status = JavaConventions.validateIdentifier(name);
        if (status.matches(IStatus.ERROR)) {
          return status.getMessage();
        }
      }
    }
    // check for unique names
    {
      // we intentionally don't check for names uniqueness, because:
      // 1. hard to implement;
      // 2. hard for user, if we force him generate unique names by hands.
    }
    // OK
    return null;
  }

  /**
   * @return the {@link RenameCommand} associated with given {@link JavaInfo}.
   */
  private RenameCommand getCommand(JavaInfo javaInfo) {
    RenameCommand command = m_commands.get(javaInfo);
    if (command == null) {
      command = new RenameCommand(javaInfo);
      m_commands.put(javaInfo, command);
    }
    return command;
  }

  /**
   * Container for remembering pending operation.
   */
  private static final class RenameCommand {
    private final JavaInfo m_javaInfo;
    private String m_name;
    private boolean m_toLocal;
    private boolean m_toField;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public RenameCommand(JavaInfo javaInfo) {
      m_javaInfo = javaInfo;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Sets the new name for {@link JavaInfo} variable.
     */
    public void setName(String name) {
      m_name = name;
    }

    /**
     * Specifies that {@link JavaInfo} variable should be converted to local.
     */
    public void toLocal() {
      m_toLocal = true;
      m_toField = false;
    }

    /**
     * Specifies that {@link JavaInfo} variable should be converted to field.
     */
    public void toField() {
      m_toLocal = false;
      m_toField = true;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Execution
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Executes operation remembered in this command.
     */
    public void execute() throws Exception {
      // to local/field
      {
        VariableSupport variable = m_javaInfo.getVariableSupport();
        if (m_toLocal && variable.canConvertFieldToLocal()) {
          variable.convertFieldToLocal();
        } else if (m_toField && variable.canConvertLocalToField()) {
          variable.convertLocalToField();
        }
      }
      // name
      {
        VariableSupport variable = m_javaInfo.getVariableSupport();
        if (m_name != null && !variable.getName().equals(m_name)) {
          ((AbstractNamedVariableSupport) variable).setNameBase(m_name);
        }
      }
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Rename dialog
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Dialog for requesting new name/local/field for components.
   */
  private class RenameDialog extends AbstractValidationTitleAreaDialog {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public RenameDialog() {
      super(DesignerPlugin.getShell(),
          DesignerPlugin.getDefault(),
          ModelMessages.RenameConvertSupport_shellTitle,
          ModelMessages.RenameConvertSupport_title,
          DesignerPlugin.getImage("actions/rename/rename_banner.gif"),
          ModelMessages.RenameConvertSupport_message);
      setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // GUI
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected void createControls(Composite container) {
      GridLayoutFactory.create(container).columns(4);
      // for each component
      for (JavaInfo javaInfo : m_components) {
        VariableSupport variableSupport = javaInfo.getVariableSupport();
        // we need name
        if (!variableSupport.hasName()) {
          continue;
        }
        // process single component
        createControls(container, javaInfo, variableSupport);
      }
      // set focus on first Text
      for (Control control : container.getChildren()) {
        if (control instanceof Text) {
          control.setFocus();
          break;
        }
      }
    }

    /**
     * Creates {@link Control}'s for single {@link JavaInfo}.
     */
    private void createControls(Composite container,
        final JavaInfo javaInfo,
        VariableSupport variableSupport) {
      // presentation
      {
        // icon
        {
          Label iconLabel = new Label(container, SWT.NONE);
          Image icon = ObjectsLabelProvider.INSTANCE.getImage(javaInfo);
          iconLabel.setImage(icon);
        }
        // text
        {
          Label textLabel = new Label(container, SWT.NONE);
          String text = ObjectsLabelProvider.INSTANCE.getText(javaInfo);
          textLabel.setText(text);
        }
      }
      // name
      {
        final Text nameText = new Text(container, SWT.BORDER);
        GridDataFactory.create(nameText).hintHC(30).grabH().fillH();
        nameText.setText(variableSupport.getName());
        // listener
        nameText.addListener(SWT.Modify, new Listener() {
          public void handleEvent(Event event) {
            getCommand(javaInfo).setName(nameText.getText());
            validateAll();
          }
        });
      }
      // local/field
      {
        ToolBar toolBar = new ToolBar(container, SWT.FLAT);
        boolean isNormalVariable =
            variableSupport instanceof LocalUniqueVariableSupport
                || variableSupport instanceof LocalReuseVariableSupport
                || variableSupport instanceof FieldUniqueVariableSupport
                || variableSupport instanceof FieldReuseVariableSupport;
        {
          ToolItem toolItem = new ToolItem(toolBar, SWT.RADIO);
          toolItem.setImage(DesignerPlugin.getImage("actions/rename/be_local.png"));
          toolItem.setToolTipText(ModelMessages.RenameConvertSupport_toLocal);
          toolItem.setEnabled(isNormalVariable);
          toolItem.setSelection(variableSupport instanceof LocalVariableSupport);
          // listener
          toolItem.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
              getCommand(javaInfo).toLocal();
            }
          });
        }
        {
          ToolItem toolItem = new ToolItem(toolBar, SWT.RADIO);
          toolItem.setImage(DesignerPlugin.getImage("actions/rename/be_field.png"));
          toolItem.setToolTipText(ModelMessages.RenameConvertSupport_beField);
          toolItem.setEnabled(isNormalVariable);
          toolItem.setSelection(variableSupport instanceof FieldVariableSupport);
          // listener
          toolItem.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
              getCommand(javaInfo).toField();
            }
          });
        }
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Validation
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected String validate() {
      return validateCommands();
    }
  }
}
