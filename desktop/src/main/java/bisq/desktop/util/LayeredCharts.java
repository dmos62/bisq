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

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.BinaryOperator;

import javafx.scene.layout.StackPane;
import javafx.scene.Node;

import javafx.scene.chart.Axis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.NumberAxis;

public class LayeredCharts {

    /* Only supports charts with Y axes that NumberAxis.
     * Automatically two-way binds Y axes together. X axes binding is not implemented.
     */
	public static <X,Y extends Number> StackPane layerCharts(final XYChart<X,Y> ... chartsArray) {
        var charts = Arrays.asList(chartsArray);

        // The first passed chart is the main one, onto which the rest are overlaid.
        charts
            .stream()
            .skip(1)
            .forEach(chart -> configureOverlayChart(chart));

        var yAxes = charts
            .stream()
            .map(chart -> chart.getYAxis())
            .collect(Collectors.toList());

        bindAxesBounds(yAxes);

		StackPane stackpane = new StackPane();
		stackpane.getChildren().addAll(charts);

		return stackpane;
	}

    private static <Y extends Number> void bindAxesBounds(final List<Axis<Y>> axes)
    {
        Function<Axis<Y>,NumberAxis> castToNumberAxis =
            (axis) -> (NumberAxis) axis;

        BinaryOperator<NumberAxis>
            bindTwoAxes = (axis1, axis2) ->
            {
                axis1.autoRangingProperty().bindBidirectional(axis2.autoRangingProperty());
                axis1.lowerBoundProperty().bindBidirectional(axis2.lowerBoundProperty());
                axis1.upperBoundProperty().bindBidirectional(axis2.upperBoundProperty());
                axis1.tickUnitProperty().bindBidirectional(axis2.tickUnitProperty());
                return axis1;
            };

        axes
            .stream()
            .map(castToNumberAxis)
            .reduce(bindTwoAxes);
    }

	private static <X,Y> void configureOverlayChart(final XYChart<X,Y> chart) {
		chart.setAlternativeRowFillVisible(false);
		chart.setAlternativeColumnFillVisible(false);
		chart.setHorizontalGridLinesVisible(false);
		chart.setVerticalGridLinesVisible(false);
		chart.getXAxis().setVisible(false);
		chart.getYAxis().setVisible(false);

        // The rest of the method is a workaround to set CSS properties
        // that don't have setters without using a CSS file.

        // TODO
        var chartBackground = chart.lookup(".chart-plot-background");
        /*
        var children = mapCssClassesToNodes(chart.getPlotChildren());

        var cssClassOfChartBackground = "chart-plot-background";
        var chartBackground = children.get(cssClassOfChartBackground);
        */

        var cssRuleToMakeBackgroundTransparent = "-fx-background-color: transparent";
        chartBackground.setStyle(cssRuleToMakeBackgroundTransparent);
	}

    private static Map<String,Node> mapCssClassesToNodes(List<Node> nodes) {
        var map = new HashMap<String,Node>();
        for (Node node : nodes) {
            var cssClasses = node.getStyleClass();
            for (String cssClass : cssClasses) {
                map.put(cssClass, node);
            }
        }
        return map;
    }

}
