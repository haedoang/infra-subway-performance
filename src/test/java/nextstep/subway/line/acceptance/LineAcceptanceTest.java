package nextstep.subway.line.acceptance;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.station.StationAcceptanceTest;
import nextstep.subway.station.dto.StationResponse;
import nextstep.subway.utils.ExpectedPageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지하철 노선 관련 기능")
public class LineAcceptanceTest extends AcceptanceTest {
    private StationResponse 강남역;
    private StationResponse 광교역;
    private Map<String, String> lineCreateParams;
    private static final int TOTAL_ELEMENTS = 100;

    @BeforeEach
    public void setUp() {
        super.setUp();

        // given
        강남역 = StationAcceptanceTest.지하철역_등록되어_있음("강남역").as(StationResponse.class);
        광교역 = StationAcceptanceTest.지하철역_등록되어_있음("광교역").as(StationResponse.class);

        lineCreateParams = new HashMap<>();
        lineCreateParams.put("name", "신분당선");
        lineCreateParams.put("color", "bg-red-600");
        lineCreateParams.put("upStationId", 강남역.getId() + "");
        lineCreateParams.put("downStationId", 광교역.getId() + "");
        lineCreateParams.put("distance", 10 + "");
    }

    @DisplayName("지하철 노선을 생성한다.")
    @Test
    void createLine() {
        // when
        ExtractableResponse<Response> response = 지하철_노선_생성_요청(lineCreateParams);

        // then
        지하철_노선_생성됨(response);
    }

    @DisplayName("기존에 존재하는 지하철 노선 이름으로 지하철 노선을 생성한다.")
    @Test
    void createLineWithDuplicateName() {
        // given
        지하철_노선_등록되어_있음(lineCreateParams);

        // when
        ExtractableResponse<Response> response = 지하철_노선_생성_요청(lineCreateParams);

        // then
        지하철_노선_생성_실패됨(response);
    }

    @DisplayName("지하철 노선 목록을 조회한다.")
    @Test
    void getLines() {
        // given
        Map<String, String> params = new HashMap<>();
        params.put("name", "구분당선");
        params.put("color", "bg-red-600");
        params.put("upStationId", 강남역.getId() + "");
        params.put("downStationId", 광교역.getId() + "");
        params.put("distance", 15 + "");
        ExtractableResponse<Response> createResponse1 = 지하철_노선_등록되어_있음(params);
        ExtractableResponse<Response> createResponse2 = 지하철_노선_등록되어_있음(lineCreateParams);

        // when
        ExtractableResponse<Response> response = 지하철_노선_목록_조회_요청();

        // then
        지하철_노선_목록_응답됨(response);
        지하철_노선_목록_포함됨(response, Arrays.asList(createResponse1, createResponse2));
    }

    @Test
    @DisplayName("지하철 노선 페이지 목록을 조회한다.")
    public void getLinesPage() throws Exception {
        // given
        지하철_노선들이_등록되어_있음(TOTAL_ELEMENTS);
        PageRequest pageRequest = PageRequest.of(5, 8);


        // when
        ExtractableResponse<Response> response = 노선_페이지_요청함(pageRequest);

        // then
        ExpectedPageResult expected = new ExpectedPageResult(5, 8, 100);
        노선_페이지_응답됨(response, expected);
    }

    @Test
    @DisplayName("지하철 노선의 페이징 목록을 조회한다.")
    public void getLinesPageWithPk() throws Exception {
        // given
        지하철_노선들이_등록되어_있음(TOTAL_ELEMENTS);
        PageRequest pageRequest = PageRequest.of(0, 10);
        Long id = 25L;


        // when
        ExtractableResponse<Response> response = 노선_페이지_요청함(id, pageRequest);

        // then
        ExpectedPageResult expected = new ExpectedPageResult(0, 10, TOTAL_ELEMENTS);
        노선_페이지_응답됨(response, expected);
    }


    @DisplayName("지하철 노선을 조회한다.")
    @Test
    void getLine() {
        // given
        ExtractableResponse<Response> createResponse = 지하철_노선_등록되어_있음(lineCreateParams);

        // when
        ExtractableResponse<Response> response = 지하철_노선_조회_요청(createResponse);

        // then
        지하철_노선_응답됨(response, createResponse);
    }

    @DisplayName("지하철 노선을 수정한다.")
    @Test
    void updateLine() {
        // given
        String name = "신분당선";
        ExtractableResponse<Response> createResponse = 지하철_노선_등록되어_있음(lineCreateParams);

        // when
        Map<String, String> params = new HashMap<>();
        params.put("name", "구분당선");
        params.put("color", "bg-red-600");
        params.put("upStationId", 강남역.getId() + "");
        params.put("downStationId", 광교역.getId() + "");
        params.put("distance", 15 + "");
        ExtractableResponse<Response> response = 지하철_노선_수정_요청(createResponse, params);

        // then
        지하철_노선_수정됨(response);
    }

