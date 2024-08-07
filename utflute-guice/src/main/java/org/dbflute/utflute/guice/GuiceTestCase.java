/*
 * Copyright 2014-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.dbflute.utflute.guice;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.transaction.TransactionManager;

import org.dbflute.utflute.core.InjectionTestCase;
import org.dbflute.utflute.core.binding.BindingAnnotationRule;
import org.dbflute.utflute.core.binding.ComponentBinder;
import org.dbflute.utflute.core.transaction.TransactionFailureException;
import org.dbflute.utflute.core.transaction.TransactionResource;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * @author jflute
 * @since 0.4.0 (2014/03/16 Sunday)
 */
public abstract class GuiceTestCase extends InjectionTestCase {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                          Static Cache
    //                                          ------------
    /** The cached injector for DI container. (NullAllowed: null means beginning or test execution) */
    private static Injector _xcachedInjector;

    // -----------------------------------------------------
    //                                          Guice Object
    //                                          ------------
    /** The current active injector for DI container. {Guice Object} */
    private Injector _xcurrentActiveInjector;

    /** The transaction manager for platform. (NotNull: after injection) */
    @Inject
    private TransactionManager _xtransactionManager;

    // ===================================================================================
    //                                                                            Settings
    //                                                                            ========
    // -----------------------------------------------------
    //                                     Prepare Container
    //                                     -----------------
    @Override
    protected void xprepareTestCaseContainer() {
        xdoPrepareTestCaseContainer();
        xsaveCachedInstance();
    }

    protected void xdoPrepareTestCaseContainer() {
        if (isUseOneTimeContainer()) {
            xdestroyContainer();
        }
        if (xisInitializedContainer()) {
            if (xcanRecycleContainer()) {
                log("...Recycling guice");
                xrecycleContainerInstance();
                return;
            } else {
                xdestroyContainer();
            }
        }
        final List<Module> moduleList = prepareModuleList();
        xinitializeContainer(moduleList);
    }

    protected boolean xcanRecycleContainer() {
        // fixedly true, change unsupported for now
        // (keep same structure as other DI containers)
        return true;
    }

    protected void xrecycleContainerInstance() {
        _xcurrentActiveInjector = _xcachedInjector;
    }

    protected void xsaveCachedInstance() {
        // no cache for now
    }

    /**
     * Prepare module list for Google Guice. <br>
     * You should add DataSource and TransactionManager to the module. 
     * @return The list of module. (NotNull)
     */
    protected List<Module> prepareModuleList() { // customize point
        return new ArrayList<Module>(); // as default
    }

    @Override
    protected void xclearCachedContainer() {
        _xcachedInjector = null;
    }

    // ===================================================================================
    //                                                                         Transaction
    //                                                                         ===========
    /**
     * {@inheritDoc}
     */
    @Override
    protected TransactionResource beginNewTransaction() { // user method
        if (_xtransactionManager == null) { // no use transaction (just in case)
            return null;
        }
        try {
            _xtransactionManager.begin();
        } catch (Exception e) {
            throw new TransactionFailureException("Failed to begin the transaction.", e);
        }
        final GuiceTransactionResource resource = new GuiceTransactionResource();
        resource.setTransactionManager(_xtransactionManager);
        return resource; // for thread-fire's transaction or manual transaction
    }

    // ===================================================================================
    //                                                                   Component Binding
    //                                                                   =================
    @Override
    protected ComponentBinder createOuterComponentBinder(Object bean) {
        final ComponentBinder binder = super.createOuterComponentBinder(bean);
        binder.annotationOnlyBinding();
        return binder;
    }

    @Override
    protected Map<Class<? extends Annotation>, BindingAnnotationRule> xprovideBindingAnnotationRuleMap() {
        // javax.annotation.Resource is unsupported on Google Guice
        final Map<Class<? extends Annotation>, BindingAnnotationRule> ruleMap = newHashMap();
        ruleMap.put(com.google.inject.Inject.class, new BindingAnnotationRule());
        ruleMap.put(javax.inject.Inject.class, new BindingAnnotationRule());
        return ruleMap;
    }

    // ===================================================================================
    //                                                                      Guice Handling
    //                                                                      ==============
    // -----------------------------------------------------
    //                                            Initialize
    //                                            ----------
    protected boolean xisInitializedContainer() {
        return _xcachedInjector != null;
    }

    protected void xinitializeContainer(List<Module> moduleList) {
        log("...Initializing guice: " + moduleList);
        _xcurrentActiveInjector = Guice.createInjector(moduleList.toArray(new Module[] {}));
        _xcachedInjector = _xcurrentActiveInjector;
    }

    // -----------------------------------------------------
    //                                               Destroy
    //                                               -------
    @Override
    protected void xdestroyContainer() {
        _xcachedInjector = null;
        _xcurrentActiveInjector = null;
    }

    // -----------------------------------------------------
    //                                             Component
    //                                             ---------
    /** {@inheritDoc} */
    protected <COMPONENT> COMPONENT getComponent(Class<COMPONENT> type) { // user method
        return _xcurrentActiveInjector.getInstance(type);
    }

    /** {@inheritDoc} */
    protected <COMPONENT> COMPONENT getComponent(String name) { // user method
        throw new IllegalStateException("The guice does not support by-name component: " + name);
    }

    /** {@inheritDoc} */
    protected boolean hasComponent(Class<?> type) { // user method
        try {
            getComponent(type);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** {@inheritDoc} */
    protected boolean hasComponent(String name) { // user method
        try {
            getComponent(name);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    protected static Injector xgetCachedInjector() {
        return _xcachedInjector;
    }

    protected static void xsetCachedInjector(Injector xcachedInjector) {
        _xcachedInjector = xcachedInjector;
    }

    protected Injector xgetCurrentActiveInjector() {
        return _xcurrentActiveInjector;
    }

    protected void xsetCurrentActiveInjector(Injector xcurrentActiveInjector) {
        _xcurrentActiveInjector = xcurrentActiveInjector;
    }
}
