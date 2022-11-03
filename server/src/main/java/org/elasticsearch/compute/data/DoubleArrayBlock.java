/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the Elastic License 2.0 or the Server
 * Side Public License, v 1.
 */

package org.elasticsearch.compute.data;

import java.util.Arrays;

/**
 * Block implementation that stores an array of double values.
 */
public final class DoubleArrayBlock extends Block {

    private final double[] values;

    public DoubleArrayBlock(double[] values, int positionCount) {
        super(positionCount);
        this.values = values;
    }

    @Override
    public double getDouble(int position) {
        assert assertPosition(position);
        return values[position];
    }

    @Override
    public Object getObject(int position) {
        return getDouble(position);
    }

    @Override
    public String toString() {
        return "DoubleArrayBlock{positions=" + getPositionCount() + ", values=" + Arrays.toString(values) + '}';
    }
}