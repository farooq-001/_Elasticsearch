/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the Elastic License 2.0 or the Server
 * Side Public License, v 1.
 */

package org.elasticsearch.compute.aggregation;

import org.elasticsearch.compute.Experimental;
import org.elasticsearch.compute.data.AggregatorStateBlock;
import org.elasticsearch.compute.data.Block;
import org.elasticsearch.compute.data.DoubleArrayBlock;
import org.elasticsearch.compute.data.LongArrayBlock;
import org.elasticsearch.compute.data.Page;

@Experimental
final class MaxAggregator implements AggregatorFunction {

    private final DoubleState state;
    private final int channel;

    static MaxAggregator create(int inputChannel) {
        if (inputChannel < 0) {
            throw new IllegalArgumentException();
        }
        return new MaxAggregator(inputChannel, new DoubleState(Double.NEGATIVE_INFINITY));
    }

    static MaxAggregator createIntermediate() {
        return new MaxAggregator(-1, new DoubleState(Double.NEGATIVE_INFINITY));
    }

    private MaxAggregator(int channel, DoubleState state) {
        this.channel = channel;
        this.state = state;
    }

    @Override
    public void addRawInput(Page page) {
        assert channel >= 0;
        Block block = page.getBlock(channel);
        double max;
        if (block instanceof LongArrayBlock longBlock) {
            max = maxFromLongBlock(longBlock);
        } else {
            max = maxFromBlock(block);
        }
        state.doubleValue(Math.max(state.doubleValue(), max));
    }

    static double maxFromBlock(Block block) {
        double max = Double.MIN_VALUE;
        int len = block.getPositionCount();
        for (int i = 0; i < len; i++) {
            max = Math.max(max, block.getDouble(i));
        }
        return max;
    }

    static double maxFromLongBlock(LongArrayBlock block) {
        double max = Double.NEGATIVE_INFINITY;
        long[] values = block.getRawLongArray();
        for (int i = 0; i < block.getPositionCount(); i++) {
            max = Math.max(max, values[i]);
        }
        return max;
    }

    static double maxFromLongBlockl(LongArrayBlock block) {
        long max = Long.MIN_VALUE;
        long[] values = block.getRawLongArray();
        for (int i = 0; i < values.length; i++) {
            max = Math.max(max, values[i]);
        }
        return (double) max;
    }

    @Override
    public void addIntermediateInput(Block block) {
        assert channel == -1;
        if (block instanceof AggregatorStateBlock) {
            @SuppressWarnings("unchecked")
            AggregatorStateBlock<DoubleState> blobBlock = (AggregatorStateBlock<DoubleState>) block;
            DoubleState state = this.state;
            DoubleState tmpState = new DoubleState();
            for (int i = 0; i < block.getPositionCount(); i++) {
                blobBlock.get(i, tmpState);
                state.doubleValue(Math.max(state.doubleValue(), tmpState.doubleValue()));
            }
        } else {
            throw new RuntimeException("expected AggregatorStateBlock, got:" + block);
        }
    }

    @Override
    public Block evaluateIntermediate() {
        AggregatorStateBlock.Builder<AggregatorStateBlock<DoubleState>, DoubleState> builder = AggregatorStateBlock
            .builderOfAggregatorState(DoubleState.class, state.getEstimatedSize());
        builder.add(state);
        return builder.build();
    }

    @Override
    public Block evaluateFinal() {
        return new DoubleArrayBlock(new double[] { state.doubleValue() }, 1);
    }
}