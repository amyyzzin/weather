package zerobase.weather.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import zerobase.weather.domain.Diary;
import zerobase.weather.service.DiaryService;

@RestController
public class DiaryController {
	private final DiaryService diaryService;

	public DiaryController(DiaryService diaryService) {
		this.diaryService = diaryService;
	}

	@ApiOperation(value = "일기 텍스트와 날씨를 이용해서 DB에 일기 저장합니다"
		, notes = "해당 기능에 대해 상세히 설명할 수 있는 노트입니다.")
	@PostMapping("/create/diary")
	void createDiary(
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		@ApiParam(value = "일기를 작성할 날", example = "2022-01-01") LocalDate date,
		@RequestBody String text) {
		diaryService.createDiary(date, text);
	}

	@ApiOperation("선택한 날짜의 모든 일기 데이터를 가져옵니다.")
	@GetMapping("/read/diary")
	List<Diary> readDiary(
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		@ApiParam(value = "조회하고 싶은 날", example = "2022-01-01") LocalDate date) {
		return diaryService.readDiary(date);
	}

	@ApiOperation("선택한 기간중의 모든 일기 데이터를 가져옵니다.")
	@GetMapping("/read/diaries")
	List<Diary> readDiaries(
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		@ApiParam(value = "조회할 기간의 첫번째 날", example = "2022-01-01") LocalDate startDate,
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		@ApiParam(value = "조회할 기간의 마지막 날", example = "2022-01-01") LocalDate endDate) {

		return diaryService.readDiaries(startDate, endDate);
	}

	@ApiOperation("선택한 날짜의 일기 데이터를 수정합니다.")
	@PutMapping("/update/diary")
	void updateDairy(
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		@ApiParam(value = "일기를 수정할 날", example = "2022-01-01") LocalDate date,
		@RequestBody String text) {
		diaryService.updateDiary(date, text);
	}

	@ApiOperation("선택한 날짜의 일기 데이터를 전부 삭제합니다.")
	@DeleteMapping("/delete/diary")
	void deleteDiary(
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		@ApiParam(value = "일기를 삭제할 날", example = "2022-01-01") LocalDate date) {
		diaryService.deleteDiary(date);
	}
}
