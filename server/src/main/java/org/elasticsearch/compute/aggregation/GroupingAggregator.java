/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the Elastic License 2.0 or the Server
 * Side Public License, v 1.
 */

package org.elasticsearch.compute.aggregation;

import org.elasticsearch.compute.Experimental;
import org.elasticsearch.compute.data.Block;
import org.elasticsearch.compute.data.Page;

import java.util.function.BiFunction;

@Experimental
public class GroupingAggregator {
    private final GroupingAggregatorFunction aggregatorFunction;

    private final AggregatorMode mode;

    private final int intermediateChannel;

    public GroupingAggregator(
        BiFunction<AggregatorMode, Integer, GroupingAggregatorFunction> aggCreationFunc,
        AggregatorMode mode,
        int inputChannel
    ) {
        this.aggregatorFunction = aggCreationFunc.apply(mode, inputChannel);
        this.mode = mode;
        this.intermediateChannel = mode.isInputPartial() ? inputChannel : -1;
    }

    public void processPage(Block groupIdBlock, Page page) {
        if (mode.isInputPartial()) {
            aggregatorFunction.addIntermediateInput(groupIdBlock, page.getBlock(intermediateChannel));
        } else {
            aggregatorFunction.addRawInput(groupIdBlock, page);
        }
    }

    public Block evaluate() {
        if (mode.isOutputPartial()) {
            return aggregatorFunction.evaluateIntermediate();
        } else {
            return aggregatorFunction.evaluateFinal();
        }
    }
}