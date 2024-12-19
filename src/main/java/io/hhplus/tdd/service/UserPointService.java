package io.hhplus.tdd.service;

import io.hhplus.tdd.point.UserPoint;

public interface UserPointService {

    public UserPoint selectDetailUserPoint(long id);

    //포인트 충전
    public UserPoint chargeUserPoint(long userId, long amount);
}
