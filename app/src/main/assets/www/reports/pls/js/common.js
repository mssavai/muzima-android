//Global variables

var sideMenuChanged = false;
var reloadedPageId = "";
var reloadedPageIdCount = 0;

const onsnavigator = document.querySelector('#onsnavigator');
document.loadPage = function (pageToLoad) {
    if (pageToLoad != null && pageToLoad != '') {
        onsnavigator.resetToPage(pageToLoad);
    }

    var sideMenuElement = document.getElementById("menu");
    if (pageToLoad == "login.html") {
        if (sideMenuElement.hasAttribute("swipeable")) {
            sideMenuElement.removeAttribute("swipeable");
        }
    } else if (!sideMenuElement.hasAttribute("swipeable")) {
        sideMenuElement.setAttribute("swipeable");
    }

    if (document.querySelector('ons-splitter-side').isOpen) {
        document.querySelector('ons-splitter-side').close();
    }
}

const setCurrentReportPeriodAsDaily = function(){
    sideMenuChanged = true;
    loggingInterface.setCurrentReportPeriodAsDaily();
}

const setCurrentReportPeriodAsWeekly = function(){
    sideMenuChanged = true;
    loggingInterface.setCurrentReportPeriodAsWeekly();
}

const setCurrentReportPeriodAsMonthly = function(){
    sideMenuChanged = true;
    loggingInterface.setCurrentReportPeriodAsMonthly();
}

const openMenu = function () {
    var menu = document.getElementById('menu');
    menu.open();
};

const refreshReportPeriodDisplayString = function () {
    var reportPeriodString = loggingInterface.getReportPeriodDisplayString();
    var labels = document.getElementsByClassName('reportPeriodLabel');
    for (i = 0; i < labels.length; i++) {
        labels[i].innerHTML = reportPeriodString;
    }

    var buttons = document.getElementsByClassName('incrementReportPeriodButton');
    if (loggingInterface.getReportPeriodOffset() == 0) {
        for (i = 0; i < buttons.length; i++) {
            buttons[i].setAttribute("disabled", "disabled");
        }
    } else {
        for (i = 0; i < buttons.length; i++) {
            buttons[i].removeAttribute("disabled");
        }
    }
}

document.querySelector('ons-splitter-side').addEventListener('postclose', function () {
    if (sideMenuChanged) {
        reloadedPageIdCount=0;
        reloadedPageId = "";
        sideMenuChanged=false;
        loggingInterface.reloadCurrentPage();
    }
});

document.addEventListener('init', function(event) {
    var activeTab = loggingInterface.getActiveTab();
    if (activeTab > -1 && reloadedPageIdCount<3) {
        var currentPage = loggingInterface.getCurrentPage();
        var currentPageId = "";
        switch(currentPage){
            case "encounter_length.html": currentPageId = "#encounterlength-onstab"; break;
            case "patients_seen.html": currentPageId = "#patientsseen-onstab"; break;
            case "work_day_length.html": currentPageId = "#workdaylength-onstab"; break;
        }
        if(currentPageId != "" && (reloadedPageId == "" || reloadedPageId == currentPageId)) {
            var tabbar = document.querySelector(currentPageId);
            if (tabbar != null) {
                tabbar.setActiveTab(activeTab);
                reloadedPageId = currentPageId;
                reloadedPageIdCount++;
            }
        }
    }
});

var savedSelectedProviderJsonString = loggingInterface.getSelectedProviders();
const selectedProviders = JSON.parse(savedSelectedProviderJsonString);

const toggleSelectedProvider = function (value) {
    var index = selectedProviders.indexOf(value);
    if (index > -1) {
        selectedProviders.splice(index, 1);
    } else {
        selectedProviders.push(value);
    }
    loggingInterface.saveSelectedProviders(JSON.stringify(selectedProviders));
    sideMenuChanged = true;
}

const addSelectedProvider = function (value) {
    var index = selectedProviders.indexOf(value);
    if (index == -1) {
        selectedProviders.push(value);
        loggingInterface.saveSelectedProviders(JSON.stringify(selectedProviders));
    }
}

const isProviderSelected = function (value) {
    return selectedProviders.indexOf(value) > -1;
}

const getChartData = function (chartType) {
    var chartData = loggingInterface.getChartData(chartType);
    chartData = JSON.parse(chartData);

    return chartData;
}

