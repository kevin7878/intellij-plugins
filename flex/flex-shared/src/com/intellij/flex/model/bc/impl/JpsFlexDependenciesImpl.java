package com.intellij.flex.model.bc.impl;

import com.intellij.flex.model.bc.JpsComponentSet;
import com.intellij.flex.model.bc.JpsFlexDependencies;
import com.intellij.flex.model.bc.JpsFlexDependencyEntry;
import com.intellij.flex.model.bc.JpsLinkageType;
import com.intellij.flex.model.sdk.JpsFlexSdkType;
import com.intellij.flex.model.sdk.JpsFlexmojosSdkProperties;
import com.intellij.flex.model.sdk.JpsFlexmojosSdkType;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.xmlb.annotations.AbstractCollection;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Property;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.*;
import org.jetbrains.jps.model.impl.JpsCompositeElementBase;
import org.jetbrains.jps.model.impl.JpsElementChildRoleBase;
import org.jetbrains.jps.model.impl.JpsElementCollectionRole;
import org.jetbrains.jps.model.library.JpsTypedLibrary;
import org.jetbrains.jps.model.library.sdk.JpsSdk;
import org.jetbrains.jps.model.library.sdk.JpsSdkReference;
import org.jetbrains.jps.model.module.impl.JpsSdkReferenceRole;

import java.util.List;

class JpsFlexDependenciesImpl extends JpsCompositeElementBase<JpsFlexDependenciesImpl> implements JpsFlexDependencies {

  static final JpsFlexDependenciesRole ROLE = new JpsFlexDependenciesRole();

  // todo  may be one untyped reference instead of 2 sdk references?
  private static final JpsSdkReferenceRole<JpsDummyElement> FLEX_SDK_REF_ROLE =
    new JpsSdkReferenceRole<JpsDummyElement>(JpsFlexSdkType.INSTANCE);
  private static final JpsSdkReferenceRole<JpsSimpleElement<JpsFlexmojosSdkProperties>> FLEXMOJOS_SDK_REF_ROLE =
    new JpsSdkReferenceRole<JpsSimpleElement<JpsFlexmojosSdkProperties>>(JpsFlexmojosSdkType.INSTANCE);

  private static final JpsElementChildRoleBase<JpsFlexDependencyEntry> ENTRY_ROLE = JpsElementChildRoleBase.create("flex dependency entry");
  private static final JpsElementCollectionRole<JpsFlexDependencyEntry> ENTRIES_ROLE = JpsElementCollectionRole.create(ENTRY_ROLE);


  private static final JpsLinkageType DEFAULT_FRAMEWORK_LINKAGE = JpsLinkageType.Default;

  private @NotNull String myTargetPlayer = "";
  private @NotNull JpsComponentSet myComponentSet = JpsComponentSet.SparkAndMx;
  private @NotNull JpsLinkageType myFrameworkLinkage = DEFAULT_FRAMEWORK_LINKAGE;

  private JpsFlexDependenciesImpl() {
    myContainer.setChild(ENTRIES_ROLE);
  }

  private JpsFlexDependenciesImpl(final JpsFlexDependenciesImpl original) {
    super(original);
    myTargetPlayer = original.myTargetPlayer;
    myComponentSet = original.myComponentSet;
    myFrameworkLinkage = original.myFrameworkLinkage;
  }

  @NotNull
  public JpsFlexDependenciesImpl createCopy() {
    return new JpsFlexDependenciesImpl(this);
  }

  public void applyChanges(@NotNull final JpsFlexDependenciesImpl modified) {
    super.applyChanges(modified);
    setTargetPlayer(modified.getTargetPlayer());
    setComponentSet(modified.getComponentSet());
    setFrameworkLinkage(modified.getFrameworkLinkage());
  }

// ------------------------------------

