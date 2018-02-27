/*
 * Copyright 2017 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.particleframework.core.util;

import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Utility methods for working with streams
 *
 * @author James Kleeh
 * @since 1.0
 */
public class StreamUtils {

    /**
     * A collector that returns all results that are the maximum based on the
     * provided comparator.
     *
     * @param comparator The comparator to order the items in the stream
     * @param downstream Which collector to use to combine the results
     * @param <T> The type of objects being streamed
     * @param <A> The mutable accumulation type of the reduction operation
     * @param <D> The result type of the reduction operation
     * @return A new collector to provide the desired result
     */
    public static <T, A, D> Collector<T, ?, D> maxAll(Comparator<? super T> comparator,
                                                      Collector<? super T, A, D> downstream) {
        Supplier<A> downstreamSupplier = downstream.supplier();
        BiConsumer<A, ? super T> downstreamAccumulator = downstream.accumulator();
        BinaryOperator<A> downstreamCombiner = downstream.combiner();
        class Container {
            A acc;
            T obj;
            boolean hasAny;

            Container(A acc) {
                this.acc = acc;
            }
        }
        Supplier<Container> supplier = () -> new Container(downstreamSupplier.get());
        BiConsumer<Container, T> accumulator = (acc, t) -> {
            if(!acc.hasAny) {
                downstreamAccumulator.accept(acc.acc, t);
                acc.obj = t;
                acc.hasAny = true;
            } else {
                int cmp = comparator.compare(t, acc.obj);
                if (cmp > 0) {
                    acc.acc = downstreamSupplier.get();
                    acc.obj = t;
                }
                if (cmp >= 0)
                    downstreamAccumulator.accept(acc.acc, t);
            }
        };
        BinaryOperator<Container> combiner = (acc1, acc2) -> {
            if (!acc2.hasAny) {
                return acc1;
            }
            if (!acc1.hasAny) {
                return acc2;
            }
            int cmp = comparator.compare(acc1.obj, acc2.obj);
            if (cmp > 0) {
                return acc1;
            }
            if (cmp < 0) {
                return acc2;
            }
            acc1.acc = downstreamCombiner.apply(acc1.acc, acc2.acc);
            return acc1;
        };
        Function<Container, D> finisher = acc -> downstream.finisher().apply(acc.acc);
        return Collector.of(supplier, accumulator, combiner, finisher);
    }

    /**
     * A collector that returns all results that are the minimum based on the
     * provided comparator.
     *
     * @param comparator The comparator to order the items in the stream
     * @param downstream Which collector to use to combine the results
     * @param <T> The type of objects being streamed
     * @param <A> The mutable accumulation type of the reduction operation
     * @param <D> The result type of the reduction operation
     * @return A new collector to provide the desired result
     */
    public static <T, A, D> Collector<T, ?, D> minAll(Comparator<? super T> comparator,
                                                      Collector<? super T, A, D> downstream) {
        Supplier<A> downstreamSupplier = downstream.supplier();
        BiConsumer<A, ? super T> downstreamAccumulator = downstream.accumulator();
        BinaryOperator<A> downstreamCombiner = downstream.combiner();
        class Container {
            A acc;
            T obj;
            boolean hasAny;

            Container(A acc) {
                this.acc = acc;
            }
        }
        Supplier<Container> supplier = () -> new Container(downstreamSupplier.get());
        BiConsumer<Container, T> accumulator = (acc, t) -> {
            if(!acc.hasAny) {
                downstreamAccumulator.accept(acc.acc, t);
                acc.obj = t;
                acc.hasAny = true;
            } else {
                int cmp = comparator.compare(t, acc.obj);
                if (cmp < 0) {
                    acc.acc = downstreamSupplier.get();
                    acc.obj = t;
                }
                if (cmp <= 0)
                    downstreamAccumulator.accept(acc.acc, t);
            }
        };
        BinaryOperator<Container> combiner = (acc1, acc2) -> {
            if (!acc2.hasAny) {
                return acc1;
            }
            if (!acc1.hasAny) {
                return acc2;
            }
            int cmp = comparator.compare(acc1.obj, acc2.obj);
            if (cmp < 0) {
                return acc1;
            }
            if (cmp > 0) {
                return acc2;
            }
            acc1.acc = downstreamCombiner.apply(acc1.acc, acc2.acc);
            return acc1;
        };
        Function<Container, D> finisher = acc -> downstream.finisher().apply(acc.acc);
        return Collector.of(supplier, accumulator, combiner, finisher);
    }
}