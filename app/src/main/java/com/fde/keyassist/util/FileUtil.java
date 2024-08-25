package com.fde.keyassist.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import com.fde.keyassist.entity.AmplifyMappingEntity;
import com.fde.keyassist.entity.CursorEntity;
import com.fde.keyassist.entity.DirectMappingEntity;
import com.fde.keyassist.entity.DoubleClickMappingEntity;
import com.fde.keyassist.entity.KeyMappingEntity;
import com.fde.keyassist.entity.Plan;
import com.fde.keyassist.entity.ScaleMappingEntity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import org.litepal.LitePal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtil {
    // 导入
    public static void importData(){
        File sdCardRoot = Environment.getExternalStorageDirectory();
        File folder = new File(sdCardRoot, "keyAssist");
        // 调用读取方法
        readJsonFilesFromFolder(folder);
    }

    //导出
    public static void exportData(List<String> planNames, Context context){
        if(planNames == null || planNames.isEmpty()){
            return;
        }
        for (String planName : planNames){
            // 获得方案

            List<Plan> plans = LitePal.where("planName = ?",planName).find(Plan.class);
            if(plans == null || plans.isEmpty()){
                return;
            }
            // 获得方案
            Plan plan = plans.get(0);

            // 获得单击事件
            List<KeyMappingEntity> curKeyMappingEntity = new ArrayList<>();
            curKeyMappingEntity = LitePal.where("planId = ?", plan.getId().toString()).find(KeyMappingEntity.class);

            // 获得十字键事件
            List<DirectMappingEntity> directMappingEntities = new ArrayList<>();
            directMappingEntities = LitePal.where("planId = ?", plan.getId().toString()).find(DirectMappingEntity.class);

            // 获得连击事件
            List<DoubleClickMappingEntity> doubleClickMappingEntityList = new ArrayList<>();
            doubleClickMappingEntityList = LitePal.where("planId = ?", plan.getId().toString()).find(DoubleClickMappingEntity.class);

            // 获得缩放事件
            List<ScaleMappingEntity> curScaleMappingEntity = new ArrayList<>();
            curScaleMappingEntity = LitePal.where("planId = ?", plan.getId().toString()).find(ScaleMappingEntity.class);

            // 获得缩放事件
            List<AmplifyMappingEntity> amplifyMappingEntities = new ArrayList<>();
            amplifyMappingEntities = LitePal.where("planId = ?", plan.getId().toString()).find(AmplifyMappingEntity.class);


            // 获得鼠标事件
            List<CursorEntity> cursorEntities = new ArrayList<>();
            cursorEntities = LitePal.where("planId = ?", plan.getId().toString()).find(CursorEntity.class);


            // 定义保存 JSON 文件的文件夹
//            File outputDir = new File(context.getExternalFilesDir(null), plan.getPlanName()+"json_files");

            // 获取 SD 卡根目录
            File sdCardRoot = Environment.getExternalStorageDirectory();
            File outputDir = new File(sdCardRoot, "keyAssist/");

            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
//            // 导出 JSON 文件
//            exportListToJson(curKeyMappingEntity, new File(outputDir, plan.getPlanName()+"_tapClick.json"));
//
//            exportListToJson(directMappingEntities, new File(outputDir, plan.getPlanName()+"_directClick.json"));
//
//            exportListToJson(doubleClickMappingEntityList, new File(outputDir, plan.getPlanName()+"_doubleClick.json"));
//
//            exportListToJson(curScaleMappingEntity, new File(outputDir, plan.getPlanName()+"_scaleClick.json"));
//
//            exportListToJson(cursorEntities, new File(outputDir, plan.getPlanName()+"_cursorClick.json"));
//
//            exportListToJson(amplifyMappingEntities, new File(outputDir, plan.getPlanName()+"_amplifyClick.json"));

            List<Object> list = new ArrayList<>();
            list.add(curKeyMappingEntity);
            list.add(directMappingEntities);
            list.add(doubleClickMappingEntityList);
            list.add(cursorEntities);
            list.add(curScaleMappingEntity);
            list.add(amplifyMappingEntities);
            exportListToJson(list,new File(outputDir,planName+".json"));
        }


    }



    public static void readJsonFilesFromFolder(File folder)  {
        try {
            // 检查文件夹是否存在且是目录
            if (folder.exists() && folder.isDirectory()) {
                // 列出文件夹中的所有方案
                File[] files = folder.listFiles();
                // 列出方案里面所有文件
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".json")) {
                        // 保存方案
                        Plan plan = new Plan();
                        int lastIndex = file.getName().lastIndexOf('.');
                        String prefix = file.getName().substring(0, lastIndex);
                        plan.setPlanName(prefix);
                        plan.save();
                        // 读取并处理 JSON 文件
                        FileReader reader = new FileReader(file);
                        JsonElement jsonElement = JsonParser.parseReader(reader);
                        if(jsonElement.isJsonArray()){
                            JsonArray jsonArray = jsonElement.getAsJsonArray(); // 全部对象
                            jsonTokeyMapping(jsonArray.get(0).getAsJsonArray(),plan.getId());
                            jsonToDirect(jsonArray.get(1).getAsJsonArray(),plan.getId());
                            jsonToDoubleClickMapping(jsonArray.get(2).getAsJsonArray(),plan.getId());
                            jsonToCursorEntity(jsonArray.get(3).getAsJsonArray(),plan.getId());
                            jsonToScale(jsonArray.get(4).getAsJsonArray(),plan.getId());
                            jsonToAmplify(jsonArray.get(5).getAsJsonArray(),plan.getId());
                        }

                    }
                }
            }
        }catch (Exception e){

        }
    }

    private static <T> List<T> readJsonFile(File file, Type type) {
        try (FileReader reader = new FileReader(file)) {
            return new Gson().fromJson(reader, type);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private static  <T> void exportListToJson(List<T> list, File outputFile) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonString = gson.toJson(list);

        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write(jsonString);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void zipDirectory(File dir, File zipFile) {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            zipFile(dir, dir.getName(), zos);
            // 压缩完成后删除临时文件夹及其内容
            deleteDirectory(dir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zos) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zos.putNextEntry(new ZipEntry(fileName));
                zos.closeEntry();
            } else {
                zos.putNextEntry(new ZipEntry(fileName + "/"));
                zos.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            if (children != null) {
                for (File childFile : children) {
                    zipFile(childFile, fileName + "/" + childFile.getName(), zos);
                }
            }
            return;
        }
        try (FileInputStream fis = new FileInputStream(fileToZip)) {
            ZipEntry zipEntry = new ZipEntry(fileName);
            zos.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zos.write(bytes, 0, length);
            }
        }
    }


    // 清理临时目录及文件
    private static void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteDirectory(child);
                }
            }
        }
        dir.delete();
    }

    // jsonOBject转为对象
    public static void jsonTokeyMapping(JsonArray jsonArray,Integer planId){
        for(int i=0;i<jsonArray.size();i++){
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
            String jsonString = jsonObject.toString();
            // 使用Gson将JSON字符串转换为Person对象
            Gson gson = new Gson();
            KeyMappingEntity keyMapping = gson.fromJson(jsonString, KeyMappingEntity.class);
            keyMapping.setPlanId(planId);
            keyMapping.assignBaseObjId(0);
            keyMapping.save();
        }
    }

    public static void jsonToDoubleClickMapping(JsonArray jsonArray,Integer planId){
        for(int i=0;i<jsonArray.size();i++){
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
            String jsonString = jsonObject.toString();
            // 使用Gson将JSON字符串转换为Person对象
            Gson gson = new Gson();
            DoubleClickMappingEntity keyMapping = gson.fromJson(jsonString, DoubleClickMappingEntity.class);
            keyMapping.setPlanId(planId);
            keyMapping.assignBaseObjId(0);
            keyMapping.save();
        }
    }

    public static void jsonToCursorEntity(JsonArray jsonArray,Integer planId){
        for(int i=0;i<jsonArray.size();i++){
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
            String jsonString = jsonObject.toString();
            // 使用Gson将JSON字符串转换为Person对象
            Gson gson = new Gson();
            CursorEntity keyMapping = gson.fromJson(jsonString, CursorEntity.class);
            keyMapping.setPlanId(planId);
            keyMapping.assignBaseObjId(0);
            keyMapping.save();
        }
    }

    public static void jsonToDirect(JsonArray jsonArray,Integer planId){
        for(int i=0;i<jsonArray.size();i++){
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
            String jsonString = jsonObject.toString();
            // 使用Gson将JSON字符串转换为Person对象
            Gson gson = new Gson();
            DirectMappingEntity keyMapping = gson.fromJson(jsonString, DirectMappingEntity.class);
            keyMapping.setPlanId(planId);
            keyMapping.assignBaseObjId(0);
            keyMapping.save();
        }
    }

    public static void jsonToAmplify(JsonArray jsonArray,Integer planId){
        for(int i=0;i<jsonArray.size();i++){
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
            String jsonString = jsonObject.toString();
            // 使用Gson将JSON字符串转换为Person对象
            Gson gson = new Gson();
            AmplifyMappingEntity keyMapping = gson.fromJson(jsonString, AmplifyMappingEntity.class);
            keyMapping.setPlanId(planId);
            keyMapping.assignBaseObjId(0);
            keyMapping.save();
        }
    }

    public static void jsonToScale(JsonArray jsonArray,Integer planId){
        for(int i=0;i<jsonArray.size();i++){
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
            String jsonString = jsonObject.toString();
            // 使用Gson将JSON字符串转换为Person对象
            Gson gson = new Gson();
            ScaleMappingEntity keyMapping = gson.fromJson(jsonString, ScaleMappingEntity.class);
            keyMapping.setPlanId(planId);
            keyMapping.assignBaseObjId(0);
            keyMapping.save();
        }
    }


    // 预置json，英雄联盟
    public static void league(Context context)  {
        List<Plan> plans = LitePal.where("planName  = ?", "王者荣耀").find(Plan.class);
        if(plans != null && !plans.isEmpty()){
            return;
        }
        try{
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open("HonorofKings.json");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            JsonElement jsonElement = JsonParser.parseReader(bufferedReader);
            Plan plan = new Plan();
            plan.setPlanName("王者荣耀");
            plan.save();
            if(jsonElement.isJsonArray()){
                JsonArray jsonArray = jsonElement.getAsJsonArray(); // 全部对象
                jsonTokeyMapping(jsonArray.get(0).getAsJsonArray(),plan.getId());
                jsonToDirect(jsonArray.get(1).getAsJsonArray(),plan.getId());
                jsonToDoubleClickMapping(jsonArray.get(2).getAsJsonArray(),plan.getId());
                jsonToCursorEntity(jsonArray.get(3).getAsJsonArray(),plan.getId());
                jsonToScale(jsonArray.get(4).getAsJsonArray(),plan.getId());
                jsonToAmplify(jsonArray.get(5).getAsJsonArray(),plan.getId());
            }
        }catch (Exception e){

        }

    }



}
