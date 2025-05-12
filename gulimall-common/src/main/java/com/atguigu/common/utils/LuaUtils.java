package com.atguigu.common.utils;

import java.io.BufferedReader;
import java.io.IOException;

public class LuaUtils {
   public static String getLua(String luaPath) throws IOException {
       BufferedReader br = new BufferedReader(new java.io.InputStreamReader(LuaUtils.class.getClassLoader().getResourceAsStream(luaPath)));
       StringBuilder sb = new StringBuilder();
       String line = null;
       while ((line = br.readLine()) != null) {
           sb.append(line);
       }
       br.close();
       return sb.toString();
   }
}
