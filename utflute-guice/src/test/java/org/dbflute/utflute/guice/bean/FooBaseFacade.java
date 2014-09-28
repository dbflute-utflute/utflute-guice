package org.dbflute.utflute.guice.bean;

import org.dbflute.utflute.guice.dbflute.exbhv.FooBhv;

import com.google.inject.Inject;

/**
 * @author jflute
 * @since 0.4.0 (2014/03/20 Thursday)
 */
public abstract class FooBaseFacade {

    @Inject
    private FooBhv fooBhv; // super's private field

    public FooBhv superBehaviorInstance() {
        return fooBhv;
    }
}
