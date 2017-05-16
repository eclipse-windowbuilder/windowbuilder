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
package org.eclipse.wb.internal.core.model.property.editor;

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.ModelMessages;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.IConfigurablePropertyObject;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.presentation.ButtonPropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.editor.presentation.PropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ReusableDialog;

import org.eclipse.jface.window.Window;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.Date;
import java.util.Map;

/**
 * The {@link PropertyEditor} for {@link Date}. Using scripts for MVEL library.
 *
 * @author sablin_aa
 * @coverage core.model.property.editor
 */
public final class DatePropertyEditor extends AbstractTextPropertyEditor
    implements
      IValueSourcePropertyEditor,
      IConfigurablePropertyObject {
  private EditorState m_state;
  private String m_toStringScript;
  private String m_toDateScript;
  private String m_functions;
  private String m_sourceTemplate;
  private boolean m_isLongValue;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public PropertyEditorPresentation getPresentation() {
    return PRESENTATION;
  }

  private final PropertyEditorPresentation PRESENTATION = new ButtonPropertyEditorPresentation() {
    @Override
    protected Image getImage() {
      return DesignerPlugin.getImage("properties/dt.png");
    }

    @Override
    protected void onClick(PropertyTable propertyTable, Property property) throws Exception {
      // create dialog
      DateDialog dialog = new DateDialog(DesignerPlugin.getShell());
      if (property.getValue() != null && property.getValue() != Property.UNKNOWN_VALUE) {
        if (m_isLongValue) {
          dialog.setValue((Long) property.getValue());
        } else {
          dialog.setValue((Date) property.getValue());
        }
      }
      // open dialog
      if (dialog.open() == Window.OK) {
        property.setValue(dialog.getValue());
      }
    }
  };

  @Override
  public String getText(Property property) throws Exception {
    Object value = property.getValue();
    if (value instanceof Long) {
      value = new Date(((Long) value).longValue());
    }
    if (value instanceof Date) {
      Map<String, Object> variables = Maps.newTreeMap();
      if (property instanceof GenericProperty) {
        GenericProperty genericProperty = (GenericProperty) property;
        variables.put("control", genericProperty.getJavaInfo().getObject());
      }
      variables.put("value", value);
      return (String) evaluate(m_toStringScript, variables);
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getEditorText(Property property) throws Exception {
    return getText(property);
  }

  @Override
  protected boolean setEditorText(Property property, String text) throws Exception {
    String valueText = text.trim();
    Object value;
    // check for delete
    if (valueText.length() == 0) {
      value = Property.UNKNOWN_VALUE;
    } else {
      // prepare value
      try {
        Map<String, Object> variables = Maps.newTreeMap();
        variables.put("value", valueText);
        if (property instanceof GenericProperty) {
          GenericProperty genericProperty = (GenericProperty) property;
          variables.put("control", genericProperty.getJavaInfo().getObject());
        }
        value = evaluate(m_toDateScript, variables);
      } catch (Throwable e) {
        UiUtils.openWarning(
            DesignerPlugin.getShell(),
            property.getTitle(),
            MessageFormat.format(ModelMessages.DatePropertyEditor_notValidDate, valueText));
        return false;
      }
    }
    // modify property
    property.setValue(value);
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IValueSourcePropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getValueSource(Object value) throws Exception {
    if (value instanceof Date) {
      String source;
      Long millisecs = ReflectionUtils.getFieldLong(value, "fastTime");
      if (StringUtils.isEmpty(m_sourceTemplate)) {
        source = "new java.util.Date(%millisecs%)";
      } else {
        Map<String, Object> variables = Maps.newTreeMap();
        variables.put("value", value);
        String valueText = (String) evaluate(m_toStringScript, variables);
        source = StringUtils.replace(m_sourceTemplate, "%value%", valueText);
      }
      return StringUtils.replace(source, "%millisecs%", millisecs.toString() + "L");
    }
    return value == null ? "(java.util.Date) null" : value.toString();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IConfigurablePropertyObject
  //
  ////////////////////////////////////////////////////////////////////////////
  public void configure(EditorState state, Map<String, Object> parameters) throws Exception {
    m_state = state;
    // functions
    {
      final String FUNCTIONS_PARAM = "functions";
      if (parameters.containsKey(FUNCTIONS_PARAM)) {
        m_functions = (String) parameters.get(FUNCTIONS_PARAM);
      } else {
        m_functions = "";
      }
    }
    // to-string script
    {
      final String TO_STRING_PARAM = "toString";
      String toStringScript = "";
      if (parameters.containsKey(TO_STRING_PARAM)) {
        toStringScript = (String) parameters.get(TO_STRING_PARAM);
      }
      if (StringUtils.isEmpty(toStringScript)) {
        toStringScript =
            StringUtils.join(new String[]{
                "import java.text.SimpleDateFormat;",
                m_functions,
                "(new SimpleDateFormat()).format(value)"});
      } else {
        toStringScript = m_functions + "\n" + toStringScript;
      }
      m_toStringScript = toStringScript;
    }
    // to-date script
    {
      final String TO_DATE_PARAM = "toDate";
      String toDateScript = "";
      if (parameters.containsKey(TO_DATE_PARAM)) {
        toDateScript = (String) parameters.get(TO_DATE_PARAM);
      }
      if (StringUtils.isEmpty(toDateScript)) {
        toDateScript =
            StringUtils.join(new String[]{
                "import java.text.SimpleDateFormat;",
                m_functions,
                "(new java.text.SimpleDateFormat()).parse(value)"});
      } else {
        toDateScript = m_functions + "\n" + toDateScript;
      }
      m_toDateScript = toDateScript;
    }
    // source template
    {
      final String SOURCE_PARAM = "source";
      if (parameters.containsKey(SOURCE_PARAM)) {
        m_sourceTemplate = (String) parameters.get(SOURCE_PARAM);
      } else {
        m_sourceTemplate = "";
      }
    }
    // isLongValue switch
    {
      final String IS_LONG_VALUE_PARAM = "isLongValue";
      if (parameters.containsKey(IS_LONG_VALUE_PARAM)) {
        m_isLongValue = ((String) parameters.get(IS_LONG_VALUE_PARAM)).equals("true");
      } else {
        m_isLongValue = false;
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private Object evaluate(String script, Map<String, Object> variables) throws Exception {
    if (m_state == null) {
      return ScriptUtils.evaluate(script, variables);
    } else {
      return ScriptUtils.evaluate(m_state.getEditorLoader(), script, variables);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dialog
  //
  ////////////////////////////////////////////////////////////////////////////
  public static class DateDialog extends ReusableDialog {
    private Date m_value;
    private boolean m_changed;
    private CDateTime m_control;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public DateDialog(Shell parentShell) {
      super(parentShell);
    }

    @Override
    protected void onBeforeOpen() {
      super.onBeforeOpen();
      m_changed = false;
    }

    public void setValue(Date value) {
      m_value = value;
    }

    public void setValue(Long value) {
      m_value = new Date(value);
    }

    public Date getValue() {
      if (isChanged()) {
        m_value = m_control.getSelection();
      }
      return m_value;
    }

    public boolean isChanged() {
      return m_changed;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Contents
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText(ModelMessages.DatePropertyEditor_dialogTitle);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
      Composite area = (Composite) super.createDialogArea(parent);
      area.setLayout(new FillLayout());
      // create date&time control
      m_control = new CDateTime(area, CDT.SIMPLE | CDT.DATE_MEDIUM | CDT.TIME_MEDIUM);
      m_control.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          m_changed = true;
        }
      });
      m_control.setSelection(m_value);
      //
      return area;
    }
  }
}