package net.ewant.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by admin on 2018/12/13.
 */
public abstract class MessageCommandUtils {

    public interface FriendCmd{
        int add = 1000;
        int accept = 1001;
        int refuse = 1002;
        int delete = 1003;
    }

    public static Command buildCommond(int type){
        return new Command(type);
    }
    public static class Command{
        private int t;
        private Map<String, Object> d;
        public Command(int type){
            this.t = type;
        }
        public Command setData(String key, Object value){
            if(key != null && value != null){
                if(d == null){
                    d = new HashMap<>();
                }
                d.put(key, value);
            }
            return this;
        }

        public int getT() {
            return t;
        }
        public Map<String, Object> getD() {
            return d;
        }
    }
}
