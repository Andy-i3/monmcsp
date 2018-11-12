/*
 * Используется для мониторинга работоспособности IP камер
 * и проверки записи Архива с помощью API Macroscop
 * Данные выгружаются в систему мониторинга Zabbix
 * через zabbix_sender.exe
 * @author Golubkov Andrey
 * @since 1.0
 * @param JSON запрос к API Macropscop
 * @version 1.01
 * @return Формат для zabbix_sender
 */

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

@SuppressWarnings("ALL")
public class Qdata {

	// @param Чтение переменной LIST_PARAM_CAM (запрос JSON) из конфигурационного файла macroscop.cfg
	// @param Чтение переменной NAME_SERVER_CAM (имя сервера) из конфигурационного файла macroscop.cfg
	private static String LIST_PARAM_CAM;
	private static String NAME_SERVER_CAM;
	private static String CONFIG_FILE;

	public Qdata() throws IOException {
		Properties props = new Properties();
		props.load( new FileInputStream( new File( CONFIG_FILE ) ) );
		LIST_PARAM_CAM = props.getProperty( "LIST_PARAM_CAM" );
		NAME_SERVER_CAM = props.getProperty( "NAME_SERVER_CAM" );
	}

	public static void main(String[] args) throws Exception {

		if (args.length>0)  { CONFIG_FILE=args[0]; }
				else { CONFIG_FILE=".\\config\\macroscop.cfg"; }
			new Qdata();
		URL url = createUrl( LIST_PARAM_CAM );
		// JSON запрос к API Macropscop
		String file = parseCurrentJson( parseUrl( url ) );
		System.out.print( file );
		// FileSave(file);

	}

	private static URL createUrl(String link) {
		try {
			return new URL( link );
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Запись данных в файл
	 */
	public static void filesave(String text) {

		try (FileWriter writer = new FileWriter( "notes3.txt", false )) {
			writer.write( text );
			writer.flush();
		} catch (IOException ex) {
			System.out.println( ex.getMessage() );
		}
	}

	/** построчно считываем результат в объект StringBuilder
	 * Добовляем тег data_json
	 * Пропускаем первые четыре строки, а остальные считываем в буфер
	 */

	private static String parseUrl(URL url) {
		if (url == null) return "";
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append( "{" );
		stringBuilder.append( "\"data_json\":\n" );
		String inputLine;
		try (BufferedReader in = new BufferedReader( new InputStreamReader( url.openStream() ) )) {
			int i = 0;

			while ((inputLine = in.readLine()) != null) {
				i++;
				if (i > 4 )stringBuilder.append( inputLine );
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		stringBuilder.append( "}" );
		return stringBuilder.toString();
	}

	// Парсим массив данных и выводим на экран
	public static String parseCurrentJson(String resultJson) {
		String resultToFile = "";
		try {
			JSONObject jsonObject = (JSONObject) JSONValue.parseWithException( resultJson );
			JSONArray ipcamArray0 = (JSONArray) jsonObject.get( "data_json" );
			for (int i = 0; i < ipcamArray0.size(); i++) {
				JSONObject ipcamData = (JSONObject) ipcamArray0.get( i );
				JSONArray ipcamArray1 = (JSONArray) ipcamData.get( "StreamsStates" );
				JSONObject ipcamData2 = (JSONObject) ipcamArray1.get( 0 );
				int f1 = (ipcamData2.get( "State" ).equals( "Active" )) ? 1 : 0;
				int f2 = (ipcamData.get( "IsRecordingOn" ).equals( "true" )) ? 0 : 1;
				resultToFile += NAME_SERVER_CAM + "   " + ipcamData.get( "Id" ) + "   " + f1 + "\n";
				resultToFile += NAME_SERVER_CAM + "   ARH" + ipcamData.get( "Id" ) + "   " + f2 + "\n";
			}
		} catch (org.json.simple.parser.ParseException e) {
			e.printStackTrace();
		}
		return resultToFile;
	}
}

