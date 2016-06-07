package com.whaley.hprof;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CmdProceManager {
	public static void createHprof(String ip,String appName,String hprofPath){
		String path = "d:/createHprof.bat";
		ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "call", "\"" + path + "\"", ip,appName,hprofPath);
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(builder.start().getInputStream()));
			String line;

			while ((line=br.readLine())!=null) {

			   System.out.println(line);

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