    @DisplayName("지하철 노선을 제거한다.")
    @Test
    void deleteLine() {
        // given
        ExtractableResponse<Response> createResponse = 지하철_노선_등록되어_있음(lineCreateParams);

        // when
        ExtractableResponse<Response> response = 지하철_노선_제거_요청(createResponse);

        // then
        지하철_노선_삭제됨(response);
    }

    public static ExtractableResponse<Response> 지하철_노선_등록되어_있음(Map<String, String> params) {
        return 지하철_노선_생성_요청(params);
    }

    private void 지하철_노선들이_등록되어_있음(int count) {
        IntStream.range(0, count)
                .forEach(index -> {
                    Map<String, String> params = new HashMap<>();
                    params.put("name", "line" + index);
                    params.put("color", "bg-red-600");
                    params.put("upStationId", 강남역.getId() + "");
                    params.put("downStationId", 광교역.getId() + "");
                    params.put("distance", index + "");

                    지하철_노선_생성_요청(params);
                });
    }

    public static ExtractableResponse<Response> 지하철_노선_생성_요청(Map<String, String> params) {
        return RestAssured.given().log().all().
                contentType(MediaType.APPLICATION_JSON_VALUE).
                body(params).
                when().
                post("/lines").
                then().
                log().all().
                extract();
    }

    public static ExtractableResponse<Response> 지하철_노선_목록_조회_요청() {
        return RestAssured.given().log().all().
                accept(MediaType.APPLICATION_JSON_VALUE).
                when().
                get("/lines").
                then().
                log().all().
                extract();
    }

    public static ExtractableResponse<Response> 지하철_노선_조회_요청(LineResponse response) {
        return RestAssured.given().log().all().
                accept(MediaType.APPLICATION_JSON_VALUE).
                when().
                get("/lines/{lineId}", response.getId()).
                then().
                log().all().
                extract();
    }

    private ExtractableResponse<Response> 노선_페이지_요청함(PageRequest pageRequest) {
        return RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .param("page", pageRequest.getPageNumber())
                .param("size", pageRequest.getPageSize())
                .when()
                .get("/lines/page")
                .then().log().all()
                .extract();
    }

    private ExtractableResponse<Response> 노선_페이지_요청함(Long id, PageRequest pageRequest) {
        return RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .params("id", id)
                .param("page", pageRequest.getPageNumber())
                .param("size", pageRequest.getPageSize())
                .when()
                .get("/lines/page")
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 지하철_노선_조회_요청(ExtractableResponse<Response> response) {
        String uri = response.header("Location");

        return RestAssured.given().log().all().
                accept(MediaType.APPLICATION_JSON_VALUE).
                when().
                get(uri).
                then().
                log().all().
                extract();
    }

    public static ExtractableResponse<Response> 지하철_노선_수정_요청(ExtractableResponse<Response> response, Map<String, String> params) {
        String uri = response.header("Location");

        return RestAssured.given().log().all().
                contentType(MediaType.APPLICATION_JSON_VALUE).
                body(params).
                when().
                put(uri).
                then().
                log().all().
                extract();
    }

    public static ExtractableResponse<Response> 지하철_노선_제거_요청(ExtractableResponse<Response> response) {
        String uri = response.header("Location");

        return RestAssured.given().log().all().
                when().
                delete(uri).
                then().
                log().all().
                extract();
    }

    public static void 지하철_노선_생성됨(ExtractableResponse response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.header("Location")).isNotBlank();
    }

    public static void 지하철_노선_생성_실패됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    public static void 지하철_노선_목록_응답됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    public static void 지하철_노선_응답됨(ExtractableResponse<Response> response, ExtractableResponse<Response> createdResponse) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.as(LineResponse.class)).isNotNull();
    }

    public static void 지하철_노선_목록_포함됨(ExtractableResponse<Response> response, List<ExtractableResponse<Response>> createdResponses) {
        List<Long> expectedLineIds = createdResponses.stream()
                .map(it -> Long.parseLong(it.header("Location").split("/")[2]))
                .collect(Collectors.toList());

        List<Long> resultLineIds = response.jsonPath().getList(".", LineResponse.class).stream()
                .map(LineResponse::getId)
                .collect(Collectors.toList());

        assertThat(resultLineIds).containsAll(expectedLineIds);
    }

    public static void 지하철_노선_수정됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    public static void 지하철_노선_삭제됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    private void 노선_페이지_응답됨(ExtractableResponse<Response> response, ExpectedPageResult result) {

        List<LineResponse> content = response.jsonPath().getList("content", LineResponse.class);
        Map<Object, Object> pageable = response.jsonPath().getMap("pageable");
        int totalElements = (int) response.jsonPath().get("totalElements");

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(content).as("페이지의 사이즈가 일치하지 않음").hasSize(result.getSize());
        assertThat((int) pageable.get("pageNumber")).as("페이지번호가 일치하지 않음").isEqualTo(result.getPageNumber());
        assertThat(totalElements).as("전체 row 수가 일치하지 않음").isEqualTo(result.getTotalSize());
    }


}