  @Nullable
  public JpsSdk<?> getSdk() {
    final JpsSdkReference<JpsDummyElement> flexSdkReference = myContainer.getChild(FLEX_SDK_REF_ROLE);
    if (flexSdkReference != null) {
      final JpsTypedLibrary<JpsSdk<JpsDummyElement>> resolve = flexSdkReference.resolve();
      if (resolve != null) {
        return resolve.getProperties();
      }
    }

    final JpsSdkReference<JpsSimpleElement<JpsFlexmojosSdkProperties>> flexmojosSdkReference =
      myContainer.getChild(FLEXMOJOS_SDK_REF_ROLE);
    if (flexmojosSdkReference != null) {
      final JpsTypedLibrary<JpsSdk<JpsSimpleElement<JpsFlexmojosSdkProperties>>> resolve = flexmojosSdkReference.resolve();
      if (resolve != null) {
        return resolve.getProperties();
      }
    }

    return null;
  }

  @NotNull
  public String getTargetPlayer() {
    return myTargetPlayer;
  }

  public void setTargetPlayer(@NotNull final String targetPlayer) {
    myTargetPlayer = targetPlayer;
  }

  @NotNull
  public JpsComponentSet getComponentSet() {
    return myComponentSet;
  }

  public void setComponentSet(@NotNull final JpsComponentSet componentSet) {
    myComponentSet = componentSet;
  }

  @Override
  @NotNull
  public JpsLinkageType getFrameworkLinkage() {
    return myFrameworkLinkage;
  }

  public void setFrameworkLinkage(@NotNull final JpsLinkageType frameworkLinkage) {
    myFrameworkLinkage = frameworkLinkage;
  }

  @NotNull
  public List<JpsFlexDependencyEntry> getEntries() {
    return myContainer.getChild(ENTRIES_ROLE).getElements();
  }

// ------------------------------------

  State getState() {
    final State state = new State();

    final JpsSdkReference<JpsDummyElement> flexSdkReference =
      myContainer.getChild(FLEX_SDK_REF_ROLE);
    final JpsSdkReference<JpsSimpleElement<JpsFlexmojosSdkProperties>> flexmojosSdkReference =
      myContainer.getChild(FLEXMOJOS_SDK_REF_ROLE);
    if (flexSdkReference != null && flexmojosSdkReference != null) {
      state.SDK.mySdkName = flexSdkReference.getSdkName();
    }
    //else if (flexSdkReference != null) {
    //  state.SDK.mySdkName = flexSdkReference.getSdkName();
    //  state.SDK.mySdkType = JpsFlexSdkType.ID;
    //}
    //else if (flexmojosSdkReference != null) {
    //  state.SDK.mySdkName = flexmojosSdkReference.getSdkName();
    //  state.SDK.mySdkType = JpsFlexmojosSdkType.ID;
    //}

    state.TARGET_PLAYER = myTargetPlayer;
    state.COMPONENT_SET = myComponentSet;
    state.FRAMEWORK_LINKAGE = myFrameworkLinkage.getSerializedText();

    /*      todo
    state.ENTRIES = ContainerUtil.mapNotNull(myEntries.toArray(new ModifiableDependencyEntry[myEntries.size()]),
                                             new Function<ModifiableDependencyEntry, EntryState>() {
                                               @Override
                                               public EntryState fun(ModifiableDependencyEntry entry) {
                                                 if (entry instanceof StatefulDependencyEntry) {
                                                   return ((StatefulDependencyEntry)entry).getState();
                                                 }
                                                 else {
                                                   throw new StateStorageException("Unexpected entry type: " + entry);
                                                 }
                                               }
                                             }, new EntryState[0]);
    */

    return state;
  }

