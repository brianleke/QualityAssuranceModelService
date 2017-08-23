package com.qamam;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

import static com.qamam.JsonUtil.json;
import static spark.Spark.*;

public class QaMaMService {

    final static Logger logger = LoggerFactory.getLogger(QaMaMService.class);

    private static String[] getDBDetails() {
        try {
            Properties props = new Properties();
            String configFile = System.getProperty("user.dir") + "/config/config.properties";
            InputStream in = new FileInputStream(configFile);
            props.load(in);
            in.close();

            String[] dbDetails = new String[4];
            dbDetails[0] = props.get("QAMAM_DB_URL").toString();
            dbDetails[1] = props.get("QAMAM_DB_USERNAME").toString();
            dbDetails[2] = props.get("QAMAM_DB_PASSWORD").toString();
            dbDetails[3] = props.get("MY_SQL_URL").toString();


            return dbDetails;
        }
        catch (Exception exception){
            logger.error(exception.getMessage());
            return new String[] {"", "", "", ""};
        }
    }

    private static ArrayList<String> getDistinctDetailsFor(String distinctType, String query) {
        Connection conn = null;
        Statement stmt = null;
        ArrayList<String> distinctResults = new ArrayList<String>();

        String[] dbDetails = getDBDetails();

        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(dbDetails[0], dbDetails[1], dbDetails[2]);
            stmt = conn.createStatement();
            ResultSet resultSet = stmt.executeQuery(query);

            while (resultSet.next()){
                distinctResults.add(resultSet.getString(distinctType));
            }
            return distinctResults;
        }
        catch (Exception exception){
            logger.error(exception.getMessage());
            return new ArrayList<String>();
        }
    }

    private static Assessments getAssessmentsByDate(String dateAssessed, String portfolio, ArrayList<String> previousDates){
        Connection conn = null;
        Statement stmt = null;
        ArrayList<Assessment> assessments = new ArrayList<Assessment>();
        String[] dbDetails = getDBDetails();

        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(dbDetails[0], dbDetails[1], dbDetails[2]);
            stmt = conn.createStatement();

            String queryStatement = "SELECT * from QualityMaturityAssessments where portfolio = '" + portfolio + "' AND (dateassessed = '";

            for(String date: previousDates){
                queryStatement += date + "' OR dateassessed = '";
            }

            int length = " OR dateassessed = '".length();
            queryStatement = queryStatement.substring(0, queryStatement.length() - length) + ")";

            ResultSet resultSet = stmt.executeQuery(queryStatement);

            int numberOfRecords = 0;
            double overallTesting = 0;
            double overallTestMetrics = 0;
            double overallQualityAlignment = 0;
            double overallPracticeInnovation = 0;
            double overallToolsArtefacts = 0;

            ArrayList<String> teamObtained = new ArrayList<String>();
            Map<String, String> teamDate = new HashMap<String, String>();
            Map<String, Assessment> teamAssessment = new HashMap<String, Assessment>();

            while (resultSet.next()){
                String teamName = resultSet.getString("teamName");
                String dateOfAssessment = resultSet.getString("dateassessed");
                String portfolioName = resultSet.getString("portfolio");

                DateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
                Date date = format.parse(dateOfAssessment);

                if(teamObtained.contains(teamName)){
                    Date previousDate = format.parse(teamDate.get(teamName));
                    if(previousDate.before(date)){
                        Assessment assessmentToBeRemoved = teamAssessment.get(teamName);
                        assessments.remove(assessmentToBeRemoved);
                        teamObtained.remove(teamName);
                        overallTesting = assessmentToBeRemoved.removeValueFromAssessment("Testing", overallTesting);
                        overallTestMetrics = assessmentToBeRemoved.removeValueFromAssessment("TestMetrics", overallTestMetrics);
                        overallQualityAlignment = assessmentToBeRemoved.removeValueFromAssessment("QualityAlignment", overallQualityAlignment);
                        overallPracticeInnovation = assessmentToBeRemoved.removeValueFromAssessment("PracticeInnovation", overallPracticeInnovation);
                        overallToolsArtefacts = assessmentToBeRemoved.removeValueFromAssessment("ToolsArtefacts", overallToolsArtefacts);
                        numberOfRecords--;
                    }

                }

                if(!teamObtained.contains(teamName) && portfolioName.equals(portfolio)) {
                    Assessment assessment = new Assessment();
                    assessment.setTeamName(teamName);

                    String testing = resultSet.getString("testing");
                    assessment.setTesting(testing);
                    overallTesting += Integer.parseInt(testing);

                    String testMetrics = resultSet.getString("testMetrics");
                    assessment.setTestMetrics(testMetrics);
                    overallTestMetrics += Integer.parseInt(testMetrics);

                    String qualityAlignment = resultSet.getString("qualityAlignment");
                    assessment.setQualityAlignment(qualityAlignment);
                    overallQualityAlignment += Integer.parseInt(qualityAlignment);

                    String practiceInnovation = resultSet.getString("practiceInnovation");
                    assessment.setPracticeInnovation(practiceInnovation);
                    overallPracticeInnovation += Integer.parseInt(practiceInnovation);

                    String toolsArtefacts = resultSet.getString("toolsArtefacts");
                    assessment.setToolsArtefacts(toolsArtefacts);
                    overallToolsArtefacts += Integer.parseInt(toolsArtefacts);

                    String recommendedCapabilities = resultSet.getString("recommendedCapabilities");
                    assessment.setRecommendedCapabilities(recommendedCapabilities);

                    String capabilitiesToStop = resultSet.getString("capabilitiesToStop");
                    assessment.setCapabilitiesToStop(capabilitiesToStop);


                    String rawData = resultSet.getString("rawdata");
                    assessment.setRawData(rawData);

                    numberOfRecords++;
                    assessments.add(assessment);
                    teamObtained.add(teamName);
                    teamDate.put(teamName, dateOfAssessment);
                    teamAssessment.put(teamName, assessment);
                }
            }
            Assessment assessmentOverall = new Assessment();
            assessmentOverall.setTeamName("Average For All The Teams");
            assessmentOverall.setTesting(String.valueOf(overallTesting/numberOfRecords));
            assessmentOverall.setTestMetrics(String.valueOf(overallTestMetrics/numberOfRecords));
            assessmentOverall.setQualityAlignment(String.valueOf(overallQualityAlignment/numberOfRecords));
            assessmentOverall.setPracticeInnovation(String.valueOf(overallPracticeInnovation/numberOfRecords));
            assessmentOverall.setToolsArtefacts(String.valueOf(overallToolsArtefacts/numberOfRecords));
            assessments.add(assessmentOverall);

            return new Assessments(dateAssessed, portfolio, assessments);
        }
        catch (Exception exception){
            logger.error(exception.getMessage());
            return new Assessments(dateAssessed, portfolio, new ArrayList<Assessment>());
        }
    }

    private static ArrayList<String> getPreviousAssessmentDates(String currentDateString, ArrayList<String> otherDates){
        ArrayList<String> previousDates = new ArrayList<String>();

        previousDates.add(currentDateString);

        for(String assessmentDate: otherDates){
            try {
                DateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
                Date date = format.parse(assessmentDate);
                Date currentDate = format.parse(currentDateString);

                if(date.before(currentDate)){
                    previousDates.add(assessmentDate);
                }
            }
            catch (Exception ex){
                logger.error(ex.getMessage());
            }
        }

        return previousDates;
    }

    private static ArrayList<Assessments> getAssessments(){

        String portfolioQuery = "SELECT DISTINCT portfolio from QualityMaturityAssessments";
        ArrayList<Assessments> allAssessmentsDone = new ArrayList<Assessments>();
        ArrayList<String> assessmentPortfolios = getDistinctDetailsFor("portfolio", portfolioQuery);
        for(String portfolio: assessmentPortfolios){
            String dateQuery = "SELECT DISTINCT dateassessed from QualityMaturityAssessments WHERE portfolio = '" + portfolio + "'";
            ArrayList<String> assessmentDates = getDistinctDetailsFor("dateassessed", dateQuery);
            for(String assessmentDate: assessmentDates){
                allAssessmentsDone.add(getAssessmentsByDate(assessmentDate, portfolio, getPreviousAssessmentDates(assessmentDate, assessmentDates)));
            }
        }

        return allAssessmentsDone;
    }

    private static Assessment getAssessmentForTeam(String teamName){

        createTableIfItDoesNotExists();
        Connection conn = null;
        Statement stmt = null;
        Assessment assessment = new Assessment();
        String[] dbDetails = getDBDetails();

        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(dbDetails[0], dbDetails[1], dbDetails[2]);
            stmt = conn.createStatement();
            DateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);

            String queryStatement = "SELECT * from QualityMaturityAssessments where teamName = '"
                    + teamName + "'";
            ResultSet resultSet = stmt.executeQuery(queryStatement);

            Date previousDate = format.parse("01-01-1990");

            while (resultSet.next()){
                String dateOfAssessment = resultSet.getString("dateassessed");
                Date date = format.parse(dateOfAssessment);

                if(date.after(previousDate)) {
                    assessment.setTeamName(resultSet.getString("teamName"));

                    String testing = resultSet.getString("testing");
                    assessment.setTesting(testing);

                    String testMetrics = resultSet.getString("testMetrics");
                    assessment.setTestMetrics(testMetrics);

                    String qualityAlignment = resultSet.getString("qualityAlignment");
                    assessment.setQualityAlignment(qualityAlignment);

                    String practiceInnovation = resultSet.getString("practiceInnovation");
                    assessment.setPracticeInnovation(practiceInnovation);

                    String toolsArtefacts = resultSet.getString("toolsArtefacts");
                    assessment.setToolsArtefacts(toolsArtefacts);

                    String recommendedCapabilities = resultSet.getString("recommendedCapabilities");
                    assessment.setRecommendedCapabilities(recommendedCapabilities);

                    String capabilitiesToStop = resultSet.getString("capabilitiesToStop");
                    assessment.setCapabilitiesToStop(capabilitiesToStop);

                    previousDate = date;

                    String rawData = resultSet.getString("rawdata");
                    assessment.setRawData(rawData);
                }
            }

            return assessment;
        }
        catch (Exception exception){
            logger.error(exception.getMessage());
            return assessment;
        }
    }

    private static void createDatabaseIfItDoesNotExists(){
        Connection conn = null;
        Statement statement = null;
        String[] dbDetails = getDBDetails();

        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(dbDetails[3], dbDetails[1], dbDetails[2]);
            statement = conn.createStatement();
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS ContinuumAssessment;");
            createTableIfItDoesNotExists();
        }
        catch (Exception exception){
            logger.error(exception.getMessage());
        }
    }

    private static void createTableIfItDoesNotExists(){
        Connection conn = null;
        Statement statement = null;
        String[] dbDetails = getDBDetails();

        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(dbDetails[0], dbDetails[1], dbDetails[2]);
            statement = conn.createStatement();
            String sqlCreate = "CREATE TABLE IF NOT EXISTS QualityMaturityAssessments"
                    + "  (teamName           VARCHAR(150),"
                    + "   testing            INTEGER,"
                    + "   testMetrics        INTEGER,"
                    + "   qualityAlignment   INTEGER,"
                    + "   practiceInnovation INTEGER,"
                    + "   toolsArtefacts     INTEGER,"
                    + "   dateassessed       VARCHAR(100),"
                    + "   portfolio          VARCHAR(150),"
                    + "   rawdata            longtext,"
                    + "   recommendedCapabilities            longtext,"
                    + "   capabilitiesToStop                 longtext,"
                    + "   UNIQUE KEY my_unique_key (teamName,dateassessed,portfolio))";

            statement.execute(sqlCreate);
        }
        catch (Exception exception){
            logger.error(exception.getMessage());
        }
    }

    public static void main(String[] args) {

        port(8001);

        post("/saveTeamData", new Route() {
            public Object handle(Request request, Response response) throws Exception {

                createDatabaseIfItDoesNotExists();
                Connection conn = null;
                Statement stmt = null;

                String teamName = request.queryParams("teamName");
                String testing = request.queryParams("testing");
                String testMetrics = request.queryParams("test-metrics");
                String qualityAlignment = request.queryParams("quality-alignment");
                String practiceInnovation = request.queryParams("practice-innovation");
                String toolsArtefacts = request.queryParams("tools-artefacts");
                DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                String dateOfEvaluation = dateFormat.format(new Date());
                String portfolioName = request.queryParams("portfolioName");
                String rawData = request.queryParams("rawData");

                JSONObject json = new JSONObject(request.body());

                String recommendedCapabilities;
                String capabilitiesToStop;

                try {
                    recommendedCapabilities = json.get("recommendedCapabilities").toString();
                }
                catch(Exception ex){
                    recommendedCapabilities = "";
                }

                try {
                    capabilitiesToStop = json.get("capabilitiesToStop").toString();
                }
                catch(Exception ex){
                    capabilitiesToStop = "";
                }

                String[] dbDetails = getDBDetails();

                try {
                    Class.forName("com.mysql.jdbc.Driver");
                    conn = DriverManager.getConnection(dbDetails[0], dbDetails[1], dbDetails[2]);
                    stmt = conn.createStatement();

                    String sql = String.format("REPLACE INTO QualityMaturityAssessments " +
                                    "VALUES ('%s',%s,%s,%s,%s,%s,'%s', '%s', '%s', '%s', '%s')", teamName, testing, testMetrics, qualityAlignment, practiceInnovation,
                            toolsArtefacts, dateOfEvaluation, portfolioName, rawData, recommendedCapabilities, capabilitiesToStop);

                    int insertedRecord = stmt.executeUpdate(sql);

                    if (insertedRecord > 0) {
                        return "Successfully inserted record";
                    } else {
                        return "Record not inserted";
                    }
                }
                catch (SQLException exception){
                    logger.error("Error Code: " + exception.toString());
                    return "Error Code: " + exception.toString();
                }

            }
        });

        get("/assessments", new Route() {
            public Object handle(Request req, Response res) throws Exception {
                return getAssessments();
            }
        }, json());


        get("/assessment", new Route() {
            public Object handle(Request request, Response response) throws Exception {
                logger.info("Request From: " + request.host());
                String teamName = request.queryParams("teamName");
                Assessment teamAssessment = getAssessmentForTeam(teamName);
                return teamAssessment;
            }
        }, json());

        options("/*",
                new Route() {
                    public Object handle(Request request, Response response) throws Exception {

                        String accessControlRequestHeaders = request
                                .headers("Access-Control-Request-Headers");
                        if (accessControlRequestHeaders != null) {
                            response.header("Access-Control-Allow-Headers",
                                    accessControlRequestHeaders);
                        }

                        String accessControlRequestMethod = request
                                .headers("Access-Control-Request-Method");
                        if (accessControlRequestMethod != null) {
                            response.header("Access-Control-Allow-Methods",
                                    accessControlRequestMethod);
                        }

                        return "OK";
                    }
                });

        before(new Filter() {
            public void handle(Request request, Response response) throws Exception {
                response.header("Access-Control-Allow-Origin", "*");
            }
        });
    }
}
