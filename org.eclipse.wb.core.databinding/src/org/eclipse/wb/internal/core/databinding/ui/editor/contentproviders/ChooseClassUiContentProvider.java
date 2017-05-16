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
package org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.databinding.Messages;
import org.eclipse.wb.internal.core.databinding.ui.UiUtils;
import org.eclipse.wb.internal.core.databinding.ui.editor.DialogFieldUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.dialogfields.ComboButtonsDialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.IDialogFieldListener;
import org.eclipse.wb.internal.core.utils.dialogfields.IStringButtonAdapter;

import org.eclipse.swt.SWT;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ClassUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

/**
 * {@link IUiContentProvider} for choose class over "browse" button or combo items.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public abstract class ChooseClassUiContentProvider extends DialogFieldUiContentProvider {
  private static final Map<String, List<String>> SCOPES = Maps.newHashMap();
  private final List<String> m_classes;
  private final ChooseClassConfiguration m_configuration;
  private final ComboButtonsDialogField m_dialogField;
  private boolean m_checkClasses = true;
  private ChooseClassRouter m_router;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ChooseClassUiContentProvider(ChooseClassConfiguration configuration) {
    m_configuration = configuration;
    // prepare value scope
    String valuesScope = m_configuration.getValuesScope();
    if (valuesScope == null) {
      m_classes = Lists.newArrayList();
    } else {
      List<String> classes = SCOPES.get(valuesScope);
      if (classes == null) {
        classes = Lists.newArrayList();
        SCOPES.put(valuesScope, classes);
      }
      m_classes = classes;
    }
    // create dialog field
    m_dialogField =
        new ComboButtonsDialogField(m_browseAdapter,
            m_clearAdapter,
            SWT.BORDER | SWT.READ_ONLY,
            m_configuration.isUseClearButton());
    // configure dialog field
    String dialogFieldLabel = m_configuration.getDialogFieldLabel();
    if (dialogFieldLabel != null) {
      m_dialogField.setLabelText(dialogFieldLabel);
    }
    //
    m_dialogField.setEnabled(m_configuration.isDialogFieldEnabled());
    // prepare combo items
    if (m_configuration.isUseClearButton()) {
      addClassToCombo(m_configuration.getClearValue());
    }
    //
    String[] defaultValues = m_configuration.getDefaultValues();
    if (!ArrayUtils.isEmpty(defaultValues)) {
      for (int i = 0; i < defaultValues.length; i++) {
        addClassToCombo(defaultValues[i]);
      }
    }
    //
    m_dialogField.addItems(m_classes);
    m_dialogField.setDialogFieldListener(m_fieldChangeListener);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  final void setRouter(ChooseClassRouter router) {
    m_router = router;
  }

  public final ChooseClassConfiguration getConfiguration() {
    return m_configuration;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractUIContentProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public DialogField getDialogField() {
    return m_dialogField;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handling
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Change selection item on combo.
   */
  private final IDialogFieldListener m_fieldChangeListener = new IDialogFieldListener() {
    public void dialogFieldChanged(DialogField field) {
      calculateFinish();
    }
  };
  /**
   * Press "browse" button.
   */
  private final IStringButtonAdapter m_browseAdapter = new IStringButtonAdapter() {
    public void changeControlPressed(DialogField field) {
      handleChooseBrowse();
    }
  };
  /**
   * Press "clear" button.
   */
  private final IStringButtonAdapter m_clearAdapter = new IStringButtonAdapter() {
    public void changeControlPressed(DialogField field) {
      m_dialogField.selectItem(m_configuration.getClearValue());
    }
  };

  private void handleChooseBrowse() {
    try {
      String className =
          UiUtils.chooseType(
              getShell(),
              getJavaProject(),
              m_configuration.getBaseClassNames(),
              m_configuration.getOpenTypeStyle());
      if (className != null) {
        setClassName(className);
      }
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
  }

  /**
   * This method calculate state and sets or clear error message. Subclasses maybe override this
   * method for observe special states.
   */
  protected void calculateFinish() {
    String className = getClassName();
    // route events
    if (m_router != null) {
      m_router.handle();
    }
    // check state
    if (className.length() == 0) {
      // empty class
      setErrorMessage(m_configuration.getEmptyClassErrorMessage());
    } else {
      // check clear of default value (maybe not class)
      if (m_configuration.isDefaultString(className)
          || className.equals(m_configuration.getClearValue())
          || ArrayUtils.indexOf(m_configuration.getDefaultValues(), className) != -1) {
        setErrorMessage(null);
        return;
      }
      // check load class
      Class<?>[][] constructorsParameters = m_configuration.getConstructorsParameters();
      String errorMessagePrefix = m_configuration.getErrorMessagePrefix();
      //
      boolean noConstructor = false;
      try {
        Class<?> testClass = loadClass(className);
        // check permissions
        int modifiers = testClass.getModifiers();
        if (!Modifier.isPublic(modifiers)) {
          setErrorMessage(errorMessagePrefix
              + Messages.ChooseClassUiContentProvider_validateNotPublic);
          return;
        }
        if (!m_configuration.isChooseInterfaces() && Modifier.isAbstract(modifiers)) {
          setErrorMessage(errorMessagePrefix
              + Messages.ChooseClassUiContentProvider_validateAbstract);
          return;
        }
        // check constructor
        if (m_checkClasses) {
          m_checkClasses = false;
          if (constructorsParameters != null) {
            for (int i = 0; i < constructorsParameters.length; i++) {
              Class<?>[] constructorParameters = constructorsParameters[i];
              for (int j = 0; j < constructorParameters.length; j++) {
                Class<?> constructorParameterClass = constructorParameters[j];
                if (constructorParameterClass.isArray()) {
                  String parameterClassName =
                      constructorParameterClass.getComponentType().getName();
                  if (parameterClassName.startsWith("org.eclipse")) {
                    constructorParameters[j] =
                        Array.newInstance(loadClass(parameterClassName), new int[1]).getClass();
                  }
                } else {
                  String parameterClassName = constructorParameterClass.getName();
                  if (parameterClassName.startsWith("org.eclipse")) {
                    constructorParameters[j] = loadClass(parameterClassName);
                  }
                }
              }
            }
          }
        }
        if (constructorsParameters != null) {
          noConstructor = true;
          for (int i = 0; i < constructorsParameters.length; i++) {
            try {
              testClass.getConstructor(constructorsParameters[i]);
              noConstructor = false;
              break;
            } catch (SecurityException e) {
            } catch (NoSuchMethodException e) {
            }
          }
        }
      } catch (ClassNotFoundException e) {
        setErrorMessage(errorMessagePrefix + Messages.ChooseClassUiContentProvider_validateNotExist);
        return;
      }
      // prepare error message for constructor
      if (noConstructor) {
        StringBuffer parameters = new StringBuffer(errorMessagePrefix);
        parameters.append(Messages.ChooseClassUiContentProvider_validatePublicConstructor);
        for (int i = 0; i < constructorsParameters.length; i++) {
          Class<?>[] constructorParameters = constructorsParameters[i];
          if (i > 0) {
            parameters.append(" ");
          }
          parameters.append(ClassUtils.getShortClassName(className));
          parameters.append("(");
          for (int j = 0; j < constructorParameters.length; j++) {
            if (j > 0) {
              parameters.append(", ");
            }
            parameters.append(ClassUtils.getShortClassName(constructorParameters[j]));
          }
          parameters.append(")");
        }
        parameters.append(".");
        setErrorMessage(parameters.toString());
      } else {
        setErrorMessage(null);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Value
  //
  ////////////////////////////////////////////////////////////////////////////
  public void addClassToCombo(String className) {
    if (m_classes.indexOf(className) == -1) {
      m_dialogField.addItem(className);
    }
  }

  /**
   * Helper method that load and returned current chosen class.
   */
  public Class<?> getChoosenClass() throws Exception {
    return loadClass(getClassName());
  }

  /**
   * @return the current chosen class name.
   */
  public final String getClassName() {
    return m_dialogField.getText();
  }

  /**
   * Clear current chosen class name and sets to combo empty item.
   */
  protected final void setClearClassName() {
    String className = "";
    int index = m_dialogField.getListItems().indexOf(className);
    if (index == -1) {
      m_dialogField.setDialogFieldListener(null);
      m_dialogField.addItem(className);
      m_dialogField.setDialogFieldListener(m_fieldChangeListener);
      m_dialogField.selectItem(className);
    } else {
      m_dialogField.selectItem(index);
    }
  }

  /**
   * Sets current chosen class name.
   */
  public final void setClassName(String className) {
    // prepare class name
    className = m_configuration.getRetargetClassName(className);
    // check clear or default value (don't add to classes scope)
    if (m_configuration.isDefaultString(className)) {
      int index = m_dialogField.getListItems().indexOf(className);
      if (index == -1) {
        m_dialogField.setDialogFieldListener(null);
        m_dialogField.addItem(className);
        m_dialogField.setDialogFieldListener(m_fieldChangeListener);
        m_dialogField.selectItem(className);
      } else {
        m_dialogField.selectItem(index);
      }
      return;
    }
    if (className.equals(m_configuration.getClearValue())
        || ArrayUtils.indexOf(m_configuration.getDefaultValues(), className) != -1) {
      m_dialogField.selectItem(className);
      return;
    }
    // select or add class name to combo and classes scope
    int index = m_dialogField.getListItems().indexOf(className);
    if (index == -1) {
      m_classes.add(className);
      m_dialogField.setDialogFieldListener(null);
      m_dialogField.addItem(className);
      m_dialogField.setDialogFieldListener(m_fieldChangeListener);
      m_dialogField.selectItem(className);
    } else {
      m_dialogField.selectItem(index);
    }
  }
}