const getChartDataDates = function () {
    var dates = loggingInterface.getChartDataDates();
    return JSON.parse(dates);
}

const setUpProviderList = function() {
    var systemStatistics = ["average", "expected"];
console.log("in setUpProviderList");
    var providerSelectorElement = document.getElementById("providerSelection");

    var isSupervisor = loggingInterface.isCurrentUserSupervisor();
    var providerListSelectionHtml = "";
    var providers = loggingInterface.getProviders();
    providers = JSON.parse(providers);
    providers.forEach(function (provider) {
        if (systemStatistics.indexOf(provider.id) > -1) {
            addSelectedProvider(provider.id);
        } else {
            if (isSupervisor) {
                var checked = isProviderSelected(provider.id) || provider.isLoggedIn === "true" ? "checked" : "";
                if (checked === "checked") {
                    addSelectedProvider(provider.id);
                }
                providerListSelectionHtml += "<ons-list-item tappable>" +
                    "<label class=\"left\">" +

                    "<ons-checkbox " + checked + " name=\"provider\"" +
                    " onchange=\"toggleSelectedProvider('" + provider.id + "')\"" +
                    " input-id=\"" + provider.id + "\"" +
                    "></ons-checkbox>" +
                    provider.full_name + " (" + provider.id + ")" +
                    "</label>" +
                    "</ons-list-item>";
            } else if (provider.isLoggedIn === "true") {
                addSelectedProvider(provider.id);
            }
        }
    });

    if (!isSupervisor) {
        providerSelectorElement.style.display = 'none';
    }

    var providerListElement = document.getElementById("providerList");
    providerListElement.innerHTML = providerListSelectionHtml;
    providerSelectorElement.style.display = 'show';
};

setUpProviderList();


const extractDataSets = function (chartData) {
    var datasets = [];
    for (var k in chartData) {
        var v = chartData[k];
        if (isProviderSelected(v.id)) {
            var dataset = {};

            dataset.label = k;
            dataset.id = k;
            dataset.backgroundColor = "rgba(" + v.color + ", 0.2)";
            dataset.tableBackgroundColor = "rgba(" + v.color + ", 0.4)";
            dataset.borderColor = "rgba(" + v.color + ", 1)";
            dataset.borderWidth = 1;

            var is_self_data = v.isLoggedIn === "true";
            if (!is_self_data) {
                dataset.type = "line";
                dataset.fill = "false";
            }

            var data = [];

            labels.forEach(function (label) {
                if (v.data[label] != undefined) {
                    data.push(v.data[label]);
                } else {
                    data.push(0);
                }

            });

            dataset.data = data;

            if (dataset.id == "average" || dataset.id == "expected") {
                if (datasets.length > 0 && datasets[0].id == "expected") {
                    datasets.splice(1, 0, dataset);
                } else {
                    datasets.unshift(dataset);
                }
            } else {
                datasets.push(dataset);
            }
        }
    }
    ;
    return datasets;
}

const createChartDataTable = function (chartType) {
    var chartData = getChartData(chartType);
    var labels = getChartDataDates();
    var datasets = extractDataSets(chartData);

    var html = "";
    var dataLength = datasets[0].data.length;
    var tableHeader = "<tr style='background-color:rgba(120,120,120,0.3)'><th class='sticky-header'></th>";
    labels.forEach(function(label,index){
        if(index < dataLength) {
            tableHeader += "<th class='sticky-header'><b>" + label + "</b></th>";
        }
    });
    tableHeader += "<th class='sticky-header'><b>TOTAL</b></th>";
    tableHeader += "</tr>";
    html +="<thead>"+tableHeader+"</thead><tbody>";

    datasets.forEach(function(dataset){
        var datasetHtml = "<tr style='background-color: " +dataset.tableBackgroundColor+ "'>" +
            "<td><b>" + dataset.label + "</b></td>";

        var total = 0;
        dataset.data.forEach(function (dataItem) {
            datasetHtml += "<td>" + dataItem + "</td>";
            total += dataItem;
        });

        if(total % 1 != 0){
            total = total.toFixed(1);
        }

        datasetHtml += "<td>" + total + "</td></tr>";
        html += datasetHtml;
    });
    html += "</tbody>";
    return html;
}