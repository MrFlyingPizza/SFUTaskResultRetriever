import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

public class ResultRetriever {

    final String BASE_TARGET_URL = "https://www.cs.sfu.ca/CourseCentral/127/common/results/";
    final String BASE_AUTH_URL = "https://cas.sfu.ca/cas/login?service=https%3a%2f%2fwww.cs.sfu.ca%2fCourseCentral%2f127%2fcommon%2fresults%2f";

    private String execution;
    private String username, password;
    private String authUrl;
    private String targetUrl;

    HashMap<String, String> cookies;

    HashMap results = new HashMap<>();
    HashMap<String, String> pushTimes = new HashMap<>();

//    public static void main(String[] args) {
//        ResultRetriever retriever = new ResultRetriever();
//        try {
//            retriever.getConsoleLogin();
//            retriever.setUrls();
//            retriever.sendLoginRequest();
//
//            Document doc = retriever.getPage(retriever.targetUrl);
//            retriever.automateResultParse();
//            System.out.println("!! testing result get : " + ((HashMap) ((HashMap) retriever.results.get("16e4d6a/")).get("lab5/")).get("task1.txt"));
//            System.out.println("!! testing result print :");
//            retriever.printResults(retriever.results, 0);
//
//        } catch (IOException | ParseException exception) {
//            exception.printStackTrace();
//        }
//    }

    /* AUTHENTICATION */
    // login
    public void getConsoleLogin() {

        Scanner inputScanner = new Scanner(System.in);

        System.out.print("Username: ");
        String username = inputScanner.nextLine();
        System.out.print("Password: ");
        String password = inputScanner.nextLine();

        setLogin(username, password);
    }

    public void setLogin(String username, String password) {
        this.username = username;
        this.password = password;

        setUrls();
    }

    public void setUrls() {
        authUrl = BASE_AUTH_URL + username + "%2f";
        targetUrl = BASE_TARGET_URL + username + "/";
        System.out.println(authUrl + "\n" + targetUrl);
    }
    // auth request
    public void sendLoginRequest() throws IOException {

        getExecution(targetUrl);

        HashMap<String, String> authData = new HashMap<>();

        authData.put("username", username);
        authData.put("password", password);
        authData.put("execution", execution);
        authData.put("_eventId", "submit");
        authData.put("geolocation", "");

        org.jsoup.Connection.Response response = Jsoup.connect(authUrl)
                .data(authData)
                .method(org.jsoup.Connection.Method.POST)
                .execute();

        cookies = (HashMap<String, String>) response.cookies();
        printCookies();
    }

    public void printCookies() {
        System.out.println("!! PRINTING COOKIES ...");
        for (String key : cookies.keySet()) {
            System.out.println("Key: " + key + " | Value: " + cookies.get(key));
        }
    }

    public void getExecution(String url) throws IOException {
        org.jsoup.Connection con = Jsoup.connect(url);
        Document doc = con.get();
        Elements elements = doc.getElementsByAttributeValue("name","execution");
        Element element = elements.get(0);
        execution = element.attr("value");
        System.out.println("Execution code for current post request:\n" + this.execution);
    }

    /* PARSE DATA */
    // send authenticated get requests
    public Document getPage(String url) throws IOException {
        Document doc = Jsoup.connect(url)
                .cookie("MOD_AUTH_CAS_S", cookies.get("MOD_AUTH_CAS_S"))
                .get();
        Document.OutputSettings outputSettings = new Document.OutputSettings();
        doc.outputSettings().prettyPrint(false);
        //System.out.println("AUTH COOKIE:" + cookies.get("MOD_AUTH_CAS_S") + "GET page: \n" + doc);

        return doc;
    }
    // automate get and parsing process
    public boolean automateGetResultParse() {
        try {
            Document doc = getPage(targetUrl);
            results = (HashMap) parseResultsRecursive(targetUrl);

            return true;

        } catch (IOException exception) {
            exception.printStackTrace();

            return false;
        }
    }
    // parse
    public HashMap<String, String> parseDirs(Document doc) {
        Elements rows = doc.getElementsByTag("tr");
        Elements cells;

        String dirName, dirTime;
        HashMap<String, String> dirs = new HashMap<>();

        System.out.println(rows);
        for (Element row : rows) {
            cells = row.getElementsByTag("td");
            if (cells.size() >= 2) {
                if (cells.get(0).getElementsByAttributeValue("alt", "[DIR]").size() > 0) {
                    dirName = cells.get(1).getElementsByTag("a").get(0).html();
                    dirTime = cells.get(2).html();
                    System.out.println("!! CELL: " + dirName + " | " + dirTime);
                    dirs.put(dirName, dirTime);
                }
            }
        }
        return dirs;
    }

    public Object parseResultsRecursive(String url) throws IOException {
        System.out.println("###########" + url);
        Document doc = getPage(url);
        Elements rows = doc.getElementsByTag("tr");
        Elements cells;

        String dirName, dirTime;

        HashMap<String, Object> dirs = new HashMap<>();
        if (rows.size() == 0) {
            String taskResultText = doc.getElementsByTag("body").html();
            System.out.println(taskResultText);
            return taskResultText;
        }

        //System.out.println(rows);
        for (Element row : rows) {
            cells = row.getElementsByTag("td");
            if (cells.size() >= 2) {
                if (cells.get(0).getElementsByAttributeValue("alt", "[DIR]").size() > 0) {
                    dirName = cells.get(1).getElementsByTag("a").get(0).html();
                    dirTime = cells.get(2).html();
                    System.out.println("!! CELL: " + dirName + " | " + dirTime);
                    HashMap temp = (HashMap) parseResultsRecursive(url + dirName);
                    dirs.put(dirName, temp);
                } else if (cells.get(0).getElementsByAttributeValue("alt", "[TXT]").size() > 0) {
                    dirName = cells.get(1).getElementsByTag("a").get(0).html();
                    dirTime = cells.get(2).html();
                    System.out.println("!! CELL: " + dirName + " | " + dirTime);
                    String tempString = (String) parseResultsRecursive(url + dirName);
                    System.out.println("!! TEMP STRING : " + tempString);
                    dirs.put(dirName, tempString);
                    System.out.println("!! dirs value : " + dirs.get(dirName));
                }
            }
        }
        return dirs;
    }

    // get task data
    public String getTaskResultByPath(String pushName, String labName, String taskName) {
        HashMap push = (HashMap) results.get(pushName);
        HashMap lab = (HashMap) push.get(labName);
        return (String) lab.get(taskName);
    }
}
