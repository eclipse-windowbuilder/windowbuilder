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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.eclipse.wb.core.controls.CCombo3;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.ModelMessages;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.IConfigurablePropertyObject;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.complex.IComplexPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.presentation.ButtonPropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.editor.presentation.PropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.jdt.ui.JdtUiUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ResizableDialog;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The {@link PropertyEditor} for selecting single constant (<code>public static final</code> field
 * in class, or field of interface).
 *
 * @author scheglov_ke
 * @coverage core.model.property.editor
 */
public final class ConstantSelectionPropertyEditor extends AbstractComboPropertyEditor
    implements
      IConfigurablePropertyObject {
  private String m_typeName;
  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  private final PropertyEditorPresentation m_presentation = new ButtonPropertyEditorPresentation() {
    @Override
    protected void onClick(PropertyTable propertyTable, Property property) throws Exception {
      openDialog(property);
    }
  };

  @Override
  public final PropertyEditorPresentation getPresentation() {
    return m_presentation;
  }

  @Override
  protected String getText(Property _property) throws Exception {
    GenericProperty property = (GenericProperty) _property;
    IField field = getField(property);
    if (field != null) {
      return field.getElementName();
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractComboPropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addItems(Property _property, CCombo3 combo) throws Exception {
    GenericProperty property = (GenericProperty) _property;
    IType type = getType(property);
    List<IField> fields = getFields(type);
    for (int index = 0; index < fields.size(); index++) {
      IField field = fields.get(index);
      combo.add(field.getElementName());
      combo.setData("" + index, field);
    }
  }

  @Override
  protected void selectItem(Property _property, CCombo3 combo) throws Exception {
    GenericProperty property = (GenericProperty) _property;
    combo.select(-1);
    // try to find current field
    IField field = getField(property);
    for (int i = 0; i < combo.getItemCount(); i++) {
      if (combo.getData("" + i) == field) {
        combo.select(i);
        break;
      }
    }
  }

  @Override
  protected void toPropertyEx(Property _property, CCombo3 combo, int index) throws Exception {
    GenericProperty property = (GenericProperty) _property;
    IField field = (IField) combo.getData("" + index);
    setField(property, field);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean activate(PropertyTable propertyTable, Property property, Point location)
      throws Exception {
    // activate using keyboard
    if (location == null) {
      openDialog(property);
      return false;
    }
    // activate as Combo
    return super.activate(propertyTable, property, location);
  }

  /**
   * Opens editing dialog.
   */
  private void openDialog(Property _property) throws Exception {
    GenericProperty property = (GenericProperty) _property;
    ConstantSelection_Dialog dialog =
        new ConstantSelection_Dialog(DesignerPlugin.getShell(),
            property.getJavaInfo(),
            getField(property));
    if (dialog.open() == Window.OK) {
      setField(property, dialog.m_selectedField);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IConfigurablePropertyObject
  //
  ////////////////////////////////////////////////////////////////////////////
  public void configure(EditorState state, Map<String, Object> parameters) throws Exception {
    m_typeName = (String) parameters.get("type");
    Assert.isNotNull(m_typeName, "'type' attribute in %s.", parameters);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Opens dialog to select {@link IField}. XXX
   *
   * @return the selected {@link IField}, or <code>null</code> if selection was cancelled.
   */
  public static IField selectField(Shell parentShell,
      String typeName,
      JavaInfo javaInfo,
      Set<IType> additionalTypes,
      IField currentField) throws Exception {
    ConstantSelectionPropertyEditor editor = new ConstantSelectionPropertyEditor();
    editor.m_typeName = typeName;
    ConstantSelection_Dialog dialog =
        editor.new ConstantSelection_Dialog(parentShell, javaInfo, currentField);
    dialog.m_additionalTypes.addAll(additionalTypes);
    if (dialog.open() == Window.OK) {
      return dialog.m_selectedField;
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utilities
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link IField}'s for constants of required type.
   */
  public List<IField> getFields(IType type) throws Exception {
    List<IField> fields = Lists.newArrayList();
    if (type != null) {
      for (IField field : type.getFields()) {
        // check that field is "public static final"
        {
          int flags = field.getFlags();
          boolean isConstant =
              Flags.isPublic(flags) && Flags.isStatic(flags) && Flags.isFinal(flags);
          if (type.isClass() && !isConstant) {
            continue;
          }
        }
        // check for valid type
        {
          String typeName = CodeUtils.getResolvedTypeName(type, field.getTypeSignature());
          if (!typeName.equals(m_typeName)) {
            continue;
          }
        }
        // OK, we can add this field
        fields.add(field);
      }
    }
    // sort fields by name
    Collections.sort(fields, new Comparator<IField>() {
      public int compare(IField field1, IField field2) {
        return field1.getElementName().compareTo(field2.getElementName());
      }
    });
    // OK, final result
    return fields;
  }

  /**
   * @return the {@link IType} for {@link IField} returned by {@link #getField(GenericProperty)},
   *         i.e. current {@link IType} to get constants from.
   */
  private static IType getType(GenericProperty property) throws Exception {
    IField field = getField(property);
    return field != null ? field.getDeclaringType() : null;
  }

  /**
   * @return the {@link IField} that is represented by {@link Expression} of given
   *         {@link GenericProperty}, may be <code>null</code> if not a field is used as
   *         {@link Expression}.
   */
  private static IField getField(GenericProperty property) throws Exception {
    IJavaProject javaProject = property.getJavaInfo().getEditor().getJavaProject();
    Expression expression = property.getExpression();
    return getField(javaProject, expression);
  }

  /**
   * @return the {@link IField} that is represented by given {@link Expression}, may be
   *         <code>null</code> if not a field is used as {@link Expression}.
   */
  public static IField getField(IJavaProject javaProject, Expression expression) throws Exception {
    // QualifiedName: contains all required information
    if (expression instanceof QualifiedName) {
      QualifiedName qualifiedName = (QualifiedName) expression;
      // prepare "typeName"
      Name qualifier = qualifiedName.getQualifier();
      String typeName = AstNodeUtils.getFullyQualifiedName(qualifier, true);
      // find field
      String fieldName = qualifiedName.getName().getIdentifier();
      return CodeUtils.findField(javaProject, typeName, fieldName);
    }
    // SimpleName: may be name of field
    if (expression instanceof SimpleName) {
      SimpleName simpleName = (SimpleName) expression;
      IVariableBinding binding = AstNodeUtils.getVariableBinding(simpleName);
      if (binding != null) {
        ITypeBinding declaringClass = binding.getDeclaringClass();
        if (declaringClass != null) {
          String typeName = AstNodeUtils.getFullyQualifiedName(declaringClass, false);
          String fieldName = simpleName.getIdentifier();
          return CodeUtils.findField(javaProject, typeName, fieldName);
        }
      }
    }
    // unknown
    return null;
  }

  /**
   * Sets new {@link IField} value for {@link GenericProperty}.
   */
  private static void setField(GenericProperty property, IField field) throws Exception {
    String code = getFieldCode(property.getJavaInfo(), field);
    property.setExpression(code, Property.UNKNOWN_VALUE);
  }

  /**
   * @param javaInfo
   *          the {@link JavaInfo} to specify context.
   * @param field
   *          the {@link IField} to get code for.
   *
   * @return the code that should be used to specify reference of given {@link IField}.
   */
  public static String getFieldCode(JavaInfo javaInfo, IField field) throws Exception {
    IType fieldType = field.getDeclaringType();
    String fieldName = field.getElementName();
    if (JavaInfoUtils.isLocalField(javaInfo, field)) {
      return fieldName;
    } else {
      String typeName = fieldType.getFullyQualifiedName();
      return typeName + "." + fieldName;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Referenced IType's with constants
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link IType}'s that have constants of valid type and are used by some
   *         {@link ConstantSelectionPropertyEditor} in current {@link CompilationUnit}.
   */
  private Set<IType> getUsedTypes(JavaInfo javaInfo) throws Exception {
    Set<IType> types = Sets.newHashSet();
    collectUsedTypes(types, javaInfo.getRootJava());
    return types;
  }

  /**
   * Adds {@link IType}'s with constants of valid type, using {@link Property}'s og given
   * {@link JavaInfo} and its children.
   */
  private void collectUsedTypes(Set<IType> types, JavaInfo javaInfo) throws Exception {
    for (Property property : javaInfo.getProperties()) {
      addUsedTypes(types, property);
    }
    // collect types for children
    for (JavaInfo child : javaInfo.getChildrenJava()) {
      collectUsedTypes(types, child);
    }
  }

  /**
   * If given {@link Property} has {@link ConstantSelectionPropertyEditor} with same constant type,
   * try to add used {@link IType}.
   */
  private void addUsedTypes(Set<IType> types, Property property) throws Exception {
    if (property.getEditor() instanceof ConstantSelectionPropertyEditor) {
      ConstantSelectionPropertyEditor propertyEditor =
          (ConstantSelectionPropertyEditor) property.getEditor();
      if (propertyEditor.m_typeName.equals(m_typeName)) {
        IType type = ConstantSelectionPropertyEditor.getType((GenericProperty) property);
        if (type != null) {
          types.add(type);
        }
      }
    }
    // check for sub-properties
    PropertyEditor propertyEditor = property.getEditor();
    if (propertyEditor instanceof IComplexPropertyEditor) {
      Property[] properties = ((IComplexPropertyEditor) propertyEditor).getProperties(property);
      for (Property subProperty : properties) {
        addUsedTypes(types, subProperty);
      }
    }
  }

  /**
   * @return the {@link IType}'s for top-level {@link TypeDeclaration} and implemented interfaces,
   *         with constants of valid type.
   */
  private List<IType> getLocalTypes(JavaInfo javaInfo) throws Exception {
    List<IType> types = Lists.newArrayList();
    IJavaProject javaProject = javaInfo.getEditor().getJavaProject();
    TypeDeclaration typeDeclaration = JavaInfoUtils.getTypeDeclaration(javaInfo);
    // add type itself
    {
      String typeName = AstNodeUtils.getFullyQualifiedName(typeDeclaration, false);
      addLocalTypes(javaProject, types, typeName);
    }
    // add interfaces
    for (Type interfaceType : DomGenerics.superInterfaces(typeDeclaration)) {
      String interfaceQualifiedName = AstNodeUtils.getFullyQualifiedName(interfaceType, false);
      addLocalTypes(javaProject, types, interfaceQualifiedName);
    }
    // OK, we prepared local types
    return types;
  }

  /**
   * If {@link IType} with given name has valid constants, adds it into {@link List}.
   */
  private void addLocalTypes(IJavaProject javaProject, List<IType> types, String typeName)
      throws Exception {
    IType type = javaProject.findType(typeName);
    if (type != null && !getFields(type).isEmpty()) {
      types.add(type);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ConstantSelection_Dialog
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * The {@link ResizableDialog} for selecting constant.
   */
  private class ConstantSelection_Dialog extends ResizableDialog {
    private static final int ADD_TYPE_ID = IDialogConstants.CLIENT_ID + 1;
    private final JavaInfo m_javaInfo;
    private final IField m_currentField;
    private final IType m_currentType;
    private final Set<IType> m_additionalTypes = Sets.newHashSet();
    private TableViewer m_typesViewer;
    private Text m_filterText;
    private TableViewer m_fieldsViewer;
    private IField m_selectedField;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public ConstantSelection_Dialog(Shell parentShell, JavaInfo javaInfo, IField currentField) {
      super(parentShell, DesignerPlugin.getDefault());
      m_javaInfo = javaInfo;
      m_currentField = currentField;
      m_currentType = m_currentField != null ? m_currentField.getDeclaringType() : null;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // GUI
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected Control createDialogArea(Composite parent) {
      Composite container = (Composite) super.createDialogArea(parent);
      GridLayoutFactory.create(container).columns(2);
      {
        Group group = new Group(container, SWT.NONE);
        GridDataFactory.create(group).grab().fill();
        GridLayoutFactory.create(group);
        group.setText(ModelMessages.ConstantSelectionPropertyEditor_typesGroup);
        {
          m_typesViewer = new TableViewer(group, SWT.BORDER | SWT.FULL_SELECTION);
          {
            Table table = m_typesViewer.getTable();
            GridDataFactory.create(table).hintC(50, 15).grab().fill();
          }
          // set providers
          m_typesViewer.setLabelProvider(new JavaElementLabelProvider());
          m_typesViewer.setContentProvider(new TypeContentProvider());
          m_typesViewer.setInput(new Object());
          // set listeners
          m_typesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent e) {
              onTypeSelected();
            }
          });
        }
      }
      {
        Group group = new Group(container, SWT.NONE);
        GridDataFactory.create(group).grab().fill();
        GridLayoutFactory.create(group).columns(2);
        group.setText(ModelMessages.ConstantSelectionPropertyEditor_fieldsGroup);
        {
          Label label = new Label(group, SWT.NONE);
          label.setText(ModelMessages.ConstantSelectionPropertyEditor_filterLabel);
        }
        {
          m_filterText = new Text(group, SWT.BORDER);
          GridDataFactory.create(m_filterText).grabH().fillH();
          m_filterText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
              onFilterModify();
            }
          });
        }
        {
          m_fieldsViewer = new TableViewer(group, SWT.BORDER | SWT.FULL_SELECTION);
          {
            Table table = m_fieldsViewer.getTable();
            GridDataFactory.create(table).spanH(2).hintC(50, 15).grab().fill();
          }
          // set providers
          m_fieldsViewer.setLabelProvider(new JavaElementLabelProvider());
          m_fieldsViewer.setContentProvider(new FieldContentProvider());
          m_fieldsViewer.addFilter(new FieldFilter());
          // set listeners
          m_fieldsViewer.addOpenListener(new IOpenListener() {
            public void open(OpenEvent e) {
              okPressed();
            }
          });
        }
      }
      // initialize selection in viewer's
      ExecutionUtils.runLog(new RunnableEx() {
        public void run() throws Exception {
          if (m_currentType != null) {
            m_typesViewer.setSelection(new StructuredSelection(m_currentType));
          } else {
            m_typesViewer.getTable().select(0);
            onTypeSelected();
          }
        }
      });
      // set field filter
      {
        IDialogSettings settings = getDialogSettings();
        String filter = settings.get("filter");
        if (filter != null) {
          m_filterText.setText(filter);
          onFilterModify();
        }
      }
      return container;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // GUI additional
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText(ModelMessages.ConstantSelectionPropertyEditor_title);
    }

    @Override
    public boolean close() {
      String filter = m_filterText.getText();
      boolean closed = super.close();
      if (closed) {
        IDialogSettings settings = getDialogSettings();
        settings.put("filter", filter);
      }
      return closed;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
      createButton(
          parent,
          ADD_TYPE_ID,
          ModelMessages.ConstantSelectionPropertyEditor_addTypeButton,
          false);
      createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false);
      createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    @Override
    protected void okPressed() {
      IStructuredSelection selection = (IStructuredSelection) m_fieldsViewer.getSelection();
      m_selectedField = (IField) selection.getFirstElement();
      super.okPressed();
    }

    @Override
    protected void buttonPressed(int buttonId) {
      if (buttonId == ADD_TYPE_ID) {
        onAddType();
      }
      super.buttonPressed(buttonId);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Handlers
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * {@link IType} was selected in {@link #m_typesViewer}, update {@link #m_fieldsViewer}.
     */
    private void onTypeSelected() {
      IStructuredSelection selection = (IStructuredSelection) m_typesViewer.getSelection();
      IType type = (IType) selection.getFirstElement();
      m_fieldsViewer.setInput(type);
      // select current IField
      ExecutionUtils.runLog(new RunnableEx() {
        public void run() throws Exception {
          if (m_currentField != null) {
            m_fieldsViewer.setSelection(new StructuredSelection(m_currentField));
            Table table = m_fieldsViewer.getTable();
            // it is good to set focus to "fields", so allow instantly use up/down keys
            table.setFocus();
            // tweak selection, because setSelection() should be used, not just select()
            {
              int selectionIndex = table.getSelectionIndex();
              if (selectionIndex != -1) {
                table.setSelection(selectionIndex);
              }
            }
          }
        }
      });
    }

    /**
     * Adds new {@link IType} into {@link #m_typesViewer}.
     */
    private void onAddType() {
      ExecutionUtils.runLog(new RunnableEx() {
        public void run() throws Exception {
          IJavaProject javaProject = m_javaInfo.getEditor().getJavaProject();
          IType type = JdtUiUtils.selectType(getShell(), javaProject);
          if (type != null) {
            m_additionalTypes.add(type);
            m_typesViewer.refresh();
            m_typesViewer.setSelection(new StructuredSelection(type));
          }
        }
      });
    }

    /**
     * Updates {@link #m_fieldsViewer} according to the new filter.
     */
    private void onFilterModify() {
      m_fieldsViewer.refresh();
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Type providers
    //
    ////////////////////////////////////////////////////////////////////////////
    private class TypeContentProvider implements IStructuredContentProvider {
      public Object[] getElements(Object inputElement) {
        return ExecutionUtils.runObject(new RunnableObjectEx<Object[]>() {
          public Object[] runObject() throws Exception {
            Set<IType> types = Sets.newHashSet();
            types.addAll(m_additionalTypes);
            types.addAll(getUsedTypes(m_javaInfo));
            types.addAll(getLocalTypes(m_javaInfo));
            return types.toArray();
          }
        });
      }

      public void dispose() {
      }

      public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      }
    }
    ////////////////////////////////////////////////////////////////////////////
    //
    // Field providers
    //
    ////////////////////////////////////////////////////////////////////////////
    private class FieldContentProvider implements IStructuredContentProvider {
      public Object[] getElements(Object inputElement) {
        final IType type = (IType) inputElement;
        return ExecutionUtils.runObject(new RunnableObjectEx<Object[]>() {
          public Object[] runObject() throws Exception {
            return getFields(type).toArray();
          }
        });
      }

      public void dispose() {
      }

      public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      }
    }
    private class FieldFilter extends ViewerFilter {
      @Override
      public boolean select(Viewer viewer, Object parentElement, Object element) {
        IField field = (IField) element;
        String filter = m_filterText.getText().toLowerCase();
        return field.getElementName().toLowerCase().startsWith(filter);
      }
    }
  }
}
