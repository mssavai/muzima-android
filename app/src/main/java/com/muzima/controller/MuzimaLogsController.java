package com.muzima.controller;

import com.muzima.MuzimaApplication;
import com.muzima.api.model.EncounterStatistic;
import com.muzima.api.model.User;
import com.muzima.api.service.EncounterStatisticService;
import com.muzima.model.LogStatistic;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.io.IOException;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MuzimaLogsController {
    private final EncounterStatisticService encounterStatisticService;
    List<LogStatistic> expected = new ArrayList<>();
    public List<LogStatistic> getLogStatisticsByTag(String tag){
        return expected;
    }
    public MuzimaLogsController(EncounterStatisticService encounterStatisticService){
        this.encounterStatisticService = encounterStatisticService;
    }

    public List<LogStatistic> getAllLogStatistics(User currentUser) throws IOException {
        List<EncounterStatistic> encounterStatistics = encounterStatisticService.getAllEncounterStatistics();
        final Set<String> providers = new HashSet<>();
        Set<Long> datesList = new HashSet<>();
        List<LogStatistic> logStatistics = new ArrayList<>();
        for(EncounterStatistic encounterStatistic:encounterStatistics){
            providers.add(encounterStatistic.getProviderId());
            datesList.add(encounterStatistic.getActivityDate());
        }

        final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");

        final DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
        for(final Long l:datesList){
            final String formattedDate = dateFormatter.format(l);

            double totalPatientsSeen = 0.0;
            double totalWorkdayLength = 0.0;
            double totalAverageEncounterLength = 0.0;
            for(final String provider:providers) {
                long workStartTime = 0;
                long workEndTime = 0;
                long providerTotalEncounterLength = 0;
                int numberOfEncounters = 0;

                final JSONArray activitylocations = new JSONArray();

                for (final EncounterStatistic encounterStatistic : encounterStatistics) {
                    if (encounterStatistic.getActivityDate() == l && provider.equalsIgnoreCase(encounterStatistic.getProviderId())) {

                        if(workStartTime == 0 || workStartTime > encounterStatistic.getStartTime()){
                            workStartTime = encounterStatistic.getStartTime();
                        }
                        if(workEndTime == 0 || workEndTime < encounterStatistic.getEndTime()){
                            workEndTime = encounterStatistic.getEndTime();
                        }

                        providerTotalEncounterLength += encounterStatistic.getEncounterLength();
                        numberOfEncounters++;


                        activitylocations.add(new JSONObject() {{
                            put("lat",encounterStatistic.getGpsLatitude());
                            put("lng",encounterStatistic.getGpsLongitude());
                            put("encounter_date",reFormatDate(l) + ", " + dateFormat.format(l));
                        }});
                    }
                }

                final double workDayLength = (workEndTime - workStartTime)+0.0;
                totalWorkdayLength +=workDayLength;

                final double averageEncounterLength = providerTotalEncounterLength/numberOfEncounters;
                totalAverageEncounterLength += averageEncounterLength;

                final double patientsSeen = numberOfEncounters;
                totalPatientsSeen += patientsSeen;

                logStatistics.add(new LogStatistic() {
                    {
                        setTag("providerStats");
                        setDate(formattedDate);
                        setProviderId(provider);
                        setDetails(new JSONObject() {{
                            put("work_day_length",workDayLength );
                            put("average_encounter_length", averageEncounterLength);

                            put("patients_seen", patientsSeen);

                            put("activity_locations", activitylocations);
                        }}.toJSONString());
                    }
                });
            }
            final double dailyAveragePatientsSeen = Math.round((totalPatientsSeen/providers.size()) * 10.0)/10.0;
            final double dailyAverageWorkdayLength = Math.round((totalWorkdayLength/providers.size()) * 10.0)/10.0;
            final double dailyAverageEncounterLength = Math.round((totalAverageEncounterLength/providers.size()) * 10.0)/10.0;

            logStatistics.add(new LogStatistic() {
                {
                    setTag("providerStats");
                    setDate(formattedDate);
                    setProviderId("average");
                    setDetails(new JSONObject() {{
                        put("work_day_length", dailyAverageWorkdayLength);
                        put("average_encounter_length", dailyAverageEncounterLength);
                        put("patients_seen", dailyAveragePatientsSeen);
                        put("activity_locations", new JSONArray());
                    }}.toJSONString());
                }
            });
            LogStatistic ls = new LogStatistic() {
                {
                    setTag("providerStats");
                    setDate(formattedDate);
                    setProviderId("expected");
                    setDetails(new JSONObject() {{
                        put("work_day_length", 8.0);
                        put("average_encounter_length", 30.0);
                        put("patients_seen", 10.0);
                        put("activity_locations", new JSONArray());
                    }}.toJSONString());
                }
            };

            expected.add(ls);
            logStatistics.add(ls);
        }
        return logStatistics;
    }

public List<LogStatistic> getAllLogStatistics2(User currentUser) throws IOException {
        //encounterStatisticService.getAllLogStatistics();
        final List<String> providers = new ArrayList();
        providers.add("ayeung");
        providers.add("smbugua");
        providers.add("bmokaya");
        providers.add("mmwaniki");
        providers.add("pbalirwa");
        providers.add("fomondi");

        int entries = 30;
        List<String> datesList = getDatesStringList(entries);

        List<LogStatistic> logStatistics = new ArrayList<>();

        for(final String date:datesList) {
            double totalPatientsSeen = 0.0;
            double totalWorkdayLength = 0.0;
            double totalAverageEncounterLength = 0.0;
            for(final String provider:providers){
                final double workDayLength = getRandom(8)+0.0;
                totalWorkdayLength += workDayLength;

                final double averageEncounterLength = getRandom(35)+0.0;
                totalAverageEncounterLength += averageEncounterLength;

                final double patientsSeen = getRandom(12)+0.0;
                totalPatientsSeen += patientsSeen;

                logStatistics.add(new LogStatistic() {
                    {
                        setTag("providerStats");
                        setDate(date);
                        setProviderId(provider);
                        setDetails(new JSONObject() {{
                            put("work_day_length", workDayLength);
                            put("average_encounter_length", averageEncounterLength);

                            put("patients_seen", patientsSeen);

                            put("activity_locations", new JSONArray() {{
                                    final Random random = new Random();
                                    final DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                                    for (int i = 0; i < patientsSeen; i++) {
                                        add(new JSONObject() {{
                                            Time time = new Time(random.nextLong());
                                            put("lat",-1*random.nextFloat());
                                            put("lng",34+random.nextFloat());
                                            put("encounter_date",reFormatDate(date) + ", " + dateFormat.format(time));
                                        }});
                                    }
                                }}
                            );
                        }}.toJSONString());
                    }
                });
            }

            final double dailyAveragePatientsSeen = Math.round((totalPatientsSeen/providers.size()) * 10.0)/10.0;
            final double dailyAverageWorkdayLength = Math.round((totalWorkdayLength/providers.size()) * 10.0)/10.0;
            final double dailyAverageEncounterLength = Math.round((totalAverageEncounterLength/providers.size()) * 10.0)/10.0;

            logStatistics.add(new LogStatistic() {
                {
                    setTag("providerStats");
                    setDate(date);
                    setProviderId("average");
                    setDetails(new JSONObject() {{
                        put("work_day_length", dailyAverageWorkdayLength);
                        put("average_encounter_length", dailyAverageEncounterLength);
                        put("patients_seen", dailyAveragePatientsSeen);
                        put("activity_locations", new JSONArray());
                    }}.toJSONString());
                }
            });
        }

        for(final String date:datesList) {
            logStatistics.add(new LogStatistic() {
                {
                    setTag("providerStats");
                    setDate(date);
                    setProviderId("expected");
                    setDetails(new JSONObject() {{
                        put("work_day_length", 8.0);
                        put("average_encounter_length", 30.0);
                        put("patients_seen", 10.0);
                        put("activity_locations", new JSONArray());
                    }}.toJSONString());
                }
            });
        }
        return logStatistics;
    }



    private String reFormatDate(String dateString){
        SimpleDateFormat weeklyDateFormatter = new SimpleDateFormat("EEE dd MMM");
        SimpleDateFormat logsDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String reformattedDateString = null;
        try {
            reformattedDateString = weeklyDateFormatter.format(logsDateFormat.parse(dateString));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return reformattedDateString;
    }
    private String reFormatDate(long dateLong){
        SimpleDateFormat weeklyDateFormatter = new SimpleDateFormat("EEE dd MMM");
        String reformattedDateString = weeklyDateFormatter.format(dateLong);
        return reformattedDateString;
    }

    private List<String> getDatesStringList(int entries){
        List<String> datesStringList =  new ArrayList();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");

        for (int x = 1; x <= entries; x++)
        {
            datesStringList.add(dateFormatter.format(calendar.getTime()));
            calendar.add(Calendar.DATE,-1);
        }
        return datesStringList;
    }



    private int getRandom(int limit){
        Random random = new Random();
        return random.nextInt(limit);
    }

    public LogStatistic getLatestConfig(final User currentUser){
        LogStatistic config =  new LogStatistic();
        config.setDate("05-08-2019");
        config.setTag("config");
        JSONObject details = new JSONObject();
        JSONArray providers = new JSONArray();

        providers.add(new JSONObject(){{
            put("id","average");
            put("full_name","Average Performance");
            put("role","extras");
            put("color","0, 127, 132");
            put("isLoggedIn","false");
        }});
        providers.add(new JSONObject(){{
            put("id","expected");
            put("full_name","Expected Performance");
            put("role","extras");
            put("color","93, 27, 232");
            put("isLoggedIn","false");
        }});
        providers.add(new JSONObject(){{
            String username = currentUser.getUsername() != null ? currentUser.getUsername() : currentUser.getSystemId();
            put("id",username);
            put("full_name",currentUser.getGivenName() + " " + currentUser.getFamilyName());

            String role = username.equals("admin") ? "supervisor" : "provider";
            put("role",role);
            put("color","0,102,0");
            put("isLoggedIn","true");
        }});

        providers.add(new JSONObject(){{
            put("id","smbugua");
            put("full_name","Sam Mbugua");
            put("role","provider");
            put("color","255, 99, 132");
            put("isLoggedIn","false");
        }});
        providers.add(new JSONObject(){{
            put("id","ayeung");
            put("full_name","Ada Yeung");
            put("role","supervisor");
            put("color","70, 199, 32");
            put("isLoggedIn","false");
        }});
        providers.add(new JSONObject(){{
            put("id","bmokaya");
            put("full_name","Benard Mokaya");
            put("role","provider");
            put("color","100, 60, 32");
            put("isLoggedIn","false");
        }});

        providers.add(new JSONObject(){{
            put("id","mmwaniki");
            put("full_name","Michael Mwaniki");
            put("role","provider");
            put("color","99, 255, 132");
            put("isLoggedIn","false");
        }});
        providers.add(new JSONObject(){{
            put("id","pbalirwa");
            put("full_name","Priscilla Balirwa");
            put("role","provider");
            put("color","199, 70, 32");
            put("isLoggedIn","false");
        }});
        providers.add(new JSONObject(){{
            put("id","fomondi");
            put("full_name","Felix Omondi");
            put("role","provider");
            put("color","60, 100, 32");
            put("isLoggedIn","false");
        }});
        details.put("providers",providers);

        config.setDetails(details.toJSONString());

        return config;

    }

}
