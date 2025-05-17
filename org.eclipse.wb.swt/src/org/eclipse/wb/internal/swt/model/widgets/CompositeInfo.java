/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *    Marcel du Preez - Altered the method fillLayoutsManager to include only layouts specified in
 *                      the preferences.
 *                    - Moved code out of initialize_createImplicitLayout and replaced it with a new method getDefaultCompositeInfo
 *                    - Added the method getDefaultCompositeInfo that returns the appropriate default layout, either specified in preferences or Absolute layout
 *******************************************************************************/
package org.eclipse.wb.internal.swt.model.widgets;

import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.editor.constants.CoreImages;
import org.eclipse.wb.core.editor.constants.IEditorPreferenceConstants;
import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.core.model.association.ImplicitObjectAssociation;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildAddBefore;
import org.eclipse.wb.core.model.broadcast.ObjectInfoTreeComplete;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.TopBoundsSupport;
import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.clipboard.ComponentClipboardCommand;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.IImplicitCreationSupport;
import org.eclipse.wb.internal.core.model.creation.IThisMethodParameterEvaluator;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.LayoutDescription;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.description.helpers.LayoutDescriptionHelper;
import org.eclipse.wb.internal.core.model.generation.statement.PureFlatStatementGenerator;
import org.eclipse.wb.internal.core.model.generation.statement.StatementGenerator;
import org.eclipse.wb.internal.core.model.generation.statement.block.BlockStatementGenerator;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.core.model.variable.EmptyInvocationVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.swt.IExceptionConstants;
import org.eclipse.wb.internal.swt.model.ModelMessages;
import org.eclipse.wb.internal.swt.model.layout.ImplicitLayoutCreationSupport;
import org.eclipse.wb.internal.swt.model.layout.ImplicitLayoutVariableSupport;
import org.eclipse.wb.internal.swt.model.layout.LayoutDataInfo;
import org.eclipse.wb.internal.swt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.absolute.AbsoluteLayoutCreationSupport;
import org.eclipse.wb.internal.swt.model.layout.absolute.AbsoluteLayoutInfo;
import org.eclipse.wb.internal.swt.model.property.TabOrderProperty;
import org.eclipse.wb.internal.swt.support.CoordinateUtils;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Bundle;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Objects;

/**
 * Model for any SWT {@link Composite}.
 *
 * @author lobas_av
 * @coverage swt.model.widgets
 */
