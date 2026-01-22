package utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class dataManager {

    private dataManager(){};
    private static dataManager instance;
    public static dataManager getInstance(){
        if(instance == null){
            synchronized (dataManager.class){
                if(instance == null){
                    instance = new dataManager();
                }
            }
        }
        return instance;
    }

    /**
     * 创建儿童评测信息
     * @param name
     * @param c
     * @param serialNumber
     * @param birthDate
     * @param testData
     * @param testLocation
     * @param examiner
     * @return
     * @throws JSONException
     */
    public JSONObject createData(String name, String c, String serialNumber, String birthDate,
                                       String testData, String testLocation, String examiner) throws JSONException {

        JSONObject childUser = new JSONObject();

        // 创建儿童的数据信息
        JSONObject info = new JSONObject();
        info.put("name", name);
        info.put("class", c);
        info.put("serialNumber", serialNumber);
        info.put("birthDate", birthDate);
        info.put("testDate", testData);
        info.put("testLocation", testLocation);
        info.put("examiner", examiner);
        info.put("gender", "");
        info.put("address", "");
        info.put("phone", "");
        info.put("familyStatus", "");
        info.put("familyMembers", new JSONArray());

        JSONObject evaluations = new JSONObject();
        evaluations.put("A", new JSONArray());
        evaluations.put("E", new JSONArray());
        evaluations.put("NWR", new JSONArray());
        evaluations.put("PN", new JSONArray());
        evaluations.put("PST", new JSONArray());
        evaluations.put("RE", new JSONArray());
        evaluations.put("RG", new JSONArray());
        evaluations.put("S", new JSONArray());
        childUser.put("info", info);
        childUser.put("evaluations", evaluations);

        return childUser;

    }

    public JSONObject createData(JSONObject info) throws JSONException {
        JSONObject childUser = new JSONObject();
        JSONObject evaluations = new JSONObject();
        evaluations.put("A", new JSONArray());
        evaluations.put("E", new JSONArray());
        evaluations.put("NWR", new JSONArray());
        evaluations.put("PN", new JSONArray());
        evaluations.put("PST", new JSONArray());
        evaluations.put("RE", new JSONArray());
        evaluations.put("RG", new JSONArray());
        evaluations.put("S", new JSONArray());
        childUser.put("info", info == null ? new JSONObject() : info);
        childUser.put("evaluations", evaluations);
        return childUser;
    }

    public void createIndex(String name,String index) throws Exception {

        File file = new File(dirpath.PATH_FETCH_DIR_INFO, index+".json");
        JSONObject data;
        if(file.createNewFile()){
            data = new JSONObject();
        }else{
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
            reader.close();
            if(json.toString().equals("")){
                data = new JSONObject();
            }else {
                data = new JSONObject(json.toString());
            }
        }

        int num = 1;
        if (data.has("number")) {
            num = data.getInt("number");
            num = num + 1;
            data.put("number", num);
        } else {
            data.put("number", num);
        }
        data.put(String.valueOf(num), name);

        FileWriter writer = new FileWriter(file);
        writer.write(data.toString());
        writer.flush();
        writer.close();

    }





    /**
     * 保存测试者评测信息（写入文件）
     *
     * @param fName
     * @param data
     * @return
     * @throws IOException
     */
    public String saveData(String fName, JSONObject data) throws IOException {
        File file = new File(dirpath.PATH_FETCH_DIR_INFO, fName);
        //如果文件不存在，FileWriter会创建文件。如果文件已存在，FileWriter将被清空（即从头开始写入）。
        FileWriter writer = new FileWriter(file);
        writer.write(data.toString());
        writer.flush();
        writer.close();
        String path = file.getName();
        return path;
    }

    public void saveChildJson(String fName, JSONObject data) throws IOException {
        saveData(fName, data);
    }


    /**
     * 加载测试者评测信息
     *
     * @param fName
     * @return
     * @throws Exception
     */

    public JSONObject loadData(String fName) throws Exception {
        File file = new File(dirpath.PATH_FETCH_DIR_INFO, fName);
        if(file.exists()){
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
            reader.close();
            return new JSONObject(json.toString());
        }
        return new JSONObject();
    }

    /**
     * 删除评测
     * @param fName
     * @throws Exception
     */
    public void deleteData(String fName) throws Exception {
        // 这里删除data下的所有录音文件
//        JSONObject data = loadData(fName);
//        JSONArray audioPath = data.getJSONObject("evaluations").getJSONArray("A");
//        for (int i=0;i<audioPath.length();i++){
//            deleteAudioFile((String) audioPath.getJSONObject(i).get("audio"));
//        }
        File file = new File(dirpath.PATH_FETCH_DIR_INFO, fName);
        file.delete();
    }

    /**
     * 创建录音文件
     *
     * @param fName
     * @param audioData
     * @throws IOException
     */
    public void saveAudioFile(String fName, byte[] audioData) throws IOException {
        File file = new File(dirpath.PATH_FETCH_DIR_AUDIO, fName);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(audioData);
        fos.close();
    }

    /**
     * 删除录音文件
     *
     * @param fName
     */

    public void deleteAudioFile(String fName) {
        File file = new File(dirpath.PATH_FETCH_DIR_AUDIO, fName);
        file.delete();
    }

}
