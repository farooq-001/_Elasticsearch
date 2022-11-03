/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the Elastic License 2.0 or the Server
 * Side Public License, v 1.
 */

package org.elasticsearch.compute.aggregation;

import org.elasticsearch.compute.Experimental;

@Experimental
public interface AggregatorStateSerializer<T extends AggregatorState<T>> {

    int size();

    // returns the number of bytes written
    int serialize(T state, byte[] ba, int offset);

    void deserialize(T state, byte[] ba, int offset);

}