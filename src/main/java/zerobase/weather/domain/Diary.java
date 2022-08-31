package zerobase.weather.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class Diary {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String weather;
	private String icon;
	private double temperature;

	private String text;
	private LocalDate date;

	public void setDateWeather(DateWeather dateWeather) {
		this.date = dateWeather.getDate();
		this.weather = dateWeather.getWeather();
		this.icon = dateWeather.getIcon();
		this.temperature = new BigDecimal(dateWeather.getTemperature() - 273.16)
			.setScale(2, RoundingMode.FLOOR)
			.doubleValue();
	}
}
