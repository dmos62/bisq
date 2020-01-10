/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.desktop.util;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Queue;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import java.util.Collections;

public class MovingAverageUtils
{

    /* with period 2, on an input of [1,2,3,4],
     * should return [Double.NaN, 1.5, 2.5, 3.5].
     */
    public static Stream<Double> simpleMovingAverage(Collection<Number> collection, int period)
    {
        if (period < 1) {
            throw new IllegalArgumentException("Simple moving average period must be a positive number.");
        }

        var collectionSize = collection.size();
        if (collectionSize < period) {
          return Collections.nCopies(collectionSize, Double.NaN).stream();
        }

        var windows = SlidingWindowSpliterator.windowed(collection, period);
        Stream<Double> averages =
            windows.map(window ->
                    window
                    .mapToDouble(number -> number.doubleValue())
                    .summaryStatistics()
                    .getAverage()
                    );

        var lag = period - 1;
        var lagCompensation = Collections.nCopies(lag, Double.NaN).stream();

        return Stream.concat(lagCompensation, averages);
    }

    static class SlidingWindowSpliterator<T> implements Spliterator<Stream<T>>
    {

        static <T> Stream<Stream<T>> windowed(Collection<T> collection, int windowSize) {
            return StreamSupport.stream(new SlidingWindowSpliterator<>(collection, windowSize), false);
        }

        private final Queue<T> buffer;
        private final Iterator<T> sourceIterator;
        private final int windowSize;
        private final int size;

        private SlidingWindowSpliterator(Collection<T> source, int windowSize) {
            this.buffer = new ArrayDeque<>(windowSize);
            this.sourceIterator = Objects.requireNonNull(source).iterator();
            this.windowSize = windowSize;
            this.size = calculateSize(source, windowSize);
        }

        @Override
        public boolean tryAdvance(Consumer<? super Stream<T>> action) {
            if (windowSize < 1) {
                return false;
            }

            while (sourceIterator.hasNext()) {
                buffer.add(sourceIterator.next());

                if (buffer.size() == windowSize) {
                    action.accept(Arrays.stream((T[]) buffer.toArray(new Object[0])));
                    buffer.poll();
                    return true;
                }
            }

            return false;
        }

        @Override
        public Spliterator<Stream<T>> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return size;
        }

        @Override
        public int characteristics() {
            return ORDERED | NONNULL | SIZED;
        }

        private static int calculateSize(Collection<?> source, int windowSize) {
            return source.size() < windowSize
              ? 0
              : source.size() - windowSize + 1;
        }
    }

}
