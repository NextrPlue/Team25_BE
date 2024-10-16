package com.team25.backend.service;

import com.team25.backend.dto.request.CancelRequest;
import com.team25.backend.dto.request.ReservationRequest;
import com.team25.backend.dto.response.ReservationResponse;
import com.team25.backend.entity.Reservation;
import com.team25.backend.enumdomain.CancelReason;
import com.team25.backend.enumdomain.ReservationStatus;
import com.team25.backend.repository.ReservationRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    // 예약 작성
    public ReservationResponse createReservation(ReservationRequest reservationRequest) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime reservationDateTime = LocalDateTime.parse(
                reservationRequest.reservationDateTime(), formatter);
            Reservation reservation = Reservation.builder()
                .departureLocation(reservationRequest.departureLocation())
                .arrivalLocation(reservationRequest.arrivalLocation())
                .reservationDateTime(reservationDateTime)
                .serviceType(reservationRequest.serviceType())
                .transportation(reservationRequest.transportation()).price(Integer.parseInt(
                    reservationRequest.price()))
                .createdTime(LocalDateTime.now()).reservationStatus(ReservationStatus.CONFIRMED)
                .build();
            reservationRepository.save(reservation);
            return new ReservationResponse(reservation.getDepartureLocation(),
                reservation.getArrivalLocation(), reservation.getReservationDateTime().toString(),
                reservation.getServiceType(), reservation.getTransportation(),
                Integer.toString(reservation.getPrice()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("보호자 전화번호를 반드시 입력해야 합니다.");
        }
    }

    // 예약 취소
    public ReservationResponse cancelReservation(Long reservationId, CancelRequest cancelRequest) {
        // 해당 reservationDTO를 통해 특정 예약을 어떻게 하면 잡아낼 수 있는가?
        // checkDetailIsNull(cancelDto); // cancelDto에 상세 사유 없으면 예외 처리
        Reservation canceledReservation = reservationRepository.findById(
                reservationId) // reservationId로 예약 데이터 찾기
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));
        CancelReason cancelReason = Arrays.stream(CancelReason.values()) // 해당 취소 이유를 Enum 타입에서 선별
            .filter(reason -> reason.getKrName().equals(cancelRequest.cancelReason())).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 취소 타입입니다."));
        addCancelReasonAndDetail(canceledReservation, cancelReason,
            cancelRequest.cancelDetail()); // 예약에 취소 사유와 상세 정보 추가
        reservationRepository.save(canceledReservation);
        return new ReservationResponse(canceledReservation.getDepartureLocation(),
            canceledReservation.getArrivalLocation(), canceledReservation.getReservationDateTime().toString(),
            canceledReservation.getServiceType(), canceledReservation.getTransportation(),
            Integer.toString(canceledReservation.getPrice()));
    }

    private static void addCancelReasonAndDetail(Reservation canceledReservation,
        CancelReason cancelReason, String cancelDetail) {
        canceledReservation.setCancelReason(cancelReason);
        canceledReservation.setCancelDetail(cancelDetail);
    }

    private static void checkDetailIsNull(CancelRequest cancelRequest) {
        if (cancelRequest.cancelDetail().isBlank()) {
            throw new IllegalArgumentException("변심 이유를 반드시 선택해야 합니다.");
        }
    }
}
