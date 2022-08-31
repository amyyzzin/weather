package zerobase.weather.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import zerobase.weather.WeatherApplication;
import zerobase.weather.domain.DateWeather;
import zerobase.weather.domain.Diary;
import zerobase.weather.error.InvalidDate;
import zerobase.weather.repository.DateWeatherRepository;
import zerobase.weather.repository.DiaryRepository;

@Service
@Transactional
public class DiaryService {

	private final double KELVIN_TEMPERATURE = 273.16;

	@Value("${openWeatherMap.key}")
	private String apiKey;

	private final DiaryRepository diaryRepository;
	private final DateWeatherRepository dateWeatherRepository;

	//logback을 이용해 log작성
	private static final Logger logger = LoggerFactory.getLogger(
		WeatherApplication.class);

	public DiaryService(DiaryRepository diaryRepository,
		DateWeatherRepository dateWeatherRepository) {
		this.diaryRepository = diaryRepository;
		this.dateWeatherRepository = dateWeatherRepository;
	}

	//매일 새벽 1시에 날씨 데이터를 받아옴
	@Scheduled(cron = "0 0 1 * * *")
	@Transactional
	public void saveWeatherDate() {
		logger.info("오늘도 날씨 데이터 잘 가져옴");
		dateWeatherRepository.save(getWeatherFromApi());
	}

	@Transactional(isolation = Isolation.SERIALIZABLE)
	public void createDiary(LocalDate date, String text) {

		logger.info("started to create diary");

		//날씨 데이터 가져오기(API or DB)
		DateWeather dateWeather = getDateWeather(date);

		//파싱된 데이터 + 일기 값 우리 db에 넣기
		Diary nowDiary = new Diary();
		nowDiary.setText(text);
		nowDiary.setDate(date);
		nowDiary.setDateWeather(dateWeather);
		diaryRepository.save(nowDiary);
		logger.info("end to create diary");
		logger.error("ERROR!!!");
		logger.warn("WARNING!!");
	}

	private DateWeather getWeatherFromApi() {
		// openWeatherMap 에서 날씨 데이터 가져오기
		String weatherData = getWeatherString();

		// 날씨 Json 파싱하기
		Map<String, Object> parseWeather = parseWeather(weatherData);
		DateWeather dateWeather = new DateWeather();
		dateWeather.setDate(LocalDate.now());
		dateWeather.setWeather(parseWeather.get("main").toString());
		dateWeather.setIcon(parseWeather.get("icon").toString());
		dateWeather.setTemperature(new BigDecimal(Double.valueOf(parseWeather.get("temp").toString()) - KELVIN_TEMPERATURE)
			.setScale(2, RoundingMode.FLOOR)
			.doubleValue());

		return dateWeather;
	}

	private DateWeather getDateWeather(LocalDate date) {
		List<DateWeather> dateWeatherListFromDB = dateWeatherRepository.findAllByDate(
			date);
		if (dateWeatherListFromDB.size() == 0) {
			return getWeatherFromApi();
		} else {
			return dateWeatherListFromDB.get(0);
		}
	}

	@Transactional(readOnly = true)
	public List<Diary> readDiary(LocalDate date) {

		if (date.isAfter(LocalDate.ofYearDay(3050, 1))) {
			throw new InvalidDate();
		}
		return diaryRepository.findAllByDate(date);
	}

	@Transactional(readOnly = true)
	public List<Diary> readDiaries(LocalDate startDate, LocalDate endDate) {
		return diaryRepository.findAllByDateBetween(startDate, endDate);
	}

	public void updateDiary(LocalDate date, String text) {
		Diary nowDiary = diaryRepository.getFirstByDate(date);
		nowDiary.setText(text);
		diaryRepository.save(nowDiary);
	}


	public void deleteDiary(LocalDate date) {
		diaryRepository.deleteAllByDate(date);
	}

	// openWeatherMap 에서 날씨 데이터 가져오기
	private String getWeatherString() {
		String apiUrl =
			"https://api.openweathermap.org/data/2.5/weather?q=seoul&units=metric&appid="
				+ apiKey;

		try {
			URL url = new URL(apiUrl);
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("GET");
			int responseCode = connection.getResponseCode();
			BufferedReader br;

			if (responseCode == 200) {
				br = new BufferedReader(
					new InputStreamReader(connection.getInputStream()));
			} else {
				br = new BufferedReader(
					new InputStreamReader(connection.getErrorStream()));
			}
			String inputLine;
			StringBuilder response = new StringBuilder();
			while ((inputLine = br.readLine()) != null) {
				response.append(inputLine);
			}
			br.close();

			return response.toString();

		} catch (Exception e) {
			return "fail to get response";
		}
	}

	//받아온 날씨 Json 파싱하기
	private Map<String, Object> parseWeather(String jsonString) {
		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject;

		try {
			jsonObject = (JSONObject)jsonParser.parse(jsonString);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
		Map<String, Object> resultMap = new HashMap<>();

		JSONObject mainData = (JSONObject)jsonObject.get("main");
		resultMap.put("temp", mainData.get("temp"));
		JSONArray weatherArray = (JSONArray)jsonObject.get("weather");
		JSONObject weatherData = (JSONObject)weatherArray.get(0);
		resultMap.put("main", weatherData.get("main"));
		resultMap.put("icon", weatherData.get("icon"));

		return resultMap;
	}

}
