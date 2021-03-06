package br.com.devpaulo.legendchat.updater;

import br.com.devpaulo.legendchat.Main;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import br.com.devpaulo.legendchat.api.Legendchat;
import br.com.devpaulo.legendchat.messages.MessageManager;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Updater {

	private String version = "";

	public Updater(String v) {
		version = v;
	}

	public Updater() {
	}
	
	public String CheckNewVersion() throws Exception {
		String v = "";
		URL url = new URL("https://api.curseforge.com/servermods/files?projectIds=74494");
		URLConnection conn = url.openConnection();
		conn.setConnectTimeout(10000);
		conn.setReadTimeout(10000);
		conn.addRequestProperty("User-Agent", "Legendchat (by PauloABR)");
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		} catch (Exception e) {
			throw new Exception();
		}
		String response = reader.readLine();
		JSONArray array = (JSONArray) JSONValue.parse(response);
		if (array.size() > 0) {
			JSONObject latest = (JSONObject) array.get(array.size() - 1);
			v = ((String) latest.get("name")).split("\\(")[1].split("\\)")[0].replace("V", "");
		} else {
			return null;
		}
		boolean f = false;
		if (!version.equals(v)) {
			String[] v_obtained = v.split("\\.");
			String[] v_here = version.split("\\.");
			
			boolean draw = true;
			for (int i = 0; i < (v_obtained.length > v_here.length ? v_here.length : v_obtained.length); i++) {
				int n_obtained = Integer.parseInt(v_obtained[i]);
				int n_here = Integer.parseInt(v_here[i]);
				
				if (n_obtained > n_here) {
					f = true;
					break;
				}
				if (n_obtained < n_here) {
					draw = false;
					break;
				}
			}
			
			if (draw && v_obtained.length > v_here.length) {
				f = true;
		}
		}
		
		String r = (f ? v : null);
		return r;
	}
	
	private boolean updConfig = false;
	private Plugin plugin = Bukkit.getPluginManager().getPlugin("Legendchat");

	public boolean updateConfig() {
		YamlConfiguration c;
		try {
			InputStreamReader is = new InputStreamReader(plugin.getResource(("config_template.yml").replace('\\', '/')));
			c = YamlConfiguration.loadConfiguration(is);
		} catch (NoSuchMethodError nsme) {
			try {
				Method loadConfigurationMethod = YamlConfiguration.class.getMethod("loadConfiguration", InputStream.class);
		InputStream is = plugin.getResource(("config_template.yml").replace('\\', '/'));
				c = (YamlConfiguration) loadConfigurationMethod.invoke(null, is);
			} catch (Exception ex) {
				Logger.getLogger(Updater.class.getName()).log(Level.SEVERE, "Updater Error", ex);
				return false;
			}
		}
		for (String n : c.getConfigurationSection("").getKeys(true)) {
			if (!has(n)) {
				set(n, c.get(n));
			}
		}
		if (updConfig) {
			plugin.saveConfig();
		}
		return updConfig;
	}
	
	private boolean has(String s) {
		return plugin.getConfig().contains(s);
	}
	
	private void set(String s, Object obj) {
		plugin.getConfig().set(s, obj);
		updConfig = true;
	}
	
	private boolean updLang = false;

	public boolean updateAndLoadLanguage(String language) {
		File f = new File(plugin.getDataFolder(), "language" + File.separator + "language_" + language + ".yml");
		MessageManager m = Legendchat.getMessageManager();
		m.registerLanguageFile(f);
		m.loadMessages(f);
		YamlConfiguration c;
		try {
			InputStreamReader is;
			if ((is = new InputStreamReader(plugin.getResource(("language" + File.separator + "language_" + language + ".yml").replace('\\', '/')))) == null) {
				is = new InputStreamReader(plugin.getResource(("language" + File.separator + "language_en.yml").replace('\\', '/')));
			}
			c = YamlConfiguration.loadConfiguration(is);
		} catch (NoSuchMethodError nsme) {
			try {
				Method loadConfigurationMethod = YamlConfiguration.class.getMethod("loadConfiguration", InputStream.class);
				InputStream is;
				if ((is = plugin.getResource(("language" + File.separator + "language_" + language + ".yml").replace('\\', '/'))) == null) {
					is = plugin.getResource(("language" + File.separator + "language_en.yml").replace('\\', '/'));
				}
				c = (YamlConfiguration) loadConfigurationMethod.invoke(null, is);
			} catch (Exception ex) {
				Logger.getLogger(Updater.class.getName()).log(Level.SEVERE, "Updater Error", ex);
				return false;
			}
		}
		for (String n : c.getConfigurationSection("").getKeys(false)) {
			if (!m.hasMessage(n)) {
				addMessage(m, n, c.getString(n));
			}
		}
		if (updLang) {
			m.loadMessages(f);
		}
		return updLang;
	}
	
	private void addMessage(MessageManager m, String name, String msg) {
		m.addMessageToFile(name, msg);
		updLang = true;
	}
	
	public boolean updateChannels() {
		boolean upd = false;
		for (File channel : new File(Bukkit.getPluginManager().getPlugin("Legendchat").getDataFolder(), "channels").listFiles()) {
			YamlConfiguration channel2 = YamlConfiguration.loadConfiguration(channel);
			if (!channel2.contains("needFocus")) {
				channel2.set("needFocus", false);
				upd = true;
			}
			try {
				channel2.save(channel);
			} catch (IOException e) {
		}
		}
		return upd;
	}
		
		public boolean loadPlayerColors() {
				boolean loaded = false;
				File playerColors = new File(Bukkit.getPluginManager().getPlugin("LegendChat").getDataFolder(),"playerColors.yml");
				if (playerColors.exists()) {
						YamlConfiguration playerColorsConf = YamlConfiguration.loadConfiguration(playerColors);
						for (String key : playerColorsConf.getConfigurationSection("Players").getKeys(false)) {
								Main.colorsPerPlayer.put(key, playerColorsConf.getString(key));
						}
						playerColors.delete();
						loaded = true;
				}
				Bukkit.getLogger().info("Loaded PlayerColorsConf");
				return loaded;
		}
		public boolean savePlayerColors() {
				boolean saved = false;
				if (!(Main.colorsPerPlayer.isEmpty())){
						File playerColors = new File(Bukkit.getPluginManager().getPlugin("LegendChat").getDataFolder(),"playerColors.yml");
						YamlConfiguration playerColorsConf = new YamlConfiguration();
						playerColorsConf.createSection("Players");
						for (String key : Main.colorsPerPlayer.keySet()) {
								playerColorsConf.set("Players." + key, Main.colorsPerPlayer.get(key));
						}
				}
				Bukkit.getLogger().info("Saved PlayerColorsConf");
				return saved;
		}
}
