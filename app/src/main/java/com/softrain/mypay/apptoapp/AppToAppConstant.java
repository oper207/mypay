package com.softrain.mypay.apptoapp;

// pos와 agent사이의 통신에서 사용되는 메시지 종류를 정의한 곳
public interface AppToAppConstant {
    // POS 에서 대리인(Agent)로 요청
    // POS에서 Agent로 데이터를 요청할 때 사용
    public final static int REQUEST_DATA = 1;
    // POS에서 Agent와의 연결을 끊을 때 사용
    public final static int DISCONNETCLIENTA = 2;
    // POS에서 Agent로 테스트를 요청할 때 사용
    public final static int REQUEST_TEST = 3;
    // POS에서 Agent의 카드 리더기 작업을 취소할 때 사용
    public final static int READER_CANCEL = 4;
    // POS에서 Agent의 서명 패드 작업을 취소할 때 사용
    public final static int SIGNPAD_CANCEL = 5;

    // Agent에서 POS로 응답
    public final static int RESPONSE_DATA = 101;

    // 인증
    public final static int CHECK_ID = 1001;
}
