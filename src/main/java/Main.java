import com.oracle.javafx.jmx.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class Main {

    public static void main(String args[]) throws IOException {
        File dir = new File("/Users/marcus.chiu/Documents/Computer copy");
        List<File> directories = getDirectories(dir);
        HashMap<String, File> directoryID2FileMap = new HashMap<String, File>();
        for (File directory: directories) {
            String id = createLogDirectory(directory.getName(), "5b0d8793154cee0e1949654d");
            directoryID2FileMap.put(id, directory);
        }

        fillDirectory(directoryID2FileMap);
    }

    private static void fillDirectory(HashMap<String, File> directoryID2FileMap) throws IOException {
        Iterator it = directoryID2FileMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            String idd = (String)pair.getKey();
            File file = (File) pair.getValue();

            List<File> directories = getDirectories(file);
            HashMap<String, File> childDirectoryID2FileMap = new HashMap<String, File>();
            for (File directory: directories) {
                String id = createLogDirectory(directory.getName(), idd);
                System.out.println(id);
                childDirectoryID2FileMap.put(id, directory);
            }

            fillDirectory(childDirectoryID2FileMap);
            it.remove();
        }
    }

    private static List<File> getDirectories(File dir) {
        List<File> response = new ArrayList<File>();

        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                if (child.isDirectory()) {
                    response.add(child);
                }
            }
        } else {
            System.out.println("no files in directory");
        }

        return response;
    }

    private static String createLogDirectory(String name, String id) throws IOException, JSONException {
        String query = "http://192.168.1.8:8888/api/log";
        String json = "{\n" +
                "\t\"metadata\": {\n" +
                "\t\t\"name\": \"" + name + "\",\n" +
                "\t\t\"description\": \"description\"\n" +
                "\t},\n" +
                "\t\"logContents\": [\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\"logContentType\": \"HeaderSectionLogContent\",\n" +
                "\t\t\t\"css\": {\n" +
                "\t\t\t\t\"margin-top\": \"20px\",\n" +
                "\t\t\t\t\"margin-left\": \"auto\",\n" +
                "\t\t\t\t\"margin-right\": \"auto\",\n" +
                "\t\t\t\t\"max-width\": \"800px\"\n" +
                "\t\t\t},\n" +
                "\t\t\t\"data\": {}\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"logContentType\": \"ChildLogsSectionLogContent\",\n" +
                "\t\t\t\"css\": {\n" +
                "\t\t\t\t\"margin-top\": \"20px\",\n" +
                "\t\t\t\t\"margin-left\": \"auto\",\n" +
                "\t\t\t\t\"margin-right\": \"auto\",\n" +
                "\t\t\t\t\"max-width\": \"800px\"\n" +
                "\t\t\t},\n" +
                "\t\t\t\"data\": {}\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"logContentType\": \"CommentSectionLogContent\",\n" +
                "\t\t\t\"css\": {\n" +
                "\t\t\t\t\"margin-top\": \"20px\",\n" +
                "\t\t\t\t\"margin-left\": \"auto\",\n" +
                "\t\t\t\t\"margin-right\": \"auto\",\n" +
                "\t\t\t\t\"max-width\": \"800px\"\n" +
                "\t\t\t},\n" +
                "\t\t\t\"data\": {}\n" +
                "\t\t}\n" +
                "\t],\n" +
                "\t\"logOrganization\": {\n" +
                "\t\t\"parentLogIDs\": [\n" +
                "\t\t\t\"" + id + "\"\n" +
                "\t\t],\n" +
                "\t\t\"tagIDs\": []\n" +
                "\t},\n" +
                "\t\"logDisplayOverride\": {\n" +
                "\t\t\"tile\": {\n" +
                "\t\t\t\"logDataToDisplayIndex\": 4\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}";

        URL url = new URL(query);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");

        OutputStream os = conn.getOutputStream();
        os.write(json.getBytes("UTF-8"));
        os.close();

        // read the response
        InputStream in = new BufferedInputStream(conn.getInputStream());
        String result = org.apache.commons.io.IOUtils.toString(in, "UTF-8");
        JSONObject jsonObject = new JSONObject(result);


        in.close();
        conn.disconnect();

        jsonObject = (JSONObject) jsonObject.getJSONObject("_embedded").getJSONArray("collection").get(0);
        return jsonObject.getString("id");
    }
}
