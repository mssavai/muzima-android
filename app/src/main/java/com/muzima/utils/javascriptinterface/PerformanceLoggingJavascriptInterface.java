package com.muzima.utils.javascriptinterface;

import android.util.Log;
import android.webkit.JavascriptInterface;
import com.muzima.MuzimaApplication;
import com.muzima.api.model.LogStatistic;
import com.muzima.utils.StringUtils;
import com.muzima.view.reports.ProviderPerformanceReportViewActivity;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.io.IOException;
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

    //ToDo: load javascriptAppContext from database
    private  static final JavascriptAppContext javascriptAppContext = new JavascriptAppContext();
    private String activeTab;

    public PerformanceLoggingJavascriptInterface(ProviderPerformanceReportViewActivity providerReportViewActivity){
        this.providerReportViewActivity = providerReportViewActivity;
        this.muzimaApplication = (MuzimaApplication) providerReportViewActivity.getApplicationContext();
        javascriptAppContext.setUserRoleForUsername(providerReportViewActivity.getUsername());
    }

    @JavascriptInterface
    public void loadMapsPage(){
        providerReportViewActivity.loadMapsPage();
    }

    @JavascriptInterface
    public void setSelectedProvider(String providerId){
        javascriptAppContext.setSelectedProvider(providerId);
        providerReportViewActivity.popTopPage();
    }

    @JavascriptInterface
    public String getSelectedProvider(){
        return javascriptAppContext.getSelectedProvider();
    }

    @JavascriptInterface
    public  boolean hasSelectedProvider(){
        return javascriptAppContext.hasSelectedProvider();
    }

    @JavascriptInterface
    public  static boolean isCurrentUserSupervisor(){
        return javascriptAppContext.isSupervisor();
    }

    @JavascriptInterface
    public String getProviders(){
        return getProvidersAsArray().toJSONString();
    }

    public static JSONArray getProvidersAsArray(){
        JSONArray providers = new JSONArray();

        JSONObject object3 = new JSONObject();
        object3.put("username","benard");
        object3.put("name","Benard Mokaya");
        providers.add(object3);

        JSONObject object4 = new JSONObject();
        object4.put("username","joan");
        object4.put("name","Joan Nakibuuka");
        providers.add(object4);

        JSONObject object5 = new JSONObject();
        object5.put("username","priscilla");
        object5.put("name","Priscilla Balirwa");
        providers.add(object5);

        JSONObject object1 = new JSONObject();
        object1.put("username","sthaiya");
        object1.put("name","Mbugua Sam");
        providers.add(object1);

        JSONObject object6 = new JSONObject();
        object6.put("username","owino");
        object6.put("name","Owino Samuel");
        providers.add(object6);

        JSONObject object2 = new JSONObject();
        object2.put("username","simon");
        object2.put("name","Simon Savai");
        providers.add(object2);

        return providers;
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
        javascriptAppContext.incrementReportPeriodOffset();
        providerReportViewActivity.reloadCurrentPage();
    }

    @JavascriptInterface
    public int getReportPeriodOffset(){
        return javascriptAppContext.getReportPeriodOffset();
    }
    @JavascriptInterface
    public String getChartData(int chartType){
        if(javascriptAppContext.isCurrentReportPeriodMonthly()) {
            System.out.println("Is Monthly");
            int offset = javascriptAppContext.getMonthlyReportPeriodOffset();
            return ChartDataUtils.getChartData(offset, "monthly", chartType, providerReportViewActivity.getUsername());
        } else {
            System.out.println("Is Not Monthly");
            int offset = javascriptAppContext.getWeeklyReportPeriodOffset();
            return ChartDataUtils.getChartData(offset, "weekly", chartType, providerReportViewActivity.getUsername());
        }
    }

    @JavascriptInterface
    public String getMapData(){
        int chartType = 4;
        if(javascriptAppContext.isCurrentReportPeriodMonthly()) {
            System.out.println("Is Monthly");
            int offset = javascriptAppContext.getMonthlyReportPeriodOffset();
            return ChartDataUtils.getChartData(offset, "monthly", chartType, providerReportViewActivity.getUsername());
        } else {
            System.out.println("Is Not Monthly");
            int offset = javascriptAppContext.getWeeklyReportPeriodOffset();
            return ChartDataUtils.getChartData(offset, "weekly", chartType, providerReportViewActivity.getUsername());
        }
    }

    @JavascriptInterface
    public String getStatistics(){
        try {
            List<LogStatistic> logStatistics = muzimaApplication.getMuzimaLogsController().getAllLogStatistics();
            if(!logStatistics.isEmpty()){
                return logStatistics.get(0).getDetails();
            }
        } catch (IOException e) {
            Log.e("TESTING","Test",e);
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("logins", 1);
        jsonObject.put("logouts", 1);
        jsonObject.put("timeouts", 1);
        return jsonObject.toJSONString();
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
    public void authenticate(String username, String password){
        //ToDo: Authenticate against log server

        if(StringUtils.isEmpty(username)) {
            javascriptAppContext.login();
            providerReportViewActivity.loadLandingPage();
        } else {
            providerReportViewActivity.loadLoginPage();
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
    public void setCurrentReportPeriodAsWeekly(){
        javascriptAppContext.setCurrentReportPeriodAsWeekly();
    }
    @JavascriptInterface
    public void setCurrentReportPeriodAsMonthly(){
        javascriptAppContext.setCurrentReportPeriodAsMonthly();
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
        }
        int offset = javascriptAppContext.getWeeklyReportPeriodOffset();
        return CalendarUtils.getWeeklyDateRangeString(offset);
    }

    @JavascriptInterface
    public void exit(){
        javascriptAppContext.exit();
        //ToDo: save javascriptAppContext
        javascriptAppContext.setSelectedProvider(null);

        providerReportViewActivity.exit();
    }

    static class JavascriptAppContext {
        String IS_LOGGED_IN_PROPERTY = "isLoggedIn";
        String USER_ROLE_PROPERTY = "userRole";
        String PROVIDER_USER_ROLE = "provider";
        String SUPERVISOR_USER_ROLE = "supervisor";
        String SELECTED_PROVIDER_PROPERTY = "selectedProvider";
        String NEXT_PAGE_TO_NAVIGATE = "nextPageToNavigate";
        String REPORT_PERIOD = "reportPeriod";
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

        void setSelectedProvider(String providerId){
            appContext.put(SELECTED_PROVIDER_PROPERTY, providerId);
        }

        String getSelectedProvider(){
            return (String) appContext.get(SELECTED_PROVIDER_PROPERTY);
        }

        boolean hasSelectedProvider(){
            return appContext.containsKey(SELECTED_PROVIDER_PROPERTY)
                    && getSelectedProvider() != null;
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

        void setCurrentReportPeriodAsWeekly(){
            setContextPropertyValue(REPORT_PERIOD,WEEKLY_REPORT_PERIOD);
        }

        void setCurrentReportPeriodAsMonthly(){
            setContextPropertyValue(REPORT_PERIOD,MONTHLY_REPORT_PERIOD);
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
            } else {
                incrementWeeklyReportPeriodOffset();
            }
        }

        void decrementReportPeriodOffset(){
            if(isCurrentReportPeriodMonthly()){
                decrementMonthlyReportPeriodOffset();
            } else {
                decrementWeeklyReportPeriodOffset();
            }
        }

        int getReportPeriodOffset(){
            if(isCurrentReportPeriodMonthly()){
                return getMonthlyReportPeriodOffset();
            } else {
                return getWeeklyReportPeriodOffset();
            }
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

        static List<String> getDatesStringList(int offset, String mode){
            if(mode.equals("monthly")){
                return getMonthlyDatesStringList(offset);
            }
            return getWeeklyDatesStringList(offset);
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
            }
            return calendar;
        }
    }

    static class ChartDataUtils{
        private static String username = "";
        private static Map<Integer,String> chartData = new HashMap();
        private static String createMapData(int offset, String mode){
            int entries = (mode.equals("weekly"))?
                    CalendarUtils.getWeekLength(offset) : CalendarUtils.getMonthLength(offset);
            JSONArray mapData = new JSONArray();
            JSONArray jsonArray = getProvidersAsArray();
            for (Object jsonObject1:jsonArray){
                System.out.println(">>>> Creating locations for : "+((JSONObject)jsonObject1).get("name"));
                JSONObject providerObject = new JSONObject();
                providerObject.put("label",((JSONObject)jsonObject1).get("name"));
                JSONArray coordinates = new JSONArray();
                for (int i=1; i<=entries; i++){
                    int items = getRandom(30);
                    for(int j=1;j<items;j++){
                        JSONObject coordinate = new JSONObject();

                        Random random = new Random();
                        coordinate.put("lat",-1*random.nextFloat());
                        coordinate.put("lng",34+random.nextFloat());
                        coordinates.add(coordinate);
                    }
                }
                providerObject.put("locations",coordinates);
                mapData.add(providerObject);
            }
            return mapData.toJSONString();
        }
        private static String createChartData(int offset, String mode, int chartType){
            if(chartType == 4){
                return createMapData(offset,mode);
            }
            int entries = (mode.equals("weekly"))?
                    CalendarUtils.getWeekLength(offset) : CalendarUtils.getMonthLength(offset);
            int limit = 16*chartType-8;
            JSONObject jsonObject = new JSONObject();

            JSONArray labels = new JSONArray();
            labels.addAll(CalendarUtils.getDatesStringList(offset, mode));
            jsonObject.put("labels",labels);

            JSONArray dataSets = new JSONArray();

            if(isCurrentUserSupervisor()){
                JSONObject self = new JSONObject();
                self.put("label","Ada Yeung");
                self.put("id","ayeung");
                self.put("backgroundColor","rgba(255, 99, 132, 0.2)");
                self.put("tableBackgroundColor","rgba(255, 99, 132, 0.4)");
                self.put("borderColor","rgba(255, 99, 132, 1)");
                self.put("borderWidth","1");
                JSONArray selfData = new JSONArray();
                for (int i=1; i<=entries; i++){
                    selfData.add(getRandom(limit));
                }
                self.put("data",selfData);
                dataSets.add(self);
                JSONArray jsonArray = getProvidersAsArray();
                for (Object jsonObject1:jsonArray){
                    String rgb = getRandom(255)+", "+getRandom(255)+", "+getRandom(255);
                    JSONObject providerObject = new JSONObject();
                    providerObject.put("label",((JSONObject)jsonObject1).get("name"));
                    providerObject.put("id",((JSONObject)jsonObject1).get("username"));
                    providerObject.put("type","line");
                    providerObject.put("fill","false");
                    providerObject.put("borderColor","rgba("+rgb+", 1)");
                    providerObject.put("pointBorderColor","rgba("+rgb+", 1)");
                    providerObject.put("tableBackgroundColor","rgba("+rgb+", 0.4)");
                    JSONArray providerObjectData = new JSONArray();
                    for (int i=1; i<=entries; i++){
                        providerObjectData.add(getRandom(limit));
                    }
                    providerObject.put("data",providerObjectData);
                    dataSets.add(providerObject);
                }
            } else {
                JSONObject self = new JSONObject();
                self.put("label","Simon Savai");
                self.put("id","savai");
                self.put("backgroundColor","rgba(255, 99, 132, 0.2)");
                self.put("tableBackgroundColor","rgba(255, 99, 132, 0.4)");
                self.put("borderColor","rgba(255, 99, 132, 1)");
                self.put("borderWidth","1");
                JSONArray selfData = new JSONArray();
                for (int i=1; i<=entries; i++){
                    selfData.add(getRandom(limit));
                }
                self.put("data",selfData);
                dataSets.add(self);
            }

            JSONObject average = new JSONObject();
            average.put("label","Team average");
            average.put("id","average");
            average.put("type","line");
            average.put("fill","false");
            average.put("borderColor","rgba(10, 159, 64, 1)");
            average.put("pointBorderColor","rgba(10, 159, 64, 1)");
            average.put("tableBackgroundColor","rgba(10, 159, 64, 0.4)");
            JSONArray averageData = new JSONArray();
            for (int i=1; i<=entries; i++){
                averageData.add(1+getRandom(limit-1));
            }
            average.put("data",averageData);
            dataSets.add(average);

            JSONObject expected = new JSONObject();
            expected.put("label","Expected");
            expected.put("id","expected");
            expected.put("type","line");
            expected.put("fill","false");
            expected.put("borderColor","rgba(75, 192, 192, 1)");
            expected.put("pointBorderColor","rgba(75, 192, 192, 1)");
            expected.put("tableBackgroundColor","rgba(75, 192, 192, 0.4)");
            JSONArray expectedData = new JSONArray();
            for (int i=1; i<=entries; i++){
                expectedData.add(7*chartType);
            }
            expected.put("data",expectedData);
            dataSets.add(expected);

            jsonObject.put("datasets",dataSets);

            return jsonObject.toJSONString();
        }

        static String getChartData(int offset, String mode, int chartType, String loggedInUser){
            if(!loggedInUser.equalsIgnoreCase(username)){
                username = loggedInUser;
                chartData = new HashMap<>();
            }
            int dataTypeOffset = mode.equals("monthly")? (2*chartType + 2)*100 : (2*chartType + 1)*100;

            if(!chartData.containsKey(offset+dataTypeOffset)) {
                chartData.put(offset + dataTypeOffset, createChartData(offset, mode, chartType));
            }
            return chartData.get(offset + dataTypeOffset);
        }

        private static int getRandom(int limit){
            Random random = new Random();
            return random.nextInt(limit);
        }
    }
}