public class CompositeInfo extends ScrollableInfo
implements
ICompositeInfo,
IThisMethodParameterEvaluator {
	private final CompositeInfo m_this = this;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public CompositeInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
		addBroacastListeners();
		m_tabOrderProperty = new TabOrderProperty(this);
		dontAllowDouble_setLayout();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Initialize
	//
	////////////////////////////////////////////////////////////////////////////
	private void addBroacastListeners() throws Exception {
		addBroadcastListener(new ObjectInfoTreeComplete() {
			@Override
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
					fillContextMenu(manager);
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
	 * We should not allow to execute {@link Composite#setLayout(Layout)} more than one time, this
	 * causes problems with implicit layouts and may also cause problems with {@link LayoutDataInfo}.
	 */
	private void dontAllowDouble_setLayout() {
		addBroadcastListener(new ObjectInfoTreeComplete() {
			@Override
			public void invoke() throws Exception {
				removeBroadcastListener(this);
			}
		});
		addBroadcastListener(new ObjectInfoChildAddBefore() {
			@Override
			public void invoke(ObjectInfo parent, ObjectInfo child, ObjectInfo[] nextChild)
					throws Exception {
				if (parent == m_this && child instanceof LayoutInfo) {
					List<LayoutInfo> layouts = getChildren(LayoutInfo.class);
					if (!layouts.isEmpty()) {
						LayoutInfo existingLayout = layouts.get(0);
						if (!(existingLayout.getCreationSupport() instanceof ImplicitLayoutCreationSupport)) {
							throw new DesignerException(IExceptionConstants.DOUBLE_SET_LAYOUT,
									m_this.toString(),
									existingLayout.toString(),
									child.toString());
						}
					}
				}
			}
		});
	}

	/**
	 * Fill context menu {@link IMenuManager}.
	 */
	protected void fillContextMenu(IMenuManager manager) throws Exception {
		contextMenu_setLayout(manager);
		contextMenu_setMinimalSize(manager);
	}

	/**
	 * Adds "Set Layout" sub-menu for setting new {@link LayoutInfo} on this {@link CompositeInfo}.
	 */
	private void contextMenu_setLayout(IMenuManager manager) throws Exception {
		// check if we have layout at all
		if (!hasLayout()) {
			return;
		}
		// OK, add "Set layout"
		IMenuManager layoutsManager = new MenuManager(ModelMessages.CompositeInfo_setLayoutManager);
		manager.appendToGroup(IContextMenuConstants.GROUP_LAYOUT, layoutsManager);
		fillLayoutsManager(layoutsManager);
	}

	/**
	 * Fills given {@link IMenuManager} with {@link IAction}s for setting new {@link LayoutInfo} on
	 * this {@link CompositeInfo}.
	 */
	public void fillLayoutsManager(IMenuManager layoutsManager) throws Exception {
		// add "absolute"
		{
			ObjectInfoAction action = new ObjectInfoAction(this) {
				@Override
				protected void runEx() throws Exception {
					AbsoluteLayoutInfo layout = AbsoluteLayoutInfo.createExplicit(m_this);
					setLayout(layout);
				}
			};
			action.setText(ModelMessages.CompositeInfo_setLayoutAbsolute);
			action.setImageDescriptor(CoreImages.LAYOUT_ABSOLUTE);
			layoutsManager.add(action);
		}
		// add layout items
		final AstEditor editor = getEditor();
		ClassLoader editorLoader = EditorState.get(editor).getEditorLoader();
		List<LayoutDescription> descriptions =
				LayoutDescriptionHelper.get(getDescription().getToolkit());
		for (final LayoutDescription description : descriptions) {
			if (InstanceScope.INSTANCE.getNode(
					IEditorPreferenceConstants.P_AVAILABLE_LAYOUTS_NODE).getBoolean(
							description.getLayoutClassName(),
							true)) {
				final Class<?> layoutClass = editorLoader.loadClass(description.getLayoutClassName());
				final String creationId = description.getCreationId();
				ComponentDescription layoutComponentDescription =
						ComponentDescriptionHelper.getDescription(editor, layoutClass);
				ObjectInfoAction action = new ObjectInfoAction(this) {
					@Override
					protected void runEx() throws Exception {
						description.ensureLibraries(editor.getJavaProject());
						LayoutInfo layout = (LayoutInfo) JavaInfoUtils.createJavaInfo(
								getEditor(),
								layoutClass,
								new ConstructorCreationSupport(creationId, true));
						setLayout(layout);
					}
				};
				action.setText(description.getName());
				action.setImageDescriptor(layoutComponentDescription.getIcon());
				layoutsManager.add(action);
			}
		}
	}

	/**
	 * Adds "Set minimal size" item.
	 */
	private void contextMenu_setMinimalSize(IMenuManager manager) throws Exception {
		if (isRoot() || JavaInfoUtils.hasTrueParameter(this, "SWT.isRoot")) {
			ObjectInfoAction action = new ObjectInfoAction(this) {
				@Override
				protected void runEx() throws Exception {
					Dimension preferredSize = getPreferredSize();
					getTopBoundsSupport().setSize(preferredSize.width, preferredSize.height);
				}
			};
			action.setText(ModelMessages.CompositeInfo_setMinimalSize);
			manager.appendToGroup(IContextMenuConstants.GROUP_LAYOUT, action);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IThisMethodParameterEvaluator
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Object evaluateParameter(EvaluationContext context,
			MethodDeclaration methodDeclaration,
			String methodSignature,
			SingleVariableDeclaration parameter,
			int index) throws Exception {
		if (Objects.equals(parameter.getName().getIdentifier(), "style")) {
			return SWT.NONE;
		}
		return AstEvaluationEngine.UNKNOWN;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void refresh_dispose() throws Exception {
		// inherit parent layout, if it is valid at all and valid time
		processInitialLayout();
		// call "super"
		super.refresh_dispose();
	}

	@Override
	protected void refresh_fetch() throws Exception {
		m_clientAreaInsets2 = CoordinateUtils.getClientAreaInsets2(getWidget());
		super.refresh_fetch();
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
		if (hasLayout()) {
			properties.add(m_tabOrderProperty);
		}
		return properties;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// TopBoundsSupport
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected TopBoundsSupport createTopBoundsSupport() {
		// return implementation from RCP bundles
		return ExecutionUtils.runObject(() -> {
			// prepare "impl" class
			Class<?> implClass;
			{
				Bundle bundle = getDescription().getToolkit().getBundle();
				String implClassName =
						bundle.getSymbolicName() + ".model.widgets.CompositeTopBoundsSupport";
				implClassName = StringUtils.replace(implClassName, ".wb.", ".wb.internal.");
				implClass = bundle.loadClass(implClassName);
			}
			// create instance
			Constructor<?> constructor = implClass.getConstructor(CompositeInfo.class);
			return (TopBoundsSupport) constructor.newInstance(m_this);
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Layout
	//
	////////////////////////////////////////////////////////////////////////////
	private static final String KEY_LAYOUT_HAS = "layout.has";
	private static final String KEY_LAYOUT_ALREADY_PROCESSED =
			"default/parent layout already processed";
	public static final String KEY_DONT_INHERIT_LAYOUT = "default/disable parent layout inheritance";
	/**
	 * We set this key during {@link #setLayout(LayoutInfo)} to prevent implicit {@link LayoutInfo}
	 * activation during layout replacement.
	 */
	public static final String KEY_DONT_SET_IMPLICIT_LAYOUT = "KEY_DONT_SET_IMPLICIT_LAYOUT";

	/**
	 * Forcibly specifies that this <b>instance</b> of {@link CompositeInfo} has no {@link LayoutInfo}
	 * .
	 */
	public final void markNoLayout() throws Exception {
		putArbitraryValue(KEY_LAYOUT_HAS, Boolean.FALSE);
		// remove possible LayoutInfo, it can be only implicit
		for (LayoutInfo layout : getChildren(LayoutInfo.class)) {
			Assert.instanceOf(ImplicitLayoutCreationSupport.class, layout.getCreationSupport());
			removeChild(layout);
		}
	}

	/**
	 * @return <code>true</code> if this {@link CompositeInfo} can have {@link LayoutInfo}.
	 */
	public final boolean hasLayout() {
		if (isPlaceholder()) {
			return false;
		}
		// check, may be we have instance-level flag
		{
			Boolean hasLayout = (Boolean) getArbitraryValue(KEY_LAYOUT_HAS);
			if (hasLayout != null) {
				return hasLayout;
			}
		}
		// OK, look into descriptions
		return JavaInfoUtils.hasTrueParameter(this, KEY_LAYOUT_HAS);
	}

	private void initialize_createImplicitLayout() throws Exception {
		if (hasLayout()) {
			if (initialize_hasExplicitLayout()) {
				return;
			}
			// prepare for creation
			AstEditor editor = getEditor();
			Layout layout = getWidget().getLayout();
			// check if same implicit already exists
			if (initialize_removeImplicitLayout(layout)) {
				return;
			}
			// create layout model
			LayoutInfo implicitLayout;
			CreationSupport creationSupport = new ImplicitLayoutCreationSupport(this);
			if (layout == null) {
				ToolkitDescription toolkit = getDescription().getToolkit();
				implicitLayout = new AbsoluteLayoutInfo(editor, toolkit, creationSupport);
			} else {
				Class<?> layoutClass = layout.getClass();
				implicitLayout = checkLayoutPreferences(layoutClass, editor, creationSupport);

			}

			// set variable support
			VariableSupport variableSupport = new ImplicitLayoutVariableSupport(implicitLayout);
			implicitLayout.setVariableSupport(variableSupport);
			// set association
			implicitLayout.setAssociation(new ImplicitObjectAssociation(this));
			// add as child
			addChildFirst(implicitLayout);
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
				if (child.getObject() != layoutObject) {
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
	 * Attempts to set absolute layout.
	 */
	private void initialize_createAbsoluteLayout() throws Exception {
		if (hasLayout()) {
			MethodInvocation setLayoutInvocation =
					getMethodInvocation("setLayout(org.eclipse.swt.widgets.Layout)");
			if (setLayoutInvocation != null
					&& setLayoutInvocation.arguments().get(0) instanceof NullLiteral) {
				AstEditor editor = getEditor();
				ToolkitDescription toolkit = getDescription().getToolkit();
				// creation support
				CreationSupport creationSupport;
				{
					NullLiteral nullLiteral = (NullLiteral) setLayoutInvocation.arguments().get(0);
					creationSupport = new AbsoluteLayoutCreationSupport(nullLiteral);
				}
				// create model
				LayoutInfo absoluteLayout = new AbsoluteLayoutInfo(editor, toolkit, creationSupport);
				absoluteLayout.setAssociation(new InvocationChildAssociation(setLayoutInvocation));
				absoluteLayout.setObject(null);
				addChild(absoluteLayout);
			}
		}
	}

	/**
	 * @return the current {@link LayoutInfo} for this composite. Can not return <code>null</code>.
	 *
	 * @throws IllegalStateException
	 *           if no {@link LayoutInfo} found.
	 */
	public final LayoutInfo getLayout() {
		Assert.isTrueException(hasLayout(), IExceptionConstants.NO_LAYOUT_EXPECTED, this);
		// try to find layout
		for (ObjectInfo child : getChildren()) {
			if (child instanceof LayoutInfo) {
				return (LayoutInfo) child;
			}
		}
		// composite that has layout, should always have some layout model
		throw new IllegalStateException(ModelMessages.CompositeInfo_shouldAlwaysHaveLayout);
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
			// prepare StatementGenerator
			StatementGenerator statementGenerator;
			if (JavaInfoUtils.hasTrueParameter(newLayout, "layout.setInBlock")) {
				statementGenerator = BlockStatementGenerator.INSTANCE;
			} else {
				statementGenerator = PureFlatStatementGenerator.INSTANCE;
			}
			// set new layout
			VariableSupport variableSupport =
					new EmptyInvocationVariableSupport(newLayout, "%parent%.setLayout(%child%)", 0);
			JavaInfoUtils.add(
					newLayout,
					variableSupport,
					statementGenerator,
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
			boolean shouldBeProcessed = hasLayout()
					&& getArbitraryValue(JavaInfo.FLAG_MANUAL_COMPONENT) == Boolean.TRUE
					&& getArbitraryValue(KEY_LAYOUT_ALREADY_PROCESSED) == null
					&& getArbitraryValue(KEY_DONT_INHERIT_LAYOUT) == null;
			if (!shouldBeProcessed) {
				return;
			}
			// this is first, and last time when we should do processing
			putArbitraryValue(KEY_LAYOUT_ALREADY_PROCESSED, Boolean.TRUE);
		}
		// check for inheritance from parent
		if (preferences.getBoolean(IPreferenceConstants.P_LAYOUT_OF_PARENT)
				&& getParent() instanceof CompositeInfo) {
			CompositeInfo parentComposite = (CompositeInfo) getParent();
			// may be no layout
			if (!parentComposite.hasLayout()) {
				return;
			}
			// may be implicit
			LayoutInfo parentLayout = parentComposite.getLayout();
			if (parentLayout.getCreationSupport() instanceof IImplicitCreationSupport) {
				return;
			}
			// prepare Layout copy
			final LayoutInfo thisLayout;
			{
				Class<?> layoutClass = parentLayout.getDescription().getComponentClass();
				if (layoutClass == null) {
					thisLayout = AbsoluteLayoutInfo.createExplicit(this);
				} else {
					thisLayout = (LayoutInfo) JavaInfoUtils.createJavaInfo(
							getEditor(),
							layoutClass,
							new ConstructorCreationSupport());
				}
			}
			// we are in process of refresh(), set inherited layout later
			ExecutionUtils.runLater(this, new RunnableEx() {
				@Override
				public void run() throws Exception {
					setLayout(thisLayout);
				}
			});
			// OK, stop here
			return;
		}
		// check for default layout
		{
			String layoutId = preferences.getString(IPreferenceConstants.P_LAYOUT_DEFAULT);
			LayoutDescription layoutDescription =
					LayoutDescriptionHelper.get(getDescription().getToolkit(), layoutId);
			if (layoutDescription != null) {
				Class<?> layoutClass;
				{
					String layoutClassName = layoutDescription.getLayoutClassName();
					ClassLoader editorLoader = EditorState.get(getEditor()).getEditorLoader();
					layoutClass = editorLoader.loadClass(layoutClassName);
				}
				//
				final LayoutInfo thisLayout = (LayoutInfo) JavaInfoUtils.createJavaInfo(
						getEditor(),
						layoutClass,
						new ConstructorCreationSupport());
				// we are in process of refresh(), set inherited layout later
				ExecutionUtils.runLater(this, new RunnableEx() {
					@Override
					public void run() throws Exception {
						setLayout(thisLayout);
					}
				});
			}
		}
	}

	/**
	 * @return <code>true</code> if need draw dots border for this composite.
	 */
	public boolean shouldDrawDotsBorder() throws Exception {
		// if has native border, no need to custom one
		{
			if ((getWidget().getStyle() & SWT.BORDER) != 0) {
				return false;
			}
		}
		// use script
		String script = JavaInfoUtils.getParameter(this, "shouldDrawBorder");
		if (StringUtils.isEmpty(script)) {
			return false;
		}
		return (Boolean) JavaInfoUtils.executeScript(this, script);
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
	private Insets m_clientAreaInsets2;

	@Override
	public final List<ControlInfo> getChildrenControls() {
		return getChildren(ControlInfo.class);
	}

	@Override
	public final Insets getClientAreaInsets2() {
		return m_clientAreaInsets2;
	}

	/**
	 * @return <code>true</code> if this {@link Composite} has {@link SWT#RIGHT_TO_LEFT} style.
	 */
	@Override
	public final boolean isRTL() {
		return getWidget() != null && (getWidget().getStyle() & SWT.RIGHT_TO_LEFT) != 0;
	}

	@Override
	public Composite getWidget() {
		return (Composite) getObject();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Clipboard
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds commands for coping this {@link CompositeInfo}.
	 */
	protected void clipboardCopy_addCommands(List<ClipboardCommand> commands) throws Exception {
		if (hasLayout()) {
			LayoutInfo layout = getLayout();
			if (layout.getCreationSupport() instanceof IImplicitCreationSupport) {
				// no need to set implicit layout
			} else if (layout instanceof AbsoluteLayoutInfo) {
				commands.add(new ComponentClipboardCommand<CompositeInfo>() {
					private static final long serialVersionUID = 0L;

					@Override
					public void execute(CompositeInfo composite) throws Exception {
						composite.addMethodInvocation("setLayout(org.eclipse.swt.widgets.Layout)", "null");
					}
				});
			} else {
				final JavaInfoMemento layoutMemento = JavaInfoMemento.createMemento(layout);
				commands.add(new ComponentClipboardCommand<CompositeInfo>() {
					private static final long serialVersionUID = 0L;

					@Override
					public void execute(CompositeInfo composite) throws Exception {
						LayoutInfo newLayout = (LayoutInfo) layoutMemento.create(composite);
						composite.setLayout(newLayout);
						layoutMemento.apply();
					}
				});
			}
		}
	}

	/**
	 * Check that implicit layout is the same as the default layout, default layout should always take
	 * precedence over implicit layout. If the default layout AND the implicit layout is not allowed
	 * (via preference settings) then the absolute layout is used.
	 *
	 * @return Default/Implicit or Absolute Layout
	 * @throws Exception
	 */
	public LayoutInfo getDefaultCompositeInfo() throws Exception {
		LayoutInfo layoutInfo = null;
		Class<?> preferenceDefaultlayoutClass = null;
		IPreferenceStore preferences = getDescription().getToolkit().getPreferences();
		String layoutId = preferences.getString(IPreferenceConstants.P_LAYOUT_DEFAULT);
		if (layoutId != "") {
			// handle the case that "Implicit" is specified in the preferences
			//handle the case that another layout has been specified as default
			LayoutDescription ldescription =
					LayoutDescriptionHelper.get(getDescription().getToolkit(), layoutId);
			if (ldescription != null) {
				String layoutClassName = ldescription.getLayoutClassName();
				ClassLoader editorLoader = EditorState.get(getEditor()).getEditorLoader();
				preferenceDefaultlayoutClass = editorLoader.loadClass(layoutClassName);
				if (layoutClassName != null && !InstanceScope.INSTANCE
						.getNode(IEditorPreferenceConstants.P_AVAILABLE_LAYOUTS_NODE).getBoolean(layoutClassName, true)) {
					return layoutInfo = AbsoluteLayoutInfo.createExplicit(this);
				}
				CreationSupport creationSupport = new ImplicitLayoutCreationSupport(this);
				layoutInfo = (LayoutInfo) JavaInfoUtils.createJavaInfo(
						getEditor(),
						preferenceDefaultlayoutClass,
						creationSupport);
			}
		}
		if (layoutId == "") {
			//Last resort is to load the Absolute layout
			layoutInfo = AbsoluteLayoutInfo.createExplicit(this);

		}

		return layoutInfo;
	}

	public LayoutInfo checkLayoutPreferences(Class<?> layoutInf, AstEditor editor,
			CreationSupport creationSupport) {

		if (layoutInf != null) {
			if (isLayout(layoutInf)) {
				if (!InstanceScope.INSTANCE.getNode(IEditorPreferenceConstants.P_AVAILABLE_LAYOUTS_NODE)
						.getBoolean(layoutInf.getCanonicalName(), true)) {
					try {
						return AbsoluteLayoutInfo.createExplicit(this);
					} catch (Exception e) {

						e.printStackTrace();
					}
				}
			}
		}
		try {
			return (LayoutInfo) JavaInfoUtils.createJavaInfo(editor, layoutInf, creationSupport);
		} catch (Exception e) {

			e.printStackTrace();
		}
		return null;
	}

	private boolean isLayout(Class<?> layoutClass) {
		List<LayoutDescription> descriptions = LayoutDescriptionHelper.get(getDescription().getToolkit());
		for (LayoutDescription description : descriptions) {
			if (description.getLayoutClassName().equals(layoutClass.getCanonicalName())) {
				return true;
			}
		}

		return false;
	}

}