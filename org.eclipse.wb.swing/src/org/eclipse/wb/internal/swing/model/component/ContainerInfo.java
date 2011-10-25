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
package org.eclipse.wb.internal.swing.model.component;

import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.core.model.association.ImplicitObjectAssociation;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoTreeComplete;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.clipboard.ComponentClipboardCommand;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.IImplicitCreationSupport;
import org.eclipse.wb.internal.core.model.creation.ThisCreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.LayoutDescription;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.description.helpers.LayoutDescriptionHelper;
import org.eclipse.wb.internal.core.model.generation.statement.PureFlatStatementGenerator;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.core.model.variable.EmptyInvocationVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.ui.ImageImageDescriptor;
import org.eclipse.wb.internal.swing.Activator;
import org.eclipse.wb.internal.swing.model.CoordinateUtils;
import org.eclipse.wb.internal.swing.model.ModelMessages;
import org.eclipse.wb.internal.swing.model.layout.ImplicitLayoutCreationSupport;
import org.eclipse.wb.internal.swing.model.layout.ImplicitLayoutVariableSupport;
import org.eclipse.wb.internal.swing.model.layout.LayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.absolute.AbsoluteLayoutCreationSupport;
import org.eclipse.wb.internal.swing.model.layout.absolute.AbsoluteLayoutInfo;
import org.eclipse.wb.internal.swing.model.property.TabOrderProperty;
import org.eclipse.wb.internal.swing.utils.SwingUtils;

import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.IPreferenceStore;

import java.awt.Component;
import java.awt.Container;
import java.awt.LayoutManager;
import java.util.List;

import javax.swing.JComponent;

/**
 * Model for any AWT {@link Container}.
 * 
 * @author scheglov_ke
 * @coverage swing.model
 */