  void loadState(@NotNull State state) {
    if (state.SDK != null && !StringUtil.isEmpty(state.SDK.mySdkName)) {
      //if (JpsFlexSdkType.ID.equals(state.SDK.mySdkType)) {
      //  myContainer.setChild(FLEX_SDK_REF_ROLE,
      //                       JpsElementFactory.getInstance().createSdkReference(state.SDK.mySdkName, JpsFlexSdkType.INSTANCE));
      //}
      //else if (JpsFlexmojosSdkType.ID.equals(state.SDK.mySdkType)) {
      //  myContainer.setChild(FLEXMOJOS_SDK_REF_ROLE,
      //                       JpsElementFactory.getInstance().createSdkReference(state.SDK.mySdkName, JpsFlexmojosSdkType.INSTANCE));
      //}
      //else {
      myContainer.setChild(FLEX_SDK_REF_ROLE,
                           JpsElementFactory.getInstance().createSdkReference(state.SDK.mySdkName, JpsFlexSdkType.INSTANCE));
      myContainer.setChild(FLEXMOJOS_SDK_REF_ROLE,
                           JpsElementFactory.getInstance().createSdkReference(state.SDK.mySdkName, JpsFlexmojosSdkType.INSTANCE));
      //}
    }

    myTargetPlayer = state.TARGET_PLAYER;
    myComponentSet = state.COMPONENT_SET;
    myFrameworkLinkage = JpsLinkageType.valueOf(state.FRAMEWORK_LINKAGE, DEFAULT_FRAMEWORK_LINKAGE);

    final JpsElementCollection<JpsFlexDependencyEntry> entries = myContainer.getChild(ENTRIES_ROLE);
    assert entries.getElements().size() == 0;

    for (State.EntryState entryState : state.ENTRIES) {
      final JpsLinkageType linkageType = JpsLinkageType.valueOf(entryState.DEPENDENCY_TYPE.LINKAGE_TYPE, DEFAULT_FRAMEWORK_LINKAGE);

      if (entryState.LIBRARY_ID != null) {
        entries.addChild(new JpsModuleLibraryDependencyEntryImpl(entryState.LIBRARY_ID, linkageType));
      }
      else if (entryState.LIBRARY_NAME != null) {
        entries.addChild(new JpsSharedLibraryDependencyEntryImpl(entryState.LIBRARY_NAME, entryState.LIBRARY_LEVEL, linkageType));
      }
      else if (entryState.BC_NAME != null) {
        entries.addChild(new JpsFlexBCDependencyEntryImpl(entryState.MODULE_NAME, entryState.BC_NAME, linkageType));
      }
      else {
        assert false : "unknown entry " + entryState;
      }
    }
  }

  private static class JpsFlexDependenciesRole extends JpsElementChildRoleBase<JpsFlexDependencies>
    implements JpsElementCreator<JpsFlexDependencies> {

    public JpsFlexDependenciesRole() {
      super("flex dependencies");
    }

    @NotNull
    public JpsFlexDependencies create() {
      return new JpsFlexDependenciesImpl();
    }
  }

  @Tag("dependencies")
  public static class State {
    @Property(surroundWithTag = false)
    public SdkState SDK = new SdkState();
    @Attribute("target-player")
    public String TARGET_PLAYER = "";
    @Attribute("component-set")
    public JpsComponentSet COMPONENT_SET = JpsComponentSet.SparkAndMx;
    @Attribute("framework-linkage")
    public String FRAMEWORK_LINKAGE = DEFAULT_FRAMEWORK_LINKAGE.getSerializedText();
    @Tag("entries")
    @AbstractCollection(surroundWithTag = false)
    public EntryState[] ENTRIES = new EntryState[0];

    @Tag("sdk")
    public static class SdkState {
      @Attribute("name")
      public String mySdkName;
      //@Attribute("type")
      //public String mySdkType;
    }

    @Tag("entry")
    public static class EntryState {
      @Attribute("module-name")
      public String MODULE_NAME;
      @Attribute("build-configuration-name")
      public String BC_NAME;
      @Attribute("library-id")
      public String LIBRARY_ID;
      @Attribute("library-name")
      public String LIBRARY_NAME;
      @Attribute("library-level")
      public String LIBRARY_LEVEL;
      @Property(surroundWithTag = false)
      public DependencyTypeState DEPENDENCY_TYPE;
    }

    @Tag("dependency")
    public static class DependencyTypeState {
      @Attribute("linkage")
      public String LINKAGE_TYPE;
    }
  }
}
