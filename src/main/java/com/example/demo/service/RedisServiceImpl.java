package com.example.demo.service;

import com.example.demo.dao.RedisDao;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

@Service
public class RedisServiceImpl implements RedisService {

    @Autowired
    RedisDao redisDao;

    public static final String SEP = "____";
    public static final String TYPE = "objectType";
    public static final String ID = "objectId";

    @Autowired
    public RedisServiceImpl() {
    }

    @Override
    public JSONObject getPlan(String key) {
        JSONObject object = new JSONObject();

//        Set<String> keys = redisDao.getKeys("*");
//        for (String k : keys){
//            try {
//                Map<Object, Object> map = redisDao.findMap(k);
//                System.out.println(k + " contains: " + map);
//            }
//            catch (Exception e) {
//                Set set = redisDao.findSet(k);
//                System.out.println(k + " contains: " + set);
//            }
//            //redisDao.delete(k);
//        }
//        return object;

        Set<String> keys = redisDao.getKeys("*" + key + "*");
//        System.out.println("key: " + key + " keys: " + keys);
        keys.remove(key);
        Map<Object, Object> map = redisDao.findMap(key);
        loadMap(object, map);

        for (String edgeKey: keys) {
            try {
                Set subObjs = redisDao.findSet(edgeKey);
                String attribute = edgeKey.split(SEP)[2];
                if (subObjs.size() == 1) {
                    String subKey = null;
                    for (Object str: subObjs) {
                        subKey = (String)str;
                    }
                    object.put(attribute, getPlan(subKey));
                } else {
                    JSONArray array = new JSONArray();
                    object.put(attribute, array);
                    for (Object str: subObjs) {
                        String subKey = (String)str;
                        array.put(getPlan(subKey));
                    }
                }
            } catch (Exception e){
//                keys.remove(edgeKey);
            }
        }

        return object;
    }

    //reconstruct the JSONObject
    private void loadMap(JSONObject object, Map<Object, Object> map) {
        for(Map.Entry<Object, Object> entry : map.entrySet()) {
            String val = (String) entry.getValue();
            try {
                object.put((String)entry.getKey(), Integer.parseInt(val));
            }
            catch(Exception e) {
                object.put((String)entry.getKey(), val);
            }
//            if (entry.getValue() instanceof String) {
//                String val = (String) entry.getValue();
//                object.put((String)entry.getKey(), val);
//            } else {
//                Integer val = (Integer) entry.getValue();
//                object.put((String)entry.getKey(), val);
//            }

        }
    }

    @Override
    public void postPlan(JSONObject object) {
        //BFS
        Queue<JSONObject> queue = new LinkedList<>();
        queue.add(object);
        while (!queue.isEmpty()) {
            JSONObject cur = queue.poll();
            String objectKey = cur.getString(TYPE) + SEP + cur.getString(ID); //e.g.508_plan

            for (String attribute : cur.keySet()) {
                Object obj = cur.get(attribute);
                if (obj instanceof JSONObject) {
                    JSONObject subObj = (JSONObject)obj;
                    String edgeKey = objectKey + SEP + attribute;  //e.g.508_plan_planCostShares
                    String subObjKey = subObj.getString(TYPE) + SEP + subObj.getString(ID);
                    redisDao.insertSet(edgeKey, subObjKey);
                    queue.offer((JSONObject)obj);
                }
                else if (obj instanceof JSONArray) {
                    String edgeKey = objectKey + SEP + attribute;  //e.g.508_plan_linkedPlanServices
                    for (int i = 0; i < ((JSONArray)obj).length(); i++) {
                        JSONObject subObj = ((JSONArray)obj).getJSONObject(i);
                        String subObjKey = subObj.getString(TYPE) + SEP + subObj.getString(ID);
                        redisDao.insertSet(edgeKey, subObjKey);
                        queue.offer(subObj);
                    }
                }
                else {
                    redisDao.insertMap(objectKey, attribute, obj);
                }
            }

        }
    }

    @Override
    public void updatePlan(String key, String value) {

    }

    @Override
    public void patchPlan(String key, JSONObject newObject) {
        for (String attribute : newObject.keySet()) {
            Object obj = newObject.get(attribute);
            String edgeKey = key + SEP + attribute;
            if (obj instanceof JSONObject) {
                JSONObject subObj = (JSONObject)obj;
                String subObjKey = subObj.getString(TYPE) + SEP + subObj.getString(ID);

                Map<Object, Object> map = redisDao.findMap(subObjKey);
                if (map == null || map.size() == 0) {
                    redisDao.insertSet(edgeKey, subObjKey);
                    postPlan(subObj);
                } else {
                    deletePlan(subObjKey);
                    postPlan(subObj);
                }
            }
            else if (obj instanceof JSONArray) {
                for (int i = 0; i < ((JSONArray)obj).length(); i++) {
                    JSONObject subObj = ((JSONArray)obj).getJSONObject(i);
                    String subObjKey = subObj.getString(TYPE) + SEP + subObj.getString(ID);

                    Map<Object, Object> map = redisDao.findMap(subObjKey);
                    if (map == null || map.size() == 0) {
                        redisDao.insertSet(edgeKey, subObjKey);
                        postPlan(subObj);
                    } else {
                        deletePlan(subObjKey);
                        postPlan(subObj);
                    }
                }
            }
        }
    }

    @Override
    public JSONObject deletePlan(String key) {
        JSONObject object = new JSONObject();
        Set<String> keys = redisDao.getKeys("*" + key + "*");
        keys.remove(key);
        Map<Object, Object> map = redisDao.findMap(key);
        loadMap(object, map);
        redisDao.delete(key);

        for (String edgeKey: keys) {
            try {
                Set subObjs = redisDao.findSet(edgeKey);
                redisDao.delete(edgeKey);
                String attribute = edgeKey.split(SEP)[2];
                if (subObjs.size() == 1) {
                    String subKey = null;
                    for(Object str: subObjs) {
                        subKey = (String)str;
                    }
                    object.put(attribute, deletePlan(subKey));
                } else {
                    JSONArray array = new JSONArray();
                    object.put(attribute, array);
                    for (Object str: subObjs) {
                        String subKey = (String)str;
                        array.put(deletePlan(subKey));
                    }
                }
            } catch (Exception e) {
//                keys.remove(edgeKey);
            }
        }

        return object;
    }

    @Override
    public boolean validate(JSONObject jo) {
        return false;
    }

    @Override
    public boolean exist(String key) {
        Map map = redisDao.findMap(key);
        if(map == null || map.size() == 0)
            return false;
        return true;
    }

    @Override
    public void enqueue(String key, JSONObject jo, String requestType) {

    }

    @Override
    public void enqueue(String key) {

    }
}
