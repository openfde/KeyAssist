package com.fde.keyassist.util;

import android.content.Context;
import android.os.Environment;

import com.fde.keyassist.entity.AmplifyMappingEntity;
import com.fde.keyassist.entity.CursorEntity;
import com.fde.keyassist.entity.DirectMappingEntity;
import com.fde.keyassist.entity.DoubleClickMappingEntity;
import com.fde.keyassist.entity.KeyMappingEntity;
import com.fde.keyassist.entity.Plan;
import com.fde.keyassist.entity.ScaleMappingEntity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.litepal.LitePal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
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
            File outputDir = new File(sdCardRoot, "keyAssist/"+planName);

            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            // 导出 JSON 文件
            exportListToJson(curKeyMappingEntity, new File(outputDir, plan.getPlanName()+"_tapClick.json"));

            exportListToJson(directMappingEntities, new File(outputDir, plan.getPlanName()+"_directClick.json"));

            exportListToJson(doubleClickMappingEntityList, new File(outputDir, plan.getPlanName()+"_doubleClick.json"));

            exportListToJson(curScaleMappingEntity, new File(outputDir, plan.getPlanName()+"_scaleClick.json"));

            exportListToJson(cursorEntities, new File(outputDir, plan.getPlanName()+"_cursorClick.json"));

            exportListToJson(amplifyMappingEntities, new File(outputDir, plan.getPlanName()+"_amplifyClick.json"));
        }


    }



    public static void readJsonFilesFromFolder(File folder) {
        // 检查文件夹是否存在且是目录
        if (folder.exists() && folder.isDirectory()) {
            // 列出文件夹中的所有方案
            File[] files = folder.listFiles();
            // 列出方案里面所有文件
            for(File planName : files){
                if(planName.exists() && planName.isDirectory()){
                    // 列出所有文件
                    File[] keyNames = planName.listFiles();
                    // 保存方案
                    Plan plan = new Plan();
                    plan.setPlanName(planName.getName());
                    plan.save();
                    for(File keyName: keyNames){
                        if (keyName.isFile() && keyName.getName().endsWith(".json")) {
                            // 读取并处理 JSON 文件
                                if(keyName.getName().contains("directClick")){
                                    List<DirectMappingEntity> directMappingEntities = readJsonFile(keyName, new TypeToken<ArrayList<DirectMappingEntity>>(){}.getType());
                                    if(directMappingEntities != null && !directMappingEntities.isEmpty()){
                                        for(int i=0;i<directMappingEntities.size();i++){
                                            DirectMappingEntity directMapping = directMappingEntities.get(i);
                                            directMapping.assignBaseObjId(0);
                                            directMapping.setPlanId(plan.getId());
                                            directMapping.save();
                                        }
                                    }
                                }
                                if(keyName.getName().contains("doubleClick")){
                                    List<DoubleClickMappingEntity>  doubleClickMappingEntities= readJsonFile(keyName, new TypeToken<ArrayList<DoubleClickMappingEntity>>(){}.getType());
                                    if(doubleClickMappingEntities != null && !doubleClickMappingEntities.isEmpty()){
                                        for(DoubleClickMappingEntity doubleClickMapping : doubleClickMappingEntities){
                                            doubleClickMapping.assignBaseObjId(0);
                                            doubleClickMapping.setId(null);
                                            doubleClickMapping.setPlanId(plan.getId());
                                            doubleClickMapping.save();
                                        }
                                    }
                                }
                                if(keyName.getName().contains("scaleClick")){
                                    List<ScaleMappingEntity> scaleMappingEntities = readJsonFile(keyName, new TypeToken<ArrayList<ScaleMappingEntity>>(){}.getType());
                                    if(scaleMappingEntities != null && !scaleMappingEntities.isEmpty()){
                                        for(ScaleMappingEntity scaleMapping : scaleMappingEntities){
                                            scaleMapping.assignBaseObjId(0);
                                            scaleMapping.setId(null);
                                            scaleMapping.setPlanId(plan.getId());
                                            scaleMapping.save();
                                        }
                                    }
                                }
                                if(keyName.getName().contains("tapClick")){
                                    List<KeyMappingEntity> keyMappingEntities = readJsonFile(keyName, new TypeToken<ArrayList<KeyMappingEntity>>(){}.getType());
                                    if(keyMappingEntities != null && !keyMappingEntities.isEmpty()){
                                        for(KeyMappingEntity keyMapping : keyMappingEntities){
                                            keyMapping.assignBaseObjId(0);
                                            keyMapping.setId(null);
                                            keyMapping.setPlanId(plan.getId());
                                            keyMapping.save();
                                        }
                                    }
                                }
                                if(keyName.getName().contains("cursorClick")){
                                    List<CursorEntity> cursorEntities = readJsonFile(keyName, new TypeToken<ArrayList<CursorEntity>>(){}.getType());
                                    if(cursorEntities != null && !cursorEntities.isEmpty()){
                                        for(CursorEntity cursor : cursorEntities){
                                            cursor.assignBaseObjId(0);
                                            cursor.setId(null);
                                            cursor.setPlanId(plan.getId());
                                            cursor.save();
                                        }
                                    }
                                }
                            if(keyName.getName().contains("amplifyClick")){
                                List<AmplifyMappingEntity> amplifyMappingEntities = readJsonFile(keyName, new TypeToken<ArrayList<AmplifyMappingEntity>>(){}.getType());
                                if(amplifyMappingEntities != null && !amplifyMappingEntities.isEmpty()){
                                    for(AmplifyMappingEntity cursor : amplifyMappingEntities){
                                        cursor.assignBaseObjId(0);
                                        cursor.setId(null);
                                        cursor.setPlanId(plan.getId());
                                        cursor.save();
                                    }
                                }
                            }
                            }
                        }
                    }

                }


        } else {
            System.out.println("The specified folder does not exist or is not a directory.");
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


}
