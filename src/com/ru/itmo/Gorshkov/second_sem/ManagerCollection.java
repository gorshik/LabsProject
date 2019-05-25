package com.ru.itmo.Gorshkov.second_sem;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.ru.itmo.Gorshkov.first_sem.Condition;
import com.ru.itmo.Gorshkov.first_sem.Human;
import com.ru.itmo.Gorshkov.first_sem.MaterialProperty;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

public class ManagerCollection {
    private ConcurrentSkipListMap<String, Human> collection = new ConcurrentSkipListMap<>();
    private String path;

    ManagerCollection(String path) {
        this.path = path;
    }

    public ConcurrentSkipListMap<String, Human> getCollection() {
        return collection;
    }

    public Human put(String key, Human value) {
        if(!key.equals(value.getName())) {
            value.setName(key);
        }
        return collection.put(key, value);
    }

    public Human put(Human value) {
        if (value == null) {
            return null;
        } else {
            return collection.put(value.getName(), value);
        }
    }

    public Human remove(String key) {
        Human hum = collection.remove(key);
        return hum;
    }

    public void exportfromfile(String path) {
        try (Scanner scan = new Scanner(Paths.get(System.getenv(path)))) {
            if (scan.hasNextLine()) {
                String str = scan.nextLine();
                if (!str.equals("{\"Humans\" : [")) throw new JSONException();
            }
            while (scan.hasNextLine()) {
                String str = scan.nextLine();
                if (str.charAt(str.length() - 1) == ',') {
                    str = str.substring(0, str.length() - 1);
                    this.put(parseHuman(str));
                } else {
                    if (!str.equals("]}")) {
                        this.put(parseHuman(str.substring(0, str.length() - 2)));
                    }
                    if (!str.substring(str.length() - 2).equals("]}")) throw new JSONException();
                }
            }
        } catch (IOException e) {
            System.err.println("File not found");
            if (System.getenv(this.path).equals(System.getenv(path))) {
                saveToFile();
                System.out.println("Created file to save collection: " + System.getenv(this.path));
            }
        } catch (java.lang.StringIndexOutOfBoundsException | JSONException e) {
            System.err.println("Invalid JSON Format");
        }
    }

    public Human parseHuman(String str) {
        try {
            Human human = helpParse(str);
            JSONObject jsonObject = (JSONObject) JSON.parse(str);
            Object allProperty = jsonObject.get("allProperty");
            for (Object property : (JSONArray) allProperty) {
                Object propertyName = ((JSONObject) property).get("name");
                Object propertyOwner = ((JSONObject) property).get("owner");
                if (!(propertyOwner == null)) {
                    Human propertyhuman = helpParse(propertyOwner.toString());
                    human.addProperty(new MaterialProperty((String) propertyName, propertyhuman));
                } else {
                    human.addProperty(new MaterialProperty((String) propertyName));
                }
            }
            return human;
        } catch (JSONException | NullPointerException e) {
            System.err.println("Can't parse Human\nInvalid syntax\nTry to use \"help\" for more information");
            return null;
        }
    }

    public Human helpParse(String str) {
        try {
            JSONObject jsonObject = (JSONObject) JSON.parse(str);
            Object name = jsonObject.get("name");
            Human human = new Human((String) name);
            try {
                Object cordX = jsonObject.get("cordX");
                Object cordY = jsonObject.get("cordY");
                human.setCords(Double.parseDouble(cordX.toString()), Double.parseDouble(cordY.toString()));
            } catch (java.lang.ClassCastException e) {
                System.err.println("cords must be digital");
            }
            Object condition = jsonObject.get("condition");
            human.setCondition(Condition.valueOf((String) condition));
            Object birhday = jsonObject.get("birthday");
            if(birhday != null)  human.setBirthday(new Date(Long.valueOf(birhday.toString())));
            else human.setBirthday(new Date());
            return human;
        } catch (JSONException e) {
            System.err.println("Can't parse Human\nInvalid syntax\nTry to use \"help\" for more information");
            return null;
        }
    }

    public void saveToFile() {
        String newpath = System.getenv(path);
        try (PrintWriter writer = new PrintWriter(newpath)) {
            writer.println("{\"Humans\" : [");
            boolean i = true;
            for (Map.Entry<String, Human> entry : collection.entrySet()) {
                if (i) {
                    i = false;
                } else {
                    writer.println(",");
                }
                writer.print(JSON.toJSONString(entry.getValue()));
            }
            writer.print("]}");
        } catch (FileNotFoundException e) {
            System.err.println("Can't save information in file\nFile not found");
        } catch (JSONException | NullPointerException e) {
            System.err.println("Can't save Human to file\nInvalid syntax");
        }
    }

    public void updateColl() {
        collection.clear();
        exportfromfile(path);
    }
    public void outCollection() {
        this.getCollection().forEach((s, human) -> {
            System.out.println(s + " = " + human.toString());
        });
    }
}
