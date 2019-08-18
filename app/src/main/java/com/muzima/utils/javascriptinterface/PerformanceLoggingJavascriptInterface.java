package com.muzima.utils.javascriptinterface;

import android.util.Log;
import android.webkit.JavascriptInterface;
import com.muzima.MuzimaApplication;
import com.muzima.api.model.LogStatistic;
import com.muzima.controller.MuzimaLogsController;
import com.muzima.util.JsonUtils;
import com.muzima.view.reports.ProviderPerformanceReportViewActivity;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PerformanceLoggingJavascriptInterface {
    private final MuzimaApplication muzimaApplication;
    private final ProviderPerformanceReportViewActivity providerReportViewActivity;

    public static final String CONFIG_COLLECTION_KEY = "config";
    public static final String LOCATIONS_COLLECTION_KEY = "activity_locations";
    public static final String WORK_DAY_LENGTH_COLLECTION_KEY = "work_day_length";
    public static final String ENCOUNTER_LENGTH_COLLECTION_KEY = "average_encounter_length";
    public static final String PATIENTS_SEEN_COLLECTION_KEY = "patients_seen";

    //ToDo: load javascriptAppContext from database
    private  static final JavascriptAppContext javascriptAppContext = new JavascriptAppContext();
    private String activeTab;

    public PerformanceLoggingJavascriptInterface(ProviderPerformanceReportViewActivity providerReportViewActivity){
        this.providerReportViewActivity = providerReportViewActivity;
        this.muzimaApplication = (MuzimaApplication) providerReportViewActivity.getApplicationContext();
        javascriptAppContext.setUserRoleForUsername(providerReportViewActivity.getUsername());
        prepareChartData();
    }

    @JavascriptInterface
    public void loadMapsPage(){
        providerReportViewActivity.loadMapsPage();
    }

    @JavascriptInterface
    public  static boolean isCurrentUserSupervisor(){
        return javascriptAppContext.isSupervisor();
    }

    @JavascriptInterface
    public String getProviders(){
        String config =  ChartDataUtils.getChartData(CONFIG_COLLECTION_KEY);
        JSONArray providers = (JSONArray) JsonUtils.readAsObjectList(config,"$['config']['providers']");
        if(providers!= null) {
            return providers.toJSONString();
        }
        return "[]";
    }

    @JavascriptInterface
    public  boolean isCurrentUserProvider(){
        return javascriptAppContext.isProvider();
    }

    @JavascriptInterface
    public static boolean isLoggedIn(){
        return javascriptAppContext.isLoggedIn();
    }

    @JavascriptInterface
    public static String getUserRole(){
        return javascriptAppContext.getUserRole();
    }

    @JavascriptInterface
    public void setActiveTab(String tab){
        activeTab = tab;
    }

    @JavascriptInterface
    public String getActiveTab(){
        return activeTab;
    }

    @JavascriptInterface
    public void decrementReportPeriodOffset(){
        javascriptAppContext.decrementReportPeriodOffset();
        providerReportViewActivity.reloadCurrentPage();
    }

    @JavascriptInterface
    public void incrementReportPeriodOffset(){
        System.out.println("Incrementing......");
        javascriptAppContext.incrementReportPeriodOffset();
        providerReportViewActivity.reloadCurrentPage();
    }

    @JavascriptInterface
    public int getReportPeriodOffset(){
        return javascriptAppContext.getReportPeriodOffset();
    }
    @JavascriptInterface
    public String getChartData(String chartType){
        return ChartDataUtils.getChartData(chartType);
    }

    public void prepareChartData(){
        MuzimaLogsController logsController = ((MuzimaApplication)providerReportViewActivity.getApplicationContext()).getMuzimaLogsController();
        try {
            List logStatistics = logsController.getAllLogStatistics(muzimaApplication.getAuthenticatedUser());
            LogStatistic config = logsController.getLatestConfig(muzimaApplication.getAuthenticatedUser());
            ChartDataUtils.createChartData(logStatistics,config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public String getChartDataDates(){
        JSONArray labels = new JSONArray();
        int datesOffset = javascriptAppContext.getReportPeriodOffset();
        String mode = "weekly";
        if(javascriptAppContext.isCurrentReportPeriodMonthly()){
            mode = "monthly";
        } else if(javascriptAppContext.isCurrentReportPeriodDaily()){
            mode = "daily";
        }
        System.out.println("getChartDataDates: mode="+mode+" offset="+datesOffset);
        labels.addAll(PerformanceLoggingJavascriptInterface.CalendarUtils.getDatesStringList(datesOffset, mode));
        return labels.toJSONString();
    }
    @JavascriptInterface
    public String getMapData(){
        return getChartData(LOCATIONS_COLLECTION_KEY);
    }

    @JavascriptInterface
    public void downloadStatistics(){
        try {
            muzimaApplication.getMuzimaLogsController().downloadlogStatistics();
        } catch (IOException e) {
            Log.e("TESTING","Test",e);
        }
    }

    @JavascriptInterface
    public  void setNextPageToNavigate(String nextPageId){
        javascriptAppContext.setNextPageToNavigate(nextPageId);
    }

    @JavascriptInterface
    public  void navigateToNextPage(){
        navigateToPage(javascriptAppContext.getNextPageToNavigate());
    }

    @JavascriptInterface
    public void navigateToPage(String pageId){
        providerReportViewActivity.loadPage(pageId, false);
    }

    @JavascriptInterface
    public void navigateToLandingPage(){
        providerReportViewActivity.loadLandingPage();
    }

    @JavascriptInterface
    public void setCurrentReportPeriodAsDaily(){
        javascriptAppContext.setCurrentReportPeriodAsDaily();
    }

    @JavascriptInterface
    public void setCurrentReportPeriodAsWeekly(){
        javascriptAppContext.setCurrentReportPeriodAsWeekly();
    }

    @JavascriptInterface
    public void setCurrentReportPeriodAsMonthly(){
        javascriptAppContext.setCurrentReportPeriodAsMonthly();
    }

    @JavascriptInterface
    public String getCurrentPage(){
        return providerReportViewActivity.getCurrentPage();
    }

    @JavascriptInterface
    public void reloadCurrentPage(){
        providerReportViewActivity.reloadCurrentPage();
    }

    @JavascriptInterface
    public String getReportPeriodDisplayString(){
        if(javascriptAppContext.isCurrentReportPeriodMonthly()){
            int offset = javascriptAppContext.getMonthlyReportPeriodOffset();
            return CalendarUtils.getMonthlyDateRangeString(offset);
        } else if(javascriptAppContext.isCurrentReportPeriodDaily()){
            int offset = javascriptAppContext.getDailyReportPeriodOffset();
            return CalendarUtils.getDailyDateRangeString(offset);
        }
        int offset = javascriptAppContext.getWeeklyReportPeriodOffset();
        return CalendarUtils.getWeeklyDateRangeString(offset);
    }

    @JavascriptInterface
    public boolean isCurrentReportPeriodDaily(){
        return javascriptAppContext.isCurrentReportPeriodDaily();
    }

    @JavascriptInterface
    public boolean isCurrentReportPeriodWeekly(){
        return javascriptAppContext.isCurrentReportPeriodWeekly();
    }

    @JavascriptInterface
    public boolean isCurrentReportPeriodMonthly(){
        return javascriptAppContext.isCurrentReportPeriodMonthly();
    }

    @JavascriptInterface
    public void saveSelectedProviders(String selectedProviders){
        System.out.println("selectedProviders");
        System.out.println(selectedProviders);
        javascriptAppContext.setSelectedProviders(selectedProviders);
    }

    @JavascriptInterface
    public String getSelectedProviders(){
        if(javascriptAppContext.hasSelectedProviders()) {
            return javascriptAppContext.getSelectedProviders();
        } else {
            return "[]";
        }
    }

    static class JavascriptAppContext {
        String IS_LOGGED_IN_PROPERTY = "isLoggedIn";
        String USER_ROLE_PROPERTY = "userRole";
        String PROVIDER_USER_ROLE = "provider";
        String SUPERVISOR_USER_ROLE = "supervisor";
        String SELECTED_PROVIDERS = "selectedProviders";
        String NEXT_PAGE_TO_NAVIGATE = "nextPageToNavigate";
        String REPORT_PERIOD = "reportPeriod";
        String DAILY_REPORT_PERIOD = "daily";
        String DAILY_REPORT_PERIOD_OFFSET = "dailyReportPeriodOffset";
        String WEEKLY_REPORT_PERIOD = "weekly";
        String WEEKLY_REPORT_PERIOD_OFFSET = "weeklyReportPeriodOffset";
        String MONTHLY_REPORT_PERIOD = "monthly";
        String MONTHLY_REPORT_PERIOD_OFFSET = "monthlyReportPeriodOffset";

        private final Map<String,Object> appContext;
        JavascriptAppContext(){
            appContext = new HashMap();
            setContextPropertyValue(IS_LOGGED_IN_PROPERTY, false);
        }

        void setUserRoleForUsername(String userName){
            if(userName.equalsIgnoreCase("admin")){
                setContextPropertyValue(USER_ROLE_PROPERTY, SUPERVISOR_USER_ROLE);
            } else {
                setContextPropertyValue(USER_ROLE_PROPERTY, PROVIDER_USER_ROLE);
            }
        }

        void setContextPropertyValue(String property, Object value){
            appContext.put(property,value);
        }

        Object getContextValueByProperty(String property){
            return appContext.get(property);
        }

        void setSelectedProviders(String selectedProviders){
            appContext.put(SELECTED_PROVIDERS, selectedProviders);
        }

        String getSelectedProviders(){
            return (String) appContext.get(SELECTED_PROVIDERS);
        }

        boolean hasSelectedProviders(){
            return appContext.containsKey(SELECTED_PROVIDERS)
                    && getSelectedProviders() != null;
        }

        void setNextPageToNavigate(String nextPageId){
            appContext.put(NEXT_PAGE_TO_NAVIGATE, nextPageId);
        }

        String getNextPageToNavigate(){
            return (String) appContext.get(NEXT_PAGE_TO_NAVIGATE);
        }

        boolean isLoggedIn(){
            return (Boolean) getContextValueByProperty(IS_LOGGED_IN_PROPERTY);
        }

        void login(){
            setContextPropertyValue(IS_LOGGED_IN_PROPERTY, true);
        }

        void exit(){
            setContextPropertyValue(IS_LOGGED_IN_PROPERTY, false);
        }

        boolean isProvider(){
            return PROVIDER_USER_ROLE.equals(getContextValueByProperty(USER_ROLE_PROPERTY));
        }

        boolean isSupervisor(){
            return SUPERVISOR_USER_ROLE.equals(getContextValueByProperty(USER_ROLE_PROPERTY));
        }

        String getUserRole(){
            return (String) getContextValueByProperty(USER_ROLE_PROPERTY);
        }

        void setCurrentReportPeriodAsDaily(){
            setContextPropertyValue(REPORT_PERIOD,DAILY_REPORT_PERIOD);
        }

        void setCurrentReportPeriodAsWeekly(){
            setContextPropertyValue(REPORT_PERIOD,WEEKLY_REPORT_PERIOD);
        }

        void setCurrentReportPeriodAsMonthly(){
            setContextPropertyValue(REPORT_PERIOD,MONTHLY_REPORT_PERIOD);
        }

        boolean isCurrentReportPeriodDaily(){
            return DAILY_REPORT_PERIOD.equals(getContextValueByProperty(REPORT_PERIOD));
        }
        boolean isCurrentReportPeriodWeekly(){
            return WEEKLY_REPORT_PERIOD.equals(getContextValueByProperty(REPORT_PERIOD));
        }

        boolean isCurrentReportPeriodMonthly(){
            return MONTHLY_REPORT_PERIOD.equals(getContextValueByProperty(REPORT_PERIOD));
        }

        void incrementReportPeriodOffset(){
            if(isCurrentReportPeriodMonthly()){
                incrementMonthlyReportPeriodOffset();
            } else if(isCurrentReportPeriodDaily()){
                incrementDailyReportPeriodOffset();
            } else {
                incrementWeeklyReportPeriodOffset();
            }
        }

        void decrementReportPeriodOffset(){
            if(isCurrentReportPeriodMonthly()){
                decrementMonthlyReportPeriodOffset();
            } else if(isCurrentReportPeriodDaily()){
                decrementDailyReportPeriodOffset();
            } else {
                decrementWeeklyReportPeriodOffset();
            }
        }

        int getReportPeriodOffset(){
            if(isCurrentReportPeriodMonthly()){
                return getMonthlyReportPeriodOffset();
            } else if(isCurrentReportPeriodDaily()){
                return getDailyReportPeriodOffset();
            } else {
                return getWeeklyReportPeriodOffset();
            }
        }

        //ToDo: refactor these methods to take report period as parameter
        void setDailyReportPeriodOffset(int dailyReportPeriodOffset){
            setContextPropertyValue(DAILY_REPORT_PERIOD_OFFSET,dailyReportPeriodOffset);
        }

        void decrementDailyReportPeriodOffset(){
            int offset = getDailyReportPeriodOffset()-1;
            setDailyReportPeriodOffset(offset);
        }

        void incrementDailyReportPeriodOffset(){
            int offset = getDailyReportPeriodOffset()+1;
            setDailyReportPeriodOffset(offset);
        }

        int getDailyReportPeriodOffset(){
            if(appContext.containsKey(DAILY_REPORT_PERIOD_OFFSET)) {
                return (int) getContextValueByProperty(DAILY_REPORT_PERIOD_OFFSET);
            }
            return 0;
        }

        void setWeeklyReportPeriodOffset(int weeklyReportPeriodOffset){
            setContextPropertyValue(WEEKLY_REPORT_PERIOD_OFFSET,weeklyReportPeriodOffset);
        }

        void decrementWeeklyReportPeriodOffset(){
            int offset = getWeeklyReportPeriodOffset()-1;
            setWeeklyReportPeriodOffset(offset);
        }

        void incrementWeeklyReportPeriodOffset(){
            int offset = getWeeklyReportPeriodOffset()+1;
            setWeeklyReportPeriodOffset(offset);
        }

        int getWeeklyReportPeriodOffset(){
            if(appContext.containsKey(WEEKLY_REPORT_PERIOD_OFFSET)) {
                return (int) getContextValueByProperty(WEEKLY_REPORT_PERIOD_OFFSET);
            }
            return 0;
        }

        void setMonthlyReportPeriodOffset(int monthlyReportPeriodOffset){
            setContextPropertyValue(MONTHLY_REPORT_PERIOD_OFFSET,monthlyReportPeriodOffset);
        }

        void decrementMonthlyReportPeriodOffset(){
            int offset = getMonthlyReportPeriodOffset()-1;
            setMonthlyReportPeriodOffset(offset);
        }

        void incrementMonthlyReportPeriodOffset(){
            int offset = getMonthlyReportPeriodOffset()+1;
            setMonthlyReportPeriodOffset(offset);
        }

        int getMonthlyReportPeriodOffset(){
            if(appContext.containsKey(MONTHLY_REPORT_PERIOD_OFFSET)) {
                return (int) getContextValueByProperty(MONTHLY_REPORT_PERIOD_OFFSET);
            }
            return 0;
        }
    }

    static class CalendarUtils{

        static private SimpleDateFormat weeklyDateFormatter = new SimpleDateFormat("EEE dd MMM");

        static int getMonthLength(int monthOffset){
            Calendar calendar = Calendar.getInstance();
            if(monthOffset == 0) {
                return calendar.get(Calendar.DAY_OF_MONTH);
            }
            calendar.add(Calendar.MONTH,monthOffset);
            return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        }
        static int getWeekLength(int weekOffset){
            if(weekOffset == 0) {
                Calendar calendar = Calendar.getInstance();
                return calendar.get(Calendar.DAY_OF_WEEK);
            }
            return 7;
        }

        static String getMonthlyDateRangeString(int monthOffset){
            StringBuilder dateRange = new StringBuilder();
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.MONTH,monthOffset);
            dateRange.append(weeklyDateFormatter.format(calendar.getTime()));
            dateRange.append(" - ");
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            dateRange.append(weeklyDateFormatter.format(calendar.getTime()));
            return dateRange.toString();
        }
        static String getWeeklyDateRangeString(int weekOffset){
            Calendar calendar = Calendar.getInstance();
            StringBuilder dateRange = new StringBuilder();
            calendar = resetCalendarToFirstDayOfWeek(calendar, weekOffset);
            dateRange.append(weeklyDateFormatter.format(calendar.getTime()));
            dateRange.append(" - ");

            calendar.add(Calendar.DATE, 6);
            dateRange.append(weeklyDateFormatter.format(calendar.getTime()));
            return dateRange.toString();
        }

        public static String getDailyDateRangeString(int dayOffset){
            String dateRange = "";
            List<String> dates = getDatesStringList(dayOffset, "daily");
            for(String date:dates){
                dateRange = date;
            }
            return dateRange;
        }

        public static String reFormatDate(String dateString){
            SimpleDateFormat logsDateFormat = new SimpleDateFormat("dd-MM-yyyy");
            String reformattedDateString = null;
            try {
                reformattedDateString = weeklyDateFormatter.format(logsDateFormat.parse(dateString));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return reformattedDateString;
        }

        static List<String> getDatesStringList(int offset, String mode){
            if(mode.equals("monthly")){
                return getMonthlyDatesStringList(offset);
            } else if(mode.equals("daily")){
                return getDailyDatesStringList(offset);
            }
            return getWeeklyDatesStringList(offset);
        }

        static List<String> getDailyDatesStringList(int dayOffset){
            List<String> datesStringList =  new ArrayList();
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE,dayOffset);
            datesStringList.add(weeklyDateFormatter.format(calendar.getTime()));
            return datesStringList;
        }

        static List<String> getWeeklyDatesStringList(int weekOffset){
            List<String> datesStringList =  new ArrayList();
            Calendar calendar = Calendar.getInstance();

            calendar = resetCalendarToFirstDayOfWeek(calendar,weekOffset);

            //Add the Monday to the days list
            datesStringList.add(weeklyDateFormatter.format(calendar.getTime()));
            for (int x = 1; x <7; x++)
            {
                //Add the remaining days to the days list
                calendar.add(Calendar.DATE,1);
                datesStringList.add(weeklyDateFormatter.format(calendar.getTime()));
            }
            return datesStringList;
        }

        static List<String> getMonthlyDatesStringList(int monthOffset){
            List<String> datesStringList =  new ArrayList();
            Calendar calendar = Calendar.getInstance();

            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.MONTH,monthOffset);
            //calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));

            int max = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            for (int x = 1; x <=max ; x++)
            {
                datesStringList.add(weeklyDateFormatter.format(calendar.getTime()));
                calendar.add(Calendar.DATE,1);
            }
            return datesStringList;
        }

        static private Calendar resetCalendarToFirstDayOfWeek(Calendar calendar, int weekOffset){
            switch (calendar.get(Calendar.DAY_OF_WEEK))
            {
                case Calendar.MONDAY:
                    calendar.add(Calendar.DATE,-1 + 7*weekOffset);
                    break;

                case Calendar.TUESDAY:
                    calendar.add(Calendar.DATE,-2 + 7*weekOffset);
                    break;

                case Calendar.WEDNESDAY:
                    calendar.add(Calendar.DATE, -3 + 7*weekOffset);
                    break;

                case Calendar.THURSDAY:
                    calendar.add(Calendar.DATE,-4 + 7*weekOffset);
                    break;

                case Calendar.FRIDAY:
                    calendar.add(Calendar.DATE,-5 + 7*weekOffset);
                    break;

                case Calendar.SATURDAY:
                    calendar.add(Calendar.DATE,-6 + 7*weekOffset);
                    break;

                case Calendar.SUNDAY:
                    calendar.add(Calendar.DATE, 7*weekOffset);
                    break;
            }
            System.out.println(weekOffset);
            System.out.println(weeklyDateFormatter.format(calendar.getTime()));
            return calendar;
        }
    }

    static class ChartDataUtils{
        private static Map<String, JSONObject> chartData;

        private static void createChartData(List<LogStatistic> logStatistics,LogStatistic config){
            final JSONObject configCollection = new JSONObject();
            configCollection.put("config",JsonUtils.readAsObject(config.getDetails(),"$"));
            final JSONObject patientsSeenCollection = new JSONObject();
            final JSONObject workDayLengthCollection = new JSONObject();
            final JSONObject encounterLengthCollection = new JSONObject();
            final JSONObject activityLocationsCollection = new JSONObject();

            List providerConfigsList = JsonUtils.readAsObjectList(config.getDetails(),"$['providers']");

            for(LogStatistic logStatistic:logStatistics){
                String providerId = logStatistic.getProviderId();
                if(providerId != null && !patientsSeenCollection.containsKey(providerId)){
                    patientsSeenCollection.put(providerId,new JSONObject(){{
                        put("data",new JSONObject());
                    }});
                    workDayLengthCollection.put(providerId,new JSONObject(){{
                        put("data",new JSONObject());
                    }});
                    encounterLengthCollection.put(providerId,new JSONObject(){{
                        put("data",new JSONObject());
                    }});
                    activityLocationsCollection.put(providerId,new JSONObject(){{
                        put("data",new JSONObject());
                    }});

                    //set colors from config?
                    for(Object provider:providerConfigsList){
                        JSONObject providerConfig = (JSONObject)provider;

                        if(providerConfig.get("id") != null && providerConfig.get("id").equals(providerId)){

                            for(String key:providerConfig.keySet()) {
                                ((JSONObject) patientsSeenCollection.get(providerId)).put(key, providerConfig.get(key));
                                ((JSONObject) workDayLengthCollection.get(providerId)).put(key, providerConfig.get(key));
                                ((JSONObject) encounterLengthCollection.get(providerId)).put(key, providerConfig.get(key));
                                ((JSONObject) activityLocationsCollection.get(providerId)).put(key, providerConfig.get(key));
                            }
                        }


                    }
                }

                final String activityDateString = CalendarUtils.reFormatDate(logStatistic.getDate());
                
                final double patientsSeenValue = (Double) JsonUtils.readAsObject(logStatistic.getDetails(),"$['patients_seen']");
                ((JSONObject)((JSONObject)patientsSeenCollection.get(providerId)).get("data")).put(activityDateString,patientsSeenValue);
                
                final double workDayLengthValue = (Double) JsonUtils.readAsObject(logStatistic.getDetails(),"$['work_day_length']");
                ((JSONObject)((JSONObject)workDayLengthCollection.get(providerId)).get("data")).put(activityDateString,workDayLengthValue);

                final double averageEncounterLengthValue = (Double) JsonUtils.readAsObject(logStatistic.getDetails(),"$['average_encounter_length']");
                ((JSONObject)((JSONObject)encounterLengthCollection.get(providerId)).get("data")).put(activityDateString,averageEncounterLengthValue);

                final List activityLocationsValue = JsonUtils.readAsObjectList(logStatistic.getDetails(),"$['activity_locations']");
                ((JSONObject)((JSONObject)activityLocationsCollection.get(providerId)).get("data")).put(activityDateString,activityLocationsValue);

                //ToDo: Ensure that for dates where there are no values, insert zero and keeps the dates ordered
            }

            //Set data for expected and average values


            chartData =  new HashMap<String, JSONObject>(){{
                put(PATIENTS_SEEN_COLLECTION_KEY,patientsSeenCollection);
                put(WORK_DAY_LENGTH_COLLECTION_KEY,workDayLengthCollection);
                put(ENCOUNTER_LENGTH_COLLECTION_KEY,encounterLengthCollection);
                put(LOCATIONS_COLLECTION_KEY,activityLocationsCollection);
                put(CONFIG_COLLECTION_KEY,configCollection);
            }};
        }

        static String getChartData(String chartType){
            if(chartData != null && chartData.containsKey(chartType)) {
                return (chartData.get(chartType)).toJSONString();
            }
            return null;
        }

        static int getRandom(int limit){
            Random random = new Random();
            return random.nextInt(limit);
        }
    }
}
