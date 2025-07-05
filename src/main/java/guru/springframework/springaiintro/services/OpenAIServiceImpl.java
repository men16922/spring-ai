package guru.springframework.springaiintro.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import guru.springframework.springaiintro.model.Answer;
import guru.springframework.springaiintro.model.ChargingHistory;
import guru.springframework.springaiintro.model.Question;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OpenAIServiceImpl implements OpenAIService {

    private final ChatModel chatModel;
    private final ChargingDataService chargingDataService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenAIServiceImpl(ChatModel chatModel, ChargingDataService chargingDataService) {
        this.chatModel = chatModel;
        this.chargingDataService = chargingDataService;
    }

    @Override
    public Answer getAnswer(Question question) {
        String query = question.question();
        String response;

        if (isChargingStatsQueryLLM(query)) {
            response = processChargingStatsQuery(query);  //
        } else {
            Prompt prompt = new Prompt(query);
            ChatResponse chatResponse = chatModel.call(prompt);
            response = chatResponse.getResult().getOutput().getText();
        }

        return new Answer(response);
    }

    @Override
    public String getAnswer(String question) {
        return getAnswer(new Question(question)).answer();
    }

    private boolean isChargingStatsQueryLLM(String query) {
        String promptText = String.format("""
                다음 문장이 전기차 충전 통계에 대한 질문인지 판단해줘. 맞으면 'YES', 아니면 'NO'만 답해줘:
                "%s"
                """, query);
        ChatResponse response = chatModel.call(new Prompt(promptText));
        return response.getResult().getOutput().getText().trim().equalsIgnoreCase("YES");
    }

    /**
     * 충전 통계 데이터를 JSON 구조로 반환
     */
    private String processChargingStatsQuery(String userQuery) {
        // 1. 사용자 이름 추출
        String extractUserPrompt = String.format("""
                다음 문장에서 사용자 이름만 정확하게 추출해줘. 사용자 이름만 출력해: "%s"
                """, userQuery);
        ChatResponse extractResponse = chatModel.call(new Prompt(extractUserPrompt));
        String userId = extractResponse.getResult().getOutput().getText().trim();

        // 2. 충전 이력 조회
        List<ChargingHistory> historyList = chargingDataService.getChargingHistoryByUser(userId);
        if (historyList.isEmpty()) {
            return String.format("{\"message\": \"해당 사용자의 충전 이력이 없습니다: %s\"}", userId);
        }

        // 3. 통계 계산
        double totalKwh = historyList.stream().mapToDouble(ChargingHistory::kwh).sum();
        int count = historyList.size();
        double avgKwh = Math.round((totalKwh / count) * 10.0) / 10.0;

        // 4. 자연어 요약 생성
        StringBuilder summary = new StringBuilder();
        summary.append("사용자 ").append(userId).append("의 충전 이력 요약:\n");
        summary.append("- 총 충전량: ").append(String.format("%.1f", totalKwh)).append(" kWh\n");
        summary.append("- 충전 횟수: ").append(count).append("회\n");
        summary.append("- 평균 충전량: ").append(String.format("%.1f", avgKwh)).append(" kWh");
        summary.append("- 평균 충전량: ").append(String.format("%.1f", avgKwh)).append(" kWh");

        // 5. JSON 구조 생성
        ObjectNode result = objectMapper.createObjectNode();
        result.put("userId", userId);
        result.put("totalKwh", totalKwh);
        result.put("averageKwh", avgKwh);
        result.put("count", count);

        ArrayNode historyArray = objectMapper.createArrayNode();
        for (ChargingHistory h : historyList) {
            ObjectNode record = objectMapper.createObjectNode();
            record.put("date", h.date());
            record.put("kwh", h.kwh());
            historyArray.add(record);
        }
        result.set("history", historyArray);

        // 6. 자연어 + 줄 바꿈 + pretty JSON 반환
        return summary.toString() + "\n\n" + result.toPrettyString();
    }
}
