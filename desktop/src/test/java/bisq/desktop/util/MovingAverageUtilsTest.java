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

import bisq.desktop.util.MovingAverageUtils;

import java.util.Collection;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.function.Supplier;

import org.junit.Test;
import org.junit.Assert;

public class MovingAverageUtilsTest {

    private static final int NAN = -99;

    private static int[] calcMA(int period, int[] input)
    {
        System.out.println("Input:");
        System.out.println(Arrays.toString(input));

        Collection<Number> listInput =
          Arrays
          .stream(input)
          .boxed()
          .map( x -> x == NAN ? Double.NaN : x )
          .collect(Collectors.toList());

        int[] output = MovingAverageUtils
          .simpleMovingAverage(listInput, period)
          .mapToInt(x -> Double.isFinite(x) ? (int) Math.round(x) : NAN)
          .toArray();

        System.out.println("Output:");
        System.out.println(Arrays.toString(output));

        return output;
    }

    private static void testMA(int period, int[] input, int[] expected)
    {
        var output = calcMA(period, input);
        Assert.assertArrayEquals(output, expected);
    }

    @Test
    public void normalConditions()
    {
        testMA(
            2,
            new int[]{10,20,30,40},
            new int[]{NAN,15,25,35}
            );
    }

    @Test
    public void inputContainsNaNs0()
    {
        testMA(
            2,
            new int[]{NAN,20,30,40},
            new int[]{NAN,NAN,25,35}
            );
    }

    @Test
    public void inputContainsNaNs1()
    {
        testMA(
            2,
            new int[]{10,NAN,30,40},
            new int[]{NAN,NAN,NAN,35}
            );
    }

    @Test
    public void inputContainsNaNs2()
    {
        testMA(
            2,
            new int[]{10,NAN,NAN,40},
            new int[]{NAN,NAN,NAN,NAN}
            );
    }

    @Test
    public void inputContainsNaNs3()
    {
        testMA(
            2,
            new int[]{10,NAN,30,NAN,40},
            new int[]{NAN,NAN,NAN,NAN,NAN}
            );
    }

    @Test
    public void nonsensicalPeriod0()
    {
        testMA(
            1,
            new int[]{10,20},
            new int[]{10,20}
            );
    }

    @Test(expected = IllegalArgumentException.class)
    public void nonsensicalPeriod1()
    {
      var impossible = new int[]{};
      testMA(
          0,
          new int[]{10,20},
          impossible
          );
    }

    @Test(expected = IllegalArgumentException.class)
    public void nonsensicalPeriod2()
    {
      var impossible = new int[]{};
      testMA(
          -1,
          new int[]{10,20},
          impossible
          );
    }

    @Test
    public void tooLittleData0()
    {
        testMA(
            3,
            new int[]{},
            new int[]{}
            );
    }

    @Test
    public void tooLittleData1()
    {
        testMA(
            3,
            new int[]{10},
            new int[]{NAN}
            );
    }

    @Test
    public void tooLittleData2()
    {
        testMA(
            3,
            new int[]{10,20},
            new int[]{NAN,NAN}
            );
    }
}
