package guru.springframework.springaiintro.services;

import guru.springframework.springaiintro.model.ChargingHistory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

@Service
public class ChargingDataService {

    private static final Random random = new Random();

    public List<ChargingHistory> getChargingHistoryByUser(String userId) {
        return generateRandomHistory(userId, 40); // 예시: 10건
    }

    private List<ChargingHistory> generateRandomHistory(String userId, int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> {
                    // 6월~7월 사이 무작위 날짜
                    int month = 6 + random.nextInt(2); // 6 or 7
                    int day = 1 + random.nextInt(28);  // 1~28일
                    LocalDate date = LocalDate.of(2025, month, day);

                    // 무작위 충전량 5.0 ~ 30.0 kWh
                    double kwh = 5 + (25 * random.nextDouble());

                    return new ChargingHistory(userId, date.toString(), Math.round(kwh * 10.0) / 10.0);
                })
                .toList();
    }
}

