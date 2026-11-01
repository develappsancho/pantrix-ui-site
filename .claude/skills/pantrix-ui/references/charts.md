# Charts

Which chart a set of data takes, and what every chart shares: its series read from the app's own rows, its parts, its
state, a screen reader's reading and its tags. Each chart has a page of its own with every example: `chart`,
`line-chart`, `bar-chart`, `pie-chart`, `gantt-chart` and the rest.

## Which chart

| Data | Chart |
| :--- | :--- |
| Values along a run of places or moments: sales by month | `PantrixLineChart`, `PantrixAreaChart`, `PantrixBarChart`; lines over bars, or a second axis, in `PantrixChart` |
| What lies between two values at each place: a day's least and most | `PantrixRangeChart` |
| How an amount comes about, step by step | `PantrixWaterfallChart` |
| Open, high, low and close at each place | `PantrixCandlestickChart` |
| The parts of a whole | `PantrixPieChart` (a ring with `hole`), `PantrixMeterGroup` on one bar, `PantrixTreemap` as tiles |
| One reading on a scale | `PantrixGauge` |
| A known amount of work done | `PantrixProgressCircle` |
| A run of steps, each in a state | `PantrixStatusTracker` |
| The run of a number in the room of a word | `PantrixSparkline` |
| Points set out by two values, bubbles by a third | `PantrixScatterChart` |
| Several measures of one thing | `PantrixRadarChart` |
| Bars round a middle: a value by direction or by month | `PantrixPolarChart` |
| A grid of values: hours by weekdays | `PantrixHeatmap` |
| Stages fewer and fewer get through | `PantrixFunnelChart` |
| What flows from where to where | `PantrixSankeyChart` |
| The tasks of a plan along time | `PantrixGanttChart` |

## Series from the app's rows

A chart reads the app's own rows: no row is copied into a type of the library. `x` gives a row's place, a name or a
moment, and each value's `y` its number, or null where it has none.

```kotlin
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pantrixui.components.chart.cartesian.ChartValue
import com.pantrixui.components.chart.line.PantrixLineChart
import org.jetbrains.compose.resources.stringResource

class MonthSales(val month: String, val online: Double, val stores: Double?)

@Composable
fun SalesChart(sales: List<MonthSales>) {
    PantrixLineChart(
        data = sales,
        x = { it.month },
        values = listOf(
            ChartValue(key = "online", name = stringResource(Res.string.online)) { it.online },
            ChartValue(key = "stores", name = stringResource(Res.string.stores)) { it.stores }
        ),
        modifier = Modifier.fillMaxWidth(),
        description = stringResource(Res.string.sales_chart_description),
        tagKey = "sales"
    )
}
```

- **Series of different kinds** go in `PantrixChart`: a `LineSeries` and a `BarSeries` each take their own `data`, `x`
  and `y`. Series that name the same `stack` stand on one another; `axis = ChartYAxis.Second` draws one by a scale of
  its own.
- **Moments.** Places that are all `kotlin.time.Instant`s are set out by time, on an axis marked at round moments,
  with no date library.
- **A series' key** is the same in every language: its legend entry is tagged by it.

## What every chart shares

- **Parts.** `xAxis`, `yAxis`, `legend` and `tooltip` are given by the value that describes them (`ChartAxis`,
  `ChartLegend`, `ChartTooltip`) and taken away by `null`. `marks` draws a line or a band to read the series against.
- **Numbers** are written in the theme's language: `12K` beside an axis, in full in the tooltip; `format` writes them
  another way.
- **State.** `rememberChartState()` holds the series the reader has hidden and the place pointed at; two charts given
  one state act as one.
- **Pointing.** A pointer, a finger and the keyboard point at a place; the chart is one Tab stop and the arrow keys go
  from place to place. `onSelect` is called with the value pressed.
- **Zoom.** `zoom = ChartZoom()` shows a part of the places and moves along the rest, by the wheel with Ctrl or
  Command, two fingers, or plus and minus.
- **Export.** `export = ChartExport(onExport = …)` puts an export button among the chart's own: PNG, SVG or PDF, of the
  chart as it is on the screen. Where the file goes is the app's to say.
- **Many points.** A line of more than two points to a pixel is thinned by `decimation`, `ChartDecimation()` by
  default.
- **Colours.** A series given no colour takes the theme's `chart` colours in turn. `ChartStyle(patterns = true)` tells
  series apart by textures and dashes as well, and is on by itself where the system asks for it.
- **Screen reader.** `description` is said first, then each place with its values: every chart is given one.
- **Tags.** The charts drawn on `PantrixChart` (line, area, range, bar, waterfall, candlestick) are tagged
  `Chart_<tagKey>`, a place `Chart_<tagKey>.Point_<number>` and a legend entry `Chart_<tagKey>.Legend_<the series'
  key>`. The others go by their own names: `PieChart_<tagKey>`, `Gauge_<tagKey>`, `Gantt_<tagKey>`
  ([components.md](components.md) lists each).
- **Size.** A chart fills the width it is given; a height given with `modifier` is the whole chart's.
