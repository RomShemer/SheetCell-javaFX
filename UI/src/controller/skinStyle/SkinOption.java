package controller.skinStyle;

import java.net.URL;

public enum SkinOption {
    DEFAULT("Default"),
    DARK_MODE("DarkMode"),
    MONOCHROME("MonochromeMode"),
    VIBRANT("VibrantMode");

    private final String modeName;
    private final String actionLine;
    private final String addRange;
    private final String commands;
    private final String expression;
    private final String filterPopUp;
    private final String filterListViewItem;
    private final String progressBar;
    private final String ranges;
    private final String sheet;
    private final String sortAndFilter;
    private final String sortPopUp;
    private final String topBar;
    private final String updateValue;
    private final String systemMessage;
    private final String tabelView;
    private final String toolTip;
    private final String tabs;
    private final String graph;


    SkinOption(String modeName) {
        this.modeName = modeName;
        this.actionLine = "/skinStyle/" + modeName +"/actionLine.css";;
        this.addRange = "/skinStyle/" + modeName +"/addRange.css";
        this.commands = "/skinStyle/" + modeName +"/commands.css";
        this.expression = "/skinStyle/" + modeName + "/expression.css";
        this.filterPopUp = "/skinStyle/" + modeName + "/filterPopUp.css";
        this.filterListViewItem = "/skinStyle/" + modeName + "/filterListViewItem.css";
        this.progressBar = "/skinStyle/" + modeName + "/progressBar.css";
        this.ranges = "/skinStyle/" + modeName + "/ranges.css";
        this.sheet = "/skinStyle/" + modeName + "/sheet.css";
        this.sortAndFilter = "/skinStyle/" + modeName + "/sortAndFilter.css";
        this.sortPopUp = "/skinStyle/" + modeName + "/sortPopUp.css";
        this.topBar = "/skinStyle/" + modeName + "/topBar.css";
        this.updateValue = "/skinStyle/" + modeName + "/updateValue.css";
        this.systemMessage = "/skinStyle/" + modeName + "/systemMessage.css";
        this.tabelView = "/skinStyle/" + modeName + "/tabelView.css";
        this.toolTip = "/skinStyle/" + modeName + "/toolTip.css";
        this.tabs = "/skinStyle/" + modeName + "/tabs.css";
        this.graph = "/skinStyle/" + modeName + "/graph.css";
    }

    public String getModeName() {
        return modeName;
    }

    public String getActionLine() {
        return getClass().getResource(actionLine).toExternalForm();
    }

    public String getAddRange() {
        return getClass().getResource(addRange).toExternalForm();
    }

    public String getCommands() {
        return getClass().getResource(commands).toExternalForm();
    }

    public String getExpression() {
        return getClass().getResource(expression).toExternalForm();
    }

    public String getFilterPopUp() {
        return getClass().getResource(filterPopUp).toExternalForm();
    }

    public String getFilterListViewItem() {
        return getClass().getResource(filterListViewItem).toExternalForm();
    }

    public String getProgressBar() {
        return getClass().getResource(progressBar).toExternalForm();
    }

    public String getRanges() {
        return getClass().getResource(ranges).toExternalForm();
    }

    public String getSheet() {
        return getClass().getResource(sheet).toExternalForm();
    }

    public String getSortAndFilter() {
        return getClass().getResource(sortAndFilter).toExternalForm();
    }

    public String getSortPopUp() {
        return getClass().getResource(sortPopUp).toExternalForm();
    }

    public String getTopBar() {
        return getClass().getResource(topBar).toExternalForm();
    }

    public String getUpdateValue() {
        return getClass().getResource(updateValue).toExternalForm();
    }

    public String getSystemMessage() {
        return getClass().getResource(systemMessage).toExternalForm();
    }

    public String getTabelView() {
        return getClass().getResource(tabelView).toExternalForm();
    }

    public String getToolTip() {
        return getClass().getResource(toolTip).toExternalForm();
    }

    public String getTabs(){
        return getClass().getResource(tabs).toExternalForm();
    }

    public String getGraph(){
        return getClass().getResource(graph).toExternalForm();
    }
}
