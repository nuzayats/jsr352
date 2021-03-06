/*
 * Copyright (c) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Cheng Fang - Initial API and implementation
 */

package org.jberet.testapps.cdiscopes.partitionscoped;

import javax.batch.api.listener.StepListener;
import javax.inject.Inject;
import javax.inject.Named;

@Named
public class PartitionScopeStepListener implements StepListener {
    @Inject
    private Foo foo;

    @Inject
    private JobScopedFoo jobScopedFoo;

    @Inject
    private StepScopedFoo stepScopedFoo;

    @Override
    public void beforeStep() throws Exception {
        System.out.printf("In %s, foo: %s%n", this, foo);
        System.out.printf("In %s, jobScopedFoo: %s, stepScopedFoo: %s%n", this, jobScopedFoo, stepScopedFoo);
    }

    @Override
    public void afterStep() throws Exception {
        System.out.printf("In %s, foo: %s%n", this, foo);
    }
}
