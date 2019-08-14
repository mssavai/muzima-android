package com.muzima.controller;

import com.muzima.MuzimaApplication;
import com.muzima.api.model.LogStatistic;
import com.muzima.api.model.User;
import com.muzima.api.service.LogStatisticService;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class MuzimaLogsController {
    private final LogStatisticService logStatisticService;
    public MuzimaLogsController(LogStatisticService logStatisticService){
        this.logStatisticService = logStatisticService;
    }

    public List<LogStatistic> getAllLogStatistics(User currentUser) throws IOException {
        logStatisticService.getAllLogStatistics();
        final List<String> providers = new ArrayList();
        providers.add("ayeung");
        providers.add("smbugua");
        providers.add("bmokaya");
        providers.add(currentUser.getUsername() != null ? currentUser.getUsername() : currentUser.getSystemId());

        int entries = 10;
        List<String> datesList = getDatesStringList(entries);

        List<LogStatistic> logStatistics = new ArrayList<>();

        for(final String date:datesList) {
            double totalPatientsSeen = 0.0;
            double totalWorkdayLength = 0.0;
            double totalAverageEncounterLength = 0.0;
            for(final String provider:providers){
                final double workDayLength = getRandom(8)+0.0;
                totalWorkdayLength += workDayLength;

                final double averageEncounterLength = getRandom(60)+0.0;
                totalAverageEncounterLength += averageEncounterLength;

                final double patientsSeen = getRandom(20)+0.0;
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
                                    for (int i = 0; i < patientsSeen; i++) {
                                        add(new JSONObject() {{
                                            put("lat",-1*random.nextFloat());
                                            put("lng",34+random.nextFloat());
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

    public void saveLogStatistics(List<LogStatistic> logStatistics) throws IOException {
        logStatisticService.saveLogStatistics(logStatistics);
    }

    public void downloadlogStatistics() throws IOException {
        logStatisticService.downloadLogStatistics();
    }

    public LogStatistic getLatestConfig(final User currentUser){
        LogStatistic config =  new LogStatistic();
        config.setDate("05-08-2019");
        config.setTag("config");
        JSONObject details = new JSONObject();
        JSONArray providers = new JSONArray();

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
            put("id","expected");
            put("full_name","Expected Performance");
            put("role","extras");
            put("color","93, 27, 232");
            put("isLoggedIn","false");
        }});
        providers.add(new JSONObject(){{
            put("id","average");
            put("full_name","Average Performance");
            put("role","extras");
            put("color","0, 127, 132");
            put("isLoggedIn","false");
        }});
        details.put("providers",providers);

        config.setDetails(details.toJSONString());

        return config;

    }

}
