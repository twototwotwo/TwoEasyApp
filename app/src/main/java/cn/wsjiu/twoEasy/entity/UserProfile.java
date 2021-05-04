package cn.wsjiu.twoEasy.entity;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 用户画像，描述用户的兴趣
 */
public class UserProfile {
    public final static float DEFAULT_WEIGHT = 0.1f;
    private final int KEY_WORD_LENGTH = 15;
    private Map<String, KeyWord> keyWordMap = new TreeMap<>();
    private KeyWord head;
    private KeyWord tail;

    public void init(JSONObject keyWordJSONObject) {
        if(keyWordJSONObject == null) return;
        for (String key : keyWordJSONObject.keySet()) {
            KeyWord keyWord = keyWordJSONObject.getObject(key, KeyWord.class);
            addKeyWord(keyWord.word, keyWord.weight);
        }
    }

    public void addKeyWord(String word, float weight) {
        KeyWord keyWord;
        if(keyWordMap.containsKey(word)) {
            keyWord = keyWordMap.get(word);
            keyWord.weight += weight;
        }else {
            keyWord = new KeyWord();
            keyWord.word = word;
            keyWord.weight = weight;
            keyWordMap.put(word, keyWord);
            if(tail == null) {
                head = keyWord;
            }else {
                tail.next = keyWord;
                keyWord.last = tail;
            }
            tail = keyWord;
        }
        KeyWord last = keyWord.last;
        while (last != null && last.weight <= keyWord.weight) {
            last.next = keyWord.next;
            if(keyWord.next != null) {
                keyWord.next.last = last;
            }else {
                tail = last;
            }
            keyWord.next = last;
            if(last.last != null) {
                last.last.next = keyWord;
            }else {
                head = keyWord;
            }
            keyWord.last = last.last;
            last.last = keyWord;
            last = keyWord.last;
        }
    }

    public Map<String, KeyWord> getKeyWordMap() {
        return keyWordMap;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder("");
        KeyWord keyWord = head;
        for (int i = 0; i < KEY_WORD_LENGTH; i++) {
            if(keyWord != null) {
                res.append(keyWord.word + " ");
                keyWord = keyWord.next;
            }else {
                break;
            }
        }
        return res.toString();
    }

    public static class KeyWord {
        public String word;
        public float weight;
        public transient KeyWord next;
        public transient KeyWord last;
    }
}
