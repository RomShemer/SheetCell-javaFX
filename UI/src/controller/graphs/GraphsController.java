package controller.graphs;

import controller.AppController;
import controller.api.CellSelectionListener;
import controller.api.CellUnselectionListener;
import controller.skinStyle.SkinManager;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import logicStructure.sheet.DTO.RangeDTO;
import logicStructure.sheet.DTO.SheetDTO;
import logicStructure.sheet.DTO.SheetMapper;

import java.util.ArrayList;
import java.util.List;

public class GraphsController implements CellSelectionListener {

    public static enum ChartType {
        BAR("Bar"),
        LINE("Line"),
        SCATTER("Scatter"),
        PIE("Pie");

        private String type;

        private ChartType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }
    }
    @FXML private TextField xAxisRange;
    @FXML private TextField yAxisRange;
    @FXML private Button selectXRange;
    @FXML private Button selectYRange;
    @FXML private Button barChartButton;
    @FXML private ImageView barImageViewer;
    @FXML private Button lineChartButton;
    @FXML private ImageView lineImageViewer;
    @FXML private Button scatterChartButton;
    @FXML private ImageView scatterImageViewer;
    @FXML private Button pieChartButton;
    @FXML private ImageView pieImageViewer;
    @FXML private Button cancelButton;
    @FXML private Button createGraphButton;

    private RangeDTO xRage;
    private RangeDTO yRage;
    private List<Number> xRangeNumericValues = new ArrayList<>();
    private List<String> xRangeCategoryValues = new ArrayList<>();

    private List<Number> yRangeValues = new ArrayList<>();
    private XYChart<?, ?> chart;
    private PieChart pieChart;
    private ChartType chartType = null;
    private SheetDTO currentSheet = null;
    private AppController mainController;
    private CellUnselectionListener cellUnselectionListener;
    private boolean isProgrammaticChange = false;
    private int counter = 0;

    @FXML
    private void initialize() {
        barImageViewer.setImage(new Image(getClass().getResource("/images/bar.png").toExternalForm()));
        lineImageViewer.setImage(new Image(getClass().getResource("/images/liner.png").toExternalForm()));
        scatterImageViewer.setImage(new Image(getClass().getResource("/images/scatter.jpg").toExternalForm()));
        pieImageViewer.setImage(new Image(getClass().getResource("/images/pie.png").toExternalForm()));

        cancelButton.setOnAction(event -> closePopUp());
        createGraphButton.setOnAction(event -> createGraph());
        selectXRange.setOnAction(event -> selectXRange());
        selectYRange.setOnAction(event -> selectYRange());
    }

    public void setMainController(AppController mainController) {
        this.mainController = mainController;
    }

    public void setSheet(SheetDTO sheet) {
        currentSheet = sheet;
    }

    @Override
    public void onCellSelection(String startCell, String endCell, CellUnselectionListener cellUnselectionListener){
        String startRange = startCell;
        String endRange = endCell;
        this.cellUnselectionListener = cellUnselectionListener;

        if(startRange!=null && endRange!=null) {
            isProgrammaticChange = true;
            String rangeSelection = startRange + ":" + endRange;
            if(xAxisRange.getText().isEmpty()){
                xAxisRange.setText(rangeSelection);
                selectXRange();
            } else if(yAxisRange.getText().isEmpty()){
                yAxisRange.setText(rangeSelection);
                selectYRange();
            } else {
                if(counter % 2 == 0){
                    xAxisRange.setText(rangeSelection);
                    selectXRange();
                } else if(counter % 2 == 1){
                    yAxisRange.setText(rangeSelection);
                    selectYRange();
                }
                counter +=1;
            }
        }
    }

    private void selectXRange() {
        this.xRage = splitRangeTextFieldAction(xAxisRange.getText());
        if (xRage == null) {
            showRangeSelectionWarning("Please select a valid range of cells for X aixs");
        }

        this.xRangeNumericValues = createNumericRangeValuesList(this.xRage);
        if(chartType != null && (chartType == chartType.BAR || chartType == chartType.PIE)) {
            this.xRangeCategoryValues = createCategoryRangeValuesList(this.xRage);

            if (this.xRangeNumericValues == null || this.xRangeCategoryValues == null) {
                AppController.showError("An error occurred while selecting a range of cells for X aixs, please try again");
            }
        } else if(this.xRangeNumericValues == null){
                AppController.showError("An error occurred while selecting a range of cells for X aixs, please try again");
        }
    }

    private void selectYRange() {
        this.yRage = splitRangeTextFieldAction(yAxisRange.getText());

        if (yRage == null) {
            showRangeSelectionWarning("Please select a valid range of cells for Y aixs");
        }

        this.yRangeValues = createNumericRangeValuesList(this.yRage);

        if (this.yRangeValues == null) {
            AppController.showError("An error occurred while selecting a range of cells for Y aixs, please try again");
        }
    }

    private List<Number> createNumericRangeValuesList(RangeDTO range) {
        List<Number> rangeValues = new ArrayList<>();

        if(currentSheet == null){
            return null;
        }

        for (int row=range.getStartCoordinate().getRow() ; row<=range.getEndCoordinate().getRow() ; row++) {
            for (int col= range.getStartCoordinate().getColumn(); col<=range.getEndCoordinate().getColumn() ; col++ ){
               String value = currentSheet.getCells().get(SheetMapper.toCoordinateDTO(row,col)).getEffectiveValue();
               Number numberValue = convertToNumeric(value);

               if(numberValue != null){
                   rangeValues.add(numberValue);
               } else {
                   rangeValues.add(Double.NaN);
               }
            }
        }

        return rangeValues;
    }

    private List<String> createCategoryRangeValuesList(RangeDTO range) {
        List<String> rangeValues = new ArrayList<>();

        if(currentSheet == null){
            return null;
        }

        for (int row=range.getStartCoordinate().getRow() ; row<=range.getEndCoordinate().getRow() ; row++) {
            for (int col= range.getStartCoordinate().getColumn(); col<=range.getEndCoordinate().getColumn() ; col++ ){
                String value = currentSheet.getCells().get(SheetMapper.toCoordinateDTO(row,col)).getEffectiveValue();
                rangeValues.add(value);
            }
        }

        return rangeValues;
    }

    public static Number convertToNumeric(String value) {
        if (value == null) {
            return null;
        }
        try {
           return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }


    private RangeDTO splitRangeTextFieldAction(String rangeSelection){
        String startRange, endRange;

        if (rangeSelection.contains(":") && rangeSelection.split(":").length == 2) {
            String[] split = rangeSelection.split(":");
            startRange = split[0];
            endRange = split[1];
            return SheetMapper.toRangeDTO(startRange, endRange);
        } else if(!rangeSelection.isEmpty()){
            AppController.showError("Invalid range selection!");
        }

        return null;
    }

    private void showRangeSelectionWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message);
        alert.setTitle("Selection");
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private boolean validateRangeForChartCreation() {
        if (xRage == null) {
            showRangeSelectionWarning("Please select a valid range of numeric cells for X aixs");
            return false;
        }
        if (yRage == null) {
            showRangeSelectionWarning("Please select a valid range of numeric cells for Y aixs");
            return false;
        }

        return true;
    }

    private void createGraph() {
        Stage chartStage = new Stage();
        if(chartType.equals(ChartType.PIE)){
            PieChart copiedChart= copyChart(pieChart);
            if(mainController.getIsWithAnimations()){
                Pane pane = new Pane(copiedChart);
                graphAnimationPieChart(copiedChart, pane);
                mainController.showStageInNewTab(pane, "Generated " + chartType.toString() + " Graph");
            } else {
                Pane pane = displayPieChartWithPercentages(copiedChart);
                mainController.showStageInNewTab(pane, "Generated " + chartType.toString() + " Graph");
            }
            return;
        }

        XYChart<?,?> copiedChart = copyChart(chart);
        if(mainController.getIsWithAnimations()){
            graphAnimationXYChart(copiedChart);
        }
        VBox vbox = new VBox(copiedChart);
        mainController.showStageInNewTab(vbox, "Generated " + chartType.toString() + " Graph");
    }

    private void graphAnimationXYChart(XYChart<?,?> copiedChart){
        for (XYChart.Data<?,?> data : copiedChart.getData().get(0).getData()) {
            TranslateTransition tt = new TranslateTransition(Duration.millis(1000), data.getNode());
            tt.setFromY(200);
            tt.setToY(0);
            tt.play();
        }
    }

    private void graphAnimationPieChart(PieChart copiedChart, Pane pane) {
        for (PieChart.Data data : copiedChart.getData()) {
            ScaleTransition st = new ScaleTransition(Duration.millis(1000), data.getNode());
            st.setFromX(0);
            st.setFromY(0);
            st.setToX(1);
            st.setToY(1);

            st.setOnFinished(event -> {
                Platform.runLater(() -> {
                    Bounds bounds = data.getNode().getBoundsInParent();
                    double centerX = bounds.getMinX() + bounds.getWidth() / 2;
                    double centerY = bounds.getMinY() + bounds.getHeight() / 2;

                    String text = String.format("%.1f%%", 100 * data.getPieValue() / copiedChart.getData().stream().mapToDouble(PieChart.Data::getPieValue).sum());
                    Label caption = new Label(text);
                    caption.getStyleClass().add("pie-percentage");

                    StackPane stackPane = new StackPane(caption);
                    stackPane.getStyleClass().add("stack-pane");
                    stackPane.setMouseTransparent(true);

                    stackPane.setLayoutX(centerX - caption.getWidth() / 2);
                    stackPane.setLayoutY(centerY - caption.getHeight() / 2);

                    pane.getChildren().add(stackPane);
                });
            });

            st.play();
        }
    }

    private PieChart copyChart(PieChart originalChart) {
        PieChart copiedChart = new PieChart();

        for (PieChart.Data data : originalChart.getData()) {
            PieChart.Data copiedData = new PieChart.Data(data.getName(), data.getPieValue());
            copiedChart.getData().add(copiedData);
        }

        copiedChart.setTitle(originalChart.getTitle());

        copiedChart.setLegendVisible(originalChart.isLegendVisible());
        copiedChart.setStartAngle(originalChart.getStartAngle());
        copiedChart.setClockwise(true);

        return copiedChart;
    }

    private <X, Y> XYChart<X, Y> copyChart(XYChart<X, Y> chart) {
        Axis<X> xAxisCopy = copyAxis(chart.getXAxis());
        Axis<Y> yAxisCopy = copyAxis(chart.getYAxis());

        XYChart<X, Y> copiedChart;
        if (chart instanceof LineChart) {
            copiedChart = new LineChart<>(xAxisCopy, yAxisCopy);
        } else if (chart instanceof BarChart) {
            copiedChart = new BarChart<>(xAxisCopy, yAxisCopy);
        } else if (chart instanceof ScatterChart) {
            copiedChart = new ScatterChart<>(xAxisCopy, yAxisCopy);
        } else {
            throw new IllegalArgumentException("Unsupported chart type");
        }

        for (XYChart.Series<X, Y> series : chart.getData()) {
            XYChart.Series<X, Y> copiedSeries = new XYChart.Series<>();
            copiedSeries.setName(series.getName());

            for (XYChart.Data<X, Y> data : series.getData()) {
                XYChart.Data<X, Y> copiedData = new XYChart.Data<>(data.getXValue(), data.getYValue());
                copiedSeries.getData().add(copiedData);
            }

            copiedChart.getData().add(copiedSeries);
        }

        copiedChart.setTitle(chart.getTitle());
        copiedChart.getXAxis().setLabel(chart.getXAxis().getLabel());
        copiedChart.getYAxis().setLabel(chart.getYAxis().getLabel());

        copiedChart.getStylesheets().add(SkinManager.getCurrentSkinMode().getGraph());
        return copiedChart;
    }

    private <T> Axis<T> copyAxis(Axis<T> axis) {
        Axis<T> copiedAxis;

        if (axis instanceof NumberAxis) {
            copiedAxis = (Axis<T>) new NumberAxis();
        } else if (axis instanceof CategoryAxis) {
            copiedAxis = (Axis<T>) new CategoryAxis();
        } else {
            throw new IllegalArgumentException("Unsupported axis type");
        }

        copiedAxis.setLabel(axis.getLabel());
        copiedAxis.setTickLabelFill(axis.getTickLabelFill());
        copiedAxis.setAutoRanging(axis.isAutoRanging());

        return copiedAxis;
    }

    private void closePopUp() {
        clearPrevInfo();
        cellUnselectionListener.removeFromOnCellSelectionListener(this);
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    public void clearPrevInfo(){
        xRage = null;
        yRage = null ;
        xAxisRange.clear();
        yAxisRange.clear();
        xRangeNumericValues.clear();
        xRangeCategoryValues.clear();
        yRangeValues.clear();
        chart = null;
        pieChart = null;
        chartType = null;
        isProgrammaticChange = false;
        counter = 0;
        cellUnselectionListener.removeFromOnCellSelectionListener(this);
    }

    public void handlePieChartAction(ActionEvent actionEvent) {
        chartType = ChartType.PIE;
        selectXRange();
        selectYRange();
        boolean isValidRanges = validateRangeForChartCreation();

        if(!isValidRanges){
            return;
        }

        int axisLength = Math.min(xRangeCategoryValues.size(), yRangeValues.size());
        PieChart pieChart = new PieChart();
        NumberAxis yAxis = new NumberAxis();

        for (int i = 0; i < axisLength; i++) {
            PieChart.Data data = new PieChart.Data(xRangeCategoryValues.get(i), yRangeValues.get(i).doubleValue());
            data.setName(String.format("%s : %.2f", data.getName(), yRangeValues.get(i).doubleValue()));
            pieChart.getData().add(data);
        }

        pieChart.setClockwise(true);
        pieChart.getStylesheets().add(SkinManager.getCurrentSkinMode().getGraph());

        this.pieChart = pieChart;

    }

    private Pane displayPieChartWithPercentages(PieChart pieChart) {
        Pane pane = new Pane();
        pane.getChildren().add(pieChart);
        double total = pieChart.getData().stream().mapToDouble(PieChart.Data::getPieValue).sum();

        for (final PieChart.Data data : pieChart.getData()) {
            String text = String.format("%.1f%%", 100*data.getPieValue()/total) ;
            Label caption = new Label(text);
            caption.getStyleClass().add("pie-percentage");

            StackPane stackPane = new StackPane(caption);
            stackPane.getStyleClass().add("stack-pane");
            stackPane.setMouseTransparent(true);

            Platform.runLater(() -> {
                Bounds bounds = data.getNode().getBoundsInParent(); // שימוש ב-Bounds לאחר רינדור
                double centerX = bounds.getMinX() + bounds.getWidth() / 2;
                double centerY = bounds.getMinY() + bounds.getHeight() / 2;

                stackPane.setLayoutX(centerX - caption.getWidth() / 2);
                stackPane.setLayoutY(centerY - caption.getHeight() / 2);
            });


            pane.getChildren().add(stackPane);
        }
        return pane;
    }

    public void handleScatterChartAction(ActionEvent actionEvent) {
        chartType = ChartType.SCATTER;
        selectXRange();
        selectYRange();

        boolean isValidRanges = validateRangeForChartCreation();

        if (!isValidRanges) {
            return;
        }

        int axisLength = Math.min(xRangeNumericValues.size(), yRangeValues.size());
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        ScatterChart<Number, Number> scatterChart = new ScatterChart<>(xAxis, yAxis);

        xAxis.setLabel("X Axis-"+xAxisRange.getText());
        yAxis.setLabel("Y Axis-"+yAxisRange.getText());

        XYChart.Series<Number, Number> series1 = new XYChart.Series<>();
        series1.setName("Data Set 1");
        XYChart.Series<Number, Number> NaNSeries = new XYChart.Series<>();
        NaNSeries.setName("NaN Values");

        for (int i = 0; i < axisLength; i++) {
            if(yRangeValues.get(i).equals(Double.NaN)){
                XYChart.Data<Number, Number> data = new XYChart.Data<>(xRangeNumericValues.get(i), 5);
                NaNSeries.getData().add(data);

            } else {
                XYChart.Data<Number, Number> data = new XYChart.Data<>(xRangeNumericValues.get(i), yRangeValues.get(i));
                series1.getData().add(data);
            }
        }

        scatterChart.getData().add(series1);
        scatterChart.getData().add(NaNSeries);
        scatterChart.getStylesheets().add(SkinManager.getCurrentSkinMode().getGraph());
        this.chart = scatterChart;
    }

    public void handleLineChartAction(ActionEvent actionEvent) {
        chartType = ChartType.LINE;
        selectXRange();
        selectYRange();
        boolean isValidRanges = validateRangeForChartCreation();

        if (!isValidRanges) {
            return;
        }

        int axisLength = Math.min(xRangeNumericValues.size(), yRangeValues.size());
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);

        xAxis.setLabel("X Axis-"+xAxisRange.getText());
        yAxis.setLabel("Y Axis-"+yAxisRange.getText());

        XYChart.Series<Number, Number> series1 = new XYChart.Series<>();
        series1.setName("Data Set 1");
        XYChart.Series<Number, Number> NaNSeries = new XYChart.Series<>();
        NaNSeries.setName("NaN Values");

        for (int i = 0; i < axisLength; i++) {
            if(yRangeValues.get(i).equals(Double.NaN)){
                XYChart.Data<Number, Number> data = new XYChart.Data<>(xRangeNumericValues.get(i), 5);
                NaNSeries.getData().add(data);

            } else {
                XYChart.Data<Number, Number> data = new XYChart.Data<>(xRangeNumericValues.get(i), yRangeValues.get(i));
                series1.getData().add(data);
            }
        }

        lineChart.getData().add(series1);
        lineChart.getData().add(NaNSeries);
        lineChart.getStylesheets().add(SkinManager.getCurrentSkinMode().getGraph());
        this.chart = lineChart;
    }

    public void handleBarChartAction(ActionEvent actionEvent) {
        chartType = ChartType.BAR;
        selectXRange();
        selectYRange();
        boolean isValidRanges = validateRangeForChartCreation();

        if (!isValidRanges) {
            return;
        }

        int axisLength = Math.min(xRangeCategoryValues.size(), yRangeValues.size());
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);

        xAxis.setLabel("X Axis-"+xAxisRange.getText());
        yAxis.setLabel("Y Axis-"+yAxisRange.getText());

        XYChart.Series<String, Number> series1 = new XYChart.Series<>();
        series1.setName("Data Set 1");
        XYChart.Series<String, Number> NaNSeries = new XYChart.Series<>();
        NaNSeries.setName("NaN Values");

        for (int i = 0; i < axisLength; i++) {
            if(yRangeValues.get(i).equals(Double.NaN)){
                XYChart.Data<String, Number> data = new XYChart.Data<>(xRangeCategoryValues.get(i), 5);
                NaNSeries.getData().add(data);

            } else {
                XYChart.Data<String, Number> data = new XYChart.Data<>(xRangeCategoryValues.get(i), yRangeValues.get(i));
                series1.getData().add(data);
            }
        }

        barChart.getData().add(series1);
        barChart.getData().add(NaNSeries);
        barChart.getStylesheets().add(SkinManager.getCurrentSkinMode().getGraph());

        this.chart = barChart;
    }
}