public class ContainerInfo extends ComponentInfo {
  /**
   * We set this key during {@link #setLayout(LayoutInfo)} to prevent implicit {@link LayoutInfo}
   * activation during layout replacement.
   */
  public static final String KEY_DONT_SET_IMPLICIT_LAYOUT = "KEY_DONT_SET_IMPLICIT_LAYOUT";
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final ContainerInfo m_this = this;
  private Insets m_insets;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ContainerInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    m_tabOrderProperty = new TabOrderProperty(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initializing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void initialize() throws Exception {
    super.initialize();
    addBroadcastListener(new ObjectInfoTreeComplete() {
      public void invoke() throws Exception {
        initialize_createAbsoluteLayout();
      }
    });
    addBroadcastListener(new ObjectEventListener() {
      @Override
      public void addContextMenu(List<? extends ObjectInfo> objects,
          ObjectInfo object,
          IMenuManager manager) throws Exception {
        if (object == m_this) {
          contextMenu_setLayout(manager);
        }
      }
    });
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void clipboardCopy(JavaInfo javaInfo, List<ClipboardCommand> commands)
          throws Exception {
        if (javaInfo == m_this) {
          clipboardCopy_addCommands(commands);
        }
      }
    });
  }

  @Override
  public void createExposedChildren() throws Exception {
    super.createExposedChildren();
    initialize_createImplicitLayout();
  }

  /**
   * Adds "Set Layout" sub-menu for setting new {@link LayoutInfo} on this {@link CompositeInfo}.
   */
  private void contextMenu_setLayout(IMenuManager manager) throws Exception {
    // check if we have layout at all
    if (!canSetLayout()) {
      return;
    }
    // OK, add "Set layout"
    IMenuManager layoutsManager = new MenuManager(ModelMessages.ContainerInfo_setLayout);
    manager.appendToGroup(IContextMenuConstants.GROUP_LAYOUT, layoutsManager);
    fillLayoutsManager(layoutsManager);
  }

  /**
   * Fills given {@link IMenuManager} with {@link IAction}s for setting new {@link LayoutInfo} on
   * this {@link CompositeInfo}.
   */
  public void fillLayoutsManager(IMenuManager layoutsManager) throws ClassNotFoundException,
      Exception {
    // add "absolute"
    {
      ObjectInfoAction action = new ObjectInfoAction(this) {
        @Override
        protected void runEx() throws Exception {
          AbsoluteLayoutInfo layout = AbsoluteLayoutInfo.createExplicit(getEditor());
          setLayout(layout);
        }
      };
      action.setText(ModelMessages.ContainerInfo_setLayoutAbsolute);
      action.setImageDescriptor(Activator.getImageDescriptor("info/layout/absolute/layout.gif"));
      layoutsManager.add(action);
    }
    // add layout items
    final AstEditor editor = getEditor();
    ClassLoader editorLoader = EditorState.get(editor).getEditorLoader();
    List<LayoutDescription> descriptions =
        LayoutDescriptionHelper.get(getDescription().getToolkit());
    for (final LayoutDescription description : descriptions) {
      final Class<?> layoutClass = editorLoader.loadClass(description.getLayoutClassName());
      final String creationId = description.getCreationId();
      ComponentDescription layoutComponentDescription =
          ComponentDescriptionHelper.getDescription(editor, layoutClass);
      ObjectInfoAction action = new ObjectInfoAction(this) {
        @Override
        protected void runEx() throws Exception {
          description.ensureLibraries(editor.getJavaProject());
          LayoutInfo layout =
              (LayoutInfo) JavaInfoUtils.createJavaInfo(
                  editor,
                  layoutClass,
                  new ConstructorCreationSupport(creationId, true));
          setLayout(layout);
        }
      };
      action.setText(description.getName());
      action.setImageDescriptor(new ImageImageDescriptor(layoutComponentDescription.getIcon()));
      layoutsManager.add(action);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  private final TabOrderProperty m_tabOrderProperty;

  @Override
  protected List<Property> getPropertyList() throws Exception {
    List<Property> properties = super.getPropertyList();
    if (!isRoot() && !hasLayout()) {
      m_tabOrderProperty.setCategory(PropertyCategory.ADVANCED);
    }
    properties.add(m_tabOrderProperty);
    return properties;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void refresh_dispose() throws Exception {
    // remember Container instance, we need it for clearSwingTree(), but "super" set object to "null"
    Container container = (Container) getObject();
    // inherit parent layout, if it is valid at all and valid time
    processInitialLayout();
    // call "super"
    super.refresh_dispose();
    if (isSwingRoot()) {
      SwingUtils.clearSwingTree(container);
    }
  }

  @Override
  protected void refresh_fetch() throws Exception {
    // fetch insets
    {
      java.awt.Insets insets = getContainer().getInsets();
      m_insets = CoordinateUtils.get(insets);
    }
    // continue in super()
    super.refresh_fetch();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Layout
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String KEY_LAYOUT_HAS = "layout.has";
  private static final String KEY_LAYOUT_ALREADY_PROCESSED =
      "default/parent layout already processed";

  /**
   * Prepares {@link LayoutInfo} for any layout existing by default for this container.
   */
  private void initialize_createImplicitLayout() throws Exception {
    if (hasLayout()) {
      if (initialize_hasExplicitLayout()) {
        return;
      }
      // prepare for creation
      AstEditor editor = getEditor();
      Container container = (Container) getObject();
      LayoutManager layout = container.getLayout();
      // check if same implicit already exists
      if (initialize_removeImplicitLayout(layout)) {
        return;
      }
      // create implicit layout model
      LayoutInfo implicitLayout;
      CreationSupport creationSupport = new ImplicitLayoutCreationSupport(this);
      if (layout == null) {
        implicitLayout = new AbsoluteLayoutInfo(editor, creationSupport);
      } else {
        Class<?> layoutClass = layout.getClass();
        implicitLayout =
            (LayoutInfo) JavaInfoUtils.createJavaInfo(editor, layoutClass, creationSupport);
      }
      // initialize layout model
      {
        // set variable support
        {
          VariableSupport variableSupport = new ImplicitLayoutVariableSupport(implicitLayout);
          implicitLayout.setVariableSupport(variableSupport);
        }
        // set association
        implicitLayout.setAssociation(new ImplicitObjectAssociation(this));
        // add as child
        addChildFirst(implicitLayout);
      }
    }
  }

  /**
   * @return <code>true</code> if explicit layout was already set, so we should not try to find
   *         implicit layout anymore.
   */
  private boolean initialize_hasExplicitLayout() {
    List<LayoutInfo> layouts = getChildren(LayoutInfo.class);
    return !layouts.isEmpty()
        && !(layouts.get(0).getCreationSupport() instanceof ImplicitLayoutCreationSupport);
  }

  /**
   * We may call {@link #initialize_createImplicitLayout()} many times, may be after each
   * {@link Statement}, so before adding new implicit layout we should remove existing one.
   * 
   * @return <code>true</code> if {@link LayoutInfo} with same object already exists, so it was not
   *         removed and no need for creating new implicit {@link LayoutInfo}.
   */
  private boolean initialize_removeImplicitLayout(Object layoutObject) throws Exception {
    for (JavaInfo child : getChildrenJava()) {
      if (child.getCreationSupport() instanceof ImplicitLayoutCreationSupport) {
        if (child.getObject() == layoutObject) {
          return true;
        }
        ImplicitLayoutCreationSupport creationSupport =
            (ImplicitLayoutCreationSupport) child.getCreationSupport();
        creationSupport.removeForever();
        break;
      }
    }
    return false;
  }

  /**
   * Adds {@link AbsoluteLayoutInfo} model, if {@link Container#setLayout(LayoutManager)} is invoked
   * with {@link NullLiteral}.
   */
  private void initialize_createAbsoluteLayout() throws Exception {
    if (hasLayout()) {
      MethodInvocation setLayoutInvocation =
          getMethodInvocation("setLayout(java.awt.LayoutManager)");
      if (setLayoutInvocation != null
          && setLayoutInvocation.arguments().get(0) instanceof NullLiteral) {
        addExplicitAbsoluteLayoutChild(setLayoutInvocation);
      }
    }
  }

  private void addExplicitAbsoluteLayoutChild(MethodInvocation setLayoutInvocation)
      throws Exception {
    AstEditor editor = getEditor();
    CreationSupport creationSupport = new AbsoluteLayoutCreationSupport(setLayoutInvocation);
    AbsoluteLayoutInfo absoluteLayoutInfo = new AbsoluteLayoutInfo(editor, creationSupport);
    absoluteLayoutInfo.setAssociation(new InvocationChildAssociation(setLayoutInvocation));
    absoluteLayoutInfo.setObject(null);
    addChild(absoluteLayoutInfo);
  }

  /**
   * @return <code>true</code> if this {@link ContainerInfo} can have {@link LayoutInfo}.
   */
  public final boolean hasLayout() {
    if (isPlaceholder()) {
      return false;
    }
    return JavaInfoUtils.hasTrueParameter(this, KEY_LAYOUT_HAS);
  }

  /**
   * @return <code>true</code> if it is possible to set new {@link LayoutInfo} for this
   *         {@link ContainerInfo}. Note difference between "has" and "can set".
   */
  public final boolean canSetLayout() {
    if (!hasLayout()) {
      return false;
    }
    MethodDescription setLayoutMethod =
        getDescription().getMethod("setLayout(java.awt.LayoutManager)");
    return setLayoutMethod != null && setLayoutMethod.isExecutable();
  }

  /**
   * @return the current {@link LayoutInfo} for this container.
   */
  public final LayoutInfo getLayout() {
    Assert.isTrue(hasLayout());
    // try to find layout
    for (ObjectInfo child : getChildren()) {
      if (child instanceof LayoutInfo) {
        return (LayoutInfo) child;
      }
    }
    // container that has layout, should always have some layout model
    throw new IllegalStateException(ModelMessages.ContainerInfo_containerShouldHaveLayout);
  }

  /**
   * Sets new {@link LayoutInfo}.
   */
  public final void setLayout(LayoutInfo newLayout) throws Exception {
    putArbitraryValue(KEY_DONT_SET_IMPLICIT_LAYOUT, Boolean.TRUE);
    startEdit();
    try {
      // remove old layout
      {
        LayoutInfo oldLayout = getLayout();
        oldLayout.delete();
      }
      // set new layout
      VariableSupport variableSupport =
          new EmptyInvocationVariableSupport(newLayout, "%parent%.setLayout(%child%)", 0);
      JavaInfoUtils.add(
          newLayout,
          variableSupport,
          PureFlatStatementGenerator.INSTANCE,
          AssociationObjects.invocationChildNull(),
          this,
          null);
      newLayout.onSet();
    } finally {
      endEdit();
      putArbitraryValue(KEY_DONT_SET_IMPLICIT_LAYOUT, Boolean.FALSE);
    }
  }

  /**
   * Sets default {@link LayoutInfo} or inherits {@link LayoutInfo} of parent {@link CompositeInfo}.
   */
  private void processInitialLayout() throws Exception {
    IPreferenceStore preferences = getDescription().getToolkit().getPreferences();
    // check if processing required
    {
      boolean shouldBeProcessed =
          hasLayout()
              && getArbitraryValue(JavaInfo.FLAG_MANUAL_COMPONENT) == Boolean.TRUE
              && getArbitraryValue(KEY_LAYOUT_ALREADY_PROCESSED) == null;
      if (!shouldBeProcessed) {
        return;
      }
      // this is first, and last time when we should do processing
      putArbitraryValue(KEY_LAYOUT_ALREADY_PROCESSED, Boolean.TRUE);
    }
    // check for inheritance from parent
    if (preferences.getBoolean(IPreferenceConstants.P_LAYOUT_OF_PARENT)
        && getParent() instanceof ContainerInfo) {
      ContainerInfo parentComposite = (ContainerInfo) getParent();
      if (parentComposite.hasLayout()) {
        final LayoutInfo thisLayout;
        {
          LayoutInfo parentLayout = parentComposite.getLayout();
          Class<?> layoutClass = parentLayout.getDescription().getComponentClass();
          if (layoutClass == null) {
            thisLayout = AbsoluteLayoutInfo.createExplicit(getEditor());
          } else {
            thisLayout =
                (LayoutInfo) JavaInfoUtils.createJavaInfo(
                    getEditor(),
                    layoutClass,
                    new ConstructorCreationSupport());
          }
        }
        // we are in process of refresh(), set inherited layout later
        ExecutionUtils.runLater(this, new RunnableEx() {
          public void run() throws Exception {
            setLayout(thisLayout);
          }
        });
      }
      // OK, stop here
      return;
    }
    // check for default layout
    {
      String layoutId = preferences.getString(IPreferenceConstants.P_LAYOUT_DEFAULT);
      LayoutDescription layoutDescription =
          LayoutDescriptionHelper.get(getDescription().getToolkit(), layoutId);
      if (layoutDescription != null) {
        final LayoutInfo thisLayout =
            (LayoutInfo) JavaInfoUtils.createJavaInfo(
                getEditor(),
                layoutDescription.getLayoutClassName(),
                new ConstructorCreationSupport());
        // we are in process of refresh(), set inherited layout later
        ExecutionUtils.runLater(this, new RunnableEx() {
          public void run() throws Exception {
            setLayout(thisLayout);
          }
        });
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Hierarchy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canBeRoot() {
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the {@link Insets} for this AWT {@link Container}. Note, that this method is different
   * from {@link #getClientAreaInsets()}, because in AWT/Swing insets means that it is preferred to
   * place children {@link Component}'s inside of area after cropping by insets, but (0,0) is point
   * on insets, not inside of insets.
   * 
   * @return the {@link Insets} for this AWT {@link Container}.
   */
  public final Insets getInsets() {
    return m_insets;
  }

  /**
   * @return the AWT {@link Container} object for this model.
   */
  public final Container getContainer() {
    return (Container) getObject();
  }

  /**
   * @return the collection of {@link ComponentInfo} children.
   */
  public final List<ComponentInfo> getChildrenComponents() {
    return getChildren(ComponentInfo.class);
  }

  @Override
  public boolean isRTL() {
    return !getComponent().getComponentOrientation().isLeftToRight();
  }

  /**
   * @return <code>true</code> if need draw dots border for this {@link ContainerInfo}.
   */
  public final boolean shouldDrawDotsBorder() {
    IPreferenceStore preferences = getDescription().getToolkit().getPreferences();
    if (preferences.getBoolean(IPreferenceConstants.P_GENERAL_HIGHLIGHT_CONTAINERS)) {
      // no border for "this"
      if (getCreationSupport() instanceof ThisCreationSupport) {
        return false;
      }
      // only if "normal" container, not complex one
      Container container = getContainer();
      if (hasLayout()) {
        // check for existing Swing border
        if (container instanceof JComponent) {
          return ((JComponent) container).getBorder() == null;
        }
        // OK, probably need border
        return true;
      }
    }
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds commands for coping this {@link ContainerInfo}.
   */
  protected void clipboardCopy_addCommands(List<ClipboardCommand> commands) throws Exception {
    if (hasLayout()) {
      LayoutInfo layout = getLayout();
      if (layout.getCreationSupport() instanceof IImplicitCreationSupport) {
        // no need to set implicit layout
      } else if (layout instanceof AbsoluteLayoutInfo) {
        commands.add(new ComponentClipboardCommand<ContainerInfo>() {
          private static final long serialVersionUID = 0L;

          @Override
          protected void execute(ContainerInfo container) throws Exception {
            MethodInvocation setLayoutInvocation =
                container.addMethodInvocation("setLayout(java.awt.LayoutManager)", "null");
            container.addExplicitAbsoluteLayoutChild(setLayoutInvocation);
          }
        });
      } else {
        final JavaInfoMemento layoutMemento = JavaInfoMemento.createMemento(layout);
        commands.add(new ComponentClipboardCommand<ContainerInfo>() {
          private static final long serialVersionUID = 0L;

          @Override
          protected void execute(ContainerInfo container) throws Exception {
            LayoutInfo newLayout = (LayoutInfo) layoutMemento.create(container);
            container.setLayout(newLayout);
            layoutMemento.apply();
          }
        });
      }
    }
  }
}
